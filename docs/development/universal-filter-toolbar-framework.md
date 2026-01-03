# Universal Filter Toolbar Framework

## Overview

The Universal Filter Toolbar Framework provides a composable, type-safe filtering system that can be used throughout the Derbent application for any filtering needs: kanban boards, grids, master-detail views, asset management, budget filtering, and more.

## Design Principles

1. **Composition over Inheritance** - Build filter toolbars from reusable filter components
2. **Type-Safe Criteria** - Generic `FilterCriteria<T>` system for type safety
3. **Dynamic Discovery** - Auto-detect available options from data (e.g., entity types from sprint items)
4. **Declarative Configuration** - Simple builder pattern for toolbar setup
5. **Minimal Complexity** - Clear, simple abstractions with minimal boilerplate

## Architecture

### Core Classes

#### `CAbstractFilterToolbar<T>`
Abstract base class for all filter toolbars. Provides:
- Filter component management
- Change listener notification
- Filter criteria holder
- Value persistence support
- Clear filters functionality

#### `IFilterComponent<T>`
Interface for individual filter components. Each filter represents one filtering criterion (entity type, sprint, responsible user, etc.).

#### `CAbstractFilterComponent<T>`
Base class for filter component implementations. Reduces boilerplate by providing common functionality.

#### `FilterCriteria<T>`
Type-safe holder for filter values. Uses a Map-based approach for flexibility while maintaining type safety.

### Pre-Built Filter Components

#### `CEntityTypeFilter`
Dynamically discovers and filters by entity types (Activity, Meeting, Sprint, etc.).
- **Key**: `"entityType"`
- **Value Type**: `Class<?>`
- **Features**: Auto-discovery, "All types" option

#### `CSprintFilter`
Filters by sprint selection.
- **Key**: `"sprint"`
- **Value Type**: `CSprint`
- **Features**: Color-aware dropdown, default sprint

#### `CResponsibleUserFilter`
Filters by item ownership.
- **Key**: `"responsibleUser"`
- **Value Type**: `ResponsibleFilterMode` (ALL, CURRENT_USER)

## Usage Examples

### Kanban Board Filtering

```java
public class CComponentKanbanBoardFilterToolbar extends CUniversalFilterToolbar<CSprintItem> {
    
    private final CSprintFilter sprintFilter;
    private final CEntityTypeFilter entityTypeFilter;
    private final CResponsibleUserFilter responsibleUserFilter;
    
    public CComponentKanbanBoardFilterToolbar() {
        super();
        setId("kanbanBoardFilterToolbar");
        
        // Create filter components
        sprintFilter = new CSprintFilter();
        entityTypeFilter = new CEntityTypeFilter();
        responsibleUserFilter = new CResponsibleUserFilter();
        
        // Add to toolbar (order matters for display)
        addFilterComponent(sprintFilter);
        addFilterComponent(entityTypeFilter);
        addFilterComponent(responsibleUserFilter);
        
        // Build clear button
        build();
    }
    
    // Update available options dynamically
    public void setAvailableSprints(List<CSprint> sprints, CSprint defaultSprint) {
        sprintFilter.setAvailableSprints(sprints, defaultSprint);
    }
    
    public void setAvailableItems(List<CSprintItem> items) {
        // Extract entities and discover types
        List<Object> entities = items.stream()
            .map(CSprintItem::getItem)
            .filter(item -> item != null)
            .map(item -> (Object) item)
            .toList();
        entityTypeFilter.setAvailableEntityTypes(entities);
    }
}
```

### Grid Filtering (Example)

```java
public class CActivityGridFilterToolbar extends CUniversalFilterToolbar<CActivity> {
    
    public CActivityGridFilterToolbar() {
        super();
        setId("activityGridFilterToolbar");
        
        // Add text search filter
        addFilterComponent(new CTextSearchFilter("Search activities..."));
        
        // Add status filter
        addFilterComponent(new CStatusFilter());
        
        // Add project filter
        addFilterComponent(new CProjectFilter());
        
        // Add date range filter
        addFilterComponent(new CDateRangeFilter());
        
        build();
    }
}
```

### Using Filter Criteria

```java
// In parent component
filterToolbar.addFilterChangeListener(criteria -> {
    // Get specific filter values
    Class<?> entityType = criteria.getValue(CEntityTypeFilter.FILTER_KEY);
    CSprint sprint = criteria.getValue(CSprintFilter.FILTER_KEY);
    ResponsibleFilterMode mode = criteria.getValue(CResponsibleUserFilter.FILTER_KEY);
    
    // Apply filters
    List<CSprintItem> filtered = filterItems(allItems, entityType, sprint, mode);
    refreshDisplay(filtered);
});
```

### Enabling Value Persistence

```java
// After creating toolbar
filterToolbar.setId("myUniqueToolbarId");  // REQUIRED
filterToolbar.valuePersist_enable();        // Enables persistence for all filter components
```

## Creating Custom Filter Components

### Step 1: Extend CAbstractFilterComponent

