package tech.derbent.meetings.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;

/**
 * CPanelMeetingBase - Abstract base class for all CMeeting-related accordion panels.
 * Layer: View (MVC) Provides common functionality for meeting entity panels following the
 * same pattern as CPanelActivityBase.
 */
public abstract class CPanelMeetingBase extends CAccordionDescription<CMeeting> {

	private static final long serialVersionUID = 1L;

	CMeetingTypeService meetingTypeService;

	/**
	 * Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current meeting entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        meeting service
	 */
	public CPanelMeetingBase(final String string, final CMeeting currentEntity,
		final BeanValidationBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService,
		final CMeetingTypeService meetingTypeService) {
		super(string, currentEntity, beanValidationBinder, CMeeting.class, entityService);
		this.meetingTypeService = meetingTypeService;
		createPanelContent();
		closePanel();
	}

	@Override
	protected ComboBoxDataProvider createComboBoxDataProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void createPanelContent() {
		updatePanelEntityFields(); // Set the entity fields first
		getBaseLayout().add(
			CEntityFormBuilder.buildForm(CMeeting.class, getBinder(), getEntityFields()));
	}
}