# BAB Calimero Client - Quick Reference Card

**For**: AI Coding Agents implementing Calimero HTTP clients  
**Date**: 2026-02-03  
**Version**: 1.0

---

## 🎯 Quick Start

### Step 1: Create Client Class

```java
package tech.derbent.bab.dashboard.service;

public class C<Domain>CalimeroClient extends CAbstractCalimeroClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(C<Domain>CalimeroClient.class);
    
    public C<Domain>CalimeroClient(final CClientProject clientProject) {
        super(clientProject);
    }
    
    // Methods here
}
```

### Step 2: Create DTO Classes

```java
package tech.derbent.bab.dashboard.dto;

public class C<Entity> extends CObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(C<Entity>.class);
    
    public static C<Entity> createFromJson(final JsonObject json) {
        final C<Entity> entity = new C<Entity>();
        entity.fromJson(json);
        return entity;
    }
    
    @Override
    protected void fromJson(final JsonObject json) {
        // Parse fields with null checks
    }
    
    @Override
    protected String toJson() {
        return "{}"; // Read-only
    }
}
```

---

## 📋 Two APIs to Implement

| API | Type | Purpose | Operations |
|-----|------|---------|------------|
| **Service Discovery** | `"webservice"` | API metadata | list |
| **Services Management** | `"systemservices"` | systemd control | list, status, start, stop, restart, reload, enable, disable |

---

## 🔧 Client Method Template

```java
public List<CEntity> fetchEntities() {
    final List<CEntity> entities = new ArrayList<>();
    
    try {
        LOGGER.debug("Fetching entities");
        
        final CCalimeroRequest request = CCalimeroRequest.builder()
                .type("messageType")      // "webservice" or "systemservices"
                .operation("operationName")
                .parameter("key", value)  // Optional parameters
                .build();
        
        final CCalimeroResponse response = sendRequest(request);
        
        if (!response.isSuccess()) {
            LOGGER.warn("Failed: {}", response.getErrorMessage());
            return entities; // Empty list = graceful degradation
        }
        
        final JsonObject data = toJsonObject(response);
        
        // Parse array
        if (data.has("entities") && data.get("entities").isJsonArray()) {
            final JsonArray array = data.getAsJsonArray("entities");
            for (final JsonElement element : array) {
                if (element.isJsonObject()) {
                    entities.add(CEntity.createFromJson(element.getAsJsonObject()));
                }
            }
        }
        
        LOGGER.info("Fetched {} entities", entities.size());
        return entities;
        
    } catch (final Exception e) {
        LOGGER.error("Failed: {}", e.getMessage(), e);
        CNotificationService.showException("Failed to fetch entities", e);
        return Collections.emptyList();
    }
}
```

---

## 🏗️ Project Structure

```
tech.derbent.bab.dashboard.service/
├── CWebServiceDiscoveryCalimeroClient.java    (API discovery)
└── CSystemdServicesCalimeroClient.java     (systemd management)

tech.derbent.bab.dashboard.dto/
├── CDTOWebServiceEndpoint.java                       (API metadata)
├── CSystemdService.java                    (service list)
├── CSystemdServiceStatus.java              (detailed status)
└── CSystemdServiceAction.java              (action result)
```

---

## 📝 Naming Conventions

| Element | Pattern | Example |
|---------|---------|---------|
| Client Class | `C<Domain>CalimeroClient` | `CSystemdServicesCalimeroClient` |
| DTO Class | `C<Entity>` | `CSystemdService` |
| Fetch Method | `fetch<Entities>()` | `fetchServices()` |
| Action Method | `<action><Entity>()` | `startService()` |
| Logger | `LOGGER` | `private static final Logger LOGGER` |
| Variables | `camelCase` | `serviceName`, `activeOnly` |
| Constants | `UPPER_SNAKE_CASE` | `DEFAULT_TIMEOUT` |

---

## 🔒 Thread Safety Rules

✅ DO:
- Make clients stateless (only `clientProject` field)
- Use `final` for fields
- Return new collections from methods
- Use `Collections.unmodifiableList()` for DTOs

❌ DON'T:
- Store mutable state in client
- Cache results in client
- Use static mutable fields

---

## ⚠️ Error Handling Pattern

```java
// 1. ALWAYS return empty collections/null on failure
// 2. NEVER throw exceptions from public methods
// 3. ALWAYS log errors
// 4. Use CNotificationService for user-facing errors

try {
    // ... operation ...
    
    if (!response.isSuccess()) {
        LOGGER.warn("Operation failed: {}", response.getErrorMessage());
        return Collections.emptyList(); // Graceful degradation
    }
    
    // ... success path ...
    
} catch (final Exception e) {
    LOGGER.error("Exception: {}", e.getMessage(), e);
    CNotificationService.showException("User-friendly message", e);
    return Collections.emptyList();
}
```

