package tech.derbent.bab.setup.view;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.calimero.service.CCalimeroProcessManager;
import tech.derbent.bab.calimero.service.CCalimeroServiceStatus;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.clientproject.service.CClientProjectService;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.api.ui.constants.CUIConstants;

/** CComponentCalimeroStatus - Value-bound component for managing Calimero service status and configuration.
 * <p>
 * Displays Calimero service status with real-time updates and provides controls for:
 * <ul>
 * <li>Enable/Disable Calimero automatic startup</li>
 * <li>Configure Calimero executable path</li>
 * <li>Restart Calimero service</li>
 * <li>View real-time service status (running/stopped/disabled)</li>
 * </ul>
 * <p>
 * <strong>Pattern: Standard CComponentBase value-bound component</strong>
 * <p>
 * This component extends {@link CComponentBase} and implements standard Vaadin value binding. It can be used with Vaadin Binder and supports:
 * <ul>
 * <li>Value change listeners via {@link #addValueChangeListener}</li>
 * <li>ReadOnly mode via {@link #setReadOnly}</li>
 * <li>Required indicator via {@link #setRequiredIndicatorVisible}</li>
 * <li>Standard Vaadin form binding</li>
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * // Create component
 * CComponentCalimeroStatus component = new CComponentCalimeroStatus(calimeroProcessManager);
 * // Bind to entity
 * component.setValue(settings);
 * // Listen for changes (replaces callback pattern)
 * component.addValueChangeListener(event -> {
 * 	CSystemSettings_Bab newSettings = event.getValue();
 * 	// Save settings...
 * });
 * // Or use with Vaadin Binder
 * Binder<CSystemSettings_Bab> binder = new Binder<>();
 * binder.forField(component).bind(CSystemSettings_Bab::get, CSystemSettings_Bab::set);
 * </pre>
 *
 * @see CComponentBase
 * @see com.vaadin.flow.component.HasValue */
public class CComponentCalimeroStatus extends CComponentBase<CSystemSettings_Bab> implements IPageServiceAutoRegistrable {

	public static final String ID_AUTOSTART_CHECKBOX = "custom-calimero-autostart-checkbox";
	public static final String ID_CARD = "custom-calimero-control-card";
	public static final String ID_ENABLE_CHECKBOX = "custom-calimero-enable-checkbox";
	public static final String ID_EXECUTABLE_CONFIG_FIELD = "custom-calimero-config-path";
	public static final String ID_EXECUTABLE_PATH_FIELD = "custom-calimero-executable-path";
	public static final String ID_HEADER = "custom-calimero-header";
	public static final String ID_HEALTH_STATUS_INDICATOR = "custom-calimero-health-status-indicator";
	public static final String ID_HELLO_BUTTON = "custom-calimero-hello-button";
	public static final String ID_ROOT = "custom-calimero-status-component";
	public static final String ID_START_STOP_BUTTON = "custom-calimero-start-stop-button";
	public static final String ID_STATUS_INDICATOR = "custom-calimero-status-indicator";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentCalimeroStatus.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonHello;
	private CSpan calimeroHealthStatusIndicator;
	private final CCalimeroProcessManager calimeroProcessManager;
	private CButton calimeroStartStopButton;
	private CSpan calimeroStatusIndicator;
	private Checkbox checkboxAutostartService;
	private Checkbox checkboxEnableService;
	private final CClientProjectService clientProjectService;
	private final ISessionService sessionService;
	private TextField textFieldConfigPath;
	private TextField textFieldExecutablePath;

	/** Constructor for Calimero status component.
	 * @param calimeroProcessManager the Calimero process manager for service control
	 * @param sessionService         the session service for accessing active project
	 * @param clientProjectService   the client project service for managing HTTP clients */
	public CComponentCalimeroStatus(final CCalimeroProcessManager calimeroProcessManager, final CClientProjectService clientProjectService) {
		Check.notNull(calimeroProcessManager, "CalimeroProcessManager cannot be null");
		Check.notNull(clientProjectService, "ClientProjectService cannot be null");
		this.calimeroProcessManager = calimeroProcessManager;
		this.clientProjectService = clientProjectService;
		sessionService = CSpringContext.getBean(ISessionService.class);
		initializeComponents();
	}

