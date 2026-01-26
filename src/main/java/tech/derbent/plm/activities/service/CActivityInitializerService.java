package tech.derbent.plm.activities.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationInitializerService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.domain.CActivityType;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CActivityInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CActivity.class;
	static Map<String, EntityFieldInfo> fields;
	static EntityFieldInfo info;
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".2";
	private static final String menuTitle = MenuTitle_PROJECT + ".Activities";
	private static final String pageDescription = "Activity management for projects";
	private static final String pageTitle = "Activity Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
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
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentId"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			CAgileParentRelationInitializerService.addDefaultSection(scr, clazz, project);
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating activity view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "componentWidget", "entityType", "assignedTo", "createdBy", "startDate", "dueDate",
				"completionDate", "progressPercentage", "estimatedHours", "actualHours", "remainingHours", "status", "priority", "project",
				"createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	/**
	 * Initialize sample activities for a project.
	 *
	 * @param project          the project to create activities for
	 * @param minimal          if true, creates only 1 activity; if false, creates 3 activities
	 * @param sampleUserStory1 the first user story to link activities to (can be null)
	 * @param sampleUserStory2 the second user story to link third activity to (can be null)
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal, final CUserStory sampleUserStory1,
			final CUserStory sampleUserStory2) throws Exception {
		try {
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CActivityTypeService activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
			final CActivityPriorityService activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);

			// Activity 1: Linked to UserStory 1
			final CActivityType type1 = activityTypeService.getRandom(project.getCompany());
			final CActivityPriority priority1 = activityPriorityService.getRandom(project.getCompany());
			final CUser user1 = userService.getRandom(project.getCompany());

			final CActivity activity1 = new CActivity("Implement Login Form UI", project);
			activity1.setDescription("Create responsive login form with email and password fields");
			activity1.setEntityType(type1);
			activity1.setPriority(priority1);
			activity1.setAssignedTo(user1);
			activity1.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 60)));
			activity1.setDueDate(activity1.getStartDate().plusDays((long) (Math.random() * 30)));
			activity1.setEstimatedHours(java.math.BigDecimal.valueOf(8));
			if (type1 != null && type1.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(activity1);
				if (!initialStatuses.isEmpty()) {
					activity1.setStatus(initialStatuses.get(0));
				}
			}
			// Link Activity to UserStory parent (type-safe)
			if (sampleUserStory1 != null) {
				activity1.setParentUserStory(sampleUserStory1);
			}
			activityService.save(activity1);
			LOGGER.info("Created Activity '{}' (ID: {}) with parent UserStory '{}'", activity1.getName(), activity1.getId(),
					sampleUserStory1 != null ? sampleUserStory1.getName() : "NONE");

			if (minimal) {
				return;
			}

			// Activity 2: Also linked to UserStory 1
			final CActivityType type2 = activityTypeService.getRandom(project.getCompany());
			final CActivityPriority priority2 = activityPriorityService.getRandom(project.getCompany());
			final CUser user2 = userService.getRandom(project.getCompany());

			final CActivity activity2 = new CActivity("Implement Authentication API", project);
			activity2.setDescription("Create backend API for user authentication and session management");
			activity2.setEntityType(type2);
			activity2.setPriority(priority2);
			activity2.setAssignedTo(user2);
			activity2.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 60)));
			activity2.setDueDate(activity2.getStartDate().plusDays((long) (Math.random() * 30)));
			activity2.setEstimatedHours(java.math.BigDecimal.valueOf(16));
			if (type2 != null && type2.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(activity2);
				if (!initialStatuses.isEmpty()) {
					activity2.setStatus(initialStatuses.get(0));
				}
			}
			if (sampleUserStory1 != null) {
				activity2.setParentUserStory(sampleUserStory1);
			}
			activityService.save(activity2);
			LOGGER.info("Created Activity '{}' (ID: {}) with parent UserStory '{}'", activity2.getName(), activity2.getId(),
					sampleUserStory1 != null ? sampleUserStory1.getName() : "NONE");

			// Activity 3: Linked to UserStory 2
			final CActivityType type3 = activityTypeService.getRandom(project.getCompany());
			final CActivityPriority priority3 = activityPriorityService.getRandom(project.getCompany());
			final CUser user3 = userService.getRandom(project.getCompany());

			final CActivity activity3 = new CActivity("Create Profile Edit Form", project);
			activity3.setDescription("Build form for users to update their profile information");
			activity3.setEntityType(type3);
			activity3.setPriority(priority3);
			activity3.setAssignedTo(user3);
			activity3.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 60)));
			activity3.setDueDate(activity3.getStartDate().plusDays((long) (Math.random() * 25)));
			activity3.setEstimatedHours(java.math.BigDecimal.valueOf(10));
			if (type3 != null && type3.getWorkflow() != null) {
				final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(activity3);
				if (!initialStatuses.isEmpty()) {
					activity3.setStatus(initialStatuses.get(0));
				}
			}
			if (sampleUserStory2 != null) {
				activity3.setParentUserStory(sampleUserStory2);
			} else if (sampleUserStory1 != null) {
				activity3.setParentUserStory(sampleUserStory1);
			}
			activityService.save(activity3);
			LOGGER.info("Created Activity '{}' (ID: {}) with parent UserStory '{}'", activity3.getName(), activity3.getId(),
					sampleUserStory2 != null ? sampleUserStory2.getName() : (sampleUserStory1 != null ? sampleUserStory1.getName() : "NONE"));

			LOGGER.debug("Created sample activities for project: {} (linked to UserStories in agile hierarchy)", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample activities for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample activities for project: " + project.getName(), e);
		}
	}
}
