# Entity Dialog Coding Standards (MANDATORY)

## Overview

This document defines mandatory coding standards for creating entity edit/create dialogs in the Derbent project. These patterns ensure consistency, maintainability, and proper FormBuilder usage across all dialogs.

**Target Audience**: Developers, AI agents (GitHub Copilot, Codex), code reviewers

---

## Core Principles (MANDATORY)

### 1. Single Dialog for Both Create and Edit

**Rule**: ALWAYS use the same dialog class for both creating new entities and editing existing ones.

**❌ WRONG - Separate dialogs:**
```java
// DON'T create separate classes
public class CDialogAttachmentUpload extends CDialogDBEdit<CAttachment> { }
public class CDialogAttachmentEdit extends CDialogDBEdit<CAttachment> { }
```

**✅ CORRECT - Single unified dialog:**
```java
// ONE dialog handles both modes
public class CDialogAttachment extends CDialogDBEdit<CAttachment> {
    
    // Constructor for create mode
    public CDialogAttachment(CAttachmentService service, 
            CEntityDB<?> parent, Consumer<CAttachment> onSave) throws Exception {
        super(new CAttachment(), onSave, true);  // isNew = true
        // ... initialization
    }
    
    // Constructor for edit mode
    public CDialogAttachment(CAttachment entity, 
            Consumer<CAttachment> onSave) throws Exception {
        super(entity, onSave, false);  // isNew = false
        // ... initialization
    }
}
```

**Benefits**:
- Less code to maintain (one class instead of two)
- Consistent behavior between create and edit
- Easier to add features (add once, works for both modes)
- Single source of truth for validation and business logic

---

### 2. Always Use FormBuilder for Form Fields

**Rule**: ALWAYS use `CFormBuilder` to generate form fields from `@AMetaData` annotations.

**❌ WRONG - Manual field creation:**
```java
private void createFormFields() {
    // DON'T create fields manually
    TextField textFieldName = new TextField("Name");
    textFieldName.setWidthFull();
    textFieldName.setMaxLength(255);
    binder.forField(textFieldName)
        .withValidator(new NotBlankValidator("Name required"))
        .bind(CEntity::getName, CEntity::setName);
    layout.add(textFieldName);
    
    TextArea textAreaDescription = new TextArea("Description");
    textAreaDescription.setWidthFull();
    textAreaDescription.setMaxLength(2000);
    binder.forField(textAreaDescription)
        .bind(CEntity::getDescription, CEntity::setDescription);
    layout.add(textAreaDescription);
}
```

**✅ CORRECT - Use FormBuilder:**
```java
private void createFormFields() throws Exception {
    // FormBuilder reads @AMetaData and creates fields automatically
    final List<String> fields = List.of(
        "name", "description", "status", "assignedTo"
    );
    
    getDialogLayout().add(formBuilder.build(CEntity.class, binder, fields));
    
    // Optionally mark fields as read-only
    if (formBuilder.getComponentMap().containsKey("status")) {
        ((HasValue<?, ?>) formBuilder.getComponentMap().get("status"))
            .setReadOnly(true);
    }
}
```

**Benefits**:
- Metadata-driven: Form structure defined in entity annotations
- Consistent: All forms look the same
- Automatic validation: Validators from `@AMetaData` applied automatically
- Less code: 5 lines instead of 20 lines per field
- Easy maintenance: Add/remove fields by updating `@AMetaData` annotations

---

### 3. Always Write Form Data Back to Entity

**Rule**: In the `save()` method, ALWAYS call `binder.writeBean()` to write form data back to the entity before saving.

**❌ WRONG - Missing writeBean:**
```java
private void saveEdit() throws Exception {
    validateForm();
    
    // Missing binder.writeBean() - form data not written to entity!
    
    if (onSave != null) {
        onSave.accept(getEntity());
    }
    close();
}
```

**✅ CORRECT - Use writeBean:**
```java
private void saveEdit() throws Exception {
    validateForm();
    
    // CRITICAL: Write form data back to entity
    binder.writeBean(getEntity());
    
    if (onSave != null) {
        onSave.accept(getEntity());
    }
    close();
}
```

