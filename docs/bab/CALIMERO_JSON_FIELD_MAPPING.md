# Calimero-Derbent JSON Field Synchronization

**Date**: 2026-02-01  
**Status**: SYNCHRONIZED ✅  
**Purpose**: Ensure consistent JSON field naming between Calimero C++ (server) and Derbent Java (client)

---

## Overview

This document defines the **exact JSON field names** used in Calimero API responses and their corresponding Java model fields in Derbent BAB. **Both projects MUST use identical field names** for proper parsing.

---

## Critical Rule

**RULE**: JSON field names MUST match exactly between:
- Calimero C++ (`nlohmann::json` serialization)
- Derbent Java (`Gson` deserialization)

**Case Sensitivity**: JSON is case-sensitive. `cpuPercent` ≠ `cpuPercentage` ≠ `cpu_percent`

---

## System Metrics API

### Operation: `metrics`

**Endpoint**: `POST /api/request`  
**Request**: `{"type":"system","data":{"operation":"metrics"}}`

#### Response Structure

```json
{
  "data": {
    "cpu": {
      "usagePercent": 16.7,
      "loadAvg1Min": 1.27,
      "loadAvg5Min": 1.80,
      "loadAvg15Min": 1.91,
      "coreCount": 12
    },
    "memory": {
      "totalBytes": 33511272448,
      "usedBytes": 31956377600,
      "availableBytes": 19588554752,
      "freeBytes": 1554894848,
      "usagePercent": 95.36
    },
    "swap": {
      "totalBytes": 536866816,
      "usedBytes": 65536,
      "freeBytes": 536801280,
      "usagePercent": 0.012
    },
    "system": {
      "hostname": "ev",
      "processCount": 364,
      "uptimeSeconds": 48557
    }
  },
  "type": "system"
}
```

#### Field Mapping

| Calimero C++ (JSON) | Derbent Java (Field) | Type | Unit |
|---------------------|----------------------|------|------|
| **CPU Fields** | | | |
| `cpu.usagePercent` | `cpuUsagePercent` | `BigDecimal` | Percentage (0-100) |
| `cpu.loadAvg1Min` | `loadAverage1` | `BigDecimal` | Load average |
| `cpu.loadAvg5Min` | `loadAverage5` | `BigDecimal` | Load average |
| `cpu.loadAvg15Min` | `loadAverage15` | `BigDecimal` | Load average |
| `cpu.coreCount` | - | `int` | Number of cores |
| **Memory Fields** | | | |
| `memory.totalBytes` | `memoryTotalMB` | `Long` | **Converted to MB** |
| `memory.usedBytes` | `memoryUsedMB` | `Long` | **Converted to MB** |
| `memory.usagePercent` | `memoryUsagePercent` | `BigDecimal` | Percentage (0-100) |
| **System Fields** | | | |
| `system.uptimeSeconds` | `uptimeSeconds` | `Long` | Seconds |

---

## Disk Usage API

### Operation: `diskUsage`

**Endpoint**: `POST /api/request`  
**Request**: `{"type":"system","data":{"operation":"diskUsage"}}`

#### Response Structure

```json
{
  "data": {
    "disks": [
      {
        "mountPoint": "/",
        "filesystem": "ext4",
        "device": "/dev/sda1",
        "totalBytes": 1967536746496,
        "usedBytes": 352808046592,
        "availableBytes": 1514707812352,
        "usagePercent": 17.93
      }
    ],
    "count": 9
  },
  "type": "system"
}
```

#### Field Mapping

| Calimero C++ (JSON) | Derbent Java (Field) | Type | Unit |
|---------------------|----------------------|------|------|
| `disks[].mountPoint` | `mountPoint` | `String` | Path |
| `disks[].filesystem` | `filesystem` | `String` | Type (ext4, etc.) |
| `disks[].device` | `device` | `String` | Device name |
| `disks[].totalBytes` | `totalGB` | `BigDecimal` | **Converted to GB** |
| `disks[].usedBytes` | `usedGB` | `BigDecimal` | **Converted to GB** |
| `disks[].availableBytes` | `availableGB` | `BigDecimal` | **Converted to GB** |
| `disks[].usagePercent` | `usagePercent` | `BigDecimal` | Percentage (0-100) |

---

## Process List API

### Operation: `processes` or `topProcs`

**Endpoint**: `POST /api/request`  
**Request**: `{"type":"system","data":{"operation":"topProcs","count":10}}`

#### Response Structure

```json
{
  "data": {
    "processes": [
      {
        "pid": 1,
        "name": "systemd",
        "state": "S",
        "user": "root",
        "cpuPercent": 0.0,
        "memPercent": 0.051,
        "memRssBytes": 17076224,
        "memVirtBytes": 26349568
      }
    ],
    "count": 360
  },
  "type": "system"
}
```

