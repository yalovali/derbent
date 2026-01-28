# Service-Based CopyTo Pattern - MANDATORY Coding Rule

## Date: 2026-01-28
## Status: ACTIVE - All entities must follow this pattern

## Overview

The CopyTo pattern has been refactored to **move business logic from entity classes to service classes**. This follows the principle of separation of concerns and leverages Spring's service layer for dependency injection and business logic.

## Why Service-Based?

### Problems with Entity-Based Copy
- ❌ Entity classes contained too much business logic
- ❌ Duplication across entity hierarchy
- ❌ Difficult to maintain and test
- ❌ Hard to access other services during copy
- ❌ Mixed concerns (persistence + business logic)

### Benefits of Service-Based Copy  
- ✅ Clean separation of concerns
- ✅ Service layer handles business logic
- ✅ Easy to inject dependencies
- ✅ Reusable across entity hierarchy
- ✅ Easy to test services independently
- ✅ Follows Spring best practices

## Architecture

### Three-Layer Pattern

```
┌─────────────────────────────────────────────────────┐
│  Entity Layer (Minimal)                              │
│  copyEntityTo() → delegates to parent                │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│  Base Entity Layer (CEntityDB)                       │
│  1. Copy base fields (active)                        │
│  2. Copy interface fields (comments, attachments)    │
│  3. Delegate to service.copyEntityFieldsTo()         │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│  Service Layer (Business Logic)                      │
│  copyEntityFieldsTo() → uses getters/setters         │
│  - Type-safe field copying                           │
│  - Access to other services                          │
│  - Business rules and validations                    │
└──────────────────────────────────────────────────────┘
```

## Implementation Guide

### Step 1: Entity Class (Minimal)

**RULE**: Entity's `copyEntityTo()` should ONLY call super and add documentation.

```java
/**
 * Copies entity fields to target entity.
 * NOTE: Field copying is delegated to {YourEntity}Service.copyEntityFieldsTo()
 * 
 * @param target        The target entity
 * @param serviceTarget The service handling copy logic
 * @param options       Clone options to control copying behavior
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, 
                           @SuppressWarnings("rawtypes") final CAbstractService serviceTarget,
                           final CCloneOptions options) {
    // Always call parent first - parent handles service delegation
    super.copyEntityTo(target, serviceTarget, options);
    
    // NOTE: Entity-specific field copying is now handled by {YourEntity}Service.copyEntityFieldsTo()
    // This reduces duplication and moves business logic to the service layer
}
```

### Step 2: Service Class (Business Logic)

**RULE**: Service's `copyEntityFieldsTo()` contains ALL field copying logic.

```java
/**
 * Service-level method to copy {YourEntity}-specific fields using getters/setters.
 * This method implements the service-based copy pattern.
 * 
 * @param source  the source entity to copy from
 * @param target  the target entity to copy to
 * @param options clone options controlling what fields to copy
 */
@Override
public void copyEntityFieldsTo(final YourEntity source, 
                               final CEntityDB<?> target,
                               final CCloneOptions options) {
    // STEP 1: Always call parent first
    super.copyEntityFieldsTo(source, target, options);
    
    // STEP 2: Type-check target
    if (!(target instanceof YourEntity)) {
        return;
    }
    final YourEntity targetEntity = (YourEntity) target;
    
    // STEP 3: Copy basic fields using getters/setters
    CEntityDB.copyField(source::getField1, targetEntity::setField1);
    CEntityDB.copyField(source::getField2, targetEntity::setField2);
    CEntityDB.copyField(source::getField3, targetEntity::setField3);
    
    // STEP 4: Handle unique fields specially
    if (source.getEmail() != null) {
        targetEntity.setEmail(source.getEmail().replace("@", "+copy@"));
    }
    
    // STEP 5: Handle conditional fields (dates)
    if (!options.isResetDates()) {
        CEntityDB.copyField(source::getDueDate, targetEntity::setDueDate);
        CEntityDB.copyField(source::getStartDate, targetEntity::setStartDate);
    }
    
    // STEP 6: Handle conditional fields (relations)
    if (options.includesRelations()) {
        CEntityDB.copyField(source::getRelatedEntity, targetEntity::setRelatedEntity);
    }
    
    // STEP 7: Handle collections
    if (options.includesRelations()) {
        CEntityDB.copyCollection(
            source::getChildren, 
            targetEntity::setChildren, 
            true  // createNew = true for new collection
        );
    }
    
    // STEP 8: Log completion
    LOGGER.debug("Successfully copied {} '{}' with options: {}", 
                 getClass().getSimpleName(), source.getName(), options);
}
```

