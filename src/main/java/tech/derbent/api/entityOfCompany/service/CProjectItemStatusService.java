package tech.derbent.api.entityOfCompany.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;

/** CProjectItemStatusService - Service class for managing CProjectItemStatus entities. Layer: Service (MVC) Provides business logic for activity
 * status management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CProjectItemStatusService extends CStatusService<CProjectItemStatus> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectItemStatusService.class);
	private final CWorkflowStatusRelationService workflowStatusRelationService;

	@Autowired
	public CProjectItemStatusService(final IProjectItemStatusRepository repository, final Clock clock, final ISessionService sessionService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		super(repository, clock, sessionService);
		this.workflowStatusRelationService = workflowStatusRelationService;
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
        @Transactional (readOnly = true)
        public CProjectItemStatus getInitialStatusFromWorkflow(final CWorkflowEntity workflow) {
                Check.notNull(workflow, "Workflow cannot be null when retrieving initial status");
                Check.notNull(workflow.getProject(), "Workflow project cannot be null when retrieving initial status");
                try {
                        final List<CWorkflowStatusRelation> relations = getWorkflowRelationsForProject(workflow,
                                        workflow.getProject());
                        Check.notEmpty(relations, "No status relations found for workflow: " + workflow.getName());
                        final Optional<CProjectItemStatus> initialStatus = relations.stream()
                                        .filter(relation -> Boolean.TRUE.equals(relation.getInitialStatus()))
                                        .map(CWorkflowStatusRelation::getToStatus).filter(Objects::nonNull).findFirst();
                        final CWorkflowStatusRelation fallbackRelation = relations.get(0);
                        Check.notNull(fallbackRelation.getToStatus(), "Initial status cannot be resolved from workflow "
                                        + workflow.getName());
                        return initialStatus.orElseGet(fallbackRelation::getToStatus);
                } catch (final Exception e) {
                        LOGGER.error("Error retrieving initial status for workflow {}: {}", workflow.getName(), e.getMessage());
                        throw e;
                }
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
        @Transactional (readOnly = true)
        public List<CProjectItemStatus> getValidNextStatuses(final IHasStatusAndWorkflow<?> item) {
                try {
                        Check.notNull(item, "Project item cannot be null when retrieving valid next statuses");
                        Check.instanceOf(item, CProjectItem.class, "Workflow items must extend CProjectItem");
                        final CProjectItem<?> projectItem = (CProjectItem<?>) item;
                        final CProject project = projectItem.getProject();
                        Check.notNull(project, "Project cannot be null when retrieving valid next statuses for project item");
                        final CWorkflowEntity workflow = item.getWorkflow();
                        Check.notNull(workflow, "Workflow cannot be null when retrieving valid next statuses for project item");
                        Check.notNull(workflow.getProject(), "Workflow project cannot be null when retrieving valid next statuses");
                        Check.isSameCompany(project, workflow.getProject());
                        Check.equals(project.getId(), workflow.getProject().getId(), "Workflow must belong to the same project");
                        final CProjectItemStatus currentStatus = item.getStatus();
                        if (currentStatus != null) {
                                Check.isSameCompany(project, currentStatus);
                        }
                        final List<CProjectItemStatus> validStatuses = new ArrayList<>();
                        final List<CWorkflowStatusRelation> relations = getWorkflowRelationsForProject(workflow, project);
                        Check.notEmpty(relations, "Workflow " + workflow.getName() + " has no status relations defined");
                        if (currentStatus != null) {
                                validStatuses.add(item.getStatus()); // Always include current status
                        }
                        if (currentStatus == null) {
                                final CProjectItemStatus initialStatus = getInitialStatusFromWorkflow(workflow);
                                Check.notNull(initialStatus,
                                                "Initial status cannot be null when retrieving valid next statuses for new project item");
                                addIfAbsent(validStatuses, initialStatus);
                                return validStatuses;
                        }
                        if (currentStatus != null && currentStatus.getId() != null) {
                                relations.stream()
                                                .filter(relation -> relation.getFromStatus() != null
                                                                && relation.getFromStatus().getId() != null
                                                                && relation.getFromStatus().getId().equals(currentStatus.getId()))
                                                .map(CWorkflowStatusRelation::getToStatus).filter(Objects::nonNull)
                                                .filter(status -> status.getId() == null
                                                                || !status.getId().equals(currentStatus.getId()))
                                                .forEach(status -> addIfAbsent(validStatuses, status));
                        }
                        return validStatuses;
                } catch (final Exception e) {
                        LOGGER.error("Error retrieving valid next statuses for project item {}: {}", item.toString(), e.getMessage());
                        throw e;
                }
        }

        private List<CWorkflowStatusRelation> getWorkflowRelationsForProject(final CWorkflowEntity workflow, final CProject project) {
                Check.notNull(workflow, "Workflow cannot be null when loading relations");
                Check.notNull(project, "Project cannot be null when loading workflow relations");
                Check.notNull(workflow.getProject(), "Workflow project cannot be null when loading relations");
                Check.equals(project.getId(), workflow.getProject().getId(), "Workflow must belong to the provided project");
                Check.isSameCompany(project, workflow.getProject());
                final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
                if (relations == null) {
                        return List.of();
                }
                return relations.stream()
                                .filter(relation -> relation.getFromStatus() != null && relation.getToStatus() != null)
                                .peek(relation -> {
                                        Check.isSameCompany(project, relation.getFromStatus());
                                        Check.isSameCompany(project, relation.getToStatus());
                                })
                                .sorted(Comparator.comparing((CWorkflowStatusRelation relation) -> {
                                        final Integer sortOrder = relation.getToStatus().getSortOrder();
                                        return sortOrder != null ? sortOrder : Integer.MAX_VALUE;
                                }).thenComparing(CWorkflowStatusRelation::getId, Comparator.nullsLast(Long::compareTo)))
                                .toList();
        }

	/** Initializes a new activity status with default values. Most common fields are initialized by super class.
	 * @param entity the newly created activity status to initialize */
	@Override
	public void initializeNewEntity(final CProjectItemStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Activity Status");
	}
}
