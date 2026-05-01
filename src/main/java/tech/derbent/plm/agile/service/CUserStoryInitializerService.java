package tech.derbent.plm.agile.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.domain.CUserStoryType;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CUserStoryInitializerService extends CProjectItemInitializerService {

	static final Class<?> clazz = CUserStory.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserStoryInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".11";
	private static final String menuTitle = MenuTitle_PROJECT + ".User Stories";
	private static final String pageDescription = "User story management for projects";
	private static final String pageTitle = "User Story Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CProjectItemInitializerService.createScreenLines(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createSection("System Access"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "completionDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "progressPercentage"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "remainingHours"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
			scr.addScreenLine(CDetailLinesService.createSection("Financials"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "hourlyRate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "acceptanceCriteria"));
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			// Links section - standard section for entities that can be linked
			CLinkInitializerService.addDefaultSection(scr, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notes"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "results"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			CParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			CParentRelationInitializerService.addDefaultChildrenSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating user story view.");
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

	/** Initialize sample user stories for a project.
	 * @param project        the project to create user stories for
	 * @param minimal        if true, creates only 1 user story; if false, creates 2 user stories
	 * @param sampleFeature1 the first feature to link user stories to (can be null)
	 * @param sampleFeature2 the second feature to link second user story to (can be null)
	 * @return array of created user stories [userStory1, userStory2] where userStory2 may be null if minimal is true */
	public static CUserStory[] initializeSample(final CProject<?> project, final boolean minimal,
			final CFeature sampleFeature1, final CFeature sampleFeature2) throws Exception {
		record UserStorySeed(String name, String description, String acceptanceCriteria, String notes,
				int parentFeatureIndex, int startOffsetDays, int durationDays, int storyPoints, int estimatedHours,
				int actualHours, int progressPercentage) {}
		final List<UserStorySeed> seeds = List.of(new UserStorySeed(
				"Requirement: Decompose level0 requirements into sub requirements",
				"A root requirement needs to be broken down into sub requirements and tracked consistently across execution artifacts.",
				"Given a level0 requirement, when sub requirements are created, then leaf requirements can link to milestones, deliverables, and activities.",
				"This is the first leaf requirement under the requirements epic.", 0, 28, 18, 5, 22, 8, 45),
				new UserStorySeed("Requirement: Leaf requirements include milestones and activities",
						"Leaf requirements should drive execution: activities to complete and milestone checkpoints to validate delivery.",
						"Given a leaf requirement, when activities and milestones are created, then progress can be tracked end-to-end.",
						"This sample story is used by activity/milestone/deliverable sample wiring.", 1, 24, 16, 5, 18,
						6, 38),
				new UserStorySeed("As an account owner I can enroll MFA for my workspace admins",
						"Workspace administrators need a guided enrollment flow so that privileged access is protected before rollout.",
						"Given a valid authenticator app, when MFA enrollment is completed, then recovery codes are shown and login requires MFA.",
						"Top priority story for identity rollout and launch controls.", 0, 32, 20, 8, 30, 12, 50),
				new UserStorySeed("As a security analyst I can revoke suspicious sessions in one click",
						"Security analysts need an immediate response action so that suspicious sessions can be neutralized quickly.",
						"Given an active suspicious session, when revoke is clicked, then the session is terminated and an audit event is stored.",
						"Frequently requested by security operations and compliance reviewers.", 1, 28, 18, 5, 24, 9,
						42),
				new UserStorySeed("As a customer admin I can update billing contacts and notification preferences",
						"Customer admins need profile and billing control so that invoices and alerts reach the right recipients.",
						"Given valid inputs, when profile changes are saved, then billing contacts and preference channels update immediately.",
						"Core self-service story tied to support deflection metrics.", 2, 18, 16, 5, 22, 7, 35),
				new UserStorySeed("As a customer admin I can save workspace filters for repeat use",
						"Users need reusable views so that recurring operational tasks do not require rebuilding filters every day.",
						"Given a custom query, when save is confirmed, then the query appears in saved views and can be pinned to the dashboard.",
						"Expected to feed the backlog component with visible future-ready work.", 3, 12, 14, 8, 26, 8,
						28),
				new UserStorySeed("As a finance specialist I can triage invoice disputes with SLA visibility",
						"Finance teams need a clear dispute workflow so that customer-facing escalations can be handled predictably.",
						"Given an incoming dispute, when triaged, then ownership, SLA target, and evidence checklist are visible.",
						"Shared story between finance and customer success squads.", 4, 8, 15, 8, 28, 6, 22),
				new UserStorySeed("As a release manager I can review launch blockers in a single command center",
						"Release managers need one decision surface so that go-live readiness can be tracked without spreadsheet handoffs.",
						"Given pending launch tasks, when command center opens, then blockers, approvals, and rollout status are visible.",
						"Future sprint candidate and strong backlog anchor.", 5, 4, 18, 5, 18, 2, 10),
				new UserStorySeed("As a support lead I can attach evidence and timelines to invoice cases",
						"Support leads need an evidence trail so that finance and customers can agree on dispute outcomes faster.",
						"Given a dispute case, when evidence is uploaded, then timeline, owner, and audit trail are preserved.",
						"Deliberately left deeper in backlog to demonstrate planning depth.", 4, 2, 16, 3, 16, 0, 0),
				new UserStorySeed("As an SRE I can validate release checklist completion before go-live",
						"SRE teams need checklist enforcement so that incomplete launches do not move forward accidentally.",
						"Given an incomplete release gate, when validation runs, then the missing checks are highlighted and launch remains blocked.",
						"Intentionally unassigned to preserve meaningful future backlog.", 5, 0, 14, 3, 14, 0, 0));
		try {
			final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
			final CFeatureService featureService = CSpringContext.getBean(CFeatureService.class);
			final CUserStoryTypeService userStoryTypeService = CSpringContext.getBean(CUserStoryTypeService.class);
			final CActivityPriorityService activityPriorityService =
					CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CFeature> availableFeatures = featureService.listByProject(project);
			final List<CUserStoryType> availableTypes = userStoryTypeService.listByCompany(project.getCompany());
			final List<CActivityPriority> availablePriorities =
					activityPriorityService.listByCompany(project.getCompany());
			final List<CUser> availableUsers = userService.listByCompany(project.getCompany());
			final CFeature[] parentFeatures = {
					sampleFeature1, sampleFeature2
			};
			final CUserStory[] createdUserStories = new CUserStory[2];
			int createdCount = 0;
			int returnIndex = 0;
			for (final UserStorySeed seed : seeds) {
				final CUserStoryType type =
						availableTypes.isEmpty() ? userStoryTypeService.getRandom(project.getCompany())
								: availableTypes.get(createdCount % availableTypes.size());
				final CActivityPriority priority =
						availablePriorities.isEmpty() ? activityPriorityService.getRandom(project.getCompany())
								: availablePriorities.get(createdCount % availablePriorities.size());
				final CUser user = availableUsers.isEmpty() ? userService.getRandom(project.getCompany())
						: availableUsers.get(createdCount % availableUsers.size());
				CUserStory userStory = new CUserStory(seed.name(), project);
				userStory.setDescription(seed.description());
				userStory.setEntityType(type);
				userStory.setPriority(priority);
				userStory.setAssignedTo(user);
				userStory.setNotes(seed.notes());
				userStory.setStartDate(LocalDate.now().minusDays(seed.startOffsetDays()));
				userStory.setDueDate(userStory.getStartDate().plusDays(seed.durationDays()));
				userStory.setAcceptanceCriteria(seed.acceptanceCriteria());
				userStory.setStoryPoint(Long.valueOf(seed.storyPoints()));
				userStory.setEstimatedHours(BigDecimal.valueOf(seed.estimatedHours()));
				userStory.setActualHours(BigDecimal.valueOf(seed.actualHours()));
				userStory
						.setRemainingHours(BigDecimal.valueOf(Math.max(seed.estimatedHours() - seed.actualHours(), 0)));
				userStory.setHourlyRate(BigDecimal.valueOf(120));
				userStory.setEstimatedCost(userStory.getHourlyRate().multiply(userStory.getEstimatedHours()));
				userStory.setActualCost(userStory.getHourlyRate().multiply(userStory.getActualHours()));
				userStory.setProgressPercentage(seed.progressPercentage());
				userStory.setResults(seed.progressPercentage() >= 30
						? "Story grooming is complete and implementation slices are prepared." : "");
				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(userStory);
					if (!initialStatuses.isEmpty()) {
						userStory.setStatus(initialStatuses.get(0));
					}
				}
				CFeature parentFeature = !availableFeatures.isEmpty()
						? availableFeatures.get(seed.parentFeatureIndex() % availableFeatures.size())
						: parentFeatures[Math.min(seed.parentFeatureIndex(), parentFeatures.length - 1)];
				if (!minimal) {
					if (createdCount == 0 && sampleFeature1 != null) {
						parentFeature = sampleFeature1;
					} else if (createdCount == 1 && sampleFeature2 != null) {
						parentFeature = sampleFeature2;
					}
				}
				if (parentFeature != null) {
					userStory.setParentItem(parentFeature);
				} else if (sampleFeature1 != null) {
					// Fallback to first feature if specified parent not available
					userStory.setParentItem(sampleFeature1);
				}
				userStory = userStoryService.save(userStory);
				createdCount++;
				if (returnIndex < createdUserStories.length) {
					createdUserStories[returnIndex++] = userStory;
				}
				if (minimal) {
					break;
				}
			}
			LOGGER.debug("Created {} sample user stor(y|ies) for project: {}", createdCount, project.getName());
			return createdUserStories;
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample user stories for project: {} reason={}", project.getName(),
					e.getMessage());
			throw new RuntimeException("Failed to initialize sample user stories for project: " + project.getName(), e);
		}
	}
}