**Why This Matters**:
- Without `writeBean()`, form changes are NOT saved to the entity
- User enters data → data stays in UI components → entity unchanged
- Save completes successfully but with OLD data
- Result: User confusion, data loss

---

### 4. Selection-Aware Grid Components

**Rule**: Grid toolbars MUST enable/disable buttons based on grid selection.

**❌ WRONG - Buttons always enabled:**
```java
private void createToolbarButtons() {
    buttonEdit = new CButton(VaadinIcon.EDIT.create());
    buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
    // Button always enabled - user can click even with nothing selected!
    toolbar.add(buttonEdit);
}
```

**✅ CORRECT - Selection-aware buttons:**
```java
private void createToolbarButtons() {
    // Create button
    buttonCreate = new CButton(VaadinIcon.PLUS.create());
    buttonCreate.addClickListener(e -> on_buttonCreate_clicked());
    toolbar.add(buttonCreate);
    
    // Edit button (disabled by default)
    buttonEdit = new CButton(VaadinIcon.EDIT.create());
    buttonEdit.setEnabled(false);  // Disabled until selection
    buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
    toolbar.add(buttonEdit);
    
    // Delete button (disabled by default)
    buttonDelete = new CButton(VaadinIcon.TRASH.create());
    buttonDelete.setEnabled(false);  // Disabled until selection
    buttonDelete.addClickListener(e -> on_buttonDelete_clicked());
    toolbar.add(buttonDelete);
}

// Update button states on selection
protected void on_grid_selectionChanged(final CEntity selected) {
    final boolean hasSelection = (selected != null);
    buttonEdit.setEnabled(hasSelection);
    buttonDelete.setEnabled(hasSelection);
}

// Wire up selection listener
private void initializeGrid() {
    grid = new CGrid<>(CEntity.class);
    grid.asSingleSelect().addValueChangeListener(
        e -> on_grid_selectionChanged(e.getValue())
    );
    // ... rest of grid configuration
}
```

**Benefits**:
- Prevents errors: User can't click Edit with nothing selected
- Better UX: Visual feedback (gray vs enabled buttons)
- Professional appearance: Matches industry standards (Excel, VS Code, etc.)

---

### 5. Always Support Double-Click to Edit

**Rule**: Grids displaying entities MUST support double-click to open edit dialog.

**❌ WRONG - No double-click:**
```java
private void initializeGrid() {
    grid = new CGrid<>(CEntity.class);
    grid.asSingleSelect().addValueChangeListener(
        e -> on_grid_selectionChanged(e.getValue())
    );
    // User must select + click Edit button (2 actions)
}
```

**✅ CORRECT - Double-click enabled:**
```java
private void initializeGrid() {
    grid = new CGrid<>(CEntity.class);
    
    // Selection listener
    grid.asSingleSelect().addValueChangeListener(
        e -> on_grid_selectionChanged(e.getValue())
    );
    
    // Double-click to edit (MANDATORY)
    grid.addItemDoubleClickListener(e -> on_grid_doubleClicked(e.getItem()));
}

/** Handle grid double-click to edit. */
protected void on_grid_doubleClicked(final CEntity entity) {
    if (entity != null) {
        on_buttonEdit_clicked();
    }
}
```

**Benefits**:
- Faster workflow: Double-click opens edit (1 action vs 2)
- Industry standard: Users expect this behavior (Excel, file managers, etc.)
- Better UX: Natural interaction pattern

---

## Complete Pattern Example

### Entity Dialog Template

