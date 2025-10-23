# Workflow Status Relation Pattern

## Overview
The Workflow Status Relation pattern manages the many-to-many relationship between workflows and status transitions, with role-based access control. This pattern follows the same structure as the CUserProjectSettings pattern, establishing a relationship between CWorkflowEntity and CProjectItemStatus entities.

## Purpose
Define which status transitions are allowed in a workflow, optionally filtered by user roles. For example:
- A workflow might allow transition from "TODO" to "IN_PROGRESS" for all users
- Another transition from "IN_PROGRESS" to "DONE" might be restricted to specific roles (e.g., Project Managers)

## Entity Structure

### CWorkflowStatusRelation
**Location:** `tech.derbent.app.workflow.domain.CWorkflowStatusRelation`

**Table:** `cworkflowstatusrelation`

**Key Fields:**
- `id` (Long): Primary key
- `workflow` (CWorkflowEntity): The workflow this relation belongs to
- `fromStatus` (CProjectItemStatus): Starting status of the transition
- `toStatus` (CProjectItemStatus): Target status of the transition
- `role` (CUserProjectRole): Optional role required to perform this transition

**JPA Annotations:**
```java
@Entity
@Table(name = "cworkflowstatusrelation", uniqueConstraints = @UniqueConstraint(columnNames = {
    "workflow_id", "from_status_id", "to_status_id", "role_id"
}))
@AttributeOverride(name = "id", column = @Column(name = "cworkflowstatusrelation_id"))
```

**Unique Constraint:**
The combination of (workflow_id, from_status_id, to_status_id, role_id) must be unique, ensuring each transition can only be defined once per role in a workflow.

## Repository Layer

### IWorkflowStatusRelationRepository
**Location:** `tech.derbent.app.workflow.service.IWorkflowStatusRelationRepository`

**Key Methods:**
```java
// Find all relations for a workflow
List<CWorkflowStatusRelation> findByWorkflowId(Long workflowId);

// Find all relations from a specific status
List<CWorkflowStatusRelation> findByFromStatusId(Long statusId);

// Find all relations to a specific status
List<CWorkflowStatusRelation> findByToStatusId(Long statusId);

// Find specific relation
Optional<CWorkflowStatusRelation> findByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId(
    Long workflowId, Long fromStatusId, Long toStatusId, Long roleId);

// Check if relation exists
boolean existsByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId(
    Long workflowId, Long fromStatusId, Long toStatusId, Long roleId);

// Delete specific relation
void deleteByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId(
    Long workflowId, Long fromStatusId, Long toStatusId, Long roleId);

// Delete all relations for a workflow
void deleteByWorkflowId(Long workflowId);
```

**Query Pattern:**
All queries use eager fetching with LEFT JOIN FETCH to load related entities:
```java
@Query("SELECT r FROM #{#entityName} r " +
       "LEFT JOIN FETCH r.workflow " +
       "LEFT JOIN FETCH r.fromStatus " +
       "LEFT JOIN FETCH r.toStatus " +
       "LEFT JOIN FETCH r.role " +
       "WHERE r.workflow.id = :workflowId")
```

## Service Layer

### CWorkflowStatusRelationService
**Location:** `tech.derbent.app.workflow.service.CWorkflowStatusRelationService`

**Extends:** `CAbstractEntityRelationService<CWorkflowStatusRelation>`

**Key Methods:**
```java
// Add a status transition to a workflow
CWorkflowStatusRelation addStatusTransition(
    CWorkflowEntity workflow,
    CProjectItemStatus fromStatus,
    CProjectItemStatus toStatus,
    CUserProjectRole role);

// Remove a status transition
void deleteByWorkflowAndStatuses(
    CWorkflowEntity workflow,
    CProjectItemStatus fromStatus,
    CProjectItemStatus toStatus,
    CUserProjectRole role);

// Find all relations for a workflow
List<CWorkflowStatusRelation> findByWorkflow(CWorkflowEntity workflow);

// Find all relations by status
List<CWorkflowStatusRelation> findByFromStatus(CProjectItemStatus fromStatus);
List<CWorkflowStatusRelation> findByToStatus(CProjectItemStatus toStatus);

// Find all relations by role
List<CWorkflowStatusRelation> findByRole(CUserProjectRole role);

// Find specific relation with role
Optional<CWorkflowStatusRelation> findRelationshipWithRole(
    Long workflowId, Long fromStatusId, Long toStatusId, Long roleId);

// Update a transition's role
CWorkflowStatusRelation updateStatusTransition(
    CWorkflowEntity workflow,
    CProjectItemStatus fromStatus,
    CProjectItemStatus toStatus,
    CUserProjectRole oldRole,
    CUserProjectRole newRole);
```

