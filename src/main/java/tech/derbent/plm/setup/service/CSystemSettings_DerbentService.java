package tech.derbent.plm.setup.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsService;
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;

/**
 * CSystemSettings_DerbentService - Derbent PLM-specific system settings service.
 * Layer: Service (MVC)
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides comprehensive configuration management for full-featured PLM environments.
 * Follows the same pattern as CProject_DerbentService.
 */
@Service("CSystemSettings_DerbentService")
@Profile({"derbent", "default"})
@PreAuthorize("isAuthenticated()")
public class CSystemSettings_DerbentService extends CSystemSettingsService<CSystemSettings_Derbent> implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettings_DerbentService.class);

    public CSystemSettings_DerbentService(final ISystemSettings_DerbentRepository repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    @Override
    public Class<CSystemSettings_Derbent> getEntityClass() {
        return CSystemSettings_Derbent.class;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CSystemSettings_DerbentInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceSystemSettings_Derbent.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    @Override
    public CSystemSettings_Derbent newEntity() {
        // Constructor already calls initializeDefaults() which calls initializeNewEntity()
        final CSystemSettings_Derbent entity = new CSystemSettings_Derbent("Derbent Project Management");
        LOGGER.debug("Created new Derbent system settings entity: {}", entity.getApplicationName());
        return entity;
    }

    @Override
    protected void validateEntity(final CSystemSettings_Derbent entity) {
        super.validateEntity(entity);
        
        // Derbent-specific validation
        if (entity.getReportGenerationTimeoutMinutes() != null && entity.getReportGenerationTimeoutMinutes() > 60) {
            throw new IllegalArgumentException("Report generation timeout cannot exceed 60 minutes");
        }
        
        if (entity.getAuditLogRetentionDays() != null && entity.getAuditLogRetentionDays() > 2555) {
            throw new IllegalArgumentException("Audit log retention cannot exceed 2555 days (7 years)");
        }
        
        if (entity.getApiRateLimitPerMinute() != null && entity.getApiRateLimitPerMinute() > 10000) {
            throw new IllegalArgumentException("API rate limit cannot exceed 10000 requests per minute");
        }
        
        if (entity.getNotificationBatchSize() != null && entity.getNotificationBatchSize() > 1000) {
            throw new IllegalArgumentException("Notification batch size cannot exceed 1000");
        }
        
        LOGGER.debug("Derbent system settings validation completed for: {}", entity.getApplicationName());
    }
}