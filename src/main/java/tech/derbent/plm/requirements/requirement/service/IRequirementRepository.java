package tech.derbent.plm.requirements.requirement.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.requirements.requirement.domain.CRequirement;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

public interface IRequirementRepository extends IEntityOfProjectRepository<CRequirement> {

	@Query("SELECT COUNT(r) FROM CRequirement r WHERE r.entityType = :type")
	long countByType(@Param("type") CRequirementType type);

	@Override
	@Query("""
			SELECT r FROM CRequirement r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			LEFT JOIN FETCH r.links
			WHERE r.id = :id
			""")
	Optional<CRequirement> findById(@Param("id") Long id);

	@Override
	@Query("""
			SELECT r FROM CRequirement r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			LEFT JOIN FETCH r.links
			WHERE r.project = :project
			ORDER BY r.startDate ASC NULLS LAST, r.name ASC
			""")
	List<CRequirement> listByProjectForPageView(@Param("project") CProject<?> project);
}
