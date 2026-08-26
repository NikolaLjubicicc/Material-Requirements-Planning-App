package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateResponse;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateResponse.InventoryOperation;
import smartMRP.smart_mrp.entity.*;
import smartMRP.smart_mrp.exception.InsufficientInventoryException;
import smartMRP.smart_mrp.exception.InvalidStatusTransitionException;
import smartMRP.smart_mrp.exception.OrderNotFoundException;
import smartMRP.smart_mrp.repository.BomItemRepository;
import smartMRP.smart_mrp.repository.InventoryRepository;
import smartMRP.smart_mrp.repository.PlannedOrderRepository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class PlannedOrderService {

    private final PlannedOrderRepository plannedOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final BomItemRepository bomItemRepository;

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

        VALID_TRANSITIONS.put(OrderStatus.PLANNED,
                EnumSet.of(OrderStatus.RELEASED, OrderStatus.CANCELLED));

        VALID_TRANSITIONS.put(OrderStatus.RELEASED,
                EnumSet.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED));

        VALID_TRANSITIONS.put(OrderStatus.IN_PROGRESS,
                EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));

        VALID_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));

        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public PlannedOrderService(PlannedOrderRepository plannedOrderRepository,
                               InventoryRepository inventoryRepository,
                               BomItemRepository bomItemRepository) {
        this.plannedOrderRepository = plannedOrderRepository;
        this.inventoryRepository = inventoryRepository;
        this.bomItemRepository = bomItemRepository;
    }

    @Transactional
    public PlannedOrderStatusUpdateResponse updateStatus(Long orderId, OrderStatus newStatus) {
        PlannedOrder order = plannedOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        validateTransition(currentStatus, newStatus);

        List<InventoryOperation> inventoryOperations = new ArrayList<>();

        order.setStatus(newStatus);
        plannedOrderRepository.save(order);

        if (order.getOrderType() == OrderType.PURCHASE) {

            if (newStatus == OrderStatus.COMPLETED) {
                Inventory inventory = inventoryRepository.findByItemId(order.getItem().getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Nema inventory zapisa za artikal: " + order.getItem().getName()));

                inventory.setQuantityOnHand(inventory.getQuantityOnHand() + order.getQuantity());
                inventoryRepository.save(inventory);

                inventoryOperations.add(new InventoryOperation(
                        "RECEIVE",
                        order.getItem().getId(),
                        order.getItem().getSku(),
                        order.getItem().getName(),
                        order.getQuantity(),
                        "Primljena roba od dobavljaca"
                ));
            }
        }

        if (order.getOrderType() == OrderType.PRODUCTION) {

            if (newStatus == OrderStatus.RELEASED) {
                List<BomItem> components = bomItemRepository.findByParentItemId(order.getItem().getId());

                for (BomItem bom : components) {
                    Double needed = bom.getQuantity() * order.getQuantity();

                    Inventory inv = inventoryRepository.findByItemId(bom.getComponentItem().getId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Nema inventory za: " + bom.getComponentItem().getName()));

                    Double available = inv.getQuantityOnHand() - inv.getReservedQuantity();
                    if (available < needed) {
                        throw new InsufficientInventoryException(
                                bom.getComponentItem().getName(), needed, available);
                    }

                    inv.setReservedQuantity(inv.getReservedQuantity() + needed);
                    inventoryRepository.save(inv);

                    inventoryOperations.add(new InventoryOperation(
                            "RESERVE",
                            bom.getComponentItem().getId(),
                            bom.getComponentItem().getSku(),
                            bom.getComponentItem().getName(),
                            needed,
                            "Rezervisano za proizvodnju"
                    ));
                }
            }

            if (newStatus == OrderStatus.IN_PROGRESS) {
                List<BomItem> components = bomItemRepository.findByParentItemId(order.getItem().getId());

                for (BomItem bom : components) {
                    Double needed = bom.getQuantity() * order.getQuantity();

                    Inventory inv = inventoryRepository.findByItemId(bom.getComponentItem().getId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Nema inventory za: " + bom.getComponentItem().getName()));

                    inv.setQuantityOnHand(inv.getQuantityOnHand() - needed);
                    inv.setReservedQuantity(inv.getReservedQuantity() - needed);
                    inventoryRepository.save(inv);

                    inventoryOperations.add(new InventoryOperation(
                            "ISSUE",
                            bom.getComponentItem().getId(),
                            bom.getComponentItem().getSku(),
                            bom.getComponentItem().getName(),
                            needed,
                            "Izdato u proizvodnju"
                    ));
                }
            }

            if (newStatus == OrderStatus.COMPLETED) {
                Inventory inv = inventoryRepository.findByItemId(order.getItem().getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Nema inventory za: " + order.getItem().getName()));

                inv.setQuantityOnHand(inv.getQuantityOnHand() + order.getQuantity());
                inventoryRepository.save(inv);

                inventoryOperations.add(new InventoryOperation(
                        "RECEIVE",
                        order.getItem().getId(),
                        order.getItem().getSku(),
                        order.getItem().getName(),
                        order.getQuantity(),
                        "Proizveden gotov proizvod"
                ));
            }

            if (newStatus == OrderStatus.CANCELLED && currentStatus == OrderStatus.RELEASED) {
                List<BomItem> components = bomItemRepository.findByParentItemId(order.getItem().getId());

                for (BomItem bom : components) {
                    Double reserved = bom.getQuantity() * order.getQuantity();

                    Inventory inv = inventoryRepository.findByItemId(bom.getComponentItem().getId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Nema inventory za: " + bom.getComponentItem().getName()));

                    inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - reserved));
                    inventoryRepository.save(inv);

                    inventoryOperations.add(new InventoryOperation(
                            "RELEASE",
                            bom.getComponentItem().getId(),
                            bom.getComponentItem().getSku(),
                            bom.getComponentItem().getName(),
                            reserved,
                            "Oslobodjena rezervacija - nalog otkazan"
                    ));
                }
            }
        }

        PlannedOrderStatusUpdateResponse response = new PlannedOrderStatusUpdateResponse()
                .orderId(orderId)
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .orderType(order.getOrderType())
                .addOperations(inventoryOperations)
                .success(true)
                .message(buildSuccessMessage(order, currentStatus, newStatus, inventoryOperations));

        return response;
    }

    private void validateTransition(OrderStatus current, OrderStatus target) {
        Set<OrderStatus> allowedTargets = VALID_TRANSITIONS.get(current);

        if (allowedTargets == null || !allowedTargets.contains(target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
    }

    private String buildSuccessMessage(PlannedOrder order, OrderStatus from,
                                        OrderStatus to, List<InventoryOperation> operations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nalog ").append(order.getId())
                .append(" uspesno promenjen iz ").append(from)
                .append(" u ").append(to).append(".");

        if (!operations.isEmpty()) {
            sb.append(" Izvrseno ").append(operations.size())
                    .append(" inventory operacija.");
        }

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public PlannedOrder findById(Long id) {
        return plannedOrderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<PlannedOrder> findByStatus(OrderStatus status) {
        return plannedOrderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<PlannedOrder> findByOrderType(OrderType orderType) {
        return plannedOrderRepository.findByOrderType(orderType);
    }
}
