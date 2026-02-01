# Calimero API Response Patterns - Parsing Guide

**Date**: 2026-02-01  
**Version**: 1.0  
**Status**: MANDATORY for all BAB Calimero integration  
**Calimero Server**: `~/git/calimero/` (Port 8077)

---

## 🤖 Agent Verification

**SSC WAS HERE!!** Praise her diligent debugging of nested JSON parsing! 🌟

---

## Overview

This document establishes **mandatory JSON parsing patterns** for Calimero C++ Gateway Server API responses. The Calimero API returns **nested JSON structures**, not flat field mappings.

**CRITICAL**: Failure to parse nested structures correctly results in ALL metric values being zero!

---

## 1. System Metrics API (`operation="metrics"`)

### 1.1 Calimero Response Structure (Actual)

```json
{
  "cpu": {
    "usagePercent": 15.5,
    "loadAvg1Min": 1.5,
    "loadAvg5Min": 1.2,
    "loadAvg15Min": 1.0,
    "coreCount": 8
  },
  "memory": {
    "totalBytes": 17179869184,
    "usedBytes": 2147483648,
    "availableBytes": 15032385536,
    "freeBytes": 14979909632,
    "usagePercent": 12.5
  },
  "swap": {
    "totalBytes": 8589934592,
    "usedBytes": 0,
    "freeBytes": 8589934592,
    "usagePercent": 0.0
  },
  "system": {
    "uptimeSeconds": 86400,
    "processCount": 342,
    "hostname": "localhost"
  }
}
```

### 1.2 Correct Java Parsing Pattern

**RULE**: Parse nested objects first, then extract fields.

#### ✅ CORRECT - Nested JSON Parsing

```java
@Override
protected void fromJson(final JsonObject json) {
    try {
        // Parse nested CPU metrics
        if (json.has("cpu") && json.get("cpu").isJsonObject()) {
            final JsonObject cpu = json.getAsJsonObject("cpu");
            if (cpu.has("usagePercent")) {
                cpuUsagePercent = BigDecimal.valueOf(cpu.get("usagePercent").getAsDouble())
                    .setScale(1, RoundingMode.HALF_UP);
            }
            if (cpu.has("loadAvg1Min")) {
                loadAverage1 = BigDecimal.valueOf(cpu.get("loadAvg1Min").getAsDouble())
                    .setScale(2, RoundingMode.HALF_UP);
            }
            if (cpu.has("loadAvg5Min")) {
                loadAverage5 = BigDecimal.valueOf(cpu.get("loadAvg5Min").getAsDouble())
                    .setScale(2, RoundingMode.HALF_UP);
            }
            if (cpu.has("loadAvg15Min")) {
                loadAverage15 = BigDecimal.valueOf(cpu.get("loadAvg15Min").getAsDouble())
                    .setScale(2, RoundingMode.HALF_UP);
            }
        }
        
        // Parse nested memory metrics
        if (json.has("memory") && json.get("memory").isJsonObject()) {
            final JsonObject memory = json.getAsJsonObject("memory");
            if (memory.has("usedBytes")) {
                // Convert bytes to MB
                memoryUsedMB = memory.get("usedBytes").getAsLong() / (1024 * 1024);
            }
            if (memory.has("totalBytes")) {
                // Convert bytes to MB
                memoryTotalMB = memory.get("totalBytes").getAsLong() / (1024 * 1024);
            }
            if (memory.has("usagePercent")) {
                memoryUsagePercent = BigDecimal.valueOf(memory.get("usagePercent").getAsDouble())
                    .setScale(1, RoundingMode.HALF_UP);
            }
        }
        
        // Parse nested system metrics
        if (json.has("system") && json.get("system").isJsonObject()) {
            final JsonObject system = json.getAsJsonObject("system");
            if (system.has("uptimeSeconds")) {
                uptimeSeconds = system.get("uptimeSeconds").getAsLong();
            }
        }
        
    } catch (final Exception e) {
        LOGGER.error("Error parsing system metrics from JSON: {}", e.getMessage(), e);
    }
}
```

#### ❌ INCORRECT - Flat Field Parsing (BUG!)