## Base Class Hierarchy

### CAbstractService (Root)

```java
/**
 * Base method to copy entity-specific fields.
 * Default implementation: no-op.
 * Override in concrete services to copy their specific fields.
 */
public void copyEntityFieldsTo(final EntityClass source, 
                               final CEntityDB<?> target, 
                               final CCloneOptions options) {
    // Default implementation: no-op
    // Concrete services override this to copy their specific fields
}
```

### CEntityNamedService

```java
@Override
public void copyEntityFieldsTo(final EntityClass source, 
                               final CEntityDB<?> target,
                               final CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    
    if (!(target instanceof CEntityNamed)) {
        return;
    }
    final CEntityNamed<?> targetNamed = (CEntityNamed<?>) target;
    
    // Copy name and description
    CEntityDB.copyField(source::getName, targetNamed::setName);
    CEntityDB.copyField(source::getDescription, targetNamed::setDescription);
    
    // Copy dates if not resetting
    if (!options.isResetDates()) {
        CEntityDB.copyField(source::getCreatedDate, targetNamed::setCreatedDate);
        CEntityDB.copyField(source::getLastModifiedDate, targetNamed::setLastModifiedDate);
    }
}
```

### CEntityOfCompanyService

```java
@Override
public void copyEntityFieldsTo(final EntityClass source, 
                               final CEntityDB<?> target,
                               final CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    
    if (!(target instanceof CEntityOfCompany)) {
        return;
    }
    final CEntityOfCompany<?> targetCompanyEntity = (CEntityOfCompany<?>) target;
    
    // Copy company reference
    CEntityDB.copyField(source::getCompany, targetCompanyEntity::setCompany);
}
```

### CProjectItemService

```java
@Override
public void copyEntityFieldsTo(final EntityClass source, 
                               final CEntityDB<?> target,
                               final CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    
    if (!(target instanceof CProjectItem)) {
        return;
    }
    final CProjectItem<?> targetProjectItem = (CProjectItem<?>) target;
    
    // Copy project and creator
    CEntityDB.copyField(source::getProject, targetProjectItem::setProject);
    CEntityDB.copyField(source::getCreatedBy, targetProjectItem::setCreatedBy);
    
    // Copy parent relationship if requested
    if (options.includesRelations()) {
        CEntityDB.copyField(source::getParentId, targetProjectItem::setParentId);
        CEntityDB.copyField(source::getParentType, targetProjectItem::setParentType);
    }
}
```

## Field Handling Rules

### ✅ ALWAYS Copy

| Field Type | Handling | Example |
|------------|----------|---------|
| **String fields** | Direct copy | `copyField(source::getName, target::setName)` |
| **Numeric fields** | Direct copy | `copyField(source::getAmount, target::setAmount)` |
| **Boolean fields** | Direct copy | `copyField(source::getActive, target::setActive)` |
| **Enum values** | Direct copy | `copyField(source::getType, target::setType)` |
| **Entity references** | Direct copy | `copyField(source::getCategory, target::setCategory)` |

### ⚠️ CONDITIONAL Copy (Check Options)

| Field Type | Condition | Example |
|------------|-----------|---------|
| **Date fields** | `!options.isResetDates()` | `if (!options.isResetDates()) { copyField(...) }` |
| **Relations** | `options.includesRelations()` | `if (options.includesRelations()) { copyField(...) }` |
| **Status** | `options.isCloneStatus()` | `if (options.isCloneStatus()) { copyField(...) }` |
| **Workflow** | `options.isCloneWorkflow()` | `if (options.isCloneWorkflow()) { copyField(...) }` |

### ❌ NEVER Copy

| Field Type | Reason | Alternative |
|------------|--------|-------------|
| **ID fields** | Auto-generated | JPA handles |
| **Passwords** | Security risk | Never copy |
| **Tokens/API Keys** | Security risk | Never copy |
| **Audit fields** | System managed | Base class handles |

### 🔧 SPECIAL Handling

#### 1. Unique Fields

