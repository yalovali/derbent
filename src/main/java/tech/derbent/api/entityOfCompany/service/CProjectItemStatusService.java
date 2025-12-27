package tech.derbent.api.entityOfCompany.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;

/** CProjectItemStatusService - Service class for managing CProjectItemStatus entities. Layer: Service (MVC) Provides business logic for activity
 * status management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CProjectItemStatusService extends CStatusService<CProjectItemStatus> implements IEntityRegistrable {

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
	public Class<CProjectItemStatus> getEntityClass() { return CProjectItemStatus.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectItemStatusInitializerService.class; }

	/** Gets the initial/default status from a workflow.
	 * <p>
	 * This method retrieves the workflow's designated initial status, which is the status that should be assigned to new entities. The initial status
	 * is defined in the workflow's status relations where CWorkflowStatusRelation.initialStatus = true.
	 * @param workflow the workflow entity to get initial status from
	 * @return Optional containing the initial status if found, empty otherwise */
        public Optional<CProjectItemStatus> getInitialStatusFromWorkflow(final CWorkflowEntity workflow) {
                Check.notNull(workflow, "Workflow cannot be null when retrieving initial status");
                try {
                        final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
                        if (relations == null || relations.isEmpty()) {
                                LOGGER.warn("No status relations found for workflow: {}", workflow.getName());
                                return Optional.empty();
                        }
                        final Optional<CProjectItemStatus> initialStatus = relations.stream()
                                        .filter(r -> Boolean.TRUE.equals(r.getInitialStatus())).map(CWorkflowStatusRelation::getToStatus).filter(Objects::nonNull).distinct()
                                        .findFirst();
                        if (initialStatus.isPresent()) {
                                return initialStatus;
                        }
                        for (final CWorkflowStatusRelation relation : relations) {
                                if (relation.getFromStatus() != null) {
                                        return Optional.of(relation.getFromStatus());
                                }
                        }
                        final CWorkflowStatusRelation firstRelation = relations.get(0);
                        if (firstRelation.getToStatus() != null) {
                                return Optional.of(firstRelation.getToStatus());
                        }
                } catch (final Exception e) {
                        LOGGER.error("Error retrieving initial status for workflow {}: {}", workflow.getName(), e.getMessage());
                }
                return Optional.empty();
        }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectItemStatus.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Gets the list of valid next statuses for the current entity based on its workflow.
	 * <p>
	 * This method handles two scenarios:
	 * <ul>
	 * <li>For existing entities with a status: Returns current status + valid next statuses from workflow transitions</li>
	 * <li>For new entities without a status: Returns the workflow's initial status (marked in status relations)</li>
	 * </ul>
	 * @param item the project item entity
	 * @return list of valid next statuses */
        public List<CProjectItemStatus> getValidNextStatuses(final IHasStatusAndWorkflow<?> item) {
                try {
                        Check.notNull(item, "Project item cannot be null when retrieving valid next statuses");
                        final List<CProjectItemStatus> validStatuses = new ArrayList<>();
                        final CWorkflowEntity workflow = item.getWorkflow();
                        final CProjectItemStatus currentStatus = item.getStatus();
                        if (currentStatus != null && !validStatuses.contains(currentStatus)) {
                                validStatuses.add(item.getStatus()); // Always include current status
                        }
                        if (workflow == null) {
                                LOGGER.warn("Workflow cannot be null for project item {}; returning default statuses", item.getClass().getSimpleName());
                                addFallbackStatuses(validStatuses);
                                return validStatuses;
                        }
                        // For new items without a status, return initial statuses from the workflow
                        if (currentStatus == null) {
                                final Optional<CProjectItemStatus> initialStatus = getInitialStatusFromWorkflow(workflow);
                                if (initialStatus.isPresent()) {
                                        addIfAbsent(validStatuses, initialStatus.get());
                                        return validStatuses;
                                }
                        }
                        final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
                        if (relations == null || relations.isEmpty()) {
                                addFallbackStatuses(validStatuses);
                                return validStatuses;
                        }
                        if (currentStatus != null && currentStatus.getId() != null) {
                                relations.stream().filter(r -> r.getFromStatus().getId().equals(currentStatus.getId())).map(CWorkflowStatusRelation::getToStatus)
                                                .filter(Objects::nonNull).distinct().filter(r -> !r.getId().equals(currentStatus.getId()))
                                                .forEach(status -> addIfAbsent(validStatuses, status));
                        }
                        if (validStatuses.isEmpty()) {
                                addFallbackStatuses(validStatuses);
                        }
                        return validStatuses;
                } catch (final Exception e) {
                        LOGGER.error("Error retrieving valid next statuses for project item {}: {}", item.toString(), e.getMessage());
                        throw e;
                }
        }

        private void addFallbackStatuses(final List<CProjectItemStatus> validStatuses) {
                list(Pageable.unpaged()).getContent().forEach(status -> addIfAbsent(validStatuses, status));
        }

        private void addIfAbsent(final List<CProjectItemStatus> statuses, final CProjectItemStatus statusToAdd) {
                if (statusToAdd == null) {
                        return;
                }
                final boolean alreadyPresent = statuses.stream().anyMatch(status -> {
                        if (status.getId() != null && statusToAdd.getId() != null) {
                                return status.getId().equals(statusToAdd.getId());
                        }
                        return status.equals(statusToAdd);
                });
                if (!alreadyPresent) {
                        statuses.add(statusToAdd);
                }
        }

	/** Initializes a new activity status with default values. Most common fields are initialized by super class.
	 * @param entity the newly created activity status to initialize */
	@Override
	public void initializeNewEntity(final CProjectItemStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Activity Status");
	}
}
