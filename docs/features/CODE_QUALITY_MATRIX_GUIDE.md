# Code Quality Matrix Guide

## Version: 1.0 (2026-01-17)
## Purpose: Comprehensive code quality tracking and compliance verification

**Target Audience**: Developers, Team Leads, Architects, Code Reviewers, AI Agents

---

## Table of Contents

1. [Overview](#overview)
2. [Using the Matrix](#using-the-matrix)
3. [Quality Dimensions Explained](#quality-dimensions-explained)
4. [Status Indicators](#status-indicators)
5. [Interpreting Results](#interpreting-results)
6. [Priority Areas](#priority-areas)
7. [Maintenance and Updates](#maintenance-and-updates)
8. [Related Documentation](#related-documentation)

---

## Overview

The **Code Quality Matrix** (`CODE_QUALITY_MATRIX.xlsx`) is a comprehensive Excel spreadsheet that tracks compliance with coding standards, patterns, and best practices across all 556 classes in the Derbent codebase.

### What It Tracks

The matrix evaluates each class against **55 quality dimensions** organized into these categories:

1. **Naming and Structure** - Conventions and package organization
2. **Entity Patterns** - Domain entity requirements
3. **Field Annotations and Validation** - Data integrity
4. **Constructor and Initialization** - Object creation patterns
5. **Repository Patterns** - Data access layer standards
6. **Service Patterns** - Business logic layer requirements
7. **Initializer Patterns** - System initialization
8. **Page Service Patterns** - UI integration
9. **Exception Handling** - Error management
10. **Logging** - Debugging and monitoring
11. **Interface Implementations** - Contract fulfillment
12. **Code Quality** - General best practices
13. **Testing** - Test coverage
14. **Documentation** - Code documentation
15. **Security** - Access control and tenant isolation
16. **Formatting** - Code style consistency

### Key Features

- **556 Classes Analyzed**: Every C-prefixed class in the codebase
- **55 Quality Dimensions**: Comprehensive coverage of all patterns
- **Visual Status Indicators**: Quick identification of issues
- **Filterable and Sortable**: Excel features for analysis
- **Automated Generation**: Can be regenerated as code evolves

---

## Using the Matrix

### Opening the Matrix

1. Open `docs/CODE_QUALITY_MATRIX.xlsx` in Excel or LibreOffice
2. The main sheet is "Code Quality Matrix"
3. A "Summary" sheet provides overview statistics

### Matrix Structure

**Columns A-E: Class Information**
- **Column A**: Class Name (e.g., `CActivity`, `CActivityService`)
- **Column B**: Module (e.g., `activities`, `validation`)
- **Column C**: Layer (e.g., `domain`, `service`, `view`)
- **Column D**: File Path (full path to Java file)
- **Column E**: Category (Entity, Service, Repository, etc.)

**Columns F onwards: Quality Dimensions**
- Each column represents a quality dimension
- Header row 1: Dimension name
- Header row 2: Dimension description
- Data rows: Status indicator for each class

### Filtering and Sorting

**Filter by Category:**
```
1. Click on Column E header
2. Use Excel's filter dropdown
3. Select: Entity, Service, Repository, Initializer, etc.
```

**Filter by Status:**
```
1. Click on any quality dimension column
2. Filter by: ✓ (Complete), ✗ (Incomplete), - (N/A), ? (Review Needed)
```

**Find All Incomplete for a Pattern:**
```
1. Click column header for the pattern (e.g., "Logger Field")
2. Filter to show only ✗ (Incomplete)
3. Review the list of classes needing attention
```

**Sort by Module:**
```
1. Select Column B
2. Sort A-Z to group classes by feature module
```

---

## Quality Dimensions Explained

### 1. Naming and Structure

#### C-Prefix Naming
- **What**: All concrete classes start with "C" prefix
- **Why**: Enables AI navigation and distinguishes custom classes
- **Example**: `CActivity`, `CActivityService`, `CActivityView`
- **Reference**: `docs/architecture/coding-standards.md`

#### Package Structure
- **What**: Correct package hierarchy: `tech.derbent.{api|app}.{module}.{entity}.{layer}`
- **Layers**: `domain`, `service`, `view`
- **Example**: `tech.derbent.plm.activities.domain.CActivity`

---

### 2. Entity Patterns

#### Entity Annotations
- **What**: Domain entities have `@Entity`, `@Table`, `@AttributeOverride`
- **Required**:
  ```java
  @Entity
  @Table(name = "cactivity")
  @AttributeOverride(name = "id", column = @Column(name = "activity_id"))
  ```
- **Reference**: `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`

#### Entity Constants
- **What**: Five mandatory constants in every entity
- **Required**:
  ```java
  public static final String DEFAULT_COLOR = "#3498db";
  public static final String DEFAULT_ICON = "vaadin:check";
  public static final String ENTITY_TITLE_SINGULAR = "Activity";
  public static final String ENTITY_TITLE_PLURAL = "Activities";
  public static final String VIEW_NAME = "Activity View";
  ```

#### Extends Base Class
- **What**: Entities extend appropriate base class
- **Decision Tree**:
  - Work items → `CProjectItem<T>`
  - Project entities → `CEntityOfProject<T>`
  - Company entities → `CEntityOfCompany<T>`
  - Type entities → `CTypeEntity<T>`
  - Simple entities → `CEntityDB<T>`
- **Reference**: `docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md`

#### Interface Implementation
- **What**: Entities implement required interfaces
- **Common Interfaces**:
  - `IHasAttachments` - For entities with file attachments
  - `IHasComments` - For entities with comments
  - `IHasStatusAndWorkflow<T>` - For workflow entities
  - `ISprintableItem` - For sprint-enabled items

---

### 3. Field Annotations and Validation

#### @AMetaData Annotations
- **What**: Every field has `@AMetaData` with metadata
- **Required Attributes**:
  ```java
  @AMetaData(
      displayName = "Activity Name",
      required = true,
      readOnly = false,
      description = "Name of the activity",
      hidden = false,
      maxLength = 255
  )
  private String name;
  ```

#### Validation Annotations
- **What**: Fields have validation constraints
- **Examples**:
  - `@NotNull` - Field cannot be null
  - `@NotBlank` - String cannot be empty
  - `@Size(min = 1, max = 255)` - String length limits
  - `@Pattern` - Regex validation

#### Column Annotations
- **What**: Proper JPA column configuration
- **Examples**:
  ```java
  @Column(name = "activity_name", nullable = false, length = 255)
  @JoinColumn(name = "project_id", nullable = false)
  ```

#### Fetch Strategy
- **What**: Collections use LAZY loading
- **Required**:
  ```java
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<CAttachment> attachments;
  ```
- **Reference**: `docs/architecture/LAZY_LOADING_BEST_PRACTICES.md`

---

### 4. Constructor and Initialization

#### Default Constructor
- **What**: No-arg constructor for JPA
- **Required**:
  ```java
  public CActivity() {
      super();
      initializeDefaults();
  }
  ```

#### Named Constructor
- **What**: Constructor for programmatic creation
- **Required**:
  ```java
  public CActivity(final String name, final CProject project) {
      super(CActivity.class, name, project);
      initializeDefaults();
  }
  ```

#### initializeDefaults()
- **What**: Initialize default values
- **Required**:
  ```java
  @Override
  private final void initializeDefaults() {
      
      if (priority == null) priority = EPriority.MEDIUM;
      if (attachments == null) attachments = new HashSet<>();
  }
  ```

---

### 5. Repository Patterns

#### Repository Interface
- **What**: Repository extends correct base interface
- **Base Types**:
  - `IProjectItemRepository<T>` - For work items
  - `IEntityOfProjectRepository<T>` - For project entities
  - `IEntityOfCompanyRepository<T>` - For company entities
  - `IChildEntityRepository<T, Master>` - For child entities

#### findById Override
- **What**: Override `findById()` with JOIN FETCH for lazy fields
- **Critical**: Prevents LazyInitializationException
- **Required**:
  ```java
  @Override
  @Query("""
      SELECT e FROM #{#entityName} e
      LEFT JOIN FETCH e.project
      LEFT JOIN FETCH e.entityType
      LEFT JOIN FETCH e.attachments
      LEFT JOIN FETCH e.comments
      WHERE e.id = :id
      """)
  Optional<CActivity> findById(@Param("id") Long id);
  ```
- **Reference**: `docs/architecture/LAZY_LOADING_BEST_PRACTICES.md`

#### Query Patterns
- **What**: Use triple-quoted strings and #{#entityName}
- **Required**:
  ```java
  @Query("""
      SELECT e FROM #{#entityName} e
      WHERE e.project = :project
      """)
  ```

#### ORDER BY Clause
- **What**: All list queries have ORDER BY
- **Standards**:
  - Named entities: `ORDER BY e.name ASC`
  - Regular entities: `ORDER BY e.id DESC`
  - Sprintable items: `ORDER BY e.sprintOrder ASC NULLS LAST, e.id DESC`

---

### 6. Service Patterns

#### Service Annotations
- **What**: Services have required security annotations
- **Required**:
  ```java
  @Service
  @PreAuthorize("isAuthenticated()")
  @PermitAll
  public class CActivityService extends CEntityOfProjectService<CActivity> {
  ```

#### Service Base Class
- **What**: Services extend appropriate base
- **Base Types**:
  - `CEntityOfProjectService<T>` - For project entities
  - `CEntityOfCompanyService<T>` - For company entities
  - `CAbstractService<T>` - For simple entities

#### Stateless Service
- **What**: Services have NO instance state (multi-user safe)
- **Rule**: Only allowed instance fields:
  - Logger (static final)
  - Repository (private final)
  - Other services (private final, injected)
- **Reference**: `docs/architecture/multi-user-singleton-advisory.md`

#### getEntityClass()
- **What**: Return entity class type
- **Required**:
  ```java
  @Override
  public Class<CActivity> getEntityClass() {
      return CActivity.class;
  }
  ```

#### getInitializerService()
- **What**: Return initializer service class
- **Required**:
  ```java
  @Override
  public Class<?> getInitializerService() {
      return CActivityInitializerService.class;
  }
  ```

---

### 7. Initializer Patterns

#### Initializer Structure
- **What**: Initializer extends CInitializerServiceBase
- **Required**:
  ```java
  public class CActivityInitializerService extends CInitializerServiceBase {
      private static final Class<?> clazz = CActivity.class;
      private static final Logger LOGGER = LoggerFactory.getLogger(CActivityInitializerService.class);
  ```

#### createBasicView()
- **What**: Define detail form structure
- **Required**:
  ```java
  public static CDetailSection createBasicView(final CProject project) throws Exception {
      final CDetailSection detailSection = createBaseScreenEntity(clazz, project);
      // Add sections and fields
      return detailSection;
  }
  ```

#### createGridEntity()
- **What**: Define grid column configuration
- **Required**:
  ```java
  public static CGridEntity createGridEntity(final CProject project) {
      final CGridEntity grid = createBaseGridEntity(clazz, project);
      grid.setColumnFields(List.of("id", "name", "entityType", "status", "assignedTo"));
      return grid;
  }
  ```

#### initialize()
- **What**: Register entity with system
- **Required**:
  ```java
  public static void initialize(final CProject project, ...) throws Exception {
      final CDetailSection detailSection = createBasicView(project);
      final CGridEntity grid = createGridEntity(project);
      initBase(clazz, project, ..., menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
  }
  ```

#### initializeSample()
- **What**: Create sample data
- **Required**:
  ```java
  public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
      // Clear existing data
      // Define sample data array
      // Initialize entities
  }
  ```

#### CDataInitializer Registration
- **What**: Entity is registered in CDataInitializer
- **Required**: Import and call initialize() and initializeSample()

---

### 8. Page Service Patterns

#### Page Service
- **What**: Page service exists for workflow/sprint entities
- **When Needed**:
  - Entity has workflow (IHasStatusAndWorkflow)
  - Entity has sprint support (ISprintableItem)
  - Custom page logic required

#### Page Service Interfaces
- **What**: Implements correct interfaces
- **Examples**:
  - `IPageServiceHasStatusAndWorkflow<T>` - For workflow entities
  - `ISprintItemPageService<T>` - For sprintable items

---

### 9. Exception Handling

#### Exception Pattern
- **What**: Use Check.notNull and proper exception handling
- **Required**:
  ```java
  Check.notNull(entity, "Entity cannot be null");
  Objects.requireNonNull(project, "Project is required");
  ```

#### User Exception Handling
- **What**: UI handlers show exceptions to user
- **Required**:
  ```java
  try {
      service.save(entity);
  } catch (Exception e) {
      CNotificationService.showException(e);
  }
  ```

---

### 10. Logging

#### Logger Field
- **What**: Static final Logger with correct class
- **Required**:
  ```java
  private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
  ```

#### Logging Pattern
- **What**: Follows ANSI logging format
- **Console Pattern**: Defined in application.properties
- **Reference**: `docs/architecture/coding-standards.md`

#### Log Levels
- **What**: Appropriate log levels
- **Guidelines**:
  - `DEBUG` - Detailed diagnostic info
  - `INFO` - Important business events
  - `WARN` - Recoverable issues
  - `ERROR` - Errors requiring attention

---

### 11. Interface Implementations

#### IHasAttachments
- **What**: Proper getter/setter for attachments
- **Required**:
  ```java
  @Override
  public Set<CAttachment> getAttachments() {
      if (attachments == null) attachments = new HashSet<>();
      return attachments;
  }
  
  @Override
  public void setAttachments(Set<CAttachment> attachments) {
      this.attachments = attachments;
  }
  ```

#### IHasComments
- **What**: Proper getter/setter for comments
- **Similar to IHasAttachments**

#### IHasStatusAndWorkflow
- **What**: Workflow-related methods
- **Required**:
  ```java
  @Override
  public CWorkflowEntity getWorkflow() {
      Check.notNull(entityType, "Entity type cannot be null");
      return entityType.getWorkflow();
  }
  ```

---

### 12. Code Quality

#### Getter/Setter Pattern
- **What**: Setters call updateLastModified()
- **Required**:
  ```java
  public void setName(final String name) {
      this.name = name;
      updateLastModified();
  }
  ```

#### No Raw Types
- **What**: All generics properly parameterized
- **Bad**: `List list = new ArrayList();`
- **Good**: `List<CActivity> list = new ArrayList<>();`

#### Constants Naming
- **What**: Constants are static final SCREAMING_SNAKE_CASE
- **Examples**:
  ```java
  private static final String MENU_ORDER = "10.1";
  public static final String DEFAULT_COLOR = "#3498db";
  ```

---

### 13. Testing

#### Unit Tests
- **What**: Has corresponding *Test class
- **Location**: `src/test/java/.../C{Entity}Test.java`
- **Minimum**: Test creation, validation, relationships

#### Integration Tests
- **What**: Service and repository tests
- **Coverage**: Business logic, queries, transactions

#### UI Tests
- **What**: Playwright tests for views
- **Location**: `src/test/java/automated_tests/.../C{Entity}PlaywrightTest.java`
- **Reference**: `PLAYWRIGHT_TESTING_GUIDE.md`

---

### 14. Documentation

#### JavaDoc
- **What**: Class-level documentation
- **Required**:
  ```java
  /**
   * Activity entity represents a work item with workflow.
   * 
   * @author Derbent Team
   * @version 1.0
   */
  public class CActivity extends CProjectItem<CActivity> {
  ```

#### Method Documentation
- **What**: Complex methods have comments
- **When**: Business logic, complex algorithms, non-obvious code

#### Implementation Doc
- **What**: Implementation markdown for complex features
- **Location**: `docs/implementation/{FEATURE}_IMPLEMENTATION.md`

---

### 15. Security

#### Access Control
- **What**: Proper security annotations
- **Service Level**:
  ```java
  @Service
  @PreAuthorize("isAuthenticated()")
  @PermitAll
  ```

#### Tenant Context
- **What**: Uses session service for company/project context
- **Required**: Never trust caller-provided IDs
- **Reference**: `docs/architecture/multi-user-singleton-advisory.md`

---

### 16. Formatting

#### Code Formatting
- **What**: Follows eclipse-formatter.xml
- **Standards**:
  - 4 spaces indentation
  - No tabs
  - Line width 120 characters

#### Import Organization
- **What**: Clean imports, no wildcards
- **Run**: `./mvnw spotless:apply`

---

## Status Indicators

### ✓ Complete (Green)
- **Meaning**: Pattern is fully implemented
- **Action**: No action needed
- **Example**: Entity has all 5 required constants

### ✗ Incomplete (Red)
- **Meaning**: Pattern is missing or partially implemented
- **Action**: Fix required
- **Priority**: High
- **Example**: Repository missing JOIN FETCH in findById()

### - N/A (Gray)
- **Meaning**: Pattern not applicable to this class type
- **Action**: No action needed
- **Example**: Logger field not needed for configuration classes

### ? Review Needed (Yellow)
- **Meaning**: Manual review required, automated check inconclusive
- **Action**: Manual inspection
- **Priority**: Medium
- **Example**: Interface implementation correctness

---

## Interpreting Results

### Finding Problem Areas

**To find all classes with incomplete validation:**
```
1. Open matrix
2. Find "@AMetaData Annotations" column
3. Filter to show only ✗ (Incomplete)
4. Review and fix each class
```

**To audit a specific module:**
```
1. Filter Column B (Module) by module name (e.g., "validation")
2. Scan across quality dimensions
3. Address red (✗) and yellow (?) cells
```

**To check test coverage:**
```
1. Find "Unit Tests" column
2. Filter by ✗ (Incomplete)
3. Create missing test classes
```

### Quality Metrics

**Calculate completion percentage:**
```
1. Count total non-N/A cells in a column
2. Count ✓ (Complete) cells
3. Percentage = (Complete / Total) * 100
```

**Identify highest priority issues:**
```
Priority 1: Entity Annotations, Service Annotations, Security
Priority 2: Repository patterns (JOIN FETCH, ORDER BY)
Priority 3: Testing, Documentation
Priority 4: Code formatting, imports
```

---

## Priority Areas

### Critical (Fix Immediately)

1. **Security Issues**
   - Missing @PreAuthorize annotations
   - Tenant context violations

2. **Data Integrity**
   - Missing validation annotations
   - Incorrect base class extension

3. **Performance**
   - Missing JOIN FETCH (causes N+1 queries)
   - Missing ORDER BY (unpredictable results)

### High Priority (Fix Soon)

1. **Service Patterns**
   - Stateless service violations (instance state)
   - Missing getEntityClass/getInitializerService

2. **Repository Patterns**
   - Incorrect query patterns
   - Missing repository methods

3. **Initializer Patterns**
   - Missing CDataInitializer registration
   - Incomplete sample data

### Medium Priority (Plan to Fix)

1. **Testing**
   - Missing unit tests
   - Missing integration tests
   - Missing UI tests

2. **Documentation**
   - Missing JavaDoc
   - Missing method comments
   - Missing implementation docs

### Low Priority (Nice to Have)

1. **Code Style**
   - Formatting issues
   - Import organization
   - Naming conventions (non-critical)

---

## Maintenance and Updates

### Regenerating the Matrix

When code changes significantly, regenerate the matrix:

```bash
# Generate updated class list
cd /home/runner/work/derbent/derbent
find src/main/java/tech/derbent -name "C*.java" -type f | \
  sed 's|src/main/java/||' | sed 's|/|.|g' | sed 's|.java||' | \
  sort > /tmp/quality_matrix/all_classes.txt

# Run generator
python3 /tmp/quality_matrix/generate_quality_matrix.py
```

### Updating Quality Dimensions

To add new quality dimensions:

1. Edit `generate_quality_matrix.py`
2. Add dimension to `QUALITY_DIMENSIONS` list
3. Add detection logic in `analyze_class_file()`
4. Add status determination in `determine_status()`
5. Regenerate matrix

### Tracking Progress

**Monthly Review:**
1. Regenerate matrix
2. Calculate completion percentages
3. Identify trends (improving/declining)
4. Plan remediation work

**Per-Feature Review:**
1. Filter by module
2. Verify all patterns complete
3. Address gaps before merging

---

## Related Documentation

### Architecture
- [Coding Standards](architecture/coding-standards.md)
- [Entity Inheritance Patterns](architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md)
- [New Entity Complete Checklist](architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md)
- [Lazy Loading Best Practices](architecture/LAZY_LOADING_BEST_PRACTICES.md)
- [Service Layer Patterns](architecture/service-layer-patterns.md)
- [View Layer Patterns](architecture/view-layer-patterns.md)
- [Multi-User Singleton Advisory](architecture/multi-user-singleton-advisory.md)

### Development
- [Getting Started](development/getting-started.md)
- [Project Structure](development/project-structure.md)
- [Copilot Guidelines](development/copilot-guidelines.md)
- [Multi-User Development Checklist](development/multi-user-development-checklist.md)

### Testing
- [Playwright Testing Guide](../PLAYWRIGHT_TESTING_GUIDE.md)
- [Testing Rules](../TESTING_RULES.md)

---

## Version History

- **v1.0** (2026-01-17): Initial code quality matrix and guide
  - 556 classes analyzed
  - 55 quality dimensions
  - Automated generation script
  - Comprehensive documentation

---

## Feedback and Improvements

If you discover patterns or rules not covered in this matrix:

1. Document the pattern in appropriate architecture/development docs
2. Update `generate_quality_matrix.py` to detect the pattern
3. Regenerate the matrix
4. Update this guide

**Contact**: Derbent Development Team

---

**Last Updated**: 2026-01-17  
**Status**: Active  
**Next Review**: Monthly or after major refactoring
