# Calimero Routing API Specification

**Document Type**: Implementation Specification for Calimero AI Agents  
**Date**: 2026-02-02  
**Status**: MANDATORY - To be implemented in Calimero C++ HTTP server  
**Target**: `~/git/calimero/src/http` codebase

---

## Executive Summary

This document specifies the routing configuration API endpoint for the Calimero HTTP server. The API must support atomic updates of system routing tables including default gateway and static routes.

**Current State**: Calimero supports `getRoutes` operation (read-only)  
**Required State**: Add `setRoutes` operation for complete routing configuration

---

## API Endpoint Specification

### Operation: `setRoutes`

**HTTP Method**: POST  
**Endpoint**: `/api/request`  
**Content-Type**: `application/json`

### Request Format

```json
{
  "type": "network",
  "operation": "setRoutes",
  "data": {
    "defaultGateway": "192.168.1.1",
    "staticRoutes": [
      {
        "network": "10.0.0.0",
        "netmask": "255.255.255.0",
        "gateway": "192.168.1.254"
      },
      {
        "network": "172.16.0.0",
        "netmask": "255.255.0.0",
        "gateway": "192.168.1.253"
      }
    ]
  }
}
```

### Request Schema

| Field | Type | Required | Description | Validation |
|-------|------|----------|-------------|------------|
| `type` | string | Yes | Must be `"network"` | Exact match |
| `operation` | string | Yes | Must be `"setRoutes"` | Exact match |
| `data` | object | Yes | Routing configuration | See below |
| `data.defaultGateway` | string | Yes | Default gateway IP address | Valid IPv4 address |
| `data.staticRoutes` | array | Yes | List of static routes (can be empty) | Array of route objects |
| `staticRoutes[].network` | string | Yes | Destination network IP | Valid IPv4 address |
| `staticRoutes[].netmask` | string | Yes | Network mask | Valid IPv4 netmask |
| `staticRoutes[].gateway` | string | Yes | Gateway IP for this route | Valid IPv4 address |

### Response Format (Success)

```json
{
  "status": 0,
  "message": "Routes configured successfully",
  "type": "network",
  "data": {
    "appliedDefaultGateway": "192.168.1.1",
    "appliedStaticRoutes": 2,
    "removedRoutes": 3
  }
}
```

### Response Format (Error)

```json
{
  "status": 1,
  "message": "Failed to apply route: Network is unreachable",
  "type": "network",
  "data": {
    "failedRoute": {
      "network": "10.0.0.0",
      "netmask": "255.255.255.0",
      "gateway": "192.168.1.254"
    }
  }
}
```

### HTTP Status Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| 200 | Success | Routes applied successfully |
| 400 | Bad Request | Invalid JSON, missing fields, invalid IP addresses |
| 500 | Internal Error | System command failed, network error |

---

## Implementation Requirements

### 1. Validation (MANDATORY)

Before applying any routes, validate:

```cpp
// Pseudo-code validation logic
bool validateSetRoutesRequest(const json& request) {
    // 1. Required fields
    if (!request.contains("type") || request["type"] != "network") {
        return false;
    }
    if (!request.contains("operation") || request["operation"] != "setRoutes") {
        return false;
    }
    if (!request.contains("data")) {
        return false;
    }
    
    const auto& data = request["data"];
    
    // 2. Validate default gateway
    if (!data.contains("defaultGateway")) {
        return false;
    }
    if (!isValidIPv4(data["defaultGateway"])) {
        return false;
    }
    
    // 3. Validate static routes array
    if (!data.contains("staticRoutes") || !data["staticRoutes"].is_array()) {
        return false;
    }
    
    for (const auto& route : data["staticRoutes"]) {
        if (!route.contains("network") || !isValidIPv4(route["network"])) {
            return false;
        }
        if (!route.contains("netmask") || !isValidNetmask(route["netmask"])) {
            return false;
        }
        if (!route.contains("gateway") || !isValidIPv4(route["gateway"])) {
            return false;
        }
    }
    
    return true;
}

bool isValidIPv4(const string& ip) {
    // Regex: ^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$
    // Additional check: each octet 0-255
    return regex_match(ip, ipv4_pattern) && allOctetsValid(ip);
}

bool isValidNetmask(const string& netmask) {
    // Valid netmasks: 255.255.255.0, 255.255.0.0, etc.
    // Must be contiguous 1s followed by 0s in binary
    if (!isValidIPv4(netmask)) return false;
    
    uint32_t mask = ipToUint32(netmask);
    // Check for contiguous 1s: (mask & (mask + 1)) == 0
    return (mask & (mask + 1)) == 0;
}
```

### 2. Route Application Strategy (ATOMIC)

**CRITICAL**: Route changes must be atomic - either all succeed or all rolled back.

