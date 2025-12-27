package tech.derbent.app.components.componenttype.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.components.componenttype.domain.CProjectComponentType;
import tech.derbent.app.projects.domain.CProject;

public interface IProjectComponentTypeRepository extends IEntityOfProjectRepository<CProjectComponentType> {

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
	List<CProjectComponentType> listByProjectForPageView(@Param ("project") CProject project);
}
