package smartMRP.smart_mrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartMRP.smart_mrp.entity.Inventory;
import smartMRP.smart_mrp.entity.Item;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByItem(Item item);

    Optional<Inventory> findByItemId(Long itemId);

    boolean existsByItem(Item item);

    boolean existsByItemId(Long itemId);

    /**
     * Ažurira količinu na stanju za artikal.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityOnHand = :quantity WHERE i.item.id = :itemId")
    int updateQuantityOnHand(@Param("itemId") Long itemId, @Param("quantity") Double quantity);

    /**
     * Ažurira rezervisanu količinu za artikal.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = :quantity WHERE i.item.id = :itemId")
    int updateReservedQuantity(@Param("itemId") Long itemId, @Param("quantity") Double quantity);
}
