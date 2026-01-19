package tech.derbent.api.workflow.service;

import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.workflow.domain.CWorkflowBase;

/** Base repository interface for workflow entities. Marked as NoRepositoryBean to prevent Spring from creating concrete implementations. */
@NoRepositoryBean
public interface IWorkflowRepository<EntityClass extends CWorkflowBase<EntityClass>> extends IEntityOfCompanyRepository<EntityClass> {
	// No additional methods needed - uses inherited operations
}
