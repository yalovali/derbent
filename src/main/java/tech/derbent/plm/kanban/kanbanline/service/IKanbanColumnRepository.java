package tech.derbent.plm.kanban.kanbanline.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;

/** IKanbanColumnRepository - Repository interface for CKanbanColumn entity.
 * Provides database access for Kanban column definitions within a kanban line. */
public interface IKanbanColumnRepository extends IAbstractRepository<CKanbanColumn> {

	/** Find all columns for a kanban line, ordered by itemOrder.
	 * @param master the kanban line
	 * @return list of columns ordered by itemOrder ascending */
	@Query ("SELECT DISTINCT e FROM #{#entityName} e LEFT JOIN FETCH e.kanbanLine LEFT JOIN FETCH e.includedStatuses "
			+ "WHERE e.kanbanLine = :master ORDER BY e.itemOrder ASC")
	List<CKanbanColumn> findByMaster(@Param ("master") CKanbanLine master);

	/** Get the next item order number for new columns in a kanban line.
	 * @param master the kanban line
	 * @return the next available order number */
	@Query ("SELECT COALESCE(MAX(e.itemOrder), 0) + 1 FROM #{#entityName} e WHERE e.kanbanLine = :master")
	Integer getNextItemOrder(@Param ("master") CKanbanLine master);

	@Query ("SELECT DISTINCT e FROM #{#entityName} e LEFT JOIN FETCH e.kanbanLine LEFT JOIN FETCH e.includedStatuses "
			+ "WHERE e.kanbanLine = :master AND LOWER(e.name) = LOWER(:name)")
	/** Finds a column by line and case-insensitive name. */
	Optional<CKanbanColumn> findByMasterAndNameIgnoreCase(@Param ("master") CKanbanLine master, @Param ("name") String name);

	/** Loads a column with its line and included statuses. */
	@Query ("SELECT DISTINCT e FROM #{#entityName} e LEFT JOIN FETCH e.kanbanLine LEFT JOIN FETCH e.includedStatuses WHERE e.id = :id")
	Optional<CKanbanColumn> findByIdWithLine(@Param ("id") Long id);
}
