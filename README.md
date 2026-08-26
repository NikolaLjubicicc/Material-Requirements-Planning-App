# Smart MRP

Sistem za planiranje materijalnih potreba (Material Requirements Planning). Ovo je projekat koji simulira jedan realan proces koji se koristi u proizvodnim firmama: kako da na osnovu plana proizvodnje i trenutnih zaliha automatski izračunaš šta treba naručiti od dobavljača, a šta treba proizvesti u sopstvenoj fabrici, i kada tačno treba krenuti sa tim da bi se stiglo do roka.

Aplikacija ima dva dela:

- **Backend** (folder `smart-mrp`) - Spring Boot aplikacija koja radi svu logiku i čuva podatke u bazi.
- **Frontend** (folder `Frontend`) - Angular aplikacija koja služi kao korisnički interfejs preko koga se sve to koristi u browseru.

Ova dva dela pričaju preko REST API-ja (obično JSON preko HTTP-a).

---

## 1. Šta MRP zapravo radi (kratko objašnjenje za nekog ko nije iz proizvodnje)

Zamisli da praviš stolove. Da bi napravio jedan sto, treba ti npr. 1 okvir i 4 noge. Da bi napravio okvir, treba ti 2 čelične ploče i 1 set vijaka. To je **sastavnica** (BOM - Bill of Materials), i to je u suštini stablo: proizvod se sastoji od komponenti, a komponente se mogu sastojati od svojih komponenti.

Kad dobiješ narudžbinu "napravi 5 stolova do 1. marta", MRP algoritam radi tri stvari:

1. **Eksplozija BOM-a (Explosion)** - rekurzivno silazi kroz sastavnicu i računa bruto potrebe. Za 5 stolova treba mi 5 okvira i 20 nogu. Za 5 okvira treba mi 10 čeličnih ploča i 5 setova vijaka.
2. **Netiranje (Netting)** - od bruto potrebe se oduzima ono što već imam na stanju (iznad sigurnosne zalihe koju uvek želim da čuvam u rezervi). Ako imam 10 čeličnih ploča na stanju, a treba mi 10, neto potreba je 0, ne moram ništa da naručim.
3. **Lead Time Offsetting** - za sve što fali, računa se kada tačno treba krenuti sa nabavkom ili proizvodnjom, na osnovu toga koliko dana traje da se ta stavka nabavi/proizvede (lead time). Ako mi rok stiže 1. marta, a nabavka traje 7 dana, nalog za nabavku mora da krene najkasnije 22. februara.

Rezultat cele te kalkulacije su **planirani nalozi** (Planned Orders) - lista konkretnih naloga tipa "naruči X komada od dobavljača" (PURCHASE) ili "proizvedi X komada u fabrici" (PRODUCTION), svaki sa svojim datumom početka i rokom.

Formula za netiranje, malo formalnije:

```
Neto potreba = Bruto potreba - (Zaliha na stanju - Sigurnosna zaliha)
Ako je rezultat manji od 0, neto potreba se postavlja na 0 (ne moze se naruciti negativna kolicina)
```

---

## 2. Tehnologije koje su korišćene

### Backend
- **Java 17**
- **Spring Boot 4** (Spring Web, Spring Data JPA, Spring Validation)
- **PostgreSQL** kao baza podataka
- **Maven** za build (koristi se Maven Wrapper, `mvnw`, tako da ne mora da se instalira Maven ručno)

### Frontend
- **Angular 16**
- **Angular Material** za UI komponente (tabele, dijalozi, dugmad, forme...)
- **RxJS** za rad sa asinhronim pozivima ka backendu (Observable umesto Promise)
- **TypeScript**

Razlog za ovu kombinaciju je prilično standardan izbor za enterprise aplikacije: Spring Boot je industrijski standard za Java backend, JPA/Hibernate rešava mapiranje objekata na tabele u bazi bez ručnog pisanja SQL-a za svaku operaciju, a Angular sa Material bibliotekom daje gotov, konzistentan izgled bez da se piše CSS za svaki dugme i svaku tabelu od nule.

