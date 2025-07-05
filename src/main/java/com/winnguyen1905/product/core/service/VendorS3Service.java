package com.winnguyen1905.product.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.winnguyen1905.product.core.model.request.CreateProductImageRequest;
import com.winnguyen1905.product.core.model.viewmodel.PagedResponse;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.secure.TAccountRequest;

public interface VendorS3Service {
  
  List<String> putPackages(List<MultipartFile> files);
  
  PagedResponse<ProductImageResponse> getProductImages(UUID productId, Pageable pageable);
  
  ProductImageResponse getImageById(UUID imageId);
  
  ProductImageResponse uploadProductImage(MultipartFile file, CreateProductImageRequest request, TAccountRequest accountRequest);
  
  ProductImageResponse updateImageMetadata(UUID imageId, Object request, TAccountRequest accountRequest);
  
  void deleteImage(UUID imageId, TAccountRequest accountRequest);
  
  List<ProductImageResponse> bulkUploadProductImages(List<MultipartFile> files, UUID productId, UUID variantId, TAccountRequest accountRequest);
  
  ProductImageResponse setPrimaryImage(UUID imageId, TAccountRequest accountRequest);
}