```java
// ❌ WRONG - This results in ALL ZEROS!
@Override
protected void fromJson(final JsonObject json) {
    try {
        // These fields don't exist in Calimero response!
        if (json.has("cpuUsagePercent")) {  // ❌ Field doesn't exist!
            cpuUsagePercent = BigDecimal.valueOf(json.get("cpuUsagePercent").getAsDouble());
        }
        if (json.has("memoryUsedMB")) {  // ❌ Field doesn't exist!
            memoryUsedMB = json.get("memoryUsedMB").getAsLong();
        }
        // Result: All metrics remain at default zero values!
    } catch (final Exception e) {
        LOGGER.error("Error parsing: {}", e.getMessage());
    }
}
```

### 1.3 Unit Conversion Rules

| Calimero Field | Units | Java Field | Units | Conversion |
|----------------|-------|------------|-------|------------|
| `memory.totalBytes` | bytes | `memoryTotalMB` | megabytes | `bytes / (1024 * 1024)` |
| `memory.usedBytes` | bytes | `memoryUsedMB` | megabytes | `bytes / (1024 * 1024)` |
| `cpu.usagePercent` | % | `cpuUsagePercent` | % | Direct (scale 1) |
| `memory.usagePercent` | % | `memoryUsagePercent` | % | Direct (scale 1) |
| `cpu.loadAvg1Min` | load | `loadAverage1` | load | Direct (scale 2) |

---

## 2. Disk Usage API (`operation="diskUsage"`)

### 2.1 Calimero Response Structure

```json
{
  "disks": [
    {
      "mountPoint": "/",
      "filesystem": "ext4",
      "device": "/dev/sda1",
      "totalBytes": 536870912000,
      "usedBytes": 53687091200,
      "availableBytes": 483183820800,
      "usagePercent": 10.0
    },
    {
      "mountPoint": "/home",
      "filesystem": "ext4",
      "device": "/dev/sda2",
      "totalBytes": 1073741824000,
      "usedBytes": 107374182400,
      "availableBytes": 966367641600,
      "usagePercent": 10.0
    }
  ],
  "count": 2
}
```

### 2.2 Parsing Pattern

**RULE**: Disk metrics are an **array**, not a single object. Parse the first disk or aggregate all disks.

```java
public void parseDiskUsage(final JsonObject json) {
    if (json.has("disks") && json.get("disks").isJsonArray()) {
        final JsonArray disks = json.getAsJsonArray("disks");
        if (disks.size() > 0) {
            // Parse first disk (root filesystem)
            final JsonObject rootDisk = disks.get(0).getAsJsonObject();
            
            if (rootDisk.has("totalBytes")) {
                diskTotalGB = BigDecimal.valueOf(rootDisk.get("totalBytes").getAsLong())
                    .divide(BigDecimal.valueOf(1024 * 1024 * 1024), 2, RoundingMode.HALF_UP);
            }
            if (rootDisk.has("usedBytes")) {
                diskUsedGB = BigDecimal.valueOf(rootDisk.get("usedBytes").getAsLong())
                    .divide(BigDecimal.valueOf(1024 * 1024 * 1024), 2, RoundingMode.HALF_UP);
            }
            if (rootDisk.has("usagePercent")) {
                diskUsagePercent = BigDecimal.valueOf(rootDisk.get("usagePercent").getAsDouble())
                    .setScale(1, RoundingMode.HALF_UP);
            }
        }
    }
}
```

**CRITICAL**: Disk metrics are NOT included in the `operation="metrics"` response. You MUST make a separate API call with `operation="diskUsage"`.

---

## 3. API Operation Summary

### 3.1 Available Operations

| Operation | Type | Response Structure | Use Case |
|-----------|------|-------------------|----------|
| `metrics` | system | Nested objects (cpu/memory/swap/system) | Dashboard metrics |
| `cpuInfo` | system | Nested object (usagePercent, loadAvg*, coreCount) | CPU details |
| `memInfo` | system | Nested object (totalBytes, usedBytes, usagePercent) | Memory details |
| `diskUsage` | system | Array of disk objects | Disk space monitoring |
| `processes` | system | Array of process objects | Process list |
| `topProcs` | system | Array of top N processes | Top CPU/memory processes |
| `getInterfaces` | network | Array of interface objects | Network interfaces |

### 3.2 Request Format (Universal)

```java
final CCalimeroRequest request = CCalimeroRequest.builder()
    .type("system")           // Service type: system, network, disk, etc.
    .operation("metrics")     // Operation name
    .parameter("count", 10)   // Optional parameters (for topProcs, etc.)
    .build();

final CCalimeroResponse response = clientProject.sendRequest(request);
```

---

## 4. Common Parsing Mistakes

### 4.1 Mistake: Expecting Flat Structure