---

## 3. Arhitektura backend-a

Kod je organizovan po slojevima, što je standardan pristup u Spring Boot projektima:

```
smartMRP.smart_mrp/
├── entity/       -> JPA entiteti, klase koje se direktno mapiraju na tabele u bazi
├── repository/   -> Spring Data JPA interfejsi za upit nad bazom (bez pisanja SQL-a)
├── service/      -> biznis logika, tu se nalazi ceo MRP algoritam
├── controller/   -> REST kontroleri, definišu API endpointe koje frontend poziva
├── dto/          -> Data Transfer Object klase, ono što se stvarno šalje kroz API
├── exception/    -> custom izuzeci (npr. kad artikal ne postoji, ili nema dovoljno zalihe)
└── config/       -> konfiguracija (CORS)
```

Zašto DTO umesto da se entiteti direktno vraćaju kroz API? Zato što entiteti imaju JPA relacije (`@ManyToOne`, `@OneToMany`) koje mogu da naprave beskonačnu petlju kad Jackson pokuša da ih pretvori u JSON, i zato što ne želiš da klijent vidi baš sve što imaš u bazi. DTO je "čista" klasa koja nosi samo ono što je frontendu stvarno potrebno.

Primer, `Item` entitet ima ovako nešto:

```java
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sku;

    private String name;
    private String unitOfMeasure;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    private Integer leadTimeDays;
    private Double safetyStock;
}
```

A `ItemDTO` samo uzme te podatke i vrati ih kao običan JSON, bez ikakvih JPA magija:

```java
public record ItemDTO(
    Long id,
    String sku,
    String name,
    String unitOfMeasure,
    ItemCategory category,
    Integer leadTimeDays,
    Double safetyStock
) {
    public static ItemDTO fromEntity(Item item) {
        return new ItemDTO(
            item.getId(), item.getSku(), item.getName(),
            item.getUnitOfMeasure(), item.getCategory(),
            item.getLeadTimeDays(), item.getSafetyStock()
        );
    }
}
```

### Glavni entiteti u bazi

| Entitet | Šta predstavlja |
|---|---|
| `Item` | Artikal - sirovina, poluproizvod ili gotov proizvod |
| `BomItem` | Jedan red u sastavnici - "za 1 komad roditelja treba X komada komponente" |
| `Inventory` | Trenutno stanje zaliha za jedan artikal (na stanju / rezervisano) |
| `ProductionPlan` | Plan proizvodnje - "treba mi X komada artikla Y do datuma Z" |
| `PlannedOrder` | Nalog koji je generisao MRP - nabavka ili proizvodnja, sa statusom |

`Item` ima kategoriju (`ItemCategory`): `RAW_MATERIAL` (sirovina, uvek ide u nabavku), `SEMI_FINISHED` (poluproizvod, uvek se proizvodi) ili `FINISHED_PRODUCT` (gotov proizvod, takođe se proizvodi). Na osnovu ove kategorije MRP servis odlučuje da li generiše `PURCHASE` ili `PRODUCTION` nalog.

### Kako izgleda sam MRP algoritam u kodu

Srce cele aplikacije je `MrpService`. Eksplozija BOM-a je klasična rekurzija:

```java
private void explodeBom(Long itemId, Double quantity, LocalDate dueDate,
                         Map<Long, BomRequirement> requirements) {

    List<BomItem> components = bomItemRepository.findByParentItemId(itemId);

    for (BomItem bomItem : components) {
        Long componentId = bomItem.getComponentItem().getId();
        Double requiredQty = quantity * bomItem.getQuantity();

        addRequirement(requirements, componentId, requiredQty, dueDate);

        // ide dalje na dete ove komponente, ako i ona ima svoj BOM
        explodeBom(componentId, requiredQty, dueDate, requirements);
    }
}
```

