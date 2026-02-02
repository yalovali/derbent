# BAB Gateway JSON Field Synchronization Guide

**Version**: 1.2  
**Date**: 2026-02-02  
**Status**: Implementation Guide for JSON Field Mapping between Calimero C++ and Java BAB Components

## Overview

This document ensures consistent JSON field naming and structure between the Calimero C++ HTTP server and Java BAB Dashboard components. All parsers must follow these standardized field mappings to prevent display issues.

## Network Interfaces Mapping

### C++ Output (CNmcliParser.cpp)
```cpp
nlohmann::json j;
j["name"] = device;                     // Java expects "name"
j["type"] = type;                       
j["operState"] = state;                 // Java expects "operState" 
j["connection"] = connection;
j["macAddress"] = macAddress;
j["mtu"] = mtu;
j["interface"] = device;                // Backward compatibility
j["ipAddress"] = extractedIP;           // Primary IP field
j["addresses"] = addressArray;          // Full address list
```

### Java Input (CNetworkInterface.java)
```java
// Primary fields
if (json.has("name")) name = json.get("name").getAsString();
if (json.has("operState")) status = json.get("operState").getAsString();
if (json.has("macAddress")) macAddress = json.get("macAddress").getAsString();

// Address parsing with multiple fallbacks
if (json.has("addresses") && json.get("addresses").isJsonArray()) {
    // Parse addresses array (preferred)
} else if (json.has("ipAddress")) {
    // Fallback to primary IP field
} else if (json.has("ipv4Address")) {
    // Legacy fallback
}
```

### Field Priority
1. **addresses[]** - Array of address objects (preferred)
2. **ipAddress** - Primary IP address field (backup)
3. **ipv4Address/ipv6Address** - Legacy fields (final fallback)

## Routing Table Mapping

### C++ Output (CCommandParser.cpp)
```cpp
nlohmann::json j;
j["destination"] = destination;
j["gateway"] = gateway;
j["interface"] = interface;
j["protocol"] = protocol;
j["metric"] = calculatedMetric;         // Calculated from protocol
j["flags"] = generatedFlags;            // Generated: "UG", "U", etc.
```

### Java Input (CNetworkRoute.java)
```java
if (json.has("destination")) destination = json.get("destination").getAsString();
if (json.has("gateway")) gateway = json.get("gateway").getAsString();
if (json.has("interface")) interfaceName = json.get("interface").getAsString();
if (json.has("metric")) metric = json.get("metric").getAsInt();
if (json.has("flags")) flags = json.get("flags").getAsString();
```

## System Process Mapping

### C++ Output (csystemprocessor.cpp)
```cpp
nlohmann::json proc;
proc["user"] = fields[0];
proc["pid"] = std::atoi(fields[1].c_str());
proc["cpuPercent"] = std::atof(fields[2].c_str());    // Java expects "cpuPercent"
proc["memPercent"] = std::atof(fields[3].c_str());    // Java expects "memPercent"  
proc["memVirtBytes"] = std::atoll(fields[4].c_str()) * 1024; // Java expects "memVirtBytes"
proc["memRssBytes"] = std::atoll(fields[5].c_str()) * 1024;  // Java expects "memRssBytes"
proc["state"] = fields[7];                           // Java expects "state"
proc["name"] = command;                              // Java expects "name"
proc["command"] = command;
```

### Java Input (CSystemProcess.java)
```java
if (json.has("cpuPercent")) cpuPercent = json.get("cpuPercent").getAsDouble();
if (json.has("memPercent")) memoryPercent = json.get("memPercent").getAsDouble();
if (json.has("memRssBytes")) {
    memRssBytes = json.get("memRssBytes").getAsLong();
    memoryMB = memRssBytes / (1024 * 1024);  // Convert bytes to MB
}
if (json.has("state")) status = json.get("state").getAsString();
if (json.has("name")) name = json.get("name").getAsString();
```

## System Metrics Mapping

