package com.caseor.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fu Kai
 * @since 20230408
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;
    private String endpointProxy;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
