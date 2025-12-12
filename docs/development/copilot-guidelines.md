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

// UI Component fields - typeName convention
private CButton buttonAdd;           // button{Name}
private CButton buttonDelete;        // button{Name}
private CDialog dialogConfirmation;  // dialog{Name}
private CVerticalLayout layoutMain;  // layout{Name}
private CGrid<CActivity> gridItems;  // grid{Name}
private ComboBox<CStatus> comboBoxStatus;  // comboBox{Name}
private TextField textFieldName;     // textField{Name}

// Service fields
private CActivityService activityService;
private final IActivityRepository repository;

// Form fields
private TextField nameField;
private ComboBox<CStatus> statusComboBox;
```

### Tip 4: Use Event Handler Naming Pattern

Copilot recognizes the `on_{componentName}_{eventType}` pattern:

```java
// Event handlers - Copilot suggests consistent implementations
protected void on_buttonAdd_clicked() {
    // Handle add button click
}

protected void on_buttonDelete_clicked() {
    // Handle delete button click
}

protected void on_gridItems_doubleClicked(CEntity item) {
    // Handle grid item double-click
}

protected void on_comboBoxStatus_selected(String status) {
    // Handle status selection
}
```

### Tip 5: Use Factory Methods for Components

Copilot recognizes the `create_{componentName}` pattern:

```java
// Factory methods - Copilot suggests consistent implementations
protected CButton create_buttonAdd() {
    final CButton button = new CButton(VaadinIcon.PLUS.create());
    button.addClickListener(e -> on_buttonAdd_clicked());
    return button;
}

protected CButton create_buttonDelete() {
    final CButton button = new CButton(VaadinIcon.TRASH.create());
    button.addClickListener(e -> on_buttonDelete_clicked());
    return button;
}
```

### Tip 6: Leverage Examples

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

## Notification Pattern Standards (MANDATORY)

### Rule: ALWAYS Use CNotificationService

**NEVER use direct Vaadin Notification.show() calls or manual dialog instantiation. ALWAYS use CNotificationService.**

#### ✅ CORRECT - Use CNotificationService

**NOTE**: CNotificationService provides **STATIC METHODS ONLY**. Use them directly without injection.

```java
// ALWAYS use static methods - NO INJECTION NEEDED
import tech.derbent.api.ui.notifications.CNotificationService;

CNotificationService.showSuccess("Data saved successfully");
CNotificationService.showError("Save failed");
CNotificationService.showWarning("Check your input");
CNotificationService.showInfo("Process completed");
CNotificationService.showException("Error saving entity", exception);

// Convenience methods for common operations
CNotificationService.showSaveSuccess();
CNotificationService.showDeleteSuccess();
CNotificationService.showCreateSuccess();
CNotificationService.showSaveError();
CNotificationService.showDeleteError();
```

#### ❌ FORBIDDEN - Do NOT Use These Patterns

```java
// ❌ WRONG - Direct Vaadin calls
Notification.show("message");
Notification.show("message", 3000, Notification.Position.TOP_CENTER);

// ❌ WRONG - Manual dialog instantiation
new CWarningDialog("message").open();
new CInformationDialog("message").open();
new CExceptionDialog(exception).open();

