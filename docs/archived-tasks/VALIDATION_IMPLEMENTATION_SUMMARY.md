# Validation Implementation Summary

## Overview
This document describes the validation enhancements added to the Derbent project to ensure data integrity and provide better user feedback.

## Key Changes

### 1. CExceptionNotify - User-Facing Exception
**File:** `src/main/java/tech/derbent/api/exceptions/CExceptionNotify.java`

A new exception class specifically designed for user-facing validation and business rule errors. Unlike technical exceptions (CReflectionException, CInitializationException), this exception is intended to be caught and displayed to end users as notifications.

**Use Cases:**
- Validation rules are violated (e.g., required fields are null)
- Business rules prevent an operation (e.g., cannot delete a status that's in use)
- Data integrity constraints are violated (e.g., status cannot be both from and to)

**Design Decision:** We chose to use String return values for `checkSaveAllowed` and `checkDeleteAllowed` instead of throwing `CExceptionNotify` to maintain consistency with the existing codebase pattern. This approach:
- Maintains backward compatibility
- Provides a simpler control flow
- Allows for easy null checks (null = success, non-null = error message)
- Can be easily converted to exceptions in the future if needed

### 2. Generic Nullable Field Validation
**File:** `src/main/java/tech/derbent/api/services/CAbstractService.java`

Enhanced the `checkSaveAllowed` method to provide generic validation for non-nullable fields using reflection:

```java
public String checkSaveAllowed(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    // Validate non-nullable fields using reflection
    final String nullableFieldsError = validateNullableFields(entity);
    if (nullableFieldsError != null) {
        return nullableFieldsError;
    }
    return null;
}
```

**Key Features:**
- Automatically validates all fields marked with `@Column(nullable=false)`
- Uses `@AMetaData` annotations to get user-friendly field names
- Falls back to formatting field names (camelCase → Title Case) if metadata is unavailable
- Provides clear error messages like "Required field 'Name' cannot be empty"
- Handles multiple missing fields: "Required fields cannot be empty: Name, Description, Status"

**How It Works:**
1. Uses reflection to iterate through all fields (including inherited ones)
2. Checks for `@Column(nullable=false)` annotation
3. Verifies field value is not null
4. Collects all missing field names
5. Returns a formatted error message with all missing fields

### 3. CCrudToolbar Integration
**File:** `src/main/java/tech/derbent/api/views/components/CCrudToolbar.java`

Integrated `checkSaveAllowed` validation into the save operation:

```java
// Check if save is allowed (validation)
final String saveError = entityService.checkSaveAllowed(currentEntity);
if (saveError != null) {
    showErrorNotification(saveError);
    return;
}
```

**Benefits:**
- Prevents saving invalid entities
- Shows user-friendly error messages in the UI
- Consistent validation across all entity types
- Works automatically for all entities using CCrudToolbar

### 4. Existing Validation Examples

#### CWorkflowStatusRelationService
**File:** `src/main/java/tech/derbent/app/workflow/service/CWorkflowStatusRelationService.java`

Already implements proper validation in `checkSaveAllowed`:
- Validates that from status and to status cannot be the same
- Example: "From status and To status cannot be the same. New -> New"

#### CProjectItemStatusService
**File:** `src/main/java/tech/derbent/app/activities/service/CProjectItemStatusService.java`

Already implements proper validation in `checkDeleteAllowed`:
- Checks if status is used by any activities
- Checks if status is used in any workflows (from or to)
- Example: "Cannot delete. It is being used by 5 activities."
- Example: "Cannot delete. This status is used in workflow: Default Workflow."

## Validation Pattern

All services now follow this pattern:

### For Save Operations (checkSaveAllowed)
```java
@Override
public String checkSaveAllowed(final EntityClass entity) {
    // 1. Call parent validation (nullable fields, etc.)
    final String superCheck = super.checkSaveAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    
    // 2. Add entity-specific validation
    if (entity.getFromStatus() == entity.getToStatus()) {
        return "From status and To status cannot be the same.";
    }
    
    // 3. Return null if all checks pass
    return null;
}
```

### For Delete Operations (checkDeleteAllowed)
```java
@Override
public String checkDeleteAllowed(final EntityClass entity) {
    // 1. Call parent validation
    final String superCheck = super.checkDeleteAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    
    // 2. Check for dependencies
    final long usageCount = repository.countUsages(entity);
    if (usageCount > 0) {
        return String.format("Cannot delete. It is being used by %d items.", usageCount);
    }
    
    // 3. Return null if delete is allowed
    return null;
}
```

## Services with Validation

### Status Services (inherit from CStatusService)
All status services automatically inherit:
- Non-nullable field validation (from CAbstractService)
- Non-deletable flag check (from CTypeEntityService)

Examples:
- CProjectItemStatusService - checks workflow usage
- CMeetingStatusService
- CDecisionStatusService
- COrderStatusService
- CApprovalStatusService
- CRiskStatusService

### Type Services (inherit from CTypeEntityService)
All type services automatically inherit:
- Non-nullable field validation (from CAbstractService)
- Non-deletable flag check (from CTypeEntityService)

Examples:
- CActivityTypeService
- CMeetingTypeService
- CDecisionTypeService
- COrderTypeService

### Custom Services
Services can override validation methods to add specific business rules:
- CWorkflowStatusRelationService - validates from/to status consistency
- CProjectItemStatusService - validates workflow usage before deletion

## User Experience

### Save Validation
When a user tries to save an entity with missing required fields:
1. The save operation is prevented
2. A red error notification appears at the bottom of the screen
3. The notification shows exactly which fields are missing
4. Example: "Required field 'Name' cannot be empty."

### Delete Validation
When a user tries to delete an entity that's in use:
1. The delete operation is prevented
2. A red error notification explains why deletion is not allowed
3. The notification shows which items are using the entity
4. Example: "Cannot delete. This status is used in workflow: Default Workflow."

## Benefits

1. **Data Integrity**: Ensures all required fields are populated before saving
2. **User-Friendly Messages**: Clear, actionable error messages with field names
3. **Consistent Validation**: All entities automatically benefit from base validation
4. **Extensible**: Services can add custom validation rules by overriding methods
5. **Maintainable**: Validation logic is centralized and follows a clear pattern
6. **Type-Safe**: Uses reflection and annotations to avoid manual field checking
7. **Comprehensive**: Validates both save and delete operations

## Future Enhancements

While the current implementation uses String return values, we could enhance it in the future:

1. **Exception-Based Validation**: Convert to `CExceptionNotify` for more structured error handling
2. **Field-Level Validation**: Add support for field-specific validation rules
3. **Validation Groups**: Support different validation rules for create vs. update
4. **Custom Validators**: Allow services to register custom validator functions
5. **Async Validation**: Support asynchronous validation (e.g., checking uniqueness against database)

## Testing Recommendations

To test the validation:

1. **Manual Testing**: Try to save entities with missing required fields
2. **Workflow Testing**: Try to delete statuses that are used in workflows
3. **Activity Testing**: Try to delete statuses that are used by activities
4. **Relationship Testing**: Try to create workflow relations with same from/to status

## Conclusion

The validation implementation provides a robust, user-friendly system for ensuring data integrity. The pattern is consistent, extensible, and maintainable, making it easy for developers to add custom validation rules while benefiting from automatic base validation.
