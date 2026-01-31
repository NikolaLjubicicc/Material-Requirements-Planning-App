package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.entity.BomItem;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.exception.BomCycleException;
import smartMRP.smart_mrp.exception.ItemNotFoundException;
import smartMRP.smart_mrp.exception.ResourceNotFoundException;
import smartMRP.smart_mrp.repository.BomItemRepository;
import smartMRP.smart_mrp.repository.ItemRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BomService {

    private final BomItemRepository bomItemRepository;
    private final ItemRepository itemRepository;

    public BomService(BomItemRepository bomItemRepository, ItemRepository itemRepository) {
        this.bomItemRepository = bomItemRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<BomItem> findAll() {
        return bomItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BomItem findById(Long id) {
        return bomItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BomItem", id));
    }

    @Transactional(readOnly = true)
    public List<BomItem> findByParentId(Long parentId) {
        return bomItemRepository.findByParentItemId(parentId);
    }

    @Transactional(readOnly = true)
    public List<BomItem> findByComponentId(Long componentId) {
        return bomItemRepository.findByComponentItemId(componentId);
    }

    public BomItem create(Long parentId, Long componentId, Double quantity) {
        // Validacija: roditelj i komponenta moraju postojati
        Item parentItem = itemRepository.findById(parentId)
                .orElseThrow(() -> new ItemNotFoundException(parentId));
        Item componentItem = itemRepository.findById(componentId)
                .orElseThrow(() -> new ItemNotFoundException(componentId));

        // Validacija: artikal ne može biti sam sebi komponenta
        if (parentId.equals(componentId)) {
            throw new BomCycleException("Artikal ne može biti sam sebi komponenta");
        }

        // Validacija: proveri cikličnu zavisnost
        if (wouldCreateCycle(parentId, componentId)) {
            throw new BomCycleException(parentId, componentId);
        }

        // Proveri da li već postoji ova veza
        if (bomItemRepository.existsByParentItemAndComponentItem(parentItem, componentItem)) {
            throw new IllegalArgumentException(
                    "Veza između artikla " + parentId + " i komponente " + componentId + " već postoji");
        }

        BomItem bomItem = new BomItem();
        bomItem.setParentItem(parentItem);
        bomItem.setComponentItem(componentItem);
        bomItem.setQuantity(quantity);

        return bomItemRepository.save(bomItem);
    }

    public BomItem updateQuantity(Long bomItemId, Double newQuantity) {
        BomItem bomItem = findById(bomItemId);
        bomItem.setQuantity(newQuantity);
        return bomItemRepository.save(bomItem);
    }

    public void delete(Long id) {
        BomItem bomItem = findById(id);
        bomItemRepository.delete(bomItem);
    }

    /**
     * Proverava da li bi dodavanje komponente stvorilo ciklus u BOM-u.
     * Koristi DFS da proveri da li componentId (ili bilo koji njegov potomak)
     * već ima parentId kao komponentu.
     */
    @Transactional(readOnly = true)
    public boolean wouldCreateCycle(Long parentId, Long componentId) {
        Set<Long> visited = new HashSet<>();
        return hasCycle(componentId, parentId, visited);
    }

    private boolean hasCycle(Long currentId, Long targetId, Set<Long> visited) {
        if (currentId.equals(targetId)) {
            return true;
        }

        if (visited.contains(currentId)) {
            return false;
        }

        visited.add(currentId);

        // Pronađi sve komponente trenutnog artikla
        List<BomItem> children = bomItemRepository.findByParentItemId(currentId);
        for (BomItem child : children) {
            if (hasCycle(child.getComponentItem().getId(), targetId, visited)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vraća kompletnu hijerarhiju komponenti za dati artikal (rekurzivno).
     */
    @Transactional(readOnly = true)
    public List<BomItem> getFullBomTree(Long parentId) {
        return bomItemRepository.findByParentItemId(parentId);
    }
}
