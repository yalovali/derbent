package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.session.service.CSessionService;

/** CAbstractNamedEntityService - Abstract service class for entities that extend CEntityNamed. Layer: Service (MVC) Provides common business logic
 * operations for named entities including validation, creation, and name-based queries with consistent error handling and logging. */
public abstract class CAbstractNamedEntityService<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractService<EntityClass> {
	/** Validates an entity name.
	 * @param name the name to validate
	 * @throws IllegalArgumentException if the name is null or empty */
	protected static void validateEntityName(final String name) {
		Check.notBlank(name, "Entity name cannot be null or empty");
	}

	public CAbstractNamedEntityService(final CAbstractNamedRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
	}

	/** Constructor for CAbstractNamedEntityService.
	 * @param repository     the repository for data access operations
	 * @param clock          the Clock instance for time-related operations
	 * @param sessionService */
	public CAbstractNamedEntityService(final CAbstractNamedRepository<EntityClass> repository, final Clock clock,
			final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional
	public EntityClass createEntity(final String name) {
		final EntityClass entity = newEntity(name);
		repository.saveAndFlush(entity);
		return entity;
	}

	@Transactional (readOnly = true)
	public boolean existsByName(final String name) {
		Check.notBlank(name, "Name cannot be null or empty");
		return ((CAbstractNamedRepository<EntityClass>) repository).existsByName(name.trim());
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> findByName(final String name) {
		Check.notBlank(name, "Name cannot be null or empty");
		return ((CAbstractNamedRepository<EntityClass>) repository).findByName(name.trim());
	}

	/** Varsayılan sıralama anahtarları. İstediğiniz entity servisinde override edebilirsiniz. */
	@Override
	protected Map<String, Function<EntityClass, ?>> getSortKeyExtractors() { return Map.of("name", e -> e.getName(), "id", e -> e.getId()); }

	@Transactional (readOnly = true)
	public boolean isNameUnique(final String name, final Long currentId) {
		Check.notBlank(name, "Name cannot be null or empty");
		final Optional<EntityClass> existingEntity = ((CAbstractNamedRepository<EntityClass>) repository).findByName(name.trim());
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
