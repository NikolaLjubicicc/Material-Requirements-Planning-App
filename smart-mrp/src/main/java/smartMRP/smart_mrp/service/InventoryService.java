package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.entity.Inventory;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.exception.InsufficientInventoryException;
import smartMRP.smart_mrp.exception.ItemNotFoundException;
import smartMRP.smart_mrp.exception.ResourceNotFoundException;
import smartMRP.smart_mrp.repository.InventoryRepository;
import smartMRP.smart_mrp.repository.ItemRepository;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;

    public InventoryService(InventoryRepository inventoryRepository, ItemRepository itemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    @Transactional(readOnly = true)
    public Inventory findByItemId(Long itemId) {
        return inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory za artikal", itemId));
    }

    public Inventory getOrCreateForItem(Long itemId) {
        return inventoryRepository.findByItemId(itemId)
                .orElseGet(() -> {
                    Item item = itemRepository.findById(itemId)
                            .orElseThrow(() -> new ItemNotFoundException(itemId));
                    Inventory inventory = new Inventory();
                    inventory.setItem(item);
                    inventory.setQuantityOnHand(0.0);
                    inventory.setReservedQuantity(0.0);
                    return inventoryRepository.save(inventory);
                });
    }

    public Inventory create(Long itemId, Double quantityOnHand, Double reservedQuantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (inventoryRepository.existsByItemId(itemId)) {
            throw new IllegalArgumentException("Inventory za artikal " + itemId + " već postoji");
        }

        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQuantityOnHand(quantityOnHand != null ? quantityOnHand : 0.0);
        inventory.setReservedQuantity(reservedQuantity != null ? reservedQuantity : 0.0);

        return inventoryRepository.save(inventory);
    }

    public Inventory updateQuantityOnHand(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);
        inventory.setQuantityOnHand(quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory updateReservedQuantity(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);
        inventory.setReservedQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory addToStock(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory removeFromStock(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientInventoryException(
                    itemId, quantity, inventory.getAvailableQuantity());
        }

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory reserve(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);

        double availableToReserve = inventory.getQuantityOnHand() - inventory.getReservedQuantity();
        if (availableToReserve < quantity) {
            throw new InsufficientInventoryException(itemId, quantity, availableToReserve);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    public Inventory releaseReservation(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);
        double newReserved = Math.max(0, inventory.getReservedQuantity() - quantity);
        inventory.setReservedQuantity(newReserved);
        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public Double getAvailableQuantity(Long itemId) {
        return inventoryRepository.findByItemId(itemId)
                .map(Inventory::getAvailableQuantity)
                .orElse(0.0);
    }

    public void delete(Long id) {
        Inventory inventory = findById(id);
        inventoryRepository.delete(inventory);
    }
}
