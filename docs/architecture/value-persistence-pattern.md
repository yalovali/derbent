# Value Persistence Pattern - Coding Standard

## Overview

Components that implement `IHasSelectedValueStorage` and use `CValueStorageHelper` for automatic value persistence MUST follow strict ID management rules to ensure persistence works correctly across component recreations.

## The Problem

Previously, components generated IDs using `System.identityHashCode(this)` or `System.currentTimeMillis()`, which created different IDs each time the component was recreated. This broke value persistence because stored values couldn't be retrieved.

## The Solution

### Rule 1: Explicit ID Requirement

**MANDATORY**: Any component implementing `IHasSelectedValueStorage` MUST set an explicit, stable ID.

### Rule 2: ID Setting Patterns

There are two acceptable patterns:

#### Pattern A: Singleton/Unique Components
For components that appear only once per context (e.g., filter toolbars, page-level search bars):

```java
public class CComponentKanbanBoardFilterToolbar extends CComponentFilterToolbar 
        implements IHasSelectedValueStorage {
    
    public CComponentKanbanBoardFilterToolbar() {
        super(new ToolbarConfig().hideAll());
        // REQUIRED: Set explicit, stable ID for value persistence
        setId("kanbanBoardFilterToolbar");
        
        // ... rest of initialization
    }
    
    @Override
    public String getStorageId() {
        final String componentId = getId().orElse(null);
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalStateException(
                "Component ID must be set in constructor for value persistence"
            );
        }
        return componentId;
    }
}
```

#### Pattern B: Reusable Components with Context
For components used in multiple places (e.g., entity selection dialogs):

```java
// In the DIALOG or PARENT that creates the component:
public class CDialogEntitySelection<EntityClass extends CEntityDB<?>> extends CDialog {
    
    @Override
    protected void setupContent() throws Exception {
        // Create the selection component
        componentEntitySelection = new CComponentEntitySelection<>(
            entityTypes, itemsProvider, this::onSelectionChanged, multiSelect
        );
        
        // REQUIRED: Set explicit ID based on context
        String contextId = "entitySelection_" + getDialogContext();
        componentEntitySelection.setId(contextId);
        
        // Now safe to enable persistence
        componentEntitySelection.enableValuePersistence();
        
        mainLayout.add(componentEntitySelection);
    }
    
    private String getDialogContext() {
        // Return context-specific identifier
        // Examples: "sprintItems", "activityAssignment", "resourceSelection"
        return this.dialogContext;
    }
}
```

### Rule 3: getStorageId() Implementation

All `getStorageId()` implementations MUST validate that an ID is set:

```java
@Override
public String getStorageId() {
    final String componentId = getId().orElse(null);
    if (componentId == null || componentId.isBlank()) {
        throw new IllegalStateException(
            "Component ID must be set explicitly for value persistence. " +
            "Call setId(\"uniqueId\") in the constructor or before enabling persistence."
        );
    }
    return componentId;
}
```

### Rule 4: NO Unstable ID Generation

**FORBIDDEN** patterns:
```java
// ❌ NEVER use System.identityHashCode()
private String generateId() {
    return "component_" + System.identityHashCode(this);
}

// ❌ NEVER use System.currentTimeMillis()
private String generateId() {
    return "component_" + System.currentTimeMillis();
}

// ❌ NEVER use UUID.randomUUID()
private String generateId() {
    return "component_" + UUID.randomUUID();
}
```

### Rule 5: CAuxillaries.setId() Usage

`CAuxillaries.setId(component)` is acceptable ONLY for:
- Components that do NOT implement `IHasSelectedValueStorage`
- Components where the generated ID is stable (based on component text content)

**For components with value persistence**: Always use explicit `setId()` calls.

## Migration Checklist

When adding value persistence to a component:

- [ ] Component implements `IHasSelectedValueStorage`
- [ ] Constructor calls `setId("explicitStableId")` 
- [ ] `getStorageId()` validates ID is set
- [ ] `getStorageId()` throws exception if ID is null/blank
- [ ] Component instantiation sets context-specific ID (if reusable)
- [ ] Remove any `generateId()` methods using unstable patterns
- [ ] Update tests to verify persistence works across recreations

## Testing Persistence

To verify persistence works:

1. Set a value in the component
2. Trigger component recreation (page refresh, dialog reopen)
3. Verify value is restored correctly
4. Check logs for any "ID must be set" exceptions

## Components Requiring Explicit IDs

Current components implementing `IHasSelectedValueStorage`:

1. **CComponentKanbanBoardFilterToolbar** ✅ - Has explicit ID "kanbanBoardFilterToolbar"
2. **CComponentEntitySelection** ⚠️ - Needs context-based ID from parent
3. **CComponentGridSearchToolbar** ⚠️ - Needs explicit ID pattern

## Benefits

1. **Reliable Persistence**: Values persist correctly across component recreations
2. **Fail-Fast**: Immediate errors if developer forgets to set ID
3. **Clear Contract**: Explicit ID requirement is documented and enforced
4. **No Hidden Magic**: No reliance on unstable hash codes or timestamps
5. **Maintainable**: Easy to understand and debug persistence issues
6. **Testable**: Can verify persistence behavior deterministically

## References

- `IHasSelectedValueStorage` interface
- `CValueStorageHelper` utility class
- `CAuxillaries.generateId()` and `setId()` methods
- Commit `7091af4` - Initial stable ID enforcement
