package tech.derbent.api.entity.service;

import java.lang.reflect.Field;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.Column;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.CSpringAuxillaries;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;

/** CAbstractService - Abstract base service class for entity operations. Layer: Service (MVC) Provides common CRUD operations and lazy loading
 * support for all entity types. */
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {

	/** Formats a Java field name into a human-readable display name. Converts camelCase to Title Case with spaces.
	 * @param fieldName the field name to format
	 * @return formatted display name */
	private static String formatFieldName(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) {
			return fieldName;
		}
		// Split camelCase into words
		final StringBuilder result = new StringBuilder();
		result.append(Character.toUpperCase(fieldName.charAt(0)));
		for (int i = 1; i < fieldName.length(); i++) {
			final char c = fieldName.charAt(i);
			if (Character.isUpperCase(c)) {
				result.append(' ');
			}
			result.append(c);
		}
		return result.toString();
	}

	protected final Clock clock;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final IAbstractRepository<EntityClass> repository;
	protected @Nullable ISessionService sessionService;

	public CAbstractService(final IAbstractRepository<EntityClass> repository, final Clock clock) {
		LOGGER.debug("Initializing service for entity: {}", getEntityClass() != null ? getEntityClass().getSimpleName() : "Unknown");
		this.clock = clock;
		this.repository = repository;
		sessionService = null;
		Check.notNull(repository, "repository cannot be null");
	}

	public CAbstractService(final IAbstractRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService) {
		// LOGGER.debug("Initializing service for entity: {}", getEntityClass() != null ? getEntityClass().getSimpleName() : "Unknown");
		this.clock = clock;
		this.repository = repository;
		this.sessionService = sessionService;
		Check.notNull(repository, "repository cannot be null");
	}

	public String checkDeleteAllowed(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(entity.getId(), "Entity ID cannot be null");
		return null;
	}

	/** Checks if an entity can be saved. This method validates that all required (non-nullable) fields are populated. Subclasses should override this
	 * method to add additional validation logic, but must call super.checkSaveAllowed() first.
	 * @param entity the entity to check
	 * @return null if entity can be saved, or an error message describing validation failures */
	public String checkSaveAllowed(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		// Validate non-nullable fields using reflection
		final String nullableFieldsError = validateNullableFields(entity);
		if (nullableFieldsError != null) {
			return nullableFieldsError;
		}
		return null;
	}

	public long count() {
		// LOGGER.debug("Counting entities in {}", getClass().getSimpleName());
		return repository.count();
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

	public void deleteAllInBatch() {
		repository.deleteAllInBatch();
	}

	/** Enhanced delete method that attempts soft delete using reflection before hard delete.
	 * @param entity the entity to delete
	 * @throws Exception */
	@Transactional
	public void deleteWithReflection(final EntityClass entity) throws Exception {
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
	 * @param id the ID of the entity to delete
	 * @throws Exception */
	@Transactional
	public void deleteWithReflection(final Long id) throws Exception {
		final EntityClass entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));
		deleteWithReflection(entity);
	}

	@PreAuthorize ("permitAll()")
	public List<EntityClass> findAll() throws Exception {
		try {
			// Repository's findAll(Sort) automatically applies sorting at database level
			final Sort defaultSort = getDefaultSort();
			return repository.findAll(defaultSort);
		} catch (final Exception e) {
			LOGGER.error("Failed to find all entities: {}", e.getMessage());
			throw e;
		}
	}

	public Optional<EntityClass> findDefault() throws Exception {
		/// returns the first item by default, you can override to provide custom logic
		final List<EntityClass> all = findAll();
		return all.isEmpty() ? Optional.empty() : Optional.ofNullable(all.get(0));
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		return id == null ? Optional.empty() : repository.findById(id);
	}

	/** Gets the default Sort object based on entity's default order field. Subclasses can override to customize ordering.
	 * @return Sort object for default ordering (descending by default)
	 * @throws Exception */
	protected Sort getDefaultSort() throws Exception {
		try {
			LOGGER.debug("Determining default sort for entity: {}", getEntityClass().getSimpleName());
			// Get a sample entity to determine default order field
			final EntityClass sampleEntity = newEntity();
			final String orderField = sampleEntity.getDefaultOrderBy();
			if (orderField != null && !orderField.isEmpty()) {
				return Sort.by(Sort.Direction.DESC, orderField);
			}
		} catch (final Exception e) {
			LOGGER.error("Could not determine default ordering: {}", e.getMessage());
			throw e;
		}
		// Fallback to ID descending
		return Sort.by(Sort.Direction.DESC, "id");
	}

	protected abstract Class<EntityClass> getEntityClass();

	public EntityClass getRandom() {
		final long count = repository.count();
		if (count == 0) {
			return null;
		}
		final int idx = (int) (Math.random() * count);
		final Page<EntityClass> page = repository.findAll(Pageable.ofSize(1).withPage(idx));
		return page.hasContent() ? page.getContent().get(0) : null;
	}

	public IAbstractRepository<EntityClass> getRepository() { return repository; }

	/** Initialize all lazy fields of an entity within a transaction context. This method should be used when you need to access lazy-loaded fields
	 * outside of the original Hibernate session. Call this from a @Transactional method in your service.
	 * @param entity the entity to initialize
	 * @return the initialized entity */
	@Transactional (readOnly = true)
	public EntityClass initializeLazyFields(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		if (entity.getId() == null) {
			LOGGER.warn("Cannot initialize lazy fields for unsaved entity");
			return entity;
		}
		// Fetch the entity from database to ensure it's managed and lazy fields are available
		final EntityClass managed = repository.findById(entity.getId()).orElse(entity);
		// Access lazy fields to trigger loading within transaction
		managed.initializeAllFields();
		return managed;
	}

	protected void initializeLazyRelationship(final Object relationshipEntity, final String relationshipName) {
		if (relationshipEntity == null) {
			return;
		}
		try {
			final boolean success = CSpringAuxillaries.initializeLazily(relationshipEntity);
			Check.isTrue(success, "Failed to initialize lazy relationship");
		} catch (final Exception e) {
			LOGGER.error("Error initializing lazy relationship '{}': {}", relationshipName, CSpringAuxillaries.safeToString(relationshipEntity), e);
			throw e;
		}
	}

	@SuppressWarnings ("unused")
	public void initializeNewEntity(final Object entity) {
		// all initialization moved to constructor
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable) {
		// LOGGER.debug("Listing entities without filter specification");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		return repository.findAll(safePage);
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable, final Specification<EntityClass> filter) {
		LOGGER.debug("Filter specification: {}", filter);
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		return repository.findAll(filter, safePage);
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> listForPageView(final Pageable pageable, final String searchText) throws Exception {
		try {
			LOGGER.debug("Search text: {}", searchText);
			final Pageable safePage = CPageableUtils.validateAndFix(pageable);
			final String term = searchText == null ? "" : searchText.trim();
			// For search queries, fetch all data with default sorting from database
			final Sort defaultSort = getDefaultSort();
			final List<EntityClass> all = repository.findAllForPageView(defaultSort);
			final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
			final List<EntityClass> filtered =
					term.isEmpty() || !searchable ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
			// Data is already sorted by the database query, no need for additional sorting
			final int start = (int) Math.min(safePage.getOffset(), filtered.size());
			final int end = Math.min(start + safePage.getPageSize(), filtered.size());
			final List<EntityClass> content = filtered.subList(start, end);
			return new PageImpl<>(content, safePage, filtered.size());
		} catch (final Exception e) {
			LOGGER.error("Error during listing entities with search text '{}': {}", searchText, e.getMessage());
			throw e;
		}
	}

	public EntityClass newEntity() throws Exception {
		try {
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor().newInstance();
			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			@SuppressWarnings ("unchecked")
			final EntityClass entity = (EntityClass) instance;
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	/** @param entity */
	public boolean onBeforeSaveEvent(final EntityClass entity) {
		return true;
	}

	@Transactional (readOnly = false)
	public EntityClass save(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		validateEntity(entity);
		return repository.save(entity);
	}

	/** Sets the session service. This method is used to break circular dependencies through configuration classes. */
	public void setSessionService(final ISessionService sessionService) {
		this.sessionService = sessionService;
	}

	protected void validateEntity(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		// Add more validation logic in subclasses if needed
	}
	
	// ========== Static Validation Helper Methods ==========
	
	/** Validates string field length.
	 * @param value     the string value to validate
	 * @param fieldName the field name for error messages
	 * @param maxLength the maximum allowed length
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateStringLength(final String value, final String fieldName, final int maxLength) {
		if (value != null && value.length() > maxLength) {
			throw new IllegalArgumentException(
					ValidationMessages.formatFieldMax(ValidationMessages.FIELD_MAX_LENGTH, fieldName, String.valueOf(maxLength)));
		}
	}
	
	/** Validates BigDecimal field is positive and within max value.
	 * @param value     the BigDecimal value to validate
	 * @param fieldName the field name for error messages
	 * @param max       the maximum allowed value
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateNumericField(final java.math.BigDecimal value, final String fieldName, final java.math.BigDecimal max) {
		if (value != null) {
			if (value.compareTo(java.math.BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(ValidationMessages.formatField(ValidationMessages.NUMERIC_MUST_BE_POSITIVE, fieldName));
			}
			if (value.compareTo(max) > 0) {
				throw new IllegalArgumentException(
						ValidationMessages.formatFieldMax(ValidationMessages.NUMERIC_EXCEEDS_MAXIMUM, fieldName, max.toString()));
			}
		}
	}
	
	/** Validates Integer field is positive and within max value.
	 * @param value     the Integer value to validate
	 * @param fieldName the field name for error messages
	 * @param max       the maximum allowed value
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateNumericField(final Integer value, final String fieldName, final Integer max) {
		if (value != null) {
			if (value < 0) {
				throw new IllegalArgumentException(ValidationMessages.formatField(ValidationMessages.NUMERIC_MUST_BE_POSITIVE, fieldName));
			}
			if (value > max) {
				throw new IllegalArgumentException(
						ValidationMessages.formatFieldMax(ValidationMessages.NUMERIC_EXCEEDS_MAXIMUM, fieldName, max.toString()));
			}
		}
	}
	
	/** Validates Long field is positive and within max value.
	 * @param value     the Long value to validate
	 * @param fieldName the field name for error messages
	 * @param max       the maximum allowed value
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateNumericField(final Long value, final String fieldName, final Long max) {
		if (value == null) {
			return;
		}
		if (value < 0) {
			throw new IllegalArgumentException(ValidationMessages.formatField(ValidationMessages.NUMERIC_MUST_BE_POSITIVE, fieldName));
		}
		if (value > max) {
			throw new IllegalArgumentException(
					ValidationMessages.formatFieldMax(ValidationMessages.NUMERIC_EXCEEDS_MAXIMUM, fieldName, max.toString()));
		}
	}
	
	/** Validates numeric field is within range.
	 * @param value     the BigDecimal value to validate
	 * @param fieldName the field name for error messages
	 * @param min       the minimum allowed value
	 * @param max       the maximum allowed value
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateNumericRange(final java.math.BigDecimal value, final String fieldName, final java.math.BigDecimal min,
			final java.math.BigDecimal max) {
		if (value != null) {
			if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
				throw new IllegalArgumentException(ValidationMessages.formatFieldRange(ValidationMessages.NUMERIC_OUT_OF_RANGE, fieldName,
						min.toString(), max.toString()));
			}
		}
	}
	
	/** Validates Integer field is within range.
	 * @param value     the Integer value to validate
	 * @param fieldName the field name for error messages
	 * @param min       the minimum allowed value
	 * @param max       the maximum allowed value
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateNumericRange(final Integer value, final String fieldName, final Integer min, final Integer max) {
		if (value != null) {
			if (value < min || value > max) {
				throw new IllegalArgumentException(ValidationMessages.formatFieldRange(ValidationMessages.NUMERIC_OUT_OF_RANGE, fieldName,
						min.toString(), max.toString()));
			}
		}
	}

	/** Validates that all required (non-nullable) fields are populated. Uses reflection to check @Column(nullable=false) annotations and
	 * corresponding field values.
	 * @param entity the entity to validate
	 * @return null if all required fields are populated, or an error message listing the missing fields */
	protected String validateNullableFields(final EntityClass entity) {
		if (entity == null) {
			return "Entity cannot be null";
		}
		final List<String> missingFields = new ArrayList<>();
		// Get all fields including inherited ones
		Class<?> currentClass = entity.getClass();
		while (currentClass != null && currentClass != Object.class) {
			for (final Field field : currentClass.getDeclaredFields()) {
				try {
					// Check if field has @Column annotation with nullable=false
					final Column columnAnnotation = field.getAnnotation(Column.class);
					if (columnAnnotation != null && !columnAnnotation.nullable()) {
						// Make field accessible to read its value
						field.setAccessible(true);
						final Object value = field.get(entity);
						if (value == null) {
							// Get display name from @AMetaData if available
							final AMetaData metaData = field.getAnnotation(AMetaData.class);
							final String displayName = metaData != null ? metaData.displayName() : formatFieldName(field.getName());
							missingFields.add(displayName);
						}
					}
				} catch (final IllegalAccessException e) {
					LOGGER.warn("Could not access field {} for validation", field.getName(), e);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		if (missingFields.isEmpty()) {
			return null;
		}
		if (missingFields.size() == 1) {
			return ValidationMessages.FIELD_REQUIRED.formatted(missingFields.get(0));
		}
		return ValidationMessages.FIELD_REQUIRED.formatted(String.join(", ", missingFields));
	}
}