```java
// Email - make unique to avoid constraint violations
if (source.getEmail() != null) {
    targetEntity.setEmail(source.getEmail().replace("@", "+copy@"));
}

// Login - append suffix
if (source.getLogin() != null) {
    targetEntity.setLogin(source.getLogin() + "_copy");
}
```

#### 2. Collections

```java
// Always create new collection instances
if (options.includesRelations()) {
    CEntityDB.copyCollection(
        source::getChildren, 
        targetEntity::setChildren, 
        true  // createNew = true - creates new HashSet/ArrayList
    );
}
```

#### 3. Composition Objects

```java
// For @OneToOne compositions, create new instances
if (source.getSprintItem() != null) {
    CSprintItem newSprintItem = new CSprintItem();
    // Copy fields from source.getSprintItem() to newSprintItem
    targetEntity.setSprintItem(newSprintItem);
}
```

## Interface Helpers

Interface helpers remain unchanged and are called automatically by base class:

```java
// In CEntityDB.copyEntityTo()
IHasComments.copyCommentsTo(this, target, options);
IHasAttachments.copyAttachmentsTo(this, target, options);
IHasStatusAndWorkflow.copyStatusAndWorkflowTo(this, target, options);
```

These helpers:
- ✅ Check if both source and target implement the interface
- ✅ Copy collection fields using standard pattern
- ✅ Return silently if interface not implemented
- ✅ Handle errors gracefully without failing entire copy

## Complete Example: CActivityService

```java
/**
 * Service-level method to copy CActivity-specific fields using getters/setters.
 * This method implements the service-based copy pattern for Activity entities.
 * 
 * @param source  the source activity to copy from
 * @param target  the target entity to copy to
 * @param options clone options controlling what fields to copy
 */
@Override
public void copyEntityFieldsTo(final CActivity source, 
                               final CEntityDB<?> target,
                               final CCloneOptions options) {
    // Call parent to copy project item fields
    super.copyEntityFieldsTo(source, target, options);
    
    // Only copy if target is an Activity
    if (!(target instanceof CActivity)) {
        return;
    }
    final CActivity targetActivity = (CActivity) target;
    
    // Copy basic activity fields using getters/setters
    CEntityDB.copyField(source::getAcceptanceCriteria, targetActivity::setAcceptanceCriteria);
    CEntityDB.copyField(source::getNotes, targetActivity::setNotes);
    CEntityDB.copyField(source::getResults, targetActivity::setResults);
    
    // Copy numeric fields using getters/setters
    CEntityDB.copyField(source::getActualCost, targetActivity::setActualCost);
    CEntityDB.copyField(source::getActualHours, targetActivity::setActualHours);
    CEntityDB.copyField(source::getEstimatedCost, targetActivity::setEstimatedCost);
    CEntityDB.copyField(source::getEstimatedHours, targetActivity::setEstimatedHours);
    CEntityDB.copyField(source::getHourlyRate, targetActivity::setHourlyRate);
    CEntityDB.copyField(source::getRemainingHours, targetActivity::setRemainingHours);
    
    // Copy priority and type using getters/setters
    CEntityDB.copyField(source::getPriority, targetActivity::setPriority);
    CEntityDB.copyField(source::getEntityType, targetActivity::setEntityType);
    
    // Handle date fields based on options using getters/setters
    if (!options.isResetDates()) {
        CEntityDB.copyField(source::getDueDate, targetActivity::setDueDate);
        CEntityDB.copyField(source::getStartDate, targetActivity::setStartDate);
        CEntityDB.copyField(source::getCompletionDate, targetActivity::setCompletionDate);
    }
    
    // Copy links using IHasLinks interface method
    IHasLinks.copyLinksTo(source, target, options);
    
    // Note: Comments, attachments, and status/workflow are copied automatically by base class
    // Note: Sprint item relationship is not cloned - clone starts outside sprint
    // Note: Widget entity is not cloned - will be created separately if needed
    
    LOGGER.debug("Successfully copied activity '{}' with options: {}", source.getName(), options);
}
```

## Migration Checklist

For each entity that already has `copyEntityTo()` logic:

- [ ] Create `copyEntityFieldsTo()` in service
- [ ] Move all field copying from entity to service
- [ ] Update entity's `copyEntityTo()` to just call super
- [ ] Add documentation comments
- [ ] Compile and test
- [ ] Verify copy dialog still works
- [ ] Verify cross-type copying works

