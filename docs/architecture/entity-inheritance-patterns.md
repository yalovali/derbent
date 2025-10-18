# Entity Inheritance Patterns

## Overview

Derbent uses a sophisticated entity inheritance hierarchy that provides consistent behavior, reduces boilerplate code, and ensures compile-time type safety across all domain entities. This document describes the complete entity inheritance hierarchy and best practices for extending it.

## The Entity Inheritance Hierarchy

```
CEntity<T>
    ↓
CEntityDB<T>
    ↓
CEntityNamed<T>
    ↓
CEntityOfProject<T>
    ↓
CProjectItem<T>
    ↓
[Domain Classes: CActivity, CRisk, CMeeting, etc.]
```

## Base Class: CEntity\<T>

**Location**: `src/main/java/tech/derbent/api/domains/CEntity.java`

**Purpose**: Root of all domain entities, provides generic type safety and logging.

**Key Features**:
- Generic type parameter `T` for compile-time type safety
- Logger instance for all subclasses
- Class reference storage via `clazz` field

**Usage Pattern**:
```java
public abstract class CEntity<EntityClass> {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Transient
    protected final Class<EntityClass> clazz;
    
    protected CEntity() {
        this.clazz = (Class<EntityClass>) getClass();
    }
    
    public CEntity(final Class<EntityClass> clazz) {
        this.clazz = clazz;
    }
}
```

**When to extend**: Never directly. Always extend one of the specialized subclasses.

## Level 1: CEntityDB\<T>

**Location**: `src/main/java/tech/derbent/api/domains/CEntityDB.java`

**Purpose**: Adds database persistence capabilities with identity and active status.

**Key Features**:
- `@Id` and `@GeneratedValue` for primary key
- `Long id` - Auto-generated database identifier
- `Boolean isActive` - Soft delete support
- `equals()` and `hashCode()` based on ID
- Metadata annotations via `@AMetaData`

**Fields Added**:
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@AMetaData(displayName = "#", required = false, readOnly = true, 
           description = "No", hidden = false, order = 0)
private Long id;

@Column(name = "is_active", nullable = false)
@AMetaData(displayName = "Active", required = false, readOnly = false,
           description = "Whether this entity definition is active", 
           hidden = false, order = 20, defaultValue = "true")
private Boolean isActive = true;
```

**When to extend**: For simple lookup/reference entities that don't need names or project association (e.g., system configuration entities).

**Example**:
```java
@Entity
@Table(name = "system_config")
public class CSystemConfig extends CEntityDB<CSystemConfig> {
    @Column(nullable = false)
    private String configKey;
    
    @Column(nullable = false)
    private String configValue;
    
