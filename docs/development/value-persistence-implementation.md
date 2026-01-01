# Value Persistence Implementation Guide

## Overview
This document describes the implementation of the generic value persistence mechanism for UI components that need to maintain their selection state across refreshes and page reloads.

## Problem Statement
When filters and selectors in the kanban backlog column (and other similar components) are refreshed, the selected values are lost. This creates a poor user experience as users must repeatedly reselect their filters after each refresh.

## Solution: IHasSelectedValueStorage Interface

### Architecture
The solution follows the **Context Owner Pattern**:
1. Reusable components implement `IHasSelectedValueStorage` interface
2. Parent components (context owners) decide which child components need persistence
3. Parent calls `enableValuePersistence()` on child components that should persist values
4. `CValueStorageHelper` handles automatic save/restore using VaadinSession

### Key Components

#### 1. IHasSelectedValueStorage Interface
```java
public interface IHasSelectedValueStorage {
    String getStorageId();          // Unique ID for this component's storage
    void saveCurrentValue();         // Save current value to session
    void restoreCurrentValue();      // Restore value from session
    void preserveValue();            // Convenience: save then restore
}
```

#### 2. CValueStorageService
Session-based storage service that stores values in VaadinSession:
- Thread-safe using ConcurrentHashMap
- Type-safe retrieval with generics
- Automatic cleanup on session end
- Supports any serializable value type

#### 3. CValueStorageHelper
Utility for enabling automatic persistence on components:
- Adds value change listener to save on change
- Adds attach listener to restore on component attach
- Supports ComboBox, TextField, and any HasValue component
- Provides custom serialization/deserialization support

### Implementation Examples

#### Example 1: ComboBox with Custom Objects
```java
public class CComponentEntitySelection implements IHasSelectedValueStorage {
    private ComboBox<EntityTypeConfig<?>> comboBoxEntityType;
    
    public void enableValuePersistence() {
        CValueStorageHelper.enableAutoPersistence(
            comboBoxEntityType, 
            getStorageId(), 
            displayName -> {
                // Converter: find entity type by display name
                return entityTypes.stream()
                    .filter(config -> config.getDisplayName().equals(displayName))
                    .findFirst()
                    .orElse(null);
            }
        );
    }
    
    @Override
    public String getStorageId() {
        return "entitySelection_" + getId().orElse(generateId());
    }
}
```

#### Example 2: Multiple Filters in Toolbar
```java
public class CComponentKanbanBoardFilterToolbar implements IHasSelectedValueStorage {
    private ComboBox<CSprint> comboSprint;
    private ComboBox<TypeOption> comboType;
    private ComboBox<ResponsibleFilterMode> comboResponsibleMode;
    
    public void enableValuePersistence() {
        // Enable persistence for Sprint ComboBox
        CValueStorageHelper.enableAutoPersistence(
            comboSprint, 
            getStorageId() + "_sprint", 
            sprint -> {
                // Find sprint by ID
                Long sprintId = Long.parseLong(sprint);
                return comboSprint.getListDataView().getItems()
                    .filter(s -> s.getId().equals(sprintId))
                    .findFirst()
                    .orElse(null);
            }
        );
        
        // Enable persistence for Type and Responsible filters...
    }
}
```

#### Example 3: Parent Component Enabling Persistence
```java
public class CComponentBacklog extends CComponentEntitySelection {
    public CComponentBacklog(final CProject project, final boolean compactMode) {
        super(...);
        setupComponent();
        
        // Enable value persistence for entity type selection
        enableValuePersistence();
    }
}
```

### Usage Guidelines

#### When to Use
- ComboBox filters that should remember user selection
- TextField search filters that should persist
- Any component where loss of value causes user frustration
- Components that are refreshed frequently

#### When NOT to Use
- Temporary selections (e.g., dialog selections that reset on close)
- Security-sensitive data
- Very large objects (store IDs instead)
- Components that should always show default values

