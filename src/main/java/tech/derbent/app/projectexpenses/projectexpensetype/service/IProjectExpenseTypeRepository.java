package tech.derbent.app.projectexpenses.projectexpensetype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.app.projects.domain.CProject;

public interface IProjectExpenseTypeRepository extends IEntityOfProjectRepository<CProjectExpenseType> {

	@Override
	@Query ("""
			SELECT t FROM #{#entityName} t
			LEFT JOIN FETCH t.project
			LEFT JOIN FETCH t.assignedTo
			LEFT JOIN FETCH t.createdBy
			LEFT JOIN FETCH t.workflow
			WHERE t.project = :project
			ORDER BY t.name ASC
			""")
	List<CProjectExpenseType> listByProjectForPageView(@Param ("project") CProject project);
}
