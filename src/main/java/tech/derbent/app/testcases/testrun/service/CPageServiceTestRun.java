package tech.derbent.app.testcases.testrun.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testrun.view.CComponentTestExecution;

public class CPageServiceTestRun extends CPageServiceDynamicPage<CTestRun> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTestRun.class);
	Long serialVersionUID = 1L;
	@Autowired
	private CTestRunService testRunService;
	private CButton buttonExecute;

	public CPageServiceTestRun(IPageServiceImplementer<CTestRun> view) {
		super(view);
	}

	/** Adds an Execute button to the CRUD toolbar for starting test execution. Button is only enabled when a test run is loaded. */
	private void addExecuteButtonToToolbar() {
		try {
			// Get the toolbar from the view if it implements ICrudToolbarOwnerPage
			if (getView() instanceof ICrudToolbarOwnerPage) {
				final CCrudToolbar toolbar = ((ICrudToolbarOwnerPage) getView()).getCrudToolbar();
				if (toolbar != null && buttonExecute == null) {
					// Create Execute button with primary styling
					buttonExecute = new CButton("Execute", VaadinIcon.PLAY.create());
					buttonExecute.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY,
							com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);
					buttonExecute.getElement().setAttribute("title", "Execute this test session");
					buttonExecute.addClickListener(e -> on_execute_clicked());
					buttonExecute.setEnabled(false); // Initially disabled until entity is loaded
					// Add button to toolbar after standard CRUD buttons
					toolbar.addCustomComponent(buttonExecute);
					LOGGER.debug("Execute button added to test run toolbar");
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
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTestRun.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
			// Add Execute button to toolbar after standard buttons
			addExecuteButtonToToolbar();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CTestRun.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	/** Creates test execution component for running tests.
	 * @return test execution component instance */
	public Component createTestExecutionComponent() {
		try {
			LOGGER.debug("Creating test execution component");
			Check.notNull(testRunService, "TestRunService must be injected");
			final CComponentTestExecution component = new CComponentTestExecution(testRunService);
			component.registerWithPageService(this);
			LOGGER.debug("Test execution component created and registered");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create test execution component: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Handler for Execute button click - navigates to execution view. */
	private void on_execute_clicked() {
		try {
			final CTestRun currentTestRun = getView().getValue();
			Check.notNull(currentTestRun, "No test run selected for execution");
			Check.notNull(currentTestRun.getId(), "Test run must be saved before execution");
			LOGGER.info("Navigating to test execution view for test run: {} (ID: {})", currentTestRun.getName(), currentTestRun.getId());
			// Navigate to the execution view page
			// The execution view will be created by the initializer with a separate route
			UI.getCurrent().navigate("tests/sessions/execute/" + currentTestRun.getId());
		} catch (final Exception e) {
			LOGGER.error("Failed to execute test run: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to start test execution: " + e.getMessage());
		}
	}

	/** Updates Execute button state when entity changes. */
	public void updateExecuteButtonState(final CTestRun entity) {
		if (buttonExecute != null) {
			// Enable Execute button only when a test run entity is loaded
			buttonExecute.setEnabled(entity != null && entity.getId() != null);
		}
	}
}
