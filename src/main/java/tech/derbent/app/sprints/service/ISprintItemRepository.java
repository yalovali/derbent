package tech.derbent.app.sprints.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/** ISprintItemRepository - Repository interface for CSprintItem entity. Provides database access for sprint items.
 * <p>
 * Naming Convention for Query Methods (per coding standards):
 * <ul>
 * <li>{@code findByMaster(M master)} - Find all child entities by master/parent entity</li>
 * <li>{@code findByMasterId(Long masterId)} - Find all child entities by master/parent ID</li>
 * <li>{@code countByMaster(M master)} - Count child entities by master/parent entity</li>
 * <li>{@code getNextItemOrder(M master)} - Get next order number for new items</li>
 * </ul>
 */
public interface ISprintItemRepository extends IAbstractRepository<CSprintItem> {

	/** Find all sprint items of a specific type.
	 * @param itemType the item type (e.g., "CActivity", "CMeeting")
	 * @return list of sprint items ordered by itemOrder ascending */
	@Query ("SELECT e FROM #{#entityName} e WHERE e.itemType = :itemType ORDER BY e.itemOrder ASC")
	List<CSprintItem> findByItemType(@Param ("itemType") String itemType);
	/** Find all sprint items for a specific sprint (master), ordered by itemOrder.
	 * @param master the sprint entity
	 * @return list of sprint items ordered by itemOrder ascending */
	@Query ("SELECT e FROM #{#entityName} e WHERE e.sprint = :master ORDER BY e.itemOrder ASC")
	List<CSprintItem> findByMaster(@Param ("master") CSprint master);
	/** Find all sprint items by sprint ID, ordered by itemOrder.
	 * @param masterId the sprint ID
	 * @return list of sprint items ordered by itemOrder ascending */
	@Query ("SELECT e FROM #{#entityName} e WHERE e.sprint.id = :masterId ORDER BY e.itemOrder ASC")
	List<CSprintItem> findByMasterId(@Param ("masterId") Long masterId);
}
