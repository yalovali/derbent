# GitHub Copilot Coding Guidelines for Derbent

This document provides comprehensive guidelines for using GitHub Copilot effectively with the Derbent project. For detailed documentation standards and utility patterns, see [.copilot/guidelines.md](.copilot/guidelines.md).

## 🎯 Core Principles

### C-Prefix Convention
All custom classes must start with 'C':
```java
// ✅ Correct
public class CActivity extends CProjectItem<CActivity> {
public class CActivityService extends CProjectItemService<CActivity> {
public class CActivitiesView extends CGridViewBaseProject<CActivity> {

// ❌ Incorrect  
public class Activity extends ProjectItem<Activity> {
public class ActivityService extends ProjectItemService<Activity> {
```

### Package Structure Convention
Follow the strict module-based package organization:
```
src/main/java/tech/derbent/
├── [module-name]/
│   ├── domain/     # Entity classes only
│   ├── service/    # Business logic, repositories, interfaces
│   └── view/       # UI components, panels, pages
```

## 📝 Documentation Standards

**Method Documentation Rules**:
- **Functions ≥10 lines**: Full JavaDoc with @param, @return, @throws
- **Functions <10 lines**: No documentation required
- **Private methods**: No documentation required unless complex

**Exception Handling**:
- Never return null - always throw meaningful exceptions
- Use Check class for parameter validation
- Create domain-specific exceptions

For complete documentation standards, see [.copilot/guidelines.md](.copilot/guidelines.md).

## 🤖 Effective Copilot Prompts

### Creating Entity Classes
```java
// Prompt: "Create CActivity entity that extends CProjectItem with activity type and status"
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> {
    
    public static String getStaticIconFilename() { return "vaadin:tasks"; }
    public static String getStaticIconColorCode() { return "#007bff"; }
    public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }
    
    @AMetaData(displayName = "Activity Type", required = false, order = 2)
    @ManyToOne(fetch = FetchType.EAGER)
    private CActivityType activityType;
}
```

### Creating Service Classes
```java
// Prompt: "Create CActivityService that extends CProjectItemService with kanban support"
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> 
    implements CKanbanService<CActivity, CActivityStatus> {
    
    public CActivityService(CActivityRepository repository, Clock clock, CSessionService sessionService) {
        super(repository, clock, sessionService);
    }
}
```

### Creating View Classes
```java
// Prompt: "Create CActivitiesView that extends CGridViewBaseProject with master-detail layout"
@Route("cactivitiesview")
@PageTitle("Activities")
@Menu(order = 1.1, icon = "class:tech.derbent.activities.view.CActivitiesView", title = "Project.Activities")
@PermitAll
public class CActivitiesView extends CGridViewBaseProject<CActivity> {
    
    public static String getStaticIconFilename() { return CActivity.getStaticIconFilename(); }
    public static String getStaticIconColorCode() { return CActivity.getStaticIconColorCode(); }
}
```

## 🔧 Common Coding Patterns

### Required Static Methods in Entities
Every entity class must implement these static methods:
```java
public class CActivity extends CProjectItem<CActivity> {
    
    // Icon for UI components
    public static String getStaticIconFilename() { 
        return "vaadin:tasks"; 
    }
    
    // Color for theming
    public static String getStaticIconColorCode() { 
        return "#007bff"; 
    }
    
    // Entity color (usually same as icon color)
    public static String getStaticEntityColorCode() { 
        return getStaticIconColorCode(); 
    }
    
    // Associated view class
    public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { 
        return CActivitiesView.class; 
    }
}
```

### Annotation Patterns
Use consistent annotation patterns:
```java
// Entity metadata
@AMetaData(
    displayName = "Activity Type",
    required = false,
    order = 2,
    dataProviderBean = "CActivityTypeService"
)
@ManyToOne(fetch = FetchType.EAGER)
private CActivityType activityType;

// Status entities with color support
@StatusEntity(colorField = "color", nameField = "name")
@Entity
public class CActivityStatus extends CStatus {
    // Implementation
}

// Table and ID column mapping
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> {
```

### Repository Patterns
```java
// Prompt: "Create repository interface for CActivity with project-aware queries"
@Repository
public interface CActivityRepository extends CEntityOfProjectRepository<CActivity> {
    
    @Query("SELECT a FROM CActivity a JOIN FETCH a.activityType WHERE a.project = :project")
    List<CActivity> findByProjectWithType(@Param("project") CProject project);
    
    @Query("SELECT a FROM CActivity a WHERE a.project = :project AND a.status = :status")
    List<CActivity> findByProjectAndStatus(@Param("project") CProject project, 
                                          @Param("status") CActivityStatus status);
}
```

## 🎨 UI Component Patterns

### Grid Configuration
```java
// Prompt: "Configure grid columns for CActivity with standard entity columns"
@Override
public void createGridForEntity(CGrid<CActivity> grid) {
    grid.addIdColumn(CEntityDB::getId, "#", "activity_id");
    grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
    grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
    grid.addLongTextColumn(CActivity::getDescription, "Description", "description");
    grid.addColumnEntityNamed(CActivity::getActivityType, "Type");
    grid.addColumnStatusEntity(CActivity::getStatus, "Status");
}
```

