package tech.derbent.meetings.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CMeetingInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Meeting Information";
	private static final Class<?> ENTITY_CLASS = CMeeting.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".5";
	private static final String menuTitle = MenuTitle_PROJECT + ".Meetings";
	private static final String pageDescription = "Meeting management with scheduling and participant tracking";
	private static final String pageTitle = "Meeting Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "meetingType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "project"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Schedule"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "meetingDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "endDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Participants"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "attendees"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "participants"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Agenda & Location"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "agenda"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "location"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Status & Follow-up"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "relatedActivity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "responsible"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "minutes"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "linkedElement"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating meeting view.");
			throw e;
		}
	}

	public static void createDefaultViews(final CProject project) {}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		grid.setColumnFields(List.of("id", "name", "description", "meetingType", "project", "meetingDate", "endDate", "createdBy", "createdDate", "status", "location"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
