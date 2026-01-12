package tech.derbent.app.risklevel.risklevel.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.risklevel.risklevel.domain.CRiskLevel;
import tech.derbent.api.projects.domain.CProject;

public interface IRiskLevelRepository extends IEntityOfProjectRepository<CRiskLevel> {

	@Override
	@Query (
		"SELECT r FROM CRiskLevel r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy "
				+ "LEFT JOIN FETCH r.status " + "WHERE r.id = :id"
	)
	Optional<CRiskLevel> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT r FROM CRiskLevel r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			WHERE r.project = :project
			ORDER BY r.name ASC
			""")
	List<CRiskLevel> listByProjectForPageView(@Param ("project") CProject project);
}