```java
package tech.derbent.plm.myentity.view;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.ui.binder.CBinderFactory;
import tech.derbent.api.ui.binder.CEnhancedBinder;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.myentity.domain.CMyEntity;
import tech.derbent.plm.myentity.service.CMyEntityService;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog for creating and editing CMyEntity instances.
 * Uses FormBuilder for metadata-driven form generation.
 * Supports both create (isNew=true) and edit (isNew=false) modes.
 */
public class CDialogMyEntity extends CDialogDBEdit<CMyEntity> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CDialogMyEntity.class);
    
    private final CMyEntityService service;
    private final CEnhancedBinder<CMyEntity> binder;
    private final CFormBuilder<CMyEntity> formBuilder;
    
    /**
     * Constructor for create mode (new entity).
     */
    public CDialogMyEntity(
            final CMyEntityService service,
            final Consumer<CMyEntity> onSave) throws Exception {
        super(new CMyEntity(), onSave, true);  // isNew = true
        Check.notNull(service, "Service cannot be null");
        
        this.service = service;
        this.binder = CBinderFactory.createEnhancedBinder(CMyEntity.class);
        this.formBuilder = new CFormBuilder<>();
        
        setupDialog();
        populateForm();
    }
    
    /**
     * Constructor for edit mode (existing entity).
     */
    public CDialogMyEntity(
            final CMyEntity entity,
            final CMyEntityService service,
            final Consumer<CMyEntity> onSave) throws Exception {
        super(entity, onSave, false);  // isNew = false
        Check.notNull(entity, "Entity cannot be null");
        Check.notNull(service, "Service cannot be null");
        
        this.service = service;
        this.binder = CBinderFactory.createEnhancedBinder(CMyEntity.class);
        this.formBuilder = new CFormBuilder<>();
        
        setupDialog();
        populateForm();
    }
    
    @Override
    protected void setupContent() throws Exception {
        super.setupContent();
        setWidth("600px");
        setResizable(true);
        createFormFields();
    }
    
    private void createFormFields() throws Exception {
        Check.notNull(getDialogLayout(), "Dialog layout must be initialized");
        
        // Define fields to display (order matters)
        final List<String> fields = List.of(
            "name",
            "description", 
            "status",
            "assignedTo",
            "dueDate"
        );
        
        // FormBuilder creates fields from @AMetaData annotations
        getDialogLayout().add(formBuilder.build(CMyEntity.class, binder, fields));
        
        // Optionally mark fields as read-only
        if (!isNew && formBuilder.getComponentMap().containsKey("status")) {
            ((HasValue<?, ?>) formBuilder.getComponentMap().get("status"))
                .setReadOnly(true);
        }
    }
    
    @Override
    protected void populateForm() {
        if (getEntity() != null && !isNew) {
            binder.readBean(getEntity());
        }
    }
    
    @Override
    protected void validateForm() {
        Check.notNull(getEntity(), "Entity cannot be null");
        
        try {
            binder.validate();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to validate form", e);
        }
    }
    
    @Override
    protected void save() throws Exception {
        try {
            validateForm();
            
            // CRITICAL: Write form data back to entity
            binder.writeBean(getEntity());
            
            // Callback handles actual save
            if (onSave != null) {
                onSave.accept(getEntity());
            }
            
            close();
            CNotificationService.showSaveSuccess();
            
        } catch (final Exception e) {
            LOGGER.error("Error saving entity", e);
            CNotificationService.showException("Failed to save entity", e);
            throw e;
        }
    }
    
    @Override
    public String getDialogTitleString() {
        return isNew ? "Create New Entity" : "Edit Entity";
    }
    
    @Override
    protected Icon getFormIcon() throws Exception {
        return isNew ? VaadinIcon.PLUS.create() : VaadinIcon.EDIT.create();
    }
    
    @Override
    protected String getFormTitleString() {
        return isNew ? "Create Entity" : "Edit Entity";
    }
    
    @Override
    protected String getSuccessCreateMessage() {
        return "Entity created successfully";
    }
    
    @Override
    protected String getSuccessUpdateMessage() {
        return "Entity updated successfully";
    }
}
```

### Grid Component with Selection Awareness and Double-Click

