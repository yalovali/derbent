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
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.abstracts.utils.PageableUtils;

/**
 * CAbstractService - Abstract base service class for entity operations. Layer: Service
 * (MVC) Provides common CRUD operations and lazy loading support for all entity types.
 */
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {

	protected final Clock clock;

	protected final CAbstractRepository<EntityClass> repository;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public CAbstractService(final CAbstractRepository<EntityClass> repository,
		final Clock clock) {
		this.clock = clock;
		this.repository = repository;
	}

	public int count() {
		// LOGGER.debug("Counting entities in {}", getClass().getSimpleName());
		return (int) repository.count();
	}

	@Transactional
	public EntityClass createEntity() {

		try {
			final EntityClass entity = newEntity();
			repository.saveAndFlush(entity);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException(
				"Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	/**
	 * Generic update method using reflection to copy non-null fields.
	 * 
	 * @param id the ID of the entity to update
	 * @param updatedEntity entity containing the new values
	 * @return the updated entity
	 */
	@Transactional
	public EntityClass update(final Long id, final EntityClass updatedEntity) {
		final EntityClass existingEntity = repository.findById(id)
			.orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));
		
		// Use reflection to copy non-null fields
		existingEntity.copyNonNullFields(updatedEntity, existingEntity);
		
		// Perform save with reflection-based audit updates
		existingEntity.performSave();
		
		return repository.save(existingEntity);
	}

	/**
	 * Enhanced delete method that attempts soft delete using reflection before hard delete.
	 * 
	 * @param entity the entity to delete
	 */
	@Transactional
	public void deleteWithReflection(final EntityClass entity) {
		if (entity == null) {
			LOGGER.warn("Cannot delete null entity");
			return;
		}

		try {
			// Try soft delete first using reflection
			if (entity.performSoftDelete()) {
				// Soft delete was successful, save the entity
				repository.save(entity);
				LOGGER.info("Performed soft delete for entity: {}", entity.getClass().getSimpleName());
			} else {
				// No soft delete field found, perform hard delete
				repository.delete(entity);
				LOGGER.info("Performed hard delete for entity: {}", entity.getClass().getSimpleName());
			}
		} catch (final Exception e) {
			LOGGER.error("Error during delete operation for entity: {}", 
				CSpringAuxillaries.safeToString(entity), e);
			throw e;
		}
	}

	/**
	 * Enhanced delete by ID method that attempts soft delete using reflection.
	 * 
	 * @param id the ID of the entity to delete
	 */
	@Transactional
	public void deleteWithReflection(final Long id) {
		final EntityClass entity = repository.findById(id)
			.orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));
		
		deleteWithReflection(entity);
	}

	public void delete(final EntityClass entity) {
		repository.delete(entity);
	}

	public void delete(final Long id) {
		repository.deleteById(id);
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		final Optional<EntityClass> entity = repository.findById(id);
		// Initialize lazy fields if entity is present
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	protected abstract Class<EntityClass> getEntityClass();

	protected void initializeLazyFields(final EntityClass entity) {

		if (entity == null) {
			return;
		}

		try {
			CSpringAuxillaries.initializeLazily(entity);
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for entity: {}",
				CSpringAuxillaries.safeToString(entity), e);
		}
	}

	protected void initializeLazyRelationship(final Object relationshipEntity) {

		if (relationshipEntity == null) {
			return;
		}
		final boolean success = CSpringAuxillaries.initializeLazily(relationshipEntity);

		if (!success) {
			LOGGER.warn("Failed to initialize lazy relationship: {}",
				CSpringAuxillaries.safeToString(relationshipEntity));
		}
	}

	@Transactional (readOnly = true)
	public List<EntityClass> list(final Pageable pageable) {
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = PageableUtils.validateAndFix(pageable);
		// LOGGER.debug("Listing entities with pageable: {}", safePage);
		final List<EntityClass> entities = repository.findAll(safePage).toList();
		// Initialize lazy fields for all entities
		entities.forEach(this::initializeLazyFields);
		return entities;
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable,
		final Specification<EntityClass> filter) {
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = PageableUtils.validateAndFix(pageable);
		// LOGGER.debug("Listing entities with filter and pageable");
		final Page<EntityClass> page = repository.findAll(filter, safePage);
		// Initialize lazy fields for all entities in the page
		page.getContent().forEach(this::initializeLazyFields);
		return page;
	}

	/**
	 * Lists entities with text-based filtering for searchable entities.
	 * This method works with entities that implement CSearchable interface.
	 * 
	 * @param pageable pagination information
	 * @param searchText text to search for (null or empty means no filtering)
	 * @return list of entities matching the search criteria
	 */
	@Transactional (readOnly = true)
	public List<EntityClass> list(final Pageable pageable, final String searchText) {
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = PageableUtils.validateAndFix(pageable);
		
		// If no search text or entity doesn't implement CSearchable, use regular listing
		if ((searchText == null) || searchText.trim().isEmpty() 
				|| !CSearchable.class.isAssignableFrom(getEntityClass())) {
			return list(safePage);
		}
		
		// Get all entities and filter using the entity's matches method
		final List<EntityClass> allEntities = repository.findAll(safePage).toList();
		// Initialize lazy fields for all entities
		allEntities.forEach(this::initializeLazyFields);
		
		// Filter entities using their search implementation
		final String trimmedSearchText = searchText.trim();
		return allEntities.stream()
			.filter(entity -> ((CSearchable) entity).matches(trimmedSearchText))
			.toList();
	}

	public EntityClass newEntity() {

		try {
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance =
				getEntityClass().getDeclaredConstructor().newInstance();

			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			@SuppressWarnings ("unchecked")
			final EntityClass entity = ((EntityClass) instance);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException(
				"Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Transactional
	public EntityClass save(final EntityClass entity) {

		try {
			final EntityClass savedEntity = repository.save(entity);
			return savedEntity;
		} catch (final Exception e) {
			LOGGER.error("Error saving entity: {}",
				CSpringAuxillaries.safeToString(entity), e);
			throw e;
		}
	}

	protected void validateEntity(final EntityClass entity) {

		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}
		// Add more validation logic in subclasses if needed
	}
}