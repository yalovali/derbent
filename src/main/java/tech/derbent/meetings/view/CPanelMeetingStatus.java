package tech.derbent.meetings.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusService;

/**
 * CPanelMeetingStatus - Panel for grouping status and responsibility fields of CMeeting
 * entity. Layer: View (MVC) Groups fields: status, responsible, relatedActivity
 */
public class CPanelMeetingStatus extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingStatus(final CMeeting currentEntity,
		final CEnhancedBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService,
		final CMeetingStatusService meetingStatusService) throws NoSuchMethodException,
		SecurityException, IllegalAccessException, InvocationTargetException {
		super("Status & Responsibility", currentEntity, beanValidationBinder,
			entityService, null);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Status and responsibility fields - meeting management information
		setEntityFields(List.of("status", "responsible", "relatedActivity"));
	}
}