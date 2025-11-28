package tech.derbent.app.sprints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.CComponentListEntityBase;
import tech.derbent.api.ui.component.CComponentListSprintItems;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CPageServiceSprint - Page service for Sprint management UI. Handles UI events and interactions for sprint views. */
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> implements IPageServiceHasStatusAndWorkflow<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprint.class);
	private CActivityService activityService;
	// private CComponentFieldSelection<CSprint, CActivity> componentFieldSelection;
	private CComponentListEntityBase<CSprint, CSprintItem> componentItemsSelection;
	private CMeetingService meetingService;
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;
	private CSprintItemService sprintItemService;

	public CPageServiceSprint(final IPageServiceImplementer<CSprint> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
			activityService = CSpringContext.getBean(CActivityService.class);
			meetingService = CSpringContext.getBean(CMeetingService.class);
			sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	private void createFormFields() throws Exception {
		componentItemsSelection = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
	}

	public Component createSpritActivitiesComponent() {
		try {
			createFormFields();
			return componentItemsSelection;
		} catch (Exception e) {
			LOGGER.error("Failed to create project user settings component.");
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading project user settings component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }

	public void on_description_blur(final Component component, final Object value) {
		LOGGER.info("function: on_description_blur for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_description_focus(final Component component, final Object value) {
		LOGGER.info("function: on_description_focus for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_name_change(final Component component, final Object value) {
		LOGGER.info("function: on_name_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_status_change(final Component component, final Object value) {
		LOGGER.info("function: on_status_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	@Override
	public void populateForm() {
		// IContentOwner implementation in CComponentListSprintItems handles this automatically.
		// CFormBuilder.populateForm() iterates through all components implementing IContentOwner
		// and calls setCurrentEntity() and populateForm() on them, so no manual call is needed here.
		LOGGER.debug("populateForm called - CComponentListSprintItems receives entity updates via IContentOwner interface");
	}
}
