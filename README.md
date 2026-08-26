# Smart MRP

Sistem za planiranje materijalnih potreba (Material Requirements Planning). Aplikacija automatizuje proces koji se koristi u proizvodnim preduzećima: na osnovu plana proizvodnje i trenutnog stanja zaliha izračunava koje sirovine treba nabaviti, šta treba proizvesti, i kada tačno treba pokrenuti nabavku odnosno proizvodnju da bi se ispoštovao rok.

Aplikacija se sastoji iz dva dela:

- **Backend** (folder `smart-mrp`) - Spring Boot aplikacija koja sadrži celokupnu poslovnu logiku i komunicira sa bazom podataka.
- **Frontend** (folder `Frontend`) - Angular aplikacija koja predstavlja korisnički interfejs.

Komunikacija između ova dva dela odvija se preko REST API-ja, razmenom JSON podataka putem HTTP protokola.

---

## 1. MRP algoritam

Osnovni koncept na kome počiva sistem jeste **sastavnica** (BOM - Bill of Materials): svaki proizvod se sastoji od određenih komponenti, a te komponente mogu, dalje, imati sopstvene komponente. Time se formira hijerarhijska struktura (stablo), na primer gotov proizvod se sastoji od poluproizvoda, a poluproizvod se sastoji od sirovina.

Kada se u sistem unese plan proizvodnje (recimo, potrebno je proizvesti određenu količinu gotovog proizvoda do zadatog roka), MRP algoritam sprovodi tri koraka:

1. **Eksplozija BOM-a (Explosion)** - rekurzivnim obilaskom sastavnice izračunavaju se bruto potrebe za svakom komponentom, na svim nivoima hijerarhije.
2. **Netiranje (Netting)** - od bruto potrebe se oduzima raspoloživa zaliha, umanjena za sigurnosnu zalihu koja se uvek zadržava kao rezerva. Ukoliko je zaliha dovoljna, neto potreba iznosi nula i ne generiše se nalog.
3. **Lead Time Offsetting** - za preostale (neto) potrebe računa se datum kada nabavka ili proizvodnja mora da počne, na osnovu vremena trajanja nabavke odnosno proizvodnje (lead time), tako da se stavka dobije tačno na vreme.

Rezultat kalkulacije je lista **planiranih naloga** (Planned Orders) - naloga tipa nabavka (PURCHASE) ili proizvodnja (PRODUCTION), svaki sa definisanim datumom početka i rokom završetka.

Formula netiranja:

```
Neto potreba = Bruto potreba - (Zaliha na stanju - Sigurnosna zaliha)
Ako je rezultat manji od 0, neto potreba se postavlja na 0.
```

---

## 2. Korišćene tehnologije

### Backend
- **Java 17**
- **Spring Boot 4** (Spring Web, Spring Data JPA, Spring Validation)
- **PostgreSQL** kao sistem za upravljanje bazom podataka
- **Maven** za upravljanje zavisnostima i build proces (koristi se Maven Wrapper, `mvnw`, pa lokalna instalacija Maven-a nije neophodna)

### Frontend
- **Angular 16**
- **Angular Material** za UI komponente (tabele, dijalozi, forme, dugmad)
- **RxJS** za rad sa asinhronim pozivima ka backendu
- **TypeScript**

Izbor tehnologija odgovara uobičajenoj enterprise arhitekturi: Spring Boot je standardno rešenje za Java backend servise, JPA/Hibernate uklanja potrebu za ručnim pisanjem SQL upita za osnovne operacije nad bazom, dok Angular u kombinaciji sa Material bibliotekom obezbeđuje konzistentan korisnički interfejs bez potrebe za pisanjem CSS-a od nule.

---

## 3. Arhitektura backend-a

Kod je organizovan po slojevima, u skladu sa uobičajenom praksom u Spring Boot aplikacijama:

```
smartMRP.smart_mrp/
├── entity/       -> JPA entiteti, klase koje se mapiraju na tabele u bazi
├── repository/   -> Spring Data JPA interfejsi za pristup bazi
├── service/      -> poslovna logika, uključujući kompletan MRP algoritam
├── controller/   -> REST kontroleri koji definišu API endpointe
├── dto/          -> Data Transfer Object klase koje se razmenjuju kroz API
├── exception/    -> custom izuzeci (npr. artikal ne postoji, nedovoljna zaliha)
└── config/       -> konfiguracija (CORS)
```

