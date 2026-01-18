# CopyTo Pattern - Mandatory Coding Rule

## Overview
All entity classes **MUST** implement the `copyEntityTo()` method to support the CopyTo/Clone functionality. This is a **MANDATORY** pattern for all entities in the Derbent codebase.

## Why This Matters
- Enables cross-type entity copying (e.g., User → Activity, Meeting → Issue)
- Supports flexible cloning with configurable options
- Maintains data integrity by explicitly handling each field
- Prevents validation errors for required/unique fields
- Works seamlessly with the Copy dialog (`CDialogClone`)

## The Pattern Hierarchy

### 1. Base Class: CEntityDB
```java
// CEntityDB provides the foundation
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // Copy active field (always)
    copyField(this::getActive, target::setActive);
    
    // Automatically copy interface fields
    IHasComments.copyCommentsTo(this, target, options);
    IHasAttachments.copyAttachmentsTo(this, target, options);
    IHasStatusAndWorkflow.copyStatusAndWorkflowTo(this, target, options);
}
```

### 2. Named Entities: CEntityNamed
```java
// CEntityNamed adds name and description
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // ALWAYS call parent first!
    super.copyEntityTo(target, options);
    
    // Copy named fields if target supports them
    if (target instanceof CEntityNamed) {
        final CEntityNamed<?> targetNamed = (CEntityNamed<?>) target;
        copyField(this::getName, targetNamed::setName);
        copyField(this::getDescription, targetNamed::setDescription);
        
        // Copy dates based on options
        if (!options.isResetDates()) {
            copyField(this::getCreatedDate, (d) -> targetNamed.createdDate = d);
            copyField(this::getLastModifiedDate, (d) -> targetNamed.lastModifiedDate = d);
        }
    }
}
```

### 3. Project Items: CProjectItem
```java
// CProjectItem adds parent relationships and status
@Override
public EntityClass createClone(final CCloneOptions options) throws Exception {
    final EntityClass clone = super.createClone(options);
    if (clone instanceof CProjectItem) {
        final CProjectItem<?> cloneItem = (CProjectItem<?>) clone;
        
        // Clone parent relationships if requested
        if (options.includesRelations()) {
            cloneItem.parentId = this.getParentId();
            cloneItem.parentType = this.getParentType();
        }
        
        // Clone status if requested
        if (options.isCloneStatus() && this.getStatus() != null) {
            cloneItem.status = this.getStatus();
        }
    }
    return clone;
}
```

