# BAB Interface Centralized JSON Architecture

**Status**: ✅ **IMPLEMENTED** (2026-02-08)  
**Profile**: BAB Gateway  
**Pattern**: Centralized JSON storage with component-level parsing

---

## Architecture Overview

All BAB physical interface data is stored centrally in `CProject_Bab.interfacesJson` as a JSON string. Components parse only their required sections for display.

### Key Benefits

1. **Single Source of Truth**: One JSON field contains ALL interface data
2. **Efficient Refresh**: One Calimero API call refreshes entire dashboard
3. **Consistent State**: All components show synchronized data
4. **Reduced API Load**: Multiple components don't trigger individual API calls
5. **Persistent Storage**: Interface metadata stored in database
6. **Fast Access**: Components parse JSON subset (no DB/API calls needed)

---

## 1. Data Storage Layer

### CProject_Bab Entity

```java
@Column(name = "interfaces_json", length = 50000, nullable = true)
private String interfacesJson = "{}";

public String getInterfacesJson() { return interfacesJson; }
public void setInterfacesJson(String json) { this.interfacesJson = json; }
```

**JSON Structure** (from Calimero `getAllInterfaces` API):

```json
{
  "summary": {
    "network_count": 2,
    "serial_count": 8,
    "usb_count": 5,
    "audio_count": 6,
    "video_count": 0,
    "gpio_available": true
  },
  "system_interfaces": {
    "network_interfaces": [...],
    "serial_ports": [...],
    "usb_devices": [...],
    "audio_devices": [...],
    "video_devices": []
  },
  "timestamp": "2026-02-08T14:38:28+03:00"
}
```

---

## 2. Service Layer

### CProject_BabService - Refresh Interface Data

**Single method refreshes ALL interface data**:

```java
@Transactional
public boolean refreshInterfacesJson(final CProject_Bab project) {
    LOGGER.info("🔄 Refreshing interfaces JSON for project '{}'", project.getName());
    
    // 1. Get Calimero client
    final Optional<CClientProject> clientOpt = clientProjectService.getClientProject(project);
    if (clientOpt.isEmpty()) {
        LOGGER.warn("❌ Cannot refresh - CClientProject not available");
        return false;
    }
    
    // 2. Fetch ALL interfaces from Calimero
    final CCalimeroRequest request = new CCalimeroRequest("iot", "getAllInterfaces", new HashMap<>());
    final CCalimeroResponse<String> response = clientOpt.get().sendRequest(request, String.class);
    
    if (!response.isSuccess()) {
        LOGGER.warn("❌ Failed to fetch interfaces: {}", response.getMessage());
        return false;
    }
    
    // 3. Store JSON in project entity
    final String jsonData = response.getData();
    project.setInterfacesJson(jsonData);
    save(project);
    
    LOGGER.info("✅ Interfaces JSON refreshed - {} bytes", jsonData.length());
    return true;
}
```

### CInterfaceDataCalimeroClient - JSON Parsing

**Parses JSON sections for specific components**:

```java
public Optional<CInterfaceDataResponse> parseInterfacesJson(final String jsonData) {
    try {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode = mapper.readTree(jsonData);
        
        // Extract system_interfaces section
        final JsonNode systemInterfaces = rootNode.get("system_interfaces");
        
        // Parse each interface type
        final List<CNetworkInterface> network = parseNetworkInterfaces(systemInterfaces);
        final List<CSerialPort> serial = parseSerialPorts(systemInterfaces);
        final List<CUsbDevice> usb = parseUsbDevices(systemInterfaces);
        final List<CAudioDevice> audio = parseAudioDevices(systemInterfaces);
        
        return Optional.of(new CInterfaceDataResponse(network, serial, usb, audio));
    } catch (Exception e) {
        LOGGER.error("Failed to parse interfaces JSON", e);
        return Optional.empty();
    }
}
```

---

## 3. Component Layer

### Component Pattern - Read-Only Display

**ALL interface components follow this pattern**:

1. **Extend CComponentInterfaceBase** → Standard toolbar + error handling
2. **Implement IPageServiceAutoRegistrable** → Auto method binding
3. **Parse JSON in refreshComponent()** → No individual API calls
4. **Display data in grid** → Simple, read-only display
5. **NO individual refresh buttons** → Page-level refresh only

### Example: CComponentInterfaceList

