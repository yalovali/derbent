package tech.derbent.meetings.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;

/**
 * CPanelMeetingAgenda - Panel for grouping agenda and location fields of CMeeting entity.
 * Layer: View (MVC) Groups fields: location, agenda
 */
public class CPanelMeetingAgenda extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	public CPanelMeetingAgenda(final CMeeting currentEntity,
		final CEnhancedBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService) throws NoSuchMethodException,
		SecurityException, IllegalAccessException, InvocationTargetException {
		super("Agenda & Location", currentEntity, beanValidationBinder, entityService,
			null);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Agenda and location fields - meeting content and venue information
		setEntityFields(List.of("location", "agenda"));
	}
}