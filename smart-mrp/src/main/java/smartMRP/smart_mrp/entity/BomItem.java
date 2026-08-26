package smartMRP.smart_mrp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bom_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parent_item_id", "component_item_id"})
})
public class BomItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Roditelj artikal je obavezan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id", nullable = false)
    private Item parentItem;

    @NotNull(message = "Komponenta je obavezna")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_item_id", nullable = false)
    private Item componentItem;

    @NotNull(message = "Količina je obavezna")
    @Min(value = 0, message = "Količina ne može biti negativna")
    @Column(nullable = false)
    private Double quantity;

    public BomItem() {
    }

    public BomItem(Long id, Item parentItem, Item componentItem, Double quantity) {
        this.id = id;
        this.parentItem = parentItem;
        this.componentItem = componentItem;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getParentItem() {
        return parentItem;
    }

    public void setParentItem(Item parentItem) {
        this.parentItem = parentItem;
    }

    public Item getComponentItem() {
        return componentItem;
    }

    public void setComponentItem(Item componentItem) {
        this.componentItem = componentItem;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
