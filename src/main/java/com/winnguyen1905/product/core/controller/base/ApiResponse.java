package com.winnguyen1905.product.core.controller.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API Response wrapper for consistent response format
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private ApiError error;
    private ApiMetadata metadata;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * Create successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Create successful response with data and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Create successful response with only message
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }
    
    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }
    
    /**
     * Create error response with details
     */
    public static <T> ApiResponse<T> error(String message, String code, List<String> details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .build();
    }
    
    /**
     * Create response with metadata (for paginated responses)
     */
    public static <T> ApiResponse<T> successWithMetadata(T data, String message, ApiMetadata metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }
    
    @Getter
    @Setter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {
        private String code;
        private String message;
        private List<String> details;
        private String field; // For validation errors
    }
    
    @Getter
    @Setter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiMetadata {
        // Pagination info
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
        private Boolean isFirstPage;
        private Boolean isLastPage;
        
        // Performance info
        private Long processingTimeMs;
        
        // API version info
        private String apiVersion;
        
        // Additional context
        private Object context;
        
        /**
         * Create pagination metadata
         */
        public static ApiMetadata pagination(int pageNumber, int pageSize, long totalElements, int totalPages, 
                                           boolean isFirstPage, boolean isLastPage) {
            return ApiMetadata.builder()
                    .pageNumber(pageNumber)
                    .pageSize(pageSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .isFirstPage(isFirstPage)
                    .isLastPage(isLastPage)
                    .build();
        }
        
        /**
         * Create performance metadata
         */
        public static ApiMetadata performance(long processingTimeMs) {
            return ApiMetadata.builder()
                    .processingTimeMs(processingTimeMs)
                    .build();
        }
    }
} 