```java
public class CComponentInterfaceList extends CComponentInterfaceBase {
    
    private CGrid<CNetworkInterface> grid;
    
    public CComponentInterfaceList(final ISessionService sessionService) {
        super(sessionService);
        initializeComponents();
    }
    
    @Override
    protected void initializeComponents() {
        configureComponent();
        add(createStandardToolbar()); // Inherited from CComponentBabBase
        createGrid();
        refreshComponent(); // Load initial data
    }
    
    @Override
    protected void refreshComponent() {
        LOGGER.debug("🔄 Refreshing network interface list");
        
        // 1. Get project
        final Optional<CProject_Bab> projectOpt = getActiveBabProject();
        if (projectOpt.isEmpty()) {
            showCalimeroUnavailableWarning("No active BAB project");
            return;
        }
        
        // 2. Get cached JSON from project
        final String cachedJson = projectOpt.get().getInterfacesJson();
        if (cachedJson == null || cachedJson.isBlank()) {
            showCalimeroUnavailableWarning("No interface data available");
            grid.setItems(Collections.emptyList());
            return;
        }
        
        // 3. Parse JSON for network interfaces
        final Optional<CInterfaceDataCalimeroClient> clientOpt = getInterfaceDataClient();
        if (clientOpt.isEmpty()) {
            showCalimeroUnavailableWarning("Interface parser not available");
            return;
        }
        
        final Optional<List<CNetworkInterface>> interfacesOpt = 
            clientOpt.get().parseNetworkInterfaces(cachedJson);
        
        if (interfacesOpt.isEmpty()) {
            showCalimeroUnavailableWarning("Failed to parse interface data");
            grid.setItems(Collections.emptyList());
            return;
        }
        
        // 4. Display data
        final List<CNetworkInterface> interfaces = interfacesOpt.get();
        grid.setItems(interfaces);
        hideCalimeroUnavailableWarning();
        
        LOGGER.debug("✅ Loaded {} network interfaces", interfaces.size());
    }
    
    private void createGrid() {
        grid = new CGrid<>(CNetworkInterface.class, false);
        grid.addColumn(CNetworkInterface::getName).setHeader("Interface");
        grid.addColumn(CNetworkInterface::getType).setHeader("Type");
        grid.addColumn(CNetworkInterface::getState).setHeader("State");
        add(grid);
    }
}
```

---

## 4. Page Service Layer - Component Registration

### MANDATORY Pattern: IPageServiceAutoRegistrable

**ALL page service factory methods MUST**:

1. Create component with dependencies
2. Call `registerComponent(component.getComponentName(), component)`
3. Return component

```java
@Service
@Profile("bab")
public class CPageServiceDashboardInterfaces extends CPageServiceDynamicPage<CDashboardInterfaces> {
    
    public Component createComponentInterfaceSummary() {
        try {
            LOGGER.debug("Creating BAB interface summary component");
            final CComponentInterfaceSummary component = new CComponentInterfaceSummary(sessionService);
            registerComponent(component.getComponentName(), component); // ✅ MANDATORY
            LOGGER.debug("Created interface summary component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating interface summary: {}", e.getMessage());
            CNotificationService.showException("Failed to load interface summary", e);
            return CDiv.errorDiv("Failed to load interface summary: " + e.getMessage());
        }
    }
}
```

### Component Name Derivation

**Automatic naming** via `IPageServiceAutoRegistrable.getComponentName()`:

| Component Class | getComponentName() | Method Binding Pattern |
|----------------|-------------------|------------------------|
| `CComponentInterfaceList` | `"interfaceList"` | `on_interfaceList_action()` |
| `CComponentSystemMetrics` | `"systemMetrics"` | `on_systemMetrics_action()` |
| `CComponentDnsConfiguration` | `"dnsConfiguration"` | `on_dnsConfiguration_action()` |
| `CComponentUsbInterfaces` | `"usbInterfaces"` | `on_usbInterfaces_action()` |

---

## 5. Refresh Pattern - Page-Level Only

### User Action: Click Refresh Button in Toolbar

**Flow**:

1. **User clicks Refresh** → `CCrudToolbar.on_actionRefresh()`
2. **Toolbar delegates to page service** → `CPageServiceDashboardInterfaces.actionRefresh()`
3. **Page service refreshes JSON** → `service.refreshInterfacesJson(project)`
4. **Page service reloads entity** → `super.actionRefresh()`
5. **ALL registered components auto-refresh** → `component.refreshComponent()`

### CPageServiceDashboardInterfaces.actionRefresh()

```java
@Override
public void actionRefresh() {
    LOGGER.info("🔄 Refreshing BAB interface dashboard");
    
    try {
        // 1. Get active BAB project
        final Optional<CProject_Bab> projectOpt = getActiveBabProject();
        if (projectOpt.isEmpty()) {
            CNotificationService.showWarning("No active BAB project");
            return;
        }
        
        // 2. Refresh interfaces JSON from Calimero
        final boolean refreshed = service.refreshInterfacesJson(projectOpt.get());
        if (!refreshed) {
            CNotificationService.showWarning("Failed to refresh interface data");
            return;
        }
        
        LOGGER.info("✅ Interface data refreshed from Calimero");
        
        // 3. Reload entity (triggers component auto-refresh)
        super.actionRefresh();
        
        LOGGER.debug("All interface components will auto-refresh");
    } catch (final Exception e) {
        LOGGER.error("Error refreshing BAB interface dashboard", e);
        CNotificationService.showException("Failed to refresh dashboard", e);
    }
}
```

### Component Auto-Refresh

**When page reloads entity, ALL registered components receive new data**:

