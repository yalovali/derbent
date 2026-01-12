# Validation Pattern Documentation

## Overview

This document defines the standard validation pattern for entities in the Derbent application. All services MUST follow this pattern to ensure consistent validation behavior across the codebase.

## Core Principle: Validation in `checkSaveAllowed()`, NOT in `save()`

**CRITICAL RULE**: Validation checks MUST be performed in `checkSaveAllowed()` method, NOT inside `save()` or `initializeNewEntity()` methods.

### Why?

1. **Separation of Concerns**: Validation logic is separate from persistence logic
2. **UI Integration**: CCrudToolbar and other UI components call `checkSaveAllowed()` before save to show user-friendly error messages
3. **Testability**: Validation can be tested independently without database operations
4. **Consistency**: All validation follows same pattern across all services

## The Validation Pattern

### 1. Base Service Pattern (CAbstractService)

```java
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {
    
    /**
     * Validates that entity can be saved.
     * @param entity the entity to validate
     * @return null if valid, or error message if validation fails
     */
    public String checkSaveAllowed(final EntityClass entity) {
        Check.notNull(entity, "Entity cannot be null");
        
        // Validate non-nullable fields using reflection
        final String nullableFieldsError = validateNullableFields(entity);
        if (nullableFieldsError != null) {
            return nullableFieldsError;
        }
        
        return null; // Validation passed
    }
    
    @Transactional(readOnly = false)
    public EntityClass save(final EntityClass entity) {
        Check.notNull(entity, "Entity cannot be null");
        validateEntity(entity); // Fail-fast validation
        return repository.save(entity);
    }
    
    protected void validateEntity(final EntityClass entity) {
        Check.notNull(entity, "Entity cannot be null");
        // Subclasses can add additional fail-fast checks here
    }
}
```

### 2. Extending in Subclass Services

```java
public class CProjectItemService<EntityClass extends CProjectItem<EntityClass>> 
        extends CEntityOfProjectService<EntityClass> {
    
    @Override
    public String checkSaveAllowed(final EntityClass entity) {
        // ALWAYS call super first
        final String superCheck = super.checkSaveAllowed(entity);
        if (superCheck != null) {
            return superCheck;
        }
        
        // Add entity-specific validation
        if (entity.getStatus() == null) {
            return "Status must be set before saving.";
        }
        
        // Validate status belongs to same company
        final var project = entity.getProject();
        if (project != null && project.getCompany() != null) {
            final var entityCompany = project.getCompany();
            final var statusCompany = entity.getStatus().getCompany();
            
            if (statusCompany == null) {
                return "Status company cannot be null.";
            }
            
            if (!entityCompany.getId().equals(statusCompany.getId())) {
                return "Status must belong to the same company as the entity.";
            }
        }
        
        return null; // All checks passed
    }
}
```

## Status Initialization Pattern

### Rule: Status MUST Be Initialized for All CProjectItem Entities

All entities extending `CProjectItem` MUST have a valid status set before saving. This is enforced by validation.

### Initialization in `initializeNewEntity()`

```java
@Override
public void initializeNewEntity(final EntityClass entity) {
    super.initializeNewEntity(entity);
    
    // Check if status already set
    if (entity.getStatus() != null) {
        return;
    }
    
    // Get project and company
    final var project = entity.getProject();
    Check.notNull(project, "Project must be set before initializing status");
    Check.notNull(project.getCompany(), "Company must be set before initializing status");
    
    // Get default or first available status
    final var defaultStatus = projectItemStatusService.findDefaultStatus(project)
        .orElseGet(() -> {
            final var available = projectItemStatusService.listByCompany(project.getCompany());
            Check.notEmpty(available, "No statuses available");
            return available.get(0);
        });
    
    entity.setStatus(defaultStatus);
}
```

### Initialization in Sample Data (InitializerService)

```java
public static void initializeSample(final CProject project, final boolean minimal) {
    // Get services
    final CEntityService entityService = CSpringContext.getBean(CEntityService.class);
    final CEntityTypeService typeService = CSpringContext.getBean(CEntityTypeService.class);
    final CProjectItemStatusService statusService = 
        CSpringContext.getBean(CProjectItemStatusService.class);
    
    // Create entity
    final CEntity entity = new CEntity("Sample Entity", project);
    entity.setDescription("Description");
    
    // Set entity type
    final CEntityType type = typeService.getRandom(project.getCompany());
    entity.setEntityType(type);
    
    // CRITICAL: Set status from workflow
    if (type != null && type.getWorkflow() != null) {
        final List<CProjectItemStatus> initialStatuses = 
            statusService.getValidNextStatuses(entity);
        if (!initialStatuses.isEmpty()) {
            entity.setStatus(initialStatuses.get(0));
        }
    }
    
    // Save entity
    entityService.save(entity); // Validation will check status is set
}
```

