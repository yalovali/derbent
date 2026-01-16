# Story Points Implementation Summary

## Overview
This implementation adds story point tracking functionality to sprintable items (Activities and Meetings) in the Derbent project management application. Story points represent the estimated effort or complexity of work items and are now displayed both at the individual item level and aggregated at the sprint level.

## Changes Made

### 1. ISprintableItem Interface
**File**: `src/main/java/tech/derbent/api/interfaces/ISprintableItem.java`

Added two new methods to the interface:
```java
/** Gets the story points for this item. Story points represent the estimated effort or complexity of the item.
 * @return the story points, or null if not set */
Long getStoryPoint();

/** Sets the story points for this item. Story points represent the estimated effort or complexity of the item.
 * @param storyPoint the story points value */
void setStoryPoint(Long storyPoint);
```

### 2. CActivity Entity
**File**: `src/main/java/tech/derbent/app/activities/domain/CActivity.java`

Added story point field with JPA and metadata annotations:
```java
@Column (nullable = true)
@AMetaData (
    displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
    description = "Estimated effort or complexity in story points", hidden = false
)
private Long storyPoint;
```

Implemented getter and setter methods:
```java
@Override
public Long getStoryPoint() { return storyPoint; }

@Override
public void setStoryPoint(final Long storyPoint) {
    this.storyPoint = storyPoint;
    updateLastModified();
}
```

### 3. CMeeting Entity
**File**: `src/main/java/tech/derbent/app/meetings/domain/CMeeting.java`

Added story point field with JPA and metadata annotations:
```java
@Column (nullable = true)
@AMetaData (
    displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
    description = "Estimated effort or complexity in story points", hidden = false
)
private Long storyPoint;
```

Implemented getter and setter methods:
```java
@Override
public Long getStoryPoint() { return storyPoint; }

@Override
public void setStoryPoint(final Long storyPoint) { this.storyPoint = storyPoint; }
```

### 4. CSprint Entity - Total Calculation
**File**: `src/main/java/tech/derbent/app/sprints/domain/CSprint.java`

Added method to calculate total story points for all items in a sprint:
```java
/** Get the total story points for all items in this sprint. This is a calculated field for UI display.
 * @return total story points, or 0 if no items have story points */
public Long getTotalStoryPoints() {
    if (sprintItems == null || sprintItems.isEmpty()) {
        return 0L;
    }
    long total = 0L;
    for (final CSprintItem sprintItem : sprintItems) {
        if (sprintItem.getItem() != null && sprintItem.getItem() instanceof ISprintableItem) {
            final Long itemStoryPoint = ((ISprintableItem) sprintItem.getItem()).getStoryPoint();
            if (itemStoryPoint != null) {
                total += itemStoryPoint;
            }
        }
    }
    return total;
}
```

### 5. Sprint Widget - Display Total
**File**: `src/main/java/tech/derbent/app/sprints/view/CComponentWidgetSprint.java`

Updated the item count label to display total story points:
```java
private CLabelEntity createItemCountLabel() {
    final Integer itemCount = getEntity().getItemCount();
    final Long totalStoryPoints = getEntity().getTotalStoryPoints();
    final CLabelEntity label = new CLabelEntity();
    // ... styling code ...
    
    // Add count text with story points
    final String countText = (itemCount != null ? itemCount : 0) + " item" + ((itemCount != null && itemCount != 1) ? "s" : "");
    final String storyPointsText = totalStoryPoints != null && totalStoryPoints > 0 ? " (" + totalStoryPoints + " SP)" : "";
    label.setText(countText + storyPointsText);
    return label;
}
```

### 6. Sprint Items Grid - Story Points Column
**File**: `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListSprintItems.java`

Added a story points column to the grid:
```java
// Add story points column
grid.addIntegerColumn(item -> {
    if (item.getItem() instanceof ISprintableItem) {
        final Long storyPoint = ((ISprintableItem) item.getItem()).getStoryPoint();
        return storyPoint != null ? storyPoint.intValue() : null;
    }
    return null;
}, "Story Points", "storyPoint");
```

## Technical Details

### Database Schema Changes
When the application runs with Hibernate's `ddl-auto=create` or `ddl-auto=update`, the following columns will be automatically added:
- `cactivity.story_point` (BIGINT, nullable)
- `cmeeting.story_point` (BIGINT, nullable)

### Type Casting Approach
Since `CProjectItem` doesn't implement `ISprintableItem` directly (only `CActivity` and `CMeeting` do), we use instanceof checks and cast to `ISprintableItem` when accessing the story point methods:

```java
if (item.getItem() instanceof ISprintableItem) {
    Long storyPoint = ((ISprintableItem) item.getItem()).getStoryPoint();
    // ... use storyPoint ...
}
```

This ensures type safety while allowing polymorphic access to the story point field.

### UI Display Format
- **Individual Items**: Story points are displayed as an integer column in the sprint items grid
- **Sprint Widget**: Total story points are displayed next to the item count in the format: `"5 items (21 SP)"`
- Only shows story points total when at least one item has story points assigned

## Build Status
✓ Compilation successful with `mvn clean compile`
✓ All Java files compile without errors
✓ No breaking changes to existing functionality

## Usage Examples

### Setting Story Points on an Activity
```java
CActivity activity = new CActivity("Implement user login", project);
activity.setStoryPoint(5L);
activityService.save(activity);
```

### Setting Story Points on a Meeting
```java
CMeeting meeting = new CMeeting("Sprint Planning", project);
meeting.setStoryPoint(2L);
meetingService.save(meeting);
```

### Getting Total Story Points for a Sprint
```java
CSprint sprint = sprintService.findById(sprintId);
Long totalStoryPoints = sprint.getTotalStoryPoints();
System.out.println("Sprint has " + totalStoryPoints + " story points");
```

## Future Enhancements
Possible future improvements could include:
1. Burndown chart showing story points completed over time
2. Velocity tracking (story points completed per sprint)
3. Story point estimation templates based on historical data
4. Story point validation rules (e.g., Fibonacci sequence)
5. Team capacity planning based on story points

## Testing Notes
The implementation follows the existing patterns in the codebase:
- Uses `@AMetaData` annotations for automatic form generation
- Implements getter/setter methods with proper null handling
- Follows the C-prefix naming convention
- Updates `lastModified` timestamp when story points change (for CActivity)
- Uses proper type casting and instanceof checks for polymorphic access

## Files Modified
1. `src/main/java/tech/derbent/api/interfaces/ISprintableItem.java`
2. `src/main/java/tech/derbent/app/activities/domain/CActivity.java`
3. `src/main/java/tech/derbent/app/meetings/domain/CMeeting.java`
4. `src/main/java/tech/derbent/app/sprints/domain/CSprint.java`
5. `src/main/java/tech/derbent/app/sprints/view/CComponentWidgetSprint.java`
6. `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListSprintItems.java`

## Compatibility
- Backward compatible: Existing sprints and items without story points will work correctly
- Null-safe: All code handles null story points gracefully
- Database schema: Columns are nullable, so existing data is not affected
