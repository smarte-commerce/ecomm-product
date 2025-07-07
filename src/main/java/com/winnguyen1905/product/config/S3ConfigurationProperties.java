package com.winnguyen1905.product.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Configuration properties for AWS S3 integration
 * Binds properties from application.yaml under 'aws.s3' prefix
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "aws.s3")
public class S3ConfigurationProperties {

    @NotBlank(message = "AWS region is required")
    private String region;

    private String accessKey;
    
    private String secretKey;
    
    private boolean useIamRole = true;

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private Bucket bucket = new Bucket();

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private Upload upload = new Upload();

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private Cdn cdn = new Cdn();

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private Connection connection = new Connection();

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private ImageProcessing imageProcessing = new ImageProcessing();

    @Data
    public static class Bucket {
        @NotBlank(message = "S3 bucket name is required")
        private String name;
        
        @NotBlank(message = "Product images bucket name is required")
        private String productImages;
    }

    @Data
    public static class Upload {
        @NotBlank(message = "Maximum file size must be specified")
        private String maxFileSize = "50MB";
        
        @NotEmpty(message = "At least one allowed content type must be specified")
        private List<String> allowedContentTypes = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", 
            "image/webp", "image/bmp", "image/tiff"
        );
        
        @NotEmpty(message = "At least one allowed extension must be specified")
        private List<String> allowedExtensions = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff"
        );
        
        @Positive(message = "Maximum files per upload must be positive")
        private Integer maxFilesPerUpload = 10;
    }

    @Data
    public static class Cdn {
        private boolean enabled = false;
        private String domain;
    }

    @Data
    public static class Connection {
        @Positive(message = "Connection timeout must be positive")
        private Integer timeout = 30000;
        
        @Positive(message = "Socket timeout must be positive")
        private Integer socketTimeout = 30000;
        
        @Positive(message = "Max connections must be positive")
        private Integer maxConnections = 50;
        
        @Positive(message = "Max error retry must be positive")
        private Integer maxErrorRetry = 3;
    }

    @Data
    public static class ImageProcessing {
        private boolean enabled = true;
        
        @Positive(message = "Image quality must be between 1 and 100")
        private Integer quality = 85;
        
        @Valid
        @NotNull
        @NestedConfigurationProperty
        private Thumbnails thumbnails = new Thumbnails();
        
        @Data
        public static class Thumbnails {
            private boolean enabled = true;
            
            @Valid
            private List<Size> sizes = List.of(
                new Size("thumbnail", 150, 150),
                new Size("small", 300, 300),
                new Size("medium", 600, 600),
                new Size("large", 1200, 1200)
            );
            
            @Data
            @Getter
            @Setter
            public static class Size {
                @NotBlank(message = "Size name is required")
                private String name;
                
                @Positive(message = "Width must be positive")
                private Integer width;
                
                @Positive(message = "Height must be positive")
                private Integer height;
                
                public Size() {}
                
                public Size(String name, Integer width, Integer height) {
                    this.name = name;
                    this.width = width;
                    this.height = height;
                }
            }
        }
    }

    // Helper methods

    /**
     * Get the primary bucket name (defaults to product-images bucket)
     */
    public String getPrimaryBucketName() {
        return bucket.getProductImages() != null ? bucket.getProductImages() : bucket.getName();
    }

    /**
     * Check if CDN is enabled and properly configured
     */
    public boolean isCdnEnabled() {
        return cdn.isEnabled() && cdn.getDomain() != null && !cdn.getDomain().trim().isEmpty();
    }

    /**
     * Generate CDN URL for a given S3 key
     */
    public String getCdnUrl(String s3Key) {
        if (!isCdnEnabled()) {
            return null;
        }
        String domain = cdn.getDomain();
        if (!domain.startsWith("http")) {
            domain = "https://" + domain;
        }
        if (!domain.endsWith("/")) {
            domain += "/";
        }
        return domain + s3Key;
    }

    /**
     * Check if content type is allowed
     */
    public boolean isAllowedContentType(String contentType) {
        return contentType != null && upload.getAllowedContentTypes().contains(contentType.toLowerCase());
    }

    /**
     * Check if file extension is allowed
     */
    public boolean isAllowedExtension(String extension) {
        return extension != null && upload.getAllowedExtensions().contains(extension.toLowerCase());
    }

    /**
     * Validate the configuration
     */
    public void validateConfiguration() {
        if (!useIamRole && (accessKey == null || secretKey == null)) {
            throw new IllegalStateException("S3 access credentials must be provided when IAM role is not used");
        }
        
        if (isCdnEnabled() && (cdn.getDomain() == null || cdn.getDomain().trim().isEmpty())) {
            throw new IllegalStateException("CDN domain must be provided when CDN is enabled");
        }
    }
} 
