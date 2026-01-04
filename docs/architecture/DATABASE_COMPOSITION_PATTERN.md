# Database Composition Pattern: CActivity/CMeeting + CSprintItem

## Overview

This document describes the **database table composition pattern** used for separating business event data from progress tracking data in the Derbent project management application.

**Pattern**: CActivity and CMeeting **compose with** CSprintItem using a has-a relationship instead of inheritance.

## Architecture Pattern

### Composition Over Inheritance

Instead of using table inheritance (single table or joined), we use **database table composition**:

```
CActivity (parent entity - business event)
    |
    |-- @OneToOne(CASCADE.ALL) --> CSprintItem (child entity - progress tracking)
          |-- @ManyToOne sprint (nullable - null = backlog)
          |-- @Transient parentItem (back-reference to CActivity)
```

### Why Composition?

1. **Separation of Concerns**: Business logic separate from progress tracking
2. **Flexibility**: Easy to add progress fields without touching business entities
3. **Consistency**: All sprintable items use the same progress mechanism
4. **Database Integrity**: Foreign key constraints enforce the relationship
5. **Clear Ownership**: Parent owns child via CASCADE.ALL

## Entity Relationships

### Parent Entities (Business Events)

**CActivity** - Represents work activities:
- Business fields: name, description, acceptance criteria, budget, etc.
- Owns CSprintItem via @OneToOne CASCADE.ALL
- Delegates progress operations to sprintItem

**CMeeting** - Represents meetings:
- Business fields: name, agenda, attendees, location, minutes, etc.
- Owns CSprintItem via @OneToOne CASCADE.ALL
- Delegates progress operations to sprintItem

### Child Entity (Progress Tracking)

**CSprintItem** - Pure progress tracking component:
- Progress fields: storyPoint, progressPercentage, startDate, dueDate, completionDate, responsible
- Ordering: itemOrder field (IOrderedEntity interface)
- Sprint assignment: sprint field (nullable - null means backlog)
- Display support: parentItem @Transient back-reference, kanbanColumnId @Transient field

## Database Schema

```sql
CREATE TABLE cactivity (
    activity_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    acceptance_criteria VARCHAR(2000),
    -- ... other business fields
    sprint_item_id BIGINT NOT NULL,  -- Foreign key to csprint_items
    CONSTRAINT fk_activity_sprint_item 
        FOREIGN KEY (sprint_item_id) 
        REFERENCES csprint_items(sprint_item_id)
        ON DELETE CASCADE  -- When activity deleted, sprint item deleted
);

CREATE TABLE cmeeting (
    meeting_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    agenda TEXT,
    location VARCHAR(500),
    -- ... other business fields
    sprint_item_id BIGINT NOT NULL,  -- Foreign key to csprint_items
    CONSTRAINT fk_meeting_sprint_item 
        FOREIGN KEY (sprint_item_id) 
        REFERENCES csprint_items(sprint_item_id)
        ON DELETE CASCADE  -- When meeting deleted, sprint item deleted
);

CREATE TABLE csprint_items (
    sprint_item_id BIGINT PRIMARY KEY,
    sprint_id BIGINT NULL,  -- Nullable - null means backlog
    story_point BIGINT,
    progress_percentage INTEGER,
    start_date DATE,
    due_date DATE,
    completion_date DATE,
    responsible_id BIGINT,
    item_order INTEGER,  -- For ordering within sprint/backlog
    CONSTRAINT fk_sprint_item_sprint 
        FOREIGN KEY (sprint_id) 
        REFERENCES csprints(sprint_id)
        ON DELETE SET NULL  -- When sprint deleted, set to null (move to backlog)
);
```

## Java Implementation

### Parent Entity (CActivity)

```java
@Entity
@Table(name = "cactivity")
public class CActivity extends CProjectItem<CActivity> 
        implements ISprintableItem, IHasStatusAndWorkflow<CActivity> {
    
    // Business fields
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 2000)
    private String acceptanceCriteria;
    
    // Composition: One-to-one with sprint item (CASCADE.ALL)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sprint_item_id", nullable = false)
    @NotNull(message = "Sprint item is required for progress tracking")
    private CSprintItem sprintItem;
    
    // Constructor creates sprintItem automatically
    public CActivity() {
        super();
        if (sprintItem == null) {
            sprintItem = CSprintItemService.createDefaultSprintItem();
            sprintItem.setParentItem(this);  // Set back-reference
        }
    }
    
    // Delegation: All progress operations delegate to sprintItem
    @Override
    public Long getStoryPoint() {
        Check.notNull(sprintItem, "Sprint item must not be null");
        return sprintItem.getStoryPoint();
    }
    
    @Override
    public void setStoryPoint(Long storyPoint) {
        Check.notNull(sprintItem, "Sprint item must not be null");
        sprintItem.setStoryPoint(storyPoint);
    }
    
    @Override
    public CSprintItem getSprintItem() { 
        return sprintItem; 
    }
    
    // ... other delegating getters/setters
}
```

