package tech.derbent.api.interfaces;

import tech.derbent.api.entity.domain.CEntityDB;

/** Interface for entities that support cloning functionality. All entities extending CEntityDB must implement this interface to provide clone
 * capabilities with various depth options. The cloning process follows a recursive pattern where each entity delegates to its parent class to clone
 * inherited fields, then adds its own specific fields. */
public interface ICloneable<EntityClass extends CEntityDB<EntityClass>> {

	/** Creates a clone of this entity with the specified options. Implementation pattern: 1. Call super.createClone(options) if applicable 2. Clone
	 * own fields based on options 3. Return the cloned entity
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the entity with cloned data
	 * @throws CloneNotSupportedException if cloning fails */
	EntityClass createClone(CCloneOptions options) throws Exception;
	/** Gets the target entity class for this cloneable entity. Used for type-safe cloning and cross-entity compatibility checks.
	 * @return the entity class */
	Class<EntityClass> getEntityClass();
}
