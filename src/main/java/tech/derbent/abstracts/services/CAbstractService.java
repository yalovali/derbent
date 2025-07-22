package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CAbstractService - Abstract base service class for entity operations. Layer: Service (MVC) Provides common CRUD
 * operations and lazy loading support for all entity types.
 */
public abstract class CAbstractService<EntityClass extends CEntityDB> {

    protected final Clock clock;
    protected final CAbstractRepository<EntityClass> repository;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
        LOGGER.debug("CAbstractService constructor called for {}", getClass().getSimpleName());
        this.clock = clock;
        this.repository = repository;
    }

    public int count() {
        LOGGER.debug("Counting entities in {}", getClass().getSimpleName());
        return (int) repository.count();
    }

    public void delete(final EntityClass entity) {
        LOGGER.info("Deleting entity with ID: {}", CSpringAuxillaries.safeGetId(entity));
        repository.delete(entity);
    }

    public void delete(final Long id) {
        LOGGER.info("Deleting entity by ID: {}", id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<EntityClass> get(final Long id) {
        LOGGER.debug("Getting entity by ID: {}", id);
        final Optional<EntityClass> entity = repository.findById(id);
        // Initialize lazy fields if entity is present
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    @Transactional(readOnly = true)
    public List<EntityClass> list(final Pageable pageable) {
        LOGGER.debug("Listing entities with pageable: {}", pageable);
        final List<EntityClass> entities = repository.findAllBy(pageable).toList();
        // Initialize lazy fields for all entities
        entities.forEach(this::initializeLazyFields);
        return entities;
    }

    @Transactional(readOnly = true)
    public Page<EntityClass> list(final Pageable pageable, final Specification<EntityClass> filter) {
        LOGGER.debug("Listing entities with filter and pageable");
        final Page<EntityClass> page = repository.findAll(filter, pageable);
        // Initialize lazy fields for all entities in the page
        page.getContent().forEach(this::initializeLazyFields);
        return page;
    }

    @Transactional
    public EntityClass save(final EntityClass entity) {
        LOGGER.info("Saving entity: {}", CSpringAuxillaries.safeToString(entity));
        try {
            final EntityClass savedEntity = repository.save(entity);
            LOGGER.debug("Entity saved successfully with ID: {}", CSpringAuxillaries.safeGetId(savedEntity));
            return savedEntity;
        } catch (final Exception e) {
            LOGGER.error("Error saving entity: {}", CSpringAuxillaries.safeToString(entity), e);
            throw e;
        }
    }

    /**
     * Initializes lazy fields for an entity to prevent LazyInitializationException. Subclasses can override this method
     * to specify which fields need initialization. This base implementation automatically handles CEntityOfProject
     * entities.
     * 
     * @param entity
     *            the entity to initialize
     */
    protected void initializeLazyFields(final EntityClass entity) {
        if (entity == null) {
            return;
        }

        try {
            // Default implementation - just initialize the entity itself
            CSpringAuxillaries.initializeLazily(entity);

            // Automatically handle CEntityOfProject's lazy project relationship
            if (entity instanceof tech.derbent.abstracts.domains.CEntityOfProject) {
                final tech.derbent.abstracts.domains.CEntityOfProject projectEntity = (tech.derbent.abstracts.domains.CEntityOfProject) entity;
                initializeLazyRelationship(projectEntity.getProject(), "project");
            }

            LOGGER.debug("Initialized lazy fields for entity: {}", entity.getClass().getSimpleName());
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for entity: {}", CSpringAuxillaries.safeToString(entity), e);
        }
    }

    /**
     * Helper method to safely initialize a specific lazy relationship.
     * 
     * @param relationshipEntity
     *            the related entity to initialize
     * @param relationshipName
     *            the name of the relationship (for logging)
     */
    protected void initializeLazyRelationship(final Object relationshipEntity, final String relationshipName) {
        if (relationshipEntity != null) {
            boolean success = CSpringAuxillaries.initializeLazily(relationshipEntity);
            if (success) {
                LOGGER.debug("Initialized lazy relationship: {}", relationshipName);
            } else {
                LOGGER.warn("Failed to initialize lazy relationship: {}", relationshipName);
            }
        }
    }

    /**
     * Validates an entity before saving. Subclasses can override this method to add custom validation logic.
     * 
     * @param entity
     *            the entity to validate
     * @throws IllegalArgumentException
     *             if validation fails
     */
    protected void validateEntity(final EntityClass entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        // Add more validation logic in subclasses if needed
    }
}