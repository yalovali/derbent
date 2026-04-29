# Calimero API Integration - Nested JSON Parsing Fix (2026-02-01)

**Date**: 2026-02-01  
**Status**: COMPLETED ✅  
**Bug Type**: Critical - All system metrics parsing as zero  
**Root Cause**: Incorrect JSON parsing (expected flat, Calimero returns nested)

---

## Problem Summary

**Symptom**: Dashboard metrics showed all zeros for CPU, memory, and disk usage despite Calimero server running and responding to API calls.

**Root Cause**: Java parsing code expected **flat JSON fields** like `cpuUsagePercent`, but Calimero C++ server returns **nested JSON objects** like `{"cpu": {"usagePercent": 15.5}}`.

**Impact**: 
- Dashboard metrics completely non-functional
- System monitoring unusable
- No visibility into actual device resource usage

---

## Technical Details

### Expected (Wrong) Format

```java
// What the Java code was looking for (INCORRECT)
{
  "cpuUsagePercent": 15.5,
  "memoryUsedMB": 2048,
  "memoryTotalMB": 16384,
  "diskUsagePercent": 10.1
}
```

### Actual Calimero Response

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

---

## Fix Applied

### File Changed

`src/main/java/tech/derbent/bab/dashboard/view/CSystemMetrics.java`

### Fix Pattern

**Before (Broken)**:
```java
if (json.has("cpuUsagePercent")) {  // Field doesn't exist!
    cpuUsagePercent = BigDecimal.valueOf(json.get("cpuUsagePercent").getAsDouble());
}
```

**After (Fixed)**:
```java
// Parse nested CPU metrics
if (json.has("cpu") && json.get("cpu").isJsonObject()) {
    final JsonObject cpu = json.getAsJsonObject("cpu");
    if (cpu.has("usagePercent")) {
        cpuUsagePercent = BigDecimal.valueOf(cpu.get("usagePercent").getAsDouble())
            .setScale(1, RoundingMode.HALF_UP);
    }
}

// Parse nested memory metrics
if (json.has("memory") && json.get("memory").isJsonObject()) {
    final JsonObject memory = json.getAsJsonObject("memory");
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
```

### Unit Conversions Added

| Field | Calimero Units | Java Units | Conversion |
|-------|----------------|------------|------------|
| `memory.totalBytes` | bytes | MB | `bytes / (1024 * 1024)` |
| `memory.usedBytes` | bytes | MB | `bytes / (1024 * 1024)` |

---

## Additional Findings

### Disk Metrics Not in Metrics Response

**Discovery**: Disk usage metrics are **NOT included** in the `operation="metrics"` response. They require a separate API call with `operation="diskUsage"`.

**Calimero Disk Response Structure**:
```json
{
  "disks": [
    {
      "mountPoint": "/",
      "filesystem": "ext4",
      "totalBytes": 536870912000,
      "usedBytes": 53687091200,
      "usagePercent": 10.0
    }
  ],
  "count": 1
}
```

### Calimero Server Configuration

- **Default Port**: 8077 (NOT 8080)
- **Auth Token Format**: `test-token-123` (underscores, not hyphens)
- **Config Location**: `~/git/calimero/config/http_server.json`
- **Binary Location**: `~/git/calimero/build/calimero`

---

## Testing

### Manual API Test

```bash
# Start Calimero
cd ~/git/calimero/build
./calimero

# Test metrics endpoint (requires auth)
curl -s -X POST http://localhost:8077/api/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","operation":"metrics"}' | jq '.'

# Expected output: Nested JSON with cpu/memory/swap/system objects
```

### Verification Steps

1. ✅ Start Calimero server
2. ✅ Navigate to BAB Dashboard
3. ✅ Verify CPU usage shows actual percentage (not 0%)
4. ✅ Verify memory usage shows actual MB values (not 0 MB)
5. ✅ Verify load averages display correctly
6. ✅ Verify uptime displays in human-readable format

---

## Documentation Created

### New Document

**`docs/bab/CALIMERO_API_RESPONSE_PATTERNS.md`**

Complete guide covering:
- ✅ Nested JSON parsing patterns
- ✅ Unit conversion rules
- ✅ All Calimero API operations
- ✅ Common parsing mistakes
- ✅ Testing patterns with curl examples
- ✅ Troubleshooting guide

