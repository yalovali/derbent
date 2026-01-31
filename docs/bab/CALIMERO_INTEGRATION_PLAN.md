# BAB-Calimero Integration Plan

**Version**: 1.0  
**Date**: 2026-01-29  
**Status**: ACTIVE - Implementation Roadmap  
**Calimero Reference**: `/home/yasin/git/calimero/`  
**Calimero Web Services Source**: `/home/yasin/git/calimero/src/http` (HTTP server implementation)

---

## Executive Summary

This document defines the **complete integration plan** between **BAB (Java/Vaadin frontend)** and **Calimero (C++ backend service)** to create a unified IoT gateway management system. The integration enables web-based administration of high-performance CAN/Serial/Ethernet protocols with real-time monitoring and configuration management.

## Integration Architecture

### System Overview
```
┌─────────────────────────────────────────────────────────────┐
│                   BAB-Calimero System                      │
├─────────────────────────────────────────────────────────────┤
│  BAB Frontend (Port 8080)      │  Calimero Backend         │
│  ├─ Device Management          │  ├─ CAN Gateway (50k fps) │
│  ├─ Node Configuration         │  ├─ Serial I/O (RS232)    │
│  ├─ User Authentication        │  ├─ Ethernet (UDP/TCP)    │
│  ├─ Project Organization       │  ├─ ROS2 Integration      │
│  ├─ Real-time Dashboard        │  ├─ Data Logging (CSV)    │
│  └─ Configuration Generation   │  └─ HTTP API (Port 8077)  │
├─────────────────────────────────────────────────────────────┤
│  Integration Layer: HTTP API + Config Files + Process Mgmt │
└─────────────────────────────────────────────────────────────┘
```

### Integration Points

| Integration Point | BAB Component | Calimero Component | Protocol |
|------------------|---------------|-------------------|----------|
| **Service Control** | `CBabDeviceService` | Calimero process | systemd/Docker |
| **Configuration** | Entity Services | JSON config files | File I/O |
| **Monitoring** | Dashboard Components | HTTP API (port 8077) | REST/JSON |
| **Log Management** | Log Browser UI | Log files (CSV/binary) | File system |
| **Status Updates** | WebSocket/Polling | `/health`, `/status` endpoints | HTTP |

## Phase 1: Foundation Integration (Weeks 1-8)

### Milestone 1.1: Service Lifecycle Management (Weeks 1-2)

#### BAB Implementation
```java
@Service
@Profile("bab")
public class CCalimeroServiceManager {
    
    /**
     * Start Calimero service instance for device
     */
    public CalimeroStatus startCalimero(CBabDevice device) {
        try {
            // Generate configuration files
            generateCalimeroConfig(device);
            
            // Start via systemd or Docker
            ProcessBuilder pb = new ProcessBuilder(
                "systemctl", "start", "calimero@" + device.getId()
            );
            Process process = pb.start();
            
            // Wait for service to be ready
            return waitForServiceReady(device, Duration.ofSeconds(30));
            
        } catch (Exception e) {
            LOGGER.error("Failed to start Calimero for device: {}", device.getId(), e);
            return CalimeroStatus.ERROR;
        }
    }
    
    /**
     * Stop Calimero service gracefully
     */
    public CalimeroStatus stopCalimero(CBabDevice device) {
        // Implementation for graceful shutdown
    }
    
    /**
     * Restart Calimero service (stop + start)
     */
    public CalimeroStatus restartCalimero(CBabDevice device) {
        // Implementation for service restart
    }
    
    /**
     * Get current service status via HTTP API
     */
    public CalimeroStatus getServiceStatus(CBabDevice device) {
        try {
            String apiUrl = String.format("http://%s:8077/health", device.getIpAddress());
            // HTTP call to Calimero health endpoint
            // Parse response and return status
        } catch (Exception e) {
            return CalimeroStatus.UNREACHABLE;
        }
    }
}

public enum CalimeroStatus {
    RUNNING, STOPPED, ERROR, STARTING, STOPPING, UNREACHABLE
}
```

#### Calimero Integration
- **Existing**: HTTP server on port 8077 with `/health` endpoint
- **Existing**: Service scripts for systemd integration
- **New**: Instance-based configuration (per device)

#### Acceptance Criteria
- [ ] BAB can start/stop Calimero service from UI
- [ ] Service status correctly reflected in BAB dashboard
- [ ] Graceful shutdown preserves data integrity
- [ ] Error handling for service failures

