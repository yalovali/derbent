package tech.derbent.app.gannt.ganntviewentity.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;
import tech.derbent.api.projects.domain.CProject;

@Repository
public interface IGanntViewEntityRepository extends IEntityOfProjectRepository<CGanntViewEntity> {

	@Override
	@Query ("""
			SELECT g FROM #{#entityName} g
			LEFT JOIN FETCH g.project
			LEFT JOIN FETCH g.assignedTo
			LEFT JOIN FETCH g.createdBy
			WHERE g.project = :project
			ORDER BY g.name ASC
			""")
	List<CGanntViewEntity> listByProjectForPageView(@Param ("project") CProject<?> project);
}