	private void configureComponent() {
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", CUIConstants.GAP_TINY);
		// NOTE: UI refresh for async operations now handled by @Push(PushMode.AUTOMATIC)
		// in Application.java - WebSocket-based push for real-time updates
	}

	private void createCalimeroControlCard() {
		final CDiv card = new CDiv();
		card.setId(ID_CARD);
		card.addClassName("calimero-control-card");
		card.styleAsCard("16px");
		// Compact header with minimal spacing
		final Span title = new Span("Calimero Service");
		title.setId(ID_HEADER);
		title.addClassName("calimero-title");
		title.getStyle().set("font-weight", "600").set("font-size", "1rem").set("margin", "0").set("padding", "0");
		// Removed verbose description to save space - tooltip on checkbox provides context
		// Enable service checkbox
		checkboxEnableService = new Checkbox("Enable Calimero Service");
		checkboxEnableService.setId(ID_ENABLE_CHECKBOX);
		checkboxEnableService.addValueChangeListener(event -> on_enableServiceChanged(event.getValue()));
		checkboxEnableService.getElement().setProperty("title",
				"Automatically start and manage Calimero HTTP server. Restart service after updating executable path.");
		// Autostart service checkbox (NEW)
		checkboxAutostartService = new Checkbox("Autostart on Login");
		checkboxAutostartService.setId(ID_AUTOSTART_CHECKBOX);
		checkboxAutostartService.setValue(calimeroProcessManager.isAutostartEnabled());
		checkboxAutostartService.addValueChangeListener(event -> on_autostartChanged(event.getValue()));
		checkboxAutostartService.getElement().setProperty("title",
				"Automatically start Calimero process when you log in. If disabled, you must start it manually.");
		// Executable path field
		textFieldExecutablePath = new TextField("Calimero Executable Path");
		textFieldExecutablePath.setId(ID_EXECUTABLE_PATH_FIELD);
		textFieldExecutablePath.setWidth("100%");
		textFieldExecutablePath.setMaxLength(500);
		textFieldExecutablePath.setPlaceholder("~/git/calimero/build/calimero");
		textFieldExecutablePath.addValueChangeListener(event -> on_executablePathChanged(event.getValue()));
		textFieldExecutablePath.getElement().setProperty("title", "Full path to the Calimero executable binary");
		// Executable config field
		textFieldConfigPath = new TextField("Calimero Config Path");
		textFieldConfigPath.setId(ID_EXECUTABLE_PATH_FIELD);
		textFieldConfigPath.setWidth("100%");
		textFieldConfigPath.setMaxLength(500);
		textFieldConfigPath.setPlaceholder("~/git/calimero/config/");
		textFieldConfigPath.addValueChangeListener(event -> on_configPathChanged(event.getValue()));
		textFieldConfigPath.getElement().setProperty("title", "Full path to the Calimero config folder");
		// Status indicator
		calimeroStatusIndicator = new CSpan("Calimero status unavailable");
		calimeroStatusIndicator.setId(ID_STATUS_INDICATOR);
		calimeroStatusIndicator.addClassName("calimero-status-chip");
		// Health status indicator
		calimeroHealthStatusIndicator = new CSpan("Health: Not checked");
		calimeroHealthStatusIndicator.setId(ID_HEALTH_STATUS_INDICATOR);
		calimeroHealthStatusIndicator.addClassName("calimero-status-chip");
		// Start/Stop button - changes based on service status
		calimeroStartStopButton = CButton.createPrimary("Start Calimero", VaadinIcon.PLAY.create(), event -> on_actionStartStopCalimeroService());
		calimeroStartStopButton.setId(ID_START_STOP_BUTTON);
		calimeroStartStopButton.getElement().setProperty("title", "Start the Calimero HTTP service");
		// Hello button - health check
		buttonHello = new CButton("Hello", VaadinIcon.CONNECT.create());
		buttonHello.setId(ID_HELLO_BUTTON);
		buttonHello.addClickListener(event -> on_buttonHello_clicked());
		buttonHello.getElement().setProperty("title", "Check Calimero server health and authentication");
		final HorizontalLayout statusActions =
				new HorizontalLayout(calimeroStatusIndicator, calimeroHealthStatusIndicator, calimeroStartStopButton, buttonHello);
		statusActions.addClassName("calimero-actions");
		statusActions.setSpacing(true);
		statusActions.setPadding(false);
		// Compact card layout without description paragraph
		card.add(title, checkboxEnableService, checkboxAutostartService, textFieldExecutablePath, textFieldConfigPath, statusActions);
		add(card);
	}

