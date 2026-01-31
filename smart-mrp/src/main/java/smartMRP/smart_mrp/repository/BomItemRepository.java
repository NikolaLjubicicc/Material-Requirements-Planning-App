package smartMRP.smart_mrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartMRP.smart_mrp.entity.BomItem;
import smartMRP.smart_mrp.entity.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface BomItemRepository extends JpaRepository<BomItem, Long> {

    /**
     * Pronalazi sve komponente za dati roditelj artikal.
     */
    List<BomItem> findByParentItem(Item parentItem);

    /**
     * Pronalazi sve komponente po ID-u roditelja.
     */
    @Query("SELECT b FROM BomItem b WHERE b.parentItem.id = :parentId")
    List<BomItem> findByParentItemId(@Param("parentId") Long parentId);

    /**
     * Pronalazi sve sastavnice gde je artikal komponenta (gde se koristi).
     */
    List<BomItem> findByComponentItem(Item componentItem);

    /**
     * Pronalazi sve sastavnice gde je artikal komponenta po ID-u.
     */
    @Query("SELECT b FROM BomItem b WHERE b.componentItem.id = :componentId")
    List<BomItem> findByComponentItemId(@Param("componentId") Long componentId);

    /**
     * Proverava da li postoji veza između roditelja i komponente.
     */
    boolean existsByParentItemAndComponentItem(Item parentItem, Item componentItem);

    /**
     * Pronalazi specifičnu BOM vezu.
     */
    Optional<BomItem> findByParentItemAndComponentItem(Item parentItem, Item componentItem);

    /**
     * Proverava da li se artikal koristi bilo gde u BOM-u (kao roditelj ili komponenta).
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BomItem b " +
           "WHERE b.parentItem.id = :itemId OR b.componentItem.id = :itemId")
    boolean isItemUsedInBom(@Param("itemId") Long itemId);
}