// ❌ WRONG - Inconsistent positioning/styling
notification.setPosition(Position.MIDDLE);
notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
```

### Notification Types and Usage

| Type | Method | Duration | Position | Use Case |
|------|--------|----------|----------|----------|
| Success | `showSuccess()` | 2s | bottom-start | Successful operations |
| Error | `showError()` | 8s | middle | Critical errors |
| Warning | `showWarning()` | 5s | top-center | Important warnings |
| Info | `showInfo()` | 5s | bottom-start | Informational messages |
| Exception | `showException()` | Modal | center | Detailed error info |

### Convenience Methods

```java
// Common operation notifications
CNotificationService.showSaveSuccess();
CNotificationService.showDeleteSuccess();
CNotificationService.showCreateSuccess();
CNotificationService.showSaveError();
CNotificationService.showDeleteError();
CNotificationService.showOptimisticLockingError();
```

## Status Change Pattern (MANDATORY)

### Rule: Status Changes Do NOT Auto-Save

**When changing status via CCrudToolbar or any UI component, the entity MUST NOT be saved automatically. Only update the entity in memory. User must explicitly click Save button to persist changes.**

#### ✅ CORRECT - Update Entity in Memory Only

```java
// In IPageServiceHasStatusAndWorkflow.actionChangeStatus()
@Override
public void actionChangeStatus(final CProjectItemStatus newStatus) {
    // Validate status transition
    final List<CProjectItemStatus> validStatuses = 
        getProjectItemStatusService().getValidNextStatuses((IHasStatusAndWorkflow<?>) entity);
    
    if (!validStatuses.contains(newStatus)) {
        CNotificationService.showWarning("Invalid status transition");
        return;
    }
    
    // Update entity in memory ONLY - do NOT call save()
    ((IHasStatusAndWorkflow<?>) entity).setStatus(newStatus);
    setCurrentEntity(entity);
    getView().populateForm();
    
    // Notify user that change is NOT yet persisted
    CNotificationService.showInfo(
        String.format("Status set to '%s' (click Save to persist)", newStatus.getName())
    );
}
```

#### ❌ FORBIDDEN - Auto-Save on Status Change

```java
// ❌ WRONG - Do NOT auto-save on status change
@Override
public void actionChangeStatus(final CProjectItemStatus newStatus) {
    ((IHasStatusAndWorkflow<?>) entity).setStatus(newStatus);
    
    // ❌ WRONG - This auto-saves, violating the pattern
    final EntityClass savedEntity = getEntityService().save(entity);
    
    // ❌ WRONG - Message implies change is persisted
    CNotificationService.showInfo("Status changed to '" + newStatus.getName() + "'");
}
```

### Rationale

1. **Consistency**: Status fields behave like all other form fields (name, description, etc.)
2. **User Control**: User explicitly controls when changes are persisted (Save button)
3. **Batch Changes**: User can change multiple fields including status, then save once
4. **Undo Capability**: User can use Refresh button to discard all unsaved changes

### Implementation Checklist

```java
☐ Status change only updates entity in memory
☐ No call to service.save() in actionChangeStatus()
☐ Notification says "set to" or "click Save to persist"
☐ User must click Save button to persist changes
☐ Refresh button discards unsaved changes
```

## Screenshot Documentation Requirements

### MANDATORY: Screenshots for All Tasks

**For ANY task that involves code changes, you MUST provide screenshot evidence as proof of the changes.**

This requirement applies to:
- UI changes (components, layouts, styling)
- Functional changes (business logic, workflows)
- Bug fixes (showing before/after or the fix working)
- New features (demonstrating the feature in action)
- Refactoring (showing the affected functionality still works)

### How to Provide Screenshots

1. **Use Test Pages**: Navigate to the Test Support Page at `/cpagetestauxillary` to access all test pages
2. **Capture Screenshots**: Use Playwright browser automation or manual screenshots
3. **Include in PR**: Embed screenshots directly in the PR description using markdown
4. **Label Clearly**: Add descriptive captions (e.g., "Before Fix", "After Fix", "Feature in Action")

### Screenshot Checklist

```
☐ Screenshot shows the relevant UI component or feature
☐ Screenshot demonstrates the change is working correctly
☐ Screenshot is embedded in PR description with markdown
☐ Caption clearly explains what the screenshot shows
☐ Test page URL is documented if applicable
```

### Example Screenshot Documentation

```markdown
## Visual Proof

### Before Fix
![Before](path/to/before.png)
*ComboBox background bleeding beyond borders*

### After Fix  
![After](path/to/after.png)
*Background color properly contained within input borders*

