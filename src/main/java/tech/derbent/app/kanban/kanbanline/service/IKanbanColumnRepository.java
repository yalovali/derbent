package tech.derbent.app.kanban.kanbanline.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;

/** IKanbanColumnRepository - Repository interface for CKanbanColumn entity.
 * Provides database access for Kanban column definitions within a kanban line. */
public interface IKanbanColumnRepository extends IAbstractRepository<CKanbanColumn> {

	/** Find all columns for a kanban line, ordered by itemOrder.
	 * @param master the kanban line
	 * @return list of columns ordered by itemOrder ascending */
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.kanbanLine WHERE e.kanbanLine = :master ORDER BY e.itemOrder ASC")
	List<CKanbanColumn> findByMaster(@Param ("master") CKanbanLine master);

	/** Get the next item order number for new columns in a kanban line.
	 * @param master the kanban line
	 * @return the next available order number */
	@Query ("SELECT COALESCE(MAX(e.itemOrder), 0) + 1 FROM #{#entityName} e WHERE e.kanbanLine = :master")
	Integer getNextItemOrder(@Param ("master") CKanbanLine master);
}
