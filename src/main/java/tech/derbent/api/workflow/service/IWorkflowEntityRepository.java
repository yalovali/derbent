package tech.derbent.api.workflow.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.workflow.domain.CWorkflowEntity;

/** IWorkflowEntityRepository - Repository interface for CWorkflowEntity entities. Layer: Data Access (MVC) Provides data access operations for
 * workflow entity management. */
@Repository
public interface IWorkflowEntityRepository extends IWorkflowRepository<CWorkflowEntity> {/**/
}
