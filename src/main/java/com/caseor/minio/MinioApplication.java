package com.caseor.minio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Fu Kai
 * @since 20210709
 */
@Slf4j
@SpringBootApplication
public class MinioApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinioApplication.class, args);
    }
}
