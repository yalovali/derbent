# Calimero-Derbent JSON Field Mapping Reference

**Date**: 2026-02-01  
**Status**: Complete field mapping verification  
**Purpose**: Document exact field names between Calimero C++ API and Derbent BAB Java parsers

---

## Overview

This document provides the authoritative mapping between Calimero HTTP API JSON field names and Derbent BAB Java parser field names. All mappings have been verified with live API testing.

---

## 1. System Metrics API

**Endpoint**: `POST /api/request` with `type="system"`, `operation="metrics"`

### Calimero JSON Response
```json
{
  "cpu": {
    "usagePercent": 0.0,
    "loadAvg1Min": 1.75,
    "loadAvg5Min": 2.0,
    "loadAvg15Min": 1.95,
    "coreCount": 12
  },
  "memory": {
    "totalBytes": 33500000000,
    "usedBytes": 27400000000,
    "usagePercent": 81.8
  },
  "system": {
    "uptimeSeconds": 12345
  }
}
```

### Java Parser: `CSystemMetrics.java`
```java
// Nested JSON parsing
if (json.has("cpu") && json.get("cpu").isJsonObject()) {
    JsonObject cpu = json.getAsJsonObject("cpu");
    cpuUsagePercent = cpu.get("usagePercent").getAsDouble();
    loadAverage1 = cpu.get("loadAvg1Min").getAsDouble();
    loadAverage5 = cpu.get("loadAvg5Min").getAsDouble();
    loadAverage15 = cpu.get("loadAvg15Min").getAsDouble();
}
```

| Calimero Field | Java Field | Unit Conversion | Status |
|----------------|------------|-----------------|--------|
| `cpu.usagePercent` | `cpuUsagePercent` | None (%) | ✅ Working |
| `cpu.loadAvg1Min` | `loadAverage1` | None | ✅ Working |
| `cpu.loadAvg5Min` | `loadAverage5` | None | ✅ Working |
| `cpu.loadAvg15Min` | `loadAverage15` | None | ✅ Working |
| `cpu.coreCount` | `cpuCoreCount` | None | ✅ Working |
| `memory.totalBytes` | `memoryTotalMB` | bytes → MB (÷1024÷1024) | ✅ Working |
| `memory.usedBytes` | `memoryUsedMB` | bytes → MB (÷1024÷1024) | ✅ Working |
| `memory.usagePercent` | `memoryUsagePercent` | None (%) | ✅ Working |
| `system.uptimeSeconds` | `uptimeSeconds` | None | ✅ Working |

---

## 2. Process List API

**Endpoint**: `POST /api/request` with `type="system"`, `operation="processes"`

### Calimero JSON Response
```json
{
  "processes": [
    {
      "pid": 2032,
      "name": "kwin_wayland",
      "user": "yasin",
      "cpuPercent": 0.0,
      "memPercent": 1.43,
      "memRssBytes": 17825792,
      "memVirtBytes": 26214400,
      "state": "S"
    }
  ]
}
```

### Java Parser: `CSystemProcess.java`
```java
if (json.has("memRssBytes")) {
    memRssBytes = json.get("memRssBytes").getAsLong();
    memoryMB = memRssBytes / (1024 * 1024);  // Convert to MB
}
if (json.has("memPercent")) {
    memoryPercent = json.get("memPercent").getAsDouble();
}
if (json.has("state")) {
    status = json.get("state").getAsString();  // R/S/D/Z/T
}
```

| Calimero Field | Java Field | Unit Conversion | Status | Notes |
|----------------|------------|-----------------|--------|-------|
| `pid` | `pid` | None | ✅ Working | Process ID |
| `name` | `name` | None | ✅ Working | Process name |
| `user` | `user` | None | ✅ Working | Owner username |
| `cpuPercent` | `cpuPercent` | None (%) | ⚠️ Always 0.0 | Calimero limitation |
| `memPercent` | `memoryPercent` | None (%) | ✅ Working | Memory usage % |
| `memRssBytes` | `memRssBytes` + `memoryMB` | bytes → MB (÷1024÷1024) | ✅ Working | Resident memory |
| `memVirtBytes` | `memVirtBytes` | None (bytes) | ✅ Working | Virtual memory |
| `state` | `status` | None | ✅ Working | R/S/D/Z/T states |
| `command` | `command` | N/A | ❌ Not sent | Calimero doesn't collect cmdline |

