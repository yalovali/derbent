# Entity Selection Component Architecture

## Overview

This document describes the architecture and design of the entity selection components in the Derbent project. The design follows a component-based pattern that separates reusable UI logic from dialog-specific concerns.

## Problem Statement

The original `CDialogEntitySelection` was a monolithic dialog that contained all the entity selection UI logic tightly coupled with dialog lifecycle management. This made it impossible to reuse the entity selection functionality outside of a modal dialog context.

**Requirements:**
1. Extract all visible content (except buttons) into a reusable component
2. Reduce the dialog to only manage component instantiation and button handling
3. Enable standalone component usage in pages, panels, and other layouts
4. Provide common interaction patterns through interfaces
5. Maintain backward compatibility with existing dialog usage

## Solution Architecture

### Component Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                   CComponentEntitySelection                  │
│                  (Reusable Component)                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Entity Type Selector (ComboBox)                       │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Search Toolbar (CComponentGridSearchToolbar)          │  │
│  │  • ID Filter                                          │  │
│  │  • Name Filter                                        │  │
│  │  • Description Filter                                 │  │
│  │  • Status Filter                                      │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Selection Indicator                                   │  │
│  │  • Selected count label                               │  │
│  │  • Reset button                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Items Grid (CGrid)                                    │  │
│  │  • ID column                                          │  │
│  │  • Name column                                        │  │
│  │  • Description column                                 │  │
│  │  • Status column (with colors/icons)                  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ▲
                            │ uses
                            │
┌─────────────────────────────────────────────────────────────┐
│              CDialogEntitySelection (Dialog)                 │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Dialog Header (Title + Icon)                         │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ CComponentEntitySelection (Content)                   │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Button Layout                                         │  │
│  │  • Select Button (Primary)                            │  │
│  │  • Cancel Button (Tertiary)                           │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Class Responsibilities

#### CComponentEntitySelection<EntityClass>
**Purpose:** Reusable entity selection component

**Responsibilities:**
- Render entity type selector dropdown
- Display filterable/searchable grid of items
- Manage selection state (single/multi-select)
- Track and display selection count
- Support already-selected items (hide or pre-select)
- Notify parent of selection changes
- Provide public API for selection access

**Key Methods:**
```java
// Constructors
CComponentEntitySelection(entityTypes, itemsProvider, onSelectionChanged, multiSelect)
CComponentEntitySelection(..., alreadySelectedProvider, alreadySelectedMode)

// Public API
Set<EntityClass> getSelectedItems()
List<EntityClass> getAlreadySelectedItems()
void reset()
void setEntityType(EntityTypeConfig<?> config)
```

#### CDialogEntitySelection<EntityClass>
**Purpose:** Modal dialog wrapper for entity selection

**Responsibilities:**
- Create and configure CComponentEntitySelection
- Manage dialog lifecycle (open, close)
- Handle button actions (Select, Cancel)
- Validate selection before confirmation
- Invoke selection callback on confirmation

**Key Methods:**
```java
// Constructor
CDialogEntitySelection(title, entityTypes, itemsProvider, onSelection, multiSelect, 
                       alreadySelectedProvider, alreadySelectedMode)

// Dialog management
void open()
void close()

// Internal handlers
void on_buttonSelect_clicked()
void on_buttonCancel_clicked()
void on_componentEntitySelection_selectionChanged(Set<EntityClass>)
```

### Supporting Classes

#### EntityTypeConfig<E>
Configuration for an entity type that can be selected.

```java
class EntityTypeConfig<E extends CEntityDB<E>> {
    String displayName;        // UI display name
    Class<E> entityClass;      // Entity class type
    CAbstractService<E> service; // Service for loading entities
}
```

#### ItemsProvider<T>
Functional interface for loading items based on entity type.

```java
@FunctionalInterface
interface ItemsProvider<T> {
    List<T> getItems(EntityTypeConfig<?> config);
}
```

#### AlreadySelectedMode
Enum controlling how already-selected items are handled.