```java
package tech.derbent.plm.myentity.view;

import tech.derbent.api.screens.view.CComponentListEntityBase;
import tech.derbent.api.ui.component.enhanced.CGrid;
import tech.derbent.plm.myentity.domain.CMyEntity;
import tech.derbent.plm.myentity.service.CMyEntityService;

public class CComponentListMyEntity extends CComponentListEntityBase<CMyEntity, CParentEntity> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListMyEntity.class);
    
    private final CMyEntityService service;
    private CGrid<CMyEntity> grid;
    private CButton buttonCreate;
    private CButton buttonEdit;
    private CButton buttonDelete;
    
    public CComponentListMyEntity(final CMyEntityService service) {
        Check.notNull(service, "Service cannot be null");
        this.service = service;
        initializeComponent();
    }
    
    private void initializeComponent() {
        setPadding(false);
        setSpacing(true);
        
        // Toolbar
        createToolbar();
        
        // Grid
        grid = new CGrid<>(CMyEntity.class);
        CGrid.setupGrid(grid);
        configureGrid(grid);
        
        // Selection listener (enables/disables buttons)
        grid.asSingleSelect().addValueChangeListener(
            e -> on_grid_selectionChanged(e.getValue())
        );
        
        // Double-click to edit (MANDATORY)
        grid.addItemDoubleClickListener(
            e -> on_grid_doubleClicked(e.getItem())
        );
        
        add(grid);
    }
    
    private void createToolbar() {
        final CHorizontalLayout toolbar = new CHorizontalLayout();
        toolbar.setSpacing(true);
        
        // Create button (always enabled)
        buttonCreate = new CButton(VaadinIcon.PLUS.create());
        buttonCreate.setTooltipText("Create new entity");
        buttonCreate.addClickListener(e -> on_buttonCreate_clicked());
        toolbar.add(buttonCreate);
        
        // Edit button (disabled until selection)
        buttonEdit = new CButton(VaadinIcon.EDIT.create());
        buttonEdit.setTooltipText("Edit selected entity");
        buttonEdit.setEnabled(false);
        buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
        toolbar.add(buttonEdit);
        
        // Delete button (disabled until selection)
        buttonDelete = new CButton(VaadinIcon.TRASH.create());
        buttonDelete.setTooltipText("Delete selected entity");
        buttonDelete.setEnabled(false);
        buttonDelete.addClickListener(e -> on_buttonDelete_clicked());
        toolbar.add(buttonDelete);
        
        add(toolbar);
    }
    
    /** Handle grid selection changes - enable/disable buttons. */
    protected void on_grid_selectionChanged(final CMyEntity selected) {
        final boolean hasSelection = (selected != null);
        buttonEdit.setEnabled(hasSelection);
        buttonDelete.setEnabled(hasSelection);
    }
    
    /** Handle grid double-click to edit. */
    protected void on_grid_doubleClicked(final CMyEntity entity) {
        if (entity != null) {
            on_buttonEdit_clicked();
        }
    }
    
    /** Handle create button click. */
    protected void on_buttonCreate_clicked() {
        try {
            final CDialogMyEntity dialog = new CDialogMyEntity(
                service,
                entity -> {
                    service.save(entity);
                    refreshGrid();
                    CNotificationService.showSaveSuccess();
                }
            );
            dialog.open();
        } catch (final Exception e) {
            LOGGER.error("Error opening create dialog", e);
            CNotificationService.showException("Error opening dialog", e);
        }
    }
    
    /** Handle edit button click. */
    protected void on_buttonEdit_clicked() {
        try {
            final CMyEntity selected = grid.asSingleSelect().getValue();
            Check.notNull(selected, "No entity selected");
            
            final CDialogMyEntity dialog = new CDialogMyEntity(
                selected,
                service,
                entity -> {
                    service.save(entity);
                    refreshGrid();
                    CNotificationService.showSaveSuccess();
                }
            );
            dialog.open();
        } catch (final Exception e) {
            LOGGER.error("Error opening edit dialog", e);
            CNotificationService.showException("Error opening dialog", e);
        }
    }
    
    /** Handle delete button click. */
    protected void on_buttonDelete_clicked() {
        try {
            final CMyEntity selected = grid.asSingleSelect().getValue();
            Check.notNull(selected, "No entity selected");
            
            CNotificationService.showConfirmationDialog(
                "Delete entity '" + selected.getName() + "'?",
                () -> {
                    service.delete(selected);
                    refreshGrid();
                    CNotificationService.showDeleteSuccess();
                }
            );
        } catch (final Exception e) {
            LOGGER.error("Error deleting entity", e);
            CNotificationService.showException("Error deleting entity", e);
        }
    }
    
    @Override
    public void configureGrid(final CGrid<CMyEntity> grid) {
        Check.notNull(grid, "Grid cannot be null");
        
        grid.addIdColumn(CMyEntity::getId, "ID", "id");
        grid.addShortTextColumn(CMyEntity::getName, "Name", "name");
        grid.addLongTextColumn(CMyEntity::getDescription, "Description", "description");
        
        // Use entity column helpers (MANDATORY)
        grid.addColumnEntityNamed(CMyEntity::getStatus, "Status");
        grid.addColumnEntityNamed(CMyEntity::getAssignedTo, "Assigned To");
        
        grid.addDateTimeColumn(CMyEntity::getCreated, "Created", "created");
    }
    
    @Override
    public void refreshGrid() {
        grid.setItems(service.findAll());
    }
}
```

