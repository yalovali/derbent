package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * CAbstractNamedEntityService - Abstract service class for entities that extend CEntityNamed.
 * Layer: Service (MVC)
 * Provides common business logic operations for named entities including validation,
 * creation, and name-based queries with consistent error handling and logging.
 */
public abstract class CAbstractNamedEntityService<EntityClass extends CEntityNamed> 
        extends CAbstractService<EntityClass> {

    protected final CAbstractNamedRepository<EntityClass> namedRepository;

    /**
     * Constructor for CAbstractNamedEntityService.
     * 
     * @param repository the repository for data access operations
     * @param clock the Clock instance for time-related operations
     */
    public CAbstractNamedEntityService(final CAbstractNamedRepository<EntityClass> repository, final Clock clock) {
        super(repository, clock);
        this.namedRepository = repository;
    }

    /**
     * Creates a new entity with the given name.
     * This method includes the standard test failure logic for error handler testing.
     * 
     * @param name the entity name
     * @throws RuntimeException if name is "fail" (for testing error handling)
     * @throws IllegalArgumentException if name is null or empty
     */
    @Transactional
    public void createEntity(final String name) {
        LOGGER.debug("createEntity called with name: {} for {}", name, getClass().getSimpleName());
        
        // Standard test failure logic for error handler testing
        if ("fail".equals(name)) {
            LOGGER.warn("Test failure requested for name: {}", name);
            throw new RuntimeException("This is for testing the error handler");
        }
        
        // Validate name
        validateEntityName(name);
        
        // Create and save entity
        final EntityClass entity = createNewEntityInstance();
        entity.setName(name.trim());
        repository.saveAndFlush(entity);
        
        LOGGER.info("Entity created successfully with name: {} for {}", name, getClass().getSimpleName());
    }

    /**
     * Finds an entity by name (case-insensitive).
     * 
     * @param name the entity name
     * @return Optional containing the entity if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<EntityClass> findByName(final String name) {
        LOGGER.debug("findByName called with name: {} for {}", name, getClass().getSimpleName());
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("findByName called with null or empty name for {}", getClass().getSimpleName());
            return Optional.empty();
        }
        
        try {
            final Optional<EntityClass> entity = namedRepository.findByNameIgnoreCase(name.trim());
            LOGGER.debug("findByName for name '{}' found: {} in {}", name, entity.isPresent(), getClass().getSimpleName());
            return entity;
        } catch (final Exception e) {
            LOGGER.error("Error finding entity by name '{}' in {}: {}", name, getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to find entity by name", e);
        }
    }

    /**
     * Checks if an entity name exists (case-insensitive).
     * 
     * @param name the name to check
     * @return true if the name exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByName(final String name) {
        LOGGER.debug("existsByName called with name: {} for {}", name, getClass().getSimpleName());
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("existsByName called with null or empty name for {}", getClass().getSimpleName());
            return false;
        }
        
        try {
            final boolean exists = namedRepository.existsByNameIgnoreCase(name.trim());
            LOGGER.debug("existsByName for name '{}' exists: {} in {}", name, exists, getClass().getSimpleName());
            return exists;
        } catch (final Exception e) {
            LOGGER.error("Error checking name existence '{}' in {}: {}", name, getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to check name existence", e);
        }
    }

    /**
     * Validates if a name is unique (excluding the current entity being updated).
     * 
     * @param name the name to validate
     * @param currentId the ID of the current entity being updated (null for new entities)
     * @return true if the name is unique, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isNameUnique(final String name, final Long currentId) {
        LOGGER.debug("isNameUnique called with name: {}, currentId: {} for {}", name, currentId, getClass().getSimpleName());
        
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("Name uniqueness check called with null or empty name for {}", getClass().getSimpleName());
            return false;
        }
        
        try {
            final Optional<EntityClass> existingEntity = namedRepository.findByNameIgnoreCase(name.trim());
            if (existingEntity.isEmpty()) {
                LOGGER.debug("Name '{}' is unique in {}", name, getClass().getSimpleName());
                return true;
            }
            
            // If we're updating an existing entity, check if it's the same entity
            if (currentId != null && existingEntity.get().getId().equals(currentId)) {
                LOGGER.debug("Name '{}' belongs to current entity being updated in {}", name, getClass().getSimpleName());
                return true;
            }
            
            LOGGER.debug("Name '{}' is not unique in {}", name, getClass().getSimpleName());
            return false;
        } catch (final Exception e) {
            LOGGER.error("Error checking name uniqueness for '{}' in {}: {}", name, getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to check name uniqueness", e);
        }
    }

    /**
     * Validates an entity name.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if the name is null or empty
     */
    protected void validateEntityName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("validateEntityName called with null or empty name for {}", getClass().getSimpleName());
            throw new IllegalArgumentException("Entity name cannot be null or empty");
        }
    }

    /**
     * Creates a new instance of the entity class.
     * Subclasses must implement this method to provide the specific entity type.
     * 
     * @return a new instance of the entity class
     */
    protected abstract EntityClass createNewEntityInstance();
}