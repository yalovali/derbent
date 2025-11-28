# GitHub Copilot Development Guidelines

## Overview

Derbent is specifically designed for AI-assisted development with GitHub Copilot. This document provides guidelines, patterns, and best practices to maximize Copilot's effectiveness when working on this project.

## Why Derbent Works Well with Copilot

### 1. Consistent Naming Convention (C-Prefix)

The C-prefix convention helps Copilot understand custom vs. framework classes:

```java
// Copilot recognizes these as custom domain classes
CActivity activity = new CActivity();
CUser user = service.findUser();
CProject project = getCurrentProject();

// Copilot distinguishes from framework classes
Activity activity;  // Would be ambiguous
User user;         // Could be javax.security or custom
```

### 2. Predictable Inheritance Hierarchies

Copilot learns patterns quickly when hierarchies are consistent:

```java
// Copilot suggests: "extends CProjectItem<CRisk>"
public class CRisk extends CProjectItem<CRisk> {
    
// Copilot suggests: "extends CEntityOfProjectService<CRisk>"  
public class CRiskService extends CEntityOfProjectService<CRisk> {
```

### 3. Metadata-Driven Development

`@AMetaData` annotations help Copilot generate consistent field definitions:

```java
// Type this annotation, Copilot suggests common patterns
@Column(nullable = false)
@Size(max = 255)
@AMetaData(
    displayName = "Risk Name",  // Copilot fills in appropriate name
    required = true,
    
)
private String name;
```

## Copilot-Friendly Patterns

### Starting a New Entity

**Type this**:
```java
@Entity
@Table(name = "crisk")
public class CRisk
```

**Copilot suggests**:
```java
@Entity
@Table(name = "crisk")
@AttributeOverride(name = "id", column = @Column(name = "risk_id"))
public class CRisk extends CProjectItem<CRisk> {
    
    // Copilot continues with common patterns
    private static final Logger LOGGER = LoggerFactory.getLogger(CRisk.class);
    
    @Column(nullable = true, length = 20)
    @AMetaData(displayName = "Severity", required = false)
    private String severity;
```

### Starting a New Service

**Type this**:
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CRiskService
```

**Copilot suggests**:
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CRiskService extends CEntityOfProjectService<CRisk> {
    
    public CRiskService(
        final IRiskRepository repository,
        final Clock clock,
        final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    protected Class<CRisk> getEntityClass() {
        return CRisk.class;
    }
```

### Starting a New Repository

**Type this**:
```java
public interface IRiskRepository
```

**Copilot suggests**:
```java
public interface IRiskRepository extends IEntityOfProjectRepository<CRisk> {
    
    // Copilot suggests common query methods
    List<CRisk> findBySeverity(String severity);
    
    @Query("SELECT COUNT(r) FROM CRisk r WHERE r.severity = :severity")
    long countBySeverity(@Param("severity") String severity);
}
```

### Starting a New View

**Type this**:
```java
@Route(value = "risks", layout = MainLayout.class)
@PageTitle("Risks")
@RolesAllowed("USER")
public class CRiskView
```

**Copilot suggests**:
```java
@Route(value = "risks", layout = MainLayout.class)
@PageTitle("Risks")
@RolesAllowed("USER")
public class CRiskView extends CAbstractPage {
    
    private final CRiskService service;
    private CGrid<CRisk> grid;
    
    public CRiskView(CRiskService service) {
        this.service = service;
        initializeComponents();
        configureLayout();
    }
```

## Code Generation Tips

### Tip 1: Write Descriptive Comments

Comments help Copilot understand intent:

```java
// Create a method to find all high-severity risks for the current project
@Transactional(readOnly = true)
public List<CRisk> findHighSeverityRisks() {
    // Copilot generates appropriate implementation
    CProject project = sessionService.getActiveProject()
        .orElseThrow(() -> new IllegalStateException("No active project"));
    return repository.findByProjectAndSeverity(project, "HIGH");
}
```

### Tip 2: Start with Method Signatures

Type complete method signatures, Copilot fills in implementation:

```java
// Type the signature
@Transactional
public void mitigateRisk(CRisk risk, String mitigationPlan) {
    // Copilot suggests validation and implementation
```

### Tip 3: Use Consistent Naming

Copilot learns from consistent naming:

```java
// Good patterns Copilot recognizes
private CActivityService activityService;      // Service fields
private final IActivityRepository repository;  // Repository fields
private CGrid<CActivity> grid;                 // Component fields
private TextField nameField;                    // Form fields
private ComboBox<CStatus> statusComboBox;      // ComboBox fields
```

### Tip 4: Leverage Examples

Show Copilot one example, it generates similar code:

```java
// First field with full metadata
@Column(nullable = false)
@Size(max = 255)
@AMetaData(
    displayName = "Risk Name",
    required = true,
    
    maxLength = 255
)
private String name;

// Type next field, Copilot follows pattern
@Column(nullable = true)
@AMetaData(
    displayName = "Description",  // Copilot fills in the rest
```

## Common Generation Patterns

### Pattern 1: CRUD Operations

```java
// In Service class, type:
@Transactional
public CRisk createRisk(String name) {
    // Copilot generates:
    Check.notBlank(name, "Name cannot be blank");
    
    CProject project = sessionService.getActiveProject()
        .orElseThrow(() -> new IllegalStateException("No active project"));
    
    CRisk risk = new CRisk(name, project);
    initializeNewEntity(risk);
    return save(risk);
}
```

### Pattern 2: Query Methods

```java
// In Repository interface, type:
@Query("SELECT r FROM CRisk r WHERE r.project = :project AND r.severity = :severity")
List<CRisk> findByProjectAndSeverity(
    // Copilot completes:
    @Param("project") CProject project,
    @Param("severity") String severity
);
```

### Pattern 3: View Components

```java
// In View class, type:
private void initializeGrid() {
    grid = new CGrid<>(CRisk.class);
    
    // Copilot suggests standard columns:
    grid.addColumn(CRisk::getName).setHeader("Name").setSortable(true);
    grid.addColumn(CRisk::getSeverity).setHeader("Severity").setSortable(true);
    grid.addColumn(r -> r.getProject().getName()).setHeader("Project");
    
    grid.setItems(service.findAll());
    grid.setSelectionMode(SelectionMode.SINGLE);
}
```

### Pattern 4: Form Creation

```java
// Type this:
private void createForm() {
    // Add field declaration
    TextField nameField = new TextField("Name");
    
    // Copilot suggests:
    nameField.setRequired(true);
    nameField.setMaxLength(255);
    
    ComboBox<CRiskSeverity> severityComboBox = new ComboBox<>("Severity");
    severityComboBox.setItems(severityService.findAll());
    severityComboBox.setItemLabelGenerator(CRiskSeverity::getName);
```

## Troubleshooting Copilot Suggestions

### Issue 1: Copilot Suggests Wrong Base Class

**Problem**: `public class CRisk extends CEntityDB<CRisk>`

**Solution**: Add comment above class:
```java
// CRisk should extend CProjectItem for project-scoped entities
public class CRisk extends CProjectItem<CRisk> {
```

### Issue 2: Copilot Forgets C-Prefix

**Problem**: `Activity activity = new Activity()`

**Solution**: Be consistent with C-prefix in all code, Copilot learns quickly:
```java
// Always use C-prefix
CActivity activity = new CActivity();
CUser user = userService.findUser();
```

### Issue 3: Copilot Generates Incorrect Annotations

**Problem**: Wrong annotation patterns

**Solution**: Provide example in comment:
```java
// Use @AMetaData annotation like this example:
// @AMetaData(displayName = "Name", required = true, )
@Column(nullable = false)
@AMetaData(
```

## Advanced Copilot Usage

### Using Copilot for Test Generation

```java
// In test class, type:
@Test
void testCreateRisk_WithValidName_Success() {
    // Given
    String name = "Test Risk";
    
    // Copilot generates test body:
    when(sessionService.getActiveProject()).thenReturn(Optional.of(project));
    when(repository.save(any(CRisk.class))).thenAnswer(i -> i.getArgument(0));
    
    // When
    CRisk result = service.createRisk(name);
    
    // Then
    assertNotNull(result);
    assertEquals(name, result.getName());
    verify(repository).save(any(CRisk.class));
}
```

### Using Copilot for Documentation