### Child Entity (CSprintItem)

```java
@Entity
@Table(name = "csprint_items")
public class CSprintItem extends CEntityDB<CSprintItem> 
        implements IOrderedEntity {
    
    // Transient back-reference to parent (set by parent after loading)
    @Transient
    private ISprintableItem parentItem;
    
    // Transient field for kanban board display
    @Transient
    private Long kanbanColumnId;
    
    // Sprint reference (nullable - null = backlog)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = true)
    private CSprint sprint;
    
    // Progress tracking fields
    @Column(nullable = true)
    private Long storyPoint;
    
    @Column(nullable = true)
    @Min(0) @Max(100)
    private Integer progressPercentage;
    
    @Column(name = "start_date", nullable = true)
    private LocalDate startDate;
    
    @Column(name = "due_date", nullable = true)
    private LocalDate dueDate;
    
    @Column(name = "completion_date", nullable = true)
    private LocalDate completionDate;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_id", nullable = true)
    private CUser responsible;
    
    // IOrderedEntity implementation
    @Column(name = "item_order", nullable = true)
    private Integer itemOrder;
    
    // Getter for parent item (alias for compatibility)
    public ISprintableItem getItem() { 
        return getParentItem(); 
    }
    
    // ... getters and setters
}
```

## Cascade Delete Behavior

### When Activity/Meeting Deleted

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
private CSprintItem sprintItem;
```

**Result**: SprintItem is **CASCADE deleted** from database.

**SQL Equivalent**:
```sql
DELETE FROM cactivity WHERE activity_id = ?;
-- Automatically triggers:
-- DELETE FROM csprint_items WHERE sprint_item_id = ?;
```

### When Sprint Deleted

```java
// In CSprintService.delete()
for (CSprintItem item : sprint.getSprintItems()) {
    item.setSprint(null);  // Move to backlog
    sprintItemService.save(item);
}
```

**Result**: Sprint items are **moved to backlog** (sprint set to null), NOT deleted.

**SQL Equivalent**:
```sql
UPDATE csprint_items SET sprint_id = NULL WHERE sprint_id = ?;
DELETE FROM csprints WHERE sprint_id = ?;
```

## Sprint Operations

### Add Item to Sprint

```java
// Get activity and sprint
CActivity activity = activityService.findById(activityId);
CSprint sprint = sprintService.findById(sprintId);

// Update existing sprintItem (don't create new one)
activity.getSprintItem().setSprint(sprint);
activity.getSprintItem().setItemOrder(nextOrder);
activityService.save(activity);  // Cascades to sprintItem
```

### Remove Item from Sprint (Move to Backlog)

```java
CActivity activity = activityService.findById(activityId);
activity.getSprintItem().setSprint(null);  // null = backlog
activityService.save(activity);
```

### Reorder Items

```java
CSprintItem sprintItem = activity.getSprintItem();
sprintItemService.moveItemUp(sprintItem);    // Swap with previous
sprintItemService.moveItemDown(sprintItem);  // Swap with next
```

### Query Backlog Items

```java
// Find all activities in backlog (sprint = null)
List<CActivity> backlogActivities = activityRepository
    .findAll()
    .stream()
    .filter(a -> a.getSprintItem().getSprint() == null)
    .collect(Collectors.toList());
```

### Query Sprint Items

```java
// Find all sprint items for a sprint
List<CSprintItem> sprintItems = sprintItemRepository
    .findByMasterId(sprintId);

// Get parent activities/meetings
for (CSprintItem item : sprintItems) {
    ISprintableItem parent = item.getParentItem();
    // parent is CActivity or CMeeting
}
```

## Kanban Board Integration

### Transient kanbanColumnId Field

Used for temporary UI state during drag/drop operations:

```java
// During drag operation
sprintItem.setKanbanColumnId(columnId);

