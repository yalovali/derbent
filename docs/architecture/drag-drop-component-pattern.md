# Drag-Drop Component Pattern

## Overview
This document defines the unified pattern for component registration and drag-drop event propagation in the Derbent application.

## Core Interfaces

### IPageServiceAutoRegistrable
Components that can automatically register themselves with CPageService.

```java
public interface IPageServiceAutoRegistrable {
    /**
     * Registers this component with the page service.
     * Should only call registerComponent(), NOT bindMethods().
     */
    void registerWithPageService(CPageService<?> pageService);
    
    /**
     * Returns the component name for method binding.
     * Used to match on_{componentName}_{action} handlers.
     */
    String getComponentName();
}
```

### IHasDragControl
Components that support enabling/disabling drag-and-drop functionality.

```java
public interface IHasDragControl {
    void setDragEnabled(boolean enabled);
    boolean isDragEnabled();
    void setDropEnabled(boolean enabled);
    boolean isDropEnabled();
}
```

### IHasDragStart<T>
Components that fire drag start events.

```java
public interface IHasDragStart<T> {
    Registration addDragStartListener(ComponentEventListener<GridDragStartEvent> listener);
}
```

### IHasDragEnd<T>
Components that fire drag end events.

```java
public interface IHasDragEnd<T> {
    Registration addDragEndListener(ComponentEventListener<GridDragEndEvent> listener);
}
```

## Base Classes

### CComponentListEntityBase<MasterEntity, ChildEntity>
Base class for list components with master-detail relationships.

**Implements:**
- IPageServiceAutoRegistrable
- IHasDragControl
- IHasDragStart<ChildEntity>
- IHasDragEnd<ChildEntity>

**Key Methods:**
- `registerWithPageService()` - Auto-registration implementation
- `getComponentName()` - Default implementation (should be overridden)
- `setDragEnabled()` / `setDropEnabled()` - Control drag-drop state

**Pattern:**
```java
public class CComponentListSprintItems extends CComponentListEntityBase<CSprint, CSprintItem> {
    @Override
    public String getComponentName() {
        return "sprintItems";  // Override for specific name
    }
}
```

### CComponentGridEntity
Grid component for master-detail views.

**Implements:**
- IPageServiceAutoRegistrable
- IHasDragControl
- IHasDragStart<CEntityDB<?>>
- IHasDragEnd<CEntityDB<?>>

**Component Name:** `masterGrid`

## Component Hierarchy

```
CPageService
    ├── masterGrid (CComponentGridEntity)
    │   └── widgets (CComponentWidgetSprint)
    │       └── sprintItems (CComponentListSprintItems)
    ├── sprintItems (standalone)
    └── backlogItems (CComponentBacklog)
```

## Event Propagation Pattern

### Drag Start Flow
```
1. User drags item in CComponentListSprintItems
2. CComponentListSprintItems fires GridDragStartEvent
3. Event propagates to CComponentWidgetSprint listeners
4. CComponentWidgetSprint propagates to CComponentGridEntity
5. CComponentGridEntity fires event to page service listeners
6. CPageService invokes on_masterGrid_dragStart handler
```

### Drop Flow
```
1. User drops item on target component
2. Target component fires GridDropEvent
3. CPageService invokes on_{target}_drop handler
4. Handler implements business logic (reorder, move, etc.)
5. Handler refreshes affected components
```

## Page Service Handler Pattern

### Naming Convention
```java
// Format: on_{componentName}_{action}
public void on_sprintItems_dragStart(Component component, Object value);
public void on_sprintItems_dragEnd(Component component, Object value);
public void on_sprintItems_drop(Component component, Object value);

public void on_backlogItems_dragStart(Component component, Object value);
public void on_backlogItems_drop(Component component, Object value);

public void on_masterGrid_dragStart(Component component, Object value);
public void on_masterGrid_dragEnd(Component component, Object value);
public void on_masterGrid_drop(Component component, Object value);
```

### Handler Implementation Pattern
```java
public void on_componentName_dragStart(Component component, Object value) {
    if (value instanceof CDragDropEvent) {
        CDragDropEvent event = (CDragDropEvent) value;
        // Extract dragged items
        // Track them in instance fields
        LOGGER.debug("[DragDebug] Drag started: {}", event.getDraggedItem());
    }
}

public void on_componentName_drop(Component component, Object value) {
    if (!(value instanceof CDragDropEvent)) return;
    
    CDragDropEvent event = (CDragDropEvent) value;
    try {
        // Business logic: reorder, move, create, delete
        // Save changes
        // Refresh UI components
        CNotificationService.showSuccess("Operation completed");
    } catch (Exception e) {
        LOGGER.error("Error in drop handler", e);
        CNotificationService.showException("Error", e);
    } finally {
        // Clear tracked items
    }
}

public void on_componentName_dragEnd(Component component, Object value) {
    LOGGER.debug("[DragDebug] Drag ended");
    // Clear tracked items if needed
}
```

## Registration Pattern

