# Multi-Value Persistence Pattern

## Overview

`IHasMultiValuePersistence` is a new interface that extends the single-value persistence pattern to support components that need to persist multiple key-value pairs. This is perfect for complex components that need to maintain various state information across sessions.

## Key Concepts

### Namespace-Based Storage

Instead of a single key, multi-value persistence uses a **namespace** to group related values:

```
Storage Pattern: {namespace}.{key} = value

Examples:
- kanbanBoard_project1.selectedColumn = "columnId123"
- kanbanBoard_project1.filter_status = "In Progress"
- kanbanBoard_project1.expandedSections = "details,comments"
```

### Component Responsibility

Each implementing component is responsible for:
1. **What to persist** - Decide which state needs to be saved
2. **How to serialize** - Convert objects to strings
3. **How to deserialize** - Convert strings back to objects
4. **When to restore** - Implement `onPersistenceRestore()` hook

## Interface Methods

### Core Methods (Must Implement)

```java
public interface IHasMultiValuePersistence {
    // Required - provide your own implementations
    Logger getLogger();
    String getPersistenceNamespace();
    void setPersistenceNamespace(String namespace);
    boolean isPersistenceEnabled();
    void setPersistenceEnabled(boolean enabled);
    
    // Optional - override for custom restoration
    default void onPersistenceRestore() { }
}
```

### Provided Methods (Default Implementation)

```java
// Enable/disable
void enableMultiValuePersistence(String namespace);
void disableMultiValuePersistence();

// Store/retrieve
void persistValue(String key, Object value);
Optional<String> getPersistedValue(String key);

// Clear
void clearPersistedValue(String key);
void clearAllPersistedValues();
```

## Implementation Example: Kanban Board

### 1. Basic Setup

```java
public class CComponentKanbanBoard extends Component 
        implements IHasMultiValuePersistence {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
    private String persistenceNamespace;
    private boolean persistenceEnabled;
    
    // Required getters/setters
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    @Override
    public String getPersistenceNamespace() {
        return persistenceNamespace;
    }
    
    @Override
    public void setPersistenceNamespace(String namespace) {
        this.persistenceNamespace = namespace;
    }
    
    @Override
    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }
    
    @Override
    public void setPersistenceEnabled(boolean enabled) {
        this.persistenceEnabled = enabled;
    }
}
```

### 2. Enable Persistence with Project Context

```java
public void setProject(CProject project) {
    this.project = project;
    
    // Enable persistence with project-specific namespace
    String namespace = "kanbanBoard_project" + project.getId();
    enableMultiValuePersistence(namespace);
}
```

### 3. Persist State on User Actions

```java
public void onColumnSelected(CKanbanColumn column) {
    selectedColumn = column;
    
    // Persist selected column ID
    if (column != null) {
        persistValue("selectedColumn", column.getId());
    } else {
        clearPersistedValue("selectedColumn");
    }
}

public void onFilterChanged(String filterName, String filterValue) {
    filters.put(filterName, filterValue);
    
    // Persist each filter value
    persistValue("filter_" + filterName, filterValue);
}

public void onSectionExpanded(String sectionId, boolean expanded) {
    if (expanded) {
        expandedSections.add(sectionId);
    } else {
        expandedSections.remove(sectionId);
    }
    
    // Persist expanded sections as comma-separated string
    String expandedStr = String.join(",", expandedSections);
    persistValue("expandedSections", expandedStr);
}
```

### 4. Restore State on Component Attach

```java
@Override
protected void onPersistenceRestore() {
    // Restore selected column
    getPersistedValue("selectedColumn").ifPresent(columnIdStr -> {
        Long columnId = Long.parseLong(columnIdStr);
        CKanbanColumn column = findColumnById(columnId);
        if (column != null) {
            selectColumn(column);
        }
    });
    
    // Restore filter values
    getPersistedValue("filter_status").ifPresent(this::applyStatusFilter);
    getPersistedValue("filter_assignee").ifPresent(this::applyAssigneeFilter);
    
    // Restore expanded sections
    getPersistedValue("expandedSections").ifPresent(expandedStr -> {
        if (!expandedStr.isEmpty()) {
            String[] sections = expandedStr.split(",");
            for (String sectionId : sections) {
                expandSection(sectionId);
            }
        }
    });
    
    // Restore scroll position (if needed)
    getPersistedValue("scrollPosition").ifPresent(scrollStr -> {
        int scrollPosition = Integer.parseInt(scrollStr);
        setScrollPosition(scrollPosition);
    });
}
```

## Common Use Cases

### 1. Selected Item Persistence

```java
// When item is selected
public void onItemSelected(Long itemId) {
    persistValue("selectedItem", itemId);
}

// On restore
getPersistedValue("selectedItem").ifPresent(idStr -> {
    Long id = Long.parseLong(idStr);
    selectItemById(id);
});
```

### 2. Multiple Filter Values

```java
// Store multiple filters
public void setFilters(Map<String, String> filters) {
    filters.forEach((name, value) -> 
        persistValue("filter_" + name, value));
}

// Restore filters
protected void onPersistenceRestore() {
    // You need to know which filter keys exist
    for (String filterName : getAvailableFilterNames()) {
        getPersistedValue("filter_" + filterName)
            .ifPresent(value -> applyFilter(filterName, value));
    }
}
```

