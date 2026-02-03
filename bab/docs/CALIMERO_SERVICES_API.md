# Calimero Services HTTP API Documentation

**Date**: 2026-02-03  
**Author**: Calimero Team  
**Version**: 1.0  
**Target**: Java Client Implementation (Derbent/BAB Project)

---

## Overview

This document describes the **System Services HTTP API** for Calimero, enabling Java clients (like Derbent/BAB) to manage systemd services on remote Linux systems. The API provides operations for listing services, checking status, and performing control operations (start, stop, restart, enable, disable).

---

## API Endpoint

**Base URL**: `http://<calimero-host>:8077/api/request`

**Request Format**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "<operation-name>",
        ... operation-specific parameters ...
    }
}
```

**Authentication**: Bearer token in `Authorization` header
```
Authorization: Bearer <token>
```

---

## Operations

### 1. List Services

**Operation**: `list`

**Description**: List all systemd services on the system with their current status.

**Request Parameters**:
- `activeOnly` (boolean, optional): Only return active services (default: false)
- `runningOnly` (boolean, optional): Only return running services (default: false)
- `filter` (string, optional): Case-insensitive filter for service name or description

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "list",
        "activeOnly": false,
        "runningOnly": false,
        "filter": "network"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "systemservices": [
            {
                "name": "NetworkManager.service",
                "load": "loaded",
                "active": "active",
                "sub": "running",
                "description": "Network Manager",
                "isRunning": true,
                "isActive": true,
                "isFailed": false,
                "isLoaded": true
            },
            {
                "name": "systemd-networkd.service",
                "load": "loaded",
                "active": "inactive",
                "sub": "dead",
                "description": "Network Service",
                "isRunning": false,
                "isActive": false,
                "isFailed": false,
                "isLoaded": true
            }
        ],
        "totalCount": 196,
        "count": 2,
        "activeOnly": false,
        "runningOnly": false,
        "filter": "network"
    }
}
```

**Java Data Model**:
```java
public class ServiceInfo {
    private String name;
    private String load;
    private String active;
    private String sub;
    private String description;
    private boolean isRunning;
    private boolean isActive;
    private boolean isFailed;
    private boolean isLoaded;
    
    // Getters and setters...
}

public class ServicesListResponse {
    private List<ServiceInfo> services;
    private int totalCount;
    private int count;
    private boolean activeOnly;
    private boolean runningOnly;
    private String filter;
    
    // Getters and setters...
}
```

---

### 2. Get Service Status

**Operation**: `status`

**Description**: Get detailed status and information about a specific service.

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service (e.g., "sshd.service")

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "status",
        "serviceName": "sshd.service"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "name": "sshd.service",
        "description": "OpenSSH server daemon",
        "loadState": "loaded",
        "activeState": "active",
        "subState": "running",
        "unitFileState": "enabled",
        "mainPID": 1234,
        "memoryUsage": "2097152",
        "cpuUsage": "123456789",
        "restartCount": 0,
        "isRunning": true,
        "isActive": true,
        "isFailed": false,
        "isEnabled": true,
        "recentLogs": [
            "Feb 03 10:00:00 server sshd[1234]: Server listening on 0.0.0.0 port 22.",
            "Feb 03 10:00:00 server sshd[1234]: Server listening on :: port 22."
        ]
    }
}
```

**Java Data Model**:
```java
public class ServiceStatus {
    private String name;
    private String description;
    private String loadState;
    private String activeState;
    private String subState;
    private String unitFileState;
    private int mainPID;
    private String memoryUsage;
    private String cpuUsage;
    private int restartCount;
    private boolean isRunning;
    private boolean isActive;
    private boolean isFailed;
    private boolean isEnabled;
    private List<String> recentLogs;
    
    // Getters and setters...
}
```

---

### 3. Start Service

**Operation**: `start`

**Description**: Start a systemd service.

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "start",
        "serviceName": "apache2.service"
    }
}
```

