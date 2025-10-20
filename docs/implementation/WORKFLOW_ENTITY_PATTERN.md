# Workflow Entity Implementation Pattern

## Overview
The Workflow Entity provides a flexible framework for defining status transition workflows based on user roles in projects. This document describes the implementation pattern, data structures, and recommended GUI components for workflow management.

## Entity Structure

### Class Hierarchy
```
CEntityDB (base)
  └─ CEntityNamed
      └─ CEntityOfProject
          └─ CProjectItem
              └─ CWorkflowBase<T>
                  └─ CWorkflowEntity
```

### Domain Model

#### CWorkflowEntity
**Location:** `tech.derbent.app.workflow.domain.CWorkflowEntity`

**Table:** `cworkflowentity`

**Key Fields:**
- `id` (Long): Primary key
- `name` (String): Workflow name (unique per project)
- `description` (String): Workflow description
- `isActive` (Boolean): Whether workflow is currently active
- `project` (CProject): Associated project
- `parentId` (Long): Optional parent workflow ID for hierarchical workflows
- `parentType` (String): Type of parent entity

**JPA Annotations:**
```java
@Entity
@Table(name = "cworkflowentity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "project_id"
}))
@AttributeOverride(name = "id", column = @Column(name = "cworkflowentity_id"))
```

## Service Layer

### CWorkflowEntityService
**Location:** `tech.derbent.app.workflow.service.CWorkflowEntityService`

**Responsibilities:**
- CRUD operations for workflow entities
- Dependency checking before deletion
- Workflow activation/deactivation
- Workflow validation

**Key Methods:**
```java
public String checkDeleteAllowed(CWorkflowEntity entity)
protected Class<CWorkflowEntity> getEntityClass()
public void initializeNewEntity(CWorkflowEntity entity)
```

### Repository Pattern
```java
IWorkflowRepository<EntityClass extends CWorkflowBase<EntityClass>>
    ├─ extends IEntityOfProjectRepository<EntityClass>
    └─ IWorkflowEntityRepository (concrete implementation)
```

## Initialization

### CWorkflowEntityInitializerService
**Location:** `tech.derbent.app.workflow.service.CWorkflowEntityInitializerService`

**Configuration:**
- Menu Order: `Menu_Order_PROJECT + ".21"`
- Menu Title: `MenuTitle_PROJECT + ".Workflows"`
- Show in Quick Toolbar: `true`
- Page Title: "Workflow Management"

**Screen Fields:**
1. Workflow Information section:
   - name
   - description
   - isActive
   - project
2. Audit section:
   - createdDate
   - lastModifiedDate

**Grid Columns:**
- id, name, description, isActive, project

## Sample Data

Sample workflows are created in `CDataInitializer.initializeSampleWorkflowEntities()`:

1. **Activity Status Workflow**: Defines status transitions for activities
2. **Meeting Status Workflow**: Defines status transitions for meetings
3. **Decision Approval Workflow**: Defines approval workflow for strategic decisions

## Workflow Matrix Data Structure

### Recommended Implementation

The workflow should support a matrix-like structure that defines status transitions based on user roles:

```
Workflow Matrix Structure:
┌──────────────┬────────────────────────────────────────────────────┐
│ From Status  │ To Status (by Role)                                │
│              ├─────────────┬─────────────┬─────────────────────────┤
│              │ Admin       │ User        │ Guest                   │
├──────────────┼─────────────┼─────────────┼─────────────────────────┤
│ Not Started  │ In Progress │ In Progress │ -                       │
│              │ Cancelled   │ -           │ -                       │
├──────────────┼─────────────┼─────────────┼─────────────────────────┤
│ In Progress  │ Completed   │ Completed   │ -                       │
│              │ On Hold     │ On Hold     │ -                       │
│              │ Cancelled   │ -           │ -                       │
├──────────────┼─────────────┼─────────────┼─────────────────────────┤
│ On Hold      │ In Progress │ In Progress │ -                       │
│              │ Cancelled   │ -           │ -                       │
└──────────────┴─────────────┴─────────────┴─────────────────────────┘
```

### Database Schema Recommendation

#### CWorkflowTransition Table
```java
@Entity
@Table(name = "cworkflowtransition")
public class CWorkflowTransition {
    @Id
    private Long id;
    
    @ManyToOne
    private CWorkflowEntity workflow;
    
    @ManyToOne
    private CStatus fromStatus;  // Source status
    
    @ManyToOne
    private CStatus toStatus;    // Target status
    
    @ManyToOne
    private CUserProjectRole requiredRole;  // Role required for transition
    
    @Column(name = "is_allowed")
    private Boolean isAllowed = true;  // Whether transition is allowed
    
    private Integer order;  // Display order in UI
}
```

### JSON Configuration Alternative

For simpler implementations, workflow transitions can be stored as JSON:

```json
{
  "workflowId": 1,
  "workflowName": "Activity Status Workflow",
  "transitions": [
    {
      "fromStatus": "Not Started",
      "toStatus": "In Progress",
      "allowedRoles": ["Admin", "User"],
      "conditions": []
    },
    {
      "fromStatus": "In Progress",
      "toStatus": "Completed",
      "allowedRoles": ["Admin", "User"],
      "conditions": ["activity.progress >= 100"]
    }
  ]
}
```

## GUI Component Recommendations

### 1. Matrix Grid Component (Vaadin Grid)

