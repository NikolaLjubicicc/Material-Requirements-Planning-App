package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateResponse;
import smartMRP.smart_mrp.dto.PlannedOrderStatusUpdateResponse.InventoryOperation;
import smartMRP.smart_mrp.entity.OrderStatus;
import smartMRP.smart_mrp.entity.OrderType;
import smartMRP.smart_mrp.entity.PlannedOrder;
import smartMRP.smart_mrp.exception.InvalidStatusTransitionException;
import smartMRP.smart_mrp.exception.OrderNotFoundException;
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
    private final InventoryService inventoryService;

    // Pravila za validne tranzicije statusa
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

        // PLANNED -> RELEASED ili CANCELLED
        VALID_TRANSITIONS.put(OrderStatus.PLANNED,
                EnumSet.of(OrderStatus.RELEASED, OrderStatus.CANCELLED));

        // RELEASED -> IN_PROGRESS ili CANCELLED
        VALID_TRANSITIONS.put(OrderStatus.RELEASED,
                EnumSet.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED));

        // IN_PROGRESS -> COMPLETED ili CANCELLED
        VALID_TRANSITIONS.put(OrderStatus.IN_PROGRESS,
                EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));

        // COMPLETED -> terminal (nema prelaza)
        VALID_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));

        // CANCELLED -> terminal (nema prelaza)
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public PlannedOrderService(PlannedOrderRepository plannedOrderRepository,
                               InventoryService inventoryService) {
        this.plannedOrderRepository = plannedOrderRepository;
        this.inventoryService = inventoryService;
    }

    /**
     * Azurira status planiranog naloga i izvrsava odgovarajuce inventory operacije.
     * Cela operacija je unutar jedne transakcije.
     *
     * @param orderId ID naloga
     * @param newStatus Novi status
     * @return Response sa detaljima izvrsenih operacija
     */
    @Transactional
    public PlannedOrderStatusUpdateResponse updateStatus(Long orderId, OrderStatus newStatus) {
        PlannedOrder order = plannedOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        // Validacija prelaza
        validateTransition(currentStatus, newStatus);

        // Pokreni response builder
        PlannedOrderStatusUpdateResponse response = new PlannedOrderStatusUpdateResponse()
                .orderId(orderId)
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .orderType(order.getOrderType());

        // Izvrsi inventory operacije na osnovu tipa naloga i novog statusa
        List<InventoryOperation> operations =
                executeInventoryOperations(order, currentStatus, newStatus);

        response.addOperations(operations);

        // Azuriraj status naloga
        order.setStatus(newStatus);
        plannedOrderRepository.save(order);

        response.success(true)
                .message(buildSuccessMessage(order, currentStatus, newStatus, operations));

        return response;
    }

    /**
     * Validira da li je prelaz iz currentStatus u newStatus dozvoljen.
     */
    private void validateTransition(OrderStatus current, OrderStatus target) {
        Set<OrderStatus> allowedTargets = VALID_TRANSITIONS.get(current);

        if (allowedTargets == null || !allowedTargets.contains(target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
    }

    /**
     * Izvrsava inventory operacije na osnovu tipa naloga i prelaza statusa.
     */
    private List<InventoryOperation> executeInventoryOperations(
            PlannedOrder order, OrderStatus from, OrderStatus to) {

        List<InventoryOperation> operations = new ArrayList<>();

        if (order.getOrderType() == OrderType.PURCHASE) {
            // PURCHASE nalozi
            operations.addAll(handlePurchaseOrderTransition(order, from, to));
        } else {
            // PRODUCTION nalozi
            operations.addAll(handleProductionOrderTransition(order, from, to));
        }

        return operations;
    }

    /**
     * PURCHASE nalog inventory operacije:
     * - COMPLETED: addToStock (prijem robe od dobavljaca)
     */
    private List<InventoryOperation> handlePurchaseOrderTransition(
            PlannedOrder order, OrderStatus from, OrderStatus to) {

        List<InventoryOperation> operations = new ArrayList<>();

        if (to == OrderStatus.COMPLETED) {
            // Prijem kupljene robe na skladiste
            InventoryOperation op = inventoryService.receiveFinishedProduct(
                    order.getItem().getId(), order.getQuantity());
            operations.add(op);
        }
        // Za CANCELLED nema inventory akcija za PURCHASE (nista nije rezervisano)

        return operations;
    }

    /**
     * PRODUCTION nalog inventory operacije:
     * - RELEASED: reserveComponents (rezervacija BOM komponenti)
     * - IN_PROGRESS: issueComponents (izdavanje komponenti u proizvodnju)
     * - COMPLETED: receiveFinishedProduct (prijem gotovog proizvoda)
     * - CANCELLED: releaseReservations (oslobadjanje rezervacija ako bio RELEASED)
     */
    private List<InventoryOperation> handleProductionOrderTransition(
            PlannedOrder order, OrderStatus from, OrderStatus to) {

        List<InventoryOperation> operations = new ArrayList<>();

        switch (to) {
            case RELEASED:
                // Rezervisi komponente iz BOM-a
                operations.addAll(inventoryService.reserveComponents(order.getId()));
                break;

            case IN_PROGRESS:
                // Izdaj komponente (skini sa stanja)
                operations.addAll(inventoryService.issueComponents(order.getId()));
                break;

            case COMPLETED:
                // Primi gotov proizvod na skladiste
                InventoryOperation receiveOp = inventoryService.receiveFinishedProduct(
                        order.getItem().getId(), order.getQuantity());
                operations.add(receiveOp);
                break;

            case CANCELLED:
                // Oslobodi rezervacije samo ako je bio u RELEASED statusu
                if (from == OrderStatus.RELEASED) {
                    operations.addAll(inventoryService.releaseReservations(order.getId()));
                }
                // Ako je IN_PROGRESS i otkazuje se - komponente su vec izdate,
                // u realnom sistemu bi se mogla dodati logika za vracanje na stanje
                break;

            default:
                break;
        }

        return operations;
    }

    /**
     * Gradi poruku o uspesnoj promeni statusa.
     */
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

    /**
     * Pronalazi nalog po ID-u.
     */
    @Transactional(readOnly = true)
    public PlannedOrder findById(Long id) {
        return plannedOrderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /**
     * Pronalazi sve naloge sa datim statusom.
     */
    @Transactional(readOnly = true)
    public List<PlannedOrder> findByStatus(OrderStatus status) {
        return plannedOrderRepository.findByStatus(status);
    }

    /**
     * Pronalazi sve naloge za dati tip.
     */
    @Transactional(readOnly = true)
    public List<PlannedOrder> findByOrderType(OrderType orderType) {
        return plannedOrderRepository.findByOrderType(orderType);
    }
}
