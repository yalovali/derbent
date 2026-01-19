package tech.derbent.plm.risks.risk.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.risks.risk.domain.CRisk;
import tech.derbent.plm.risks.risktype.domain.CRiskType;

public interface IRiskRepository extends IEntityOfProjectRepository<CRisk> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :type")
	long countByType(@Param ("type") CRiskType type);
	@Override
	@Query ("""
				SELECT r FROM CRisk r
				LEFT JOIN FETCH r.project
				LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy
				LEFT JOIN FETCH r.createdBy
				LEFT JOIN FETCH r.attachments
			   LEFT JOIN FETCH r.comments
			   LEFT JOIN FETCH r.links
				LEFT JOIN FETCH r.status
				LEFT JOIN FETCH r.entityType et
				LEFT JOIN FETCH et.workflow WHERE r.id = :id
			""")
	Optional<CRisk> findById(@Param ("id") Long id);
	@Override
	@Query ("""
			SELECT r FROM CRisk r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.attachments
			   LEFT JOIN FETCH r.comments
			   LEFT JOIN FETCH r.links
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			WHERE r.project = :project
			ORDER BY r.name ASC
			""")
	List<CRisk> listByProjectForPageView(@Param ("project") CProject<?> project);
}