### Milestone 1.2: Configuration Pipeline (Weeks 3-4)

#### Configuration Generation Flow
```
BAB Entity Changes → Validation → JSON Generation → File Writing → Service Reload
```

#### BAB Implementation
```java
@Service
@Profile("bab")
public class CCalimeroConfigGenerator {
    
    /**
     * Generate complete Calimero configuration from BAB entities
     */
    public void generateConfiguration(CBabDevice device) {
        Path configDir = getDeviceConfigDirectory(device);
        
        // Generate configuration files
        generateMainConfig(device, configDir);
        generateCanConfig(device, configDir);
        generateSerialConfig(device, configDir);
        generateEthernetConfig(device, configDir);
        generateRosConfig(device, configDir);
        generateRoutingConfig(device, configDir);
    }
    
    /**
     * Generate config/config.json - Main application settings
     */
    private void generateMainConfig(CBabDevice device, Path configDir) {
        Map<String, Object> config = Map.of(
            "log_dir", "/var/log/calimero/" + device.getId(),
            "config_dir", configDir.toString(),
            "http_port", 8077 + device.getId().intValue(),  // Unique port per device
            "auth_token", generateAuthToken(device)
        );
        
        writeJsonFile(configDir.resolve("config.json"), config);
    }
    
    /**
     * Generate config/can.json - CAN interface configuration
     */
    private void generateCanConfig(CBabDevice device, Path configDir) {
        List<CBabNodeCAN> canNodes = canNodeService.findByDevice(device);
        
        List<Map<String, Object>> canSockets = canNodes.stream()
            .filter(CBabNodeCAN::getEnabled)
            .map(node -> Map.of(
                "name", node.getCanInterface(),
                "bitrate", node.getBitrate(),
                "extended_frames", node.getExtendedFrames()
            ))
            .collect(toList());
            
        Map<String, Object> config = Map.of("can_sockets", canSockets);
        writeJsonFile(configDir.resolve("can.json"), config);
    }
    
    /**
     * Generate config/serial.json - Serial port configuration
     */
    private void generateSerialConfig(CBabDevice device, Path configDir) {
        List<CBabNodeSerial> serialNodes = serialNodeService.findByDevice(device);
        
        List<Map<String, Object>> serialPorts = serialNodes.stream()
            .filter(CBabNodeSerial::getEnabled)
            .map(node -> Map.of(
                "port", node.getSerialPort(),
                "baud_rate", node.getBaudRate(),
                "parity", node.getParity(),
                "data_bits", node.getDataBits(),
                "stop_bits", node.getStopBits()
            ))
            .collect(toList());
            
        Map<String, Object> config = Map.of("serial_ports", serialPorts);
        writeJsonFile(configDir.resolve("serial.json"), config);
    }
    
    /**
     * Generate config/routing.json - Port-to-port routing rules
     */
    private void generateRoutingConfig(CBabDevice device, Path configDir) {
        // Generate routing rules from BAB routing entities
        List<CRoutingRule> rules = routingService.findByDevice(device);
        
        List<String> routingRules = rules.stream()
            .map(rule -> String.format("route src=%s -> dst=%s match=%s",
                rule.getSourceNode(), rule.getDestinationNode(), rule.getMatchCriteria()))
            .collect(toList());
            
        // Write as text file (Calimero routing.conf format)
        writeTextFile(configDir.resolve("routing.conf"), routingRules);
    }
}
```

#### Calimero Integration
- **Existing**: JSON configuration loader for all protocols
- **Existing**: Hot reload capability via HTTP API
- **New**: Device-specific configuration directories

#### Configuration File Structure
```
/etc/calimero/
├── devices/
│   ├── device_1/
│   │   ├── config.json      # Main application config
│   │   ├── can.json         # CAN interfaces
│   │   ├── serial.json      # Serial ports  
│   │   ├── ethernet.json    # Ethernet endpoints
│   │   ├── ros.json         # ROS2 integration
│   │   └── routing.conf     # Routing rules
│   └── device_2/
│       └── ...
└── templates/               # Default configuration templates
    ├── config.template.json
    └── ...
```

