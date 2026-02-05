# Calimero API Actual Issues - Analysis Report

**Date**: 2026-02-01  
**Status**: ANALYSIS COMPLETE  
**Reporter**: Master Yasin

---

## Executive Summary

After thorough investigation of Calimero API responses with actual curl testing, the following issues were identified:

### Issues Identified

| Issue | Root Cause | Severity | Status |
|-------|-----------|----------|--------|
| **CPU usage shows 0%** | CPU usage fluctuates (0% when idle) | ⚠️ NORMAL | ✅ NO BUG |
| **Disk usage not displayed** | Java not calling diskUsage API | 🔴 HIGH | ❌ NEEDS FIX |
| **Process CPU/Memory all zero** | Calimero not collecting process metrics | 🔴 HIGH | ❌ CALIMERO BUG |
| **Process user/state empty** | Calimero only reads /proc/[pid]/comm | 🔴 HIGH | ❌ CALIMERO BUG |

---

## 1. CPU Usage Shows 0% - NOT A BUG

### Observation
Dashboard sometimes shows CPU usage as 0.0%.

### Investigation
```bash
# First curl request
curl ... '{"type":"system","data":{"operation":"metrics"}}'
# Response: "usagePercent": 0.0

# Second curl request (20 seconds later)
curl ... '{"type":"system","data":{"operation":"metrics"}}'
# Response: "usagePercent": 12.945700790034698
```

### Root Cause
CPU usage **fluctuates** based on system activity. A value of 0.0% simply means the system was idle at that exact moment.

### Conclusion
✅ **NO BUG** - This is expected behavior. CPU usage varies from 0-100% depending on workload.

---

## 2. Disk Usage Not Displayed - JAVA BUG

### Observation
Disk usage fields (diskUsedGB, diskTotalGB, diskUsagePercent) always show zero in dashboard.

### Root Cause
**Java code never calls the diskUsage API**. The `operation="metrics"` response does NOT include disk data.

### Calimero API Behavior (CORRECT)

**Metrics Response** (does NOT include disk):
```json
{
  "data": {
    "cpu": {...},
    "memory": {...},
    "swap": {...},
    "system": {...}
    // NO disk data here!
  }
}
```

**Disk Usage Response** (requires separate call):
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","data":{"operation":"diskUsage"}}'

# Returns:
{
  "data": {
    "disks": [
      {
        "mountPoint": "/",
        "totalBytes": 1967536746496,
        "usedBytes": 352808046592,
        "availableBytes": 1514707812352,
        "usagePercent": 17.931459080513658,
        "filesystem": "",
        "device": ""
      }
    ],
    "count": 9
  }
}
```

### Fix Required

**Location**: `src/main/java/tech/derbent/bab/dashboard/service/CSystemMetricsCalimeroClient.java`

Add method to fetch disk usage:

```java
/**
 * Fetch disk usage from Calimero server.
 * Separate API call required - NOT included in metrics response.
 */
public Optional<List<CDiskInfo>> fetchDiskUsage() {
    try {
        LOGGER.debug("Fetching disk usage from Calimero server");
        
        final CCalimeroRequest request = CCalimeroRequest.builder()
            .type("system")
            .operation("diskUsage")
            .build();
        
        final CCalimeroResponse response = clientProject.sendRequest(request);
        
        if (!response.isSuccess()) {
            LOGGER.warn("Failed to fetch disk usage: {}", response.getErrorMessage());
            return Optional.empty();
        }
        
        final JsonObject data = toJsonObject(response);
        final List<CDiskInfo> disks = parseDiskArray(data);
        
        LOGGER.info("Fetched {} disks", disks.size());
        return Optional.of(disks);
        
    } catch (final Exception e) {
        LOGGER.error("Failed to fetch disk usage: {}", e.getMessage(), e);
        return Optional.empty();
    }
}

