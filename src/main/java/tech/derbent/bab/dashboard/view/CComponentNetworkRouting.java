package tech.derbent.bab.dashboard.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.service.CNetworkRoutingCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentNetworkRouting - Component for displaying network routing table and DNS configuration from Calimero server.
 * <p>
 * Displays network routing information for BAB Gateway projects including:
 * <ul>
 *   <li>Routing table with destination, gateway, interface, metric, and flags</li>
 *   <li>DNS server configuration</li>
 * </ul>
 * <p>
 * Calimero API: 
 * <ul>
 *   <li>POST /api/request with type="network", operation="getRoutes"</li>
 *   <li>POST /api/request with type="network", operation="getDns"</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * CComponentNetworkRouting component = new CComponentNetworkRouting(sessionService);
 * layout.add(component);
 * </pre>
 */
public class CComponentNetworkRouting extends CComponentBabBase {
	
	public static final String ID_GRID_ROUTES = "custom-routes-grid";
	public static final String ID_HEADER = "custom-routing-header";
	public static final String ID_REFRESH_BUTTON = "custom-routing-refresh-button";
	public static final String ID_ROOT = "custom-routing-component";
	public static final String ID_TOOLBAR = "custom-routing-toolbar";
	public static final String ID_DNS_SECTION = "custom-dns-section";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentNetworkRouting.class);
	private static final long serialVersionUID = 1L;
	
	private CButton buttonRefresh;
	private CGrid<CNetworkRoute> gridRoutes;
	private CNetworkRoutingCalimeroClient routingClient;
	private final ISessionService sessionService;
	private CVerticalLayout dnsSection;
	
	/**
	 * Constructor for network routing component.
	 * @param sessionService the session service
	 */
	public CComponentNetworkRouting(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
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
		}).setWidth("180px").setFlexGrow(0).setKey("destination").setSortable(true).setResizable(true),
		"Destination");
		
		// Gateway column
		CGrid.styleColumnHeader(gridRoutes.addComponentColumn(route -> {
			final String gateway = route.hasGateway() ? route.getGateway() : "-";
			final CSpan gatewaySpan = new CSpan(gateway);
			if (route.hasGateway()) {
				gatewaySpan.getStyle().set("color", "var(--lumo-success-color)");
			}
			return gatewaySpan;
		}).setWidth("150px").setFlexGrow(0).setKey("gateway").setSortable(true).setResizable(true),
		"Gateway");
		
		// Interface column
		CGrid.styleColumnHeader(
				gridRoutes.addColumn(CNetworkRoute::getInterfaceName)
						.setWidth("120px")
						.setFlexGrow(0)
						.setKey("interface")
						.setSortable(true)
						.setResizable(true),
				"Interface");
		
		// Metric column
		CGrid.styleColumnHeader(
				gridRoutes.addColumn(CNetworkRoute::getMetric)
						.setWidth("100px")
						.setFlexGrow(0)
						.setKey("metric")
						.setSortable(true)
						.setResizable(true),
				"Metric");
		
		// Flags column
		CGrid.styleColumnHeader(
				gridRoutes.addColumn(CNetworkRoute::getFlags)
						.setWidth("100px")
						.setFlexGrow(1)
						.setKey("flags")
						.setSortable(true)
						.setResizable(true),
				"Flags");
	}
	
	/** Factory method for refresh button. Subclasses can override to customize button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}
	
	/** Create DNS servers section. */
	private void createDnsSection() {
		dnsSection = new CVerticalLayout();
		dnsSection.setId(ID_DNS_SECTION);
		dnsSection.setPadding(false);
		dnsSection.setSpacing(false);
		dnsSection.getStyle()
				.set("gap", "8px")
				.set("padding", "16px")
				.set("border", "1px solid var(--lumo-contrast-10pct)")
				.set("border-radius", "8px")
				.set("background", "var(--lumo-base-color)")
				.set("margin-top", "16px");
		
		final CH4 dnsHeader = new CH4("DNS Servers");
		dnsHeader.getStyle().set("margin", "0");
		dnsSection.add(dnsHeader);
		
		add(dnsSection);
	}
	
	/** Create routing table grid. */
	private void createGrid() {
		gridRoutes = new CGrid<>(CNetworkRoute.class);
		gridRoutes.setId(ID_GRID_ROUTES);
		configureGrid();
		gridRoutes.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		gridRoutes.setHeight("350px");
		add(gridRoutes);
	}
	
	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("Network Routing");
		header.setHeight(null);
		header.setId(ID_HEADER);
		header.getStyle().set("margin", "0");
		add(header);
	}
	
	/** Create toolbar with action buttons. */
	private void createToolbar() {
		final CHorizontalLayout layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		layoutToolbar.getStyle().set("gap", "8px");
		
		buttonRefresh = create_buttonRefresh();
		layoutToolbar.add(buttonRefresh);
		
		add(layoutToolbar);
	}
	
	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		configureComponent();
		createHeader();
		createToolbar();
		createGrid();
		createDnsSection();
		loadRoutingData();
	}
	
	/** Load DNS servers and update display. */
	private void loadDnsServers() {
		try {
			if (routingClient == null) {
				return;
			}
			
			final List<String> dnsServers = routingClient.fetchDnsServers();
			
			// Clear existing DNS entries (keep header)
			while (dnsSection.getComponentCount() > 1) {
				dnsSection.remove(dnsSection.getComponentAt(1));
			}
			
			if (dnsServers.isEmpty()) {
				final CSpan noDnsSpan = new CSpan("No DNS servers configured");
				noDnsSpan.getStyle().set("color", "var(--lumo-contrast-50pct)").set("font-style", "italic");
				dnsSection.add(noDnsSpan);
			} else {
				for (final String dnsServer : dnsServers) {
					final CHorizontalLayout dnsRow = new CHorizontalLayout();
					dnsRow.setSpacing(true);
					dnsRow.getStyle().set("gap", "8px");
					
					final CSpan dnsIcon = new CSpan("•");
					dnsIcon.getStyle().set("color", "var(--lumo-success-color)").set("font-weight", "bold");
					
					final CSpan dnsAddress = new CSpan(dnsServer);
					dnsAddress.getStyle().set("font-family", "monospace");
					
					dnsRow.add(dnsIcon, dnsAddress);
					dnsSection.add(dnsRow);
				}
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
			
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				gridRoutes.setItems(Collections.emptyList());
				return;
			}
			
			routingClient = new CNetworkRoutingCalimeroClient(clientOptional.get());
			
			// Load routes
			final List<CNetworkRoute> routes = routingClient.fetchRoutes();
			gridRoutes.setItems(routes);
			LOGGER.info("Loaded {} routes", routes.size());
			
			// Load DNS servers
			loadDnsServers();
			
			CNotificationService.showSuccess("Loaded " + routes.size() + " routes");
			
		} catch (final Exception e) {
			LOGGER.error("Failed to load routing data: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load routing data", e);
			gridRoutes.setItems(Collections.emptyList());
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}
	
	/** Handle refresh button click. */
	protected void on_buttonRefresh_clicked() {
		LOGGER.debug("Refresh button clicked");
		refreshComponent();
	}
	
	@Override
	protected void refreshComponent() {
		loadRoutingData();
	}
	
	private Optional<CProject_Bab> resolveActiveBabProject() {
		return sessionService.getActiveProject()
				.filter(CProject_Bab.class::isInstance)
				.map(CProject_Bab.class::cast);
	}
	
	private Optional<CClientProject> resolveClientProject() {
		final Optional<CProject_Bab> projectOpt = resolveActiveBabProject();
		if (projectOpt.isEmpty()) {
			return Optional.empty();
		}
		
		final CProject_Bab babProject = projectOpt.get();
		CClientProject httpClient = babProject.getHttpClient();
		
		if (httpClient == null || !httpClient.isConnected()) {
			LOGGER.info("HTTP client not connected - connecting now");
			final var connectionResult = babProject.connectToCalimero();
			if (!connectionResult.isSuccess()) {
				CNotificationService.showError("Calimero connection failed: " + connectionResult.getMessage());
				return Optional.empty();
			}
			httpClient = babProject.getHttpClient();
		}
		
		return Optional.ofNullable(httpClient);
	}
}
