package tech.derbent.bab.dashboard.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.base.session.service.ISessionService;

/**
 * CComponentInterfaceList - Component for displaying network interfaces from Calimero server.
 * <p>
 * Displays network interfaces for BAB Gateway projects with real-time data from Calimero HTTP API.
 * Uses the project's HTTP client to fetch interface information.
 * <p>
 * Calimero API: POST /api/request with type="network", operation="getInterfaces"
 * <p>
 * Usage:
 * <pre>
 * CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
 * </pre>
 */
public class CComponentInterfaceList extends CVerticalLayout {

public static final String ID_GRID = "custom-interfaces-grid";
public static final String ID_HEADER = "custom-interfaces-header";
public static final String ID_REFRESH_BUTTON = "custom-interfaces-refresh-button";
public static final String ID_ROOT = "custom-interfaces-component";
public static final String ID_TOOLBAR = "custom-interfaces-toolbar";
private static final Logger LOGGER = LoggerFactory.getLogger(CComponentInterfaceList.class);
private static final long serialVersionUID = 1L;
private static final Gson GSON = new Gson();

private CButton buttonRefresh;
private CGrid<CNetworkInterface> grid;
private final ISessionService sessionService;

/** Constructor for interface list component.
 * @param sessionService the session service */
public CComponentInterfaceList(final ISessionService sessionService) {
this.sessionService = sessionService;
initializeComponent();
}

private void configureGrid() {
// Name column
CGrid.styleColumnHeader(grid.addColumn(CNetworkInterface::getName).setWidth("120px").setFlexGrow(0).setKey("name").setSortable(true)
.setResizable(true), "Interface");

// Type column
CGrid.styleColumnHeader(grid.addColumn(CNetworkInterface::getType).setWidth("100px").setFlexGrow(0).setKey("type").setSortable(true)
.setResizable(true), "Type");

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
CGrid.styleColumnHeader(grid.addColumn(CNetworkInterface::getMtu).setWidth("80px").setFlexGrow(0).setKey("mtu").setSortable(true)
.setResizable(true), "MTU");

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
}

private void initializeComponent() {
// Set component properties
setId(ID_ROOT);
setSpacing(false);
getStyle().set("gap", "12px");
setPadding(false);

// Create header
final CH3 header = new CH3("Network Interfaces");
header.setId(ID_HEADER);
header.getStyle().set("margin", "0");
add(header);

// Create toolbar
final CHorizontalLayout layoutToolbar = new CHorizontalLayout();
layoutToolbar.setId(ID_TOOLBAR);
layoutToolbar.setSpacing(true);
layoutToolbar.getStyle().set("gap", "8px");

// Refresh button
buttonRefresh = new CButton("Refresh", VaadinIcon.REFRESH.create());
buttonRefresh.setId(ID_REFRESH_BUTTON);
buttonRefresh.addThemeVariants(ButtonVariant.LUMO_SMALL);
buttonRefresh.addClickListener(e -> on_buttonRefresh_clicked());

layoutToolbar.add(buttonRefresh);
add(layoutToolbar);

// Create grid
grid = new CGrid<>(CNetworkInterface.class);
grid.setId(ID_GRID);
configureGrid();
grid.setHeight("400px");
add(grid);

// Load initial data
loadInterfaces();
}

/** Load network interfaces from Calimero server. */
private void loadInterfaces() {
try {
LOGGER.debug("Loading network interfaces from Calimero server");
buttonRefresh.setEnabled(false);

// Get active project - must be BAB project
final var projectOptional = sessionService.getActiveProject();
if (projectOptional.isEmpty()) {
CNotificationService.showError("No active project");
return;
}

if (!(projectOptional.get() instanceof CProject_Bab)) {
CNotificationService.showError("Active project is not a BAB project");
return;
}

final CProject_Bab babProject = (CProject_Bab) projectOptional.get();

// Get HTTP client from project
CClientProject httpClient = babProject.getHttpClient();
if ((httpClient == null) || !httpClient.isConnected()) {
LOGGER.info("HTTP client not connected - connecting now");
babProject.connectToCalimero();
httpClient = babProject.getHttpClient();
}

if (httpClient == null) {
CNotificationService.showError("Failed to connect to Calimero server");
return;
}

// Build Calimero request for network interfaces
final CCalimeroRequest request = CCalimeroRequest.builder().type("network").operation("getInterfaces").build();

// Send request
final CCalimeroResponse response = httpClient.sendRequest(request);

if (!response.isSuccess()) {
CNotificationService.showError("Failed to get interfaces: " + response.getErrorMessage());
grid.setItems(List.of());
return;
}

// Parse interfaces from response
final List<CNetworkInterface> interfaces = parseInterfacesFromResponse(response);

// Update grid
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

private void on_buttonRefresh_clicked() {
LOGGER.debug("Refresh button clicked");
loadInterfaces();
}

/** Parse network interfaces from Calimero response.
 * @param response Calimero response
 * @return list of network interfaces */
private List<CNetworkInterface> parseInterfacesFromResponse(final CCalimeroResponse response) {
final List<CNetworkInterface> interfaces = new ArrayList<>();

try {
// Get data field from response (it's a Map<String, Object>)
final Map<String, Object> dataMap = response.getData();
if (dataMap == null) {
LOGGER.warn("No data field in response");
return interfaces;
}

// Convert to JSON for easier parsing
final String dataJson = GSON.toJson(dataMap);
final JsonObject data = GSON.fromJson(dataJson, JsonObject.class);

// Get interfaces array
if (!data.has("interfaces")) {
LOGGER.warn("No interfaces field in response data");
return interfaces;
}

final JsonElement interfacesElement = data.get("interfaces");
if (!interfacesElement.isJsonArray()) {
LOGGER.warn("Interfaces field is not an array");
return interfaces;
}

final JsonArray interfacesArray = interfacesElement.getAsJsonArray();

// Parse each interface
for (final JsonElement element : interfacesArray) {
if (element.isJsonObject()) {
final CNetworkInterface iface = CNetworkInterface.fromJson(element.getAsJsonObject());
interfaces.add(iface);
}
}

LOGGER.debug("Parsed {} interfaces from response", interfaces.size());

} catch (final Exception e) {
LOGGER.error("Failed to parse interface response: {}", e.getMessage(), e);
throw new RuntimeException("Failed to parse interface data: " + e.getMessage(), e);
}

return interfaces;
}
}
