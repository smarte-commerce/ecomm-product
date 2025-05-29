package com.winnguyen1905.product.util;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;

public class PaginationUtils {

  public static <T> Page<T> createPaginationFromDataListAndPageable(List<T> list, Pageable pageable) {
    Integer pageSize = pageable.getPageSize();
    Integer currentPage = pageable.getPageNumber();
    Integer startItem = currentPage * pageSize;
    List<T> paginatedList;
    if (list.size() < startItem)
      paginatedList = List.of();
    else {
      Integer toIndex = Math.min(startItem + pageSize, list.size());
      paginatedList = list.subList(startItem, toIndex);
    }

    return new PageImpl<>(paginatedList, pageable, list.size());
  }

  public static <T, S> PagedResponse<S> rawPaginationResponse(Page<T> page) {
    // PagedResponse<S> pageResponse = new PagedResponse<S>();
    // pageResponse.setPage(page.getNumber());
    // pageResponse.setSize(page.getSize());
    // pageResponse.setTotalElements(page.getNumberOfElements());
    // pageResponse.setTotalPages(page.getTotalPages());
    return null;
  }

}
