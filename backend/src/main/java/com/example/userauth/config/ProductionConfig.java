package com.example.userauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Production Configuration Class
 * Explicitly specify only necessary components, excluding all test classes
 */
@Configuration
@ComponentScan(
    basePackages = {"com.example.userauth"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = {
            "com\\.example\\.userauth\\..*Test.*",
            "com\\.example\\.userauth\\..*test.*"
        }
    )
)
public class ProductionConfig {
}