```java
enum AlreadySelectedMode {
    HIDE_ALREADY_SELECTED,  // Filter out from available items
    SHOW_AS_SELECTED        // Show but pre-select in grid
}
```

### Interface Pattern: IEntitySelectionDialogSupport<ItemType>

This interface standardizes entity selection across components.

```java
interface IEntitySelectionDialogSupport<ItemType extends CEntityDB<?>> {
    // Required methods
    List<CDialogEntitySelection.EntityTypeConfig<?>> getDialogEntityTypes();
    ItemsProvider<ItemType> getItemsProvider();
    Consumer<List<ItemType>> getSelectionHandler();
    
    // Optional methods with defaults
    ItemsProvider<ItemType> getAlreadySelectedProvider() { return null; }
    AlreadySelectedMode getAlreadySelectedMode() { return HIDE_ALREADY_SELECTED; }
    String getDialogTitle() { return "Select Items"; }
    boolean isMultiSelect() { return true; }
}
```

**Benefits:**
1. **Consistency** - All components use the same patterns
2. **Discoverability** - Clear contract for what's needed
3. **Flexibility** - Default implementations for optional features
4. **Type Safety** - Generic type checking at compile time

## Usage Patterns

### Pattern 1: Modal Dialog (Simple)

```java
// Define entity types
List<EntityTypeConfig<?>> types = List.of(
    new EntityTypeConfig<>("Activities", CActivity.class, activityService)
);

// Create dialog
CDialogEntitySelection dialog = new CDialogEntitySelection(
    "Select Activity",
    types,
    config -> activityService.listByProject(project),
    selectedItems -> processSelectedActivities(selectedItems),
    true  // multi-select
);
dialog.open();
```

### Pattern 2: Modal Dialog (with Interface)

```java
class MyComponent implements IEntitySelectionDialogSupport<CActivity> {
    @Override
    public List<EntityTypeConfig<?>> getDialogEntityTypes() {
        return List.of(new EntityTypeConfig<>("Activities", CActivity.class, service));
    }
    
    @Override
    public ItemsProvider<CActivity> getItemsProvider() {
        return config -> activityService.listByProject(project);
    }
    
    @Override
    public Consumer<List<CActivity>> getSelectionHandler() {
        return items -> processActivities(items);
    }
    
    void openDialog() {
        new CDialogEntitySelection(
            getDialogTitle(),
            getDialogEntityTypes(),
            getItemsProvider(),
            getSelectionHandler(),
            isMultiSelect()
        ).open();
    }
}
```

### Pattern 3: Standalone Component

```java
// Create component for embedding in a page
CComponentEntitySelection<CActivity> selector = new CComponentEntitySelection<>(
    entityTypes,
    itemsProvider,
    selectedItems -> {
        // React to selection changes in real-time
        updatePreview(selectedItems);
    },
    true  // multi-select
);

// Add to layout
contentLayout.add(selector);

// Access selections programmatically
Set<CActivity> selected = selector.getSelectedItems();
```

### Pattern 4: Already-Selected Items (Hide Mode)

```java
// Hide items that are already in the sprint
ItemsProvider<CProjectItem<?>> alreadySelected = config -> {
    List<CSprintItem> currentItems = sprint.getItems();
    return currentItems.stream()
        .filter(item -> item.getItemType().equals(config.getEntityClass().getSimpleName()))
        .map(CSprintItem::getItem)
        .collect(Collectors.toList());
};

CDialogEntitySelection dialog = new CDialogEntitySelection(
    "Add to Sprint",
    entityTypes,
    itemsProvider,
    onSelection,
    true,
    alreadySelected,
    AlreadySelectedMode.HIDE_ALREADY_SELECTED
);
```

### Pattern 5: Already-Selected Items (Pre-Select Mode)

```java
// Show and pre-select items that are already assigned
CComponentEntitySelection<CUser> userSelector = new CComponentEntitySelection<>(
    entityTypes,
    allUsersProvider,
    selectionChanged -> updateAssignments(selectionChanged),
    true,
    currentAssignedUsers,
    AlreadySelectedMode.SHOW_AS_SELECTED
);
```

