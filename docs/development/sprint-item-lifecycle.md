# Sprint Item Lifecycle and Drag-Drop Patterns

## Overview

This document explains the correct patterns for managing sprint items and their lifecycle, particularly during drag-drop operations between backlog and sprints.

## Critical Concepts

### Ownership Model

**Sprint items are OWNED by their parent entities (CActivity/CMeeting), NOT by sprints.**

```
CActivity/CMeeting (Parent)
    └── @OneToOne (cascade = ALL, orphanRemoval = true)
        └── CSprintItem (Child)
            └── @ManyToOne (nullable = true)
                └── CSprint (Referenced, not owner)
```

### Backlog Semantics

**Backlog is NOT a separate entity - it's a state where sprintItem.sprint = NULL:**

- `sprintItem.sprint = null` → Item is in backlog
- `sprintItem.sprint = someSprint` → Item is in that sprint

## Correct Patterns

### ✅ Moving Items Between Backlog and Sprint

```java
// Add item to sprint
activity.getSprintItem().setSprint(targetSprint);
sprintItemService.save(activity.getSprintItem());

// Move item to backlog
activity.getSprintItem().setSprint(null);
sprintItemService.save(activity.getSprintItem());

// Move item to different sprint
activity.getSprintItem().setSprint(newSprint);
sprintItemService.save(activity.getSprintItem());
```

### ✅ Using the Unified Service

```java
@Autowired
private CSprintItemDragDropService dragDropService;

// Move to backlog
dragDropService.moveSprintItemToBacklog(sprintItem);

// Move to sprint
dragDropService.moveSprintItemToSprint(sprintItem, targetSprint);

// Add backlog item to sprint
dragDropService.addBacklogItemToSprint(activity, targetSprint);
```

### ✅ Sprint Deletion (Returns Items to Backlog)

```java
@Override
@Transactional
public void delete(final CSprint sprint) {
    // Move all sprint items to backlog
    final List<CSprintItem> items = sprintItemService.findByMasterId(sprint.getId());
    for (final CSprintItem item : items) {
        item.setSprint(null);  // Move to backlog
        sprintItemService.save(item);
    }
    
    // Now delete the sprint
    super.delete(sprint);
}
```

## Incorrect Patterns (NEVER DO THIS)

### ❌ Deleting Sprint Items

```java
// ❌ WRONG: Deletes parent Activity/Meeting due to orphanRemoval=true
sprintItemService.delete(sprintItem);

// ❌ WRONG: Orphans sprint item, causes constraint violation
activity.setSprintItem(null);
```

### ❌ Replacing Sprint Items

```java
// ❌ WRONG: Creates orphaned sprint item
activity.setSprintItem(new CSprintItem());

// ❌ WRONG: Replaces existing sprint item
activity.setSprintItem(someOtherSprintItem);
```

### ❌ Manual Sprint Reference Changes Without Service

```java
// ❌ WRONG: Doesn't update ordering, kanban columns, etc.
sprintItem.setSprint(targetSprint);
// Missing: ordering, validation, notifications
```

## Sprint Item Lifecycle

### Creation

Sprint items are created ONCE when the parent entity is created:

```java
@Override
public void initializeNewEntity(final CActivity entity) {
    // Create sprint item (ONLY time setSprintItem should be called)
    final CSprintItem sprintItem = new CSprintItem();
    sprintItem.setSprint(null);  // Start in backlog
    sprintItem.setProgressPercentage(0);
    sprintItem.setStoryPoint(0L);
    entity.setSprintItem(sprintItem);  // Initial assignment only
}
```

### Modification

Sprint items are modified throughout their lifetime:

```java
// ✅ Modify sprint reference
activity.getSprintItem().setSprint(targetSprint);

// ✅ Update progress
activity.getSprintItem().setProgressPercentage(50);

// ✅ Change story points
activity.getSprintItem().setStoryPoint(8L);
```

### Deletion

Sprint items are deleted ONLY when the parent is deleted (CASCADE):

```java
// ✅ Deleting activity cascades to sprint item
activityService.delete(activity);

// Sprint item is automatically deleted by JPA CASCADE.ALL
```

## Drag-Drop Operation Flows

### Backlog → Sprint

```
1. User drags Activity from backlog to sprint widget
2. CPageServiceSprint.drag_handle_backlogItem_toSprint()
3. dragDropService.addBacklogItemToSprint(activity, targetSprint)
4. activity.getSprintItem().setSprint(targetSprint)
5. sprintItemService.save(activity.getSprintItem())
6. Refresh UI
```

### Sprint → Backlog

