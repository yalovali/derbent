# Clone Methods Cleanup Plan

## Date: 2026-01-18
## Status: DEPRECATED - Using copyTo Pattern Instead

## Summary

The old **clone pattern** using `createClone()` and `actionClone()` is **DEPRECATED** and should be removed. We now use the **copyTo pattern** which is more flexible and supports cross-type copying.

## Current Status

### ✅ CopyTo Pattern (NEW - In Use)
- **Dialog**: `CDialogClone` (reused for copyTo)
- **Page Service Method**: `actionCopyTo()` ✅ ACTIVE
- **Toolbar Button**: "Copy To" button calls `on_actionCopyTo()` ✅ ACTIVE
- **Entity Method**: `copyEntityTo(target, options)` ✅ ACTIVE
- **Pattern**: Supports same-type AND cross-type copying

### ❌ Clone Pattern (OLD - NOT Used)
- **Dialog**: Same `CDialogClone` (reused)
- **Page Service Method**: `actionClone()` ❌ DEPRECATED - Not called
- **Toolbar Handler**: `on_actionClone()` ❌ DEPRECATED - Not wired
- **Entity Method**: `createClone(options)` ❌ PARTIALLY DEPRECATED
- **Pattern**: Only supports same-type cloning

## Methods to Remove

### 1. CPageService.actionClone() - REMOVE
**File**: `src/main/java/tech/derbent/api/services/pageservice/CPageService.java`
**Lines**: 95-126
**Reason**: Not called anywhere - toolbar uses `actionCopyTo()` instead

```java
// REMOVE THIS METHOD
public void actionClone() throws Exception {
    // ... 32 lines of unused code
}
```

### 2. CCrudToolbar.on_actionClone() - REMOVE
**File**: `src/main/java/tech/derbent/api/ui/component/enhanced/CCrudToolbar.java`
**Lines**: 236-241
**Reason**: Not called anywhere - button wired to `on_actionCopyTo()` instead

```java
// REMOVE THIS METHOD
private void on_actionClone() {
    try {
        pageBase.getPageService().actionClone();
    } catch (final Exception e) {
        CNotificationService.showException("Error during clone action", e);
    }
}
```

## Entity createClone() Methods - Decision

### ⚠️ KEEP (But Mark as Internal)
The `createClone()` methods in entities should be **KEPT** because:

1. **Used internally** by copyTo pattern for child collections
2. **CValidationSuite** uses `validationCase.createClone()` for cloning children
3. **CValidationCase** uses `validationStep.createClone()` for cloning children
4. **CKanbanLine** uses `column.createClone()` for cloning children

### Examples of Internal Usage:

```java
// CValidationSuite - clones child validation cases
if (options.includesRelations() && validationCases != null) {
    for (final CValidationCase validationCase : validationCases) {
        final CValidationCase validationCaseClone = validationCase.createClone(options);
        // ...
    }
}

// CValidationCase - clones child validation steps
if (validationSteps != null) {
    for (final CValidationStep validationStep : validationSteps) {
        final CValidationStep validationStepClone = validationStep.createClone(options);
        // ...
    }
}
```

### Solution: Mark as @Deprecated for External Use

Add JavaDoc to `createClone()` methods:

```java
/**
 * Creates a clone of this entity with the specified options.
 * 
 * @deprecated For external cloning, use {@link #copyTo(Class, CCloneOptions)} instead.
 * This method is kept for internal use by the copyTo pattern when cloning child collections.
 * 
 * @param options the cloning options determining what to clone
 * @return a new instance of the entity with cloned data
 * @throws Exception if cloning fails
 */
@Deprecated(forRemoval = false)
@Override
public EntityClass createClone(final CCloneOptions options) throws Exception {
    // Implementation...
}
```

## Cleanup Steps

### Step 1: Remove actionClone() from CPageService ✅

**File**: `src/main/java/tech/derbent/api/services/pageservice/CPageService.java`

**Remove**:
```java
public void actionClone() throws Exception {
    try {
        final EntityClass entity = getValue();
        LOGGER.debug("Clone action triggered for entity: {}", entity != null ? entity.getId() : "null");
        if (entity == null || entity.getId() == null) {
            CNotificationService.showWarning("Please select an item to clone.");
            return;
        }
        // Open clone dialog with options
        final CDialogClone<EntityClass> dialog = new CDialogClone<>(entity, clonedEntity -> {
            try {
                // Initialize the cloned entity (sets status, workflow, etc.)
                getEntityService().initializeNewEntity(clonedEntity);
                // Save the cloned entity
                final EntityClass saved = getEntityService().save(clonedEntity);
                LOGGER.info("Entity cloned successfully with new ID: {}", saved.getId());
                // Update the view with the new entity
                setValue(saved);
                getView().onEntityCreated(saved);
                getView().populateForm();
                CNotificationService.showSuccess("Entity cloned successfully");
            } catch (final Exception ex) {
                LOGGER.error("Error saving cloned entity: {}", ex.getMessage(), ex);
                CNotificationService.showException("Error saving cloned entity", ex);
            }
        });
        dialog.open();
    } catch (final Exception e) {
        LOGGER.error("Error during clone action: {}", e.getMessage());
        throw e;
    }
}
```

### Step 2: Remove on_actionClone() from CCrudToolbar ✅

**File**: `src/main/java/tech/derbent/api/ui/component/enhanced/CCrudToolbar.java`

