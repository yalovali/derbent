package tech.derbent.app.workflow.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowBase;

@NoRepositoryBean
public interface IWorkflowRepository<EntityClass extends CWorkflowBase<EntityClass>> extends IEntityOfProjectRepository<EntityClass> {

	@Override
	@Query ("""
			SELECT w FROM #{#entityName} w
			LEFT JOIN FETCH w.project
			LEFT JOIN FETCH w.assignedTo
			LEFT JOIN FETCH w.createdBy
			LEFT JOIN FETCH w.status
			WHERE w.project = :project
			ORDER BY w.name ASC
			""")
	List<EntityClass> listByProjectForPageView(@Param ("project") CProject project);
}
