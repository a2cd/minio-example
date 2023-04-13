package com.caseor.minio.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Fu Kai
 * @since 20220408
 */

@Configuration
public class MinioConfig {

    @Resource
    MinioProperties minioProperties;

    public MinioClient minioClient() {
        MinioClient.Builder minioClientBuilder = MinioClient.builder();
        return minioClientBuilder.endpoint(minioProperties.getEndpoint()).credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey()).build();
    }

    @Bean
    public CustomizedMinioClient customizedMinioClient() {
        return new CustomizedMinioClient(minioClient());
    }


}
