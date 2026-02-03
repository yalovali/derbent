# BAB Calimero DTOs - Complete Specifications

**Project**: Derbent/BAB  
**Date**: 2026-02-03  
**Purpose**: Data Transfer Object specifications for Calimero HTTP APIs

---

## Overview

This document provides complete implementations for all DTOs required for:
1. **Web Service Discovery API** - API metadata DTOs
2. **System Services Management API** - Systemd service DTOs

All DTOs follow BAB conventions and extend `CObject`.

---

## Table of Contents

1. [Service Discovery DTOs](#service-discovery-dtos)
2. [Services Management DTOs](#services-management-dtos)
3. [Common Patterns](#common-patterns)

---

## 1. Service Discovery DTOs

### 1.1 CDTOWebServiceEndpoint

**Purpose**: Represents a single API endpoint from service discovery.

**Package**: `tech.derbent.bab.dashboard.dto`

**JSON Example**:
```json
{
  "type": "systemservices",
  "action": "list",
  "description": "List all systemd services",
  "parameters": {
    "activeOnly": "boolean (optional)",
    "runningOnly": "boolean (optional)",
    "filter": "string (optional)"
  },
  "endpoint": "/api/v1/message"
}
```

**Implementation**:

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
 * Used for API introspection and documentation.
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
 * <p>
 * Thread Safety: Immutable after construction.
 */
public class CDTOWebServiceEndpoint extends CObject {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CDTOWebServiceEndpoint.class);
    
    /**
     * Create from JSON object.
     * @param json JSON object from Calimero
     * @return New instance
     */
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
    
    /**
     * Check if endpoint has required parameters.
     * @return true if any parameter is marked as "required"
     */
    public boolean hasRequiredParameters() {
        if (parameters == null || parameters.size() == 0) {
            return false;
        }
        for (final String key : parameters.keySet()) {
            final String value = parameters.get(key).getAsString();
            if (value != null && value.toLowerCase().contains("required")) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("CDTOWebServiceEndpoint{type='%s', action='%s', description='%s'}", 
            type, action, description);
    }
}
```

---

## 2. Services Management DTOs

### 2.1 CSystemdService

**Purpose**: Service list item (basic information).

**Package**: `tech.derbent.bab.dashboard.dto`

**JSON Example**:
```json
{
  "name": "sshd.service",
  "load": "loaded",
  "active": "active",
  "sub": "running",
  "description": "OpenSSH server daemon",
  "isRunning": true,
  "isActive": true,
  "isFailed": false,
  "isLoaded": true
}
```

**Implementation**:

```java
package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CSystemdService - Systemd service information from list operation.
 * <p>
 * Represents basic information about a systemd service.
 * For detailed status, use CSystemdServiceStatus.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "name": "sshd.service",
 *   "load": "loaded",
 *   "active": "active",
 *   "sub": "running",
 *   "description": "OpenSSH server daemon",
 *   "isRunning": true,
 *   "isActive": true,
 *   "isFailed": false,
 *   "isLoaded": true
 * }
 * </pre>
 * <p>
 * Thread Safety: Immutable after construction.
 */
public class CSystemdService extends CObject {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemdService.class);
    
    public static CSystemdService createFromJson(final JsonObject json) {
        final CSystemdService service = new CSystemdService();
        service.fromJson(json);
        return service;
    }
    
    private String name = "";
    private String load = "";
    private String active = "";
    private String sub = "";
    private String description = "";
    private boolean isRunning = false;
    private boolean isActive = false;
    private boolean isFailed = false;
    private boolean isLoaded = false;
    
    public CSystemdService() {
        // Default constructor
    }
    
    @Override
    protected void fromJson(final JsonObject json) {
        try {
            if (json.has("name")) {
                name = json.get("name").getAsString();
            }
            if (json.has("load")) {
                load = json.get("load").getAsString();
            }
            if (json.has("active")) {
                active = json.get("active").getAsString();
            }
            if (json.has("sub")) {
                sub = json.get("sub").getAsString();
            }
            if (json.has("description")) {
                description = json.get("description").getAsString();
            }
            if (json.has("isRunning")) {
                isRunning = json.get("isRunning").getAsBoolean();
            }
            if (json.has("isActive")) {
                isActive = json.get("isActive").getAsBoolean();
            }
            if (json.has("isFailed")) {
                isFailed = json.get("isFailed").getAsBoolean();
            }
            if (json.has("isLoaded")) {
                isLoaded = json.get("isLoaded").getAsBoolean();
            }
        } catch (final Exception e) {
            LOGGER.error("Error parsing CSystemdService from JSON: {}", e.getMessage());
        }
    }
    
    @Override
    protected String toJson() {
        return "{}"; // Read-only from server
    }
    
    // Getters
    public String getName() { return name; }
    public String getLoad() { return load; }
    public String getActive() { return active; }
    public String getSub() { return sub; }
    public String getDescription() { return description; }
    public boolean isRunning() { return isRunning; }
    public boolean isActive() { return isActive; }
    public boolean isFailed() { return isFailed; }
    public boolean isLoaded() { return isLoaded; }
    
    /**
     * Get status display string.
     * @return Formatted status (e.g., "active (running)")
     */
    public String getStatusDisplay() {
        return String.format("%s (%s)", active, sub);
    }
    
    /**
     * Get short name without .service suffix.
     * @return Name without suffix (e.g., "sshd" from "sshd.service")
     */
    public String getShortName() {
        if (name.endsWith(".service")) {
            return name.substring(0, name.length() - 8);
        }
        return name;
    }
    
    @Override
    public String toString() {
        return String.format("CSystemdService{name='%s', active='%s', sub='%s'}", 
            name, active, sub);
    }
}
```

### 2.2 CSystemdServiceStatus

**Purpose**: Detailed service status (from status operation).

**Package**: `tech.derbent.bab.dashboard.dto`

**JSON Example**:
```json
{
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
    "Feb 03 10:00:00 server sshd[1234]: Server listening on 0.0.0.0 port 22."
  ]
}
```

**Implementation**:

```java
package tech.derbent.bab.dashboard.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CSystemdServiceStatus - Detailed systemd service status.
 * <p>
 * Includes PID, memory usage, CPU usage, logs, and configuration.
 * Obtained from status operation.
 * <p>
 * Thread Safety: Immutable after construction.
 */
