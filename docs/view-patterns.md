# View Class Patterns

This document outlines the view layer architecture and UI component patterns used in the Derbent project. Understanding these patterns is essential for creating consistent, maintainable user interfaces and effective GitHub Copilot usage.

## 🏗️ View Inheritance Hierarchy

### Core View Hierarchy
```
CAbstractPage
    ↓
CAbstractEntityDBPage<T>
    ↓
CProjectAwareMDPage<T>
    ↓
CGridViewBaseProject<T>
    ↓
[Your View Classes]
```

### Panel Hierarchy
```
CPanelBase
    ↓
CPanelEntityBase<T>
    ↓
[Your Panel Classes]
```

## 📋 Base View Classes

### CAbstractPage
**Purpose**: Root view class with basic page functionality
**Location**: `tech.derbent.api.views.CAbstractPage`

```java
public abstract class CAbstractPage extends VerticalLayout implements HasDynamicTitle {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(CAbstractPage.class);
    
    // Basic page setup and navigation
    protected void configurePage() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }
    
    // Dynamic title support
    @Override
    public String getPageTitle() {
        return "Derbent";
    }
}
```

### CAbstractEntityDBPage<T>
**Purpose**: Base for entity-focused pages with CRUD operations
**Location**: `tech.derbent.api.views.CAbstractEntityDBPage`

```java
public abstract class CAbstractEntityDBPage<T extends CEntityDB<T>> extends CAbstractPage {
    
    protected final Class<T> entityClass;
    protected final CEntityService<T> entityService;
    
    protected CAbstractEntityDBPage(Class<T> entityClass, CEntityService<T> entityService) {
        this.entityClass = entityClass;
        this.entityService = entityService;
        configurePage();
    }
    
    // Entity-specific operations
    protected abstract void configureGrid();
    protected abstract void configureForm();
    protected void handleEntitySelection(T entity) { /* Default implementation */ }
}
```

### CProjectAwareMDPage<T>
**Purpose**: Project-aware master-detail pages
**Location**: `tech.derbent.api.views.CProjectAwareMDPage`

```java
public abstract class CProjectAwareMDPage<T extends CProjectItem<T>> extends CAbstractEntityDBPage<T> {
    
    protected final CSessionService sessionService;
    protected CProject currentProject;
    
    protected CProjectAwareMDPage(Class<T> entityClass, 
                                CProjectItemService<T> entityService,
                                CSessionService sessionService) {
        super(entityClass, entityService);
        this.sessionService = sessionService;
        initializeProject();
    }
    
    protected void initializeProject() {
        this.currentProject = sessionService.getCurrentProject();
        if (currentProject == null) {
            // Handle no project selected
            showNoProjectWarning();
        }
    }
}
```

### CGridViewBaseProject<T>
**Purpose**: Grid-based views with project awareness
**Location**: `tech.derbent.api.views.grids.CGridViewBaseProject`

```java
public abstract class CGridViewBaseProject<T extends CProjectItem<T>> extends CProjectAwareMDPage<T> {
    
    protected CGrid<T> grid;
    protected CAccordionDBEntity<T> detailAccordion;
    protected HorizontalLayout toolbar;
    
    protected CGridViewBaseProject(Class<T> entityClass,
                                 CProjectItemService<T> entityService,
                                 CSessionService sessionService,
                                 CDetailSectionService screenService) {
        super(entityClass, entityService, sessionService);
        this.screenService = screenService;
        createLayout();
    }
    
    protected void createLayout() {
        createToolbar();
        createGrid();
        createDetailAccordion();
        setupMasterDetail();
    }
    
    // Abstract methods for customization
    public abstract void createGridForEntity(CGrid<T> grid);
}
```

## 🛠️ View Implementation Patterns

