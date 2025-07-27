package tech.derbent.meetings.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;

/**
 * CPanelMeetingBasicInfo - Panel for grouping basic information fields
 * of CMeeting entity.
 * Layer: View (MVC)
 * Groups fields: name, description, meetingType, project
 */
public class CPanelMeetingBasicInfo extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingBasicInfo(final CMeeting currentEntity,
		final BeanValidationBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService) {
		super("Basic Information", currentEntity, beanValidationBinder, entityService);
		// only open this panel
		openPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Basic Information fields - meeting identity and categorization
		setEntityFields(List.of("name", "description", "meetingType", "project"));
	}
}