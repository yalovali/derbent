package tech.derbent.api.interfaces;

import tech.derbent.api.entity.domain.CEntityDB;

/** Interface for receiving notifications when entities are saved, deleted, or updated. This allows components to react to data changes and refresh
 * their content accordingly. */
public interface IEntityUpdateListener<EntityClass extends CEntityDB<EntityClass>> {

	void on_entity_deleted(EntityClass entity);
	void on_entity_saved(EntityClass savedEntity);
	void onEntityCreated(EntityClass newEntity) throws Exception;
	void onEntityRefreshed(EntityClass reloaded) throws Exception;
}
