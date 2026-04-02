package com.zorvyn.fintech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // Enables JPA auditing for @CreatedDate, @LastModifiedDate annotations
}
