# Entity Class Inheritance Patterns

This document outlines the complete inheritance hierarchy and patterns for entity classes in the Derbent project. Understanding these patterns is crucial for proper entity design and GitHub Copilot effectiveness.

## 🏗️ Entity Inheritance Hierarchy

### Core Inheritance Chain
```
CEntity<T>
    ↓
CEntityDB<T>
    ↓
CEntityNamed<T>
    ↓
CEntityOfProject<T> / CProjectItem<T>
    ↓
[Your Entity Classes]
```

### Specialized Hierarchies
```
CEntityDB<T>
    ↓
CTypeEntity<T>          # For type/category entities
    ↓
CActivityType, CUserType, etc.

CEntityDB<T>
    ↓
CStatus<T>              # For status entities with colors
    ↓
CActivityStatus, CRiskStatus, etc.
```

## 📋 Base Class Responsibilities

### CEntity<T>
**Purpose**: Root entity class with basic functionality
**Location**: `tech.derbent.api.domains.CEntity`

```java
@MappedSuperclass
public abstract class CEntity<EntityClass> {
    // Basic entity operations
    // Generic type safety
    // Common utility methods
}
```

**Key Features**:
- Generic type parameter for type safety
- Basic entity operations
- Equality and hash code handling

### CEntityDB<T>
**Purpose**: Database-backed entities with ID and persistence
**Location**: `tech.derbent.api.domains.CEntityDB`

```java
@MappedSuperclass
public abstract class CEntityDB<EntityClass> extends CEntity<EntityClass> 
    implements IDisplayEntity, IEntityDBStatics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    // Required static methods
    public static String getStaticIconFilename() { /* Override in subclasses */ }
    public static String getStaticIconColorCode() { /* Override in subclasses */ }
    public static String getStaticEntityColorCode() { /* Override in subclasses */ }
}
```

**Key Features**:
- Database ID management
- Audit trail support
- Static icon and color methods
- Reflection-based save operations

**Required Implementations**:
```java
// Every entity must override these static methods
public static String getStaticIconFilename() { return "vaadin:file"; }
public static String getStaticIconColorCode() { return "#000000"; }
public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }
public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return YourView.class; }
```

### CEntityNamed<T>
**Purpose**: Entities with name and description
**Location**: `tech.derbent.api.domains.CEntityNamed`

```java
@MappedSuperclass
public abstract class CEntityNamed<EntityClass> extends CEntityDB<EntityClass> {
    
    @Column(nullable = false, length = 100)
    @Size(max = 100)
    @AMetaData(displayName = "Name", required = true, order = 1)
    private String name;
    
    @Column(length = 500)
    @Size(max = 500)
    @AMetaData(displayName = "Description", required = false, order = 100)
    private String description;
}
```

**Key Features**:
- Standard name field (required, max 100 chars)
- Optional description field (max 500 chars)
- Automatic metadata for form generation

### CEntityOfProject<T> / CProjectItem<T>
**Purpose**: Project-scoped entities for multi-tenant support
**Location**: `tech.derbent.api.domains.CEntityOfProject` / `CProjectItem`

```java
@MappedSuperclass
public abstract class CEntityOfProject<EntityClass> extends CEntityNamed<EntityClass> {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    @AMetaData(displayName = "Project", required = true, order = 0)
    private CProject project;
    
    // Project-aware constructors and methods
}
```

**Key Features**:
- Automatic project association
- Multi-tenant support
- Project-aware queries and operations
- Cascading project context

## 🎯 Specialized Entity Types

### Type Entities (CTypeEntity<T>)
**Purpose**: Configurable type/category entities
**Usage**: Activity types, user types, meeting types

```java
@Entity
@Table(name = "cactivitytype")
@AttributeOverride(name = "id", column = @Column(name = "activitytype_id"))
public class CActivityType extends CTypeEntity<CActivityType> {
    
    public static String getStaticIconFilename() { return "vaadin:tag"; }
    public static String getStaticIconColorCode() { return "#28a745"; }
    public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }
    
    // Default constructor for JPA
    public CActivityType() {
        super();
    }
    
    // Named constructor
    public CActivityType(String name, CProject project) {
        super(CActivityType.class, name, project);
    }
}
```

