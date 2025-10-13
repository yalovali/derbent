package tech.derbent.api.services;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.utils.Check;
import tech.derbent.session.service.ISessionService;

/** CAbstractNamedEntityService - Abstract service class for entities that extend CEntityNamed. Layer: Service (MVC) Provides common business logic
 * operations for named entities including validation, creation, and name-based queries with consistent error handling and logging. */
public abstract class CEntityNamedService<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractService<EntityClass> {

	public CEntityNamedService(final IAbstractNamedRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
	}

	public CEntityNamedService(final IAbstractNamedRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null; // No dependencies found by default
	}

	@Transactional
	public EntityClass createEntity(final String name) {
		final EntityClass entity = newEntity(name);
		repository.saveAndFlush(entity);
		return entity;
	}

	/** Varsayılan sıralama anahtarları. İstediğiniz entity servisinde override edebilirsiniz. */
	@Override
	protected Map<String, Function<EntityClass, ?>> getSortKeyExtractors() { return Map.of("name", e -> e.getName(), "id", e -> e.getId()); }

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		// Initialize description with default value if entity has this field
		if (entity.getDescription() == null || entity.getDescription().isEmpty()) {
			entity.setDescription("");
		}
	}

	@Transactional (readOnly = true)
	public boolean isNameUnique(final String name, final Long currentId) {
		Check.notBlank(name, "Name cannot be null or empty");
		final Optional<EntityClass> existingEntity = ((IAbstractNamedRepository<EntityClass>) repository).findByName(name.trim());
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
		Check.notBlank(name, "Name cannot be null or empty");
		try {
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor(String.class).newInstance(name.trim());
			Check.notNull(instance, "Failed to create instance of " + getEntityClass().getName());
			Check.instanceOf(instance, getEntityClass(), "Created object is not instance of " + getEntityClass().getName());
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
