# Coding Rules - UI, Copy Pattern, and Entity Management

## Date: 2026-01-18
## Status: MANDATORY for all future development

---

## 1. Dialog UI Design Rules

### 1.1 Width and Spacing

**RULE**: All dialogs must use max-width with responsive width
```java
// ✅ CORRECT
mainLayout.setMaxWidth("600px");  // Max constraint
mainLayout.setWidthFull();        // Fill available up to max

// ❌ WRONG
mainLayout.setWidth("600px");     // Fixed, no flexibility
mainLayout.setWidth("100%");      // Too wide on desktop
```

**Rationale**: 
- 600px is optimal for form readability
- Max-width prevents overflow on large screens
- WidthFull ensures responsiveness on smaller screens

### 1.2 Compact Spacing

**RULE**: Use custom gap instead of default spacing
```java
// ✅ CORRECT
layout.setSpacing(false);
layout.getStyle().set("gap", "12px");  // Between sections
layout.getStyle().set("gap", "8px");   // Between items

// ❌ WRONG
layout.setSpacing(true);  // Too much space
```

**Rationale**: Default Vaadin spacing is too generous; custom gaps provide compact, professional appearance

### 1.3 Multi-Column Layouts

**RULE**: Use 2-column grids for 6+ checkboxes or similar items
```java
// ✅ CORRECT - 8 checkboxes in 2 columns
final HorizontalLayout grid = new HorizontalLayout();
final VerticalLayout leftColumn = new VerticalLayout();  // 4 items
final VerticalLayout rightColumn = new VerticalLayout(); // 4 items
grid.add(leftColumn, rightColumn);

// ❌ WRONG - All in one column
final VerticalLayout column = new VerticalLayout();
// 8 items stacked vertically = too tall
```

**Rationale**: Better space utilization, reduced dialog height, easier scanning

### 1.4 Select All/Deselect All

**RULE**: Toggle button must affect ALL checkboxes equally
```java
// ✅ CORRECT - Simple, all checkboxes treated the same
private void toggleSelectAll() {
    allSelected = !allSelected;
    final boolean checkValue = allSelected;
    
    buttonSelectAll.setText(allSelected ? "Deselect All" : "Select All");
    
    // All checkboxes get same value
    checkbox1.setValue(checkValue);
    checkbox2.setValue(checkValue);
    checkbox3.setValue(checkValue);
    // ... all checkboxes
}

// ❌ WRONG - Inverse logic for some checkboxes
if (allSelected) {
    checkbox1.setValue(true);
    checkbox2.setValue(false);  // ❌ Confusing!
}
```

**Rationale**: Users expect "Select All" to check everything; inverse logic is confusing

### 1.5 Visual Hierarchy

**RULE**: Avoid unnecessary visual clutter
```java
// ✅ CORRECT - Clean, minimal
- No Hr dividers between sections
- No extra wrapper Divs
- Subtle background for groups (var(--lumo-contrast-5pct))
- Headers only when needed

// ❌ WRONG - Cluttered
- Hr dividers everywhere
- Multiple nested Divs
- Heavy borders
- Redundant headers
```

**Rationale**: Less visual noise = better focus and professional appearance

---

## 2. Entity Type Selection Rules

### 2.1 Use CComboBox with Entity Registry

**RULE**: Always use CComboBox with CEntityRegistry for entity type selection
```java
// ✅ CORRECT
final CComboBox<String> comboBox = new CComboBox<>("Select Entity Type");
final List<String> entityKeys = CEntityRegistry.getAllRegisteredEntityKeys();
comboBox.setItems(entityKeys);

// Custom label generator using entity titles
comboBox.setItemLabelGenerator(key -> {
    final Class<?> clazz = CEntityRegistry.getEntityClass(key);
    final String title = CEntityRegistry.getEntityTitleSingular(clazz);
    return title != null ? title : clazz.getSimpleName();
});

// ❌ WRONG
final ComboBox<Class<?>> comboBox = new ComboBox<>();
comboBox.setItems(CActivity.class, CMeeting.class);  // Hardcoded
comboBox.setItemLabelGenerator(Class::getSimpleName); // Not user-friendly
```

