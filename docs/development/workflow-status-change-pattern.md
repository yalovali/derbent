# Workflow Status Change Pattern

## Overview

This document describes the pattern for handling status changes in entities that support workflow-based status management.

## Architecture

### Components Involved

1. **CCrudToolbar** - UI component that displays the status combobox
2. **CPageService** - Base page service with default status change handling
3. **CPageServiceWithWorkflow** - Specialized base class for workflow-aware entities
4. **CProjectItemStatusService** - Service that validates status transitions
5. **IHasStatusAndWorkflow** - Interface for entities with workflow support

### Class Hierarchy

```
CPageService<EntityClass>
    ↑
    |
CPageServiceDynamicPage<EntityClass>
    ↑
    |
CPageServiceWithWorkflow<EntityClass>
    ↑
    |
    ├── CPageServiceActivity
    ├── CPageServiceMeeting
    ├── CPageServiceDecision
    ├── CPageServiceRisk
    └── CPageServiceOrder
```

## How It Works

### 1. Status Combobox Population

When `CCrudToolbar.setCurrentEntity()` is called with a workflow entity:

```java
// CCrudToolbar checks if entity supports workflow
if (currentEntity instanceof IHasStatusAndWorkflow) {
    // Get valid next statuses from workflow service
    statusService.getValidNextStatuses((IHasStatusAndWorkflow<?>) currentEntity);
}
```

This ensures the combobox only shows statuses that are valid transitions according to workflow rules.

### 2. User Selection

When the user selects a new status:

```java
statusComboBox.addValueChangeListener(event -> {
    if (event.isFromClient() && (event.getValue() != null)) {
        // Delegate to page service for validation and persistence
        pageBase.getPageService().actionChangeStatus(event.getValue());
    }
});
```

### 3. Validation and Application

The page service validates and applies the status change:

```java
// In CPageServiceWithWorkflow
public void actionChangeStatus(final CProjectItemStatus newStatus) throws Exception {
    // 1. Get valid statuses from workflow
    List<CProjectItemStatus> validStatuses = 
        projectItemStatusService.getValidNextStatuses(entity);
    
    // 2. Validate the transition
    boolean isValidTransition = validStatuses.stream()
        .anyMatch(s -> s.getId().equals(newStatus.getId()));
    
    // 3. If valid, apply and save
    if (isValidTransition) {
        entity.setStatus(newStatus);
        EntityClass savedEntity = getEntityService().save(entity);
        // Update UI and show notification
    } else {
        // Show error notification
    }
}
```

### 4. Error Recovery

If the status change fails, the combobox resets to the current entity status:

```java
catch (final Exception e) {
    LOGGER.error("Error handling workflow status change", e);
    // Reset combobox to previous value
    if (projectItem.getStatus() != null) {
        statusComboBox.setValue(projectItem.getStatus());
    }
}
```

## Adding Workflow Support to a New Entity

### Step 1: Entity Implementation

Make your entity extend `CProjectItem` and implement `IHasStatusAndWorkflow`:

```java
@Entity
@Table(name = "my_entity")
public class CMyEntity extends CProjectItem<CMyEntity> 
    implements IHasStatusAndWorkflow<CMyEntity> {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entitytype_id", nullable = true)
    private CMyEntityType entityType;
    
    // Implement required methods from IHasStatusAndWorkflow
    @Override
    public CTypeEntity<?> getEntityType() { return entityType; }
    
    @Override
    public void setEntityType(CTypeEntity<?> typeEntity) {
        this.entityType = (CMyEntityType) typeEntity;
    }
    
    @Override
    public CWorkflowEntity getWorkflow() {
        return entityType != null ? entityType.getWorkflow() : null;
    }
}
```

### Step 2: Create PageService

Create a page service extending `CPageServiceWithWorkflow`:

```java
public class CPageServiceMyEntity extends CPageServiceWithWorkflow<CMyEntity> {
    
    Logger LOGGER = LoggerFactory.getLogger(CPageServiceMyEntity.class);
    
    public CPageServiceMyEntity(IPageServiceImplementer<CMyEntity> view) {
        super(view);
    }
    
    @Override
    public void bind() {
        LOGGER.debug("Binding page service for entity: {}", 
            CMyEntity.class.getSimpleName());
        super.bind();
    }
}
```

### Step 3: Update Service Class

