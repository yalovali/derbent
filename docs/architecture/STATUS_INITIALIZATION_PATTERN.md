# Status Initialization Pattern

## Overview

All entities extending `CProjectItem` MUST have a valid status set before being saved to the database. This document defines the standard patterns for status initialization.

## The Problem

Sprint entities (and potentially others) were being created without status, leading to:
- Null pointer exceptions when accessing status
- Inconsistent data in database
- Failed validations
- UI display issues

## The Solution

Implement consistent status initialization in THREE places:

1. Service `initializeNewEntity()` method
2. Sample data initialization (`*InitializerService.initializeSample()`)
3. Validation check in `checkSaveAllowed()`

## Pattern 1: Service initializeNewEntity() Method

### Implementation

```java
public class CProjectItemService<EntityClass extends CProjectItem<EntityClass>> 
        extends CEntityOfProjectService<EntityClass> {
    
    @Override
    public void initializeNewEntity(final EntityClass entity) {
        super.initializeNewEntity(entity);
        
        // Check if status already set (e.g., from constructor)
        if (entity.getStatus() != null) {
            return;
        }
        
        // Get project and validate it's set
        final var project = entity.getProject();
        Check.notNull(project, "Project must be set before initializing status");
        
        // Get default status or first available status
        final var defaultStatus = projectItemStatusService.findDefaultStatus(project)
            .orElseGet(() -> {
                Check.notNull(project.getCompany(), 
                    "Company must be set before initializing status");
                final var available = projectItemStatusService.listByCompany(
                    project.getCompany());
                Check.notEmpty(available, 
                    "No project item statuses available for company " + 
                    project.getCompany().getName());
                return available.get(0);
            });
        
        entity.setStatus(defaultStatus);
    }
}
```

### When This Runs

- When UI creates new entity via "New" button
- When service.newEntity() is called
- Before entity is displayed in edit dialog

## Pattern 2: Sample Data Initialization

### Standard Pattern for Entities with Workflow

```java
public static void initializeSample(final CProject project, final boolean minimal) 
        throws Exception {
    // Get services
    final CEntityService entityService = CSpringContext.getBean(CEntityService.class);
    final CEntityTypeService typeService = CSpringContext.getBean(CEntityTypeService.class);
    final CProjectItemStatusService projectItemStatusService = 
        CSpringContext.getBean(CProjectItemStatusService.class);
    
    // Create entity with basic fields
    final CEntity entity = new CEntity("Sample Entity", project);
    entity.setDescription("Description of sample entity");
    
    // Set entity type (required for workflow)
    final CEntityType type = typeService.getRandom(project.getCompany());
    entity.setEntityType(type);
    
    // CRITICAL: Initialize status from workflow
    if (type != null && type.getWorkflow() != null) {
        final List<CProjectItemStatus> initialStatuses = 
            projectItemStatusService.getValidNextStatuses(entity);
        if (!initialStatuses.isEmpty()) {
            entity.setStatus(initialStatuses.get(0));
        }
    }
    
    // Save entity (validation will ensure status is set)
    entityService.save(entity);
}
```

### Alternative Pattern: Random Status (Not Recommended)

```java
// ALTERNATIVE (used by some entities like Decisions)
// Note: This doesn't respect workflow, use workflow pattern instead
final CProjectItemStatus status = statusService.getRandom(project.getCompany());
entity.setStatus(status);
```

### Sprint Example (FIXED)

```java
public static void initializeSample(final CProject project, final boolean minimal) 
        throws Exception {
    final CSprintService sprintService = CSpringContext.getBean(CSprintService.class);
    final CSprintTypeService sprintTypeService = 
        CSpringContext.getBean(CSprintTypeService.class);
    final CProjectItemStatusService projectItemStatusService =
        CSpringContext.getBean(CProjectItemStatusService.class);
    
    for (int i = 1; i <= 2; i++) {
        final CSprintType sprintType = sprintTypeService.getRandom(project.getCompany());
        final CSprint sprint = new CSprint("Sprint " + i, project);
        sprint.setDescription("Sprint " + i + " - Development iteration");
        sprint.setEntityType(sprintType);
        sprint.setStartDate(LocalDate.now().plusWeeks((i - 1) * 2));
        sprint.setEndDate(LocalDate.now().plusWeeks(i * 2));
        
        // CRITICAL: Set initial status from workflow
        if (sprintType != null && sprintType.getWorkflow() != null) {
            final List<CProjectItemStatus> initialStatuses =
                projectItemStatusService.getValidNextStatuses(sprint);
            if (!initialStatuses.isEmpty()) {
                sprint.setStatus(initialStatuses.get(0));
            }
        }
        
        sprintService.save(sprint);
    }
}
```

## Pattern 3: Validation in checkSaveAllowed()

### Implementation

```java
@Override
public String checkSaveAllowed(final EntityClass entity) {
    // Call parent validation first
    final String superCheck = super.checkSaveAllowed(entity);
    if (superCheck != null) {
        return superCheck;
    }
    
    // Validate status is set
    if (entity.getStatus() == null) {
        return "Status must be set before saving. Please select a status.";
    }
    
    // Validate status belongs to same company as entity
    final var project = entity.getProject();
    if (project != null && project.getCompany() != null) {
        final var entityCompany = project.getCompany();
        final var statusCompany = entity.getStatus().getCompany();
        
        if (statusCompany == null) {
            return "Status company cannot be null.";
        }
        
        if (!entityCompany.getId().equals(statusCompany.getId())) {
            return "Status must belong to the same company as the entity. " +
                   "Entity company: " + entityCompany.getName() + 
                   ", Status company: " + statusCompany.getName();
        }
    }
    
    return null; // Validation passed
}
```