### 3. Column Visibility

```java
// Store which columns are visible
public void setColumnVisible(String columnKey, boolean visible) {
    persistValue("column_" + columnKey + "_visible", 
                 String.valueOf(visible));
}

// Restore visibility
getPersistedValue("column_name_visible").ifPresent(visibleStr -> {
    boolean visible = Boolean.parseBoolean(visibleStr);
    setColumnVisible("name", visible);
});
```

### 4. Complex Object Serialization

```java
// Custom serialization for complex objects
public void persistKanbanState(KanbanState state) {
    // Option 1: JSON serialization
    String json = new Gson().toJson(state);
    persistValue("kanbanState", json);
    
    // Option 2: Multiple values
    persistValue("state_selectedColumn", state.getSelectedColumn());
    persistValue("state_filterValue", state.getFilterValue());
    persistValue("state_sortOrder", state.getSortOrder());
}

// Custom deserialization
protected void onPersistenceRestore() {
    // Option 1: From JSON
    getPersistedValue("kanbanState").ifPresent(json -> {
        KanbanState state = new Gson().fromJson(json, KanbanState.class);
        applyKanbanState(state);
    });
    
    // Option 2: From multiple values
    KanbanState state = new KanbanState();
    getPersistedValue("state_selectedColumn")
        .ifPresent(state::setSelectedColumn);
    getPersistedValue("state_filterValue")
        .ifPresent(state::setFilterValue);
    getPersistedValue("state_sortOrder")
        .ifPresent(state::setSortOrder);
    applyKanbanState(state);
}
```

## Storage Keys Best Practices

### 1. Use Descriptive Keys

```java
// ✅ GOOD - Clear purpose
persistValue("selectedColumn", columnId);
persistValue("filter_status", statusValue);
persistValue("sort_direction", "ASC");

// ❌ BAD - Unclear purpose
persistValue("col", columnId);
persistValue("f1", statusValue);
persistValue("dir", "ASC");
```

### 2. Use Consistent Prefixes for Related Values

```java
// ✅ GOOD - Grouped by prefix
persistValue("filter_status", "Active");
persistValue("filter_assignee", "user123");
persistValue("filter_priority", "High");

// ✅ GOOD - Grouped by domain
persistValue("column_name_visible", "true");
persistValue("column_status_visible", "true");
persistValue("column_date_visible", "false");
```

### 3. Choose Appropriate Namespaces

```java
// ✅ GOOD - Project-specific
enableMultiValuePersistence("kanbanBoard_project" + projectId);

// ✅ GOOD - View-specific
enableMultiValuePersistence("activitiesView_user" + userId);

// ❌ BAD - Too generic (can conflict)
enableMultiValuePersistence("kanban");
```

## Comparison with Single-Value Persistence

| Feature | IHasValuePersistence | IHasMultiValuePersistence |
|---------|---------------------|---------------------------|
| **Use Case** | Single value (e.g., ComboBox selection) | Multiple related values (e.g., component state) |
| **Storage** | One key = one value | Namespace + multiple keys |
| **Type Safety** | Generic type `<T>` | String-based, custom serialization |
| **Simplicity** | Automatic save on value change | Manual save/restore control |
| **Best For** | Form fields, simple selections | Complex components, state management |

### When to Use Each

**Use IHasValuePersistence (`<T>`) when:**
- Component has ONE primary value (ComboBox, TextField, CheckBox)
- Automatic save on every change is desired
- Type-safe serialization is important

**Use IHasMultiValuePersistence when:**
- Component needs to persist MULTIPLE values
- Component needs full control over save/restore timing
- Complex state needs custom serialization
- Examples: Kanban board, filter toolbar, grid with multiple settings

## Testing

```java
@Test
public void testMultiValuePersistence() {
    CComponentKanbanBoard board = new CComponentKanbanBoard();
    board.enableMultiValuePersistence("test_kanban");
    
    // Persist values
    board.persistValue("selectedColumn", "123");
    board.persistValue("filter_status", "Active");
    
    // Verify stored
    assertEquals("123", board.getPersistedValue("selectedColumn").orElse(null));
    assertEquals("Active", board.getPersistedValue("filter_status").orElse(null));
    
    // Clear value
    board.clearPersistedValue("selectedColumn");
    assertFalse(board.getPersistedValue("selectedColumn").isPresent());
    
    // Disable and verify cleared
    board.disableMultiValuePersistence();
    assertFalse(board.isPersistenceEnabled());
}
```

## Summary

`IHasMultiValuePersistence` provides a flexible, namespace-based pattern for persisting complex component state:

✅ **Benefits:**
- Store multiple related values under one namespace
- Full control over serialization/deserialization
- Automatic restoration on component attach
- Clean separation of persistence logic
- Type-safe through custom conversion methods

✅ **Best Practices:**
- Use descriptive, prefixed keys
- Choose unique namespaces
- Implement custom serialization for complex objects
- Override `onPersistenceRestore()` for automatic state restoration
- Clear values when component state is reset

✅ **Perfect For:**
- Kanban boards (selected column, filters, expanded sections)
- Grid components (column visibility, sort order, page size)
- Filter toolbars (multiple filter values)
- Complex forms (tab selection, section expansion state)
