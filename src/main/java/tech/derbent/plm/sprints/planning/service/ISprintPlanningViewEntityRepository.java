package tech.derbent.plm.sprints.planning.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;

@Repository
public interface ISprintPlanningViewEntityRepository extends IEntityOfProjectRepository<CSprintPlanningViewEntity> {

	@Override
	@Query("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			WHERE e.project = :project
			ORDER BY e.name ASC
			""")
	List<CSprintPlanningViewEntity> listByProjectForPageView(@Param("project") CProject<?> project);
}
