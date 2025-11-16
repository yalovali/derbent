package tech.derbent.app.meetings.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CMeetingTypeInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Meeting Type Information";
	private static final Class<?> clazz = CMeetingType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".5";
	private static final String menuTitle = MenuTitle_TYPES + ".Meeting Types";
	private static final String pageDescription = "Manage meeting type categories";
	private static final String pageTitle = "Meeting Type Management";
	private static final boolean showInQuickToolbar = false;

        public static CDetailSection createBasicView(final CProject project) throws Exception {
                Check.notNull(project, "project cannot be null");
                try {
                        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workflow"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Display Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
                        detailSection.debug_printScreenInformation();
                        return detailSection;
                } catch (final Exception e) {
                        LOGGER.error("Error creating meeting type view.");
                        throw e;
                }
        }

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "project"));
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
		final String[][] meetingTypes = {
				{
						"Daily Standup", "Daily team synchronization meetings"
				}, {
						"Sprint Planning", "Sprint planning and estimation meetings"
				}, {
						"Sprint Review", "Sprint review and demonstration meetings"
				}, {
						"Sprint Retrospective", "Sprint retrospective and improvement meetings"
				}, {
						"Project Review", "Project review and status meetings"
				}, {
						"Technical Review", "Technical design and code review meetings"
				}, {
						"Stakeholder Meeting", "Meetings with project stakeholders"
				}, {
						"Training Session", "Training and knowledge sharing sessions"
				}
		};
		final tech.derbent.app.meetings.service.CMeetingTypeService service =
				tech.derbent.api.config.CSpringContext.getBean(tech.derbent.app.meetings.service.CMeetingTypeService.class);
		initializeProjectEntity(meetingTypes, service, project, minimal);
	}
}