### Status Entities (CStatus<T>)
**Purpose**: Status entities with color and workflow support
**Usage**: Activity status, risk status, approval status

```java
@StatusEntity(colorField = "color", nameField = "name")
@Entity
@Table(name = "cactivitystatus")
@AttributeOverride(name = "id", column = @Column(name = "activitystatus_id"))
public class CActivityStatus extends CStatus<CActivityStatus> {
    
    public static String getStaticIconFilename() { return "vaadin:flag"; }
    public static String getStaticIconColorCode() { return "#dc3545"; }
    public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }
    
    @Column(length = 7)
    @AMetaData(displayName = "Color", required = true, order = 3)
    private String color = "#95a5a6";
    
    // Constructors
    public CActivityStatus() {
        super();
    }
    
    public CActivityStatus(String name, CProject project) {
        super(CActivityStatus.class, name, project);
        this.color = "#95a5a6"; // Default gray
    }
    
    // Color methods for UI components
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
```

## 🛠️ Entity Creation Patterns

### Standard Business Entity
```java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> implements CKanbanEntity {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivity.class);
    public static final String VIEW_NAME = "Activities View";
    
    // Static methods for UI integration
    public static String getStaticIconFilename() { return "vaadin:tasks"; }
    public static String getStaticIconColorCode() { return "#007bff"; }
    public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }
    public static Class<?> getViewClassStatic() { return CActivitiesView.class; }
    
    // Entity fields with metadata
    @AMetaData(displayName = "Activity Type", required = false, order = 2, 
              dataProviderBean = "CActivityTypeService")
    @ManyToOne(fetch = FetchType.EAGER)
    private CActivityType activityType;
    
    @AMetaData(displayName = "Status", required = false, order = 3,
              dataProviderBean = "CActivityStatusService")
    @ManyToOne(fetch = FetchType.EAGER)
    private CActivityStatus status;
    
    @AMetaData(displayName = "Priority", required = false, order = 4)
    @Column(nullable = true)
    @Min(1) @Max(5)
    private Integer priority = 3;
    
    @AMetaData(displayName = "Due Date", required = false, order = 5)
    @Column(nullable = true)
    private LocalDate dueDate;
    
    @AMetaData(displayName = "Estimated Hours", required = false, order = 6)
    @Column(nullable = true, precision = 10, scale = 2)
    @DecimalMin("0.0") @DecimalMax("9999.99")
    private BigDecimal estimatedHours;
    
    // Constructors
    public CActivity() {
        super();
        initializeDefaults();
    }
    
    public CActivity(String name, CProject project) {
        super(CActivity.class, name, project);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        this.priority = 3;
        this.estimatedHours = BigDecimal.ZERO;
    }
    
    // Getters and setters with proper validation
    public CActivityType getActivityType() { return activityType; }
    public void setActivityType(CActivityType activityType) { this.activityType = activityType; }
    
    public CActivityStatus getStatus() { return status; }
    public void setStatus(CActivityStatus status) { this.status = status; }
    
    // ... other getters/setters
}
```

## 📋 Required Annotations

### Entity-Level Annotations
```java
@Entity                          // JPA entity marker
@Table(name = "cactivity")      // Database table name (lowercase with 'c' prefix)
@AttributeOverride(             // Override inherited ID column name
    name = "id", 
    column = @Column(name = "activity_id")
)
```

