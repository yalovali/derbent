package tech.derbent.app.sprints.service;

import java.util.List;
import java.util.Optional;
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

	@Override
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.sprint
			WHERE e.id = :id
			""")
	Optional<CSprintItem> findById(@Param ("id") Long id);
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.sprint WHERE e.itemType = :itemType ORDER BY e.itemOrder ASC")
	List<CSprintItem> findByItemType(@Param ("itemType") String itemType);
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.sprint WHERE e.sprint = :master ORDER BY e.itemOrder ASC")
	List<CSprintItem> findByMaster(@Param ("master") CSprint master);
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.sprint WHERE e.sprint.id = :masterId ORDER BY e.itemOrder ASC")
	List<CSprintItem> findByMasterId(@Param ("masterId") Long masterId);
}
