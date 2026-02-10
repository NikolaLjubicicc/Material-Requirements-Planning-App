package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateResponse.InventoryOperation;
import smartMRP.smart_mrp.entity.*;
import smartMRP.smart_mrp.exception.InsufficientInventoryException;
import smartMRP.smart_mrp.exception.ItemNotFoundException;
import smartMRP.smart_mrp.exception.OrderNotFoundException;
import smartMRP.smart_mrp.exception.ResourceNotFoundException;
import smartMRP.smart_mrp.repository.InventoryRepository;
import smartMRP.smart_mrp.repository.ItemRepository;
import smartMRP.smart_mrp.repository.OrderComponentReservationRepository;
import smartMRP.smart_mrp.repository.PlannedOrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final BomService bomService;
    private final PlannedOrderRepository plannedOrderRepository;
    private final OrderComponentReservationRepository reservationRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ItemRepository itemRepository,
                            BomService bomService,
                            PlannedOrderRepository plannedOrderRepository,
                            OrderComponentReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
        this.bomService = bomService;
        this.plannedOrderRepository = plannedOrderRepository;
        this.reservationRepository = reservationRepository;
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

    /**
     * Kreira ili vraća postojeći inventory za artikal.
     */
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

    /**
     * Dodaje količinu na stanje (prijem robe).
     */
    public Inventory addToStock(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Skida količinu sa stanja (izdavanje robe).
     */
    public Inventory removeFromStock(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientInventoryException(
                    itemId, quantity, inventory.getAvailableQuantity());
        }

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Rezerviše količinu (za planirane naloge).
     */
    public Inventory reserve(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);

        double availableToReserve = inventory.getQuantityOnHand() - inventory.getReservedQuantity();
        if (availableToReserve < quantity) {
            throw new InsufficientInventoryException(itemId, quantity, availableToReserve);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Oslobađa rezervisanu količinu.
     */
    public Inventory releaseReservation(Long itemId, Double quantity) {
        Inventory inventory = getOrCreateForItem(itemId);
        double newReserved = Math.max(0, inventory.getReservedQuantity() - quantity);
        inventory.setReservedQuantity(newReserved);
        return inventoryRepository.save(inventory);
    }

    /**
     * Vraća dostupnu količinu (na stanju minus rezervisano).
     */
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

    // ========== NOVE METODE ZA AUTOMATIZACIJU MRP -> ZALIHE ==========

    /**
     * Rezervise sve BOM komponente za proizvodni nalog.
     * Poziva se kada nalog prelazi u RELEASED status.
     *
     * @param plannedOrderId ID planiranog naloga
     * @return Lista izvrsenih operacija rezervacije
     */
    public List<InventoryOperation> reserveComponents(Long plannedOrderId) {
        PlannedOrder order = plannedOrderRepository.findById(plannedOrderId)
                .orElseThrow(() -> new OrderNotFoundException(plannedOrderId));

        // Samo za PRODUCTION naloge
        if (order.getOrderType() != OrderType.PRODUCTION) {
            throw new IllegalArgumentException(
                    "Rezervacija komponenti moguca samo za PRODUCTION naloge");
        }

        List<InventoryOperation> operations = new ArrayList<>();

        // Dohvati BOM za artikal koji se proizvodi
        List<BomItem> components = bomService.findByParentId(order.getItem().getId());

        for (BomItem bomItem : components) {
            Item component = bomItem.getComponentItem();
            Double requiredQty = order.getQuantity() * bomItem.getQuantity();

            // Rezervisi komponentu u inventory
            reserve(component.getId(), requiredQty);

            // Sacuvaj zapis o rezervaciji
            OrderComponentReservation reservation = new OrderComponentReservation(
                    order, component, requiredQty);
            reservationRepository.save(reservation);

            operations.add(InventoryOperation.reserve(
                    component.getId(),
                    component.getSku(),
                    component.getName(),
                    requiredQty));
        }

        return operations;
    }

    /**
     * Izdaje komponente iz skladista u proizvodnju.
     * Smanjuje stanje i smanjuje rezervaciju.
     * Poziva se kada nalog prelazi u IN_PROGRESS status.
     *
     * @param plannedOrderId ID planiranog naloga
     * @return Lista izvrsenih operacija izdavanja
     */
    public List<InventoryOperation> issueComponents(Long plannedOrderId) {
        List<OrderComponentReservation> reservations =
                reservationRepository.findByPlannedOrderIdAndStatus(
                        plannedOrderId, ReservationStatus.ACTIVE);

        List<InventoryOperation> operations = new ArrayList<>();

        for (OrderComponentReservation reservation : reservations) {
            Item item = reservation.getItem();
            Double quantity = reservation.getReservedQuantity();

            // Dohvati inventory
            Inventory inventory = getOrCreateForItem(item.getId());

            // Skini sa stanja
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);

            // Skini rezervaciju
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventoryRepository.save(inventory);

            // Azuriraj status rezervacije
            reservation.setStatus(ReservationStatus.ISSUED);
            reservationRepository.save(reservation);

            operations.add(InventoryOperation.issue(
                    item.getId(), item.getSku(), item.getName(), quantity));
        }

        return operations;
    }

    /**
     * Prima gotov proizvod na skladiste.
     * Poziva se kada PRODUCTION nalog prelazi u COMPLETED status,
     * ili kada PURCHASE nalog prelazi u COMPLETED status.
     *
     * @param itemId ID artikla koji se prima
     * @param quantity Kolicina koja se prima
     * @return Operacija prijema
     */
    public InventoryOperation receiveFinishedProduct(Long itemId, Double quantity) {
        Inventory inventory = addToStock(itemId, quantity);
        Item item = inventory.getItem();

        return InventoryOperation.receive(
                item.getId(), item.getSku(), item.getName(), quantity);
    }

    /**
     * Oslobadja sve rezervacije za otkazani nalog.
     * Poziva se kada nalog prelazi u CANCELLED status (ako je bio RELEASED).
     *
     * @param plannedOrderId ID planiranog naloga
     * @return Lista izvrsenih operacija oslobadjanja
     */
    public List<InventoryOperation> releaseReservations(Long plannedOrderId) {
        List<OrderComponentReservation> reservations =
                reservationRepository.findByPlannedOrderIdAndStatus(
                        plannedOrderId, ReservationStatus.ACTIVE);

        List<InventoryOperation> operations = new ArrayList<>();

        for (OrderComponentReservation reservation : reservations) {
            Item item = reservation.getItem();
            Double quantity = reservation.getReservedQuantity();

            // Oslobodi rezervaciju u inventory
            releaseReservation(item.getId(), quantity);

            // Azuriraj status
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);

            operations.add(InventoryOperation.release(
                    item.getId(), item.getSku(), item.getName(), quantity));
        }

        return operations;
    }
}
