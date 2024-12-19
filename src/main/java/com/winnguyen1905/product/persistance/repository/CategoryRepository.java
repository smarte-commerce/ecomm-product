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
  Optional<ECategory> findByIdAndShopId(UUID id, UUID shopId);

  @Query(value = "update categories c " +
      "set c.category_left = case " +
          "when c.category_left > :start then c.category_left + 2 " +
          "else c.category_left " +
      "end, " +
      "c.category_right = case " +
          "when c.category_right >= :start then c.category_right + 2 " +
          "else c.category_right " +
      "end " +
      "where c.shop_id = :shopId " +
      "and ( " +
          "(c.category_left > :start and c.category_right > :start) or " +
          "(c.category_left < :start and c.category_right >= :start) " +
      ")", nativeQuery = true)
  Long updateCategoryTreeOfShop(long start, UUID shopId);

  Optional<ECategory> findTopByShopIdOrderByRightDesc(UUID shopId);

  Long countByShopId(UUID shopId);
}