```cpp
bool applyRoutesAtomic(const json& data) {
    // Step 1: Backup current routing table
    vector<Route> currentRoutes = getCurrentRoutes();
    string currentDefaultGateway = getCurrentDefaultGateway();
    
    try {
        // Step 2: Remove all manual static routes (keep kernel/dhcp routes)
        removeManualStaticRoutes();
        
        // Step 3: Set new default gateway
        if (!setDefaultGateway(data["defaultGateway"])) {
            throw runtime_error("Failed to set default gateway");
        }
        
        // Step 4: Add new static routes
        for (const auto& route : data["staticRoutes"]) {
            if (!addStaticRoute(route["network"], route["netmask"], route["gateway"])) {
                throw runtime_error("Failed to add static route");
            }
        }
        
        return true;
        
    } catch (const exception& e) {
        // Step 5: ROLLBACK on any failure
        LOG_ERROR("Route application failed, rolling back: " << e.what());
        restoreRoutes(currentRoutes, currentDefaultGateway);
        throw;
    }
}
```

### 3. NetworkManager Integration (nmcli)

**Preferred Method**: Use NetworkManager for persistent configuration

```bash
# Set default gateway (persistent)
nmcli connection modify <connection-name> ipv4.gateway "192.168.1.1"
nmcli connection up <connection-name>

# Add static route (persistent)
nmcli connection modify <connection-name> +ipv4.routes "10.0.0.0/24 192.168.1.254"

# Remove all static routes
nmcli connection modify <connection-name> ipv4.routes ""

# Apply changes
nmcli connection up <connection-name>
```

**C++ Implementation**:

```cpp
bool setDefaultGateway(const string& gateway) {
    // Get active connection
    string connection = getActiveConnection();
    
    string cmd = "nmcli connection modify " + connection + 
                 " ipv4.gateway " + gateway;
    
    if (system(cmd.c_str()) != 0) {
        return false;
    }
    
    // Apply changes
    cmd = "nmcli connection up " + connection;
    return system(cmd.c_str()) == 0;
}

bool addStaticRoute(const string& network, const string& netmask, const string& gateway) {
    string connection = getActiveConnection();
    
    // Convert netmask to CIDR (255.255.255.0 -> /24)
    int cidr = netmaskToCIDR(netmask);
    
    string route = network + "/" + to_string(cidr) + " " + gateway;
    string cmd = "nmcli connection modify " + connection + 
                 " +ipv4.routes \"" + route + "\"";
    
    if (system(cmd.c_str()) != 0) {
        return false;
    }
    
    // Apply changes
    cmd = "nmcli connection up " + connection;
    return system(cmd.c_str()) == 0;
}

void removeManualStaticRoutes() {
    string connection = getActiveConnection();
    
    // Clear all static routes
    string cmd = "nmcli connection modify " + connection + " ipv4.routes \"\"";
    system(cmd.c_str());
    
    // Apply changes
    cmd = "nmcli connection up " + connection;
    system(cmd.c_str());
}

string getActiveConnection() {
    // Get active connection name from nmcli
    FILE* pipe = popen("nmcli -t -f NAME,DEVICE connection show --active | head -n1 | cut -d':' -f1", "r");
    if (!pipe) throw runtime_error("Failed to get active connection");
    
    char buffer[128];
    string result;
    if (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        result = buffer;
        result.erase(result.find_last_not_of("\n\r") + 1);
    }
    pclose(pipe);
    
    return result;
}

int netmaskToCIDR(const string& netmask) {
    // Convert 255.255.255.0 to 24
    uint32_t mask = ipToUint32(netmask);
    int cidr = 0;
    while (mask) {
        cidr += (mask & 1);
        mask >>= 1;
    }
    return cidr;
}
```

### 4. Fallback: ip route command

If NetworkManager is not available, use `ip route` directly:

```cpp
bool setDefaultGatewayFallback(const string& gateway) {
    // Remove old default gateway
    system("ip route del default");
    
    // Add new default gateway
    string cmd = "ip route add default via " + gateway;
    return system(cmd.c_str()) == 0;
}

bool addStaticRouteFallback(const string& network, const string& netmask, const string& gateway) {
    int cidr = netmaskToCIDR(netmask);
    string cmd = "ip route add " + network + "/" + to_string(cidr) + " via " + gateway;
    return system(cmd.c_str()) == 0;
}
```

**⚠️ WARNING**: `ip route` changes are NOT persistent across reboots. Always prefer NetworkManager.

---

## Request Handler Implementation

### Handler Function Signature

