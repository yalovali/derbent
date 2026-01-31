# Entity Inheritance and Design Patterns

## Overview

This document provides comprehensive guidance on entity inheritance, interface implementation, lazy loading patterns, and design decisions for the Derbent project. It captures patterns from recent development work and establishes rules for consistent implementation.

---

## Table of Contents

1. [Entity Inheritance Decision Tree](#entity-inheritance-decision-tree)
2. [Interface vs Inheritance Guidelines](#interface-vs-inheritance-guidelines)
3. [Lazy Loading Patterns](#lazy-loading-patterns)
4. [Progress Indicators and Story Points](#progress-indicators-and-story-points)
5. [Multi-Value Persistence Pattern](#multi-value-persistence-pattern)
6. [Company-Scoped vs Project-Scoped Entities](#company-scoped-vs-project-scoped-entities)
7. [Testing Requirements](#testing-requirements)

---

## Entity Inheritance Decision Tree

### When to Choose Base Classes

Use this decision tree when creating new entities:

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

### Inheritance Hierarchy Reference

```
CEntity<T>                          # Root - generic type safety, logging
    ↓
CEntityDB<T>                        # Adds: id, isActive, equals/hashCode
    ↓
CEntityNamed<T>                     # Adds: name, description, color, icon
    ↓
    ├─→ CEntityOfCompany<T>        # Adds: company association (workflows, roles, types)
    │       │
    │       └─→ CRole              # Example: company-scoped role definitions
    │       └─→ CWorkflowBase      # Example: company-scoped workflows
    │       └─→ CProjectType       # Example: project types with workflows
    │
    └─→ CEntityOfProject<T>        # Adds: project association
            ↓
        CProjectItem<T>             # Adds: status, workflow, dates, assignedTo
            ↓
        [Work Items: CActivity, CMeeting, CDecision, etc.]
```

---

## Interface vs Inheritance Guidelines

### Rule: Prefer Composition Over Inheritance for Features

**When to use inheritance:**
- Shared **identity** (all entities need id, name, etc.)
- Core **persistence behavior** (database operations)
- **Is-A relationship** that cannot change (Activity IS-A ProjectItem)

**When to use interfaces:**
- Optional **capabilities** (can be added/removed from entities)
- **Behavior contracts** (defines what entity can do)
- Features that apply to **multiple unrelated hierarchies**

### Interface Pattern Examples

#### 1. ISprintableItem - Optional Sprint Capability

```java
// GOOD: Interface for optional capability
public interface ISprintableItem {
    CSprintItem getSprintItem();
    Long getStoryPoint();
    void moveSprintItemToSprint(CSprint targetSprint);
    void moveSprintItemToBacklog();
}

// Entities opt-in to sprint capability
public class CActivity extends CProjectItem<CActivity> 
        implements ISprintableItem {
    
    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private CSprintItem sprintItem;
    
    @Override
    public CSprintItem getSprintItem() { return sprintItem; }
}

// NOT all project items need sprints
public class CRisk extends CProjectItem<CRisk> {
    // Does NOT implement ISprintableItem - risks don't go in sprints
}
```

**Why interface here?**
- Sprint capability is **optional** (not all project items need it)
- Provides **default implementations** via interface methods
- Multiple entity types can share sprint behavior **without common parent**
- Can be **added/removed** per entity type without affecting hierarchy

#### 2. IGanttDisplayable - Optional Gantt Display

```java
// GOOD: Interface for rendering contract
public interface IGanttDisplayable {
    LocalDate getStartDate();
    LocalDate getEndDate();
    String getGanttDisplayName();
    String getGanttColor();
}

// Entities that can appear in Gantt chart
public class CActivity extends CProjectItem<CActivity> 
        implements IGanttDisplayable {
    
    @Override
    public String getGanttDisplayName() {
        return getName() + " (" + getStatus().getName() + ")";
    }
}
```

**Why interface here?**
- Display capability is **presentation concern** (not domain identity)
- Different entities may have **different display logic**
- Gantt component works with **any entity** implementing this interface

#### 3. CSprintItem - Composition Pattern (NOT Inheritance)

```java
// EXCELLENT: Sprint data is OWNED BY Activity, not inherited
public class CSprintItem extends CEntityDB<CSprintItem> {
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private CSprint sprint;
    
    @OneToOne
    @JoinColumn(name = "activity_id")
    private CActivity activity;
    
    @Column(name = "item_order")
    private Integer itemOrder;
}

public class CActivity extends CProjectItem<CActivity> 
        implements ISprintableItem {
    
    // GOOD: Activity OWNS sprint item (composition)
    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private CSprintItem sprintItem;
}
```

**Why composition here?**
- Sprint assignment is **mutable state** (can change multiple times)
- Activity might have **no sprint item** (not in any sprint)
- Sprint item is a **join table entity** (many-to-many with ordering)
- Keeps Activity focused on its **core responsibility** (work item)

### Bad Pattern: Inheritance for Features

```java
// ❌ BAD: Using inheritance for optional feature
public abstract class CSprintableProjectItem<T> extends CProjectItem<T> {
    @OneToOne
    private CSprintItem sprintItem;
}

public class CActivity extends CSprintableProjectItem<CActivity> {
    // Forced to inherit sprint functionality
}

public class CRisk extends CProjectItem<CRisk> {
    // Can't inherit sprint functionality even if needed later
}
```

**Problems:**
- Forces **single inheritance chain** (can't add other features)
- Creates **middle-layer classes** that clutter hierarchy
- Makes it **hard to add/remove** features from entities
- Violates **interface segregation** principle

---

## Lazy Loading Patterns

### Problem: Detached Entities and Lazy Fields

**Scenario:** Entities selected in grids are often **detached** from Hibernate session, causing `LazyInitializationException` when accessing lazy-loaded fields.

### Pattern 1: Refresh Selected Entity Before Access

```java
// GOOD: Refresh entity when selecting from grid
protected void on_grid_selectionChanged(SelectionEvent<CGrid<T>, T> event) {
    T selectedEntity = event.getFirstSelectedItem().orElse(null);
    
    if (selectedEntity != null && selectedEntity.getId() != null) {
        // Refresh from database to get managed entity
        selectedEntity = service.getById(selectedEntity.getId())
            .orElse(selectedEntity);
    }
    
    // Now safe to access lazy fields
    if (selectedEntity != null) {
        updateToolbar(selectedEntity);
    }
}
```

**Implementation in `CComponentGridEntity`:**
```java
protected void on_grid_selectionChanged(final SelectionEvent<CGrid<T>, T> event) {
    CEntityDB<?> selectedEntity = (CEntityDB<?>) event.getValue();
    
    // Refresh entity if it has an ID
    if (selectedEntity != null && gridEntity != null) {
        try {
            final CAbstractService<?> serviceBean = 
                CSpringContext.getBean(gridEntity.getDataServiceBeanName());
            final Optional<?> refreshed = serviceBean.getById(selectedEntity.getId());
            if (refreshed.isPresent()) {
                selectedEntity = (CEntityDB<?>) refreshed.get();
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to refresh selected entity: {}", e.getMessage());
        }
    }
    
    fireEvent(new SelectionChangeEvent(this, selectedEntity));
}
```

### Pattern 2: Graceful Fallback on Lazy Load Failures

```java
// GOOD: Catch lazy loading exceptions and provide fallback
protected List<CProjectItemStatus> statusProvider(T entity) {
    try {
        // Try to get workflow-specific statuses (requires lazy field access)
        return entity.getValidNextStatuses();
    } catch (LazyInitializationException | IllegalStateException e) {
        LOGGER.debug("Lazy loading failed for entity {}, falling back to all statuses", 
            entity.getId());
        // Fallback: show all statuses
        return statusService.findAll();
    }
}
```

**Implementation in `CCrudToolbar`:**
```java
public CCrudToolbar(@Nonnull final CAbstractService<T> service, 
        @Nullable final CProjectItemStatusService statusService) {
    // ...
    
    if (statusService != null && CProjectItem.class.isAssignableFrom(entityClass)) {
        comboBoxStatus.setDataProvider((item, query) -> {
            try {
                return ((CProjectItem<?>) item).getValidNextStatuses().stream();
            } catch (Exception e) {
                // Graceful fallback on lazy loading failure
                return statusService.findAll().stream();
            }
        });
    }
}
```

### Pattern 3: Eager Loading in Service Layer

```java
// GOOD: Fetch with JOIN FETCH when you know you'll need lazy fields
@Query("SELECT e FROM #{#entityName} e " +
       "LEFT JOIN FETCH e.company " +
       "LEFT JOIN FETCH e.status " +
       "WHERE e.id = :id")
Optional<T> findByIdWithRelations(@Param("id") Long id);

// Use this method when populating forms or displaying details
public Optional<T> getForEditing(Long id) {
    return repository.findByIdWithRelations(id);
}
```

### Pattern 4: Override getById() for UI-Critical Lazy Fields (CRITICAL - MANDATORY)

**Problem:** Grids use `service.getById()` to refresh selected entities. The default implementation doesn't fetch lazy fields, causing exceptions when UI components try to display them.

**Rule:** If your entity has **lazy fields that are displayed in UI** (grids, toolbars, forms), **ALWAYS override getById()** in the service to use a query with JOIN FETCH.

#### When to Override getById()

**MUST override if:**
- ✅ Entity has lazy @ManyToOne or @OneToMany fields
- ✅ These fields are displayed in grids, forms, or toolbars
- ✅ Grid selection or toolbar updates access these fields
- ✅ Entity is used in CComponentGridEntity

**Examples requiring override:**
- CProject with lazy kanbanLine field (displayed in project grid)
- CComment with lazy activity field (displayed in comment grid)  
- CActivity with lazy comments collection (accessed in detail view)
- CSprintItem with lazy activity/meeting references

#### Implementation Pattern

**Step 1: Create Repository Query with JOIN FETCH**

```java
public interface IProjectRepository extends IEntityOfCompanyRepository<CProject> {
    
    // CRITICAL: Include all lazy fields needed for UI display
    @Query("""
        SELECT p FROM CProject p
        LEFT JOIN FETCH p.company
        LEFT JOIN FETCH p.kanbanLine
        WHERE p.id = :id
        """)
    Optional<CProject> findByIdForPageView(@Param("id") Long id);
}
```

**Naming Convention:**
- Use `findByIdForPageView()` - indicates query is optimized for UI
- Use `findById()` for simple fetch without joins (repository default)

**Step 2: Override getById() in Service**

```java
@Service
public class CProjectService extends CEntityOfCompanyService<CProject> {
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CProject> getById(final Long id) {
        Check.notNull(id, "ID cannot be null");
        // Use findByIdForPageView to fetch with lazy fields
        return ((IProjectRepository) repository).findByIdForPageView(id);
    }
}
```

**Pattern Elements:**
1. **@Override** - Override base service method
2. **@Transactional(readOnly = true)** - Read-only transaction
3. **Check.notNull(id, ...)** - Validate input
4. **Cast repository** - `((IProjectRepository) repository)`
5. **Use ForPageView query** - Returns entity with JOIN FETCH
6. **Document why** - Comment explaining lazy loading avoidance

#### Real-World Examples

**Example 1: CProject with kanbanLine**

```java
// Entity with lazy field
@Entity
public class CProject extends CEntityOfCompany<CProject> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanban_line_id")
    private CKanbanLine kanbanLine;  // Displayed in project grid
    
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<CUserProjectSettings> userSettings;  // Collection, not in grid
}

// Repository with ForPageView query
public interface IProjectRepository extends IEntityOfCompanyRepository<CProject> {
    @Query("""
        SELECT p FROM CProject p
        LEFT JOIN FETCH p.company
        LEFT JOIN FETCH p.kanbanLine
        WHERE p.id = :id
        """)
    Optional<CProject> findByIdForPageView(@Param("id") Long id);
}

// Service override
@Service
public class CProjectService extends CEntityOfCompanyService<CProject> {
    @Override
    @Transactional(readOnly = true)
    public Optional<CProject> getById(final Long id) {
        Check.notNull(id, "ID cannot be null");
        return ((IProjectRepository) repository).findByIdForPageView(id);
    }
}
```

**Why kanbanLine needs JOIN FETCH:**
- Displayed in project grid columns
- Accessed when project is selected in grid
- Grid selection triggers `onSelectionChange()` → calls `getById()`
- Without JOIN FETCH → LazyInitializationException

**Why userSettings collection doesn't need JOIN FETCH:**
- Collection is NOT displayed in grid
- Only accessed in dedicated detail view
- Detail view can load separately when needed

**Example 2: CComment with activity**

```java
// Entity with lazy field
@Entity
public class CComment extends CEntityDB<CComment> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private CActivity activity;  // Displayed in comment grid
}

// Repository with ForPageView query
public interface ICommentRepository extends IAbstractRepository<CComment> {
    @Query("SELECT c FROM CComment c LEFT JOIN FETCH c.activity WHERE c.id = :id")
    Optional<CComment> findByIdForPageView(@Param("id") Long id);
}

// Service override
@Service
public class CCommentService extends CAbstractService<CComment> {
    @Override
    @Transactional(readOnly = true)
    public Optional<CComment> getById(final Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return ((ICommentRepository) repository).findByIdForPageView(id);
    }
}
```

#### Decision Criteria: Which Lazy Fields to Fetch?

Use this checklist to determine which lazy fields need JOIN FETCH:

| Field Characteristic | JOIN FETCH? | Reason |
|----------------------|-------------|--------|
| Displayed in grid columns | ✅ YES | Grid selection refreshes entity |
| Shown in toolbar/header | ✅ YES | Accessed immediately on selection |
| Used in form validation | ✅ YES | Form population needs data |
| Shown in detail tabs | ❓ MAYBE | Consider if loaded on selection |
| Collection rarely accessed | ❌ NO | Load separately when needed |
| Admin-only field | ❌ NO | Most users never access |
| Audit fields (createdBy, etc.) | ❓ MAYBE | If shown in grid/toolbar |

**Example Decision Process:**

```java
@Entity
public class CActivity extends CProjectItem<CActivity> {
    
    // DECISION TREE:
    
    @ManyToOne(fetch = FetchType.LAZY)
    private CProject project;  
    // ✅ JOIN FETCH: Displayed in activity grid, shown in toolbar
    
    @ManyToOne(fetch = FetchType.LAZY)
    private CUser assignedTo;  
    // ✅ JOIN FETCH: Displayed in grid, used for filtering
    
    @OneToMany(mappedBy = "activity", fetch = FetchType.LAZY)
    private List<CComment> comments;  
    // ❌ NO JOIN FETCH: Collection loaded in separate tab, not in grid
    
    @OneToOne(mappedBy = "activity", fetch = FetchType.LAZY)
    private CSprintItem sprintItem;  
    // ✅ JOIN FETCH: Sprint info shown in grid, toolbar updates
}

// Repository query fetches only grid-critical fields
@Query("""
    SELECT a FROM CActivity a
    LEFT JOIN FETCH a.project
    LEFT JOIN FETCH a.assignedTo
    LEFT JOIN FETCH a.sprintItem
    WHERE a.id = :id
    """)
Optional<CActivity> findByIdForPageView(@Param("id") Long id);
// Note: comments NOT fetched - loaded separately when tab opened
```

#### Common Mistakes to Avoid

❌ **DON'T fetch all lazy fields blindly:**
```java
// ❌ BAD: Fetching unnecessary collections hurts performance
@Query("""
    SELECT a FROM CActivity a
    LEFT JOIN FETCH a.project
    LEFT JOIN FETCH a.assignedTo
    LEFT JOIN FETCH a.comments         -- ❌ Large collection, not in grid
    LEFT JOIN FETCH a.attachments      -- ❌ Large collection, not in grid
    LEFT JOIN FETCH a.historyEntries   -- ❌ Rarely accessed
    WHERE a.id = :id
    """)
```

✅ **DO fetch only UI-critical fields:**
```java
// ✅ GOOD: Only grid/toolbar fields
@Query("""
    SELECT a FROM CActivity a
    LEFT JOIN FETCH a.project
    LEFT JOIN FETCH a.assignedTo
    LEFT JOIN FETCH a.sprintItem
    WHERE a.id = :id
    """)
```

❌ **DON'T skip override for grid-displayed lazy fields:**
```java
// ❌ BAD: Entity has lazy kanbanLine, but no getById() override
@Service
public class CProjectService extends CEntityOfCompanyService<CProject> {
    // Missing override - will cause LazyInitializationException in grid!
}
```

✅ **DO override for any grid-displayed lazy field:**
```java
// ✅ GOOD: Override ensures lazy fields loaded for grid
@Service
public class CProjectService extends CEntityOfCompanyService<CProject> {
    @Override
    @Transactional(readOnly = true)
    public Optional<CProject> getById(final Long id) {
        Check.notNull(id, "ID cannot be null");
        return ((IProjectRepository) repository).findByIdForPageView(id);
    }
}
```

#### Interaction with Base Service Overrides

**Pattern: Chain with parent getById() if it has additional logic**

Some base services already override `getById()` (e.g., `CEntityOfCompanyService`). Chain calls appropriately:

```java
// CEntityOfCompanyService already overrides getById()
public abstract class CEntityOfCompanyService<EntityClass> {
    @Override
    public Optional<EntityClass> getById(final Long id) {
        final Optional<EntityClass> entity = super.getById(id);
        entity.ifPresent(CEntityOfCompany::initializeAllFields);  // Extra logic
        return entity;
    }
}

// Your service should fetch with JOIN FETCH, then apply parent logic
@Service
public class CProjectService extends CEntityOfCompanyService<CProject> {
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CProject> getById(final Long id) {
        Check.notNull(id, "ID cannot be null");
        
        // Fetch with JOIN FETCH from repository
        final Optional<CProject> project = 
            ((IProjectRepository) repository).findByIdForPageView(id);
        
        // Apply parent logic (initializeAllFields if entity present)
        project.ifPresent(CEntityOfCompany::initializeAllFields);
        
        return project;
    }
}
```

**Alternative: If parent only calls initializeAllFields, skip chaining:**

```java
@Override
@Transactional(readOnly = true)
public Optional<CProject> getById(final Long id) {
    Check.notNull(id, "ID cannot be null");
    // findByIdForPageView already fetches with JOIN FETCH
    // No need to call parent since we're fetching eagerly
    return ((IProjectRepository) repository).findByIdForPageView(id);
}
```

#### Testing Pattern for getById() Override

```java
@Test
void testGetByIdFetchesLazyFields() {
    // Create project with kanbanLine
    CProject project = new CProject("Test Project", company);
    CKanbanLine kanbanLine = new CKanbanLine("Sprint Kanban", company);
    kanbanLineService.save(kanbanLine);
    project.setKanbanLine(kanbanLine);
    projectService.save(project);
    
    // Clear session to detach entities
    entityManager.clear();
    
    // Retrieve via getById()
    CProject retrieved = projectService.getById(project.getId()).orElseThrow();
    
    // Should NOT throw LazyInitializationException
    assertDoesNotThrow(() -> {
        CKanbanLine line = retrieved.getKanbanLine();
        assertNotNull(line);
        assertEquals("Sprint Kanban", line.getName());
    });
}
```

#### Summary: getById() Override Pattern

**Checklist for Every Entity Service:**

- [ ] Identify all lazy @ManyToOne/@OneToMany fields in entity
- [ ] Determine which lazy fields are displayed in grid/toolbar
- [ ] Create `findByIdForPageView()` query with JOIN FETCH for UI fields
- [ ] Override `getById()` in service to use ForPageView query
- [ ] Add @Transactional(readOnly = true) annotation
- [ ] Document why override is needed (lazy loading avoidance)
- [ ] Write test verifying lazy fields load without exception
- [ ] Update if grid columns change (add/remove lazy fields)

**Quick Reference:**

```java
// 1. Repository: Add ForPageView query
@Query("""
    SELECT e FROM EntityName e
    LEFT JOIN FETCH e.lazyField1
    LEFT JOIN FETCH e.lazyField2
    WHERE e.id = :id
    """)
Optional<EntityName> findByIdForPageView(@Param("id") Long id);

// 2. Service: Override getById()
@Override
@Transactional(readOnly = true)
public Optional<EntityName> getById(final Long id) {
    Check.notNull(id, "ID cannot be null");
    return ((IEntityRepository) repository).findByIdForPageView(id);
}

// 3. Test: Verify lazy loading works
@Test
void testGetByIdFetchesLazyFields() {
    entityManager.clear();  // Detach
    entity = service.getById(id).orElseThrow();
    assertDoesNotThrow(() -> entity.getLazyField());  // Should work
}
```

### Pattern 5: Company-Scoped Entity Initialization

```java
// GOOD: Initialize company entities on first access
public class CEntityOfCompany<T extends CEntityOfCompany<T>> extends CEntityNamed<T> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CCompany company;
    
    @PostLoad
    protected void initializeLazyFields() {
        if (company != null) {
            // Access company to initialize proxy if in managed state
            try {
                company.getId();
            } catch (Exception e) {
                // Entity is detached, ignore
            }
        }
    }
}
```

### Testing Pattern: Verify Lazy Loading Handling

```java
@Test
void testGridSelectionWithDetachedEntity() {
    // Create and save entity
    CActivity activity = createTestActivity();
    activityService.save(activity);
    
    // Clear session to detach entity
    entityManager.clear();
    
    // Retrieve detached entity
    CActivity detached = activityService.getById(activity.getId()).get();
    
    // Grid selection should refresh entity
    grid.select(detached);
    
    // Toolbar should handle lazy loading gracefully
    CCrudToolbar<CActivity> toolbar = new CCrudToolbar<>(activityService, statusService);
    
    // Should not throw LazyInitializationException
    assertDoesNotThrow(() -> toolbar.updateForEntity(detached));
}
```

---

## Progress Indicators and Story Points

### ISprintableItem Interface Pattern

**Purpose:** Provides story point tracking and sprint assignment for work items.

```java
public interface ISprintableItem {
    
    // Story point management
    Long getStoryPoint();
    void setStoryPoint(Long storyPoint);
    
    // Sprint assignment (uses composition, not inheritance)
    CSprintItem getSprintItem();
    void setSprintItem(CSprintItem sprintItem);
    
    // Workflow integration
    CProjectItemStatus getStatus();
    
    // Sprint operations
    @Transactional
    default void moveSprintItemToSprint(CSprint targetSprint) {
        CSprintItem sprintItem = getSprintItem();
        Check.notNull(sprintItem, "Sprint item must exist");
        Check.notNull(targetSprint, "Target sprint cannot be null");
        
        sprintItem.setSprint(targetSrint);
        sprintItem.setKanbanColumnId(null); // Clear kanban assignment
        
        CSprintItemService service = CSpringContext.getBean(CSprintItemService.class);
        service.save(sprintItem);
    }
    
    @Transactional
    default void moveSprintItemToBacklog() {
        CSprintItem sprintItem = getSprintItem();
        Check.notNull(sprintItem, "Sprint item must exist");
        
        sprintItem.setSprint(null); // NULL = backlog
        sprintItem.setKanbanColumnId(null);
        
        CSprintItemService service = CSpringContext.getBean(CSprintItemService.class);
        service.save(sprintItem);
    }
}
```

### Story Point Display Components

#### CComponentStoryPoint - Editable Story Point Widget

```java
// Always visible, click to edit
public class CComponentStoryPoint extends CDiv {
    private final ISprintableItem item;
    private final Runnable onChangeCallback;
    private CSpan displayLabel;
    private CIntegerField editor;
    
    public CComponentStoryPoint(ISprintableItem item, Runnable onChangeCallback) {
        this.item = item;
        this.onChangeCallback = onChangeCallback;
        
        createDisplayLabel();
        createEditor();
        
        add(displayLabel);
        displayLabel.addClickListener(e -> showEditor());
    }
    
    private void createDisplayLabel() {
        Long storyPoint = item.getStoryPoint();
        displayLabel = new CSpan(storyPoint != null ? storyPoint + " SP" : "0 SP");
        displayLabel.addClassName("story-point-display");
    }
    
    private void showEditor() {
        remove(displayLabel);
        add(editor);
        editor.focus();
    }
    
    private void saveStoryPoint() {
        Long newValue = editor.getValue() != null ? editor.getValue().longValue() : 0L;
        item.setStoryPoint(newValue);
        
        // Save through service
        CProjectItemService<?> service = getServiceForItem(item);
        service.save((CProjectItem<?>) item);
        
        // Update display
        remove(editor);
        createDisplayLabel();
        add(displayLabel);
        
        // Notify parent to refresh totals
        if (onChangeCallback != null) {
            onChangeCallback.run();
        }
    }
}
```

#### Usage in Kanban Board

```java
public class CComponentKanbanPostit extends CDiv {
    
    private void createStoryPointSection(ISprintableItem item) {
        // Always show story points with click-to-edit
        CComponentStoryPoint storyPointWidget = new CComponentStoryPoint(
            item,
            () -> refreshColumnTotals() // Callback to refresh column story point sum
        );
        
        add(storyPointWidget);
    }
    
    private void refreshColumnTotals() {
        // Notify parent column to recalculate story point totals
        fireEvent(new StoryPointChangedEvent(this));
    }
}
```

### Progress Bar Testing Requirements

#### Visual Behavior Rules

1. **Always Visible**: Story points display even when value is 0
2. **Click to Edit**: Single-click on story point value opens inline editor
3. **Auto-Save**: Blur or Enter key saves value and closes editor
4. **Escape Cancels**: Escape key discards changes and closes editor
5. **Column Totals**: Parent column story point sum updates immediately after save
6. **Validation**: Only non-negative integers allowed

#### Playwright Test Pattern

```java
@Test
void testStoryPointEditingInKanban() {
    // Navigate to kanban board
    navigateToKanbanBoard();
    
    // Find a postit
    Locator postit = page.locator(".kanban-postit").first();
    Locator storyPointDisplay = postit.locator(".story-point-display");
    
    // Verify initial display
    assertThat(storyPointDisplay).hasText("5 SP");
    
    // Click to edit
    storyPointDisplay.click();
    
    // Editor should appear
    Locator editor = postit.locator("vaadin-integer-field");
    assertThat(editor).isVisible();
    assertThat(editor).hasValue("5");
    
    // Change value
    editor.fill("8");
    editor.press("Enter");
    
    // Editor should close, display should update
    assertThat(editor).not().isVisible();
    assertThat(storyPointDisplay).hasText("8 SP");
    
    // Column total should update
    Locator columnTotal = page.locator(".kanban-column-header .story-point-total");
    String totalText = columnTotal.textContent();
    assertTrue(totalText.contains("8")); // Column total includes this postit
}

@Test
void testStoryPointCancelEdit() {
    navigateToKanbanBoard();
    
    Locator postit = page.locator(".kanban-postit").first();
    Locator storyPointDisplay = postit.locator(".story-point-display");
    
    String originalValue = storyPointDisplay.textContent();
    
    // Click to edit
    storyPointDisplay.click();
    
    // Change value
    Locator editor = postit.locator("vaadin-integer-field");
    editor.fill("999");
    
    // Cancel with Escape
    editor.press("Escape");
    
    // Value should not change
    assertThat(storyPointDisplay).hasText(originalValue);
}
```

---

## Multi-Value Persistence Pattern

### Purpose

Persist multiple related UI state values under a single namespace for complex components like Kanban boards, dashboards, and filter toolbars.

### Interface: IHasMultiValuePersistence

```java
public interface IHasMultiValuePersistence {
    
    Logger getLogger();
    ISessionService getSessionService();
    
    // Enable persistence with namespace
    default void persist_enable(String namespace) {
        persist_setNamespace(namespace);
        persist_setEnabled(true);
    }
    
    // Store a value
    default void persist_setValue(String key, String value) {
        if (!persist_isEnabled()) return;
        
        String fullKey = persist_getNamespace() + "." + key;
        getSessionService().setUserSetting(fullKey, value);
        getLogger().debug("Persisted {}: {}", fullKey, value);
    }
    
    // Retrieve a value
    default Optional<String> persist_getValue(String key) {
        if (!persist_isEnabled()) return Optional.empty();
        
        String fullKey = persist_getNamespace() + "." + key;
        return getSessionService().getUserSetting(fullKey);
    }
    
    // Clear specific value
    default void persist_clearValue(String key) {
        if (!persist_isEnabled()) return;
        
        String fullKey = persist_getNamespace() + "." + key;
        getSessionService().removeUserSetting(fullKey);
    }
    
    // Clear all values in namespace
    default void persist_clearAllValues() {
        if (!persist_isEnabled()) return;
        
        String prefix = persist_getNamespace() + ".";
        // Implementation depends on session service capabilities
    }
    
    // Namespace management
    String persist_getNamespace();
    void persist_setNamespace(String namespace);
    boolean persist_isEnabled();
    void persist_setEnabled(boolean enabled);
}
```

### Usage Example: Kanban Board State

```java
public class CComponentKanbanBoard extends CDiv 
        implements IHasMultiValuePersistence {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
    private String persistenceNamespace;
    private boolean persistenceEnabled;
    
    public CComponentKanbanBoard(Long projectId, ISessionService sessionService) {
        this.sessionService = sessionService;
        
        // Enable persistence with unique namespace
        persist_enable("kanbanBoard_project" + projectId);
        
        // Restore previous state
        restorePersistedState();
    }
    
    private void restorePersistedState() {
        // Restore selected kanban line
        persist_getValue("selectedKanbanLineId")
            .map(Long::valueOf)
            .ifPresent(this::selectKanbanLine);
        
        // Restore expanded columns
        persist_getValue("expandedColumns")
            .ifPresent(this::expandColumns);
        
        // Restore filter values
        persist_getValue("filterStatus")
            .ifPresent(statusFilter::setValue);
    }
    
    protected void on_kanbanLineCombo_changed(Long kanbanLineId) {
        // Persist selection
        persist_setValue("selectedKanbanLineId", kanbanLineId.toString());
        
        // Update UI
        loadKanbanLine(kanbanLineId);
    }
    
    protected void on_columnExpanded(Long columnId) {
        // Persist expanded state
        String currentExpanded = persist_getValue("expandedColumns")
            .orElse("");
        String newExpanded = currentExpanded + "," + columnId;
        persist_setValue("expandedColumns", newExpanded);
    }
    
    @Override
    public Logger getLogger() { return LOGGER; }
    
    @Override
    public String persist_getNamespace() { return persistenceNamespace; }
    
    @Override
    public void persist_setNamespace(String namespace) { 
        this.persistenceNamespace = namespace; 
    }
    
    @Override
    public boolean persist_isEnabled() { return persistenceEnabled; }
    
    @Override
    public void persist_setEnabled(boolean enabled) { 
        this.persistenceEnabled = enabled; 
    }
}
```

### Testing Multi-Value Persistence

```java
@Test
void testKanbanBoardPersistenceAcrossSessions() {
    // First session: configure board
    navigateToKanbanBoard();
    selectKanbanLine("Sprint Kanban");
    expandColumn("In Progress");
    setStatusFilter("Active");
    
    // Close browser (simulating logout/session end)
    page.close();
    
    // Second session: reopen board
    page = browser.newPage();
    login();
    navigateToKanbanBoard();
    
    // State should be restored
    assertThat(kanbanLineCombo).hasValue("Sprint Kanban");
    assertThat(page.locator(".column[data-id='in-progress']"))
        .hasClass("expanded");
    assertThat(statusFilter).hasValue("Active");
}
```

---

## Company-Scoped vs Project-Scoped Entities

### Decision: When to Use Company Scope

**Use CEntityOfCompany when:**
1. Entity defines **reusable templates** (workflows, roles, types)
2. Entity is **shared across all projects** in company
3. Entity represents **company-wide configuration**
4. Changes should affect **all projects** using the entity

**Use CEntityOfProject when:**
5. Entity is **specific to one project**
6. Entity represents **project work items** (activities, meetings, risks)
7. Different projects need **different instances** of the entity

### Example 1: CRole - Company-Scoped

**Before (WRONG):**
```java
// ❌ BAD: Role was project-scoped, meaning each project had separate roles
public class CRole extends CEntityOfProject<CRole> {
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private CProject project;
}

// Problem: Role definitions duplicated across projects
// Problem: Can't assign same role across multiple projects
```

**After (CORRECT):**
```java
// ✅ GOOD: Role is company-scoped, can be used in any project
public class CRole extends CEntityOfCompany<CRole> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CCompany company;
}

// User-Project-Role assignment links role to specific project
public class CUserProjectRole extends CRole {
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private CProject project; // Explicit project reference
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
}
```

**Benefits:**
- **Reusable**: Same role definition across all company projects
- **Consistent**: Role permissions uniform across projects
- **Maintainable**: Update role once, affects all projects
- **Flexible**: User can have different roles in different projects

### Example 2: CWorkflowBase - Company-Scoped

**Reasoning:** Workflows are templates that define how entities progress through states. They should be consistent across projects.

```java
// ✅ GOOD: Workflow is company-scoped template
public class CWorkflowBase extends CEntityOfCompany<CWorkflowBase> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CCompany company;
}

// CProjectType links workflow to projects
public class CProjectType extends CTypeEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private CWorkflowBase workflow;
}

// Project references type (and indirectly workflow)
public class CProject extends CEntityOfProject<CProject> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_type_id")
    private CProjectType entityType;
    
    // Convenience method
    public CWorkflowBase getWorkflow() {
        return entityType != null ? entityType.getWorkflow() : null;
    }
}
```

### Example 3: CProjectType - New Pattern

**Implementation:**
```java
// ✅ GOOD: Project type is company-scoped, provides workflow template
@Entity
@Table(name = "cproject_type")
public class CProjectType extends CTypeEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CCompany company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private CWorkflowBase workflow;
    
    // Getters/setters
}

// Service checks dependencies before deletion
@Service
public class CProjectTypeService extends CAbstractService<CProjectType> {
    
    private final IProjectRepository projectRepository;
    
    @Override
    public void delete(CProjectType type) {
        // Check if any projects use this type
        Long projectCount = projectRepository.countByType(type);
        if (projectCount > 0) {
            throw new IllegalStateException(
                "Cannot delete project type: " + projectCount + " projects use it");
        }
        
        super.delete(type);
    }
}
```

**Migration Pattern:**
```java
// Initializer creates default project types
public class CProjectTypeInitializerService {
    
    public static void initializeSample(CCompany company, boolean minimal) {
        String[][] typeDefinitions = {
            {"Software Development", "Agile software development projects"},
            {"Infrastructure", "IT infrastructure and operations"},
            {"Research", "Research and development projects"}
        };
        
        for (String[] typeDef : typeDefinitions) {
            CProjectType type = new CProjectType();
            type.setCompany(company);
            type.setName(typeDef[0]);
            type.setDescription(typeDef[1]);
            
            // Link to appropriate workflow
            CWorkflowBase workflow = findOrCreateWorkflow(company, typeDef[0]);
            type.setWorkflow(workflow);
            
            service.save(type);
        }
    }
}
```

---

## Testing Requirements

### Grid Filtering Tests (Comprehensive)

**Pattern from `CPageTestComprehensive`:**

```java
@Test
void testGridFilteringForAllPages() {
    // Test each registered page
    for (String pageTitle : CPageRegistry.getAllPageTitles()) {
        testGridFiltering(pageTitle);
    }
}

private void testGridFiltering(String pageTitle) {
    // Navigate to page
    navigateToPage(pageTitle);
    
    // Wait for grid to load
    Locator grid = page.locator("vaadin-grid");
    assertThat(grid).isVisible();
    
    // Get initial row count
    int initialCount = grid.locator("vaadin-grid-cell-content").count();
    
    // Apply filters if filter toolbar exists
    Locator filterToolbar = page.locator(".filter-toolbar");
    if (filterToolbar.isVisible()) {
        // Test each filter type
        testEntityTypeFilter(pageTitle, initialCount);
        testStatusFilter(pageTitle, initialCount);
        testAssignedToFilter(pageTitle, initialCount);
        testSearchFilter(pageTitle, initialCount);
        testClearFilters(pageTitle);
    }
}

private void testEntityTypeFilter(String pageTitle, int initialCount) {
    Locator entityTypeFilter = page.locator("#entityTypeFilter");
    if (!entityTypeFilter.isVisible()) return;
    
    // Select a type
    entityTypeFilter.click();
    page.locator("vaadin-combo-box-item").first().click();
    
    // Wait for grid update
    page.waitForTimeout(500);
    
    // Verify filtering occurred
    int filteredCount = page.locator("vaadin-grid-cell-content").count();
    assertTrue(filteredCount <= initialCount, 
        pageTitle + ": Entity type filter should reduce rows");
    
    // Take screenshot
    takeScreenshot(pageTitle + "_entity_type_filtered");
}

private void testClearFilters(String pageTitle) {
    // Click clear button
    page.locator("button[title='Clear Filters']").click();
    
    // Verify all filters reset
    assertThat(page.locator("#entityTypeFilter")).hasValue("");
    assertThat(page.locator("#statusFilter")).hasValue("");
    assertThat(page.locator("#assignedToFilter")).hasValue("");
    assertThat(page.locator("#searchField")).hasValue("");
    
    // Grid should show all rows again
    takeScreenshot(pageTitle + "_filters_cleared");
}
```

### Lazy Loading Exception Tests

```java
@Test
void testToolbarHandlesDetachedEntity() {
    // Create entity
    CActivity activity = createTestActivity();
    activityService.save(activity);
    
    // Clear session to detach
    entityManager.clear();
    
    // Get detached entity
    CActivity detached = activityService.getById(activity.getId()).get();
    
    // Toolbar should handle gracefully
    CCrudToolbar<CActivity> toolbar = new CCrudToolbar<>(activityService, statusService);
    assertDoesNotThrow(() -> toolbar.updateForEntity(detached));
    
    // Status combobox should show fallback options
    ComboBox<CProjectItemStatus> statusCombo = toolbar.getStatusComboBox();
    assertNotNull(statusCombo.getDataProvider());
}
```

### Story Point Editing Tests

```java
@Test
void testStoryPointEditingCascadesToColumnTotal() {
    navigateToKanbanBoard();
    
    // Find column with story point total
    Locator column = page.locator(".kanban-column[data-id='in-progress']");
    Locator columnTotal = column.locator(".story-point-total");
    
    // Get initial total
    String initialTotal = columnTotal.textContent();
    int initialValue = extractNumber(initialTotal);
    
    // Edit a postit's story points
    Locator postit = column.locator(".kanban-postit").first();
    Locator storyPoint = postit.locator(".story-point-display");
    
    storyPoint.click();
    
    Locator editor = postit.locator("vaadin-integer-field");
    int oldValue = Integer.parseInt(editor.inputValue());
    int newValue = oldValue + 5;
    
    editor.fill(String.valueOf(newValue));
    editor.press("Enter");
    
    // Wait for update
    page.waitForTimeout(500);
    
    // Column total should increase by 5
    String newTotal = columnTotal.textContent();
    int newTotalValue = extractNumber(newTotal);
    
    assertEquals(initialValue + 5, newTotalValue,
        "Column total should increase by 5 story points");
}
```

---

## Summary Checklist

### Entity Design Decisions
- [ ] Used inheritance decision tree to choose base class
- [ ] Chose interface over inheritance for optional capabilities
- [ ] Used composition for mutable relationships (sprint items)
- [ ] Determined correct scope (company vs project)

### Lazy Loading Safety
- [ ] Refresh detached entities before accessing lazy fields
- [ ] Implement graceful fallback on lazy load exceptions
- [ ] Use eager loading queries for known access patterns
- [ ] Add @PostLoad for company-scoped entity initialization

### Progress Indicators
- [ ] Story points always visible (show 0 if null)
- [ ] Click-to-edit with inline field
- [ ] Auto-save on blur/enter, cancel on escape
- [ ] Column totals refresh after story point changes

### Multi-Value Persistence
- [ ] Enable with unique namespace per component instance
- [ ] Persist all important UI state (selections, filters, expansions)
- [ ] Restore state on component creation
- [ ] Clear state when no longer needed

### Testing Coverage
- [ ] Grid filtering tests for all pages
- [ ] Lazy loading exception handling tests
- [ ] Story point editing with cascade tests
- [ ] Multi-value persistence across sessions tests
- [ ] Screenshots for all UI state changes

---

**Last Updated:** Based on commits from 2026-01-09 to 2026-01-11  
**Key Commits:**
- `e563eb88` - Lazy initialization for company entities
- `989e342f` - CRole company-scoped refactoring
- `4cf743d8` - Lazy loading exception handling
- `89d57b77` - Story points in kanban postits
- `04b9e7f0` - Multi-value persistence interface
- `ddf7aadf` - CProjectType entity creation
