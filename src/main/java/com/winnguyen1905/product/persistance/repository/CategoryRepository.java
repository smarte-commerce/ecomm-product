package com.winnguyen1905.product.persistance.repository;

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
  Optional<ECategory> findByNameAndShopId(String name, UUID shopId);

  @Modifying
  @Query("""
      update categories as c
      set c.category_right = c.category_right + 2, c.category_left = c.category_left + 2
      where c.category_right >= :start and c.shop_id = :shopId
      """)
  @Transactional
  int updateCategoryTree(int start, UUID shopId);
}
