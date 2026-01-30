package tech.derbent.api.entity.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** CAbstractNamedEntityService - Abstract service class for entities that extend CEntityNamed. Layer: Service (MVC) Provides common business logic
 * operations for named entities including validation, creation, and name-based queries with consistent error handling and logging. */
public abstract class CEntityNamedService<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityNamedService.class);

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
		final String superCheck = super.checkDeleteAllowed(entity);
		return superCheck != null ? superCheck : null;
	}

	/** Generates a unique name for new entities based on existing entities count. Child classes can override this method for custom name generation
	 * patterns. Default pattern: "EntitySimpleName##" where ## is a zero-padded number (e.g., "Activity01", "Meeting02")
	 * @param clazzName
	 * @return a unique name string */
	protected String generateUniqueName(String clazzName) {
		try {
			// Count existing entities to generate next available number
			final long existingCount = count();
			// Format: EntitySimpleName + zero-padded number (e.g., "Activity01", "Meeting02")
			return "%s%02d".formatted(clazzName, existingCount + 1);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique name, falling back to generic name: {}", e.getMessage());
			return "New " + getEntityClass().getSimpleName();
		}
	}

	public String getUniqueNameFromList(String prefix, List<EntityClass> existingEntities) {
		//
		final int maxNumber = existingEntities.stream().map(EntityClass::getName).filter(name -> name.matches(prefix + "(\\d{2})"))
				.mapToInt(name -> Integer.parseInt(name.replaceAll(prefix, ""))).max().orElse(0);
		return String.format(prefix + "%02d", maxNumber + 1);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// Only generate unique name if entity doesn't already have a name
		final CEntityNamed<EntityClass> namedEntity = (CEntityNamed<EntityClass>) entity;
		if (namedEntity.getName() == null || namedEntity.getName().isBlank()) {
			namedEntity.setName(generateUniqueName(entity.getClass().getSimpleName()));
		}
		// If entity already has a name (set via constructor), keep it as-is
	}

	@Transactional (readOnly = true)
	public boolean isNameUnique(final String name, final Long currentId) {
		Check.notBlank(name, "Name cannot be null or empty");
		final Optional<EntityClass> existingEntity = ((IAbstractNamedRepository<EntityClass>) repository).findByName(name.trim());
		if (existingEntity.isEmpty()) {
			return true;
		}
		// If we're updating an existing entity, check if it's the same entity
		if (currentId != null && existingEntity.get().getId().equals(currentId)) {
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
			// LOGGER.debug("Creating new entity instance of {} with name: {}", getEntityClass().getName(), name);
			if ("fail".equals(name)) {
				throw new RuntimeException("This is for testing the error handler");
			}
			Check.notBlank(name, "Name cannot be null or empty");
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor(String.class).newInstance(name.trim());
			Check.notNull(instance, "Failed to create instance of " + getEntityClass().getName());
			Check.instanceOf(instance, getEntityClass(), "Created object is not instance of " + getEntityClass().getName());
			return (EntityClass) instance;
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

	@Override
	protected void validateEntity(final EntityClass entity) {
		super.validateEntity(entity);
		// Name length validation MOVED to base class (CAbstractService)
		// Use: validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
	}
	
	/** Service-level method to copy CEntityNamed-specific fields using direct setters/getters.
	 * Override in concrete services to add entity-specific field copying.
	 * Always call super.copyEntityFieldsTo() first!
	 * 
	 * @param source the source entity to copy from
	 * @param target the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final EntityClass source, final CEntityDB<?> target,
			final CCloneOptions options) {
		// Call parent to copy base entity fields
		super.copyEntityFieldsTo(source, target, options);
		
		// Copy named entity fields if target supports them
		if (!(target instanceof CEntityNamed)) {
			return;
		}
		final CEntityNamed<?> targetNamed = (CEntityNamed<?>) target;
		
		// Copy name and description - direct setter/getter
		targetNamed.setName(source.getName());
		targetNamed.setDescription(source.getDescription());
		
		// Copy date fields based on options
		if (!options.isResetDates()) {
			targetNamed.setCreatedDate(source.getCreatedDate());
			targetNamed.setLastModifiedDate(source.getLastModifiedDate());
		}
		
		LOGGER.debug("Copied named entity fields for: {}", source.getName());
	}
}
