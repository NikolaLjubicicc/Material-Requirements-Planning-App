package smartMRP.smart_mrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartMRP.smart_mrp.entity.OrderComponentReservation;
import smartMRP.smart_mrp.entity.ReservationStatus;

import java.util.List;

@Repository
public interface OrderComponentReservationRepository extends JpaRepository<OrderComponentReservation, Long> {

    /**
     * Pronalazi sve rezervacije za dati planirani nalog.
     */
    @Query("SELECT r FROM OrderComponentReservation r WHERE r.plannedOrder.id = :plannedOrderId")
    List<OrderComponentReservation> findByPlannedOrderId(@Param("plannedOrderId") Long plannedOrderId);

    /**
     * Pronalazi rezervacije za nalog sa odredjenim statusom.
     */
    @Query("SELECT r FROM OrderComponentReservation r WHERE r.plannedOrder.id = :plannedOrderId AND r.status = :status")
    List<OrderComponentReservation> findByPlannedOrderIdAndStatus(
            @Param("plannedOrderId") Long plannedOrderId,
            @Param("status") ReservationStatus status);

    /**
     * Pronalazi sve rezervacije za dati artikal.
     */
    @Query("SELECT r FROM OrderComponentReservation r WHERE r.item.id = :itemId")
    List<OrderComponentReservation> findByItemId(@Param("itemId") Long itemId);

    /**
     * Proverava da li postoje aktivne rezervacije za nalog.
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM OrderComponentReservation r " +
           "WHERE r.plannedOrder.id = :plannedOrderId AND r.status = :status")
    boolean existsByPlannedOrderIdAndStatus(
            @Param("plannedOrderId") Long plannedOrderId,
            @Param("status") ReservationStatus status);
}
