# Grid Column and Container Width Expansion Fix

## Problem Statement
The backlog component grid was correctly expanding its columns according to its container width, and its container was also expanding the width. However, other component grids were not expanding in width together with their grid columns.

## Root Cause Analysis
The issue was in `CComponentListEntityBase.createGrid()` method which creates grids for various list-based components (sprint items, detail lines, etc.). The method was missing the `setWidthFull()` call that enables the grid to expand horizontally with its container.

### Working Example (CComponentEntitySelection)
```java
protected void create_gridItems() {
    final CGrid rawGrid = new CGrid<>(Object.class);
    grid = rawGrid;
    // Configure size
    grid.setSizeFull(); // Grid should expand ✅
    grid.setHeightFull(); // Ensure full height expansion ✅
    grid.setMinHeight("120px");
    // ... rest of configuration
}
```

### Problem Code (CComponentListEntityBase - BEFORE)
```java
protected void createGrid() {
    grid = new CGrid<>(entityClass);
    grid.setSelectionMode(CGrid.SelectionMode.SINGLE);
    // Configure height - if dynamic height enabled, use content-based sizing
    if (useDynamicHeight) {
        grid.setDynamicHeight();
    } else {
        grid.setHeightFull();
        grid.setMinHeight("120px");
    }
    // ❌ Missing: grid.setWidthFull() 
    // Grid could not expand horizontally with container
    configureGrid(grid);
    // ...
}
```

## Solution Implemented
Added `grid.setWidthFull()` to enable horizontal expansion:

```java
protected void createGrid() {
    grid = new CGrid<>(entityClass);
    grid.setSelectionMode(CGrid.SelectionMode.SINGLE);
    // Configure size - grid should expand to fill container width
    grid.setWidthFull(); // ✅ Enable grid to expand horizontally with container
    // Configure height - if dynamic height enabled, use content-based sizing
    if (useDynamicHeight) {
        grid.setDynamicHeight();
    } else {
        grid.setHeightFull();
        grid.setMinHeight("120px");
    }
    configureGrid(grid);
    // ...
}
```

## Impact
This fix affects all components that extend `CComponentListEntityBase`, including:
- `CComponentListSprintItems` - Sprint items management
- `CComponentListDetailLines` - Detail lines management
- Any future components extending this base class

## Technical Details
- **File Changed**: `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListEntityBase.java`
- **Lines Modified**: Line 216-217 (added `setWidthFull()` call with comment)
- **Pattern Applied**: Same pattern used in `CComponentEntitySelection` which was already working correctly

## Grid Sizing Behavior
With this fix:
1. Container (VerticalLayout) has `setWidthFull()` (already present in `initializeComponents()`)
2. Grid now also has `setWidthFull()` (newly added)
3. Grid columns configured with `flexGrow(1)` or `addExpandingColumn()` methods will expand to fill available width
4. Result: Grid and its columns expand horizontally with the container width

## Related Memory
This follows the grid column expansion pattern documented in repository memories:
- Use `addExpandingLongTextColumn()` or `addExpandingShortTextColumn()` for columns that should fill remaining grid width
- These methods use `flexGrow(1)` without fixed width
- Now that grid has `setWidthFull()`, these expanding columns work correctly

## Verification Notes
The fix ensures consistency across all grid-based components:
- Backlog component (CComponentEntitySelection): Already had `setSizeFull()` ✅
- List entity components (CComponentListEntityBase descendants): Now have `setWidthFull()` ✅
- Relation components (CComponentRelationBase): Uses `CGrid.setupGrid()` which calls `setWidthFull()` ✅

All grid-based components now properly expand with their containers.
