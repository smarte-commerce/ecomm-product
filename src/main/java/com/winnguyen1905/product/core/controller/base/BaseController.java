package com.winnguyen1905.product.core.controller.base;

import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.secure.TAccountRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Base controller class providing common functionality for all controllers
 */
@Slf4j
public abstract class BaseController {

    /**
     * Create a successful response with data
     */
    protected <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Create a successful response with created status
     */
    protected <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    /**
     * Create a successful response with no content
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a paged response from Spring Data Page
     */
    protected <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build();
    }

    /**
     * Create a paged response from list and pagination info
     */
    protected <T> PagedResponse<T> toPagedResponse(List<T> content, Pageable pageable, long totalElements) {
        boolean hasNext = (pageable.getPageNumber() + 1) * pageable.getPageSize() < totalElements;
        
        return PagedResponse.<T>builder()
                .content(content)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / pageable.getPageSize()))
                .isLastPage(!hasNext)
                .build();
    }

    /**
     * Transform a page of one type to another
     */
    protected <T, R> PagedResponse<R> transformPage(Page<T> page, Function<T, R> transformer) {
        List<R> transformedContent = page.getContent().stream()
                .map(transformer)
                .toList();
        
        return PagedResponse.<R>builder()
                .content(transformedContent)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build();
    }

    /**
     * Log request with user context
     */
    protected void logRequest(String operation, TAccountRequest accountRequest) {
        log.info("Request: {} by user: {}", operation, getUserInfo(accountRequest));
    }

    /**
     * Log request with user context and additional info
     */
    protected void logRequest(String operation, TAccountRequest accountRequest, String additionalInfo) {
        log.info("Request: {} by user: {} - {}", operation, getUserInfo(accountRequest), additionalInfo);
    }

    /**
     * Log request with resource ID
     */
    protected void logRequest(String operation, UUID resourceId, TAccountRequest accountRequest) {
        log.info("Request: {} for resource: {} by user: {}", operation, resourceId, getUserInfo(accountRequest));
    }

    /**
     * Log public request (no authentication)
     */
    protected void logPublicRequest(String operation) {
        log.info("Public request: {}", operation);
    }

    /**
     * Log public request with resource ID
     */
    protected void logPublicRequest(String operation, UUID resourceId) {
        log.info("Public request: {} for resource: {}", operation, resourceId);
    }

    /**
     * Get user information for logging
     */
    private String getUserInfo(TAccountRequest accountRequest) {
        return accountRequest != null ? accountRequest.id().toString() : "anonymous";
    }

    /**
     * Validate vendor access - ensure user can only access their own resources
     */
    protected void validateVendorAccess(UUID vendorId, TAccountRequest accountRequest) {
        if (!accountRequest.id().equals(vendorId)) {
            throw new RuntimeException("Access denied: Cannot access other vendor's resources");
        }
    }

    /**
     * Check if user is admin
     */
    protected boolean isAdmin(TAccountRequest accountRequest) {
        return accountRequest != null && accountRequest.isAdmin();
    }

    /**
     * Check if user is vendor
     */
    protected boolean isVendor(TAccountRequest accountRequest) {
        return accountRequest != null && accountRequest.isVendor();
    }

    /**
     * Get current user ID safely
     */
    protected UUID getCurrentUserId(TAccountRequest accountRequest) {
        return accountRequest != null ? accountRequest.id() : null;
    }
} 
