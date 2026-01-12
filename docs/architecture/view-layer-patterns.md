# View Layer Patterns

## Overview

Derbent's view layer uses Vaadin Flow for server-side rendering with type-safe Java components. This document describes the view architecture, component patterns, and UI development best practices.

## View Architecture

The view layer follows a hierarchical structure:

```
CAbstractPage (Base UI component)
    ↓
CAbstractEntityDBPage (Entity-aware page)
    ↓
CAbstractNamedEntityPage (Named entity pages)
    ↓
[Domain Pages/Views]
```

## Core View Components

### CAbstractPage

**Location**: `src/main/java/tech/derbent/api/views/CAbstractPage.java`

**Purpose**: Base class for all pages and views.

**Key Features**:
- Vaadin component inheritance
- Route configuration support
- Layout management
- Navigation helpers
- Logger integration

**Usage Pattern**:
```java
@Route(value = "my-view", layout = MainLayout.class)
@PageTitle("My View")
@RolesAllowed("USER")
public class CMyView extends CAbstractPage {
    
    public CMyView() {
        initializeComponents();
        configureLayout();
    }
    
    private void initializeComponents() {
        // Initialize UI components
    }
    
    private void configureLayout() {
        // Configure layout
    }
}
```

### CAbstractEntityDBPage

**Location**: `src/main/java/tech/derbent/api/views/CAbstractEntityDBPage.java`

**Purpose**: Page for displaying and editing database entities.

**Key Features**:
- Service injection
- Entity CRUD operations
- Form binding
- Validation handling
- Save/cancel/delete actions

**Usage Pattern**:
```java
@Route(value = "activities", layout = MainLayout.class)
@RolesAllowed("USER")
public class CActivityPage extends CAbstractEntityDBPage<CActivity> {
    
    private final CActivityService service;
    
    public CActivityPage(CActivityService service) {
        super(service);
        this.service = service;
    }
    
    @Override
    protected void configureForm() {
        // Add form fields
        form.add(nameField, descriptionField, statusComboBox);
    }
}
```

## Grid Patterns

### CGrid - Enhanced Data Grid

**Location**: `src/main/java/tech/derbent/api/views/grids/CGrid.java`

**Purpose**: Extended Vaadin Grid with enhanced features.

**Key Features**:
- Automatic column configuration
- Entity collection rendering
- Sorting and filtering
- Export functionality
- Responsive design

**Usage Pattern**:
```java
public class CActivityGrid extends CGrid<CActivity> {
    
    public CActivityGrid(CActivityService service) {
        super(CActivity.class);
        
        // Add columns
        addColumn(CActivity::getName)
            .setHeader("Name")
            .setSortable(true);
        
        addColumn(activity -> activity.getStatus().getName())
            .setHeader("Status")
            .setSortable(true);
        
        // Entity collection column
        addColumnEntityCollection(
            CActivity::getAssignedUsers,
            "Assigned To");
        
        // Configure grid
        setItems(service.findAll());
        setSelectionMode(SelectionMode.SINGLE);
    }
}
```

### Grid Configuration Patterns

```java
// Basic grid setup
grid.addColumn(Entity::getName).setHeader("Name");
grid.addColumn(Entity::getDescription).setHeader("Description");

// Formatted columns
grid.addColumn(entity -> 
    entity.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
    .setHeader("Created");

// Custom renderer
grid.addColumn(new ComponentRenderer<>(entity -> {
    Span status = new Span(entity.getStatus().getName());
    status.getStyle().set("color", entity.getStatus().getColor());
    return status;
})).setHeader("Status");

// Collection column
grid.addColumnEntityCollection(
    Entity::getTags,
    "Tags");
```

## Form Patterns

### Metadata-Driven Forms

Forms are automatically generated from entity metadata:

```java
// Entity with @AMetaData annotations
@Column(nullable = false)
@Size(max = 255)
@AMetaData(
    displayName = "Activity Name",
    required = true,
    readOnly = false,
    description = "Name of the activity",
    
    maxLength = 255
)
private String name;

// Auto-generated form field
TextField nameField = new TextField("Activity Name");
nameField.setRequired(true);
nameField.setMaxLength(255);
nameField.setHelperText("Name of the activity");
```

### Binder Pattern

```java
public class CActivityForm extends FormLayout {
    
    private final Binder<CActivity> binder;
    private TextField nameField;
    private TextArea descriptionField;
    private ComboBox<CProjectItemStatus> statusComboBox;
    
    public CActivityForm(CActivityService service) {
        binder = new Binder<>(CActivity.class);
        
        createFields();
        configureBindings();
        configureLayout();
    }
    
    private void createFields() {
        nameField = new TextField("Name");
        nameField.setRequired(true);
        
        descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(2000);
        
        statusComboBox = new ComboBox<>("Status");
        statusComboBox.setItemLabelGenerator(CProjectItemStatus::getName);
    }
    
    private void configureBindings() {
        binder.forField(nameField)
            .asRequired("Name is required")
            .withValidator(name -> name.length() <= 255,
                         "Name too long")
            .bind(CActivity::getName, CActivity::setName);
        
        binder.forField(descriptionField)
            .bind(CActivity::getDescription, CActivity::setDescription);
        
        binder.forField(statusComboBox)
            .bind(CActivity::getStatus, CActivity::setStatus);
    }
    
    public void setActivity(CActivity activity) {
        binder.setBean(activity);
    }
    
    public boolean validate() {
        return binder.validate().isOk();
    }
}
```

## Component Patterns

### CButton - Enhanced Button

```java
public class CButton extends Button {
    
    public CButton(String text, VaadinIcon icon) {
        super(text);
        setIcon(icon.create());
        addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }
    
    public void setSuccess() {
        removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }
}
```

### Component Factory Pattern

```java
public class ComponentFactory {
    
    public static ComboBox<CProjectItemStatus> createStatusComboBox(
        CProjectItemStatusService service,
        CProject project) {
        
        ComboBox<CProjectItemStatus> comboBox = 
            new ComboBox<>("Status");
        comboBox.setItems(service.listByProject(project));
        comboBox.setItemLabelGenerator(CProjectItemStatus::getName);
        comboBox.setRenderer(new ComponentRenderer<>(status -> {
            Span span = new Span(status.getName());
            span.getStyle()
                .set("color", status.getColor())
                .set("font-weight", "bold");
            return span;
        }));
        
        return comboBox;
    }
}
```

## Dialog Patterns

### Confirmation Dialog

```java
public class ConfirmationDialog extends Dialog {
    
    public ConfirmationDialog(
        String title,
        String message,
        Runnable onConfirm) {
        
        setHeaderTitle(title);
        
        Div content = new Div(new Text(message));
        add(content);
        
        Button confirmButton = new Button("Confirm", e -> {
            onConfirm.run();
            close();
        });
        confirmButton.addThemeVariants(
            ButtonVariant.LUMO_PRIMARY,
            ButtonVariant.LUMO_ERROR);
        
        Button cancelButton = new Button("Cancel", e -> close());
        
        getFooter().add(cancelButton, confirmButton);
    }
}
```

## Routing and Navigation

### Route Configuration

```java
@Route(value = "activities", layout = MainLayout.class)
@PageTitle("Activities")
@RolesAllowed("USER")
public class CActivityView extends CAbstractPage {
    // View implementation
}

// With parameter
@Route(value = "activity/:id", layout = MainLayout.class)
@PageTitle("Activity Details")
@RolesAllowed("USER")
public class CActivityDetailView extends CAbstractPage 
    implements HasUrlParameter<Long> {
    
    @Override
    public void setParameter(BeforeEvent event, Long activityId) {
        // Load and display activity
    }
}
```

For profile-specific routes (ex: bab vs derbent), set `registerAtStartup = false` on the `@Route` and register the route in a profile-scoped
`VaadinServiceInitListener` using `RouteConfiguration.forApplicationScope()`. This prevents ambiguous route registrations when both products share the
same route path.