#### Acceptance Criteria
- [ ] Configuration files generated automatically when BAB entities change
- [ ] JSON validation ensures Calimero compatibility
- [ ] Hot reload updates Calimero without restart
- [ ] Configuration versioning and rollback capability

### Milestone 1.3: HTTP API Integration (Weeks 5-6)

#### BAB HTTP Client Implementation
```java
@Service
@Profile("bab")
public class CCalimeroApiClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Call Calimero API with request/response pattern
     */
    public CalimeroApiResponse callApi(CBabDevice device, CalimeroApiRequest request) {
        try {
            String apiUrl = String.format("http://%s:8077/api/request", device.getIpAddress());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(device.getAuthToken());
            
            HttpEntity<CalimeroApiRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<CalimeroApiResponse> response = 
                restTemplate.postForEntity(apiUrl, entity, CalimeroApiResponse.class);
                
            return response.getBody();
            
        } catch (Exception e) {
            LOGGER.error("Calimero API call failed for device: {}", device.getId(), e);
            throw new CalimeroApiException("API call failed", e);
        }
    }
    
    /**
     * Get system information from Calimero
     */
    public CalimeroSystemInfo getSystemInfo(CBabDevice device) {
        CalimeroApiRequest request = CalimeroApiRequest.builder()
            .type("system")
            .data(Map.of("operation", "info"))
            .build();
            
        CalimeroApiResponse response = callApi(device, request);
        return objectMapper.convertValue(response.getData(), CalimeroSystemInfo.class);
    }
    
    /**
     * List all active nodes in Calimero
     */
    public List<CalimeroNodeStatus> listNodes(CBabDevice device) {
        CalimeroApiRequest request = CalimeroApiRequest.builder()
            .type("node")
            .data(Map.of("operation", "list"))
            .build();
            
        CalimeroApiResponse response = callApi(device, request);
        // Parse response.data into list of node statuses
        return parseNodeList(response.getData());
    }
    
    /**
     * Start specific node by ID
     */
    public void startNode(CBabDevice device, String nodeId) {
        CalimeroApiRequest request = CalimeroApiRequest.builder()
            .type("node")
            .data(Map.of(
                "operation", "start",
                "node_id", nodeId
            ))
            .build();
            
        CalimeroApiResponse response = callApi(device, request);
        if (response.getStatus() != 0) {
            throw new CalimeroApiException("Failed to start node: " + nodeId);
        }
    }
    
    /**
     * Get real-time metrics from Calimero
     */
    public CalimeroMetrics getMetrics(CBabDevice device) {
        // Call Prometheus /metrics endpoint
        String metricsUrl = String.format("http://%s:8077/metrics", device.getIpAddress());
        String metricsText = restTemplate.getForObject(metricsUrl, String.class);
        
        // Parse Prometheus format into structured metrics
        return parsePrometheusMetrics(metricsText);
    }
}

// Data transfer objects
@Data
@Builder
public class CalimeroApiRequest {
    private String type;        // "node", "system", "disk", etc.
    private Map<String, Object> data;
}

@Data
public class CalimeroApiResponse {
    private String type;        // "reply"
    private int status;         // 0=success, 1=error
    private Map<String, Object> data;
    private String message;     // Error message if status != 0
}

@Data
public class CalimeroSystemInfo {
    private String version;
    private long uptime;
    private double cpuUsage;
    private long memoryUsage;
    private int activeNodes;
    private long framesProcessed;
}

@Data
public class CalimeroNodeStatus {
    private String nodeId;
    private String nodeType;    // "CAN", "Serial", "Ethernet", "ROS"
    private String status;      // "RUNNING", "STOPPED", "ERROR"
    private long framesReceived;
    private long framesSent;
    private double latencyMs;
    private List<String> errors;
}

@Data
public class CalimeroMetrics {
    private Map<String, Double> frameRates;     // nodeId -> frames/second
    private Map<String, Integer> queueDepths;  // nodeId -> queue size
    private Map<String, Double> latencies;     // nodeId -> latency ms
    private double systemCpuUsage;
    private long systemMemoryUsage;
    private long totalFramesProcessed;
    private long totalDroppedFrames;
}
```

#### Calimero API Endpoints Used
Based on existing Calimero HTTP API:

**Source Location**: `/home/yasin/git/calimero/src/http/`
- HTTP server implementation in C++
- Handles REST API, WebSocket connections, authentication
- Prometheus metrics export
- Real-time interface status updates

