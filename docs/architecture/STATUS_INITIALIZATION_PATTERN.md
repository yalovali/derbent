# Status Initialization Pattern

## Overview

This document describes the mandatory pattern for initializing entities that implement `IHasStatusAndWorkflow`. This pattern ensures that all workflow-enabled entities have a valid status from creation, preventing workflow state corruption.

## Critical Rules

### 1. Status MUST Never Be Null

All entities implementing `IHasStatusAndWorkflow` MUST have a non-null status at all times after initialization:

```java
// ❌ WRONG - Direct instantiation without status
CSprint sprint = new CSprint("Sprint 1", project);
// sprint.status is NULL here!

// ✅ CORRECT - Use service to create and initialize
CSprintService service = getBean(CSprintService.class);
CSprint sprint = service.newEntity("Sprint 1", project);
// sprint.status is properly set via initializeNewEntity()
```

### 2. Entity Services MUST Call initializeNewEntity

All service classes for `IHasStatusAndWorkflow` entities must implement proper initialization:

```java
@Override
public void initializeNewEntity(final CSprint entity) {
    super.initializeNewEntity(entity);
    
    // Get current project from session
    final CProject currentProject = sessionService.getActiveProject()
        .orElseThrow(() -> new CInitializationException("No active project"));
    
    // Initialize workflow-based status and type
    IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, 
        entityTypeService, projectItemStatusService);
    
    // Set entity-specific defaults
    entity.setStartDate(LocalDate.now(clock));
    entity.setEndDate(LocalDate.now(clock).plusWeeks(2));
}
```

### 3. Sample Initializers MUST Use Standard Pattern

All `*InitializerService.initializeSample()` methods must use the standard pattern:

```java
// ✅ CORRECT - Standard pattern
public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
    final String[][] nameAndDescriptions = {
        { "Sprint 1", "Sprint 1 - Development iteration" },
        { "Sprint 2", "Sprint 2 - Development iteration" }
    };
    
    final CSprintService sprintService = (CSprintService) CSpringContext.getBean(
        CEntityRegistry.getServiceClassForEntity(clazz));
    
    initializeProjectEntity(nameAndDescriptions, sprintService, project, minimal, 
        (sprint, index) -> {
            // Set entity-specific fields
            sprint.setColor(CSprint.DEFAULT_COLOR);
            sprint.setStartDate(LocalDate.now().plusWeeks(index * 2));
            sprint.setEndDate(LocalDate.now().plusWeeks((index + 1) * 2));
        });
}
```

The `initializeProjectEntity()` base method automatically:
- Calls `service.newEntity()` to create instances
- Calls `service.initializeNewEntity()` if needed
- For `IHasStatusAndWorkflow` entities:
  - Assigns entity type from available types
  - Sets initial status from workflow
  - Validates company matching
- Saves each entity

```java
// ❌ WRONG - Direct instantiation in initializer
public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
    final CSprint sprint = new CSprint("Sprint 1", project);
    sprint.setDescription("...");
    sprint.setEntityType(sprintType);  // Type set manually
    // ⚠️ STATUS IS NULL!
    sprintService.save(sprint);
}
```

## Implementation Checklist

When creating a new entity implementing `IHasStatusAndWorkflow`:

- [ ] Entity domain class implements `IHasStatusAndWorkflow<EntityClass>`
- [ ] Entity service extends appropriate base service (e.g., `CProjectItemService`)
- [ ] Entity service overrides `initializeNewEntity()` and calls `IHasStatusAndWorkflowService.initializeNewEntity()`
- [ ] Initializer service's `initializeSample()` uses `initializeProjectEntity()` or `initializeCompanyEntity()`
- [ ] Entity type service exists and provides available types for the company
- [ ] Workflows are configured with initial status marked

## Status Initialization Flow

```
User/System creates entity
    ↓
Service.newEntity()
    ↓
new Entity(name, project)
    ↓
Service.initializeNewEntity(entity)
    ↓
IHasStatusAndWorkflowService.initializeNewEntity(entity, ...)
    ↓
    ├─ Set entity type (first available in company)
    │
    ├─ Get workflow from entity type
    │
    └─ Set initial status from workflow
        └─ Query workflow relations for initialStatus=true
        └─ Fallback to first status in workflow
    ↓
Entity has valid status
    ↓
Service.save(entity)
    ↓
Validation: status != null ✓
```

## Validation Points

### At Service Level

`CProjectItemService.save()` validates status before persistence:

```java
@Override
public EntityClass save(final EntityClass entity) {
    // Validate status is set for IHasStatusAndWorkflow entities
    if (entity instanceof IHasStatusAndWorkflow) {
        Check.notNull(entity.getStatus(), 
            "Status cannot be null for " + entity.getClass().getSimpleName());
    }
    return super.save(entity);
}
```

### At Setter Level

`CProjectItem.setStatus()` validates non-null:

```java
public void setStatus(final CProjectItemStatus status) {
    Check.notNull(status, "Status cannot be null");
    Check.notNull(getProject(), "Project must be set before applying status");
    Check.isSameCompany(getProject(), status);
    this.status = status;
    updateLastModified();
}
```

## Benefits

1. **Prevents Workflow Corruption**: All entities always have valid status
2. **Consistent Initialization**: Standard pattern across all entities
3. **Company Validation**: Status always matches entity's company
4. **Workflow Integration**: Initial status comes from workflow configuration
5. **Early Error Detection**: Validation catches issues before persistence

## See Also

- `IHasStatusAndWorkflow` interface documentation
- `IHasStatusAndWorkflowService.initializeNewEntity()` method
- `CInitializerServiceBase.initializeProjectEntity()` pattern
- `.github/copilot-instructions.md` - Project coding standards
