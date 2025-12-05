# CComponentEntitySelection Usage Guide

## Overview

`CComponentEntitySelection` is a reusable component for selecting entities from a grid with search/filter capabilities. This component can be embedded in dialogs, pages, or panels for entity selection functionality.

## Features

- **Entity type selection dropdown** - Switch between different entity types
- **Grid with colored status display** - Visual status indicators
- **Search toolbar** - Filter by ID, Name, Description, Status
- **Single or multi-select modes** - Configurable selection behavior
- **Selected item count indicator** - Real-time selection feedback
- **Reset button** - Clear all selections
- **Selection persistence** - Selections persist across grid filtering
- **Already-selected items support** - Hide or pre-select already chosen items

## Direct Component Usage

### Basic Example

```java
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.ItemsProvider;

// Define entity types available for selection
List<EntityTypeConfig<?>> entityTypes = List.of(
    new EntityTypeConfig<>("Activities", CActivity.class, activityService),
    new EntityTypeConfig<>("Meetings", CMeeting.class, meetingService)
);

// Create items provider
ItemsProvider<CProjectItem<?>> itemsProvider = config -> {
    if (config.getEntityClass() == CActivity.class) {
        return (List<CProjectItem<?>>) (List<?>) activityService.listByProject(currentProject);
    } else if (config.getEntityClass() == CMeeting.class) {
        return (List<CProjectItem<?>>) (List<?>) meetingService.listByProject(currentProject);
    }
    return new ArrayList<>();
};

// Create selection change handler
Consumer<Set<CProjectItem<?>>> selectionHandler = selectedItems -> {
    LOGGER.debug("Selected {} items", selectedItems.size());
    // Handle selection change
};

// Create component
CComponentEntitySelection<CProjectItem<?>> component = new CComponentEntitySelection<>(
    entityTypes,
    itemsProvider,
    selectionHandler,
    true  // multi-select mode
);

// Add to your layout
layout.add(component);
```

### Example with Already-Selected Items

```java
// Provider for already-selected items (e.g., items already in a sprint)
ItemsProvider<CProjectItem<?>> alreadySelectedProvider = config -> {
    final CSprint sprint = getCurrentSprint();
    if (sprint == null || sprint.getId() == null) {
        return new ArrayList<>();
    }
    
    // Get current sprint items and filter by entity type
    List<CSprintItem> sprintItems = sprintItemService.findByMasterIdWithItems(sprint.getId());
    List<CProjectItem<?>> result = new ArrayList<>();
    
    String targetType = config.getEntityClass().getSimpleName();
    for (CSprintItem sprintItem : sprintItems) {
        if (sprintItem.getItem() != null && targetType.equals(sprintItem.getItemType())) {
            result.add(sprintItem.getItem());
        }
    }
    return result;
};

// Create component with already-selected items support
CComponentEntitySelection<CProjectItem<?>> component = new CComponentEntitySelection<>(
    entityTypes,
    itemsProvider,
    selectionHandler,
    true,  // multi-select
    alreadySelectedProvider,
    CComponentEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED
);
```

### Already-Selected Modes

```java
// Mode 1: Hide already-selected items from the grid
AlreadySelectedMode.HIDE_ALREADY_SELECTED

// Mode 2: Show already-selected items but pre-select them (visually marked)
AlreadySelectedMode.SHOW_AS_SELECTED
```

## Dialog Usage Pattern

For modal selection, use `CDialogEntitySelection` which wraps the component:

```java
CDialogEntitySelection<CProjectItem<?>> dialog = new CDialogEntitySelection<>(
    "Select Items to Add",
    entityTypes,
    itemsProvider,
    selectedItems -> {
        // Handle confirmed selection
        for (CProjectItem<?> item : selectedItems) {
            addItemToContainer(item);
        }
    },
    true,  // multi-select
    alreadySelectedProvider,
    CDialogEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED
);
dialog.open();
```

## Using IEntitySelectionDialogSupport Interface

Components that need entity selection dialogs should implement `IEntitySelectionDialogSupport`:

```java
public class MyComponent extends VerticalLayout 
        implements IEntitySelectionDialogSupport<CProjectItem<?>> {
    
    @Override
    public List<CDialogEntitySelection.EntityTypeConfig<?>> getDialogEntityTypes() {
        return List.of(
            new CDialogEntitySelection.EntityTypeConfig<>("Activities", CActivity.class, activityService),
            new CDialogEntitySelection.EntityTypeConfig<>("Meetings", CMeeting.class, meetingService)
        );
    }
    
    @Override
    public CDialogEntitySelection.ItemsProvider<CProjectItem<?>> getItemsProvider() {
        return config -> {
            // Return items based on entity type
            if (config.getEntityClass() == CActivity.class) {
                return (List<CProjectItem<?>>) (List<?>) activityService.listByProject(project);
            } else if (config.getEntityClass() == CMeeting.class) {
                return (List<CProjectItem<?>>) (List<?>) meetingService.listByProject(project);
            }
            return new ArrayList<>();
        };
    }
    
    @Override
    public Consumer<List<CProjectItem<?>>> getSelectionHandler() {
        return selectedItems -> {
            // Process selected items
            for (CProjectItem<?> item : selectedItems) {
                processItem(item);
            }
        };
    }
    
    @Override
    public CDialogEntitySelection.ItemsProvider<CProjectItem<?>> getAlreadySelectedProvider() {
        return config -> {
            // Return already selected items
            return getCurrentlySelectedItems(config);
        };
    }
    
    @Override
    public CDialogEntitySelection.AlreadySelectedMode getAlreadySelectedMode() {
        return CDialogEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED;
    }
    
    @Override
    public String getDialogTitle() {
        return "Select Items";
    }
    
    @Override
    public boolean isMultiSelect() {
        return true;
    }
    
    // Then open the dialog using the interface methods
    private void openSelectionDialog() {
        CDialogEntitySelection<CProjectItem<?>> dialog = new CDialogEntitySelection<>(
            getDialogTitle(),
            getDialogEntityTypes(),
            getItemsProvider(),
            getSelectionHandler(),
            isMultiSelect(),
            getAlreadySelectedProvider(),
            getAlreadySelectedMode()
        );
        dialog.open();
    }
}
```

