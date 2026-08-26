package smartMRP.smart_mrp.dto;

import jakarta.validation.constraints.NotNull;
import smartMRP.smart_mrp.entity.OrderStatus;

public class PlannedOrderStatusUpdateDTO {

    @NotNull(message = "Novi status je obavezan")
    private OrderStatus status;

    public PlannedOrderStatusUpdateDTO() {}

    public PlannedOrderStatusUpdateDTO(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
