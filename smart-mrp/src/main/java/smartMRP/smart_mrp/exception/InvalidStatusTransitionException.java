package smartMRP.smart_mrp.exception;

import smartMRP.smart_mrp.entity.OrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;

    public InvalidStatusTransitionException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Nevazeci prelaz statusa: " + currentStatus + " -> " + targetStatus);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidStatusTransitionException(OrderStatus currentStatus, OrderStatus targetStatus, String reason) {
        super("Nevazeci prelaz statusa: " + currentStatus + " -> " + targetStatus + ". Razlog: " + reason);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
}
