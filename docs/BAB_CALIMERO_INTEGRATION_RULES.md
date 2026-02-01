# BAB Calimero Integration - Coding Rules and Patterns

**Date**: 2026-02-01  
**Version**: 1.0  
**Status**: MANDATORY for all BAB profile development  
**Authors**: SSC + Master Yasin

## 🎯 Overview

This document establishes mandatory coding patterns for BAB IoT Gateway integration with Calimero HTTP server. These rules ensure consistent, production-ready integration across all network management, device communication, and system monitoring features.

---

## 1. HTTP Client Integration Pattern (MANDATORY)

### 1.1 Authentication Token Management

**RULE**: All Calimero HTTP requests MUST use Bearer token authentication from configuration.

#### ✅ CORRECT Pattern
```java
public class CNetworkInterfaceCalimeroClient {
    private final CClientProject clientProject;
    
    public List<CNetworkInterface> fetchInterfaces() {
        // Build request with type and operation
        final CCalimeroRequest request = CCalimeroRequest.builder()
            .type("network")
            .operation("getInterfaces")
            .build();
        
        // Send via project's HTTP client (handles auth automatically)
        final CCalimeroResponse response = clientProject.sendRequest(request);
        
        if (!response.isSuccess()) {
            LOGGER.warn("Failed to load interfaces: {}", response.getErrorMessage());
            return Collections.emptyList();
        }
        
        return parseInterfaces(response);
    }
}
```

#### ❌ INCORRECT - Manual token handling
```java
// ❌ WRONG - Don't manage tokens manually
public List<CNetworkInterface> fetchInterfaces() {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8077/api/request"))
        .header("Authorization", "Bearer test-token-123")  // ❌ Hardcoded!
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
}
```

**Rationale**: Token configuration changes between environments. Use `CClientProject` abstraction.

### 1.2 Request Format Pattern

**RULE**: All Calimero API requests use consistent JSON structure.

```java
{
  "type": "network",           // Service type
  "data": {
    "operation": "getInterfaces",  // Operation name
    "interface": "eth0"            // Optional parameters
  }
}
```

**Request Builder**:
```java
final CCalimeroRequest request = CCalimeroRequest.builder()
    .type("network")              // MANDATORY
    .operation("getInterfaces")   // MANDATORY
    .parameter("interface", "eth0")  // OPTIONAL
    .parameter("readOnly", true)     // OPTIONAL
    .build();
```

### 1.3 Response Handling Pattern

**RULE**: Always check `response.isSuccess()` before parsing data.

#### ✅ CORRECT Error Handling
```java
final CCalimeroResponse response = clientProject.sendRequest(request);

if (!response.isSuccess()) {
    final String message = "Failed to update IP: " + response.getErrorMessage();
    LOGGER.warn(message);
    CNotificationService.showWarning(message);
    return Optional.empty();
}

// Safe to parse data
final JsonObject data = toJsonObject(response);
return parseConfiguration(data);
```

#### ❌ INCORRECT - No error checking
```java
// ❌ WRONG - Assuming success
final CCalimeroResponse response = clientProject.sendRequest(request);
final JsonObject data = toJsonObject(response);  // ❌ May fail!
return parseConfiguration(data);
```

---

## 2. Calimero API Operations (MANDATORY)

### 2.1 Network Management Operations

| Operation | Type | Parameters | Response | Use Case |
|-----------|------|------------|----------|----------|
| `getInterfaces` | `network` | None | List of interfaces | Initial load |
| `getInterface` | `network` | `interface: "eth0"` | Single interface details | Detailed view |
| `getIP` | `network` | `interface: "eth0"` | IP configuration | Edit dialog |
| `setIP` | `network` | `interface, address, gateway, readOnly` | Success/Error | Save IP changes |
| `getRoutes` | `network` | None | Routing table | Network diagnostics |
| `getDns` | `network` | None | DNS configuration | Network diagnostics |

