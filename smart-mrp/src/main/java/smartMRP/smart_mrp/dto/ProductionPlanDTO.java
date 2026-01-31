package smartMRP.smart_mrp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import smartMRP.smart_mrp.entity.PlanStatus;
import smartMRP.smart_mrp.entity.ProductionPlan;

import java.time.LocalDate;

public class ProductionPlanDTO {

    private Long id;

    @NotNull(message = "ID artikla je obavezan")
    private Long itemId;

    private String itemSku;
    private String itemName;

    @NotNull(message = "Količina je obavezna")
    @Min(value = 1, message = "Količina mora biti najmanje 1")
    private Double requiredQuantity;

    @NotNull(message = "Datum je obavezan")
    private LocalDate dueDate;

    private PlanStatus status;
    private LocalDate createdAt;

    public ProductionPlanDTO() {}

    // Konverzija iz Entity u DTO
    public static ProductionPlanDTO fromEntity(ProductionPlan plan) {
        ProductionPlanDTO dto = new ProductionPlanDTO();
        dto.setId(plan.getId());
        dto.setItemId(plan.getItem().getId());
        dto.setItemSku(plan.getItem().getSku());
        dto.setItemName(plan.getItem().getName());
        dto.setRequiredQuantity(plan.getRequiredQuantity());
        dto.setDueDate(plan.getDueDate());
        dto.setStatus(plan.getStatus());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }

    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemSku() { return itemSku; }
    public void setItemSku(String itemSku) { this.itemSku = itemSku; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Double requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