### Test Page
The fix can be verified at: `/cpagetestauxillary` → "User Icon Test" page
```

### When Screenshots Are Not Required

Screenshots are **NOT** required for:
- Pure documentation changes (no code)
- Configuration-only changes (pom.xml, properties files)
- Backend-only changes with no UI impact (if explicitly documented as backend-only)

**Note**: If you claim a change has no UI impact, you must explicitly state this and explain why screenshots are not applicable.

## Widget State Preservation Pattern

### Problem
When grids are refreshed (e.g., after drag-drop operations), widget components are recreated, losing their internal UI state such as:
- Expanded/collapsed sections
- Selected tabs
- Scroll positions
- Custom visual states

### Solution: State Preservation Mechanism

Derbent provides a built-in state preservation mechanism in `CComponentWidgetEntity` that automatically saves and restores widget state across recreations.

#### How It Works

1. **Before Grid Refresh**: `CComponentGridEntity.unregisterAllWidgetComponents()` calls `saveWidgetState()` on all widgets
2. **Widget Recreation**: Grid creates fresh widget instances
3. **After Initialization**: Each widget's `initializeWidget()` calls `restoreWidgetState()` automatically

#### Implementation Pattern

**In Custom Widget Classes** (extending `CComponentWidgetEntity`):

```java
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> {
    
    private boolean sprintItemsVisible = false;  // State to preserve
    
    // =============== WIDGET STATE PRESERVATION ===============
    
    /** Saves widget UI state before destruction. */
    @Override
    public void saveWidgetState() {
        super.saveWidgetState();
        // Save any boolean, string, or simple object state
        saveStateValue(getEntity().getClass(), getEntity().getId(), 
            "sprintItemsVisible", sprintItemsVisible);
    }
    
    /** Restores widget UI state after reconstruction. */
    @Override
    protected void restoreWidgetState() {
        super.restoreWidgetState();
        // Restore state and update UI accordingly
        Boolean visible = (Boolean) getStateValue(
            getEntity().getClass(), getEntity().getId(), "sprintItemsVisible");
        if (visible != null && visible) {
            sprintItemsVisible = true;
            containerSprintItems.setVisible(true);
            // Update related UI components (buttons, icons, etc.)
            buttonToggleItems.setIcon(VaadinIcon.ANGLE_UP.create());
        }
    }
}
```

#### Key Methods

| Method | Access | Purpose | Called By |
|--------|--------|---------|-----------|
| `saveWidgetState()` | `public` | Save widget state before destruction | `CComponentGridEntity` before clearing widgets |
| `restoreWidgetState()` | `protected` | Restore widget state after creation | `initializeWidget()` automatically |
| `saveStateValue()` | `protected static` | Store a state value | Subclass `saveWidgetState()` |
| `getStateValue()` | `protected static` | Retrieve a state value | Subclass `restoreWidgetState()` |
| `clearWidgetState()` | `protected static` | Clear state for specific entity | Optional cleanup |
| `clearAllWidgetState()` | `public static` | Clear all stored state | When leaving view |

#### State Storage

- State is stored in a **static `ConcurrentHashMap`** keyed by `EntityClass_EntityId` (e.g., "CSprint_123")
- State persists across widget recreations **within the same session**
- State is **thread-safe** and can handle concurrent access
- State should be cleared when leaving the view to prevent memory leaks

#### What Can Be Stored

✅ **Recommended State Types**:
- Booleans (expanded/collapsed, visible/hidden)
- Strings (selected values, filter text)
- Integers (selected indices, scroll positions)
- Simple value objects (immutable)

❌ **Avoid Storing**:
- Component references (will be stale after recreation)
- Large data structures (use data sources instead)
- Mutable objects shared across widgets
- Session-specific data (use VaadinSession instead)

#### Best Practices

1. **Always call super**: Both `saveWidgetState()` and `restoreWidgetState()` should call their super method first
2. **Null checks**: Always check for null when retrieving state
3. **Idempotent restore**: `restoreWidgetState()` should handle being called multiple times
4. **Minimal state**: Only store what's necessary to recreate the visual appearance
5. **Clear on navigation**: Call `clearAllWidgetState()` when leaving the view

#### Example Use Cases

- **CComponentWidgetSprint**: Preserves expanded/collapsed state of sprint items grid
- **Tab selection**: Remember which tab was active in a multi-tab widget
- **Accordion panels**: Remember which panels were expanded
- **Custom filters**: Remember applied filter values
- **Scroll positions**: Restore scroll position after refresh (advanced)

#### Copilot Pattern Recognition

**Type this**:
```java
public class CCustomWidget extends CComponentWidgetEntity<CEntity> {
    
    private boolean sectionVisible = false;
    