### Automatic Registration (Preferred)
```java
// In page service initialization:
componentSprintItems = new CComponentListSprintItems(...);
componentSprintItems.registerWithPageService(this);  // ✅ Correct

// DON'T do this:
registerComponent("sprintItems", componentSprintItems);  // ❌ Manual
bindMethods(this);  // ❌ Redundant - called once in CPageService.bind()
```

### Component Implementation
```java
public class CComponentListSprintItems extends CComponentListEntityBase<CSprint, CSprintItem> {
    
    @Override
    public String getComponentName() {
        return "sprintItems";  // Must match handler names
    }
    
    // No need to override registerWithPageService() - inherited from base
}
```

## Binding Lifecycle

1. **Component Creation**: Component is instantiated
2. **Registration**: `component.registerWithPageService(pageService)` called
3. **Binding**: `CPageService.bind()` called ONCE during initialization
4. **Method Scanning**: CPageService scans for `on_{name}_{action}` methods
5. **Event Attachment**: Listeners attached to registered components
6. **Runtime**: Events propagate to bound handlers

## Helper Method Pattern

Helper methods should be:
- `private` or `protected`
- Well-documented with JavaDoc
- Named clearly: `extract...`, `calculate...`, `validate...`, etc.
- Return null or throw meaningful exceptions on error
- Log errors with [DragDebug] prefix

```java
/**
 * Helper method to extract sprint item from drag event.
 * @param event the drag-drop event
 * @return extracted sprint item, or null if extraction fails
 */
private CSprintItem extractSprintItemFromEvent(CDragDropEvent event) {
    if (event == null || event.getDraggedItems() == null) {
        return null;
    }
    try {
        Object item = event.getDraggedItem();
        if (item instanceof CSprintItem) {
            return (CSprintItem) item;
        }
        LOGGER.warn("[DragDebug] Item not a CSprintItem: {}", 
            item != null ? item.getClass() : "null");
        return null;
    } catch (Exception e) {
        LOGGER.error("[DragDebug] Error extracting item", e);
        return null;
    }
}
```

## Coding Rules

### DO:
- ✅ Use interfaces (IPageServiceAutoRegistrable, IHasDragControl)
- ✅ Extend base classes (CComponentListEntityBase)
- ✅ Override `getComponentName()` with specific names
- ✅ Call `registerWithPageService()` for automatic registration
- ✅ Use [DragDebug] prefix in drag-drop logs
- ✅ Use [BindDebug] prefix in binding logs
- ✅ Check instanceof before casting CDragDropEvent
- ✅ Use try-catch-finally in handlers
- ✅ Clear tracked items in finally blocks
- ✅ Refresh UI after business logic completes

### DON'T:
- ❌ Call `bindMethods()` after component registration
- ❌ Use manual `registerComponent()` unless absolutely necessary
- ❌ Put business logic in UI components
- ❌ Mix drag-drop logic with UI rendering
- ❌ Skip null checks in event handlers
- ❌ Forget to clear tracked items after drops
- ❌ Use raw Vaadin Grid events (use CDragDropEvent)

## Component Naming Convention

| Component Type | Name Pattern | Example |
|---------------|--------------|---------|
| Master Grid | `masterGrid` | CComponentGridEntity |
| Entity List | `{entity}Items` | sprintItems, backlogItems |
| Detail Panel | `{entity}Details` | itemDetails |
| Toolbar | `toolbar{Purpose}` | toolbarCrud |

## Troubleshooting

### Events Not Reaching Page Service
1. Check component is registered: `component.registerWithPageService(this)`
2. Verify `getComponentName()` matches handler name
3. Check `CPageService.bind()` was called
4. Look for [BindDebug] logs showing registration

### Drag-Drop Not Working
1. Verify `setDragEnabled(true)` called on source
2. Verify `setDropEnabled(true)` called on target
3. Check component implements IHasDragStart/IHasDragEnd
4. Look for [DragDebug] logs showing event flow

### Handler Not Invoked
1. Verify handler signature: `on_{componentName}_{action}(Component, Object)`
2. Check method is public
3. Verify component name matches: `getComponentName() == "sprintItems"`
4. Check CPageService can access the method (not in subclass)

## Future Extensions

To add drag-drop to a new component:

1. **Implement interfaces:**
   ```java
   public class CNewComponent extends CComponentListEntityBase<Master, Child>
           implements IPageServiceAutoRegistrable, IHasDragControl {
   ```

2. **Override getComponentName:**
   ```java
   @Override
   public String getComponentName() {
       return "newComponent";
   }
   ```

3. **Register with page service:**
   ```java
   newComponent = new CNewComponent(...);
   newComponent.registerWithPageService(this);
   ```

4. **Add page service handlers:**
   ```java
   public void on_newComponent_dragStart(Component c, Object v) { }
   public void on_newComponent_drop(Component c, Object v) { }
   public void on_newComponent_dragEnd(Component c, Object v) { }
   ```

5. **Enable drag-drop:**
   ```java
   newComponent.setDragEnabled(true);
   newComponent.setDropEnabled(true);
   ```