	/** Ensure Calimero service is running (or restart if requested).
	 * @param forceRestart if true, force restart even if already running */
	public void ensureCalimeroRunningAsync(final boolean forceRestart) {
		LOGGER.debug("Ensuring Calimero service is running (forceRestart={})", forceRestart);
		if (calimeroProcessManager == null || calimeroStatusIndicator == null) {
			return;
		}
		final UI ui = getUI().orElse(null);
		if (ui == null) {
			return;
		}
		final String pendingText = forceRestart ? "Restarting Calimero service..." : "Checking Calimero service...";
		calimeroStatusIndicator.setText(pendingText);
		calimeroStartStopButton.setEnabled(false);
		CompletableFuture
				.supplyAsync(
						() -> forceRestart ? calimeroProcessManager.restartCalimeroService() : calimeroProcessManager.forceStartCalimeroService())
				.whenComplete((status, error) -> ui.access(() -> {
					if (error != null) {
						LOGGER.error("Calimero service operation failed", error);
						updateCalimeroStatus(CCalimeroServiceStatus.of(true, false, "Calimero operation failed"));
						CNotificationService.showException("Calimero service error", (Exception) error);
						return;
					}
					if (status != null) {
						updateCalimeroStatus(status);
					} else {
						updateCalimeroStatus(CCalimeroServiceStatus.of(false, false, "Calimero status unavailable"));
					}
				}));
	}

	@Override
	public String getComponentName() { return "calimeroStatus"; }

	private void initializeComponents() {
		try {
			setId(ID_ROOT);
			configureComponent();
			createCalimeroControlCard();
		} catch (final Exception e) {
			CNotificationService.showException("Error initializing Calimero status component", e);
		}
	}

	private void on_actionStartStopCalimeroService() {
		LOGGER.debug("Start/Stop Calimero service button clicked");
		final UI ui = getUI().orElse(null);
		Check.notNull(ui, "UI instance not available");
		// Check current status to determine action
		final CCalimeroServiceStatus currentStatus = calimeroProcessManager.getCurrentStatus();
		final boolean isRunning = currentStatus != null && currentStatus.isRunning();
		if (isRunning) {
			// Stop the service
			ui.access(() -> {
				calimeroStatusIndicator.setText("Stopping Calimero service...");
				calimeroStartStopButton.setEnabled(false);
				CompletableFuture.runAsync(calimeroProcessManager::stopCalimeroService).whenComplete((result, error) -> ui.access(() -> {
					if (error != null) {
						LOGGER.error("Failed to stop Calimero service", error);
						CNotificationService.showException("Failed to stop Calimero service", (Exception) error);
					}
					refreshCalimeroStatus();
				}));
			});
		} else {
			// Start the service
			ui.access(() -> ensureCalimeroRunningAsync(false));
		}
	}

	private void on_autostartChanged(final Boolean enabled) {
		// Update the autostart preference in the process manager
		calimeroProcessManager.setAutostartEnabled(enabled);
		LOGGER.info("🔌 Calimero autostart preference changed to: {}", enabled);
		// Note: This doesn't affect the entity - it's a session preference
		// Show a notification to inform the user
		if (enabled) {
			CNotificationService.showInfo("Calimero will start automatically on your next login");
		} else {
			CNotificationService.showInfo("Calimero autostart disabled. You'll need to start it manually.");
		}
	}

