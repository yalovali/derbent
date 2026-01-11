# Validation Pattern

## Overview

This document describes the mandatory validation pattern for all service classes. Validation ensures data integrity and business rule enforcement before entities are persisted to the database.

## Critical Rules

### 1. Validation MUST Be in validateEntity()

**ALL validation logic MUST be placed in the `validateEntity()` method, NOT in `save()` method:**

```java
// ❌ WRONG - Validation in save()
@Override
public EntityClass save(final EntityClass entity) {
    Check.notNull(entity.getStatus(), "Status cannot be null");
    return super.save(entity);
}

// ✅ CORRECT - Validation in validateEntity()
@Override
protected void validateEntity(final EntityClass entity) {
    super.validateEntity(entity);
    Check.notNull(entity.getStatus(), "Status cannot be null");
}
```

**Why?** The base `CAbstractService.save()` method already calls `validateEntity()` before persisting. This ensures:
- Consistent validation across all services
- Proper exception handling and rollback
- Clear separation of concerns
- Reusable validation logic

### 2. Always Call super.validateEntity()

All overridden `validateEntity()` methods MUST call the parent implementation first:

```java
@Override
protected void validateEntity(final CActivityType entity) {
    super.validateEntity(entity);  // ✅ ALWAYS call super first
    
    // Then add your specific validation
    Check.notNull(entity.getWorkflow(), "Workflow cannot be null");
}
```

### 3. Validation Flow

```
User/System calls save(entity)
    ↓
CAbstractService.save()
    ↓
validateEntity(entity)  ← ALL VALIDATION HERE
    ↓
    ├─ Check.notNull()
    ├─ Business rule validation
    ├─ Uniqueness checks
    └─ Cross-field validation
    ↓
repository.save(entity)
    ↓
Entity persisted to database
```

## Validation Patterns by Service Type

### CAbstractService (Base)

```java
@Override
protected void validateEntity(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    // Base validation - can be extended by subclasses
}
```

### CEntityOfProjectService

```java
@Override
protected void validateEntity(final EntityClass entity) {
    super.validateEntity(entity);
    
    // Validate project is set
    Check.notNull(entity.getProject(), "Project cannot be null");
    
    // Validate uniqueness within project
    validateNameUniqueInProject(entity);
}

private void validateNameUniqueInProject(final EntityClass entity) {
    final Optional<EntityClass> existing = repository
        .findByNameAndProject(entity.getName().trim(), entity.getProject())
        .filter(e -> entity.getId() == null || !e.getId().equals(entity.getId()));
    
    if (existing.isPresent()) {
        throw new IllegalArgumentException(
            "Entity with name '" + entity.getName() + 
            "' already exists in project '" + entity.getProject().getName() + "'");
    }
}
```

### CEntityOfCompanyService

```java
@Override
protected void validateEntity(final EntityClass entity) {
    super.validateEntity(entity);
    
    // Validate company is set
    Check.notNull(entity.getCompany(), "Company cannot be null");
    
    // Validate uniqueness within company
    validateNameUniqueInCompany(entity);
}

private void validateNameUniqueInCompany(final EntityClass entity) {
    final Optional<EntityClass> existing = repository
        .findByNameAndCompany(entity.getName().trim(), entity.getCompany())
        .filter(e -> entity.getId() == null || !e.getId().equals(entity.getId()));
    
    if (existing.isPresent()) {
        throw new IllegalArgumentException(
            "Entity with name '" + entity.getName() + 
            "' already exists in company '" + entity.getCompany().getName() + "'");
    }
}
```

### CProjectItemService (Status Validation)

```java
@Override
protected void validateEntity(final EntityClass entity) {
    super.validateEntity(entity);
    
    // Validate status for IHasStatusAndWorkflow entities
    if (entity instanceof IHasStatusAndWorkflow) {
        Check.notNull(entity.getStatus(), 
            "Status cannot be null for " + entity.getClass().getSimpleName() + 
            ". All entities implementing IHasStatusAndWorkflow must have status initialized.");
        
        // Validate status belongs to same company
        Check.notNull(entity.getProject(), "Project must be set before validating status");
        Check.isSameCompany(entity.getProject(), entity.getStatus());
    }
}
```

## Common Validation Scenarios

### 1. Required Field Validation

```java
@Override
protected void validateEntity(final CActivity entity) {
    super.validateEntity(entity);
    
    Check.notNull(entity.getName(), "Activity name cannot be null");
    Check.notBlank(entity.getName(), "Activity name cannot be blank");
    Check.notNull(entity.getProject(), "Project cannot be null");
    Check.notNull(entity.getEntityType(), "Activity type cannot be null");
}
```

### 2. Cross-Field Validation

```java
@Override
protected void validateEntity(final CSprint entity) {
    super.validateEntity(entity);
    
    // Validate date range
    if (entity.getStartDate() != null && entity.getEndDate() != null) {
        Check.isTrue(entity.getEndDate().isAfter(entity.getStartDate()) || 
                     entity.getEndDate().isEqual(entity.getStartDate()),
            "Sprint end date must be after or equal to start date");
    }
}
```

### 3. Relationship Validation

