package tech.derbent.plm.projectexpenses.projectexpense.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.plm.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.api.projects.domain.CProject;

public interface IProjectExpenseRepository extends IEntityOfProjectRepository<CProjectExpense> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProjectExpenseType type);
	@Override
	@Query ("""
			SELECT r FROM CProjectExpense r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			WHERE r.id = :id
			""")
	Optional<CProjectExpense> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT r FROM CProjectExpense r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			WHERE r.project = :project
			ORDER BY r.name ASC
			""")
	List<CProjectExpense> listByProjectForPageView(@Param ("project") CProject<?> project);
}
