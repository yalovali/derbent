package tech.derbent.plm.agile.service;

import java.time.LocalDate;
import java.util.List;
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
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.domain.CUserStoryType;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;

public class CUserStoryInitializerService extends CInitializerServiceProjectItem {

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
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(scr, clazz);
			// Links section - standard section for entities that can be linked
			CLinkInitializerService.addDefaultSection(scr, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(scr, clazz);
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
			LOGGER.error("Error creating user story view.");
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
	 * Initialize sample user stories for a project.
	 *
	 * @param project       the project to create user stories for
	 * @param minimal       if true, creates only 1 user story; if false, creates 2 user stories
	 * @param sampleFeature1 the first feature to link user stories to (can be null)
	 * @param sampleFeature2 the second feature to link second user story to (can be null)
	 * @return array of created user stories [userStory1, userStory2] where userStory2 may be null if minimal is true
	 */
	public static CUserStory[] initializeSample(final CProject<?> project, final boolean minimal, final CFeature sampleFeature1,
			final CFeature sampleFeature2) throws Exception {
		// Seed data for sample user stories with parent feature index
		record UserStorySeed(String name, String description, String acceptanceCriteria, int parentFeatureIndex) {}

		final List<UserStorySeed> seeds = List.of(
				new UserStorySeed("User Login and Authentication",
						"As a user, I want to securely login to the system so that I can access my personalized dashboard",
						"Given valid credentials, when user logs in, then dashboard is displayed within 2 seconds", 0),
				new UserStorySeed("Profile Management", "As a user, I want to update my profile information so that my details are current",
						"Given authenticated user, when profile is updated, then changes are persisted and confirmed", 1));

		try {
			final CUserStoryService userStoryService = CSpringContext.getBean(CUserStoryService.class);
			final CUserStoryTypeService userStoryTypeService = CSpringContext.getBean(CUserStoryTypeService.class);
			final CActivityPriorityService activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);

			final CFeature[] parentFeatures = { sampleFeature1, sampleFeature2 };
			final CUserStory[] createdUserStories = new CUserStory[2];
			int index = 0;

			for (final UserStorySeed seed : seeds) {
				final CUserStoryType type = userStoryTypeService.getRandom(project.getCompany());
				final CActivityPriority priority = activityPriorityService.getRandom(project.getCompany());
				final CUser user = userService.getRandom(project.getCompany());

				CUserStory userStory = new CUserStory(seed.name(), project);
				userStory.setDescription(seed.description());
				userStory.setEntityType(type);
				userStory.setPriority(priority);
				userStory.setAssignedTo(user);
				userStory.setStartDate(LocalDate.now().plusDays((int) (Math.random() * 90)));
				userStory.setDueDate(userStory.getStartDate().plusDays((long) (Math.random() * 60)));
				userStory.setAcceptanceCriteria(seed.acceptanceCriteria());

				if (type != null && type.getWorkflow() != null) {
					final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(userStory);
					if (!initialStatuses.isEmpty()) {
						userStory.setStatus(initialStatuses.get(0));
					}
				}

				// Link UserStory to Feature parent
				final CFeature parentFeature = parentFeatures[seed.parentFeatureIndex()];
				if (parentFeature != null) {
					userStory.setParentFeature(parentFeature);
				} else if (sampleFeature1 != null) {
					// Fallback to first feature if specified parent not available
					userStory.setParentFeature(sampleFeature1);
				}

				userStory = userStoryService.save(userStory);
				createdUserStories[index++] = userStory;
				LOGGER.info("Created UserStory '{}' (ID: {}) with parent Feature '{}'", userStory.getName(), userStory.getId(),
						userStory.getParentFeature() != null ? userStory.getParentFeature().getName() : "NONE");

				if (minimal) {
					break;
				}
			}

			LOGGER.debug("Created {} sample user stor(y|ies) for project: {}", index, project.getName());
			return createdUserStories;
		} catch (final Exception e) {
			LOGGER.error("Error initializing sample user stories for project: {}", project.getName(), e);
			throw new RuntimeException("Failed to initialize sample user stories for project: " + project.getName(), e);
		}
	}
}
