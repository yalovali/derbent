package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterface;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CNetworkInterfaceCalimeroClient;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentEthernetInterfaces - Component for displaying and configuring Ethernet interface settings.
 * <p>
 * Displays Ethernet/Network interfaces for BAB Gateway projects with configuration options.
 * Shows network interface information including:
 * <ul>
 * <li>Interface name and status</li>
 * <li>IP configuration (static/DHCP)</li>
 * <li>MAC address and MTU</li>
 * <li>Network status and connectivity</li>
 * <li>IP address configuration options</li>
 * </ul>
 * <p>
 * Uses CNetworkInterfaceCalimeroClient to fetch network interface data via network/getInterfaces operation.
 */
public class CComponentEthernetInterfaces extends CComponentInterfaceBase {

	public static final String ID_CONFIG_BUTTON = "custom-ethernet-config-button";
	public static final String ID_GRID = "custom-ethernet-interfaces-grid";
	public static final String ID_REFRESH_BUTTON = "custom-ethernet-refresh-button";
	public static final String ID_ROOT = "custom-ethernet-interfaces-component";

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentEthernetInterfaces.class);
	private static final long serialVersionUID = 1L;

	// UI Components
	private CButton buttonConfig;
	private CGrid<CDTONetworkInterface> grid;
	private CNetworkInterfaceCalimeroClient networkClient;

	public CComponentEthernetInterfaces(final ISessionService sessionService) {
		super(sessionService);
		initializeComponents();
	}

	@Override
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		buttonConfig = new CButton("Configure", VaadinIcon.COG.create());
		buttonConfig.setId(ID_CONFIG_BUTTON);
		buttonConfig.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		buttonConfig.setEnabled(false); // Enabled when interface selected
		buttonConfig.addClickListener(e -> on_buttonConfig_clicked());
		toolbarLayout.add(buttonConfig);
	}

	private void configureGridColumns() {
		// Name column
		grid.addColumn(CDTONetworkInterface::getName)
			.setHeader("Interface")
			.setWidth("120px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// Status column with colored indicator
		grid.addComponentColumn(iface -> {
			final String status = iface.getStatus();
			final CSpan statusSpan = new CSpan("up".equals(status) ? "Up" : "Down");
			statusSpan.getStyle().set("padding", "4px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			statusSpan.getStyle().set("font-size", "0.8em");
			statusSpan.getStyle().set("font-weight", "bold");
			
			if ("up".equals(status)) {
				statusSpan.getStyle().set("background", "var(--lumo-success-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
			} else {
				statusSpan.getStyle().set("background", "var(--lumo-error-color-10pct)");
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			
			return statusSpan;
		}).setHeader("Status")
		  .setWidth("100px")
		  .setFlexGrow(0)
		  .setSortable(false)
		  .setResizable(true);

		// IP Addresses column
		grid.addColumn(iface -> {
			final List<String> addresses = iface.getAddresses();
			if (addresses == null || addresses.isEmpty()) {
				return "No IP";
			}
			return String.join(", ", addresses);
		}).setHeader("IP Address")
		  .setWidth("200px")
		  .setFlexGrow(1)
		  .setSortable(false)
		  .setResizable(true);

		// MAC Address column
		grid.addColumn(CDTONetworkInterface::getMacAddress)
			.setHeader("MAC Address")
			.setWidth("150px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// Type/Technology column
		grid.addColumn(CDTONetworkInterface::getType)
			.setHeader("Type")
			.setWidth("100px")
			.setFlexGrow(0)
			.setSortable(true)
			.setResizable(true);

		// MTU column
		grid.addColumn(iface -> {
			final Integer mtu = iface.getMtu();
			return mtu != null ? mtu.toString() : "";
		}).setHeader("MTU")
		  .setWidth("80px")
		  .setFlexGrow(0)
		  .setSortable(true)
		  .setResizable(true);
	}

	@Override
	protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
		networkClient = new CNetworkInterfaceCalimeroClient(clientProject);
		return networkClient;
	}

	private void createGrid() {
		grid = new CGrid<CDTONetworkInterface>(CDTONetworkInterface.class);
		grid.setId(ID_GRID);
		
		// Configure columns for network interface display
		configureGridColumns();
		
		// Selection listener for Config button
		grid.asSingleSelect().addValueChangeListener(e -> {
			buttonConfig.setEnabled(e.getValue() != null);
		});
		
		add(grid);
	}

	@Override
	protected String getHeaderText() {
		return "Network Interfaces";
	}

	@Override
	protected String getRefreshButtonId() {
		return ID_REFRESH_BUTTON;
	}

	@Override
	public ISessionService getSessionService() {
		return sessionService;
	}

	@Override
	protected void initializeComponents() {
		setId(ID_ROOT);
		
		// Configure component styling
		configureComponent();
		
		// Create header
		add(createHeader());
		
		// Create standard toolbar with refresh and additional buttons
		add(createStandardToolbar());
		
		// Create grid
		createGrid();
		
		// Load initial data
		loadNetworkData();
	}

	private void loadNetworkData() {
		try {
			hideCalimeroUnavailableWarning();
			
			// Check if interface data is available
			if (!isInterfaceDataAvailable()) {
				showInterfaceDataUnavailableWarning();
				updateSummary(null);
				grid.setItems();
				return;
			}
			
			if (networkClient == null) {
				showInterfaceDataUnavailableWarning();
				updateSummary(null);
				grid.setItems();
				return;
			}
			
			// Fetch network interfaces from Calimero
			final List<CDTONetworkInterface> interfaces = networkClient.fetchInterfaces();
			
			if (interfaces != null && !interfaces.isEmpty()) {
				grid.setItems(interfaces);
				
				// Update summary
				final long upInterfaces = interfaces.stream()
					.filter(iface -> "up".equals(iface.getStatus()))
					.count();
				final long withIpAddresses = interfaces.stream()
					.filter(iface -> iface.getAddresses() != null && !iface.getAddresses().isEmpty())
					.count();
					
				updateSummary(String.format("%d interfaces (%d up, %d configured)",
					interfaces.size(), upInterfaces, withIpAddresses));
				
				LOGGER.debug("Loaded {} network interfaces ({} up, {} with IP)",
					interfaces.size(), upInterfaces, withIpAddresses);
			} else {
				grid.setItems();
				updateSummary("No interfaces found");
				showCalimeroUnavailableWarning("No network interfaces found - Check Calimero connection");
			}
			
		} catch (final Exception e) {
			LOGGER.error("Error loading network interface data", e);
			CNotificationService.showException("Failed to load network interfaces", e);
			grid.setItems();
			updateSummary(null);
		}
	}

	private void on_buttonConfig_clicked() {
		final CDTONetworkInterface selectedInterface = grid.asSingleSelect().getValue();
		if (selectedInterface != null) {
			CNotificationService.showInfo("Network configuration for " + selectedInterface.getName() + " - Feature coming soon");
			// TODO: Open IP configuration dialog for selected interface
		}
	}

	@Override
	protected void refreshComponent() {
		LOGGER.debug("Refreshing network interface data");
		loadNetworkData();
	}
}