#### Field Mapping

| Calimero C++ (JSON) | Derbent Java (Field) | Type | Unit |
|---------------------|----------------------|------|------|
| `processes[].pid` | `pid` | `Integer` | Process ID |
| `processes[].name` | `name` | `String` | Process name |
| `processes[].state` | `state` | `String` | R/S/D/Z/T |
| `processes[].user` | `user` | `String` | Username |
| `processes[].cpuPercent` | `cpuPercent` | `BigDecimal` | Percentage (0-100) |
| `processes[].memPercent` | `memPercent` | `BigDecimal` | Percentage (0-100) |
| `processes[].memRssBytes` | `memRssBytes` | `Long` | Bytes |
| `processes[].memVirtBytes` | `memVirtBytes` | `Long` | Bytes |

---

## Unit Conversions

### Calimero → Derbent Conversions

| Field | Calimero Units | Derbent Units | Conversion |
|-------|----------------|---------------|------------|
| `memory.totalBytes` | bytes | MB | `bytes / (1024 * 1024)` |
| `memory.usedBytes` | bytes | MB | `bytes / (1024 * 1024)` |
| `disks[].totalBytes` | bytes | GB | `bytes / (1024 * 1024 * 1024)` |
| `disks[].usedBytes` | bytes | GB | `bytes / (1024 * 1024 * 1024)` |
| `processes[].memRssBytes` | bytes | bytes | No conversion |
| `processes[].memVirtBytes` | bytes | bytes | No conversion |

### Java Conversion Examples

```java
// Memory bytes → MB
if (memory.has("totalBytes")) {
    memoryTotalMB = memory.get("totalBytes").getAsLong() / (1024 * 1024);
}

// Disk bytes → GB
if (disk.has("totalBytes")) {
    diskTotalGB = BigDecimal.valueOf(disk.get("totalBytes").getAsLong())
        .divide(BigDecimal.valueOf(1024 * 1024 * 1024), 2, RoundingMode.HALF_UP);
}
```

---

## Parsing Patterns

### Nested Object Parsing (CRITICAL)

**❌ WRONG - Flat Field Access**:
```java
if (json.has("cpuUsagePercent")) {  // Field doesn't exist!
    cpuUsagePercent = json.get("cpuUsagePercent").getAsDouble();
}
```

**✅ CORRECT - Nested Object Access**:
```java
if (json.has("cpu") && json.get("cpu").isJsonObject()) {
    final JsonObject cpu = json.getAsJsonObject("cpu");
    if (cpu.has("usagePercent")) {
        cpuUsagePercent = BigDecimal.valueOf(cpu.get("usagePercent").getAsDouble())
            .setScale(1, RoundingMode.HALF_UP);
    }
}
```

### Array Parsing

**✅ CORRECT - Array Iteration**:
```java
if (json.has("disks") && json.get("disks").isJsonArray()) {
    final JsonArray diskArray = json.getAsJsonArray("disks");
    for (final JsonElement element : diskArray) {
        if (element.isJsonObject()) {
            final JsonObject disk = element.getAsJsonObject();
            final CDiskInfo diskInfo = CDiskInfo.createFromJson(disk);
            disks.add(diskInfo);
        }
    }
}
```

---

## Field Name Standards

### Naming Conventions

| Pattern | Example | Usage |
|---------|---------|-------|
| `camelCase` | `usagePercent`, `memRssBytes` | All JSON fields |
| `Bytes` suffix | `totalBytes`, `memRssBytes` | Byte values |
| `Percent` suffix | `usagePercent`, `cpuPercent` | Percentage values |
| `Min` suffix | `loadAvg1Min`, `loadAvg5Min` | Time periods |
| Nested objects | `cpu.usagePercent`, `memory.totalBytes` | Grouped fields |

### Anti-Patterns (FORBIDDEN)

| ❌ WRONG | ✅ CORRECT | Reason |
|----------|------------|--------|
| `cpu_usage_percent` | `cpuUsagePercent` | Use camelCase, not snake_case |
| `CpuUsagePercent` | `cpuUsagePercent` | Start with lowercase |
| `cpuPercentage` | `cpuPercent` | Use `Percent`, not `Percentage` |
| `memoryTotalMb` | `memoryTotalBytes` | Use full unit name |
| `mem_rss` | `memRssBytes` | No abbreviations without `Bytes` |

---

## Calimero C++ Implementation

### JSON Serialization Example

