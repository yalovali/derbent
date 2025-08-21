package tech.derbent.abstracts.domains;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CEntityDB<EntityClass> extends CEntity<EntityClass>
	implements CInterfaceIconSet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityDB.class);

	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Default constructor for JPA.
	 */
	protected CEntityDB() {
		super();
	}

	public CEntityDB(final Class<EntityClass> clazz) {
		super(clazz);
	}

	/**
	 * Generic method to copy non-null fields from source to target using reflection. This
	 * method handles update operations by copying only the fields that have values.
	 * @param source the source entity with updated values
	 * @param target the target entity to update
	 */
	public void copyNonNullFields(final EntityClass source, final EntityClass target) {

		if ((source == null) || (target == null)) {
			LOGGER.warn("Cannot copy fields: source or target is null");
			return;
		}

		try {
			final Class<?> entityClass = ProxyUtils.getUserClass(source.getClass());
			final Field[] fields = getAllFields(entityClass);

			for (final Field field : fields) {

				// Skip ID field to prevent overwriting existing entity ID
				if ("id".equals(field.getName())) {
					continue;
				}
				// Skip static and final fields
				final int modifiers = field.getModifiers();

				if (java.lang.reflect.Modifier.isStatic(modifiers)
					|| java.lang.reflect.Modifier.isFinal(modifiers)) {
					continue;
				}
				field.setAccessible(true);
				final Object value = field.get(source);

				if (value != null) {
					field.set(target, value);
				}
			}
			LOGGER.debug("Successfully copied non-null fields from {} to {}",
				source.getClass().getSimpleName(), target.getClass().getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Error copying fields using reflection", e);
			throw new RuntimeException("Failed to copy entity fields", e);
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
		final Long id = getId();
		return (id != null) && id.equals(other.getId());
	}

	/**
	 * Helper method to find a method by name in class hierarchy.
	 * @param clazz      the class to search in
	 * @param methodName the method name to find
	 * @return the method if found, null otherwise
	 */
	private Method findMethod(final Class<?> clazz, final String methodName) {
		Class<?> currentClass = clazz;

		while (currentClass != null) {

			try {
				return currentClass.getDeclaredMethod(methodName);
			} catch (final NoSuchMethodException e) {
				// Continue searching in parent class
			}
			currentClass = currentClass.getSuperclass();
		}
		return null;
	}

	/**
	 * Helper method to get all fields including inherited fields.
	 * @param clazz the class to get fields from
	 * @return array of all fields
	 */
	private Field[] getAllFields(final Class<?> clazz) {
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

	public String getColor() { return null; }

	@Nullable
	public Long getId() { return id; }

	@Override
	public int hashCode() {
		final Long id = getId();

		if (id != null) {
			return id.hashCode();
		}
		return ProxyUtils.getUserClass(getClass()).hashCode();
	}

	protected void initializeDefaults() {}

	/**
	 * Generic method to save entity state using reflection. This method can be overridden
	 * by subclasses for custom save behavior.
	 * @return this entity instance for method chaining
	 */
	@SuppressWarnings ("unchecked")
	public EntityClass performSave() {

		try {
			// Update audit fields if they exist
			updateAuditFields();
			LOGGER.debug("Performed save operation for entity: {}",
				this.getClass().getSimpleName());
			return (EntityClass) this;
		} catch (final Exception e) {
			LOGGER.error("Error during save operation using reflection", e);
			throw new RuntimeException("Failed to perform save operation", e);
		}
	}

	/**
	 * Generic method to perform soft delete using reflection. Looks for common soft
	 * delete fields like 'deleted', 'active', 'enabled'.
	 * @return true if soft delete was performed, false if hard delete should be used
	 */
	public boolean performSoftDelete() {

		try {
			final Class<?> entityClass = ProxyUtils.getUserClass(this.getClass());
			final Field[] fields = getAllFields(entityClass);

			// Look for common soft delete fields
			for (final Field field : fields) {
				final String fieldName = field.getName().toLowerCase();

				if ("deleted".equals(fieldName) && (field.getType() == Boolean.class)) {
					field.setAccessible(true);
					field.set(this, Boolean.TRUE);
					LOGGER.debug("Performed soft delete using 'deleted' field for: {}",
						this.getClass().getSimpleName());
					return true;
				}
				else if ("active".equals(fieldName)
					&& (field.getType() == Boolean.class)) {
					field.setAccessible(true);
					field.set(this, Boolean.FALSE);
					LOGGER.debug("Performed soft delete using 'active' field for: {}",
						this.getClass().getSimpleName());
					return true;
				}
				else if ("enabled".equals(fieldName)
					&& (field.getType() == Boolean.class)) {
					field.setAccessible(true);
					field.set(this, Boolean.FALSE);
					LOGGER.debug("Performed soft delete using 'enabled' field for: {}",
						this.getClass().getSimpleName());
					return true;
				}
			}
			LOGGER.debug("No soft delete field found for: {}, hard delete should be used",
				this.getClass().getSimpleName());
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error during soft delete operation using reflection", e);
			throw new RuntimeException("Failed to perform soft delete operation", e);
		}
	}

	@Override
	public String toString() {
		return "%s{id=%s}".formatted(getClass().getSimpleName(), getId());
	}

	/**
	 * Helper method to update audit fields using reflection. Looks for common audit
	 * fields like 'lastModifiedDate', 'updatedAt'.
	 */
	private void updateAuditFields() {

		try {
			final Class<?> entityClass = ProxyUtils.getUserClass(this.getClass());
			final Method updateMethod = findMethod(entityClass, "updateLastModified");

			if (updateMethod != null) {
				updateMethod.setAccessible(true);
				updateMethod.invoke(this);
				LOGGER.debug("Updated audit fields for: {}",
					this.getClass().getSimpleName());
			}
		} catch (final Exception e) {
			// Don't fail the save operation if audit update fails
			LOGGER.debug("Could not update audit fields (this is not an error): {}",
				e.getMessage());
		}
	}
}