```
1. User drags CSprintItem from sprint items grid to backlog
2. CPageServiceSprint.drag_handle_sprintItem_toBacklog()
3. dragDropService.moveSprintItemToBacklog(sprintItem)
4. sprintItem.setSprint(null)
5. sprintItemService.save(sprintItem)
6. Refresh UI
```

### Sprint → Sprint

```
1. User drags CSprintItem to different sprint widget
2. CPageServiceSprint.drag_handle_sprintItem_toSprint()
3. dragDropService.moveSprintItemToSprint(sprintItem, targetSprint)
4. sprintItem.setSprint(targetSprint)
5. Update item order
6. sprintItemService.save(sprintItem)
7. Reorder items in source sprint
8. Refresh UI
```

### Kanban Column → Backlog

```
1. User drags sprint item from kanban column to backlog column
2. CPageServiceKanbanLine.handleDropOnBacklog()
3. dragDropService.moveSprintItemToBacklog(sprintItem)
4. sprintItem.setSprint(null)
5. sprintItem.setKanbanColumnId(null)
6. sprintItemService.save(sprintItem)
7. Refresh UI (deferred with UI.access())
```

## JPA Relationships

### CActivity/CMeeting → CSprintItem (Composition)

```java
@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "sprintitem_id", nullable = false)
@NotNull(message = "Sprint item is required")
private CSprintItem sprintItem;
```

**Key Points:**
- `CASCADE.ALL`: All operations cascade to sprint item
- `orphanRemoval=true`: Removing sprint item from parent deletes it
- `nullable=false`: Sprint item is REQUIRED (always exists)

### CSprintItem → CSprint (Reference)

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "sprint_id", nullable = true)
private CSprint sprint;
```

**Key Points:**
- `nullable=true`: Sprint can be NULL (backlog semantics)
- `NO CASCADE`: Sprint does not own sprint items
- `LAZY`: Sprint is loaded on demand

### CSprint → CSprintItem (Back-Reference)

```java
@OneToMany(mappedBy = "sprint", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
private List<CSprintItem> sprintItems = new ArrayList<>();
```

**Key Points:**
- `mappedBy="sprint"`: This is the inverse side (read-only)
- `NO CASCADE.ALL`: Sprint does NOT own sprint items
- `NO orphanRemoval`: Items persist when removed from collection

## Common Mistakes and Solutions

### Mistake: Deleting Sprint Items in Drag-Drop

**Problem:**
```java
// ❌ WRONG
private void drag_moveSprintItemToBacklog(final CSprintItem sprintItem) {
    sprintItemService.delete(sprintItem);  // Deletes parent Activity!
}
```

**Solution:**
```java
// ✅ CORRECT
private void drag_moveSprintItemToBacklog(final CSprintItem sprintItem) {
    dragDropService.moveSprintItemToBacklog(sprintItem);  // Sets sprint to null
}
```

### Mistake: Clearing Sprint Item Reference

**Problem:**
```java
// ❌ WRONG
item.setSprintItem(null);  // Orphans sprint item, constraint violation
```

**Solution:**
```java
// ✅ CORRECT
item.getSprintItem().setSprint(null);  // Moves to backlog
```

### Mistake: Creating New Sprint Items

**Problem:**
```java
// ❌ WRONG
item.setSprintItem(new CSprintItem());  // Orphans old sprint item
```

**Solution:**
```java
// ✅ CORRECT
// Sprint items are created ONCE during entity initialization
// Never replaced, only modified
```

## Testing Checklist

When testing sprint item operations, verify:

- [ ] Moving item from backlog to sprint updates sprint reference
- [ ] Moving item from sprint to backlog sets sprint to NULL
- [ ] Moving item between sprints updates sprint reference
- [ ] Sprint items are NOT deleted during any drag-drop operation
- [ ] Parent Activity/Meeting entities persist after backlog operations
- [ ] Deleting a sprint moves all items to backlog (sprint=null)
- [ ] Deleting Activity/Meeting cascades to sprint item
- [ ] UI refreshes correctly after all operations

## References

- **CSprintItemDragDropService**: Unified service for drag-drop operations
- **CPageServiceSprint**: Sprint view drag-drop handling
- **CPageServiceKanbanLine**: Kanban board drag-drop handling
- **CSprintService.delete()**: Sprint deletion with backlog migration
- **CActivityService.initializeNewEntity()**: Sprint item creation
- **CMeetingService.initializeNewEntity()**: Sprint item creation

## Version History

- **2026-01-06**: Initial documentation after unified drag-drop refactoring
- **Fixed**: Sprint item deletion bug in drag-drop operations
- **Unified**: CPageServiceSprint and CPageServiceKanbanLine drag-drop handling