### Navigation

```java
// Navigate to route
UI.getCurrent().navigate("activities");

// Navigate with parameter
UI.getCurrent().navigate(CActivityDetailView.class, activityId);

// Navigate with query parameters
RouteParameters params = new RouteParameters(
    "id", activityId.toString(),
    "mode", "edit"
);
UI.getCurrent().navigate(CActivityDetailView.class, params);
```

## Best Practices

### 1. Use Service Injection

```java
// ✅ CORRECT - Constructor injection
public class CActivityView extends CAbstractPage {
    private final CActivityService service;
    
    public CActivityView(CActivityService service) {
        this.service = service;
    }
}

// ❌ INCORRECT - Field injection
public class CActivityView extends CAbstractPage {
    @Autowired
    private CActivityService service;
}
```

### 2. Separate Concerns

```java
// ✅ GOOD - Separate initialization
public CActivityView(CActivityService service) {
    this.service = service;
    
    initializeComponents();
    configureBindings();
    configureLayout();
    loadData();
}

private void initializeComponents() {
    grid = new CGrid<>(CActivity.class);
    form = new CActivityForm(service);
}

private void configureLayout() {
    HorizontalLayout toolbar = createToolbar();
    add(toolbar, grid, form);
}
```

### 3. Handle Errors Gracefully

```java
private void saveActivity() {
    try {
        if (!form.validate()) {
            Notification.show("Please fix validation errors",
                            3000, Position.MIDDLE);
            return;
        }
        
        CActivity activity = form.getActivity();
        service.save(activity);
        
        Notification.show("Activity saved successfully",
                        3000, Position.BOTTOM_START);
        refresh();
        
    } catch (Exception e) {
        LOGGER.error("Error saving activity", e);
        Notification.show("Error: " + e.getMessage(),
                        5000, Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
```

### 4. Use Proper Security

```java
@Route("admin/users")
@PageTitle("User Management")
@RolesAllowed("ADMIN")  // Only admins can access
public class CUserManagementView extends CAbstractPage {
    // Admin-only view
}

@Route("activities")
@PageTitle("Activities")
@PermitAll  // All authenticated users
public class CActivityView extends CAbstractPage {
    // Public view
}
```

### 5. Responsive Design

```java
public CActivityView() {
    // Use responsive layouts
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidthFull();
    layout.setFlexGrow(2, grid);
    layout.setFlexGrow(1, form);
    
    // Mobile responsive
    if (UIUtils.isMobile()) {
        layout = new VerticalLayout();
    }
    
    add(layout);
}
```

### 6. Leverage Vaadin Components

```java
// Rich text editor
RichTextEditor editor = new RichTextEditor();
editor.setValue(activity.getDescription());

// Date picker with constraints
DatePicker datePicker = new DatePicker("Due Date");
datePicker.setMin(LocalDate.now());
datePicker.setMax(LocalDate.now().plusYears(1));

// Upload component
Upload upload = new Upload();
upload.setAcceptedFileTypes("image/*", ".pdf");
upload.setMaxFiles(5);
```

## Testing Views

### UI Unit Test

```java
@ExtendWith(MockitoExtension.class)
class CActivityViewTest {
    
    @Mock
    private CActivityService service;
    
    @Test
    void testViewInitialization() {
        CActivityView view = new CActivityView(service);
        
        assertNotNull(view);
        verify(service).findAll();
    }
}
```

### Playwright Integration Test

See [Playwright Test Guide](../implementation/PLAYWRIGHT_TEST_GUIDE.md) for comprehensive UI testing.

## Related Documentation

- [Entity Inheritance Patterns](entity-inheritance-patterns.md)
- [Service Layer Patterns](service-layer-patterns.md)
- [Playwright Test Guide](../implementation/PLAYWRIGHT_TEST_GUIDE.md)
- [Coding Standards](coding-standards.md)
