package com.example.userauth;

import com.example.userauth.config.ProductionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Production Application Startup Class
 * Does not contain any test code, only used to start the backend service
 */
@SpringBootApplication
@Import(ProductionConfig.class)
public class ProductionApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ProductionApplication.class);
        app.setAdditionalProfiles("production");
        app.run(args);
    }
}