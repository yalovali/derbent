package tech.derbent.api.screens.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.api.projects.domain.CProject;

public interface IMasterSectionRepository extends IEntityOfProjectRepository<CMasterSection> {

	@Override
	@Query ("""
			SELECT m FROM CMasterSection m
			LEFT JOIN FETCH m.project
			LEFT JOIN FETCH m.assignedTo
			LEFT JOIN FETCH m.createdBy
			WHERE m.project = :project
			ORDER BY m.name ASC
			""")
	List<CMasterSection> listByProjectForPageView(@Param ("project") CProject<?> project);
}