**Validation:**
The service validates:
- All entities (workflow, fromStatus, toStatus) are not null
- All entities have valid IDs before creating relations
- No duplicate relations exist before creating new ones

## UI Components

### CWorkflowStatusRelationInitializerService
**Location:** `tech.derbent.app.workflow.service.CWorkflowStatusRelationInitializerService`

**Purpose:** Initializes UI configuration for workflow status relations.

**Key Methods:**
```java
// Create detail view
CDetailSection createBasicView(CProject project);

// Create grid entity
CGridEntity createGridEntity(CProject project);

// Initialize complete UI setup
void initialize(CProject project, 
                CGridEntityService gridEntityService,
                CDetailSectionService detailSectionService,
                CPageEntityService pageEntityService,
                boolean showInQuickToolbar);
```

**Display Fields:**
- Detail View: workflow, fromStatus, toStatus, role, active, audit fields
- Grid View: id, workflow, fromStatus, toStatus, role, active

### CWorkflowStatusRelationDialog
**Location:** `tech.derbent.app.workflow.view.CWorkflowStatusRelationDialog`

**Extends:** `CDBRelationDialog<CWorkflowStatusRelation, CWorkflowEntity, CProjectItemStatus>`

**Purpose:** Dialog for creating and editing workflow status transitions.

**Form Fields:**
- `fromStatus`: ComboBox to select the starting status
- `toStatus`: ComboBox to select the target status
- `role`: ComboBox to select the required role (optional)

**Constructor:**
```java
public CWorkflowStatusRelationDialog(
    IContentOwner parentContent,
    CWorkflowEntityService workflowService,
    CProjectItemStatusService statusService,
    CWorkflowStatusRelationService workflowStatusRelationService,
    CWorkflowStatusRelation relation,
    CWorkflowEntity workflow,
    Consumer<CWorkflowStatusRelation> onSave) throws Exception
```

## Usage Examples

### Creating a Status Transition

```java
@Autowired
private CWorkflowStatusRelationService relationService;

@Autowired
private CWorkflowEntityService workflowService;

@Autowired
private CProjectItemStatusService statusService;

@Autowired
private CUserProjectRoleService roleService;

public void createTransition() {
    // Get entities
    CWorkflowEntity workflow = workflowService.findById(workflowId);
    CProjectItemStatus fromStatus = statusService.findByName("TODO");
    CProjectItemStatus toStatus = statusService.findByName("IN_PROGRESS");
    CUserProjectRole role = roleService.findByName("Developer");
    
    // Create the transition
    CWorkflowStatusRelation relation = relationService.addStatusTransition(
        workflow, fromStatus, toStatus, role);
}
```

### Querying Status Transitions

```java
// Get all transitions for a workflow
List<CWorkflowStatusRelation> transitions = 
    relationService.findByWorkflow(workflow);

// Get all possible next statuses from current status
List<CWorkflowStatusRelation> availableTransitions = 
    relationService.findByFromStatus(currentStatus);

// Check if a transition is allowed for a role
boolean canTransition = relationService.relationshipExists(
    workflow.getId(), 
    fromStatus.getId(), 
    toStatus.getId(), 
    role.getId());
```

### Opening the Edit Dialog