**Example - Get Specific Interface**:
```java
public Optional<CNetworkInterface> fetchInterfaceDetails(final String interfaceName) {
    final CCalimeroRequest request = CCalimeroRequest.builder()
        .type("network")
        .operation("getInterface")
        .parameter("interface", interfaceName)
        .build();
    
    final CCalimeroResponse response = clientProject.sendRequest(request);
    
    if (!response.isSuccess()) {
        LOGGER.warn("Failed to get interface {}: {}", interfaceName, response.getErrorMessage());
        return Optional.empty();
    }
    
    final JsonObject data = toJsonObject(response);
    return Optional.of(CNetworkInterface.createFromJson(data));
}
```

### 2.2 System Management Operations

| Operation | Type | Parameters | Response | Use Case |
|-----------|------|------------|----------|----------|
| `metrics` | `system` | None | CPU, Memory, Disk | Dashboard |
| `cpuInfo` | `system` | None | CPU details | System monitoring |
| `memInfo` | `system` | None | Memory details | System monitoring |
| `diskUsage` | `system` | None | Filesystem usage | Storage monitoring |
| `processes` | `system` | None | Process list | Process management |

### 2.3 Operation Naming Convention

**RULE**: Use camelCase for operations matching Calimero's C++ implementation.

✅ **CORRECT**: `getInterfaces`, `setIP`, `cpuInfo`  
❌ **WRONG**: `get-interfaces`, `set_ip`, `cpu_info`

---

## 3. Component Architecture Pattern (MANDATORY)

### 3.1 BAB Component Base Class

**RULE**: All BAB custom components MUST extend `CComponentBabBase`.

```java
public abstract class CComponentBabBase extends CComponentBase<Object> {
    
    /**
     * Resolve active BAB project from session.
     * @return Optional containing active BAB project or empty
     */
    protected Optional<CProject_Bab> resolveActiveBabProject() {
        return sessionService.getActiveProject()
            .filter(CProject_Bab.class::isInstance)
            .map(CProject_Bab.class::cast);
    }
    
    /**
     * Resolve HTTP client for Calimero communication.
     * Auto-connects if not already connected.
     * @return Optional containing connected client or empty
     */
    protected Optional<CClientProject> resolveClientProject() {
        final Optional<CProject_Bab> projectOpt = resolveActiveBabProject();
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        
        final CProject_Bab babProject = projectOpt.get();
        CClientProject httpClient = babProject.getHttpClient();
        
        if (httpClient == null || !httpClient.isConnected()) {
            final var result = babProject.connectToCalimero();
            if (!result.isSuccess()) {
                CNotificationService.showError("Calimero connection failed: " + result.getMessage());
                return Optional.empty();
            }
            httpClient = babProject.getHttpClient();
        }
        
        return Optional.ofNullable(httpClient);
    }
    
    /**
     * Configure component UI properties.
     * Subclasses MUST override to set component-specific styling.
     */
    protected abstract void configureComponent();
    
    /**
     * Refresh component data from Calimero server.
     * Subclasses MUST override for reload functionality.
     */
    protected abstract void refreshComponent();
}
```

### 3.2 Component Implementation Pattern

#### ✅ CORRECT Component Structure

```java
public class CComponentInterfaceList extends CComponentBabBase {
    
    // 1. Constants (MANDATORY)
    public static final String ID_ROOT = "custom-interfaces-component";
    public static final String ID_GRID = "custom-interfaces-grid";
    public static final String ID_REFRESH_BUTTON = "custom-interfaces-refresh-button";
    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentInterfaceList.class);
    private static final long serialVersionUID = 1L;
    
    // 2. Services (final, injected via constructor)
    private final ISessionService sessionService;
    
    // 3. Components (initialized in initializeComponents)
    private CButton buttonRefresh;
    private CButton buttonEditIp;
    private CGrid<CNetworkInterface> grid;
    private CNetworkInterfaceCalimeroClient interfaceClient;
    
    // 4. Constructor
    public CComponentInterfaceList(final ISessionService sessionService) {
        this.sessionService = sessionService;
        initializeComponents();
    }
    
    // 5. Initialization
    @Override
    protected void initializeComponents() {
        setId(ID_ROOT);
        configureComponent();
        createHeader();
        createToolbar();
        createGrid();
        loadInterfaces();  // Load data AFTER UI created
    }
    
    // 6. Configuration
    @Override
    protected void configureComponent() {
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "12px");
        setWidthFull();
    }
    
    // 7. Data loading with proper error handling
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
            grid.setItems(Collections.emptyList());
        } finally {
            buttonRefresh.setEnabled(true);
        }
    }
    
    // 8. Refresh implementation
    @Override
    protected void refreshComponent() {
        loadInterfaces();
    }
    
    // 9. Event handlers
    protected void on_buttonRefresh_clicked() {
        LOGGER.debug("Refresh button clicked");
        refreshComponent();
    }
}
```

