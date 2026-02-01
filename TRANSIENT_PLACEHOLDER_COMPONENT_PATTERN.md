# @Transient Placeholder Pattern for Custom Components (MANDATORY)

**Version**: 1.0  
**Date**: 2026-02-01  
**Status**: MANDATORY CODING RULE  
**Enforcement**: Code review rejection for non-compliance  

---

## Overview

**RULE**: When using `CComponentBase<T>` components within CFormBuilder-generated forms, use the **@Transient placeholder pattern** to enable proper Vaadin binding.

This pattern allows custom components to be integrated into entity detail forms via metadata-driven form generation while maintaining full entity access.

---

## When to Use This Pattern

### ✅ REQUIRED when ALL conditions are met:

1. Component extends `CComponentBase<T>` (value-bound component)
2. Component is created via `createComponentMethod` in `@AMetaData`
3. Component needs access to the full entity (not just a single field)
4. Entity form is generated via `CFormBuilder.buildForm()`

### ✅ Use Cases:

- **Complex custom components**: Status displays, Kanban boards, multi-field editors
- **Components with action buttons**: Restart, refresh, delete, configure
- **Components with real-time updates**: Service status, device monitoring, live dashboards
- **Components managing multiple fields**: Interface lists, device configurations, settings panels

### ❌ DON'T use when:

- Simple field editing (use CFormBuilder auto-generation instead)
- Component extends `CVerticalLayout`/`CHorizontalLayout` directly (not `CComponentBase`)
- Component doesn't need entity binding
- Standalone component not within a form

---

## Complete Pattern (5-Step Implementation)

### Step 1: Add @Transient Placeholder Field in Entity

```java
@Entity
@Table(name = "csystem_settings_bab")
public class CSystemSettings_Bab extends CSystemSettings<CSystemSettings_Bab> {
    
    // Real persistent fields
    @Column(name = "enable_calimero_service", nullable = false)
    @AMetaData(displayName = "Enable Calimero Service", hidden = true)
    private Boolean enableCalimeroService = Boolean.FALSE;
    
    @Column(name = "calimero_executable_path", length = 500)
    @AMetaData(displayName = "Calimero Executable Path", hidden = true)
    private String calimeroExecutablePath = "~/git/calimero/build/calimero";
    
    // STEP 1: Transient placeholder field for custom component
    @AMetaData(
        displayName = "Calimero Service Status",  // UI section/field name
        required = false,
        readOnly = false,
        description = "Current status of the Calimero service (managed internally)",
        hidden = false,  // ✅ Component should be visible in form
        dataProviderBean = "pageservice",  // ✅ MANDATORY - tells framework to look in PageService
        createComponentMethod = "createComponentCComponentCalimeroStatus",  // ✅ Method name in PageService
        captionVisible = false  // ✅ Hide field label (component has its own title)
    )
    @Transient  // ✅ MANDATORY - not persisted to database
    private CSystemSettings_Bab placeHolder_ccomponentCalimeroStatus = null;
    
    // Other fields...
}
```

**Field Naming Convention**:
- **Prefix**: `placeHolder_` (indicates transient placeholder)
- **Suffix**: Component name in camelCase (e.g., `ccomponentCalimeroStatus`)
- **Type**: Same as entity class (e.g., `CSystemSettings_Bab`)
- **Initialization**: `= null` (never used directly)
- **Modifier**: NO `final` (Vaadin Binder needs setter)

**@AMetaData Requirements**:
| Attribute | Value | Purpose |
|-----------|-------|---------|
| `displayName` | `"Component Name"` | UI section title (if `captionVisible=true`) |
| `hidden` | `false` | Component should be visible in form |
| `dataProviderBean` | `"pageservice"` | ✅ MANDATORY - directs to PageService |
| `createComponentMethod` | `"createComponentXxx"` | ✅ MANDATORY - factory method name |
| `captionVisible` | `false` | Hide Vaadin field label (component has its own) |
| `required` | `false` | Usually false for components |
| `readOnly` | `false` | Usually false for interactive components |

