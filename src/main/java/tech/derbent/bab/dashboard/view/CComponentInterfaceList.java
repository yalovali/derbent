package tech.derbent.bab.dashboard.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.dashboard.service.CNetworkInterfaceCalimeroClient;
import tech.derbent.bab.dashboard.view.dialog.CDialogEditInterfaceIp;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

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
	private CButton buttonRefresh;
	private CButton buttonEditIp;
	private CGrid<CNetworkInterface> grid;
	private final ISessionService sessionService;
	private CNetworkInterfaceCalimeroClient interfaceClient;

	/** Constructor for interface list component.
	 * @param sessionService the session service */
	public CComponentInterfaceList(final ISessionService sessionService) {
		this.sessionService = sessionService;
		initializeComponents();
	}

	private void configureGrid() {
		// Name column
		CGrid.styleColumnHeader(
				grid.addColumn(CNetworkInterface::getName).setWidth("120px").setFlexGrow(0).setKey("name").setSortable(true).setResizable(true),
				"Interface");
		// Type column
		CGrid.styleColumnHeader(
				grid.addColumn(CNetworkInterface::getType).setWidth("100px").setFlexGrow(0).setKey("type").setSortable(true).setResizable(true),
				"Type");
		// Status column with styling
		CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
			final CSpan statusSpan = new CSpan(iface.getStatus());
			if (iface.isUp()) {
				statusSpan.getStyle().set("color", "var(--lumo-success-color)");
				statusSpan.getStyle().set("font-weight", "bold");
			} else {
				statusSpan.getStyle().set("color", "var(--lumo-error-color)");
			}
			return statusSpan;
		}).setWidth("80px").setFlexGrow(0).setKey("status").setSortable(true).setResizable(true), "Status");
		// MAC Address column
		CGrid.styleColumnHeader(grid.addColumn(CNetworkInterface::getMacAddress).setWidth("150px").setFlexGrow(0).setKey("macAddress")
				.setSortable(true).setResizable(true), "MAC Address");
		// MTU column
		CGrid.styleColumnHeader(
				grid.addColumn(CNetworkInterface::getMtu).setWidth("80px").setFlexGrow(0).setKey("mtu").setSortable(true).setResizable(true), "MTU");
		// DHCP4 column
		CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
			final Boolean dhcp4 = iface.getDhcp4();
			return new CSpan((dhcp4 != null) && dhcp4 ? "Yes" : "No");
		}).setWidth("80px").setFlexGrow(0).setKey("dhcp4").setSortable(true).setResizable(true), "DHCP4");
		// DHCP6 column
		CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
			final Boolean dhcp6 = iface.getDhcp6();
			return new CSpan((dhcp6 != null) && dhcp6 ? "Yes" : "No");
		}).setWidth("80px").setFlexGrow(0).setKey("dhcp6").setSortable(true).setResizable(true), "DHCP6");
		// IPv4 column
		CGrid.styleColumnHeader(grid.addColumn(CNetworkInterface::getIpv4Display).setWidth("170px").setFlexGrow(0).setKey("ipv4").setSortable(true)
				.setResizable(true), "IPv4");
		// Gateway column
		CGrid.styleColumnHeader(grid.addColumn(CNetworkInterface::getIpv4GatewayDisplay).setWidth("150px").setFlexGrow(0).setKey("gateway")
				.setSortable(true).setResizable(true), "Gateway");
	}

	/** Factory method for refresh button. Subclasses can override to customize button. */
	protected CButton create_buttonRefresh() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(ID_REFRESH_BUTTON);
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}

	/** Create grid component. */
	private void createGrid() {
		grid = new CGrid<>(CNetworkInterface.class);
		grid.setId(ID_GRID);
		configureGrid();
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		grid.addSelectionListener(new SelectionListener<com.vaadin.flow.component.grid.Grid<CNetworkInterface>, CNetworkInterface>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectionChange(final SelectionEvent<com.vaadin.flow.component.grid.Grid<CNetworkInterface>, CNetworkInterface> event) {
				buttonEditIp.setEnabled(event.getFirstSelectedItem().isPresent());
			}
		});
		grid.setHeight("400px");
		add(grid);
	}

	/** Create header component. */
	private void createHeader() {
		final CH3 header = new CH3("Network Interfaces");
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
		buttonEditIp = CButton.createPrimary("Edit IP", VaadinIcon.EDIT.create(), event -> openEditDialog());
		buttonEditIp.setId("cbutton-interface-edit");
		buttonEditIp.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		buttonEditIp.setEnabled(false);
		layoutToolbar.add(buttonRefresh, buttonEditIp);
		add(layoutToolbar);
	}

	@Override
	protected void initializeComponents() {
		// Set component properties
		setId(ID_ROOT);
		configureComponent();
		createHeader();
		createToolbar();
		createGrid();
		loadInterfaces();
	}

	/** Load network interfaces from Calimero server. */
	private void loadInterfaces() {
		try {
			LOGGER.debug("Loading network interfaces from Calimero server");
			buttonRefresh.setEnabled(false);
			final Optional<CClientProject> clientOptional = resolveClientProject();
			if (clientOptional.isEmpty()) {
				grid.setItems(Collections.emptyList());
				return;
			}
			interfaceClient = new CNetworkInterfaceCalimeroClient(clientOptional.get());
			final List<CNetworkInterface> interfaces = interfaceClient.fetchInterfaces();
			grid.setItems(interfaces);
			LOGGER.info("Loaded {} network interfaces", interfaces.size());
			CNotificationService.showSuccess("Loaded " + interfaces.size() + " network interfaces");
		} catch (final Exception e) {
			LOGGER.error("Failed to load network interfaces: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load network interfaces", e);
			grid.setItems(List.of());
		} finally {
			buttonRefresh.setEnabled(true);
		}
	}

	/** Handle refresh button click. */
	protected void on_buttonRefresh_clicked() {
		LOGGER.debug("Refresh button clicked");
		refreshComponent();
	}

	private void openEditDialog() {
		final CNetworkInterface selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			CNotificationService.showWarning("Select an interface first");
			return;
		}
		final CNetworkInterface target = selected;
		if (interfaceClient == null) {
			resolveClientProject().ifPresent(client -> interfaceClient = new CNetworkInterfaceCalimeroClient(client));
		}
		if (interfaceClient == null) {
			CNotificationService.showWarning("Unable to resolve Calimero client for editing");
			return;
		}
		interfaceClient.fetchIpConfiguration(target.getName()).ifPresent(target::setIpConfiguration);
		final CDialogEditInterfaceIp dialog = new CDialogEditInterfaceIp(target, update -> performIpUpdate(target, update));
		dialog.open();
	}

	private void performIpUpdate(final CNetworkInterface networkInterface, final CNetworkInterfaceIpUpdate update) {
		final Optional<CClientProject> clientOpt = resolveClientProject();
		if (clientOpt.isEmpty()) {
			return;
		}
		final CNetworkInterfaceCalimeroClient client = new CNetworkInterfaceCalimeroClient(clientOpt.get());
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
		loadInterfaces();
	}

	private Optional<CProject_Bab> resolveActiveBabProject() {
		return sessionService.getActiveProject().filter(CProject_Bab.class::isInstance).map(CProject_Bab.class::cast);
	}

	private Optional<CClientProject> resolveClientProject() {
		final Optional<CProject_Bab> projectOpt = resolveActiveBabProject();
		if (projectOpt.isEmpty()) {
			return Optional.empty();
		}
		final CProject_Bab babProject = projectOpt.get();
		CClientProject httpClient = babProject.getHttpClient();
		if ((httpClient == null) || !httpClient.isConnected()) {
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
