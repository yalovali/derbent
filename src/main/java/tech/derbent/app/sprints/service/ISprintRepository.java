package tech.derbent.app.sprints.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintType;

/** ISprintRepository - Repository interface for CSprint entity. Provides data access methods for sprint management. */
public interface ISprintRepository extends IProjectItemRespository<CSprint> {

	@Query ("SELECT COUNT(s) FROM #{#entityName} s WHERE s.entityType = :entityType")
	long countByType(@Param ("entityType") CSprintType type);
	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			LEFT JOIN FETCH s.project
			LEFT JOIN FETCH s.assignedTo
			LEFT JOIN FETCH s.createdBy
			LEFT JOIN FETCH s.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH s.attachments
			LEFT JOIN FETCH s.status
			LEFT JOIN FETCH s.sprintItems si
			WHERE s.id = :id
			""")
	Optional<CSprint> findById(@Param ("id") Long id);
	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			LEFT JOIN FETCH s.project
			LEFT JOIN FETCH s.assignedTo
			LEFT JOIN FETCH s.createdBy
			LEFT JOIN FETCH s.attachments
			LEFT JOIN FETCH s.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH s.status
			LEFT JOIN FETCH s.sprintItems si
			WHERE s.project = :project
			""")
	Page<CSprint> listByProject(@Param ("project") CProject project, Pageable pageable);
	@Override
	@Query ("""
			SELECT s FROM #{#entityName} s
			LEFT JOIN FETCH s.project
			LEFT JOIN FETCH s.assignedTo
			LEFT JOIN FETCH s.createdBy
			LEFT JOIN FETCH s.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH s.attachments
			LEFT JOIN FETCH s.status
			LEFT JOIN FETCH s.sprintItems si
			WHERE s.project = :project
			""")
	List<CSprint> listByProjectForPageView(@Param ("project") CProject project);
}