### 3.3 Grid Configuration Pattern

**RULE**: Use consistent column widths and styling for network data grids.

```java
private void configureGrid() {
    // Status column with color coding
    CGrid.styleColumnHeader(grid.addComponentColumn(iface -> {
        final CSpan statusSpan = new CSpan(iface.getStatus());
        if (iface.isUp()) {
            statusSpan.getStyle().set("color", "var(--lumo-success-color)");
            statusSpan.getStyle().set("font-weight", "bold");
        } else {
            statusSpan.getStyle().set("color", "var(--lumo-error-color)");
        }
        return statusSpan;
    }).setWidth("80px").setFlexGrow(0).setKey("status").setSortable(true).setResizable(true), 
    "Status");
    
    // IPv4 column with display helper
    CGrid.styleColumnHeader(
        grid.addColumn(CNetworkInterface::getIpv4Display)
            .setWidth("170px")
            .setFlexGrow(0)
            .setKey("ipv4")
            .setSortable(true)
            .setResizable(true), 
        "IPv4");
}
```

**Column Width Standards**:
- Interface name: `120px`
- Type: `100px`
- Status: `80px`
- MAC Address: `150px`
- MTU: `80px`
- DHCP flags: `80px`
- IPv4: `170px`
- Gateway: `150px`
- DNS: `200px` (flex-grow: 1)

---

## 4. Data Enrichment Pattern (MANDATORY)

### 4.1 Progressive Data Loading

**RULE**: Load basic interface list first, then enrich with detailed data.

```java
public List<CNetworkInterface> fetchInterfaces() {
    final List<CNetworkInterface> interfaces = new ArrayList<>();
    
    // Step 1: Get basic interface list
    final CCalimeroRequest request = CCalimeroRequest.builder()
        .type("network")
        .operation("getInterfaces")
        .build();
    
    final CCalimeroResponse response = clientProject.sendRequest(request);
    if (!response.isSuccess()) {
        return interfaces;
    }
    
    // Step 2: Parse basic data
    final JsonObject data = toJsonObject(response);
    if (data.has("interfaces") && data.get("interfaces").isJsonArray()) {
        for (final JsonElement element : data.getAsJsonArray("interfaces")) {
            if (element.isJsonObject()) {
                final CNetworkInterface iface = CNetworkInterface.createFromJson(element.getAsJsonObject());
                
                // Step 3: Enrich with detailed configuration
                enrichInterfaceWithDetailedInfo(iface);
                
                interfaces.add(iface);
            }
        }
    }
    
    return interfaces;
}

/**
 * Enrich interface with detailed IP configuration.
 * Fetches addresses, gateway, DNS via separate API call.
 */
public void enrichInterfaceWithDetailedInfo(final CNetworkInterface iface) {
    Check.notNull(iface, "Interface cannot be null");
    
    final CCalimeroRequest request = CCalimeroRequest.builder()
        .type("network")
        .operation("getInterface")
        .parameter("interface", iface.getName())
        .build();
    
    final CCalimeroResponse response = clientProject.sendRequest(request);
    if (!response.isSuccess()) {
        LOGGER.warn("Unable to fetch detailed info for {}: {}", 
            iface.getName(), response.getErrorMessage());
        return;
    }
    
    final JsonObject data = toJsonObject(response);
    
    // Extract addresses
    if (data.has("addresses") && data.get("addresses").isJsonArray()) {
        final JsonArray addressArray = data.getAsJsonArray("addresses");
        final List<String> addresses = new ArrayList<>();
        addressArray.forEach(element -> addresses.add(element.getAsString()));
        iface.getAddresses().clear();
        iface.getAddresses().addAll(addresses);
    }
    
    // Extract gateway, DNS, etc.
    if (data.has("gateway4")) {
        iface.setGateway(data.get("gateway4").getAsString());
    }
}
```