**Rationale**: 
- CEntityRegistry provides all registered types dynamically
- Entity titles (ENTITY_TITLE_SINGULAR) are user-friendly
- Simple class names are technical and unclear

### 2.2 Special First Item

**RULE**: For copy/move dialogs, first item should be "Same as Source"
```java
// ✅ CORRECT
private static final String SAME_AS_SOURCE_KEY = "__SAME_AS_SOURCE__";

private List<String> getCompatibleTargetTypes() {
    final List<String> types = new ArrayList<>();
    types.add(SAME_AS_SOURCE_KEY);  // First item
    types.addAll(CEntityRegistry.getAllRegisteredEntityKeys());
    return types;
}

comboBox.setItemLabelGenerator(key -> {
    if (SAME_AS_SOURCE_KEY.equals(key)) {
        return "⭐ Same as Source (" + sourceEntityTitle + ")";
    }
    // ... other items
});

// ❌ WRONG
comboBox.setItems(CEntityRegistry.getAllRegisteredEntityKeys());
// No special first item
```

**Rationale**: Most common case (copy to same type) should be first and visually distinct

---

## 3. Unique Name Generation Rules

### 3.1 Use Service generateUniqueName()

**RULE**: Always use service's generateUniqueName() pattern, never manual concatenation
```java
// ✅ CORRECT - Let service generate name
final CAbstractService service = getServiceForEntity(targetClass);
final CEntityDB tempEntity = service.newEntity();
if (tempEntity instanceof CEntityNamed) {
    final String uniqueName = ((CEntityNamed<?>) tempEntity).getName();
    // Use uniqueName
}

// ❌ WRONG - Manual name generation
final String name = entityName + " (Copy)";  // Not unique!
final String name = entityName + System.currentTimeMillis();  // Ugly!
```

**Rationale**: 
- Service already has logic to generate unique names
- Format: `EntityName##` (e.g., `Activity01`, `Meeting15`)
- Counts existing entities automatically
- Consistent across entire application

### 3.2 Pattern: EntityName + Zero-Padded Number

**Standard Format**: `EntitySimpleName` + `%02d`
- Examples: `Activity01`, `Meeting02`, `Issue23`
- Implemented in: `CEntityNamedService.generateUniqueName()`

### 3.3 Update Name on Type Change

**RULE**: When user changes target entity type, regenerate name
```java
// ✅ CORRECT - Dynamic update
comboBoxTargetType.addValueChangeListener(event -> {
    if (event.getValue() != null) {
        updateGeneratedName(event.getValue());
    }
});

private void updateGeneratedName(String selectedKey) {
    // Get service for target type
    // Generate new unique name
    // Update name field
}

// ❌ WRONG - Static name
comboBoxTargetType.addValueChangeListener(event -> {
    // Name field not updated
});
```

**Rationale**: Different entity types have different name sequences; copying Activity15 to Meeting should show Meeting08, not Activity15

---

## 4. Entity Initialization Rules

### 4.1 Always Call initializeNewEntity()

**RULE**: Before saving a new or copied entity, ALWAYS call service.initializeNewEntity()
```java
// ✅ CORRECT
final CEntityDB copiedEntity = original.copyTo(targetClass, options);
service.initializeNewEntity(copiedEntity);  // ← MANDATORY
service.save(copiedEntity);

// ❌ WRONG
final CEntityDB copiedEntity = original.copyTo(targetClass, options);
service.save(copiedEntity);  // Missing initialization!
```

**What initializeNewEntity() Does**:
- Sets initial status (from workflow)
- Sets initial workflow (from entity type)
- Sets createdBy / createdDate (current user/timestamp)
- Sets lastModifiedBy / lastModifiedDate (current user/timestamp)
- Sets project / company context (from session)
- Generates unique name (if CEntityNamed)

**Rationale**: Ensures all required fields are properly initialized before persistence

### 4.2 Initialization Order

