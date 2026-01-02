# Kanban Board and Component Refresh Patterns - Coding Standard

## Overview

This document establishes coding standards for kanban board operations, component refresh mechanisms, and sprint item management to ensure optimal performance and correct behavior.

## 1. Sprint Item Lifecycle Management

### Rule: Items Dropped to Backlog Must Be Reset

When a sprint item is removed from the sprint (dropped to backlog), **ALL** sprint-related state must be cleared:

**MANDATORY Reset Actions:**
1. **Status**: Set to `null` - item is no longer in any workflow state
2. **Sprint Item Reference**: Clear the `sprintItem` field
3. **Sprint Item Record**: Delete the `CSprintItem` database record

```java
// ✅ CORRECT: Complete state reset when moving to backlog
private void handleDropOnBacklog(final CSprintItem sprintItem) {
    // Get underlying item before deletion
    final ISprintableItem item = sprintItem.getItem();
    Objects.requireNonNull(item, "Sprint item must have an underlying item");
    
    // Reset ALL sprint-related state
    item.setStatus(null);              // 1. Clear workflow status
    item.setSprintItem(null);          // 2. Clear sprint reference
    
    // Save the reset state
    final CProjectItemService<?> service = getProjectItemService(item);
    service.revokeSave((CProjectItem<?>) item);
    
    // Delete sprint membership
    sprintItemService.delete(sprintItem);
    
    LOGGER.info("Item {} removed from sprint and reset to backlog state", item.getId());
}

// ❌ WRONG: Only deleting sprint item without resetting item state
private void handleDropOnBacklog(final CSprintItem sprintItem) {
    sprintItemService.delete(sprintItem);  // WRONG: Status remains, state not reset
}
```

**Rationale**: When an item returns to backlog, it should be in a "clean" state - not locked into any workflow status, sprint, or column assignment. This allows proper re-planning and status assignment when added to a future sprint.

### Rule: Sprint Item Deletion Cascades

When `CSprintItem` is deleted, ensure no orphaned references remain:

1. Check `item.getSprintItem()` is cleared
2. Verify no dangling foreign keys
3. Log the complete operation for debugging

## 2. Component Refresh Optimization

### Problem: Excessive Refresh Calls

**Anti-Pattern**: Multiple refresh triggers causing duplicate operations:
- Setting items triggers refresh
- Setting value triggers another refresh
- Each refresh filters items multiple times
- CPU-intensive with large datasets

### Solution: Intelligent Refresh Control

#### Pattern A: Deferred Refresh Until Fully Initialized

```java
// ✅ CORRECT: Set value BEFORE items to avoid double refresh
public void setupComponent() {
    final CComponentKanbanColumn column = new CComponentKanbanColumn();
    
    // 1. Set configuration (value) first
    column.setValue(kanbanColumn);
    
    // 2. Then set data - this triggers single refresh
    column.setItems(sprintItems);
}

// ❌ WRONG: Items then value causes double refresh
public void setupComponent() {
    final CComponentKanbanColumn column = new CComponentKanbanColumn();
    
    // Setting items triggers refresh #1 (but column value is null)
    column.setItems(sprintItems);
    
    // Setting value triggers refresh #2 (items already set)
    column.setValue(kanbanColumn);
}
```

#### Pattern B: Guard Against Premature Refresh

```java
// ✅ CORRECT: Only refresh when fully initialized
public void setItems(final List<T> items) {
    this.items = items;
    invalidateCache();
    
    // Only refresh if component is ready (value is set)
    if (getValue() != null) {
        refreshItems();
    }
}

protected void onValueChanged(final T oldValue, final T newValue) {
    binder.setBean(newValue);
    invalidateCache();
    
    // Only refresh if we have data (items are set)
    if (!items.isEmpty()) {
        refreshItems();
    }
}
```

#### Pattern C: Cache Filtered Results