**Known Issues**:
- **cpuPercent**: Always 0.0 due to Calimero C++ implementation limitation (instant sampling issue)
- **command**: Not available - would require reading `/proc/[pid]/cmdline` in Calimero

---

## 3. Disk Usage API

**Endpoint**: `POST /api/request` with `type="system"`, `operation="diskUsage"`

### Calimero JSON Response
```json
{
  "disks": [
    {
      "mountPoint": "/",
      "filesystem": "",
      "device": "",
      "totalBytes": 1967536746496,
      "usedBytes": 348036177920,
      "availableBytes": 1519479681024,
      "usagePercent": 17.69
    }
  ]
}
```

### Java Parser: `CDiskInfo.java`
```java
if (json.has("totalBytes")) {
    totalGB = json.get("totalBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
}
if (json.has("usedBytes")) {
    usedGB = json.get("usedBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
}
if (json.has("availableBytes")) {
    availableGB = json.get("availableBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
}
```

| Calimero Field | Java Field | Unit Conversion | Status | Notes |
|----------------|------------|-----------------|--------|-------|
| `mountPoint` | `mountPoint` | None | ✅ Working | `/`, `/home`, etc. |
| `filesystem` | `filesystem` | None | ⚠️ Empty string | Calimero sends `""` |
| `device` | `filesystem` | None | ⚠️ Empty string | Calimero sends `""` |
| `totalBytes` | `totalGB` | bytes → GB (÷1024÷1024÷1024) | ✅ Working | Total disk space |
| `usedBytes` | `usedGB` | bytes → GB (÷1024÷1024÷1024) | ✅ Working | Used disk space |
| `availableBytes` | `availableGB` | bytes → GB (÷1024÷1024÷1024) | ✅ Working | Available space |
| `usagePercent` | `usagePercent` | None (%) | ✅ Working | Usage percentage |

**Known Issues**:
- **filesystem/device**: Empty strings from Calimero - parser handles gracefully
- **type**: Not sent by Calimero, Java field remains empty

---

## 4. Network Interfaces API

**Endpoint**: `POST /api/request` with `type="network"`, `operation="getInterfaces"`

### Calimero JSON Response
```json
{
  "interfaces": [
    {
      "name": "wlp0s20f3",
      "type": "ether",
      "operState": "up",
      "isUp": true,
      "macAddress": "00:11:22:33:44:55",
      "mtu": 1500,
      "addresses": [
        {
          "address": "192.168.68.66",
          "family": "inet",
          "prefixLength": 24,
          "scope": "global"
        },
        {
          "address": "fe80::eecd:4a07:aef9:96e0",
          "family": "inet6",
          "prefixLength": 64,
          "scope": "global"
        }
      ]
    }
  ]
}
```

### Java Parser: `CNetworkInterface.java`
```java
// Calimero sends "operState" (not "status")
if (json.has("operState") && !json.get("operState").isJsonNull()) {
    status = json.get("operState").getAsString();
}

// Calimero sends addresses as array of objects
if (json.has("addresses") && json.get("addresses").isJsonArray()) {
    JsonArray addrArray = json.getAsJsonArray("addresses");
    addresses.clear();
    addrArray.forEach(element -> {
        if (element.isJsonObject()) {
            JsonObject addrObj = element.getAsJsonObject();
            if (addrObj.has("address")) {
                addresses.add(addrObj.get("address").getAsString());
            }
        }
    });
}
```

