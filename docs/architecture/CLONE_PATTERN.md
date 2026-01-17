# Entity Clone Pattern

## Overview

The Derbent application provides a comprehensive entity cloning system that allows users to duplicate entities with configurable options. This document describes the clone pattern implementation and how to implement cloning for new entities.

## Architecture

### Core Components

1. **ICloneable Interface** (`tech.derbent.api.interfaces.ICloneable`)
   - Defines the contract for cloneable entities
   - Requires `createClone(CCloneOptions options)` method
   - Enforced at `CEntityDB` level (all entities must implement)

2. **CCloneOptions** (`tech.derbent.api.interfaces.CCloneOptions`)
   - Configuration object for clone operations
   - Builder pattern for easy construction
   - Five clone depth levels
   - Flags for status, workflow, dates, and assignments

3. **CDialogClone** (`tech.derbent.api.ui.dialogs.CDialogClone`)
   - User interface for configuring clone options
   - Displays checkboxes and selectors for all options
   - Validates input and performs the clone operation

4. **CRUD Toolbar Integration** (`tech.derbent.api.ui.component.enhanced.CCrudToolbar`)
   - Clone button between Delete and Refresh
   - Enabled when an entity is selected
   - Triggers `actionClone()` in page service

## Clone Depth Levels

```java
public enum CloneDepth {
    BASIC_ONLY,          // Name, description, numbers only
    WITH_RELATIONS,      // + parent/child relationships
    WITH_ATTACHMENTS,    // + file attachments
    WITH_COMMENTS,       // + comment history
    FULL_DEEP_CLONE      // Everything including collections
}
```

## Implementation Pattern

### Recursive Pattern

Each entity class implements `createClone()` following this pattern:

```java
@Override
public EntityClass createClone(final CCloneOptions options) throws CloneNotSupportedException {
    // 1. Call parent's createClone() to clone inherited fields
    final EntityClass clone = super.createClone(options);
    
    // 2. Clone own basic fields (always included)
    clone.myField1 = this.myField1;
    clone.myField2 = this.myField2;
    
    // 3. Clone relations based on options
    if (options.includesRelations()) {
        clone.parent = this.parent;
        clone.children = new HashSet<>(this.children);
    }
    
    // 4. Handle dates based on options
    if (!options.isResetDates()) {
        clone.startDate = this.startDate;
        clone.endDate = this.endDate;
    }
    
    // 5. Handle assignments based on options
    if (!options.isResetAssignments()) {
        clone.assignedTo = this.assignedTo;
    }
    
    // 6. Clone collections recursively if requested
    if (options.includesComments() && this.comments != null) {
        clone.comments = new HashSet<>();
        for (final CComment comment : this.comments) {
            clone.comments.add(comment.createClone(options));
        }
    }
    
    // 7. Return the clone
    return clone;
}
```

### Entity Hierarchy

The clone pattern is implemented across the entity hierarchy:

```
CEntityDB (base)
    ↓ (clones: active field)
CEntityNamed
    ↓ (clones: name, description, dates)
CEntityOfProject
    ↓ (clones: project, assignedTo, createdBy)
CProjectItem
    ↓ (clones: parent relations, status)
CActivity / CMeeting / etc.
    ↓ (clones: entity-specific fields)
```

## Reference Implementations

### CActivity (Complete Example)

```java
@Override
public CActivity createClone(final CCloneOptions options) throws CloneNotSupportedException {
    // Get parent's clone
    final CActivity clone = super.createClone(options);

    // Always clone basic fields
    clone.acceptanceCriteria = this.acceptanceCriteria;
    clone.actualCost = this.actualCost;
    clone.actualHours = this.actualHours;
    clone.priority = this.priority;
    clone.activityType = this.activityType;
    
    // Optional: Clone workflow
    if (options.isCloneWorkflow()) {
        clone.workflow = this.workflow;
    }
    
    // Optional: Clone dates
    if (!options.isResetDates()) {
        clone.dueDate = this.dueDate;
        clone.plannedStartDate = this.plannedStartDate;
        clone.plannedEndDate = this.plannedEndDate;
    }
    
    // Optional: Clone comments
    if (options.includesComments() && this.comments != null) {
        clone.comments = new HashSet<>();
        for (final CComment comment : this.comments) {
            try {
                clone.comments.add(comment.createClone(options));
            } catch (final Exception e) {
                LOGGER.warn("Could not clone comment: {}", e.getMessage());
            }
        }
    }
    
    // Optional: Clone attachments
    if (options.includesAttachments() && this.attachments != null) {
        clone.attachments = new HashSet<>();
        for (final CAttachment attachment : this.attachments) {
            try {
                clone.attachments.add(attachment.createClone(options));
            } catch (final Exception e) {
                LOGGER.warn("Could not clone attachment: {}", e.getMessage());
            }
        }
    }
    
    return clone;
}
```