### C++ Output (csystemprocessor.cpp)
```cpp
// CPU metrics with multiple data points
output["cpu"] = {
    {"usage", calculatedUsage},
    {"usagePercent", calculatedUsage},     // Java compatibility
    {"cores", coreCount},
    {"frequencyMHz", frequency}
};

// Memory metrics with comprehensive fields  
output["memory"] = {
    {"totalBytes", memTotal},              // Java compatibility
    {"usedBytes", memUsed},                // Java compatibility  
    {"freeBytes", memFree},                // Java compatibility
    {"availableBytes", memAvailable},      // Java compatibility
    {"usagePercent", calculatedPercent}
};

// System info with formatted fields
output["hostname"] = hostname;
output["uptimeSeconds"] = uptimeSeconds;           // Java compatibility
output["uptimeFormatted"] = formattedUptime;
output["totalMemoryBytes"] = memTotal * 1024;     // Java compatibility
```

## DNS Configuration Mapping

### C++ Output (cnetworkprocessor.cpp)
```cpp
// Structured DNS info (resolvectl format)
if (resolvectl_available) {
    output["dnsInfo"] = [
        {
            "interface": "eth0",
            "servers": ["8.8.8.8", "8.8.4.4"],
            "domains": ["example.com"]
        }
    ];
}

// Simple DNS list (resolv.conf fallback)
else {
    output["servers"] = ["8.8.8.8", "8.8.4.4"];
    output["source"] = "resolv.conf";
}
```

### Java Input (CDnsConfigurationCalimeroClient.java)
```java
// Parse structured DNS info (preferred)
if (data.has("dnsInfo") && data.get("dnsInfo").isJsonArray()) {
    for (JsonElement element : dnsInfoArray) {
        parseDnsInfoEntry(element.getAsJsonObject(), dnsServers);
    }
}
// Parse simple server list (fallback)
else if (data.has("servers") && data.get("servers").isJsonArray()) {
    for (JsonElement element : serversArray) {
        String serverIp = element.getAsString();
        CDnsServer dnsServer = new CDnsServer(serverIp);
        dnsServer.setSource("resolv.conf");
        dnsServers.add(dnsServer);
    }
}
```

## Disk Usage Mapping

### C++ Output (CCommandParser.cpp)
```cpp
nlohmann::json j;
j["filesystem"] = filesystem;
j["mountpoint"] = mountpoint;
j["mountPoint"] = mountpoint;           // Java compatibility
j["totalBytes"] = totalBytes;
j["usedBytes"] = usedBytes;  
j["freeBytes"] = freeBytes;
j["availableBytes"] = freeBytes;        // Java compatibility
j["usedPercent"] = usedPercent;
j["usagePercent"] = usedPercent;        // Java compatibility

// Human-readable sizes
j["totalSize"] = formatBytes(totalBytes);
j["usedSize"] = formatBytes(usedBytes);
j["freeSize"] = formatBytes(freeBytes);
```

## Service Pattern Requirements

### BAB Component Services

All BAB services MUST follow this pattern:

```java
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")  
public class CEntityService extends CEntityOfCompanyService<CEntity> 
        implements IEntityRegistrable, IEntityWithView {

    @Override
    protected void validateEntity(final CEntity entity) {
        super.validateEntity(entity);
        
        // 1. Required fields validation
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        
        // 2. Use validation helpers (MANDATORY)
        validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
        validateUniqueNameInCompany(repository, entity, entity.getName(), entity.getCompany());
        
        // 3. Business validation
        // Entity-specific validation here
    }
    
    @Override
    public void copyEntityFieldsTo(final CEntity source, final CEntityDB<?> target, final CCloneOptions options) {
        super.copyEntityFieldsTo(source, target, options);
        
        if (!(target instanceof CEntity)) return;
        final CEntity targetEntity = (CEntity) target;
        
        // Direct field copying
        targetEntity.setField1(source.getField1());
        targetEntity.setField2(source.getField2());
        
        // Conditional copying
        if (!options.isResetDates()) {
            targetEntity.setDate(source.getDate());
        }
        
        LOGGER.debug("Copied {} fields", getClass().getSimpleName());
    }
}
```

### Fail-Fast Validation

All services MUST implement fail-fast validation:

```java
@Override
protected void validateEntity(final CEntity entity) {
    super.validateEntity(entity);  // MANDATORY - calls validateNullableFields()
    
    // 1. Critical business fields - explicit validation
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
    
    // 2. Use standardized helpers (MANDATORY)
    validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
    validateUniqueNameInCompany(repository, entity, entity.getName(), entity.getCompany());
    
    // 3. Business logic validation
    if (entity.getStartDate() != null && entity.getEndDate() != null && 
        entity.getEndDate().isBefore(entity.getStartDate())) {
        throw new CValidationException("End date cannot be before start date");
    }
}
```

