package smartMRP.smart_mrp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Prati rezervacije komponenti za planirane naloge.
 * Omogucava oslobadjanje tacne rezervisane kolicine kada se nalog otkazuje.
 */
@Entity
@Table(name = "order_component_reservations",
       indexes = {
           @Index(name = "idx_ocr_planned_order", columnList = "planned_order_id"),
           @Index(name = "idx_ocr_item", columnList = "item_id")
       })
public class OrderComponentReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Planirani nalog je obavezan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_order_id", nullable = false)
    private PlannedOrder plannedOrder;

    @NotNull(message = "Artikal je obavezan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull(message = "Rezervisana kolicina je obavezna")
    @Min(value = 0, message = "Rezervisana kolicina ne moze biti negativna")
    @Column(name = "reserved_quantity", nullable = false)
    private Double reservedQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OrderComponentReservation() {
    }

    public OrderComponentReservation(PlannedOrder plannedOrder, Item item, Double reservedQuantity) {
        this.plannedOrder = plannedOrder;
        this.item = item;
        this.reservedQuantity = reservedQuantity;
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlannedOrder getPlannedOrder() {
        return plannedOrder;
    }

    public void setPlannedOrder(PlannedOrder plannedOrder) {
        this.plannedOrder = plannedOrder;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Double getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Double reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
