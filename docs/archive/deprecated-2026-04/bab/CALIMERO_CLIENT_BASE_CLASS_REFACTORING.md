# Calimero Client Base Class Refactoring

**Date**: 2026-02-03  
**Status**: 🔄 IN PROGRESS (Infrastructure Complete, Component Migration Pending)  

## Summary

Created unified base class infrastructure for all Calimero HTTP API clients and enhanced CComponentBabBase to manage client lifecycle centrally.

## Completed Work

### ✅ Phase 1: Base Calimero Client (COMPLETE)
- Created `CAbstractCalimeroClient` base class
- Extracted common patterns:
  - `clientProject` field
  - `sendRequest()` method with logging
  - `toJsonObject()` utility method
  - Consistent error handling

### ✅ Phase 2: Client Classes Refactored (COMPLETE)
All 8 Calimero client classes now extend `CAbstractCalimeroClient`:
- ✅ `CNetworkInterfaceCalimeroClient`
- ✅ `CDiskUsageCalimeroClient`
- ✅ `CSystemMetricsCalimeroClient`
- ✅ `CCpuInfoCalimeroClient`
- ✅ `CDnsConfigurationCalimeroClient`
- ✅ `CSystemServiceCalimeroClient`
- ✅ `CNetworkRoutingCalimeroClient`
- ✅ `CSystemProcessCalimeroClient`

**Benefits**:
- Removed duplicate `GSON` fields
- Removed duplicate `clientProject` fields  
- Removed duplicate `toJsonObject()` methods
- **~80 lines of duplicate code eliminated**

### ✅ Phase 3: CComponentBabBase Enhanced (COMPLETE)
Added client management infrastructure:
- `calimeroClient` field (non-final, generic type)
- `createCalimeroClient()` abstract method
- `getCalimeroClient()` helper with lazy initialization
- `resolveClientProject()` helper
- `getSessionService()` abstract method

### 🔄 Phase 4: Component Migration (PARTIAL)
**Status**: 1 of 9 components fully migrated

#### ✅ Fully Migrated
- `CComponentInterfaceList` - Complete refactoring, all methods updated

#### ⏳ Pending Migration (8 components)
- `CComponentSystemMetrics`
- `CComponentCpuUsage`
- `CComponentDiskUsage`
- `CComponentDnsConfiguration`
- `CComponentSystemServices`
- `CComponentNetworkRouting`
- `CComponentRoutingTable`
- `CComponentSystemProcessList`

## New Pattern Usage

### Component Implementation
```java
public class CComponentMyData extends CComponentBabBase {
    
    private final ISessionService sessionService;
    
    public CComponentMyData(final ISessionService sessionService) {
        this.sessionService = sessionService;
        initializeComponents();
    }
    
    @Override
    protected CAbstractCalimeroClient createCalimeroClient(final CClientProject clientProject) {
        return new CMyDataCalimeroClient(clientProject);
    }
    
    @Override
    protected ISessionService getSessionService() {
        return sessionService;
    }
    
    private void loadData() {
        final Optional<CAbstractCalimeroClient> clientOpt = getCalimeroClient();
        if (clientOpt.isEmpty()) {
            showCalimeroUnavailableWarning("Service not available");
            return;
        }
        
        final CMyDataCalimeroClient client = (CMyDataCalimeroClient) clientOpt.get();
        final List<CMyData> data = client.fetchData();
        grid.setItems(data);
    }
}
```

## Remaining Work

1. **Clean up orphaned code** in 8 pending components
2. **Update load methods** to use `getCalimeroClient()` instead of manual client creation
3. **Remove private client fields** from component classes
4. **Test compilation** and fix any remaining issues
5. **Verify runtime** - Test all 9 components with Calimero running and stopped

## Benefits When Complete

1. **-200+ lines of duplicate code** removed across components
2. **Centralized client management** in base class
3. **Consistent error handling** via base class methods
4. **Easier testing** - single mocking point for all clients
5. **Type safety** - Generic base client with typed subclasses
6. **Non-final fields** - Better for testing and dependency injection

## Files Modified

**New files (1)**:
- `src/main/java/tech/derbent/bab/dashboard/service/CAbstractCalimeroClient.java`

**Client classes (8)**:
- All extend `CAbstractCalimeroClient`
- Removed duplicate fields and methods

**Base component (1)**:
- `CComponentBabBase.java` - Added client management infrastructure

**Component classes (9)**:
- 1 fully migrated (CComponentInterfaceList)
- 8 pending completion

## Next Steps

1. Complete component migration (8 remaining)
2. Test compilation
3. Runtime verification
4. Update documentation

## Testing Checklist

- [ ] Compile with `-Pagents` profile
- [ ] Start Calimero service
- [ ] Test all 9 components load data correctly
- [ ] Stop Calimero service
- [ ] Verify graceful error handling (warning banners, no exception dialogs)
- [ ] Test Edit/Refresh buttons work correctly

