package smartMRP.smart_mrp.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartMRP.smart_mrp.dto.MrpResultDTO;
import smartMRP.smart_mrp.dto.PlannedOrderDTO;
import smartMRP.smart_mrp.service.MrpService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mrp")
public class MrpController {

    private final MrpService mrpService;

    public MrpController(MrpService mrpService) {
        this.mrpService = mrpService;
    }

    /**
     * Pokreće MRP kalkulaciju za dati ProductionPlan.
     */
    @PostMapping("/run/{planId}")
    public ResponseEntity<MrpResultDTO> runMrp(@PathVariable Long planId) {
        MrpService.MrpResult result = mrpService.runMrp(planId);
        return ResponseEntity.ok(MrpResultDTO.fromMrpResult(result));
    }

    /**
     * Vraća sve PLANNED naloge za nabavku (lista za kupovinu).
     */
    @GetMapping("/orders/purchase")
    public ResponseEntity<List<PlannedOrderDTO>> getPurchaseOrders() {
        List<PlannedOrderDTO> orders = mrpService.getPurchaseOrders().stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    /**
     * Vraća sve PLANNED naloge za proizvodnju.
     */
    @GetMapping("/orders/production")
    public ResponseEntity<List<PlannedOrderDTO>> getProductionOrders() {
        List<PlannedOrderDTO> orders = mrpService.getProductionOrders().stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    /**
     * Vraća sve naloge za određeni ProductionPlan.
     */
    @GetMapping("/orders/plan/{planId}")
    public ResponseEntity<List<PlannedOrderDTO>> getOrdersByPlan(@PathVariable Long planId) {
        List<PlannedOrderDTO> orders = mrpService.getOrdersByPlan(planId).stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    /**
     * Vraća naloge u zadatom vremenskom periodu.
     */
    @GetMapping("/orders/date-range")
    public ResponseEntity<List<PlannedOrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PlannedOrderDTO> orders = mrpService.getOrdersByDateRange(startDate, endDate).stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }
}