```java
public class CStatusFilter extends CAbstractFilterComponent<String> {
    
    public static final String FILTER_KEY = "status";
    
    private final ComboBox<String> comboBox;
    
    public CStatusFilter() {
        super(FILTER_KEY);
        comboBox = new ComboBox<>("Status");
        comboBox.setPlaceholder("All statuses");
        comboBox.setClearButtonVisible(true);
        comboBox.addValueChangeListener(event -> {
            notifyChangeListeners(event.getValue());
        });
    }
    
    @Override
    protected Component createComponent() {
        return comboBox;
    }
    
    @Override
    protected void updateComponentValue(String value) {
        comboBox.setValue(value);
    }
    
    @Override
    public void clearFilter() {
        comboBox.clear();
    }
    
    @Override
    public void enableValuePersistence(String storageId) {
        CValueStorageHelper.valuePersist_enable(
            comboBox, 
            storageId + "_" + FILTER_KEY,
            value -> value,  // toString converter
            value -> value   // fromString converter
        );
    }
    
    // Custom methods
    public void setAvailableStatuses(Set<String> statuses) {
        comboBox.setItems(statuses);
    }
}
```

### Step 2: Use in Toolbar

```java
CStatusFilter statusFilter = new CStatusFilter();
statusFilter.setAvailableStatuses(Set.of("Open", "In Progress", "Done"));
toolbar.addFilterComponent(statusFilter);
```

### Step 3: Access Filter Value

```java
String status = criteria.getValue(CStatusFilter.FILTER_KEY);
if (status != null && !status.isBlank()) {
    // Filter by status
}
```

## Benefits Over Old System

### Before (Old CComponentKanbanBoardFilterToolbar)
- 370+ lines of code
- Inner classes (CTypeOption, FilterCriteria, ResponsibleFilterMode)
- Tightly coupled to kanban board
- Hard to extend or reuse
- Meeting entity type missing (manual addition required)

### After (New Universal Framework)
- 120 lines of code (-67% reduction)
- Composable filter components
- Reusable across entire application
- Easy to extend with new filters
- **Automatic entity type discovery** (Meeting appears automatically)

## Key Improvements

1. **Automatic Entity Type Discovery**: No more manual additions - Meeting, Sprint, and any future ISprintableItem types appear automatically
2. **Composition**: Build toolbars from reusable components
3. **Consistency**: All filtering UIs use the same pattern
4. **Maintainability**: Changes to one filter affect all uses
5. **Testability**: Each filter component can be tested independently
6. **Flexibility**: Add any combination of filters to any toolbar

## Migration Guide

### Migrating Existing Filter Toolbars

1. **Identify filter needs**: What filters does your toolbar need?
2. **Choose or create filter components**: Use existing (CEntityTypeFilter, CSprintFilter) or create custom
3. **Extend CUniversalFilterToolbar**: Create a new class extending CUniversalFilterToolbar<T>
4. **Add filter components in constructor**: Use addFilterComponent()
5. **Update parent component**: Use addFilterChangeListener() and access criteria via getValue(FILTER_KEY)

### Example Migration

Old code:
```java
// Complex inner classes, custom logic
public class CMyFilterToolbar extends CComponentGridSearchToolbar {
    private ComboBox<String> statusFilter;
    private TextField searchField;
    // 200+ lines of custom code
}
```

New code:
```java
public class CMyFilterToolbar extends CUniversalFilterToolbar<MyEntity> {
    public CMyFilterToolbar() {
        super();
        setId("myFilterToolbar");
        addFilterComponent(new CTextSearchFilter());
        addFilterComponent(new CStatusFilter());
        build();
    }
}
```

## Future Extensions

Additional filter components that can be created:
- `CTextSearchFilter` - Text-based search
- `CProjectFilter` - Project selection
- `CDateRangeFilter` - Date range selection
- `CPriorityFilter` - Priority filtering
- `CNumericRangeFilter` - Numeric range (budget, hours, etc.)
- `CMultiSelectFilter` - Multiple value selection
- `CCategoryFilter` - Category/tag filtering

All following the same composable pattern!

## Best Practices

1. **Always set toolbar ID before enabling persistence**: `setId("uniqueId")`
2. **Use meaningful filter keys**: Constant strings like `FILTER_KEY = "entityType"`
3. **Provide methods to update filter options**: `setAvailableX()` methods
4. **Document filter behavior**: What values does it produce? How are they interpreted?
5. **Test filter components independently**: Each filter should work standalone
6. **Keep filters focused**: One filter = one criterion

## Troubleshooting

### Filter values not persisting
- Ensure `setId()` is called before `valuePersist_enable()`
- Check that filter component implements `enableValuePersistence()`

### Filter not updating display
- Verify filter change listeners are registered
- Check that `notifyChangeListeners()` is called in filter component

### Entity types not appearing
- Ensure `setAvailableEntityTypes()` is called with non-empty list
- Verify entities implement proper interfaces (ISprintableItem, etc.)
- Check entity registry has titles registered

## Conclusion

The Universal Filter Toolbar Framework provides a clean, maintainable, and extensible solution for filtering throughout the Derbent application. By using composition and dynamic discovery, it eliminates complexity and ensures consistency across all filtering UIs.