| Endpoint | Method | Purpose | BAB Usage |
|----------|--------|---------|-----------|
| `/health` | GET | Health check | Service status monitoring |
| `/status` | GET | System status | Dashboard metrics |
| `/api/request` | POST | API operations | Node control, configuration |
| `/metrics` | GET | Prometheus metrics | Real-time performance data |

**Key Files**:
- `server.cpp` - Main HTTP server loop
- `handlers.cpp` - REST endpoint handlers  
- `websocket.cpp` - WebSocket real-time updates
- `auth.cpp` - Token-based authentication

#### Acceptance Criteria
- [ ] BAB can communicate with all Calimero HTTP endpoints
- [ ] Error handling for network failures and API errors
- [ ] Authentication using Bearer token
- [ ] Real-time data updates for dashboard

### Milestone 1.4: Basic Health Monitoring (Weeks 7-8)

#### Dashboard Implementation
```java
@Route(value = "bab/dashboard", layout = MainLayout.class)
@PageTitle("Calimero Dashboard")
@RolesAllowed("USER")
@Profile("bab")
public class CCalimeroMonitoringView extends VerticalLayout {
    
    private final CCalimeroApiClient apiClient;
    private final CBabDeviceService deviceService;
    
    // UI Components
    private Grid<CalimeroNodeStatus> nodeGrid;
    private Div systemInfoPanel;
    private Div metricsPanel;
    private ProgressBar cpuUsageBar;
    private Span memoryUsageLabel;
    
    // Auto-refresh
    private Registration refreshTimer;
    
    public CCalimeroMonitoringView(CCalimeroApiClient apiClient, CBabDeviceService deviceService) {
        this.apiClient = apiClient;
        this.deviceService = deviceService;
        
        initializeComponents();
        startAutoRefresh();
    }
    
    private void initializeComponents() {
        // System info panel
        systemInfoPanel = new Div();
        systemInfoPanel.addClassName("system-info-panel");
        add(systemInfoPanel);
        
        // Node status grid
        nodeGrid = new Grid<>(CalimeroNodeStatus.class);
        configureNodeGrid();
        add(nodeGrid);
        
        // Metrics panel
        metricsPanel = new Div();
        metricsPanel.addClassName("metrics-panel");
        add(metricsPanel);
    }
    
    private void configureNodeGrid() {
        nodeGrid.removeAllColumns();
        
        // Node ID with status color
        nodeGrid.addComponentColumn(node -> {
            Span nodeSpan = new Span(node.getNodeId());
            nodeSpan.getElement().getThemeList().add(getStatusBadgeTheme(node.getStatus()));
            return nodeSpan;
        }).setHeader("Node").setWidth("150px");
        
        // Node type with icon
        nodeGrid.addComponentColumn(node -> {
            Icon icon = getNodeTypeIcon(node.getNodeType());
            Span typeSpan = new Span(icon, new Span(" " + node.getNodeType()));
            return typeSpan;
        }).setHeader("Type").setWidth("120px");
        
        // Frame rate (real-time)
        nodeGrid.addColumn(node -> 
            String.format("%.1f fps", node.getFramesReceived() / getUptimeSeconds()))
            .setHeader("RX Rate").setWidth("100px");
            
        nodeGrid.addColumn(node -> 
            String.format("%.1f fps", node.getFramesSent() / getUptimeSeconds()))
            .setHeader("TX Rate").setWidth("100px");
        
        // Latency
        nodeGrid.addColumn(node -> String.format("%.2f ms", node.getLatencyMs()))
            .setHeader("Latency").setWidth("100px");
        
        // Status
        nodeGrid.addComponentColumn(node -> {
            Badge statusBadge = new Badge(node.getStatus());
            statusBadge.getElement().getThemeList().add(getStatusBadgeTheme(node.getStatus()));
            return statusBadge;
        }).setHeader("Status").setWidth("100px");
        
        // Actions
        nodeGrid.addComponentColumn(node -> {
            Button startBtn = new Button("Start");
            Button stopBtn = new Button("Stop");
            Button restartBtn = new Button("Restart");
            
            startBtn.addClickListener(e -> startNode(node.getNodeId()));
            stopBtn.addClickListener(e -> stopNode(node.getNodeId()));
            restartBtn.addClickListener(e -> restartNode(node.getNodeId()));
            
            HorizontalLayout actions = new HorizontalLayout(startBtn, stopBtn, restartBtn);
            return actions;
        }).setHeader("Actions").setWidth("200px");
    }
    
    private void startAutoRefresh() {
        refreshTimer = UI.getCurrent().setPollInterval(5000);  // 5 second updates
        UI.getCurrent().addPollListener(e -> refreshData());
    }
    
    private void refreshData() {
        try {
            CBabDevice device = getCurrentDevice();
            if (device == null) return;
            
            // Update system info
            CalimeroSystemInfo systemInfo = apiClient.getSystemInfo(device);
            updateSystemInfoPanel(systemInfo);
            
            // Update node list
            List<CalimeroNodeStatus> nodes = apiClient.listNodes(device);
            nodeGrid.setItems(nodes);
            
            // Update metrics
            CalimeroMetrics metrics = apiClient.getMetrics(device);
            updateMetricsPanel(metrics);
            
        } catch (Exception e) {
            LOGGER.warn("Failed to refresh Calimero data", e);
            showErrorNotification("Failed to connect to Calimero service");
        }
    }
    
    private void updateSystemInfoPanel(CalimeroSystemInfo info) {
        systemInfoPanel.removeAll();
        
        Div versionDiv = new Div("Version: " + info.getVersion());
        Div uptimeDiv = new Div("Uptime: " + formatUptime(info.getUptime()));
        
        cpuUsageBar = new ProgressBar(0, 100, info.getCpuUsage());
        Div cpuDiv = new Div("CPU Usage: " + String.format("%.1f%%", info.getCpuUsage()));
        
        memoryUsageLabel = new Span("Memory: " + formatMemory(info.getMemoryUsage()));
        
        Div nodesDiv = new Div("Active Nodes: " + info.getActiveNodes());
        Div framesDiv = new Div("Total Frames: " + String.format("%,d", info.getFramesProcessed()));
        
        systemInfoPanel.add(versionDiv, uptimeDiv, cpuDiv, cpuUsageBar, memoryUsageLabel, nodesDiv, framesDiv);
    }
    
    private void updateMetricsPanel(CalimeroMetrics metrics) {
        metricsPanel.removeAll();
        
        // Performance summary
        double totalFrameRate = metrics.getFrameRates().values().stream().mapToDouble(Double::doubleValue).sum();
        long totalDrops = metrics.getTotalDroppedFrames();
        double dropRate = (double) totalDrops / metrics.getTotalFramesProcessed() * 100;
        
        Div frameRateDiv = new Div(String.format("Total Frame Rate: %.1f fps", totalFrameRate));
        Div dropRateDiv = new Div(String.format("Drop Rate: %.3f%%", dropRate));
        
        // Queue depths
        Div queueDiv = new Div("Queue Depths:");
        metrics.getQueueDepths().forEach((nodeId, depth) -> {
            Span queueSpan = new Span(String.format("%s: %d", nodeId, depth));
            queueDiv.add(new Div(queueSpan));
        });
        
        metricsPanel.add(frameRateDiv, dropRateDiv, queueDiv);
    }
    
    // Helper methods
    private CBabDevice getCurrentDevice() {
        return deviceService.findByCompany(sessionService.getActiveCompany().orElse(null))
            .orElse(null);
    }
    
    private String getStatusBadgeTheme(String status) {
        return switch (status) {
            case "RUNNING" -> "success";
            case "ERROR" -> "error";
            case "STOPPED" -> "contrast";
            default -> "primary";
        };
    }
    
    private Icon getNodeTypeIcon(String nodeType) {
        return switch (nodeType) {
            case "CAN" -> VaadinIcon.CAR.create();
            case "Serial" -> VaadinIcon.CONNECT_O.create();
            case "Ethernet" -> VaadinIcon.CONNECT.create();
            case "ROS" -> VaadinIcon.ROBOT.create();
            default -> VaadinIcon.CIRCLE.create();
        };
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (refreshTimer != null) {
            refreshTimer.remove();
        }
        super.onDetach(detachEvent);
    }
}
```