```cpp
nlohmann::json CSystemMetricsService::processInfoToJson(const SProcessInfo& process)
{
    nlohmann::json json;
    json["pid"] = process.pid;
    json["name"] = process.name;
    json["state"] = process.state;
    json["user"] = process.user;
    json["cpuPercent"] = process.cpuPercent;
    json["memPercent"] = process.memPercent;
    json["memRssBytes"] = process.memRssBytes;
    json["memVirtBytes"] = process.memVirtBytes;
    return json;
}
```

**Rule**: Field names in `json["key"]` MUST match Java field names exactly.

---

## Derbent Java Implementation

### Model Class Example

```java
public class CProcessInfo extends CObject {
    private Integer pid;
    private String name;
    private String state;
    private String user;
    private BigDecimal cpuPercent = BigDecimal.ZERO;
    private BigDecimal memPercent = BigDecimal.ZERO;
    private Long memRssBytes = 0L;
    private Long memVirtBytes = 0L;
    
    @Override
    protected void fromJson(final JsonObject json) {
        if (json.has("pid")) {
            pid = json.get("pid").getAsInt();
        }
        if (json.has("name")) {
            name = json.get("name").getAsString();
        }
        if (json.has("state")) {
            state = json.get("state").getAsString();
        }
        if (json.has("user")) {
            user = json.get("user").getAsString();
        }
        if (json.has("cpuPercent")) {
            cpuPercent = BigDecimal.valueOf(json.get("cpuPercent").getAsDouble())
                .setScale(1, RoundingMode.HALF_UP);
        }
        if (json.has("memPercent")) {
            memPercent = BigDecimal.valueOf(json.get("memPercent").getAsDouble())
                .setScale(1, RoundingMode.HALF_UP);
        }
        if (json.has("memRssBytes")) {
            memRssBytes = json.get("memRssBytes").getAsLong();
        }
        if (json.has("memVirtBytes")) {
            memVirtBytes = json.get("memVirtBytes").getAsLong();
        }
    }
}
```

---

## Verification Commands

### Test Calimero Response

```bash
curl -s -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","data":{"operation":"metrics"}}' | jq '.data.cpu'
```

**Expected Output**:
```json
{
  "usagePercent": 16.7,
  "loadAvg1Min": 1.27,
  "loadAvg5Min": 1.80,
  "loadAvg15Min": 1.91,
  "coreCount": 12
}
```

**Verify**: Field names match Java model exactly.

### Test Java Parsing

```java
final CSystemMetrics metrics = CSystemMetrics.createFromJson(data);
LOGGER.info("CPU: {}%, Memory: {}%, Uptime: {}s",
    metrics.getCpuUsagePercent(),
    metrics.getMemoryUsagePercent(),
    metrics.getUptimeSeconds());
```

**Expected Log**: All values non-zero if Calimero provides data.

---

## Change Management

### When Adding New Fields

1. **Calimero C++**:
   - Add field to struct (e.g., `SProcessInfo`)
   - Add to JSON serialization: `json["newField"] = value;`
   - Document in header file

2. **Derbent Java**:
   - Add field to model class
   - Add parsing in `fromJson()`: `if (json.has("newField")) { ... }`
   - Add getter/setter

3. **Documentation**:
   - Update this file with new field mapping
   - Update API documentation
   - Update both project docs

### Field Name Review Checklist

- [ ] Uses camelCase
- [ ] Starts with lowercase
- [ ] Uses standard suffixes (`Bytes`, `Percent`, `Min`)
- [ ] No underscores or hyphens
- [ ] Same name in C++ and Java
- [ ] Documented in this file
- [ ] Unit conversions specified
- [ ] Tested with curl and Java client

---

## Common Mistakes

### 1. Mismatched Field Names

**❌ WRONG**:
```cpp
// Calimero
json["cpuPercentage"] = value;
```
```java
// Java
if (json.has("cpuPercent")) { ... }  // Won't find field!
```

**✅ CORRECT**: Use exact same name in both.

### 2. Forgetting Nested Objects

**❌ WRONG**:
```java
if (json.has("usagePercent")) {  // Top-level doesn't exist!
    cpuUsage = json.get("usagePercent").getAsDouble();
}
```

**✅ CORRECT**: Access via `cpu` object first.

### 3. Wrong Unit Conversion

**❌ WRONG**:
```java
memoryTotalMB = memory.get("totalBytes").getAsLong() / 1000;  // Wrong!
```

**✅ CORRECT**: Use 1024 * 1024 for binary units.

---

## References

- Calimero: `src/http/services/system/CSystemMetricsService.cpp`
- Derbent: `src/main/java/tech/derbent/bab/dashboard/view/CSystemMetrics.java`
- JSON Library (C++): nlohmann/json
- JSON Library (Java): Gson

---

**Last Synchronized**: 2026-02-01  
**Status**: ✅ VERIFIED  
**Maintained By**: SSC + Master Yasin