### 4. Concrete Entity: YOUR ENTITY
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // STEP 1: ALWAYS call parent first!
    super.copyEntityTo(target, options);
    
    // STEP 2: Check if target is the correct type
    if (target instanceof YourEntity) {
        final YourEntity targetEntity = (YourEntity) target;
        
        // STEP 3: Copy YOUR entity-specific fields
        // ... (see templates below)
    }
}
```

## Mandatory Template for ALL Entities

```java
/**
 * Copies entity fields to target entity.
 * MANDATORY: All entities must override this method.
 * 
 * @param target  The target entity
 * @param options Clone options to control copying behavior
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // RULE 1: ALWAYS call parent first
    super.copyEntityTo(target, options);
    
    // RULE 2: Type-check and cast
    if (target instanceof YourEntityClass) {
        final YourEntityClass targetEntity = (YourEntityClass) target;
        
        // RULE 3: Copy basic fields (always)
        copyField(this::getYourField1, targetEntity::setYourField1);
        copyField(this::getYourField2, targetEntity::setYourField2);
        
        // RULE 4: Handle unique/required fields specially
        // Make them unique to avoid validation errors
        if (this.getEmail() != null) {
            targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
        }
        if (this.getLogin() != null) {
            targetEntity.setLogin(this.getLogin() + "_copy");
        }
        
        // RULE 5: Handle dates based on options
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetEntity::setDueDate);
            copyField(this::getStartDate, targetEntity::setStartDate);
        }
        
        // RULE 6: Handle relations based on options
        if (options.includesRelations()) {
            copyField(this::getRelatedEntity, targetEntity::setRelatedEntity);
        }
        
        // RULE 7: Handle collections based on options
        if (options.includesRelations()) {
            copyCollection(this::getChildren, 
                targetEntity::setChildren, 
                true); // createNew = true for new collection
        }
        
        // RULE 8: DON'T copy sensitive fields
        // Password, tokens, session data, etc. must NOT be copied
        
        // RULE 9: DON'T copy auto-generated fields
        // IDs, audit fields, computed values handled by base class
        
        // RULE 10: Log for debugging
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

## Field Handling Rules

### ✅ ALWAYS Copy These Fields:
- Basic data fields (name, description, notes, text fields)
- Numeric fields (amounts, quantities, percentages)
- Boolean flags (except security/state flags)
- Enum values (type, category, priority)
- Reference IDs (when `options.includesRelations()`)

### ⚠️ CONDITIONAL Copy (Check Options):
- **Dates**: Only if `!options.isResetDates()`
- **Relations**: Only if `options.includesRelations()`
- **Status**: Only if `options.isCloneStatus()`
- **Workflow**: Only if `options.isCloneWorkflow()`
- **Attachments**: Handled by `IHasAttachments.copyAttachmentsTo()`
- **Comments**: Handled by `IHasComments.copyCommentsTo()`

### ❌ NEVER Copy These Fields:
- **ID fields** (id, primary keys) - handled by JPA
- **Passwords** - security risk
- **Tokens/API Keys** - security risk
- **Session data** - temporary state
- **Audit fields** (createdBy, lastModifiedBy) - set by system
- **Unique constraints** - must be made unique
- **File data** - unless specifically requested
- **Profile pictures** - privacy concern

### 🔧 SPECIAL Handling Required:

#### 1. Unique Fields (Email, Login, Username)
```java
// Make unique to avoid constraint violations
if (this.getEmail() != null) {
    targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
}
if (this.getLogin() != null) {
    targetEntity.setLogin(this.getLogin() + "_copy");
}
```

#### 2. Required Fields (NotNull, NotBlank)
```java
// Always copy, never leave null if source has value
copyField(this::getRequiredField, targetEntity::setRequiredField);

// Or provide default if source is null
targetEntity.setRequiredField(
    this.getRequiredField() != null 
        ? this.getRequiredField() 
        : "Default Value"
);
```

#### 3. Collections (OneToMany, ManyToMany)
```java
// Use copyCollection with createNew=true for new instances
if (options.includesRelations()) {
    copyCollection(
        this::getChildren, 
        (col) -> targetEntity.children = (Set<Child>) col, 
        true  // createNew = true - create new collection instance
    );
}
```

#### 4. Parent/Child Relationships
```java
// Only copy if relations are included
if (options.includesRelations()) {
    copyField(this::getParentId, targetEntity::setParentId);
    copyField(this::getParentType, targetEntity::setParentType);
}
```

## Interface-Based Copy Helpers

Interfaces should provide static helper methods for copying their fields:

```java
public interface IYourInterface {
    
    // Regular interface methods
    YourType getYourField();
    void setYourField(YourType value);
    
    /**
     * Copy interface fields from source to target.
     * PATTERN: Static helper method for interface field copying.
     * 
     * @param source the source entity
     * @param target the target entity
     * @param options copy options
     * @return true if copied, false if skipped
     */
    static boolean copyYourFieldsTo(
            final CEntityDB<?> source, 
            final CEntityDB<?> target, 
            final CCloneOptions options) {
        
        // Check if both implement interface
        if (!(source instanceof IYourInterface) 
                || !(target instanceof IYourInterface)) {
            return false;
        }
        
        // Check options if field is conditional
        if (!options.yourCondition()) {
            return false;
        }
        
        try {
            final IYourInterface sourceWith = (IYourInterface) source;
            final IYourInterface targetWith = (IYourInterface) target;
            
            // Copy using copyField or copyCollection
            source.copyField(sourceWith::getYourField, targetWith::setYourField);
            
            return true;
        } catch (final Exception e) {
            // Log and skip - don't fail entire copy
            return false;
        }
    }
}
```

Then call in base class:
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    // Call interface copy helpers
    IYourInterface.copyYourFieldsTo(this, target, options);
}
```