## Design Decisions

### 1. Component Extends Composite<CVerticalLayout>

**Rationale:** Using Composite provides:
- Clean encapsulation of internal structure
- Type-safe component API
- Proper Vaadin component lifecycle
- Easy integration with layouts

### 2. Generic Type Parameter <EntityClass extends CEntityDB<?>>

**Rationale:**
- Type safety at compile time
- IDE autocomplete support
- Prevention of type casting errors
- Clear contract for supported entity types

### 3. Callback-Based Selection Notification

**Rationale:**
- Decouples component from parent logic
- Supports both real-time and confirmed selection
- Flexible enough for different use cases
- Standard Java functional interface pattern

### 4. Backward-Compatible Dialog Wrapper Enums

**Rationale:**
- Existing code continues to work unchanged
- No breaking changes to public API
- Gradual migration path available
- Clear separation between dialog and component APIs

### 5. Interface-Based Configuration

**Rationale:**
- Standardizes entity selection across codebase
- Reduces boilerplate code
- Makes patterns discoverable
- Enables code reuse through base implementations

## Real-World Example: CComponentListSprintItems

This class demonstrates best practices for using the entity selection component.

**Features:**
- Implements `IEntitySelectionDialogSupport<CProjectItem<?>>`
- Supports multiple entity types (Activities, Meetings)
- Filters items by project context
- Hides already-selected items from dialog
- Processes selections into sprint items
- Notifies listeners of changes

**Key Implementation Details:**

```java
public class CComponentListSprintItems 
        extends CComponentListEntityBase<CSprint, CSprintItem>
        implements IEntitySelectionDialogSupport<CProjectItem<?>> {
    
    @Override
    public List<EntityTypeConfig<?>> getDialogEntityTypes() {
        return List.of(
            new EntityTypeConfig<>("CActivity", CActivity.class, activityService),
            new EntityTypeConfig<>("CMeeting", CMeeting.class, meetingService)
        );
    }
    
    @Override
    public ItemsProvider<CProjectItem<?>> getItemsProvider() {
        return config -> {
            CProject project = getMasterEntity().getProject();
            if (config.getEntityClass() == CActivity.class) {
                return activityService.listByProject(project);
            } else if (config.getEntityClass() == CMeeting.class) {
                return meetingService.listByProject(project);
            }
            return new ArrayList<>();
        };
    }
    
    @Override
    public ItemsProvider<CProjectItem<?>> getAlreadySelectedProvider() {
        return config -> {
            // Return items already in the sprint
            List<CSprintItem> current = sprintItemService.findByMasterId(sprint.getId());
            return current.stream()
                .filter(item -> config.getEntityClass().getSimpleName()
                                      .equals(item.getItemType()))
                .map(CSprintItem::getItem)
                .collect(Collectors.toList());
        };
    }
    
    @Override
    public Consumer<List<CProjectItem<?>>> getSelectionHandler() {
        return selectedItems -> {
            for (CProjectItem<?> item : selectedItems) {
                CSprintItem sprintItem = new CSprintItem();
                sprintItem.setSprint(getMasterEntity());
                sprintItem.setItemId(item.getId());
                sprintItem.setItemType(item.getClass().getSimpleName());
                sprintItem.setItemOrder(getNextOrder());
                childService.save(sprintItem);
            }
            refreshGrid();
            notifyListeners();
        };
    }
}
```

## Migration Guide

### From Dialog-Only to Component-Based

**Before (Dialog only):**
```java
CDialogEntitySelection dialog = new CDialogEntitySelection(...);
dialog.open();
// No access to component
```

**After (Component access):**
```java
// Option 1: Still use dialog (no changes needed)
CDialogEntitySelection dialog = new CDialogEntitySelection(...);
dialog.open();

// Option 2: Use component directly
CComponentEntitySelection component = new CComponentEntitySelection(...);
myLayout.add(component);
Set<Entity> selected = component.getSelectedItems();
```

### Adding Interface Support