**Response Example** (Success):
```json
{
    "status": 200,
    "data": {
        "action": "start",
        "serviceName": "apache2.service",
        "result": "success",
        "message": "Service apache2.service started successfully"
    }
}
```

**Response Example** (Failure):
```json
{
    "status": 400,
    "error": "Failed to start service: apache2.service"
}
```

---

### 4. Stop Service

**Operation**: `stop`

**Description**: Stop a systemd service.

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "stop",
        "serviceName": "apache2.service"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "action": "stop",
        "serviceName": "apache2.service",
        "result": "success",
        "message": "Service apache2.service stopped successfully"
    }
}
```

---

### 5. Restart Service

**Operation**: `restart`

**Description**: Restart a systemd service (stop and start).

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "restart",
        "serviceName": "nginx.service"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "action": "restart",
        "serviceName": "nginx.service",
        "result": "success",
        "message": "Service nginx.service restarted successfully"
    }
}
```

---

### 6. Reload Service

**Operation**: `reload`

**Description**: Reload service configuration without stopping the service.

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "reload",
        "serviceName": "nginx.service"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "action": "reload",
        "serviceName": "nginx.service",
        "result": "success",
        "message": "Service nginx.service reloaded successfully"
    }
}
```

---

### 7. Enable Service

**Operation**: `enable`

**Description**: Enable a service to start automatically at boot.

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "enable",
        "serviceName": "docker.service"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "action": "enable",
        "serviceName": "docker.service",
        "result": "success",
        "message": "Service docker.service enabled successfully"
    }
}
```

---

### 8. Disable Service

**Operation**: `disable`

**Description**: Disable a service from starting automatically at boot.

**Request Parameters**:
- `serviceName` (string, **required**): Name of the service

**Request Example**:
```json
{
    "type": "systemservices",
    "data": {
        "operation": "disable",
        "serviceName": "docker.service"
    }
}
```

**Response Example**:
```json
{
    "status": 200,
    "data": {
        "action": "disable",
        "serviceName": "docker.service",
        "result": "success",
        "message": "Service docker.service disabled successfully"
    }
}
```

---

## Error Handling

### Common Error Responses

**Missing Operation**:
```json
{
    "status": 400,
    "error": "Missing 'operation' field"
}
```

**Unknown Operation**:
```json
{
    "status": 400,
    "error": "Unknown operation: invalid_op"
}
```

**Missing Service Name**:
```json
{
    "status": 400,
    "error": "Missing 'serviceName' field"
}
```

**Service Not Found**:
```json
{
    "status": 400,
    "error": "Service not found: nonexistent.service"
}
```

**Permission Denied**:
```json
{
    "status": 500,
    "error": "Failed to start service: Permission denied"
}
```

**Server Error**:
```json
{
    "status": 500,
    "error": "Internal server error: <details>"
}
```

---

## Implementation Guide for Java Client

### 1. HTTP Client Setup

Use the existing `CalimeroClient` pattern with the services message type.

**Example Configuration**:
```java
public class CalimeroServicesClient extends CalimeroClientBase {
    
    private static final String MESSAGE_TYPE = "systemservices";
    
    public CalimeroServicesClient(String baseUrl, String authToken) {
        super(baseUrl, authToken);
    }
    
    // Implementation methods...
}
```

---

### 2. Data Transfer Objects (DTOs)

Create DTOs for request and response data:

**ServiceListRequest.java**:
```java
public class ServiceListRequest {
    private boolean activeOnly;
    private boolean runningOnly;
    private String filter;
    
    public ServiceListRequest() {
        this.activeOnly = false;
        this.runningOnly = false;
    }
    
    // Builder pattern
    public ServiceListRequest activeOnly(boolean activeOnly) {
        this.activeOnly = activeOnly;
        return this;
    }
    
    public ServiceListRequest runningOnly(boolean runningOnly) {
        this.runningOnly = runningOnly;
        return this;
    }
    
    public ServiceListRequest filter(String filter) {
        this.filter = filter;
        return this;
    }
    
    // Getters...
}
```

