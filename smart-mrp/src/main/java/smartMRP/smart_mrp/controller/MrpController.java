package smartMRP.smart_mrp.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartMRP.smart_mrp.dto.MrpResultDTO;
import smartMRP.smart_mrp.dto.PlannedOrderDTO;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateDTO;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateResponse;
import smartMRP.smart_mrp.entity.PlannedOrder;
import smartMRP.smart_mrp.service.MrpService;
import smartMRP.smart_mrp.service.PlannedOrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mrp")
public class MrpController {

    private final MrpService mrpService;
    private final PlannedOrderService plannedOrderService;

    public MrpController(MrpService mrpService, PlannedOrderService plannedOrderService) {
        this.mrpService = mrpService;
        this.plannedOrderService = plannedOrderService;
    }

    @PostMapping("/run/{planId}")
    public ResponseEntity<MrpResultDTO> runMrp(@PathVariable Long planId) {
        MrpService.MrpResult result = mrpService.runMrp(planId);
        return ResponseEntity.ok(MrpResultDTO.fromMrpResult(result));
    }

    @GetMapping("/orders/purchase")
    public ResponseEntity<List<PlannedOrderDTO>> getPurchaseOrders() {
        List<PlannedOrderDTO> orders = mrpService.getPurchaseOrders().stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/orders/production")
    public ResponseEntity<List<PlannedOrderDTO>> getProductionOrders() {
        List<PlannedOrderDTO> orders = mrpService.getProductionOrders().stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/orders/plan/{planId}")
    public ResponseEntity<List<PlannedOrderDTO>> getOrdersByPlan(@PathVariable Long planId) {
        List<PlannedOrderDTO> orders = mrpService.getOrdersByPlan(planId).stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/date-range")
    public ResponseEntity<List<PlannedOrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PlannedOrderDTO> orders = mrpService.getOrdersByDateRange(startDate, endDate).stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<PlannedOrderStatusUpdateResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody PlannedOrderStatusUpdateDTO request) {

        PlannedOrderStatusUpdateResponse response =
                plannedOrderService.updateStatus(orderId, request.getStatus());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PlannedOrderDTO> getOrderById(@PathVariable Long orderId) {
        PlannedOrder order = plannedOrderService.findById(orderId);
        return ResponseEntity.ok(PlannedOrderDTO.fromEntity(order));
    }
}
