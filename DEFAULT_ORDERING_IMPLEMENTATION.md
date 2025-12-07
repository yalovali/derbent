# Default Ordering Implementation Summary

## Overview
This implementation adds default ordering to all entity queries in the Derbent application. Every entity now has a defined default order, and all queries automatically apply this ordering. This ensures consistent, predictable results across the application.

## What Was Implemented

### 1. Default Ordering Infrastructure

#### Entity Level
- **`CEntityDB.getDefaultOrderBy()`**: Base implementation returns "id" as default order field
- **`CEntityNamed.getDefaultOrderBy()`**: Override returns "name" for named entities
- Entities can override this method to specify custom default ordering

#### Service Level
- **`CAbstractService.getDefaultSort()`**: Creates Sort object based on entity's default order field
- **`CAbstractService.findAll()`**: Now applies default ordering (DESC by default)
- **`CAbstractService.ensureDefaultOrdering()`**: Merges user-provided Sort with default ordering
- **`CAbstractService.list()` methods**: All list methods now ensure default ordering

#### Repository Level
All repository queries now include explicit ORDER BY clauses:
- **IActivityRepository**: `ORDER BY a.id DESC`
- **IMeetingRepository**: `ORDER BY m.id DESC`
- **IProjectRepository**: `ORDER BY p.name`

### 2. Sprint Order Field

Added `sprintOrder` field for sprint-aware ordering:

#### Interface Update
- **`ISprintableItem`**: Added `getSprintOrder()` and `setSprintOrder()` methods

#### Entity Implementation
- **`CActivity`**: Added `sprintOrder` Integer field with database column `sprint_order`
- **`CMeeting`**: Added `sprintOrder` Integer field with database column `sprint_order`

#### Sprint-Aware Queries
New repository methods for sprint-aware ordering:
- **`IActivityRepository.listByProjectOrderedBySprintOrder()`**: Orders by `sprintOrder ASC NULLS LAST, id DESC`
- **`IMeetingRepository.listByProjectOrderedBySprintOrder()`**: Orders by `sprintOrder ASC NULLS LAST, id DESC`

Corresponding service methods:
- **`CActivityService.listByProjectOrderedBySprintOrder(project)`**
- **`CMeetingService.listByProjectOrderedBySprintOrder(project)`**

### 3. Sprint Item Ordering

Sprint items already use `itemOrder` field for ordering (existing functionality):
- **`ISprintItemRepository`**: All queries use `ORDER BY e.itemOrder ASC`

## How To Use

### For Regular Entities

Entities automatically use their default ordering:

```java
// Service automatically applies default ordering
List<CActivity> activities = activityService.findAll();
// Result: Activities ordered by ID descending

List<CProject> projects = projectService.findAll();
// Result: Projects ordered by name ascending (CEntityNamed default)
```

### For Custom Default Ordering

Override `getDefaultOrderBy()` in your entity:

```java
@Entity
public class CMyEntity extends CEntityDB<CMyEntity> {
    
    @Override
    public String getDefaultOrderBy() {
        return "createDate"; // Order by creation date instead of ID
    }
}
```

### For Sprint-Aware Components

Use the sprint-order aware methods:

```java
// For activities in a backlog - orders by sprintOrder
List<CActivity> orderedActivities = activityService.listByProjectOrderedBySprintOrder(project);

// For meetings in a sprint view
List<CMeeting> orderedMeetings = meetingService.listByProjectOrderedBySprintOrder(project);
```

### For User-Provided Sorting

User sorting is combined with default sorting:

```java
// User wants to sort by priority
Sort userSort = Sort.by("priority").ascending();
Pageable pageable = PageRequest.of(0, 20, userSort);

// Service combines: first priority ASC, then default (id DESC)
Page<CActivity> page = activityService.list(pageable);
```

## Database Changes

The implementation adds new columns to existing tables:
- `cactivity.sprint_order` (INTEGER, nullable)
- `cmeeting.sprint_order` (INTEGER, nullable)

These columns are automatically created by Hibernate on application startup.

## Testing

Unit tests verify the ordering functionality:
- `CEntityDBDefaultOrderingTest`: Tests default ordering behavior
  - Verifies CEntityDB returns "id"
  - Verifies CEntityNamed returns "name"
  - Verifies custom overrides work
  - Verifies Sort objects can be created

All tests pass successfully (4/4).

## Future Enhancements

To complete the sprint ordering functionality:

1. **Drag-and-Drop Support**: Implement drag-and-drop reordering in backlog grids that updates `sprintOrder` field
2. **Automatic Sprint Order Assignment**: When adding items to sprints, automatically assign sequential `sprintOrder` values
3. **Sprint Order Maintenance**: Add methods to reorder items and maintain sequential order without gaps

## Examples

### Example 1: Activity List Ordered by Default

```java
// Before: Activities could appear in any order
List<CActivity> activities = activityService.findAll();

// After: Activities are consistently ordered by ID descending
// Activity 100, Activity 99, Activity 98, ...
```

### Example 2: Project List Ordered by Name

```java
// Projects extend CEntityNamed, so they order by name
List<CProject> projects = projectService.findAll();
// Result: "Alpha Project", "Beta Project", "Gamma Project"
```

### Example 3: Sprint Backlog with Custom Order

```java
// Load activities ordered by sprint order for backlog display
List<CActivity> backlogItems = activityService.listByProjectOrderedBySprintOrder(project);

// Items with sprintOrder appear first (in order), then items without sprintOrder
// Activity 5 (sprintOrder=1)
// Activity 3 (sprintOrder=2)
// Activity 7 (sprintOrder=3)
// Activity 2 (sprintOrder=null) - ordered by ID desc
// Activity 1 (sprintOrder=null) - ordered by ID desc
```

## Migration Notes

- **Existing Data**: All existing entities will have `null` values for `sprintOrder`
- **Backward Compatibility**: Null `sprintOrder` values are handled gracefully (appear last in sprint-ordered queries)
- **No Breaking Changes**: Default ordering is applied transparently; existing code continues to work

## Implementation Philosophy

The implementation follows the requirements to use "straight simple approach" with JPA features:

- ✅ Uses standard JPA `ORDER BY` clauses in JPQL queries
- ✅ Leverages Spring Data `Sort` objects for programmatic ordering
- ✅ Provides simple string-based default order field specification
- ✅ Allows easy override for custom ordering per entity
- ✅ No complex reflection or dynamic query building
- ✅ Clear, maintainable code with explicit ordering

## Key Files Modified

### Core Infrastructure
- `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java`
- `src/main/java/tech/derbent/api/entity/domain/CEntityNamed.java`
- `src/main/java/tech/derbent/api/entity/service/CAbstractService.java`

### Sprint Order Field
- `src/main/java/tech/derbent/api/interfaces/ISprintableItem.java`
- `src/main/java/tech/derbent/app/activities/domain/CActivity.java`
- `src/main/java/tech/derbent/app/meetings/domain/CMeeting.java`

### Repository Queries
- `src/main/java/tech/derbent/app/activities/service/IActivityRepository.java`
- `src/main/java/tech/derbent/app/meetings/service/IMeetingRepository.java`
- `src/main/java/tech/derbent/app/projects/service/IProjectRepository.java`

### Service Methods
- `src/main/java/tech/derbent/app/activities/service/CActivityService.java`
- `src/main/java/tech/derbent/app/meetings/service/CMeetingService.java`

### Tests
- `src/test/java/tech/derbent/api/entity/service/CEntityDBDefaultOrderingTest.java`