### Step 2: Add Getter that Returns Entity Itself

```java
/**
 * Getter for transient placeholder field - returns entity itself for component binding.
 * Following CKanbanLine pattern: transient field with getter returning 'this'.
 * 
 * CRITICAL: Binder needs this getter to bind the component.
 * Component receives full entity via setValue(entity).
 * 
 * @return this entity (for CFormBuilder binding to CComponentCalimeroStatus)
 */
public CSystemSettings_Bab getPlaceHolder_ccomponentCalimeroStatus() {
    return this;  // ✅ Returns entity itself, NOT the field value!
}

/**
 * Setter for transient placeholder field - required by Vaadin Binder.
 * Does nothing as field is transient and getter returns 'this'.
 * 
 * @param value ignored (placeholder field is not used)
 */
public void setPlaceHolder_ccomponentCalimeroStatus(final CSystemSettings_Bab value) {
    this.placeHolder_ccomponentCalimeroStatus = value;  // ✅ Assigned but never used
}
```

**Why return `this`**:
1. Vaadin Binder calls `getPlaceHolder_ccomponentCalimeroStatus()`
2. Returns the **full entity** (not the placeholder `null` value)
3. Component's `setValue(entity)` is called with complete entity
4. Component can access **all entity fields** via `getValue()`

**Pattern Origin**: Based on `CKanbanLine` pattern (first documented usage)

### Step 3: Create Component Factory Method in PageService

```java
@Service
public class CPageServiceSystemSettings_Bab extends CPageServiceSystemSettings<CSystemSettings_Bab> {
    
    private final CCalimeroProcessManager calimeroProcessManager;
    
    public CPageServiceSystemSettings_Bab(
            final IPageServiceImplementer<CSystemSettings_Bab> view,
            final CCalimeroProcessManager calimeroProcessManager) {
        super(view);
        this.calimeroProcessManager = calimeroProcessManager;
    }
    
    /**
     * Creates custom Calimero status component.
     * Called by CFormBuilder when building form from @AMetaData.
     * 
     * Method name MUST match @AMetaData(createComponentMethod="...").
     * 
     * @return CComponentCalimeroStatus bound to entity
     */
    public Component createComponentCComponentCalimeroStatus() {
        try {
            // Create component with required dependencies
            final CComponentCalimeroStatus component = 
                new CComponentCalimeroStatus(calimeroProcessManager, sessionService);
            
            // Optional: Register listener for value changes (entity updates)
            component.addValueChangeListener(event -> {
                final CSystemSettings_Bab settings = event.getValue();
                LOGGER.debug("Calimero settings changed via component: enabled={}, path={}",
                    settings.getEnableCalimeroService(), settings.getCalimeroExecutablePath());
                // Auto-save or validate as needed
            });
            
            LOGGER.debug("Created Calimero status component");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Failed to create Calimero status component.", e);
            final Div errorDiv = new Div();
            errorDiv.setText("Error loading Calimero status: " + e.getMessage());
            errorDiv.addClassName("error-message");
            return errorDiv;
        }
    }
}
```

**Factory Method Requirements**:
- **Signature**: `public Component createComponentXxx()`
- **Name**: Must match `@AMetaData(createComponentMethod="...")`
- **Return**: `Component` (Vaadin base class)
- **Error Handling**: Return error component (Div with message) instead of throwing
- **Logging**: Debug component creation for troubleshooting

### Step 4: Implement Component Extending CComponentBase

