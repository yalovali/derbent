package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;

/** Interface for receiving notifications when entities are saved, deleted, or updated. This allows components to react to data changes and refresh
 * their content accordingly. */
public interface IEntityUpdateListener {

	/** Called when an entity has been successfully deleted.
	 * @param entity the entity that was deleted
	 * @throws Exception */
	default void onEntityDeleted(CEntityDB<?> entity) throws Exception {
		// Default implementation does nothing
	}

	/** Called when an entity has been successfully saved.
	 * @param entity the entity that was saved
	 * @throws Exception */
	default void onEntitySaved(CEntityDB<?> entity) throws Exception {
		// Default implementation does nothing
	}

	/** Called when an entity has been updated (general purpose notification).
	 * @param entity the entity that was updated */
	default void onEntityUpdated(CEntityDB<?> entity) {
		// Default implementation does nothing
	}
}
