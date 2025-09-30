package tech.derbent.api.interfaces;

import java.util.List;
import java.util.Map;

/** CKanbanService - Interface for services that provide data for Kanban boards. Layer: Service Interface (Abstraction) Provides the contract for
 * services that can supply entities grouped by status for kanban board display.
 * @param <T> the type of entity this service manages
 * @param <S> the type of status this service works with */
public interface IKanbanService<T extends IKanbanEntity, S extends IKanbanStatus> {

	Map<S, List<T>> getEntitiesGroupedByStatus(Long projectId);
	T updateEntityStatus(T entity, S newStatus);
	List<S> getAllStatuses(Long projectId);
}
