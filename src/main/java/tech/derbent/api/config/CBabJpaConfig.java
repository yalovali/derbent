package tech.derbent.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration for BAB profile.
 * Includes all entities and repositories for full BAB functionality.
 */
@Configuration
@Profile("bab")
@EntityScan(basePackages = "tech.derbent")
@EnableJpaRepositories(basePackages = "tech.derbent")
public class CBabJpaConfig {
    // JPA configuration for BAB profile - includes all packages
}