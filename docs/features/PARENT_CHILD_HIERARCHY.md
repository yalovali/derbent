# Parent-Child Hierarchy Implementation Guide

## Overview

The Derbent project management application now supports hierarchical parent-child relationships between project items, similar to Jira's Epic → Feature → User Story → Task hierarchy. This feature allows teams to organize work items in up to 4 levels of hierarchy.

## Key Components

### 1. Domain Model

#### CTypeEntity Extensions

Each entity type (CActivityType, CMeetingType, etc.) now has configuration for hierarchical relationships:

- **canHaveChildren** (boolean): Determines if items of this type can have child items
- **parentLevel1EntityClass** (String): Entity class for level 1 parents (e.g., "CActivity" for Epics)
- **parentLevel2EntityClass** (String): Entity class for level 2 parents (e.g., "CActivity" for Features)
- **parentLevel3EntityClass** (String): Entity class for level 3 parents (e.g., "CActivity" for User Stories)
- **parentLevel4EntityClass** (String): Entity class for level 4 parents (e.g., "CActivity" for Tasks)

#### CProjectItem

Base class for all project items, already had support for parent relationships:
- `parentId` (Long): ID of the parent item
- `parentType` (String): Class name of the parent item (e.g., "CActivity")
- `hasParent()`: Helper method to check if item has a parent
- `setParent(CProjectItem parent)`: Set parent with validation
- `clearParent()`: Remove parent relationship

#### CParentChildRelation

Entity for tracking parent-child relationships:
- Unique constraint on (childId, childType, parentId, parentType)
- Supports recursive queries for circular dependency detection

### 2. Service Layer

#### CParentChildRelationService

Core service for managing hierarchical relationships:

```java
// Establish parent relationship
parentChildService.setParent(childItem, parentItem);

// Remove parent relationship
parentChildService.clearParent(childItem);

// Get parent of an item
Optional<CProjectItem<?>> parent = parentChildService.getParent(childItem);

// Get all children of an item
List<CProjectItem<?>> children = parentChildService.getChildren(parentItem);

// Check if assignment would create circular dependency
boolean wouldBeCircular = parentChildService.wouldCreateCircularDependency(
    parentId, parentType, childId, childType);

// Check if item type allows children
boolean canHave = parentChildService.canHaveChildren(item);
```

### 3. UI Components

#### CDialogParentSelection

Dialog for selecting hierarchical parents with 4-level filtering:

```java
// Open parent selection dialog
CDialogParentSelection dialog = new CDialogParentSelection(childItem, selectedParent -> {
    if (selectedParent == null) {
        // Parent was cleared
        parentChildService.clearParent(childItem);
    } else {
        // Parent was selected
        parentChildService.setParent(childItem, selectedParent);
    }
    // Refresh UI
    refreshGrid();
});
dialog.open();
```

**Features:**
- Up to 4 levels of comboboxes (Epic → Feature → Story → Task)
- Each level filters based on previous selection
- Validates circular dependencies
- Validates parent type can have children
- Option to clear existing parent
- Disabled when no hierarchy levels configured

## Configuration Example

### Setting up Activity Types with Hierarchy

