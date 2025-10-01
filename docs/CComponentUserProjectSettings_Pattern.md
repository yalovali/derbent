# CComponentUserProjectSettings Pattern Documentation

## Overview
This document describes the established pattern used by `CComponentUserProjectSettings` which should be replicated for CProject perspective and CCompany domains.

## Pattern Structure

### 1. Base Architecture
- **Base Class**: `CComponentUserProjectBase<MasterClass, RelationalClass>`
- **Inheritance Chain**: 
  - `CComponentUserProjectBase` extends `CComponentRelationBase<MasterClass, RelationalClass>`
  - `CComponentRelationBase` extends `CComponentDBEntity<MasterClass>`

### 2. Key Components

#### A. Component Class (`CComponentUserProjectSettings`)
```java
public class CComponentUserProjectSettings extends CComponentUserProjectBase<CUser, CUserProjectSettings>
```

**Responsibilities:**
- Manages user's project assignments
- CRUD operations for project relationships
- Data accessor setup via `setupDataAccessors()`
- Form population and refresh patterns

**Key Methods:**
- `setupDataAccessors()`: Sets up Supplier/Runnable for data access
- `populateForm()`: Refreshes data from accessors
- `openAddDialog()`: Opens dialog for adding new relationships
- `openEditDialog()`: Opens dialog for editing existing relationships
- `onSettingsSaved()`: Handles save operations with proper service calls

#### B. Dialog Class (`CUserProjectSettingsDialog`)
```java
public class CUserProjectSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CUser, CProject>
```

**Responsibilities:**
- Manages relationship entity editing
- Form fields: "project", "role", "permission"
- Proper entity setup and validation

#### C. Base Panel Class (`CPanelUserProjectBase`)
```java
public abstract class CPanelUserProjectBase<MasterClass, RelationalClass> extends CPanelRelationalBase<MasterClass, CUserProjectSettings>
```

**Responsibilities:**
- Common grid setup and button management
- Delete confirmation dialogs
- Abstract methods for add/edit dialog implementation

### 3. Integration Pattern

#### A. In Parent View (e.g., CUsersView)
```java
// Component instantiation with all required services
final CComponentUserProjectSettings component = new CComponentUserProjectSettings(
    parentContent, currentEntity, binder, 
    entityService, userTypeService, companyService, 
    projectService, userProjectSettingsService
);
```

#### B. Data Access Pattern
```java
// Supplier for getting related data
final Supplier<List<CUserProjectSettings>> supplier = () -> 
    userProjectSettingsService.findByUser(entity);

// Runnable for saving parent entity
final Runnable runnable = () -> entityService.save(entity);

// Set accessors
component.setSettingsAccessors(supplier, runnable);
```

### 4. Service Layer Integration

#### A. Relationship Service (`CUserProjectSettingsService`)
- Extends `CAbstractService<CUserProjectSettings>`
- Methods: `findByUser()`, `addUserToProject()`, `deleteByUserProject()`

#### B. Master Entity Service Integration
- Methods like `getAvailableProjectsForUser()` in related services
- Eager loading methods like `findByIdWithUserSettings()`

## Implementation Requirements for New Domains

### For CProject Perspective (`CComponentProjectUserSettings`)

1. **Create Component Class**:
   ```java
   public class CComponentProjectUserSettings extends CComponentUserProjectBase<CProject, CUserProjectSettings>
   ```

2. **Data Accessors**:
   ```java
   final Supplier<List<CUserProjectSettings>> supplier = () -> 
       project.getUserSettings(); // Or via service method
   ```

3. **Dialog Usage**:
   - Reuse existing `CProjectUserSettingsDialog`
   - Form fields: "user", "role", "permission"

4. **Integration in CProjectsView**:
   - Add component to accordion panels
   - Set up proper data loading patterns

### For CCompany Perspective (`CComponentCompanyUserSettings`)

1. **Create Component Class**:
   ```java
   public class CComponentCompanyUserSettings extends CComponentUserCompanyBase<CCompany, CUserCompanySettings>
   ```

2. **Create Base Class**:
   ```java
   public abstract class CComponentUserCompanyBase<MasterClass, RelationalClass> 
       extends CComponentRelationBase<MasterClass, CUserCompanySettings>
   ```

3. **Data Integration**:
   - Use existing `CUserCompanySettings` domain
   - Use existing `CUserCompanySettingsService`

## Key Patterns to Follow

### 1. Constructor Pattern
```java
public CComponent...(IContentOwner parentContent, final EntityType currentEntity, final CEnhancedBinder<EntityType> binder,
        final EntityService entityService, ..., final RelationshipService relationshipService) throws Exception {
    super("Title", parentContent, binder, EntityType.class, entityService, relationshipService);
    // Null checks
    // Service assignments  
    initPanel();
}
```

### 2. Data Accessor Pattern
```java
private void setupDataAccessors() {
    final Supplier<List<RelationshipType>> getterFunction = () -> {
        // Get current entity
        // Return relationships via service call
    };
    final Runnable saveEntityFunction = () -> {
        // Save current entity via service
    };
    setSettingsAccessors(getterFunction, saveEntityFunction);
}
```

### 3. Dialog Integration Pattern
```java
@Override
protected void openAddDialog() throws Exception {
    // Validation checks
    final Dialog dialog = new Dialog(this, services..., null, currentEntity, this::onSettingsSaved);
    dialog.open();
}
```

### 4. Save Callback Pattern
```java
@Override
protected void onSettingsSaved(final RelationshipType settings) {
    // Use service layer for persistence
    final RelationshipType savedSettings = relationshipService.save(settings);
    // Refresh form
    populateForm();
}
```

## Important Notes

### FetchType Configuration ⚠️
The `CUserProjectSettings` entity has a critical FetchType configuration to prevent PostgreSQL errors:

```java
@ManyToOne (fetch = FetchType.LAZY)  // ✅ MUST be LAZY
@JoinColumn (name = "role_id", nullable = true)
private CUserProjectRole role;
```

**Why LAZY is required:**
- `CUserProjectRole` has two `@ElementCollection` fields with `FetchType.EAGER` (readAccessPages, writeAccessPages)
- Without explicit `FetchType.LAZY`, the default EAGER fetch creates circular eager loading
- This generates massive JOIN queries that exceed PostgreSQL's 1664 column limit
- Error: `ERROR: target lists can have at most 1664 entries`

**All `@ManyToOne` relationships in `CUserProjectSettings` use `FetchType.LAZY`:**
- `user` → `CUser`
- `project` → `CProject`
- `role` → `CUserProjectRole`

See `CUserProjectSettingsFetchTypeTest` for validation tests.

## Testing Strategy
- Unit tests focusing on populate form pattern and lazy loading
- Integration tests for CRUD operations
- FetchType validation tests to prevent PostgreSQL column limit errors
- UI tests via Playwright for full component workflows

## Benefits of This Pattern
1. **Consistent UI/UX**: Same interaction patterns across all relationship management
2. **Reusable Components**: Base classes provide common functionality
3. **Proper Data Management**: Accessor pattern ensures consistent data loading/saving
4. **Service Layer Integration**: Proper use of service layer for business logic
5. **Bidirectional Support**: Same pattern works for both sides of relationships