```java
@Override
protected void validateEntity(final CActivity entity) {
    super.validateEntity(entity);
    
    // Validate entity type belongs to same company
    if (entity.getEntityType() != null) {
        Check.notNull(entity.getProject(), "Project must be set before validating entity type");
        Check.isSameCompany(entity.getProject(), entity.getEntityType());
    }
    
    // Validate assigned user belongs to company
    if (entity.getAssignedTo() != null) {
        Check.notNull(entity.getProject(), "Project must be set before validating assigned user");
        Check.isSameCompany(entity.getProject(), entity.getAssignedTo());
    }
}
```

### 4. Business Rule Validation

```java
@Override
protected void validateEntity(final CKanbanColumn entity) {
    super.validateEntity(entity);
    
    // Validate status uniqueness across columns
    validateStatusUniqueness(entity);
}

private void validateStatusUniqueness(final CKanbanColumn entity) {
    // Check if any status in this column already exists in another column
    final List<CKanbanColumn> allColumns = listByKanbanLine(entity.getKanbanLine());
    
    for (final CKanbanColumn otherColumn : allColumns) {
        // Skip self
        if (entity.getId() != null && otherColumn.getId().equals(entity.getId())) {
            continue;
        }
        
        // Check for overlapping statuses
        final Set<Long> otherStatusIds = otherColumn.getStatuses().stream()
            .map(CProjectItemStatus::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        final Set<Long> thisStatusIds = entity.getStatuses().stream()
            .map(CProjectItemStatus::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        final Set<Long> overlap = new HashSet<>(thisStatusIds);
        overlap.retainAll(otherStatusIds);
        
        if (!overlap.isEmpty()) {
            throw new IllegalArgumentException(
                "Status already exists in column '" + otherColumn.getName() + "'");
        }
    }
}
```

### 5. Conditional Validation

```java
@Override
protected void validateEntity(final COrder entity) {
    super.validateEntity(entity);
    
    // Validate order number is unique if provided
    if (entity.getOrderNumber() != null && !entity.getOrderNumber().isBlank()) {
        validateOrderNumberUnique(entity);
    }
    
    // Validate provider is required for external orders
    if (entity.getOrderType() == OrderType.EXTERNAL) {
        Check.notNull(entity.getProvider(), 
            "Provider is required for external orders");
    }
}
```

## Integration with CRUD Operations

### Create Operation

```java
@Transactional
public EntityClass create(final EntityClass entity) {
    // Initialize defaults
    initializeNewEntity(entity);
    
    // Validation happens automatically in save()
    return save(entity);  // → calls validateEntity() internally
}
```

### Update Operation

```java
@Transactional
public EntityClass update(final EntityClass entity) {
    Check.notNull(entity.getId(), "Entity ID cannot be null for update");
    
    // Ensure entity exists
    final EntityClass existing = getById(entity.getId())
        .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
    
    // Validation happens automatically in save()
    return save(entity);  // → calls validateEntity() internally
}
```

### Delete Operation

Delete validation uses a different pattern with `checkDeleteAllowed()`:

```java
@Override
public String checkDeleteAllowed(final EntityClass entity) {
    final String superCheck = super.checkDeleteAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    
    // Check for dependencies
    final long dependencyCount = countDependencies(entity);
    if (dependencyCount > 0) {
        return "Cannot delete: " + dependencyCount + " dependencies exist";
    }
    
    return null;  // Delete allowed
}

@Override
@Transactional
public void delete(final EntityClass entity) {
    final String error = checkDeleteAllowed(entity);
    if (error != null) {
        throw new IllegalStateException(error);
    }
    super.delete(entity);
}
```

## Error Handling in UI

UI components should catch validation exceptions and display user-friendly messages:

```java
protected void on_buttonSave_clicked() {
    try {
        final CActivity activity = grid.asSingleSelect().getValue();
        Objects.requireNonNull(activity, "No activity selected");
        
        // Save calls validateEntity() internally
        activityService.save(activity);
        
        notificationService.showSaveSuccess();
        refreshGrid();
    } catch (final IllegalArgumentException ex) {
        // Validation error from Check.notNull(), Check.isTrue(), etc.
        notificationService.showError("Validation Error: " + ex.getMessage());
    } catch (final Exception ex) {
        LOGGER.error("Error saving activity", ex);
        notificationService.showSaveError();
    }
}
```

## Validation Checklist

When implementing a service, ensure:

- [ ] Override `validateEntity()` method (not `save()`)
- [ ] Call `super.validateEntity(entity)` first
- [ ] Validate all required fields using `Check` utility
- [ ] Validate cross-field relationships
- [ ] Validate business rules
- [ ] Check uniqueness constraints (scoped to company/project)
- [ ] Validate entity references belong to same company
- [ ] Use descriptive error messages
- [ ] Document complex validation logic

## Benefits

1. **Consistency**: All validation in one place
2. **Reusability**: Validation runs for all save operations
3. **Maintainability**: Easy to find and update validation rules
4. **Testability**: Can test validation without database
5. **Exception Safety**: Proper transaction rollback on validation failure
6. **Clear Separation**: Business logic separate from persistence logic

## See Also

- `CAbstractService.validateEntity()` - Base implementation
- `CAbstractService.save()` - Calls validateEntity() before persist
- `Check` utility class - Validation helper methods
- `docs/architecture/coding-standards.md` - Validation flow section
- `docs/architecture/service-layer-patterns.md` - Service patterns