```java
/**
 * CComponentCalimeroStatus - Value-bound component for managing Calimero service.
 * 
 * Pattern: Standard CComponentBase value-bound component with @Transient placeholder.
 * 
 * Entity binding flow:
 * 1. PageService.createComponentCComponentCalimeroStatus() creates component
 * 2. CFormBuilder calls entity.getPlaceHolder_ccomponentCalimeroStatus() (returns entity itself)
 * 3. Binder calls component.setValue(entity)
 * 4. Component can now access all entity fields via getValue()
 */
public class CComponentCalimeroStatus extends CComponentBase<CSystemSettings_Bab> {
    
    // Component IDs for Playwright testing
    public static final String ID_ROOT = "custom-calimero-status-component";
    public static final String ID_ENABLE_CHECKBOX = "custom-calimero-enable-checkbox";
    public static final String ID_EXECUTABLE_PATH_FIELD = "custom-calimero-executable-path";
    public static final String ID_START_STOP_BUTTON = "custom-calimero-start-stop-button";
    
    private final CCalimeroProcessManager calimeroProcessManager;
    private Checkbox checkboxEnableService;
    private TextField textFieldExecutablePath;
    private CButton buttonStartStop;
    
    public CComponentCalimeroStatus(final CCalimeroProcessManager calimeroProcessManager) {
        Check.notNull(calimeroProcessManager, "CalimeroProcessManager cannot be null");
        this.calimeroProcessManager = calimeroProcessManager;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setId(ID_ROOT);
        
        // Enable checkbox - modifies entity field
        checkboxEnableService = new Checkbox("Enable Calimero Service");
        checkboxEnableService.setId(ID_ENABLE_CHECKBOX);
        checkboxEnableService.addValueChangeListener(event -> on_enableServiceChanged(event.getValue()));
        
        // Path field - modifies entity field
        textFieldExecutablePath = new TextField("Executable Path");
        textFieldExecutablePath.setId(ID_EXECUTABLE_PATH_FIELD);
        textFieldExecutablePath.addValueChangeListener(event -> on_pathChanged(event.getValue()));
        
        // Start/Stop button - operates on service, not entity
        buttonStartStop = CButton.createPrimary("Start", VaadinIcon.PLAY.create(), 
            event -> on_startStopClicked());
        buttonStartStop.setId(ID_START_STOP_BUTTON);
        
        add(checkboxEnableService, textFieldExecutablePath, buttonStartStop);
    }
    
    private void on_enableServiceChanged(final Boolean enabled) {
        final CSystemSettings_Bab entity = getValue();  // ✅ Get full entity
        entity.setEnableCalimeroService(enabled);  // ✅ Modify entity field
        updateValueFromClient(entity);  // ✅ Fire change event to Binder
    }
    
    private void on_pathChanged(final String path) {
        final CSystemSettings_Bab entity = getValue();
        entity.setCalimeroExecutablePath(path);
        updateValueFromClient(entity);
    }
    
    /**
     * Override from CComponentBase - Update UI when value changes.
     * Called when setValue() is called or when updateValueFromClient() fires.
     */
    @Override
    protected void onValueChanged(final CSystemSettings_Bab oldValue, 
                                  final CSystemSettings_Bab newValue, 
                                  final boolean fromClient) {
        LOGGER.debug("Entity changed: fromClient={}", fromClient);
        
        // Update UI from entity
        if (newValue != null) {
            checkboxEnableService.setValue(newValue.getEnableCalimeroService());
            textFieldExecutablePath.setValue(newValue.getCalimeroExecutablePath());
        }
        
        // Refresh service status (only on programmatic changes)
        if (!fromClient) {
            refreshServiceStatus();
        }
    }
    
    private void refreshServiceStatus() {
        final CCalimeroServiceStatus status = calimeroProcessManager.getCurrentStatus();
        updateButtonState(status);
    }
}
```

**CComponentBase Benefits**:
- ✅ **Full HasValue interface** implementation
- ✅ **Value change listeners** with `fromClient` flag
- ✅ **Automatic Binder integration** via `setValue()`/`getValue()`
- ✅ **ReadOnly mode** support
- ✅ **Required indicator** support

### Step 5: Add Component Field to Initializer