```java
// ✅ CORRECT: Cache filtered items to avoid repeated filtering
public class CComponentKanbanColumn {
    private List<CSprintItem> sprintItems = List.of();
    private List<CSprintItem> cachedFilteredItems = List.of();
    
    private List<CSprintItem> filterItems(final List<CSprintItem> items) {
        // Expensive filtering operation
        return items.stream()
            .filter(item -> item.getKanbanColumnId().equals(column.getId()))
            .toList();
    }
    
    private List<CSprintItem> getFilteredItems() {
        // Use cache if available
        if (cachedFilteredItems.isEmpty() || getValue() == null) {
            cachedFilteredItems = filterItems(sprintItems);
        }
        return cachedFilteredItems;
    }
    
    private void invalidateCache() {
        cachedFilteredItems = List.of();
    }
    
    public void setItems(final List<CSprintItem> items) {
        this.sprintItems = items;
        invalidateCache();  // Invalidate when data changes
        if (getValue() != null) {
            refreshItems();
        }
    }
    
    private void refreshItems() {
        itemsLayout.removeAll();
        // Use cached filtered items - single filtering per refresh
        for (final CSprintItem item : getFilteredItems()) {
            itemsLayout.add(createPostit(item));
        }
    }
    
    protected void refreshStoryPointTotal() {
        // Reuse cached filtered items - no repeated filtering
        final long total = getFilteredItems().stream()
            .mapToLong(item -> item.getStoryPoint())
            .sum();
    }
}
```

### Performance Metrics

| Pattern | Filter Calls | Refresh Calls | Notes |
|---------|--------------|---------------|-------|
| ❌ Before Optimization | 6-8 per column | 2 per column | 2x setItems then setValue |
| ✅ After Optimization | 1 per column | 1 per column | setValue then setItems |
| **Improvement** | **85% reduction** | **50% reduction** | Scales with columns |

## 3. Refresh Control During User Interactions

### Rule: Suspend Auto-Refresh During Editing

**Problem**: Automatic refreshes disrupt user input, lose cursor position, and waste CPU.

**Solution**: Add refresh suspension flag:

```java
public abstract class CComponentBase<T> {
    private boolean refreshSuspended = false;
    
    /** Suspends automatic refresh operations. Call before user edits. */
    protected void suspendRefresh() {
        this.refreshSuspended = true;
    }
    
    /** Resumes automatic refresh and performs a single refresh. */
    protected void resumeRefresh() {
        this.refreshSuspended = false;
        refreshComponent();  // Single deliberate refresh
    }
    
    @Override
    protected void refreshComponent() {
        if (refreshSuspended) {
            LOGGER.debug("Refresh suspended - skipping");
            return;
        }
        // Perform actual refresh
        doRefresh();
    }
}
```

**Usage Pattern**:
```java
// ✅ CORRECT: Suspend during binder operations
public void editEntity(final CActivity entity) {
    suspendRefresh();  // Stop auto-refresh
    
    try {
        binder.setBean(entity);
        // User edits fields...
        binder.writeBean(entity);
        service.save(entity);
    } finally {
        resumeRefresh();  // Single refresh after save
    }
}

// ✅ CORRECT: Suspend during drag operations
@Override
public void drag_onStart(final CDragStartEvent event) {
    suspendRefresh();
}

@Override
public void drag_onEnd(final CDragEndEvent event) {
    resumeRefresh();  // Refresh once after drop completes
}
```

## 4. Kanban Column Assignment Logic

### Rule: Respect Manual Assignments

When assigning kanban columns to sprint items:

```java
// ✅ CORRECT: Skip auto-mapping if manually assigned
private void assignKanbanColumns(final List<CSprintItem> items) {
    for (final CSprintItem sprintItem : items) {
        // CRITICAL: Respect manual column assignment
        if (sprintItem.getKanbanColumnId() != null) {
            LOGGER.debug("Column already assigned, skipping auto-mapping");
            continue;
        }
        
        // Auto-map based on status
        final Long statusId = sprintItem.getItem().getStatus().getId();
        final Long columnId = statusToColumnMap.get(statusId);
        sprintItem.setKanbanColumnId(columnId);
    }
}
```

## 5. Logging Best Practices

### Rule: Conditional Debug Logging

