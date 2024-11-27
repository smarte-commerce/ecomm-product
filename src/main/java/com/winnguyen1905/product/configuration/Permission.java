package com.winnguyen1905.product.configuration;

import com.winnguyen1905.product.core.model.BaseObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission extends BaseObject<Permission> {
  private String name;

  private String code;

  private String apiPath;

  private String method;

  private String module;
}
