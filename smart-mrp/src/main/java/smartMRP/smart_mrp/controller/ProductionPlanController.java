package smartMRP.smart_mrp.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartMRP.smart_mrp.dto.ProductionPlanDTO;
import smartMRP.smart_mrp.entity.PlanStatus;
import smartMRP.smart_mrp.entity.ProductionPlan;
import smartMRP.smart_mrp.service.ProductionPlanService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/production-plans")
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    public ProductionPlanController(ProductionPlanService productionPlanService) {
        this.productionPlanService = productionPlanService;
    }

    @GetMapping
    public ResponseEntity<List<ProductionPlanDTO>> getAll() {
        List<ProductionPlanDTO> plans = productionPlanService.findAll().stream()
                .map(ProductionPlanDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionPlanDTO> getById(@PathVariable Long id) {
        ProductionPlan plan = productionPlanService.findById(id);
        return ResponseEntity.ok(ProductionPlanDTO.fromEntity(plan));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductionPlanDTO>> getByStatus(@PathVariable PlanStatus status) {
        List<ProductionPlanDTO> plans = productionPlanService.findByStatus(status).stream()
                .map(ProductionPlanDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ProductionPlanDTO>> getPending() {
        List<ProductionPlanDTO> plans = productionPlanService.findPendingPlans().stream()
                .map(ProductionPlanDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ProductionPlanDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ProductionPlanDTO> plans = productionPlanService.findByDateRange(startDate, endDate).stream()
                .map(ProductionPlanDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    @PostMapping
    public ResponseEntity<ProductionPlanDTO> create(@Valid @RequestBody ProductionPlanDTO dto) {
        ProductionPlan plan = productionPlanService.create(
                dto.getItemId(),
                dto.getRequiredQuantity(),
                dto.getDueDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductionPlanDTO.fromEntity(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionPlanDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductionPlanDTO dto) {
        ProductionPlan plan = productionPlanService.update(
                id,
                dto.getRequiredQuantity(),
                dto.getDueDate()
        );
        return ResponseEntity.ok(ProductionPlanDTO.fromEntity(plan));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductionPlanDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam PlanStatus status) {
        ProductionPlan plan = productionPlanService.updateStatus(id, status);
        return ResponseEntity.ok(ProductionPlanDTO.fromEntity(plan));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ProductionPlanDTO> cancel(@PathVariable Long id) {
        ProductionPlan plan = productionPlanService.cancel(id);
        return ResponseEntity.ok(ProductionPlanDTO.fromEntity(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productionPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
