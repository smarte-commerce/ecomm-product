package com.winnguyen1905.product.core.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.micrometer.common.lang.NonNull;

public interface S3Service {
  List<String> putPackages(@NonNull final List<MultipartFile> packages);
}