**RULE**: Follow this exact order
```
1. Create/copy entity
2. Call initializeNewEntity()
3. Set custom fields (if needed)
4. Save entity
5. Navigate to entity
```

Example:
```java
// 1. Create/copy
final CActivity copy = (CActivity) original.copyTo(CActivity.class, options);

// 2. Initialize
service.initializeNewEntity(copy);

// 3. Custom fields
copy.setName(userProvidedName);

// 4. Save
final CActivity saved = service.save(copy);

// 5. Navigate
CDynamicPageRouter.navigateToEntity(saved);
```

---

## 5. Service Lookup Rules

### 5.1 Use CEntityRegistry for Service Lookup

**RULE**: Never try to get service by entity class; use CEntityRegistry
```java
// ✅ CORRECT - Registry-based lookup
final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
if (serviceClass == null) {
    throw new IllegalStateException("No service found for: " + entityClass);
}
final CAbstractService service = (CAbstractService) CSpringContext.getBean(serviceClass);

// ❌ WRONG - Direct class lookup
final CAbstractService service = CSpringContext.getBean(entityClass);  // Fails!
```

**Rationale**: 
- Spring beans are registered by service class, not entity class
- CEntityRegistry maintains entity → service mapping
- Enables cross-type operations (copy Activity to Meeting)

### 5.2 Null Check Service Class

**RULE**: Always check if service class is found
```java
// ✅ CORRECT
final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
if (serviceClass == null) {
    throw new IllegalStateException("No service found for entity: " + 
        entityClass.getSimpleName());
}

// ❌ WRONG
final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
// Directly use without checking - NullPointerException risk
```

**Rationale**: Fail fast with clear error message instead of cryptic NPE

---

## 6. Navigation Rules

### 6.1 Use CDynamicPageRouter.navigateToEntity()

**RULE**: Always use CDynamicPageRouter for entity navigation
```java
// ✅ CORRECT
final CEntityDB savedEntity = service.save(entity);
CDynamicPageRouter.navigateToEntity(savedEntity);

// ❌ WRONG
UI.getCurrent().navigate("/activities/" + entity.getId());  // Hardcoded
// Or trying to manually construct routes
```

**What CDynamicPageRouter Does**:
1. Gets entity's VIEW_NAME field
2. Looks up CPageEntity by view name and project
3. Constructs route: `cdynamicpagerouter/page:{pageId}&item:{entityId}`
4. Navigates to route
5. Entity is automatically selected in grid

**Rationale**: 
- Routes are dynamic, not hardcoded
- Works for all entity types
- Automatically selects entity in target page

### 6.2 Navigation After Save

**RULE**: Navigate after successful save, handle errors before navigation
```java
// ✅ CORRECT
try {
    final CEntityDB saved = service.save(entity);
    CDynamicPageRouter.navigateToEntity(saved);
    CNotificationService.showSuccess("Entity saved successfully");
} catch (Exception e) {
    CNotificationService.showException("Error saving entity", e);
    // Don't navigate on error
}

// ❌ WRONG
CDynamicPageRouter.navigateToEntity(entity);  // Before save!
service.save(entity);
```

**Rationale**: Only navigate after successful save; entity must have ID for navigation

---

## 7. CopyTo Pattern Rules

### 7.1 Mandatory copyEntityTo() Implementation

**RULE**: ALL entities MUST implement copyEntityTo()
```java
// ✅ CORRECT - Every entity has this
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof MyEntity) {
        final MyEntity targetEntity = (MyEntity) target;
        
        // Copy basic fields
        copyField(this::getField1, targetEntity::setField1);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetEntity::setDueDate);
        }
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getParent, targetEntity::setParent);
        }
    }
}

// ❌ WRONG - Missing implementation
// Entity without copyEntityTo() method
```

**Rationale**: 
- Enables both same-type and cross-type copying
- Consistent pattern across all entities
- See: `docs/architecture/COPY_TO_PATTERN_CODING_RULE.md`

### 7.2 Field Copying Rules