**ServiceInfo.java**:
```java
public class ServiceInfo {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("load")
    private String load;
    
    @JsonProperty("active")
    private String active;
    
    @JsonProperty("sub")
    private String sub;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("isRunning")
    private boolean isRunning;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("isFailed")
    private boolean isFailed;
    
    @JsonProperty("isLoaded")
    private boolean isLoaded;
    
    // Getters and setters...
}
```

**ServiceStatus.java**:
```java
public class ServiceStatus {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("loadState")
    private String loadState;
    
    @JsonProperty("activeState")
    private String activeState;
    
    @JsonProperty("subState")
    private String subState;
    
    @JsonProperty("unitFileState")
    private String unitFileState;
    
    @JsonProperty("mainPID")
    private int mainPID;
    
    @JsonProperty("memoryUsage")
    private String memoryUsage;
    
    @JsonProperty("cpuUsage")
    private String cpuUsage;
    
    @JsonProperty("restartCount")
    private int restartCount;
    
    @JsonProperty("isRunning")
    private boolean isRunning;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("isFailed")
    private boolean isFailed;
    
    @JsonProperty("isEnabled")
    private boolean isEnabled;
    
    @JsonProperty("recentLogs")
    private List<String> recentLogs;
    
    // Getters and setters...
}
```

**ServiceActionResult.java**:
```java
public class ServiceActionResult {
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("serviceName")
    private String serviceName;
    
    @JsonProperty("result")
    private String result;
    
    @JsonProperty("message")
    private String message;
    
    public boolean isSuccess() {
        return "success".equals(result);
    }
    
    // Getters and setters...
}
```

---

### 3. Client Implementation

**CalimeroServicesClient.java**:
```java
public class CalimeroServicesClient extends CalimeroClientBase {
    
    private static final String MESSAGE_TYPE = "systemservices";
    private static final Logger logger = LoggerFactory.getLogger(CalimeroServicesClient.class);
    
    public CalimeroServicesClient(String baseUrl, String authToken) {
        super(baseUrl, authToken);
    }
    
    /**
     * List all services
     */
    public List<ServiceInfo> listServices() throws IOException {
        return listServices(new ServiceListRequest());
    }
    
    /**
     * List services with filters
     */
    public List<ServiceInfo> listServices(ServiceListRequest request) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "list");
        
        if (request.isActiveOnly()) {
            data.put("activeOnly", true);
        }
        if (request.isRunningOnly()) {
            data.put("runningOnly", true);
        }
        if (request.getFilter() != null && !request.getFilter().isEmpty()) {
            data.put("filter", request.getFilter());
        }
        
        JsonNode response = sendRequest(MESSAGE_TYPE, data);
        
        // Parse services array
        JsonNode servicesNode = response.get("systemservices");
        List<ServiceInfo> services = new ArrayList<>();
        
        if (servicesNode != null && servicesNode.isArray()) {
            for (JsonNode serviceNode : servicesNode) {
                ServiceInfo service = objectMapper.treeToValue(serviceNode, ServiceInfo.class);
                services.add(service);
            }
        }
        
        logger.info("Listed {} services", services.size());
        return services;
    }
    
    /**
     * Get detailed status of a service
     */
    public ServiceStatus getServiceStatus(String serviceName) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "status");
        data.put("serviceName", serviceName);
        
        JsonNode response = sendRequest(MESSAGE_TYPE, data);
        
        ServiceStatus status = objectMapper.treeToValue(response, ServiceStatus.class);
        logger.info("Got status for service: {}", serviceName);
        return status;
    }
    
    /**
     * Start a service
     */
    public ServiceActionResult startService(String serviceName) throws IOException {
        return performServiceAction("start", serviceName);
    }
    
    /**
     * Stop a service
     */
    public ServiceActionResult stopService(String serviceName) throws IOException {
        return performServiceAction("stop", serviceName);
    }
    
    /**
     * Restart a service
     */
    public ServiceActionResult restartService(String serviceName) throws IOException {
        return performServiceAction("restart", serviceName);
    }
    
    /**
     * Reload a service
     */
    public ServiceActionResult reloadService(String serviceName) throws IOException {
        return performServiceAction("reload", serviceName);
    }
    
    /**
     * Enable a service (auto-start at boot)
     */
    public ServiceActionResult enableService(String serviceName) throws IOException {
        return performServiceAction("enable", serviceName);
    }
    
    /**
     * Disable a service (no auto-start at boot)
     */
    public ServiceActionResult disableService(String serviceName) throws IOException {
        return performServiceAction("disable", serviceName);
    }
    
    /**
     * Helper method for service actions
     */
    private ServiceActionResult performServiceAction(String action, String serviceName) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", action);
        data.put("serviceName", serviceName);
        
        JsonNode response = sendRequest(MESSAGE_TYPE, data);
        
        ServiceActionResult result = objectMapper.treeToValue(response, ServiceActionResult.class);
        logger.info("Performed {} on service {} with result: {}", action, serviceName, result.getResult());
        return result;
    }
}
```

