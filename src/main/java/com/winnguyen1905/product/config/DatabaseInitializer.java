package com.winnguyen1905.product.config;

import java.util.UUID;
import java.util.Locale.Category;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        .shopId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
        .build();
    brandRepository.save(brand);
    categoryRepository.save(category);
  }
}
