package tech.derbent.api.interfaces;

import tech.derbent.api.entity.domain.CEntityDB;

/** Interface for services that provide dependency checking before entity deletion. Services implementing this interface can provide custom logic to
 * determine if an entity can be safely deleted based on its relationships with other entities.
 * @param <EntityClass> the entity type this checker validates */
public interface IDependencyChecker<EntityClass extends CEntityDB<EntityClass>> {

	/** Checks if an entity has dependencies that prevent it from being deleted. This method should check all relevant relationships and return an
	 * appropriate error message if the entity cannot be deleted.
	 * @param entity the entity to check for dependencies
	 * @return null if the entity can be deleted, or an error message describing why it cannot be deleted */
	String checkDependencies(EntityClass entity);
}
