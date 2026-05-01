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
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CEpicType;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CEpicInitializerService extends CProjectItemInitializerService {

	static final Class<?> clazz = CEpic.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CEpicInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".10";
	private static final String menuTitle = MenuTitle_PROJECT + ".Epics";
	private static final String pageDescription = "Epic management for projects";
	private static final String pageTitle = "Epic Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CEntityNamedInitializerService.createBasicView(scr, clazz, project, true);
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
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			CParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			CParentRelationInitializerService.addDefaultChildrenSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating epic view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "componentWidget", "entityType", "assignedTo", "createdBy",
				"startDate", "dueDate", "completionDate", "progressPercentage", "estimatedHours", "actualHours",
				"remainingHours", "status", "priority", "project", "createdDate", "lastModifiedDate"));
		grid.setEditableColumnFields(List.of("name", "assignedTo", "startDate", "dueDate", "progressPercentage", "estimatedHours", "actualHours", "remainingHours", "status", "priority"));
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

	/** Initialize sample epics for a project.
	 * @param project the project to create epics for
	 * @param minimal if true, creates only 1 epic; if false, creates 2 epics
	 * @return array of created epics [epic1, epic2] where epic2 may be null if minimal is true */
	public static CEpic[] initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		record EpicSeed(String name, String description, String acceptanceCriteria, String notes, int startOffsetDays,
				int durationDays, int storyPoints, int estimatedHours, int actualHours, int progressPercentage) {}
		final List<EpicSeed> seeds = List.of(new EpicSeed("Requirements Breakdown & Delivery Plan",
				"Demonstrate a multi-level requirements tree (level0 → sub requirements → leaf requirements) that drives milestones, deliverables, and activities.",
				"A clear Epic→Feature→UserStory chain exists and leaf requirements have activities + milestones attached.",
				"Used as a sample backbone for requirement decomposition and delivery tracking.", 60, 120, 40, 240, 90,
				35),
				new EpicSeed("Milestone-Driven Release Scope",
						"Model milestone checkpoints and deliverables that roll up into a delivery epic.",
						"Milestones, deliverables, and activities are linked under the same requirement hierarchy.",
						"Shows how planning artifacts connect to execution items.", 40, 100, 34, 190, 70, 28),
				new EpicSeed("Identity and Access Modernization",
						"Modernize login, session security, and access review capabilities for enterprise customers.",
						"Authentication, session revocation, and audit controls are available for pilot customers.",
						"Program increment focused on hardening sign-in, role review, and audit readiness.", 75, 150,
						55, 320, 170, 55),
				new EpicSeed("Customer Workspace Experience",
						"Deliver a self-service workspace where customers can manage preferences, saved views, and daily operations.",
						"Customers can update profile data, save workspace filters, and complete core self-service flows without support.",
						"High-visibility product stream for reducing support effort and improving retention.", 45, 130,
						48, 280, 120, 40),
				new EpicSeed("Billing and Case Operations",
						"Improve dispute handling, auditability, and operational follow-up for finance and customer success teams.",
						"Billing disputes, follow-up actions, and evidence capture are traceable end-to-end.",
						"Cross-functional epic shared by finance, support, and product operations.", 20, 120, 34, 210,
						80, 30),
				new EpicSeed("Release Readiness and Reliability",
						"Strengthen release governance, observability, and go-live readiness for major launches.",
						"Release checklist, rollback preparedness, and monitoring coverage are verified before launch approval.",
						"Foundation epic for launch confidence, incident handling, and service resilience.", 5, 90, 26,
						160, 45, 20));
		try {
			final CEpicService epicService = CSpringContext.getBean(CEpicService.class);
			final CEpicTypeService epicTypeService = CSpringContext.getBean(CEpicTypeService.class);
			final CActivityPriorityService activityPriorityService =
					CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CEpicType> availableTypes = epicTypeService.listByCompany(project.getCompany());
			final List<CActivityPriority> availablePriorities =
					activityPriorityService.listByCompany(project.getCompany());
			final List<CUser> availableUsers = userService.listByCompany(project.getCompany());
			final CEpic[] createdEpics = new CEpic[2];
			int createdCount = 0;
			int returnIndex = 0;
			for (final EpicSeed seed : seeds) {
				final CEpicType type = availableTypes.isEmpty() ? epicTypeService.getRandom(project.getCompany())
						: availableTypes.get(createdCount % availableTypes.size());
				final CActivityPriority priority =
						availablePriorities.isEmpty() ? activityPriorityService.getRandom(project.getCompany())
								: availablePriorities.get(createdCount % availablePriorities.size());
				final CUser user = availableUsers.isEmpty() ? userService.getRandom(project.getCompany())
						: availableUsers.get(createdCount % availableUsers.size());
				CEpic epic = new CEpic(seed.name(), project);
				epic.setDescription(seed.description());
				epic.setEntityType(type);
				epic.setPriority(priority);
				epic.setAssignedTo(user);
				epic.setAcceptanceCriteria(seed.acceptanceCriteria());
				epic.setNotes(seed.notes());
				epic.setStartDate(LocalDate.now().minusDays(seed.startOffsetDays()));
				epic.setDueDate(epic.getStartDate().plusDays(seed.durationDays()));
				epic.setStoryPoint(Long.valueOf(seed.storyPoints()));
				epic.setEstimatedHours(BigDecimal.valueOf(seed.estimatedHours()));
				epic.setActualHours(BigDecimal.valueOf(seed.actualHours()));
				epic.setRemainingHours(BigDecimal.valueOf(Math.max(seed.estimatedHours() - seed.actualHours(), 0)));
				epic.setHourlyRate(BigDecimal.valueOf(140));
				epic.setEstimatedCost(epic.getHourlyRate().multiply(epic.getEstimatedHours()));
				epic.setActualCost(epic.getHourlyRate().multiply(epic.getActualHours()));
				epic.setProgressPercentage(seed.progressPercentage());
				epic.setResults(seed.progressPercentage() >= 50
						? "Discovery, architecture, and dependency mapping are progressing." : "");
				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(epic);
					if (!initialStatuses.isEmpty()) {
						epic.setStatus(initialStatuses.get(0));
					}
				}
				// Epic has no parent - it's root level
				epic = epicService.save(epic);
				createdCount++;
				if (returnIndex < createdEpics.length) {
					createdEpics[returnIndex++] = epic;
				}
				if (minimal) {
					break;
				}
			}
			LOGGER.debug("Created {} sample epic(s) for project: {}", createdCount, project.getName());
			return createdEpics;
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample epics for project: {} reason={}", project.getName(),
					e.getMessage());
			throw new RuntimeException("Failed to initialize sample epics for project: " + project.getName(), e);
		}
	}
}
