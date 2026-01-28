package tech.derbent.bab.setup.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.service.CSystemSettingsService;

/**
 * CSystemSettings_BabService - BAB IoT Gateway-specific system settings service.
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * 
 * Provides simplified configuration management for IoT gateway environments.
 * Follows the same pattern as CProject_BabService.
 */
@Service("CSystemSettings_BabService")
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CSystemSettings_BabService extends CSystemSettingsService<CSystemSettings_Bab> implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettings_BabService.class);

    public CSystemSettings_BabService(final ISystemSettings_BabRepository repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    @Override
    public Class<CSystemSettings_Bab> getEntityClass() {
        return CSystemSettings_Bab.class;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CSystemSettings_BabInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceSystemSettings_Bab.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    @Override
    public CSystemSettings_Bab newEntity() {
        // Constructor already calls initializeDefaults() which calls initializeNewEntity()
        final CSystemSettings_Bab entity = new CSystemSettings_Bab("BAB IoT Gateway");
        LOGGER.debug("Created new BAB system settings entity: {}", entity.getApplicationName());
        return entity;
    }

    @Override
    protected void validateEntity(final CSystemSettings_Bab entity) {
        super.validateEntity(entity);
        
        // BAB-specific validation
        if (entity.getGatewayPort() != null && (entity.getGatewayPort() < 1024 || entity.getGatewayPort() > 65535)) {
            throw new IllegalArgumentException("Gateway port must be between 1024 and 65535");
        }
        
        if (entity.getDeviceScanIntervalSeconds() != null && entity.getDeviceScanIntervalSeconds() < 5) {
            throw new IllegalArgumentException("Device scan interval must be at least 5 seconds");
        }
        
        if (entity.getMaxConcurrentConnections() != null && entity.getMaxConcurrentConnections() < 1) {
            throw new IllegalArgumentException("Max concurrent connections must be at least 1");
        }
        
        LOGGER.debug("BAB system settings validation completed for: {}", entity.getApplicationName());
    }
}