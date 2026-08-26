package smartMRP.smart_mrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.entity.PlanStatus;
import smartMRP.smart_mrp.entity.ProductionPlan;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {

    List<ProductionPlan> findByStatus(PlanStatus status);

    List<ProductionPlan> findByItem(Item item);

    List<ProductionPlan> findByItemId(Long itemId);

    @Query("SELECT p FROM ProductionPlan p WHERE p.dueDate BETWEEN :startDate AND :endDate")
    List<ProductionPlan> findByDueDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM ProductionPlan p WHERE p.status = 'PENDING' ORDER BY p.dueDate ASC")
    List<ProductionPlan> findPendingPlansOrderedByDueDate();

    List<ProductionPlan> findByStatusAndDueDateBefore(PlanStatus status, LocalDate date);
}