**Before:**
```java
class MyComponent extends VerticalLayout {
    void openSelectionDialog() {
        CDialogEntitySelection dialog = new CDialogEntitySelection(
            "Select",
            types,
            provider,
            handler,
            true
        );
        dialog.open();
    }
}
```

**After:**
```java
class MyComponent extends VerticalLayout 
        implements IEntitySelectionDialogSupport<MyEntity> {
    
    @Override
    public List<EntityTypeConfig<?>> getDialogEntityTypes() {
        return List.of(new EntityTypeConfig<>("Type", MyEntity.class, service));
    }
    
    @Override
    public ItemsProvider<MyEntity> getItemsProvider() {
        return config -> service.listAll();
    }
    
    @Override
    public Consumer<List<MyEntity>> getSelectionHandler() {
        return items -> processItems(items);
    }
    
    void openSelectionDialog() {
        new CDialogEntitySelection(
            getDialogTitle(),
            getDialogEntityTypes(),
            getItemsProvider(),
            getSelectionHandler(),
            isMultiSelect()
        ).open();
    }
}
```

## Testing Strategy

### Unit Tests
- Test component selection logic
- Test filter application
- Test already-selected item handling
- Test single vs multi-select modes

### Integration Tests
- Test dialog workflow (open, select, confirm)
- Test component in different layouts
- Test with multiple entity types
- Test with real services

### UI Tests (Playwright)
- Navigate to pages using entity selection
- Open selection dialogs
- Filter and search items
- Select items and verify state
- Confirm selections and verify results

**Example Test:**
```java
// Navigate to sprint management
playwright.navigate("/sprints");
// Click add items button
playwright.click("button[tooltip='Add items to sprint']");
// Wait for dialog
playwright.waitFor("dialog[title='Select Items to Add to Sprint']");
// Select entity type
playwright.select("combo-box[label='Entity Type']", "Activities");
// Enter search filter
playwright.type("text-field[label='Name']", "Sprint Planning");
// Click item in grid
playwright.click("grid tr:has-text('Sprint Planning')");
// Verify selection count
playwright.assertText("span:has-text('1 selected')");
// Click select button
playwright.click("button:has-text('Select')");
// Verify item added to sprint
playwright.assertText("grid tr:has-text('Sprint Planning')");
```

## Performance Considerations

### Lazy Loading
- Items are loaded only when entity type changes
- Grid uses virtual scrolling for large datasets
- Filters are debounced (300ms default)

### Memory Management
- Selected items maintained as HashSet for O(1) lookup
- Grid rows are virtualized by Vaadin
- Reflection methods are cached per entity type

### Network Optimization
- Single service call per entity type
- Filtering happens client-side
- No server round-trips during filtering

## Future Enhancements

### Potential Improvements
1. **Pagination** - Support for very large datasets
2. **Column Customization** - Allow custom column configuration
3. **Bulk Actions** - Actions on selected items (export, assign, etc.)
4. **Saved Filters** - Remember filter preferences per user
5. **Advanced Filters** - Date ranges, numeric comparisons, etc.
6. **Keyboard Navigation** - Full keyboard support for accessibility
7. **Drag and Drop** - Drag items from grid to target

### Extension Points
- Custom column renderers
- Additional filter types
- Custom selection validation
- Export selected items
- Item preview panel

## Conclusion

The entity selection component architecture provides a flexible, reusable solution for item selection across the Derbent application. The separation of concerns between the component and dialog allows for:

1. **Reusability** - Component can be used in any context
2. **Maintainability** - Clear separation of responsibilities
3. **Testability** - Component and dialog can be tested independently
4. **Extensibility** - Easy to add new features or customizations
5. **Consistency** - Standard patterns across the codebase

The interface-based approach ensures that all entity selection implementations follow the same patterns while allowing for customization based on specific requirements.

## References

- `CComponentEntitySelection.java` - Main component implementation
- `CDialogEntitySelection.java` - Dialog wrapper implementation
- `IEntitySelectionDialogSupport.java` - Interface definition
- `CComponentListSprintItems.java` - Reference implementation
- `docs/development/component-entity-selection-usage.md` - Usage guide