DTO klase se koriste umesto direktnog vraćanja entiteta kroz API iz dva razloga: entiteti sadrže JPA relacije (`@ManyToOne`, `@OneToMany`) koje mogu prouzrokovati beskonačnu petlju prilikom serijalizacije u JSON, i nije poželjno da klijent ima uvid u kompletnu internu strukturu podataka. DTO predstavlja klasu koja nosi isključivo podatke koji su frontendu potrebni.

Primer entiteta `Item`:

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

Odgovarajući `ItemDTO` sadrži samo podatke koji se prosleđuju kroz API, bez JPA specifičnosti:

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

### Glavni entiteti

| Entitet | Opis |
|---|---|
| `Item` | Artikal - sirovina, poluproizvod ili gotov proizvod |
| `BomItem` | Jedan red sastavnice - definiše koliko jedinica komponente je potrebno za jednu jedinicu roditeljskog artikla |
| `Inventory` | Trenutno stanje zaliha za artikal (količina na stanju i rezervisana količina) |
| `ProductionPlan` | Plan proizvodnje - definiše traženu količinu artikla i rok |
| `PlannedOrder` | Nalog generisan MRP kalkulacijom - nabavka ili proizvodnja, sa svojim statusom |

Svaki `Item` ima kategoriju (`ItemCategory`): `RAW_MATERIAL` (sirovina, uvek se nabavlja), `SEMI_FINISHED` (poluproizvod, uvek se proizvodi) i `FINISHED_PRODUCT` (gotov proizvod, takođe se proizvodi). Na osnovu ove kategorije MRP servis određuje da li generiše `PURCHASE` ili `PRODUCTION` nalog.

### Implementacija MRP algoritma

Centralna klasa je `MrpService`. Eksplozija BOM-a je implementirana kao rekurzivna funkcija:

```java
private void explodeBom(Long itemId, Double quantity, LocalDate dueDate,
                         Map<Long, BomRequirement> requirements) {

    List<BomItem> components = bomItemRepository.findByParentItemId(itemId);

    for (BomItem bomItem : components) {
        Long componentId = bomItem.getComponentItem().getId();
        Double requiredQty = quantity * bomItem.getQuantity();

        addRequirement(requirements, componentId, requiredQty, dueDate);

        explodeBom(componentId, requiredQty, dueDate, requirements);
    }
}
```

Svaki poziv silazi jedan nivo dublje u sastavnicu, sve dok se ne dođe do sirovina koje više nemaju sopstveni BOM. Nakon što se prikupe sve bruto potrebe, sprovodi se netiranje, a zatim se za svaku preostalu (neto) potrebu kreira `PlannedOrder`.

### Status naloga i automatsko ažuriranje zaliha

Prilikom promene statusa `PlannedOrder`-a (npr. iz `PLANNED` u `RELEASED`), sistem automatski izvršava odgovarajuću operaciju nad zalihama, bez potrebe za ručnim ažuriranjem od strane korisnika. Dozvoljeni prelazi statusa:

```
PLANNED     -> RELEASED, CANCELLED
RELEASED    -> IN_PROGRESS, CANCELLED
IN_PROGRESS -> COMPLETED, CANCELLED
COMPLETED   -> (terminalni status)
CANCELLED   -> (terminalni status)
```

Operacije nad zalihama u zavisnosti od tipa naloga i prelaza statusa:

| Tip naloga | Prelaz statusa | Operacija nad zalihama |
|---|---|---|
| PURCHASE | -> COMPLETED | Prijem robe, `quantityOnHand` se uvećava |
| PRODUCTION | -> RELEASED | Rezervacija komponenti iz BOM-a, `reservedQuantity` se uvećava |
| PRODUCTION | -> IN_PROGRESS | Izdavanje komponenti u proizvodnju (umanjuje se i stanje i rezervacija) |
| PRODUCTION | -> COMPLETED | Prijem gotovog proizvoda na stanje |
| PRODUCTION | RELEASED -> CANCELLED | Oslobađanje rezervacije komponenti |

Ova logika je implementirana u `PlannedOrderService.updateStatus()`, tako da se sve operacije izvršavaju unutar jedne transakcije - ili se sve uspešno završi, ili se sve poništava.

---

## 4. Arhitektura frontend-a

Angular projekat koristi standalone komponente (bez NgModule-a) i organizovan je po funkcionalnim celinama:

```
src/app/
├── core/
│   ├── models/         -> TypeScript interfejsi koji odgovaraju backend DTO-ovima
│   └── services/        -> servisi koji pozivaju backend API preko HttpClient-a
├── shared/
│   └── components/      -> komponente koje se koriste na više mesta (npr. layout sa navigacijom)
└── features/
    ├── items/            -> artikli
    ├── bom/              -> sastavnice
    ├── inventory/         -> zalihe
    ├── production-plans/   -> planovi proizvodnje
    ├── mrp/               -> pokretanje MRP kalkulacije i pregled naloga
    └── dashboard/          -> početna stranica
```

