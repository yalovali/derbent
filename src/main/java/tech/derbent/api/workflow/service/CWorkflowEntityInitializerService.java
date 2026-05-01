package tech.derbent.api.workflow.service;

import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;

/** CWorkflowEntityInitializerService - Initializer service for workflow entity screens. Layer: Service (MVC) Provides screen and grid configurations
 * for workflow entity management. */
public class CWorkflowEntityInitializerService extends CWorkflowBaseInitializationService {

	private static final String BAB_WORKFLOW_DESCRIPTION = "Minimal workflow for BAB project types";
	private static final String BAB_WORKFLOW_NAME = "BAB Gateway Workflow";
	private static final Class<?> clazz = CWorkflowEntity.class;
	public static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowEntityInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".21";
	private static final String menuTitle = MenuTitle_PROJECT + ".Workflows";
	private static final String pageDescription = "Workflow management for companies";
	private static final String pageTitle = "Workflow Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CEntityNamedInitializerService.createScreenLines(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "statusRelations"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating workflow entity view reason={}", e.getMessage());
			throw e;
		}
	}

	/** Helper function to create a workflow transition (flow) from one status to another. This simplifies workflow initialization by providing a clean
	 * API for defining transitions. The helper finds statuses by name and creates the transition with specified roles.
	 * @param workflow                      The workflow entity to add the transition to
	 * @param startStatusName               Name of the source status (e.g., "In Progress", "New")
	 * @param destinationStatusName         Name of the target status (e.g., "Done", "Canceled")
	 * @param statuses                      List of all available statuses to search from
	 * @param roles                         List of roles that can perform this transition (empty list = no roles)
	 * @param isInitialStatus               Whether the start status should be marked as initial (default: false)
	 * @param workflowStatusRelationService Service for saving the transition
	 * @throws IllegalArgumentException if status names are not found */
	private static void createFlowFromTo(final CWorkflowEntity workflow, final String startStatusName,
			final String destinationStatusName, final List<CProjectItemStatus> statuses,
			final List<CUserProjectRole> roles, final boolean isInitialStatus,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		Check.notNull(workflow, "Workflow cannot be null");
		Check.notBlank(startStatusName, "Start status name cannot be blank");
		Check.notBlank(destinationStatusName, "Destination status name cannot be blank");
		Check.notNull(statuses, "Statuses list cannot be null");
		Check.notNull(roles, "Roles list cannot be null");
		// Find statuses by name
		final CProjectItemStatus startStatus =
				statuses.stream().filter(s -> s != null && startStatusName.equalsIgnoreCase(s.getName())).findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Status not found: " + startStatusName));
		final CProjectItemStatus destinationStatus = statuses.stream()
				.filter(s -> s != null && destinationStatusName.equalsIgnoreCase(s.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Status not found: " + destinationStatusName));
		// Create the transition
		final CWorkflowStatusRelation relation = new CWorkflowStatusRelation(true);
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
	private static void createFlowFromTo(final CWorkflowEntity workflow, final String startStatusName,
			final String destinationStatusName, final List<CProjectItemStatus> statuses,
			final List<CUserProjectRole> roles, final CWorkflowStatusRelationService workflowStatusRelationService) {
		createFlowFromTo(workflow, startStatusName, destinationStatusName, statuses, roles, false,
				workflowStatusRelationService);
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "company", "active"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static CWorkflowEntity initializeSampleBab(final CCompany company, final boolean minimal) throws Exception {
		final String[][] seeds = {
				{
						BAB_WORKFLOW_NAME, BAB_WORKFLOW_DESCRIPTION
				}
		};
		final CWorkflowEntityService service = CSpringContext.getBean(CWorkflowEntityService.class);
		initializeCompanyEntity(seeds, service, company, minimal, null);
		return service.listByCompany(company).stream().filter(workflow -> BAB_WORKFLOW_NAME.equals(workflow.getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("BAB workflow not found after initialization"));
	}

	/** Creates the canonical Agile Item Workflow used by Epic, Feature, UserStory and Activity types.
	 * <p>
	 * Standard Kanban/Scrum agile column flow:
	 * <pre>
	 *   To Do → In Progress → In Review → Done
	 *                ↕              ↕
	 *             Blocked      In Progress (back)
	 *                ↓
	 *           Cancelled (from any active state)
	 *           Done/Cancelled → To Do (restart/re-open)
	 * </pre>
	 * The initial status is "To Do" so that newly created items land in the first Kanban column ("To Do"),
	 * which is the standard starting point when sprint planning populates the backlog.
	 * <p>
	 * Pattern note: the initial status is encoded on a transition whose {@code isInitialStatus=true} flag is set,
	 * and the framework reads {@code relation.getToStatus()} to determine what status to assign on item creation.
	 * We reuse the "Done → To Do" restart transition as the initial-status carrier so we don't need a
	 * dummy placeholder transition (same technique as the Sprint Workflow using "Started → Planning"). */
	private static void initializeSampleAgileItemWorkflow(final CCompany company, final List<CProjectItemStatus> statuses,
			final List<CUserProjectRole> roles, final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		final String workflowName = "Agile Item Workflow";
		if (workflowEntityService.findByNameAndCompany(workflowName, company).isPresent()) {
			return;
		}
		final List<String> requiredStatusNames = List.of("To Do", "In Progress", "In Review", "Blocked", "Done",
				"Cancelled");
		final boolean allPresent = requiredStatusNames.stream().allMatch(name -> statuses.stream()
				.anyMatch(status -> status != null && name.equalsIgnoreCase(status.getName())));
		if (!allPresent) {
			LOGGER.warn(
					"Skipping agile item workflow initialization for company '{}' because required statuses are missing: {}",
					company.getName(), requiredStatusNames);
			return;
		}
		final CWorkflowEntity workflow = new CWorkflowEntity(workflowName, company);
		workflow.setDescription(
				"Standard agile Kanban workflow: To Do → In Progress → In Review → Done. Used by Epic, Feature, UserStory and Activity types.");
		workflowEntityService.save(workflow);
		// "Done → To Do" is marked isInitialStatus=true so the framework assigns "To Do" to brand-new items
		// (those with no current status). The same transition also serves as the re-open/restart path.
		createFlowFromTo(workflow, "Done", "To Do", statuses, roles, true, workflowStatusRelationService);
		// Forward progress transitions (the happy path through Kanban columns)
		createFlowFromTo(workflow, "To Do", "In Progress", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "In Progress", "In Review", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "In Review", "Done", statuses, roles, workflowStatusRelationService);
		// Backward transitions — move an item earlier in the flow without cancelling it
		createFlowFromTo(workflow, "In Progress", "To Do", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "In Review", "In Progress", statuses, roles, workflowStatusRelationService);
		// Blocking flow — item is stalled but not cancelled
		createFlowFromTo(workflow, "In Progress", "Blocked", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "Blocked", "In Progress", statuses, roles, workflowStatusRelationService);
		// Unblock back to start — required so a blocked item can be dragged to the "To Do" kanban column
		createFlowFromTo(workflow, "Blocked", "To Do", statuses, roles, workflowStatusRelationService);
		// Cancellation — allowed from any active state
		createFlowFromTo(workflow, "To Do", "Cancelled", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "In Progress", "Cancelled", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "In Review", "Cancelled", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "Blocked", "Cancelled", statuses, roles, workflowStatusRelationService);
		// Re-open a cancelled item back to the backlog
		createFlowFromTo(workflow, "Cancelled", "To Do", statuses, roles, workflowStatusRelationService);
	}

	/** Creates the Sprint Workflow used by Sprint types.
	 * <p>
	 * Standard Scrum sprint lifecycle:
	 * <pre>
	 *   Planning → Started → Done
	 *                   ↓
	 *               Canceled
	 *   Done/Canceled → Planning (restart)
	 * </pre>
	 * The initial status is "Planning" (sprints begin in planning, not yet active).
	 * Pattern: "Started → Planning" with {@code isInitialStatus=true} marks "Planning" as the initial status
	 * while also providing the sprint-restart transition. */
	private static void initializeSampleSprintWorkflow(final CCompany company, final List<CProjectItemStatus> statuses,
			final List<CUserProjectRole> roles, final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		final String workflowName = "Sprint Workflow";
		if (workflowEntityService.findByNameAndCompany(workflowName, company).isPresent()) {
			return;
		}
		final List<String> requiredStatusNames = List.of("Planning", "Started", "Done", "Canceled");
		final boolean allPresent = requiredStatusNames.stream().allMatch(name -> statuses.stream()
				.anyMatch(status -> status != null && name.equalsIgnoreCase(status.getName())));
		if (!allPresent) {
			LOGGER.warn(
					"Skipping sprint workflow initialization for company '{}' because required statuses are missing: {}",
					company.getName(), requiredStatusNames);
			return;
		}
		final CWorkflowEntity workflow = new CWorkflowEntity(workflowName, company);
		workflow.setDescription("Sprint lifecycle workflow: Planning → Started → Done / Canceled");
		workflowEntityService.save(workflow);
		// "Started → Planning" marked isInitialStatus=true so new sprints start in "Planning" (sprint backlog preparation).
		// This transition also serves as the sprint-restart path after Done or Canceled.
		createFlowFromTo(workflow, "Started", "Planning", statuses, roles, true, workflowStatusRelationService);
		// Forward: sprint backlog is ready, team begins executing
		createFlowFromTo(workflow, "Planning", "Started", statuses, roles, workflowStatusRelationService);
		// Sprint can be cancelled while still in planning (e.g. scope change)
		createFlowFromTo(workflow, "Planning", "Canceled", statuses, roles, workflowStatusRelationService);
		// Sprint completes at end of timebox
		createFlowFromTo(workflow, "Started", "Done", statuses, roles, workflowStatusRelationService);
		// Sprint is interrupted/abandoned mid-execution
		createFlowFromTo(workflow, "Started", "Canceled", statuses, roles, workflowStatusRelationService);
		// Allow restarting a sprint after completion or cancellation (useful for demo data and corrections)
		createFlowFromTo(workflow, "Done", "Planning", statuses, roles, workflowStatusRelationService);
		createFlowFromTo(workflow, "Canceled", "Planning", statuses, roles, workflowStatusRelationService);
	}

	/** Initializes a single workflow with complete status transitions including cancel/done/restart paths. Creates a workflow entity with status
	 * relations that include: - Forward transitions (progress through workflow) - Backward transitions (return to previous state) - Cancel transition
	 * (from any intermediate state to final state) - Done/Complete transition (from last state back to initial state) - Restart transition (from final
	 * state back to initial state - allows restarting canceled/completed workflows) This ensures complete workflow coverage for all common scenarios
	 * including the ability to restart workflows that have been canceled or completed.
	 * @param name                          Workflow name
	 * @param company                       Company for the workflow
	 * @param statuses                      Available project item statuses
	 * @param roles                         User project roles for transition permissions
	 * @param workflowEntityService         Service for saving workflow entities
	 * @param workflowStatusRelationService Service for saving status relations */
	public static void initializeSampleWorkflow(final String name, final CCompany company,
			final List<CProjectItemStatus> statuses, final List<CUserProjectRole> roles,
			final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		Check.notNull(name, "Workflow name cannot be null");
		Check.notNull(company, "Company cannot be null");
		Check.notNull(statuses, "Statuses list cannot be null");
		Check.notNull(roles, "Roles list cannot be null");
		Check.notEmpty(statuses, "Statuses list cannot be empty");
		Check.notEmpty(roles, "Roles list cannot be empty");
		if (workflowEntityService.findByNameAndCompany(name, company).isPresent()) {
			LOGGER.debug("Skipping sample workflow '{}' because it already exists for company: {}", name,
					company.getName());
			return;
		}
		final List<CProjectItemStatus> filteredStatuses = statuses.stream()
				.filter(status -> status != null && status.getCompany() != null && status.getCompany().getId() != null
						&& status.getCompany().getId().equals(company.getId()))
				.sorted(Comparator.comparing(CProjectItemStatus::getSortOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(CProjectItemStatus::getId, Comparator.nullsLast(Long::compareTo)))
				.toList();
		Check.notEmpty(filteredStatuses,
				"No statuses available for workflow " + name + " in company " + company.getName());
		final List<CUserProjectRole> filteredRoles = roles.stream()
				.filter(role -> role != null && role.getCompany() != null && role.getCompany().getId() != null
						&& role.getCompany().getId().equals(company.getId()))
				.toList();
		Check.notEmpty(filteredRoles, "No roles available for workflow " + name + " in company " + company.getName());
		final CWorkflowEntity workflow = new CWorkflowEntity(name, company);
		workflow.setDescription("Defines status transitions for " + name + " based on user roles");
		workflowEntityService.save(workflow);
		// Use helper function to create clean workflow transitions
		// Assuming standard status names: first status is initial, last is final/done/canceled
		// LOGGER.debug("Created workflow '{}' with complete cancel/done/restart transitions for company: {}", name, company.getName());
		if (filteredStatuses.size() < 2) {
			return;
		}
		final String initialStatus = filteredStatuses.get(0).getName();
		final String finalStatus = filteredStatuses.get(filteredStatuses.size() - 1).getName();
		// Forward transitions (progress through workflow) - mark first as initial
		for (int i = 0; i < Math.min(filteredStatuses.size() - 1, 3); i++) {
			final String fromStatus = filteredStatuses.get(i).getName();
			final String toStatus = filteredStatuses.get(i + 1).getName();
			final boolean isInitial = i == 0;
			// Forward transition with first role
			final List<CUserProjectRole> forwardRoles =
					filteredRoles.isEmpty() ? List.of() : List.of(filteredRoles.get(0));
			createFlowFromTo(workflow, fromStatus, toStatus, filteredStatuses, forwardRoles, isInitial,
					workflowStatusRelationService);
			// Backward transitions (except from first status)
			if (i > 0) {
				final List<CUserProjectRole> backwardRoles =
						filteredRoles.size() > 1 ? List.of(filteredRoles.get(1)) : filteredRoles;
				createFlowFromTo(workflow, toStatus, fromStatus, filteredStatuses, backwardRoles,
						workflowStatusRelationService);
			}
		}
		// Done transition: from final status back to initial (complete cycle)
		createFlowFromTo(workflow, finalStatus, initialStatus, filteredStatuses, filteredRoles,
				workflowStatusRelationService);
		// Cancel transitions: from all intermediate statuses to final status
		for (int i = 1; i < filteredStatuses.size() - 1; i++) {
			final String intermediateStatus = filteredStatuses.get(i).getName();
			createFlowFromTo(workflow, intermediateStatus, finalStatus, filteredStatuses, filteredRoles,
					workflowStatusRelationService);
		}
		// Restart transition: from final status back to initial (allows restarting canceled/completed workflows)
		// Note: This creates a duplicate of the "done" transition, but that's OK for clarity
		// In practice, the done transition above already handles this case
	}

	/** Initialize sample workflow entities to demonstrate workflow management. Creates multiple workflow entities with complete status transitions
	 * including: - Activity Status Workflow - Decision Status Workflow - Meeting Status Workflow - Risk Status Workflow - Project Status Workflow Each
	 * workflow includes forward, backward, cancel, done, and restart transitions. The restart transition allows items in canceled or completed states to
	 * be returned to the initial state to restart the workflow.
	 * @param company                       The company context for role filtering
	 * @param minimal                       Whether to create minimal sample data
	 * @param statusService                 Service for loading statuses
	 * @param userProjectRoleService        Service for loading roles
	 * @param workflowEntityService         Service for saving workflow entities
	 * @param workflowStatusRelationService Service for saving status relations */
	public static void initializeSampleWorkflowEntities(final CCompany company, final boolean minimal,
			final CProjectItemStatusService statusService, final CUserProjectRoleService userProjectRoleService,
			final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService workflowStatusRelationService) {
		try {
			Check.notNull(company, "Company cannot be null");
			// Get available statuses for this company
			final List<CProjectItemStatus> statuses = statusService.listByCompany(company).stream()
					.sorted(Comparator
							.comparing(CProjectItemStatus::getSortOrder, Comparator.nullsLast(Integer::compareTo))
							.thenComparing(CProjectItemStatus::getId, Comparator.nullsLast(Long::compareTo)))
					.toList();
			Check.notEmpty(statuses, "No project item statuses found for company: " + company.getName());
			final List<CUserProjectRole> roles = userProjectRoleService.listByCompany(company);
			Check.notEmpty(roles, "No user project roles found for company: " + company.getName());
			initializeSampleWorkflow("Activity Status Workflow", company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
			initializeSampleWorkflow("Decision Status Workflow", company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
			initializeSampleWorkflow("Meeting Status Workflow", company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
			initializeSampleWorkflow("Risk Status Workflow", company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
			initializeSampleWorkflow("Project Status Workflow", company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
			// Agile item workflow: used by Epic, Feature, UserStory, and Activity types.
			// Initial status is "To Do" so new items land in the first Kanban column when sprint planning populates the backlog.
			initializeSampleAgileItemWorkflow(company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
			// Sprint-specific workflow: Planning → Started → Done/Canceled (assigned only to Sprint types, not to backlog items).
			initializeSampleSprintWorkflow(company, statuses, roles, workflowEntityService,
					workflowStatusRelationService);
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample workflow entities for company: {} reason={}", company.getName(),
					e.getMessage());
			throw new RuntimeException(
					"Failed to initialize sample workflow entities for company: " + company.getName(), e);
		}
	}
}
