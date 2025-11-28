package tech.derbent.app.sprints.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/** ISprintItemRepository - Repository interface for CSprintItem entity. Provides database access for sprint items. */
public interface ISprintItemRepository extends IAbstractRepository<CSprintItem> {

	@Query ("SELECT a FROM #{#entityName} a WHERE a.itemType = :itemType ORDER BY a.itemOrder ASC")
	List<CSprintItem> findByItemType(@Param ("itemType") String itemType);
	@Query ("SELECT a FROM #{#entityName} a WHERE a.itemId = :itemId ORDER BY a.itemOrder ASC")
	List<CDetailLines> findByMaster(@Param ("master") CSprint master);
	@Query ("SELECT a FROM #{#entityName} si WHERE a.sprint.id = :sprintId AND a.itemId = :itemId ORDER BY a.itemOrder ASC")
	CSprintItem findBySprintIdAndItemId(@Param ("sprintId") Long sprintId, @Param ("itemId") Long itemId);
	@Query ("SELECT  FROM #{#entityName} si WHERE a.sprint.id = :sprintId ORDER BY a.itemOrder ASC")
	List<CSprintItem> findBySprintIdOrderByItemOrderAsc(@Param ("sprintId") Long sprintId);
}
