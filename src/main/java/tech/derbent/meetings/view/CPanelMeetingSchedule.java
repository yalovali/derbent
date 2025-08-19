package tech.derbent.meetings.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;

/**
 * CPanelMeetingSchedule - Panel for grouping schedule-related fields of CMeeting entity. Layer: View (MVC) Groups
 * fields: meetingDate, endDate
 */
public class CPanelMeetingSchedule extends CPanelMeetingBase {

    private static final long serialVersionUID = 1L;

    public CPanelMeetingSchedule(final CMeeting currentEntity, final CEnhancedBinder<CMeeting> beanValidationBinder,
            final CMeetingService entityService)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        super("Schedule", currentEntity, beanValidationBinder, entityService, null);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Schedule fields - timing information
        setEntityFields(List.of("meetingDate", "endDate"));
    }
}