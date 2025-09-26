# Enhanced User-Company Relationship and Access Control System

## Overview
This comprehensive solution extends the existing user-project relationship system to include company-user relationships with advanced role-based access control, ownership privileges, and generic relationship management components.

## Key Features

### 1. Generic Relationship Management Framework
- **`@ARelationshipMetadata`** - Annotation for defining relationship metadata
- **`CAbstractEntityRelationship`** - Base class for all relationship entities with ownership support
- **`CGenericRelationshipManager`** - Reusable component for managing entity relationships using reflection

### 2. Company-User Relationship System
- **`CUserCompanySettings`** - Entity managing user membership in companies with ownership levels
- **`CUserCompanySettingsService`** - Service for managing company-user relationships
- **`CUserCompanySettingsRepository`** - Data access layer for company-user relationships

### 3. Enhanced Role-Based Access Control
- **`EEnhancedUserRole`** - Hierarchical role system with 11 distinct roles
- **`CPageAccessControl`** - Entity for page-level access control configuration
- **`CPageAccessControlService`** - Service for managing page access permissions

### 4. Session Management Enhancement
- **Enhanced `CSessionService`** - Includes company context management
- **Automatic Primary Company Setting** - Sets user's primary company on login

## Architecture Details

### Relationship Hierarchy
```
CAbstractEntityRelationship<T>
├── CUserProjectSettings (existing, enhanced)
└── CUserCompanySettings (new)
```

### User Role Hierarchy (by privilege level)
1. **APPLICATION_ADMIN** (1000) - Complete system access
2. **COMPANY_OWNER** (900) - Full company ownership
3. **COMPANY_ADMIN** (800) - Company administration
4. **DEPARTMENT_MANAGER** (700) - Department management
5. **PROJECT_MANAGER** (600) - Project management
6. **TEAM_LEAD** (500) - Team leadership
7. **SENIOR_MEMBER** (400) - Advanced features + mentoring
8. **TEAM_MEMBER** (300) - Standard user access
9. **CONTRACTOR** (200) - Limited project access
10. **CLIENT** (150) - Read-only project access
11. **GUEST** (100) - Minimal public access

### Ownership Levels
- **OWNER** - Complete ownership with deletion rights
- **ADMIN** - Administrative privileges
- **MEMBER** - Standard member privileges
- **VIEWER** - Read-only access

### Access Types
- **OWNER** - Complete ownership including deletion
- **ADMIN** - Full administrative access
- **WRITE** - Read and write access
- **READ** - Read-only access
- **NONE** - No access

## Usage Examples

### 1. Adding User to Company with Ownership
```java
@Autowired
private CUserCompanySettingsService userCompanyService;

// Add user as company admin
CUserCompanySettings settings = userCompanyService.addUserToCompany(
    user, company, "ADMIN", "HR_MANAGER", "Human Resources", true
);

// Check privileges
if (settings.isCompanyAdmin()) {
    // User can manage other users
}
```

### 2. Page Access Control
```java
@Autowired
private CPageAccessControlService accessControlService;

// Check if user has write access to user management page
boolean canEdit = accessControlService.hasAccess(
    user, "UserManagement", EAccessType.WRITE
);

// Get all pages user can access
List<String> accessiblePages = accessControlService.getAccessiblePages(
    user.getEnhancedRole()
);
```

### 3. Session Management with Company Context
```java
@Autowired
private CSessionService sessionService;

// Login sets primary company automatically
sessionService.setActiveUser(user);
Company currentCompany = sessionService.getCurrentCompany();

// Check admin privileges
boolean isAdmin = sessionService.isCurrentUserCompanyAdmin();
```

### 4. Generic Relationship Management
```java
@Autowired
private CGenericRelationshipManager relationshipManager;

// Establish any entity relationship
relationshipManager.establishRelationship(parentEntity, childEntity, relationshipEntity);

// Check if relationship exists
boolean exists = relationshipManager.relationshipExists(parent, child, RelationshipClass.class);

// Remove relationship
relationshipManager.removeRelationship(parent, child, RelationshipClass.class);
```

## Database Schema

### New Tables
- **`cusercompanysettings`** - User-company relationships
- **`cpageaccesscontrol`** - Page-level access control rules

### Key Fields
- **Ownership Level** - OWNER, ADMIN, MEMBER, VIEWER
- **Privileges** - Comma-separated privilege list
- **Company Specific** - Whether access rule applies to company context
- **Primary Company** - User's default company
- **Role & Department** - User's role and department within company

## Security Features

### 1. Hierarchical Access Control
- Higher roles can manage lower roles
- Application admins have universal access
- Company-specific access isolation

### 2. Privilege-Based Permissions
- Fine-grained privilege system
- Additive privilege model
- Custom privilege definitions per relationship

### 3. Session Security
- Automatic company context setting
- Role-based session validation
- Secure privilege checking methods

## Initialization & Configuration

### Automatic Setup
- **`CEnhancedUserRoleInitializerService`** - Sets up default page access controls
- **Comprehensive Access Patterns** - Pre-configured for all roles and common pages
- **Flexible Configuration** - Easy to extend and customize

### Default Access Patterns
- **System Administration** - Application admin only
- **Company Management** - Company owners and admins
- **Project Management** - Project managers and above
- **User Tasks** - All authenticated users
- **Public Pages** - Guests and above

## Benefits

### 1. Reusability
- Generic relationship framework works for any entity pair
- Metadata-driven configuration
- Reflection-based automatic relationship management

### 2. Scalability
- Hierarchical role system supports complex organizations
- Fine-grained access control
- Company-specific access isolation

### 3. Security
- Comprehensive privilege system
- Automatic session management
- Role-based access enforcement

### 4. Maintainability
- Clear separation of concerns
- Extensive test coverage
- Well-documented API

## Testing
- **`CUserCompanyRelationshipTest`** - Company-user relationship tests
- **`CEnhancedUserRoleTest`** - Role system and access control tests
- **Comprehensive Coverage** - All major functionality tested
- **Integration Ready** - Tests work with existing infrastructure

## Migration Path
The system is designed to be backward compatible with existing user-project relationships while adding new company-user capabilities. Existing `EUserRole` enum is mapped to enhanced roles automatically.

## Conclusion
This solution provides a comprehensive, flexible, and secure foundation for managing complex organizational relationships and access control in the application. The generic nature of the relationship framework makes it easily extensible for future entity relationships while maintaining security and performance.