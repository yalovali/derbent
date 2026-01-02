# Database Uniqueness Constraints Audit

## Overview
This document provides a comprehensive audit of database uniqueness constraints across all domain entities in the Derbent project. It identifies entities that need unique constraints to enforce data integrity at the database level.

## Constraint Categories

### 1. Already Implemented ✅
These entities already have unique constraints:

| Entity | Constraint | Columns | Purpose |
|--------|------------|---------|---------|
| CSprintItem | uk_sprint_item_unique | (sprint_id, item_id) | One item per sprint |
| CKanbanColumn | uk_kanban_column_name | (kanban_line_id, name) | Unique column names within kanban line |
| CWorkflowStatusRelation | - | (workflow_id, from_status_id, to_status_id) | No duplicate workflow transitions |
| CUserProjectRole | - | (name, project_id) | Unique role names within project |
| CUserCompanyRole | - | (name, company_id) | Unique role names within company |
| CUserProjectSettings | - | (user_id, project_id) | One settings record per user-project pair |
| CUserCompanySetting | - | (user_id, company_id) | One settings record per user-company pair |

### 2. Critical Missing Constraints (High Priority) ⚠️

#### A. User-Role Assignment Relationships
**CUserProjectRoleAssignment** (if exists - needs verification)
- **Constraint**: (user_id, project_id, role_id)
- **Purpose**: Prevent same user from having duplicate role assignments in same project
- **Risk**: Users could be assigned same role multiple times

**CUserCompanyRoleAssignment** (if exists - needs verification)
- **Constraint**: (user_id, company_id, role_id)
- **Purpose**: Prevent duplicate company role assignments
- **Risk**: Data redundancy, inconsistent role management

#### B. Team-Member Relationships
**CTeamMember** (if exists - needs verification)
- **Constraint**: (team_id, user_id)
- **Purpose**: One user per team membership
- **Risk**: Duplicate team memberships

#### C. Workflow Entity Relationships
**CWorkflowEntity**
- **Constraint**: (name, project_id) for project-scoped workflows OR (name, company_id) for company-scoped
- **Purpose**: Unique workflow names within scope
- **Risk**: Confusing duplicate workflow names

#### D. Project-Related Entities
**CProject**
- **Constraint**: (name, company_id)
- **Purpose**: Unique project names within company
- **Risk**: Confusing duplicate project names

**CActivity**
- **Constraint**: Consider (name, project_id) if activities must have unique names within project
- **Risk**: Depends on business requirements

**CMeeting**
- **Constraint**: Consider (name, project_id, scheduled_date) for unique meetings
- **Risk**: Depends on business requirements

#### E. Type/Category Entities
All entity types that extend CTypeOfProject or similar should have:
- **Constraint**: (name, project_id)
- **Examples**: CActivityType, CMeetingType, CRiskType, COrderType, etc.
- **Purpose**: Unique type names within project
- **Risk**: Duplicate type names cause confusion

### 3. Medium Priority Constraints 📋

#### A. Named Company Entities
Entities extending CEntityOfCompany should consider:
- **Constraint**: (name, company_id)
- **Examples**: CProvider, CAsset, CProduct, etc.
- **Purpose**: Unique names within company scope
- **Risk**: Depends on whether duplicates are acceptable

#### B. Grid and View Entities
**CGrid**
- **Constraint**: (name, company_id) or (name, user_id) depending on scope
- **Purpose**: Unique grid names for user/company
- **Risk**: Grid name confusion

**CPageEntity**
- **Constraint**: (name, company_id)
- **Purpose**: Unique page names
- **Risk**: Page configuration confusion

### 4. Detail/Line Item Relationships 📝

#### A. Master-Detail Relationships
**CDetailLines**
- **Constraint**: (master_section_id, order_number) if order matters
- **Purpose**: Prevent duplicate order numbers within section
- **Risk**: Data display order corruption

**CSprintItem** (already done ✅)
- **Constraint**: (sprint_id, item_id)
- **Status**: Implemented

**COrderItem** (if exists - needs verification)
- **Constraint**: (order_id, product_id) or (order_id, line_number)
- **Purpose**: Prevent duplicate line items
- **Risk**: Billing/calculation errors

### 5. Low Priority (Consider Based on Business Rules) ⏸️

#### A. Comment Relationships
**CComment**
- **Possible Constraint**: None - multiple comments on same entity are expected
- **Rationale**: Users should be able to comment multiple times

#### B. Approval Workflows
**COrderApproval**
- **Constraint**: (order_id, approver_id, approval_level)
- **Purpose**: One approval per approver per level
- **Risk**: Duplicate approvals