```java
public final class CSystemSettings_BabInitializerService extends CInitializerServiceBase {
    
    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        final Class<?> clazz = CSystemSettings_Bab.class;
        final CDetailSection scr = createBaseScreenEntity(project, clazz);
        
        // Application Configuration Section
        scr.addScreenLine(CDetailLinesService.createSection("Application Configuration"));
        
        // STEP 5: Add placeholder field to form
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, 
            "placeHolder_ccomponentCalimeroStatus"));  // ✅ Field name matches entity
        
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationName"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "applicationDescription"));
        
        // ... other fields
        
        return scr;
    }
}
```

**Initializer Requirements**:
- **Field name**: Must match entity field name exactly
- **Order**: Place where component should appear in form
- **Section**: Typically has its own section or is first in a section

---

## Complete Example: Interface List Component (Dashboard)

```java
// === ENTITY ===
@Entity
public class CDashboardProject_Bab extends CDashboardProject<CDashboardProject_Bab> {
    
    @AMetaData(
        displayName = "Interface List",
        required = false,
        readOnly = false,
        description = "Network interface configuration for this dashboard",
        hidden = false,
        dataProviderBean = "pageservice",
        createComponentMethod = "createComponentInterfaceList",
        captionVisible = false
    )
    @Transient
    private CDashboardProject_Bab placeHolder_createComponentInterfaceList = null;
    
    // ✅ MANDATORY getter - returns entity itself
    public CDashboardProject_Bab getPlaceHolder_createComponentInterfaceList() {
        return this;
    }
    
    // ✅ MANDATORY setter - required by Binder
    public void setPlaceHolder_createComponentInterfaceList(
            final CDashboardProject_Bab value) {
        this.placeHolder_createComponentInterfaceList = value;
    }
}

// === PAGE SERVICE ===
@Profile("bab")
public class CPageServiceDashboardProject_Bab 
        extends CPageServiceDynamicPage<CDashboardProject_Bab> {
    
    private final ISessionService sessionService;
    
    public Component createComponentInterfaceList() {
        try {
            return new CComponentInterfaceList(sessionService);
        } catch (final Exception e) {
            LOGGER.error("Error creating interface list: {}", e.getMessage());
            return CDiv.errorDiv("Failed to load interface list: " + e.getMessage());
        }
    }
}

// === INITIALIZER ===
public final class CDashboardProject_BabInitializerService 
        extends CInitializerServiceBase {
    
    public static CDetailSection createBasicView(final CProject<?> project) 
            throws Exception {
        final Class<?> clazz = CDashboardProject_Bab.class;
        final CDetailSection scr = createBaseScreenEntity(project, clazz);
        
        scr.addScreenLine(CDetailLinesService.createSection("Basic Information"));
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, 
            "placeHolder_createComponentInterfaceList"));  // ✅ Placeholder field
        scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
        
        return scr;
    }
}
```

---

## Pattern Checklist (MANDATORY for Code Review)

### ✅ Entity Class Requirements

- [ ] **@Transient placeholder field** defined
- [ ] **@AMetaData annotation** with all required attributes:
  - [ ] `dataProviderBean = "pageservice"`
  - [ ] `createComponentMethod = "createComponentXxx"`
  - [ ] `captionVisible = false`
  - [ ] `hidden = false`
- [ ] **Placeholder field type** matches entity class
- [ ] **Placeholder field initialized** to `null`
- [ ] **Getter returns `this`** (not field value)
- [ ] **Setter provided** (assigns field but never used)
- [ ] **NO `final` modifier** on placeholder field

### ✅ PageService Requirements

