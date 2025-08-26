package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.utils.PageableUtils;

/** CAbstractService - Abstract base service class for entity operations. Layer: Service (MVC) Provides common CRUD operations and lazy loading
 * support for all entity types. */
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {
	protected final Clock clock;
	protected final CAbstractRepository<EntityClass> repository;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
		this.clock = clock;
		this.repository = repository;
		Check.notNull(repository, "repository cannot be null");
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
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Transactional
	public void delete(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(entity.getId(), "Entity ID cannot be null");
		LOGGER.debug("Deleting entity: {}", CSpringAuxillaries.safeToString(entity));
		repository.deleteById(entity.getId());
	}

	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Entity ID cannot be null");
		LOGGER.debug("Deleting entity with ID: {}", id);
		repository.deleteById(id);
	}

	/** Enhanced delete method that attempts soft delete using reflection before hard delete.
	 * @param entity the entity to delete */
	@Transactional
	public void deleteWithReflection(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
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
	}

	/** Enhanced delete by ID method that attempts soft delete using reflection.
	 * @param id the ID of the entity to delete */
	@Transactional
	public void deleteWithReflection(final Long id) {
		final EntityClass entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));
		deleteWithReflection(entity);
	}

	@PreAuthorize ("permitAll()")
	public List<EntityClass> findAll() {
		return repository.findAll();
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		if (id == null) {
			return Optional.empty();
		}
		final Optional<EntityClass> entity = repository.findById(id);
		return entity;
	}

	protected abstract Class<EntityClass> getEntityClass();

	protected void initializeLazyRelationship(final Object relationshipEntity, final String relationshipName) {
		if (relationshipEntity == null) {
			return;
		}
		try {
			final boolean success = CSpringAuxillaries.initializeLazily(relationshipEntity);
			if (!success) {
				LOGGER.warn("Failed to initialize lazy relationship '{}': {}", relationshipName, CSpringAuxillaries.safeToString(relationshipEntity));
			}
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy relationship '{}': {}", relationshipName, CSpringAuxillaries.safeToString(relationshipEntity), e);
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable) {
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = PageableUtils.validateAndFix(pageable);
		// LOGGER.debug("Listing entities with pageable: {}", safePage);
		final Page<EntityClass> entities = repository.findAll(safePage);
		// Initialize lazy fields for all entities
		return entities;
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable, final Specification<EntityClass> filter) {
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = PageableUtils.validateAndFix(pageable);
		// LOGGER.debug("Listing entities with filter and pageable");
		final Page<EntityClass> page = repository.findAll(filter, safePage);
		// Initialize lazy fields for all entities in the page
		return page;
	}

	@Transactional (readOnly = true)
	public List<EntityClass> list(final Pageable pageable, final String searchText) {
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = PageableUtils.validateAndFix(pageable);
		// If no search text or entity doesn't implement CSearchable, use regular listing
		if ((searchText == null) || searchText.trim().isEmpty() || !CSearchable.class.isAssignableFrom(getEntityClass())) {
			return list(safePage).getContent();
		}
		// Get all entities and filter using the entity's matches method
		final List<EntityClass> allEntities = repository.findAll(safePage).toList();
		// Initialize lazy fields for all entities
		// Filter entities using their search implementation
		final String trimmedSearchText = searchText.trim();
		return allEntities.stream().filter(entity -> ((CSearchable) entity).matches(trimmedSearchText)).toList();
	}

	public EntityClass newEntity() {
		try {
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor().newInstance();
			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			@SuppressWarnings ("unchecked")
			final EntityClass entity = ((EntityClass) instance);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	public boolean onBeforeSaveEvent(final EntityClass entity) {
		return true;
	}

	@Transactional
	public EntityClass save(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		return repository.save(entity);
	}

	protected void validateEntity(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		// Add more validation logic in subclasses if needed
	}
}
