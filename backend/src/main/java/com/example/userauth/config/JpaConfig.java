package com.example.userauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA配置类
 * 启用JPA审计功能，自动处理@CreatedDate和@LastModifiedDate等注解
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}