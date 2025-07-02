package com.winnguyen1905.product.core.controller;

import com.winnguyen1905.product.core.model.request.ProductImageRequest;
import com.winnguyen1905.product.core.model.viewmodel.ProductImageResponse;
import com.winnguyen1905.product.persistance.entity.EProductImage;
import com.winnguyen1905.product.persistance.repository.ProductImageRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("images")
public class ProductImageController {

    // @Autowired
    // private ProductImageRepository imageRepository;

    // @Autowired
    // private ProductRepository productRepository;

    // @GetMapping
    // public ResponseEntity<Page<ProductImageResponse>> getProductImages(
    //         @PathVariable UUID productId,
    //         Pageable pageable) {
    //     return ResponseEntity.ok(imageRepository.findByProductId(productId, pageable)
    //             .map(this::mapToResponse));
    // }

    // @GetMapping("/{imageId}")
    // public ResponseEntity<ProductImageResponse> getImageById(
    //         @PathVariable UUID productId,
    //         @PathVariable UUID imageId) {
    //     return imageRepository.findByIdAndProductId(imageId, productId)
    //             .map(this::mapToResponse)
    //             .map(ResponseEntity::ok)
    //             .orElse(ResponseEntity.notFound().build());
    // }

    // @PostMapping
    // public ResponseEntity<ProductImageResponse> uploadImage(
    //         @PathVariable UUID productId,
    //         @RequestParam("file") MultipartFile file,
    //         @RequestParam("type") String imageType) {
    //     return productRepository.findById(productId)
    //             .map(product -> {
    //                 EProductImage image = createProductImage(product, file, imageType);
    //                 return ResponseEntity.ok(mapToResponse(imageRepository.save(image)));
    //             })
    //             .orElse(ResponseEntity.notFound().build());
    // }

    // @PutMapping("/{imageId}")
    // public ResponseEntity<ProductImageResponse> updateImage(
    //         @PathVariable UUID productId,
    //         @PathVariable UUID imageId,
    //         @RequestBody ProductImageRequest request) {
    //     return imageRepository.findByIdAndProductId(imageId, productId)
    //             .map(image -> updateImageFromRequest(image, request))
    //             .map(imageRepository::save)
    //             .map(this::mapToResponse)
    //             .map(ResponseEntity::ok)
    //             .orElse(ResponseEntity.notFound().build());
    // }

    // @DeleteMapping("/{imageId}")
    // public ResponseEntity<Void> deleteImage(
    //         @PathVariable UUID productId,
    //         @PathVariable UUID imageId) {
    //     return imageRepository.findByIdAndProductId(imageId, productId)
    //             .map(image -> {
    //                 imageRepository.delete(image);
    //                 return ResponseEntity.ok().<Void>build();
    //             })
    //             .orElse(ResponseEntity.notFound().build());
    // }

    // private ProductImageResponse mapToResponse(EProductImage image) {
    //     return new ProductImageResponse(
    //             image.getId(),
    //             image.getUrl(),
    //             image.getType(),
    //             image.getProductId(),
    //             image.getProductVariantId(),
    //             image.getVersion()
    //     );
    // }

    // private EProductImage createProductImage(EProduct product, MultipartFile file, String imageType) {
    //     // Implementation for file upload and storage
    //     String imageUrl = uploadFileToStorage(file);
        
    //     return EProductImage.builder()
    //             .product(product)
    //             .url(imageUrl)
    //             .type(ProductImageType.valueOf(imageType))
    //             .build();
    // }

    // private EProductImage updateImageFromRequest(EProductImage image, ProductImageRequest request) {
    //     image.setType(request.type());
    //     image.setProductVariantId(request.productVariantId());
    //     return image;
    // }

    // private String uploadFileToStorage(MultipartFile file) {
    //     // Implementation for file storage service
    //     throw new UnsupportedOperationException("File storage not implemented");
    // }
}