---

### 4. Usage Examples

**Basic Listing**:
```java
CalimeroServicesClient client = new CalimeroServicesClient("http://localhost:8077", "test-token-123");

// List all services
List<ServiceInfo> allServices = client.listServices();
System.out.println("Total services: " + allServices.size());

// List only running services
ServiceListRequest request = new ServiceListRequest()
    .runningOnly(true);
List<ServiceInfo> runningServices = client.listServices(request);
```

**Filter Services**:
```java
// Find services containing "network"
ServiceListRequest request = new ServiceListRequest()
    .filter("network");
List<ServiceInfo> networkServices = client.listServices(request);
```

**Get Service Status**:
```java
ServiceStatus status = client.getServiceStatus("sshd.service");
System.out.println("Service: " + status.getName());
System.out.println("Status: " + status.getActiveState());
System.out.println("Running: " + status.isRunning());
System.out.println("Enabled: " + status.isEnabled());
System.out.println("PID: " + status.getMainPID());
```

**Control Services**:
```java
// Start service
ServiceActionResult result = client.startService("apache2.service");
if (result.isSuccess()) {
    System.out.println("Service started: " + result.getMessage());
}

// Restart service
result = client.restartService("nginx.service");

// Stop service
result = client.stopService("apache2.service");
```

**Enable/Disable Services**:
```java
// Enable at boot
ServiceActionResult result = client.enableService("docker.service");

// Disable at boot
result = client.disableService("docker.service");
```

---

## Testing

### Manual Testing with curl

**List all services**:
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "systemservices",
    "data": {
      "operation": "list"
    }
  }'
```

**Get service status**:
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "systemservices",
    "data": {
      "operation": "status",
      "serviceName": "sshd.service"
    }
  }'
```

**Start service**:
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "systemservices",
    "data": {
      "operation": "start",
      "serviceName": "apache2.service"
    }
  }'
```

---

## Security Considerations

1. **Authentication**: Always use Bearer token authentication
2. **Permissions**: Service control operations require appropriate system privileges
3. **Validation**: Always validate service names on the client side
4. **Error Handling**: Implement proper error handling for permission denied scenarios
5. **Logging**: Log all service control operations for audit purposes

---

## Next Steps

1. ✅ Create Java DTOs (ServiceInfo, ServiceStatus, ServiceActionResult)
2. ✅ Implement CalimeroServicesClient extending CalimeroClientBase
3. ✅ Add unit tests for client methods
4. ✅ Integrate with Vaadin UI components
5. ✅ Add service management views in BAB dashboard
6. ✅ Implement real-time service status monitoring

---

## References

- Calimero Architecture: `/home/yasin/git/calimero/docs/ARCHITECTURE.md`
- HTTP Server Implementation: `/home/yasin/git/calimero/src/http/`
- Services Processor: `/home/yasin/git/calimero/src/http/webservice/processors/cservicesprocessor.cpp`

---

**End of Document**