### Standard Grid View
```java
@Route("cactivitiesview")
@PageTitle("Activities")
@Menu(order = 1.1, icon = "class:tech.derbent.activities.view.CActivitiesView", title = "Project.Activities")
@PermitAll
public class CActivitiesView extends CGridViewBaseProject<CActivity> {
    
    private static final long serialVersionUID = 1L;
    public static final String VIEW_NAME = "Activities View";
    
    // Static methods for UI integration
    public static String getStaticIconFilename() { return CActivity.getStaticIconFilename(); }
    public static String getStaticIconColorCode() { return CActivity.getStaticIconColorCode(); }
    public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }
    
    private final CCommentService commentService;
    private final String ENTITY_ID_FIELD = "activity_id";
    
    // Constructor injection
    public CActivitiesView(CActivityService entityService,
                          CSessionService sessionService,
                          CCommentService commentService,
                          CDetailSectionService screenService) {
        super(CActivity.class, entityService, sessionService, screenService);
        this.commentService = commentService;
    }
    
    // Grid configuration - required implementation
    @Override
    public void createGridForEntity(CGrid<CActivity> grid) {
        // ID column
        grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
        
        // Project column (for project-aware entities)
        grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
        
        // Basic entity columns
        grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
        grid.addLongTextColumn(CActivity::getDescription, "Description", "description");
        
        // Type and status columns with color support
        grid.addColumnEntityNamed(CActivity::getActivityType, "Type");
        grid.addColumnStatusEntity(CActivity::getStatus, "Status");
        
        // Numeric and date columns
        grid.addColumn(CActivity::getPriority, "Priority", "priority");
        grid.addDateColumn(CActivity::getDueDate, "Due Date", "dueDate");
        grid.addColumn(activity -> activity.getEstimatedHours() + " hrs", "Estimated", "estimatedHours");
        
        // Configure grid behavior
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setColumnReorderingAllowed(true);
        grid.setMultiSort(true);
    }
    
    // Page title override
    @Override
    public String getPageTitle() {
        return "Activities - " + (currentProject != null ? currentProject.getName() : "No Project");
    }
    
    // Custom toolbar actions
    @Override
    protected void createToolbar() {
        super.createToolbar();
        
        // Add custom buttons
        final CButton kanbanButton = new CButton("Kanban View", VaadinIcon.KANBAN.create());
        kanbanButton.addClickListener(e -> navigateToKanban());
        toolbar.add(kanbanButton);
        
        final CButton exportButton = new CButton("Export", VaadinIcon.DOWNLOAD.create());
        exportButton.addClickListener(e -> exportToExcel());
        toolbar.add(exportButton);
    }
    
    // Custom navigation methods
    private void navigateToKanban() {
        getUI().ifPresent(ui -> ui.navigate("cactivitieskanban"));
    }
    
    private void exportToExcel() {
        // Export implementation
        final List<CActivity> activities = ((CActivityService) entityService).listByCurrentProject();
        // Export logic here
    }
    
    // Entity selection handling
    @Override
    protected void handleEntitySelection(CActivity activity) {
        if (activity != null) {
            LOGGER.debug("Activity selected: id={}, name={}", activity.getId(), activity.getName());
            
            // Update detail accordion
            if (detailAccordion != null) {
                detailAccordion.setEntity(activity);
            }
            
            // Load comments if applicable
            loadActivityComments(activity);
        }
    }
    
    private void loadActivityComments(CActivity activity) {
        if (commentService != null) {
            final List<CComment> comments = commentService.findByEntityIdAndType(
                activity.getId(), CActivity.class.getSimpleName());
            // Update comments display
        }
    }
}
```