	private void on_buttonHello_clicked() {
		LOGGER.debug("Hello button clicked - checking Calimero server health");
		final UI ui = getUI().orElse(null);
		Check.notNull(ui, "UI instance not available for health check");
		// CRITICAL: Capture project and client BEFORE async call (while still on UI thread with session context)
		final CProject_Bab babProject;
		final CClientProject clientProject;
		try {
			// Get active BAB project from session (on UI thread - session available)
			final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
			if (projectOpt.isEmpty()) {
				calimeroHealthStatusIndicator.setText("Health: No project");
				calimeroHealthStatusIndicator.getStyle().set("background-color", "#FFB74D");
				CNotificationService.showError("No active project");
				return;
			}
			if (!(projectOpt.get() instanceof CProject_Bab)) {
				calimeroHealthStatusIndicator.setText("Health: Not BAB project");
				calimeroHealthStatusIndicator.getStyle().set("background-color", "#FFB74D");
				CNotificationService.showError("Active project is not a BAB project");
				return;
			}
			babProject = (CProject_Bab) projectOpt.get();
			// Get or create client from registry (reuses existing client if available)
			clientProject = clientProjectService.getOrCreateClient(babProject);
		} catch (final Exception e) {
			LOGGER.error("Failed to get project/client", e);
			calimeroHealthStatusIndicator.setText("Health: Setup failed");
			calimeroHealthStatusIndicator.getStyle().set("background-color", "#E57373");
			CNotificationService.showError("Failed to get project: " + e.getMessage());
			return;
		}
		// Disable button during check
		buttonHello.setEnabled(false);
		buttonHello.setText("Checking...");
		calimeroHealthStatusIndicator.setText("Health: Checking...");
		calimeroHealthStatusIndicator.getStyle().set("background-color", "#90CAF9");
		// Run health check asynchronously with captured client (no session access needed)
		CompletableFuture.supplyAsync(() -> {
			try {
				LOGGER.debug("Executing health check for project '{}'", babProject.getName());
				return clientProject.sayHello();
			} catch (final Exception e) {
				LOGGER.error("Health check failed with exception", e);
				return CCalimeroResponse.error("Health check failed: " + e.getMessage());
			}
		}).whenComplete((response, error) -> ui.access(() -> {
			// Re-enable button
			buttonHello.setEnabled(true);
			buttonHello.setText("Hello");
			if (error != null) {
				LOGGER.error("❌ Health check error", error);
				calimeroHealthStatusIndicator.setText("Health: Failed");
				calimeroHealthStatusIndicator.getStyle().set("background-color", "#E57373");
				CNotificationService.showError("Health check failed: " + error.getMessage());
				return;
			}
			if (response.isSuccess()) {
				LOGGER.info("✅ Calimero server is healthy and authenticated");
				calimeroHealthStatusIndicator.setText("Health: Success");
				calimeroHealthStatusIndicator.getStyle().set("background-color", "#81C784");
				CNotificationService.showSuccess("✅ Calimero server is UP and responding!");
			} else {
				LOGGER.warn("⚠️ Calimero health check failed: {}", response.getErrorMessage());
				calimeroHealthStatusIndicator.setText("Health: Failed");
				calimeroHealthStatusIndicator.getStyle().set("background-color", "#E57373");
				if (response.getStatus() == 401 || response.getErrorMessage().toLowerCase().contains("auth")) {
					CNotificationService.showError("❌ Authentication failed - check credentials");
				} else {
					CNotificationService.showError("❌ Health check failed: " + response.getErrorMessage());
				}
			}
		}));
	}

	private void on_configPathChanged(final String path) {
		final CSystemSettings_Bab currentValue = getValue();
		currentValue.setCalimeroConfigPath(path);
		LOGGER.debug("Calimero config path changed to: {}", path);
		updateValueFromClient(currentValue);
	}

	private void on_enableServiceChanged(final Boolean enabled) {
		final CSystemSettings_Bab currentValue = getValue();
		currentValue.setEnableCalimeroService(enabled);
		LOGGER.debug("Calimero service enabled changed to: {}", enabled);
		updateValueFromClient(currentValue);
	}

	private void on_executablePathChanged(final String path) {
		final CSystemSettings_Bab currentValue = getValue();
		currentValue.setCalimeroExecutablePath(path);
		LOGGER.debug("Calimero executable path changed to: {}", path);
		updateValueFromClient(currentValue);
	}

