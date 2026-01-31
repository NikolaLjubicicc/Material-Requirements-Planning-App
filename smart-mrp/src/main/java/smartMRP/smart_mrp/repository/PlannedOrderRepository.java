package smartMRP.smart_mrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartMRP.smart_mrp.entity.*;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlannedOrderRepository extends JpaRepository<PlannedOrder, Long> {

    List<PlannedOrder> findByItem(Item item);

    List<PlannedOrder> findByItemId(Long itemId);

    List<PlannedOrder> findByOrderType(OrderType orderType);

    List<PlannedOrder> findByStatus(OrderStatus status);

    List<PlannedOrder> findByProductionPlan(ProductionPlan productionPlan);

    List<PlannedOrder> findByProductionPlanId(Long productionPlanId);

    /**
     * Pronalazi naloge po tipu i statusu.
     */
    List<PlannedOrder> findByOrderTypeAndStatus(OrderType orderType, OrderStatus status);

    /**
     * Pronalazi naloge čiji je startDate u zadatom periodu.
     */
    @Query("SELECT o FROM PlannedOrder o WHERE o.startDate BETWEEN :startDate AND :endDate ORDER BY o.startDate ASC")
    List<PlannedOrder> findByStartDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Pronalazi naloge čiji je dueDate u zadatom periodu.
     */
    @Query("SELECT o FROM PlannedOrder o WHERE o.dueDate BETWEEN :startDate AND :endDate ORDER BY o.dueDate ASC")
    List<PlannedOrder> findByDueDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Pronalazi sve PLANNED naloge za nabavku (lista za kupovinu).
     */
    @Query("SELECT o FROM PlannedOrder o WHERE o.orderType = 'PURCHASE' AND o.status = 'PLANNED' ORDER BY o.startDate ASC")
    List<PlannedOrder> findPurchaseOrdersToProcess();

    /**
     * Pronalazi sve PLANNED naloge za proizvodnju.
     */
    @Query("SELECT o FROM PlannedOrder o WHERE o.orderType = 'PRODUCTION' AND o.status = 'PLANNED' ORDER BY o.startDate ASC")
    List<PlannedOrder> findProductionOrdersToProcess();

    /**
     * Briše sve naloge vezane za određeni ProductionPlan.
     */
    void deleteByProductionPlanId(Long productionPlanId);
}
