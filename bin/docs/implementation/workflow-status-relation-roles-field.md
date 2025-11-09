# Workflow Status Relation Roles Field Implementation

## Overview
This document describes the implementation of the multi-role selection feature for workflow status transitions. The change updates the `CWorkflowStatusRelation` entity from supporting a single role to supporting multiple roles for each state transition.

## Problem Statement
Previously, workflow status transitions (e.g., from "In Progress" to "Done") could only be assigned to a single role. This was limiting because in real-world scenarios, multiple roles might be allowed to perform the same transition.

## Solution
We implemented a many-to-many relationship between `CWorkflowStatusRelation` and `CUserProjectRole`, allowing multiple roles to be assigned to each transition. We also created a new UI component `CComponentListSelection` that displays items in a grid with checkmarks for selected items, without ordering controls.

## Key Changes

### 1. Domain Model (`CWorkflowStatusRelation`)
**Before:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "role_id", nullable = true)
private CUserProjectRole role;
```

**After:**
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "cworkflowstatusrelation_roles", 
    joinColumns = @JoinColumn(name = "cworkflowstatusrelation_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
@AMetaData(
    displayName = "User Roles", 
    required = false, 
    readOnly = false,
    description = "The user roles allowed to make this transition (allowed transition roles)", 
    hidden = false, 
    order = 4,
    setBackgroundFromColor = true, 
    useIcon = true, 
    dataProviderBean = "CUserProjectRoleService", 
    useGridSelection = true
)
private List<CUserProjectRole> roles = new ArrayList<>();
```

**Key Points:**
- Changed from `@ManyToOne` to `@ManyToMany`
- Added join table `cworkflowstatusrelation_roles`
- Updated annotation to use `useGridSelection = true`
- Changed field name from `role` to `roles`
- Changed type from `CUserProjectRole` to `List<CUserProjectRole>`

### 2. Database Schema
**New Join Table:**
```sql
CREATE TABLE cworkflowstatusrelation_roles (
    cworkflowstatusrelation_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (cworkflowstatusrelation_id, role_id),
    FOREIGN KEY (cworkflowstatusrelation_id) REFERENCES cworkflowstatusrelation(cworkflowstatusrelation_id),
    FOREIGN KEY (role_id) REFERENCES cuserprojectrole(cuserprojectrole_id)
);
```

**Updated Unique Constraint:**
Previously included `role_id`, now only:
```java
@UniqueConstraint(columnNames = {"workflow_id", "from_status_id", "to_status_id"})
```

### 3. New UI Component (`CComponentListSelection`)
Created a new generic list selection component that:
- Displays all items in a single grid
- Shows checkmarks (✓) for selected items
- Supports click-to-toggle selection
- Does NOT support ordering (unlike `CComponentFieldSelection`)
- Integrates with Vaadin binders
- Supports colorful rendering for `CEntityNamed` entities
- Follows the same pattern as `CComponentFieldSelection` but simplified

**Key Features:**
```java
public class CComponentListSelection<MasterEntity, DetailEntity> 
    extends CVerticalLayout
    implements HasValue<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>>
```

**Usage Pattern:**
1. Component receives all available items via `setSourceItems()`
2. Binder sets currently selected items via `setValue()`
3. User clicks items to toggle selection
4. Selected items are marked with a green checkmark
5. Binder reads selection via `getValue()`

### 4. Annotation System (`AMetaData`)
**New Annotation Attribute:**
```java
/** When true, uses a grid-based list selector component (single grid with checkmarks for selected items) 
 * instead of MultiSelectComboBox for List/Set fields. This provides a simpler selection UX without ordering controls. 
 * If both useDualListSelector and useGridSelection are true, useGridSelection takes precedence. */
boolean useGridSelection() default false;
```

### 5. Form Builder Integration
Added logic to `CFormBuilder` to create `CComponentListSelection` when `useGridSelection = true`:

```java
if (fieldInfo.isUseGridSelection()) {
    component = createGridListSelector(contentOwner, fieldInfo, binder);
} else if (fieldInfo.isUseDualListSelector()) {
    component = createDualListSelector2(contentOwner, fieldInfo, binder);
} else {
    component = createComboBoxMultiSelect(contentOwner, fieldInfo, binder);
}
```

### 6. Service Layer Updates