private List<CDiskInfo> parseDiskArray(final JsonObject json) {
    final List<CDiskInfo> disks = new ArrayList<>();
    
    if (json.has("disks") && json.get("disks").isJsonArray()) {
        final JsonArray diskArray = json.getAsJsonArray("disks");
        for (final JsonElement element : diskArray) {
            if (element.isJsonObject()) {
                final CDiskInfo disk = CDiskInfo.createFromJson(element.getAsJsonObject());
                disks.add(disk);
            }
        }
    }
    
    return disks;
}
```

**Priority**: HIGH - Users cannot see disk space usage without this fix.

---

## 3. Process Metrics All Zero - CALIMERO BUG

### Observation
Process list shows names correctly, but all numeric fields are zero:
- cpuPercent: 0.0
- memPercent: 0.0
- memRssBytes: 0
- memVirtBytes: 0
- state: ""
- user: ""

### Calimero Response (Actual)
```json
{
  "processes": [
    {
      "pid": 1,
      "name": "systemd",
      "cpuPercent": 0.0,      // ❌ Always 0
      "memPercent": 0.0,      // ❌ Always 0
      "memRssBytes": 0,       // ❌ Always 0
      "memVirtBytes": 0,      // ❌ Always 0
      "state": "",            // ❌ Always empty
      "user": ""              // ❌ Always empty
    }
  ]
}
```

### Root Cause Analysis

**Calimero C++ Implementation** (`src/http/services/system/CSystemMetricsService.cpp`):

```cpp
bool CSystemMetricsService::getProcessList(std::vector<SProcessInfo>& processes, std::string& error)
{
    processes.clear();
    DIR* dir = opendir("/proc");
    
    // ... iterate /proc ...
    
    SProcessInfo proc;
    proc.pid = std::atoi(entry->d_name);
    
    // ❌ ONLY reads process name from /proc/[pid]/comm
    std::string commPath = std::string("/proc/") + entry->d_name + "/comm";
    std::ifstream commFile(commPath);
    if (commFile.is_open())
    {
        std::getline(commFile, proc.name);
        processes.push_back(proc);
    }
    
    // ❌ MISSING: CPU%, memory%, user, state
    // Fields remain at default (0, empty string)
}
```

**What's Missing**:
1. ❌ CPU usage per process (requires `/proc/[pid]/stat` parsing + time delta calculation)
2. ❌ Memory usage (requires `/proc/[pid]/status` or `/proc/[pid]/statm`)
3. ❌ Process state (requires `/proc/[pid]/status` - State: field)
4. ❌ Process user (requires `/proc/[pid]/status` - Uid: field + lookup in /etc/passwd)

### Fix Required

**Location**: Calimero C++ code - `~/git/calimero/src/http/services/system/CSystemMetricsService.cpp`

**Required Implementation**:

1. **Read `/proc/[pid]/stat`** for:
   - CPU time (utime + stime fields)
   - Process state (field 3: R=running, S=sleeping, Z=zombie, etc.)

2. **Read `/proc/[pid]/status`** for:
   - VmRSS (Resident Set Size - physical memory)
   - VmSize (Virtual memory size)
   - Uid (User ID)

3. **Calculate CPU percentage**:
   - Requires two samples with time delta
   - Formula: `((utime2 + stime2) - (utime1 + stime1)) / (totalTime2 - totalTime1) * 100`

4. **Lookup username** from UID:
   - Parse `/etc/passwd` or use `getpwuid()` function

**Example Implementation**:

```cpp
bool CSystemMetricsService::readProcessStats(int pid, SProcessInfo& proc, std::string& error)
{
    // Read /proc/[pid]/stat
    std::string statPath = "/proc/" + std::to_string(pid) + "/stat";
    std::ifstream statFile(statPath);
    if (!statFile.is_open()) {
        error = "Failed to open " + statPath;
        return false;
    }
    
    std::string line;
    std::getline(statFile, line);
    
    // Parse fields (man proc(5) for format)
    // Field 1: pid
    // Field 2: (comm)  - process name in parentheses
    // Field 3: state   - R/S/D/Z/T/etc.
    // Field 14: utime  - CPU time in user mode
    // Field 15: stime  - CPU time in kernel mode
    // Field 23: vsize  - Virtual memory size in bytes
    // Field 24: rss    - Resident Set Size in pages
    
    // ... parsing logic ...
    
    proc.state = stateChar;  // e.g., "R", "S", "D"
    proc.cpuPercent = calculateCpuPercent(utime, stime);
    proc.memRssBytes = rss * getpagesize();
    proc.memVirtBytes = vsize;
    
    // Read /proc/[pid]/status for Uid
    std::string statusPath = "/proc/" + std::to_string(pid) + "/status";
    // ... read Uid field ...
    
    // Lookup username
    struct passwd* pw = getpwuid(uid);
    if (pw) {
        proc.user = pw->pw_name;
    }
    
    return true;
}
```

**Priority**: HIGH - Process monitoring is incomplete without these metrics.

**Complexity**: MEDIUM - Requires careful /proc filesystem parsing and CPU time delta tracking.

---

## 4. Authentication Token Issue - DOCUMENTATION BUG

### Observation
Earlier documentation stated auth token was `test_token_123` (underscores), but Calimero config shows `test-token-123` (hyphens).

### Correct Configuration

**File**: `~/git/calimero/config/http_server.json`

```json
{
    "authToken": "test-token-123",    // ✅ HYPHENS, not underscores!
    "host": "0.0.0.0",
    "httpPort": 8077,
    "idleTimeoutSec": 60,
    "readTimeoutSec": 30,
    "runState": "normal",
    "writeTimeoutSec": 30
}
```

### Correct Usage

```bash
curl -X POST http://localhost:8077/api/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token-123" \   # ✅ Hyphens!
  -d '{"type":"system","data":{"operation":"metrics"}}'
