package com.winnguyen1905.product.core.service;

import java.util.UUID;

import com.winnguyen1905.product.core.model.Brand;

public interface BrandService {
  Brand addBrand(UUID userId, Brand brand);
}
