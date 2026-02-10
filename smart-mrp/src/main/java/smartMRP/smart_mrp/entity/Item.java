package smartMRP.smart_mrp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Artikal - osnovni entitet sistema.
 * Predstavlja sirovinu, poluproizvod ili gotov proizvod.
 */
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU je obavezan")
    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @NotBlank(message = "Naziv je obavezan")
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank(message = "Merna jedinica je obavezna")
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @NotNull(message = "Kategorija je obavezna")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Min(value = 0, message = "Lead time ne može biti negativan")
    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays = 0;

    @Min(value = 0, message = "Safety stock ne može biti negativan")
    @Column(name = "safety_stock", nullable = false)
    private Double safetyStock = 0.0;

    public Item() {
    }

    public Item(Long id, String sku, String name, String unitOfMeasure,
                ItemCategory category, Integer leadTimeDays, Double safetyStock) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
        this.category = category;
        this.leadTimeDays = leadTimeDays;
        this.safetyStock = safetyStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public Double getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(Double safetyStock) {
        this.safetyStock = safetyStock;
    }
}