## Component API Reference

### Constructors

#### Basic Constructor
```java
public CComponentEntitySelection(
    List<EntityTypeConfig<?>> entityTypes,
    ItemsProvider<EntityClass> itemsProvider,
    Consumer<Set<EntityClass>> onSelectionChanged,
    boolean multiSelect
)
```

#### Full Constructor with Already-Selected Support
```java
public CComponentEntitySelection(
    List<EntityTypeConfig<?>> entityTypes,
    ItemsProvider<EntityClass> itemsProvider,
    Consumer<Set<EntityClass>> onSelectionChanged,
    boolean multiSelect,
    ItemsProvider<EntityClass> alreadySelectedProvider,
    AlreadySelectedMode alreadySelectedMode
)
```

### Public Methods

- `Set<EntityClass> getSelectedItems()` - Get currently selected items
- `List<EntityClass> getAlreadySelectedItems()` - Get already-selected items
- `AlreadySelectedMode getAlreadySelectedMode()` - Get already-selected mode
- `boolean isMultiSelect()` - Check if multi-select is enabled
- `void reset()` - Clear all selections
- `void setEntityType(EntityTypeConfig<?> config)` - Programmatically set entity type

### Nested Classes

#### EntityTypeConfig<E>
Represents a selectable entity type configuration:
- `String getDisplayName()` - Display name for UI
- `Class<E> getEntityClass()` - Entity class type
- `CAbstractService<E> getService()` - Service for the entity

#### ItemsProvider<T>
Functional interface for providing items:
```java
@FunctionalInterface
public interface ItemsProvider<T> {
    List<T> getItems(EntityTypeConfig<?> config);
}
```

#### AlreadySelectedMode
Enum for handling already-selected items:
- `HIDE_ALREADY_SELECTED` - Hide from grid
- `SHOW_AS_SELECTED` - Show but pre-select

## Real-World Example: CComponentListSprintItems

See `CComponentListSprintItems` for a complete working example that:
1. Implements `IEntitySelectionDialogSupport`
2. Provides multiple entity types (Activities, Meetings)
3. Filters items by project
4. Handles already-selected items
5. Processes selected items into sprint items

## Best Practices

1. **Use interface pattern** - Implement `IEntitySelectionDialogSupport` for consistent behavior
2. **Handle null cases** - Always check for null master entities in providers
3. **Log operations** - Use LOGGER for debugging selection operations
4. **Type safety** - Use proper generic bounds and type checks
5. **Error handling** - Wrap provider logic in try-catch blocks
6. **Notification feedback** - Use `CNotificationService` for user feedback

## Architecture Notes

### Component Structure
```
CComponentEntitySelection extends Composite<CVerticalLayout>
├── Entity type selector (ComboBox<EntityTypeConfig<?>>)
├── Search toolbar (CComponentGridSearchToolbar)
│   ├── ID filter (TextField)
│   ├── Name filter (TextField)
│   ├── Description filter (TextField)
│   └── Status filter (ComboBox)
├── Selection indicator (HorizontalLayout)
│   ├── Selected count label
│   └── Reset button
└── Items grid (CGrid<EntityClass>)
    ├── ID column
    ├── Name column
    ├── Description column
    └── Status column (with color/icon)
```

### Dialog Wrapper
```
CDialogEntitySelection extends CDialog
├── CComponentEntitySelection (content)
└── Button layout
    ├── Select button (primary)
    └── Cancel button (tertiary)
```

### Integration Pattern
```
Component/Page
└── implements IEntitySelectionDialogSupport
    ├── Defines entity types
    ├── Provides items
    ├── Handles selection
    └── Opens CDialogEntitySelection
        └── Creates CComponentEntitySelection
            └── Displays selection UI
```

## Testing

The component automatically initializes sample data if the company combobox is empty during Playwright tests. This ensures consistent test behavior.

See `run-playwright-tests.sh` for testing approaches:
- Menu navigation tests
- Comprehensive view tests
- CRUD operation tests

## Migration from Old Dialog Pattern

If you have existing code using `CDialogEntitySelection`, no changes are needed - the dialog maintains full backward compatibility. However, if you want to use the component directly:

**Old (Dialog only):**
```java
CDialogEntitySelection dialog = new CDialogEntitySelection(...);
dialog.open();
```

**New (Component directly):**
```java
CComponentEntitySelection component = new CComponentEntitySelection(...);
myLayout.add(component);
// Access selections via component.getSelectedItems()
```

## Related Classes

- `CDialogEntitySelection` - Dialog wrapper for modal selection
- `CComponentGridSearchToolbar` - Search/filter toolbar component
- `IEntitySelectionDialogSupport` - Interface for standardized dialog support
- `CComponentListEntityBase` - Base class for entity list components
- `CComponentListSprintItems` - Example implementation

## Conclusion

`CComponentEntitySelection` provides a flexible, reusable entity selection component that can be used in dialogs, embedded in pages, or integrated into custom layouts. The interface-based pattern ensures consistent behavior across the application while allowing for customization based on specific needs.
