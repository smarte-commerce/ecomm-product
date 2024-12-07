package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.persistance.entity.ECategory;

@Repository
public interface CategoryRepository extends JpaRepository<ECategory, UUID> {
  List<ECategory> findAllByShopId(UUID shopId);
  List<ECategory> findAllByShopIdAndPageable(UUID shopId);
  Optional<ECategory> findByIdAndShopId(UUID id, UUID shopId);

  @Modifying
  @Query("""
      update categories as c
      set c.category_right = c.category_right + 2, c.category_left = c.category_left + 2
      where c.category_left > :start and c.shop_id = :shopId
      """)
  @Transactional
  Long updateCategoryTreeOfShop(Long start, UUID shopId);

  Optional<ECategory> findTopByShopIdOrderByRightDesc(UUID shopId);

  Long countByShopId(UUID shopId);
}
