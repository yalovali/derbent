# Widget State Preservation Pattern

## Overview
This document describes the extensible widget state preservation pattern used in the Derbent application to maintain UI component state across grid refresh operations.

## Problem Statement
When grids are refreshed (after drag-drop operations, data updates, or page refresh), the grid recreates all row components, causing loss of:
- Expanded/collapsed sections in widgets
- Selected items in nested grids
- User preferences (sort order, filter state, etc.)
- Any other transient UI state

## Solution: Widget State Preservation Pattern

### Architecture

The pattern consists of three main components:

1. **CComponentWidgetEntity** (Base Class)
   - Provides static storage for widget state using `ConcurrentHashMap`
   - Defines abstract methods for saving and restoring state
   - Automatically calls `restoreWidgetState()` during widget initialization

2. **Subclass Implementation** (e.g., CComponentWidgetSprint)
   - Overrides `saveWidgetState()` to save specific state properties
   - Overrides `restoreWidgetState()` to restore specific state properties

3. **CComponentGridEntity** (Grid Manager)
   - Calls `saveWidgetState()` on all widgets before clearing them
   - Widget construction automatically triggers `restoreWidgetState()`

### State Storage Structure

```java
// Static map storing widget state
private static final Map<String, Map<String, Object>> widgetStateStore = new ConcurrentHashMap<>();

// Key format: "EntityClass_EntityId"
// Example: "CSprint_123"
String stateKey = "CSprint_123";

// Value: Map of property names to values
Map<String, Object> state = {
    "sprintItemsVisible": true,
    "selectedTabIndex": 2,
    // ... other properties
};
```

### Implementation Pattern

#### 1. Base Class (CComponentWidgetEntity)

```java
public class CComponentWidgetEntity<EntityClass extends CEntityDB<?>> {
    
    // Static storage accessible to all widget instances
    private static final Map<String, Map<String, Object>> widgetStateStore = new ConcurrentHashMap<>();
    
    // Helper methods for state management
    protected static void saveStateValue(Class<?> entityClass, Long entityId, String key, Object value) {
        String stateKey = entityClass.getSimpleName() + "_" + entityId;
        widgetStateStore.computeIfAbsent(stateKey, k -> new ConcurrentHashMap<>()).put(key, value);
    }
    
    protected static Object getStateValue(Class<?> entityClass, Long entityId, String key) {
        String stateKey = entityClass.getSimpleName() + "_" + entityId;
        Map<String, Object> state = widgetStateStore.get(stateKey);
        return state != null ? state.get(key) : null;
    }
    
    // Called during widget initialization (after super())
    protected void initializeWidget() {
        // ... create UI components ...
        restoreWidgetState();  // Automatically restore state
    }
    
    // Default implementation - subclasses override
    protected void restoreWidgetState() {
        // Base implementation does nothing
    }
    
    // Default implementation - subclasses override
    public void saveWidgetState() {
        // Base implementation does nothing
    }
}
```

#### 2. Subclass Implementation (CComponentWidgetSprint)

```java
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> {
    
    private boolean sprintItemsVisible = false;
    private CDiv containerSprintItems;
    
    @Override
    protected void restoreWidgetState() {
        super.restoreWidgetState();  // ALWAYS call super first
        
        // Restore sprint items visibility state
        Boolean visible = (Boolean) getStateValue(
            getEntity().getClass(), 
            getEntity().getId(), 
            "sprintItemsVisible"
        );
        
        if (visible != null && visible) {
            sprintItemsVisible = true;
            if (containerSprintItems != null) {
                containerSprintItems.setVisible(true);
                // Update button icon to match restored state
                if (buttonToggleItems != null) {
                    buttonToggleItems.setIcon(VaadinIcon.ANGLE_UP.create());
                    buttonToggleItems.setTooltipText("Hide sprint items");
                }
            }
        }
    }
    
    @Override
    public void saveWidgetState() {
        super.saveWidgetState();  // ALWAYS call super first
        
        // Save sprint items visibility state
        saveStateValue(
            getEntity().getClass(), 
            getEntity().getId(), 
            "sprintItemsVisible", 
            sprintItemsVisible
        );
    }
}
```

#### 3. Grid Manager Integration (CComponentGridEntity)

```java
public class CComponentGridEntity {
    
    public void refreshGridData() {
        // 1. Get selected item to restore later
        CEntityDB<?> selectedItem = getSelectedItem();
        
        // 2. Save widget state before clearing
        unregisterAllWidgetComponents();  // This calls saveWidgetState()
        
        // 3. Load fresh data from service
        List data = serviceBean.list(pageRequest).getContent();
        grid.setItems(data);
        
        // 4. Restore selection
        selectEntity(selectedItem);
        
        // Note: Widget state is automatically restored when new widgets are created
        // because CComponentWidgetEntity.initializeWidget() calls restoreWidgetState()
    }
    
    private void unregisterAllWidgetComponents() {
        // Save widget state before clearing
        for (Map.Entry<Object, Component> entry : entityToWidgetMap.entrySet()) {
            Component component = entry.getValue();
            if (component instanceof CComponentWidgetEntity) {
                CComponentWidgetEntity<?> widget = (CComponentWidgetEntity<?>) component;
                widget.saveWidgetState();  // Save state before destruction
            }
        }
        // ... unregister and clear widgets ...
    }
}
```

### Workflow

