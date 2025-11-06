package tech.derbent.api.services;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** CAbstractNamedEntityService - Abstract service class for entities that extend CEntityNamed. Layer: Service (MVC) Provides common business logic
 * operations for named entities including validation, creation, and name-based queries with consistent error handling and logging. */
public abstract class CEntityNamedService<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractService<EntityClass> {

	/** Constructor without session service. Use this constructor when session service will be injected via setter method to avoid circular
	 * dependencies. */
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

	/** Varsayılan sıralama anahtarları. İstediğiniz entity servisinde override edebilirsiniz. */
	@Override
	protected Map<String, Function<EntityClass, ?>> getSortKeyExtractors() { return Map.of("name", e -> e.getName(), "id", e -> e.getId()); }

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		entity.setDescription("");
		// Generate unique name automatically to avoid name conflicts
		entity.setName(generateUniqueName());
	}

	/** Generates a unique name for new entities based on existing entities count. Child classes can override this method for custom name generation
	 * patterns. Default pattern: "EntitySimpleName##" where ## is a zero-padded number (e.g., "Activity01", "Meeting02")
	 * @return a unique name string */
	protected String generateUniqueName() {
		try {
			// Count existing entities to generate next available number
			final long existingCount = count();
			// Format: EntitySimpleName + zero-padded number (e.g., "Activity01", "Meeting02")
			return String.format("%s%02d", getEntityClass().getSimpleName(), existingCount + 1);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique name, falling back to generic name: {}", e.getMessage());
			return "New " + getEntityClass().getSimpleName();
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
	public EntityClass newEntity() throws Exception {
		return newEntity("New " + getEntityClass().getSimpleName());
	}

	@SuppressWarnings ("unchecked")
	@Transactional
	public EntityClass newEntity(final String name) throws Exception {
		try {
			if ("fail".equals(name)) {
				throw new RuntimeException("This is for testing the error handler");
			}
			Check.notBlank(name, "Name cannot be null or empty");
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor(String.class).newInstance(name.trim());
			Check.notNull(instance, "Failed to create instance of " + getEntityClass().getName());
			Check.instanceOf(instance, getEntityClass(), "Created object is not instance of " + getEntityClass().getName());
			return ((EntityClass) instance);
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance of {}: {}", getEntityClass().getName(), e.getMessage());
			throw e;
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
