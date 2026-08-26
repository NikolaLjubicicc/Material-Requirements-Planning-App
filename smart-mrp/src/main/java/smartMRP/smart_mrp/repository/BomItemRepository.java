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

    List<BomItem> findByParentItem(Item parentItem);

    @Query("SELECT b FROM BomItem b WHERE b.parentItem.id = :parentId")
    List<BomItem> findByParentItemId(@Param("parentId") Long parentId);

    List<BomItem> findByComponentItem(Item componentItem);

    @Query("SELECT b FROM BomItem b WHERE b.componentItem.id = :componentId")
    List<BomItem> findByComponentItemId(@Param("componentId") Long componentId);

    boolean existsByParentItemAndComponentItem(Item parentItem, Item componentItem);

    Optional<BomItem> findByParentItemAndComponentItem(Item parentItem, Item componentItem);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BomItem b " +
           "WHERE b.parentItem.id = :itemId OR b.componentItem.id = :itemId")
    boolean isItemUsedInBom(@Param("itemId") Long itemId);
}