public class CSystemdServiceStatus extends CObject {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemdServiceStatus.class);
    
    public static CSystemdServiceStatus createFromJson(final JsonObject json) {
        final CSystemdServiceStatus status = new CSystemdServiceStatus();
        status.fromJson(json);
        return status;
    }
    
    private String name = "";
    private String description = "";
    private String loadState = "";
    private String activeState = "";
    private String subState = "";
    private String unitFileState = "";
    private int mainPID = 0;
    private String memoryUsage = "";
    private String cpuUsage = "";
    private int restartCount = 0;
    private boolean isRunning = false;
    private boolean isActive = false;
    private boolean isFailed = false;
    private boolean isEnabled = false;
    private List<String> recentLogs = new ArrayList<>();
    
    public CSystemdServiceStatus() {
        // Default constructor
    }
    
    @Override
    protected void fromJson(final JsonObject json) {
        try {
            if (json.has("name")) {
                name = json.get("name").getAsString();
            }
            if (json.has("description")) {
                description = json.get("description").getAsString();
            }
            if (json.has("loadState")) {
                loadState = json.get("loadState").getAsString();
            }
            if (json.has("activeState")) {
                activeState = json.get("activeState").getAsString();
            }
            if (json.has("subState")) {
                subState = json.get("subState").getAsString();
            }
            if (json.has("unitFileState")) {
                unitFileState = json.get("unitFileState").getAsString();
            }
            if (json.has("mainPID")) {
                mainPID = json.get("mainPID").getAsInt();
            }
            if (json.has("memoryUsage")) {
                memoryUsage = json.get("memoryUsage").getAsString();
            }
            if (json.has("cpuUsage")) {
                cpuUsage = json.get("cpuUsage").getAsString();
            }
            if (json.has("restartCount")) {
                restartCount = json.get("restartCount").getAsInt();
            }
            if (json.has("isRunning")) {
                isRunning = json.get("isRunning").getAsBoolean();
            }
            if (json.has("isActive")) {
                isActive = json.get("isActive").getAsBoolean();
            }
            if (json.has("isFailed")) {
                isFailed = json.get("isFailed").getAsBoolean();
            }
            if (json.has("isEnabled")) {
                isEnabled = json.get("isEnabled").getAsBoolean();
            }
            if (json.has("recentLogs") && json.get("recentLogs").isJsonArray()) {
                final JsonArray logsArray = json.getAsJsonArray("recentLogs");
                recentLogs = new ArrayList<>();
                for (final JsonElement element : logsArray) {
                    recentLogs.add(element.getAsString());
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error parsing CSystemdServiceStatus from JSON: {}", e.getMessage());
        }
    }
    
    @Override
    protected String toJson() {
        return "{}"; // Read-only from server
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLoadState() { return loadState; }
    public String getActiveState() { return activeState; }
    public String getSubState() { return subState; }
    public String getUnitFileState() { return unitFileState; }
    public int getMainPID() { return mainPID; }
    public String getMemoryUsage() { return memoryUsage; }
    public String getCpuUsage() { return cpuUsage; }
    public int getRestartCount() { return restartCount; }
    public boolean isRunning() { return isRunning; }
    public boolean isActive() { return isActive; }
    public boolean isFailed() { return isFailed; }
    public boolean isEnabled() { return isEnabled; }
    public List<String> getRecentLogs() { return Collections.unmodifiableList(recentLogs); }
    
    /**
     * Get formatted memory usage.
     * @return Memory in human-readable format (e.g., "2.0 MB")
     */
    public String getMemoryFormatted() {
        try {
            final long bytes = Long.parseLong(memoryUsage);
            return formatBytes(bytes);
        } catch (final Exception e) {
            return memoryUsage;
        }
    }
    
    /**
     * Format bytes to human-readable string.
     */
    private String formatBytes(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), unit);
    }
    
    /**
     * Get logs as single string.
     * @return All logs joined by newlines
     */
    public String getLogsAsString() {
        return String.join("\n", recentLogs);
    }
    
    @Override
    public String toString() {
        return String.format("CSystemdServiceStatus{name='%s', active='%s', pid=%d}", 
            name, activeState, mainPID);
    }
}
```

### 2.3 CSystemdServiceAction

**Purpose**: Result of service action (start/stop/restart/enable/disable).

**Package**: `tech.derbent.bab.dashboard.dto`

**JSON Example**:
```json
{
  "action": "start",
  "serviceName": "apache2.service",
  "result": "success",
  "message": "Service apache2.service started successfully"
}
```

**Implementation**:

```java
package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CSystemdServiceAction - Result of service control action.
 * <p>
 * Represents the result of start/stop/restart/reload/enable/disable operations.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "action": "start",
 *   "serviceName": "apache2.service",
 *   "result": "success",
 *   "message": "Service apache2.service started successfully"
 * }
 * </pre>
 * <p>
 * Thread Safety: Immutable after construction.
 */