---

## Validation Checklist

Before committing a dialog or grid component:

### Dialog Checklist

- [ ] ✅ Single dialog class for both create and edit
- [ ] ✅ Two constructors: one for create (isNew=true), one for edit (isNew=false)
- [ ] ✅ Extends `CDialogDBEdit<T>`
- [ ] ✅ Uses `CFormBuilder` to generate form fields
- [ ] ✅ Field list defined in `createFormFields()`
- [ ] ✅ `save()` method calls `binder.writeBean(getEntity())`
- [ ] ✅ `populateForm()` calls `binder.readBean(getEntity())` for edit mode
- [ ] ✅ `validateForm()` calls `binder.validate()`
- [ ] ✅ Implements title/icon override methods
- [ ] ✅ Proper error handling with `CNotificationService`

### Grid Component Checklist

- [ ] ✅ Create button always enabled
- [ ] ✅ Edit/Delete buttons disabled by default
- [ ] ✅ Selection listener updates button states
- [ ] ✅ Double-click listener opens edit dialog
- [ ] ✅ `on_grid_selectionChanged()` method implemented
- [ ] ✅ `on_grid_doubleClicked()` method implemented
- [ ] ✅ Uses entity column helpers (`addColumnEntityNamed`, etc.)
- [ ] ✅ Confirmation dialog for delete operations
- [ ] ✅ Proper error handling with try-catch

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Separate Upload and Edit Dialogs

```java
// WRONG - duplicate code, different behavior
public class CDialogEntityUpload { }
public class CDialogEntityEdit { }
```

**Fix**: One dialog, two constructors with `isNew` flag.

### ❌ Mistake 2: Manual Field Creation

```java
// WRONG - manual field creation
TextField field = new TextField("Name");
field.setWidthFull();
binder.bind(field, Entity::getName, Entity::setName);
```

**Fix**: Use FormBuilder with field list.

### ❌ Mistake 3: Forgetting writeBean()

```java
// WRONG - form data not written to entity
protected void save() {
    validateForm();
    onSave.accept(getEntity());  // Entity has OLD data!
}
```

**Fix**: Call `binder.writeBean(getEntity())` before callback.

### ❌ Mistake 4: Always-Enabled Buttons

```java
// WRONG - Edit button enabled even with no selection
buttonEdit = new CButton(VaadinIcon.EDIT.create());
buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
```

**Fix**: Disable by default, enable on selection.

### ❌ Mistake 5: No Double-Click Support

```java
// WRONG - user must select + click Edit (2 actions)
grid = new CGrid<>(Entity.class);
// No double-click listener
```

**Fix**: Add `addItemDoubleClickListener()`.

---

## Related Documentation

- [Coding Standards](coding-standards.md) - General coding guidelines
- [CGrid Configuration Patterns](cgrid-configuration-patterns.md) - Grid configuration guide
- [Component Coding Standards](../development/component-coding-standards.md) - Component patterns
- [FormBuilder Guide](formbuilder-guide.md) - FormBuilder usage patterns

---

## Version History

- **Version 1.0** (2026-01-13): Initial creation
  - Core principles defined
  - Complete pattern examples
  - Validation checklists
  - Common mistakes documented

