package tech.derbent.api.registry;
/** Interface for entities and services that can register themselves with the entity registry. Implementing classes should provide metadata about
 * themselves for fast lookup. */
public interface IEntityRegistrable {

	Class<?> getEntityClass();
	/** Gets the plural title for this entity (e.g., "Activities", "Users", "Projects").
	 * @return the plural entity title */
	Class<?> getPageServiceClass();
	Class<?> getServiceClass();

	default String getSimpleName() { return getEntityClass().getSimpleName(); }
}