**Repository (`IWorkflowStatusRelationRepository`):**
- Updated queries to use `LEFT JOIN FETCH r.roles` instead of `LEFT JOIN FETCH r.role`
- Removed role-specific filters from unique constraint checks
- Changed method signatures:
  - `findByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId()` → `findByWorkflowIdAndFromStatusIdAndToStatusId()`
  - `existsByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId()` → `existsByWorkflowIdAndFromStatusIdAndToStatusId()`
  - `deleteByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId()` → `deleteByWorkflowIdAndFromStatusIdAndToStatusId()`

**Service (`CWorkflowStatusRelationService`):**
- Updated method signatures to accept `List<CUserProjectRole>` instead of `CUserProjectRole`
- Removed role-based filtering from methods
- Example changes:
  - `addStatusTransition(workflow, fromStatus, toStatus, role)` → `addStatusTransition(workflow, fromStatus, toStatus, roles)`
  - `deleteByWorkflowAndStatuses(workflow, fromStatus, toStatus, role)` → `deleteByWorkflowAndStatuses(workflow, fromStatus, toStatus)`

### 7. View Layer Updates

**Dialog (`CWorkflowStatusRelationDialog`):**
- Updated form fields list: `"role"` → `"roles"`

**Base Component (`CComponentWorkflowStatusRelationBase`):**
- Updated grid rendering to display roles as comma-separated list with icons
- Updated delete confirmation message to show all roles
- Updated display text generation to handle multiple roles

## Testing
Comprehensive unit tests were added for `CComponentListSelection`:
- Binder integration tests
- Value change listener tests
- Clear functionality
- Component initialization
- List separation
- Read-only mode
- Source items handling
- Null value handling

All tests pass successfully.

## Migration Notes

### Database Migration
When deploying this change, the database schema must be updated:
1. Create the new join table `cworkflowstatusrelation_roles`
2. Migrate existing data from `role_id` column to the join table
3. Update the unique constraint on `cworkflowstatusrelation` table

**Example migration SQL:**
```sql
-- Create new join table
CREATE TABLE cworkflowstatusrelation_roles (
    cworkflowstatusrelation_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (cworkflowstatusrelation_id, role_id),
    FOREIGN KEY (cworkflowstatusrelation_id) REFERENCES cworkflowstatusrelation(cworkflowstatusrelation_id),
    FOREIGN KEY (role_id) REFERENCES cuserprojectrole(cuserprojectrole_id)
);

-- Migrate existing data
INSERT INTO cworkflowstatusrelation_roles (cworkflowstatusrelation_id, role_id)
SELECT cworkflowstatusrelation_id, role_id 
FROM cworkflowstatusrelation 
WHERE role_id IS NOT NULL;

-- Drop old unique constraint
ALTER TABLE cworkflowstatusrelation 
DROP CONSTRAINT IF EXISTS unique_workflow_status_role;

-- Add new unique constraint (without role_id)
ALTER TABLE cworkflowstatusrelation 
ADD CONSTRAINT unique_workflow_status 
UNIQUE (workflow_id, from_status_id, to_status_id);

-- Drop old foreign key column
ALTER TABLE cworkflowstatusrelation 
DROP COLUMN role_id;
```

### Code Migration
Existing code that references `getRole()` or `setRole()` must be updated to use `getRoles()` or `setRoles()`.

## UI Screenshots
Since the application requires a database to run and we couldn't start it in the test environment, manual UI verification should be performed after deployment to ensure:
1. The roles field displays as a grid with checkmarks
2. Clicking items toggles selection
3. Selected roles are shown with green checkmarks
4. The grid displays role icons and colors correctly
5. Form submission saves all selected roles

## Benefits
1. **More Flexible Permissions**: Multiple roles can now perform the same status transition
2. **Better UX**: Grid-based selection with visual checkmarks is clearer than multi-select combo box
3. **Consistent Pattern**: Follows existing component patterns (CComponentFieldSelection)
4. **Generic Solution**: The new component can be reused for other list selection needs
5. **Maintainable**: Clear separation of concerns and follows coding guidelines

## Files Changed
- `CWorkflowStatusRelation.java` - Domain entity
- `CComponentListSelection.java` - New UI component (created)
- `AMetaData.java` - Added useGridSelection attribute
- `CEntityFieldService.java` - Added useGridSelection support
- `CFormBuilder.java` - Added createGridListSelector method
- `CWorkflowStatusRelationService.java` - Updated service methods
- `IWorkflowStatusRelationRepository.java` - Updated repository queries
- `CWorkflowStatusRelationDialog.java` - Updated form fields
- `CComponentWorkflowStatusRelationBase.java` - Updated grid display
- `CComponentListSelectionTest.java` - Test file (created)
