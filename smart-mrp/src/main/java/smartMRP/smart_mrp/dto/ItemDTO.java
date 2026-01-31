package smartMRP.smart_mrp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.entity.ItemCategory;

public class ItemDTO {

    private Long id;

    @NotBlank(message = "SKU je obavezan")
    private String sku;

    @NotBlank(message = "Naziv je obavezan")
    private String name;

    @NotBlank(message = "Merna jedinica je obavezna")
    private String unitOfMeasure;

    @NotNull(message = "Kategorija je obavezna")
    private ItemCategory category;

    @Min(value = 0, message = "Lead time ne može biti negativan")
    private Integer leadTimeDays = 0;

    @Min(value = 0, message = "Safety stock ne može biti negativan")
    private Double safetyStock = 0.0;

    public ItemDTO() {}

    // Konverzija iz Entity u DTO
    public static ItemDTO fromEntity(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setSku(item.getSku());
        dto.setName(item.getName());
        dto.setUnitOfMeasure(item.getUnitOfMeasure());
        dto.setCategory(item.getCategory());
        dto.setLeadTimeDays(item.getLeadTimeDays());
        dto.setSafetyStock(item.getSafetyStock());
        return dto;
    }

    // Konverzija iz DTO u Entity
    public Item toEntity() {
        Item item = new Item();
        item.setId(this.id);
        item.setSku(this.sku);
        item.setName(this.name);
        item.setUnitOfMeasure(this.unitOfMeasure);
        item.setCategory(this.category);
        item.setLeadTimeDays(this.leadTimeDays != null ? this.leadTimeDays : 0);
        item.setSafetyStock(this.safetyStock != null ? this.safetyStock : 0.0);
        return item;
    }

    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public Double getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Double safetyStock) { this.safetyStock = safetyStock; }
}