### CMeeting (Simplified Example)

See `CMeeting.createClone()` for another complete implementation demonstrating:
- Meeting-specific fields (agenda, location, minutes)
- Attendees and participants (relation sets)
- Related activities (full deep clone only)

## Important Rules

### What Gets Cloned

✅ **Always cloned:**
- Basic fields (strings, numbers, booleans)
- Entity type references
- Active status

✅ **Optionally cloned (based on options):**
- Parent/child relationships (`includesRelations()`)
- File attachments (`includesAttachments()`)
- Comments (`includesComments()`)
- Status field (`isCloneStatus()`)
- Workflow field (`isCloneWorkflow()`)
- Date fields (`!isResetDates()`)
- Assignment fields (`!isResetAssignments()`)

❌ **Never cloned:**
- Entity ID (always null - new entity gets fresh ID)
- Sprint item relationships (clone starts outside sprint)
- Widget entities (created separately if needed)

### Error Handling

- Log warnings for failed collection item clones
- Continue cloning remaining items
- Throw `CloneNotSupportedException` for fatal errors
- Show user-friendly error in UI via `CNotificationService`

### Initialization After Clone

The page service automatically calls `initializeNewEntity()` after cloning:

```java
// In CPageService.actionClone()
final EntityClass clone = cloneable.createClone(options);
getEntityService().initializeNewEntity(clone);  // Sets status, workflow, etc.
getEntityService().save(clone);
```

## User Experience

### Clone Dialog

When user clicks the Clone button:

1. Dialog opens showing current entity name
2. User configures:
   - New name (pre-filled with "EntityName (Clone)")
   - Clone depth (dropdown with 5 options)
   - Clone status (checkbox)
   - Clone workflow (checkbox)
   - Reset dates (checkbox, default checked)
   - Reset assignments (checkbox, default checked)
3. User clicks Save
4. Entity is cloned with selected options
5. Clone is initialized and saved
6. UI refreshes to show the new clone

### Clone Button

- Located in CRUD toolbar between Delete and Refresh
- Enabled when an entity is selected
- Uses copy icon (VaadinIcon.COPY)
- Tooltip: "Clone current entity"

## Implementation Checklist

When adding clone support to a new entity:

- [ ] Add import: `import tech.derbent.api.interfaces.CCloneOptions;`
- [ ] Override `createClone(CCloneOptions options)` method
- [ ] Call `super.createClone(options)` first
- [ ] Clone all basic fields (strings, numbers, enums)
- [ ] Clone type entity references
- [ ] Use `options.includesRelations()` for parent/child
- [ ] Use `!options.isResetDates()` for date fields
- [ ] Use `!options.isResetAssignments()` for user references
- [ ] Clone collections recursively with try-catch
- [ ] Add JavaDoc explaining what gets cloned
- [ ] Test the clone functionality manually
- [ ] Add unit tests if applicable

## Benefits

1. **Consistent Pattern**: All entities follow the same cloning approach
2. **User Control**: Users decide what to include in clones
3. **Type Safety**: Compile-time enforcement via interface
4. **Flexibility**: Easy to customize clone behavior per entity
5. **Maintainability**: Clear separation of concerns
6. **Extensibility**: Easy to add new clone options

## Related Documentation

- [Coding Standards](coding-standards.md) - General coding patterns
- [Entity Inheritance Patterns](ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md) - Entity hierarchy
- [Service Layer Patterns](service-layer-patterns.md) - Service implementation
- [View Layer Patterns](view-layer-patterns.md) - UI integration

---

**Version**: 1.0  
**Last Updated**: 2026-01-17  
**Author**: GitHub Copilot Agent