Make your service extend `CProjectItemService`:

```java
@Service
public class CMyEntityService extends CProjectItemService<CMyEntity> {
    
    private final CMyEntityTypeService entityTypeService;
    
    public CMyEntityService(IMyEntityRepository repository, 
                           Clock clock, 
                           ISessionService sessionService,
                           CMyEntityTypeService entityTypeService,
                           CProjectItemStatusService projectItemStatusService) {
        super(repository, clock, sessionService, projectItemStatusService);
        this.entityTypeService = entityTypeService;
    }
    
    @Override
    public void initializeNewEntity(CMyEntity entity) {
        super.initializeNewEntity(entity);
        CProject currentProject = sessionService.getActiveProject()
            .orElseThrow(() -> new CInitializationException("No active project"));
        
        // Initialize workflow-based status and type using interface default method
        entity.initializeDefaults_IHasStatusAndWorkflow(
            currentProject, entityTypeService, projectItemStatusService);
    }
}
```

### Step 4: Configure Workflow

No code changes needed! The `CCrudToolbar` automatically detects workflow support via the `IHasStatusAndWorkflow` interface and creates the status combobox.

Configure workflow rules in the application through `CWorkflowStatusRelation` entities.

## Workflow Validation Rules

Status transitions are validated using `CProjectItemStatusService.getValidNextStatuses()`:

1. **For entities WITH a current status**: Returns current status + all valid next statuses defined in workflow relations
2. **For NEW entities WITHOUT a status**: Returns the initial status defined in the workflow

Example workflow configuration:

```
Workflow: "Development Process"
Relations:
- From: "To Do" → To: "In Progress" (initialStatus: true)
- From: "In Progress" → To: "In Review"
- From: "In Progress" → To: "Blocked"
- From: "In Review" → To: "Done"
- From: "In Review" → To: "In Progress"
- From: "Blocked" → To: "In Progress"
```

## Benefits of This Pattern

1. **Centralized Logic**: All status change logic is in page services, not scattered across UI components
2. **Workflow Validation**: Prevents invalid status transitions automatically
3. **Consistent Behavior**: All workflow entities behave the same way
4. **User-Friendly**: Clear error messages when transitions are not allowed
5. **Automatic Persistence**: Status changes are immediately saved to database
6. **Clean Architecture**: Separation between UI (CCrudToolbar) and business logic (PageService)
7. **Easy to Extend**: Adding workflow support to new entities requires minimal code

## Notifications

The pattern uses the centralized notification system:

- **Success**: `CNotificationService.showInfo("Status changed to 'In Progress'")`
- **Validation Error**: `CNotificationService.showWarning("Cannot change status...")`
- **System Error**: `CNotificationService.showError("Failed to change status")`

## Testing

To test status changes:

1. Create a workflow with defined transitions
2. Create an entity of a workflow-supporting type
3. Open the entity in the UI
4. Observe the status combobox shows only valid transitions
5. Select a new status and verify:
   - Status is updated in the entity
   - Entity is saved to database
   - Success notification is shown
   - Grid reflects the new status
6. Try to manually set an invalid status (via code/API) and verify validation prevents it

## Common Issues and Solutions

### Issue: Combobox shows all statuses instead of valid transitions

**Solution**: Check that:
1. Entity properly implements `IHasStatusAndWorkflow`
2. Entity type has a workflow assigned
3. Workflow has status relations configured

### Issue: Status change doesn't save

**Solution**: Check that:
1. PageService extends `CPageServiceWithWorkflow`
2. Entity service is properly configured
3. No validation errors in the entity

### Issue: "Workflow cannot be null" error

**Solution**: Ensure entity type is set before checking status transitions. Entity type contains the workflow reference.

## Related Classes

- `tech.derbent.api.ui.component.CCrudToolbar`
- `tech.derbent.api.services.pageservice.CPageService`
- `tech.derbent.api.services.pageservice.CPageServiceWithWorkflow`
- `tech.derbent.api.workflow.service.IHasStatusAndWorkflow`
- `tech.derbent.api.workflow.service.IHasStatusAndWorkflowService`
- `tech.derbent.plm.activities.service.CProjectItemStatusService`
- `tech.derbent.api.workflow.domain.CWorkflowEntity`
- `tech.derbent.api.workflow.domain.CWorkflowStatusRelation`
