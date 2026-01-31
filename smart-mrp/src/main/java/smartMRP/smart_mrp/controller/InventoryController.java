package smartMRP.smart_mrp.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartMRP.smart_mrp.dto.InventoryDTO;
import smartMRP.smart_mrp.entity.Inventory;
import smartMRP.smart_mrp.service.InventoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAll() {
        List<InventoryDTO> inventories = inventoryService.findAll().stream()
                .map(InventoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getById(@PathVariable Long id) {
        Inventory inventory = inventoryService.findById(id);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<InventoryDTO> getByItemId(@PathVariable Long itemId) {
        Inventory inventory = inventoryService.findByItemId(itemId);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> create(@Valid @RequestBody InventoryDTO dto) {
        Inventory inventory = inventoryService.create(
                dto.getItemId(),
                dto.getQuantityOnHand(),
                dto.getReservedQuantity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryDTO.fromEntity(inventory));
    }

    @PutMapping("/item/{itemId}/quantity")
    public ResponseEntity<InventoryDTO> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Double quantity) {
        Inventory inventory = inventoryService.updateQuantityOnHand(itemId, quantity);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping("/item/{itemId}/add")
    public ResponseEntity<InventoryDTO> addToStock(
            @PathVariable Long itemId,
            @RequestParam Double quantity) {
        Inventory inventory = inventoryService.addToStock(itemId, quantity);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping("/item/{itemId}/remove")
    public ResponseEntity<InventoryDTO> removeFromStock(
            @PathVariable Long itemId,
            @RequestParam Double quantity) {
        Inventory inventory = inventoryService.removeFromStock(itemId, quantity);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping("/item/{itemId}/reserve")
    public ResponseEntity<InventoryDTO> reserve(
            @PathVariable Long itemId,
            @RequestParam Double quantity) {
        Inventory inventory = inventoryService.reserve(itemId, quantity);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @PostMapping("/item/{itemId}/release")
    public ResponseEntity<InventoryDTO> releaseReservation(
            @PathVariable Long itemId,
            @RequestParam Double quantity) {
        Inventory inventory = inventoryService.releaseReservation(itemId, quantity);
        return ResponseEntity.ok(InventoryDTO.fromEntity(inventory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