Svaki poziv silazi jedan nivo dublje u sastavnicu, sve dok ne stigne do sirovina koje više nemaju svoj BOM. Kad se sve potrebe skupe, ide netiranje (oduzimanje zaliha) i na kraju se za svaku stavku koja je ostala kreira `PlannedOrder`.

### Status naloga i automatska promena zaliha

Kad korisnik ručno promeni status nekog `PlannedOrder`-a (npr. iz `PLANNED` u `RELEASED`), aplikacija automatski radi odgovarajuću operaciju nad zalihama, da korisnik ne mora sam da ažurira brojeve. Dozvoljeni prelazi statusa su:

```
PLANNED     -> RELEASED, CANCELLED
RELEASED    -> IN_PROGRESS, CANCELLED
IN_PROGRESS -> COMPLETED, CANCELLED
COMPLETED   -> (kraj, nema dalje)
CANCELLED   -> (kraj, nema dalje)
```

Šta se dešava sa zalihama u pozadini, u zavisnosti od tipa naloga:

| Tip naloga | Prelaz statusa | Šta se radi sa zalihama |
|---|---|---|
| PURCHASE | -> COMPLETED | Roba stiže, `quantityOnHand` se povećava |
| PRODUCTION | -> RELEASED | Rezervišu se komponente iz BOM-a (`reservedQuantity` raste) |
| PRODUCTION | -> IN_PROGRESS | Komponente se stvarno izdaju (skida se i sa stanja i iz rezervacije) |
| PRODUCTION | -> COMPLETED | Gotov proizvod ulazi na stanje |
| PRODUCTION | RELEASED -> CANCELLED | Rezervacija komponenti se oslobađa |

Ovo je urađeno u `PlannedOrderService.updateStatus()`, tako da se cela ta logika nalazi na jednom mestu i sve je unutar jedne transakcije (ili sve prođe, ili ništa).

---

## 4. Arhitektura frontend-a

Angular projekat je standalone (bez starih NgModule-ova), organizovan po feature-ima:

```
src/app/
├── core/
│   ├── models/        -> TypeScript interfejsi koji odgovaraju backend DTO-ovima
│   └── services/       -> servisi koji zovu backend API preko HttpClient-a
├── shared/
│   └── components/     -> komponente koje se koriste na više mesta (npr. layout sa sidebar-om)
└── features/
    ├── items/           -> artikli
    ├── bom/             -> sastavnice
    ├── inventory/        -> zalihe
    ├── production-plans/  -> planovi proizvodnje
    ├── mrp/              -> pokretanje MRP-a i pregled naloga
    └── dashboard/         -> početna stranica
```

Svaki servis je tanka omotnica oko `HttpClient`-a. Na primer `MrpService`:

```typescript
@Injectable({ providedIn: 'root' })
export class MrpService {
  private apiUrl = `${environment.apiUrl}/mrp`;

  constructor(private http: HttpClient) {}

  runMrp(planId: number): Observable<MrpResult> {
    return this.http.post<MrpResult>(`${this.apiUrl}/run/${planId}`, {});
  }

  getPurchaseOrders(): Observable<PlannedOrder[]> {
    return this.http.get<PlannedOrder[]>(`${this.apiUrl}/orders/purchase`);
  }
}
```

Komponenta se onda samo pretplati na taj Observable i prikaže rezultat, ili obradi grešku ako nešto pukne (npr. nedovoljna zaliha, backend to vraća kao 400 sa porukom).

---

## 5. Kako pokrenuti aplikaciju

Treba ti instalirano:

- **Java 17** (ili noviji)
- **Node.js** (18+, testirano na novijim verzijama) i npm
- **PostgreSQL** (lokalno pokrenut server)

Frontend i backend se pokreću odvojeno, u dva terminala.

### Korak 1: priprema baze

Kreiraj bazu koja se zove `mrp_db` (ime mora tačno tako da se zove, ili moraš da izmeniš konfiguraciju u koraku 2):