## Example Implementations

### Example 1: CUser (Unique Fields)
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof CUser) {
        final CUser targetUser = (CUser) target;
        
        // Make unique fields unique
        if (this.email != null) {
            targetUser.setEmail(this.email.replace("@", "+copy@"));
        }
        if (this.login != null) {
            targetUser.setLogin(this.login + "_copy");
        }
        
        // Copy non-sensitive fields
        copyField(this::getLastname, targetUser::setLastname);
        copyField(this::getPhone, targetUser::setPhone);
        copyField(this::getColor, targetUser::setColor);
        
        // DON'T copy password, profile pictures, or roles
    }
}
```

### Example 2: CActivity (Full Implementation)
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof CActivity) {
        final CActivity targetActivity = (CActivity) target;
        
        // Copy basic fields
        copyField(this::getAcceptanceCriteria, targetActivity::setAcceptanceCriteria);
        copyField(this::getNotes, targetActivity::setNotes);
        copyField(this::getResults, targetActivity::setResults);
        
        // Copy numeric fields
        copyField(this::getActualCost, targetActivity::setActualCost);
        copyField(this::getActualHours, targetActivity::setActualHours);
        copyField(this::getEstimatedCost, targetActivity::setEstimatedCost);
        copyField(this::getEstimatedHours, targetActivity::setEstimatedHours);
        
        // Copy type and priority
        copyField(this::getPriority, targetActivity::setPriority);
        copyField(this::getEntityType, targetActivity::setEntityType);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetActivity::setDueDate);
            copyField(this::getStartDate, targetActivity::setStartDate);
            copyField(this::getCompletionDate, targetActivity::setCompletionDate);
        }
        
        // Note: Comments, attachments, status/workflow handled by base class
        // Note: Sprint relationships NOT copied (starts outside sprint)
        
        LOGGER.debug("Successfully copied activity '{}' with options: {}", getName(), options);
    }
}
```

### Example 3: CMeeting (With Collections)
```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof CMeeting) {
        final CMeeting targetMeeting = (CMeeting) target;
        
        // Copy basic fields
        copyField(this::getAgenda, targetMeeting::setAgenda);
        copyField(this::getLocation, targetMeeting::setLocation);
        copyField(this::getMinutes, targetMeeting::setMinutes);
        copyField(this::getEntityType, targetMeeting::setEntityType);
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getRelatedActivity, targetMeeting::setRelatedActivity);
            
            // Collections - create new instances
            copyCollection(this::getAttendees, 
                (a) -> targetMeeting.attendees = (Set<CUser>) a, 
                true);
            copyCollection(this::getParticipants, 
                (p) -> targetMeeting.participants = (Set<CUser>) p, 
                true);
        }
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getStartDate, targetMeeting::setStartDate);
            copyField(this::getStartTime, targetMeeting::setStartTime);
            copyField(this::getEndDate, targetMeeting::setEndDate);
            copyField(this::getEndTime, targetMeeting::setEndTime);
        }
    }
}
```

## Checklist for New Entities

When creating a new entity, you **MUST**:

- [ ] Override `copyEntityTo(final CEntityDB<?> target, final CCloneOptions options)`
- [ ] Call `super.copyEntityTo(target, options)` FIRST
- [ ] Type-check target: `if (target instanceof YourEntity)`
- [ ] Copy ALL basic fields using `copyField(getter, setter)`
- [ ] Handle unique fields (make them unique with suffix/prefix)
- [ ] Handle required fields (ensure never null if source has value)
- [ ] Handle conditional fields (check options for dates, relations)
- [ ] Handle collections (use `copyCollection` with `createNew=true`)
- [ ] DON'T copy sensitive fields (passwords, tokens)
- [ ] DON'T copy auto-generated fields (IDs, audit fields)
- [ ] Add debug logging at the end
- [ ] Test copying to same type
- [ ] Test copying to different type (if applicable)

