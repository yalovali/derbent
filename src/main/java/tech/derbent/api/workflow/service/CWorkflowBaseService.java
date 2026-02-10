package tech.derbent.api.workflow.service;

import java.time.Clock;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.workflow.domain.CWorkflowBase;
import tech.derbent.api.session.service.ISessionService;

/** CWorkflowBaseService - Abstract base service for workflow entities. Layer: Service (MVC) Provides common business logic operations for workflow
 * entities. */
public abstract class CWorkflowBaseService<EntityClass extends CWorkflowBase<EntityClass>> extends CEntityOfCompanyService<EntityClass> {

	public CWorkflowBaseService(final IWorkflowRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// No additional workflow-specific checks by default
		return null;
	}
}
