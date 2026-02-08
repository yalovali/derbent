package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTOSystemService;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CSystemServiceCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentSystemServices - Component for displaying systemd services from Calimero server.
 * <p>
 * Displays system services for BAB Gateway projects with real-time data from Calimero HTTP API. Shows service information including:
 * <ul>
 * <li>Service name</li>
 * <li>Description</li>
 * <li>Load state (loaded, not-found, etc.)</li>
 * <li>Active state (active, inactive, failed)</li>
 * <li>Sub-state (running, exited, dead, etc.)</li>
 * <li>Unit file state (enabled, disabled, static)</li>
 * </ul>
 * <p>
 * Features:
 * <ul>
 * <li>Start/Stop/Restart services</li>
 * <li>Enable/Disable boot auto-start</li>
 * <li>Reload service configuration</li>
 * <li>Color-coded status indicators</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="systemservices"
 * <p>
 * Operations: list, start, stop, restart, reload, enable, disable
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentSystemServices component = new CComponentSystemServices(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentSystemServices extends CComponentBabBase {

	public static final String ID_DISABLE_BUTTON = "custom-services-disable-button";
	public static final String ID_ENABLE_BUTTON = "custom-services-enable-button";
	public static final String ID_GRID = "custom-services-grid";
	public static final String ID_HEADER = "custom-services-header";
	public static final String ID_REFRESH_BUTTON = "custom-services-refresh-button";
	public static final String ID_RESTART_BUTTON = "custom-services-restart-button";
	public static final String ID_ROOT = "custom-services-component";
	// Action button IDs
	public static final String ID_START_BUTTON = "custom-services-start-button";
	public static final String ID_STOP_BUTTON = "custom-services-stop-button";
	public static final String ID_TOOLBAR = "custom-services-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSystemServices.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonDisable;
	private CButton buttonEnable;
	private CButton buttonRestart;
	// Action buttons
	private CButton buttonStart;
	private CButton buttonStop;
	// UI Components
	private CGrid<CDTOSystemService> grid;
	// Selected service
	private CDTOSystemService selectedService;
	private CSystemServiceCalimeroClient serviceClient;

	/** Constructor for system services component.
	 * @param sessionService the session service */
	public CComponentSystemServices(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	private void configureGrid() {
		// Service name column
		CGrid.styleColumnHeader(
				grid.addColumn(CDTOSystemService::getName).setWidth("220px").setFlexGrow(0).setKey("name").setSortable(true).setResizable(true),
				"Service");
		// Description column (flexible)
		CGrid.styleColumnHeader(grid.addColumn(CDTOSystemService::getDescription).setWidth("300px").setFlexGrow(1).setKey("description")
				.setSortable(true).setResizable(true), "Description");
		// Load state column
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan loadSpan = new CSpan(service.getLoadState());
			if (service.isLoaded()) {
				loadSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				loadSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return loadSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("loadState").setSortable(true).setResizable(true), "Load");
		// Active state column with color coding
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan activeSpan = new CSpan(service.getActiveState());
			if (service.isActive()) {
				activeSpan.getStyle().set("color", "var(--lumo-success-color)").set("font-weight", "bold");
			} else if (service.isFailed()) {
				activeSpan.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
			} else {
				activeSpan.getStyle().set("color", "var(--lumo-contrast-50pct)");
			}
			return activeSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("activeState").setSortable(true).setResizable(true), "State");
		// Sub-state column
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan subSpan = new CSpan(service.getSubState());
			if (service.isRunning()) {
				subSpan.getStyle().set("color", "var(--lumo-success-color)");
			}
			return subSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("subState").setSortable(true).setResizable(true), "Sub-State");
		// Unit file state column
		CGrid.styleColumnHeader(grid.addComponentColumn(service -> {
			final CSpan unitSpan = new CSpan(service.getUnitFileState());
			if (service.isEnabled()) {
				unitSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				unitSpan.getStyle().set("color", "var(--lumo-contrast-50pct)");
			}
			return unitSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("unitFileState").setSortable(true).setResizable(true), "Enabled");
	}

	/** Create action buttons for service control.
	 * <p>
	 * Buttons are disabled by default and enabled when a service is selected. */
	private void createActionButtons() {
		// Start button - green theme
		buttonStart = new CButton("Start", VaadinIcon.PLAY.create());
		buttonStart.setId(ID_START_BUTTON);
		buttonStart.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
		buttonStart.setEnabled(false);
		buttonStart.addClickListener(e -> on_buttonStart_clicked());
		// Stop button - red theme
		buttonStop = new CButton("Stop", VaadinIcon.STOP.create());
		buttonStop.setId(ID_STOP_BUTTON);
		buttonStop.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
		buttonStop.setEnabled(false);
		buttonStop.addClickListener(e -> on_buttonStop_clicked());
		// Restart button - primary theme
		buttonRestart = new CButton("Restart", VaadinIcon.ROTATE_RIGHT.create());
		buttonRestart.setId(ID_RESTART_BUTTON);
		buttonRestart.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonRestart.setEnabled(false);
		buttonRestart.addClickListener(e -> on_buttonRestart_clicked());
		// Enable button - success theme
		buttonEnable = new CButton("Enable Boot", VaadinIcon.CHECK_CIRCLE.create());
		buttonEnable.setId(ID_ENABLE_BUTTON);
		buttonEnable.addThemeVariants(ButtonVariant.LUMO_SMALL);
		buttonEnable.setEnabled(false);
		buttonEnable.addClickListener(e -> on_buttonEnable_clicked());
		// Disable button - contrast theme
		buttonDisable = new CButton("Disable Boot", VaadinIcon.CLOSE_CIRCLE.create());
		buttonDisable.setId(ID_DISABLE_BUTTON);
		buttonDisable.addThemeVariants(ButtonVariant.LUMO_SMALL);
		buttonDisable.setEnabled(false);
		buttonDisable.addClickListener(e -> on_buttonDisable_clicked());
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CSystemServiceCalimeroClient(clientProject);
	}

	/** Create custom toolbar with action buttons.
	 * <p>
	 * Layout: [Refresh] | [Start] [Stop] [Restart] | [Enable Boot] [Disable Boot] */
	private void createCustomToolbar() {
		final CHorizontalLayout toolbarLayout = createStandardToolbar();
		// Create action buttons
		createActionButtons();
		// Add separator
		final CSpan separator = new CSpan("|");
		separator.getStyle().set("color", "var(--lumo-contrast-50pct)").set("margin", "0 8px");
		// Add action buttons to toolbar
		toolbarLayout.add(separator);
		toolbarLayout.add(buttonStart, buttonStop, buttonRestart);
		final CSpan separator2 = new CSpan("|");
		separator2.getStyle().set("color", "var(--lumo-contrast-50pct)").set("margin", "0 8px");
		toolbarLayout.add(separator2);
		toolbarLayout.add(buttonEnable, buttonDisable);
		add(toolbarLayout);
	}

	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CDTOSystemService.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("500px");
		// Add selection listener to enable/disable action buttons
		grid.asSingleSelect().addValueChangeListener(event -> {
			selectedService = event.getValue();
			updateActionButtonStates();
		});
		add(grid);
	}

	@Override
	protected String getHeaderText() { return "System Services"; }

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		add(createHeader());
		createCustomToolbar(); // Use custom toolbar with action buttons
		createGrid();
		loadServices();
	}

	/** Load system services from Calimero server. */
	private void loadServices() {
		try {
			LOGGER.debug("Loading system services from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				grid.setItems(Collections.emptyList());
				clearSummary(); // Hide summary when unavailable
				return;
			}
			hideCalimeroUnavailableWarning();
			serviceClient = (CSystemServiceCalimeroClient) clientOpt.get();
			final List<CDTOSystemService> services = serviceClient.fetchServices();
			grid.setItems(services);
			// Update summary with service counts
			updateServiceSummary(services);
			LOGGER.info("Loaded {} system services", services.size());
			CNotificationService.showSuccess("Loaded " + services.size() + " services");
		} catch (final Exception e) {
			LOGGER.error("Failed to load system services: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load system services", e);
			showCalimeroUnavailableWarning("Failed to load system services");
			grid.setItems(Collections.emptyList());
			clearSummary(); // Hide summary on error
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}

	/** Event handler for Disable button click.
	 * <p>
	 * Disables the selected systemd service from boot auto-start via Calimero API. */
	private void on_buttonDisable_clicked() {
		if (selectedService == null) {
			LOGGER.warn("No service selected for disable operation");
			return;
		}
		LOGGER.info("User requested disable for service: {}", selectedService.getName());
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				return;
			}
			serviceClient = (CSystemServiceCalimeroClient) clientOpt.get();
			final boolean success = serviceClient.disableService(selectedService.getName());
			if (success) {
				CNotificationService.showSuccess("Service disabled from boot: " + selectedService.getName());
				refreshComponent(); // Reload to show new status
			}
		} catch (final Exception e) {
			LOGGER.error("Error disabling service: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to disable service", e);
		}
	}

	/** Event handler for Enable button click.
	 * <p>
	 * Enables the selected systemd service for boot auto-start via Calimero API. */
	private void on_buttonEnable_clicked() {
		if (selectedService == null) {
			LOGGER.warn("No service selected for enable operation");
			return;
		}
		LOGGER.info("User requested enable for service: {}", selectedService.getName());
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				return;
			}
			serviceClient = (CSystemServiceCalimeroClient) clientOpt.get();
			final boolean success = serviceClient.enableService(selectedService.getName());
			if (success) {
				CNotificationService.showSuccess("Service enabled for boot: " + selectedService.getName());
				refreshComponent(); // Reload to show new status
			}
		} catch (final Exception e) {
			LOGGER.error("Error enabling service: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to enable service", e);
		}
	}

	/** Event handler for Restart button click.
	 * <p>
	 * Restarts the selected systemd service via Calimero API. */
	private void on_buttonRestart_clicked() {
		if (selectedService == null) {
			LOGGER.warn("No service selected for restart operation");
			return;
		}
		LOGGER.info("User requested restart for service: {}", selectedService.getName());
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				return;
			}
			serviceClient = (CSystemServiceCalimeroClient) clientOpt.get();
			final boolean success = serviceClient.restartService(selectedService.getName());
			if (success) {
				CNotificationService.showSuccess("Service restarted: " + selectedService.getName());
				refreshComponent(); // Reload to show new status
			}
		} catch (final Exception e) {
			LOGGER.error("Error restarting service: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to restart service", e);
		}
	}

	/** Event handler for Start button click.
	 * <p>
	 * Starts the selected systemd service via Calimero API. */
	private void on_buttonStart_clicked() {
		if (selectedService == null) {
			LOGGER.warn("No service selected for start operation");
			return;
		}
		LOGGER.info("User requested start for service: {}", selectedService.getName());
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				return;
			}
			serviceClient = (CSystemServiceCalimeroClient) clientOpt.get();
			final boolean success = serviceClient.startService(selectedService.getName());
			if (success) {
				CNotificationService.showSuccess("Service started: " + selectedService.getName());
				refreshComponent(); // Reload to show new status
			}
		} catch (final Exception e) {
			LOGGER.error("Error starting service: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to start service", e);
		}
	}

	/** Event handler for Stop button click.
	 * <p>
	 * Stops the selected systemd service via Calimero API. */
	private void on_buttonStop_clicked() {
		if (selectedService == null) {
			LOGGER.warn("No service selected for stop operation");
			return;
		}
		LOGGER.info("User requested stop for service: {}", selectedService.getName());
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				return;
			}
			serviceClient = (CSystemServiceCalimeroClient) clientOpt.get();
			final boolean success = serviceClient.stopService(selectedService.getName());
			if (success) {
				CNotificationService.showSuccess("Service stopped: " + selectedService.getName());
				refreshComponent(); // Reload to show new status
			}
		} catch (final Exception e) {
			LOGGER.error("Error stopping service: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to stop service", e);
		}
	}

	@Override
	protected void refreshComponent() {
		loadServices();
	}

	/** Update action button states based on selected service.
	 * <p>
	 * Logic:
	 * <ul>
	 * <li>Start: Enabled if service inactive/failed</li>
	 * <li>Stop: Enabled if service active/running</li>
	 * <li>Restart: Always enabled if service selected</li>
	 * <li>Enable: Enabled if service not enabled</li>
	 * <li>Disable: Enabled if service enabled</li>
	 * </ul>
	 */
	private void updateActionButtonStates() {
		if (selectedService == null) {
			// No selection - disable all buttons
			buttonStart.setEnabled(false);
			buttonStop.setEnabled(false);
			buttonRestart.setEnabled(false);
			buttonEnable.setEnabled(false);
			buttonDisable.setEnabled(false);
			return;
		}
		// Start button - enabled if service is not active
		buttonStart.setEnabled(!selectedService.isActive());
		// Stop button - enabled if service is active
		buttonStop.setEnabled(selectedService.isActive());
		// Restart button - always enabled for loaded services
		buttonRestart.setEnabled(selectedService.isLoaded());
		// Enable/Disable buttons - toggle based on current state
		buttonEnable.setEnabled(!selectedService.isEnabled());
		buttonDisable.setEnabled(selectedService.isEnabled());
	}

	/** Update summary label with service statistics.
	 * <p>
	 * Format: "N services (X running, Y stopped, Z failed)"
	 * @param services List of services to analyze */
	private void updateServiceSummary(final List<CDTOSystemService> services) {
		if (services == null || services.isEmpty()) {
			clearSummary();
			return;
		}
		// Count service states
		final long running = services.stream().filter(CDTOSystemService::isRunning).count();
		final long active = services.stream().filter(CDTOSystemService::isActive).count();
		final long failed = services.stream().filter(CDTOSystemService::isFailed).count();
		final long inactive = services.size() - active;
		// Build summary string
		final StringBuilder summary = new StringBuilder();
		summary.append(services.size()).append(" service");
		if (services.size() != 1) {
			summary.append("s");
		}
		// Add state breakdown if there are multiple states
		if (services.size() > 1) {
			summary.append(" (");
			final List<String> parts = new ArrayList<>();
			if (running > 0) {
				parts.add(running + " running");
			}
			if (inactive > 0) {
				parts.add(inactive + " stopped");
			}
			if (failed > 0) {
				parts.add(failed + " failed");
			}
			summary.append(String.join(", ", parts));
			summary.append(")");
		}
		updateSummary(summary.toString());
	}
}
