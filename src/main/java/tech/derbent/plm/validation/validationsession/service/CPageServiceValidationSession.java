package tech.derbent.plm.validation.validationsession.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.validation.validationsession.domain.CValidationSession;
import tech.derbent.plm.validation.validationsession.view.CComponentValidationExecution;

public class CPageServiceValidationSession extends CPageServiceDynamicPage<CValidationSession> {

	private CButton buttonExecute;
	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationSession.class);
	Long serialVersionUID = 1L;
	@Autowired
	private CValidationSessionService validationSessionService;

	public CPageServiceValidationSession(IPageServiceImplementer<CValidationSession> view) {
		super(view);
	}

	/** Adds an Execute button to the CRUD toolbar for starting validation execution. Button is only enabled when a session is loaded. */
	
	private void addExecuteButtonToToolbar() {
		try {
			// Get the toolbar from the view if it implements ICrudToolbarOwnerPage
			if (getView() instanceof ICrudToolbarOwnerPage) {
				final CCrudToolbar toolbar = ((ICrudToolbarOwnerPage) getView()).getCrudToolbar();
				if (toolbar != null && buttonExecute == null) {
					// Create Execute button with primary styling
					buttonExecute = new CButton("Execute", VaadinIcon.PLAY.create());
					buttonExecute.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
					buttonExecute.getElement().setAttribute("title", "Execute this validation session");
					buttonExecute.addClickListener(event -> on_execute_clicked());
					buttonExecute.setEnabled(false); // Initially disabled until entity is loaded
					// Add button to toolbar after standard CRUD buttons
					toolbar.addCustomComponent(buttonExecute);
					LOGGER.debug("Execute button added to validation session toolbar");
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to add Execute button to toolbar: {}", e.getMessage(), e);
			// Don't throw - toolbar customization failure shouldn't break page load
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CValidationSession.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
			// Add Execute button to toolbar after standard buttons
			addExecuteButtonToToolbar();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CValidationSession.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CValidationSession");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CValidationSession> gridView = (CGridViewBaseDBEntity<CValidationSession>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}


	/** Creates validation execution component for running validations.
	 * @return validation execution component instance */
	public Component createValidationExecutionComponent() {
		try {
			LOGGER.debug("Creating validation execution component");
			Check.notNull(validationSessionService, "ValidationSessionService must be injected");
			final CComponentValidationExecution component = new CComponentValidationExecution(validationSessionService);
			registerComponent(component.getComponentName(), component);
			LOGGER.debug("Validation execution component created and registered");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create validation execution component: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Handler for Execute button click - navigates to execution view. */
	private void on_execute_clicked() {
		try {
			final CValidationSession currentSession = getView().getValue();
			Check.notNull(currentSession, "No validation session selected for execution");
			Check.notNull(currentSession.getId(), "Validation session must be saved before execution");
			LOGGER.info("Navigating to validation execution view for session: {} (ID: {})", currentSession.getName(), currentSession.getId());
			// Navigate to the execution view page
			// The execution view will be created by the initializer with a separate route
			UI.getCurrent().navigate("validation/sessions/execute/" + currentSession.getId());
		} catch (final Exception e) {
			LOGGER.error("Failed to execute validation session: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to start validation execution: " + e.getMessage());
		}
	}

	/** Updates Execute button state when entity changes. */
	public void updateExecuteButtonState(final CValidationSession entity) {
		if (buttonExecute != null) {
			// Enable Execute button only when a validation session entity is loaded
			buttonExecute.setEnabled(entity != null && entity.getId() != null);
		}
	}
}