| Calimero Field | Java Field | Unit Conversion | Status | Notes |
|----------------|------------|-----------------|--------|-------|
| `name` | `name` | None | ✅ Working | Interface name |
| `type` | `type` | None | ✅ Working | ether/loopback/etc |
| `operState` | `status` | None | ✅ Working | **Field name mismatch!** |
| `macAddress` | `macAddress` | None | ⚠️ Empty for some | Calimero limitation |
| `mtu` | `mtu` | None | ⚠️ 0 for some | Calimero limitation |
| `addresses[]` | `addresses` | Object → String | ✅ Working | **Array parsing!** |
| `addresses[].address` | `addresses[i]` | Extract IP | ✅ Working | IPv4/IPv6 |
| `addresses[].family` | N/A | Ignored | - | inet/inet6 |
| `addresses[].prefixLength` | N/A | Ignored | - | CIDR prefix |

**Known Issues**:
- **operState vs status**: Field name mismatch handled in parser
- **addresses**: Complex nested structure - parser extracts IP addresses correctly
- **macAddress/mtu**: Often empty/0 for virtual interfaces

---

## 5. Common Parsing Patterns

### Pattern 1: Null-Safe Field Access
```java
if (json.has("fieldName") && !json.get("fieldName").isJsonNull()) {
    value = json.get("fieldName").getAsType();
}
```

### Pattern 2: Nested Object Parsing
```java
if (json.has("parent") && json.get("parent").isJsonObject()) {
    JsonObject parent = json.getAsJsonObject("parent");
    field = parent.get("child").getAsType();
}
```

### Pattern 3: Array Parsing
```java
if (json.has("items") && json.get("items").isJsonArray()) {
    JsonArray items = json.getAsJsonArray("items");
    items.forEach(element -> {
        // Process each element
    });
}
```

### Pattern 4: Unit Conversion
```java
// Bytes to MB
memoryMB = json.get("memBytes").getAsLong() / (1024 * 1024);

// Bytes to GB  
diskGB = json.get("diskBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
```

---

## 6. Verification Checklist

When adding new Calimero API endpoints:

- [ ] Test API with `curl` to see actual JSON response
- [ ] Document Calimero field names (C++ side)
- [ ] Document Java field names (parser side)
- [ ] Identify unit conversions needed
- [ ] Handle null/empty values gracefully
- [ ] Add JavaDoc with actual JSON example
- [ ] Update this mapping document
- [ ] Test with Playwright BAB Dashboard test

---

## 7. Testing Commands

### Test System Metrics
```bash
curl -s -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"metrics"}}' | jq '.'
```

### Test Process List
```bash
curl -s -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"processes"}}' | jq '.data.processes[0:3]'
```

### Test Disk Usage
```bash
curl -s -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"diskUsage"}}' | jq '.data.disks[0]'
```

### Test Network Interfaces
```bash
curl -s -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"network","data":{"operation":"getInterfaces"}}' | jq '.data.interfaces[0]'
```

---

## 8. Current Status Summary

| Component | Parsing Status | Display Status | Known Issues |
|-----------|---------------|----------------|--------------|
| **System Metrics** | ✅ 100% | ✅ Working | None |
| **Process List** | ✅ 95% | ✅ Working | cpuPercent=0, no cmdline |
| **Disk Usage** | ✅ 100% | ✅ Working | filesystem/device empty |
| **Network Interfaces** | ✅ 100% | ✅ Working | MAC/MTU sometimes empty |

**Overall Status**: ✅ **PRODUCTION READY**

All critical metrics are parsed correctly. Known issues are Calimero C++ implementation limitations, not parser bugs.

---

## 9. Future Enhancements

### Calimero C++ Side
1. **Process CPU usage**: Implement time-based CPU sampling
2. **Process cmdline**: Read `/proc/[pid]/cmdline` for command arguments
3. **Disk device names**: Parse `/proc/mounts` for device and filesystem type
4. **Network MAC/MTU**: Ensure all interfaces report correct values

### Derbent Java Side
5. **Virtual field binding**: Support binding to interface method results (e.g., `getIpv4Display()`)
6. **Real-time updates**: WebSocket support for live metric streaming
7. **Historical data**: Time-series storage for metrics trending
8. **Alerting**: Threshold-based notifications for critical metrics

---

**Document Status**: Complete and verified ✅  
**Last Updated**: 2026-02-01 23:00 UTC+3