**ALWAYS Copy**:
- Basic data fields (name, description, notes)
- Numeric fields (amounts, quantities)
- Boolean flags (except security/state)
- Enum values (type, category, priority)

**CONDITIONAL Copy** (check options):
- Dates: Only if `!options.isResetDates()`
- Relations: Only if `options.includesRelations()`
- Status: Only if `options.isCloneStatus()`
- Workflow: Only if `options.isCloneWorkflow()`

**NEVER Copy**:
- ID fields (auto-generated)
- Passwords / tokens (security)
- Session data (temporary)
- Audit fields (createdBy, lastModifiedBy) - set by system
- Unique constraints (must be made unique)

### 7.3 Handle Unique Fields

**RULE**: Make unique fields unique during copy
```java
// ✅ CORRECT - Make unique
if (this.getEmail() != null) {
    targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
}
if (this.getLogin() != null) {
    targetEntity.setLogin(this.getLogin() + "_copy");
}

// ❌ WRONG - Direct copy
copyField(this::getEmail, targetEntity::setEmail);  // Constraint violation!
```

**Rationale**: Avoid database constraint violations on unique fields

---

## 8. Exception Handling Rules

### 8.1 User-Triggered vs System Operations

**RULE**: Only user-triggered actions show exceptions to users
```java
// ✅ CORRECT - User-triggered (button click)
private void on_save_clicked() {
    try {
        service.save(entity);
        CNotificationService.showSuccess("Saved successfully");
    } catch (Exception e) {
        LOGGER.error("Error saving: {}", e.getMessage(), e);
        CNotificationService.showException("Error saving entity", e);
    }
}

// ✅ CORRECT - System operation (service layer)
public void internalProcess() throws Exception {
    // Let exception bubble up
    validateEntity(entity);
    repository.save(entity);
    // No notification here
}

// ❌ WRONG - System operation showing UI notification
public void serviceMethod() {
    try {
        repository.save(entity);
    } catch (Exception e) {
        CNotificationService.showError("Error");  // Wrong layer!
    }
}
```

**Rationale**: 
- UI layer handles user notifications
- Service/repository layers throw exceptions
- Separation of concerns

### 8.2 Log Once, Rethrow

**RULE**: Log error with context, then rethrow; don't swallow
```java
// ✅ CORRECT
public void process() throws Exception {
    try {
        dangerousOperation();
    } catch (Exception e) {
        LOGGER.error("Error in process: {}", e.getMessage(), e);
        throw e;  // Rethrow
    }
}

// ❌ WRONG - Silent failure
public void process() {
    try {
        dangerousOperation();
    } catch (Exception e) {
        // Silently swallowed!
    }
}
```

**Rationale**: Exceptions should surface, not be hidden; log for debugging

---

## 9. Component ID and Selector Rules

### 9.1 Stable IDs for UI Automation

**RULE**: All interactive components must have stable IDs
```java
// ✅ CORRECT
button.setId("custom-save-button");
textField.setId("custom-username-input");
grid.setId("custom-activities-grid");

// ❌ WRONG
button.setId("btn" + System.currentTimeMillis());  // Dynamic!
// Or no ID at all
```

**Rationale**: Playwright tests need stable selectors

### 9.2 ID Naming Convention

**Format**: `custom-{entity}-{component}-{action}`
- `custom-activity-save-button`
- `custom-user-name-input`
- `custom-meeting-grid`

**Rationale**: 
- "custom-" prefix avoids conflicts with Vaadin IDs
- Descriptive and searchable
- Easy to find in tests

---

## 10. Responsive Design Rules

### 10.1 Max-Width + WidthFull Pattern

**RULE**: Use max-width with widthFull for responsive layouts
```java
// ✅ CORRECT - Responsive
component.setMaxWidth("600px");
component.setWidthFull();

// Desktop: 600px (respects max)
// Tablet: 100% (smaller than 600px)
// Mobile: 100% (fills screen)

// ❌ WRONG - Fixed
component.setWidth("600px");  // Fixed, cuts off on mobile
```

### 10.2 Grid Column Behavior

