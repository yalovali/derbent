package tech.derbent.bab.setup.view;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
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
 * 
 * // Bind to entity
 * component.setValue(settings);
 * 
 * // Listen for changes (replaces callback pattern)
 * component.addValueChangeListener(event -> {
 *     CSystemSettings_Bab newSettings = event.getValue();
 *     // Save settings...
 * });
 * 
 * // Or use with Vaadin Binder
 * Binder<CSystemSettings_Bab> binder = new Binder<>();
 * binder.forField(component).bind(CSystemSettings_Bab::get, CSystemSettings_Bab::set);
 * </pre>
 * 
 * @see CComponentBase
 * @see com.vaadin.flow.component.HasValue
 */
public class CComponentCalimeroStatus extends CComponentBase<CSystemSettings_Bab> {
	public static final String ID_CARD = "custom-calimero-control-card";
	public static final String ID_ENABLE_CHECKBOX = "custom-calimero-enable-checkbox";
	public static final String ID_EXECUTABLE_PATH_FIELD = "custom-calimero-executable-path";
	public static final String ID_HEADER = "custom-calimero-header";
	public static final String ID_RESTART_BUTTON = "custom-calimero-restart-button";
	public static final String ID_ROOT = "custom-calimero-status-component";
	public static final String ID_STATUS_INDICATOR = "custom-calimero-status-indicator";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentCalimeroStatus.class);
	private static final long serialVersionUID = 1L;
	private final CCalimeroProcessManager calimeroProcessManager;
	private CSpan calimeroStatusIndicator;
	private CButton calimeroRestartButton;
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
		final var title = new CH3("Calimero Service");
		title.setId(ID_HEADER);
		final var description = new Paragraph("Manage the Calimero HTTP process that powers BAB dashboard interfaces. "
				+ "Restart the service after updating the executable path.");
		description.addClassName("calimero-description");
		// Enable service checkbox
		checkboxEnableService = new com.vaadin.flow.component.checkbox.Checkbox("Enable Calimero Service");
		checkboxEnableService.setId(ID_ENABLE_CHECKBOX);
		checkboxEnableService.addValueChangeListener(event -> on_enableServiceChanged(event.getValue()));
		checkboxEnableService.getElement().setProperty("title", "Automatically start and manage Calimero HTTP server on application startup");
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
		// Restart button
		calimeroRestartButton = CButton.createPrimary("Restart Calimero", VaadinIcon.REFRESH.create(), event -> on_actionRestartCalimeroService());
		calimeroRestartButton.setId(ID_RESTART_BUTTON);
		calimeroRestartButton.getElement().setProperty("title", "Restart the Calimero HTTP service");
		final HorizontalLayout statusActions = new HorizontalLayout(calimeroStatusIndicator, calimeroRestartButton);
		statusActions.addClassName("calimero-actions");
		statusActions.setSpacing(true);
		statusActions.setPadding(false);
		card.add(title, description, checkboxEnableService, textFieldExecutablePath, statusActions);
		add(card);
	}

	/** Ensure Calimero service is running (or restart if requested).
	 * @param forceRestart if true, force restart even if already running */
	public void ensureCalimeroRunningAsync(final boolean forceRestart) {
		if ((calimeroProcessManager == null) || (calimeroStatusIndicator == null)) {
			return;
		}
		final UI ui = getUI().orElse(null);
		if (ui == null) {
			return;
		}
		final String pendingText = forceRestart ? "Restarting Calimero service..." : "Checking Calimero service...";
		calimeroStatusIndicator.setText(pendingText);
		if (calimeroRestartButton != null) {
			calimeroRestartButton.setEnabled(false);
		}
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

	private void on_actionRestartCalimeroService() {
		if ((calimeroProcessManager == null) || (calimeroRestartButton == null)) {
			CNotificationService.showWarning("Calimero process manager is not available in this environment");
			return;
		}
		final UI ui = getUI().orElse(null);
		if (ui == null) {
			CNotificationService.showWarning("UI not available - cannot restart Calimero service");
			return;
		}
		ui.access(() -> ensureCalimeroRunningAsync(true));
	}

	private void on_enableServiceChanged(final Boolean enabled) {
		final CSystemSettings_Bab currentValue = getValue();
		if (currentValue != null) {
			currentValue.setEnableCalimeroService(enabled);
			LOGGER.debug("Calimero service enabled changed to: {}", enabled);
			// Notify via value change event (standard pattern)
			updateValueFromClient(currentValue);
		}
	}

	private void on_executablePathChanged(final String path) {
		final CSystemSettings_Bab currentValue = getValue();
		if (currentValue != null) {
			currentValue.setCalimeroExecutablePath(path);
			LOGGER.debug("Calimero executable path changed to: {}", path);
			// Notify via value change event (standard pattern)
			updateValueFromClient(currentValue);
		}
	}

	/** Override from CComponentBase - Update UI when value changes.
	 * <p>
	 * Called automatically when setValue() is called or when updateValueFromClient() fires. Updates UI components to reflect the new entity state.
	 * @param oldValue the previous settings value
	 * @param newValue the new settings value
	 * @param fromClient true if change originated from UI interaction */
	@Override
	protected void onValueChanged(final CSystemSettings_Bab oldValue, final CSystemSettings_Bab newValue, final boolean fromClient) {
		if (newValue != null) {
			updateUIFromSettings(newValue);
			if (!fromClient) {
				// Only refresh on programmatic changes (not from user input)
				refreshCalimeroStatus();
				ensureCalimeroRunningAsync(false);
			}
		}
	}

	/** Refresh Calimero status indicator with current service state. */
	public void refreshCalimeroStatus() {
		if (calimeroStatusIndicator == null) {
			return;
		}
		if (calimeroProcessManager == null) {
			updateCalimeroStatus(CCalimeroServiceStatus.of(false, false, "Calimero manager unavailable"));
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

	/** Override from CComponentBase - Support readOnly mode.
	 * <p>
	 * When readOnly is true, disables all input fields and action buttons. When false, enables them based on service state.
	 * @param readOnly true to make component read-only */
	@Override
	public void setReadOnly(final boolean readOnly) {
		super.setReadOnly(readOnly);
		if (checkboxEnableService != null) {
			checkboxEnableService.setReadOnly(readOnly);
		}
		if (textFieldExecutablePath != null) {
			textFieldExecutablePath.setReadOnly(readOnly);
		}
		if (calimeroRestartButton != null) {
			final boolean enabled = !readOnly && getValue() != null && Boolean.TRUE.equals(getValue().getEnableCalimeroService());
			calimeroRestartButton.setEnabled(enabled);
		}
	}

	private void updateCalimeroStatus(final CCalimeroServiceStatus status) {
		if (calimeroStatusIndicator == null) {
			return;
		}
		final boolean running = (status != null) && status.isRunning();
		final boolean enabled = (status != null) && status.isEnabled();
		final String message = status != null ? status.getMessage() : "Calimero status unavailable";
		calimeroStatusIndicator.setText(message);
		calimeroStatusIndicator.getElement().setAttribute("data-running", String.valueOf(running));
		calimeroStatusIndicator.getElement().setAttribute("data-enabled", String.valueOf(enabled));
		calimeroStatusIndicator.getElement().getClassList().remove("status-running");
		calimeroStatusIndicator.getElement().getClassList().remove("status-stopped");
		calimeroStatusIndicator.getElement().getClassList().remove("status-disabled");
		calimeroStatusIndicator.getElement().getClassList().add(running ? "status-running" : enabled ? "status-stopped" : "status-disabled");
		if (calimeroRestartButton != null) {
			final boolean buttonEnabled = enabled && !isReadOnly();
			calimeroRestartButton.setEnabled(buttonEnabled);
			final String tooltip = enabled ? "Restart the Calimero HTTP service" : "Enable Calimero service to allow restarts";
			calimeroRestartButton.getElement().setProperty("title", tooltip);
		}
	}

	private void updateUIFromSettings(final CSystemSettings_Bab settings) {
		checkboxEnableService.setValue(settings.getEnableCalimeroService() != null ? settings.getEnableCalimeroService() : Boolean.FALSE);
		textFieldExecutablePath.setValue(settings.getCalimeroExecutablePath() != null ? settings.getCalimeroExecutablePath() : "");
	}
}
