package com.winnguyen1905.product.core.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseObject<T> extends AbstractModel {
  private UUID id;

  @JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7")
  private String createdDate;

  @JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7")
  private String updatedDate;

  private String createdBy;

  private String updatedBy;

  private Boolean isDeleted;
}
