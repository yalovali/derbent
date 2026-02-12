package tech.derbent.bab.dashboard.dashboardproject_bab.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkRoute;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTORouteConfigurationUpdate;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTORouteEntry;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CNetworkRoutingCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.dialog.CDialogEditRouteConfiguration;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.constants.CUIConstants;

/** CComponentNetworkRouting - Component for displaying network routing table and DNS configuration from Calimero server.
 * <p>
 * Displays network routing information for BAB Gateway projects including:
 * <ul>
 * <li>Routing table with destination, gateway, interface, metric, and flags</li>
 * <li>DNS server configuration</li>
 * </ul>
 * <p>
 * Calimero API:
 * <ul>
 * <li>POST /api/request with type="network", operation="getRoutes"</li>
 * <li>POST /api/request with type="network", operation="getDns"</li>
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentNetworkRouting component = new CComponentNetworkRouting(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentNetworkRouting extends CComponentBabBase {

	public static final String ID_DNS_SECTION = "custom-dns-section";
	public static final String ID_EDIT_BUTTON = "custom-routing-edit-button";
	public static final String ID_GRID_ROUTES = "custom-routes-grid";
	public static final String ID_HEADER = "custom-routing-header";
	public static final String ID_REFRESH_BUTTON = "custom-routing-refresh-button";
	public static final String ID_ROOT = "custom-routing-component";
	public static final String ID_TOOLBAR = "custom-routing-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentNetworkRouting.class);
	private static final long serialVersionUID = 1L;
	// UI Components (buttonEdit and buttonRefresh inherited from CComponentBabBase)
	private CVerticalLayout dnsSection;
	private CGrid<CDTONetworkRoute> gridRoutes;

	/** Constructor for network routing component.
	 * @param sessionService the session service */
	public CComponentNetworkRouting(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	/** Apply route configuration via Calimero HTTP API. */
	private void applyRouteConfiguration(final CDTORouteConfigurationUpdate update) {
		LOGGER.info("Applying route configuration: {}", update);
		try {
			// TODO: Implement setRoutes in Calimero client
			// final boolean success = routingClient.applyRouteConfiguration(update);
			// For now, show info message
			CNotificationService.showInfo("Route configuration saved. Apply via Calimero setRoutes (pending implementation)");
			// Refresh display
			refreshComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to apply route configuration", e);
			CNotificationService.showException("Failed to apply route configuration", e);
		}
	}

	@Override
	protected void configureComponent() {
		super.configureComponent();
		createGrid();
		createGrid();
		createDnsSection();
	}

	private void configureGrid() {
		// Destination column
		CGrid.styleColumnHeader(gridRoutes.addComponentColumn(route -> {
			final CSpan destSpan = new CSpan(route.getDestination());
			if (route.isDefaultRoute()) {
				destSpan.getStyle().set("font-weight", "bold");
				destSpan.getStyle().set("color", "var(--lumo-primary-color)");
			}
			return destSpan;
		}).setWidth("180px").setFlexGrow(0).setKey("destination").setSortable(true).setResizable(true), "Destination");
		// Gateway column
		CGrid.styleColumnHeader(gridRoutes.addComponentColumn(route -> {
			final String gateway = route.hasGateway() ? route.getGateway() : "-";
			final CSpan gatewaySpan = new CSpan(gateway);
			if (route.hasGateway()) {
				gatewaySpan.getStyle().set("color", "var(--lumo-success-color)");
			}
			return gatewaySpan;
		}).setWidth("150px").setFlexGrow(0).setKey("gateway").setSortable(true).setResizable(true), "Gateway");
		// Interface column
		CGrid.styleColumnHeader(gridRoutes.addColumn(CDTONetworkRoute::getInterfaceName).setWidth("120px").setFlexGrow(0).setKey("interface")
				.setSortable(true).setResizable(true), "Interface");
		// Metric column
		CGrid.styleColumnHeader(gridRoutes.addColumn(CDTONetworkRoute::getMetric).setWidth("100px").setFlexGrow(0).setKey("metric").setSortable(true)
				.setResizable(true), "Metric");
		// Flags column
		CGrid.styleColumnHeader(gridRoutes.addColumn(CDTONetworkRoute::getFlags).setWidth("100px").setFlexGrow(1).setKey("flags").setSortable(true)
				.setResizable(true), "Flags");
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
		return new CNetworkRoutingCalimeroClient(clientProject);
	}

	/** Create DNS servers section. */
	private void createDnsSection() {
		dnsSection = new CVerticalLayout();
		dnsSection.setId(ID_DNS_SECTION);
		dnsSection.setPadding(false);
		dnsSection.setSpacing(false);
		dnsSection.getStyle().set("gap", CUIConstants.GAP_SMALL).set("padding", "16px").set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("border-radius", "8px").set("background", "var(--lumo-base-color)").set("margin-top", "16px");
		final CH4 dnsHeader = new CH4("DNS Servers");
		dnsHeader.getStyle().set("margin", "0");
		dnsSection.add(dnsHeader);
		add(dnsSection);
	}

	/** Create routing table grid. */
	private void createGrid() {
		gridRoutes = new CGrid<>(CDTONetworkRoute.class);
		gridRoutes.setId(ID_GRID_ROUTES);
		configureGrid();
		gridRoutes.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		gridRoutes.setHeight("350px");
		add(gridRoutes);
	}

	@Override
	protected String getEditButtonId() { return ID_EDIT_BUTTON; }

	@Override
	protected String getHeaderText() { return "Network Routing"; }

	@Override
	protected String getID_ROOT() { // TODO Auto-generated method stub
		return ID_ROOT;
	}

	@Override
	protected boolean hasEditButton() {
		return true;
	}

	/** Load DNS servers and update display. */
	private void loadDnsServers() {
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				return;
			}
			final CNetworkRoutingCalimeroClient routingClient = (CNetworkRoutingCalimeroClient) clientOpt.get();
			final List<String> dnsServers = routingClient.fetchDnsServers();
			while (dnsSection.getComponentCount() > 1) {
				dnsSection.remove(dnsSection.getComponentAt(1));
			}
			if (dnsServers.isEmpty()) {
				final CSpan noDnsSpan = new CSpan("No DNS servers configured");
				noDnsSpan.getStyle().set("color", "var(--lumo-contrast-50pct)").set("font-style", "italic");
				dnsSection.add(noDnsSpan);
			} else {
				dnsServers.forEach((final String dnsServer) -> {
					final CHorizontalLayout dnsRow = new CHorizontalLayout();
					dnsRow.setSpacing(true);
					dnsRow.getStyle().set("gap", CUIConstants.GAP_SMALL);
					final CSpan dnsIcon = new CSpan("•");
					dnsIcon.getStyle().set("color", "var(--lumo-success-color)").set("font-weight", "bold");
					final CSpan dnsAddress = new CSpan(dnsServer);
					dnsAddress.getStyle().set("font-family", "monospace");
					dnsRow.add(dnsIcon, dnsAddress);
					dnsSection.add(dnsRow);
				});
			}
			LOGGER.info("Loaded {} DNS servers", dnsServers.size());
		} catch (final Exception e) {
			LOGGER.error("Failed to load DNS servers: {}", e.getMessage(), e);
		}
	}

	/** Load routing table from Calimero server. */
	private void loadRoutingData() {
		try {
			LOGGER.debug("Loading routing table from Calimero server");
			buttonRefresh.setEnabled(false);
			buttonEdit.setEnabled(false);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				gridRoutes.setItems(Collections.emptyList());
				return;
			}
			hideCalimeroUnavailableWarning();
			final CNetworkRoutingCalimeroClient routingClient = (CNetworkRoutingCalimeroClient) clientOpt.get();
			final List<CDTONetworkRoute> routes = routingClient.fetchRoutes();
			gridRoutes.setItems(routes);
			LOGGER.info("Loaded {} routes", routes.size());
			loadDnsServers();
			CNotificationService.showSuccess("Loaded " + routes.size() + " routes");
		} catch (final Exception e) {
			LOGGER.error("Failed to load routing data: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load routing data", e);
			showCalimeroUnavailableWarning("Failed to load routing data");
			gridRoutes.setItems(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
			buttonEdit.setEnabled(true);
		}
	}

	@Override
	protected void on_buttonEdit_clicked() {
		LOGGER.debug("Edit Routes button clicked");
		openRouteEditDialog();
	}

	/** Open route edit dialog with current configuration. */
	private void openRouteEditDialog() {
		try {
			// Get current routes from grid
			final List<CDTONetworkRoute> allRoutes = gridRoutes.getListDataView().getItems().toList();
			// Extract default gateway
			final String defaultGateway =
					allRoutes.stream().filter(CDTONetworkRoute::isDefaultRoute).findFirst().map(route -> route.getGateway()).orElse("");
			// Filter manual routes (exclude kernel/dhcp flags)
			final List<CDTORouteEntry> manualRoutes = new java.util.ArrayList<>();
			allRoutes.forEach((final CDTONetworkRoute route) -> {
				// Only include non-default routes that are not kernel or link-local
				if (!route.isDefaultRoute() && !route.getFlags().contains("link")) {
					// Parse network and netmask from destination (e.g., "192.168.2.0/24")
					String network = route.getDestination();
					String netmask = "";
					if (network != null && network.contains("/")) {
						final String[] parts = network.split("/");
						network = parts[0];
						netmask = parts.length > 1 ? parts[1] : "";
					}
					final CDTORouteEntry entry = new CDTORouteEntry(network, netmask, route.getGateway() != null ? route.getGateway() : "", true);
					manualRoutes.add(entry);
				}
			});
			// Open dialog
			final CDialogEditRouteConfiguration dialog =
					new CDialogEditRouteConfiguration(defaultGateway, manualRoutes, update -> applyRouteConfiguration(update));
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error opening route edit dialog", e);
			CNotificationService.showException("Failed to open route edit dialog", e);
		}
	}

	@Override
	protected void refreshComponent() {
		loadRoutingData();
	}
}