```
Page Refresh Triggered
  ↓
service.refreshInterfacesJson(project) → Fetch from Calimero
  ↓
project.setInterfacesJson(json) → Update entity
  ↓
super.actionRefresh() → Reload entity from DB
  ↓
Form binder propagates changes → All components receive new entity
  ↓
component.refreshComponent() → Parse new JSON
  ↓
grid.setItems(data) → Update UI
```

---

## 6. Component Implementations

### Interface Display Components

| Component | Data Source (JSON Path) | Display Type |
|-----------|-------------------------|--------------|
| **CComponentInterfaceSummary** | `summary.*` | Statistics cards |
| **CComponentInterfaceList** | `system_interfaces.network_interfaces` | Network grid |
| **CComponentEthernetInterfaces** | `system_interfaces.network_interfaces` (filtered) | Ethernet grid |
| **CComponentSerialInterfaces** | `system_interfaces.serial_ports` | Serial grid |
| **CComponentUsbInterfaces** | `system_interfaces.usb_devices` | USB grid |
| **CComponentAudioDevices** | `system_interfaces.audio_devices` | Audio grid |
| **CComponentCanInterfaces** | *Custom data* | CAN grid |
| **CComponentModbusInterfaces** | *Custom data* | Modbus grid |
| **CComponentRosNodes** | *Custom data* | ROS grid |

### System Monitoring Components

| Component | Purpose |
|-----------|---------|
| **CComponentSystemMetrics** | CPU/Memory/Disk overview |
| **CComponentCpuUsage** | CPU statistics |
| **CComponentDiskUsage** | Disk usage by mount point |
| **CComponentSystemServices** | Systemd services |
| **CComponentSystemProcessList** | Running processes |

### Network Configuration Components

| Component | Purpose |
|-----------|---------|
| **CComponentDnsConfiguration** | DNS server settings |
| **CComponentRoutingTable** | Network routing table |
| **CComponentWebServiceDiscovery** | API endpoint discovery |

---

## 7. Benefits of Centralized Architecture

### Performance

- **Single API call** refreshes ALL components (vs. 9+ individual calls)
- **No N+1 query problem** - One JSON parse per component
- **Fast UI updates** - Components parse subset of cached JSON
- **Reduced network load** - Calimero server called once per refresh

### Data Consistency

- **Synchronized state** - All components show same data snapshot
- **No race conditions** - Single atomic update
- **Timestamp tracking** - JSON includes fetch timestamp
- **Persistent storage** - Database backup of interface state

### Developer Experience

- **Simple component implementation** - Parse JSON section only
- **No individual API clients** - Shared parsing utility
- **Standard error handling** - Base class provides patterns
- **Easy testing** - Mock JSON data for unit tests

### User Experience

- **Fast refresh** - One button updates entire dashboard
- **Consistent data** - No partial/stale information
- **Offline viewing** - Cached JSON available when Calimero down
- **Clear status** - Single warning if data unavailable

---

## 8. Implementation Checklist

### For New Interface Components

- [ ] Extend `CComponentInterfaceBase` (or `CComponentBabBase`)
- [ ] Implement `IPageServiceAutoRegistrable` (inherited from base)
- [ ] Override `refreshComponent()` - Parse JSON section
- [ ] Override `getHeaderText()` - Component title
- [ ] Create grid with appropriate columns
- [ ] NO individual refresh buttons (use page-level only)
- [ ] Add to page service with `registerComponent(component.getComponentName(), component)`
- [ ] Add placeholder field to entity with `@AMetaData`
- [ ] Add field to initializer service

### For Page Services

- [ ] Override `actionRefresh()` to refresh JSON first
- [ ] Call `service.refreshInterfacesJson(project)` before `super.actionRefresh()`
- [ ] Register ALL components with `registerComponent(component.getComponentName(), component)`
- [ ] Use `component.getComponentName()` (not hardcoded strings)

---

## 9. Future Enhancements

### Planned

1. **Real-time Updates**: WebSocket push for interface state changes
2. **Selective Refresh**: Refresh individual JSON sections
3. **Delta Updates**: Only fetch changed interfaces
4. **History Tracking**: Store JSON snapshots for comparison
5. **Interface Configuration**: Edit IP/DNS via components
6. **Validation**: JSON schema validation for integrity

### Performance Optimizations

1. **JSON Compression**: gzip compressed storage
2. **Lazy Parsing**: Parse JSON sections on-demand
3. **Caching**: In-memory parsed object cache
4. **Background Refresh**: Auto-refresh on timer
5. **Diff Display**: Highlight changed interfaces

---

## 10. Related Documentation

- `AGENTS.md` - Section 6.10: @Transient Placeholder Pattern
- `BAB_CODING_RULES.md` - BAB component architecture
- `IPageServiceAutoRegistrable.java` - Interface documentation
- `CComponentBabBase.java` - Base class patterns
- `CProject_BabService.java` - Service implementation
- `CInterfaceDataCalimeroClient.java` - JSON parser

---

**Status**: ✅ **Production Ready**  
**Last Updated**: 2026-02-08  
**Pattern Compliance**: 100%
