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
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.domains.IHasStatusAndWorkflow;
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
	 * @param item the project item entity
	 * @return list of valid next statuses */
	public List<CProjectItemStatus> getValidNextStatuses(final IHasStatusAndWorkflow<?> item) {
		final List<CProjectItemStatus> validStatuses = new ArrayList<>();
		if (item == null) {
			return validStatuses;
		}
		final CWorkflowEntity workflow = item.getWorkflow();
		Check.notNull(workflow, "Workflow cannot be null for project item");
		final CProjectItemStatus currentStatus = item.getStatus();
		if (currentStatus != null) {
			validStatuses.add(item.getStatus()); // Always include current status
		} else {
			// For new items without a status, return initial statuses from the workflow
			LOGGER.debug("Getting initial statuses for new project item with workflow: {}", workflow.getName());
			try {
				final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
				// Get statuses from relations marked as initial
				relations.stream().filter(r -> r.getInitialStatus() != null && r.getInitialStatus()).map(CWorkflowStatusRelation::getToStatus)
						.distinct().forEach(validStatuses::add);
				// If no initial statuses found, use the first status in the workflow
				if (validStatuses.isEmpty() && !relations.isEmpty()) {
					validStatuses.add(relations.get(0).getFromStatus());
				}
			} catch (Exception e) {
				LOGGER.error("Error retrieving initial statuses for project item: {}", e.getMessage());
			}
			return validStatuses;
		}
		try {
			// Get workflow relations to find valid next statuses
			final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
			// Find relations where fromStatus matches current status and exclude current status from valid next statuses
			relations.stream().filter(r -> r.getFromStatus().getId().equals(currentStatus.getId())).map(CWorkflowStatusRelation::getToStatus)
					.distinct().filter(r -> !r.getId().equals(currentStatus.getId())).forEach(validStatuses::add);
		} catch (Exception e) {
			LOGGER.error("Error retrieving valid next statuses for project item {}: {}", item.toString(), e.getMessage());
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
