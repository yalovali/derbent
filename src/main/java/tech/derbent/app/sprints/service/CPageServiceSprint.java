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
import tech.derbent.app.sprints.domain.CSprint;

/** CPageServiceSprint - Page service for Sprint management UI. Handles UI events and interactions for sprint views. */
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> implements IPageServiceHasStatusAndWorkflow<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprint.class);
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;

	public CPageServiceSprint(final IPageServiceImplementer<CSprint> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	public Component createSpritActivitiesComponent() {
		try {
			Component component = new Div("safdfasfsaf");
			return component;
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
}
