package tech.derbent.meetings.view;

import tech.derbent.abstracts.components.CEnhancedBinder;

import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;

/**
 * CPanelMeetingBase - Abstract base class for all CMeeting-related accordion panels. Layer: View (MVC) Provides common
 * functionality for meeting entity panels following the same pattern as CPanelActivityBase.
 */
public abstract class CPanelMeetingBase extends CAccordionDBEntity<CMeeting> {

    private static final long serialVersionUID = 1L;

    CMeetingTypeService meetingTypeService;

    public CPanelMeetingBase(final String string, final CMeeting currentEntity,
            final CEnhancedBinder<CMeeting> beanValidationBinder, final CMeetingService entityService,
            final CMeetingTypeService meetingTypeService) {
        super(string, currentEntity, beanValidationBinder, CMeeting.class, entityService);
        this.meetingTypeService = meetingTypeService;
        createPanelContent();
        closePanel();
    }
}