1. **User performs action** (e.g., drag-drop, data update)
2. **Service method executes** business logic
3. **Refresh is called** on affected grids
4. **Grid saves widget states**
   - `CComponentGridEntity.unregisterAllWidgetComponents()`
   - Calls `widget.saveWidgetState()` for each widget
5. **Grid clears old widgets**
6. **Grid loads fresh data**
7. **Grid creates new widgets**
   - `CComponentWidgetEntity` constructor calls `initializeWidget()`
   - `initializeWidget()` calls `restoreWidgetState()`
8. **Widget state is restored** from static storage
9. **Grid selection is restored**

### Adding New State Properties

To add a new state property to any widget:

```java
public class CComponentWidgetCustom extends CComponentWidgetEntity<CCustomEntity> {
    
    private int selectedTabIndex = 0;
    private String filterText = "";
    private boolean advancedMode = false;
    
    @Override
    protected void restoreWidgetState() {
        super.restoreWidgetState();
        
        // Restore tab index
        Integer tabIndex = (Integer) getStateValue(
            getEntity().getClass(), 
            getEntity().getId(), 
            "selectedTabIndex"
        );
        if (tabIndex != null) {
            selectedTabIndex = tabIndex;
            if (tabSheet != null) {
                tabSheet.setSelectedIndex(tabIndex);
            }
        }
        
        // Restore filter text
        String filter = (String) getStateValue(
            getEntity().getClass(), 
            getEntity().getId(), 
            "filterText"
        );
        if (filter != null) {
            filterText = filter;
            if (textFieldFilter != null) {
                textFieldFilter.setValue(filter);
            }
        }
        
        // Restore advanced mode flag
        Boolean advanced = (Boolean) getStateValue(
            getEntity().getClass(), 
            getEntity().getId(), 
            "advancedMode"
        );
        if (advanced != null) {
            advancedMode = advanced;
            updateUIForAdvancedMode(advanced);
        }
    }
    
    @Override
    public void saveWidgetState() {
        super.saveWidgetState();
        
        saveStateValue(getEntity().getClass(), getEntity().getId(), "selectedTabIndex", selectedTabIndex);
        saveStateValue(getEntity().getClass(), getEntity().getId(), "filterText", filterText);
        saveStateValue(getEntity().getClass(), getEntity().getId(), "advancedMode", advancedMode);
    }
}
```

### Best Practices

1. **Always call super methods first**
   ```java
   @Override
   protected void restoreWidgetState() {
       super.restoreWidgetState();  // CRITICAL
       // ... your code ...
   }
   ```

2. **Use descriptive state keys**
   ```java
   // Good
   saveStateValue(..., "sprintItemsVisible", visible);
   
   // Bad
   saveStateValue(..., "vis", visible);
   ```

3. **Check for null before applying state**
   ```java
   Boolean visible = (Boolean) getStateValue(..., "visible");
   if (visible != null && visible) {  // Check null first
       applyVisibility(visible);
   }
   ```

4. **Cast safely**
   ```java
   // Safe casting
   Object value = getStateValue(..., "count");
   if (value instanceof Integer) {
       int count = (Integer) value;
   }
   ```

5. **Document state properties**
   ```java
   /**
    * Saved state properties:
    * - sprintItemsVisible: Boolean - expanded/collapsed state
    * - selectedTabIndex: Integer - active tab index
    * - sortColumn: String - current sort column
    */
   ```

### Thread Safety

The pattern uses `ConcurrentHashMap` for thread-safe state storage:
- Multiple widgets can save/restore state concurrently
- Safe for multi-user Vaadin applications
- No additional synchronization needed

### Memory Management

State is stored in static maps and persists across widget recreations:
- **Advantage**: State survives grid refresh
- **Consideration**: State persists until explicitly cleared

To clear state when navigating away:
```java
CComponentWidgetEntity.clearAllWidgetState();
```

To clear state for specific entity:
```java
CComponentWidgetEntity.clearWidgetState(CSprint.class, sprintId);
```

### Example: Sprint Widget State Preservation

Current implementation in `CComponentWidgetSprint`:

**Properties preserved:**
- `sprintItemsVisible` - Whether the sprint items grid is expanded or collapsed

**User experience:**
1. User expands sprint items grid by clicking item count
2. User drags item from sprint to backlog
3. Grid refreshes (all widgets recreated)
4. Sprint items grid remains expanded (state restored)
5. Previously selected items remain selected

### Extending to Other Components

The pattern is not limited to grid widgets. Any component that needs state preservation can:

1. Store state in a static map with unique keys
2. Save state before destruction/recreation
3. Restore state after construction

Example for non-widget components:
```java
public class CCustomPanel extends CVerticalLayout {
    private static final Map<String, Object> panelState = new ConcurrentHashMap<>();
    
    public void saveState(String panelId) {
        panelState.put(panelId + "_expanded", isExpanded);
    }
    
    public void restoreState(String panelId) {
        Boolean expanded = (Boolean) panelState.get(panelId + "_expanded");
        if (expanded != null) {
            setExpanded(expanded);
        }
    }
}
```

## Conclusion

The widget state preservation pattern provides:
- ✅ Automatic state management across widget recreation
- ✅ Extensible to any widget type
- ✅ Thread-safe for multi-user applications
- ✅ Minimal code changes to add new state properties
- ✅ Consistent user experience during grid refresh

For questions or improvements, refer to:
- `CComponentWidgetEntity.java` - Base implementation
- `CComponentWidgetSprint.java` - Example implementation
- `CComponentGridEntity.java` - Grid refresh integration
