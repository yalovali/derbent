# Universal Entity Type Filter Component

## Overview

`CEntityTypeFilter` is THE STANDARD component for entity type filtering and selection throughout the Derbent application. It eliminates duplicate ComboBox patterns and ensures consistent, human-friendly entity type names across the entire codebase.

## Why One Universal Component?

**Problem:** Multiple places in the codebase were creating their own ComboBoxes for entity type selection:
- Filter toolbars had `CEntityTypeFilter`
- Selection dialogs had custom `ComboBox<EntityTypeConfig<?>>`
- Each implementation handled entity names differently
- Code duplication and inconsistency

**Solution:** ONE universal component that supports both filtering and selection use cases.

## Two Operating Modes

### Filter Mode (Default)
- Includes "All types" option for showing everything
- Used in toolbars and filter panels
- Optional selection (can be null)

### Selection Mode
- No "All types" option
- Required selection from available types
- Used in dialogs and forms

## Usage Examples

### Filter Mode (Toolbars)
```java
// Create filter with "All types" option
CEntityTypeFilter filter = new CEntityTypeFilter();
filter.setAvailableEntityTypes(sprintItems); // Auto-discovers types from items
toolbar.addFilterComponent(filter);

// Listen for changes
filter.addChangeListener(selectedClass -> {
    if (selectedClass == null) {
        // "All types" selected - show everything
    } else {
        // Specific type selected - filter by it
    }
});
```

### Selection Mode (Dialogs/Forms)
```java
// Create selector without "All types"
CEntityTypeFilter selector = new CEntityTypeFilter(false);
selector.setLabel("Entity Type");
selector.setRequired(true);
selector.setWidth("150px");

// Set specific entity types to choose from
selector.setAvailableEntityClasses(List.of(
    CActivity.class, 
    CMeeting.class, 
    CRisk.class
));

// Get selected type
Class<?> selectedClass = selector.getSelectedEntityClass();
```

### Dynamic Discovery (Filter Mode)
```java
// Automatically discover types from a list of entities
CEntityTypeFilter filter = new CEntityTypeFilter();
filter.setAvailableEntityTypes(projectItems); 
// Auto-discovers: Activity, Meeting, Risk, etc.
// Always includes Activity and Meeting even if not in list
```

### Explicit Types (Selection Mode)
```java
// Specify exact types to show
CEntityTypeFilter selector = new CEntityTypeFilter(false);
selector.setAvailableEntityClasses(List.of(
    CActivity.class,
    CMeeting.class
));
// Shows only: "Activity", "Meeting"
```

## API Reference

### Constructors

```java
// Filter mode with "All types"
CEntityTypeFilter()

// Configurable mode
CEntityTypeFilter(boolean includeAllTypesOption)
```

### Configuration Methods

```java
// Set entity types from a list of entities (auto-discovery)
void setAvailableEntityTypes(List<?> items)

// Set entity types from a list of classes (explicit)
void setAvailableEntityClasses(List<Class<?>> entityClasses)

// Set component properties
void setLabel(String label)
void setRequired(boolean required)
void setWidth(String width)
```

### Query Methods

```java
// Get currently selected entity class (null if "All types" or nothing selected)
Class<?> getSelectedEntityClass()

// Get underlying ComboBox for advanced usage
CComboBox<TypeOption> getComboBox()
```

### Filter Component Methods (inherited)

```java
// Clear selection (resets to "All types" in filter mode)
void clearFilter()

// Add listener for value changes
void addChangeListener(Consumer<Class<?>> listener)
```

## Human-Friendly Names

The component automatically uses `CEntityRegistry.getEntityTitleSingular()` to display human-friendly names:

| Entity Class | Displayed As |
|--------------|--------------|
| `CActivity` | "Activity" |
| `CMeeting` | "Meeting" |
| `CRisk` | "Risk" |
| `CProject` | "Project" |
| `CSprint` | "Sprint" |

Entity classes must define the `ENTITY_TITLE_SINGULAR` constant:
```java
public class CActivity extends CProjectItem<CActivity> {
    public static final String ENTITY_TITLE_SINGULAR = "Activity";
    // ...
}
```

## Value Persistence

The component automatically persists selected values across page refreshes using `CComboBox.enablePersistence()`. Each instance uses a unique storage ID based on its filter key.

## Migration Guide

### From Custom ComboBox in CComponentEntitySelection

**Before:**
```java
private ComboBox<EntityTypeConfig<?>> comboBoxEntityType;

protected void create_combobox_typeSelector() {
    comboBoxEntityType = new ComboBox<>("Entity Type");
    comboBoxEntityType.setItems(entityTypes);
    comboBoxEntityType.setItemLabelGenerator(EntityTypeConfig::getDisplayName);
    comboBoxEntityType.setWidth("150px");
    comboBoxEntityType.setRequired(true);
    comboBoxEntityType.addValueChangeListener(e -> on_comboBoxEntityType_selectionChanged(e.getValue()));
}
```

