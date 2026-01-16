package tech.derbent.app.milestones.milestone.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.milestones.milestone.domain.CMilestone;
import tech.derbent.app.milestones.milestonetype.domain.CMilestoneType;
import tech.derbent.api.projects.domain.CProject;

public interface IMilestoneRepository extends IEntityOfProjectRepository<CMilestone> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CMilestoneType type);
	@Override
	@Query ("""
			SELECT r FROM CMilestone r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			WHERE r.id = :id
			""")
	Optional<CMilestone> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT r FROM CMilestone r
			LEFT JOIN FETCH r.project
			LEFT JOIN FETCH r.assignedTo
			LEFT JOIN FETCH r.createdBy
			LEFT JOIN FETCH r.status
			LEFT JOIN FETCH r.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH r.attachments
			LEFT JOIN FETCH r.comments
			WHERE r.project = :project
			ORDER BY r.name ASC
			""")
	List<CMilestone> listByProjectForPageView(@Param ("project") CProject project);
}