**Remove**:
```java
private void on_actionClone() {
    try {
        pageBase.getPageService().actionClone();
    } catch (final Exception e) {
        CNotificationService.showException("Error during clone action", e);
    }
}
```

### Step 3: Update ICloneable Interface Documentation ✅

**File**: `src/main/java/tech/derbent/api/interfaces/ICloneable.java`

**Add deprecation notice**:
```java
/**
 * ICloneable - Interface for entities that can be cloned with options.
 * 
 * @deprecated For external cloning, use the copyTo pattern instead:
 * {@link tech.derbent.api.entity.domain.CEntityDB#copyTo(Class, CCloneOptions)}.
 * 
 * This interface is kept for internal use by the copyTo pattern when cloning child collections.
 * The createClone() method is used internally for cloning nested entities.
 * 
 * @param <EntityClass> the entity type being cloned
 */
@Deprecated(forRemoval = false)
public interface ICloneable<EntityClass extends CEntityDB<EntityClass>> {
    
    /**
     * Creates a clone of this entity with the specified options.
     * 
     * @deprecated For external cloning, use {@link tech.derbent.api.entity.domain.CEntityDB#copyTo(Class, CCloneOptions)} instead.
     * This method is kept for internal use by child collection cloning.
     * 
     * @param options the cloning options determining what to clone
     * @return a new instance of the entity with cloned data
     * @throws Exception if cloning fails
     */
    @Deprecated(forRemoval = false)
    EntityClass createClone(CCloneOptions options) throws Exception;
}
```

### Step 4: Update Base Class createClone() Documentation ✅

**Files to Update**:
- `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java`
- `src/main/java/tech/derbent/api/entity/domain/CEntityNamed.java`
- `src/main/java/tech/derbent/api/entityOfProject/domain/CProjectItem.java`

**Add to each createClone() method**:
```java
/**
 * Creates a clone of this entity with the specified options.
 * 
 * @deprecated For external cloning, use {@link #copyTo(Class, CCloneOptions)} instead.
 * This method is kept for internal use by the copyTo pattern when cloning child collections.
 * 
 * @param options the cloning options determining what to clone
 * @return a new instance of the entity with cloned data
 * @throws Exception if cloning fails
 */
@Deprecated(forRemoval = false)
@Override
public EntityClass createClone(final CCloneOptions options) throws Exception {
    // Implementation...
}
```

## Migration Guide

### Old Pattern (Deprecated):
```java
// DON'T USE THIS ANYMORE
final CActivity clone = activity.createClone(options);
service.save(clone);
```

### New Pattern (Use This):
```java
// USE THIS INSTEAD
final CActivity copy = (CActivity) activity.copyTo(CActivity.class, options);
service.initializeNewEntity(copy);
service.save(copy);

// Or for cross-type:
final CMeeting meeting = (CMeeting) activity.copyTo(CMeeting.class, options);
service.initializeNewEntity(meeting);
service.save(meeting);
```

### In UI (Automatic):
```java
// Users just click "Copy To" button
// Everything is handled by actionCopyTo() method
```

## Why CopyTo is Better

| Feature | createClone() | copyTo() |
|---------|--------------|----------|
| Same-type copying | ✅ Yes | ✅ Yes |
| Cross-type copying | ❌ No | ✅ Yes |
| Flexible target | ❌ No | ✅ Yes |
| Type-safe | ⚠️ Partial | ✅ Yes |
| UI Integration | ❌ No | ✅ Yes |
| Future-proof | ❌ No | ✅ Yes |

## Testing After Cleanup

### 1. Compilation
- [ ] Project compiles without errors
- [ ] No missing method errors

### 2. Functionality
- [ ] "Copy To" button works in UI
- [ ] Same-type copying works (Activity → Activity)
- [ ] Cross-type copying works (Activity → Meeting)
- [ ] Child collection cloning works (CValidationSuite with cases)

### 3. Documentation
- [ ] @Deprecated annotations in place
- [ ] JavaDoc updated
- [ ] Migration guide available

## Files to Modify

1. ✅ `src/main/java/tech/derbent/api/services/pageservice/CPageService.java` - Remove actionClone()
2. ✅ `src/main/java/tech/derbent/api/ui/component/enhanced/CCrudToolbar.java` - Remove on_actionClone()
3. ⚠️ `src/main/java/tech/derbent/api/interfaces/ICloneable.java` - Add @Deprecated
4. ⚠️ `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java` - Add @Deprecated to createClone()
5. ⚠️ `src/main/java/tech/derbent/api/entity/domain/CEntityNamed.java` - Add @Deprecated to createClone()
6. ⚠️ `src/main/java/tech/derbent/api/entityOfProject/domain/CProjectItem.java` - Add @Deprecated to createClone()

## Summary

- **Remove**: 2 unused methods (`actionClone()`, `on_actionClone()`)
- **Deprecate**: `createClone()` methods (keep for internal use)
- **Keep**: `copyTo()` pattern as the primary method
- **Benefit**: Cleaner codebase, less confusion, better pattern

## Status

- [x] Audit complete
- [ ] actionClone() removed
- [ ] on_actionClone() removed
- [ ] @Deprecated annotations added
- [ ] Documentation updated
- [ ] Testing complete

---

**Conclusion**: The clone pattern is deprecated in favor of copyTo pattern. Remove unused UI methods, deprecate createClone() for internal use only, and guide developers to use copyTo() instead.
