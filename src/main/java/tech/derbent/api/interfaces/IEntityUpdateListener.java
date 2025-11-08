package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;

/** Interface for receiving notifications when entities are saved, deleted, or updated. This allows components to react to data changes and refresh
 * their content accordingly. */
public interface IEntityUpdateListener<EntityClass extends CEntityDB<EntityClass>> {

	void onEntityCreated(EntityClass newEntity) throws Exception;
	void onEntityDeleted(EntityClass entity) throws Exception;
	void onEntityRefreshed(EntityClass reloaded) throws Exception;
	void onEntitySaved(EntityClass savedEntity) throws Exception;
}