#### Best Practices
1. **Use unique storage IDs**: Include component type and instance identifier
2. **Store minimal data**: Store IDs or display names, not entire objects
3. **Validate restored values**: Check if option still exists before setting
4. **Document persistence behavior**: Add comments explaining what persists
5. **Test across refreshes**: Verify values persist after component recreation

### Storage ID Patterns
- Entity selection: `"entitySelection_" + componentId`
- Kanban filters: `"kanbanBoardFilter_" + componentId + "_" + filterType`
- Grid search: `"gridSearchToolbar_" + componentId + "_" + fieldName`

### Implemented Components
1. **CComponentEntitySelection** - Entity type ComboBox
2. **CComponentKanbanBoardFilterToolbar** - Sprint, Type, Responsible filters
3. **CComponentGridSearchToolbar** - ID, Name, Description, Status filters

### Testing Checklist
- [ ] Select a filter value
- [ ] Trigger a refresh (e.g., grid refresh, data reload)
- [ ] Verify selected value is still present
- [ ] Refresh the page (F5)
- [ ] Verify selected value persists across page reload
- [ ] Open in different tab/window
- [ ] Verify each session has independent values
- [ ] Clear session and verify values are cleaned up

### Future Enhancements
1. Add expiration time for stored values
2. Support storing multiple selections for multi-select components
3. Add storage quota management
4. Provide UI to view/clear stored values
5. Add metrics for storage usage

## Migration Guide for Existing Components

### Step 1: Implement Interface
```java
public class YourComponent extends BaseComponent implements IHasSelectedValueStorage {
    @Override
    public String getStorageId() {
        return "yourComponent_" + getId().orElse(generateId());
    }
    
    @Override
    public void saveCurrentValue() {
        // Usually handled by CValueStorageHelper
    }
    
    @Override
    public void restoreCurrentValue() {
        // Usually handled by CValueStorageHelper
    }
}
```

### Step 2: Add enableValuePersistence Method
```java
public void enableValuePersistence() {
    CValueStorageHelper.enableAutoPersistence(
        yourComboBox,
        getStorageId(),
        value -> convertFromString(value)
    );
}
```

### Step 3: Parent Enables Persistence
```java
public class ParentComponent {
    public ParentComponent() {
        yourComponent = new YourComponent();
        // Enable persistence after component is fully initialized
        yourComponent.enableValuePersistence();
    }
}
```

## Troubleshooting

### Value Not Persisting
1. Check if `enableValuePersistence()` was called
2. Verify storage ID is unique
3. Check if value is serializable
4. Look for exceptions in logs

### Value Not Restoring
1. Verify component attaches to UI properly
2. Check if items are loaded before restoration
3. Ensure converter can find the value in current items
4. Check if value was cleared by programmatic changes

### Storage ID Conflicts
1. Make storage IDs more specific
2. Include component hierarchy in ID
3. Use instance hash code if needed

## API Reference

### IHasSelectedValueStorage
- `String getStorageId()` - Returns unique storage identifier
- `void saveCurrentValue()` - Saves current value to session
- `void restoreCurrentValue()` - Restores value from session
- `void preserveValue()` - Convenience method (save + restore)

### CValueStorageService
- `static void storeValue(String key, Object value)` - Store value
- `static <T> Optional<T> retrieveValue(String key)` - Retrieve value
- `static void removeValue(String key)` - Remove value
- `static void clearAll()` - Clear all stored values

### CValueStorageHelper
- `static <T> void enableAutoPersistence(HasValue<?, T> component, String storageId, ValueConverter<T> converter)` - Enable automatic persistence
- `static void enableAutoPersistence(ComboBox<T> comboBox, String storageId, ValueConverter<T> converter)` - ComboBox convenience method
- `static void enableAutoPersistence(TextField textField, String storageId)` - TextField convenience method
- `static void disableAutoPersistence(Component component)` - Disable persistence
- `static boolean isAutoPersistenceEnabled(Component component)` - Check if enabled
