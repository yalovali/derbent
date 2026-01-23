package tech.derbent.bab.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.bab.node.domain.CBabNodeCAN;

/**
 * Service class for CBabNodeCAN entity. 
 * Provides business logic for CAN Bus communication node management.
 * Following Derbent pattern: Concrete service with @Service and interfaces.
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabNodeCANService extends CBabNodeService<CBabNodeCAN> implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeCANService.class);

    public CBabNodeCANService(final IBabNodeCANRepository repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    @Override
    public Class<CBabNodeCAN> getEntityClass() {
        return CBabNodeCAN.class;
    }

    @Override
    protected void validateEntity(final CBabNodeCAN entity) {
        super.validateEntity(entity);
        
        // CAN-specific validation
        if (entity.getBitrate() != null && entity.getBitrate() <= 0) {
            throw new IllegalArgumentException("CAN bitrate must be positive");
        }
        
        if (entity.getSamplePoint() != null && (entity.getSamplePoint() < 0.0 || entity.getSamplePoint() > 1.0)) {
            throw new IllegalArgumentException("CAN sample point must be between 0.0 and 1.0");
        }
    }

    @Override
    public void initializeNewEntity(final CBabNodeCAN entity) {
        super.initializeNewEntity(entity);
        LOGGER.debug("Initializing CAN node specific defaults");
        
        // CAN-specific initialization is handled by initializeDefaults() in entity constructor
        
        LOGGER.debug("CAN node initialization complete");
    }

    @Override
    public String checkDeleteAllowed(final CBabNodeCAN entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) return superCheck;
        
        // Add CAN-specific deletion checks here if needed
        return null;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CBabNodeInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceBabNode.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }
}