**Benefits**:
- Fast initial grid display
- Progressive enhancement
- Graceful degradation if detailed fetch fails

---

## 5. Calimero Process Management (MANDATORY)

### 5.1 Startup Configuration

**RULE**: Calimero binary path MUST be configurable via `CSystemSettings_Bab`.

```java
@Entity
@Table(name = "csystem_settings_bab")
public class CSystemSettings_Bab extends CSystemSettings<CSystemSettings_Bab> {
    
    @Column(name = "enable_calimero_service", nullable = false)
    @AMetaData(displayName = "Enable Calimero Service", required = false)
    private Boolean enableCalimeroService = Boolean.FALSE;
    
    @Column(name = "calimero_executable_path", length = 500)
    @AMetaData(
        displayName = "Calimero Executable Path",
        required = false,
        description = "Path to Calimero binary (default: ~/git/calimero/build/calimero)"
    )
    private String calimeroExecutablePath = "~/git/calimero/build/calimero";
    
    // Getters/setters...
}
```

### 5.2 Process Lifecycle Management

**RULE**: Use `CCalimeroProcessManager` for all process control.

```java
@Service
@Profile("bab")
public class CCalimeroProcessManager {
    
    /**
     * Start Calimero service if enabled in settings.
     * Called during application startup.
     * @return Status object with enabled/running flags
     */
    public synchronized CCalimeroServiceStatus startCalimeroServiceIfEnabled() {
        try {
            final CSystemSettings_Bab settings = settingsService.getSystemSettings();
            
            if (settings == null || Boolean.FALSE.equals(settings.getEnableCalimeroService())) {
                return CCalimeroServiceStatus.of(false, false, "Service disabled");
            }
            
            String executablePath = settings.getCalimeroExecutablePath();
            if (executablePath == null || executablePath.isBlank()) {
                executablePath = "~/git/calimero/build/calimero";
            }
            
            // Expand ~ to home directory
            if (executablePath.startsWith("~")) {
                executablePath = System.getProperty("user.home") + executablePath.substring(1);
            }
            
            final Path execPath = Paths.get(executablePath);
            
            // Validation
            if (!Files.exists(execPath)) {
                return CCalimeroServiceStatus.of(true, false, 
                    "Binary not found at: " + executablePath);
            }
            
            if (!Files.isExecutable(execPath)) {
                return CCalimeroServiceStatus.of(true, false, 
                    "Binary not executable: " + executablePath);
            }
            
            // Start process
            final boolean started = startCalimeroProcess(execPath);
            return CCalimeroServiceStatus.of(true, started, 
                started ? "Service running" : "Failed to start");
                
        } catch (final Exception e) {
            LOGGER.error("Failed to start Calimero: {}", e.getMessage(), e);
            return CCalimeroServiceStatus.of(true, false, 
                "Failed: " + e.getMessage());
        }
    }
    
    /**
     * Stop Calimero service and cleanup resources.
     * Called on application shutdown.
     */
    @PreDestroy
    public synchronized void stopCalimeroService() {
        if (!isRunning.get()) {
            return;
        }
        
        try {
            shutdownRequested.set(true);
            
            if (calimeroProcess != null && calimeroProcess.isAlive()) {
                calimeroProcess.destroy();
                
                final boolean terminated = calimeroProcess.waitFor(5, TimeUnit.SECONDS);
                if (!terminated) {
                    LOGGER.warn("Graceful shutdown failed, forcing");
                    calimeroProcess.destroyForcibly();
                }
            }
            
            isRunning.set(false);
            LOGGER.info("Calimero service stopped");
            
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error stopping Calimero: {}", e.getMessage(), e);
        }
    }
}
```

