# Activity Parent-Child Relationship Feature

## Overview

Activities in the Derbent project management system support hierarchical parent-child relationships, allowing you to organize activities into multi-level structures. This is useful for breaking down large activities into smaller, manageable sub-tasks and tracking work at different levels of granularity.

## Architecture

### Implementation

The parent-child relationship is implemented in the `CProjectItem` base class, which `CActivity` extends:

- **Parent ID** (`parentId`): Stores the database ID of the parent activity
- **Parent Type** (`parentType`): Stores the class name of the parent entity (e.g., "CActivity")

### Key Methods

#### `CProjectItem` Base Class Methods

```java
// Set a parent activity
void setParent(CProjectItem<?> parent)

// Clear the parent relationship
void clearParent()

// Check if activity has a parent
boolean hasParent()

// Get parent ID
Long getParentId()

// Get parent type
String getParentType()
```

### Validation Rules

1. **Self-parent prevention**: An activity cannot be its own parent
2. **Persisted parent requirement**: The parent activity must be saved to the database (have an ID) before being assigned
3. **Null parent handling**: Setting parent to `null` clears the parent relationship

## Usage Examples

### Creating a Hierarchical Structure

```java
// Create a parent activity
CActivity parentActivity = new CActivity("Phase 1: Planning", project);
activityService.save(parentActivity);

// Create a child activity
CActivity childActivity = new CActivity("Requirements Gathering", project);
childActivity.setParent(parentActivity);
activityService.save(childActivity);

// Create a grandchild activity
CActivity grandchildActivity = new CActivity("Define User Stories", project);
grandchildActivity.setParent(childActivity);
activityService.save(grandchildActivity);
```

### Clearing Parent Relationship

```java
CActivity activity = activityService.getById(activityId);
activity.clearParent();
activityService.save(activity);
```

### Checking for Parent

```java
CActivity activity = activityService.getById(activityId);
if (activity.hasParent()) {
    Long parentId = activity.getParentId();
    String parentType = activity.getParentType();
    // Load parent and perform operations
}
```

## UI Display

### Grid Widget Display

Activities with parents display the parent activity name in the widget's third line with:
- **Prefix**: "↳" character indicating a child relationship
- **Styling**: Italic font with secondary text color for visual distinction
- **Graceful handling**: If parent cannot be loaded, the display is silently omitted

### Form Selection

When creating or editing an activity, you can select a parent activity from a dropdown selector. The parent must already exist in the system.

## Sample Data

The system includes sample data with a 4-level hierarchy:

```
Phase 1: Planning and Analysis (Level 1)
├── Requirements Gathering (Level 2)
│   └── Define User Stories (Level 3)
│       ├── User Story: Login Functionality (Level 4)
│       └── User Story: Dashboard View (Level 4)
└── System Architecture Design (Level 2)
    └── Design System Components (Level 3)
        └── Component Design Document (Level 4)
```

This sample data demonstrates:
- Multiple children per parent
- Deep hierarchy support (4 levels)
- Realistic activity breakdown structure

## Testing

### Unit Tests

The `CActivityParentChildTest` class provides comprehensive unit tests:
- Parent assignment and clearing
- Self-parent prevention
- Persisted parent requirement
- Multi-level hierarchy creation
- Parent changing
- Null parent handling

### UI Automation Tests

The `CActivityParentChildUITest` class provides UI-level tests:
- Parent activity display in grid widget
- Parent activity selection in forms
- Hierarchical structure verification

## Best Practices

1. **Save parent first**: Always save the parent activity before assigning it to a child
2. **Logical hierarchy**: Organize activities in a way that reflects the actual work breakdown structure
3. **Avoid circular dependencies**: The system prevents self-parenting, but be mindful of creating circular references through multiple levels
4. **Appropriate depth**: While the system supports unlimited depth, consider usability when creating very deep hierarchies

## Database Schema

The parent-child relationship is stored in the database using two columns in the activity table:
- `parent_id`: Foreign key to the parent activity's ID (nullable)
- `parent_type`: String storing the parent entity's class name (nullable)

This design allows for:
- **Polymorphic relationships**: Different entity types can be parents
- **Nullable fields**: Not all activities require a parent
- **Simple queries**: Easy to fetch all children of a parent or find the parent of a child

## Future Enhancements

Potential improvements to consider:
1. **Circular dependency detection**: Prevent circular references through multiple levels
2. **Hierarchy visualization**: Tree view or indented list view of activities
3. **Bulk operations**: Move entire branches of the hierarchy
4. **Progress rollup**: Automatically calculate parent progress from children
5. **Cascade operations**: Options to cascade delete or status changes to children