#### Acceptance Criteria
- [ ] Real-time dashboard shows Calimero service status
- [ ] Node status, frame rates, and latency displayed accurately
- [ ] Auto-refresh updates data every 5 seconds
- [ ] Error handling for connection failures

## Phase 2: Advanced Integration (Weeks 9-16)

### Milestone 2.1: Complete Node Management (Weeks 9-12)

Implementation of all Calimero transport protocols:
- CAN configuration UI (bitrate, extended frames, filtering)
- Serial port setup (baud rate, parity, flow control)
- Ethernet endpoints (UDP/TCP, multicast)
- ROS2 integration (topics, QoS, namespaces)

### Milestone 2.2: Routing Configuration (Weeks 13-14)

Implementation of Calimero routing engine management:
- Port-to-port routing rules
- CAN ID filtering (exact, range, mask)
- Multi-destination routing
- Protocol bridging configuration

### Milestone 2.3: Log Management Interface (Weeks 15-16)

Implementation of data management features:
- Log file browser and download
- Replay configuration and control
- Data format conversion tools
- Storage management and cleanup

## Phase 3: Production Features (Weeks 17-25)

### Milestone 3.1: Performance Optimization (Weeks 17-19)

### Milestone 3.2: Production Deployment (Weeks 20-22)

### Milestone 3.3: Monitoring & Alerting (Weeks 23-25)