### 5.3 Startup Listener Pattern

**RULE**: Auto-start Calimero on application ready if enabled.

```java
@Component
@Profile("bab")
public class CCalimeroStartupListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroStartupListener.class);
    private final CCalimeroProcessManager processManager;
    
    public CCalimeroStartupListener(final CCalimeroProcessManager processManager) {
        this.processManager = processManager;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        LOGGER.info("Application ready - checking Calimero service configuration");
        
        final CCalimeroServiceStatus status = processManager.startCalimeroServiceIfEnabled();
        
        if (!status.isEnabled()) {
            LOGGER.info("Calimero service is disabled");
        } else if (status.isRunning()) {
            LOGGER.info("Calimero service started successfully");
        } else {
            LOGGER.warn("Calimero service enabled but not running: {}", status.getMessage());
        }
    }
}
```

---

## 6. Error Handling Patterns (MANDATORY)

### 6.1 Three-Layer Error Handling

**RULE**: Handle errors at three levels: API client, component, and user notification.

#### Layer 1: API Client (Log and return Optional/Empty)
```java
public Optional<CNetworkInterface> fetchInterface(final String name) {
    final CCalimeroResponse response = clientProject.sendRequest(request);
    
    if (!response.isSuccess()) {
        LOGGER.warn("Failed to fetch interface {}: {}", name, response.getErrorMessage());
        return Optional.empty();  // ✅ Return empty, don't throw
    }
    
    return Optional.of(parseInterface(response));
}
```

#### Layer 2: Component (Handle gracefully, show in UI)
```java
private void loadInterfaces() {
    try {
        buttonRefresh.setEnabled(false);
        
        final Optional<CClientProject> clientOpt = resolveClientProject();
        if (clientOpt.isEmpty()) {
            grid.setItems(Collections.emptyList());
            CNotificationService.showWarning("Calimero client not available");
            return;
        }
        
        final List<CNetworkInterface> interfaces = client.fetchInterfaces();
        grid.setItems(interfaces);
        
        if (interfaces.isEmpty()) {
            CNotificationService.showInfo("No interfaces found");
        } else {
            CNotificationService.showSuccess("Loaded " + interfaces.size() + " interfaces");
        }
        
    } catch (final Exception e) {
        LOGGER.error("Failed to load interfaces: {}", e.getMessage(), e);
        CNotificationService.showException("Failed to load interfaces", e);
        grid.setItems(Collections.emptyList());
    } finally {
        buttonRefresh.setEnabled(true);
    }
}
```

#### Layer 3: User Notification (Clear, actionable messages)
```java
// ✅ CORRECT - User-friendly messages
CNotificationService.showWarning("Calimero server not responding. Check connection.");
CNotificationService.showError("Failed to update IP address. Verify configuration.");
CNotificationService.showSuccess("Network interface updated successfully.");

// ❌ WRONG - Technical jargon
CNotificationService.showError("IOException: Connection refused");
CNotificationService.showError("NullPointerException in fetchInterfaces()");
```

### 6.2 Connection Failure Patterns

**RULE**: Always check connection before API calls and provide reconnect option.

```java
private Optional<CClientProject> resolveClientProject() {
    final Optional<CProject_Bab> projectOpt = resolveActiveBabProject();
    if (projectOpt.isEmpty()) {
        LOGGER.warn("No active BAB project");
        return Optional.empty();
    }
    
    final CProject_Bab project = projectOpt.get();
    CClientProject client = project.getHttpClient();
    
    // Auto-reconnect if disconnected
    if (client == null || !client.isConnected()) {
        LOGGER.info("HTTP client not connected - attempting connection");
        
        final var result = project.connectToCalimero();
        if (!result.isSuccess()) {
            CNotificationService.showError(
                "Calimero connection failed: " + result.getMessage());
            return Optional.empty();
        }
        
        client = project.getHttpClient();
    }
    
    return Optional.ofNullable(client);
}
```