---

## 📊 Logging Levels

```java
LOGGER.debug("Method entry/exit, parameters");          // DEBUG
LOGGER.info("Success with summary: Fetched {} items");  // INFO
LOGGER.warn("Failed request (recoverable)");            // WARN
LOGGER.error("Exception caught", e);                    // ERROR
```

---

## 🧪 Testing Checklist

- [ ] Unit test for successful response
- [ ] Unit test for failed response (empty result)
- [ ] Unit test for JSON parsing
- [ ] Unit test for missing fields (defaults)
- [ ] Mock `CClientProject.sendRequest()`
- [ ] Verify graceful degradation
- [ ] Verify logging calls

---

## 📚 Complete Documentation

For full details, see:
1. **BAB_CALIMERO_CLIENT_GUIDELINES.md** - Complete implementation guide (29 KB)
2. **BAB_CALIMERO_DTOS.md** - All DTO specifications (24 KB)
3. **CALIMERO_SERVICES_API.md** - Calimero API reference (20 KB)
4. **CALIMERO_SERVICES_QUICK_GUIDE.md** - Quick guide (16 KB)

---

## 🚀 Implementation Checklist

### Phase 1: DTOs (30 min)
- [ ] Create `CDTOWebServiceEndpoint.java`
- [ ] Create `CSystemdService.java`
- [ ] Create `CSystemdServiceStatus.java`
- [ ] Create `CSystemdServiceAction.java`

### Phase 2: Clients (45 min)
- [ ] Create `CWebServiceDiscoveryCalimeroClient.java`
- [ ] Create `CSystemdServicesCalimeroClient.java`

### Phase 3: Tests (30 min)
- [ ] Unit tests for DTOs
- [ ] Unit tests for clients
- [ ] Mock responses

### Phase 4: Integration (15 min)
- [ ] Register clients in Spring/CDI context
- [ ] Verify dependency injection works

**Total: ~2 hours**

---

## 💡 Common Mistakes to Avoid

❌ **Don't**:
- Throw exceptions from public methods
- Return null from collection methods
- Use setters in DTOs
- Hardcode "webservice" vs "systemservices" (use correct type!)
- Forget null checks in `fromJson()`
- Mix GSON and Jackson

✅ **Do**:
- Return empty collections on failure
- Log all errors
- Check `response.isSuccess()` before parsing
- Use `createFromJson()` factory pattern
- Handle missing JSON fields gracefully
- Follow BAB naming conventions

---

## 📞 API Endpoints Summary

### Web Service Discovery API
- **Type**: `"webservice"` (singular)
- **Operation**: `list`
- **Returns**: Array of API endpoints with metadata

### System Services Management API
- **Type**: `"systemservices"` (plural)
- **Operations**:
  - Query: `list`, `status`
  - Control: `start`, `stop`, `restart`, `reload`
  - Config: `enable`, `disable`
- **Returns**: Services array or action result

---

## 🎨 Code Style

```java
// JavaDoc on public methods
/**
 * Fetch list of services.
 * <p>
 * Calimero API: POST /api/request with type="systemservices", operation="list"
 * 
 * @param activeOnly Only active services
 * @return List of services (empty on failure)
 */
public List<CSystemdService> fetchServices(final boolean activeOnly) {
    // Implementation
}

// Final parameters
public void method(final String param) { }

// Braces on same line
if (condition) {
    doSomething();
}

// No abbreviations in variable names
String serviceName;  // ✅ Good
String svcName;      // ❌ Bad
```

---

## 🔗 Quick Links

**Local Files**:
- Guidelines: `/home/yasin/git/derbent/bab/docs/BAB_CALIMERO_CLIENT_GUIDELINES.md`
- DTOs: `/home/yasin/git/derbent/bab/docs/BAB_CALIMERO_DTOS.md`
- API Ref: `/home/yasin/git/derbent/bab/docs/CALIMERO_SERVICES_API.md`

**Examples**:
- Client: `CSystemServiceCalimeroClient.java`
- DTO: `CDTOSystemService.java`
- Base: `CAbstractCalimeroClient.java`

---

**Status**: Ready for implementation ✅  
**Estimated Time**: 2 hours  
**Difficulty**: Easy (follow patterns)

---

🎆 **Good luck coding!** 🎆
