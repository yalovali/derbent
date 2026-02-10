package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterface;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterfaceIpUpdate;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CNetworkInterfaceCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.dialog.CDialogEditInterfaceIp;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.project.service.CProject_BabService;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.api.session.service.ISessionService;

/** CComponentInterfaceList - Component for displaying network interfaces from Calimero server.
 * <p>
 * Displays network interfaces for BAB Gateway projects with real-time data from Calimero HTTP API. Uses the project's HTTP client to fetch interface
 * information.
 * <p>
 * Calimero API: POST /api/request with type="network", operation="getInterfaces"
 * <p>
 * Usage:
 *
 * <pre>
 *
 * CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
 * </pre>
 */
public class CComponentInterfaceList extends CComponentBabBase {

	public static final String ID_GRID = "custom-interfaces-grid";
	public static final String ID_HEADER = "custom-interfaces-header";
	public static final String ID_REFRESH_BUTTON = "custom-interfaces-refresh-button";
	public static final String ID_ROOT = "custom-interfaces-component";
	public static final String ID_TOOLBAR = "custom-interfaces-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentInterfaceList.class);
	private static final long serialVersionUID = 1L;
	// buttonRefresh and buttonEdit inherited from CComponentBabBase
	private CGrid<CDTONetworkInterface> grid;

	/** Constructor for interface list component.
	 * @param sessionService the session service */
	public CComponentInterfaceList(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createGrid();
	}