---

## 7. Testing Patterns (MANDATORY)

### 7.1 Playwright Component Testing

**RULE**: Use component testers for reusable test logic across test classes.

```java
public class CBabInterfaceListComponentTester {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabInterfaceListComponentTester.class);
    
    /**
     * Check if interface list component is present and testable.
     * @param page Playwright page
     * @return true if component can be tested
     */
    public boolean canTest(final Page page) {
        try {
            final Locator component = page.locator("#custom-interfaces-component");
            return component.isVisible();
        } catch (final Exception e) {
            LOGGER.debug("Interface list component not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Run comprehensive tests on interface list component.
     * @param page Playwright page with component visible
     */
    public void test(final Page page) {
        LOGGER.info("Testing network interface list component");
        
        // Test 1: Component visibility
        final Locator component = page.locator("#custom-interfaces-component");
        assertTrue(component.isVisible(), "Component should be visible");
        
        // Test 2: Grid presence
        final Locator grid = page.locator("#custom-interfaces-grid");
        assertTrue(grid.isVisible(), "Grid should be visible");
        
        // Test 3: Refresh button
        final Locator refreshButton = page.locator("#custom-interfaces-refresh-button");
        assertTrue(refreshButton.isVisible(), "Refresh button should be visible");
        
        // Test 4: Click refresh
        refreshButton.click();
        page.waitForTimeout(2000);  // Wait for API call
        
        // Test 5: Verify grid has data
        final Locator gridCells = page.locator("vaadin-grid-cell-content");
        assertTrue(gridCells.count() > 0, "Grid should have data after refresh");
        
        LOGGER.info("Interface list component tests passed");
    }
}
```

### 7.2 API Mock Testing

**RULE**: Test component behavior with both successful and failed API responses.

```java
@Test
void testInterfaceListWithSuccessfulApi() {
    // Mock successful API response
    final CCalimeroResponse mockResponse = CCalimeroResponse.builder()
        .success(true)
        .data(createMockInterfaceData())
        .build();
    
    when(mockClient.sendRequest(any())).thenReturn(mockResponse);
    
    // Create component and verify
    final CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
    assertNotNull(component);
    
    // Verify grid populated
    // ...
}

@Test
void testInterfaceListWithFailedApi() {
    // Mock failed API response
    final CCalimeroResponse mockResponse = CCalimeroResponse.builder()
        .success(false)
        .errorMessage("Connection timeout")
        .build();
    
    when(mockClient.sendRequest(any())).thenReturn(mockResponse);
    
    // Create component and verify graceful failure
    final CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
    assertNotNull(component);
    
    // Verify empty grid with error message shown
    // ...
}
```

---

## 8. Documentation Requirements (MANDATORY)

### 8.1 JavaDoc Standards for BAB Components

```java
/**
 * CComponentInterfaceList - Component for displaying network interfaces from Calimero server.
 * <p>
 * Displays network interfaces for BAB Gateway projects with real-time data from Calimero HTTP API.
 * Uses the project's HTTP client to fetch interface information.
 * <p>
 * Calimero API: POST /api/request with type="network", operation="getInterfaces"
 * <p>
 * Features:
 * <ul>
 *   <li>Real-time interface status display</li>
 *   <li>Color-coded status indicators (up=green, down=red)</li>
 *   <li>IPv4/IPv6 address display</li>
 *   <li>DHCP configuration</li>
 *   <li>Gateway and DNS information</li>
 *   <li>Edit IP configuration dialog</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
 * layout.add(component);
 * </pre>
 * 
 * @see CNetworkInterfaceCalimeroClient
 * @see CProject_Bab
 * @see CClientProject
 */
public class CComponentInterfaceList extends CComponentBabBase {
    // ...
}
```

### 8.2 API Client Documentation

