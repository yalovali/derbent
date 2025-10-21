package tech.derbent.app.workflow.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.view.CComponentWorkflowStatusRelations;
import tech.derbent.base.session.service.ISessionService;

/** CWorkflowEntityService - Service class for managing CWorkflowEntity entities. Layer: Service (MVC) Provides business logic for workflow entity
 * management including CRUD operations and validation. */
@Service
@Transactional
public class CWorkflowEntityService extends CWorkflowBaseService<CWorkflowEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowEntityService.class);
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	public CWorkflowEntityService(final IWorkflowEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing workflow entity deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * are performed.
	 * @param entity the workflow entity to check
	 * @return null if entity can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CWorkflowEntity entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Add workflow-specific dependency checks here if needed in the future
			return null; // Entity can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for workflow entity: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	public Component createWorkflowStatusRelationsComponent(CWorkflowEntity workflowEntity) {
		try {
			CComponentWorkflowStatusRelations component = new CComponentWorkflowStatusRelations(this, sessionService, applicationContext);
			return component;
		} catch (Exception e) {
			LOGGER.error("Failed to create workflow status relations component.");
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading workflow status relations component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	protected Class<CWorkflowEntity> getEntityClass() { return CWorkflowEntity.class; }

	/** Initializes a new workflow entity with default values.
	 * @param entity the newly created workflow entity to initialize */
	@Override
	public void initializeNewEntity(final CWorkflowEntity entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Workflow");
	}
}