### Panel Creation
```java
// Prompt: "Create CPanelActivity with standard entity fields and enhanced binder"
public class CPanelActivity extends CPanelEntityBase<CActivity> {
    
    @Override
    protected void updatePanelEntityFields() {
        setEntityFields(List.of("name", "description", "activityType", "status"));
    }
    
    @Override
    protected void configureForm() {
        super.configureForm();
        // Additional form configuration
    }
}
```

## 🔍 CAuxillaries Usage Patterns

### Reflection and Utility Methods
```java
// Getting entity classes dynamically
Class<?> entityClass = CAuxillaries.getEntityClass("CActivity");

// Invoking static methods
String iconFilename = CAuxillaries.invokeStaticMethodOfStr(CActivity.class, "getStaticIconFilename");
String colorCode = CAuxillaries.invokeStaticMethodOfStr(CActivity.class, "getStaticIconColorCode");

// Getting service classes
Class<?> serviceClass = CAuxillaries.getServiceClass("CActivity");

// Setting component IDs for testing
CAuxillaries.setId(component);
```

### Icon Resolution Patterns
```java
// Menu icon patterns - use class reference for dynamic resolution
@Menu(
    order = 1.1, 
    icon = "class:tech.derbent.activities.view.CActivitiesView", 
    title = "Project.Activities"
)

// Direct icon usage
public static String getStaticIconFilename() { 
    return "vaadin:tasks"; // Use Vaadin icon names
}

// Color codes - use hex values
public static String getStaticIconColorCode() { 
    return "#007bff"; // Bootstrap blue
}
```

## 🚫 Anti-Patterns to Avoid

### Prohibited Practices
```java
// ❌ Never use primitive types in entities
private int count;           // Wrong
private Integer count;       // Correct

// ❌ Don't use standard Vaadin components directly
Button button = new Button(); // Wrong
CButton button = new CButton(); // Correct

// ❌ Avoid auxiliary setter methods in services
public void setAuxiliaryField(String value) { } // Wrong

// ❌ Don't access entities without proper loading
entity.getLazyField(); // Wrong - may cause LazyInitializationException
```

### Required Practices
```java
// ✅ Always use wrapper types
private Integer count;
private Boolean active;
private BigDecimal amount;

// ✅ Use enhanced binder for forms
CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);

// ✅ Use JOIN FETCH for eager loading
@Query("SELECT a FROM CActivity a JOIN FETCH a.activityType WHERE a.project = :project")

// ✅ Always validate parameters
Check.notNull(project, "Project cannot be null");
Check.notBlank(name, "Name cannot be blank");
```

## 📝 Documentation Standards

### JavaDoc Patterns
```java
/**
 * Service for managing CActivity entities within project context.
 * Provides CRUD operations and kanban board support.
 * 
 * @author Derbent Framework
 * @since 1.0
 */
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    
    /**
     * Groups activities by status for kanban display.
     * 
     * @param project the project to filter activities
     * @return map of status to activities list
     * @throws IllegalArgumentException if project is null
     */
    public Map<CActivityStatus, List<CActivity>> getActivitiesGroupedByStatus(CProject project) {
        Check.notNull(project, "Project cannot be null");
        // Implementation
    }
}
```

### Logging Patterns
```java
// Use SLF4J with consistent patterns
private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);

// Log method entry with parameters
LOGGER.debug("Creating activity: name={}, project={}", name, project.getId());

// Log important operations
LOGGER.info("Activity created successfully: id={}, name={}", activity.getId(), activity.getName());

// Log errors with context
LOGGER.error("Failed to create activity: name={}, project={}, error={}", name, project.getId(), e.getMessage(), e);
```

## 🎯 Copilot Optimization Tips

### Prompt Engineering
1. **Be Specific**: Include exact class names and inheritance chains
2. **Mention Patterns**: Reference existing patterns like "CProjectItem entity" or "CGridViewBaseProject view"
3. **Include Context**: Mention the module (activities, users, etc.) and layer (domain, service, view)
4. **Request Standards**: Ask for "following Derbent patterns" or "with standard annotations"

### Example Effective Prompts
```
"Create CRisk entity that extends CProjectItem with risk type, status, and probability fields, following Derbent entity patterns"

"Implement CRiskService that extends CProjectItemService with risk assessment methods and proper repository injection"

"Create CRisksView that extends CGridViewBaseProject with risk grid columns and master-detail layout"

"Add CPanelRisk form panel with enhanced binder for risk entity fields"
```

### Context Setup for Copilot
When starting a new session, provide this context:
```
// Working on Derbent project - Java Spring Boot + Vaadin Flow
// All classes use C-prefix convention
// Entities extend CProjectItem, services extend CProjectItemService  
// Views extend CGridViewBaseProject or CAbstractEntityDBPage
// Use @AMetaData annotations for form generation
// Follow module structure: domain/service/view packages
```

This ensures Copilot understands the project's conventions and generates appropriate code.