### BAB Component Pattern

All BAB dashboard components MUST use the @Transient placeholder pattern:

```java
@Entity  
@Table(name = "cbab_entity")
public class CBabEntity extends CEntityOfCompany<CBabEntity> {
    
    // @Transient placeholder for BAB component
    @AMetaData(
        displayName = "Component Display",
        createComponentMethod = "createCustomComponent",
        dataProviderBean = "pageservice"
    )
    @Transient
    private CBabEntity placeHolder_createCustomComponent = null;
    
    public CBabEntity getPlaceHolder_createCustomComponent() {
        return this;  // Return entity itself for component initialization
    }
}
```

## Messaging Method Patterns

### HTTP API Messaging

Calimero C++ services MUST use structured messaging:

```cpp
bool CProcessor::execute(const nlohmann::json& input, nlohmann::json& output, std::string& error) {
    try {
        if (!input.contains("operation")) {
            error = "Missing 'operation' field";
            return false;
        }
        
        std::string operation = input["operation"];
        
        if (operation == "getMetrics") {
            return handleGetMetrics(output, error);
        }
        
        error = "Unknown operation: " + operation;
        return false;
    }
    catch (const std::exception& e) {
        error = std::string("Execution error: ") + e.what();
        return false;
    }
}
```

### Java API Client Messaging

BAB client services MUST handle responses gracefully:

```java
public CCalimeroResponse<List<CEntity>> fetchEntities() {
    final CCalimeroRequest request = CCalimeroRequest.builder()
            .type("service")
            .operation("getEntities")
            .build();
    
    try {
        final CCalimeroResponse response = clientProject.sendRequest(request);
        
        if (!response.isSuccess()) {
            LOGGER.warn("⚠️ Failed to fetch entities: {}", response.getErrorMessage());
            return CCalimeroResponse.<List<CEntity>>error(response.getErrorMessage());
        }
        
        final List<CEntity> entities = parseEntities(response);
        return CCalimeroResponse.success(entities);
        
    } catch (final Exception e) {
        LOGGER.error("❌ Exception fetching entities: {}", e.getMessage(), e);
        return CCalimeroResponse.<List<CEntity>>error("Failed to fetch entities: " + e.getMessage());
    }
}
```

## Testing and Verification

### Field Mapping Verification

Use these commands to verify correct field mapping:

```bash
# Test network interface parsing
curl -X POST http://localhost:8077/api/request -H "Authorization: Bearer token" \
  -d '{"type":"network","operation":"getInterfaces"}' | jq '.data.interfaces[0]'

# Expected fields: name, type, operState, macAddress, mtu, addresses[], ipAddress

# Test system processes parsing  
curl -X POST http://localhost:8077/api/request -H "Authorization: Bearer token" \
  -d '{"type":"system","operation":"processes"}' | jq '.data.processes[0]'

# Expected fields: name, pid, cpuPercent, memPercent, state, memRssBytes, memVirtBytes
```

### Java Component Testing

```java
@Test
public void testNetworkInterfaceParsing() {
    String jsonResponse = "{\"name\":\"eth0\",\"operState\":\"connected\"," +
                         "\"macAddress\":\"00:11:22:33:44:55\",\"ipAddress\":\"192.168.1.100\"}";
    
    JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
    CNetworkInterface iface = CNetworkInterface.createFromJson(json);
    
    assertEquals("eth0", iface.getName());
    assertEquals("connected", iface.getStatus());  
    assertEquals("00:11:22:33:44:55", iface.getMacAddress());
    assertFalse(iface.getAddresses().isEmpty());
}
```

## Root Privilege Requirements

Some system commands require root privileges for full functionality:

### Commands Requiring Root:
- **Process monitoring** (`ps aux`) - Full process list
- **Network statistics** (`/proc/net/dev`) - Detailed interface stats  
- **System metrics** (`/proc/meminfo`, `/proc/stat`) - Memory and CPU details
- **Service management** (`systemctl`) - Service status

### Graceful Degradation:
- Components must handle permission failures gracefully
- Show partial data when available
- Log warnings for missing permissions, not errors
- Use fallback commands when possible

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-01 | Initial field mapping documentation |
| 1.1 | 2026-02-02 | Added service patterns and validation helpers |  
| 1.2 | 2026-02-02 | Added messaging patterns and root privilege requirements |