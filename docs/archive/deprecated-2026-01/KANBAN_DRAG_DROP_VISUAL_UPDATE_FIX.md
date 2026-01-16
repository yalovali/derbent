# Kanban Drag-Drop Visual Update Fix

## Problem Statement

**Issue**: When dragging a component (kanban card) to another column in the kanban board, the changes are successfully saved to the database but the UI doesn't reflect the changes until the user manually refreshes the entire page.

**User Impact**: This creates a confusing user experience where:
- Drag operation appears to succeed (no error message)
- Item visually "snaps back" to original column
- Item only appears in new column after full page refresh
- Users don't get immediate visual feedback for their actions

## Root Cause Analysis

### The Problem

The kanban board maintains an in-memory list of sprint items (`sprintItems` field) that is loaded from the database and used to render the UI. When a drag-drop operation occurs:

1. The drag-drop handler (`handleKanbanDrop` in `CPageServiceKanbanLine`) receives a `CSprintItem` object from the drag event
2. This object is modified (e.g., `sprintItem.setKanbanColumnId(targetColumn.getId())`)
3. The changes are saved to the database
4. The board's `refreshComponent()` method is called to update the UI

**However**, the `CSprintItem` object being modified is **NOT** the same instance as the objects in the board's `sprintItems` list. The board's list contains copies created when data was loaded from the database.

### Why This Happens

```java
// In CComponentKanbanBoard.loadSprintItemsForSprint()
final List<CSprintItem> sprintItemsRaw = sprintItemService.findByMasterIdWithItems(sprint.getId());
allSprintItems = new ArrayList<>(sprintItemsRaw);  // Creates a new ArrayList
sprintItems = new ArrayList<>(allSprintItems);      // Creates another copy
```

The `sprintItems` list is a **copy** of the database results. When the drag-drop handler modifies a `CSprintItem`, it modifies a different object instance that came from the drag event, not the instance in the board's list.

### The Consequence

When `refreshComponent()` is called:
```java
// In CComponentKanbanBoard.refreshComponent()
assignKanbanColumns(sprintItems, columns);  // Uses the OLD, stale list
```

The stale list still has the old `kanbanColumnId` values, so the UI renders the items in their original columns, even though the database has been updated with new values.

## Solution

### Approach

Reload the sprint items from the database after saving, then refresh the UI with the fresh data.

### Implementation

#### 1. Added `reloadSprintItems()` Method

```java
// In CComponentKanbanBoard
public void reloadSprintItems() {
    LOGGER.debug("Reloading sprint items from database for Kanban board");
    if (currentSprint != null && currentSprint.getId() != null) {
        loadSprintItemsForSprint(currentSprint);
        // Reapply filters to maintain filter state after reload
        final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria = filterToolbar.getCurrentCriteria();
        sprintItems = filterSprintItems(criteria);
    }
}
```

This method:
- Reloads sprint items from the database (fresh data with updated `kanbanColumnId` values)
- Reapplies active filters to preserve the user's filter selection
- Updates the board's `sprintItems` list with the fresh filtered data

#### 2. Extracted `filterSprintItems()` Method

To avoid code duplication, the filter logic was extracted into a shared method:

```java
private List<CSprintItem> filterSprintItems(final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria) {
    final List<CSprintItem> filtered = new ArrayList<>();
    for (final CSprintItem sprintItem : allSprintItems) {
        if (sprintItem == null || sprintItem.getItem() == null) {
            continue;
        }
        if (!matchesTypeFilter(sprintItem, criteria.getEntityType())) {
            continue;
        }
        if (!matchesResponsibleFilter(sprintItem, criteria)) {
            continue;
        }
        filtered.add(sprintItem);
    }
    return filtered;
}
```

#### 3. Updated All Drag-Drop Save Paths

**Path 1: No Valid Status Transition**
```java
// In CPageServiceKanbanLine.handleKanbanDrop()
if (targetStatuses.isEmpty()) {
    saveSprintItemOnly(sprintItem);
    
    // FIX: Reload before refresh
    componentKanbanBoard.reloadSprintItems();
    componentKanbanBoard.refreshComponent();
    
    CNotificationService.showWarning(...);
}
```

**Path 2: Single Valid Status Transition**
```java
// In CPageServiceKanbanLine.applyStatusAndSave()
projectItemService.revokeSave(item);

// FIX: Reload before refresh
componentKanbanBoard.reloadSprintItems();
componentKanbanBoard.refreshComponent();
```

**Path 3: Multiple Valid Status Transitions (Dialog Callback)**
```java
// In CPageServiceKanbanLine.showStatusSelectionDialog()
if (selectedStatus != null) {
    applyStatusAndSave(item, sprintItem, selectedStatus);  // Already has reload
} else {
    saveSprintItemOnly(sprintItem);
    
    // FIX: Reload before refresh
    componentKanbanBoard.reloadSprintItems();
    componentKanbanBoard.refreshComponent();
}
```

