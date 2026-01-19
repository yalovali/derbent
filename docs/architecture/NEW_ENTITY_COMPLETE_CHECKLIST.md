# New Entity Implementation Complete Checklist

## Version: 2.0 (2026-01-17)
## Purpose: Comprehensive checklist to ensure NO steps are skipped when creating new entities

**Target Audience**: Developers, AI Agents (GitHub Copilot)

---

## Table of Contents

1. [Pre-Implementation Planning](#pre-implementation-planning)
2. [Domain Entity Checklist](#domain-entity-checklist)
3. [Repository Checklist](#repository-checklist)
4. [Service Checklist](#service-checklist)
5. [Initializer Service Checklist](#initializer-service-checklist)
6. [Page Service Checklist](#page-service-checklist)
7. [CDataInitializer Registration](#cdatainitializer-registration)
8. [UI Components (If Needed)](#ui-components-if-needed)
9. [Testing Checklist](#testing-checklist)
10. [Documentation Checklist](#documentation-checklist)
11. [Final Verification](#final-verification)

---

## Pre-Implementation Planning

### ☐ 1. Determine Entity Scope and Type

**Answer these questions:**
- [ ] **Scope**: Company-scoped or Project-scoped?
- [ ] **Type**: Work item (with workflow) or Configuration/Type entity?
- [ ] **Parent**: Is this a child entity (master-detail relationship)?
- [ ] **Features**: Does it need attachments? Comments? Sprint support? Gantt display?

**Use the Entity Inheritance Decision Tree:**

```
Is this entity stored in database?
├─ NO → Don't extend anything (POJO or Component)
└─ YES → Extend CEntityDB<T>
    │
    ├─ Does it need a human-readable name?
    │  └─ YES → Extend CEntityNamed<T>
    │      │
    │      ├─ Is it scoped to a company (workflows, roles, types)?
    │      │  └─ YES → Extend CEntityOfCompany<T>
    │      │      │
    │      │      └─ Examples: CRole, CWorkflowBase, CProjectType
    │      │
    │      └─ Is it scoped to a project?
    │          └─ YES → Extend CEntityOfProject<T>
    │              │
    │              ├─ Is it a work item with status workflow?
    │              │  └─ YES → Extend CProjectItem<T>
    │              │      │
    │              │      └─ Examples: CActivity, CMeeting, CDecision
    │              │
    │              └─ NO → Stay at CEntityOfProject<T>
    │                  │
    │                  └─ Examples: CProject, CProjectPhase
    │
    └─ NO → Stay at CEntityDB<T>
        │
        └─ Examples: System config, simple lookup tables
```

**Reference**: `docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md`

### ☐ 2. Define Entity Characteristics

- [ ] **Name Pattern**: Follows C-prefix naming (e.g., `CValidationCase`)
- [ ] **Has Type Entity?**: Do you need a separate Type entity (e.g., `CValidationCaseType`)?
- [ ] **Has Priority?**: Enum for priority levels?
- [ ] **Has Severity?**: Enum for severity/criticality?
- [ ] **Has Status?**: Uses workflow status transitions?
- [ ] **Child Entities?**: Will it have master-detail children?

---

## Domain Entity Checklist

### ☐ 1. Entity Class Structure

**File Location**: `src/main/java/tech/derbent/app/{module}/{entity}/domain/C{EntityName}.java`

#### ✅ MANDATORY Elements:

```java
@Entity
@Table (name = "c{entityname}")  // lowercase, no underscores
@AttributeOverride (name = "id", column = @Column (name = "{entity}_id"))
public class C{EntityName} extends [BaseClass]<C{EntityName}>
        implements [Interfaces] {

    // 1. CONSTANTS (MANDATORY)
    public static final String DEFAULT_COLOR = "#RRGGBB";
    public static final String DEFAULT_ICON = "vaadin:icon-name";
    public static final String ENTITY_TITLE_PLURAL = "Plural Name";
    public static final String ENTITY_TITLE_SINGULAR = "Singular Name";
    public static final String VIEW_NAME = "Entity Name View";

    // 2. FIELDS (with @AMetaData annotations)
    // 3. CONSTRUCTORS
    // 4. initializeDefaults() method
    // 5. GETTERS AND SETTERS
    // 6. INTERFACE IMPLEMENTATIONS
}
```

#### Checklist Items:

- [ ] **Package**: Correct package `tech.derbent.plm.{module}.{entity}.domain`
- [ ] **Class Name**: C-prefix (e.g., `CValidationCase`)
- [ ] **@Entity**: Annotation present
- [ ] **@Table**: Correct table name (lowercase, c-prefix)
- [ ] **@AttributeOverride**: ID column named `{entity}_id`
- [ ] **Extends**: Correct base class from decision tree
- [ ] **Implements**: All required interfaces

**MANDATORY Interfaces by Entity Type:**

| Entity Type | Interfaces Required |
|-------------|---------------------|
| Work Item (CProjectItem) | `IHasStatusAndWorkflow<T>`, `IHasAttachments`, `IHasComments` |
| Project Entity | `IHasAttachments`, `IHasComments` (optional) |
| Type Entity (CTypeEntity) | None (base class handles it) |
| Child Entity | None typically |

- [ ] **Constants**: All 5 mandatory constants defined
- [ ] **DEFAULT_COLOR**: Valid hex color
- [ ] **DEFAULT_ICON**: Valid Vaadin icon name
- [ ] **ENTITY_TITLE_SINGULAR**: Used by CEntityRegistry
- [ ] **ENTITY_TITLE_PLURAL**: Used by CEntityRegistry
- [ ] **VIEW_NAME**: Descriptive view name

### ☐ 2. Fields and Annotations

#### For Each Field:

- [ ] **@AMetaData**: Annotation present with all attributes
  ```java
  @AMetaData(
      displayName = "Field Label",
      required = true/false,
      readOnly = false,
      description = "Field description",
      hidden = false,
      maxLength = 255,  // if string
      dataProviderBean = "CServiceName"  // if reference
  )
  ```
- [ ] **@Column**: Database column configuration
- [ ] **@Size**: For string fields (matches maxLength)
- [ ] **@NotNull/@NotBlank**: Validation if required
- [ ] **@ManyToOne/@OneToMany**: Correct relationship type
- [ ] **FetchType**: LAZY for relationships (performance)

**Special Fields:**

- [ ] **Attachments** (if IHasAttachments):
  ```java
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "{entity}_id")
  @AMetaData(
      displayName = "Attachments",
      dataProviderBean = "CAttachmentService",
      createComponentMethod = "createComponent"
  )
  private Set<CAttachment> attachments = new HashSet<>();
  ```

- [ ] **Comments** (if IHasComments):
  ```java
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "{entity}_id")
  @AMetaData(
      displayName = "Comments",
      dataProviderBean = "CCommentService",
      createComponentMethod = "createComponent"
  )
  private Set<CComment> comments = new HashSet<>();
  ```

- [ ] **EntityType** (if IHasStatusAndWorkflow):
  ```java
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "entitytype_id", nullable = true)
  @AMetaData(
      displayName = "Type",
      dataProviderBean = "C{Entity}TypeService",
      setBackgroundFromColor = true,
      useIcon = true
  )
  private C{Entity}Type entityType;
  ```

### ☐ 3. Constructors and Initialization

- [ ] **Default Constructor**: No-arg constructor for JPA
  ```java
  public C{Entity}() {
      super();
      initializeDefaults();
  }
  ```

- [ ] **Named Constructor**: For programmatic creation
  ```java
  public C{Entity}(final String name, final CProject project) {
      super(C{Entity}.class, name, project);
      initializeDefaults();
  }
  ```

- [ ] **initializeDefaults()**: Initialize all non-null fields
  ```java
  @Override
  protected void initializeDefaults() {
      super.initializeDefaults();
      if (priority == null) priority = EPriority.MEDIUM;
      if (severity == null) severity = ESeverity.NORMAL;
  }
  ```

### ☐ 4. Interface Implementation Methods

**If implements IHasAttachments:**
- [ ] `getAttachments()` - with null check and initialization
- [ ] `setAttachments(Set<CAttachment> attachments)`

**If implements IHasComments:**
- [ ] `getComments()` - with null check and initialization
- [ ] `setComments(Set<CComment> comments)`

**If implements IHasStatusAndWorkflow:**
- [ ] `getEntityType()` - returns type entity
- [ ] `setEntityType(CTypeEntity<?> typeEntity)` - with validation
- [ ] `getWorkflow()` - returns workflow from type
  ```java
  @Override
  public CWorkflowEntity getWorkflow() {
      Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
      return entityType.getWorkflow();
  }
  ```

### ☐ 5. CopyTo Pattern Implementation (MANDATORY)

**ALL entities MUST implement copyEntityTo() method for copy/clone support:**

```java
/**
 * Copies entity fields to target entity.
 * MANDATORY: All entities must override this method.
 * 
 * @param target  The target entity
 * @param options Clone options to control copying behavior
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    // RULE 1: ALWAYS call parent first
    super.copyEntityTo(target, options);
    
    // RULE 2: Type-check and cast
    if (target instanceof C{YourEntity}) {
        final C{YourEntity} targetEntity = (C{YourEntity}) target;
        
        // RULE 3: Copy basic fields using copyField()
        copyField(this::getYourField1, targetEntity::setYourField1);
        copyField(this::getYourField2, targetEntity::setYourField2);
        
        // RULE 4: Handle unique/required fields specially
        // Make unique fields unique to avoid constraint violations
        if (this.getEmail() != null) {
            targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
        }
        if (this.getLogin() != null) {
            targetEntity.setLogin(this.getLogin() + "_copy");
        }
        
        // RULE 5: Handle dates based on options
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetEntity::setDueDate);
            copyField(this::getStartDate, targetEntity::setStartDate);
        }
        
        // RULE 6: Handle relations based on options
        if (options.includesRelations()) {
            copyField(this::getRelatedEntity, targetEntity::setRelatedEntity);
        }
        
        // RULE 7: Handle collections based on options
        if (options.includesRelations()) {
            copyCollection(this::getChildren, 
                (col) -> targetEntity.children = (Set<Child>) col, 
                true); // createNew = true
        }
        
        // RULE 8: DON'T copy sensitive fields (passwords, tokens, etc.)
        // RULE 9: DON'T copy auto-generated fields (IDs, audit fields)
        
        // RULE 10: Log for debugging
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

**Checklist:**
- [ ] Method present: `copyEntityTo(final CEntityDB<?> target, final CCloneOptions options)`
- [ ] Calls `super.copyEntityTo(target, options)` first
- [ ] Type-checks target before casting
- [ ] Copies ALL entity-specific fields
- [ ] Handles unique fields (appends suffix/prefix)
- [ ] Handles required fields (ensures not null)
- [ ] Respects copy options (dates, relations, status)
- [ ] Uses `copyField()` for simple fields
- [ ] Uses `copyCollection()` for collections
- [ ] Never copies sensitive data (passwords, etc.)
- [ ] Never copies auto-generated fields
- [ ] Includes debug logging
- [ ] Has proper JavaDoc

**Reference**: `docs/architecture/COPY_TO_PATTERN_CODING_RULE.md` for complete specification and examples.

### ☐ 6. Getters and Setters

**For Each Field:**
- [ ] Getter exists and follows naming convention
- [ ] Setter exists
- [ ] Setter calls `updateLastModified()` (inherited from base class)
  ```java
  public void setName(final String name) {
      this.name = name;
      updateLastModified();
  }
  ```

---

## Repository Checklist

### ☐ 1. Repository Interface

**File Location**: `src/main/java/tech/derbent/app/{module}/{entity}/service/I{Entity}Repository.java`

#### Structure:

```java
public interface I{Entity}Repository extends [BaseRepositoryInterface]<C{Entity}> {

    // MANDATORY: Override findById with JOIN FETCH
    @Override
    @Query("""
            SELECT e FROM #{#entityName} e
            LEFT JOIN FETCH e.field1
            LEFT JOIN FETCH e.attachments
            LEFT JOIN FETCH e.comments
            WHERE e.id = :id
            """)
    Optional<C{Entity}> findById(@Param("id") Long id);

    // Additional custom queries
}
```

#### Checklist:

- [ ] **Package**: `tech.derbent.plm.{module}.{entity}.service`
- [ ] **Interface Name**: `I{Entity}Repository`
- [ ] **Extends**: Correct base repository interface

**Base Repository Types:**

| Entity Type | Extends |
|-------------|---------|
| Project Item | `IProjectItemRespository<T>` |
| Project Entity | `IEntityOfProjectRepository<T>` |
| Company Entity | `IEntityOfCompanyRepository<T>` |
| Child Entity | `IChildEntityRepository<T, Master>` |
| Simple Entity | `IAbstractRepository<T>` |

### ☐ 2. Query Methods

#### MANDATORY: findById() Override

**CRITICAL: ALWAYS override findById() with JOIN FETCH for:**
- [ ] Attachments (if IHasAttachments)
- [ ] Comments (if IHasComments)
- [ ] Type entity and workflow (if IHasStatusAndWorkflow)
- [ ] Any LAZY fields needed in UI

**Pattern:**
```java
@Override
@Query("""
        SELECT e FROM #{#entityName} e
        LEFT JOIN FETCH e.project
        LEFT JOIN FETCH e.assignedTo
        LEFT JOIN FETCH e.createdBy
        LEFT JOIN FETCH e.entityType et
        LEFT JOIN FETCH et.workflow
        LEFT JOIN FETCH e.status
        LEFT JOIN FETCH e.attachments
        LEFT JOIN FETCH e.comments
        WHERE e.id = :id
        """)
Optional<C{Entity}> findById(@Param("id") Long id);
```

**Reference**: `docs/architecture/LAZY_LOADING_BEST_PRACTICES.md`

#### MANDATORY: Override List Methods (if applicable)

- [ ] **listByProject()** or **listByCompany()**: For paginated views
  ```java
  @Override
  @Query("""
          SELECT e FROM #{#entityName} e
          WHERE e.project = :project
          ORDER BY e.id DESC
          """)
  Page<C{Entity}> listByProject(@Param("project") CProject project, Pageable pageable);
  ```

- [ ] **listByProjectForPageView()**: For non-paginated views
  ```java
  @Override
  @Query("""
          SELECT e FROM #{#entityName} e
          WHERE e.project = :project
          ORDER BY e.id DESC
          """)
  List<C{Entity}> listByProjectForPageView(@Param("project") CProject project);
  ```

#### Query Standards:

- [ ] **Use Triple Quotes**: All queries use `"""` for multiline format
- [ ] **Use #{#entityName}**: Generic entity placeholder
- [ ] **ORDER BY**: Always include ordering clause
  - Named entities: `ORDER BY e.name ASC`
  - Regular entities: `ORDER BY e.id DESC`
  - Sprintable items: `ORDER BY e.sprintOrder ASC NULLS LAST, e.id DESC`

### ☐ 3. Child Entity Repository Pattern

**If this is a child entity (master-detail):**

- [ ] **Extends**: `IChildEntityRepository<T, MasterType>`
- [ ] **findByMaster()**: Get all children for a master
  ```java
  @Override
  @Query("""
          SELECT e FROM #{#entityName} e
          LEFT JOIN FETCH e.master
          WHERE e.master = :master
          ORDER BY e.itemOrder ASC
          """)
  List<C{Child}> findByMaster(@Param("master") C{Master} master);
  ```

- [ ] **findByMasterId()**: Get children by master ID
- [ ] **countByMaster()**: Count children
- [ ] **getNextItemOrder()**: Get next order number

**Reference**: `docs/architecture/CHILD_ENTITY_PATTERNS.md`

---

## Service Checklist

### ☐ 1. Service Class

**File Location**: `src/main/java/tech/derbent/app/{module}/{entity}/service/C{Entity}Service.java`

#### Structure:

```java
@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class C{Entity}Service extends [BaseService]<C{Entity}> {

    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}Service.class);
    private final I{Entity}Repository repository;

    public C{Entity}Service(final I{Entity}Repository repository,
                            final Clock clock,
                            final ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
    }

    @Override
    public Class<C{Entity}> getEntityClass() {
        return C{Entity}.class;
    }

    @Override
    public Class<?> getInitializerService() {
        return C{Entity}InitializerService.class;
    }
}
```

#### Checklist:

- [ ] **Package**: `tech.derbent.plm.{module}.{entity}.service`
- [ ] **Class Name**: `C{Entity}Service`
- [ ] **@Service**: Annotation present
- [ ] **@PreAuthorize**: Security annotation
- [ ] **@PermitAll**: Required annotation
- [ ] **Extends**: Correct base service class

**Base Service Types:**

| Entity Type | Extends |
|-------------|---------|
| Project Item | `CEntityOfProjectService<T>` |
| Project Entity | `CEntityOfProjectService<T>` |
| Company Entity | `CEntityOfCompanyService<T>` |
| Simple Entity | `CAbstractService<T>` |

- [ ] **Logger**: Static logger field
- [ ] **Repository**: Private final field
- [ ] **Constructor**: With repository, clock, sessionService
- [ ] **getEntityClass()**: Returns entity class
- [ ] **getInitializerService()**: Returns initializer class (or null for child entities)

### ☐ 2. Custom Service Methods (if needed)

**Common Custom Methods:**

- [ ] **Business Logic**: Domain-specific operations
- [ ] **Validation**: Custom validation methods
- [ ] **Calculations**: Computed values
- [ ] **Workflow**: Status transition logic

**Example:**
```java
@Transactional
public void startTest(C{Entity} entity, CUser executedBy) {
    Check.notNull(entity, "Entity cannot be null");
    entity.setExecutedBy(executedBy);
    entity.setExecutionStart(LocalDateTime.now());
    save(entity);
}
```

---

## Initializer Service Checklist

### ☐ 1. Initializer Class

**File Location**: `src/main/java/tech/derbent/app/{module}/{entity}/service/C{Entity}InitializerService.java`

#### MANDATORY Structure:

```java
public class C{Entity}InitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = C{Entity}.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}InitializerService.class);
    private static final String menuOrder = Menu_Order_PROJECT + ".XX";
    private static final String menuTitle = MenuTitle_PROJECT + ".{Entity Name}";
    private static final String pageDescription = "{Entity} management";
    private static final String pageTitle = "{Entity} Management";
    private static final boolean showInQuickToolbar = false;

    // 1. createBasicView() - MANDATORY
    public static CDetailSection createBasicView(final CProject project) throws Exception { }

    // 2. createGridEntity() - MANDATORY
    public static CGridEntity createGridEntity(final CProject project) { }

    // 3. initialize() - MANDATORY
    public static void initialize(final CProject project, ...) throws Exception { }

    // 4. initializeSample() - MANDATORY
    public static void initializeSample(final CProject project, final boolean minimal) throws Exception { }
}
```

#### Checklist:

- [ ] **Package**: `tech.derbent.plm.{module}.{entity}.service`
- [ ] **Class Name**: `C{Entity}InitializerService`
- [ ] **Extends**: `CInitializerServiceBase`
- [ ] **Constants**: All menu/page constants defined
- [ ] **menuOrder**: Correct menu position (see below)

**Menu Order Standards:**

| Entity Type | Menu Order |
|-------------|------------|
| Core Project Entities | `Menu_Order_PROJECT + ".0-9"` |
| Work Items | `Menu_Order_PROJECT + ".10-29"` |
| Test Entities | `Menu_Order_PROJECT + ".30-39"` |
| Configuration | `Menu_Order_PROJECT + ".40-49"` |
| Type Entities | `Menu_Order_TYPES + ".XX"` |

### ☐ 2. createBasicView() Method

**Purpose**: Define the detail form/view structure

#### Checklist:

- [ ] **Method Signature**: `public static CDetailSection createBasicView(final CProject project)`
- [ ] **Returns**: CDetailSection
- [ ] **Try-Catch**: Error handling with logger
- [ ] **createBaseScreenEntity()**: Call to create base section
- [ ] **CInitializerServiceNamedEntity.createBasicView()**: For named entities
- [ ] **Sections**: Organized into logical sections

**Standard Sections:**

```java
// Basic info - from CInitializerServiceNamedEntity.createBasicView()

// Entity-specific fields
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));

// Context section
detailSection.addScreenLine(CDetailLinesService.createSection("Context"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));

// Attachments section (if IHasAttachments)
tech.derbent.plm.attachments.service.CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);

// Comments section (if IHasComments)
tech.derbent.plm.comments.service.CCommentInitializerService.addCommentsSection(detailSection, clazz);

// Audit section
detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
```

**CRITICAL NOTES:**

- [ ] **Type Entities**: Do NOT include `createdBy` (only `createdDate` and `lastModifiedDate`)
- [ ] **Child Entities**: Include master entity reference field
- [ ] **Workflow Entities**: Include `entityType`, `status`, `workflow`

### ☐ 3. createGridEntity() Method

**Purpose**: Define grid column configuration

#### Checklist:

- [ ] **Method Signature**: `public static CGridEntity createGridEntity(final CProject project)`
- [ ] **Returns**: CGridEntity
- [ ] **createBaseGridEntity()**: Call to create base grid
- [ ] **setColumnFields()**: List of field names to display

**Standard Columns:**

```java
grid.setColumnFields(List.of(
    "id",                  // Always first
    "name",                // If named entity
    "description",         // If has description
    "entityType",          // If has type
    "status",              // If has workflow
    "priority",            // If has priority
    "assignedTo",          // If has assignment
    "project",             // If project-scoped
    "createdBy",           // For regular entities (NOT type entities)
    "createdDate"          // Always last
));
```

**Type Entity Columns (NO createdBy):**

```java
grid.setColumnFields(List.of("id", "name", "description", "color", "icon", 
                             "workflow", "company", "createdDate"));
```

### ☐ 4. initialize() Method

**Purpose**: Register entity with the system

#### Checklist:

- [ ] **Method Signature**: Correct parameters (project, gridEntityService, detailSectionService, pageEntityService)
- [ ] **Calls createBasicView()**: Create detail section
- [ ] **Calls createGridEntity()**: Create grid config
- [ ] **Calls initBase()**: Register with system

```java
public static void initialize(final CProject project,
                              final CGridEntityService gridEntityService,
                              final CDetailSectionService detailSectionService,
                              final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
             detailSection, grid, menuTitle, pageTitle, pageDescription,
             showInQuickToolbar, menuOrder);
}
```

### ☐ 5. initializeSample() Method

**Purpose**: Create sample data for demonstration

#### Checklist:

- [ ] **Method Signature**: `public static void initializeSample(final CProject project, final boolean minimal)`
- [ ] **Clears Existing Data**: Delete existing records (with try-catch)
- [ ] **Sample Data Array**: String[][] with name and description
- [ ] **Calls initializeProjectEntity()** or **initializeCompanyEntity()**
- [ ] **Lambda Consumer**: Customize sample data

**Pattern for Project-Scoped Entities:**

```java
public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
    // Get service
    final C{Entity}Service service = (C{Entity}Service) CSpringContext.getBean(
        CEntityRegistry.getServiceClassForEntity(clazz));

    // Clear existing
    final List<C{Entity}> existing = service.findAll();
    if (!existing.isEmpty()) {
        LOGGER.info("Clearing {} existing entities for project: {}", 
                    existing.size(), project.getName());
        for (final C{Entity} item : existing) {
            try {
                service.delete(item);
            } catch (final Exception e) {
                LOGGER.warn("Could not delete entity {}: {}", item.getId(), e.getMessage());
            }
        }
    }

    // Sample data
    final String[][] nameAndDescriptions = {
        { "Name 1", "Description 1" },
        { "Name 2", "Description 2" },
        { "Name 3", "Description 3" }
    };

    // Initialize
    initializeProjectEntity(nameAndDescriptions,
        (CEntityOfProjectService<?>) service,
        project,
        minimal,
        (item, index) -> {
            final C{Entity} entity = (C{Entity}) item;
            // Customize entity
            entity.setCustomField(value);
        });
}
```

**Pattern for Company-Scoped Entities (Types):**

```java
public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
    final String[][] nameAndDescriptions = {
        { "Type 1", "Description 1" },
        { "Type 2", "Description 2" }
    };
    initializeCompanyEntity(nameAndDescriptions,
        (CEntityOfCompanyService<?>) CSpringContext.getBean(
            CEntityRegistry.getServiceClassForEntity(clazz)),
        project.getCompany(),
        minimal,
        null);
}
```

---

## Page Service Checklist

### ☐ 1. Page Service Class (if needed)

**File Location**: `src/main/java/tech/derbent/app/{module}/{entity}/service/CPageService{Entity}.java`

**When Needed**:
- [ ] Entity has workflow (implements IHasStatusAndWorkflow)
- [ ] Entity has sprint support (implements ISprintableItem)
- [ ] Entity has Gantt display (implements IGanttDisplayable)
- [ ] Custom page logic required

#### Structure:

```java
public class CPageService{Entity} extends CPageServiceDynamicPage<C{Entity}>
        implements IPageServiceHasStatusAndWorkflow<C{Entity}> {

    Logger LOGGER = LoggerFactory.getLogger(CPageService{Entity}.class);
    Long serialVersionUID = 1L;

    private CProjectItemStatusService projectItemStatusService;

    public CPageService{Entity}(IPageServiceImplementer<C{Entity}> view) {
        super(view);
        try {
            projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize services", e);
        }
    }

    @Override
    public void bind() {
        try {
            LOGGER.debug("Binding page service for {}", C{Entity}.class.getSimpleName());
            Check.notNull(getView(), "View must not be null");
            super.bind();
        } catch (Exception e) {
            LOGGER.error("Error binding page service: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public CProjectItemStatusService getProjectItemStatusService() {
        return projectItemStatusService;
    }
}
```

#### Checklist:

- [ ] **Package**: `tech.derbent.plm.{module}.{entity}.service`
- [ ] **Class Name**: `CPageService{Entity}`
- [ ] **Extends**: `CPageServiceDynamicPage<T>`
- [ ] **Implements**: Appropriate interface(s)
  - `IPageServiceHasStatusAndWorkflow<T>` - for workflow entities
  - `ISprintItemPageService<T>` - for sprintable items
- [ ] **Logger**: Instance logger field
- [ ] **serialVersionUID**: Required field
- [ ] **Constructor**: Accepts view parameter
- [ ] **bind()**: Override if custom logic needed
- [ ] **Interface Methods**: All required methods implemented

---

## CDataInitializer Registration

### ☐ 1. Import Statements

**File**: `src/main/java/tech/derbent/api/config/CDataInitializer.java`

Add imports for all initializer services:

```java
import tech.derbent.plm.{module}.{entity}.service.C{Entity}InitializerService;
import tech.derbent.plm.{module}.{entitytype}.service.C{Entity}TypeInitializerService;
```

- [ ] **Initializer Import**: Added
- [ ] **Type Initializer Import**: Added (if has type entity)

### ☐ 2. System Initialization Phase

**Location**: Inside `loadSampleData()` method, project loop section

**Pattern**:
```java
// Around line 730-790
C{Entity}TypeInitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
C{Entity}InitializerService.initialize(project, gridEntityService, screenService, pageEntityService);
```

- [ ] **Type Initializer Call**: Added (if has type entity)
- [ ] **Entity Initializer Call**: Added
- [ ] **Correct Order**: Types before entities
- [ ] **Correct Location**: With similar entities

**Ordering Guidelines:**

1. Core System (Activities, Users, Companies, etc.)
2. Work Items (Decisions, Meetings, Orders)
3. Validation Management (Validation Case Types, Validation Suites, Validation Cases, Validation Sessions)
4. Configuration (Risks, Assets, Budgets, etc.)

### ☐ 3. Sample Data Phase

**Location**: Inside `loadSampleData()` method, sample data section

#### For Type Entities (Company-Scoped):

**Pattern** (around line 796-818):
```java
if (project.getId().equals(sampleProject.getId())) {
    // Types only for first project
    C{Entity}TypeInitializerService.initializeSample(sampleProject, minimal);
}
```

- [ ] **Type Sample Call**: Added inside `if (project.getId().equals(sampleProject.getId()))`
- [ ] **Company-Scoped**: Only called for first project

#### For Entity Data (Project-Scoped):

**Pattern** (around line 820-844):
```java
// Outside the if block, applies to all projects
C{Entity}InitializerService.initializeSample(project, minimal);
```

- [ ] **Entity Sample Call**: Added after types section
- [ ] **Correct Order**: After dependent entities
- [ ] **Project-Scoped**: Called for each project

**Dependency Order Examples:**
- Types before entities using those types
- Scenarios before test cases (test cases reference scenarios)
- Validation cases before validation sessions (sessions execute validation cases)
- Parent entities before child entities

### ☐ 4. Verification

- [ ] **Compilation**: Code compiles without errors
- [ ] **No Duplicates**: Initialization calls not duplicated
- [ ] **Proper Grouping**: Related entities grouped together
- [ ] **Comments**: Section comments maintained

---

## UI Components (If Needed)

### ☐ 1. Custom Components

**Only create if standard components are insufficient**

- [ ] **List Components**: For managing collections
- [ ] **Selection Components**: For picking related entities
- [ ] **Display Components**: For read-only views
- [ ] **Widget Components**: For dashboards

**Naming Convention**:
- `CComponentList{Entity}` - For list management
- `CComponentSelect{Entity}` - For selection dialogs
- `CComponent{Entity}Widget` - For dashboard widgets

**Reference**: `docs/development/component-coding-standards.md`

### ☐ 2. Views (if custom view needed)

**File Location**: `src/main/java/tech/derbent/app/{module}/{entity}/view/C{Entity}View.java`

- [ ] **Extends**: `CAbstractPage` or appropriate base
- [ ] **Route**: `@Route` annotation with correct path
- [ ] **Page Title**: `@PageTitle` annotation
- [ ] **Grid Configuration**: Uses CGrid<T>
- [ ] **CRUD Operations**: All implemented
- [ ] **Follows Naming Standards**: See `.copilot/instructions.md`

---

## Testing Checklist

### ☐ 1. Manual Testing

After implementation, manually test:

- [ ] **Application Starts**: No startup errors
- [ ] **Menu Item Visible**: Entity appears in correct menu
- [ ] **Grid Loads**: Entity list displays
- [ ] **Create Operation**: Can create new entity
- [ ] **Edit Operation**: Can edit existing entity
- [ ] **Delete Operation**: Can delete entity (if allowed)
- [ ] **Form Validation**: Required fields validated
- [ ] **Workflow Transitions**: Status changes work (if applicable)
- [ ] **Attachments**: Can add/view attachments (if IHasAttachments)
- [ ] **Comments**: Can add/view comments (if IHasComments)
- [ ] **Sample Data**: Sample entities created on initialization

### ☐ 2. Database Verification

- [ ] **Table Created**: Entity table exists in database
- [ ] **Columns Correct**: All fields have columns
- [ ] **Constraints**: Foreign keys and constraints correct
- [ ] **Sample Data**: Sample records inserted

### ☐ 3. Playwright Tests (if applicable)

**File Location**: `src/test/java/automated_tests/tech/derbent/ui/automation/C{Entity}PlaywrightTest.java`

- [ ] **Navigation Test**: Can navigate to entity page
- [ ] **CRUD Tests**: Create, read, update, delete operations
- [ ] **Grid Tests**: Grid loads and displays data
- [ ] **Form Tests**: Form validation and submission

**Reference**: `docs/testing/PLAYWRIGHT_TESTING_GUIDE.md`

---

## Documentation Checklist

### ☐ 1. Code Documentation

- [ ] **JavaDoc**: Class-level documentation
- [ ] **Method Comments**: For complex methods
- [ ] **Field Comments**: For non-obvious fields
- [ ] **Interface Implementation**: Document interface methods

### ☐ 2. Implementation Notes

Create markdown file if entity is complex:

**File**: `docs/implementation/{ENTITY}_IMPLEMENTATION.md`

Include:
- [ ] **Purpose**: What problem does this entity solve?
- [ ] **Relationships**: How does it relate to other entities?
- [ ] **Workflows**: Status transitions and business rules
- [ ] **Sample Data**: What sample data is created?
- [ ] **UI Patterns**: Any custom UI components
- [ ] **Testing Notes**: How to test the entity

### ☐ 3. Update Main Documentation

If entity introduces new patterns:

- [ ] Update `docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md`
- [ ] Update this checklist if new patterns discovered
- [ ] Update `.copilot/instructions.md` if affects AI instructions

---

## Final Verification

### ☐ 1. Complete Build and Test

```bash
# Setup environment
source ./bin/setup-java-env.sh

# Full clean build
mvn clean compile -DskipTests

# Run with H2 database
mvn spring-boot:run -Dspring.profiles.active=h2

# Run Playwright tests (in separate terminal)
./run-playwright-tests.sh menu
```

- [ ] **Build Success**: No compilation errors
- [ ] **Application Starts**: No startup errors
- [ ] **Tests Pass**: Playwright tests pass
- [ ] **No Warnings**: Address any critical warnings

### ☐ 2. Code Quality Checks

- [ ] **C-Prefix**: All custom classes use C-prefix
- [ ] **Imports**: Use short class names, not full paths
- [ ] **Formatting**: Code formatted (eclipse-formatter.xml)
- [ ] **No TODO**: All TODOs resolved or documented
- [ ] **No Debug Code**: Remove debug statements
- [ ] **Logging**: Appropriate log levels used

### ☐ 3. Git Commit

```bash
# Stage changes
git add -A

# Commit with conventional commit format
git commit -m "feat: add {Entity} entity with full CRUD and sample data

- Domain entity C{Entity} with validation
- Repository with optimized queries
- Service with business logic
- Initializer with sample data generation
- Page service for workflow (if applicable)
- CDataInitializer registration
- Documentation

All patterns followed per NEW_ENTITY_COMPLETE_CHECKLIST.md"
```

- [ ] **Conventional Commit**: Use proper commit type (feat/fix/refactor)
- [ ] **Descriptive Message**: Clear what was added
- [ ] **List Changes**: Bulleted list of components
- [ ] **Reference Checklist**: Mention this document

### ☐ 4. Review Checklist

**Go through this checklist again and verify:**
- [ ] **All checkboxes checked**: Nothing skipped
- [ ] **All files created**: Domain, repository, service, initializer
- [ ] **All registrations done**: CDataInitializer updated
- [ ] **All tests pass**: Manual and automated tests
- [ ] **Documentation complete**: Code and markdown docs

---

## Common Mistakes to Avoid

### ❌ CRITICAL MISTAKES:

1. **Missing createdBy on Type Entities**
   - CTypeEntity does NOT have createdBy
   - Only use createdDate and lastModifiedDate

2. **Missing Attachments/Comments JOIN FETCH**
   - Always add LEFT JOIN FETCH for attachments and comments in findById()
   - Prevents LazyInitializationException

3. **Missing Services for Result/Child Entities**
   - Child entities still need services (e.g., CValidationCaseResultService)
   - Even if they don't have initializers

4. **Wrong Base Class**
   - Use decision tree to choose correct base class
   - Don't use CEntityDB when CProjectItem is needed

5. **Missing CDataInitializer Registration**
   - Must register both initialize() and initializeSample()
   - Must add imports

6. **Wrong Menu Order**
   - Follow menu order conventions
   - Types in TYPES section, entities in PROJECT section

7. **Missing Interface Implementations**
   - IHasAttachments requires getAttachments() and setAttachments()
   - IHasComments requires getComments() and setComments()
   - IHasStatusAndWorkflow requires getEntityType(), setEntityType(), getWorkflow()

8. **Missing updateLastModified()**
   - All setters must call updateLastModified()
   - Inherited from base class

9. **No ORDER BY in Queries**
   - All list queries must have ORDER BY
   - Use appropriate ordering (name ASC or id DESC)

10. **Missing Constants**
    - All 5 constants are MANDATORY
    - Used by CEntityRegistry and UI components

---

## Pattern Summary by Entity Type

### CTypeEntity (Company-Scoped)

**Extends**: `CTypeEntity<T>`  
**Examples**: CActivityType, CValidationCaseType  
**Has**: name, description, color, icon, workflow, company  
**Does NOT Have**: createdBy, attachments, comments

**Key Files**:
- Domain: `C{Entity}Type`
- Repository: `I{Entity}TypeRepository` (extends IEntityOfCompanyRepository)
- Service: `C{Entity}TypeService` (extends CEntityOfCompanyService)
- Initializer: `C{Entity}TypeInitializerService`
- PageService: `CPageService{Entity}Type`

**Initializer Pattern**:
```java
// No createdBy in audit section
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

// Use initializeCompanyEntity in initializeSample()
```

### CProjectItem (Work Item)

**Extends**: `CProjectItem<T>`  
**Implements**: `IHasStatusAndWorkflow<T>`, `IHasAttachments`, `IHasComments`  
**Examples**: CActivity, CValidationCase, CMeeting  
**Has**: entityType, status, workflow, assignedTo, attachments, comments

**Key Files**:
- Domain: `C{Entity}`
- Type Domain: `C{Entity}Type`
- Repository: `I{Entity}Repository` (extends IProjectItemRepository)
- Service: `C{Entity}Service` (extends CEntityOfProjectService)
- Initializer: `C{Entity}InitializerService`
- Type Initializer: `C{Entity}TypeInitializerService`
- PageService: `CPageService{Entity}` (implements IPageServiceHasStatusAndWorkflow)

**Initializer Pattern**:
```java
// Include attachments and comments sections
CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
CCommentInitializerService.addCommentsSection(detailSection, clazz);

// Include createdBy in audit section
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));

// Use initializeProjectEntity in initializeSample()
```

### CEntityOfProject (Non-Work Item)

**Extends**: `CEntityOfProject<T>`  
**Examples**: CProject, CValidationSuite, CValidationSession  
**May Implement**: `IHasAttachments`, `IHasComments` (optional)

**Similar to CProjectItem but without workflow**

### Child Entity (Master-Detail)

**Extends**: `CEntityDB<T>`  
**Examples**: CValidationStep, CSprintItem  
**Has**: Master entity reference, order field

**Key Files**:
- Domain: `C{ChildEntity}`
- Repository: `I{ChildEntity}Repository` (extends IChildEntityRepository)
- Service: `C{ChildEntity}Service` (extends CAbstractService)
- No Initializer (created via master entity)
- No PageService typically

**Repository Pattern**:
```java
public interface I{Child}Repository extends IChildEntityRepository<C{Child}, C{Master}> {
    @Override
    @Query("...")
    List<C{Child}> findByMaster(@Param("master") C{Master} master);
    
    // Standard child methods...
}
```

---

## Quick Reference: File Checklist

**For a typical project item entity with type:**

```
✅ Domain Classes (2):
   □ C{Entity}.java
   □ C{Entity}Type.java

✅ Repositories (2):
   □ I{Entity}Repository.java
   □ I{Entity}TypeRepository.java

✅ Services (2):
   □ C{Entity}Service.java
   □ C{Entity}TypeService.java

✅ Initializers (2):
   □ C{Entity}InitializerService.java
   □ C{Entity}TypeInitializerService.java

✅ Page Services (2):
   □ CPageService{Entity}.java
   □ CPageService{Entity}Type.java

✅ Registration:
   □ CDataInitializer.java (imports + 2 system calls + 2 sample calls)

✅ Documentation:
   □ {ENTITY}_IMPLEMENTATION.md (optional)

Total: 11-13 files
```

---

## Version History

- **v2.0** (2026-01-17): Complete rewrite based on test module implementation lessons
- **v1.0** (2026-01-15): Initial checklist

---

## Related Documentation

- [Entity Inheritance and Design Patterns](ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md)
- [Lazy Loading Best Practices](LAZY_LOADING_BEST_PRACTICES.md)
- [Child Entity Patterns](CHILD_ENTITY_PATTERNS.md)
- [Coding Standards](coding-standards.md)
- [Component Coding Standards](../development/component-coding-standards.md)
- [GitHub Copilot Guidelines](../development/copilot-guidelines.md)

---

**Last Updated**: 2026-01-17  
**Status**: Complete and Verified  
**Next Review**: After next entity implementation
