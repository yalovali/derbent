package tech.derbent.app.sprints.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

public class CSprintItemInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CSprintItem.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintItemInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".31";
	private static final String menuTitle = MenuTitle_PROJECT + ".Sprint Items";
	private static final String pageDescription = "Items assigned to active sprints";
	private static final String pageTitle = "Sprint Items";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection("Sprint Item"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sprint"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemId"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "itemOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "componentWidget"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating sprint item view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "sprint", "itemType", "itemId", "itemOrder", "status", "createdDate", "lastModifiedDate"));
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
		final CSprintService sprintService = CSpringContext.getBean(CSprintService.class);
		final CSprintItemService sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
		final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
		final List<CSprint> sprints = sprintService.listByProject(project);
		for (final CSprint sprint : sprints) {
			if (!sprintItemService.findByMasterIdWithItems(sprint.getId()).isEmpty()) {
				if (minimal) {
					return;
				}
				continue;
			}
			final CActivity activity = activityService.getRandom(project);
			if (activity != null) {
				sprint.addItem(activity);
			}
			if (!minimal) {
				final CMeeting meeting = meetingService.getRandom(project);
				if (meeting != null) {
					sprint.addItem(meeting);
				}
			}
			if (!sprint.getSprintItems().isEmpty()) {
				sprintService.save(sprint);
			}
			if (minimal) {
				return;
			}
		}
	}
}