**Symptom**: All metrics parse as zero, no errors logged.

**Cause**: Code expects `json.cpuUsagePercent` but Calimero returns `json.cpu.usagePercent`.

**Fix**: Parse nested objects first.

### 4.2 Mistake: Wrong Unit Assumptions

**Symptom**: Memory shows as 17179869184 MB instead of 16384 MB.

**Cause**: Calimero returns bytes, not megabytes.

**Fix**: Divide by `1024 * 1024` for MB conversion.

### 4.3 Mistake: Missing Disk API Call

**Symptom**: Disk usage always shows zero, even when CPU/memory work.

**Cause**: Disk metrics require separate `operation="diskUsage"` call.

**Fix**: Make two API calls - one for metrics, one for disk usage.

### 4.4 Mistake: Not Checking isJsonObject()

**Symptom**: `ClassCastException` or `NullPointerException` during parsing.

**Cause**: Field exists but is not a JSON object (could be null or primitive).

**Fix**: Always check `json.has("field") && json.get("field").isJsonObject()`.

---

## 5. Testing Patterns

### 5.1 Manual API Testing with curl

```bash
# Start Calimero server
cd ~/git/calimero/build
./calimero

# Test metrics endpoint (requires authentication)
curl -s -X POST http://localhost:8077/api/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","operation":"metrics"}' | jq '.'

# Test disk usage endpoint
curl -s -X POST http://localhost:8077/api/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","operation":"diskUsage"}' | jq '.'

# Test public health endpoint (no auth required)
curl -s http://localhost:8077/health | jq '.'
```

### 5.2 Calimero Configuration

**Default Port**: 8077 (NOT 8080)  
**Auth Token**: `test-token-123` (underscores, not hyphens)  
**Config File**: `~/git/calimero/config/http_server.json`

```json
{
    "authToken": "test-token-123",
    "host": "0.0.0.0",
    "httpPort": 8077,
    "idleTimeoutSec": 60,
    "readTimeoutSec": 30,
    "runState": "normal",
    "writeTimeoutSec": 30
}
```

### 5.3 Integration Testing

```java
@Test
void testSystemMetricsParsing() {
    // Mock Calimero response with nested structure
    final String jsonResponse = """
        {
          "cpu": {
            "usagePercent": 15.5,
            "loadAvg1Min": 1.5
          },
          "memory": {
            "totalBytes": 17179869184,
            "usedBytes": 2147483648,
            "usagePercent": 12.5
          },
          "system": {
            "uptimeSeconds": 86400
          }
        }
        """;
    
    final JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
    final CSystemMetrics metrics = CSystemMetrics.createFromJson(json);
    
    // Verify parsing succeeded
    assertEquals(BigDecimal.valueOf(15.5).setScale(1), metrics.getCpuUsagePercent());
    assertEquals(16384L, metrics.getMemoryTotalMB());  // 17179869184 / (1024*1024)
    assertEquals(2048L, metrics.getMemoryUsedMB());    // 2147483648 / (1024*1024)
    assertEquals(BigDecimal.valueOf(12.5).setScale(1), metrics.getMemoryUsagePercent());
    assertEquals(86400L, metrics.getUptimeSeconds());
}
```

---

## 6. Calimero Server Source Reference

### 6.1 Key Implementation Files

| File | Purpose | Key Methods |
|------|---------|-------------|
| `src/http/webservice/processors/csystemprocessor.cpp` | System operations handler | `handleSystemMetrics()`, `handleCpuInfo()`, `handleMemInfo()`, `handleDiskUsage()` |
| `src/http/services/system/CSystemMetricsService.cpp` | Metrics collection | `getSystemMetrics()`, `metricsToJson()` |
| `src/http/services/system/CSystemMetricsService.h` | Struct definitions | `SSystemMetrics`, `SDiskUsage`, `SProcessInfo` |
| `src/http/server/chttpserver.cpp` | HTTP server | Authentication, routing |

### 6.2 C++ Response Generation

