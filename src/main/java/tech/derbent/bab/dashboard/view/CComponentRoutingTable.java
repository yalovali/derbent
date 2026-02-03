package tech.derbent.bab.dashboard.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dto.CNetworkRoute;
import tech.derbent.bab.dashboard.dto.CRouteConfigurationUpdate;
import tech.derbent.bab.dashboard.dto.CRouteEntry;
import tech.derbent.bab.dashboard.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.service.CNetworkRoutingCalimeroClient;
import tech.derbent.bab.dashboard.view.dialog.CDialogEditRouteConfiguration;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/** CComponentRoutingTable - Component for displaying network routing table from Calimero server.
 * <p>
 * Displays routing table for BAB Gateway projects with real-time data from Calimero HTTP API. Shows routing information including:
 * <ul>
 * <li>Destination network/host</li>
 * <li>Gateway address</li>
 * <li>Network interface</li>
 * <li>Route metric (priority)</li>
 * <li>Route flags (UG, UH, etc.)</li>
 * </ul>
 * <p>
 * Calimero API: POST /api/request with type="network", operation="getRoutes"
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentRoutingTable component = new CComponentRoutingTable(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentRoutingTable extends CComponentBabBase {
	public static final String ID_EDIT_BUTTON = "custom-routing-edit-button";
	public static final String ID_GRID = "custom-routing-grid";
	public static final String ID_HEADER = "custom-routing-header";
	public static final String ID_REFRESH_BUTTON = "custom-routing-refresh-button";
	public static final String ID_ROOT = "custom-routing-component";
	public static final String ID_TOOLBAR = "custom-routing-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentRoutingTable.class);
	private static final long serialVersionUID = 1L;
	// buttonEdit and buttonRefresh inherited from CComponentBabBase
	private CGrid<CNetworkRoute> grid;

	/** Constructor for routing table component.
	 * @param sessionService the session service */
	public CComponentRoutingTable(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	/** Apply route configuration via Calimero API. */
	private void applyRouteConfiguration(final CRouteConfigurationUpdate update) {
		try {
			LOGGER.warn("Route configuration apply not yet implemented - needs Calimero server support");
			CNotificationService.showWarning("Route configuration will be implemented soon. Default gateway: " + update.getDefaultGateway());
			refreshComponent();
		} catch (final Exception e) {
			LOGGER.error("Failed to apply route configuration", e);
			CNotificationService.showException("Failed to apply route configuration", e);
		}
	}

	private void configureGrid() {
		// Destination column with default route highlighting
		CGrid.styleColumnHeader(grid.addComponentColumn(route -> {
			final CSpan destSpan = new CSpan(route.getDestination());
			if (route.isDefaultRoute()) {
				destSpan.getStyle().set("font-weight", "bold").set("color", "var(--lumo-primary-color)").set("font-size", "1.05rem");
			}
			return destSpan;
		}).setWidth("200px").setFlexGrow(0).setKey("destination").setSortable(true).setResizable(true), "Destination");
		// Gateway column with presence indicator
		CGrid.styleColumnHeader(grid.addComponentColumn(route -> {
			final String gateway = route.hasGateway() ? route.getGateway() : "-";
			final CSpan gatewaySpan = new CSpan(gateway);
			if (route.hasGateway()) {
				gatewaySpan.getStyle().set("color", "var(--lumo-success-color)").set("font-weight", "600");
			} else {
				gatewaySpan.getStyle().set("color", "var(--lumo-contrast-50pct)");
			}
			return gatewaySpan;
		}).setWidth("170px").setFlexGrow(0).setKey("gateway").setSortable(true).setResizable(true), "Gateway");
		// Interface column
		CGrid.styleColumnHeader(grid.addColumn(CNetworkRoute::getInterfaceName).setWidth("130px").setFlexGrow(0).setKey("interface").setSortable(true)
				.setResizable(true), "Interface");
		// Metric column
		CGrid.styleColumnHeader(grid.addComponentColumn(route -> {
			final CSpan metricSpan = new CSpan(String.valueOf(route.getMetric()));
			if ((route.getMetric() != null) && (route.getMetric() < 100)) {
				metricSpan.getStyle().set("color", "var(--lumo-success-color)");
			}
			return metricSpan;
		}).setWidth("100px").setFlexGrow(0).setKey("metric").setSortable(true).setResizable(true), "Metric");
		// Flags column
		CGrid.styleColumnHeader(
				grid.addColumn(CNetworkRoute::getFlags).setWidth("120px").setFlexGrow(1).setKey("flags").setSortable(true).setResizable(true),
				"Flags");
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		return new CNetworkRoutingCalimeroClient(clientProject);
	}

	/** Create routing table grid. */
	private void createGrid() {
		grid = new CGrid<>(CNetworkRoute.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.setHeight("450px");
		add(grid);
	}

	@Override
	protected String getEditButtonId() { return ID_EDIT_BUTTON; }

	@Override
	protected String getHeaderText() { return "Routing Table"; }

	@Override
	protected ISessionService getSessionService() { return sessionService; }

	@Override
	protected boolean hasEditButton() {
		return true;
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		add(createHeader());
		add(createStandardToolbar());
		createGrid();
		loadRoutes();
	}

	/** Load routing table from Calimero server. */
	private void loadRoutes() {
		try {
			LOGGER.debug("Loading routing table from Calimero server");
			buttonRefresh.setEnabled(false);
			buttonEdit.setEnabled(false);
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				showCalimeroUnavailableWarning("Calimero service not available");
				grid.setItems(Collections.emptyList());
				return;
			}
			hideCalimeroUnavailableWarning();
			final CNetworkRoutingCalimeroClient routingClient = (CNetworkRoutingCalimeroClient) clientOpt.get();
			final List<CNetworkRoute> routes = routingClient.fetchRoutes();
			grid.setItems(routes);
			LOGGER.info("Loaded {} routes", routes.size());
			CNotificationService.showSuccess("Loaded " + routes.size() + " routes");
		} catch (final Exception e) {
			LOGGER.error("Failed to load routing table: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load routing table", e);
			showCalimeroUnavailableWarning("Failed to load routing table");
			grid.setItems(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
			buttonEdit.setEnabled(true);
		}
	}

	@Override
	protected void on_buttonEdit_clicked() {
		LOGGER.debug("Edit routes button clicked");
		openRouteEditDialog();
	}

	/** Open route configuration edit dialog. */
	private void openRouteEditDialog() {
		try {
			final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
			if (clientOpt.isEmpty()) {
				CNotificationService.showWarning("Cannot connect to Calimero server");
				return;
			}
			final CNetworkRoutingCalimeroClient client = (CNetworkRoutingCalimeroClient) clientOpt.get();
			final List<CNetworkRoute> allRoutes = client.fetchRoutes();
			String defaultGateway = "";
			final List<CRouteEntry> manualRoutes = new ArrayList<>();
			for (final CNetworkRoute route : allRoutes) {
				if (route.isDefaultRoute() && route.hasGateway()) {
					defaultGateway = route.getGateway();
				}
			}
			final String finalDefaultGateway = defaultGateway;
			final CDialogEditRouteConfiguration dialog =
					new CDialogEditRouteConfiguration(finalDefaultGateway, manualRoutes, update -> applyRouteConfiguration(update));
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open route edit dialog", e);
			CNotificationService.showException("Failed to open route configuration dialog", e);
		}
	}

	@Override
	protected void refreshComponent() {
		loadRoutes();
	}
}
