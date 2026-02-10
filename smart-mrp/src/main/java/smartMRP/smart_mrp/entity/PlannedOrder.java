package smartMRP.smart_mrp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Planirani nalog - rezultat MRP kalkulacije.
 * Predstavlja nalog za nabavku ili proizvodnju koji treba izvršiti.
 */
@Entity
@Table(name = "planned_orders")
public class PlannedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Artikal je obavezan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id")
    private ProductionPlan productionPlan;

    @NotNull(message = "Količina je obavezna")
    @Min(value = 0, message = "Količina ne može biti negativna")
    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @NotNull(message = "Tip naloga je obavezan")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @NotNull(message = "Datum početka je obavezan")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Datum završetka je obavezan")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLANNED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    public PlannedOrder() {
    }

    public PlannedOrder(Long id, Item item, ProductionPlan productionPlan, Double quantity,
                        OrderType orderType, LocalDate startDate, LocalDate dueDate,
                        OrderStatus status, LocalDate createdAt) {
        this.id = id;
        this.item = item;
        this.productionPlan = productionPlan;
        this.quantity = quantity;
        this.orderType = orderType;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public ProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(ProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
