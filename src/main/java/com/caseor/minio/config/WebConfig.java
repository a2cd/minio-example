package com.caseor.minio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Fu Kai
 * @since 20230408
 */

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowCredentials(false)
                    .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                    .allowedOrigins("*");
            }
        };
    }
}
