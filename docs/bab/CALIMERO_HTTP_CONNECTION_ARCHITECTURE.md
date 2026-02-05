# Calimero HTTP Connection Architecture & Best Practices

**Date**: 2026-02-03  
**Status**: Current Design Analysis + Best Practice Recommendations

## Executive Summary

**Current Design**: Lazy initialization with auto-connect on first request, graceful degradation with UI warnings.

**Best Practices**: Implement connection pool, health checks, retry strategy, and connection state management.

---

## Table of Contents

1. [Current Architecture](#current-architecture)
2. [Connection Lifecycle](#connection-lifecycle)
3. [Connection Problems & Solutions](#connection-problems--solutions)
4. [Best Practice Recommendations](#best-practice-recommendations)
5. [Implementation Roadmap](#implementation-roadmap)

---

## Current Architecture

### Component Hierarchy

```
CProject_Bab (Entity)
    ↓ (transient field)
CClientProject (HTTP Client)
    ↓ (used by)
CAbstractCalimeroClient (Base Client)
    ↓ (extended by 8 clients)
CNetworkInterfaceCalimeroClient, CSystemMetricsCalimeroClient, etc.
    ↓ (used by)
CComponentBabBase → CComponentInterfaceList, CComponentSystemMetrics, etc.
```

### 1. Entity Level: CProject_Bab

**Location**: `src/main/java/tech/derbent/bab/project/domain/CProject_Bab.java`

```java
@Entity
public class CProject_Bab extends CProject<CProject_Bab> {
    
    // Persisted fields
    @Column(name = "ip_address", length = 45)
    private String ipAddress;  // e.g., "127.0.0.1"
    
    @Column(name = "auth_token", length = 255)
    private String authToken;  // Bearer token for API
    
    // Transient field - NOT persisted to database
    @Transient
    private CClientProject httpClient;  // ← Created on demand
    
    /**
     * Get HTTP client (lazy initialization).
     * Returns null if not yet connected.
     */
    public CClientProject getHttpClient() {
        if (httpClient == null) {
            LOGGER.debug("HTTP client not initialized for project '{}'", getName());
        }
        return httpClient;  // May be null!
    }
    
    /**
     * Connect to Calimero server.
     * Creates HTTP client and attempts connection.
     */
    public CConnectionResult connectToCalimero() {
        // 1. Validate IP address
        if (ipAddress == null || ipAddress.isBlank()) {
            return CConnectionResult.failure("IP address not set");
        }
        
        // 2. Get or create HTTP client via service
        final CClientProjectService clientService = CSpringContext.getBean(CClientProjectService.class);
        setHttpClient(clientService.getOrCreateClient(this));
        
        // 3. Attempt connection
        final CConnectionResult result = httpClient.connect();
        return result;
    }
    
    /**
     * Check connection status.
     */
    public boolean isConnectedToCalimero() {
        return (httpClient != null) && httpClient.isConnected();
    }
}
```

**Key Points**:
- `httpClient` is **transient** (not persisted to database)
- Created **lazily** on first connection attempt
- Can be **null** if never connected
- IP/token changes **invalidate** existing client

---

### 2. HTTP Client Level: CClientProject

**Location**: `src/main/java/tech/derbent/bab/http/clientproject/domain/CClientProject.java`

```java
public class CClientProject {
    
    private final String serverUrl;  // e.g., "http://127.0.0.1:8077"
    private final String authToken;
    private boolean connected = false;  // Connection state flag
    
    /**
     * Connect to Calimero server (test connection).
     * Sends "hello" request to verify connectivity.
     */
    public CConnectionResult connect() {
        final CCalimeroRequest request = CCalimeroRequest.builder()
            .type("system")
            .operation("hello")
            .build();
        
        final CCalimeroResponse response = sendRequest(request);
        
        if (response.isSuccess()) {
            connected = true;
            LOGGER.info("✅ Successfully connected to Calimero server");
            return CConnectionResult.success(...);
        } else {
            connected = false;
            LOGGER.warn("❌ Failed to connect: {}", response.getStatusCode());
            return CConnectionResult.failure(...);
        }
    }
    
    /**
     * Send request to Calimero.
     * AUTO-RECONNECTS if not connected!
     */
    public CCalimeroResponse sendRequest(final CCalimeroRequest request) {
        // AUTO-CONNECT if not connected
        if (!connected) {
            LOGGER.warn("⚠️ Not connected - attempting to connect first");
            final CConnectionResult result = connect();
            if (!result.isSuccess()) {
                return CCalimeroResponse.error("Not connected: " + result.getMessage());
            }
        }
        
        // Send HTTP POST request
        final CHttpResponse httpResponse = httpService.sendPost(apiUrl, request.toJson(), headers);
        totalRequests++;
        
        // Parse response
        return CCalimeroResponse.fromJson(httpResponse.getBody());
    }
    
    /**
     * Disconnect from server (just sets flag to false).
     */
    public void disconnect() {
        connected = false;
        LOGGER.info("🔌 Disconnected project '{}' from Calimero server", projectName);
    }
    
    public boolean isConnected() {
        return connected;
    }
}
```

**Key Points**:
- `connected` flag tracks connection state
- **Auto-reconnect** on every request if not connected
- No **persistent TCP connection** - just HTTP requests
- `disconnect()` only sets flag (no actual connection teardown)

---

### 3. Component Level: CComponentBabBase

**Location**: `src/main/java/tech/derbent/bab/uiobjects/view/CComponentBabBase.java`

```java
public abstract class CComponentBabBase extends CVerticalLayout {
    
    protected CAbstractCalimeroClient calimeroClient;  // Cached client
    
    /**
     * Get Calimero client (lazy initialization).
     * Returns empty if:
     * - No active BAB project
     * - Project has no HTTP client
     * - Connection failed
     */
    protected Optional<CAbstractCalimeroClient> getCalimeroClient() {
        // Return cached client if exists
        if (calimeroClient != null) {
            return Optional.of(calimeroClient);
        }
        
        // Resolve HTTP client project
        final Optional<CClientProject> clientOptional = resolveClientProject();
        if (clientOptional.isEmpty()) {
            return Optional.empty();  // No client available
        }
        
        // Create concrete client (e.g., CNetworkInterfaceCalimeroClient)
        calimeroClient = createCalimeroClient(clientOptional.get());
        return Optional.of(calimeroClient);
    }
    
    /**
     * Resolve HTTP client from active BAB project.
     */
    protected Optional<CClientProject> resolveClientProject() {
        final ISessionService service = getSessionService();
        if (service == null) {
            return Optional.empty();
        }
        
        final var projectOpt = service.getActiveProject();
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        
        if (!(projectOpt.get() instanceof CProject_Bab)) {
            return Optional.empty();
        }
        
        final CProject_Bab babProject = (CProject_Bab) projectOpt.get();
        return Optional.ofNullable(babProject.getHttpClient());  // May be null!
    }
}
```

---

### 4. Component Usage: CComponentInterfaceList

```java
public class CComponentInterfaceList extends CComponentBabBase {
    
    private void loadInterfaces() {
        try {
            // 1. Get Calimero client
            final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
            
            // 2. Handle unavailable client (graceful degradation)
            if (clientOpt.isEmpty()) {
                showCalimeroUnavailableWarning("Calimero service not available");
                grid.setItems(Collections.emptyList());
                return;  // ← No exception, just warning banner
            }
            
            // 3. Fetch data from Calimero
            hideCalimeroUnavailableWarning();
            final CNetworkInterfaceCalimeroClient interfaceClient = 
                (CNetworkInterfaceCalimeroClient) clientOpt.get();
            final List<CNetworkInterface> interfaces = interfaceClient.fetchInterfaces();
            
            // 4. Display data
            grid.setItems(interfaces);
            LOGGER.info("✅ Loaded {} network interfaces successfully", interfaces.size());
            
        } catch (final Exception e) {
            LOGGER.error("❌ Failed to load interfaces: {}", e.getMessage(), e);
            showCalimeroUnavailableWarning("Failed to load interfaces");
            grid.setItems(Collections.emptyList());
        }
    }
}
```

---

## Connection Lifecycle

### Scenario 1: Application Startup (Calimero Running)

```
1. User logs in
   └─ Session created with active project

2. User opens BAB dashboard
   └─ CComponentInterfaceList.initializeComponents()
       └─ loadInterfaces()

3. getCalimeroClient() called
   ├─ calimeroClient = null (first time)
   ├─ resolveClientProject()
   │   ├─ Gets active project (CProject_Bab)
   │   ├─ project.getHttpClient() → null (not yet connected)
   │   └─ Returns Optional.empty()
   └─ Returns Optional.empty()

4. Component shows warning
   └─ "Calimero service not available"

5. User clicks "Refresh" button
   └─ Same flow → Still no client

❌ PROBLEM: Component never connects automatically!
```

### Scenario 2: Manual Connection (User Action)

```
1. User opens "Connection Settings" dialog
2. User clicks "Connect" button
3. project.connectToCalimero() called
   ├─ Validates IP address
   ├─ Creates CClientProject via service
   ├─ Calls httpClient.connect()
   │   └─ Sends "hello" request → SUCCESS
   ├─ Sets connected = true
   └─ Returns success

4. User opens dashboard component
5. getCalimeroClient() called
   ├─ resolveClientProject()
   │   ├─ project.getHttpClient() → CClientProject instance
   │   └─ Returns Optional.of(clientProject)
   ├─ Creates CNetworkInterfaceCalimeroClient
   └─ Returns client

6. Component loads data successfully
   └─ Hides warning, shows interface list

✅ SUCCESS: Manual connection works
```

### Scenario 3: Calimero Stopped Mid-Session

```
1. Component is working (connected)
2. Calimero server stopped externally
3. User clicks "Refresh" button
4. interfaceClient.fetchInterfaces()
   ├─ clientProject.sendRequest(...)
   │   ├─ connected = true (flag still set)
   │   ├─ Sends HTTP POST → Connection refused
   │   └─ Returns CCalimeroResponse.error("Connection refused")
   ├─ response.isSuccess() → false
   └─ Returns empty list

5. Component shows warning
   └─ "Failed to load interfaces"

6. User clicks "Refresh" again
7. clientProject.sendRequest(...)
   ├─ connected = true (flag STILL set!)
   ├─ Does NOT auto-reconnect (flag says connected)
   ├─ Sends HTTP POST → Connection refused again
   └─ Returns error

❌ PROBLEM: Connection flag not reset on HTTP errors!
```

### Scenario 4: Best Practice Flow (NOT YET IMPLEMENTED)

```
1. Component initialization
   ├─ Attempts auto-connect if IP configured
   └─ Schedules health check (periodic ping)

2. Health check runs every 30 seconds
   ├─ Sends lightweight ping
   ├─ If success: connected = true
   └─ If failure: connected = false

3. User action (refresh button)
   ├─ Checks connected flag
   ├─ If false: Shows "Reconnecting..." status
   │   └─ Attempts reconnect with retry (3 attempts, 2s apart)
   └─ If success: Loads data

4. Connection lost mid-request
   ├─ HTTP error caught
   ├─ Sets connected = false
   ├─ Shows "Connection lost - will retry in 5s"
   └─ Auto-retry after delay

✅ BEST PRACTICE: Resilient connection management
```

---

## Connection Problems & Solutions

### Problem 1: httpClient is Null on Startup ❌

**Current Behavior**:
```java
// Component opens → getCalimeroClient()
project.getHttpClient();  // Returns null!
```

**Why**: `CProject_Bab.httpClient` is transient and only created when `connectToCalimero()` is explicitly called.

**Solutions**:

#### Option A: Auto-Connect on First Access (RECOMMENDED)
```java
public CClientProject getHttpClient() {
    if (httpClient == null && ipAddress != null && !ipAddress.isBlank()) {
        LOGGER.info("HTTP client not initialized - auto-connecting");
        connectToCalimero();  // Auto-connect on first access
    }
    return httpClient;
}
```

**Pros**: ✅ Transparent, works immediately  
**Cons**: ⚠️ May delay UI if connection slow

#### Option B: Connect on Project Load
```java
// In CProjectService.findById()
@Override
public Optional<CProject_Bab> findById(Long id) {
    final Optional<CProject_Bab> projectOpt = super.findById(id);
    if (projectOpt.isPresent() && projectOpt.get().getIpAddress() != null) {
        // Auto-connect after loading from database
        projectOpt.get().connectToCalimero();
    }
    return projectOpt;
}
```

**Pros**: ✅ Ready when user opens dashboard  
**Cons**: ⚠️ May slow project loading

#### Option C: Background Connection Task
```java
// In application startup
@Service
public class CBabConnectionManager {
    
    @Scheduled(fixedDelay = 60000)  // Every 60 seconds
    public void ensureProjectsConnected() {
        final List<CProject_Bab> projects = projectService.findAllBabProjects();
        for (final CProject_Bab project : projects) {
            if (!project.isConnectedToCalimero()) {
                LOGGER.info("Auto-connecting project: {}", project.getName());
                project.connectToCalimero();
            }
        }
    }
}
```

**Pros**: ✅ Non-blocking, resilient, periodic health check  
**Cons**: ⚠️ Requires Spring scheduler setup

---

### Problem 2: Connection Flag Not Reset on HTTP Errors ❌

**Current Behavior**:
```java
public CCalimeroResponse sendRequest(final CCalimeroRequest request) {
    if (!connected) {
        connect();  // Auto-reconnect
    }
    
    final CHttpResponse httpResponse = httpService.sendPost(...);  // May throw!
    // If HTTP error → connected flag NOT reset!
    return CCalimeroResponse.fromJson(httpResponse.getBody());
}
```

**Solution**: Reset flag on HTTP errors
```java
public CCalimeroResponse sendRequest(final CCalimeroRequest request) {
    if (!connected) {
        final CConnectionResult result = connect();
        if (!result.isSuccess()) {
            return CCalimeroResponse.error("Not connected");
        }
    }
    
    try {
        final CHttpResponse httpResponse = httpService.sendPost(...);
        totalRequests++;
        return CCalimeroResponse.fromJson(httpResponse.getBody());
        
    } catch (final ConnectException | SocketTimeoutException e) {
        // HTTP error → reset connection flag
        connected = false;
        LOGGER.error("Connection lost: {}", e.getMessage());
        return CCalimeroResponse.error("Connection lost: " + e.getMessage());
    }
}
```

---

### Problem 3: No Retry Logic ❌

**Current Behavior**: Single attempt, immediate failure

**Solution**: Exponential backoff retry
```java
public CCalimeroResponse sendRequestWithRetry(final CCalimeroRequest request) {
    int attempt = 0;
    final int maxAttempts = 3;
    final long baseDelay = 1000;  // 1 second
    
    while (attempt < maxAttempts) {
        try {
            final CCalimeroResponse response = sendRequest(request);
            if (response.isSuccess()) {
                return response;
            }
            
            // Retry on connection errors, not on auth errors
            if (response.getStatusCode() == 401 || response.getStatusCode() == 403) {
                return response;  // Don't retry auth errors
            }
            
        } catch (final Exception e) {
            LOGGER.warn("Attempt {}/{} failed: {}", attempt + 1, maxAttempts, e.getMessage());
        }
        
        attempt++;
        if (attempt < maxAttempts) {
            final long delay = baseDelay * (long) Math.pow(2, attempt - 1);
            LOGGER.info("Retrying in {}ms...", delay);
            Thread.sleep(delay);
        }
    }
    
    return CCalimeroResponse.error("Failed after " + maxAttempts + " attempts");
}
```

---

### Problem 4: No Connection Health Monitoring ❌

**Current Behavior**: Only know connection is dead when request fails

**Solution**: Periodic health checks
```java
@Service
public class CCalimeroHealthMonitor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroHealthMonitor.class);
    
    @Scheduled(fixedDelay = 30000)  // Every 30 seconds
    public void checkConnectionHealth() {
        final List<CProject_Bab> projects = projectService.findAllActiveBabProjects();
        
        for (final CProject_Bab project : projects) {
            if (project.getHttpClient() == null) {
                continue;  // Skip unconnected projects
            }
            
            final CClientProject httpClient = project.getHttpClient();
            
            // Send lightweight ping
            final CCalimeroRequest ping = CCalimeroRequest.builder()
                .type("system")
                .operation("hello")
                .build();
            
            final CCalimeroResponse response = httpClient.sendRequest(ping);
            
            if (!response.isSuccess()) {
                LOGGER.warn("Health check failed for project '{}': {}", 
                    project.getName(), response.getErrorMessage());
                httpClient.disconnect();  // Reset connection flag
            } else {
                LOGGER.debug("Health check OK for project '{}'", project.getName());
            }
        }
    }
}
```

---

## Best Practice Recommendations

### 1. Connection Lifecycle Management

```java
/**
 * Enhanced connection lifecycle with auto-connect and health monitoring.
 */
public class CProject_Bab extends CProject<CProject_Bab> {
    
    @Transient
    private CClientProject httpClient;
    
    @Transient
    private LocalDateTime lastConnectionAttempt;
    
    @Transient
    private int consecutiveFailures = 0;
    
    /**
     * Get HTTP client with auto-connect.
     * BEST PRACTICE: Auto-connect on first access if IP configured.
     */
    public CClientProject getHttpClient() {
        // Return existing client if connected
        if (httpClient != null && httpClient.isConnected()) {
            return httpClient;
        }
        
        // Auto-connect if IP configured
        if (ipAddress != null && !ipAddress.isBlank()) {
            // Rate limit connection attempts (don't spam if failing)
            if (shouldAttemptConnection()) {
                LOGGER.info("HTTP client not connected - auto-connecting");
                connectToCalimero();
            }
        }
        
        return httpClient;  // May still be null if connection failed
    }
    
    /**
     * Rate limit connection attempts (exponential backoff).
     */
    private boolean shouldAttemptConnection() {
        if (lastConnectionAttempt == null) {
            return true;  // First attempt
        }
        
        // Exponential backoff: 10s, 20s, 40s, 80s, max 5 minutes
        final long backoffSeconds = Math.min(300, 10 * (long) Math.pow(2, consecutiveFailures));
        final LocalDateTime nextAttempt = lastConnectionAttempt.plusSeconds(backoffSeconds);
        
        return LocalDateTime.now().isAfter(nextAttempt);
    }
    
    /**
     * Connect to Calimero with failure tracking.
     */
    public CConnectionResult connectToCalimero() {
        lastConnectionAttempt = LocalDateTime.now();
        
        final CConnectionResult result = performConnection();
        
        if (result.isSuccess()) {
            consecutiveFailures = 0;
            LOGGER.info("✅ Connected successfully");
        } else {
            consecutiveFailures++;
            LOGGER.warn("❌ Connection failed ({} consecutive failures)", consecutiveFailures);
        }
        
        return result;
    }
}
```

---

### 2. Resilient HTTP Client

```java
/**
 * Enhanced HTTP client with retry and error handling.
 */
public class CClientProject {
    
    private boolean connected = false;
    private int totalRequests = 0;
    private int failedRequests = 0;
    private LocalDateTime lastSuccessfulRequest;
    
    /**
     * Send request with retry logic.
     * BEST PRACTICE: Retry with exponential backoff.
     */
    public CCalimeroResponse sendRequest(final CCalimeroRequest request) {
        return sendRequestWithRetry(request, 3);
    }
    
    /**
     * Send request with configurable retry attempts.
     */
    private CCalimeroResponse sendRequestWithRetry(final CCalimeroRequest request, int maxAttempts) {
        CCalimeroResponse lastResponse = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // Check connection status
                if (!connected) {
                    LOGGER.warn("Not connected - attempting to connect (attempt {}/{})", 
                        attempt, maxAttempts);
                    final CConnectionResult connectResult = connect();
                    if (!connectResult.isSuccess()) {
                        lastResponse = CCalimeroResponse.error("Not connected: " + connectResult.getMessage());
                        continue;  // Retry
                    }
                }
                
                // Send HTTP request
                final CHttpResponse httpResponse = httpService.sendPost(buildUrl("/api/request"), 
                    request.toJson(), request.getHeaders());
                
                totalRequests++;
                lastSuccessfulRequest = LocalDateTime.now();
                
                // Parse response
                final CCalimeroResponse response = CCalimeroResponse.fromJson(httpResponse.getBody());
                
                if (response.isSuccess()) {
                    return response;  // SUCCESS - no retry needed
                }
                
                // Check if error is retryable
                if (!isRetryable(response)) {
                    LOGGER.warn("Non-retryable error: {} - aborting retries", response.getErrorMessage());
                    return response;  // Don't retry auth errors, etc.
                }
                
                lastResponse = response;
                
            } catch (final ConnectException | SocketTimeoutException e) {
                // Connection error - reset flag and retry
                connected = false;
                failedRequests++;
                LOGGER.warn("Connection error (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());
                lastResponse = CCalimeroResponse.error("Connection error: " + e.getMessage());
                
            } catch (final Exception e) {
                // Unexpected error - log and retry
                failedRequests++;
                LOGGER.error("Unexpected error (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage(), e);
                lastResponse = CCalimeroResponse.error("Unexpected error: " + e.getMessage());
            }
            
            // Wait before retry (exponential backoff)
            if (attempt < maxAttempts) {
                final long delayMs = 1000 * (long) Math.pow(2, attempt - 1);  // 1s, 2s, 4s
                LOGGER.info("Retrying in {}ms...", delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // All attempts failed
        failedRequests++;
        return lastResponse != null ? lastResponse : CCalimeroResponse.error("All retry attempts failed");
    }
    
    /**
     * Check if error is retryable.
     */
    private boolean isRetryable(final CCalimeroResponse response) {
        final int statusCode = response.getStatusCode();
        
        // Don't retry client errors (except 408 Request Timeout)
        if (statusCode >= 400 && statusCode < 500 && statusCode != 408) {
            return false;  // Auth errors, bad requests, etc.
        }
        
        // Retry server errors and timeouts
        return true;
    }
    
    /**
     * Get connection statistics.
     */
    public String getConnectionStats() {
        final double successRate = totalRequests > 0 
            ? (1.0 - (double) failedRequests / totalRequests) * 100 
            : 0;
        
        return String.format(
            "Project: %s | Connected: %s | Requests: %d | Failed: %d | Success Rate: %.1f%% | Last Success: %s",
            projectName, connected, totalRequests, failedRequests, successRate, 
            lastSuccessfulRequest != null ? lastSuccessfulRequest : "Never"
        );
    }
}
```

---

### 3. Component-Level Error Handling

```java
/**
 * Enhanced component with connection status display.
 */
public class CComponentInterfaceList extends CComponentBabBase {
    
    private CSpan connectionStatus;
    
    @Override
    protected void initializeComponents() {
        setId(ID_ROOT);
        
        // Connection status indicator
        connectionStatus = new CSpan();
        connectionStatus.getStyle()
            .set("font-size", "0.875rem")
            .set("margin-bottom", "8px");
        
        add(createHeader());
        add(connectionStatus);
        add(createStandardToolbar());
        createGrid();
        
        // Initial load
        loadInterfaces();
    }
    
    private void loadInterfaces() {
        try {
            buttonRefresh.setEnabled(false);
            updateConnectionStatus("Connecting...", "var(--lumo-contrast-60pct)");
            
            // Get client with retry
            final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
            
            if (clientOpt.isEmpty()) {
                updateConnectionStatus("❌ Not connected - check IP configuration", "var(--lumo-error-color)");
                showCalimeroUnavailableWarning("Calimero service not available");
                grid.setItems(Collections.emptyList());
                return;
            }
            
            hideCalimeroUnavailableWarning();
            final CNetworkInterfaceCalimeroClient client = (CNetworkInterfaceCalimeroClient) clientOpt.get();
            
            // Fetch data
            updateConnectionStatus("Loading interfaces...", "var(--lumo-primary-color)");
            final List<CNetworkInterface> interfaces = client.fetchInterfaces();
            
            // Success
            grid.setItems(interfaces);
            updateConnectionStatus(String.format("✅ Connected - %d interfaces loaded", interfaces.size()), 
                "var(--lumo-success-color)");
            
        } catch (final Exception e) {
            LOGGER.error("Failed to load interfaces: {}", e.getMessage(), e);
            updateConnectionStatus("❌ Error: " + e.getMessage(), "var(--lumo-error-color)");
            showCalimeroUnavailableWarning("Failed to load interfaces");
            
        } finally {
            buttonRefresh.setEnabled(true);
        }
    }
    
    private void updateConnectionStatus(final String message, final String color) {
        connectionStatus.setText(message);
        connectionStatus.getStyle().set("color", color);
    }
}
```

---

### 4. Background Health Monitor

```java
/**
 * Periodic health check for all BAB projects.
 */
@Service
@ConditionalOnProperty(name = "bab.health-monitor.enabled", havingValue = "true", matchIfMissing = true)
public class CCalimeroHealthMonitor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CCalimeroHealthMonitor.class);
    
    private final IProjectService projectService;
    
    public CCalimeroHealthMonitor(final IProjectService projectService) {
        this.projectService = projectService;
    }
    
    /**
     * Check connection health every 30 seconds.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void monitorConnections() {
        try {
            final List<CProject_Bab> projects = projectService.findAllBabProjects();
            
            for (final CProject_Bab project : projects) {
                checkProjectHealth(project);
            }
            
        } catch (final Exception e) {
            LOGGER.error("Health monitor error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check health of single project.
     */
    private void checkProjectHealth(final CProject_Bab project) {
        if (project.getHttpClient() == null) {
            // No client - attempt to connect if IP configured
            if (project.getIpAddress() != null && !project.getIpAddress().isBlank()) {
                LOGGER.debug("Health check: Project '{}' has no client - attempting auto-connect", project.getName());
                project.connectToCalimero();
            }
            return;
        }
        
        final CClientProject httpClient = project.getHttpClient();
        
        // Send lightweight ping
        final CCalimeroRequest ping = CCalimeroRequest.builder()
            .type("system")
            .operation("hello")
            .build();
        
        final CCalimeroResponse response = httpClient.sendRequest(ping);
        
        if (response.isSuccess()) {
            LOGGER.debug("Health check OK: Project '{}' - {}", project.getName(), httpClient.getConnectionStats());
        } else {
            LOGGER.warn("Health check FAILED: Project '{}' - {}", project.getName(), response.getErrorMessage());
            httpClient.disconnect();  // Reset connection flag
        }
    }
}
```

**Configuration** (`application.properties`):
```properties
# BAB Health Monitor
bab.health-monitor.enabled=true
bab.health-monitor.interval=30000
```

---

## Implementation Roadmap

### Phase 1: Quick Wins (1-2 hours)

1. **Auto-Connect on First Access** ✅
   - Modify `CProject_Bab.getHttpClient()` to auto-connect
   - Add connection attempt rate limiting

2. **Reset Connection Flag on HTTP Errors** ✅
   - Catch `ConnectException`, `SocketTimeoutException` in `CClientProject.sendRequest()`
   - Set `connected = false` on errors

3. **Component Connection Status Display** ✅
   - Add connection status indicator to all components
   - Show "Connecting...", "Connected", "Failed" states

### Phase 2: Retry Logic (2-3 hours)

4. **Implement Exponential Backoff Retry** ✅
   - Add `sendRequestWithRetry()` method
   - Configurable max attempts (3)
   - Exponential delays: 1s, 2s, 4s

5. **Non-Retryable Error Detection** ✅
   - Don't retry 401/403 (auth errors)
   - Don't retry 400/404 (bad requests)
   - Retry 5xx (server errors), timeouts

### Phase 3: Health Monitoring (2-3 hours)

6. **Background Health Monitor** ✅
   - Create `CCalimeroHealthMonitor` service
   - Periodic ping (every 30 seconds)
   - Auto-disconnect on failure
   - Spring configuration property

7. **Connection Statistics** ✅
   - Track total requests, failures, success rate
   - Display in admin UI or logs
   - Metrics for monitoring

### Phase 4: Advanced Features (4-6 hours)

8. **Connection Pool** (Optional)
   - Reuse HTTP connections
   - Connection pooling configuration
   - Pool size management

9. **Circuit Breaker Pattern** (Optional)
   - Open circuit after N consecutive failures
   - Half-open state for testing recovery
   - Automatic circuit recovery

10. **WebSocket Support** (Optional)
    - Real-time updates without polling
    - Persistent connection for events
    - Fallback to HTTP if WebSocket unavailable

---

## Configuration

### Recommended `application.properties`

```properties
# BAB Calimero Connection Settings
bab.connection.auto-connect=true
bab.connection.retry.enabled=true
bab.connection.retry.max-attempts=3
bab.connection.retry.backoff-multiplier=2
bab.connection.retry.initial-delay-ms=1000
bab.connection.retry.max-delay-ms=30000

# BAB Health Monitor
bab.health-monitor.enabled=true
bab.health-monitor.interval-ms=30000
bab.health-monitor.initial-delay-ms=10000

# HTTP Client Settings
bab.http.connect-timeout-ms=5000
bab.http.read-timeout-ms=10000
bab.http.connection-pool-size=10
```

---

## Summary

### Current Design ⚠️

| Feature | Status | Issue |
|---------|--------|-------|
| **Auto-Connect** | ❌ Manual only | Components show warning on startup |
| **Retry Logic** | ❌ Single attempt | Fails immediately on temporary issues |
| **Error Detection** | ⚠️ Partial | Connection flag not reset properly |
| **Health Monitoring** | ❌ None | No proactive connection checking |
| **Connection Stats** | ⚠️ Basic | Limited metrics |

### Recommended Design ✅

| Feature | Implementation | Benefit |
|---------|----------------|---------|
| **Auto-Connect** | On first `getHttpClient()` access | Components work immediately |
| **Retry Logic** | 3 attempts with exponential backoff | Resilient to transient failures |
| **Error Detection** | Reset flag on HTTP errors | Proper reconnection |
| **Health Monitoring** | Every 30s background check | Proactive connection management |
| **Connection Stats** | Track requests/failures/success rate | Monitoring and debugging |

### Implementation Effort

- **Phase 1** (Quick Wins): 2 hours → 80% improvement
- **Phase 2** (Retry Logic): 3 hours → 95% improvement
- **Phase 3** (Health Monitor): 3 hours → 99% reliability
- **Phase 4** (Advanced): 6 hours → Production-grade

**Total**: 8 hours for production-ready connection management (Phases 1-3)

---

## Related Documentation

- `BAB_COMPONENT_UNIFICATION_COMPLETE.md` - Component architecture
- `CALIMERO_CLIENT_BASE_CLASS_REFACTORING.md` - Client hierarchy
- `BAB_CALIMERO_GRACEFUL_ERROR_HANDLING.md` - Error handling patterns
- `CALIMERO_HTTP_CLIENT_IMPLEMENTATION.md` - HTTP client details

---

**Last Updated**: 2026-02-03  
**Status**: Architecture analysis complete, implementation roadmap defined