## Risk Management & Mitigation

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Calimero API Changes** | High | Medium | Version pinning, API compatibility testing |
| **Performance Bottlenecks** | Medium | High | Asynchronous operations, caching, profiling |
| **Configuration Complexity** | High | Medium | Comprehensive validation, rollback mechanisms |
| **Process Management Issues** | Medium | Medium | Systemd integration, health checks, auto-restart |
| **Network Connectivity Problems** | Medium | Low | Connection pooling, retry logic, offline mode |

### Integration Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **HTTP API Reliability** | High | Medium | Connection pooling, circuit breaker pattern |
| **Config File Synchronization** | High | Low | Atomic writes, validation, backup/restore |
| **Service Dependency Management** | Medium | Medium | Health checks, graceful degradation |
| **Real-time Data Flow** | Medium | High | WebSocket fallback, data buffering |

## Success Metrics

### Integration Success
- [ ] 100% Calimero API endpoint integration
- [ ] <2 second response time for configuration changes
- [ ] 99.9% service availability
- [ ] Zero data loss during configuration updates

### Performance Success
- [ ] Real-time monitoring of 50k+ fps data streams
- [ ] <500ms dashboard refresh times
- [ ] Support for 10+ concurrent BAB users
- [ ] <10MB memory footprint per device

### User Experience Success
- [ ] Single-click Calimero service management
- [ ] Intuitive node configuration workflow
- [ ] Real-time status visibility
- [ ] Comprehensive error handling and user feedback

## Testing Strategy

### Unit Testing
- **Configuration generation**: JSON schema validation
- **API client**: Mock Calimero responses  
- **Service management**: Process control testing

### Integration Testing
- **End-to-end workflow**: BAB → Calimero → Data logging
- **Error scenarios**: Network failures, service crashes
- **Performance testing**: High-throughput data monitoring

### User Acceptance Testing
- **Device management workflow**: Complete device setup
- **Monitoring dashboard**: Real-time data accuracy
- **Troubleshooting scenarios**: Error diagnosis and resolution

---

## Related Documentation

### Implementation References
- **[BAB Entity Model](ENTITY_MODEL.md)**: Entity relationships and data model
- **[BAB Architecture](ARCHITECTURE.md)**: Technical architecture overview
- **[Calimero Product Overview](../../../calimero/docs/management/01_product_overview.md)**: Calimero capabilities and scope
- **[Calimero HTTP API](../../../calimero/src/http/docs/API_REFERENCE.md)**: Complete API specification
- **[Calimero Configuration](../../../calimero/docs/CONFIGURATION.md)**: JSON configuration format

### Project Management
- **[Calimero Execution Roadmap](../../../calimero/docs/management/06_execution_roadmap.md)**: 5-phase Calimero development plan
- **[Calimero User Stories](../../../calimero/docs/management/05_user_stories.md)**: 69 stories, 272 story points
- **[Project Definition](PROJECT_DEFINITION.md)**: BAB project scope and requirements

---

**Document Control**:
- **Version**: 1.0
- **Created**: 2026-01-29  
- **Calimero Reference**: `/home/yasin/git/calimero/` - Complete C++ project location
- **Implementation Status**: Planning phase - ready for development
- **Next Review**: 2026-03-29
- **Classification**: Technical Implementation Plan