## Validation Return Values

### String Return Pattern (RECOMMENDED)

We use **String return values** instead of exceptions for validation:

- `null` = validation passed (no error)
- `String` = validation failed (error message to display to user)

### Benefits:

1. **Consistency**: Matches existing `checkDeleteAllowed()` pattern
2. **Simpler Control Flow**: No try-catch blocks needed
3. **Better Performance**: No exception overhead
4. **Clear Semantics**: null = success, String = error message

### Example Usage in UI:

```java
protected void on_buttonSave_clicked() {
    try {
        final String validationError = service.checkSaveAllowed(entity);
        if (validationError != null) {
            notificationService.showError(validationError);
            return;
        }
        
        service.save(entity);
        notificationService.showSaveSuccess();
    } catch (Exception ex) {
        LOGGER.error("Error saving entity", ex);
        notificationService.showException("Error saving entity", ex);
    }
}
```

## Common Validation Checks

### 1. Required Field Validation

```java
// Automatic via @Column(nullable=false) annotation
// Handled by validateNullableFields() in base class
```

### 2. Status Validation (CProjectItem entities)

```java
if (entity.getStatus() == null) {
    return "Status must be set before saving.";
}
```

### 3. Company Consistency Validation

```java
if (!entityCompany.getId().equals(statusCompany.getId())) {
    return "Status must belong to the same company as the entity.";
}
```

### 4. Workflow Validation

```java
if (!projectItemStatusService.isValidTransition(entity, newStatus)) {
    return "Invalid status transition. Status change not allowed by workflow.";
}
```

### 5. Unique Constraint Validation

```java
if (repository.existsByNameAndProject(entity.getName(), entity.getProject())) {
    return "Entity with this name already exists in the project.";
}
```

## Testing Validation

### Unit Test Pattern

```java
@Test
void testCheckSaveAllowed_WithNullStatus_ReturnsError() {
    // Given
    CSprint sprint = new CSprint("Test Sprint", project);
    sprint.setStatus(null);
    
    // When
    String error = sprintService.checkSaveAllowed(sprint);
    
    // Then
    assertNotNull(error);
    assertTrue(error.contains("Status must be set"));
}

@Test
void testCheckSaveAllowed_WithValidData_ReturnsNull() {
    // Given
    CSprint sprint = new CSprint("Test Sprint", project);
    sprint.setStatus(validStatus);
    sprint.setEntityType(sprintType);
    
    // When
    String error = sprintService.checkSaveAllowed(sprint);
    
    // Then
    assertNull(error); // No validation error
}
```

## Migration Checklist

When adding validation to existing service:

- [ ] Move status checks from `initializeNewEntity()` to `checkSaveAllowed()`
- [ ] Move status checks from `save()` method to `checkSaveAllowed()`
- [ ] Call `super.checkSaveAllowed()` first in override
- [ ] Return appropriate error messages for each validation failure
- [ ] Test validation with unit tests
- [ ] Update sample data initialization to set status
- [ ] Verify UI integration (CCrudToolbar should show error messages)

## Common Mistakes to Avoid

### ❌ WRONG: Validation in save() method

```java
@Transactional
public EntityClass save(final EntityClass entity) {
    // WRONG: Don't validate here!
    if (entity.getStatus() == null) {
        throw new IllegalArgumentException("Status is required");
    }
    return repository.save(entity);
}
```

### ❌ WRONG: Validation in initializeNewEntity()

```java
public void initializeNewEntity(final EntityClass entity) {
    // WRONG: Don't validate here!
    Check.notNull(entity.getStatus(), "Status must be set");
    // This is initialization, not validation
}
```

### ✅ CORRECT: Validation in checkSaveAllowed()

```java
@Override
public String checkSaveAllowed(final EntityClass entity) {
    final String superCheck = super.checkSaveAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    
    // CORRECT: Validate here!
    if (entity.getStatus() == null) {
        return "Status must be set before saving.";
    }
    
    return null;
}
```

## Summary

**The Golden Rules:**

1. ✅ **DO** validate in `checkSaveAllowed()` method
2. ✅ **DO** call `super.checkSaveAllowed()` first
3. ✅ **DO** return null for success, String for errors
4. ✅ **DO** initialize status in `initializeNewEntity()` and sample data
5. ❌ **DON'T** validate in `save()` method
6. ❌ **DON'T** validate in `initializeNewEntity()` method
7. ❌ **DON'T** throw exceptions from validation methods
8. ❌ **DON'T** create entities without setting status

Following this pattern ensures:
- Consistent validation across all entities
- User-friendly error messages in UI
- Testable validation logic
- Maintainable codebase
