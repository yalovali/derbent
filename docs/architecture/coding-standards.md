# Derbent Project Coding Standards

## Entity Layer

### Class Naming & Structure
- **All domain classes must start with "C"**: `CActivity`, `CUser`, `CProject`
- **Inheritance hierarchy**: `CEntityDB` → `CEntityNamed` → `CEntityOfProject`
- **Type entities**: Extend `CTypeEntity` (e.g., `CActivityType`, `CUserType`)
- **Use wrapper types only**: `Integer`, `Boolean`, `BigDecimal` (never primitives)

### Entity Annotations
```java
@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CEntityOfProject<CActivity> {
    
    @AMetaData(
        displayName = "Activity Type", 
        required = false, 
        order = 2,
        dataProviderBean = "CActivityTypeService"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    private CActivityType activityType;
}
```

### Icon & Color Standards
```java
public static String getIconFilename() { return "vaadin:tasks"; }
public static String getIconColorCode() { return "#007bff"; }
public static String getEntityColorCode() { return getIconColorCode(); }
```

## Service Layer

### Service Structure
- **Extend appropriate base**: `CAbstractService` or `CEntityOfProjectService`
- **Security annotations**: `@PreAuthorize("isAuthenticated()")`
- **Constructor injection**: `repository`, `clock`, `sessionService`

### Lazy Loading Pattern
```java
@Override
protected void initializeLazyFields(final CActivity entity) {
    super.initializeLazyFields(entity); // Handles CEntityOfProject
    initializeLazyRelationship(entity.getSpecificField());
}

@Override
public CActivity findById(final Long id) {
    return ((CActivityRepository) repository).findByIdWithEagerLoading(id).orElse(null);
}
```

### Repository Queries
```java
@Query("SELECT e FROM CActivity e " +
       "LEFT JOIN FETCH e.activityType " +
       "LEFT JOIN FETCH e.status " +
       "LEFT JOIN FETCH e.project " +
       "WHERE e.id = :id")
Optional<CActivity> findByIdWithEagerLoading(@Param("id") Long id);
```

## View Layer

### View Inheritance
- **Base classes**: `CAbstractPage` → `CAbstractEntityDBPage` → `CProjectAwareMDPage`
- **Panel architecture**: `CPanelEntityBase` for complex forms
- **Use enhanced binder**: `CEnhancedBinder` with `CEntityFormBuilder`

### Routing Pattern
```java
@Route("cactivitiesview")
@PageTitle("Activities")
@Menu(order = 1.1, icon = "class:tech.derbent.activities.view.CActivitiesView", title = "Project.Activities")
@PermitAll
public class CActivitiesView extends CProjectAwareMDPage<CActivity> {
```

### Form Building
```java
public class CPanelActivityDescription extends CPanelActivityBase {
    @Override
    protected void updatePanelEntityFields() {
        setEntityFields(List.of("name", "description", "activityType", "status"));
    }
}
```

## Project Structure

### Module Organization
```
src/main/java/tech/derbent/
├── abstracts/          # Base classes, annotations, utilities
├── activities/         # Activity management
│   ├── domain/        # CActivity, CActivityType, CActivityStatus
│   ├── service/       # Business logic and repositories
│   └── view/          # UI components and panels
├── users/             # User management
├── projects/          # Project management
└── meetings/          # Meeting management
```

### Package Rules
- **Domain**: Entity classes only
- **Service**: Business logic, repositories, interfaces
- **View**: UI components, panels, pages

## Testing Standards

### Test Organization
```
src/test/java/
├── unit_tests/        # Business logic tests
├── ui_tests/          # Vaadin component tests
└── automated_tests/   # Playwright automation
```

### Test Patterns
```java
public class CActivitiesViewTest extends CGenericViewTest<CActivity> {
    @Override
    protected Class<?> getViewClass() { return CActivitiesView.class; }
    
    @Override
    protected Class<CActivity> getEntityClass() { return CActivity.class; }
}
```

### Common Test Functions
- `clickCancel()`, `clickNew()`, `clickSave()`
- `clickGrid(int index)`
- `takeScreenshot(String name, boolean isFailure)`

## Code Quality Rules

### General Principles
- **Use `final` everywhere possible**
- **Null checking**: Always validate parameters
- **Logging**: At method entry with parameters
- **Exception handling**: Use appropriate dialog classes

### Prohibited Patterns
- ❌ Primitive types in entities
- ❌ Standard Vaadin `Button` (use `CButton`)
- ❌ Auxiliary setter methods in services
- ❌ Direct entity access without lazy loading

### Required Patterns
- ✅ Enhanced binder for form generation
- ✅ JOIN FETCH queries for eager loading
- ✅ Project context for project entities
- ✅ Panel-based UI architecture

## Icon Reference

| Entity Type | Icon | Color |
|-------------|------|-------|
| Activities | `vaadin:tasks` | `#007bff` |
| Users | `vaadin:user` | `#28a745` |
| Projects | `vaadin:folder` | `#6f42c1` |
| Meetings | `vaadin:calendar` | `#fd7e14` |
| Risks | `vaadin:warning` | `#dc3545` |
| Comments | `vaadin:comment` | `#20c997` |
| Orders | `vaadin:invoice` | `#ffc107` |

## Development Commands

### Build & Test
```bash
mvn clean compile                    # Build (12-15 seconds)
mvn spotless:apply                   # Format code
mvn spring-boot:run -Ph2-local-development  # Run with H2
./run-playwright-tests.sh mock      # UI tests (37+ seconds)
```

### Database Rules
- **PostgreSQL for production**, H2 for development only
- **Password hash**: `$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu` (test123)
- **Reset sequences** in data.sql after schema changes

---

*Keep this document concise and focused. For detailed implementation examples, refer to existing code patterns in the codebase.*