### Updated Documents

**`docs/BAB_CALIMERO_INTEGRATION_RULES.md`** (Version 1.1)
- Added reference to response patterns document
- Added quick example of nested vs flat parsing
- Added actual response structure examples

---

## Lessons Learned

### API Integration Best Practices

1. **Always verify actual API response format** - Don't assume flat structure
2. **Check Calimero C++ source code** for authoritative response structure
3. **Use nested object parsing** with proper null-safety checks
4. **Log actual vs expected values** during debugging
5. **Test with real Calimero server** before assuming mocks are correct

### Debugging Tips

1. **Use curl to inspect raw API responses** before writing Java parsing code
2. **Check Calimero server logs** for errors/warnings
3. **Verify field names match exactly** (case-sensitive!)
4. **Add defensive checks** for nested objects (`has()` + `isJsonObject()`)
5. **Test unit conversions** (bytes → MB, seconds → hours, etc.)

### Common Mistakes to Avoid

| Mistake | Symptom | Solution |
|---------|---------|----------|
| Flat field parsing | All metrics zero | Use nested `getAsJsonObject()` |
| Wrong unit assumptions | Huge memory values | Convert bytes to MB/GB |
| Missing disk API call | Disk always zero | Call `diskUsage` separately |
| Not checking `isJsonObject()` | ClassCastException | Add null-safety checks |

---

## Related Issues

### IP Address Parsing (Not Yet Fixed)

**Status**: Identified but not addressed in this fix  
**Issue**: Connection result shows incorrect IP address  
**Location**: Connection logic (separate from metrics parsing)  
**Priority**: Medium (doesn't affect core functionality)

---

## Code Review Checklist

When reviewing Calimero API integration code:

- [ ] Parse nested JSON objects, not flat fields
- [ ] Check `json.has()` AND `json.get().isJsonObject()`
- [ ] Convert units correctly (bytes → MB, etc.)
- [ ] Handle Optional/empty responses gracefully
- [ ] Log actual vs expected field names
- [ ] Test with real Calimero server responses
- [ ] Document response structure in JavaDoc
- [ ] Include curl test examples in comments

---

## References

- **Calimero Source**: `~/git/calimero/src/http/webservice/processors/csystemprocessor.cpp`
- **Metrics Service**: `~/git/calimero/src/http/services/system/CSystemMetricsService.cpp`
- **Response Structure**: `CSystemMetricsService::metricsToJson()` method
- **Java Model**: `src/main/java/tech/derbent/bab/dashboard/view/CSystemMetrics.java`
- **Client**: `src/main/java/tech/derbent/bab/dashboard/service/CSystemMetricsCalimeroClient.java`

---

## Impact Assessment

### Before Fix

- ❌ Dashboard metrics: All zeros
- ❌ CPU usage: 0.0%
- ❌ Memory usage: 0 MB / 0 MB
- ❌ Disk usage: 0 GB / 0 GB
- ❌ Load averages: 0.00 / 0.00 / 0.00
- ❌ Uptime: 0 seconds

### After Fix

- ✅ Dashboard metrics: Real values from Calimero
- ✅ CPU usage: Actual percentage (e.g., 15.5%)
- ✅ Memory usage: Actual MB values (e.g., 2048 MB / 16384 MB)
- ✅ Load averages: System load (e.g., 1.50 / 1.20 / 1.00)
- ✅ Uptime: Human-readable (e.g., "1d 2h 30m")
- ⚠️ Disk usage: Still zero (requires separate API call - future enhancement)

---

## Future Work

### Disk Metrics Integration

**Task**: Add disk usage fetching to dashboard  
**Approach**: Call `operation="diskUsage"` separately and merge with metrics  
**Priority**: Low (metrics working, disk is optional)

### API Response Caching

**Task**: Cache metrics for 30-60 seconds to reduce API calls  
**Benefit**: Reduce Calimero server load during dashboard refreshes  
**Priority**: Low (performance optimization)

---

**Contributors**: SSC (debugging + fix), Master Yasin (code review + documentation)  
**Status**: PRODUCTION READY ✅  
**Last Updated**: 2026-02-01
