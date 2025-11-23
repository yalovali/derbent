package tech.derbent.app.sprints.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.screens.service.CInitializerServiceProjectItem;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintType;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

/** CSprintInitializerService - Initializer service for sprint management. Creates UI configuration and sample data for sprints. */
public class CSprintInitializerService extends CInitializerServiceProjectItem {

	static final Class<?> clazz = CSprint.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".3";
	private static final String menuTitle = MenuTitle_PROJECT + ".Sprints";
	private static final String pageDescription = "Sprint management for agile development";
	private static final String pageTitle = "Sprint Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection scr = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(scr, clazz, project, true);
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Details"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "startDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "endDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CDetailLinesService.createSection("Sprint Items"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activities"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "meetings"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemCount"));
			scr.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentId"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "description", "startDate", "endDate", "status", "color", "assignedTo", "itemCount",
				"project", "createdDate", "lastModifiedDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		try {
			LOGGER.debug("Initializing sample sprints for project: {}", project.getName());
			// Get services
			final CSprintService sprintService = CSpringContext.getBean(CSprintService.class);
			final CSprintTypeService sprintTypeService = CSpringContext.getBean(CSprintTypeService.class);
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
			// Create sprints
			final int sprintCount = minimal ? 2 : 3;
			for (int i = 1; i <= sprintCount; i++) {
				final CSprintType sprintType = sprintTypeService.getRandom(project);
				final CUser assignedUser = userService.getRandom();
				final CSprint sprint = new CSprint("Sprint " + i, project);
				sprint.setDescription("Sprint " + i + " - Development iteration");
				sprint.setEntityType(sprintType);
				sprint.setAssignedTo(assignedUser);
				sprint.setColor(CSprint.DEFAULT_COLOR);
				sprint.setStartDate(LocalDate.now().plusWeeks((i - 1) * 2));
				sprint.setEndDate(LocalDate.now().plusWeeks(i * 2));
				// Add random activities and meetings to sprint
				final List<CActivity> activities = activityService.listByProject(project);
				if (!activities.isEmpty()) {
					// Add 2-4 random activities
					final int activityCount = minimal ? 2 : (3 + (int) (Math.random() * 2));
					for (int j = 0; j < Math.min(activityCount, activities.size()); j++) {
						final CActivity activity = activities.get((int) (Math.random() * activities.size()));
						sprint.addItem(activity);
					}
				}
				final List<CMeeting> meetings = meetingService.listByProject(project);
				if (!meetings.isEmpty()) {
					// Add 1-2 random meetings
					final int meetingCount = minimal ? 1 : (1 + (int) (Math.random() * 2));
					for (int j = 0; j < Math.min(meetingCount, meetings.size()); j++) {
						final CMeeting meeting = meetings.get((int) (Math.random() * meetings.size()));
						sprint.addItem(meeting);
					}
				}
				sprintService.save(sprint);
				LOGGER.debug("Created sample sprint: {} with {} items", sprint.getName(), sprint.getItemCount());
			}
		} catch (final Exception e) {
			LOGGER.error("Error creating sample sprints", e);
			throw e;
		}
	}
}