public class CSystemdServiceAction extends CObject {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemdServiceAction.class);
    
    public static CSystemdServiceAction createFromJson(final JsonObject json) {
        final CSystemdServiceAction action = new CSystemdServiceAction();
        action.fromJson(json);
        return action;
    }
    
    private String action = "";
    private String serviceName = "";
    private String result = "";
    private String message = "";
    
    public CSystemdServiceAction() {
        // Default constructor
    }
    
    @Override
    protected void fromJson(final JsonObject json) {
        try {
            if (json.has("action")) {
                action = json.get("action").getAsString();
            }
            if (json.has("serviceName")) {
                serviceName = json.get("serviceName").getAsString();
            }
            if (json.has("result")) {
                result = json.get("result").getAsString();
            }
            if (json.has("message")) {
                message = json.get("message").getAsString();
            }
        } catch (final Exception e) {
            LOGGER.error("Error parsing CSystemdServiceAction from JSON: {}", e.getMessage());
        }
    }
    
    @Override
    protected String toJson() {
        return "{}"; // Read-only from server
    }
    
    // Getters
    public String getAction() { return action; }
    public String getServiceName() { return serviceName; }
    public String getResult() { return result; }
    public String getMessage() { return message; }
    
    /**
     * Check if action was successful.
     * @return true if result is "success"
     */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(result);
    }
    
    /**
     * Check if action failed.
     * @return true if result is "failed"
     */
    public boolean isFailed() {
        return "failed".equalsIgnoreCase(result);
    }
    
    @Override
    public String toString() {
        return String.format("CSystemdServiceAction{action='%s', service='%s', result='%s'}", 
            action, serviceName, result);
    }
}
```

---

## 3. Common Patterns

### 3.1 JSON Parsing Safety

All DTOs follow safe parsing:

```java
@Override
protected void fromJson(final JsonObject json) {
    try {
        // Check if field exists before parsing
        if (json.has("fieldName")) {
            fieldName = json.get("fieldName").getAsString();
        }
        
        // Handle arrays
        if (json.has("arrayField") && json.get("arrayField").isJsonArray()) {
            final JsonArray array = json.getAsJsonArray("arrayField");
            for (final JsonElement element : array) {
                // Process element
            }
        }
        
        // Handle nested objects
        if (json.has("nestedObject") && json.get("nestedObject").isJsonObject()) {
            final JsonObject nested = json.getAsJsonObject("nestedObject");
            // Process nested object
        }
        
    } catch (final Exception e) {
        LOGGER.error("Error parsing DTO from JSON: {}", e.getMessage());
        // Don't throw - graceful degradation with default values
    }
}
```

### 3.2 Read-Only DTOs

All Calimero DTOs are read-only (data comes from server):

```java
@Override
protected String toJson() {
    return "{}"; // Read-only from server - no serialization needed
}
```

### 3.3 Immutability

DTOs are immutable after construction:
- No setters
- Only getters
- Collections returned as unmodifiable

```java
public List<String> getItems() {
    return Collections.unmodifiableList(items);
}
```

### 3.4 Convenience Methods

Add utility methods for common operations:

```java
// Boolean checks
public boolean isActive() {
    return "active".equalsIgnoreCase(activeState);
}

