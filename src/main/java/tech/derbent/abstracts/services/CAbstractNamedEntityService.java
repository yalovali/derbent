package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * CAbstractNamedEntityService - Abstract service class for entities that extend
 * CEntityNamed. Layer: Service (MVC) Provides common business logic operations for named
 * entities including validation, creation, and name-based queries with consistent error
 * handling and logging.
 */
public abstract class CAbstractNamedEntityService<
	EntityClass extends CEntityNamed<EntityClass>> extends CAbstractService<EntityClass> {

	/**
	 * Validates an entity name.
	 * @param name the name to validate
	 * @throws IllegalArgumentException if the name is null or empty
	 */
	protected static void validateEntityName(final String name) {

		if ((name == null) || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Entity name cannot be null or empty");
		}
	}

	protected final CAbstractNamedRepository<EntityClass> namedRepository;

	/**
	 * Constructor for CAbstractNamedEntityService.
	 * @param repository the repository for data access operations
	 * @param clock      the Clock instance for time-related operations
	 */
	public CAbstractNamedEntityService(
		final CAbstractNamedRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
		this.namedRepository = repository;
	}

	@Transactional
	public EntityClass createEntity(final String name) {

		try {
			final EntityClass entity = newEntity(name);
			repository.saveAndFlush(entity);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException(
				"Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	/**
	 * Checks if an entity name exists (case-insensitive).
	 * @param name the name to check
	 * @return true if the name exists, false otherwise
	 */
	@Transactional (readOnly = true)
	public boolean existsByName(final String name) {

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("existsByName called with null or empty name for {}",
				getClass().getSimpleName());
			return false;
		}

		try {
			final boolean exists = namedRepository.existsByNameIgnoreCase(name.trim());
			return exists;
		} catch (final Exception e) {
			LOGGER.error("Error checking name existence '{}' in {}: {}", name,
				getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to check name existence", e);
		}
	}

	/**
	 * Finds an entity by name (case-insensitive).
	 * @param name the entity name
	 * @return Optional containing the entity if found, empty otherwise
	 */
	@Transactional (readOnly = true)
	public Optional<EntityClass> findByName(final String name) {

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("findByName called with null or empty name for {}",
				getClass().getSimpleName());
			return Optional.empty();
		}

		try {
			final Optional<EntityClass> entity =
				namedRepository.findByNameIgnoreCase(name.trim());
			return entity;
		} catch (final Exception e) {
			LOGGER.error("Error finding entity by name '{}' in {}: {}", name,
				getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to find entity by name", e);
		}
	}

	/**
	 * Validates if a name is unique (excluding the current entity being updated).
	 * @param name      the name to validate
	 * @param currentId the ID of the current entity being updated (null for new entities)
	 * @return true if the name is unique, false otherwise
	 */
	@Transactional (readOnly = true)
	public boolean isNameUnique(final String name, final Long currentId) {

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("Name uniqueness check called with null or empty name for {}",
				getClass().getSimpleName());
			return false;
		}

		try {
			final Optional<EntityClass> existingEntity =
				namedRepository.findByNameIgnoreCase(name.trim());

			if (existingEntity.isEmpty()) {
				return true;
			}

			// If we're updating an existing entity, check if it's the same entity
			if ((currentId != null) && existingEntity.get().getId().equals(currentId)) {
				return true;
			}
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error checking name uniqueness for '{}' in {}: {}", name,
				getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to check name uniqueness", e);
		}
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
			final Object instance = getEntityClass().getDeclaredConstructor(String.class)
				.newInstance(name.trim());

			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			final EntityClass entity = ((EntityClass) instance);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException(
				"Failed to create instance of " + getEntityClass().getName(), e);
		}
	}
}