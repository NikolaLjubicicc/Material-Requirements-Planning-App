package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.entity.*;
import smartMRP.smart_mrp.repository.*;

import java.time.LocalDate;
import java.util.*;

/**
 * MRP Engine - Glavni servis za Material Requirements Planning.
 *
 * Proces MRP kalkulacije:
 * 1. Eksplozija (Explosion) - rekurzivno prolazi kroz BOM
 * 2. Netiranje (Netting) - računa neto potrebe uzimajući u obzir zalihe
 * 3. Lead Time Offsetting - računa datume unazad
 * 4. Generisanje naloga - kreira PlannedOrder zapise
 */
@Service
@Transactional
public class MrpService {

    private final ProductionPlanRepository productionPlanRepository;
    private final BomItemRepository bomItemRepository;
    private final InventoryRepository inventoryRepository;
    private final PlannedOrderRepository plannedOrderRepository;
    private final ItemRepository itemRepository;

    public MrpService(ProductionPlanRepository productionPlanRepository,
                      BomItemRepository bomItemRepository,
                      InventoryRepository inventoryRepository,
                      PlannedOrderRepository plannedOrderRepository,
                      ItemRepository itemRepository) {
        this.productionPlanRepository = productionPlanRepository;
        this.bomItemRepository = bomItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.plannedOrderRepository = plannedOrderRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Pokreće MRP kalkulaciju za jedan ProductionPlan.
     * Ovo je glavna metoda koja orkestira ceo proces.
     */
    public MrpResult runMrp(Long productionPlanId) {
        ProductionPlan plan = productionPlanRepository.findById(productionPlanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProductionPlan sa ID-jem " + productionPlanId + " ne postoji"));

        // Postavi status na PROCESSING
        plan.setStatus(PlanStatus.PROCESSING);
        productionPlanRepository.save(plan);

        try {
            // Obriši prethodne naloge za ovaj plan (ako postoje)
            plannedOrderRepository.deleteByProductionPlanId(productionPlanId);

            // 1. Eksplozija BOM-a - dobijamo sve potrebne komponente sa količinama
            Map<Long, BomRequirement> requirements = new HashMap<>();
            explodeBom(plan.getItem().getId(), plan.getRequiredQuantity(),
                       plan.getDueDate(), requirements);

            // Dodaj i sam glavni artikal ako ima BOM (znači da se proizvodi)
            List<BomItem> mainItemBom = bomItemRepository.findByParentItemId(plan.getItem().getId());
            if (!mainItemBom.isEmpty()) {
                // Glavni artikal je proizvod koji se sastavlja
                addRequirement(requirements, plan.getItem().getId(),
                              plan.getRequiredQuantity(), plan.getDueDate());
            }

            // 2. Netiranje - izračunaj neto potrebe
            Map<Long, NetRequirement> netRequirements = calculateNetRequirements(requirements);

            // 3. Lead Time Offsetting + Generisanje naloga
            List<PlannedOrder> generatedOrders = generatePlannedOrders(netRequirements, plan);

            // Postavi status na COMPLETED
            plan.setStatus(PlanStatus.COMPLETED);
            productionPlanRepository.save(plan);

            return new MrpResult(plan, generatedOrders, netRequirements.size());

        } catch (Exception e) {
            // Vrati status na PENDING u slučaju greške
            plan.setStatus(PlanStatus.PENDING);
            productionPlanRepository.save(plan);
            throw new RuntimeException("Greška prilikom MRP kalkulacije: " + e.getMessage(), e);
        }
    }

    /**
     * Rekurzivna eksplozija BOM-a.
     * Za svaki artikal pronalazi komponente i računa potrebne količine.
     */
    private void explodeBom(Long itemId, Double quantity, LocalDate dueDate,
                           Map<Long, BomRequirement> requirements) {

        List<BomItem> components = bomItemRepository.findByParentItemId(itemId);

        for (BomItem bomItem : components) {
            Long componentId = bomItem.getComponentItem().getId();
            Double requiredQty = quantity * bomItem.getQuantity();

            // Izračunaj datum potrebe za komponentu (uzimajući u obzir lead time roditelja)
            Item parentItem = itemRepository.findById(itemId).orElse(null);
            int parentLeadTime = (parentItem != null) ? parentItem.getLeadTimeDays() : 0;
            LocalDate componentDueDate = dueDate.minusDays(parentLeadTime);

            // Dodaj potrebu za komponentu
            addRequirement(requirements, componentId, requiredQty, componentDueDate);

            // Rekurzivno eksplodiraj ako komponenta ima svoje komponente
            explodeBom(componentId, requiredQty, componentDueDate, requirements);
        }
    }

    private void addRequirement(Map<Long, BomRequirement> requirements,
                               Long itemId, Double quantity, LocalDate dueDate) {
        requirements.compute(itemId, (key, existing) -> {
            if (existing == null) {
                return new BomRequirement(itemId, quantity, dueDate);
            } else {
                existing.addQuantity(quantity);
                // Uzmi raniji datum ako je potrebno ranije
                if (dueDate.isBefore(existing.getDueDate())) {
                    existing.setDueDate(dueDate);
                }
                return existing;
            }
        });
    }

    /**
     * Netiranje - računa neto potrebe.
     * Formula: Neto = Bruto - (Zaliha - SafetyStock)
     */
    private Map<Long, NetRequirement> calculateNetRequirements(
            Map<Long, BomRequirement> grossRequirements) {

        Map<Long, NetRequirement> netRequirements = new HashMap<>();

        for (Map.Entry<Long, BomRequirement> entry : grossRequirements.entrySet()) {
            Long itemId = entry.getKey();
            BomRequirement gross = entry.getValue();

            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null) continue;

            // Dohvati zalihe
            Double onHand = inventoryRepository.findByItemId(itemId)
                    .map(Inventory::getAvailableQuantity)
                    .orElse(0.0);

            Double safetyStock = item.getSafetyStock();

            // Neto = Bruto - (OnHand - SafetyStock)
            Double netQty = gross.getQuantity() - (onHand - safetyStock);

            // Ako je neto > 0, imamo potrebu
            if (netQty > 0) {
                netRequirements.put(itemId, new NetRequirement(
                        item,
                        gross.getQuantity(),
                        netQty,
                        onHand,
                        gross.getDueDate()
                ));
            }
        }

        return netRequirements;
    }

    /**
     * Lead Time Offsetting + Generisanje PlannedOrder zapisa.
     */
    private List<PlannedOrder> generatePlannedOrders(
            Map<Long, NetRequirement> netRequirements, ProductionPlan plan) {

        List<PlannedOrder> orders = new ArrayList<>();

        for (NetRequirement req : netRequirements.values()) {
            Item item = req.getItem();

            // Izračunaj datum početka (Lead Time Offsetting)
            LocalDate startDate = req.getDueDate().minusDays(item.getLeadTimeDays());

            // Odredi tip naloga na osnovu kategorije artikla
            OrderType orderType;
            if (item.getCategory() == ItemCategory.RAW_MATERIAL) {
                orderType = OrderType.PURCHASE;
            } else {
                // SEMI_FINISHED i FINISHED_PRODUCT se proizvode
                orderType = OrderType.PRODUCTION;
            }

            PlannedOrder order = new PlannedOrder();
            order.setItem(item);
            order.setProductionPlan(plan);
            order.setQuantity(req.getNetQuantity());
            order.setOrderType(orderType);
            order.setStartDate(startDate);
            order.setDueDate(req.getDueDate());
            order.setStatus(OrderStatus.PLANNED);
            order.setCreatedAt(LocalDate.now());

            orders.add(plannedOrderRepository.save(order));
        }

        return orders;
    }

    /**
     * Vraća sve PLANNED naloge za nabavku (lista za kupovinu).
     */
    @Transactional(readOnly = true)
    public List<PlannedOrder> getPurchaseOrders() {
        return plannedOrderRepository.findPurchaseOrdersToProcess();
    }

    /**
     * Vraća sve PLANNED naloge za proizvodnju.
     */
    @Transactional(readOnly = true)
    public List<PlannedOrder> getProductionOrders() {
        return plannedOrderRepository.findProductionOrdersToProcess();
    }

    /**
     * Vraća sve naloge za određeni ProductionPlan.
     */
    @Transactional(readOnly = true)
    public List<PlannedOrder> getOrdersByPlan(Long planId) {
        return plannedOrderRepository.findByProductionPlanId(planId);
    }

    /**
     * Vraća naloge u zadatom vremenskom periodu.
     */
    @Transactional(readOnly = true)
    public List<PlannedOrder> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return plannedOrderRepository.findByStartDateBetween(startDate, endDate);
    }

