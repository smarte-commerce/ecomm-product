package com.winnguyen1905.product.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.repository.custom.ProductQueryRewriter;
import com.winnguyen1905.product.persistance.repository.custom.SoftDeleteRepository;

@Repository
public interface ProductRepository
    extends SoftDeleteRepository<EProduct, UUID>, JpaRepository<EProduct, UUID>, JpaSpecificationExecutor<EProduct> {
  void deleteByIdIn(List<UUID> ids);

  Page<EProduct> findAll(Specification<EProduct> specification, Pageable pageable);

  @Query(value = """
      select p.* from products as p inner join discount_products as dp on dp.product_id = p.id
      inner join discounts as d on d.id = dp.discount_id where d.id = :discountId and is_published = true
      """, nativeQuery = true, queryRewriter = ProductQueryRewriter.class)
  Page<EProduct> findByDiscountIdAndIsPublishedTrue(UUID discountId, Pageable pageable);

  Optional<EProduct> findByIdAndIsPublishedTrue(UUID id);

  List<EProduct> findByIdInAndShopId(List<UUID> ids, UUID shopId); // REAL

  List<EProduct> findByIdInAndShopIdOrderById(List<UUID> ids, UUID shopId);

  Page<EProduct> findAllByShopIdAndIsPublishedTrue(UUID shopId, Pageable pageable);

}