- [ ] **Factory method name** matches `@AMetaData(createComponentMethod="...")`
- [ ] **Return type** is `Component`
- [ ] **Error handling** returns error Div (doesn't throw)
- [ ] **Logging** for component creation
- [ ] **Dependencies** injected via constructor

### ✅ Component Requirements

- [ ] **Extends `CComponentBase<EntityType>`**
- [ ] **Component IDs** defined as `public static final String`
- [ ] **All IDs set** in `initializeComponents()`
- [ ] **getValue()** used to access entity
- [ ] **updateValueFromClient()** called after entity changes
- [ ] **onValueChanged()** overridden to update UI

### ✅ Initializer Requirements

- [ ] **Placeholder field added** to detail section
- [ ] **Field name matches** entity field name
- [ ] **Proper section** for component placement

---

## Common Mistakes

### ❌ WRONG - Missing Getter

```java
@Transient
private CSystemSettings_Bab placeHolder_ccomponentCalimeroStatus = null;

// ❌ MISSING: getter that returns 'this'
// Binder will fail with NullPointerException!
```

### ❌ WRONG - Getter Returns Field Value

```java
public CSystemSettings_Bab getPlaceHolder_ccomponentCalimeroStatus() {
    return placeHolder_ccomponentCalimeroStatus;  // ❌ Returns null!
}
```

### ❌ WRONG - Missing `dataProviderBean`

```java
@AMetaData(
    displayName = "Status",
    createComponentMethod = "createComponentStatus"
    // ❌ MISSING: dataProviderBean = "pageservice"
)
@Transient
private CEntity placeHolder_status = null;
```

### ❌ WRONG - Factory Method Throws Exception

```java
public Component createComponentStatus() {
    return new CComponentStatus(service);  // ❌ If service is null, form breaks!
}

// ✅ CORRECT: Catch exceptions and return error component
public Component createComponentStatus() {
    try {
        return new CComponentStatus(service);
    } catch (final Exception e) {
        LOGGER.error("Failed to create status component", e);
        return CDiv.errorDiv("Error: " + e.getMessage());
    }
}
```

### ❌ WRONG - Field Marked `final`

```java
@Transient
private final CEntity placeHolder_status = null;  // ❌ Binder can't call setter!
```

---

## Benefits of This Pattern

### ✅ Advantages

1. **Full Entity Access**: Component receives complete entity via `getValue()`
2. **Type-Safe Binding**: Vaadin Binder handles all type conversions
3. **Change Tracking**: `fromClient` flag distinguishes user vs programmatic changes
4. **Form Integration**: Seamless integration with CFormBuilder-generated forms
5. **Metadata-Driven**: Component placement via @AMetaData (no code changes)
6. **Dependency Injection**: PageService provides component dependencies
7. **Error Isolation**: Component creation errors don't break entire form

### 🎯 Real-World Use Cases

| Component Type | Entity | Purpose |
|----------------|--------|---------|
| **CComponentCalimeroStatus** | `CSystemSettings_Bab` | Service control panel |
| **CComponentInterfaceList** | `CDashboardProject_Bab` | Network interface configuration |
| **CComponentKanbanBoard** | `CKanbanLine` | Sprint board visualization |
| **CComponentAgileParentSelector** | `CActivity` | Hierarchical parent selection |

---

## Verification Commands

```bash
# Find entities with placeholder fields
grep -r "@Transient" src/main/java --include="*.java" | grep "placeHolder_"

# Check for missing getters
for file in $(grep -l "placeHolder_" src/main/java/*/domain/*.java); do
    entity=$(basename "$file" .java)
    placeholders=$(grep "placeHolder_" "$file" | grep -oP "placeHolder_\w+" | sort -u)
    for ph in $placeholders; do
        getter="public.*get${ph^}"
        if ! grep -q "$getter" "$file"; then
            echo "MISSING GETTER: $entity.$ph"
        fi
    done
done

# Verify @AMetaData has required attributes
grep -B 5 "@Transient" src/main/java --include="*.java" | grep "placeHolder_" -B 5 | \
    grep -L "dataProviderBean.*pageservice"
```

---

## References

- **Original Pattern**: `CKanbanLine` (first documented usage)
- **Complete Example**: `CSystemSettings_Bab` + `CComponentCalimeroStatus`
- **Dashboard Example**: `CDashboardProject_Bab` + `CComponentInterfaceList`
- **AGENTS.md**: Section 3.10 - @Transient Placeholder Pattern

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-01 | Initial pattern documentation |

---

**ENFORCEMENT**: This pattern is MANDATORY. Code reviews MUST reject any placeholder fields missing getters or required @AMetaData attributes.
