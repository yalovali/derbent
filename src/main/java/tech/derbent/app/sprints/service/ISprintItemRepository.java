package tech.derbent.app.sprints.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.sprints.domain.CSprintItem;

/**
 * ISprintItemRepository - Repository interface for CSprintItem entity.
 * Provides database access for sprint items.
 */
public interface ISprintItemRepository extends IAbstractRepository<CSprintItem> {

	/**
	 * Find all sprint items for a specific sprint, ordered by itemOrder.
	 * @param sprintId the sprint ID
	 * @return list of sprint items
	 */
	@Query("SELECT si FROM CSprintItem si WHERE si.sprint.id = :sprintId ORDER BY si.itemOrder ASC")
	List<CSprintItem> findBySprintIdOrderByItemOrderAsc(@Param("sprintId") Long sprintId);

	/**
	 * Find all sprint items of a specific type.
	 * @param itemType the item type (e.g., "CActivity", "CMeeting")
	 * @return list of sprint items
	 */
	@Query("SELECT si FROM CSprintItem si WHERE si.itemType = :itemType")
	List<CSprintItem> findByItemType(@Param("itemType") String itemType);

	/**
	 * Find a sprint item by sprint ID and item ID.
	 * @param sprintId the sprint ID
	 * @param itemId the item ID
	 * @return the sprint item, or null if not found
	 */
	@Query("SELECT si FROM CSprintItem si WHERE si.sprint.id = :sprintId AND si.itemId = :itemId")
	CSprintItem findBySprintIdAndItemId(@Param("sprintId") Long sprintId, @Param("itemId") Long itemId);
}