	/** Override from CComponentBase - Update UI when value changes.
	 * <p>
	 * Called automatically when setValue() is called or when updateValueFromClient() fires. Updates UI components to reflect the new entity state.
	 * @param oldValue   the previous settings value
	 * @param newValue   the new settings value
	 * @param fromClient true if change originated from UI interaction */
	@Override
	protected void onValueChanged(final CSystemSettings_Bab oldValue, final CSystemSettings_Bab newValue, final boolean fromClient) {
		LOGGER.debug("Calimero settings value changed: fromClient={}, oldValue={}, newValue={}", fromClient, oldValue, newValue);
		updateUIFromSettings(newValue);
		if (fromClient) {
			return;
		}
		// Only refresh on programmatic changes (not from user input)
		refreshCalimeroStatus();
		ensureCalimeroRunningAsync(false);
	}

	/** Refresh Calimero status indicator with current service state. */
	public void refreshCalimeroStatus() {
		LOGGER.debug("Refreshing Calimero service status indicator");
		if (calimeroStatusIndicator == null) {
			return;
		}
		final CCalimeroServiceStatus status = calimeroProcessManager.getCurrentStatus();
		updateCalimeroStatus(status);
	}

	/** Override from CComponentBase - Refresh component state.
	 * <p>
	 * Called when component needs to refresh its display from current value. Updates Calimero service status indicator. */
	@Override
	protected void refreshComponent() {
		refreshCalimeroStatus();
	}

	private void updateCalimeroStatus(final CCalimeroServiceStatus status) {
		LOGGER.debug("Updating Calimero status indicator: {}", status);
		if (calimeroStatusIndicator == null) {
			return;
		}
		final boolean running = status != null && status.isRunning();
		final boolean enabled = status != null && status.isEnabled();
		final String message = status != null ? status.getMessage() : "Calimero status unavailable";
		calimeroStatusIndicator.setText(message);
		calimeroStatusIndicator.getElement().setAttribute("data-running", String.valueOf(running));
		calimeroStatusIndicator.getElement().setAttribute("data-enabled", String.valueOf(enabled));
		calimeroStatusIndicator.getElement().getClassList().remove("status-running");
		calimeroStatusIndicator.getElement().getClassList().remove("status-stopped");
		calimeroStatusIndicator.getElement().getClassList().remove("status-disabled");
		calimeroStatusIndicator.getElement().getClassList().add(running ? "status-running" : enabled ? "status-stopped" : "status-disabled");
		// Update button based on service status
		// CRITICAL FIX: Button should ALWAYS be enabled (unless readOnly)
		// - If running: User can stop it
		// - If stopped: User can start it
		// The 'enabled' flag only controls AUTO-START, not manual control
		final boolean buttonEnabled = !isReadOnly();
		calimeroStartStopButton.setEnabled(buttonEnabled);
		if (running) {
			// Service is running - show Stop button (ALWAYS enabled)
			calimeroStartStopButton.setText("Stop Calimero");
			calimeroStartStopButton.setIcon(VaadinIcon.STOP.create());
			calimeroStartStopButton.getElement().setProperty("title", "Stop the Calimero HTTP service");
		} else {
			// Service is stopped - show Start button (ALWAYS enabled)
			calimeroStartStopButton.setText("Start Calimero");
			calimeroStartStopButton.setIcon(VaadinIcon.PLAY.create());
			calimeroStartStopButton.getElement().setProperty("title", "Start the Calimero HTTP service");
		}
	}

	private void updateUIFromSettings(final CSystemSettings_Bab settings) {
		checkboxEnableService.setValue(settings.getEnableCalimeroService() != null ? settings.getEnableCalimeroService() : Boolean.FALSE);
		checkboxAutostartService.setValue(calimeroProcessManager.isAutostartEnabled());
		textFieldExecutablePath.setValue(settings.getCalimeroExecutablePath() != null ? settings.getCalimeroExecutablePath() : "");
		textFieldConfigPath.setValue(settings.getCalimeroConfigPath() != null ? settings.getCalimeroConfigPath() : "");
	}
}
