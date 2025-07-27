# User-Company Association & Role Management Requirements

## Task Description

Update the CUser class to support new requirements ensuring adherence to coding standards outlined in the provided documentation. The primary objectives are:

1. **User-Company Association**: Associate one user with one company, and one company with many users
2. **Enhanced Role Management**: Support structured user roles (admin, project manager, team member, guest)
3. **Flexible Relationships**: Company and users are not required fields for each other (nullable associations)

## Main Features

### 1. User Role Management System

#### 1.1 Role Enumeration (CUserRole)
- **Admin**: System administrator with full access to all features and system management
- **Project Manager**: Can manage projects, assign tasks, oversee team members, and access project-level reporting
- **Team Member**: Standard user with task management capabilities, time tracking, and progress updates
- **Guest**: Limited read-only access to designated project information

#### 1.2 Role Features
- Backward compatibility with existing string-based role system
- Automatic role parsing from legacy role strings
- Spring Security integration with proper authority mapping
- Human-readable display names and descriptions

### 2. User-Company Relationship System

#### 2.1 Association Model
- **One-to-Many**: One company can have multiple users
- **Many-to-One**: Each user can belong to one company (optional)
- **Nullable Relationships**: Both associations are optional to maintain flexibility

#### 2.2 Database Schema
- Added `company_id` foreign key to `cuser` table
- Added `user_role` enum column to `cuser` table
- Maintained existing `roles` string column for backward compatibility
- Updated sample data with company associations

### 3. Enhanced Domain Model

#### 3.1 CUser Entity Updates
- Added `CUserRole userRole` enum field with proper JPA annotations
- Added `CCompany company` relationship with lazy loading
- Added constructors supporting both role enum and legacy string roles
- Added getter/setter methods with automatic synchronization between role formats
- Maintained all existing functionality for backward compatibility

#### 3.2 CCompany Entity Updates
- Added `List<CUser> users` bidirectional relationship
- Added proper JPA annotations for one-to-many mapping
- Added getter/setter methods for users collection
- Maintained all existing company functionality

## User Roles

### Role Hierarchy and Permissions

| Role | Authority | Primary Responsibilities | Access Level |
|------|-----------|-------------------------|--------------|
| **Admin** | `ROLE_ADMIN` | System management, user administration, global settings | Full System |
| **Project Manager** | `ROLE_PROJECT_MANAGER` | Project oversight, team management, resource allocation | Project Level |
| **Team Member** | `ROLE_TEAM_MEMBER` | Task execution, progress tracking, time logging | Task Level |
| **Guest** | `ROLE_GUEST` | Read-only access to assigned project information | Read Only |

### Role Assignment Rules
- Default role for new users: `TEAM_MEMBER`
- Role changes require appropriate administrative permissions
- Legacy role strings are automatically converted to enum values
- Multiple role support maintained through string field for complex scenarios

## Technical Specifications

### 1. Technologies and Frameworks

#### Core Technologies
- **Java 17+**: Primary development language
- **Spring Boot 3+**: Application framework
- **Spring Data JPA**: Data persistence layer
- **Hibernate**: ORM implementation
- **PostgreSQL**: Primary database
- **Vaadin Flow 24+**: UI framework

#### Design Patterns
- **MVC Architecture**: Clear separation of concerns
- **Domain-Driven Design**: Rich domain models with business logic
- **Repository Pattern**: Data access abstraction
- **Builder Pattern**: Entity form generation using MetaData annotations

### 2. Database Integration

#### Schema Changes
```sql
-- Added columns to cuser table
ALTER TABLE cuser ADD COLUMN user_role VARCHAR(50) NOT NULL DEFAULT 'TEAM_MEMBER';
ALTER TABLE cuser ADD COLUMN company_id BIGINT NULL;
ALTER TABLE cuser ADD CONSTRAINT fk_cuser_company FOREIGN KEY (company_id) REFERENCES ccompany(company_id);
```

#### Data Migration
- Existing users automatically assigned `TEAM_MEMBER` role
- Sample data includes company associations
- Backward compatibility maintained for existing role strings

### 3. Code Quality Standards

#### Adherence to Coding Guidelines
- **Class Naming**: All domain classes prefixed with "C" (CUser, CCompany, CUserRole)
- **Final Keywords**: Used extensively for immutability where possible
- **Null Safety**: Comprehensive null checking and default value handling
- **Logging**: Logger statements at function entry with parameter details
- **Documentation**: Complete JavaDoc for all public methods and classes
- **MetaData Annotations**: Consistent use for form generation and UI binding

#### Testing Strategy
- **Unit Tests**: Comprehensive coverage for domain logic and role management
- **Integration Tests**: Database relationship validation
- **Backward Compatibility Tests**: Ensure existing functionality remains intact
- **Role Permission Tests**: Validate security and access control

## Integration Requirements

### 1. Spring Security Integration
- Automatic authority mapping from CUserRole enum
- Backward compatibility with existing authentication mechanisms
- Role-based access control for methods and endpoints

### 2. Vaadin UI Integration
- MetaData annotations for automatic form generation
- ComboBox data providers for role and company selection
- Validation rules for role assignments and company associations

### 3. Database Migration Strategy
- Flyway or Liquibase scripts for schema updates
- Data migration scripts for existing user role assignments
- Rollback capabilities for safe deployment

## Implementation Validation

### 1. Functionality Testing
- [x] CUser entity creation with company association
- [x] CUserRole enum parsing and conversion from legacy strings
- [x] Bidirectional relationship management (User ↔ Company)
- [x] Backward compatibility with existing user creation workflows
- [x] Database schema validation with sample data

### 2. Code Quality Testing
- [x] Maven compilation without errors
- [x] Unit test coverage for new functionality
- [x] Integration with existing service layer
- [x] Adherence to project coding standards
- [x] Proper JPA annotation usage and lazy loading configuration

### 3. Security Testing
- [ ] Role-based access control validation
- [ ] Spring Security authority mapping verification
- [ ] User permission inheritance from company associations

## Future Enhancements

### 1. Advanced Role Management
- Role hierarchy with inheritance
- Dynamic role assignment based on project context
- Role-based UI component visibility

### 2. Company Management Features
- Company-level settings and configurations
- Multi-tenant architecture support
- Company-specific project templates and workflows

### 3. Audit and Compliance
- User role change audit trails
- Company association history tracking
- Compliance reporting for role assignments

## Success Criteria

1. **Functional Requirements Met**: All specified user-company associations and role management features implemented
2. **Backward Compatibility**: Existing functionality preserved without breaking changes
3. **Code Quality**: Full adherence to project coding standards and guidelines
4. **Test Coverage**: Comprehensive test suite with passing unit and integration tests
5. **Database Integrity**: Proper schema updates with sample data validation
6. **Documentation**: Complete requirements document and code documentation

## Deployment Considerations

### Database Updates
- Schema migration scripts required for production deployment
- Sample data updates for development and testing environments
- Backup and rollback procedures for safe deployment

### Configuration Changes
- No application configuration changes required
- Existing Spring Security configuration remains compatible
- Vaadin UI components automatically recognize new MetaData annotations

---

**Document Version**: 1.0  
**Last Updated**: January 2025  
**Status**: Implementation Complete, Ready for Review