	private void configureGrid() {
		// 1. IPv4 Address column (FIRST - most important)
		CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
			final CSpan ipSpan = new CSpan(iface.getIpv4Display());
			ipSpan.getStyle().set("font-weight", "bold");
			ipSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
			return ipSpan;
		}).setWidth("150px").setFlexGrow(0).setKey("ipv4").setSortable(true).setResizable(true), "IP Address");
		// 2. Interface Name
		CGrid.styleColumnHeader(
				grid.addColumn(CDTONetworkInterface::getName).setWidth("100px").setFlexGrow(0).setKey("name").setSortable(true).setResizable(true),
				"Interface");
		// 3. Status column with color coding
		CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
			final CSpan statusSpan = new CSpan(iface.getStatus().toUpperCase());
			if (iface.isUp()) {
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
				statusSpan.getStyle().set("font-weight", "bold");
			} else {
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return statusSpan;
		}).setWidth("80px").setFlexGrow(0).setKey("status").setSortable(true).setResizable(true), "Status");
		// 4. Configuration (DHCP/Manual)
		CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
			final Boolean dhcp4 = iface.getDhcp4();
			final String config = dhcp4 != null && dhcp4 ? "DHCP" : "Manual";
			final CSpan configSpan = new CSpan(config);
			if (dhcp4 != null && dhcp4) {
				configSpan.getStyle().set("color", "var(--lumo-primary-color)");
			}
			return configSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("config").setSortable(true).setResizable(true), "Configuration");
		// 5. MAC Address
		CGrid.styleColumnHeader(grid.addColumn(CDTONetworkInterface::getMacAddress).setWidth("160px").setFlexGrow(0).setKey("macAddress")
				.setSortable(true).setResizable(true), "MAC Address");
		// 6. Gateway
		CGrid.styleColumnHeader(grid.addColumn(CDTONetworkInterface::getIpv4GatewayDisplay).setWidth("140px").setFlexGrow(0).setKey("gateway")
				.setSortable(true).setResizable(true), "Gateway");
		// 7. Type
		CGrid.styleColumnHeader(
				grid.addColumn(CDTONetworkInterface::getType).setWidth("90px").setFlexGrow(0).setKey("type").setSortable(true).setResizable(true),
				"Type");
		// 8. MTU
		CGrid.styleColumnHeader(
				grid.addColumn(CDTONetworkInterface::getMtu).setWidth("70px").setFlexGrow(0).setKey("mtu").setSortable(true).setResizable(true),
				"MTU");
		// DNS column removed - 127.0.0.53 is loopback and not useful
		// IPv6 columns removed per user request
	}

	/** Factory method for refresh button. Subclasses can override to customize button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CNetworkInterfaceCalimeroClient(clientProject);
	}

	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CDTONetworkInterface.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.addSelectionListener(event -> buttonEdit.setEnabled(event.getFirstSelectedItem().isPresent()));
		grid.setHeight("400px");
		add(grid);
	}

	@Override
	protected String getEditButtonId() { return "custom-interfaces-edit-button"; }

	@Override
	protected String getHeaderText() { return "Network Interfaces"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	@Override
	protected boolean hasEditButton() {
		return true;
	}

	@Override
	protected void on_buttonEdit_clicked() {
		LOGGER.debug("Edit button clicked");
		openEditDialog();
	}

	private void openEditDialog() {
		final CDTONetworkInterface selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			CNotificationService.showWarning("Select an interface first");
			return;
		}
		final CDTONetworkInterface target = selected;
		final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
		if (clientOpt.isEmpty()) {
			CNotificationService.showWarning("Unable to resolve Calimero client for editing");
			return;
		}
		final CNetworkInterfaceCalimeroClient interfaceClient = (CNetworkInterfaceCalimeroClient) clientOpt.get();
		interfaceClient.fetchIpConfiguration(target.getName()).ifPresent(target::setIpConfiguration);
		final CDialogEditInterfaceIp dialog = new CDialogEditInterfaceIp(target, update -> performIpUpdate(update));
		dialog.open();
	}

	private void performIpUpdate(final CDTONetworkInterfaceIpUpdate update) {
		final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
		if (clientOpt.isEmpty()) {
			return;
		}
		final CNetworkInterfaceCalimeroClient client = (CNetworkInterfaceCalimeroClient) clientOpt.get();
		final CCalimeroResponse response = client.updateInterfaceIp(update);
		if (response.isSuccess()) {
			final String status = response.getDataField("status", "configured");
			final String message = response.getDataField("message", "Interface updated");
			CNotificationService.showSuccess(message + " (" + status + ")");
			refreshComponent();
		} else {
			CNotificationService.showError("Failed to update IP: " + response.getErrorMessage());
		}
	}

	@Override
	protected void refreshComponent() {
		try {
			LOGGER.info("🔄 CComponentInterfaceList: Refreshing network interfaces");
			// Get active BAB project
			final Optional<CProject_Bab> projectOpt = sessionService.getActiveProject().map(p -> (CProject_Bab) p);
			if (projectOpt.isEmpty()) {
				showCalimeroUnavailableWarning("No active BAB project");
				grid.setItems(Collections.emptyList());
				clearSummary();
				return;
			}
			final CProject_Bab project = projectOpt.get();
			// Get service
			final CProject_BabService service = CSpringContext.getBean(CProject_BabService.class);
			// Check if cache is empty
			final String cachedJson = project.getInterfacesJson();
			if (cachedJson == null || cachedJson.isBlank() || "{}".equals(cachedJson)) {
				showCalimeroUnavailableWarning(
						"No interface data available. Please use the Refresh button in the toolbar to load data from Calimero.");
				grid.setItems(Collections.emptyList());
				clearSummary();
				return;
			}
			// Parse from cached JSON
			final List<CDTONetworkInterface> interfaces = service.getNetworkInterfaces(project);
			grid.setItems(interfaces);
			// Update summary
			updateInterfaceSummary(interfaces);
			// Hide warning if data loaded successfully
			if (!interfaces.isEmpty()) {
				hideCalimeroUnavailableWarning();
				LOGGER.info("✅ CComponentInterfaceList: Loaded {} network interfaces (last updated: {})", interfaces.size(),
						project.getInterfacesLastUpdated());
			} else {
				showCalimeroUnavailableWarning(
						"No interface data available. Please use the Refresh button in the toolbar to load data from Calimero.");
			}
		} catch (final Exception e) {
			LOGGER.error("❌ CComponentInterfaceList: Failed to refresh: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load network interfaces", e);
			showCalimeroUnavailableWarning(
					"Calimero service not available - Interface data cannot be loaded. Please check that the BAB Gateway is running and accessible.");
			grid.setItems(List.of());
			clearSummary();
		} finally {
			buttonRefresh.setEnabled(true);
			buttonEdit.setEnabled(true);
		}
	}

	/** Update summary label with interface statistics.
	 * <p>
	 * Format: "N interfaces (X up, Y down)"
	 * @param interfaces List of interfaces to analyze */
	private void updateInterfaceSummary(final List<CDTONetworkInterface> interfaces) {
		if (interfaces == null || interfaces.isEmpty()) {
			clearSummary();
			return;
		}
		// Count interface states (status field contains "up", "down", etc.)
		final long up = interfaces.stream().filter(iface -> "up".equalsIgnoreCase(iface.getStatus())).count();
		final long down = interfaces.size() - up;
		// Build summary string
		final StringBuilder summary = new StringBuilder();
		summary.append(interfaces.size()).append(" interface");
		if (interfaces.size() != 1) {
			summary.append("s");
		}
		// Add state breakdown
		if (interfaces.size() > 1) {
			summary.append(" (");
			if (up > 0) {
				summary.append(up).append(" up");
			}
			if (down > 0) {
				if (up > 0) {
					summary.append(", ");
				}
				summary.append(down).append(" down");
			}
			summary.append(")");
		}
		updateSummary(summary.toString());
	}
}
