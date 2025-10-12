package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;

/** Interface for receiving notifications when entities are saved, deleted, or updated. This allows components to react to data changes and refresh
 * their content accordingly. */
public interface IEntityUpdateListener {

	default void onEntityDeleted(CEntityDB<?> entity) throws Exception {}

	default void onEntitySaved(CEntityDB<?> entity) throws Exception {}

	default void onEntityUpdated(CEntityDB<?> entity) {}

	default void onEntityCreated(CEntityDB<?> entity) throws Exception {}
}
