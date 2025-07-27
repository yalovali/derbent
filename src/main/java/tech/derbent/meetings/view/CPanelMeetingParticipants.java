package tech.derbent.meetings.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;

/**
 * CPanelMeetingParticipants - Panel for grouping participant-related fields
 * of CMeeting entity.
 * Layer: View (MVC)
 * Groups fields: participants
 */
public class CPanelMeetingParticipants extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingParticipants(final CMeeting currentEntity,
		final BeanValidationBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService) {
		super("Participants", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Participants fields - attendee information
		setEntityFields(List.of("participants"));
	}
}