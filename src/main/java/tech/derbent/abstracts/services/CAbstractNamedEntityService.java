package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.utils.Check;

/** CAbstractNamedEntityService - Abstract service class for entities that extend CEntityNamed. Layer: Service (MVC) Provides common business logic
 * operations for named entities including validation, creation, and name-based queries with consistent error handling and logging. */
public abstract class CAbstractNamedEntityService<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractService<EntityClass> {

	/** Validates an entity name.
	 * @param name the name to validate
	 * @throws IllegalArgumentException if the name is null or empty */
	protected static void validateEntityName(final String name) {
		Check.notBlank(name, "Entity name cannot be null or empty");
	}

	/** Constructor for CAbstractNamedEntityService.
	 * @param repository the repository for data access operations
	 * @param clock      the Clock instance for time-related operations */
	public CAbstractNamedEntityService(final CAbstractNamedRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
	}

	@Transactional
	public EntityClass createEntity(final String name) {
		final EntityClass entity = newEntity(name);
		repository.saveAndFlush(entity);
		return entity;
	}

	/** Checks if an entity name exists (case-insensitive).
	 * @param name the name to check
	 * @return true if the name exists, false otherwise */
	@Transactional (readOnly = true)
	public boolean existsByName(final String name) {
		Check.notBlank(name, "Name cannot be null or empty");
		return ((CAbstractNamedRepository<EntityClass>) repository).existsByNameIgnoreCase(name.trim());
	}

	/** Finds an entity by name (case-insensitive).
	 * @param name the entity name
	 * @return Optional containing the entity if found, empty otherwise */
	@Transactional (readOnly = true)
	public Optional<EntityClass> findByName(final String name) {
		Check.notBlank(name, "Name cannot be null or empty");
		return ((CAbstractNamedRepository<EntityClass>) repository).findByNameIgnoreCase(name.trim());
	}

	/** Validates if a name is unique (excluding the current entity being updated).
	 * @param name      the name to validate
	 * @param currentId the ID of the current entity being updated (null for new entities)
	 * @return true if the name is unique, false otherwise */
	@Transactional (readOnly = true)
	public boolean isNameUnique(final String name, final Long currentId) {
		Check.notBlank(name, "Name cannot be null or empty");
		final Optional<EntityClass> existingEntity = ((CAbstractNamedRepository<EntityClass>) repository).findByNameIgnoreCase(name.trim());
		if (existingEntity.isEmpty()) {
			return true;
		}
		// If we're updating an existing entity, check if it's the same entity
		if ((currentId != null) && existingEntity.get().getId().equals(currentId)) {
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public EntityClass newEntity() {
		return newEntity("New " + getEntityClass().getSimpleName());
	}

	@SuppressWarnings ("unchecked")
	@Transactional
	public EntityClass newEntity(final String name) {
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		// Validate inputs
		validateEntityName(name);
		try {
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor(String.class).newInstance(name.trim());
			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			return ((EntityClass) instance);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Override
	public boolean onBeforeSaveEvent(final EntityClass entity) {
		if (super.onBeforeSaveEvent(entity) == false) {
			return false;
		}
		return true;
	}
}
