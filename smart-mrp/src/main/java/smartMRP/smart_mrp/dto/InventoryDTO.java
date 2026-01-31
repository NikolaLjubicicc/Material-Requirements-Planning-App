package smartMRP.smart_mrp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import smartMRP.smart_mrp.entity.Inventory;

public class InventoryDTO {

    private Long id;

    @NotNull(message = "ID artikla je obavezan")
    private Long itemId;

    private String itemSku;
    private String itemName;

    @Min(value = 0, message = "Količina ne može biti negativna")
    private Double quantityOnHand = 0.0;

    @Min(value = 0, message = "Rezervisana količina ne može biti negativna")
    private Double reservedQuantity = 0.0;

    private Double availableQuantity;

    public InventoryDTO() {}

    // Konverzija iz Entity u DTO
    public static InventoryDTO fromEntity(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setItemId(inventory.getItem().getId());
        dto.setItemSku(inventory.getItem().getSku());
        dto.setItemName(inventory.getItem().getName());
        dto.setQuantityOnHand(inventory.getQuantityOnHand());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
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

    public Double getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Double quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public Double getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Double reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public Double getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Double availableQuantity) { this.availableQuantity = availableQuantity; }
}
