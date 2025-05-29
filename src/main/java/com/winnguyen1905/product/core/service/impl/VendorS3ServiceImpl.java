package com.winnguyen1905.product.core.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.winnguyen1905.product.core.service.VendorS3Service;
import com.winnguyen1905.product.exception.S3FileException;

import java.io.IOException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class VendorS3ServiceImpl implements VendorS3Service {

  private static S3Client s3Client = null;
  private static final String BUCKET_NAME = "product-images";

  private static S3Client getClient() {
    if (Objects.isNull(s3Client)) {
      Region region = Region.CA_CENTRAL_1;
      s3Client = S3Client.builder()
          .credentialsProvider(DefaultCredentialsProvider.create())
          .region(region)
          .build();
    }
    return s3Client;
  }

  private String putObject(MultipartFile file) {

    PutObjectRequest putOb = getPutObjectRequest(file);
    try {
      getClient().putObject(putOb, RequestBody.fromBytes(file.getBytes()));
      var s3key = putOb.getValueForField("Key", String.class).orElseThrow();

      log.info(
          "[{}] File {} uploaded at path {}",
          MDC.get("requestId"),
          file.getOriginalFilename(),
          s3key);

      GetUrlRequest request = GetUrlRequest.builder().bucket(BUCKET_NAME).key(s3key).build();

      URL url = getClient().utilities().getUrl(request);
      return url.toString();
    } catch (IOException e) {
      throw new S3FileException("Could not upload file", HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  @Override
  public List<String> putPackages(List<MultipartFile> files) {
    return files.stream().map(this::putObject).collect(Collectors.toList());
  }

  private PutObjectRequest getPutObjectRequest(MultipartFile file) {
    return PutObjectRequest.builder()
        .bucket(BUCKET_NAME)
        .key(Objects.requireNonNull(file.getOriginalFilename()))
        .build();
  }

}
