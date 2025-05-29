package com.winnguyen1905.product.config;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.winnguyen1905.product.persistance.entity.EBrand;
import com.winnguyen1905.product.persistance.entity.ECategory;
import com.winnguyen1905.product.persistance.repository.BrandRepository;
import com.winnguyen1905.product.persistance.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;

  @Override
  public void run(String... args) throws Exception {
    EBrand brand = EBrand.builder()
        .name("Apple")
        .code("APPLE")
        .build();
        
    ECategory category = ECategory.builder()
        .name("Smartphone")
        .code("SMARTPHONE")
        .build();

    brandRepository.save(brand);
    categoryRepository.save(category);
  }
}