```java
/**
 * Copilot generates JavaDoc based on method signature:
 * 
 * @param risk the risk to mitigate
 * @param mitigationPlan the plan to mitigate the risk
 * @throws IllegalArgumentException if risk or plan is null
 */
@Transactional
public void mitigateRisk(CRisk risk, String mitigationPlan) {
```

### Using Copilot for Refactoring

```java
// Add comment describing refactoring goal:
// Refactor this method to use the new status service
@Transactional
public void updateRiskStatus(CRisk risk, String statusName) {
    // Copilot suggests refactored implementation
```

## Best Practices for Copilot Development

### 1. Keep Context Window Clean

- Remove unused imports
- Delete commented-out code  
- Keep methods focused and small
- Copilot works better with clean code

### 2. Use Descriptive Variable Names

```java
// ✅ GOOD - Copilot understands intent
List<CActivity> overdueActivities = findOverdueActivities();
CProject currentProject = getCurrentProject();

// ❌ BAD - Ambiguous for Copilot
List<CActivity> list = findOverdue();
CProject p = getProject();
```

### 3. Break Down Complex Tasks

```java
// Instead of one complex method, break into steps:

// Step 1: Validate input
private void validateRiskCreation(String name, CProject project) {
    // Copilot generates validation
}

// Step 2: Create entity
private CRisk createRiskEntity(String name, CProject project) {
    // Copilot generates creation logic
}

// Step 3: Initialize and save
private CRisk initializeAndSaveRisk(CRisk risk) {
    // Copilot generates initialization
}
```

### 4. Provide Context with Comments

```java
// Context helps Copilot understand business rules:

// Business rule: Only high-severity risks require approval
@Transactional
public void createRisk(CRisk risk) {
    if ("HIGH".equals(risk.getSeverity())) {
        // Copilot suggests approval workflow
```

### 5. Use Type Hints

```java
// Explicit types help Copilot:
CActivityService service = applicationContext.getBean(CActivityService.class);

// Better than:
var service = applicationContext.getBean(CActivityService.class);
```

## Learning Resources

### Example Files to Study

These files demonstrate Copilot-friendly patterns:

1. **Entity**: `src/main/java/tech/derbent/activities/domain/CActivity.java`
2. **Service**: `src/main/java/tech/derbent/activities/service/CActivityService.java`
3. **Repository**: `src/main/java/tech/derbent/activities/service/IActivityRepository.java`
4. **View**: `src/main/java/tech/derbent/kanban/CKanbanView.java`

### Documentation References

- [Entity Inheritance Patterns](../architecture/entity-inheritance-patterns.md)
- [Service Layer Patterns](../architecture/service-layer-patterns.md)
- [View Layer Patterns](../architecture/view-layer-patterns.md)
- [Coding Standards](../architecture/coding-standards.md)

## Quick Reference

### Entity Checklist

```java
☐ @Entity and @Table annotations
☐ Extends appropriate base class (CProjectItem, CEntityOfProject, etc.)
☐ @AttributeOverride for custom ID column name
☐ C-prefix in class name
☐ @AMetaData on all fields
☐ Proper fetch strategies (EAGER/LAZY)
☐ Default JPA constructor
☐ Business constructor with required fields
☐ Getters and setters
```

### Service Checklist

```java
☐ @Service annotation
☐ @PreAuthorize for security
☐ Extends appropriate base service
☐ Constructor injection of dependencies
☐ Override getEntityClass()
☐ Override checkDeleteAllowed() if needed
☐ Business logic methods with @Transactional
☐ Proper exception handling
☐ Logging for important operations
```

### View Checklist

```java
☐ @Route annotation with value and layout
☐ @PageTitle annotation
☐ @RolesAllowed for security
☐ Constructor injection of services
☐ initializeComponents() method
☐ configureLayout() method
☐ Event handlers
☐ Proper error handling with notifications
```

## Summary

GitHub Copilot works exceptionally well with Derbent because of:

1. **Consistent Patterns**: C-prefix, inheritance hierarchies, metadata annotations
2. **Predictable Structure**: Entity → Service → View pattern
3. **Clear Naming**: Descriptive names that indicate purpose
4. **Good Documentation**: Comments and JavaDoc that provide context
5. **Type Safety**: Strong typing with generics

By following these guidelines, you'll maximize Copilot's effectiveness and accelerate development while maintaining code quality.
