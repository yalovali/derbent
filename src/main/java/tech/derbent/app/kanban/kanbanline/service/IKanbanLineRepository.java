package tech.derbent.app.kanban.kanbanline.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;

public interface IKanbanLineRepository extends IEntityOfCompanyRepository<CKanbanLine> {

	@Override
	/** Loads lines for a company with columns and statuses for UI use. */
	@Query ("""
			   SELECT DISTINCT l FROM #{#entityName} l
			   LEFT JOIN FETCH l.company
			   LEFT JOIN FETCH l.kanbanColumns kc
			   LEFT JOIN FETCH kc.includedStatuses
			   WHERE l.company = :company
			   ORDER BY l.name ASC
	""")
	List<CKanbanLine> findByCompany(@Param ("company") CCompany company);

	@Override
	/** Loads lines for a company with pagination and eager relations. */
	@Query ("""
			   SELECT DISTINCT l FROM #{#entityName} l
			   LEFT JOIN FETCH l.company
			   LEFT JOIN FETCH l.kanbanColumns kc
			   LEFT JOIN FETCH kc.includedStatuses
			   WHERE l.company = :company
			   ORDER BY l.name ASC
	""")
	Page<CKanbanLine> findByCompany(@Param ("company") CCompany company, Pageable pageable);

	@Override
	/** Loads lines for page views with eager relations. */
	@Query ("""
			   SELECT DISTINCT l FROM #{#entityName} l
			   LEFT JOIN FETCH l.company
			   LEFT JOIN FETCH l.kanbanColumns kc
			   LEFT JOIN FETCH kc.includedStatuses
			   WHERE l.company = :company
			   ORDER BY l.name ASC
	""")
	List<CKanbanLine> listByCompanyForPageView(@Param ("company") CCompany company);

	@Override
	/** Loads a line by id with columns and statuses initialized. */
	@Query ("""
			   SELECT DISTINCT l FROM #{#entityName} l
			   LEFT JOIN FETCH l.company
			   LEFT JOIN FETCH l.kanbanColumns kc
			   LEFT JOIN FETCH kc.includedStatuses
			   WHERE l.id = :id
			""")
	Optional<CKanbanLine> findById(@Param ("id") Long id);
}