```bash
psql -U postgres -c "CREATE DATABASE mrp_db;"
```

Ne treba ručno praviti tabele. Hibernate to radi sam pri pokretanju backenda (`ddl-auto=update` u konfiguraciji), na osnovu `@Entity` klasa.

### Korak 2: konfiguracija backenda (ako treba)

Fajl `smart-mrp/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mrp_db
spring.datasource.username=postgres
spring.datasource.password=postgres
server.port=8081
```

Ako ti se korisničko ime/lozinka za Postgres razlikuju, izmeni ovde pre pokretanja.

### Korak 3: pokretanje backenda

Iz foldera `smart-mrp` (onaj koji sadrži `pom.xml`):

```bash
cd smart-mrp
./mvnw spring-boot:run
```

Na Windows-u koristiš `mvnw.cmd` umesto `./mvnw`, ili prosto `mvnw spring-boot:run` u cmd/PowerShell-u. Backend će se pokrenuti na `http://localhost:8081`.

Kad se ispiše nešto tipa `Started SmartMrpApplication`, backend radi i spreman je da prima zahteve.

### Korak 4: pokretanje frontenda

Iz foldera `Frontend`:

```bash
cd Frontend
npm install
npm start
```

`npm install` treba samo prvi put (ili kad se promene dependency-ji). `npm start` pokreće Angular dev server, obično na `http://localhost:4200`. Otvoriš taj link u browseru i to je to.

Frontend je već podešen da priča sa backendom na `http://localhost:8081/api` (fajl `src/environments/environment.ts`), tako da ne treba ništa dodatno da se menja ako si ostavio podrazumevani port.

### Redosled pokretanja

Prvo backend, pa frontend. Ako pokreneš frontend pre backenda, aplikacija će se učitati ali pozivi ka API-ju će failovati dok ne upališ backend - to je normalno, samo osveži stranicu kad backend upali.

---

## 6. Kako se aplikacija koristi (tipičan scenario)

1. Otvoriš **Artikli** i kreiraš par artikala - recimo sirovinu (čelik), poluproizvod (okvir) i gotov proizvod (sto).
2. Odeš na **Sastavnice (BOM)** i povežeš ih - sto se sastoji od 1 okvira i 4 noge, okvir se sastoji od 2 čelične ploče itd.
3. U **Zalihama** postaviš koliko trenutno imaš na stanju od svake sirovine.
4. U **Planovi proizvodnje** kreiraš plan - "treba mi 5 stolova do 1. marta".
5. Odeš na **MRP** i pokreneš kalkulaciju za taj plan. Sistem ti generiše listu naloga - šta treba naručiti, šta proizvesti, i kada tačno treba krenuti sa svakim.
6. Kroz **Planirane naloge** pratiš i menjaš status svakog naloga kako napreduje (poručeno, u toku, završeno), a zalihe se ažuriraju same u pozadini.

---

## 7. Kratak pregled API-ja

Baza URL-a je `http://localhost:8081/api`. Par najbitnijih poziva:

```
POST /api/mrp/run/{planId}          - pokreće MRP kalkulaciju za dati plan
GET  /api/mrp/orders/purchase       - svi nalozi za nabavku
GET  /api/mrp/orders/production     - svi nalozi za proizvodnju
PUT  /api/mrp/orders/{id}/status    - promena statusa naloga (auto azurira zalihe)

GET  /api/items                     - svi artikli
POST /api/items                     - kreiranje artikla

GET  /api/bom/parent/{parentId}     - komponente za dati artikal

GET  /api/inventory                 - stanje zaliha
POST /api/inventory/item/{id}/add   - prijem robe na stanje
```

Kompletna lista endpointa (sa primerima request/response tela) nalazi se u [ANGULAR_FRONTEND_INSTRUKCIJE.md](ANGULAR_FRONTEND_INSTRUKCIJE.md), a detaljan opis svakog sloja backenda u [CLAUDE_CONTEXT.md](CLAUDE_CONTEXT.md).

