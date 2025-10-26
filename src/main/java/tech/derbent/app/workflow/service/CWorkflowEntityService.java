package tech.derbent.app.workflow.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.view.CComponentWorkflowStatusRelations;
import tech.derbent.base.session.service.ISessionService;

/** CWorkflowEntityService - Service class for managing CWorkflowEntity entities. Layer: Service (MVC) Provides business logic for workflow entity
 * management including CRUD operations and validation. */
@Service
@Transactional
public class CWorkflowEntityService extends CWorkflowBaseService<CWorkflowEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowEntityService.class);
	private final IWorkflowEntityRepository workflowEntityRepository;

	@Autowired
	public CWorkflowEntityService(final IWorkflowEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		this.workflowEntityRepository = repository;
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
			CComponentWorkflowStatusRelations component = new CComponentWorkflowStatusRelations(this, sessionService);
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

	/** Gets a random workflow entity for a specific project and entity class.
	 * @param project     the project to filter by
	 * @param entityClass the entity class to filter by (e.g., CActivity.class, CMeeting.class)
	 * @return a random workflow matching the criteria, or null if none found */
	public CWorkflowEntity getRandomByEntityType(final CProject project, final Class<?> entityClass) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(entityClass, "Entity class cannot be null");
		final String className = entityClass.getSimpleName();
		final List<CWorkflowEntity> workflows = workflowEntityRepository.findByProjectAndTargetEntityClass(project, className);
		Check.notEmpty(workflows, "Workflows list cannot be empty for project: " + project.getName() + " and entity class: " + className);
		final int randomIndex = (int) (Math.random() * workflows.size());
		return workflows.get(randomIndex);
	}

	/** Initializes a new workflow entity with default values.
	 * @param entity the newly created workflow entity to initialize */
	@Override
	public void initializeNewEntity(final CWorkflowEntity entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Workflow");
	}
}
