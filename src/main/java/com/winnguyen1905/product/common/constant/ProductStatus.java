package com.winnguyen1905.product.common.constant;

/**
 * Enum representing the various statuses a product can have in the system.
 * This supports multi-vendor scenarios where products may need approval workflows.
 */
public enum ProductStatus {
    /**
     * Product is in draft state, not visible to customers
     */
    DRAFT("Draft"),
    
    /**
     * Product is pending approval from admin/platform
     */
    PENDING_APPROVAL("Pending Approval"),
    
    /**
     * Product has been approved and is active/published
     */
    ACTIVE("Active"),
    
    /**
     * Product is temporarily inactive but can be reactivated
     */
    INACTIVE("Inactive"),
    
    /**
     * Product has been rejected by admin/platform
     */
    REJECTED("Rejected"),
    
    /**
     * Product is archived and no longer available
     */
    ARCHIVED("Archived"),
    
    /**
     * Product is out of stock across all variants
     */
    OUT_OF_STOCK("Out of Stock"),
    
    /**
     * Product is discontinued and will not be restocked
     */
    DISCONTINUED("Discontinued");
    
    private final String displayName;
    
    ProductStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if the product status allows customer visibility
     */
    public boolean isCustomerVisible() {
        return this == ACTIVE;
    }
    
    /**
     * Check if the product status allows vendor editing
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED || this == INACTIVE;
    }
    
    /**
     * Check if the product status requires admin approval
     */
    public boolean requiresApproval() {
        return this == PENDING_APPROVAL;
    }
}
