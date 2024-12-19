package com.winnguyen1905.product.core.service.vendor;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.micrometer.common.lang.NonNull;

public interface VendorS3Service {
  List<String> putPackages(@NonNull final List<MultipartFile> packages);
}
