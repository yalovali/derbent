# Grid Selection After Save Pattern

## Overview

When saving an entity in a dynamic page, the grid should maintain selection of the saved entity. This pattern ensures proper grid selection behavior without triggering infinite loops.

## Pattern Implementation

### Problem
After clicking Save, the grid refreshes but doesn't maintain selection on the saved entity. Instead, it may select a different row (often the next one after the newly inserted item).

### Solution
The `actionSave()` method calls `view.onEntitySaved(savedEntity)` which:
1. Refreshes the grid data
2. Checks if entity is already selected (to avoid loops)
3. Selects the saved entity if not already selected

## Code Flow

```
User clicks Save 
  → CPageService.actionSave()
    → Save entity via service
    → setCurrentEntity(savedEntity)
    → view.onEntitySaved(savedEntity)  // Key call
      → refreshGrid()
      → Check if already selected
      → grid.selectEntity(entity) if needed
    → view.populateForm()
    → Show success notification
```

## Implementation Details

### CPageService.actionSave()
```java
// Save entity
final EntityClass savedEntity = getEntityService().save(entity);
// Update current entity
setCurrentEntity(savedEntity);
// Notify view that entity was saved (triggers grid refresh and selection)
view.onEntitySaved(savedEntity);
// Populate form
view.populateForm();
```

### CDynamicPageViewWithSections.onEntitySaved()
```java
public void onEntitySaved(final CEntityDB<?> entity) {
    refreshGrid();
    // Only select if not already selected to avoid triggering selection change loop
    final CEntityDB<?> currentSelection = grid.getSelectedItem();
    if (currentSelection == null || !entity.getId().equals(currentSelection.getId())) {
        grid.selectEntity(entity);
        LOGGER.debug("Selected saved entity in grid: {}", entity.getId());
    } else {
        LOGGER.debug("Entity already selected in grid, skipping selection to avoid loop");
    }
}
```

## Key Points

### ✅ DO:
- **Call `view.onEntitySaved()`** from `actionSave()` to trigger proper grid update
- **Check current selection** before calling `grid.selectEntity()` 
- **Compare entity IDs** to determine if selection is needed
- **Log selection decisions** for debugging

### ❌ DON'T:
- Don't call `grid.selectEntity()` without checking current selection
- Don't call selection from multiple places (creates loops)
- Don't access grid directly from PageService (use view methods)

## Why This Prevents Loops

The selection change listener triggers when `grid.selectEntity()` is called. Without the check:

```
Save → grid.selectEntity()
  → SelectionChangeEvent fires
    → onEntitySelected() called
      → setCurrentEntity()
        → Possible save trigger
          → grid.selectEntity() again
            → LOOP!
```

With the check:
```
Save → grid.selectEntity()
  → SelectionChangeEvent fires
    → onEntitySelected() called
      → setCurrentEntity()
        → If saved again → onEntitySaved()
          → Check: already selected? YES
            → Skip grid.selectEntity()
              → No loop!
```

## Integration Points

This pattern integrates with:
- **IEntityUpdateListener**: View implements this interface
- **CComponentGridEntity**: Provides `getSelectedItem()` and `selectEntity()`
- **CPageService**: Calls `view.onEntitySaved()` after save
- **Grid selection events**: Triggers `onEntitySelected()`

## Testing

Test scenarios:
1. ✅ Create new entity → Save → Entity appears in grid and is selected
2. ✅ Edit existing entity → Save → Entity remains selected after grid refresh
3. ✅ Save twice without changing selection → No selection loop
4. ✅ Select different entity → Save → Newly saved entity gets selected

## Benefits

1. **User Experience**: User doesn't lose selection after save
2. **Consistency**: Same behavior across all dynamic pages
3. **No Loops**: Selection check prevents infinite loops
4. **Maintainable**: Pattern is centralized in base classes
5. **Debugging**: Clear logging of selection decisions

## Related Patterns

- **Entity Listener Pattern**: Uses `IEntityUpdateListener` interface
- **CRUD Operations**: Part of complete CRUD workflow
- **Grid Management**: Integrates with grid component lifecycle
