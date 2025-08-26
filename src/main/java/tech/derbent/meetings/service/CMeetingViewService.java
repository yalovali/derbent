package tech.derbent.meetings.service;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenLinesService;

public class CMeetingViewService {

	public static final String BASE_VIEW_NAME = "Meetings View";
	public static final String BASE_PANEL_NAME = "Meetings Information";

	public static CScreen createBasicView(final CProject project) {
		try {
			final CScreen scr = new CScreen();
			final Class<?> clazz = CMeeting.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CScreenLinesService.createSection(CMeetingViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "meetingType"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CScreenLinesService.createSection("Schedule"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "meetingDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "endDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CScreenLinesService.createSection("Participants"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "attendees"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "participants"));
			scr.addScreenLine(CScreenLinesService.createSection("Agenda & Location"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "agenda"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "location"));
			scr.addScreenLine(CScreenLinesService.createSection("Status & Follow-up"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "relatedActivity"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "responsible"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "minutes"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "linkedElement"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void createDefaultViews(final CProject project) {}
}
