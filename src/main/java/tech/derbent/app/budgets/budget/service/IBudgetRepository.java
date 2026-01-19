package tech.derbent.app.budgets.budget.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.budgets.budget.domain.CBudget;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;
import tech.derbent.api.projects.domain.CProject;

public interface IBudgetRepository extends IEntityOfProjectRepository<CBudget> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CBudgetType type);
	@Override
	@Query ("""
		SELECT r FROM CBudget r 
		LEFT JOIN FETCH r.attachments
		LEFT JOIN FETCH r.comments
		LEFT JOIN FETCH r.project 
		LEFT JOIN FETCH r.assignedTo 
		LEFT JOIN FETCH r.createdBy 
		LEFT JOIN FETCH r.status 
		LEFT JOIN FETCH r.entityType et 
		LEFT JOIN FETCH et.workflow 
		WHERE r.id = :id
		"""
	)
	Optional<CBudget> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT r FROM CBudget r
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
	List<CBudget> listByProjectForPageView(@Param ("project") CProject<?> project);
}
