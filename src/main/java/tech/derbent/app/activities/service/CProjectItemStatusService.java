package tech.derbent.app.activities.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.services.CStatusService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.ISessionService;

/** CProjectItemStatusService - Service class for managing CProjectItemStatus entities. Layer: Service (MVC) Provides business logic for activity
 * status management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CProjectItemStatusService extends CStatusService<CProjectItemStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectItemStatusService.class);
	private final CWorkflowStatusRelationService workflowStatusRelationService;

	@Autowired
	public CProjectItemStatusService(final IProjectItemStatusRepository repository, final Clock clock, final ISessionService sessionService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		super(repository, clock, sessionService);
		this.workflowStatusRelationService = workflowStatusRelationService;
	}

	/** Checks dependencies before allowing activity status deletion. Prevents deletion if the status is being used by any activities or workflows.
	 * Always calls super.checkDeleteAllowed() first to ensure all parent-level checks (null validation, non-deletable flag) are performed.
	 * @param entity the activity status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CProjectItemStatus entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	/** Find the default status for new activities.
	 * @return Optional containing the default status if found */
	@Transactional (readOnly = true)
	public Optional<CProjectItemStatus> findDefaultStatus(final CProject project) {
		final Optional<CProjectItemStatus> status = ((CProjectItemStatusService) repository).findDefaultStatus(project);
		return status;
	}

	@Override
	protected Class<CProjectItemStatus> getEntityClass() { return CProjectItemStatus.class; }

	/** Gets the list of valid next statuses for the current entity based on its workflow.
	 * @param projectItem the project item entity
	 * @return list of valid next statuses */
	public List<CProjectItemStatus> getValidNextStatuses(final CProjectItem<?> projectItem) {
		final List<CProjectItemStatus> validStatuses = new ArrayList<>();
		if (projectItem == null) {
			return validStatuses;
		}
		CTypeEntity<?> typeEntity = projectItem.getTypeEntity();
		final CWorkflowEntity workflow = projectItem.getWorkflow();
		Check.notNull(workflow, "Workflow cannot be null for project item: " + projectItem.getId());
		final CProjectItemStatus currentStatus = projectItem.getStatus();
		if (currentStatus != null) {
			validStatuses.add(projectItem.getStatus()); // Always include current status
		} else {
			// todo log warning?
			LOGGER.warn("Current status is null for project item: {}", projectItem.getId());
			// set the default status if current status is null ???
			return validStatuses;
		}
		try {
			// Get workflow relations to find valid next statuses
			final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
			// Find relations where fromStatus matches current status and exclude current status from valid next statuses
			relations.stream().filter(r -> r.getFromStatus().getId().equals(currentStatus.getId())).map(CWorkflowStatusRelation::getToStatus)
					.distinct().filter(r -> r.getId() == currentStatus.getId()).forEach(validStatuses::add);
		} catch (Exception e) {
			LOGGER.error("Error retrieving valid next statuses for project item {}: {}", projectItem.getId(), e.getMessage());
		}
		return validStatuses;
	}

	/** Initializes a new activity status with default values. Most common fields are initialized by super class.
	 * @param entity the newly created activity status to initialize */
	@Override
	public void initializeNewEntity(final CProjectItemStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Activity Status");
	}
}
