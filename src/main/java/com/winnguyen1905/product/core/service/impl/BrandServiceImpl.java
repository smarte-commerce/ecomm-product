package com.winnguyen1905.product.core.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.winnguyen1905.product.core.mapper.BrandMapper;
import com.winnguyen1905.product.core.model.Brand;
import com.winnguyen1905.product.core.service.BrandService;
import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.repository.BrandRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

  private final BrandMapper brandMapper;
  private final BrandRepository brandRepository;

  @Override
  public Brand addBrand(UUID userId, Brand brand) {
    return Mono.just(this.brandMapper.toBrandEntity(brand))
        .map(eBrand -> this.brandRepository.save(eBrand))
        .map(this.brandMapper::toBrand).block();
  }

}