    @Override
    public void saveWidgetState() {
```

**Copilot suggests**:
```java
    @Override
    public void saveWidgetState() {
        super.saveWidgetState();
        saveStateValue(getEntity().getClass(), getEntity().getId(), 
            "sectionVisible", sectionVisible);
    }
    
    @Override
    protected void restoreWidgetState() {
        super.restoreWidgetState();
        Boolean visible = (Boolean) getStateValue(
            getEntity().getClass(), getEntity().getId(), "sectionVisible");
        if (visible != null && visible) {
            sectionVisible = true;
            // Update UI...
        }
    }
```

## Component State Management with IStateOwnerComponent

### Overview

The `IStateOwnerComponent` interface provides a standardized way to save and restore component UI state to/from JSON. This is particularly useful for preserving user context during operations like grid refresh or page navigation.

### Pattern Usage in CGrid

CGrid implements `IStateOwnerComponent` and provides automatic state preservation for:
- Selected item (by ID for CEntityDB entities)
- Child component states (widgets in grid cells)

**Key Methods:**

```java
// Get current state as JSON
JsonObject state = grid.getStateInformation();

// Restore state from JSON
grid.restoreStateInformation(state);

// Refresh grid with automatic state preservation
grid.setItemsWithStatePreservation(newItems);
```

### Example: Grid Refresh with State Preservation

**Before (manual state management):**
```java
public void refreshGrid() {
    final ChildEntity currentValue = grid.asSingleSelect().getValue();
    final List<ChildEntity> items = loadItems(master);
    grid.setItems(items);
    grid.asSingleSelect().setValue(currentValue);  // Manual restore
}
```

**After (using IStateOwnerComponent):**
```java
public void refreshGrid() {
    final List<ChildEntity> items = loadItems(master);
    grid.setItemsWithStatePreservation(items);  // Automatic state preservation
}
```

### Implementing IStateOwnerComponent in Custom Components

Components can implement this interface to provide their own state management:

```java
public class CCustomPanel extends VerticalLayout implements IStateOwnerComponent {
    
    private boolean expanded = false;
    private String selectedTabId;
    
    @Override
    public JsonObject getStateInformation() {
        final JsonObject state = Json.createObject();
        state.put("expanded", expanded);
        if (selectedTabId != null) {
            state.put("selectedTabId", selectedTabId);
        }
        
        // Collect child component states
        final JsonArray childStates = Json.createArray();
        int index = 0;
        for (Component child : getChildren().toList()) {
            if (child instanceof IStateOwnerComponent) {
                final JsonObject childState = ((IStateOwnerComponent) child).getStateInformation();
                childState.put("componentIndex", index);
                childStates.set(index++, childState);
            }
        }
        if (childStates.length() > 0) {
            state.put("childStates", childStates);
        }
        
        return state;
    }
    
    @Override
    public void restoreStateInformation(final JsonObject state) {
        if (state == null) return;
        
        // Restore own state
        if (state.hasKey("expanded")) {
            expanded = state.getBoolean("expanded");
            updateExpandedState();
        }
        if (state.hasKey("selectedTabId")) {
            selectedTabId = state.getString("selectedTabId");
            selectTab(selectedTabId);
        }
        
        // Restore child states
        if (state.hasKey("childStates")) {
            final JsonArray childStates = state.getArray("childStates");
            final List<Component> children = getChildren().toList();
            for (int i = 0; i < childStates.length(); i++) {
                final JsonObject childState = childStates.getObject(i);
                if (i < children.size() && children.get(i) instanceof IStateOwnerComponent) {
                    ((IStateOwnerComponent) children.get(i)).restoreStateInformation(childState);
                }
            }
        }
    }
}
```

### Best Practices

1. **Save Minimal State**: Only save what's necessary to restore user context
2. **Handle Nulls**: Always check for null state in `restoreStateInformation()`
3. **Log State Operations**: Use `LOGGER.debug("[StateOwner] ...")` for debugging
4. **Recursive Collection**: Collect state from child components implementing the interface
5. **Fail Gracefully**: Wrap state operations in try-catch to avoid breaking functionality

### Debugging State Issues

Enable debug logging to trace state operations:

```java
// In logback.xml or application.properties
<logger name="tech.derbent.api.grid.domain.CGrid" level="DEBUG"/>
```

Look for `[StateOwner]` prefix in logs:
```
[StateOwner] Saved selected item ID: 42
[StateOwner] Grid state saved successfully
[StateOwner] Attempting to restore selected item ID: 42
[StateOwner] Restored selection to item ID: 42
[StateOwner] Grid state restored successfully
```

## Summary

GitHub Copilot works exceptionally well with Derbent because of:

1. **Consistent Patterns**: C-prefix, inheritance hierarchies, metadata annotations
2. **Predictable Structure**: Entity → Service → View pattern
3. **Clear Naming**: Descriptive names that indicate purpose
4. **Good Documentation**: Comments and JavaDoc that provide context
5. **Type Safety**: Strong typing with generics
6. **State Management**: Standardized component state preservation with IStateOwnerComponent

By following these guidelines, you'll maximize Copilot's effectiveness and accelerate development while maintaining code quality.
