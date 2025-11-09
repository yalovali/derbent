# CheckDeleteAllowed Refactoring Pattern

## Overview

This document describes the refactored `checkDeleteAllowed` and `initializeNewEntity` patterns implemented across the service hierarchy to ensure consistent, maintainable, and well-documented dependency checking and entity initialization.

## Service Hierarchy

The service hierarchy has been organized to distribute validation logic appropriately:

```
CAbstractService<EntityClass>
  ├── CEntityNamedService<EntityClass>
  │     └── CEntityOfProjectService<EntityClass>
  │           ├── CTypeEntityService<EntityClass>
  │           │     └── [Type Services: CActivityTypeService, CUserTypeService, etc.]
  │           └── CStatusService<EntityClass>
  │                 └── [Status Services: CProjectItemStatusService, CRiskStatusService, etc.]
  └── [Other Services: CCompanyService, CUserService, etc.]
```

## CheckDeleteAllowed Pattern

### Implementation Rules

1. **Always call super.checkDeleteAllowed() first**
   ```java
   @Override
   public String checkDeleteAllowed(final EntityClass entity) {
       final String superCheck = super.checkDeleteAllowed(entity);
       if (superCheck != null) {
           return superCheck;
       }
       // Add entity-specific checks here
       return null;
   }
   ```

2. **Use generic variable name 'entity'** instead of specific entity names (e.g., `activityType`, `userType`)
   
3. **Document what checks are performed**
   - Each level should document which checks it performs
   - Reference that super checks are called first

### Checks at Each Level

#### CAbstractService
- **Validates**: Entity is not null, Entity ID is not null
- **Returns**: null (allows deletion by default)

#### CEntityNamedService
- **Validates**: (calls super)
- **Returns**: null (no additional checks)

#### CEntityOfProjectService
- **Validates**: (calls super)
- **Returns**: null (no additional checks)

#### CTypeEntityService
- **Validates**: (calls super), Entity is not marked as non-deletable (attributeNonDeletable)
- **Returns**: Error message if entity is marked as non-deletable

#### CStatusService
- **Validates**: (calls super, which includes non-deletable check from CTypeEntityService)
- **Returns**: null (no additional status-specific checks)

#### Child Services (e.g., CActivityTypeService, CProjectItemStatusService)
- **Validates**: (calls super), Entity-specific dependency checks (e.g., usage count)
- **Returns**: Error message with specific usage information

### Example Implementations

#### Type Service Example (CActivityTypeService)
```java
@Override
public String checkDeleteAllowed(final CActivityType entity) {
    final String superCheck = super.checkDeleteAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    try {
        // Check if any activities are using this type
        final long usageCount = activityRepository.countByActivityType(entity);
        if (usageCount > 0) {
            return String.format("Cannot delete. It is being used by %d activit%s.", 
                usageCount, usageCount == 1 ? "y" : "ies");
        }
        return null;
    } catch (final Exception e) {
        LOGGER.error("Error checking dependencies: {}", entity.getName(), e);
        return "Error checking dependencies: " + e.getMessage();
    }
}
```

#### Status Service Example (CProjectItemStatusService)
```java
@Override
public String checkDeleteAllowed(final CProjectItemStatus entity) {
    final String superCheck = super.checkDeleteAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    try {
        // Check if any activities are using this status
        final long usageCount = activityRepository.countByProjectItemStatus(entity);
        if (usageCount > 0) {
            return String.format("Cannot delete. It is being used by %d activit%s.", 
                usageCount, usageCount == 1 ? "y" : "ies");
        }
        return null;
    } catch (final Exception e) {
        LOGGER.error("Error checking dependencies: {}", entity.getName(), e);
        return "Error checking dependencies: " + e.getMessage();
    }
}
```

## InitializeNewEntity Pattern

### Implementation Rules

1. **Always call super.initializeNewEntity() first**
   ```java
   @Override
   public void initializeNewEntity(final EntityClass entity) {
       super.initializeNewEntity(entity);
       // Add entity-specific initialization here
   }
   ```

2. **Use generic variable name 'entity'**

3. **Document what initialization is performed**

### Initialization at Each Level

#### CAbstractService
- Validates entity is not null

#### CEntityNamedService
- Calls super
- Initializes description to empty string if null

#### CEntityOfProjectService
- Calls super
- Initializes project from session
- Initializes createdBy from session
- If entity is CTypeEntity:
  - Initializes color to "#4A90E2"
  - Initializes sortOrder to 100
  - Sets attributeNonDeletable to false

#### Child Services
- Calls super (inherits all above initialization)
- Sets entity-specific fields (e.g., auto-generated names)

## Integration with CCrudToolbar

CCrudToolbar automatically uses the service's `checkDeleteAllowed` method:

```java
public CCrudToolbar(final CAbstractService<EntityClass> entityService, final Class<EntityClass> entityClass) {
    // ...
    this.dependencyChecker = entityService::checkDeleteAllowed;
    // ...
}
```

When the delete button is clicked, the toolbar:
1. Calls `checkDeleteAllowed` on the current entity
2. If an error message is returned, displays it to the user
3. If null is returned, shows confirmation dialog
4. Performs deletion only after confirmation

## Services Updated

### Type Services (extend CTypeEntityService)
- CActivityTypeService
- CUserTypeService
- CMeetingTypeService
- CDecisionTypeService
- COrderTypeService
- CActivityPriorityService
- CCommentPriorityService

### Status Services (extend CStatusService)
- CProjectItemStatusService
- CRiskStatusService
- CMeetingStatusService
- CDecisionStatusService
- COrderStatusService
- CApprovalStatusService

### Other Services
- CCompanyService (extends CEntityNamedService)
- CUserService (extends CAbstractService)

## Benefits of This Pattern

1. **Code Reuse**: Common checks (null validation, non-deletable flag) are implemented once
2. **Maintainability**: Changes to common logic only need to be made in one place
3. **Consistency**: All services follow the same pattern
4. **Documentation**: Each level clearly documents what it checks
5. **Extensibility**: Easy to add new services following the pattern
6. **Type Safety**: Compiler ensures all services implement the method correctly

## Migration Guide for New Services

When creating a new service:

1. Determine the correct parent class:
   - If entity extends CStatus → extend CStatusService
   - If entity extends CTypeEntity → extend CTypeEntityService
   - If entity extends CEntityOfProject → extend CEntityOfProjectService
   - If entity extends CEntityNamed → extend CEntityNamedService
   - Otherwise → extend CAbstractService

2. Override `checkDeleteAllowed`:
   ```java
   @Override
   public String checkDeleteAllowed(final EntityClass entity) {
       final String superCheck = super.checkDeleteAllowed(entity);
       if (superCheck != null) {
           return superCheck;
       }
       // Add your specific checks
       return null;
   }
   ```

3. Override `initializeNewEntity`:
   ```java
   @Override
   public void initializeNewEntity(final EntityClass entity) {
       super.initializeNewEntity(entity);
       // Add your specific initialization
   }
   ```

4. Use generic variable name 'entity' throughout
5. Document what checks/initialization you perform
