package smartMRP.smart_mrp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Artikal je obavezan")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    @Min(value = 0, message = "Količina na stanju ne može biti negativna")
    @Column(name = "quantity_on_hand", nullable = false)
    private Double quantityOnHand = 0.0;

    @Min(value = 0, message = "Rezervisana količina ne može biti negativna")
    @Column(name = "reserved_quantity", nullable = false)
    private Double reservedQuantity = 0.0;

    public Inventory() {
    }

    public Inventory(Long id, Item item, Double quantityOnHand, Double reservedQuantity) {
        this.id = id;
        this.item = item;
        this.quantityOnHand = quantityOnHand;
        this.reservedQuantity = reservedQuantity;
    }

    public Double getAvailableQuantity() {
        return quantityOnHand - reservedQuantity;
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

    public Double getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Double quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Double getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Double reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
}
