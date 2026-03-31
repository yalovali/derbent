package tech.derbent.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration for Derbent profile.
 * Excludes BAB entities and repositories to prevent schema conflicts.
 */
@Configuration
@Profile({"derbent", "default", "test"})
@EntityScan(basePackages = {
    "tech.derbent.api",
    "tech.derbent.plm",
    "tech.derbent.base"
})
@EnableJpaRepositories(basePackages = {
    "tech.derbent.api",
    "tech.derbent.plm", 
    "tech.derbent.base"
})
public class CDerbentJpaConfig {
    // JPA configuration for Derbent profile - excludes BAB packages
}