```java
// For creating a new transition
CWorkflowStatusRelationDialog dialog = new CWorkflowStatusRelationDialog(
    parentContent,
    workflowService,
    statusService,
    relationService,
    null,  // null for new relation
    workflow,
    relation -> {
        // Handle save
        LOGGER.info("Saved transition: {}", relation);
    }
);
dialog.open();

// For editing an existing transition
CWorkflowStatusRelationDialog dialog = new CWorkflowStatusRelationDialog(
    parentContent,
    workflowService,
    statusService,
    relationService,
    existingRelation,
    workflow,
    relation -> {
        // Handle save
        LOGGER.info("Updated transition: {}", relation);
    }
);
dialog.open();
```

## Database Schema

### Table: cworkflowstatusrelation

**Columns:**
- `cworkflowstatusrelation_id` (BIGINT, PRIMARY KEY)
- `workflow_id` (BIGINT, NOT NULL, FOREIGN KEY -> cworkflowentity)
- `from_status_id` (BIGINT, NOT NULL, FOREIGN KEY -> cactivitystatus)
- `to_status_id` (BIGINT, NOT NULL, FOREIGN KEY -> cactivitystatus)
- `role_id` (BIGINT, NULLABLE, FOREIGN KEY -> cuserprojectrole)
- `active` (BOOLEAN)
- `created_date` (TIMESTAMP)
- `last_modified_date` (TIMESTAMP)

**Constraints:**
```sql
-- Unique constraint on transition
UNIQUE (workflow_id, from_status_id, to_status_id, role_id)

-- Foreign keys
FOREIGN KEY (workflow_id) REFERENCES cworkflowentity(cworkflowentity_id)
FOREIGN KEY (from_status_id) REFERENCES cactivitystatus(cactivitystatus_id)
FOREIGN KEY (to_status_id) REFERENCES cactivitystatus(cactivitystatus_id)
FOREIGN KEY (role_id) REFERENCES cuserprojectrole(cuserprojectrole_id)
```

## Comparison with CUserProjectSettings Pattern

This implementation directly mirrors the CUserProjectSettings pattern:

| Aspect | CUserProjectSettings | CWorkflowStatusRelation |
|--------|---------------------|-------------------------|
| **Purpose** | User-Project membership with roles | Workflow status transitions with roles |
| **Main Entity 1** | CUser | CWorkflowEntity |
| **Main Entity 2** | CProject | CProjectItemStatus (from) |
| **Additional Entity** | - | CProjectItemStatus (to) |
| **Role Entity** | CUserProjectRole | CUserProjectRole |
| **Repository** | IUserProjectSettingsRepository | IWorkflowStatusRelationRepository |
| **Service** | CUserProjectSettingsService | CWorkflowStatusRelationService |
| **Initializer** | CUserProjectSettingsInitializerService | CWorkflowStatusRelationInitializerService |
| **Dialog** | CUserProjectSettingsDialog | CWorkflowStatusRelationDialog |
| **Base Service** | CAbstractEntityRelationService | CAbstractEntityRelationService |

## Key Design Decisions

1. **Role is Optional:** The `role` field is nullable, allowing transitions that apply to all users
2. **Lazy Loading:** All relationships use LAZY fetch type with explicit eager loading in queries
3. **Unique Constraint:** Includes role_id to allow the same transition with different role requirements
4. **CProjectItemStatus:** Uses concrete status type rather than abstract CStatus for type safety
5. **Audit Fields:** Inherits created_date and last_modified_date from CEntityDB

## Testing Considerations

When testing workflow status relations:

1. **Database Initialization:** Ensure workflow, statuses, and roles exist before creating relations
2. **Null Roles:** Test both with and without role restrictions
3. **Duplicate Detection:** Verify that duplicate relation creation is prevented
4. **Cascade Deletion:** Consider what happens when workflow, status, or role is deleted
5. **Transaction Boundaries:** Test lazy loading works correctly across transaction boundaries

## Future Enhancements

Potential additions to this pattern:

1. **Conditions:** Add conditions for when a transition is available (e.g., field values)
2. **Actions:** Define actions to execute during transition (e.g., notifications)
3. **Validation Rules:** Custom validation before allowing transition
4. **History Tracking:** Log all status transitions
5. **Multiple Status Types:** Support different status types beyond CProjectItemStatus