```java
/**
 * Helper client responsible for retrieving and updating network interface information 
 * via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>getInterfaces - List all network interfaces</li>
 *   <li>getInterface - Get specific interface details</li>
 *   <li>getIP - Get IP configuration</li>
 *   <li>setIP - Update IP configuration</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe. Multiple instances can be created
 * but share the same underlying HTTP client connection.
 * <p>
 * Error Handling: All methods return Optional or List, never throw exceptions.
 * Check logs for error details.
 * 
 * @see CClientProject
 * @see CCalimeroRequest
 * @see CCalimeroResponse
 */
public class CNetworkInterfaceCalimeroClient {
    // ...
}
```

---

## 9. Performance Patterns (MANDATORY)

### 9.1 Lazy Loading for Large Lists

**RULE**: Load interface list without detailed data, enrich on-demand.

```java
// ✅ CORRECT - Fast initial load
public List<CNetworkInterface> fetchInterfaces() {
    // Get basic list quickly
    final List<CNetworkInterface> interfaces = fetchBasicInterfaceList();
    
    // Enrich each interface (can be parallelized if needed)
    interfaces.forEach(this::enrichInterfaceWithDetailedInfo);
    
    return interfaces;
}

// ❌ WRONG - Slow initial load with all data
public List<CNetworkInterface> fetchInterfaces() {
    final List<CNetworkInterface> interfaces = fetchBasicInterfaceList();
    
    // Don't fetch ALL detailed configs upfront
    for (final CNetworkInterface iface : interfaces) {
        iface.setConfiguration(fetchFullConfiguration(iface));  // Slow!
        iface.setStatistics(fetchStatistics(iface));  // Slow!
        iface.setRoutes(fetchRoutes(iface));  // Slow!
    }
    
    return interfaces;
}
```

### 9.2 Caching Pattern for Static Data

**RULE**: Cache configuration data that changes infrequently.

```java
public class CNetworkInterfaceCalimeroClient {
    
    private Map<String, CNetworkInterface> interfaceCache = new ConcurrentHashMap<>();
    private LocalDateTime lastCacheUpdate;
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    public List<CNetworkInterface> fetchInterfaces() {
        // Check cache freshness
        if (lastCacheUpdate != null && 
            Duration.between(lastCacheUpdate, LocalDateTime.now()).compareTo(CACHE_TTL) < 0) {
            LOGGER.debug("Returning cached interface data");
            return new ArrayList<>(interfaceCache.values());
        }
        
        // Fetch fresh data
        final List<CNetworkInterface> interfaces = fetchFromServer();
        
        // Update cache
        interfaceCache.clear();
        interfaces.forEach(iface -> interfaceCache.put(iface.getName(), iface));
        lastCacheUpdate = LocalDateTime.now();
        
        return interfaces;
    }
    
    public void invalidateCache() {
        interfaceCache.clear();
        lastCacheUpdate = null;
    }
}
```

---

## 10. Security Patterns (MANDATORY)

### 10.1 Token Storage

**RULE**: Never hardcode authentication tokens in source code.

✅ **CORRECT** - Configuration-based:
```java
// Token stored in Calimero config: config/http_server.json
{
  "authToken": "test-token-123",
  "host": "0.0.0.0",
  "httpPort": 8077
}

// Token injected via CClientProject abstraction
final CClientProject client = project.getHttpClient();
final CCalimeroResponse response = client.sendRequest(request);
// Auth header added automatically by CClientProject
```

❌ **WRONG** - Hardcoded:
```java
final String AUTH_TOKEN = "test-token-123";  // ❌ Never do this!
request.header("Authorization", "Bearer " + AUTH_TOKEN);
```

### 10.2 Input Validation for IP Addresses

**RULE**: Validate all user input before sending to Calimero API.

