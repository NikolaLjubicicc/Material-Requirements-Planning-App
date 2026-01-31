package smartMRP.smart_mrp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartMRP.smart_mrp.entity.Item;
import smartMRP.smart_mrp.entity.ItemCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Item> findByCategory(ItemCategory category);

    List<Item> findByNameContainingIgnoreCase(String name);
}
