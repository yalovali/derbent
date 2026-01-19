# Workflow Entity Quick Reference

## Quick Start

### 1. Access Workflow Management
- **Menu**: Projects → Workflows
- **URL**: `/workflow-entities`
- **Permissions**: Project Admin or User role required

### 2. Create New Workflow
```java
CWorkflowEntity workflow = new CWorkflowEntity("My Workflow", project);
workflow.setDescription("Description of the workflow");
workflow.setIsActive(true);
workflowEntityService.save(workflow);
```

### 3. Database Tables
```sql
-- Main workflow entity table
CREATE TABLE cworkflowentity (
    cworkflowentity_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    project_id BIGINT NOT NULL,
    parent_id BIGINT,
    parent_type VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    UNIQUE(name, project_id)
);
```

## Class References

### Domain Layer
- **CWorkflowEntity**: `tech.derbent.plm.workflow.domain.CWorkflowEntity`
- **CWorkflowBase**: `tech.derbent.plm.workflow.domain.CWorkflowBase`

### Service Layer
- **CWorkflowEntityService**: `tech.derbent.plm.workflow.service.CWorkflowEntityService`
- **CWorkflowBaseService**: `tech.derbent.plm.workflow.service.CWorkflowBaseService`
- **IWorkflowEntityRepository**: `tech.derbent.plm.workflow.service.IWorkflowEntityRepository`

### Initialization
- **CWorkflowEntityInitializerService**: `tech.derbent.plm.workflow.service.CWorkflowEntityInitializerService`

## Common Operations

### Find Workflows by Project
```java
List<CWorkflowEntity> workflows = 
    workflowEntityService.listByProject(project);
```

### Activate/Deactivate Workflow
```java
workflow.setIsActive(false);
workflowEntityService.save(workflow);
```

### Check if Workflow Can Be Deleted
```java
String error = workflowEntityService.checkDeleteAllowed(workflow);
if (error != null) {
    Notification.show(error);
} else {
    workflowEntityService.delete(workflow);
}
```

## Sample Data
Three sample workflows are created automatically:
1. **Activity Status Workflow** - Status transitions for activities
2. **Meeting Status Workflow** - Status transitions for meetings
3. **Decision Approval Workflow** - Approval process for decisions

## Recommended Next Steps

### Phase 1: Basic Implementation (Current)
✅ Domain entity with JPA annotations
✅ Service layer with CRUD operations
✅ Repository interface
✅ Screen initialization
✅ Sample data creation

### Phase 2: Workflow Transitions (To Implement)
- [ ] Create CWorkflowTransition entity
- [ ] Define status-to-status transitions
- [ ] Associate transitions with user roles
- [ ] Implement transition validation

### Phase 3: GUI Components (To Implement)
- [ ] Matrix grid for visualizing transitions
- [ ] Transition editor dialog
- [ ] Role-based permission selector
- [ ] Workflow activation toggle

### Phase 4: Advanced Features (Future)
- [ ] Conditional transitions with expressions
- [ ] Workflow templates
- [ ] Audit trail for workflow executions
- [ ] Notification rules

## Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Unique workflow name within project |
| description | String | No | Detailed workflow description |
| isActive | Boolean | Yes | Whether workflow is currently active |
| project | CProject | Yes | Associated project |
| parentId | Long | No | ID of parent entity (for hierarchical workflows) |
| parentType | String | No | Type of parent entity |

## Code Patterns

### Service Injection
```java
@Autowired
public MyService(CWorkflowEntityService workflowEntityService) {
    this.workflowEntityService = workflowEntityService;
}
```

### Finding Active Workflows
```java
List<CWorkflowEntity> activeWorkflows = 
    workflowEntityService.listByProject(project)
        .stream()
        .filter(CWorkflowEntity::getIsActive)
        .collect(Collectors.toList());
```

### Error Handling
```java
try {
    workflowEntityService.save(workflow);
    Notification.show("Workflow saved successfully");
} catch (Exception e) {
    LOGGER.error("Error saving workflow", e);
    Notification.show("Error: " + e.getMessage());
}
```

## Testing Checklist

- [ ] Create workflow entity
- [ ] Update workflow entity
- [ ] Delete workflow entity (with dependency check)
- [ ] List workflows by project
- [ ] Activate/deactivate workflow
- [ ] Test unique constraint (name + project)
- [ ] Test cascading delete when project is deleted
- [ ] Test screen initialization
- [ ] Test sample data creation

## Troubleshooting

### "WorkflowEntityService bean not found"
- Ensure `@Service` annotation is present
- Check that component scanning includes workflow package
- Verify Spring Boot application context is loading correctly

### "Cannot delete workflow" Error
- Check if workflow is being referenced by other entities
- Verify user has appropriate permissions
- Review `checkDeleteAllowed()` implementation

### Unique Constraint Violation
- Ensure workflow name is unique within the project
- Check database for existing workflows with same name
- Consider adding project prefix to workflow name

## Related Files

### Source Files
- Domain: `src/main/java/tech/derbent/app/workflow/domain/`
- Service: `src/main/java/tech/derbent/app/workflow/service/`
- Data Initializer: `src/main/java/tech/derbent/api/config/CDataInitializer.java`

### Documentation
- [Workflow Entity Pattern](WORKFLOW_ENTITY_PATTERN.md)
- [Check Delete Allowed Pattern](CHECK_DELETE_ALLOWED_PATTERN.md)

## Configuration

### Menu Configuration
```java
menuOrder = Menu_Order_PROJECT + ".21"
menuTitle = MenuTitle_PROJECT + ".Workflows"
showInQuickToolbar = true
```

### Grid Columns
```java
grid.setColumnFields(List.of(
    "id", "name", "description", "isActive", "project"
));
```

### Detail Screen Fields
1. Workflow Information: name, description, isActive, project
2. Audit: createdDate, lastModifiedDate

## Support

For questions or issues:
1. Check implementation documentation
2. Review sample data in CDataInitializer
3. Examine existing entity patterns (CProjectItemStatus, CMeetingStatus)
4. Consult development team
