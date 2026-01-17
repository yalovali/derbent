# Entity Clone and Copy Pattern

## Overview

The Derbent application provides a comprehensive entity cloning and copying system that allows users to duplicate entities with configurable options. This document describes both patterns:

1. **Clone Pattern**: Duplicates entities to the same type
2. **CopyTo Pattern**: Copies entity fields to different types (cross-type copying)

Both patterns use the same `CCloneOptions` configuration and are integrated into the UI through a unified "Copy To" button.

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
public EntityClass createClone(final CCloneOptions options) throws Exception {
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
public CActivity createClone(final CCloneOptions options) throws Exception {
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

---

## CopyTo Pattern (Cross-Type Copying)

### Overview

The `copyEntityTo` pattern allows copying fields from one entity to another, even if they are different types. This enables flexible cross-type data transfer while maintaining type safety.

### Core Implementation

The pattern uses:
1. **Interface default methods** for shared field copying
2. **Getter/setter reflection** for type-safe field access
3. **Interface detection** to skip incompatible fields automatically
4. **Same CCloneOptions** as clone pattern for consistency

### Pattern Architecture

```java
// Base implementation in CEntityDB
@Override
public <T extends CEntityDB> void copyEntityTo(final T target, final CCloneOptions options) {
    // Copy ID and active status
    target.setId(this.getId());
    target.setActive(this.isActive());
}
```

### Interface-Based Copying

Each domain interface provides a static copy method:

```java
public interface IHasName {
    String getName();
    void setName(String name);
    
    // Static copy method - automatically used by copyEntityTo
    static <T> void copyIHasNameTo(final IHasName source, final T target, final CCloneOptions options) {
        if (target instanceof IHasName targetWithName) {
            targetWithName.setName(source.getName());
        }
        // If target doesn't implement IHasName, silently skip
    }
}
```

### Recursive Copy Implementation

Entities override `copyEntityTo` to copy their specific fields:

```java
@Override
public <T extends CEntityDB> void copyEntityTo(final T target, final CCloneOptions options) {
    // 1. Always call super first
    super.copyEntityTo(target, options);
    
    // 2. Copy basic fields using setters (type-safe)
    if (target instanceof CActivity targetActivity) {
        targetActivity.setAcceptanceCriteria(this.getAcceptanceCriteria());
        targetActivity.setActualCost(this.getActualCost());
        targetActivity.setPriority(this.getPriority());
    }
    
    // 3. Interface methods are called automatically by parent classes
    // NO need to manually call IHasName.copyIHasNameTo(), etc.
}
```

### Key Rules for copyEntityTo Implementation

✅ **ALWAYS DO:**
- Call `super.copyEntityTo(target, options)` FIRST
- Use getters/setters for all field access
- Type-check with `instanceof` before casting
- Let interfaces handle their own fields (don't duplicate)
- Respect CCloneOptions flags for dates, assignments, etc.

❌ **NEVER DO:**
- Access fields directly (`this.field = target.field`)
- Manually call interface copy methods (`IHasName.copyIHasNameTo(...)`)
- Copy fields that interfaces already handle
- Skip `super.copyEntityTo()` call
- Ignore type safety checks

### Complete Example

```java
// CActivity implementation
@Override
public <T extends CEntityDB> void copyEntityTo(final T target, final CCloneOptions options) {
    // Step 1: Call super (copies inherited fields + calls interface methods)
    super.copyEntityTo(target, options);
    
    // Step 2: Copy activity-specific fields
    if (target instanceof CActivity targetActivity) {
        targetActivity.setAcceptanceCriteria(this.getAcceptanceCriteria());
        targetActivity.setActualCost(this.getActualCost());
        targetActivity.setActualHours(this.getActualHours());
        targetActivity.setPriority(this.getPriority());
        targetActivity.setActivityType(this.getActivityType());
        
        // Respect options for workflow
        if (options.isCloneWorkflow() && this.getWorkflow() != null) {
            targetActivity.setWorkflow(this.getWorkflow());
        }
        
        // Collections (optional based on depth)
        if (options.includesComments() && this.getComments() != null) {
            // Note: Collections may need special handling
            targetActivity.setComments(new HashSet<>(this.getComments()));
        }
    }
    // If target is not CActivity, only inherited fields are copied
}
```

### Interface Copy Methods Already Implemented

All these interfaces have `copy*To()` methods that work automatically:

- `IHasName` → `copyIHasNameTo()`
- `IHasDescription` → `copyIHasDescriptionTo()`
- `IHasStartEnd` → `copyIHasStartEndTo()`
- `IHasAssignedTo` → `copyIHasAssignedToTo()`
- `IHasParent` → `copyIHasParentTo()`
- `IHasDragControl` → `copyIHasDragControlTo()`
- `IHasColor` → `copyIHasColorTo()`
- `IHasIcon` → `copyIHasIconTo()`

### Benefits of CopyTo Pattern

1. **Cross-Type Flexibility**: Copy from Activity to Meeting, etc.
2. **Type Safety**: Compile-time checks via generics
3. **Code Reduction**: Interfaces handle their own copying
4. **Maintainability**: Single source of truth per interface
5. **Extensibility**: Easy to add new copyable fields
6. **Fail-Soft**: Missing interfaces are silently skipped

### UI Integration

The "Copy To" button replaces the old "Clone" button:

```java
// In CCrudToolbar
private Button createCopyToButton() {
    final Button button = new Button("Copy To", VaadinIcon.COPY.create());
    button.addClickListener(e -> {
        if (selectedEntity != null) {
            pageService.actionCopyTo(selectedEntity);
        }
    });
    return button;
}
```

Dialog allows selecting target type:
- Same type (traditional clone)
- Different type (cross-type copy)
- All CCloneOptions apply equally

## Implementation Checklist

When adding clone/copy support to a new entity:

### Clone Pattern Checklist
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

### CopyTo Pattern Checklist
- [ ] Override `copyEntityTo(T target, CCloneOptions options)` method
- [ ] **ALWAYS call `super.copyEntityTo(target, options)` FIRST**
- [ ] Use `instanceof` for type checking before copying
- [ ] Use getters/setters ONLY (never direct field access)
- [ ] **DO NOT manually call interface copy methods** (they're called by super)
- [ ] Copy only entity-specific fields (not interface fields)
- [ ] Respect CCloneOptions for dates, assignments, workflow, etc.
- [ ] Handle collections carefully (may need special logic)
- [ ] Test cross-type copying with different target types
- [ ] Verify that interface fields are copied correctly via super call

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

## Quality Matrix Integration

The Code Quality Matrix (`docs/CODE_QUALITY_MATRIX.xlsx`) tracks compliance with these patterns:

### Tracked Metrics

| Column | Metric | Expected Value |
|--------|--------|----------------|
| 12 | copyEntityTo() Override | ✓ for all entities |
| 13 | Calls super.copyEntityTo() | ✓ for all non-base entities |
| 14 | No Manual Interface Calls | ✓ (no manual `copyIHas*To()` calls) |
| 15 | Interface Copy Method | ✓ for all interfaces with copyable fields |

### Validation Rules

✅ **PASS Criteria:**
- Entity overrides `copyEntityTo()`
- First line is `super.copyEntityTo(target, options)`
- No manual calls to interface copy methods
- Uses getters/setters only
- Type-checks with `instanceof`

✗ **FAIL Criteria:**
- Missing `copyEntityTo()` override
- Direct field access (`this.field`)
- Manual interface method calls
- Missing `super.copyEntityTo()` call
- No type safety checks

---

**Version**: 2.0  
**Last Updated**: 2026-01-17  
**Changes**: Added CopyTo pattern, interface-based copying, quality matrix integration  
**Author**: GitHub Copilot Agent
