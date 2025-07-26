package tech.derbent.meetings.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;

/**
 * CPanelMeetingSchedule - Panel for grouping schedule-related fields
 * of CMeeting entity.
 * Layer: View (MVC)
 * Groups fields: meetingDate, endDate
 */
public class CPanelMeetingSchedule extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingSchedule(final CMeeting currentEntity,
		final BeanValidationBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService) {
		super("Schedule", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Schedule fields - timing information
		setEntityFields(List.of("meetingDate", "endDate"));
	}
}