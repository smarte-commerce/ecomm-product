package com.winnguyen1905.product.core.model.request;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import com.winnguyen1905.product.common.constant.ProductStatus;
import com.winnguyen1905.product.common.constant.ProductType;
import com.winnguyen1905.product.secure.RegionPartition;

import lombok.Builder;

@Builder
public record SearchProductRequest(
    List<Sort> sorts,
    String searchTerm,
    String keyword, // For compatibility
    List<Filter> filters,
    Pagination pagination,
    RegionPartition region,
    ProductType productType,
    ProductStatus status,
    Boolean isPublished
) implements AbstractModel {

  /**
   * Get search keyword (supports both searchTerm and keyword fields)
   */
  public String getKeyword() {
    if (keyword != null && !keyword.trim().isEmpty()) {
      return keyword.trim();
    }
    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      return searchTerm.trim();
    }
    return null;
  }

  /**
   * Get pagination with defaults
   */
  public Pagination getPage() {
    if (Objects.isNull(pagination)) {
      return new Pagination(0, 15);
    }
    return pagination;
  }
  
  /**
   * Convert to Spring Pageable
   */
  public Pageable getPageable() {
    Pagination page = getPage();
    
    if (sorts == null || sorts.isEmpty()) {
      return PageRequest.of(page.getPageNumber(), page.getPageSize());
    }
    
    org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
    for (Sort sort : sorts) {
      Direction direction = "desc".equalsIgnoreCase(sort.order()) ? Direction.DESC : Direction.ASC;
      springSort = springSort.and(org.springframework.data.domain.Sort.by(direction, sort.field()));
    }
    
    return PageRequest.of(page.getPageNumber(), page.getPageSize(), springSort);
  }

  @Builder
  public static record Pagination(
      int pageNum,
      int pageSize
  ) implements AbstractModel, Pageable {
    
    public Pagination {
      pageSize = pageSize <= 0 ? 15 : Math.min(pageSize, 100); // Max 100 items per page
      pageNum = Math.max(pageNum, 0); // Min page 0
    }
    
    /**
     * Get page number (0-based)
     */
    @Override
    public int getPageNumber() {
      return pageNum;
    }
    
    /**
     * Get page size
     */
    @Override
    public int getPageSize() {
      return pageSize;
    }
    
    @Override
    public long getOffset() {
      return (long) pageNum * pageSize;
    }
    
    @Override
    public org.springframework.data.domain.Sort getSort() {
      return org.springframework.data.domain.Sort.unsorted();
    }
    
    @Override
    public Pageable next() {
      return new Pagination(pageNum + 1, pageSize);
    }
    
    @Override
    public Pageable previousOrFirst() {
      return hasPrevious() ? new Pagination(pageNum - 1, pageSize) : this;
    }
    
    @Override
    public Pageable first() {
      return new Pagination(0, pageSize);
    }
    
    @Override
    public boolean hasPrevious() {
      return pageNum > 0;
    }
    
    @Override
    public Pageable withPage(int pageNumber) {
      return new Pagination(pageNumber, pageSize);
    }
  }

  @Builder
  public static record Filter(
      String field,
      List<String> values,
      String operator // eq, in, range, etc.
  ) implements AbstractModel {
  }

  @Builder
  public static record Sort(
      String order, // asc, desc
      String field
  ) implements AbstractModel {
  }
}