### Field-Level Annotations
```java
// Standard field with metadata
@Column(nullable = false, length = 100)
@Size(max = 100)
@AMetaData(
    displayName = "Name",       // UI label
    required = true,           // Form validation
    order = 1,                // Form field order
    maxLength = 100           // Additional validation
)
private String name;

// Relationship with data provider
@AMetaData(
    displayName = "Activity Type",
    required = false,
    order = 2,
    dataProviderBean = "CActivityTypeService"  // ComboBox data source
)
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "activitytype_id")
private CActivityType activityType;

// Numeric field with validation
@AMetaData(displayName = "Priority", required = false, order = 4)
@Column(nullable = true)
@Min(1) @Max(5)
private Integer priority;

// Decimal field with precision
@AMetaData(displayName = "Estimated Hours", required = false, order = 6)
@Column(nullable = true, precision = 10, scale = 2)
@DecimalMin("0.0") @DecimalMax("9999.99")
private BigDecimal estimatedHours;

// Date field
@AMetaData(displayName = "Due Date", required = false, order = 5)
@Column(nullable = true)
private LocalDate dueDate;
```

## 🎨 Icon and Color Standards

### Icon Naming Convention
Use Vaadin icon names with descriptive prefixes:
```java
// Entity type icons
"vaadin:tasks"      // Activities
"vaadin:users"      // Users  
"vaadin:folder"     // Projects
"vaadin:calendar"   // Meetings
"vaadin:warning"    // Risks

// Type entity icons
"vaadin:tag"        // Types
"vaadin:flag"       // Statuses
"vaadin:star"       // Priorities
```

### Color Standards
Use Bootstrap-compatible color schemes:
```java
// Primary colors
"#007bff"   // Blue - Activities
"#28a745"   // Green - Success/Completed
"#dc3545"   // Red - Errors/High Priority
"#ffc107"   // Yellow - Warnings/Medium
"#6c757d"   // Gray - Inactive/Low

// Semantic colors
"#17a2b8"   // Cyan - Information
"#6f42c1"   // Purple - Special
"#e83e8c"   // Pink - Urgent
"#fd7e14"   // Orange - Pending
```

## 🔧 CAuxillaries Integration

### Entity Class Registration
All entities must be registered in CAuxillaries for dynamic resolution:
```java
public static Class<?> getEntityClass(String simpleName) {
    switch (simpleName) {
        case "CActivity":
            return CActivity.class;
        case "CActivityType":
            return CActivityType.class;
        case "CActivityStatus":
            return CActivityStatus.class;
        // Add your new entities here
        default:
            throw new IllegalArgumentException("Unknown entity type: " + simpleName);
    }
}
```

### Static Method Invocation
Entities support reflection-based method calls:
```java
// Get icon filename dynamically
String icon = CAuxillaries.invokeStaticMethodOfStr(CActivity.class, "getStaticIconFilename");

// Get color code dynamically  
String color = CAuxillaries.invokeStaticMethodOfStr(CActivity.class, "getStaticIconColorCode");

// Get associated view class
Class<?> viewClass = CAuxillaries.getViewClassForEntity("CActivity");
```

## ✅ Entity Checklist

When creating a new entity, ensure:

### Required Elements
- [ ] Extends appropriate base class (CProjectItem, CTypeEntity, CStatus)
- [ ] Uses C-prefix naming convention
- [ ] Includes @Entity, @Table, @AttributeOverride annotations
- [ ] Implements all required static methods
- [ ] Uses wrapper types (Integer, Boolean, BigDecimal)
- [ ] Includes proper @AMetaData annotations
- [ ] Has default and named constructors
- [ ] Implements initializeDefaults() method

### Optional Elements
- [ ] Implements relevant interfaces (CKanbanEntity, etc.)
- [ ] Includes relationship fields with proper fetch strategies
- [ ] Has validation annotations (@Size, @Min, @Max, etc.)
- [ ] Includes audit fields if needed
- [ ] Has custom business methods

### Integration Requirements
- [ ] Added to CAuxillaries.getEntityClass() switch
- [ ] Created corresponding service class
- [ ] Created corresponding view class
- [ ] Added proper repository interface
- [ ] Updated menu configuration if applicable

This entity inheritance system provides a solid foundation for consistent, maintainable entity design while supporting advanced features like multi-tenancy, metadata-driven forms, and dynamic UI generation.