### Why This Is Important

1. **Safety Net**: Catches any case where status wasn't initialized
2. **User Feedback**: Provides clear error message to user
3. **Data Integrity**: Prevents saving entities with null status
4. **Company Isolation**: Ensures status belongs to correct company

## Status and Workflow Relationship

### Entity Type → Workflow → Initial Status

```
CProjectItem (Sprint/Activity/Meeting)
    ↓
has EntityType (CSprintType/CActivityType/CMeetingType)
    ↓
has Workflow (CWorkflowEntity)
    ↓
has Initial Statuses (CProjectItemStatus via CWorkflowStatusRelation)
    ↓
First valid status is assigned to new entity
```

### Getting Initial Status from Workflow

```java
// Step 1: Entity must have type set
entity.setEntityType(typeEntity);

// Step 2: Get valid next statuses (for new entity, returns initial statuses)
List<CProjectItemStatus> initialStatuses = 
    projectItemStatusService.getValidNextStatuses(entity);

// Step 3: Set first status
if (!initialStatuses.isEmpty()) {
    entity.setStatus(initialStatuses.get(0));
}
```

## Common Mistakes

### ❌ WRONG: Creating Entity Without Status

```java
// WRONG: No status set
final CSprint sprint = new CSprint("Sprint 1", project);
sprint.setEntityType(type);
sprintService.save(sprint); // Will fail validation!
```

### ❌ WRONG: Setting Random Status Without Workflow

```java
// WRONG: Ignores workflow configuration
final CProjectItemStatus randomStatus = statusService.getRandom(company);
sprint.setStatus(randomStatus); // May not be valid for this workflow!
```

### ❌ WRONG: Status from Wrong Company

```java
// WRONG: Status from different company
final CProjectItemStatus status = statusService.getRandom(otherCompany);
sprint.setStatus(status); // Will fail validation!
```

### ✅ CORRECT: Using Workflow Pattern

```java
// CORRECT: Status from entity's workflow
if (sprintType != null && sprintType.getWorkflow() != null) {
    final List<CProjectItemStatus> initialStatuses =
        projectItemStatusService.getValidNextStatuses(sprint);
    if (!initialStatuses.isEmpty()) {
        sprint.setStatus(initialStatuses.get(0));
    }
}
```

## Checklist for New Entities

When creating a new entity that extends CProjectItem:

- [ ] Override `initializeNewEntity()` in service to set default status
- [ ] Override `checkSaveAllowed()` to validate status is set
- [ ] Validate status belongs to same company as entity
- [ ] In `*InitializerService.initializeSample()`:
  - [ ] Set entity type first
  - [ ] Get initial statuses from workflow
  - [ ] Set first status from workflow
  - [ ] Save entity (validation will catch missing status)
- [ ] Test entity creation in UI
- [ ] Test sample data initialization
- [ ] Verify status is never null in database

## Entities Requiring Status

All entities extending `CProjectItem` require status:

- ✅ CActivity (fixed in CDataInitializer)
- ✅ CMeeting (fixed in CDataInitializer)
- ✅ CDecision (uses random status - consider migrating to workflow pattern)
- ✅ CSprint (FIXED in this commit)
- CRisk
- CTicket
- CAsset
- CBudget
- CDeliverable
- CMilestone
- CProduct
- CProductVersion
- CProjectComponent
- CProjectComponentVersion
- CProjectExpense
- CProjectIncome
- CProvider
- CRiskLevel
- COrder
- CPageEntity (if extends CProjectItem)

## Testing Status Initialization

### Unit Test Pattern

```java
@Test
void testInitializeNewEntity_SetsDefaultStatus() {
    // Given
    CSprint sprint = new CSprint("Test Sprint", project);
    
    // When
    sprintService.initializeNewEntity(sprint);
    
    // Then
    assertNotNull(sprint.getStatus());
    assertEquals(project.getCompany().getId(), 
                 sprint.getStatus().getCompany().getId());
}

@Test
void testSampleInitialization_AllSprintsHaveStatus() {
    // Given
    CProject project = createTestProject();
    
    // When
    CSprintInitializerService.initializeSample(project, false);
    
    // Then
    List<CSprint> sprints = sprintService.listByProject(project);
    assertTrue(sprints.size() >= 2);
    for (CSprint sprint : sprints) {
        assertNotNull(sprint.getStatus(), 
            "Sprint " + sprint.getName() + " should have status");
    }
}
```

## Summary

**Three-Layer Defense:**

1. **Initialization Layer**: `initializeNewEntity()` sets default status
2. **Sample Data Layer**: `initializeSample()` sets status from workflow
3. **Validation Layer**: `checkSaveAllowed()` prevents saving without status

**Golden Rules:**

1. ✅ **ALWAYS** set entity type before status
2. ✅ **ALWAYS** get initial status from workflow via `getValidNextStatuses()`
3. ✅ **ALWAYS** validate status is set in `checkSaveAllowed()`
4. ✅ **ALWAYS** validate status belongs to same company
5. ❌ **NEVER** save entity without status
6. ❌ **NEVER** use status from different company
7. ❌ **NEVER** skip workflow when setting status

By following these patterns, we ensure:
- All entities have valid status
- Status respects workflow configuration
- Company isolation is maintained
- Data integrity is preserved
- User experience is consistent
