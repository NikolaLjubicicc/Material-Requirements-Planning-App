package smartMRP.smart_mrp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Plan proizvodnje.
 * Definiše šta treba proizvesti, u kojoj količini i do kog datuma.
 */
@Entity
@Table(name = "production_plans")
public class ProductionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Artikal je obavezan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull(message = "Količina je obavezna")
    @Min(value = 1, message = "Količina mora biti najmanje 1")
    @Column(name = "required_quantity", nullable = false)
    private Double requiredQuantity;

    @NotNull(message = "Datum potrebe je obavezan")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    // Prazan konstruktor
    public ProductionPlan() {
    }

    // Konstruktor sa svim poljima
    public ProductionPlan(Long id, Item item, Double requiredQuantity,
                          LocalDate dueDate, PlanStatus status, LocalDate createdAt) {
        this.id = id;
        this.item = item;
        this.requiredQuantity = requiredQuantity;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getteri i Setteri
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

    public Double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(Double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