## Testing Your Implementation

### Test 1: Copy to Same Type
```java
@Test
public void testCopyToSameType() {
    final YourEntity source = createTestEntity();
    final CCloneOptions options = new CCloneOptions.Builder().build();
    
    final YourEntity copy = source.copyTo(YourEntity.class, options);
    
    assertNotNull(copy);
    assertNotEquals(source.getId(), copy.getId());
    assertEquals(source.getName(), copy.getName());
    // Assert all fields copied correctly
}
```

### Test 2: Copy to Different Type
```java
@Test
public void testCopyToDifferentType() {
    final YourEntity source = createTestEntity();
    final CCloneOptions options = new CCloneOptions.Builder().build();
    
    final OtherEntity copy = source.copyTo(OtherEntity.class, options);
    
    assertNotNull(copy);
    assertEquals(source.getName(), copy.getName());
    assertEquals(source.getDescription(), copy.getDescription());
    // Assert common fields copied
}
```

### Test 3: Unique Fields Made Unique
```java
@Test
public void testUniqueFieldsHandled() {
    final CUser source = new CUser();
    source.setEmail("user@example.com");
    source.setLogin("john");
    
    final CUser copy = source.copyTo(CUser.class, new CCloneOptions.Builder().build());
    
    assertEquals("user+copy@example.com", copy.getEmail());
    assertEquals("john_copy", copy.getLogin());
}
```

## Common Mistakes to Avoid

### ❌ Mistake 1: Forgetting to Call Super
```java
// WRONG - will lose parent fields!
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    if (target instanceof MyEntity) {
        // ... copy my fields
    }
}
```

```java
// CORRECT
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options); // ✅ Always call parent first!
    if (target instanceof MyEntity) {
        // ... copy my fields
    }
}
```

### ❌ Mistake 2: Not Type-Checking
```java
// WRONG - will fail if copying to different type!
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    final MyEntity targetEntity = (MyEntity) target; // ❌ ClassCastException!
    // ...
}
```

```java
// CORRECT
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    if (target instanceof MyEntity) { // ✅ Type-safe
        final MyEntity targetEntity = (MyEntity) target;
        // ...
    }
}
```

### ❌ Mistake 3: Copying Sensitive Fields
```java
// WRONG - security risk!
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    if (target instanceof CUser) {
        final CUser targetUser = (CUser) target;
        copyField(this::getPassword, targetUser::setPassword); // ❌ DON'T copy passwords!
    }
}
```

### ❌ Mistake 4: Not Making Unique Fields Unique
```java
// WRONG - will cause constraint violation!
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    if (target instanceof CUser) {
        final CUser targetUser = (CUser) target;
        copyField(this::getEmail, targetUser::setEmail); // ❌ Duplicate email!
    }
}
```

```java
// CORRECT
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    if (target instanceof CUser) {
        final CUser targetUser = (CUser) target;
        if (this.email != null) {
            targetUser.setEmail(this.email.replace("@", "+copy@")); // ✅ Make unique
        }
    }
}
```

## Summary

The CopyTo pattern is **MANDATORY** for all entities. It:

1. ✅ Enables flexible entity copying and cloning
2. ✅ Supports cross-type copying (User → Activity, etc.)
3. ✅ Handles unique and required fields correctly
4. ✅ Respects copy options (dates, relations, status)
5. ✅ Works with interfaces for common fields
6. ✅ Maintains data integrity
7. ✅ Provides clear debugging information

**Remember**: 
- Always call `super.copyEntityTo()` first
- Always type-check before casting
- Handle unique fields specially
- Don't copy sensitive data
- Test both same-type and cross-type copying

This is not optional - it's a **core architectural pattern** that must be followed for all entities in the Derbent codebase.
