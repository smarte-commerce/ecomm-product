package com.winnguyen1905.product.secure;

import java.util.UUID;

import com.winnguyen1905.product.core.model.request.AbstractModel;

import lombok.Builder;

@Builder
public record TAccountRequest(
    UUID id,
    String username,
    AccountType accountType,
    UUID socketClientId, 
    RegionPartition region
) implements AbstractModel {

  @Builder
  public TAccountRequest(
      UUID id,
      String username,
      AccountType accountType,
      UUID socketClientId, 
      RegionPartition region) {
    this.id = id;
    this.username = username;
    this.accountType = accountType;
    this.region = region;
    this.socketClientId = socketClientId;
  }
  
  /**
   * Check if account has admin privileges
   */
  public boolean isAdmin() {
    return accountType == AccountType.ADMIN;
  }
  
  /**
   * Check if account is a vendor
   */
  public boolean isVendor() {
    return accountType == AccountType.VENDOR;
  }
  
  /**
   * Check if account is a customer
   */
  public boolean isCustomer() {
    return accountType == AccountType.CUSTOMER;
  }
}
