package com.winnguyen1905.product.config;

import java.util.List;

import com.winnguyen1905.product.core.model.BaseObject;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Role extends BaseObject<Role> {
  private String name;
  private String code;
  private List<Permission> permissions;
  private List<String> permissionCodes;
}