// Formatting
public String getStatusDisplay() {
    return String.format("%s (%s)", activeState, subState);
}

// Derived values
public String getShortName() {
    if (name.endsWith(".service")) {
        return name.substring(0, name.length() - 8);
    }
    return name;
}
```

---

## 4. Testing DTOs

### 4.1 Unit Test Template

```java
package tech.derbent.bab.dashboard.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class CSystemdServiceTest {
    
    @Test
    void testFromJson_AllFields() {
        final String json = """
            {
                "name": "sshd.service",
                "load": "loaded",
                "active": "active",
                "sub": "running",
                "description": "OpenSSH server daemon",
                "isRunning": true,
                "isActive": true,
                "isFailed": false,
                "isLoaded": true
            }
            """;
        
        final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        final CSystemdService service = CSystemdService.createFromJson(jsonObject);
        
        assertEquals("sshd.service", service.getName());
        assertEquals("loaded", service.getLoad());
        assertEquals("active", service.getActive());
        assertEquals("running", service.getSub());
        assertEquals("OpenSSH server daemon", service.getDescription());
        assertTrue(service.isRunning());
        assertTrue(service.isActive());
        assertFalse(service.isFailed());
        assertTrue(service.isLoaded());
    }
    
    @Test
    void testFromJson_MissingFields() {
        final String json = "{\"name\": \"test.service\"}";
        
        final JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        final CSystemdService service = CSystemdService.createFromJson(jsonObject);
        
        assertEquals("test.service", service.getName());
        assertEquals("", service.getLoad()); // Default value
        assertFalse(service.isRunning()); // Default false
    }
    
    @Test
    void testGetShortName() {
        final CSystemdService service = new CSystemdService();
        // Use reflection or create with JSON to set name
        // assertEquals("sshd", service.getShortName());
    }
}
```

---

## 5. Summary

**DTOs Created**:
1. `CDTOWebServiceEndpoint` - API discovery metadata
2. `CSystemdService` - Service list item
3. `CSystemdServiceStatus` - Detailed service status
4. `CSystemdServiceAction` - Action result

**Common Features**:
- Extend `CObject`
- Static `createFromJson()` factory
- Safe JSON parsing with null checks
- Read-only (no setters)
- Comprehensive getters
- Utility methods (isActive, getStatusDisplay, etc.)
- Logging on parse errors
- Immutable collections

**Ready for Use**: ✅

---

**Last Updated**: 2026-02-03  
**Version**: 1.0  
**Status**: Complete
