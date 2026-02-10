package smartMRP.smart_mrp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.entity.PlanStatus;
import smartMRP.smart_mrp.entity.ProductionPlan;
import smartMRP.smart_mrp.exception.ItemNotFoundException;
import smartMRP.smart_mrp.exception.ResourceNotFoundException;
import smartMRP.smart_mrp.repository.ItemRepository;
import smartMRP.smart_mrp.repository.PlannedOrderRepository;
import smartMRP.smart_mrp.repository.ProductionPlanRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;
    private final ItemRepository itemRepository;
    private final PlannedOrderRepository plannedOrderRepository;

    public ProductionPlanService(ProductionPlanRepository productionPlanRepository,
                                  ItemRepository itemRepository,
                                  PlannedOrderRepository plannedOrderRepository) {
        this.productionPlanRepository = productionPlanRepository;
        this.itemRepository = itemRepository;
        this.plannedOrderRepository = plannedOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> findAll() {
        return productionPlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProductionPlan findById(Long id) {
        return productionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductionPlan", id));
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> findByStatus(PlanStatus status) {
        return productionPlanRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> findPendingPlans() {
        return productionPlanRepository.findPendingPlansOrderedByDueDate();
    }

    @Transactional(readOnly = true)
    public List<ProductionPlan> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return productionPlanRepository.findByDueDateBetween(startDate, endDate);
    }

    public ProductionPlan create(Long itemId, Double requiredQuantity, LocalDate dueDate) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        ProductionPlan plan = new ProductionPlan();
        plan.setItem(item);
        plan.setRequiredQuantity(requiredQuantity);
        plan.setDueDate(dueDate);
        plan.setStatus(PlanStatus.PENDING);
        plan.setCreatedAt(LocalDate.now());

        return productionPlanRepository.save(plan);
    }

    public ProductionPlan update(Long id, Double requiredQuantity, LocalDate dueDate) {
        ProductionPlan plan = findById(id);

        if (plan.getStatus() != PlanStatus.PENDING) {
            throw new IllegalStateException(
                    "Nije moguće izmeniti plan koji nije u PENDING statusu");
        }

        plan.setRequiredQuantity(requiredQuantity);
        plan.setDueDate(dueDate);

        return productionPlanRepository.save(plan);
    }

    public ProductionPlan updateStatus(Long id, PlanStatus newStatus) {
        ProductionPlan plan = findById(id);
        plan.setStatus(newStatus);
        return productionPlanRepository.save(plan);
    }

    public void delete(Long id) {
        ProductionPlan plan = findById(id);

        if (plan.getStatus() == PlanStatus.PROCESSING) {
            throw new IllegalStateException("Nije moguće obrisati plan koji je u obradi");
        }

        plannedOrderRepository.deleteByProductionPlanId(id);

        productionPlanRepository.delete(plan);
    }

    public ProductionPlan cancel(Long id) {
        ProductionPlan plan = findById(id);

        if (plan.getStatus() == PlanStatus.COMPLETED) {
            throw new IllegalStateException("Nije moguće otkazati završen plan");
        }

        plan.setStatus(PlanStatus.CANCELLED);
        return productionPlanRepository.save(plan);
    }
}
