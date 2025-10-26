package tech.derbent.app.workflow.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

/** IWorkflowEntityRepository - Repository interface for CWorkflowEntity entities. Layer: Data Access (MVC) Provides data access operations for
 * workflow entity management. */
@Repository
public interface IWorkflowEntityRepository extends IWorkflowRepository<CWorkflowEntity> {

	/** Find all workflows for a specific project and target entity class.
	 * @param project           the project to filter by
	 * @param targetEntityClass the fully qualified class name to filter by
	 * @return list of matching workflows */
	@Query ("SELECT w FROM CWorkflowEntity w WHERE w.project = :project AND w.targetEntityClass = :targetEntityClass")
	List<CWorkflowEntity> findByProjectAndTargetEntityClass(@Param ("project") CProject project,
			@Param ("targetEntityClass") String targetEntityClass);
}