## Testing Checklist

After implementing the service-based copy pattern:

### 1. Compilation
- [ ] `./mvnw compile -DskipTests` succeeds
- [ ] No compilation errors
- [ ] Minimal warnings

### 2. Same-Type Copy
- [ ] Copy Activity → Activity works
- [ ] All fields copied correctly
- [ ] No validation errors
- [ ] Navigate to copied entity works

### 3. Cross-Type Copy
- [ ] Copy Activity → Meeting works
- [ ] Common fields copied
- [ ] Type-specific fields skipped
- [ ] No errors

### 4. Copy Options
- [ ] `resetDates=true` resets dates
- [ ] `resetDates=false` keeps dates
- [ ] `includesRelations=true` copies relations
- [ ] `includesRelations=false` skips relations

### 5. Unique Fields
- [ ] Email made unique (+copy@)
- [ ] Login made unique (_copy suffix)
- [ ] No constraint violations

### 6. Collections
- [ ] Comments copied if option enabled
- [ ] Attachments copied if option enabled
- [ ] Links copied if option enabled
- [ ] New collection instances created

## Common Mistakes to Avoid

### ❌ Mistake 1: Forgetting to Call Super in Service

```java
// WRONG - will lose parent fields!
@Override
public void copyEntityFieldsTo(YourEntity source, CEntityDB<?> target, CCloneOptions options) {
    if (target instanceof YourEntity) {
        // ... copy fields
    }
}
```

```java
// CORRECT
@Override
public void copyEntityFieldsTo(YourEntity source, CEntityDB<?> target, CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);  // ✅ ALWAYS call parent first
    if (target instanceof YourEntity) {
        // ... copy fields
    }
}
```

### ❌ Mistake 2: Putting Logic in Entity

```java
// WRONG - logic belongs in service!
@Override
protected void copyEntityTo(CEntityDB<?> target, CAbstractService serviceTarget, CCloneOptions options) {
    super.copyEntityTo(target, serviceTarget, options);
    if (target instanceof YourEntity) {
        YourEntity t = (YourEntity) target;
        copyField(this::getField, t::setField);  // ❌ Don't do this!
    }
}
```

```java
// CORRECT - entity just calls super
@Override
protected void copyEntityTo(CEntityDB<?> target, CAbstractService serviceTarget, CCloneOptions options) {
    super.copyEntityTo(target, serviceTarget, options);  // ✅ Just call super
    // NOTE: Field copying delegated to service.copyEntityFieldsTo()
}
```

### ❌ Mistake 3: Not Type-Checking in Service

```java
// WRONG - will fail on cross-type copy!
@Override
public void copyEntityFieldsTo(YourEntity source, CEntityDB<?> target, CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    YourEntity t = (YourEntity) target;  // ❌ ClassCastException!
    // ...
}
```

```java
// CORRECT - always type-check
@Override
public void copyEntityFieldsTo(YourEntity source, CEntityDB<?> target, CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    if (!(target instanceof YourEntity)) {  // ✅ Type-safe
        return;
    }
    YourEntity t = (YourEntity) target;
    // ...
}
```

## Benefits Summary

1. ✅ **Separation of Concerns**: Business logic in services, persistence in entities
2. ✅ **Reusability**: Base services provide common copying logic
3. ✅ **Testability**: Services can be unit tested independently
4. ✅ **Maintainability**: Single place to update field copying logic
5. ✅ **Type Safety**: Uses getters/setters for compile-time safety
6. ✅ **Extensibility**: Easy to add new fields or change logic
7. ✅ **Dependency Injection**: Services can access other services
8. ✅ **Clean Entities**: Entities focus on persistence concerns only

## References

- **Base Implementation**: `CAbstractService.copyEntityFieldsTo()`
- **Entity Delegation**: `CEntityDB.copyEntityTo()`
- **Complete Example**: `CActivityService.copyEntityFieldsTo()`
- **Testing Guide**: `docs/implementation/COPY_TO_IMPLEMENTATION_STATUS.md`
- **AGENTS.md**: Section 4.3 will be updated with this pattern

---

**Last Updated**: 2026-01-28  
**Status**: ACTIVE - All new entities must follow this pattern  
**Migration**: Existing entities should be migrated to this pattern
