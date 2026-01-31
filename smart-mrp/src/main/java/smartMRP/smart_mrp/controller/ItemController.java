package smartMRP.smart_mrp.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartMRP.smart_mrp.dto.ItemDTO;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.entity.ItemCategory;
import smartMRP.smart_mrp.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAll() {
        List<ItemDTO> items = itemService.findAll().stream()
                .map(ItemDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getById(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return ResponseEntity.ok(ItemDTO.fromEntity(item));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ItemDTO> getBySku(@PathVariable String sku) {
        Item item = itemService.findBySku(sku);
        return ResponseEntity.ok(ItemDTO.fromEntity(item));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ItemDTO>> getByCategory(@PathVariable ItemCategory category) {
        List<ItemDTO> items = itemService.findByCategory(category).stream()
                .map(ItemDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDTO>> searchByName(@RequestParam String name) {
        List<ItemDTO> items = itemService.searchByName(name).stream()
                .map(ItemDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<ItemDTO> create(@Valid @RequestBody ItemDTO itemDTO) {
        Item item = itemService.create(itemDTO.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemDTO.fromEntity(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> update(@PathVariable Long id, @Valid @RequestBody ItemDTO itemDTO) {
        Item item = itemService.update(id, itemDTO.toEntity());
        return ResponseEntity.ok(ItemDTO.fromEntity(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
