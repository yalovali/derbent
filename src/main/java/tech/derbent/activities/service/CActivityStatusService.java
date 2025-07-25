package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivityStatus;

/**
 * CActivityStatusService - Service class for managing CActivityStatus entities.
 * Layer: Service (MVC)
 * 
 * Extends CAbstractService to provide standard CRUD operations and business logic
 * for activity status entities. All operations require authentication.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityStatusService extends CAbstractService<CActivityStatus> {

    /**
     * Constructor for dependency injection.
     * @param repository the CActivityStatusRepository
     * @param clock      the system clock for timestamps
     */
    CActivityStatusService(final CActivityStatusRepository repository, final Clock clock) {
        super(repository, clock);
        LOGGER.debug("CActivityStatusService constructor called with repository: {} and clock: {}", 
                    repository.getClass().getSimpleName(), clock.getClass().getSimpleName());
    }

    /**
     * Creates a new activity status entity with the given name.
     * @param name the name of the activity status
     * @throws RuntimeException if name is "fail" (for testing error handling)
     */
    @Transactional
    public void createEntity(final String name) {
        LOGGER.debug("CActivityStatusService.createEntity called with name: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("Attempt to create CActivityStatus with null or empty name");
            throw new IllegalArgumentException("Activity status name cannot be null or empty");
        }
        
        if ("fail".equals(name)) {
            LOGGER.warn("Test failure triggered in CActivityStatusService.createEntity");
            throw new RuntimeException("This is for testing the error handler");
        }
        
        final var entity = new CActivityStatus();
        entity.setName(name);
        repository.saveAndFlush(entity);
        
        LOGGER.debug("CActivityStatus created successfully with name: {}", name);
    }

    /**
     * Creates a new activity status entity with the given name and description.
     * @param name        the name of the activity status
     * @param description the description of the activity status
     */
    @Transactional
    public void createEntity(final String name, final String description) {
        LOGGER.debug("CActivityStatusService.createEntity called with name: {} and description: {}", 
                    name, description);
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("Attempt to create CActivityStatus with null or empty name");
            throw new IllegalArgumentException("Activity status name cannot be null or empty");
        }
        
        final var entity = new CActivityStatus(name, description);
        repository.saveAndFlush(entity);
        
        LOGGER.debug("CActivityStatus created successfully with name: {} and description: {}", 
                    name, description);
    }

    /**
     * Overrides the base get method to provide additional logging.
     * No lazy loading concerns for CActivityStatus as it extends CTypeEntity.
     * 
     * @param id the activity status ID
     * @return optional CActivityStatus
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CActivityStatus> get(final Long id) {
        LOGGER.debug("CActivityStatusService.get called with ID: {}", id);
        
        if (id == null) {
            LOGGER.warn("Attempt to get CActivityStatus with null ID");
            return Optional.empty();
        }
        
        final Optional<CActivityStatus> entity = super.get(id);
        
        if (entity.isPresent()) {
            LOGGER.debug("CActivityStatus found with ID: {} and name: {}", id, entity.get().getName());
        } else {
            LOGGER.debug("CActivityStatus not found with ID: {}", id);
        }
        
        return entity;
    }

    /**
     * Initializes lazy fields for CActivityStatus entity.
     * Since CActivityStatus extends CTypeEntity and has no lazy relationships,
     * this method primarily calls the superclass implementation.
     * 
     * @param entity the CActivityStatus entity to initialize
     */
    @Override
    protected void initializeLazyFields(final CActivityStatus entity) {
        if (entity == null) {
            LOGGER.debug("CActivityStatusService.initializeLazyFields called with null entity");
            return;
        }

        LOGGER.debug("Initializing lazy fields for CActivityStatus with ID: {}", entity.getId());

        try {
            // Initialize the entity itself first
            super.initializeLazyFields(entity);
            
            // CActivityStatus has no lazy relationships to initialize
            LOGGER.debug("Lazy fields initialization completed for CActivityStatus with ID: {}", entity.getId());
            
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for CActivityStatus with ID: {}", entity.getId(), e);
        }
    }
}