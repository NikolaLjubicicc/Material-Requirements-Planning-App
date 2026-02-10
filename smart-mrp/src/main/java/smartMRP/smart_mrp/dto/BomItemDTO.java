package smartMRP.smart_mrp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import smartMRP.smart_mrp.entity.BomItem;

public class BomItemDTO {

    private Long id;

    @NotNull(message = "ID roditelj artikla je obavezan")
    private Long parentItemId;

    private String parentItemSku;
    private String parentItemName;

    @NotNull(message = "ID komponente je obavezan")
    private Long componentItemId;

    private String componentItemSku;
    private String componentItemName;

    @NotNull(message = "Količina je obavezna")
    @Min(value = 0, message = "Količina ne može biti negativna")
    private Double quantity;

    public BomItemDTO() {}

    public static BomItemDTO fromEntity(BomItem bomItem) {
        BomItemDTO dto = new BomItemDTO();
        dto.setId(bomItem.getId());
        dto.setParentItemId(bomItem.getParentItem().getId());
        dto.setParentItemSku(bomItem.getParentItem().getSku());
        dto.setParentItemName(bomItem.getParentItem().getName());
        dto.setComponentItemId(bomItem.getComponentItem().getId());
        dto.setComponentItemSku(bomItem.getComponentItem().getSku());
        dto.setComponentItemName(bomItem.getComponentItem().getName());
        dto.setQuantity(bomItem.getQuantity());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParentItemId() { return parentItemId; }
    public void setParentItemId(Long parentItemId) { this.parentItemId = parentItemId; }

    public String getParentItemSku() { return parentItemSku; }
    public void setParentItemSku(String parentItemSku) { this.parentItemSku = parentItemSku; }

    public String getParentItemName() { return parentItemName; }
    public void setParentItemName(String parentItemName) { this.parentItemName = parentItemName; }

    public Long getComponentItemId() { return componentItemId; }
    public void setComponentItemId(Long componentItemId) { this.componentItemId = componentItemId; }

    public String getComponentItemSku() { return componentItemSku; }
    public void setComponentItemSku(String componentItemSku) { this.componentItemSku = componentItemSku; }

    public String getComponentItemName() { return componentItemName; }
    public void setComponentItemName(String componentItemName) { this.componentItemName = componentItemName; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
