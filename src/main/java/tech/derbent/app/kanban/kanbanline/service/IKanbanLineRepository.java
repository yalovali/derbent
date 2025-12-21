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
	@Query ("""
			   SELECT DISTINCT l FROM #{#entityName} l
			   LEFT JOIN FETCH l.company
			   LEFT JOIN FETCH l.kanbanColumns kc
			   LEFT JOIN FETCH kc.includedStatuses
			   WHERE l.id = :id
			""")
	Optional<CKanbanLine> findById(@Param ("id") Long id);
}