```cpp
// In cwebservice.cpp or appropriate network handler file
json handleSetRoutes(const json& request) {
    json response;
    response["type"] = "network";
    
    try {
        // 1. Validate request
        if (!validateSetRoutesRequest(request)) {
            response["status"] = 1;
            response["message"] = "Invalid request format or IP addresses";
            return response;
        }
        
        // 2. Extract data
        const auto& data = request["data"];
        string defaultGateway = data["defaultGateway"];
        vector<StaticRoute> staticRoutes;
        
        for (const auto& route : data["staticRoutes"]) {
            staticRoutes.push_back({
                route["network"],
                route["netmask"],
                route["gateway"]
            });
        }
        
        // 3. Apply routes atomically
        int removedCount = removeManualStaticRoutes();
        
        if (!setDefaultGateway(defaultGateway)) {
            throw runtime_error("Failed to set default gateway");
        }
        
        int appliedCount = 0;
        for (const auto& route : staticRoutes) {
            if (!addStaticRoute(route.network, route.netmask, route.gateway)) {
                throw runtime_error("Failed to add static route: " + route.network);
            }
            appliedCount++;
        }
        
        // 4. Success response
        response["status"] = 0;
        response["message"] = "Routes configured successfully";
        response["data"]["appliedDefaultGateway"] = defaultGateway;
        response["data"]["appliedStaticRoutes"] = appliedCount;
        response["data"]["removedRoutes"] = removedCount;
        
    } catch (const exception& e) {
        response["status"] = 1;
        response["message"] = string("Failed to apply routes: ") + e.what();
    }
    
    return response;
}
```

### Integration into Dispatcher

```cpp
// In dispatcher.cpp or main request router
json dispatchNetworkRequest(const json& request) {
    string operation = request["operation"];
    
    if (operation == "getInterfaces") {
        return handleGetInterfaces(request);
    }
    else if (operation == "setInterface") {
        return handleSetInterface(request);
    }
    else if (operation == "getRoutes") {
        return handleGetRoutes(request);
    }
    else if (operation == "setRoutes") {
        return handleSetRoutes(request);  // NEW
    }
    else if (operation == "getDnsServers") {
        return handleGetDnsServers(request);
    }
    else if (operation == "setDnsServers") {
        return handleSetDnsServers(request);
    }
    else {
        return errorResponse("Unknown operation: " + operation);
    }
}
```

---

## Testing Requirements

### 1. Unit Tests

```cpp
TEST(RoutingAPI, ValidateIPv4Address) {
    ASSERT_TRUE(isValidIPv4("192.168.1.1"));
    ASSERT_TRUE(isValidIPv4("10.0.0.1"));
    ASSERT_FALSE(isValidIPv4("256.1.1.1"));
    ASSERT_FALSE(isValidIPv4("192.168.1"));
    ASSERT_FALSE(isValidIPv4("invalid"));
}

TEST(RoutingAPI, ValidateNetmask) {
    ASSERT_TRUE(isValidNetmask("255.255.255.0"));
    ASSERT_TRUE(isValidNetmask("255.255.0.0"));
    ASSERT_TRUE(isValidNetmask("255.0.0.0"));
    ASSERT_FALSE(isValidNetmask("255.255.255.1"));  // Not contiguous
    ASSERT_FALSE(isValidNetmask("192.168.1.1"));
}

TEST(RoutingAPI, SetRoutesRequest) {
    json request = {
        {"type", "network"},
        {"operation", "setRoutes"},
        {"data", {
            {"defaultGateway", "192.168.1.1"},
            {"staticRoutes", json::array()}
        }}
    };
    
    ASSERT_TRUE(validateSetRoutesRequest(request));
}
```

### 2. Integration Tests

```bash
# Test 1: Set default gateway only
curl -X POST http://localhost:8080/api/request \
  -H "Content-Type: application/json" \
  -d '{
    "type": "network",
    "operation": "setRoutes",
    "data": {
      "defaultGateway": "192.168.1.1",
      "staticRoutes": []
    }
  }'

# Test 2: Set default gateway + static routes
curl -X POST http://localhost:8080/api/request \
  -H "Content-Type: application/json" \
  -d '{
    "type": "network",
    "operation": "setRoutes",
    "data": {
      "defaultGateway": "192.168.1.1",
      "staticRoutes": [
        {
          "network": "10.0.0.0",
          "netmask": "255.255.255.0",
          "gateway": "192.168.1.254"
        }
      ]
    }
  }'

# Test 3: Invalid IP address (should fail)
curl -X POST http://localhost:8080/api/request \
  -H "Content-Type: application/json" \
  -d '{
    "type": "network",
    "operation": "setRoutes",
    "data": {
      "defaultGateway": "999.999.999.999",
      "staticRoutes": []
    }
  }'

# Test 4: Verify routes applied
curl -X POST http://localhost:8080/api/request \
  -H "Content-Type: application/json" \
  -d '{
    "type": "network",
    "operation": "getRoutes"
  }'
```

---

## Security Considerations

### 1. Input Validation (CRITICAL)