**RULE**: Use appropriate column sizing for grids
```java
// ✅ CORRECT - Flexible columns
grid.addColumn("name").setFlexGrow(1);  // Expands
grid.addColumn("status").setWidth("120px");  // Fixed
grid.addColumn("date").setWidth("150px");  // Fixed

// ❌ WRONG - All fixed or all flexible
grid.addColumn("name").setWidth("200px");  // No flexibility
grid.addColumn("description").setWidth("300px");
```

**Rationale**: Balance between fixed important columns and flexible content

---

## 11. Testing Support Rules

### 11.1 Provide Test Helpers

**RULE**: Complex operations should have test-friendly helper methods
```java
// ✅ CORRECT
public class CEntityService {
    
    // Public method for tests
    public CEntity createTestEntity(String name) {
        final CEntity entity = newEntity();
        entity.setName(name);
        initializeNewEntity(entity);
        return save(entity);
    }
}

// ❌ WRONG
// Tests have to mock 5 method calls to create one entity
```

### 11.2 Expose Necessary Components

**RULE**: UI components needed for testing should be accessible
```java
// ✅ CORRECT
public CButton getSaveButton() {
    return saveButton;
}

// Tests can verify button state
toolbar.getSaveButton().isEnabled();

// ❌ WRONG
private CButton saveButton;  // No getter
// Tests can't verify button state
```

---

## 12. Documentation Rules

### 12.1 JavaDoc for Public Methods

**RULE**: All public methods must have JavaDoc
```java
// ✅ CORRECT
/**
 * Copies this entity to a target entity type with specified options.
 * 
 * @param targetClass the target entity class
 * @param options copy options (dates, relations, status, etc.)
 * @return the copied entity instance
 * @throws Exception if copy fails
 */
public <T extends CEntityDB<?>> T copyTo(Class<T> targetClass, CCloneOptions options) 
    throws Exception {
    // Implementation
}

// ❌ WRONG
public <T extends CEntityDB<?>> T copyTo(Class<T> targetClass, CCloneOptions options) 
    throws Exception {
    // No documentation
}
```

### 12.2 Update Documentation

**RULE**: When changing patterns, update relevant docs
```
Code change → Update:
- AGENTS.md (if coding rule change)
- NEW_ENTITY_COMPLETE_CHECKLIST.md (if entity requirement)
- Relevant docs/architecture/*.md
- Relevant docs/implementation/*.md
```

---

## 13. Summary Checklist

When implementing any new feature, verify:

### Dialog UI:
- [ ] Max-width 600px + widthFull
- [ ] Custom gap spacing (12px sections, 8px items)
- [ ] 2-column layout for 6+ items
- [ ] Select All affects ALL checkboxes equally
- [ ] No unnecessary dividers or wrappers

### Entity Selection:
- [ ] CComboBox with CEntityRegistry
- [ ] Item labels use ENTITY_TITLE_SINGULAR
- [ ] "Same as Source" as first item (if applicable)

### Name Generation:
- [ ] Use service.newEntity() for name
- [ ] Update name on type change
- [ ] Never hardcode names

### Entity Operations:
- [ ] Call initializeNewEntity() before save
- [ ] Use CEntityRegistry for service lookup
- [ ] Check service class for null
- [ ] Navigate after successful save

### CopyTo Pattern:
- [ ] Entity has copyEntityTo() method
- [ ] Calls super.copyEntityTo() first
- [ ] Handles unique fields
- [ ] Respects copy options

### Error Handling:
- [ ] User actions show exceptions in UI
- [ ] Service methods throw exceptions
- [ ] Log once, rethrow

### Testing:
- [ ] Stable component IDs
- [ ] Test helper methods provided
- [ ] Components accessible for tests

---

**Reference Documents**:
- `docs/architecture/COPY_TO_PATTERN_CODING_RULE.md` - Complete copyTo specification
- `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md` - Entity requirements
- `docs/architecture/coding-standards.md` - General coding standards
- `AGENTS.md` - AI agent rules

**Status**: ✅ MANDATORY - All new code must follow these rules
**Last Updated**: 2026-01-18
