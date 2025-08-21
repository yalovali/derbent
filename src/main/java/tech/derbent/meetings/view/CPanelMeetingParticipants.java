package tech.derbent.meetings.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.users.service.CUserService;

/**
 * CPanelMeetingParticipants - Panel for grouping participant-related fields of CMeeting
 * entity. Layer: View (MVC) Groups fields: participants, attendees
 */
public class CPanelMeetingParticipants extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingParticipants(final CMeeting currentEntity,
		final CEnhancedBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService, final CMeetingTypeService meetingTypeService,
		final CUserService userService) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		super("Participants & Attendees", currentEntity, beanValidationBinder,
			entityService, meetingTypeService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of("participants", "attendees"));
	}
}
