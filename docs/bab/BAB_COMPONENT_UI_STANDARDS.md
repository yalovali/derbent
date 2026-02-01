# BAB Component UI Standards (MANDATORY)

**Date**: 2026-02-01  
**Status**: ENFORCED  
**Scope**: ALL BAB components and UI elements

## Header Component Sizing (MANDATORY - 2026-02-01)

**RULE**: ALL header components (CH1, CH2, CH3, CH4, CH6) MUST use `setHeight(null)` instead of `setHeightFull()`.

### Why This Rule Exists

Headers with `setHeightFull()` look ugly and take up unnecessary vertical space. They should expand to content width but have natural height based on text size.

### Correct Pattern

```java
private void initializeComponent() {
    getStyle().set("display", "flex").set("justify-content", "space-evenly");
    setWidthFull();              // ✅ CORRECT - Full width
    setHeight(null);             // ✅ CORRECT - Natural height
    getStyle().set("margin", "0");
    getStyle().set("padding", "0");
}
```

### Incorrect Pattern (OLD - DO NOT USE)

```java
private void initializeComponent() {
    setWidthFull();
    setHeightFull();  // ❌ WRONG - Makes headers look ugly!
}
```

### Affected Components

| Component | File | Fixed Date |
|-----------|------|------------|
| CH1 | `src/main/java/tech/derbent/api/ui/component/basic/CH1.java` | 2026-02-01 |
| CH2 | `src/main/java/tech/derbent/api/ui/component/basic/CH2.java` | 2026-02-01 |
| CH3 | `src/main/java/tech/derbent/api/ui/component/basic/CH3.java` | 2026-02-01 |
| CH4 | `src/main/java/tech/derbent/api/ui/component/basic/CH4.java` | 2026-02-01 |
| CH6 | `src/main/java/tech/derbent/api/ui/component/basic/CH6.java` | 2026-02-01 |

**Code Review**: Reject ANY pull request that uses `setHeightFull()` on header components!

## JSON Parser Fail-Fast Pattern (MANDATORY)

**RULE**: ALL JSON parsers MUST validate data and fail fast with clear error messages.

### Pattern

```java
@Override
protected void fromJson(final JsonObject json) {
    try {
        // 1. Null check at start
        if (json == null) {
            LOGGER.warn("Null JSON object passed to fromJson()");
            return;
        }
        
        // 2. Validate required fields exist
        if (!json.has("name") || json.get("name").isJsonNull()) {
            LOGGER.error("Required field 'name' missing in JSON: {}", json);
            throw new IllegalArgumentException("Required field 'name' missing");
        }
        
        // 3. Safe parsing with null checks
        if (json.has("name") && !json.get("name").isJsonNull()) {
            name = json.get("name").getAsString();
        }
        
        // 4. Type validation where needed
        if (json.has("port") && !json.get("port").isJsonNull()) {
            try {
                port = json.get("port").getAsInt();
                if (port < 0 || port > 65535) {
                    throw new IllegalArgumentException("Invalid port number: " + port);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid port format in JSON: {}", json.get("port"));
                throw new IllegalArgumentException("Port must be a number", e);
            }
        }
        
    } catch (final Exception e) {
        LOGGER.error("Error parsing from JSON: {}", e.getMessage(), e);
        // Re-throw for fail-fast behavior
        throw new IllegalStateException("Failed to parse JSON", e);
    }
}
```

### Key Principles

1. **Validate early**: Check null and required fields first
2. **Fail fast**: Throw exceptions immediately on invalid data
3. **Clear messages**: Error messages must identify the problem field
4. **Type safety**: Validate number ranges, string formats, etc.
5. **Log everything**: LOGGER.error for all parse failures

## Component Removal Checklist (MANDATORY)

When removing unused components:

1. **Remove from initializer**:
   ```java
   // Remove this line:
   scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "placeHolder_createComponentXxx"));
   ```

2. **Remove from entity**:
   - Remove `@AMetaData` annotation
   - Remove `@Transient` field
   - Remove getter method
   - Remove setter method

3. **Remove from page service**:
   ```java
   // Remove entire method:
   public Component createComponentXxx() { ... }
   ```

4. **Verify**:
   ```bash
   # Search for any remaining references
   grep -r "ComponentXxx" src/main/java/tech/derbent/bab
   
   # Should return only the component file itself (if keeping for future)
   ```

## BAB Component Dashboard Organization (CURRENT)

### Active Components (2026-02-01)