```java
public CCalimeroResponse updateInterfaceIp(final CNetworkInterfaceIpUpdate update) {
    // Validate inputs
    Check.notBlank(update.getInterfaceName(), "Interface name required");
    
    if (update.getAddress() != null && !isValidIpAddress(update.getAddress())) {
        throw new IllegalArgumentException("Invalid IP address format");
    }
    
    if (update.getGateway() != null && !isValidIpAddress(update.getGateway())) {
        throw new IllegalArgumentException("Invalid gateway address format");
    }
    
    // Send validated data to Calimero
    final CCalimeroRequest request = CCalimeroRequest.builder()
        .type("network")
        .operation("setIP")
        .parameter("interface", update.getInterfaceName())
        .parameter("address", update.toAddressArgument())
        .parameter("gateway", update.getGateway())
        .build();
    
    return clientProject.sendRequest(request);
}

private boolean isValidIpAddress(final String address) {
    return address.matches(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
}
```

---

## 11. Lessons Learned Summary

### 11.1 Key Achievements ✅

1. **Real Data Integration**: Calimero returns actual system network data, not mocks
2. **Proper Abstraction**: `CClientProject` handles auth and connection management
3. **Error Handling**: Three-layer error handling with user-friendly messages
4. **Progressive Enhancement**: Fast initial load with on-demand enrichment
5. **Process Management**: Automatic Calimero startup/shutdown integration
6. **Component Architecture**: Reusable `CComponentBabBase` for all BAB components

### 11.2 Common Pitfalls to Avoid ❌

1. **Auth Token**: Use `test-token-123` (dash), not `test_token_123` (underscore)
2. **Connection Checking**: Always verify connection before API calls
3. **Error Propagation**: Don't throw exceptions from API clients, return Optional
4. **Hardcoded Paths**: Make Calimero binary path configurable
5. **Missing Validation**: Validate IP addresses before sending to API
6. **Empty Grid Handling**: Show user-friendly message when no data returned

### 11.3 Testing Recommendations

1. **Component Testers**: Create reusable test helpers like `CBabInterfaceListComponentTester`
2. **Startup Coordination**: Wait for "Started Application" log before running Playwright tests
3. **Health Checks**: Poll health endpoints before asserting page elements
4. **Screenshots**: Capture screenshots at each test step for debugging
5. **Mock Responses**: Test both success and failure scenarios

---

## 12. Verification Checklist

Before committing BAB/Calimero integration code, verify:

### Code Quality
- [ ] All classes follow C-prefix convention
- [ ] Components extend `CComponentBabBase`
- [ ] API clients use `CCalimeroRequest` builder
- [ ] Error handling at three layers (client, component, notification)
- [ ] No hardcoded auth tokens
- [ ] No hardcoded Calimero paths
- [ ] Input validation for all user-provided data

### Functionality
- [ ] Connection auto-reconnect on failure
- [ ] Graceful degradation when Calimero unavailable
- [ ] Refresh button updates data
- [ ] Grid displays real interface data
- [ ] Status column color-coded (green=up, red=down)
- [ ] Edit dialog saves IP configuration

### Documentation
- [ ] JavaDoc on all public classes and methods
- [ ] Calimero API operations documented
- [ ] Error handling patterns documented
- [ ] Usage examples in class JavaDoc

### Testing
- [ ] Unit tests for API clients
- [ ] Component tests for UI components
- [ ] Playwright tests for integration
- [ ] Both success and failure scenarios tested

---

## Appendix A: Calimero API Reference

### Network Operations
- `POST /api/request` with `type: "network"`
  - `getInterfaces` - List all interfaces
  - `getInterface` - Get specific interface (param: `interface`)
  - `getIP` - Get IP config (param: `interface`)
  - `setIP` - Set IP config (params: `interface, address, gateway, readOnly`)
  - `getRoutes` - Routing table
  - `getDns` - DNS configuration

### System Operations
- `POST /api/request` with `type: "system"`
  - `metrics` - CPU, memory, disk
  - `cpuInfo` - CPU details
  - `memInfo` - Memory details
  - `diskUsage` - Filesystem usage
  - `processes` - Process list

### Health Check
- `GET /health` - Returns `{"status":"ok"}`

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-01 | Initial release - Comprehensive BAB/Calimero integration rules |

---

**Maintained by**: SSC + Master Yasin  
**Review Cycle**: Quarterly or after major BAB feature additions  
**Enforcement**: MANDATORY for all BAB profile code reviews
