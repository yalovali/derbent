package tech.derbent.app.activities.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CStatusService;
import tech.derbent.app.activities.domain.CActivityStatus;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.IWorkflowStatusRelationRepository;
import tech.derbent.base.session.service.ISessionService;

/** CActivityStatusService - Service class for managing CActivityStatus entities. Layer: Service (MVC) Provides business logic for activity status
 * management including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CActivityStatusService extends CStatusService<CActivityStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityStatusService.class);
	@Autowired
	private final IActivityRepository activityRepository;
	@Autowired
	private final IWorkflowStatusRelationRepository workflowStatusRelationRepository;

	@Autowired
	public CActivityStatusService(final IActivityStatusRepository repository, final Clock clock, final ISessionService sessionService,
			final IActivityRepository activityRepository, final IWorkflowStatusRelationRepository workflowStatusRelationRepository) {
		super(repository, clock, sessionService);
		this.activityRepository = activityRepository;
		this.workflowStatusRelationRepository = workflowStatusRelationRepository;
	}

	/** Checks dependencies before allowing activity status deletion. Prevents deletion if the status is being used by any activities or workflows.
	 * Always calls super.checkDeleteAllowed() first to ensure all parent-level checks (null validation, non-deletable flag) are performed.
	 * @param entity the activity status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CActivityStatus entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Check if any activities are using this status
			final long usageCount = activityRepository.countByActivityStatus(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d activit%s.", usageCount, usageCount == 1 ? "y" : "ies");
			}
			// Check if the status is used in any workflows
			final List<CWorkflowStatusRelation> fromStatusRelations = workflowStatusRelationRepository.findByFromStatusId(entity.getId());
			final List<CWorkflowStatusRelation> toStatusRelations = workflowStatusRelationRepository.findByToStatusId(entity.getId());
			if (!fromStatusRelations.isEmpty() || !toStatusRelations.isEmpty()) {
				// Collect unique workflow names
				final List<String> workflowNames =
						fromStatusRelations.stream().map(r -> r.getWorkflowEntity().getName()).distinct().collect(Collectors.toList());
				final List<String> toWorkflowNames =
						toStatusRelations.stream().map(r -> r.getWorkflowEntity().getName()).distinct().collect(Collectors.toList());
				workflowNames.addAll(toWorkflowNames);
				final List<String> uniqueWorkflowNames = workflowNames.stream().distinct().collect(Collectors.toList());
				if (uniqueWorkflowNames.size() == 1) {
					return String.format("Cannot delete. This status is used in workflow: %s.", uniqueWorkflowNames.get(0));
				} else {
					return String.format("Cannot delete. This status is used in workflows: %s.", String.join(", ", uniqueWorkflowNames));
				}
			}
			return null; // Status can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for activity status: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	/** Find the default status for new activities.
	 * @return Optional containing the default status if found */
	@Transactional (readOnly = true)
	public Optional<CActivityStatus> findDefaultStatus(final CProject project) {
		final Optional<CActivityStatus> status = ((CActivityStatusService) repository).findDefaultStatus(project);
		return status;
	}

	@Override
	protected Class<CActivityStatus> getEntityClass() { return CActivityStatus.class; }

	/** Initializes a new activity status with default values. Most common fields are initialized by super class.
	 * @param entity the newly created activity status to initialize */
	@Override
	public void initializeNewEntity(final CActivityStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Activity Status");
	}
}
