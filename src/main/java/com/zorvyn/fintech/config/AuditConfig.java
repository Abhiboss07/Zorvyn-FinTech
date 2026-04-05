package com.zorvyn.fintech.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableJpaAuditing
@EnableAsync
@EnableCaching
public class AuditConfig {
    // Enables JPA auditing for @CreatedDate, @LastModifiedDate annotations
    // Enables @Async for non-blocking audit log writes to MongoDB
    // Enables @Cacheable for analytics summary caching
}