### Kanban Board View
```java
@Route("cactivitieskanban")
@PageTitle("Activities Kanban")
@Menu(order = 1.2, icon = "vaadin:kanban", title = "Activities.Kanban")
@PermitAll
public class CActivitiesKanbanView extends CProjectAwareMDPage<CActivity> {
    
    private final CActivityService activityService;
    private final CActivityStatusService statusService;
    
    private HorizontalLayout kanbanBoard;
    private Map<CActivityStatus, VerticalLayout> statusColumns;
    
    public CActivitiesKanbanView(CActivityService activityService,
                               CActivityStatusService statusService,
                               CSessionService sessionService) {
        super(CActivity.class, activityService, sessionService);
        this.activityService = activityService;
        this.statusService = statusService;
        createKanbanBoard();
    }
    
    private void createKanbanBoard() {
        kanbanBoard = new HorizontalLayout();
        kanbanBoard.setSizeFull();
        kanbanBoard.setSpacing(true);
        
        createStatusColumns();
        loadActivities();
        
        add(kanbanBoard);
    }
    
    private void createStatusColumns() {
        statusColumns = new HashMap<>();
        final List<CActivityStatus> statuses = statusService.listByCurrentProject();
        
        for (CActivityStatus status : statuses) {
            final VerticalLayout column = createStatusColumn(status);
            statusColumns.put(status, column);
            kanbanBoard.add(column);
        }
    }
    
    private VerticalLayout createStatusColumn(CActivityStatus status) {
        final VerticalLayout column = new VerticalLayout();
        column.setWidth("300px");
        column.setHeightFull();
        column.setSpacing(true);
        column.setPadding(true);
        
        // Column header with status color
        final Div header = new Div();
        header.setText(status.getName());
        header.getStyle()
            .set("background-color", status.getColor())
            .set("color", "white")
            .set("padding", "10px")
            .set("text-align", "center")
            .set("font-weight", "bold");
        
        column.add(header);
        return column;
    }
    
    private void loadActivities() {
        final Map<CActivityStatus, List<CActivity>> groupedActivities = 
            activityService.getActivitiesGroupedByStatus(currentProject);
        
        for (Map.Entry<CActivityStatus, List<CActivity>> entry : groupedActivities.entrySet()) {
            final VerticalLayout column = statusColumns.get(entry.getKey());
            if (column != null) {
                for (CActivity activity : entry.getValue()) {
                    column.add(createActivityCard(activity));
                }
            }
        }
    }
    
    private Component createActivityCard(CActivity activity) {
        final Div card = new Div();
        card.addClassName("activity-card");
        card.getStyle()
            .set("background", "white")
            .set("border", "1px solid #ddd")
            .set("border-radius", "4px")
            .set("padding", "10px")
            .set("margin", "5px 0")
            .set("cursor", "pointer");
        
        final H4 title = new H4(activity.getName());
        title.getStyle().set("margin", "0 0 5px 0");
        
        final Paragraph description = new Paragraph(
            activity.getDescription() != null ? activity.getDescription() : "");
        description.getStyle().set("margin", "0");
        description.getStyle().set("font-size", "0.9em");
        description.getStyle().set("color", "#666");
        
        card.add(title, description);
        
        // Add click listener for activity details
        card.addClickListener(e -> showActivityDetails(activity));
        
        return card;
    }
    
    private void showActivityDetails(CActivity activity) {
        // Open activity detail dialog or navigate to detail view
        final Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Activity Details");
        
        // Add activity form or detail panel
        final CPanelActivity activityPanel = new CPanelActivity();
        activityPanel.setEntity(activity);
        
        dialog.add(activityPanel);
        dialog.open();
    }
}
```