```java
public class WorkflowMatrixGrid extends Grid<WorkflowTransitionRow> {
    private List<CStatus> statuses;
    private List<CUserProjectRole> roles;
    
    public WorkflowMatrixGrid() {
        // Column for "From Status"
        addColumn(WorkflowTransitionRow::getFromStatus)
            .setHeader("From Status")
            .setFrozen(true);
        
        // Dynamic columns for each role
        roles.forEach(role -> {
            addComponentColumn(row -> {
                Set<CStatus> allowedStatuses = 
                    row.getAllowedStatusesForRole(role);
                return new MultiSelect<>(allowedStatuses);
            }).setHeader(role.getName());
        });
    }
}
```

### 2. Transition Editor Dialog

```java
public class WorkflowTransitionEditor extends Dialog {
    private ComboBox<CStatus> fromStatus;
    private ComboBox<CStatus> toStatus;
    private MultiSelectComboBox<CUserProjectRole> allowedRoles;
    private TextField conditionExpression;
    
    public WorkflowTransitionEditor(CWorkflowEntity workflow) {
        setHeaderTitle("Edit Workflow Transition");
        
        // Configure form fields
        fromStatus = new ComboBox<>("From Status");
        toStatus = new ComboBox<>("To Status");
        allowedRoles = new MultiSelectComboBox<>("Allowed Roles");
        conditionExpression = new TextField("Condition (optional)");
        
        // Layout components
        FormLayout form = new FormLayout();
        form.add(fromStatus, toStatus, allowedRoles, conditionExpression);
        add(form);
        
        // Action buttons
        Button save = new Button("Save", e -> save());
        Button cancel = new Button("Cancel", e -> close());
        getFooter().add(cancel, save);
    }
}
```

### 3. Visual Workflow Designer (Advanced)

For advanced visualization, consider using:
- **mxGraph** or **JointJS** libraries for flowchart-style workflow visualization
- **Dagre** for automatic layout of workflow diagrams
- Custom Vaadin components wrapping D3.js for interactive workflow graphs

### 4. Simple List View (Minimal Implementation)

```java
public class WorkflowTransitionList extends VerticalLayout {
    private Grid<CWorkflowTransition> grid;
    
    public WorkflowTransitionList(CWorkflowEntity workflow) {
        grid = new Grid<>(CWorkflowTransition.class);
        grid.setColumns("fromStatus", "toStatus", "requiredRole");
        grid.addComponentColumn(this::createActionButtons);
        
        add(new H3("Workflow Transitions"), grid);
    }
    
    private Component createActionButtons(CWorkflowTransition transition) {
        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        return new HorizontalLayout(edit, delete);
    }
}
```

## Usage Patterns

### Creating a New Workflow

```java
CProject project = sessionService.getActiveProject().orElseThrow();
CWorkflowEntity workflow = new CWorkflowEntity("Custom Workflow", project);
workflow.setDescription("Workflow for custom process");
workflow.setIsActive(true);
workflowEntityService.save(workflow);
```

### Checking Workflow Transitions

```java
public boolean canTransition(CActivity activity, 
                             CActivityStatus targetStatus, 
                             CUser user) {
    CWorkflowEntity workflow = findWorkflowForActivity(activity);
    CUserProjectRole userRole = getUserRoleInProject(user, activity.getProject());
    
    return workflow.getTransitions().stream()
        .anyMatch(t -> 
            t.getFromStatus().equals(activity.getStatus()) &&
            t.getToStatus().equals(targetStatus) &&
            t.getAllowedRoles().contains(userRole)
        );
}
```

## Testing Considerations

### Unit Tests
```java
@Test
void testWorkflowCreation() {
    CProject project = new CProject("Test Project", company);
    CWorkflowEntity workflow = new CWorkflowEntity("Test Workflow", project);
    workflow.setIsActive(true);
    
    assertNotNull(workflow.getName());
    assertTrue(workflow.getIsActive());
    assertEquals(project, workflow.getProject());
}
```

### Integration Tests
- Test workflow CRUD operations
- Test status transition validation
- Test role-based access control
- Test workflow activation/deactivation

## Future Enhancements

1. **Conditional Transitions**: Support for complex conditions (e.g., "activity.progress >= 80")
2. **Notification Rules**: Automatic notifications on status transitions
3. **Audit Trail**: Track all workflow executions
4. **Workflow Templates**: Pre-defined workflow templates for common scenarios
5. **Workflow Versioning**: Support for workflow version history
6. **Parallel Workflows**: Support for parallel approval processes
7. **Time-based Transitions**: Automatic transitions after time periods

## Best Practices

1. **Keep Workflows Simple**: Start with simple status transitions before adding complexity
2. **Role-Based Design**: Design workflows around user roles, not individual users
3. **Document Workflows**: Clearly document workflow purpose and transitions
4. **Test Thoroughly**: Test all transition paths and edge cases
5. **Version Control**: Maintain workflow versions when making changes
6. **Performance**: Index foreign keys and frequently queried fields
7. **Security**: Always validate user permissions before allowing transitions

## Related Documentation

- [Check Delete Allowed Pattern](CHECK_DELETE_ALLOWED_PATTERN.md)
- [Dependency Checking System](DEPENDENCY_CHECKING_SYSTEM.md)
- [Authentication Mechanism](LOGIN_AUTHENTICATION_MECHANISM.md)
