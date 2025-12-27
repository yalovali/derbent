package tech.derbent.app.comments.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.comments.domain.CCommentPriority;
import tech.derbent.app.projects.domain.CProject;

/** CCommentPriorityRepository - Repository interface for CCommentPriority entities. Layer: Service (MVC) - Repository interface Provides data access
 * methods for comment priority entities. */
public interface ICommentPriorityRepository extends IEntityOfProjectRepository<CCommentPriority> {

	@Override
	@Query ("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.project
			LEFT JOIN FETCH p.assignedTo
			LEFT JOIN FETCH p.createdBy
			LEFT JOIN FETCH p.workflow
			WHERE p.project = :project
			ORDER BY p.name ASC
			""")
	List<CCommentPriority> listByProjectForPageView(@Param ("project") CProject project);
}