```cpp
// From CSystemMetricsService.cpp
nlohmann::json CSystemMetricsService::metricsToJson(const SSystemMetrics& metrics)
{
    nlohmann::json json;
    
    json["cpu"] = nlohmann::json::object();
    json["cpu"]["usagePercent"] = metrics.cpuUsagePercent;
    json["cpu"]["loadAvg1Min"] = metrics.cpuLoadAvg1Min;
    json["cpu"]["loadAvg5Min"] = metrics.cpuLoadAvg5Min;
    json["cpu"]["loadAvg15Min"] = metrics.cpuLoadAvg15Min;
    json["cpu"]["coreCount"] = metrics.cpuCoreCount;
    
    json["memory"] = nlohmann::json::object();
    json["memory"]["totalBytes"] = metrics.memTotalBytes;
    json["memory"]["usedBytes"] = metrics.memUsedBytes;
    json["memory"]["usagePercent"] = metrics.memUsagePercent;
    
    json["swap"] = nlohmann::json::object();
    json["swap"]["totalBytes"] = metrics.swapTotalBytes;
    json["swap"]["usedBytes"] = metrics.swapUsedBytes;
    json["swap"]["usagePercent"] = metrics.swapUsagePercent;
    
    json["system"] = nlohmann::json::object();
    json["system"]["uptimeSeconds"] = metrics.uptimeSeconds;
    json["system"]["processCount"] = metrics.processCount;
    json["system"]["hostname"] = metrics.hostname;
    
    return json;
}
```

---

## 7. Best Practices

### 7.1 Defensive Parsing

**RULE**: Always use null-safe checks for nested objects.

```java
// ✅ CORRECT - Null-safe nested parsing
if (json.has("cpu") && json.get("cpu").isJsonObject()) {
    final JsonObject cpu = json.getAsJsonObject("cpu");
    if (cpu.has("usagePercent")) {
        // Safe to parse
    }
}

// ❌ WRONG - Can throw NullPointerException
final JsonObject cpu = json.getAsJsonObject("cpu");
cpuUsagePercent = BigDecimal.valueOf(cpu.get("usagePercent").getAsDouble());
```

### 7.2 Logging

**RULE**: Log actual vs expected values when debugging parsing issues.

```java
LOGGER.debug("Parsing system metrics - CPU: {}, Memory: {} MB, Uptime: {}s",
    metrics.getCpuUsagePercent(),
    metrics.getMemoryUsedMB(),
    metrics.getUptimeSeconds());
```

### 7.3 Error Handling

**RULE**: Parse errors should NOT crash the application. Return default values.

```java
try {
    // Parse nested JSON
} catch (final Exception e) {
    LOGGER.error("Error parsing system metrics from JSON: {}", e.getMessage(), e);
    // Metrics remain at default (zero) values - graceful degradation
}
```

---

## 8. Migration Checklist

When updating existing parsing code:

- [ ] Identify flat field access (e.g., `json.get("cpuUsagePercent")`)
- [ ] Check Calimero C++ source for actual response structure
- [ ] Update to nested object parsing (e.g., `json.cpu.usagePercent`)
- [ ] Add unit conversion if needed (bytes → MB/GB)
- [ ] Add null-safety checks (`has()` + `isJsonObject()`)
- [ ] Update JavaDoc with actual response structure
- [ ] Test with real Calimero server response
- [ ] Verify metrics display correctly in UI
- [ ] Check logs for parsing errors/warnings

---

## 9. Related Documentation

- **HTTP Client Architecture**: `docs/bab/HTTP_CLIENT_ARCHITECTURE.md`
- **Calimero Integration Rules**: `docs/BAB_CALIMERO_INTEGRATION_RULES.md`
- **Dashboard Implementation**: `src/main/java/tech/derbent/bab/dashboard/`
- **System Metrics Client**: `src/main/java/tech/derbent/bab/dashboard/service/CSystemMetricsCalimeroClient.java`
- **Metrics Model**: `src/main/java/tech/derbent/bab/dashboard/view/CSystemMetrics.java`

---

## 10. Troubleshooting

### Problem: All metrics show zero

**Solution**: Check if you're parsing nested objects. Use browser dev tools to inspect actual API response.

### Problem: Memory shows huge numbers (billions)

**Solution**: Calimero returns bytes, not megabytes. Divide by `1024 * 1024`.

### Problem: Disk usage always zero

**Solution**: Make separate `operation="diskUsage"` API call. Not included in `metrics` response.

### Problem: ClassCastException during parsing

**Solution**: Add `isJsonObject()` checks before calling `getAsJsonObject()`.

### Problem: Calimero returns 401 Unauthorized

**Solution**: Check auth token format. Should be `test-token-123` (underscores), sent as `Authorization: Bearer test-token-123`.

---

**Last Updated**: 2026-02-01  
**Contributors**: SSC (debugging), Master Yasin (documentation)  
**Status**: PRODUCTION READY ✅
