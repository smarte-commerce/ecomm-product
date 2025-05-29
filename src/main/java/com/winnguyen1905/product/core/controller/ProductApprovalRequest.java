package com.winnguyen1905.product.core.controller;

// Request DTO for product approval
public record ProductApprovalRequest(
    Boolean isPublished,
    String rejectionReason
) {}
