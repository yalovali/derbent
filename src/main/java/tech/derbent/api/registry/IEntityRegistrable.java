package tech.derbent.api.registry;

/**
 * Interface for entities and services that can register themselves with the entity registry.
 * Implementing classes should provide metadata about themselves for fast lookup.
 */
public interface IEntityRegistrable {

	/**
	 * Gets the entity class that this registrable is associated with.
	 * @return the entity class
	 */
	Class<?> getEntityClass();

	/**
	 * Gets the service class associated with the entity.
	 * @return the service class
	 */
	Class<?> getServiceClass();

	/**
	 * Gets the initializer service class for the entity.
	 * @return the initializer service class, or null if not applicable
	 */
	default Class<?> getInitializerServiceClass() {
		return null;
	}

	/**
	 * Gets the page service class for the entity.
	 * @return the page service class, or null if not applicable
	 */
	default Class<?> getPageServiceClass() {
		return null;
	}

	/**
	 * Gets the default icon for the entity.
	 * @return the icon string (e.g., "vaadin:user"), or null if not applicable
	 */
	default String getDefaultIcon() {
		return null;
	}

	/**
	 * Gets the default color code for the entity.
	 * @return the color code (e.g., "#FF0000"), or null if not applicable
	 */
	default String getDefaultColor() {
		return null;
	}

	/**
	 * Gets the simple name identifier for the entity.
	 * @return the simple name (e.g., "CActivity")
	 */
	default String getSimpleName() {
		return getEntityClass().getSimpleName();
	}
}