1. **Epic Type** (Level 1)
   - canHaveChildren: true
   - parentLevel1EntityClass: null (Epics don't have parents)
   - parentLevel2EntityClass: null
   - parentLevel3EntityClass: null
   - parentLevel4EntityClass: null

2. **Feature Type** (Level 2)
   - canHaveChildren: true
   - parentLevel1EntityClass: "CActivity" (can have Epic parents)
   - parentLevel2EntityClass: null
   - parentLevel3EntityClass: null
   - parentLevel4EntityClass: null

3. **User Story Type** (Level 3)
   - canHaveChildren: true
   - parentLevel1EntityClass: "CActivity" (Epic level)
   - parentLevel2EntityClass: "CActivity" (Feature level)
   - parentLevel3EntityClass: null
   - parentLevel4EntityClass: null

4. **Task Type** (Level 4)
   - canHaveChildren: false (Tasks cannot have children)
   - parentLevel1EntityClass: "CActivity" (Epic level)
   - parentLevel2EntityClass: "CActivity" (Feature level)
   - parentLevel3EntityClass: "CActivity" (User Story level)
   - parentLevel4EntityClass: null

## Database Schema

### cparentchildrelation Table

```sql
CREATE TABLE cparentchildrelation (
    id BIGINT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    child_type VARCHAR(32) NOT NULL,
    parent_id BIGINT NOT NULL,
    parent_type VARCHAR(32) NOT NULL,
    CONSTRAINT uk_parentchild UNIQUE (child_id, child_type, parent_id, parent_type)
);
```

### CTypeEntity Columns Added

```sql
ALTER TABLE cactivitytype ADD COLUMN can_have_children BOOLEAN DEFAULT true;
ALTER TABLE cactivitytype ADD COLUMN parent_level1_entity_class VARCHAR(100);
ALTER TABLE cactivitytype ADD COLUMN parent_level2_entity_class VARCHAR(100);
ALTER TABLE cactivitytype ADD COLUMN parent_level3_entity_class VARCHAR(100);
ALTER TABLE cactivitytype ADD COLUMN parent_level4_entity_class VARCHAR(100);
```

## Usage Patterns

### Creating a Hierarchy

```java
// 1. Create Epic
CActivity epic = new CActivity("Project Alpha", project);
epic.setEntityType(epicType);
activityService.save(epic);

// 2. Create Feature under Epic
CActivity feature = new CActivity("User Management", project);
feature.setEntityType(featureType);
activityService.save(feature);
parentChildService.setParent(feature, epic);

// 3. Create User Story under Feature
CActivity userStory = new CActivity("Login Screen", project);
userStory.setEntityType(userStoryType);
activityService.save(userStory);
parentChildService.setParent(userStory, feature);

// 4. Create Task under User Story
CActivity task = new CActivity("Design login form", project);
task.setEntityType(taskType);
activityService.save(task);
parentChildService.setParent(task, userStory);
```

### Navigating the Hierarchy

```java
// Get parent
Optional<CProjectItem<?>> parent = parentChildService.getParent(task);
if (parent.isPresent()) {
    System.out.println("Parent: " + parent.get().getName());
}

// Get all children
List<CProjectItem<?>> children = parentChildService.getChildren(epic);
System.out.println("Epic has " + children.size() + " direct children");

// Get all descendants (recursive)
// Use getChildren recursively for full tree
```

### Gantt Chart Integration

The hierarchy can be displayed in Gantt charts with parent-child indentation:

```java
// In Gantt view rendering
List<CProjectItem<?>> allItems = getAllProjectItems();
for (CProjectItem<?> item : allItems) {
    int level = calculateHierarchyLevel(item);
    renderGanttRow(item, level * INDENT_SIZE);
}

private int calculateHierarchyLevel(CProjectItem<?> item) {
    int level = 0;
    CProjectItem<?> current = item;
    while (current.hasParent()) {
        level++;
        Optional<CProjectItem<?>> parent = parentChildService.getParent(current);
        if (parent.isEmpty()) break;
        current = parent.get();
    }
    return level;
}
```

## Validation Rules

1. **Self-Parent Prevention**: An item cannot be its own parent
2. **Circular Dependency Prevention**: Setting a parent must not create a cycle in the hierarchy
3. **Type Validation**: Parent item's type must allow children (canHaveChildren = true)
4. **Persistence Requirement**: Both parent and child must be persisted (have IDs)
5. **Level Configuration**: Child's type must have appropriate parent level configured

## Best Practices

1. **Configure Types First**: Set up entity types with proper hierarchy levels before creating items
2. **Use Meaningful Names**: Use clear level names (Epic, Feature, Story, Task) for your organization
3. **Limit Depth**: While 4 levels are supported, most teams use 2-3 levels effectively
4. **Validate Early**: Check circular dependencies before attempting to set parents
5. **Update Gantt Views**: Ensure Gantt chart views respect the hierarchy for proper visualization
6. **Document Your Hierarchy**: Clearly document your organization's hierarchy levels and usage

## Known Limitations

1. **Single Parent Only**: Each item can have only one parent (no multiple inheritance)
2. **Same Project Only**: Parent and child must be in the same project
3. **Type-Based Filtering**: Hierarchy levels are configured per entity type, not per individual item
4. **Performance**: Deep hierarchies (many descendants) may impact query performance

## Future Enhancements

1. **Bulk Parent Assignment**: Assign parent to multiple items at once
2. **Hierarchy Visualization**: Tree view of the entire project hierarchy
3. **Move with Children**: Option to move an item and all its descendants together
4. **Hierarchy Reports**: Reports showing hierarchy statistics and depth analysis
5. **Gantt Auto-Scheduling**: Auto-schedule child items based on parent dates

## Troubleshooting

### "Cannot set this parent: would create a circular dependency"
- One of the proposed parent's ancestors is the child item itself
- Solution: Choose a different parent or restructure the hierarchy

### "The selected item type cannot have children"
- The parent item's type has canHaveChildren = false
- Solution: Change the type's configuration or choose a different parent

### "No hierarchical levels configured for this item type"
- The child item's type has no parent level configuration
- Solution: Configure parent levels in the type settings

### Parent Selection Dialog Shows No Items
- Check that items of the configured parent types exist in the project
- Verify that the project is set correctly on the child item
- Ensure parent types allow children

## Migration from Existing Projects

For projects created before this feature:

1. **Type Configuration**: Configure hierarchy levels for all relevant types
2. **Existing Relationships**: Check for any existing parent_id/parent_type data
3. **Validation**: Run validation to ensure no circular dependencies exist
4. **CParentChildRelation**: Create CParentChildRelation records for existing parent relationships
5. **Testing**: Thoroughly test hierarchy operations before full rollout

## Summary

The parent-child hierarchy feature provides flexible, Jira-like work item organization with:
- ✅ Up to 4 levels of hierarchy
- ✅ Type-based configuration
- ✅ Circular dependency prevention
- ✅ User-friendly dialog for parent selection
- ✅ Integration with Gantt charts
- ✅ Extensible architecture for future enhancements

This feature enables teams to organize complex projects with clear hierarchical relationships between work items, improving planning and tracking capabilities.
