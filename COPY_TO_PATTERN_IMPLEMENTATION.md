# CopyTo Pattern Implementation Guide

## Overview
This document describes the enhanced copyTo pattern for flexible entity copying with CloneOptions support, including cross-type copying and optional field mapping.

## Pattern Design

### Core Concepts
1. **Getter/Setter Based**: All field access uses getters/setters (no direct field access)
2. **Optional Fields**: If getter/setter doesn't exist, skip silently (no errors)
3. **CloneOptions Integration**: Respect existing clone options (resetDates, includeComments, etc.)
4. **Collection Support**: Handle List/Set copying where field names match
5. **Type Safety**: Use Supplier<T> for getters, Consumer<T> for setters

### Base Pattern in CEntityDB

```java
/**
 * Copies a single field from source to target using Supplier/Consumer pattern.
 * If either supplier or consumer is null, the field is skipped silently.
 * 
 * @param supplier The getter method reference (e.g., source::getFieldName)
 * @param consumer The setter method reference (e.g., target::setFieldName)
 * @param <T> The field type
 */
protected <T> void copyField(
    final java.util.function.Supplier<T> supplier,
    final java.util.function.Consumer<T> consumer) {
    if (supplier == null || consumer == null) {
        return; // Skip if either is missing
    }
    try {
        final T value = supplier.get();
        consumer.accept(value);
    } catch (final Exception e) {
        // Log but don't fail - optional field
        LOGGER.debug("Could not copy field: {}", e.getMessage());
    }
}

/**
 * Copies a collection field with option to create new collection or reuse.
 * 
 * @param supplier The collection getter
 * @param consumer The collection setter
 * @param createNew If true, creates new HashSet/ArrayList; if false, reuses reference
 * @param <T> The collection element type
 */
protected <T> void copyCollection(
    final java.util.function.Supplier<? extends Collection<T>> supplier,
    final java.util.function.Consumer<? super Collection<T>> consumer,
    final boolean createNew) {
    if (supplier == null || consumer == null) {
        return;
    }
    try {
        final Collection<T> source = supplier.get();
        if (source == null) {
            consumer.accept(null);
            return;
        }
        if (createNew) {
            if (source instanceof Set) {
                consumer.accept(new HashSet<>(source));
            } else {
                consumer.accept(new ArrayList<>(source));
            }
        } else {
            consumer.accept(source);
        }
    } catch (final Exception e) {
        LOGGER.debug("Could not copy collection: {}", e.getMessage());
    }
}

/**
 * Copies entity to another class type, using CloneOptions to control what is copied.
 * 
 * @param targetClass The target entity class
 * @param options Clone options to control copying behavior
 * @return New instance of target class with copied fields
 * @throws Exception if instantiation fails
 */
public <T extends CEntityDB<?>> T copyTo(
    final Class<T> targetClass,
    final CCloneOptions options) throws Exception {
    final T target = targetClass.getDeclaredConstructor().newInstance();
    copyEntityTo(target, options);
    return target;
}

/**
 * Override in subclasses to copy entity-specific fields.
 * Always call super.copyEntityTo() first!
 * 
 * @param target The target entity
 * @param options Clone options
 */
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // Copy active field (always)
    copyField(this::getActive, target::setActive);
}
```

## Implementation in Subclasses

### Example: CEntityNamed

```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // Always call parent first
    super.copyEntityTo(target, options);
    
    // Copy named fields if target supports them
    if (target instanceof CEntityNamed) {
        final CEntityNamed targetNamed = (CEntityNamed) target;
        copyField(this::getName, targetNamed::setName);
        copyField(this::getDescription, targetNamed::setDescription);
    }
}
```

### Example: CProjectItem

```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof CProjectItem) {
        final CProjectItem<?> targetItem = (CProjectItem<?>) target;
        
        // Copy basic fields
        copyField(this::getProject, targetItem::setProject);
        copyField(this::getStatus, targetItem::setStatus);
        copyField(this::getAssignedTo, targetItem::setAssignedTo);
        copyField(this::getPriority, targetItem::setPriority);
        
        // Copy dates based on options
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetItem::setDueDate);
            copyField(this::getStartDate, targetItem::setStartDate);
            copyField(this::getCompletionDate, targetItem::setCompletionDate);
        }
        
        // Note: Sprint item NOT copied - clones start outside sprint
    }
}
```

### Example: CActivity

```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof CActivity) {
        final CActivity targetActivity = (CActivity) target;
        
        // Copy activity-specific fields
        copyField(this::getAcceptanceCriteria, targetActivity::setAcceptanceCriteria);
        copyField(this::getActualCost, targetActivity::setActualCost);
        copyField(this::getActualHours, targetActivity::setActualHours);
        copyField(this::getEstimatedCost, targetActivity::setEstimatedCost);
        copyField(this::getEstimatedHours, targetActivity::setEstimatedHours);
        
        // Copy dates based on options
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetActivity::setDueDate);
            copyField(this::getStartDate, targetActivity::setStartDate);
            copyField(this::getCompletionDate, targetActivity::setCompletionDate);
        }
        
        // Copy comments if requested
        if (options.includesComments()) {
            copyCollection(this::getComments, targetActivity::setComments, true);
        }
        
        // Copy attachments if requested
        if (options.includesAttachments()) {
            copyCollection(this::getAttachments, targetActivity::setAttachments, true);
        }
    }
}
```

## Benefits

1. **Type Safe**: Compile-time checking of getter/setter types
2. **Null Safe**: Silent skip if getter/setter doesn't exist
3. **Flexible**: Can copy between different but compatible types
4. **Consistent**: Uses CloneOptions for all copy operations
5. **Maintainable**: Explicit field-by-field mapping
6. **No Reflection**: Uses method references, not reflection strings

## Integration with createClone

The existing `createClone()` method can be simplified:

```java
@Override
public CActivity createClone(final CCloneOptions options) throws Exception {
    // Use copyTo instead of manual field copying
    return copyTo(CActivity.class, options);
}
```

## Testing

Test that:
1. Fields are copied correctly
2. Missing getters/setters don't cause errors
3. Collections are properly cloned (new instances)
4. CloneOptions are respected
5. Cross-type copying works when types are compatible

## Migration Strategy

1. Implement copyField/copyCollection in CEntityDB
2. Implement copyEntityTo in base classes (CEntityNamed, CEntityOfProject, CProjectItem)
3. Implement copyEntityTo in concrete classes (CActivity, CMeeting, etc.)
4. Update createClone methods to use copyTo
5. Test each class after migration
