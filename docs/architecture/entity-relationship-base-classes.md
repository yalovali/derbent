# Enhanced Base Classes for Entity Relationships

## Overview

This document describes the new base classes created for managing entity-to-entity relationships in the Derbent project. These classes provide a standardized approach to handling CRUD operations for relationship entities.

## New Base Classes

### 1. CAbstractEntityRelationPanel

**Purpose**: Base class for UI panels that manage entity-to-entity relationships.

**Location**: `tech.derbent.abstracts.views.CAbstractEntityRelationPanel`

**Key Features**:
- Generic support for any relationship entity type
- Standardized CRUD operations (Add, Edit, Delete)
- Consistent button layout and grid management
- Abstract methods for customization

**Example Usage**:
```java
public class CPanelProjectUsers extends CAbstractEntityRelationPanel<CProject, CUserProjectSettings> {
    
    @Override
    protected Class<CUserProjectSettings> getRelationEntityClass() {
        return CUserProjectSettings.class;
    }
    
    @Override
    protected void setupGrid() {
        grid.addIdColumn(CUserProjectSettings::getId, "ID");
        grid.addEntityNameColumn(settings -> settings.getUser().getName(), "User");
        grid.addRoleColumn(CUserProjectSettings::getRole, "Role");
        grid.addPermissionColumn(CUserProjectSettings::getPermission, "Permission");
    }
    
    // Implement other abstract methods...
}
```

### 2. CAbstractEntityRelationGrid

**Purpose**: Specialized grid for displaying entity relationships with consistent styling.

**Location**: `tech.derbent.abstracts.views.CAbstractEntityRelationGrid`

**Key Features**:
- Predefined column types for common relation fields
- Consistent width and styling constants
- Automatic grid initialization with proper themes

**Column Types Available**:
- `addIdColumn()` - Standard ID columns (80px)
- `addEntityNameColumn()` - Entity names (200px)
- `addRoleColumn()` - Role fields (150px)
- `addPermissionColumn()` - Permission fields (150px)
- `addStatusColumn()` - Status fields (120px)

### 3. CAbstractEntityRelationService

**Purpose**: Service layer base class for relationship entities.

**Location**: `tech.derbent.abstracts.services.CAbstractEntityRelationService`

**Key Features**:
- CRUD operations for relationship entities
- Relationship existence checking
- Parent/child entity relationship management
- Transaction management

**Core Methods**:
```java
// Find relationships by parent entity
List<RelationEntity> findByParentEntityId(Long parentEntityId);

// Find relationships by child entity
List<RelationEntity> findByChildEntityId(Long childEntityId);

// Check if relationship exists
boolean relationshipExists(Long parentEntityId, Long childEntityId);

// Create new relationship
RelationEntity createRelationship(Long parentEntityId, Long childEntityId);

// Update existing relationship
RelationEntity updateRelationship(RelationEntity relationship);
```

## Implementation Example: CUserProjectSettings

### Service Implementation

```java
@Service
public class CUserProjectSettingsService extends CAbstractEntityRelationService<CUserProjectSettings> {
    
    @Override
    protected Class<CUserProjectSettings> getEntityClass() {
        return CUserProjectSettings.class;
    }
    
    @Override
    public List<CUserProjectSettings> findByParentEntityId(Long userId) {
        return repository.findByUserId(userId);
    }
    
    @Override
    public List<CUserProjectSettings> findByChildEntityId(Long projectId) {
        return repository.findByProjectId(projectId);
    }
    
    // Business logic methods
    public CUserProjectSettings addUserToProject(CUser user, CProject project, 
            String role, String permission) {
        // Implementation with validation
    }
}
```

### Repository Implementation

```java
@Repository
public interface CUserProjectSettingsRepository extends CAbstractRepository<CUserProjectSettings> {
    
    @Query("SELECT ups FROM CUserProjectSettings ups LEFT JOIN FETCH ups.project LEFT JOIN FETCH ups.user WHERE ups.user.id = :userId")
    List<CUserProjectSettings> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ups FROM CUserProjectSettings ups LEFT JOIN FETCH ups.project LEFT JOIN FETCH ups.user WHERE ups.project.id = :projectId")
    List<CUserProjectSettings> findByProjectId(@Param("projectId") Long projectId);
    
    boolean existsByUserIdAndProjectId(Long userId, Long projectId);
}
```

## Validation Enhancements

### Dialog Validation

Enhanced validation in relationship dialogs:

1. **Required Field Validation**: Role and permission fields are now required
2. **Null/Empty Checking**: Proper validation for all input fields
3. **Trim Whitespace**: Input values are trimmed before saving

**Example**:
```java
@Override
protected void validateForm() {
    if (projectComboBox.getValue() == null) {
        throw new IllegalArgumentException("Please select a project");
    }
    
    final String role = rolesField.getValue();
    if (role == null || role.trim().isEmpty()) {
        throw new IllegalArgumentException("Role is required and cannot be empty");
    }
    
    final String permission = permissionsField.getValue();
    if (permission == null || permission.trim().isEmpty()) {
        throw new IllegalArgumentException("Permission is required and cannot be empty");
    }
    
    // Set validated values
    data.setRole(role.trim());
    data.setPermission(permission.trim());
}
```

## Benefits

1. **Consistency**: All relationship panels follow the same patterns
2. **Reduced Code Duplication**: Common functionality is abstracted
3. **Maintainability**: Changes to base classes affect all implementations
4. **Validation**: Standardized validation across all relationship forms
5. **Extensibility**: Easy to add new relationship types

## Migration Guide

To migrate existing relationship panels:

1. Extend `CAbstractEntityRelationPanel` instead of custom base classes
2. Implement the required abstract methods
3. Use the new repository patterns with JOIN FETCH queries
4. Update services to extend `CAbstractEntityRelationService`
5. Add proper validation to dialog classes

## Best Practices

1. Always use JOIN FETCH in repository queries to avoid lazy loading issues
2. Implement proper validation in both dialog and service layers
3. Use the standardized column methods for consistent grid appearance
4. Follow the established naming conventions for methods and fields
5. Add comprehensive JavaDoc documentation for all public methods