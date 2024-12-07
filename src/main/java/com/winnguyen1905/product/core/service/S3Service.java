package com.winnguyen1905.product.core.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
  List<MultipartFile> putPackages(List<MultipartFile> packages);
}
