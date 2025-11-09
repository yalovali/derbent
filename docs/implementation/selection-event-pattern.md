# Selection Event Pattern - Master View Sections

## Overview

This document describes the unified selection change event pattern used across all Master View Section implementations (Grid, Gantt, TreeGrid, etc.) in the Derbent application.

## Problem Statement

Previously, both `CMasterViewSectionGrid` and `CMasterViewSectionGannt` defined their own separate `SelectionChangeEvent` classes, leading to:
- Code duplication
- Inconsistent event handling
- Hardcoded dependency in base class to Grid's event
- Manual implementation required at each final class level

## Solution

Move the `SelectionChangeEvent` definition to the base class (`CMasterViewSectionBase`) and use automatic listener registration at the parent page level.

## Architecture

### Class Hierarchy

```
CAbstractEntityDBPage (handles selection events)
    ↓
CAbstractNamedEntityPage
    ↓
CProjectAwareMDPage
    ↓
CGridViewBaseGannt (creates Gantt master view)
```

### Event Flow

```
1. createMasterComponent()
   - Subclass creates specific master view (Grid, Gantt, etc.)
   - Example: new CMasterViewSectionGannt<>(...)

2. updateMasterComponent() [AUTOMATIC - called by parent]
   - masterViewSection.addSelectionChangeListener(this::onSelectionChanged)
   - Registers listener for ANY master view type

3. User Selection in UI
   - Grid or Gantt detects selection change
   - Fires: fireEvent(new SelectionChangeEvent<>(this, selectedItem))

4. onSelectionChanged() [AUTOMATIC - parent class handler]
   - setCurrentEntity(selectedItem)
   - populateForm()
   - crudToolbar.setCurrentEntity(selectedItem)
   - sessionService.setActiveId(...)
```

## Implementation Details

### CMasterViewSectionBase

**Location:** `src/main/java/tech/derbent/api/views/grids/CMasterViewSectionBase.java`

```java
public abstract class CMasterViewSectionBase<EntityClass extends CEntityDB<EntityClass>> 
    extends CVerticalLayout {

    // --- Custom Event Definition ---
    public static class SelectionChangeEvent<T extends CEntityDB<T>> 
        extends ComponentEvent<CMasterViewSectionBase<T>> {

        private static final long serialVersionUID = 1L;
        private final T selectedItem;

        public SelectionChangeEvent(final CMasterViewSectionBase<T> source, 
                                   final T selectedItem) {
            super(source, false);
            this.selectedItem = selectedItem;
        }

        public T getSelectedItem() { return selectedItem; }
    }

    // Listener registration method
    public Registration addSelectionChangeListener(
        final ComponentEventListener<SelectionChangeEvent<EntityClass>> listener) {
        return addListener(SelectionChangeEvent.class, listener);
    }
}
```

### CMasterViewSectionGrid

**Location:** `src/main/java/tech/derbent/api/views/grids/CMasterViewSectionGrid.java`

```java
public class CMasterViewSectionGrid<EntityClass extends CEntityDB<EntityClass>> 
    extends CMasterViewSectionBase<EntityClass> {

    protected void onSelectionChange(final ValueChangeEvent<?> event) {
        final EntityClass value = (EntityClass) event.getValue();
        // Fire base class event - works automatically!
        fireEvent(new SelectionChangeEvent<>(this, value));
    }
}
```

### CMasterViewSectionGannt

**Location:** `src/main/java/tech/derbent/app/gannt/view/CMasterViewSectionGannt.java`

```java
public class CMasterViewSectionGannt<EntityClass extends CEntityDB<EntityClass>> 
    extends CMasterViewSectionBase<EntityClass> {

    protected void onSelectionChange(final ValueChangeEvent<?> event) {
        final EntityClass value = (EntityClass) event.getValue();
        // Fire base class event - same as Grid!
        fireEvent(new SelectionChangeEvent<>(this, value));
    }
}
```

### CAbstractEntityDBPage

**Location:** `src/main/java/tech/derbent/api/views/CAbstractEntityDBPage.java`

```java
public abstract class CAbstractEntityDBPage<EntityClass extends CEntityDB<EntityClass>> {

    @PostConstruct
    protected final void createMaster() throws Exception {
        createMasterComponent();  // Subclass creates Grid/Gantt/etc.
        updateMasterComponent();  // Register listener automatically
    }

    protected void updateMasterComponent() throws Exception {
        if (masterViewSection == null) {
            return;
        }
        // Works for ANY master view type!
        masterViewSection.addSelectionChangeListener(this::onSelectionChanged);
        // ... rest of setup
    }

    protected void onSelectionChanged(
        final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
        final EntityClass value = event.getSelectedItem();
        setCurrentEntity(value);
        populateForm();
        crudToolbar.setCurrentEntity(value);
        sessionService.setActiveId(...);
    }
}
```

## Benefits

### 1. Single Source of Truth
- Event defined once in `CMasterViewSectionBase`
- No duplicate definitions
- Follows DRY (Don't Repeat Yourself) principle

### 2. Automatic Registration
- Parent class (`CAbstractEntityDBPage`) registers listener
- No manual implementation needed at final class level
- Works for Grid, Gantt, TreeGrid, and any future implementations

### 3. Type Safety
- Proper generics throughout: `SelectionChangeEvent<T extends CEntityDB<T>>`
- Compile-time type checking
- No casting required

### 4. Consistent Pattern
- All master view sections use same event type
- Same handler in parent class
- Predictable behavior across application

### 5. Interface-Based Design
- Parent class level implementation
- Follows Open/Closed Principle
- Easy to extend with new master view types

## Usage Example

### Creating a New Master View Type

```java
public class CMasterViewSectionCustom<EntityClass extends CEntityDB<EntityClass>> 
    extends CMasterViewSectionBase<EntityClass> {

    @Override
    protected void onSelectionChange(final ValueChangeEvent<?> event) {
        final EntityClass value = (EntityClass) event.getValue();
        // Just fire the base class event - automatic handling!
        fireEvent(new SelectionChangeEvent<>(this, value));
    }
}
```

That's it! The parent page class will automatically:
1. Register the selection change listener
2. Handle selection changes
3. Update the current entity
4. Populate the form
5. Update session state

## Testing

### Manual Testing
1. Navigate to a page with Gantt chart (e.g., Project Gantt View)
2. Select an item in the Gantt grid
3. Verify the detail form populates with selected item
4. Verify CRUD toolbar updates with current entity

### Automated Testing
- Test file: `src/test/java/automated_tests/tech/derbent/ui/automation/CGanttChartTest.java`
- Includes Gantt chart UI tests
- Verifies login workflow and DB generation

## Conclusion

The unified selection event pattern provides a clean, maintainable, and extensible architecture for handling selection changes across all master view types. By implementing the pattern at the parent class level with proper interfaces, we eliminate code duplication and ensure consistent behavior throughout the application.

## See Also
- [Coding Standards](../architecture/coding-standards.md)
- [Playwright Testing Strategies](../testing/playwright-strategies.md)
- [Component Guidelines](../development/copilot-guidelines.md)
