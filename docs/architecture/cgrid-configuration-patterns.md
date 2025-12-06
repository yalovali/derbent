# CGrid Configuration Patterns

## Overview

This document defines the standardized patterns for configuring CGrid instances across the Derbent application. Consistent grid configuration ensures maintainability, readability, and uniform user experience.

## IGridComponent Interface

All grid-based components should implement the `IGridComponent<T>` interface to ensure consistent behavior:

```java
public interface IGridComponent<T> {
    CGrid<T> getGrid();
    void refreshGrid();
    void clearGrid();
    void configureGrid(CGrid<T> grid);
}
```

**Benefits:**
- Standardized API across all grid components
- Easier to maintain and extend
- Type-safe grid operations
- Consistent method naming

## Standard Pattern

### 1. Grid Configuration Method

All components that use CGrid must implement the `configureGrid()` method from the `IGridComponent` interface:

```java
/**
 * Configures grid columns and appearance.
 * Implements IGridComponent.configureGrid()
 * @param grid The grid to configure (must not be null)
 */
@Override
public void configureGrid(final CGrid<EntityType> grid) {
    Check.notNull(grid, "Grid cannot be null");
    LOGGER.debug("Configuring grid columns for {}", entityClass.getSimpleName());
    
    // Column configuration goes here
}
```

**Key Points:**
- Method name must be `configureGrid`
- Must be `public` (interface requirement)
- Must have `@Override` annotation
- Must validate the grid parameter with `Check.notNull()`
- Should log debug information about configuration
- Use `final` modifier for parameters

### 2. Column Creation

Always use CGrid helper methods instead of raw `addColumn()` calls:

#### Standard Column Types

```java
// ID columns (width: 80px, no flex grow)
grid.addIdColumn(Entity::getId, "ID", "id");

// Integer columns (width: 100px, no flex grow)
grid.addIntegerColumn(Entity::getOrder, "Order", "order");

// Short text columns (width: 200px, no flex grow)
grid.addShortTextColumn(Entity::getName, "Name", "name");

// Long text columns (width: 300px, no flex grow)
grid.addLongTextColumn(Entity::getDescription, "Description", "description");

// Boolean columns (width: 100px, no flex grow)
grid.addBooleanColumn(Entity::getIsActive, "Active", "Yes", "No");

// Date columns (width: 150px, no flex grow)
grid.addDateColumn(Entity::getCreatedDate, "Created", "created");

// Decimal columns (width: 120px, no flex grow)
grid.addDecimalColumn(Entity::getAmount, "Amount", "amount");
```

#### Entity Columns with Color and Icon

For entity references that should display with color and icon:

```java
try {
    grid.addEntityColumn(
        item -> item.getStatus(),
        "Status",
        "status",
        EntityClass.class
    );
} catch (final Exception e) {
    LOGGER.error("Error adding status column: {}", e.getMessage(), e);
}
```

#### Component Columns

For custom component rendering:

```java
CGrid.styleColumnHeader(
    grid.addComponentColumn(item -> {
        try {
            return new CLabelEntity(item.getStatus());
        } catch (final Exception e) {
            LOGGER.error("Error creating status component: {}", e.getMessage());
            return new Span("Error");
        }
    }).setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setKey("status"),
    "Status"
);
```

### 3. Column Styling

- Always use `CGrid.styleColumnHeader()` for manual column creation
- Use predefined width constants from CGrid (WIDTH_ID, WIDTH_SHORT_TEXT, etc.)
- Set explicit widths with `.setWidth()` and flex grow with `.setFlexGrow()`
- Enable sorting with `.setSortable(true)` where appropriate
- Enable resizing with `.setResizable(true)` where appropriate
- Set column keys with `.setKey()` for programmatic access

### 4. Grid Initialization

#### Factory Method Pattern

```java
/**
 * Factory method for grid following standard pattern.
 */
protected void create_gridItems() {
    Check.isTrue(grid == null, "Grid should only be created once");
    
    grid = new CGrid<>(entityClass);
    
    // Configure size
    grid.setSizeFull();
    grid.setHeightFull();
    grid.setMinHeight("120px");
    
    // Configure selection mode
    grid.setSelectionMode(CGrid.SelectionMode.SINGLE);
    
    // Add listeners
    grid.asSingleSelect().addValueChangeListener(e -> on_gridItems_selected(e.getValue()));
    grid.addItemDoubleClickListener(e -> on_gridItems_doubleClicked(e.getItem()));
    
    // Configure columns
    configureGrid(grid);
    
    LOGGER.debug("Grid created and configured for {}", entityClass.getSimpleName());
}
```

#### Using CGrid.setupGrid()

For components that need common grid setup:

```java
protected void createGrid() {
    grid = new CGrid<>(entityClass);
    CGrid.setupGrid(grid);  // Applies common styling and behavior
    grid.setHeight(DEFAULT_GRID_HEIGHT);
    configureGrid(grid);
}
```

## Examples

### Example 1: Simple Entity List (CComponentListSprintItems)

```java
public class CComponentListSprintItems extends CComponentListEntityBase<CSprint, CSprintItem>
        implements IEntitySelectionDialogSupport<CProjectItem<?>>, IDropTarget<CProjectItem<?>> {
    
    @Override
    public void configureGrid(final CGrid<CSprintItem> grid) {
        Check.notNull(grid, "Grid cannot be null");
        LOGGER.debug("Configuring grid columns for CSprintItem");
        
        // Use CGrid helper methods for consistent column creation
        grid.addIdColumn(CSprintItem::getId, "ID", "id");
        grid.addIntegerColumn(CSprintItem::getItemOrder, "Order", "order");
        grid.addShortTextColumn(CSprintItem::getItemType, "Type", "type");
        grid.addShortTextColumn(item -> {
            if (item.getItem() != null) {
                return item.getItem().getName();
            }
            return "Item " + item.getItemId();
        }, "Name", "name");
        
        // Use addEntityColumn for status with color and icon
        try {
            grid.addEntityColumn(
                item -> item.getItem().getStatus(),
                "Status",
                "status",
                CSprintItem.class
            );
        } catch (final Exception e) {
            LOGGER.error("Error adding status column: {}", e.getMessage(), e);
        }
    }
}
```

