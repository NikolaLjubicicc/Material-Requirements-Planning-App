package smartMRP.smart_mrp.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartMRP.smart_mrp.dto.BomItemDTO;
import smartMRP.smart_mrp.entity.BomItem;
import smartMRP.smart_mrp.service.BomService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bom")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @GetMapping
    public ResponseEntity<List<BomItemDTO>> getAll() {
        List<BomItemDTO> items = bomService.findAll().stream()
                .map(BomItemDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BomItemDTO> getById(@PathVariable Long id) {
        BomItem bomItem = bomService.findById(id);
        return ResponseEntity.ok(BomItemDTO.fromEntity(bomItem));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<BomItemDTO>> getByParentId(@PathVariable Long parentId) {
        List<BomItemDTO> items = bomService.findByParentId(parentId).stream()
                .map(BomItemDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/component/{componentId}")
    public ResponseEntity<List<BomItemDTO>> getByComponentId(@PathVariable Long componentId) {
        List<BomItemDTO> items = bomService.findByComponentId(componentId).stream()
                .map(BomItemDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<BomItemDTO> create(@Valid @RequestBody BomItemDTO dto) {
        BomItem bomItem = bomService.create(
                dto.getParentItemId(),
                dto.getComponentItemId(),
                dto.getQuantity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(BomItemDTO.fromEntity(bomItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BomItemDTO> updateQuantity(
            @PathVariable Long id,
            @RequestParam Double quantity) {
        BomItem bomItem = bomService.updateQuantity(id, quantity);
        return ResponseEntity.ok(BomItemDTO.fromEntity(bomItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
