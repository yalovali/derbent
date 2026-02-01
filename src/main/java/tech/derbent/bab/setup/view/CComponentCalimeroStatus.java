package tech.derbent.bab.setup.view;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.calimero.service.CCalimeroProcessManager;
import tech.derbent.bab.calimero.service.CCalimeroServiceStatus;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;

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
public class CComponentCalimeroStatus extends CComponentBase<CSystemSettings_Bab> {

	public static final String ID_CARD = "custom-calimero-control-card";
	public static final String ID_ENABLE_CHECKBOX = "custom-calimero-enable-checkbox";
	public static final String ID_EXECUTABLE_PATH_FIELD = "custom-calimero-executable-path";
	public static final String ID_HEADER = "custom-calimero-header";
	public static final String ID_ROOT = "custom-calimero-status-component";
	public static final String ID_START_STOP_BUTTON = "custom-calimero-start-stop-button";
	public static final String ID_STATUS_INDICATOR = "custom-calimero-status-indicator";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentCalimeroStatus.class);
	private static final long serialVersionUID = 1L;
	private final CCalimeroProcessManager calimeroProcessManager;
	private CButton calimeroStartStopButton;
	private CSpan calimeroStatusIndicator;
	private com.vaadin.flow.component.checkbox.Checkbox checkboxEnableService;
	private TextField textFieldExecutablePath;

	/** Constructor for Calimero status component.
	 * @param calimeroProcessManager the Calimero process manager for service control */
	public CComponentCalimeroStatus(final CCalimeroProcessManager calimeroProcessManager) {
		Check.notNull(calimeroProcessManager, "CalimeroProcessManager cannot be null");
		this.calimeroProcessManager = calimeroProcessManager;
		initializeComponents();
	}

	private void configureComponent() {
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");
	}

	private void createCalimeroControlCard() {
		final Div card = new Div();
		card.setId(ID_CARD);
		card.addClassName("calimero-control-card");
		// Compact header with minimal spacing
		final Span title = new Span("Calimero Service");
		title.setId(ID_HEADER);
		title.addClassName("calimero-title");
		title.getStyle().set("font-weight", "600").set("font-size", "1rem").set("margin", "0").set("padding", "0");
		// Removed verbose description to save space - tooltip on checkbox provides context
		// Enable service checkbox
		checkboxEnableService = new com.vaadin.flow.component.checkbox.Checkbox("Enable Calimero Service");
		checkboxEnableService.setId(ID_ENABLE_CHECKBOX);
		checkboxEnableService.addValueChangeListener(event -> on_enableServiceChanged(event.getValue()));
		checkboxEnableService.getElement().setProperty("title",
				"Automatically start and manage Calimero HTTP server. Restart service after updating executable path.");
		// Executable path field
		textFieldExecutablePath = new TextField("Calimero Executable Path");
		textFieldExecutablePath.setId(ID_EXECUTABLE_PATH_FIELD);
		textFieldExecutablePath.setWidth("100%");
		textFieldExecutablePath.setMaxLength(500);
		textFieldExecutablePath.setPlaceholder("~/git/calimero/build/calimero");
		textFieldExecutablePath.addValueChangeListener(event -> on_executablePathChanged(event.getValue()));
		textFieldExecutablePath.getElement().setProperty("title", "Full path to the Calimero executable binary");
		// Status indicator
		calimeroStatusIndicator = new CSpan("Calimero status unavailable");
		calimeroStatusIndicator.setId(ID_STATUS_INDICATOR);
		calimeroStatusIndicator.addClassName("calimero-status-chip");
		// Start/Stop button - changes based on service status
		calimeroStartStopButton = CButton.createPrimary("Start Calimero", VaadinIcon.PLAY.create(), event -> on_actionStartStopCalimeroService());
		calimeroStartStopButton.setId(ID_START_STOP_BUTTON);
		calimeroStartStopButton.getElement().setProperty("title", "Start the Calimero HTTP service");
		final HorizontalLayout statusActions = new HorizontalLayout(calimeroStatusIndicator, calimeroStartStopButton);
		statusActions.addClassName("calimero-actions");
		statusActions.setSpacing(true);
		statusActions.setPadding(false);
		// Compact card layout without description paragraph
		card.add(title, checkboxEnableService, textFieldExecutablePath, statusActions);
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
						() -> forceRestart ? calimeroProcessManager.restartCalimeroService() : calimeroProcessManager.startCalimeroServiceIfEnabled())
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
				CompletableFuture.runAsync(() -> calimeroProcessManager.stopCalimeroService()).whenComplete((result, error) -> ui.access(() -> {
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
		textFieldExecutablePath.setValue(settings.getCalimeroExecutablePath() != null ? settings.getCalimeroExecutablePath() : "");
	}
}
