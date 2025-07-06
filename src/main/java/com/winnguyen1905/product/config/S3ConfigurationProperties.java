package com.winnguyen1905.product.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * S3 Configuration Properties
 * Binds configuration from application.yaml aws.s3 section
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Validated
public class S3ConfigurationProperties {

    @NotBlank(message = "S3 region is required")
    private String region = "ca-central-1";

    private String accessKey;
    private String secretKey;
    private boolean useIamRole = true;

    @Valid
    private Bucket bucket = new Bucket();

    @Valid
    private Upload upload = new Upload();

    @Valid
    private Cdn cdn = new Cdn();

    @Valid
    private Connection connection = new Connection();

    @Valid
    private ImageProcessing imageProcessing = new ImageProcessing();

    @Data
    public static class Bucket {
        @NotBlank(message = "Bucket name is required")
        private String name = "product-images";

        @NotBlank(message = "Product images bucket is required")
        private String productImages = "product-images";
    }

    @Data
    public static class Upload {
        @NotBlank(message = "Max file size is required")
        private String maxFileSize = "50MB";

        @NotNull(message = "Allowed content types are required")
        private List<String> allowedContentTypes = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", 
            "image/webp", "image/bmp", "image/tiff"
        );

        @NotNull(message = "Allowed extensions are required")
        private List<String> allowedExtensions = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff"
        );

        @Positive(message = "Max files per upload must be positive")
        private int maxFilesPerUpload = 10;
    }

    @Data
    public static class Cdn {
        private boolean enabled = false;
        private String domain;
    }

    @Data
    public static class Connection {
        @Positive(message = "Connection timeout must be positive")
        private int timeout = 30000;

        @Positive(message = "Socket timeout must be positive")
        private int socketTimeout = 30000;

        @Positive(message = "Max connections must be positive")
        private int maxConnections = 50;

        @Min(value = 1, message = "Max error retry must be at least 1")
        @Max(value = 10, message = "Max error retry cannot exceed 10")
        private int maxErrorRetry = 3;
    }

    @Data
    public static class ImageProcessing {
        private boolean enabled = true;

        @Min(value = 1, message = "Quality must be at least 1")
        @Max(value = 100, message = "Quality cannot exceed 100")
        private int quality = 85;

        @Valid
        private Thumbnails thumbnails = new Thumbnails();

        @Data
        public static class Thumbnails {
            private boolean enabled = true;

            @NotNull(message = "Thumbnail sizes are required")
            private List<ThumbnailSize> sizes = List.of(
                new ThumbnailSize("thumbnail", 150, 150),
                new ThumbnailSize("small", 300, 300),
                new ThumbnailSize("medium", 600, 600),
                new ThumbnailSize("large", 1200, 1200)
            );

            @Data
            public static class ThumbnailSize {
                @NotBlank(message = "Thumbnail name is required")
                private String name;

                @Positive(message = "Width must be positive")
                private int width;

                @Positive(message = "Height must be positive")
                private int height;

                public ThumbnailSize() {}

                public ThumbnailSize(String name, int width, int height) {
                    this.name = name;
                    this.width = width;
                    this.height = height;
                }
            }
        }
    }

    /**
     * Get the primary bucket name for product images
     */
    public String getPrimaryBucketName() {
        return bucket.getProductImages();
    }

    /**
     * Check if CDN is enabled and configured
     */
    public boolean isCdnEnabled() {
        return cdn.isEnabled() && cdn.getDomain() != null && !cdn.getDomain().trim().isEmpty();
    }

    /**
     * Get CDN URL for a given S3 key
     */
    public String getCdnUrl(String key) {
        if (isCdnEnabled()) {
            return "https://" + cdn.getDomain().trim() + "/" + key;
        }
        return null;
    }

    /**
     * Check if file extension is allowed
     */
    public boolean isAllowedExtension(String extension) {
        if (extension == null) return false;
        return upload.getAllowedExtensions().contains(extension.toLowerCase());
    }

    /**
     * Check if content type is allowed
     */
    public boolean isAllowedContentType(String contentType) {
        if (contentType == null) return false;
        return upload.getAllowedContentTypes().contains(contentType.toLowerCase());
    }
} 
