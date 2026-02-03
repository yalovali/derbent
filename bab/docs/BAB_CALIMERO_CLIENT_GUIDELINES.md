# BAB Calimero Client Implementation Guidelines

**Project**: Derbent/BAB  
**Date**: 2026-02-03  
**Target**: Java client implementation for Calimero HTTP APIs  
**Status**: Coding Standards & Best Practices

---

## Overview

This document provides comprehensive guidelines for implementing Java HTTP clients that communicate with the Calimero server. It covers both the **Web Service Discovery API** (API metadata) and the **System Services Management API** (systemd services).

**Key Principles**:
- Follow existing BAB patterns (see `CAbstractCalimeroClient`)
- Thread-safe by design
- Graceful degradation on failure
- Consistent error handling
- Comprehensive logging

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Client Implementation Pattern](#client-implementation-pattern)
3. [Service Discovery Client](#service-discovery-client)
4. [Services Management Client](#services-management-client)
5. [DTO Implementation](#dto-implementation)
6. [Error Handling](#error-handling)
7. [Logging Standards](#logging-standards)
8. [Testing Guidelines](#testing-guidelines)
9. [Code Examples](#code-examples)

---

## 1. Project Structure

```
tech.derbent.bab.dashboard.service/
├── CAbstractCalimeroClient.java           (Base class - EXISTING)
├── CWebServiceDiscoveryCalimeroClient.java   (NEW - API discovery)
├── CSystemdServicesCalimeroClient.java    (NEW - systemd management)
└── ...

tech.derbent.bab.dashboard.dto/
├── CDTOWebServiceEndpoint.java                      (NEW - API metadata)
├── CSystemdService.java                   (NEW - service info)
├── CSystemdServiceStatus.java             (NEW - detailed status)
├── CSystemdServiceAction.java             (NEW - action result)
└── ...
```

---

## 2. Client Implementation Pattern

### 2.1 Base Class Structure

All Calimero clients MUST extend `CAbstractCalimeroClient`:

```java
package tech.derbent.bab.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.bab.http.clientproject.domain.CClientProject;

/**
 * Client for [describe functionality].
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>operation1 - Description</li>
 *   <li>operation2 - Description</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe.
 * <p>
 * Error Handling: Methods return empty collections on failure.
 * Check logs for error details.
 */
public class CYourCalimeroClient extends CAbstractCalimeroClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CYourCalimeroClient.class);
    
    public CYourCalimeroClient(final CClientProject clientProject) {
        super(clientProject);
    }
    
    // Implementation methods...
}
```

### 2.2 Naming Conventions

**Class Names**:
- Pattern: `C<Domain><Purpose>CalimeroClient`
- Examples: `CWebServiceDiscoveryCalimeroClient`, `CSystemdServicesCalimeroClient`
- Prefix: Always `C` (BAB convention)
- Suffix: Always `CalimeroClient`

**Method Names**:
- Pattern: `fetch<Entity>()` for queries
- Pattern: `perform<Action>()` or `execute<Action>()` for commands
- Examples: `fetchEndpoints()`, `fetchServices()`, `startService()`, `stopService()`

**Variable Names**:
- camelCase for locals
- Constants: `UPPER_SNAKE_CASE`
- No Hungarian notation

---

## 3. Service Discovery Client

### 3.1 Purpose

Fetch metadata about available Calimero HTTP API endpoints (like Swagger/OpenAPI).

### 3.2 Calimero API

**Message Type**: `"webservice"` (singular)  
**Operations**: `list`

### 3.3 Implementation Template

```java
package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CDTOWebServiceEndpoint;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Client for discovering available Calimero HTTP API endpoints.
 * <p>
 * Provides introspection capability to discover what APIs are available
 * on the connected Calimero server. Similar to Swagger/OpenAPI metadata.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>list - Get list of all available API endpoints</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe.
 * <p>
 * Error Handling: Returns empty list on failure. Check logs for details.
 * 
 * @see CDTOWebServiceEndpoint
 * @see CAbstractCalimeroClient
 */
public class CWebServiceDiscoveryCalimeroClient extends CAbstractCalimeroClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CWebServiceDiscoveryCalimeroClient.class);
    
    public CWebServiceDiscoveryCalimeroClient(final CClientProject clientProject) {
        super(clientProject);
    }
    
    /**
     * Fetch list of available API endpoints from Calimero server.
     * <p>
     * Calimero API: POST /api/request with type="webservice", operation="list"
     * <p>
     * Response contains all available endpoints with their operations,
     * descriptions, and required parameters.
     * 
     * @return List of API endpoints (empty on failure)
     */
    public List<CDTOWebServiceEndpoint> fetchEndpoints() {
        final List<CDTOWebServiceEndpoint> endpoints = new ArrayList<>();
        
        try {
            LOGGER.debug("Fetching API endpoints from Calimero server");
            
            final CCalimeroRequest request = CCalimeroRequest.builder()
                    .type("webservice")           // Message type: "webservice"
                    .operation("list")         // Operation: list
                    .build();
            
            final CCalimeroResponse response = sendRequest(request);
            
            if (!response.isSuccess()) {
                final String message = "Failed to load API endpoints: " + response.getErrorMessage();
                LOGGER.warn(message);
                return endpoints; // Graceful degradation
            }
            
            final JsonObject data = toJsonObject(response);
            
            // Parse "systemservices" array (confusing name, but that's what Calimero returns)
            if (data.has("systemservices") && data.get("systemservices").isJsonArray()) {
                final JsonArray servicesArray = data.getAsJsonArray("systemservices");
                for (final JsonElement element : servicesArray) {
                    if (element.isJsonObject()) {
                        final CDTOWebServiceEndpoint endpoint = CDTOWebServiceEndpoint.createFromJson(element.getAsJsonObject());
                        endpoints.add(endpoint);
                    }
                }
            }
            
            LOGGER.info("Fetched {} API endpoints from Calimero", endpoints.size());
            return endpoints;
            
        } catch (final Exception e) {
            LOGGER.error("Failed to fetch API endpoints: {}", e.getMessage(), e);
            CNotificationService.showException("Failed to fetch API endpoints", e);
            return Collections.emptyList();
        }
    }
}
```

### 3.4 DTO: CDTOWebServiceEndpoint

```java
package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOWebServiceEndpoint - API endpoint metadata from service discovery.
 * <p>
 * Represents a single HTTP API endpoint with its operations and parameters.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "type": "systemservices",
 *   "action": "list",
 *   "description": "List all systemd services",
 *   "parameters": {
 *     "activeOnly": "boolean (optional)",
 *     "runningOnly": "boolean (optional)"
 *   },
 *   "endpoint": "/api/v1/message"
 * }
 * </pre>
 */
public class CDTOWebServiceEndpoint extends CObject {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CDTOWebServiceEndpoint.class);
    
    public static CDTOWebServiceEndpoint createFromJson(final JsonObject json) {
        final CDTOWebServiceEndpoint endpoint = new CDTOWebServiceEndpoint();
        endpoint.fromJson(json);
        return endpoint;
    }
    
    private String type = "";
    private String action = "";
    private String description = "";
    private JsonObject parameters = new JsonObject();
    private String endpoint = "";
    
    public CDTOWebServiceEndpoint() {
        // Default constructor
    }
    
    @Override
    protected void fromJson(final JsonObject json) {
        try {
            if (json.has("type")) {
                type = json.get("type").getAsString();
            }
            if (json.has("action")) {
                action = json.get("action").getAsString();
            }
            if (json.has("description")) {
                description = json.get("description").getAsString();
            }
            if (json.has("parameters") && json.get("parameters").isJsonObject()) {
                parameters = json.getAsJsonObject("parameters");
            }
            if (json.has("endpoint")) {
                endpoint = json.get("endpoint").getAsString();
            }
        } catch (final Exception e) {
            LOGGER.error("Error parsing CDTOWebServiceEndpoint from JSON: {}", e.getMessage());
        }
    }
    
    @Override
    protected String toJson() {
        return "{}"; // Read-only from server
    }
    
    // Getters
    public String getType() { return type; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
    public JsonObject getParameters() { return parameters; }
    public String getEndpoint() { return endpoint; }
    
    /**
     * Get full operation name (type.action).
     * @return Formatted operation name (e.g., "services.list")
     */
    public String getFullOperation() {
        return String.format("%s.%s", type, action);
    }
}
```

---

## 4. Services Management Client

### 4.1 Purpose

Manage Linux systemd services (start, stop, restart, enable, disable).

### 4.2 Calimero API

**Message Type**: `"systemservices"` (plural)  
**Operations**: `list`, `status`, `start`, `stop`, `restart`, `reload`, `enable`, `disable`

### 4.3 Implementation Template

```java
package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CSystemdService;
import tech.derbent.bab.dashboard.dto.CSystemdServiceStatus;
import tech.derbent.bab.dashboard.dto.CSystemdServiceAction;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Client for managing Linux systemd services via Calimero.
 * <p>
 * Provides full lifecycle management of system services:
 * <ul>
 *   <li>Query: list, status</li>
 *   <li>Control: start, stop, restart, reload</li>
 *   <li>Configuration: enable, disable</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe.
 * <p>
 * Error Handling: Query methods return empty collections on failure.
 * Control methods return false on failure. Check logs for details.
 * 
 * @see CSystemdService
 * @see CSystemdServiceStatus
 * @see CSystemdServiceAction
 */
public class CSystemdServicesCalimeroClient extends CAbstractCalimeroClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemdServicesCalimeroClient.class);
    
    public CSystemdServicesCalimeroClient(final CClientProject clientProject) {
        super(clientProject);
    }
    
    /**
     * Fetch list of systemd services.
     * <p>
     * Calimero API: POST /api/request with type="systemservices", operation="list"
     * 
     * @param activeOnly Only return active services
     * @param runningOnly Only return running services
     * @param filter Filter by name/description (case-insensitive)
     * @return List of services (empty on failure)
     */
    public List<CSystemdService> fetchServices(final boolean activeOnly, 
                                               final boolean runningOnly, 
                                               final String filter) {
        final List<CSystemdService> services = new ArrayList<>();
        
        try {
            LOGGER.debug("Fetching systemd services - activeOnly: {}, runningOnly: {}, filter: '{}'", 
                activeOnly, runningOnly, filter);
            
            final CCalimeroRequest.Builder builder = CCalimeroRequest.builder()
                    .type("systemservices")
                    .operation("list");
            
            if (activeOnly) {
                builder.parameter("activeOnly", true);
            }
            if (runningOnly) {
                builder.parameter("runningOnly", true);
            }
            if (filter != null && !filter.isEmpty()) {
                builder.parameter("filter", filter);
            }
            
            final CCalimeroRequest request = builder.build();
            final CCalimeroResponse response = sendRequest(request);
            
            if (!response.isSuccess()) {
                final String message = "Failed to load services: " + response.getErrorMessage();
                LOGGER.warn(message);
                return services;
            }
            
            final JsonObject data = toJsonObject(response);
            
            if (data.has("systemservices") && data.get("systemservices").isJsonArray()) {
                final JsonArray servicesArray = data.getAsJsonArray("systemservices");
                for (final JsonElement element : servicesArray) {
                    if (element.isJsonObject()) {
                        final CSystemdService service = CSystemdService.createFromJson(element.getAsJsonObject());
                        services.add(service);
                    }
                }
            }
            
            LOGGER.info("Fetched {} systemd services from Calimero", services.size());
            return services;
            
        } catch (final Exception e) {
            LOGGER.error("Failed to fetch systemd services: {}", e.getMessage(), e);
            CNotificationService.showException("Failed to fetch services", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Fetch list of all services (convenience method).
     */
    public List<CSystemdService> fetchAllServices() {
        return fetchServices(false, false, null);
    }
    
    /**
     * Fetch detailed status of a specific service.
     * <p>
     * Calimero API: POST /api/request with type="systemservices", operation="status"
     * 
     * @param serviceName Name of service (e.g., "sshd.service")
     * @return Service status (null on failure)
     */
    public CSystemdServiceStatus fetchServiceStatus(final String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            LOGGER.warn("Service name is required");
            return null;
        }
        
        try {
            LOGGER.debug("Fetching status for service: {}", serviceName);
            
            final CCalimeroRequest request = CCalimeroRequest.builder()
                    .type("systemservices")
                    .operation("status")
                    .parameter("serviceName", serviceName)
                    .build();
            
            final CCalimeroResponse response = sendRequest(request);
            
            if (!response.isSuccess()) {
                final String message = "Failed to get service status: " + response.getErrorMessage();
                LOGGER.warn(message);
                return null;
            }
            
            final JsonObject data = toJsonObject(response);
            final CSystemdServiceStatus status = CSystemdServiceStatus.createFromJson(data);
            
            LOGGER.info("Fetched status for service: {}", serviceName);
            return status;
            
        } catch (final Exception e) {
            LOGGER.error("Failed to fetch service status: {}", e.getMessage(), e);
            CNotificationService.showException("Failed to fetch service status", e);
            return null;
        }
    }
    
    /**
     * Start a systemd service.
     * <p>
     * Calimero API: POST /api/request with type="systemservices", operation="start"
     * 
     * @param serviceName Name of service
     * @return Action result (null on failure)
     */
    public CSystemdServiceAction startService(final String serviceName) {
        return executeServiceAction("start", serviceName);
    }
    
    /**
     * Stop a systemd service.
     */
    public CSystemdServiceAction stopService(final String serviceName) {
        return executeServiceAction("stop", serviceName);
    }
    
    /**
     * Restart a systemd service.
     */
    public CSystemdServiceAction restartService(final String serviceName) {
        return executeServiceAction("restart", serviceName);
    }
    
    /**
     * Reload service configuration.
     */
    public CSystemdServiceAction reloadService(final String serviceName) {
        return executeServiceAction("reload", serviceName);
    }
    
    /**
     * Enable service (start at boot).
     */
    public CSystemdServiceAction enableService(final String serviceName) {
        return executeServiceAction("enable", serviceName);
    }
    
    /**
     * Disable service (do not start at boot).
     */
    public CSystemdServiceAction disableService(final String serviceName) {
        return executeServiceAction("disable", serviceName);
    }
    
    /**
     * Execute service action (internal helper).
     */
    private CSystemdServiceAction executeServiceAction(final String operation, final String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            LOGGER.warn("Service name is required for operation: {}", operation);
            return null;
        }
        
        try {
            LOGGER.debug("Executing {} on service: {}", operation, serviceName);
            
            final CCalimeroRequest request = CCalimeroRequest.builder()
                    .type("systemservices")
                    .operation(operation)
                    .parameter("serviceName", serviceName)
                    .build();
            
            final CCalimeroResponse response = sendRequest(request);
            
            if (!response.isSuccess()) {
                final String message = String.format("Failed to %s service %s: %s", 
                    operation, serviceName, response.getErrorMessage());
                LOGGER.warn(message);
                CNotificationService.showError(message);
                return null;
            }
            
            final JsonObject data = toJsonObject(response);
            final CSystemdServiceAction action = CSystemdServiceAction.createFromJson(data);
            
            if (action.isSuccess()) {
                LOGGER.info("Successfully executed {} on service: {}", operation, serviceName);
                CNotificationService.showSuccess(action.getMessage());
            } else {
                LOGGER.warn("Action failed: {}", action.getMessage());
                CNotificationService.showWarning(action.getMessage());
            }
            
            return action;
            
        } catch (final Exception e) {
            LOGGER.error("Failed to execute {} on service {}: {}", operation, serviceName, e.getMessage(), e);
            CNotificationService.showException(String.format("Failed to %s service", operation), e);
            return null;
        }
    }
}
```

### 4.4 DTOs for Services Management

See separate file: `CALIMERO_SERVICES_DTOS.md` for complete DTO implementations:
- `CSystemdService` - Service list item
- `CSystemdServiceStatus` - Detailed status
- `CSystemdServiceAction` - Action result

---

## 5. DTO Implementation

### 5.1 Base Pattern

All DTOs MUST:
- Extend `CObject` (BAB base class)
- Implement `createFromJson()` static factory
- Override `fromJson()` for parsing
- Override `toJson()` (return `"{}"` for read-only)
- Provide getters (no setters for immutable data)
- Handle null/missing fields gracefully
- Log parsing errors

### 5.2 Template

```java
package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CYourDto - [Description].
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "field1": "value",
 *   "field2": 123
 * }
 * </pre>
 */
public class CYourDto extends CObject {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CYourDto.class);
    
    public static CYourDto createFromJson(final JsonObject json) {
        final CYourDto dto = new CYourDto();
        dto.fromJson(json);
        return dto;
    }
    
    private String field1 = "";
    private int field2 = 0;
    
    public CYourDto() {
        // Default constructor
    }
    
    @Override
    protected void fromJson(final JsonObject json) {
        try {
            if (json.has("field1")) {
                field1 = json.get("field1").getAsString();
            }
            if (json.has("field2")) {
                field2 = json.get("field2").getAsInt();
            }
        } catch (final Exception e) {
            LOGGER.error("Error parsing CYourDto from JSON: {}", e.getMessage());
        }
    }
    
    @Override
    protected String toJson() {
        return "{}"; // Read-only from server
    }
    
    // Getters
    public String getField1() { return field1; }
    public int getField2() { return field2; }
}
```

---

## 6. Error Handling

### 6.1 Principles

1. **Graceful Degradation**: Return empty collections/null on failure
2. **No Exceptions in Public Methods**: Catch all exceptions internally
3. **Logging**: Always log errors with context
4. **User Notifications**: Use `CNotificationService` for user-facing errors
5. **Check Response**: Always validate `response.isSuccess()` before parsing

### 6.2 Pattern

```java
public List<CEntity> fetchEntities() {
    final List<CEntity> entities = new ArrayList<>();
    
    try {
        LOGGER.debug("Fetching entities");
        
        final CCalimeroRequest request = CCalimeroRequest.builder()
                .type("entityType")
                .operation("list")
                .build();
        
        final CCalimeroResponse response = sendRequest(request);
        
        // Check success BEFORE parsing
        if (!response.isSuccess()) {
            LOGGER.warn("Failed to fetch entities: {}", response.getErrorMessage());
            return entities; // Empty list = graceful degradation
        }
        
        // Parse response
        final JsonObject data = toJsonObject(response);
        // ... parse entities ...
        
        LOGGER.info("Fetched {} entities", entities.size());
        return entities;
        
    } catch (final Exception e) {
        LOGGER.error("Failed to fetch entities: {}", e.getMessage(), e);
        CNotificationService.showException("Failed to fetch entities", e);
        return Collections.emptyList(); // Always return non-null
    }
}
```

---

## 7. Logging Standards

### 7.1 Log Levels

- **DEBUG**: Method entry/exit, detailed flow
- **INFO**: Successful operations with counts/summary
- **WARN**: Recoverable errors, failed requests (graceful degradation)
- **ERROR**: Unexpected exceptions, parsing failures

### 7.2 Log Format

```java
// DEBUG: Method entry with parameters
LOGGER.debug("Fetching services - activeOnly: {}, filter: '{}'", activeOnly, filter);

// INFO: Success with summary
LOGGER.info("Fetched {} services from Calimero", services.size());

// WARN: Failed request (not exception)
LOGGER.warn("Failed to load services: {}", response.getErrorMessage());

// ERROR: Exception caught
LOGGER.error("Failed to fetch services: {}", e.getMessage(), e);
```

### 7.3 Emojis in Logs (Optional)

BAB uses emojis in some logs for visual clarity (see `CAbstractCalimeroClient`):

```java
LOGGER.debug("📤 Sending Calimero request - type: {}, operation: {}", type, operation);
LOGGER.debug("✅ Calimero request successful");
LOGGER.warn("⚠️ Calimero request failed");
```

---

## 8. Testing Guidelines

### 8.1 Unit Tests

Create test classes in `src/test/java/`:

```java
package tech.derbent.bab.dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.derbent.bab.http.clientproject.domain.CClientProject;

class CSystemdServicesCalimeroClientTest {
    
    @Mock
    private CClientProject clientProject;
    
    private CSystemdServicesCalimeroClient client;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new CSystemdServicesCalimeroClient(clientProject);
    }
    
    @Test
    void testFetchAllServices_Success() {
        // TODO: Mock successful response
        // TODO: Verify parsing
        // TODO: Assert results
    }
    
    @Test
    void testFetchAllServices_Failure() {
        // TODO: Mock failed response
        // TODO: Verify graceful degradation (empty list)
    }
}
```

### 8.2 Integration Tests

Test against real Calimero server (optional):

```java
// Mark as integration test
@Tag("integration")
@Disabled("Requires running Calimero server")
class CSystemdServicesCalimeroClientIntegrationTest {
    
    private CSystemdServicesCalimeroClient client;
    
    @BeforeEach
    void setUp() {
        // Setup real connection to Calimero
        // client = ...
    }
    
    @Test
    void testRealCalimeroConnection() {
        final List<CSystemdService> services = client.fetchAllServices();
        assertNotNull(services);
        assertTrue(services.size() > 0);
    }
}
```

---

## 9. Code Examples

### 9.1 Complete Service Discovery Example

See: `CWebServiceDiscoveryCalimeroClient` in section 3.3

### 9.2 Complete Services Management Example

See: `CSystemdServicesCalimeroClient` in section 4.3

### 9.3 Using Clients in UI Components

```java
package tech.derbent.bab.dashboard.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import tech.derbent.bab.dashboard.service.CSystemdServicesCalimeroClient;
import tech.derbent.bab.dashboard.dto.CSystemdService;

@Route("systemservices")
public class ServicesView extends VerticalLayout {
    
    private final CSystemdServicesCalimeroClient client;
    private final Grid<CSystemdService> grid = new Grid<>();
    
    public ServicesView(CSystemdServicesCalimeroClient client) {
        this.client = client;
        setupGrid();
        loadServices();
    }
    
    private void setupGrid() {
        grid.addColumn(CSystemdService::getName).setHeader("Service");
        grid.addColumn(CSystemdService::getStatusDisplay).setHeader("Status");
        grid.addColumn(CSystemdService::getDescription).setHeader("Description");
        add(grid);
    }
    
    private void loadServices() {
        final List<CSystemdService> services = client.fetchAllServices();
        grid.setItems(services);
    }
}
```

---

## 10. Checklist

Before committing, verify:

- [ ] Class extends `CAbstractCalimeroClient`
- [ ] JavaDoc with `@see`, `@param`, `@return`
- [ ] Thread-safe (stateless or synchronized)
- [ ] Graceful error handling (no public exceptions)
- [ ] Logging at appropriate levels
- [ ] DTOs extend `CObject`
- [ ] DTOs have `createFromJson()` factory
- [ ] Null/missing field handling in DTOs
- [ ] Unit tests created
- [ ] Code follows BAB naming conventions
- [ ] No hardcoded URLs/credentials

---

## 11. References

**Calimero API Documentation**:
- `~/git/derbent/bab/docs/CALIMERO_SERVICES_API.md` - System Services Management API
- `~/git/derbent/bab/docs/CALIMERO_SERVICES_QUICK_GUIDE.md` - Implementation Guide

**BAB Project Patterns**:
- `CAbstractCalimeroClient.java` - Base class
- `CSystemServiceCalimeroClient.java` - Example (old service discovery)
- `CNetworkInterfaceCalimeroClient.java` - Example network client
- `CSystemMetricsCalimeroClient.java` - Example metrics client

**Calimero Backend**:
- `/home/yasin/git/calimero/src/http/webservice/handlers/cservicediscoveryhandler.cpp`
- `/home/yasin/git/calimero/src/http/webservice/processors/cservicesprocessor.cpp`

---

**Last Updated**: 2026-02-03  
**Version**: 1.0  
**Status**: Ready for implementation

---

**Happy Coding!** 🚀