// In kanban display component
Long columnId = sprintItem.getKanbanColumnId();
```

**Important**: This field is NOT persisted to database, only used for UI state management.

## Ordering Within Sprint/Backlog

### IOrderedEntity Interface

CSprintItem implements IOrderedEntity for ordering:

```java
public interface IOrderedEntity {
    Integer getItemOrder();
    void setItemOrder(Integer order);
}
```

### Ordering Logic

- Items within same sprint are ordered by `itemOrder` field
- Items in backlog (sprint = null) are also ordered by `itemOrder`
- `moveItemUp()` and `moveItemDown()` swap order values between adjacent items

### Get Next Order Number

```java
Integer nextOrder = sprintItemService.getNextItemOrder(sprint);
// Returns max(itemOrder) + 1 for sprint, or 1 if empty
```

## Validation Rules

### Required Fields

1. **CActivity.sprintItem**: @NotNull - Every activity MUST have a sprint item
2. **CMeeting.sprintItem**: @NotNull - Every meeting MUST have a sprint item
3. **CSprintItem.sprint**: Nullable - null means item is in backlog

### Check.notNull() Pattern

All delegation methods use Check.notNull() for fail-fast validation:

```java
@Override
public Long getStoryPoint() {
    Check.notNull(sprintItem, "Sprint item must not be null");
    return sprintItem.getStoryPoint();
}
```

**Why**: Ensures bugs are caught immediately during development rather than causing NPE later.

## Backward Compatibility

### getItem() Alias

For compatibility with existing code that expects `getItem()`:

```java
public ISprintableItem getItem() { 
    return getParentItem(); 
}
```

### Migration Fields

During migration, some fields remain in parent entities for backward compatibility:

```java
// CActivity - these will be removed after migration complete
@Column(nullable = true)
private Long storyPoint;  // Delegated to sprintItem.storyPoint

@Column(nullable = true)
private LocalDate startDate;  // Delegated to sprintItem.startDate
```

Getters/setters delegate to sprintItem but also sync the old field temporarily.

## Benefits

### Separation of Concerns

- **Business entities** (CActivity/CMeeting) focus on their domain logic
- **Progress tracking** (CSprintItem) is centralized and consistent
- Easy to add new progress fields without touching business entities

### Database Integrity

- Foreign key constraints enforce relationships
- CASCADE rules ensure proper cleanup
- NOT NULL constraint on sprintItem ensures composition is always valid

### Flexibility

- New progress fields added only to CSprintItem
- Changes don't ripple through Activity/Meeting entities
- Interface-driven design (ISprintableItem) allows polymorphism

### Code Reuse

- Single progress tracking implementation
- No duplication between Activity and Meeting
- Consistent delegation pattern

### Testing

- Progress logic tested once in CSprintItem
- Business logic tested separately in Activity/Meeting
- Clear boundaries for unit testing

## Common Pitfalls

### ❌ Creating Sprint Items Separately

```java
// WRONG - Don't create sprint items separately
CSprintItem sprintItem = new CSprintItem();
sprintItem.setItemId(activity.getId());
sprintItemService.save(sprintItem);
```

**Why wrong**: Sprint items are owned by their parent. They should never be created independently.

### ✅ Correct Approach

```java
// RIGHT - Sprint item is created automatically in constructor
CActivity activity = new CActivity();
// activity.sprintItem is already initialized
activity.getSprintItem().setSprint(sprint);
activityService.save(activity);
```

### ❌ Forgetting to Set Back-Reference

```java
// WRONG - Parent item not accessible from sprintItem
CSprintItem sprintItem = new CSprintItem();
// sprintItem.getParentItem() returns null!
```

### ✅ Correct Approach

```java
// RIGHT - Constructor sets back-reference
public CActivity() {
    super();
    if (sprintItem == null) {
        sprintItem = CSprintItemService.createDefaultSprintItem();
        sprintItem.setParentItem(this);  // ✓ Back-reference set
    }
}
```

## Design Decisions

### Why @Transient for parentItem?

- **Avoids circular reference** in database
- **Prevents N+1 queries** (would need bidirectional mapping)
- **Simplifies schema** (no extra foreign key column needed)
- **Set programmatically** after loading from database

### Why Nullable sprint Field?

- **Backlog support**: sprint = null means item is in backlog
- **Flexible workflow**: Items can exist without sprint assignment
- **Simple queries**: `WHERE sprint_id IS NULL` finds backlog items

### Why CASCADE.ALL on sprintItem?

- **Lifecycle ownership**: Parent owns child completely
- **Automatic cleanup**: Deleting parent automatically deletes child
- **Data integrity**: No orphaned sprint items in database

### Why @OneToOne Instead of @Embedded?

- **Independent entity**: Sprint item has its own ID and relationships
- **Relationship to Sprint**: Sprint item can reference a sprint
- **Ordering interface**: Implements IOrderedEntity for reordering
- **Query flexibility**: Can query sprint items directly

## Summary

The database composition pattern provides:
- ✅ Clear separation of business and progress concerns
- ✅ Strong database integrity via foreign keys and cascades
- ✅ Consistent progress tracking across all sprintable items
- ✅ Flexible sprint and backlog management
- ✅ Ordering support via IOrderedEntity
- ✅ Kanban board integration via transient fields
- ✅ No code duplication between Activity and Meeting
- ✅ Fail-fast validation with Check.notNull()

**Pattern Name**: Database Table Composition with Transient Back-Reference and IOrderedEntity Support
