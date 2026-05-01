package tech.derbent.plm.activities.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.parentrelation.service.CParentRelationInitializerService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.domain.CActivityType;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.CUserStoryService;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.service.CDecisionService;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.service.CLinkInitializerService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.service.CMeetingService;

public class CActivityInitializerService extends CProjectItemInitializerService {

	static final Class<?> clazz = CActivity.class;
	static Map<String, EntityFieldInfo> fields;
	static EntityFieldInfo info;
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".2";
	private static final String menuTitle = MenuTitle_PROJECT + ".Activities";
	private static final String pageDescription = "Activity management for projects";
	private static final String pageTitle = "Activity Management";
	private static final boolean showInQuickToolbar = true;

	/** Add relationships (comments, attachments, links) to sample activities. */
	private static void _addRelationshipsToActivities(final List<CActivity> activities, final CProject<?> project) {
		try {
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			// Add comments to first activity
			if (activities.size() > 0) {
				final CActivity activity1 = activities.get(0);
				final List<CComment> comments = CCommentInitializerService.createSampleComments(new String[] {
						"Started implementation of login UI components",
						"Need to review accessibility requirements for form fields"
				}, new boolean[] {
						false, true
				} // Second comment is important
				);
				activity1.getComments().addAll(comments);
				activityService.save(activity1);
			}
			// Add attachments to second activity
			if (activities.size() > 1) {
				final CActivity activity2 = activities.get(1);
				final List<CAttachment> attachments =
						CAttachmentInitializerService.createSampleAttachments(new String[][] {
								{
										"API_Design_Spec.pdf", "API design specification for authentication endpoints",
										"245760"
								}, {
										"Auth_Sequence_Diagram.png", "UML sequence diagram for authentication flow",
										"89340"
								}
						}, project.getCompany());
				activity2.getAttachments().addAll(attachments);
				activityService.save(activity2);
			}
			// Add links to random related entities
			if (activities.size() > 0) {
				final CActivity activity = activities.get(0);
				// Link to random meeting
				final CLink linkToMeeting = CLinkInitializerService.createRandomLink(activity, project, CMeeting.class,
						CMeetingService.class, "Discussed In", "Activity discussed in planning meeting",
						project.getCompany());
				if (linkToMeeting != null) {
					activity.getLinks().add(linkToMeeting);
				}
				// Link to random decision
				final CLink linkToDecision = CLinkInitializerService.createRandomLink(activity, project,
						CDecision.class, CDecisionService.class, "Implements", "Activity implements strategic decision",
						project.getCompany());
				if (linkToDecision != null) {
					activity.getLinks().add(linkToDecision);
				}
				activityService.save(activity);
			}
			LOGGER.info("Added relationships (comments, attachments, links) to {} activities", activities.size());
		} catch (final Exception e) {
			LOGGER.warn("Error adding relationships to activities. reason={}", e.getMessage());
			// Don't fail the whole initialization if relationships fail
		}
	}

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CProjectItemInitializerService.createScreenLines(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "acceptanceCriteria"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "completionDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "progressPercentage"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
			/******************/
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			/******************/
			// Links section - standard section for entities that can be linked
			CLinkInitializerService.addDefaultSection(scr, clazz);
			/******************/
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
			/******************/
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notes"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "results"));
			CParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating activity view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "componentWidget", "entityType", "assignedTo", "createdBy",
				"startDate", "dueDate", "completionDate", "progressPercentage", "estimatedHours", "actualHours",
				"remainingHours", "status", "priority", "project", "createdDate", "lastModifiedDate"));
		grid.setEditableColumnFields(List.of("name", "assignedTo", "startDate", "dueDate", "progressPercentage",
				"estimatedHours", "actualHours", "remainingHours", "status", "priority"));
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

	private static CProjectItemStatus findStatusByName(final List<CProjectItemStatus> statuses, final String name) {
		return statuses.stream().filter(s -> s != null && name.equalsIgnoreCase(s.getName())).findFirst().orElse(null);
	}

	/** Initialize sample activities for a project with relationships (comments, attachments, links).
	 * @param project          the project to create activities for
	 * @param minimal          if true, creates only 6 activities; if false, creates all 25 activities
	 * @param sampleUserStory1 the first user story to link activities to (can be null)
	 * @param sampleUserStory2 the second user story to link third activity to (can be null) */
	public static void initializeSample(final CProject<?> project, final boolean minimal,
			final CUserStory sampleUserStory1, final CUserStory sampleUserStory2) throws Exception {
		record ActivitySeed(String name, String description, String acceptanceCriteria, String notes,
				int parentUserStoryIndex, int startOffsetDays, int durationDays, int storyPoints, int estimatedHours,
				int actualHours, int progressPercentage) {}
		final List<ActivitySeed> seeds = List.of(new ActivitySeed("Design MFA Enrollment Flow",
				"Prepare the account and workspace admin enrollment flow for MFA, including recovery code handoff.",
				"Wireframes, validation rules, and API contract are reviewed with security and UX.",
				"Depends on identity rollout decisions and usability review feedback.", 0, 25, 5, 3, 12, 8, 65),
				new ActivitySeed("Implement TOTP Enrollment API",
						"Build backend endpoints for TOTP setup, challenge verification, and recovery code generation.",
						"Enrollment endpoint stores device metadata and produces one-time recovery codes after verification.",
						"Backend slice for first sprint delivery of the identity epic.", 0, 24, 7, 5, 20, 11, 55),
				new ActivitySeed("Add Suspicious Session Revocation",
						"Allow security analysts to revoke a suspicious session directly from the audit review screen.",
						"Revocation updates session state immediately and emits an audit event with the acting administrator.",
						"Cross-team work shared between security operations and platform engineering.", 1, 22, 4, 2, 9,
						5, 70),
				new ActivitySeed("Build Billing Contact Editor",
						"Create the customer-facing form for updating billing contacts and notification recipients.",
						"Billing contacts can be edited, validated, and persisted without page refresh.",
						"Part of the customer workspace self-service rollout.", 2, 16, 6, 3, 14, 6, 40),
				new ActivitySeed("Persist Saved Workspace Filters",
						"Store named filter sets and expose them in the dashboard widget picker.",
						"Saved filters can be created, renamed, pinned, and reused across sessions.",
						"Provides visible backlog value for workspace-heavy customer admins.", 3, 13, 5, 5, 16, 7, 35),
				new ActivitySeed("Create Dispute SLA Timeline",
						"Render SLA milestones and owner transitions for invoice dispute triage workflows.",
						"SLA dates and ownership changes are visible in the case timeline with audit traceability.",
						"Finance operations requested this for executive escalation reporting.", 4, 10, 6, 5, 18, 6,
						28),
				new ActivitySeed("Wire Release Command Center Widgets",
						"Surface launch blockers, owners, and environment readiness indicators in the release hub.",
						"Release dashboard widgets summarize blockers, pending approvals, and rollout checkpoints.",
						"Supports later sprint planning while still leaving future backlog depth.", 5, 6, 7, 3, 10, 2,
						12),
				new ActivitySeed("Publish Dispute Evidence Upload Service",
						"Add attachment and evidence metadata support for invoice dispute handling.",
						"Support leads can upload evidence and link it to dispute actions with ownership metadata.",
						"Intentionally not sprint-assigned in the initial sample to keep backlog visible.", 6, 3, 6, 3,
						12, 0, 0),
				new ActivitySeed("Automate Release Gate Validation",
						"Implement release checklist validation so missing gates block go-live approval automatically.",
						"Validation highlights incomplete gates and records who waived or resolved each blocker.",
						"Future backlog item for release hardening work.", 7, 1, 8, 5, 18, 0, 0),
				new ActivitySeed("Draft Go-Live Runbook Updates",
						"Refresh incident, rollback, and communication runbooks for the release program.",
						"Runbooks include rollback owner, communication template, and on-call handoff instructions.",
						"Documentation-heavy backlog item that complements the release readiness epic.", 5, 0, 5, 2, 8,
						0, 0),
				new ActivitySeed("Implement MFA Recovery Code Export",
						"Allow users to export backup recovery codes in a secure, one-time download.",
						"Recovery codes are downloadable as a text file after re-authentication, and prior codes are invalidated.",
						"Security requirement tied to the MFA enrollment epic.", 0, 20, 6, 3, 12, 9, 80),
				new ActivitySeed("Add Session Activity Log Panel",
						"Show a chronological activity log for each active session, including login source and actions.",
						"Session log is accessible from account security settings with pagination and date filter.",
						"Needed for audit and SOC2 compliance controls.", 1, 18, 7, 4, 16, 10, 62),
				new ActivitySeed("Build Workspace Role Assignment UI",
						"Create the interface for assigning and revoking workspace roles for users and teams.",
						"Roles can be assigned, changed, and revoked with confirmation and audit trail.",
						"Prerequisite for the access modernization epic.", 0, 15, 8, 5, 20, 8, 45),
				new ActivitySeed("Implement Payment Method Update Form",
						"Allow customers to update their payment method directly from the billing workspace.",
						"Payment method form validates card details, previews changes, and updates on save.",
						"High-priority customer request for self-service billing management.", 2, 12, 6, 4, 14, 5, 38),
				new ActivitySeed("Create Invoice Dispute API Endpoints",
						"Build the REST endpoints for creating, updating, and resolving invoice disputes.",
						"Endpoints support dispute CRUD, SLA tracking, and evidence attachment.",
						"Backend foundation for the billing operations epic.", 4, 10, 9, 5, 22, 7, 30),
				new ActivitySeed("Wire Dashboard Activity Feed Widget",
						"Add a real-time activity feed widget to the main dashboard showing recent project events.",
						"Feed updates in real time, shows action type, actor, and timestamp for the last 50 events.",
						"Requested by project managers for status overview.", 3, 9, 7, 4, 18, 4, 22),
				new ActivitySeed("Implement Sprint Burndown Chart",
						"Render a burndown chart for each sprint showing remaining story points over time.",
						"Chart updates daily, includes trend line, and is visible from the sprint planning view.",
						"Key visibility tool for scrum masters and release managers.", 5, 8, 8, 5, 20, 3, 15),
				new ActivitySeed("Create Team Capacity Planner",
						"Build a capacity planning tool that shows developer availability across sprints.",
						"Planner shows available hours per sprint per team member and flags over-allocation.",
						"Long-requested feature for sprint planning accuracy.", 3, 6, 10, 5, 24, 2, 8),
				new ActivitySeed("Build API Rate Limit Dashboard",
						"Show real-time API usage, rate limit consumption, and throttling events per tenant.",
						"Dashboard displays current usage vs. limits and alerts on approaching thresholds.",
						"Required for enterprise tenants with strict API SLA agreements.", 5, 5, 8, 4, 18, 0, 5),
				new ActivitySeed("Implement Webhook Event Delivery Retries",
						"Add automatic retry logic with exponential backoff for failed webhook deliveries.",
						"Failed deliveries are retried up to 5 times with full delivery log visible to customers.",
						"Reliability improvement for customers using event-driven integrations.", 5, 3, 9, 5, 20, 0, 0),
				new ActivitySeed("Add Tenant Isolation Audit Report",
						"Generate a periodic report confirming data isolation between tenants.",
						"Report covers storage, API access, and search index boundaries across all tenant contexts.",
						"Compliance requirement for multi-tenant SaaS certification.", 1, 2, 7, 3, 16, 0, 0),
				new ActivitySeed("Create Release Rollback Orchestrator",
						"Implement automated rollback logic that triggers on critical health-check failure post-deploy.",
						"Rollback completes within 5 minutes, notifies on-call channel, and records a rollback event.",
						"Critical safety net for zero-downtime deploy strategy.", 5, 1, 10, 5, 22, 0, 0),
				new ActivitySeed("Write Integration Test Suite for Auth Flow",
						"Build an automated integration test suite covering the full MFA login and session lifecycle.",
						"Suite covers enrollment, login with MFA, session revocation, and re-authentication.",
						"Test infrastructure investment for the identity modernization epic.", 0, 0, 12, 5, 28, 0, 0),
				new ActivitySeed("Document API Contract for Billing Endpoints",
						"Write OpenAPI specs and developer guide for all billing dispute and payment endpoints.",
						"Specs are published to the developer portal and cover all request/response schemas.",
						"Required before partner integrations begin.", 4, 0, 6, 2, 10, 0, 0),
				new ActivitySeed("Prototype AI-Assisted Dispute Triage",
						"Build a proof-of-concept that uses ML to classify and prioritize incoming invoice disputes.",
						"Prototype classifies dispute type with 80% accuracy and routes to the correct team queue.",
						"Exploratory R&D backlog item; not sprint-committed.", 4, 0, 14, 3, 16, 0, 0));
		try {
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CActivityTypeService activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
			final CActivityPriorityService activityPriorityService =
					CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CActivityType> availableTypes = activityTypeService.listByCompany(project.getCompany());
			final List<CActivityPriority> availablePriorities =
					activityPriorityService.listByCompany(project.getCompany());
			final List<CUser> availableUsers = userService.listByCompany(project.getCompany());
			final List<CUserStory> availableUserStories = new ArrayList<>(userStoryService.listByProject(project));
			final CUserStory[] parentUserStories = {
					sampleUserStory1, sampleUserStory2
			};
			if (availableUserStories.isEmpty()) {
				for (final CUserStory parentUserStory : parentUserStories) {
					if (parentUserStory != null) {
						availableUserStories.add(parentUserStory);
					}
				}
			}
			final List<CActivity> createdActivities = new ArrayList<>();
			int createdCount = 0;
			for (final ActivitySeed seed : seeds) {
				final CActivityType type =
						availableTypes.isEmpty() ? activityTypeService.getRandom(project.getCompany())
								: availableTypes.get(createdCount % availableTypes.size());
				final CActivityPriority priority =
						availablePriorities.isEmpty() ? activityPriorityService.getRandom(project.getCompany())
								: availablePriorities.get(createdCount % availablePriorities.size());
				final CUser user = availableUsers.isEmpty() ? userService.getRandom(project.getCompany())
						: availableUsers.get(createdCount % availableUsers.size());
				final CActivity activity = new CActivity(seed.name(), project);
				activity.setDescription(seed.description());
				activity.setEntityType(type);
				activity.setPriority(priority);
				activity.setAssignedTo(user);
				activity.setAcceptanceCriteria(seed.acceptanceCriteria());
				activity.setNotes(seed.notes());
				activity.setStartDate(LocalDate.now().minusDays(seed.startOffsetDays()));
				activity.setDueDate(activity.getStartDate().plusDays(seed.durationDays()));
				activity.setStoryPoint(Long.valueOf(seed.storyPoints()));
				activity.setEstimatedHours(BigDecimal.valueOf(seed.estimatedHours()));
				activity.setProgressPercentage(seed.progressPercentage());
				activity.setResults(
						seed.progressPercentage() >= 45 ? "Implementation slice reviewed with product and QA." : "");
				final List<CProjectItemStatus> allStatuses = statusService.listByCompany(project.getCompany());
				// Assign realistic status based on progress
				CProjectItemStatus assignedStatus;
				if (seed.progressPercentage() >= 75) {
					assignedStatus = findStatusByName(allStatuses, "In Review");
					if (assignedStatus == null) {
						assignedStatus = findStatusByName(allStatuses, "Done");
					}
				} else if (seed.progressPercentage() >= 15) {
					// Make 1 out of 5 items Blocked
					if (createdCount % 5 == 3) {
						assignedStatus = findStatusByName(allStatuses, "Blocked");
					} else {
						assignedStatus = findStatusByName(allStatuses, "In Progress");
					}
				} else {
					assignedStatus = findStatusByName(allStatuses, "To Do");
				}
				if (assignedStatus == null && !allStatuses.isEmpty()) {
					assignedStatus = allStatuses.get(0);
				}
				if (assignedStatus != null) {
					activity.setStatus(assignedStatus);
				}
				CUserStory parentUserStory = !availableUserStories.isEmpty()
						? availableUserStories.get(seed.parentUserStoryIndex() % availableUserStories.size())
						: parentUserStories[Math.min(seed.parentUserStoryIndex(), parentUserStories.length - 1)];
				if (!minimal) {
					if (createdCount == 0 && sampleUserStory1 != null) {
						parentUserStory = sampleUserStory1;
					} else if (createdCount == 1 && sampleUserStory2 != null) {
						parentUserStory = sampleUserStory2;
					}
				}
				if (parentUserStory != null) {
					activity.setParentItem(parentUserStory);
				} else if (sampleUserStory1 != null) {
					activity.setParentItem(sampleUserStory1);
				}
				activityService.save(activity);
				createdActivities.add(activity);
				createdCount++;
				if (minimal && createdCount >= 6) {
					break;
				}
			}
			// Add relationships: comments, attachments, links (only if not minimal)
			if (!minimal && !createdActivities.isEmpty()) {
				_addRelationshipsToActivities(createdActivities, project);
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample activities for project: {} reason={}", project.getName(),
					e.getMessage());
			throw new RuntimeException("Failed to initialize sample activities for project: " + project.getName(), e);
		}
	}
}
