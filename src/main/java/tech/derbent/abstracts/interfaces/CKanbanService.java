package tech.derbent.abstracts.interfaces;

import java.util.List;
import java.util.Map;

/** CKanbanService - Interface for services that provide data for Kanban boards. Layer: Service Interface (Abstraction) Provides the contract for
 * services that can supply entities grouped by status for kanban board display.
 * @param <T> the type of entity this service manages
 * @param <S> the type of status this service works with */
public interface CKanbanService<T extends CKanbanEntity, S extends CKanbanStatus> {

	/** Gets all entities grouped by their status for the given project.
	 * @param projectId the ID of the project to get entities for
	 * @return a map of status to list of entities */
	Map<S, List<T>> getEntitiesGroupedByStatus(Long projectId);
	/** Updates the status of an entity and saves it.
	 * @param entity    the entity to update
	 * @param newStatus the new status to set
	 * @return the updated entity */
	T updateEntityStatus(T entity, S newStatus);
	/** Gets all available statuses for this entity type.
	 * @return list of available statuses, sorted by sort order */
	List<S> getAllStatuses();
}
