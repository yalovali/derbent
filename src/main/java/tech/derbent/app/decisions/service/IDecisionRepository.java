package tech.derbent.app.decisions.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.decisions.domain.CDecision;

public interface IDecisionRepository extends IEntityOfProjectRepository<CDecision> {

	@Override
	@Query ("""
			SELECT d FROM #{#entityName} d
			LEFT JOIN FETCH d.project
			LEFT JOIN FETCH d.assignedTo
			LEFT JOIN FETCH d.createdBy
			LEFT JOIN FETCH d.attachments
			   LEFT JOIN FETCH d.comments
		   LEFT JOIN FETCH r.links
			LEFT JOIN FETCH d.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH d.status
			WHERE d.id = :id
				""")
	Optional<CDecision> findById(@Param ("id") Long id);
	@Override
	@Query ("""
				SELECT d FROM #{#entityName} d
				LEFT JOIN FETCH d.project
				LEFT JOIN FETCH d.assignedTo
				LEFT JOIN FETCH d.createdBy
				LEFT JOIN FETCH d.attachments
			   LEFT JOIN FETCH d.comments
		   LEFT JOIN FETCH r.links
				LEFT JOIN FETCH d.entityType et
				LEFT JOIN FETCH et.workflow
				LEFT JOIN FETCH d.status
				WHERE d.project = :project
			""")
	Page<CDecision> listByProject(@Param ("project") CProject project, Pageable pageable);
	@Override
	@Query ("""
				SELECT d FROM #{#entityName} d
				LEFT JOIN FETCH d.project
				LEFT JOIN FETCH d.assignedTo
				LEFT JOIN FETCH d.createdBy
				LEFT JOIN FETCH d.attachments
			   LEFT JOIN FETCH d.comments
		   LEFT JOIN FETCH r.links
				LEFT JOIN FETCH d.entityType et
				LEFT JOIN FETCH et.workflow
				LEFT JOIN FETCH d.status
				WHERE d.project = :project
			""")
	List<CDecision> listByProjectForPageView(@Param ("project") CProject project);
}