### Form Panel Pattern
```java
public class CPanelActivity extends CPanelEntityBase<CActivity> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPanelActivity.class);
    
    private final CActivityTypeService activityTypeService;
    private final CActivityStatusService activityStatusService;
    
    // Form components
    private TextField nameField;
    private TextArea descriptionField;
    private ComboBox<CActivityType> typeComboBox;
    private ComboBox<CActivityStatus> statusComboBox;
    private IntegerField priorityField;
    private DatePicker dueDatePicker;
    private NumberField estimatedHoursField;
    
    public CPanelActivity(CActivityTypeService activityTypeService,
                         CActivityStatusService activityStatusService) {
        this.activityTypeService = activityTypeService;
        this.activityStatusService = activityStatusService;
        initializePanel();
    }
    
    @Override
    protected void updatePanelEntityFields() {
        // Define fields for automatic form generation
        setEntityFields(List.of(
            "name", "description", "activityType", "status", 
            "priority", "dueDate", "estimatedHours"
        ));
    }
    
    @Override
    protected void configureForm() {
        super.configureForm();
        
        // Get components from enhanced binder
        nameField = (TextField) getFieldComponent("name");
        descriptionField = (TextArea) getFieldComponent("description");
        typeComboBox = (ComboBox<CActivityType>) getFieldComponent("activityType");
        statusComboBox = (ComboBox<CActivityStatus>) getFieldComponent("status");
        priorityField = (IntegerField) getFieldComponent("priority");
        dueDatePicker = (DatePicker) getFieldComponent("dueDate");
        estimatedHoursField = (NumberField) getFieldComponent("estimatedHours");
        
        // Configure specific components
        configureNameField();
        configureDescriptionField();
        configureTypeComboBox();
        configureStatusComboBox();
        configurePriorityField();
        configureDueDatePicker();
        configureEstimatedHoursField();
    }
    
    private void configureNameField() {
        if (nameField != null) {
            nameField.setRequired(true);
            nameField.setMaxLength(100);
            nameField.setHelperText("Enter a descriptive activity name");
            CAuxillaries.setId(nameField); // For testing
        }
    }
    
    private void configureDescriptionField() {
        if (descriptionField != null) {
            descriptionField.setMaxLength(500);
            descriptionField.setHelperText("Optional activity description");
            descriptionField.setHeight("100px");
            CAuxillaries.setId(descriptionField);
        }
    }
    
    private void configureTypeComboBox() {
        if (typeComboBox != null) {
            typeComboBox.setItems(activityTypeService.getItems());
            typeComboBox.setItemLabelGenerator(CActivityType::getName);
            typeComboBox.setHelperText("Select activity type");
            CAuxillaries.setId(typeComboBox);
        }
    }
    
    private void configureStatusComboBox() {
        if (statusComboBox != null) {
            statusComboBox.setItems(activityStatusService.getItems());
            statusComboBox.setItemLabelGenerator(CActivityStatus::getName);
            statusComboBox.setRenderer(createStatusRenderer());
            statusComboBox.setHelperText("Select current status");
            CAuxillaries.setId(statusComboBox);
        }
    }
    
    private ComponentRenderer<Div, CActivityStatus> createStatusRenderer() {
        return new ComponentRenderer<>(status -> {
            final Div div = new Div();
            
            final Span colorIndicator = new Span();
            colorIndicator.getStyle()
                .set("display", "inline-block")
                .set("width", "12px")
                .set("height", "12px")
                .set("background-color", status.getColor())
                .set("border-radius", "50%")
                .set("margin-right", "8px");
            
            final Span nameSpan = new Span(status.getName());
            
            div.add(colorIndicator, nameSpan);
            return div;
        });
    }
    
    private void configurePriorityField() {
        if (priorityField != null) {
            priorityField.setMin(1);
            priorityField.setMax(5);
            priorityField.setValue(3);
            priorityField.setHelperText("Priority: 1 (Low) to 5 (High)");
            CAuxillaries.setId(priorityField);
        }
    }
    
    private void configureDueDatePicker() {
        if (dueDatePicker != null) {
            dueDatePicker.setMin(LocalDate.now());
            dueDatePicker.setHelperText("Optional due date");
            CAuxillaries.setId(dueDatePicker);
        }
    }
    
    private void configureEstimatedHoursField() {
        if (estimatedHoursField != null) {
            estimatedHoursField.setMin(0);
            estimatedHoursField.setMax(9999.99);
            estimatedHoursField.setStep(0.25);
            estimatedHoursField.setHelperText("Estimated hours for completion");
            CAuxillaries.setId(estimatedHoursField);
        }
    }
    
    @Override
    protected void onEntityChanged(CActivity activity) {
        super.onEntityChanged(activity);
        
        if (activity != null) {
            LOGGER.debug("Loading activity into form: id={}, name={}", 
                activity.getId(), activity.getName());
        }
    }
    
    @Override
    protected boolean validateEntity(CActivity activity) {
        boolean valid = super.validateEntity(activity);
        
        // Custom validation logic
        if (activity.getDueDate() != null && activity.getDueDate().isBefore(LocalDate.now())) {
            showErrorNotification("Due date cannot be in the past");
            valid = false;
        }
        
        if (activity.getEstimatedHours() != null && 
            activity.getEstimatedHours().compareTo(BigDecimal.ZERO) < 0) {
            showErrorNotification("Estimated hours cannot be negative");
            valid = false;
        }
        
        return valid;
    }
}
```

## 🎨 UI Component Patterns

### Custom Button Usage
```java
// Always use CButton instead of standard Button
final CButton saveButton = new CButton("Save", VaadinIcon.CHECK.create());
saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
saveButton.addClickListener(e -> saveEntity());
CAuxillaries.setId(saveButton); // For testing

// Icon buttons
final CButton editButton = new CButton(VaadinIcon.EDIT.create());
editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
editButton.setTooltipText("Edit Activity");
```

### Grid Column Configuration
```java
// Standard columns with proper field references
grid.addIdColumn(CEntityDB::getId, "#", "id");
grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
grid.addLongTextColumn(CActivity::getDescription, "Description", "description");

// Entity reference columns
grid.addColumnEntityNamed(CActivity::getActivityType, "Type");
grid.addColumnStatusEntity(CActivity::getStatus, "Status");

// Date and numeric columns  
grid.addDateColumn(CActivity::getDueDate, "Due Date", "dueDate");
grid.addColumn(CActivity::getPriority, "Priority", "priority");

// Custom formatted columns
grid.addColumn(activity -> 
    activity.getEstimatedHours() + " hrs", "Estimated", "estimatedHours");
```