    // ============ Pomoćne klase ============

    /**
     * Bruto potreba iz BOM eksplozije.
     */
    public static class BomRequirement {
        private Long itemId;
        private Double quantity;
        private LocalDate dueDate;

        public BomRequirement(Long itemId, Double quantity, LocalDate dueDate) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.dueDate = dueDate;
        }

        public void addQuantity(Double qty) {
            this.quantity += qty;
        }

        public Long getItemId() { return itemId; }
        public Double getQuantity() { return quantity; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    }

    /**
     * Neto potreba nakon netiranja.
     */
    public static class NetRequirement {
        private Item item;
        private Double grossQuantity;
        private Double netQuantity;
        private Double onHandQuantity;
        private LocalDate dueDate;

        public NetRequirement(Item item, Double grossQuantity, Double netQuantity,
                             Double onHandQuantity, LocalDate dueDate) {
            this.item = item;
            this.grossQuantity = grossQuantity;
            this.netQuantity = netQuantity;
            this.onHandQuantity = onHandQuantity;
            this.dueDate = dueDate;
        }

        public Item getItem() { return item; }
        public Double getGrossQuantity() { return grossQuantity; }
        public Double getNetQuantity() { return netQuantity; }
        public Double getOnHandQuantity() { return onHandQuantity; }
        public LocalDate getDueDate() { return dueDate; }
    }

    /**
     * Rezultat MRP kalkulacije.
     */
    public static class MrpResult {
        private ProductionPlan plan;
        private List<PlannedOrder> generatedOrders;
        private int totalItemsProcessed;

        public MrpResult(ProductionPlan plan, List<PlannedOrder> generatedOrders,
                        int totalItemsProcessed) {
            this.plan = plan;
            this.generatedOrders = generatedOrders;
            this.totalItemsProcessed = totalItemsProcessed;
        }

        public ProductionPlan getPlan() { return plan; }
        public List<PlannedOrder> getGeneratedOrders() { return generatedOrders; }
        public int getTotalItemsProcessed() { return totalItemsProcessed; }
        public int getOrderCount() { return generatedOrders.size(); }
    }
}