## Performance Considerations

### Database Queries
- **Before Fix**: 0 queries after drag-drop (used stale in-memory data)
- **After Fix**: 1 query after drag-drop (`findByMasterIdWithItems`)

This is acceptable because:
- Only one query per user action (not excessive)
- Query is necessary to get fresh data
- Alternative would be complex in-memory list management (error-prone)

### UI Refreshes
- **No excessive refreshes**: Only one `refreshComponent()` call per drag-drop
- **No render loops**: Reload doesn't trigger filter change events
- **Efficient rendering**: Vaadin only updates changed DOM elements

## Code Quality Improvements

### DRY Principle
Extracted duplicated filter logic into shared `filterSprintItems()` method, used by:
- `applyFilters()` - when user changes filter settings
- `reloadSprintItems()` - after drag-drop to preserve filter state

### Separation of Concerns
- `loadSprintItemsForSprint()` - loads data from database
- `filterSprintItems()` - applies filter criteria to loaded data
- `reloadSprintItems()` - combines reload + filter for post-save updates
- `refreshComponent()` - updates UI with current data

### Documentation
Added comprehensive Javadoc explaining:
- When each method should be called
- Why reload is necessary (stale data issue)
- What filters are applied

## Testing Strategy

### Test Scenarios

1. **Single Valid Status Transition**
   - Drag item from "To Do" column to "In Progress" column
   - Expected: Item immediately appears in "In Progress" column
   - Expected: Status automatically changes to "In Progress"
   - Expected: Success notification shown

2. **Multiple Valid Status Transitions**
   - Drag item to column with multiple valid status options
   - Expected: Dialog appears with status choices
   - Expected: After selection, item appears in new column with selected status
   - Expected: If cancelled, item appears in new column with unchanged status

3. **No Valid Status Transition**
   - Drag item to column with no valid workflow transition
   - Expected: Item appears in new column (visual change)
   - Expected: Status remains unchanged
   - Expected: Warning notification shown

4. **With Active Filters**
   - Apply type filter (e.g., show only "Activities")
   - Drag item between columns
   - Expected: Item appears in new column
   - Expected: Filter remains active (other types still hidden)

5. **Multiple Drag-Drops**
   - Perform several drag-drop operations in sequence
   - Expected: Each operation updates UI immediately
   - Expected: No performance degradation

### Manual Testing Checklist

- [ ] Test all three drag-drop scenarios (single status, multiple statuses, no valid status)
- [ ] Test with type filter active
- [ ] Test with responsible user filter active
- [ ] Test with both filters active
- [ ] Perform 5-10 consecutive drag-drops without issues
- [ ] Check browser console for errors
- [ ] Check server logs for errors or warnings
- [ ] Verify no excessive database queries (should be 1 per drag-drop)

## Files Changed

### 1. CComponentKanbanBoard.java
**Location**: `src/main/java/tech/derbent/app/kanban/kanbanline/view/CComponentKanbanBoard.java`

**Changes**:
- Added `reloadSprintItems()` method (lines 501-516)
- Extracted `filterSprintItems()` method (lines 158-175)
- Updated `applyFilters()` to use `filterSprintItems()` (lines 144-156)

**Impact**: Provides mechanism to reload data from database and reapply filters

### 2. CPageServiceKanbanLine.java
**Location**: `src/main/java/tech/derbent/app/kanban/kanbanline/service/CPageServiceKanbanLine.java`

**Changes**:
- Updated `handleKanbanDrop()` - added reload before refresh (line 160)
- Updated `applyStatusAndSave()` - added reload before refresh (lines 215, 225)
- Updated `showStatusSelectionDialog()` callback - added reload before refresh (line 299)

**Impact**: Ensures UI always displays fresh data after drag-drop save

## Security Analysis

✅ **CodeQL Scan**: No security vulnerabilities found

The changes:
- Don't introduce SQL injection risks (using parameterized queries via JPA)
- Don't expose sensitive data (same data access as before)
- Don't create authentication/authorization issues (same permissions as before)
- Don't introduce XSS risks (no new user input rendering)

## Migration Notes

### Backward Compatibility
✅ **Fully backward compatible** - no API changes, only internal behavior improvements

### Database Changes
❌ **No database migrations required** - no schema changes

### Configuration Changes
❌ **No configuration changes required** - works with existing configuration

## Conclusion

This fix resolves the visual update issue by ensuring the kanban board always displays fresh data from the database after drag-drop operations. The solution:

- ✅ Fixes the core issue (immediate visual feedback)
- ✅ Preserves filter state (good UX)
- ✅ Minimal code changes (low risk)
- ✅ No code duplication (DRY principle)
- ✅ Well documented (maintainable)
- ✅ No security issues (CodeQL clean)
- ✅ Acceptable performance (1 query per action)

### Next Steps
1. Deploy to test environment
2. Execute manual testing checklist
3. Monitor performance metrics
4. Deploy to production after validation
