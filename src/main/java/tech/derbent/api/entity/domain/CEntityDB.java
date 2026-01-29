package tech.derbent.api.entity.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.IEntityDBStatics;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.ICopyable;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.IHasComments;

@MappedSuperclass
public abstract class CEntityDB<EntityClass> extends CEntity<EntityClass> implements IEntityDBStatics, ICopyable<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityDB.class);

	/** Copies a collection field with option to create new collection or reuse reference.
	 * @param supplier  The collection getter
	 * @param consumer  The collection setter
	 * @param createNew If true, creates new HashSet/ArrayList; if false, reuses reference
	 * @param <T>       The collection element type */
	public static <T> void copyCollection(final Supplier<? extends Collection<T>> supplier, final Consumer<? super Collection<T>> consumer,
			final boolean createNew) {
		if (supplier == null || consumer == null) {
			return;
		}
		try {
			final Collection<T> source = supplier.get();
			if (source == null) {
				consumer.accept(null);
				return;
			}
			if (createNew) {
				if (source instanceof Set) {
					consumer.accept(new HashSet<>(source));
				} else {
					consumer.accept(new ArrayList<>(source));
				}
			} else {
				consumer.accept(source);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not copy collection: {}", e.getMessage());
		}
	}

	/** Copies a single field from source to target using Supplier/Consumer pattern. If either supplier or consumer is null, the field is skipped
	 * silently. This allows optional field mapping without errors.
	 * @param supplier The getter method reference (e.g., this::getFieldName)
	 * @param consumer The setter method reference (e.g., target::setFieldName)
	 * @param <T>      The field type */
	public static <T> void copyField(final Supplier<T> supplier, final Consumer<T> consumer) {
		if (supplier == null || consumer == null) {
			return; // Skip if either is missing
		}
		try {
			final T value = supplier.get();
			consumer.accept(value);
		} catch (final Exception e) {
			// Log but don't fail - optional field
			LOGGER.debug("Could not copy field: {}", e.getMessage());
		}
	}

	/** Helper method to get all fields including inherited fields.
	 * @param clazz the class to get fields from
	 * @return array of all fields */
	private static Field[] getAllFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		Class<?> currentClass = clazz;
		while (currentClass != null) {
			final Field[] declaredFields = currentClass.getDeclaredFields();
			for (final Field field : declaredFields) {
				fields.add(field);
			}
			currentClass = currentClass.getSuperclass();
		}
		return fields.toArray(new Field[0]);
	}

	@Column (name = "active", nullable = false)
	@AMetaData (
			displayName = "Active", required = false, readOnly = false, description = "Whether this entity definition is active", hidden = false,
			defaultValue = "true"
	)
	private Boolean active = true;
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@AMetaData (displayName = "#", required = false, readOnly = true, description = "No", hidden = false)
	protected Long id;

	/** Default constructor for JPA. */
	protected CEntityDB() {}

	public CEntityDB(final Class<EntityClass> clazz) {
		super(clazz);
		initializeDefaults();
	}

	/** Copies entity fields to target entity using CloneOptions to control what is copied. PATTERN: 1. Base class copies common fields (active,
	 * interfaces) 2. Calls service.copyEntityFieldsTo() for entity-specific field copying 3. Subclasses can override to add additional logic after
	 * service copying Override in subclasses only if you need pre/post processing. For regular field copying, override copyEntityFieldsTo() in the
	 * SERVICE class instead. Always call super.copyEntityTo() first!
	 * @param target        The target entity
	 * @param serviceTarget The service for target entity (handles entity-specific copy logic)
	 * @param options       Clone options to control copying behavior */
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") final CAbstractService serviceTarget,
			final CCloneOptions options) {
		try {
			// STEP 1: Copy base fields (active)
			copyField(this::getActive, target::setActive);
			// STEP 2: Automatically copy common interface fields
			// These are handled by interface helpers to reduce duplication
			IHasComments.copyCommentsTo(this, target, options);
			IHasAttachments.copyAttachmentsTo(this, target, options);
			IHasStatusAndWorkflow.copyStatusAndWorkflowTo(this, target, options);
			// STEP 3: Delegate entity-specific field copying to service
			// Service uses reflection and getters/setters for type-safe field copying
			Check.notNull(serviceTarget, "Service target cannot be null for entity copy");
			// Use raw type service to avoid generic type issues
			@SuppressWarnings ({
					"rawtypes"
			})
			final CAbstractService rawService = serviceTarget;
			rawService.copyEntityFieldsTo(this, target, options);
		} catch (final Exception e) {
			LOGGER.debug("Service copy delegation failed: {} - continuing with base copy", e.getMessage());
		}
	}

	public CEntityDB<?> copyTo(Class<?> clazz1) throws Exception {
		try {
			@SuppressWarnings ("unchecked")
			final CEntityDB<?> target = copyTo((Class<? extends CEntityDB<?>>) clazz1, new CCloneOptions.Builder().build());
			return target;
		} catch (final Exception e) {
			LOGGER.error("Error copying entity to class: {} {}", clazz1.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	/** Copies entity to another class type, using CloneOptions to control what is copied.
	 * @param targetClass The target entity class
	 * @param options     Clone options to control copying behavior
	 * @return New instance of target class with copied fields
	 * @throws Exception if instantiation fails */
	public final <T extends CEntityDB<?>> T copyTo(final Class<T> targetClass, final CCloneOptions options) throws Exception {
		try {
			final var ctor = targetClass.getDeclaredConstructor();
			if (!ctor.canAccess(null)) {
				ctor.setAccessible(true);
			}
			final T target = ctor.newInstance();
			// locate service to initialize new entity
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(target.getClass());
			Check.notNull(serviceClass, "No service class found for entity: " + target.getClass().getSimpleName());
			@SuppressWarnings ("rawtypes")
			final CAbstractService serviceTarget = (CAbstractService) CSpringContext.getBean(serviceClass);
			Check.notNull(serviceTarget, "Service bean not found for class: " + serviceClass.getSimpleName());
			// serviceTarget.initializeNewEntity(target);
			//
			copyEntityTo(target, serviceTarget, options);
			return target;
		} catch (final Exception e) {
			LOGGER.error("Error copying entity to class: {} {}", targetClass.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CEntityDB)) {
			return false;
		}
		final CEntityDB<EntityClass> other = (CEntityDB<EntityClass>) obj;
		final Class<?> thisClass = ProxyUtils.getUserClass(getClass());
		final Class<?> otherClass = ProxyUtils.getUserClass(other.getClass());
		if (!thisClass.equals(otherClass)) {
			return false;
		}
		final Long id1 = getId();
		return id1 != null && id1.equals(other.getId());
	}

	public Boolean getActive() { return active; }

	/** Returns the default ordering field for queries. Subclasses can override this to provide custom default ordering. The default implementation
	 * returns "id" to ensure consistent ordering by ID in descending order.
	 * @return the field name to order by (e.g., "id", "name", "createDate") */
	public String getDefaultOrderBy() { return "id"; }

	/** Gets the entity class for this entity instance. Required by ICloneable interface.
	 * @return the entity class */
	@SuppressWarnings ("unchecked")
	public Class<EntityClass> getEntityClass() { return (Class<EntityClass>) ProxyUtils.getUserClass(getClass()); }

	@Nullable
	public Long getId() { return id; }

	@Override
	public int hashCode() {
		final Long id1 = getId();
		if (id1 != null) {
			return id1.hashCode();
		}
		return ProxyUtils.getUserClass(getClass()).hashCode();
	}

	public void initializeAllFields() { /*****/
	}

	private final void initializeDefaults() { /*****/
	}

	public boolean isNew() { return id == null; }

	/** Checks if this entity matches the given search value in the specified fields. This base implementation searches in 'id' and 'active' fields.
	 * Subclasses should override to add their specific fields while calling super.matchesFilter().
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "id" field. Supported field names: "id", "active"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check ID field if requested
		if (fieldNames.remove("id") && getId().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("active") && getActive().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		return false;
	}

	/** Generic method to save entity state using reflection. This method can be overridden by subclasses for custom save behavior.
	 * @return this entity instance for method chaining */
	@SuppressWarnings ("unchecked")
	public EntityClass performSave() {
		try {
			// Update audit fields if they exist
			updateAuditFields();
			LOGGER.debug("Performed save operation for entity: {}", this.getClass().getSimpleName());
			return (EntityClass) this;
		} catch (final Exception e) {
			LOGGER.error("Error during save operation using reflection", e);
			throw e;
		}
	}

	/** Generic method to perform soft delete using reflection. Looks for common soft delete fields like 'deleted', 'active'.
	 * @return true if soft delete was performed, false if hard delete should be used
	 * @throws Exception */
	public boolean performSoftDelete() throws Exception {
		try {
			final Class<?> entityClass = ProxyUtils.getUserClass(this.getClass());
			final Field[] fields = getAllFields(entityClass);
			// Look for common soft delete fields
			for (final Field field : fields) {
				final String fieldName = field.getName().toLowerCase();
				if ("deleted".equals(fieldName) && field.getType() == Boolean.class) {
					field.setAccessible(true);
					field.set(this, Boolean.TRUE);
					LOGGER.debug("Performed soft delete using 'deleted' field for: {}", this.getClass().getSimpleName());
					return true;
				} else if ("active".equals(fieldName) && field.getType() == Boolean.class) {
					field.setAccessible(true);
					field.set(this, Boolean.FALSE);
					LOGGER.debug("Performed soft delete using 'active' field for: {}", this.getClass().getSimpleName());
					return true;
				}
			}
			LOGGER.debug("No soft delete field found for: {}, hard delete should be used", this.getClass().getSimpleName());
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error during soft delete operation using reflection", e);
			throw e;
		}
	}

	public void setActive(final Boolean active) { this.active = active; }

	@Override
	public String toString() {
		return "%s{id=%s}".formatted(getClass().getSimpleName(), getId());
	}

	/** Helper method to update audit fields using reflection. Looks for common audit fields like 'lastModifiedDate', 'updatedAt'. */
	private void updateAuditFields() {
		try {
			final Class<?> entityClass = ProxyUtils.getUserClass(this.getClass());
			final Method updateMethod = CAuxillaries.getMethod(entityClass, "updateLastModified");
			if (updateMethod != null) {
				updateMethod.invoke(this);
				LOGGER.debug("Updated audit fields for: {}", this.getClass().getSimpleName());
			}
		} catch (final Exception e) {
			// Don't fail the save operation if audit update fails
			LOGGER.debug("Could not update audit fields (this is not an error): {}", e.getMessage());
		}
	}
}
