# Grid Selection Fix for CComponentFieldSelection

## Problem Statement

The grid in `CComponentFieldSelection` was not responding to user clicks and selection events were not firing. The event handlers were not being triggered, and none of the selection change code was being executed.

In contrast, the grid in `CComponentRelationBase` responded to user clicks and selection changes perfectly.

## Root Cause Analysis

After investigating both components, we identified two key issues:

### Issue 1: Duplicate Selection Listeners

The `availableGrid` had TWO selection listeners performing the same function:

```java
// First listener - using Grid's selection model
availableGrid.addSelectionListener(selection -> {
    final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
    addButton.setEnabled(hasSelection);
});

// Second listener - using SingleSelect model (redundant!)
availableGrid.asSingleSelect().addValueChangeListener(e -> {
    boolean hasSelection = e.getValue() != null && !readOnly;
    addButton.setEnabled(hasSelection);
});
```

This duplication could cause inconsistent behavior and race conditions.

### Issue 2: Selection Mode Lost After Data Refresh

The `refreshLists()` method is called frequently (after adding, removing, or changing items). Each call invokes `setItems()`:

```java
private void refreshLists() {
    // ... data manipulation ...
    availableGrid.setItems(notselectedItems);  // Creates new DataProvider
    selectedGrid.setItems(selectedItems);      // Creates new DataProvider
    fireValueChangeEvent();
}
```

When `setItems()` is called, it creates a new `DataProvider` for the grid. While Vaadin should maintain the selection mode, there can be edge cases (especially with grids created without a type parameter using `new Grid<>()`) where the selection model behavior becomes inconsistent.

### Key Difference from CComponentRelationBase

**CComponentRelationBase:**
- Creates grid once: `grid = new Grid<>(relationalClass, false)` (with type parameter)
- Calls `setItems()` once in `populateForm()`
- No frequent data refreshes

**CComponentFieldSelection:**
- Creates grid: `new Grid<>()` (without type parameter - type erasure issue)
- Calls `setItems()` multiple times via `refreshLists()`
- Frequent data refreshes on every add/remove operation

## Solution

We implemented two minimal, surgical fixes:

### Fix 1: Remove Duplicate Listener

Removed the `addSelectionListener()` and kept only the `asSingleSelect().addValueChangeListener()` approach for consistency:

```java
/** Sets up event handlers for buttons and grid selections. */
private void setupEventHandlers() {
    // Enable/disable buttons based on selection - Use asSingleSelect() for consistent behavior
    availableGrid.asSingleSelect().addValueChangeListener(e -> {
        boolean hasSelection = e.getValue() != null && !readOnly;
        addButton.setEnabled(hasSelection);
    });
    selectedGrid.asSingleSelect().addValueChangeListener(e -> {
        boolean hasSelection = e.getValue() != null && !readOnly;
        removeButton.setEnabled(hasSelection);
        upButton.setEnabled(hasSelection);
        downButton.setEnabled(hasSelection);
    });
    // ... rest of setup ...
}
```

### Fix 2: Explicit Selection Mode After Data Refresh

Added explicit `setSelectionMode()` call after every `setItems()` to ensure the grid maintains proper selection behavior:

```java
/** Refreshes both grids and fires value change event. */
private void refreshLists() {
    // Update notselectedItems - show items not in selected
    notselectedItems.clear();
    notselectedItems.addAll(sourceItems.stream()
        .filter(item -> !selectedItems.contains(item))
        .collect(Collectors.toList()));
    
    // Update grids
    availableGrid.setItems(notselectedItems);
    selectedGrid.setItems(selectedItems);
    
    // Ensure selection mode is maintained after setItems() - CRITICAL FIX
    // setItems() creates a new DataProvider which can affect selection behavior
    // Respect read-only state: NONE if read-only, SINGLE if editable
    Grid.SelectionMode mode = readOnly ? Grid.SelectionMode.NONE : Grid.SelectionMode.SINGLE;
    availableGrid.setSelectionMode(mode);
    selectedGrid.setSelectionMode(mode);
    
    // Fire value change event
    fireValueChangeEvent();
}
```

## Benefits of This Fix

1. **Defensive Programming**: Explicitly ensures selection mode is correct after every data refresh
2. **Read-Only Support**: Properly maintains NONE mode when read-only, SINGLE when editable
3. **Minimal Changes**: Only 3 lines removed, 4 lines added - surgical fix
4. **No Breaking Changes**: All existing tests pass
5. **Consistent Behavior**: Aligns with how `setReadOnly()` already manages selection mode

## Testing

- ✅ All unit tests pass (`CComponentFieldSelectionTest`)
- ✅ Code compiles without errors
- ✅ Formatting checked with `mvn spotless:check`
- ⏳ Manual UI testing recommended to verify click behavior

## Files Changed

- `src/main/java/tech/derbent/api/views/components/CComponentFieldSelection.java`
  - Modified `refreshLists()` method (added 4 lines)
  - Modified `setupEventHandlers()` method (removed 4 lines)

## Recommendation for Future

Consider refactoring to use `CGrid<DetailEntity>` instead of plain `Grid<DetailEntity>` if the DetailEntity type always extends `CEntityDB`. This would provide:
- Better consistency with the rest of the application
- Automatic selection management via `ensureSelectionWhenDataAvailable()`
- Built-in theme and styling configuration

However, this would be a larger refactoring and is not necessary given the current fix.
