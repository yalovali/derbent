package tech.derbent.app.projectincomes.projectincometype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.projectincomes.projectincometype.domain.CProjectIncomeType;
import tech.derbent.app.projects.domain.CProject;

public interface IProjectIncomeTypeRepository extends IEntityOfProjectRepository<CProjectIncomeType> {

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
	List<CProjectIncomeType> listByProjectForPageView(@Param ("project") CProject project);
}