### Dialog Patterns
```java
private void showEntityDialog(CActivity activity) {
    final Dialog dialog = new Dialog();
    dialog.setHeaderTitle(activity.getId() != null ? "Edit Activity" : "New Activity");
    dialog.setModal(true);
    dialog.setResizable(true);
    dialog.setDraggable(true);
    
    // Add form panel
    final CPanelActivity activityPanel = new CPanelActivity(
        activityTypeService, activityStatusService);
    activityPanel.setEntity(activity);
    
    // Add buttons
    final HorizontalLayout buttonLayout = new HorizontalLayout();
    
    final CButton saveButton = new CButton("Save", VaadinIcon.CHECK.create());
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(e -> {
        if (activityPanel.validateAndSave()) {
            dialog.close();
            refreshGrid();
            showSuccessNotification("Activity saved successfully");
        }
    });
    
    final CButton cancelButton = new CButton("Cancel");
    cancelButton.addClickListener(e -> dialog.close());
    
    buttonLayout.add(saveButton, cancelButton);
    
    dialog.add(activityPanel);
    dialog.getFooter().add(buttonLayout);
    dialog.open();
}
```

## 🔐 View Security Patterns

### Route-Level Security
```java
@Route("cactivitiesview")
@PageTitle("Activities") 
@PermitAll                          // Allow all authenticated users
public class CActivitiesView extends CGridViewBaseProject<CActivity> {

@Route("cadminview")
@PageTitle("Administration")
@RolesAllowed("ADMIN")              // Admin only
public class CAdminView extends CAbstractPage {

@Route("cprojectmanagerview")  
@PageTitle("Project Management")
@RolesAllowed({"ADMIN", "PROJECT_MANAGER"})  // Multiple roles
public class CProjectManagerView extends CAbstractPage {
```

### Component-Level Security
```java
@Override
protected void createToolbar() {
    super.createToolbar();
    
    // Only show admin buttons to admins
    if (securityService.hasRole("ADMIN")) {
        final CButton adminButton = new CButton("Admin Functions");
        adminButton.addClickListener(e -> showAdminFunctions());
        toolbar.add(adminButton);
    }
    
    // Project-specific permissions
    if (securityService.hasProjectAccess(currentProject)) {
        final CButton editButton = new CButton("Edit Project");
        editButton.addClickListener(e -> editProject());
        toolbar.add(editButton);
    }
}
```

## 🎯 Menu Integration Patterns

### Menu Configuration
```java
@Menu(
    order = 1.1,                                    // Menu position
    icon = "class:tech.derbent.activities.view.CActivitiesView",  // Dynamic icon
    title = "Project.Activities"                    // I18n key
)
```

### Hierarchical Menu Structure
```java
// Main menu item
@Menu(order = 1, icon = "vaadin:tasks", title = "Activities")

// Sub-menu items  
@Menu(order = 1.1, icon = "class:...", title = "Activities.List")
@Menu(order = 1.2, icon = "vaadin:kanban", title = "Activities.Kanban")
@Menu(order = 1.3, icon = "vaadin:chart", title = "Activities.Reports")
```

## 🚫 View Anti-Patterns

### Prohibited Practices
```java
// ❌ Don't use standard Vaadin components directly
Button button = new Button("Save");         // Wrong
CButton button = new CButton("Save");       // Correct

// ❌ Don't hardcode component IDs
button.setId("save-button");               // Wrong  
CAuxillaries.setId(button);               // Correct

// ❌ Don't ignore project context
List<CActivity> activities = activityService.findAll();  // Wrong
List<CActivity> activities = activityService.listByCurrentProject();  // Correct

// ❌ Don't create components without proper styling
Div div = new Div();                       // Missing styling
```

### Required Practices
```java
// ✅ Always use project-aware services
final List<CActivity> activities = activityService.listByCurrentProject();

// ✅ Always set component IDs for testing
CAuxillaries.setId(component);

// ✅ Use enhanced binder for forms
final CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);

// ✅ Handle null project gracefully
if (currentProject == null) {
    showNoProjectWarning();
    return;
}
```

## ✅ View Checklist

When creating a new view, ensure:

### Required Elements
- [ ] Extends appropriate base class
- [ ] Uses proper route and security annotations
- [ ] Implements required abstract methods
- [ ] Uses CButton instead of standard Button
- [ ] Sets component IDs with CAuxillaries.setId()
- [ ] Handles project context appropriately
- [ ] Includes proper error handling
- [ ] Uses consistent styling and layout

### Optional Elements
- [ ] Custom toolbar actions
- [ ] Dialog implementations
- [ ] Export functionality
- [ ] Advanced grid features
- [ ] Custom validation
- [ ] Context menus
- [ ] Keyboard shortcuts

### Integration Requirements
- [ ] Added to menu system if needed
- [ ] Proper navigation patterns
- [ ] Consistent with design system
- [ ] Playwright test coverage
- [ ] Responsive design considerations

This view architecture provides a consistent, maintainable foundation for building rich user interfaces while maintaining project awareness and security throughout the application.