**Network Monitoring Section:**
1. CComponentInterfaceList - Network interfaces with IP/MAC/Status
2. CComponentRoutingTable - Complete routing table
3. CComponentDnsConfiguration - DNS resolver settings

**System Monitoring Section:**
4. CComponentSystemMetrics - CPU/Memory/Disk/Uptime metrics
5. CComponentDiskUsage - Detailed disk space information

**System Management Section:**
6. CComponentSystemServices - Service status and management
7. CComponentSystemProcessList - Running processes

### Removed Components (2026-02-01)

**NO LONGER IN DASHBOARD:**
- ❌ CComponentCpuUsage - DUPLICATE of CPU in SystemMetrics
- ❌ CComponentNetworkRouting - DUPLICATE of RoutingTable

**Action**: Files still exist but are not referenced. Can be deleted after confirmation.

## Rich UI Component Standards (TO BE IMPLEMENTED)

### Progress Bars

```java
// CPU usage with color coding
ProgressBar cpuBar = new ProgressBar();
cpuBar.setValue(cpuUsage / 100.0);
if (cpuUsage > 80) {
    cpuBar.addClassName("error");  // Red
} else if (cpuUsage > 60) {
    cpuBar.addClassName("warning");  // Yellow
} else {
    cpuBar.addClassName("success");  // Green
}
```

### Gauges (Future Enhancement)

```java
// TODO: Implement circular gauges for metrics
// Library: Vaadin Charts or custom SVG component
```

### Color Coding Standards

| Metric Range | Color | CSS Class | Usage |
|-------------|-------|-----------|--------|
| 0-60% | Green | `success` | Normal operation |
| 61-80% | Yellow | `warning` | Attention needed |
| 81-100% | Red | `error` | Critical |

### Icon Usage

```java
// Status indicators
if (status.equals("up")) {
    icon = VaadinIcon.CHECK_CIRCLE.create();
    icon.setColor("var(--lumo-success-color)");
} else {
    icon = VaadinIcon.CLOSE_CIRCLE.create();
    icon.setColor("var(--lumo-error-color)");
}
```

## Calimero Source Sync Requirements

### Parser-to-Source Compatibility

**RULE**: All Java parsers MUST match Calimero C++ service return structures.

### Verification Process

1. **Check Calimero service** (~/git/calimero/src/http/services/):
   ```cpp
   // CNetworkService.cpp
   json response;
   response["name"] = interface.name;
   response["type"] = interface.type;
   response["status"] = interface.status;
   // ... etc
   ```

2. **Match Java parser**:
   ```java
   // CNetworkInterface.java
   if (json.has("name")) name = json.get("name").getAsString();
   if (json.has("type")) type = json.get("type").getAsString();
   if (json.has("status")) status = json.get("status").getAsString();
   ```

3. **Document in JavaDoc**:
   ```java
   /**
    * JSON structure from Calimero (CNetworkService.cpp):
    * {
    *   "name": "eth0",
    *   "type": "ethernet",
    *   "status": "up"
    * }
    */
   ```

### Sync Verification Commands

```bash
# 1. Check Calimero service output format
cd ~/git/calimero
grep -A 20 "json response" src/http/services/network/CNetworkService.cpp

# 2. Check Java parser fields
cd ~/git/derbent
grep "json.has" src/main/java/tech/derbent/bab/dashboard/view/CNetworkInterface.java

# 3. Compare field lists
# Calimero fields: name, type, status, macAddress, mtu, dhcp4, addresses
# Java fields: same list (must match!)
```

## Testing Requirements

### Component Testing

```bash
# Test specific dashboard component
MAVEN_OPTS="-ea" mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="BAB Dashboard" \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=500
```

### Visual Verification Checklist

- [ ] Headers have proper height (not full height)
- [ ] No duplicate components visible
- [ ] Data displays correctly
- [ ] Colors are used appropriately
- [ ] Progress bars show correct percentages
- [ ] No JSON parsing errors in logs

## Code Review Enforcement

**MANDATORY checks for ALL BAB component PRs:**

1. ✅ Headers use `setHeight(null)`, NOT `setHeightFull()`
2. ✅ JSON parsers have fail-fast validation
3. ✅ No duplicate components in dashboard
4. ✅ Component removal followed complete checklist
5. ✅ Calimero source sync verified
6. ✅ Tests pass with no errors

**AUTO-REJECT if:**
- ❌ ANY header uses `setHeightFull()`
- ❌ JSON parser silently ignores errors
- ❌ Duplicate components not removed
- ❌ Tests fail or show errors

## Status: ENFORCED ✅

All standards are now mandatory and enforced in code review. Non-compliance will result in PR rejection.
