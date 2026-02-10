package smartMRP.smart_mrp.dto;

import smartMRP.smart_mrp.entity.OrderStatus;
import smartMRP.smart_mrp.entity.OrderType;

import java.util.ArrayList;
import java.util.List;


public class PlannedOrderStatusUpdateResponse {

    private Long orderId;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private OrderType orderType;
    private List<InventoryOperation> inventoryOperations = new ArrayList<>();
    private String message;
    private boolean success;

    public PlannedOrderStatusUpdateResponse() {}

    public PlannedOrderStatusUpdateResponse orderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public PlannedOrderStatusUpdateResponse previousStatus(OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
        return this;
    }

    public PlannedOrderStatusUpdateResponse newStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
        return this;
    }

    public PlannedOrderStatusUpdateResponse orderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public PlannedOrderStatusUpdateResponse addOperation(InventoryOperation operation) {
        this.inventoryOperations.add(operation);
        return this;
    }

    public PlannedOrderStatusUpdateResponse addOperations(List<InventoryOperation> operations) {
        this.inventoryOperations.addAll(operations);
        return this;
    }

    public PlannedOrderStatusUpdateResponse message(String message) {
        this.message = message;
        return this;
    }

    public PlannedOrderStatusUpdateResponse success(boolean success) {
        this.success = success;
        return this;
    }


    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public OrderStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(OrderStatus previousStatus) { this.previousStatus = previousStatus; }

    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public List<InventoryOperation> getInventoryOperations() { return inventoryOperations; }
    public void setInventoryOperations(List<InventoryOperation> inventoryOperations) { this.inventoryOperations = inventoryOperations; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public static class InventoryOperation {
        private String operationType;  // RESERVE, ISSUE, RECEIVE, RELEASE
        private Long itemId;
        private String itemSku;
        private String itemName;
        private Double quantity;
        private String description;

        public InventoryOperation() {}

        public InventoryOperation(String operationType, Long itemId, String itemSku,
                                  String itemName, Double quantity, String description) {
            this.operationType = operationType;
            this.itemId = itemId;
            this.itemSku = itemSku;
            this.itemName = itemName;
            this.quantity = quantity;
            this.description = description;
        }

        public static InventoryOperation reserve(Long itemId, String sku, String name, Double qty) {
            return new InventoryOperation("RESERVE", itemId, sku, name, qty,
                    "Rezervisano " + qty + " " + name);
        }

        public static InventoryOperation issue(Long itemId, String sku, String name, Double qty) {
            return new InventoryOperation("ISSUE", itemId, sku, name, qty,
                    "Izdato " + qty + " " + name + " u proizvodnju");
        }

        public static InventoryOperation receive(Long itemId, String sku, String name, Double qty) {
            return new InventoryOperation("RECEIVE", itemId, sku, name, qty,
                    "Primljeno " + qty + " " + name + " na stanje");
        }

        public static InventoryOperation release(Long itemId, String sku, String name, Double qty) {
            return new InventoryOperation("RELEASE", itemId, sku, name, qty,
                    "Oslobodjena rezervacija od " + qty + " " + name);
        }

        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public String getItemSku() { return itemSku; }
        public void setItemSku(String itemSku) { this.itemSku = itemSku; }

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