Servisi predstavljaju tanak sloj iznad `HttpClient`-a. Primer `MrpService`:

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

Komponenta se pretplaćuje na vraćeni Observable i prikazuje rezultat, odnosno obrađuje grešku ukoliko zahtev ne uspe (na primer, backend vraća status 400 sa porukom u slučaju nedovoljne zalihe).

---

## 5. Pokretanje aplikacije

Preduslovi:

- **Java 17** ili novija verzija
- **Node.js** (18+) i npm
- **PostgreSQL** server, lokalno pokrenut

Backend i frontend se pokreću odvojeno, u dva terminala.

### Korak 1: Priprema baze podataka

Potrebno je kreirati bazu pod nazivom `mrp_db`:

```bash
psql -U postgres -c "CREATE DATABASE mrp_db;"
```

Tabele se ne kreiraju ručno - Hibernate ih automatski generiše prilikom pokretanja backenda (opcija `ddl-auto=update`), na osnovu definisanih `@Entity` klasa.

### Korak 2: Konfiguracija backenda

Fajl `smart-mrp/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mrp_db
spring.datasource.username=postgres
spring.datasource.password=postgres
server.port=8081
```

Ukoliko se korisničko ime ili lozinka za PostgreSQL razlikuju od podrazumevanih, potrebno je izmeniti ove vrednosti pre pokretanja.

### Korak 3: Pokretanje backenda

Iz foldera `smart-mrp` (folder koji sadrži `pom.xml`):

```bash
cd smart-mrp
./mvnw spring-boot:run
```

Na operativnom sistemu Windows koristi se `mvnw.cmd` umesto `./mvnw`. Backend se pokreće na adresi `http://localhost:8081`. Poruka `Started SmartMrpApplication` u konzoli označava da je aplikacija spremna za prijem zahteva.

### Korak 4: Pokretanje frontenda

Iz foldera `Frontend`:

```bash
cd Frontend
npm install
npm start
```

Komandu `npm install` je potrebno izvršiti samo prilikom prvog pokretanja, odnosno nakon promene zavisnosti. Komanda `npm start` pokreće Angular development server, podrazumevano na adresi `http://localhost:4200`.

Frontend je unapred konfigurisan da komunicira sa backendom na adresi `http://localhost:8081/api` (fajl `src/environments/environment.ts`), tako da dodatna podešavanja nisu potrebna ukoliko se koriste podrazumevani portovi.

### Redosled pokretanja

Backend je potrebno pokrenuti pre frontenda. Ukoliko se frontend pokrene prvi, aplikacija će se učitati, ali pozivi ka API-ju neće uspeti dok backend ne bude aktivan - u tom slučaju je dovoljno osvežiti stranicu nakon pokretanja backenda.

---

## 6. Tipičan scenario korišćenja

1. U sekciji **Artikli** kreiraju se artikli - na primer sirovina, poluproizvod i gotov proizvod.
2. U sekciji **Sastavnice (BOM)** definišu se odnosi između artikala - od kojih komponenti se sastoji gotov proizvod, i u kojim količinama.
3. U sekciji **Zalihe** unosi se trenutno stanje na skladištu za svaku sirovinu.
4. U sekciji **Planovi proizvodnje** kreira se plan sa traženom količinom gotovog proizvoda i rokom.
5. U sekciji **MRP** pokreće se kalkulacija za dati plan. Sistem generiše listu naloga - šta treba nabaviti, šta proizvesti, i kada je potrebno pokrenuti svaki od njih.
6. Kroz sekciju **Planirani nalozi** prati se i menja status svakog naloga (poručeno, u toku, završeno), pri čemu se zalihe automatski ažuriraju u pozadini.

---

## 7. Pregled API-ja

Osnovni URL: `http://localhost:8081/api`. Najvažniji pozivi:

```
POST /api/mrp/run/{planId}          - pokretanje MRP kalkulacije za dati plan
GET  /api/mrp/orders/purchase       - svi nalozi za nabavku
GET  /api/mrp/orders/production     - svi nalozi za proizvodnju
PUT  /api/mrp/orders/{id}/status    - promena statusa naloga (automatski ažurira zalihe)

GET  /api/items                     - svi artikli
POST /api/items                     - kreiranje artikla

GET  /api/bom/parent/{parentId}     - komponente datog artikla

GET  /api/inventory                 - stanje zaliha
POST /api/inventory/item/{id}/add   - prijem robe na stanje
```