**After:**
```java
private CEntityTypeFilter entityTypeSelector;

protected void create_entityTypeSelector() {
    entityTypeSelector = new CEntityTypeFilter(false); // Selection mode
    entityTypeSelector.setLabel("Entity Type");
    entityTypeSelector.setWidth("150px");
    entityTypeSelector.setRequired(true);
    
    // Extract classes from EntityTypeConfig list
    List<Class<?>> classes = entityTypes.stream()
        .map(EntityTypeConfig::getEntityClass)
        .collect(Collectors.toList());
    entityTypeSelector.setAvailableEntityClasses(classes);
    
    entityTypeSelector.addChangeListener(selectedClass -> {
        // Find matching EntityTypeConfig for selected class
        EntityTypeConfig<?> config = entityTypes.stream()
            .filter(c -> c.getEntityClass().equals(selectedClass))
            .findFirst()
            .orElse(null);
        on_entityType_selectionChanged(config);
    });
}
```

### From Custom Filter ComboBox

**Before:**
```java
ComboBox<String> typeFilter = new ComboBox<>("Type");
typeFilter.setItems("CActivity", "CMeeting", "All");
typeFilter.setValue("All");
typeFilter.addValueChangeListener(e -> filterByType(e.getValue()));
```

**After:**
```java
CEntityTypeFilter typeFilter = new CEntityTypeFilter(); // Filter mode
typeFilter.setAvailableEntityClasses(List.of(CActivity.class, CMeeting.class));
typeFilter.addChangeListener(selectedClass -> filterByType(selectedClass));
```

## Best Practices

### DO ✅

1. **Use CEntityTypeFilter everywhere** for entity type filtering/selection
2. **Use filter mode** (default) in toolbars and filter panels
3. **Use selection mode** (`new CEntityTypeFilter(false)`) in dialogs and forms
4. **Set appropriate labels** with `setLabel()` for selection mode
5. **Mark as required** with `setRequired(true)` in forms where appropriate

### DON'T ❌

1. **Don't create custom ComboBoxes** for entity types - use CEntityTypeFilter
2. **Don't hardcode entity names** like "CActivity" - let the component use registry
3. **Don't duplicate entity type filtering logic** - reuse the universal component
4. **Don't forget to set types** - call either `setAvailableEntityTypes()` or `setAvailableEntityClasses()`

## Implementation Details

### TypeOption Internal Class

The component uses an internal `TypeOption` class to represent entity types:
- `entityClass`: The Class<?> of the entity
- `label`: Human-friendly display name from CEntityRegistry
- `toString()`: Returns class name for persistence

### Auto-Discovery Logic

When using `setAvailableEntityTypes(List<?>)`:
1. Iterates through all items in the list
2. Extracts unique entity classes
3. Resolves display names from CEntityRegistry
4. Always includes CActivity and CMeeting (even if not in list)
5. Sorts alphabetically by display name
6. Adds "All types" option first (if in filter mode)

### Persistence

Uses `CComboBox.enablePersistence()` with storage ID pattern:
```
entityTypeFilter_entityType
```

The selected entity class name is stored (e.g., "tech.derbent.app.activities.domain.CActivity").

## Complete Example

```java
public class CComponentKanbanBoardFilterToolbar extends CUniversalFilterToolbar<CSprintItem> {
    
    private final CEntityTypeFilter entityTypeFilter;
    
    public CComponentKanbanBoardFilterToolbar() {
        super();
        
        // Create filter in filter mode
        entityTypeFilter = new CEntityTypeFilter();
        
        // Add to toolbar
        addFilterComponent(entityTypeFilter);
        
        // Listen for changes
        entityTypeFilter.addChangeListener(this::on_entityTypeChanged);
        
        build();
    }
    
    public void setAvailableItems(List<CSprintItem> items) {
        // Extract actual entities
        List<Object> entities = items.stream()
            .map(CSprintItem::getParentItem)
            .filter(Objects::nonNull)
            .toList();
        
        // Update filter - auto-discovers types
        entityTypeFilter.setAvailableEntityTypes(entities);
    }
    
    private void on_entityTypeChanged(Class<?> selectedClass) {
        if (selectedClass == null) {
            // Show all types
            showAllItems();
        } else {
            // Filter by specific type
            filterByEntityClass(selectedClass);
        }
    }
}
```

## Future Enhancements

Potential improvements for future versions:

1. **Icon Support**: Show entity-specific icons next to type names
2. **Color Support**: Use entity-specific colors in the dropdown
3. **Grouping**: Group related entity types (e.g., "Work Items" group)
4. **Search**: Add search/filter capability for long lists
5. **Multi-Select**: Support selecting multiple entity types

## Conclusion

`CEntityTypeFilter` is now THE STANDARD component for all entity type filtering and selection in the Derbent application. Using this universal component ensures:

- ✅ Consistent human-friendly names everywhere
- ✅ No code duplication
- ✅ Single source of truth (CEntityRegistry)
- ✅ Easy to maintain and extend
- ✅ Works for all use cases (filtering and selection)

**Always use `CEntityTypeFilter` - never create custom ComboBoxes for entity types!**
