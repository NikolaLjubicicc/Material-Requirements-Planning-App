package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.entity.ItemCategory;
import smartMRP.smart_mrp.exception.DuplicateSkuException;
import smartMRP.smart_mrp.exception.ItemInUseException;
import smartMRP.smart_mrp.exception.ItemNotFoundException;
import smartMRP.smart_mrp.repository.BomItemRepository;
import smartMRP.smart_mrp.repository.ItemRepository;

import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final BomItemRepository bomItemRepository;

    public ItemService(ItemRepository itemRepository, BomItemRepository bomItemRepository) {
        this.itemRepository = itemRepository;
        this.bomItemRepository = bomItemRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Item findBySku(String sku) {
        return itemRepository.findBySku(sku)
                .orElseThrow(() -> new ItemNotFoundException(sku));
    }

    @Transactional(readOnly = true)
    public List<Item> findByCategory(ItemCategory category) {
        return itemRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Item> searchByName(String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }

    public Item create(Item item) {
        if (itemRepository.existsBySku(item.getSku())) {
            throw new DuplicateSkuException(item.getSku());
        }
        return itemRepository.save(item);
    }

    public Item update(Long id, Item updatedItem) {
        Item existingItem = findById(id);

        if (!existingItem.getSku().equals(updatedItem.getSku())
                && itemRepository.existsBySku(updatedItem.getSku())) {
            throw new DuplicateSkuException(updatedItem.getSku());
        }

        existingItem.setSku(updatedItem.getSku());
        existingItem.setName(updatedItem.getName());
        existingItem.setUnitOfMeasure(updatedItem.getUnitOfMeasure());
        existingItem.setCategory(updatedItem.getCategory());
        existingItem.setLeadTimeDays(updatedItem.getLeadTimeDays());
        existingItem.setSafetyStock(updatedItem.getSafetyStock());

        return itemRepository.save(existingItem);
    }

    public void delete(Long id) {
        Item item = findById(id);

        if (bomItemRepository.isItemUsedInBom(id)) {
            throw new ItemInUseException(id);
        }

        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return itemRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return itemRepository.existsBySku(sku);
    }
}