#### C. Budget Relationships
**CBudget**
- **Constraint**: (name, project_id) or (fiscal_year, project_id)
- **Purpose**: Unique budget names or one budget per fiscal year per project
- **Risk**: Budget confusion

### 6. Relationship Tables (Many-to-Many) 🔗

#### A. Role-Privilege Relationships
**WorkflowStatusRelation_Roles**
- **Constraint**: Already handled via @ManyToMany with join table
- **Purpose**: Prevent duplicate role assignments to workflow transitions
- **Status**: Framework handles this ✅

### 7. Additional Constraints to Consider 🔍

#### A. Check Constraints (Not Just Unique)
- Date ranges: start_date <= end_date
- Percentages: 0 <= progress <= 100
- Positive values: budget_amount > 0
- Email format: email matches pattern
- Phone format: phone matches pattern

#### B. Foreign Key Constraints
Most relationships should have:
- ON DELETE behavior specified (CASCADE, RESTRICT, SET NULL)
- ON UPDATE behavior specified (CASCADE, RESTRICT)

**Example**:
```java
@ManyToOne
@JoinColumn(name = "company_id", nullable = false)
@OnDelete(action = OnDeleteAction.CASCADE)
private CCompany company;
```

## Implementation Priority

### Phase 1 (Immediate) 🚨
1. CWorkflowEntity - (name, project_id)
2. CProject - (name, company_id)
3. Team member relationships
4. User role assignment relationships

### Phase 2 (Short-term) 📅
1. All Type entities - (name, project_id)
2. Named company entities - (name, company_id)
3. Grid/View entities
4. Order-based detail line items

### Phase 3 (Medium-term) 📆
1. Budget entities
2. Approval workflow entities
3. Additional business-rule constraints

### Phase 4 (Long-term) 📊
1. Check constraints (date ranges, percentages, etc.)
2. Review and optimize existing constraints
3. Add database-level CHECK constraints for business rules

## Migration Strategy

### Step 1: Data Cleanup
Before adding constraints, clean existing data:
```sql
-- Example: Find duplicates
SELECT sprint_id, item_id, COUNT(*)
FROM csprint_items
GROUP BY sprint_id, item_id
HAVING COUNT(*) > 1;

-- Delete duplicates, keeping oldest
DELETE FROM csprint_items
WHERE sprint_item_id NOT IN (
    SELECT MIN(sprint_item_id)
    FROM csprint_items
    GROUP BY sprint_id, item_id
);
```

### Step 2: Add Constraints
Add constraints to entity classes:
```java
@Entity
@Table(name = "centity",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_entity_name_scope",
        columnNames = {"name", "company_id"}
    )
)
public class CEntity extends CEntityDB<CEntity> {
    // ...
}
```

### Step 3: Database Migration
Generate and run Hibernate DDL or write manual migration scripts:
```sql
ALTER TABLE centity
ADD CONSTRAINT uk_entity_name_scope
UNIQUE (name, company_id);
```

### Step 4: Application-Level Guards
Add application-level checks before database constraints:
```java
// Check for existing entity
final Optional<CEntity> existing = 
    repository.findByNameAndCompany(name, company);
if (existing.isPresent()) {
    throw new DuplicateEntityException("Entity already exists");
}
```

### Step 5: Testing
- Test constraint violations return proper error messages
- Test that duplicates are actually prevented
- Test CASCADE delete behavior
- Test NULL handling

## Constraint Naming Convention

Use consistent naming for constraints:
- **Format**: `uk_{table}_{column1}_{column2}_{columnN}`
- **Examples**:
  - `uk_sprint_item_unique` → (sprint_id, item_id)
  - `uk_project_name_company` → (name, company_id)
  - `uk_workflow_transition` → (workflow_id, from_status_id, to_status_id)

## Notes

1. **Nullable Columns**: Unique constraints treat NULL as distinct values. Consider business rules carefully.
2. **Partial Indexes**: For conditional uniqueness, consider partial indexes (PostgreSQL specific).
3. **Case Sensitivity**: String uniqueness is case-sensitive. Consider using UPPER() or indexes with collation.
4. **Performance**: Unique constraints create implicit indexes, improving query performance.
5. **Error Handling**: Application must handle constraint violation exceptions gracefully.

## References

- JPA Specification: https://jakarta.ee/specifications/persistence/3.1/
- Hibernate Documentation: https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html
- PostgreSQL Constraints: https://www.postgresql.org/docs/current/ddl-constraints.html
