package com.winnguyen1905.product.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vendor")
public class VendorController {

  // @GetMapping("/products")
  // public ResponseEntity<List<Product>> listProducts() {
  //   return ResponseEntity.ok().build();
  // }

  // @PostMapping("/products")
  // public ResponseEntity<Product> createProduct(@RequestBody Product product) {
  //   return ResponseEntity.status(HttpStatus.CREATED).build();
  // }

  // @GetMapping("/products/{productId}")
  // public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
  //   return ResponseEntity.ok().build();
  // }

  // @PutMapping("/products/{productId}")
  // public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product product) {
  //   return ResponseEntity.ok().build();
  // }

  // @DeleteMapping("/products/{productId}")
  // public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
  //   return ResponseEntity.noContent().build();
  // }
}