```

### Documentation Fix

Update all documentation to use `test-token-123` instead of `test_token_123`.

---

## 5. Request Format - NESTED DATA OBJECT

### Observation
Calimero API uses nested `data` object for operation parameters.

### Correct Format

```json
{
  "type": "system",
  "data": {
    "operation": "metrics"    // ✅ Nested inside "data"
  }
}
```

### Incorrect Format (Doesn't Work)

```json
{
  "type": "system",
  "operation": "metrics"    // ❌ WRONG - Returns "Missing 'operation' field"
}
```

### Java Code Status

The Java `CCalimeroRequest` builder already handles this correctly:

```java
final CCalimeroRequest request = CCalimeroRequest.builder()
    .type("system")
    .operation("metrics")
    .build();

// Generates: {"type":"system","data":{"operation":"metrics"}}
```

✅ **NO FIX NEEDED** - Java code is correct.

---

## Summary of Required Fixes

### Java Code Fixes (Derbent Project)

1. **HIGH PRIORITY**: Add `fetchDiskUsage()` method to `CSystemMetricsCalimeroClient`
2. **HIGH PRIORITY**: Update dashboard to call disk usage API
3. **MEDIUM PRIORITY**: Update documentation auth token from underscores to hyphens

### Calimero C++ Fixes (Calimero Project)

1. **HIGH PRIORITY**: Implement process CPU usage calculation
2. **HIGH PRIORITY**: Implement process memory usage reading
3. **HIGH PRIORITY**: Implement process state reading
4. **HIGH PRIORITY**: Implement process user lookup
5. **MEDIUM PRIORITY**: Add /proc/[pid]/stat parsing
6. **MEDIUM PRIORITY**: Add /proc/[pid]/status parsing

### Documentation Updates

1. ✅ DONE: Created `CALIMERO_API_RESPONSE_PATTERNS.md`
2. ❌ TODO: Update token format examples (underscores → hyphens)
3. ❌ TODO: Add disk usage API call documentation
4. ❌ TODO: Document Calimero process metrics limitations

---

## Testing Verification

### Working Correctly ✅

```bash
# Metrics API (cpu, memory, swap, system)
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","data":{"operation":"metrics"}}'

# Returns:
{
  "cpu": {
    "usagePercent": 12.9,           # ✅ Working (fluctuates)
    "loadAvg1Min": 1.26,            # ✅ Working
    "loadAvg5Min": 1.76,            # ✅ Working
    "loadAvg15Min": 1.89,           # ✅ Working
    "coreCount": 12                 # ✅ Working
  },
  "memory": {
    "totalBytes": 33511272448,      # ✅ Working
    "usedBytes": 32071581696,       # ✅ Working
    "usagePercent": 95.7            # ✅ Working
  },
  "system": {
    "uptimeSeconds": 48577,         # ✅ Working
    "processCount": 360,            # ✅ Working
    "hostname": "ev"                # ✅ Working
  }
}
```

### Needs Fixing ❌

```bash
# Disk Usage API (not called by Java)
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","data":{"operation":"diskUsage"}}'

# Returns: ✅ Calimero provides data (Java just doesn't fetch it)
{
  "disks": [
    {
      "mountPoint": "/",
      "totalBytes": 1967536746496,      # ✅ Calimero has data
      "usedBytes": 352808046592,        # ✅ Calimero has data
      "usagePercent": 17.93             # ✅ Calimero has data
    }
  ]
}

# Process API (Calimero bug - incomplete data)
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","data":{"operation":"processes"}}'

# Returns: ❌ Calimero missing most fields
{
  "processes": [
    {
      "pid": 1,                   # ✅ Working
      "name": "systemd",          # ✅ Working
      "cpuPercent": 0.0,          # ❌ Always 0 (Calimero bug)
      "memPercent": 0.0,          # ❌ Always 0 (Calimero bug)
      "memRssBytes": 0,           # ❌ Always 0 (Calimero bug)
      "memVirtBytes": 0,          # ❌ Always 0 (Calimero bug)
      "state": "",                # ❌ Always empty (Calimero bug)
      "user": ""                  # ❌ Always empty (Calimero bug)
    }
  ]
}
```

---

## Recommended Action Plan

### Phase 1: Quick Wins (Java - 1-2 hours)

1. Add `fetchDiskUsage()` method to client
2. Update dashboard to display disk metrics
3. Fix documentation token format

### Phase 2: Calimero Enhancement (C++ - 4-8 hours)

1. Implement `/proc/[pid]/stat` parsing
2. Implement `/proc/[pid]/status` parsing
3. Add CPU usage calculation with time deltas
4. Add user lookup functionality
5. Test with various process states

### Phase 3: Integration Testing (1-2 hours)

1. Test Java + updated Calimero together
2. Verify all metrics display correctly
3. Load test with many processes
4. Update documentation

---

**Contributors**: Master Yasin (investigation), SSC (earlier parsing fix)  
**Status**: Analysis Complete - Ready for Implementation  
**Last Updated**: 2026-02-01