    // Constructor, getters, setters
}
```

## Level 2: CEntityNamed\<T>

**Location**: `src/main/java/tech/derbent/api/domains/CEntityNamed.java`

**Purpose**: Adds name, description, and audit trail capabilities.

**Key Features**:
- `String name` - Required, max 255 characters
- `String description` - Optional, max 2000 characters  
- `LocalDateTime createdDate` - Audit trail
- `LocalDateTime lastModifiedDate` - Audit trail
- Automatic name validation via `@Size` and `@AMetaData`

**Fields Added**:
```java
@Column(nullable = false, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
@Size(max = CEntityConstants.MAX_LENGTH_NAME)
@AMetaData(displayName = "Name", required = true, readOnly = false,
           defaultValue = "", description = "Name", hidden = false, order = 0,
           maxLength = CEntityConstants.MAX_LENGTH_NAME, setBackgroundFromColor = true)
private String name;

@Column(nullable = true, length = 2000)
@Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
@AMetaData(displayName = "Description", required = false, readOnly = false,
           defaultValue = "", description = "Detailed description of the project",
           hidden = false, order = 1, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION)
private String description;

@Column(name = "created_date", nullable = true)
@AMetaData(displayName = "Created Date", required = false, readOnly = true,
           description = "Date and time when the activity was created",
           hidden = false, order = 80)
private LocalDateTime createdDate;

@Column(name = "last_modified_date", nullable = true)
@AMetaData(displayName = "Last Modified", required = false, readOnly = true,
           description = "Date and time when the activity was last modified",
           hidden = false, order = 81)
private LocalDateTime lastModifiedDate;
```

**When to extend**: For entities that need names but don't belong to projects (e.g., companies, global type definitions).

**Example**:
```java
@Entity
@Table(name = "companies")
public class CCompany extends CEntityNamed<CCompany> {
    @Column(nullable = false)
    private String taxId;
    
    @Column(nullable = true)
    private String address;
    
    // Constructor, getters, setters
}
```

## Level 3: CEntityOfProject\<T>

**Location**: `src/main/java/tech/derbent/api/domains/CEntityOfProject.java`

**Purpose**: Adds project context and user assignment capabilities for multi-tenant support.

**Key Features**:
- `CProject project` - Required, cascade delete
- `CUser assignedTo` - Optional user assignment
- `CUser createdBy` - Audit trail for creator
- Automatic project filtering in queries
- Context-aware security and permissions

**Fields Added**:
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "project_id", nullable = false)
@OnDelete(action = OnDeleteAction.CASCADE)
@AMetaData(displayName = "Project", required = true, readOnly = true,
           description = "Project of this entity", hidden = false, order = 10)
private CProject project;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "assigned_to_id", nullable = true)
@AMetaData(displayName = "Assigned To", required = false, readOnly = false,
           description = "User assigned to this activity", hidden = false,
           order = 10, dataProviderBean = "CUserService")
private CUser assignedTo;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "created_by_id", nullable = true)
@AMetaData(displayName = "Created By", required = false, readOnly = true,
           description = "User who created this activity", hidden = false, 
           order = 11, dataProviderBean = "CUserService")
private CUser createdBy;
```

**When to extend**: For project-scoped entities that don't need hierarchical relationships (e.g., project settings, project-specific types).

**Example**:
```java
@Entity
@Table(name = "activity_types")
public class CActivityType extends CEntityOfProject<CActivityType> {
    @Column(nullable = true)
    private String color;
    
    @Column(nullable = true)
    private String icon;
    
    @Column(nullable = false)
    private Boolean isDeletable = true;
    
    // Constructor, getters, setters
}
```

## Level 4: CProjectItem\<T>

**Location**: `src/main/java/tech/derbent/api/domains/CProjectItem.java`

**Purpose**: Adds hierarchical parent-child relationship capabilities.

**Key Features**:
- `Long parentId` - ID of parent entity
- `String parentType` - Type of parent entity (class simple name)
- Self-parent protection
- Automatic parent validation
- Support for polymorphic hierarchies

**Fields Added**:
```java
@Column(name = "parent_id", nullable = true)
@AMetaData(displayName = "Parent #", required = false, readOnly = true,
           description = "ID of the parent entity", hidden = true, order = 62)
private Long parentId;

@Column(name = "parent_type", nullable = true)
@AMetaData(displayName = "Parent Type", required = false, readOnly = true,
           description = "Type of the parent entity", hidden = true, order = 61)
private String parentType;
```

**Key Methods**:
```java
public void setParent(final CProjectItem<?> parent) {
    // Validates and sets parent relationship
    // Includes self-parent protection
}

public void clearParent() {
    // Removes parent relationship
    this.parentType = null;
    this.parentId = null;
    updateLastModified();
}
```

**When to extend**: For main business entities that need hierarchical relationships (e.g., activities, risks, meetings).

**Example**:
```java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> {
    @Column(nullable = true, length = 2000)
    @Size(max = 2000)
    @AMetaData(displayName = "Acceptance Criteria", required = false,
               readOnly = false, defaultValue = "",
               description = "Criteria that must be met for completion",
               hidden = false, order = 70, maxLength = 2000)
    private String acceptanceCriteria;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = true)
    @AMetaData(displayName = "Status", required = false, readOnly = false,
               description = "Current status of the activity",
               hidden = false, order = 20, dataProviderBean = "CActivityStatusService")
    private CActivityStatus status;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = true)
    @AMetaData(displayName = "Type", required = false, readOnly = false,
               description = "Type/category of the activity",
               hidden = false, order = 21, dataProviderBean = "CActivityTypeService")
    private CActivityType type;
    
    // Additional fields, constructors, getters, setters
}
```

## Best Practices

### 1. Choose the Right Base Class

| Use Case | Base Class | Example |
|----------|-----------|---------|
| System configuration, simple lookup | `CEntityDB<T>` | System settings |
| Named entities without project scope | `CEntityNamed<T>` | Companies, global roles |
| Project-scoped types/statuses | `CEntityOfProject<T>` | Activity types, statuses |
| Main business entities | `CProjectItem<T>` | Activities, risks, meetings |

### 2. Always Use Generic Type Parameter

```java
// ✅ CORRECT
public class CActivity extends CProjectItem<CActivity> {
    // ...
}

// ❌ INCORRECT - Raw type loses type safety
public class CActivity extends CProjectItem {
    // ...
}
```

### 3. Provide Both Constructors

```java
@Entity
@Table(name = "my_entity")
public class CMyEntity extends CProjectItem<CMyEntity> {
    
    /** Default constructor for JPA. */
    protected CMyEntity() {
        super();
    }
    
    public CMyEntity(final String name, final CProject project) {
        super(CMyEntity.class, name, project);
    }
}
```

### 4. Use @AttributeOverride for Custom ID Column Names

```java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> {
    // ...
}
```

### 5. Leverage @AMetaData for Automatic UI Generation

```java
@Column(nullable = false)
@Size(max = 100)
@AMetaData(
    displayName = "Title",           // Label in UI
    required = true,                 // Validation
    readOnly = false,                // Editable
    description = "Brief title",     // Tooltip
    hidden = false,                  // Visible in forms
    order = 10,                      // Field order
    maxLength = 100,                 // Max input length
    dataProviderBean = "MyService"   // For ComboBox data
)
private String title;
```

### 6. Follow C-Prefix Naming Convention

All custom entity classes must start with "C":
- ✅ `CActivity`, `CUser`, `CProject`
- ❌ `Activity`, `User`, `Project`

### 7. Use Appropriate Fetch Strategies

```java
// For required relationships that are frequently accessed
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "project_id", nullable = false)
private CProject project;

// For optional relationships or collections
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_activity_id", nullable = true)
private CActivity parentActivity;
```

### 8. Implement Business Logic in Domain Classes

```java
public class CActivity extends CProjectItem<CActivity> {
    
    public boolean isOverdue() {
        if (plannedEndDate == null) return false;
        return LocalDate.now().isAfter(plannedEndDate) 
               && !isCompleted();
    }
    
    public boolean isCompleted() {
        return status != null 
               && status.getName().equalsIgnoreCase("Completed");
    }
}
```

## Common Patterns

### Pattern 1: Type Entities

Type entities define categories for main entities:

```java
@Entity
@Table(name = "activity_types")
public class CActivityType extends CEntityOfProject<CActivityType> {
    @Column(nullable = true, length = 20)
    private String color;
    
    @Column(nullable = true, length = 50)
    private String icon;
    
    @Column(nullable = false)
    private Boolean isDeletable = true;
    
    // Constructors, getters, setters
}
```

### Pattern 2: Status Entities

Status entities track workflow states:

```java
@Entity
@Table(name = "activity_statuses")
public class CActivityStatus extends CEntityOfProject<CActivityStatus> {
    @Column(nullable = true, length = 20)
    private String color;
    
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private Integer orderIndex = 0;
    
    @Column(nullable = false)
    private Boolean isClosedStatus = false;
    
    // Constructors, getters, setters
}
```

### Pattern 3: Relationship Entities

For many-to-many relationships:

```java
@Entity
@Table(name = "user_project_roles")
public class CUserProjectRole extends CEntityDB<CUserProjectRole> {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private CProject project;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    // Constructors, getters, setters
}
```

## Testing Entity Classes

Always test entity behavior:

```java
@Test
void testActivityCreation() {
    CProject project = new CProject("Test Project", company);
    CActivity activity = new CActivity("Test Activity", project);
    
    assertNotNull(activity.getName());
    assertEquals(project, activity.getProject());
    assertNull(activity.getId()); // Not persisted yet
    assertTrue(activity.getIsActive());
}

@Test
void testParentChildRelationship() {
    CActivity parent = new CActivity("Parent", project);
    parent.setId(1L);
    
    CActivity child = new CActivity("Child", project);
    child.setParent(parent);
    
    assertEquals(1L, child.getParentId());
    assertEquals("CActivity", child.getParentType());
}
```

## Migration Guide

### Adding a New Entity

1. **Choose appropriate base class** based on requirements
2. **Create entity class** with C-prefix
3. **Add domain-specific fields** with proper annotations
4. **Provide constructors** (default for JPA + business constructor)
5. **Add getters/setters** (use IDE generation)
6. **Create corresponding repository interface**
7. **Create service class** extending appropriate base service
8. **Create view/page class** for UI
9. **Add database migration** (if using Flyway/Liquibase)
10. **Write unit tests** for entity behavior

### Example: Adding CDecision Entity

```java
// 1. Domain entity
@Entity
@Table(name = "cdecision")
@AttributeOverride(name = "id", column = @Column(name = "decision_id"))
public class CDecision extends CProjectItem<CDecision> {
    
    @Column(nullable = false)
    @AMetaData(displayName = "Decision Date", required = true,
               description = "Date when decision was made", order = 20)
    private LocalDate decisionDate;
    
    @Column(nullable = true, length = 1000)
    @AMetaData(displayName = "Rationale", required = false,
               description = "Reasoning behind the decision", 
               order = 30, maxLength = 1000)
    private String rationale;
    
    protected CDecision() {
        super();
    }
    
    public CDecision(final String name, final CProject project) {
        super(CDecision.class, name, project);
    }
    
    // Getters and setters
}

// 2. Repository interface
public interface IDecisionRepository 
    extends IEntityOfProjectRepository<CDecision> {
    
    List<CDecision> findByDecisionDateBetween(
        LocalDate start, LocalDate end);
}

// 3. Service class
@Service
@PreAuthorize("isAuthenticated()")
public class CDecisionService 
    extends CEntityOfProjectService<CDecision> {
    
    public CDecisionService(final IDecisionRepository repository,
                           final Clock clock,
                           final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    protected Class<CDecision> getEntityClass() {
        return CDecision.class;
    }
}
```

## Related Documentation

- [Service Layer Patterns](service-layer-patterns.md) - Service implementation patterns
- [View Layer Patterns](view-layer-patterns.md) - UI component patterns  
- [Coding Standards](coding-standards.md) - Naming conventions and best practices
- [Database Query Debugging](../DATABASE_QUERY_DEBUGGING.md) - SQL debugging tools