### Example 2: Entity Selection Grid (CComponentEntitySelection)

```java
public class CComponentEntitySelection<EntityClass extends CEntityDB<?>> extends Composite<CVerticalLayout>
        implements IGridComponent<EntityClass> {
    
    @Override
    public void configureGrid(final CGrid<EntityClass> grid) {
        Check.notNull(grid, "Grid cannot be null");
        
        // Clear existing columns
        grid.getColumns().forEach(grid::removeColumn);
        
        if (currentEntityType == null) {
            return;
        }
        
        LOGGER.debug("Configuring grid columns for entity type: {}", 
                     currentEntityType.getDisplayName());
        
        grid.addIdColumn(item -> item.getId(), "ID", "id");
        grid.addShortTextColumn(this::getEntityName, "Name", "name");
        grid.addLongTextColumn(this::getEntityDescription, "Description", "description");
        
        CGrid.styleColumnHeader(grid.addComponentColumn(item -> {
            try {
                return new CLabelEntity(((IHasStatusAndWorkflow) item).getStatus());
            } catch (final Exception e) {
            LOGGER.error("Error rendering status: {}", e.getMessage());
            return new CLabelEntity("Error");
        }
        }).setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setKey("status"), 
        "Status");
    }
}
```

### Example 3: Using Standard Methods from IGridComponent

```java
// Accessing the grid
CGrid<EntityType> grid = component.getGrid();

// Refreshing data
component.refreshGrid();

// Clearing the grid
component.clearGrid();

// All components implementing IGridComponent have consistent behavior
```

## Common Mistakes to Avoid

### ❌ DON'T: Use protected instead of public for configureGrid

```java
// INCORRECT - Violates IGridComponent interface
@Override
protected void configureGrid(CGrid<Entity> grid) { ... }
```

### ✅ DO: Use public as required by interface

```java
// CORRECT
@Override
public void configureGrid(final CGrid<Entity> grid) { ... }
```

### ❌ DON'T: Use raw addColumn with manual styling

```java
// INCORRECT
CGrid.styleColumnHeader(grid.addColumn(Entity::getId).setWidth("80px"), "Id");
CGrid.styleColumnHeader(grid.addColumn(Entity::getName).setAutoWidth(true), "Name");
```

### ✅ DO: Use CGrid helper methods

```java
// CORRECT
grid.addIdColumn(Entity::getId, "ID", "id");
grid.addShortTextColumn(Entity::getName, "Name", "name");
```

### ❌ DON'T: Name method configureGridColumns or setupColumns

```java
// INCORRECT
private void configureGridColumns() { ... }
private void setupColumns() { ... }
```

### ✅ DO: Use standard configureGrid name

```java
// CORRECT
protected void configureGrid(final CGrid<Entity> grid) { ... }
```

### ❌ DON'T: Add duplicate columns

```java
// INCORRECT
grid.addShortTextColumn(Entity::getType, "Type", "type");
// ... other columns ...
grid.addShortTextColumn(Entity::getType, "Type", "type"); // Duplicate!
```

### ✅ DO: Review columns before adding

```java
// CORRECT - Each column added once
grid.addIdColumn(Entity::getId, "ID", "id");
grid.addShortTextColumn(Entity::getType, "Type", "type");
grid.addShortTextColumn(Entity::getName, "Name", "name");
```

### ❌ DON'T: Skip parameter validation

```java
// INCORRECT
protected void configureGrid(CGrid<Entity> grid) {
    grid.addIdColumn(...); // Could fail if grid is null
}
```

### ✅ DO: Always validate parameters

```java
// CORRECT
protected void configureGrid(final CGrid<Entity> grid) {
    Check.notNull(grid, "Grid cannot be null");
    LOGGER.debug("Configuring grid...");
    grid.addIdColumn(...);
}
```

## Component Hierarchy

Components implementing IGridComponent interface:

- **CComponentListEntityBase** - Abstract base for entity list components (implements IGridComponent)
  - CComponentListSprintItems
  - CComponentListDetailLines
  
- **CComponentEntitySelection** - Entity selection component (implements IGridComponent)

Other grid-based components (may be updated to implement IGridComponent in the future):
- **CComponentFieldSelection** - Field selection component
- **CComponentListSelection** - List selection component

## Related Documentation

- [Coding Standards](coding-standards.md) - General coding guidelines
- [View Layer Patterns](view-layer-patterns.md) - View component patterns
- [Entity Selection Component Design](entity-selection-component-design.md) - Entity selection patterns

## Version History

- **2025-12-06**: Added IGridComponent interface
  - Created IGridComponent interface for standard grid operations
  - Updated CComponentListEntityBase to implement IGridComponent
  - Updated CComponentEntitySelection to implement IGridComponent
  - Changed configureGrid() visibility to public (interface requirement)
  - Added getGrid(), refreshGrid(), clearGrid() standard methods
  - Deprecated old method names (getGridItems, refresh) for consistency

- **2025-12-05**: Initial standardization
  - Renamed configureGridColumns() to configureGrid()
  - Standardized column creation with CGrid helper methods
  - Added parameter validation requirements
  - Documented common patterns and examples
