# Derbent Coding Standards - Complete Reference

**Version**: 2.0
**Last Updated**: 2026-01-18
**Target Audience**: Developers, AI Agents (GitHub Copilot)

## Overview

This document consolidates ALL coding standards, patterns, and rules for the Derbent project. It replaces multiple scattered documents with a single authoritative source.

## Table of Contents

1. [Core Coding Standards](#core-coding-standards)
2. [One-to-One Composition Pattern](#one-to-one-composition-pattern)
3. [Entity Patterns](#entity-patterns)
4. [UI and Copy Patterns](#ui-and-copy-patterns)
5. [Repository and Query Patterns](#repository-and-query-patterns)
6. [Service Layer Patterns](#service-layer-patterns)
7. [Testing Standards](#testing-standards)
8. [AI Agent Guidelines](#ai-agent-guidelines)

---

## Core Coding Standards

### Naming Conventions

**C-Prefix Rule (MANDATORY)**:
- All concrete classes start with `C`: `CActivity`, `CActivityService`, `CActivityView`
- Interfaces use `I*` prefix: `IActivityRepository`, `ISprintableItem`
- Tests use `C*Test` suffix: `CActivityTest`, `CActivityServiceTest`

**Constants**:
- `static final` fields in `SCREAMING_SNAKE_CASE`
- Entity constants: `ENTITY_TITLE_SINGULAR`, `ENTITY_TITLE_PLURAL`, `VIEW_NAME`, `DEFAULT_COLOR`, `DEFAULT_ICON`

**Field Naming**:
- Use exact entity field names in screens/grids
- Field names must match getters/setters for reflection-based metadata
- No aliases or alternate names

### Type Safety

**No Raw Types (MANDATORY)**:
```java
// ✅ CORRECT
List<CActivity> activities = new ArrayList<>();
CProjectItem<CActivity> item = activity;

// ❌ WRONG
List activities = new ArrayList();
CProjectItem item = activity;
```

**Generics Usage**:
- Always specify type parameters
- Use bounded wildcards when appropriate: `<? extends CEntity>`
- Maintain type safety through the stack

### Validation and Checks

**Fail Fast (MANDATORY)**:
```java
// ✅ CORRECT - Fail fast with informative error
Check.notNull(entity, "Entity cannot be null");
Check.notNull(entity.getId(), "Entity must be persisted");

// ❌ WRONG - Silent failure
if (entity == null) return;
if (entity.getId() == null) return;
```

**Validation Methods**:
- `Check.notNull(obj, message)` - Null checks
- `Check.instanceOf(obj, clazz, message)` - Type checks
- `Objects.requireNonNull(obj, message)` - Standard library option

### Logging

**Console Format (MANDATORY)**:
```properties
# application.properties
spring.output.ansi.enabled=ALWAYS
logging.pattern.console=%clr(%d{HH:mm:ss.SSS}){magenta} %clr(%-5level) %clr([%t]){cyan} %clr(%logger{36}){cyan} %clr(:){red} %msg%n
```

**Logging Levels**:
- `LOGGER.error(message, exception)` - Errors with stack trace
- `LOGGER.warn(message, args...)` - Warnings (e.g., data issues)
- `LOGGER.info(message, args...)` - Important operations
- `LOGGER.debug(message, args...)` - Detailed diagnostics

**Log Once Rule**:
- Exception handlers log once, then rethrow or convert to UI notification
- Service methods log once at entry point
- No duplicate logging up the call stack

---

## One-to-One Composition Pattern

### Overview

Framework for entities owned via `@OneToOne CASCADE.ALL` (like CSprintItem, CAgileParentRelation).

### Base Classes

#### COneToOneRelationBase<T>

**Location**: `tech.derbent.api.domains.COneToOneRelationBase`

**Provides**:
- `@Transient CProjectItem<?> ownerItem` - Back-reference to owner
- `getOwnerItem()` - Access owner with validation
- `setOwnerItem(item)` - Set back-reference
- `toString()` - Standard implementation

**Usage**:
```java
@Entity
@Table(name = "my_relation")
public class CMyRelation extends COneToOneRelationBase<CMyRelation> {
    
    @ManyToOne
    private CReferencedEntity reference;
    
    // Only domain-specific fields needed
    // ownerItem management inherited!
}
```

#### COneToOneRelationServiceBase<T>

**Location**: `tech.derbent.api.domains.COneToOneRelationServiceBase`

**Provides**:
- `validateOwnership(entity, interfaceClass)` - Interface validation
- `validateNotSelfReference(id1, id2, message)` - Prevent self-references
- `validateSameProject(entity1, entity2)` - Same-project constraint
- `logOperation(operation, name, id)` - Standard logging
- `logWarning(message, args)` - Warning logging

**Usage**:
```java
@Service
public class CMyRelationService extends COneToOneRelationServiceBase<CMyRelation> {
    
    @Override
    protected Class<CMyRelation> getEntityClass() {
        return CMyRelation.class;
    }
    
    @Transactional
    public void setReference(CProjectItem<?> entity, CReferencedEntity ref) {
        validateOwnership(entity, IHasMyRelation.class);
        IHasMyRelation hasRel = (IHasMyRelation) entity;
        
        if (ref != null) {
            validateSameProject(entity, ref);
        }
        
        hasRel.getMyRelation().setReference(ref);
        logOperation("Set reference", entity.getName(), entity.getId());
    }
}
```

### 4-Step Recipe: Adding to Any Entity

**Step 1: Create relation entity**:
```java
@Entity
@Table(name = "c{name}_relation")
public class C{Name}Relation extends COneToOneRelationBase<C{Name}Relation> {
    // Domain-specific fields only
}
```

**Step 2: Create service**:
```java
@Service
public class C{Name}RelationService extends COneToOneRelationServiceBase<C{Name}Relation> {
    // Validation helpers inherited
}
```

**Step 3: Create interface**:
```java
public interface IHas{Name}Relation {
    C{Name}Relation get{Name}Relation();
    void set{Name}Relation(C{Name}Relation relation);
}
```

**Step 4: Add to target entity**:
```java
@Entity
public class CMyEntity implements IHas{Name}Relation {
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private C{Name}Relation {name}Relation;
    
    // Initialize in constructor + @PostLoad
    public CMyEntity() {
        if ({name}Relation == null) {
            {name}Relation = C{Name}RelationService.createDefault{Name}Relation();
            {name}Relation.setOwnerItem(this);
        }
    }
    
    @PostLoad
    protected void ensureBackReferences() {
        if ({name}Relation != null) {
            {name}Relation.setOwnerItem(this);
        }
    }
}
```

### Lifecycle Rules (MANDATORY)

**NEVER delete owned entities directly**:
```java
// ✅ CORRECT - Modify properties
entity.getOwnedRelation().setProperty(value);
entityService.save(entity);  // Cascades to owned

// ❌ WRONG - These violate ownership
entity.setOwnedRelation(new COwned());  // Creates orphan
ownedService.delete(ownedEntity);       // Deletes owner!
entity.setOwnedRelation(null);          // Orphans entity
```

---

## Entity Patterns

### Entity Initialization

**Constructor Pattern (MANDATORY)**:
```java
public CActivity() {
    super();
    // Initialize owned entities
    if (sprintItem == null) {
        sprintItem = CSprintItemService.createDefaultSprintItem();
        sprintItem.setParentItem(this);
    }
    if (agileParentRelation == null) {
        agileParentRelation = CAgileParentRelationService.createDefaultAgileParentRelation();
        agileParentRelation.setOwnerItem(this);
    }
}
```

**PostLoad Pattern (MANDATORY)**:
```java
@PostLoad
protected void ensureBackReferences() {
    if (sprintItem != null) {
        sprintItem.setParentItem(this);
    }
    if (agileParentRelation != null) {
        agileParentRelation.setOwnerItem(this);
    }
}
```

### CopyTo Pattern (MANDATORY)

All entities MUST implement `copyEntityTo(target, options)`:

```java
@Override
public void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    // Type check
    if (!(target instanceof CActivity)) {
        throw new IllegalArgumentException("Target must be CActivity");
    }
    CActivity targetActivity = (CActivity) target;
    
    // Copy entity-specific fields
    copyField(this::getDescription, targetActivity::setDescription);
    copyField(this::getPriority, targetActivity::setPriority);
    
    // Make unique fields unique
    if (getEmail() != null) {
        targetActivity.setEmail(getEmail() + "_copy@domain.com");
    }
    
    // Respect options
    if (options.includesRelations()) {
        copyCollection(this::getAttachments, targetActivity::setAttachments, true);
    }
    
    // Never copy sensitive data (passwords, tokens, etc.)
}
```

**Rules**:
- Always call `super.copyEntityTo()` first
- Type-check target before casting
- Use `copyField()` and `copyCollection()` helpers
- Make unique fields unique (email, username, etc.)
- Respect `CCloneOptions` flags
- Never copy sensitive data

### Metadata Annotations

**@AMetaData (MANDATORY)**:
```java
@Column(name = "description")
@Size(max = 2000)
@AMetaData(
    displayName = "Description",
    required = false,
    readOnly = false,
    description = "Detailed description of the activity",
    hidden = false,
    maxLength = 2000,
    order = 20
)
private String description;
```

**Required Fields**:
- `displayName` - User-friendly label
- `order` - Display order in forms (10, 20, 30...)
- `description` - Help text
- `required` - true/false
- `hidden` - Show in UI or hide

---

## UI and Copy Patterns

### Dialog Layouts (MANDATORY)

**Max-Width and Responsive**:
```java
dialog.setMaxWidth("600px");
dialog.setWidthFull();  // Responsive on mobile
```

**Custom Gap Spacing (NOT default)**:
```java
VerticalLayout layout = new VerticalLayout();
layout.setSpacing(false);  // Disable default spacing
layout.getStyle().set("gap", "12px");  // Section gap
```

**Two-Column for Checkboxes (6+ items)**:
```java
HorizontalLayout twoColumn = new HorizontalLayout();
twoColumn.setWidthFull();

VerticalLayout column1 = new VerticalLayout();
VerticalLayout column2 = new VerticalLayout();

// Split checkboxes evenly
for (int i = 0; i < checkboxes.size(); i++) {
    if (i < checkboxes.size() / 2) {
        column1.add(checkboxes.get(i));
    } else {
        column2.add(checkboxes.get(i));
    }
}

twoColumn.add(column1, column2);
```

### Entity Type Selection

**Use CEntityRegistry (MANDATORY)**:
```java
CComboBox<String> entityTypeCombo = new CComboBox<>("Entity Type");
entityTypeCombo.setItems(CEntityRegistry.getAllRegisteredEntityKeys());
entityTypeCombo.setItemLabelGenerator(key -> 
    CEntityRegistry.getEntityTitleSingular(key)
);
```

**"Same as Source" for Copy/Move**:
```java
List<String> items = new ArrayList<>();
items.add("Same as Source");
items.addAll(CEntityRegistry.getAllRegisteredEntityKeys());
entityTypeCombo.setItems(items);
```

### Unique Name Generation

**Auto-Name Pattern (MANDATORY)**:
```java
// Use service.newEntity() to trigger auto-name
CActivity newActivity = activityService.newEntity(project);
// Name will be "Activity01", "Activity02", etc.

// Update name when target type changes
entityTypeCombo.addValueChangeListener(e -> {
    String entityKey = e.getValue();
    if (!"Same as Source".equals(entityKey)) {
        CProjectItem<?> newEntity = serviceRegistry.getService(entityKey).newEntity(project);
        // newEntity has unique name
    }
});
```

**NEVER**:
- Manual concatenation with timestamps
- Copy name without making unique
- Hardcoded numbers

### Entity Initialization Order (MANDATORY)

```java
// 1. Create or copy
CActivity newActivity = new CActivity("name", project);

// 2. Set entity type
newActivity.setEntityType(activityType);

// 3. Initialize with service
activityService.initializeNewEntity(newActivity);

// 4. Customize specific fields
newActivity.setDescription("Custom description");

// 5. Save
CActivity saved = activityService.save(newActivity);

// 6. Navigate
CDynamicPageRouter.navigateToEntity(saved);
```

**initializeNewEntity() sets**:
- Status (workflow initial status)
- Workflow reference
- Audit fields (created date, user)
- Project/company context
- Unique name (if not set)

---

## Repository and Query Patterns

### Query Annotations

**JPQL Queries (Preferred)**:
```java
@Query("SELECT e FROM CActivity e WHERE e.project.id = :projectId")
List<CActivity> findByProjectId(@Param("projectId") Long projectId);

@Query("SELECT e FROM CActivity e WHERE e.parent.id = :parentId")
List<CActivity> findChildren(@Param("parentId") Long parentId);
```

**Native Queries (Only When Necessary)**:
```java
@Query(value = "WITH RECURSIVE descendants AS (" +
    "  SELECT id FROM cactivity WHERE id = :activityId " +
    "  UNION ALL " +
    "  SELECT a.id FROM cactivity a " +
    "  INNER JOIN descendants d ON a.parent_id = d.id" +
    ") SELECT DISTINCT id FROM descendants",
    nativeQuery = true)
List<Long> findAllDescendantIds(@Param("activityId") Long activityId);
```

**Method Name Queries (Simple Cases)**:
```java
List<CActivity> findByName(String name);
List<CActivity> findByProjectAndActive(CProject project, Boolean active);
Optional<CActivity> findByProjectAndName(CProject project, String name);
```

### Query Best Practices

**1. Use Explicit Joins**:
```java
// ✅ CORRECT
@Query("SELECT e FROM CActivity e JOIN FETCH e.project WHERE e.id = :id")

// ❌ WRONG (lazy loading issues)
@Query("SELECT e FROM CActivity e WHERE e.id = :id")
```

**2. Parameter Naming**:
```java
// ✅ CORRECT - Named parameters
@Query("SELECT e FROM CActivity e WHERE e.name = :name AND e.project.id = :projectId")
List<CActivity> findByNameAndProject(@Param("name") String name, @Param("projectId") Long projectId);

// ❌ WRONG - Positional parameters
@Query("SELECT e FROM CActivity e WHERE e.name = ?1 AND e.project.id = ?2")
```

**3. Null Handling**:
```java
// ✅ CORRECT - Explicit NULL check
@Query("SELECT e FROM CActivity e WHERE e.parentActivity IS NULL")
List<CActivity> findRootActivities();

// ✅ CORRECT - COALESCE for defaults
@Query("SELECT COALESCE(MAX(e.order), 0) FROM CActivity e WHERE e.project = :project")
```

**4. Count Queries**:
```java
@Query("SELECT COUNT(e) FROM CActivity e WHERE e.parent.id = :parentId")
long countChildren(@Param("parentId") Long parentId);
```

---

## Service Layer Patterns

### Stateless Service Rule (MANDATORY)

Services are singletons - NO mutable instance state:

```java
// ✅ CORRECT
@Service
public class CActivityService extends CAbstractService<CActivity> {
    
    private final IActivityRepository repository;  // ✅ Injected dependency
    private static final Logger LOGGER = ...;      // ✅ Logger
    
    @Transactional
    public CActivity createActivity(String name, CProject project) {
        // Get context per method call
        CCompany company = sessionService.getCurrentCompany();
        // ...
    }
}

// ❌ WRONG
@Service
public class CActivityService {
    private CCompany currentCompany;  // ❌ Mutable state!
    private List<CActivity> cache;    // ❌ User-specific cache!
}
```

**User/Project Context**:
- Retrieved per method via `ISessionService`
- Never stored in service fields
- See `docs/architecture/multi-user-singleton-advisory.md`

### Transaction Annotations

**Service Entry Points (MANDATORY)**:
```java
@Transactional
public CActivity save(CActivity activity) {
    // Write operations
}

@Transactional(readOnly = true)
public CActivity findById(Long id) {
    // Read-only operations
}
```

### Access Control

**Method-Level Security (MANDATORY)**:
```java
@PreAuthorize("hasRole('USER')")
@Transactional
public CActivity createActivity(...) {
    // Only users with USER role can call
}

@RolesAllowed({"ADMIN", "PROJECT_MANAGER"})
@Transactional
public void deleteActivity(Long id) {
    // Only admins and project managers
}
```

---

## Testing Standards

### Unit Test Structure

**Naming Convention**:
- Test class: `C{Entity}Test`
- Test method: `test{MethodName}_{Scenario}_{ExpectedResult}`

**Example**:
```java
public class CActivityServiceTest {
    
    @Test
    void testCreateActivity_WithValidData_ReturnsActivity() {
        // Arrange
        CProject project = new CProject("Test Project");
        String name = "Test Activity";
        
        // Act
        CActivity result = activityService.createActivity(name, project);
        
        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(project, result.getProject());
    }
    
    @Test
    void testCreateActivity_WithNullProject_ThrowsException() {
        // Arrange
        String name = "Test Activity";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            activityService.createActivity(name, null);
        });
    }
}
```

### UI Automation Tests

**Playwright Structure**:
```java
@ExtendWith(PlaywrightExtension.class)
public class CActivityUITest extends CBaseUITest {
    
    @Test
    void testCreateActivity_ThroughUI_Success() {
        // Login
        loginAsUser("user@company", "password");
        
        // Navigate
        navigateTo("activities");
        
        // Fill form
        page.fill("#activity-name", "Test Activity");
        page.selectOption("#activity-type", "Development");
        
        // Submit
        page.click("button:has-text('Save')");
        
        // Verify
        assertVisible("text=Test Activity");
        assertVisible("text=Development");
    }
}
```

**Selectors (MANDATORY)**:
- Use stable IDs: `#custom-username-input`
- Use semantic selectors: `button:has-text("Save")`
- Avoid brittle XPath
- No `Thread.sleep()` - use Playwright waits

---

## AI Agent Guidelines

### For GitHub Copilot

**MANDATORY Rules**:

1. **Always read existing implementations** before generating code
2. **Follow the closest existing example** in the codebase
3. **Use existing base classes** (COneToOneRelationBase, CAbstractService, etc.)
4. **Never create parallel abstractions** if one exists
5. **Reuse components** (CGrid, CPageService, CEntityFormBuilder, etc.)

**Pattern Recognition**:
- If you see `CActivity`, look for similar patterns in other entities
- If implementing a relation, check if base class exists
- If creating a service, extend the appropriate base class
- If creating UI, reuse existing components

**Code Generation**:
```java
// ✅ CORRECT - Extends base, minimal code
@Entity
public class CMyRelation extends COneToOneRelationBase<CMyRelation> {
    // Only domain-specific fields
}

// ❌ WRONG - Duplicates base class code
@Entity
public class CMyRelation extends CEntityDB<CMyRelation> {
    @Transient
    private CProjectItem<?> ownerItem;  // Already in base!
    
    public CProjectItem<?> getOwnerItem() { ... }  // Already in base!
}
```

### Conservative Decision Making

**If Unsure**:
1. Choose the most conservative option
2. Document your reasoning
3. Don't leave TODOs
4. Explain assumptions made

**Example**:
```java
// Conservative choice: Using existing CActivity as parent type
// Rationale: Maintains clear hierarchy (only activities can be parents)
// Alternative considered: Generic parent type - rejected for type safety
@ManyToOne
private CActivity parentActivity;
```

---

## Quick Reference

### One-to-One Composition Checklist

- [ ] Entity extends `COneToOneRelationBase<T>`
- [ ] Service extends `COneToOneRelationServiceBase<T>`
- [ ] Interface defines get/set methods
- [ ] Owner has `@OneToOne(CASCADE.ALL, orphanRemoval=true)`
- [ ] Constructor initializes and sets back-reference
- [ ] @PostLoad sets back-reference
- [ ] Factory method: `createDefault{Name}Relation()`

### Entity Implementation Checklist

- [ ] Extends appropriate base class (CProjectItem, CEntityDB, etc.)
- [ ] Implements required interfaces
- [ ] Has proper @AMetaData annotations
- [ ] Implements copyEntityTo() method
- [ ] Initializes owned entities in constructor
- [ ] Sets back-references in @PostLoad
- [ ] Has proper workflow integration
- [ ] Repository extends IAbstractRepository

### Entity-to-Entity Relation Checklist (Field Relations)

When implementing entities with references to other entities (e.g., CLink referencing target entities):

**Grid Display:**
- [ ] Use `CLabelEntity` for entity columns with color badges
- [ ] Use `addComponentColumn()` with `CLabelEntity` constructor
- [ ] Handle null entities gracefully with fallback text
- [ ] For status/type entities: show with color badge
- [ ] For user entities: use `CLabelEntity.createUserLabel()` with avatar
- [ ] Provide helper methods to fetch related entities (e.g., `getTargetEntity()`)

**Edit Dialog:**
- [ ] Load full entity from database before opening edit dialog
- [ ] Use `CComponentEntitySelection` for entity type + grid selection
- [ ] Implement `restoreTargetSelection()` to pre-select in edit mode
- [ ] Add comprehensive error logging for entity loading failures
- [ ] Show user-friendly error messages when entities can't be loaded

**Grid Selection:**
- [ ] Add `GridVariant.LUMO_ROW_STRIPES` for alternating rows
- [ ] Set custom selection highlight color via CSS variable
- [ ] Ensure selected row is visually distinct (50% opacity recommended)

**Testing:**
- [ ] Test all CRUD operations (Add, Edit, Delete)
- [ ] Test grid selection visual feedback
- [ ] Test entity details expansion/collapse
- [ ] Test error scenarios (missing entities, invalid references)

**Example Implementation (CLink):**
```java
// Grid column with color-aware entity rendering
grid.addComponentColumn(link -> {
    try {
        final CEntityDB<?> targetEntity = getTargetEntity(link);
        if (targetEntity != null) {
            return new CLabelEntity(targetEntity); // Shows with color badge
        }
        return new CLabelEntity("Unknown");
    } catch (final Exception e) {
        LOGGER.debug("Could not render target entity: {}", e.getMessage());
        return new CLabelEntity("");
    }
}).setHeader("Target Entity").setWidth("200px");

// Edit dialog with entity refresh
final CLink refreshedLink = linkService.findById(selected.getId())
    .orElseThrow(() -> new IllegalStateException("Link not found"));
final CDialogLink dialog = new CDialogLink(linkService, sessionService, refreshedLink, ...);
```

### UI Component Checklist

- [ ] Max-width 600px for dialogs
- [ ] Custom gap spacing (not default)
- [ ] Entity type selection uses CEntityRegistry
- [ ] Unique name generation via service.newEntity()
- [ ] Proper initialization order (create → init → customize → save → navigate)
- [ ] Proper error handling with CNotificationService

---

## Related Documentation

For detailed implementation:
- **Service Patterns**: `docs/architecture/service-layer-patterns.md`
- **View Patterns**: `docs/architecture/view-layer-patterns.md`
- **Testing**: `docs/testing/playwright-testing-guide.md`
- **Multi-User**: `docs/architecture/multi-user-singleton-advisory.md`

For specific features:
- **Agile Hierarchy**: `AGILE_PARENT_RELATION_IMPLEMENTATION.md`
- **Generic Framework**: `GENERIC_ONE_TO_ONE_COMPOSITION_FRAMEWORK.md`

---

**Version History**:
- 2.1 (2026-01-22): Added Entity-to-Entity Relation guidelines with CLink example
- 2.0 (2026-01-18): Consolidated all coding rules into single document
- 1.x: Multiple scattered documents (deprecated)