- **NEVER trust client input**
- Validate ALL IP addresses using strict regex
- Validate netmasks are contiguous
- Sanitize input before passing to shell commands
- Prevent command injection via shell escaping

```cpp
string sanitizeIP(const string& ip) {
    // Only allow: digits and dots
    regex safe_pattern("^[0-9.]+$");
    if (!regex_match(ip, safe_pattern)) {
        throw invalid_argument("Invalid IP format");
    }
    return ip;
}
```

### 2. Privilege Requirements

- Modifying routes requires **root privileges**
- Ensure Calimero HTTP server runs with appropriate permissions
- Consider using `sudo` with specific command whitelist

```cpp
bool setDefaultGateway(const string& gateway) {
    string safeGateway = sanitizeIP(gateway);
    string connection = getActiveConnection();
    
    // Use sudo with specific command
    string cmd = "sudo nmcli connection modify " + connection + 
                 " ipv4.gateway " + safeGateway;
    
    return system(cmd.c_str()) == 0;
}
```

### 3. Rate Limiting

- Limit route updates to 1 request per 5 seconds
- Prevent DOS attacks via rapid route changes

---

## Error Handling

### Common Error Scenarios

| Error | HTTP Code | Response |
|-------|-----------|----------|
| Invalid JSON | 400 | `{"status": 1, "message": "Invalid JSON format"}` |
| Missing required field | 400 | `{"status": 1, "message": "Missing required field: defaultGateway"}` |
| Invalid IP address | 400 | `{"status": 1, "message": "Invalid IP address: 999.999.999.999"}` |
| Invalid netmask | 400 | `{"status": 1, "message": "Invalid netmask: 255.255.255.1"}` |
| NetworkManager error | 500 | `{"status": 1, "message": "Failed to apply routes: Connection error"}` |
| Permission denied | 500 | `{"status": 1, "message": "Permission denied: requires root"}` |
| Network unreachable | 500 | `{"status": 1, "message": "Gateway is unreachable"}` |

---

## Logging Requirements

```cpp
void logRouteChange(const string& operation, const json& data, bool success) {
    if (success) {
        LOG_INFO("[Routing] " << operation << " succeeded: " 
                 << data.dump());
    } else {
        LOG_ERROR("[Routing] " << operation << " failed: " 
                  << data.dump());
    }
    
    // Audit log for security
    LOG_AUDIT("Route change: operation=" << operation 
              << " user=" << getCurrentUser()
              << " success=" << success);
}
```

---

## Implementation Checklist

### Phase 1: Core Functionality
- [ ] Add `validateSetRoutesRequest()` function
- [ ] Add `isValidIPv4()` validation
- [ ] Add `isValidNetmask()` validation
- [ ] Add `handleSetRoutes()` request handler
- [ ] Integrate handler into dispatcher
- [ ] Add response JSON formatting

### Phase 2: NetworkManager Integration
- [ ] Implement `getActiveConnection()`
- [ ] Implement `setDefaultGateway()`
- [ ] Implement `addStaticRoute()`
- [ ] Implement `removeManualStaticRoutes()`
- [ ] Implement `netmaskToCIDR()` conversion
- [ ] Add atomic transaction support

### Phase 3: Error Handling
- [ ] Add rollback mechanism
- [ ] Add detailed error messages
- [ ] Add logging for all operations
- [ ] Add audit logging for security

### Phase 4: Testing
- [ ] Write unit tests for validation
- [ ] Write integration tests
- [ ] Test rollback scenarios
- [ ] Test edge cases (invalid IPs, etc.)
- [ ] Test with actual network changes

### Phase 5: Documentation
- [ ] Update API documentation
- [ ] Add usage examples
- [ ] Document error codes
- [ ] Add troubleshooting guide

---

## Example Client Usage (Derbent Side)

```java
// Already implemented in Derbent
public CCalimeroResponse<Void> setRoutes(
        final String defaultGateway,
        final List<CRoute> staticRoutes) {
    
    final CRouteConfigurationUpdate update = new CRouteConfigurationUpdate();
    update.setDefaultGateway(defaultGateway);
    update.setStaticRoutes(staticRoutes);
    
    return sendRequest("setRoutes", update, new TypeReference<Void>() {});
}
```

---

## Success Criteria

1. ✅ API endpoint accepts valid JSON requests
2. ✅ All IP addresses validated before application
3. ✅ Routes applied atomically (all or nothing)
4. ✅ Changes persist across reboots (via NetworkManager)
5. ✅ Proper error messages returned to client
6. ✅ Rollback works on any failure
7. ✅ All operations logged for audit
8. ✅ Integration tests pass 100%

---

## Contact & Support

**Calimero Implementation Team**: See `~/git/calimero/src/http` repository  
**Derbent Integration**: This document is reference for Calimero implementation  
**Questions**: Raise issues in Calimero repository

---

**END OF SPECIFICATION**
