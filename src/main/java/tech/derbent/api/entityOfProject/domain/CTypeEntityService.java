package tech.derbent.api.entityOfProject.domain;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.base.session.service.ISessionService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CTypeEntityService<EntityClass extends CTypeEntity<EntityClass>> extends CEntityOfCompanyService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTypeEntityService.class);
	
	@Autowired(required = false)
	private CWorkflowEntityService workflowEntityService;

	public CTypeEntityService(IEntityOfCompanyRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Check if this type entity is marked as non-deletable
		if (entity.getAttributeNonDeletable()) {
			return "This entity is marked as non-deletable and cannot be removed from the system.";
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		// Intrinsic defaults (color, sortOrder, attributeNonDeletable, canHaveChildren)
		// are initialized in CTypeEntity.initializeDefaults() called by constructor.
		
		// Assign default workflow if not already set
		if (entity.getWorkflow() == null && workflowEntityService != null) {
			try {
				final List<CWorkflowEntity> workflows = workflowEntityService.listByCompany(entity.getCompany());
				if (!workflows.isEmpty()) {
					entity.setWorkflow(workflows.get(0));
					LOGGER.debug("[TypeEntityService] Auto-assigned workflow '{}' to type entity '{}'", 
							workflows.get(0).getName(), entity.getName());
				} else {
					LOGGER.warn("[TypeEntityService] No workflows available for company '{}', type entity '{}' will have no workflow", 
							entity.getCompany().getName(), entity.getName());
				}
			} catch (final Exception e) {
				LOGGER.warn("[TypeEntityService] Failed to auto-assign workflow to type entity '{}': {}", 
						entity.getName(), e.getMessage());
			}
		}
	}
}
