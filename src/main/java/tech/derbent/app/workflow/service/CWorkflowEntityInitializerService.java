package tech.derbent.app.workflow.service;

import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.roles.service.CUserProjectRoleService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;

/** CWorkflowEntityInitializerService - Initializer service for workflow entity screens. Layer: Service (MVC) Provides screen and grid configurations
 * for workflow entity management. */
public class CWorkflowEntityInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CWorkflowEntity.class;
	public static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowEntityInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".21";
	private static final String menuTitle = MenuTitle_PROJECT + ".Workflows";
	private static final String pageDescription = "Workflow management for projects";
	private static final String pageTitle = "Workflow Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "statusRelations"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating workflow entity view", e);
			throw e;
		}
	}

	/** Helper function to create a workflow transition (flow) from one status to another. This simplifies workflow initialization by providing a
	 * clean API for defining transitions. The helper finds statuses by name and creates the transition with specified roles.
	 * @param workflow                      The workflow entity to add the transition to
	 * @param startStatusName               Name of the source status (e.g., "In Progress", "New")
	 * @param destinationStatusName         Name of the target status (e.g., "Done", "Canceled")
	 * @param statuses                      List of all available statuses to search from
	 * @param roles                         List of roles that can perform this transition (empty list = no roles)
	 * @param isInitialStatus               Whether the start status should be marked as initial (default: false)
	 * @param workflowStatusRelationService Service for saving the transition
	 * @throws IllegalArgumentException if status names are not found */
	private static void createFlowFromTo(final CWorkflowEntity workflow, final String startStatusName, final String destinationStatusName,
			final List<CProjectItemStatus> statuses, final List<CUserProjectRole> roles, final boolean isInitialStatus,
			final tech.derbent.app.workflow.service.CWorkflowStatusRelationService workflowStatusRelationService) {
		Check.notNull(workflow, "Workflow cannot be null");
		Check.notBlank(startStatusName, "Start status name cannot be blank");
		Check.notBlank(destinationStatusName, "Destination status name cannot be blank");
		Check.notNull(statuses, "Statuses list cannot be null");
		Check.notNull(roles, "Roles list cannot be null");
		// Find statuses by name
		final CProjectItemStatus startStatus = statuses.stream().filter(s -> s != null && startStatusName.equalsIgnoreCase(s.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Status not found: " + startStatusName));
		final CProjectItemStatus destinationStatus = statuses.stream().filter(s -> s != null && destinationStatusName.equalsIgnoreCase(s.getName()))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Status not found: " + destinationStatusName));
		// Create the transition
		final CWorkflowStatusRelation relation = new CWorkflowStatusRelation();
		relation.setWorkflowEntity(workflow);
		relation.setFromStatus(startStatus);
		relation.setToStatus(destinationStatus);
		relation.setInitialStatus(isInitialStatus);
		// Add roles if provided
		if (roles != null && !roles.isEmpty()) {
			relation.getRoles().addAll(roles);
		}
		workflowStatusRelationService.save(relation);
		// LOGGER.debug("Created workflow transition: {} -> {} (initial: {})", startStatusName, destinationStatusName, isInitialStatus);
	}

	/** Helper overload without isInitialStatus parameter (defaults to false). */
	private static void createFlowFromTo(final CWorkflowEntity workflow, final String startStatusName, final String destinationStatusName,
			final List<CProjectItemStatus> statuses, final List<CUserProjectRole> roles,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		createFlowFromTo(workflow, startStatusName, destinationStatusName, statuses, roles, false, workflowStatusRelationService);
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "project", "isActive"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/** Initializes a single workflow with complete status transitions including cancel/done/restart paths. Creates a workflow entity with status
	 * relations that include: - Forward transitions (progress through workflow) - Backward transitions (return to previous state) - Cancel transition
	 * (from any intermediate state to final state) - Done/Complete transition (from last state back to initial state) - Restart transition (from
	 * final state back to initial state - allows restarting canceled/completed workflows) This ensures complete workflow coverage for all common
	 * scenarios including the ability to restart workflows that have been canceled or completed.
	 * @param name                          Workflow name
	 * @param project                       Project for the workflow
	 * @param statuses                      Available project item statuses
	 * @param roles                         User project roles for transition permissions
	 * @param workflowEntityService         Service for saving workflow entities
	 * @param workflowStatusRelationService Service for saving status relations */
	public static void initializeSampleWorkflow(final String name, final CProject project, final List<CProjectItemStatus> statuses,
			final List<CUserProjectRole> roles, final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		Check.notNull(name, "Workflow name cannot be null");
		Check.notNull(project, "Project cannot be null");
		Check.notNull(statuses, "Statuses list cannot be null");
		Check.notNull(roles, "Roles list cannot be null");
		Check.notEmpty(statuses, "Statuses list cannot be empty");
		Check.notEmpty(roles, "Roles list cannot be empty");
		final List<CProjectItemStatus> filteredStatuses =
				statuses.stream().filter(status -> status != null).peek(status -> Check.isSameCompany(project, status))
						.sorted(Comparator.comparing(CProjectItemStatus::getSortOrder, Comparator.nullsLast(Integer::compareTo))
								.thenComparing(CProjectItemStatus::getId, Comparator.nullsLast(Long::compareTo)))
						.toList();
		Check.notEmpty(filteredStatuses, "No statuses available for workflow " + name + " in project " + project.getName());
		final List<CUserProjectRole> filteredRoles = roles.stream().filter(role -> role != null && role.getProject() != null
				&& role.getProject().getId() != null && role.getProject().getId().equals(project.getId())).toList();
		Check.notEmpty(filteredRoles, "No roles available for workflow " + name + " in project " + project.getName());
		final CWorkflowEntity workflow = new CWorkflowEntity(name, project);
		workflow.setDescription("Defines status transitions for " + name + " based on user roles");
		workflow.setIsActive(true);
		workflowEntityService.save(workflow);
		// Use helper function to create clean workflow transitions
		// Assuming standard status names: first status is initial, last is final/done/canceled
		if (filteredStatuses.size() >= 2) {
			final String initialStatus = filteredStatuses.get(0).getName();
			final String finalStatus = filteredStatuses.get(filteredStatuses.size() - 1).getName();
			// Forward transitions (progress through workflow) - mark first as initial
			for (int i = 0; i < Math.min(filteredStatuses.size() - 1, 3); i++) {
				final String fromStatus = filteredStatuses.get(i).getName();
				final String toStatus = filteredStatuses.get(i + 1).getName();
				final boolean isInitial = i == 0;
				// Forward transition with first role
				final List<CUserProjectRole> forwardRoles = filteredRoles.isEmpty() ? List.of() : List.of(filteredRoles.get(0));
				createFlowFromTo(workflow, fromStatus, toStatus, filteredStatuses, forwardRoles, isInitial, workflowStatusRelationService);
				// Backward transitions (except from first status)
				if (i > 0) {
					final List<CUserProjectRole> backwardRoles = filteredRoles.size() > 1 ? List.of(filteredRoles.get(1)) : filteredRoles;
					createFlowFromTo(workflow, toStatus, fromStatus, filteredStatuses, backwardRoles, workflowStatusRelationService);
				}
			}
			// Done transition: from final status back to initial (complete cycle)
			createFlowFromTo(workflow, finalStatus, initialStatus, filteredStatuses, filteredRoles, workflowStatusRelationService);
			// Cancel transitions: from all intermediate statuses to final status
			for (int i = 1; i < filteredStatuses.size() - 1; i++) {
				final String intermediateStatus = filteredStatuses.get(i).getName();
				createFlowFromTo(workflow, intermediateStatus, finalStatus, filteredStatuses, filteredRoles, workflowStatusRelationService);
			}
			// Restart transition: from final status back to initial (allows restarting canceled/completed workflows)
			// Note: This creates a duplicate of the "done" transition, but that's OK for clarity
			// In practice, the done transition above already handles this case
		}
		// LOGGER.debug("Created workflow '{}' with complete cancel/done/restart transitions for project: {}", name, project.getName());
	}

	/** Initialize sample workflow entities to demonstrate workflow management. Creates multiple workflow entities with complete status transitions
	 * including: - Activity Status Workflow - Decision Status Workflow - Meeting Status Workflow - Risk Status Workflow - Project Status Workflow
	 * Each workflow includes forward, backward, cancel, done, and restart transitions. The restart transition allows items in canceled or completed
	 * states to be returned to the initial state to restart the workflow.
	 * @param project                       The project to create workflow entities for
	 * @param minimal                       Whether to create minimal sample data
	 * @param projectItemStatusService      Service for loading statuses
	 * @param userProjectRoleService        Service for loading roles
	 * @param workflowEntityService         Service for saving workflow entities
	 * @param workflowStatusRelationService Service for saving status relations */
	public static void initializeSampleWorkflowEntities(final CProject project, final boolean minimal,
			final CProjectItemStatusService projectItemStatusService, final CUserProjectRoleService userProjectRoleService,
			final CWorkflowEntityService workflowEntityService, final CWorkflowStatusRelationService workflowStatusRelationService) {
		try {
			// Get available statuses for this project
			final List<CProjectItemStatus> statuses = projectItemStatusService.listByCompany(project.getCompany()).stream()
					.sorted(Comparator.comparing(CProjectItemStatus::getSortOrder, Comparator.nullsLast(Integer::compareTo))
							.thenComparing(CProjectItemStatus::getId, Comparator.nullsLast(Long::compareTo)))
					.toList();
			Check.notEmpty(statuses, "No project item statuses found for project: " + project.getName());
			final List<CUserProjectRole> roles = userProjectRoleService.findByProject(project).stream()
					.filter(role -> role.getProject() != null && project.getId().equals(role.getProject().getId())).toList();
			Check.notEmpty(roles, "No user project roles found for project: " + project.getName());
			initializeSampleWorkflow("Activity Status Workflow", project, statuses, roles, workflowEntityService, workflowStatusRelationService);
			initializeSampleWorkflow("Decision Status Workflow", project, statuses, roles, workflowEntityService, workflowStatusRelationService);
			initializeSampleWorkflow("Meeting Status Workflow", project, statuses, roles, workflowEntityService, workflowStatusRelationService);
			initializeSampleWorkflow("Risk Status Workflow", project, statuses, roles, workflowEntityService, workflowStatusRelationService);
			initializeSampleWorkflow("Project Status Workflow", project, statuses, roles, workflowEntityService, workflowStatusRelationService);
			LOGGER.debug("Created sample workflow entities with complete cancel/done/restart transitions for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample workflow entities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample workflow entities for project: " + project.getName(), e);
		}
	}
}