```java
// ✅ CORRECT: Minimal logging in tight loops
private void refreshItems() {
    if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Refreshing {} items for column {}", 
            items.size(), getValue().getName());
    }
    // ... perform refresh
}

// ❌ WRONG: Expensive string operations in every call
private void refreshItems() {
    LOGGER.debug("Refreshing items for column {}", getValue().getName());
    // This creates strings even when DEBUG is disabled
}
```

### Rule: Avoid "null" in Log Output

```java
// ✅ CORRECT: Handle null values
LOGGER.debug("Filtering items for column {}", 
    getValue() != null ? getValue().getName() : "[not initialized]");

// ❌ WRONG: Confusing "null" literal in logs
LOGGER.debug("Filtering items for column {}", 
    getValue() != null ? getValue().getName() : "null");
```

## 6. Component Initialization Order

### Standard Initialization Pattern

```java
public void initializeComponent() {
    // 1. Create component
    final CComponentKanbanColumn column = new CComponentKanbanColumn();
    
    // 2. Set ID (if persistence needed)
    column.setId("kanbanColumn_" + context);
    
    // 3. Configure drag-drop
    column.drag_setDragEnabled(true);
    column.drag_setDropEnabled(true);
    
    // 4. Set up event listeners
    column.addSelectionListener(this::onSelection);
    
    // 5. Set configuration/value FIRST
    column.setValue(kanbanColumnEntity);
    
    // 6. Set data LAST (triggers single refresh)
    column.setItems(sprintItems);
    
    // 7. Add to layout
    layout.add(column);
}
```

## 7. Migration Checklist

When updating existing components to follow these patterns:

### Refresh Optimization
- [ ] Add `cachedFilteredItems` field
- [ ] Implement `getFilteredItems()` and `invalidateCache()`
- [ ] Replace direct `filterItems()` calls with `getFilteredItems()`
- [ ] Update `setItems()` to check if value is set before refreshing
- [ ] Update `onValueChanged()` to check if items are set before refreshing
- [ ] Reorder initialization: setValue before setItems

### Sprint Item Management
- [ ] Verify `handleDropOnBacklog()` resets all state
- [ ] Add status reset: `item.setStatus(null)`
- [ ] Add sprint reference clear: `item.setSprintItem(null)`
- [ ] Save item before deleting sprint item
- [ ] Add comprehensive logging

### Refresh Suspension
- [ ] Add `refreshSuspended` flag
- [ ] Implement `suspendRefresh()` and `resumeRefresh()`
- [ ] Update `refreshComponent()` to check flag
- [ ] Use suspension in binder operations
- [ ] Use suspension in drag-drop operations

## 8. Testing Guidelines

### Performance Test
```java
@Test
public void testRefreshOptimization() {
    // Measure filter calls
    int filterCallsBefore = filterCallCounter.get();
    
    component.setValue(column);
    component.setItems(items);
    
    int filterCallsAfter = filterCallCounter.get();
    
    // Should only filter once
    assertEquals(1, filterCallsAfter - filterCallsBefore);
}
```

### State Reset Test
```java
@Test
public void testBacklogDropResetsState() {
    // Given: Item in sprint with status
    CSprintItem sprintItem = createSprintItem();
    sprintItem.getItem().setStatus(inProgressStatus);
    
    // When: Dropped to backlog
    handleDropOnBacklog(sprintItem);
    
    // Then: State is reset
    assertNull(sprintItem.getItem().getStatus());
    assertNull(sprintItem.getItem().getSprintItem());
    assertFalse(sprintItemRepository.existsById(sprintItem.getId()));
}
```

## Summary

| Pattern | Benefit | Impact |
|---------|---------|--------|
| Deferred Refresh | 50% fewer refresh calls | Better UX, less CPU |
| Cached Filtering | 85% fewer filter operations | Faster rendering |
| Refresh Suspension | No interruptions during editing | Better UX |
| Complete State Reset | Correct backlog behavior | No stale state |
| Proper Initialization Order | Single refresh per component | Optimal startup |

## References

- Initial optimization: Commit addressing comment_3706000247
- Value persistence pattern: `docs/architecture/value-persistence-pattern.md`
- Component base classes: `CComponentBase`, `CComponentKanbanColumn`
- Kanban service: `CPageServiceKanbanLine`
