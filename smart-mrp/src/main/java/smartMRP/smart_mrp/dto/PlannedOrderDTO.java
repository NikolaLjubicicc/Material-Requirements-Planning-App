package smartMRP.smart_mrp.dto;

import smartMRP.smart_mrp.entity.OrderStatus;
import smartMRP.smart_mrp.entity.OrderType;
import smartMRP.smart_mrp.entity.PlannedOrder;

import java.time.LocalDate;

public class PlannedOrderDTO {

    private Long id;
    private Long itemId;
    private String itemSku;
    private String itemName;
    private String itemUnitOfMeasure;
    private Long productionPlanId;
    private Double quantity;
    private OrderType orderType;
    private LocalDate startDate;
    private LocalDate dueDate;
    private OrderStatus status;
    private LocalDate createdAt;

    public PlannedOrderDTO() {}

    // Konverzija iz Entity u DTO
    public static PlannedOrderDTO fromEntity(PlannedOrder order) {
        PlannedOrderDTO dto = new PlannedOrderDTO();
        dto.setId(order.getId());
        dto.setItemId(order.getItem().getId());
        dto.setItemSku(order.getItem().getSku());
        dto.setItemName(order.getItem().getName());
        dto.setItemUnitOfMeasure(order.getItem().getUnitOfMeasure());
        if (order.getProductionPlan() != null) {
            dto.setProductionPlanId(order.getProductionPlan().getId());
        }
        dto.setQuantity(order.getQuantity());
        dto.setOrderType(order.getOrderType());
        dto.setStartDate(order.getStartDate());
        dto.setDueDate(order.getDueDate());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
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

    public String getItemUnitOfMeasure() { return itemUnitOfMeasure; }
    public void setItemUnitOfMeasure(String itemUnitOfMeasure) { this.itemUnitOfMeasure = itemUnitOfMeasure; }

    public Long getProductionPlanId() { return productionPlanId; }
    public void setProductionPlanId(Long productionPlanId) { this.productionPlanId = productionPlanId; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
