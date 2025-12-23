# Component Development Coding Standards

## Overview
This document defines coding standards for developing UI components in the Derbent application, based on the CComponentListEntityBase pattern.

## 1. Exception Handling

### Rule: User-Facing Functions Must NOT Throw Exceptions
All user-facing functions (button clicks, event handlers, etc.) must catch exceptions and display them to users using `CNotificationService.showException()`.

**❌ INCORRECT:**
```java
private void onButtonClick() {
    entity.doSomething(); // Could throw exception
}
```

**✅ CORRECT:**
```java
private void onButtonClick() {
    try {
        entity.doSomething();
    } catch (final Exception ex) {
        LOGGER.error("Error performing operation", ex);
        CNotificationService.showException("Error performing operation", ex);
    }
}
```

### Rule: Always Use CNotificationService.showException for User Errors
Never use `showError()` when you have an exception. Always use `showException()` to provide full error details.

**❌ INCORRECT:**
```java
} catch (Exception e) {
    CNotificationService.showError("Error: " + e.getMessage());
}
```

**✅ CORRECT:**
```java
} catch (Exception e) {
    LOGGER.error("Error description", e);
    CNotificationService.showException("Error description", e);
}
```

## 2. Null Checking

### Rule: Replace Redundant Null Checks with Objects.requireNonNull
Remove `if (x != null) { ... }` patterns with no else clause. Use `Objects.requireNonNull()` instead to throw exceptions immediately.

**❌ INCORRECT:**
```java
public void process(Entity entity) {
    if (entity != null) {
        entity.doSomething();
    }
    // Continues without handling null case
}
```

**✅ CORRECT:**
```java
public void process(Entity entity) {
    Objects.requireNonNull(entity, "Entity cannot be null");
    entity.doSomething();
}
```

### Note: Conditional Logic with Else is Allowed
If your code has proper else handling or returns alternative values, keep the if statement:

**✅ ACCEPTABLE:**
```java
// Has else clause - this is fine
if (item != null) {
    return item.getName();
} else {
    return "Unknown";
}

// Returns alternative - this is fine
if (item != null && item.getStatus() != null) {
    return item.getStatus().getName();
}
return ""; // Alternative return
```

## 3. Validation Annotations

### Rule: All Entity Fields Must Have Validation Annotations
All non-nullable entity fields must have appropriate Jakarta Validation annotations with clear messages.

**✅ REQUIRED Annotations:**
- `@NotNull(message = "Clear description")` - For required fields
- `@Size(max = N, message = "Clear description")` - For string length constraints
- `@Min/@Max` - For numeric constraints
- `@NotBlank` - For strings that cannot be empty

**Example:**
```java
@Column(name = "item_type", nullable = false, length = 50)
@NotNull(message = "Item type is required")
@Size(max = 50, message = "Item type must not exceed 50 characters")
@AMetaData(
    displayName = "Item Type", required = true, readOnly = false,
    description = "Type of the project item", hidden = false, 
    maxLength = 50
)
private String itemType;
```

## 4. Component Structure

### Rule: Follow the CComponentListEntityBase Pattern
When creating list-based CRUD components:

1. **Extend CComponentListEntityBase** for generic functionality
2. **Implement all abstract methods** required by the base class
3. **Use proper service injection** in constructor
4. **Validate constructor parameters** with Objects.requireNonNull
5. **Configure grid columns** in configureGrid method
6. **Handle exceptions** in all user-facing methods

**Example Structure:**
```java
public class CComponentListMyEntity extends CComponentListEntityBase<CMyEntity, CParentEntity> {
    
    private final CMyEntityService myEntityService;
    private CParentEntity currentParent;
    
    public CComponentListMyEntity(final CMyEntityService myEntityService) {
        super("My Entities", CMyEntity.class, myEntityService);
        Objects.requireNonNull(myEntityService, "MyEntityService cannot be null");
        this.myEntityService = myEntityService;
    }
    
    @Override
    protected void configureGrid(final CGrid<CMyEntity> grid) {
        Objects.requireNonNull(grid, "Grid cannot be null");
        // Add columns...
    }
    
    @Override
    protected CMyEntity createNewEntity() {
        Objects.requireNonNull(currentParent, "Parent cannot be null when creating entity");
        // Create and return entity...
    }
    
    // ... implement other abstract methods
}
```

## 5. Logging

### Rule: Log at Appropriate Levels
- `LOGGER.debug()` - For flow tracking and variable values
- `LOGGER.error()` - For exceptions and error conditions
- `LOGGER.warn()` - For unexpected but handled conditions

**Example:**
```java
try {
    LOGGER.debug("Processing entity: {}", entity.getId());
    entityService.save(entity);
} catch (final Exception e) {
    LOGGER.error("Error saving entity", e);
    CNotificationService.showException("Error saving entity", e);
}
```

## 6. Icons and UI Elements

### Rule: Use VaadinIcon for All Icons
Always use VaadinIcon constants instead of string literals.

**✅ CORRECT:**
```java
Button addButton = new Button("Add", VaadinIcon.PLUS.create());
Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
Button moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
Button moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
```

## 7. Button Click Handlers

### Rule: Always Wrap Button Click Handlers with Try-Catch
Button click listeners must never propagate exceptions to the UI framework.

**✅ CORRECT:**
```java
addButton.addClickListener(e -> {
    try {
        handleAdd();
    } catch (final Exception ex) {
        LOGGER.error("Error handling add operation", ex);
        CNotificationService.showException("Error adding item", ex);
    }
});
```

Or delegate to a method that has try-catch:

**✅ ALSO CORRECT:**
```java
addButton.addClickListener(e -> handleAdd());

// In handleAdd method:
protected void handleAdd() {
    try {
        // Logic here
    } catch (final Exception ex) {
        LOGGER.error("Error handling add operation", ex);
        CNotificationService.showException("Error adding item", ex);
    }
}
```

## 8. Service Methods

### Rule: Service Methods Should Validate Parameters
All service methods should validate parameters with Check utilities at the beginning.

**✅ CORRECT:**
```java
public void processEntity(Entity entity, Parent parent) {
    Objects.requireNonNull(entity, "Entity cannot be null");
    Objects.requireNonNull(parent, "Parent cannot be null");
    Objects.requireNonNull(parent.getId(), "Parent must be saved before processing");
    
    // Process entity...
}
```

## 9. Constants

### Rule: Define Constants for Magic Strings and Numbers
Never use magic strings or numbers in code. Define them as constants.

**❌ INCORRECT:**
```java
if ("CActivity".equals(type)) { ... }
```

**✅ CORRECT:**
```java
private static final String ITEM_TYPE_ACTIVITY = "CActivity";
private static final String ITEM_TYPE_MEETING = "CMeeting";

if (ITEM_TYPE_ACTIVITY.equals(type)) { ... }
```

## 10. Notification Methods

### Standard Notification Methods
Use these CNotificationService methods appropriately:

- `showException(String message, Exception ex)` - For exceptions with full details
- `showSaveSuccess()` - After successful save
- `showDeleteSuccess()` - After successful delete
- `showWarning(String message)` - For warnings
- `showInfo(String message)` - For informational messages

## Summary Checklist

For each new component or feature:

- [ ] All button click handlers wrapped in try-catch
- [ ] All exceptions shown to user with `showException()`
- [ ] Redundant `if (x != null)` replaced with `Objects.requireNonNull()`
- [ ] Entity fields have `@NotNull` and other validation annotations
- [ ] Constructor parameters validated with Check utilities
- [ ] All service methods validate their parameters
- [ ] VaadinIcon used for all icons
- [ ] Magic strings/numbers defined as constants
- [ ] Appropriate logging at DEBUG and ERROR levels
- [ ] Follows CComponentListEntityBase pattern (if list component)

## 11. Field and Component Naming Convention

### Rule: Use TypeName Format for Component Fields

All component fields must follow the `typeName` naming convention where:
- First part is the type (lowercased)
- Second part is the descriptive name (capitalized)

This makes it immediately clear what type of component a field is and what its purpose is.

**Format**: `{type}{Name}` - e.g., `buttonAdd`, `dialogEntitySelection`, `comboBoxStatus`

#### ✅ CORRECT
```java
// Buttons
private CButton buttonAdd;
private CButton buttonDelete;
private CButton buttonMoveUp;
private CButton buttonMoveDown;
private CButton buttonSave;
private CButton buttonCancel;

// Dialogs
private CDialog dialogEntitySelection;
private CDialog dialogConfirmation;
private CDialogWarning dialogWarning;

// Layouts
private CVerticalLayout layoutMain;
private CHorizontalLayout layoutToolbar;
private CHorizontalLayout layoutButtons;

// Other components
private ComboBox<String> comboBoxStatus;
private TextField textFieldName;
private CGrid<CEntity> gridItems;
```

#### ❌ INCORRECT
```java
// Inconsistent naming
private CButton addBtn;           // ❌ Should be buttonAdd
private CButton btn_delete;       // ❌ Should be buttonDelete
private Button addButton;         // ❌ Use CButton, and name should be buttonAdd
private CDialog selectionDlg;     // ❌ Should be dialogSelection
private ComboBox statusCB;        // ❌ Should be comboBoxStatus
```

## 12. Event Handler Naming Convention

### Rule: Use on_xxx_eventType Pattern for Event Handlers

All event handlers must follow the `on_{componentName}_{eventType}` naming convention.

**Format**: `on_{componentName}_{eventType}()` - e.g., `on_buttonAdd_clicked()`, `on_comboBoxStatus_selected()`

Common event types:
- `clicked` - for button clicks
- `selected` - for selection events (combobox, grid, etc.)
- `changed` - for value change events
- `doubleClicked` - for double-click events
- `closed` - for dialog close events

#### ✅ CORRECT
```java
protected void on_buttonAdd_clicked() {
    // Handle add button click
}

protected void on_buttonDelete_clicked() {
    // Handle delete button click
}

protected void on_comboBoxStatus_selected(String status) {
    // Handle status selection
}

protected void on_gridItems_doubleClicked(CEntity item) {
    // Handle grid item double-click
}

protected void on_dialogSelection_closed() {
    // Handle dialog close
}
```

#### ❌ INCORRECT
```java
// Lambda consumers - hard to read and override
button.addClickListener(e -> {
    // Long complex logic here...
    doThis();
    doThat();
    andThis();
});

// Vague method names
private void handleAdd() { }        // ❌ Should be on_buttonAdd_clicked
private void onDeleteClick() { }    // ❌ Should be on_buttonDelete_clicked
private void processSelection() { } // ❌ Should be on_xxx_selected
```

### Rule: Avoid Complex Lambda Functions

Never put complex logic directly in lambda event listeners. Instead, delegate to named methods that can be easily overridden in subclasses.

**❌ INCORRECT - Hard to read and override:**
```java
buttonAdd.addClickListener(e -> {
    try {
        Objects.requireNonNull(entity, "Entity cannot be null");
        final CEntity newEntity = createNewEntity();
        service.save(newEntity);
        refreshGrid();
        CNotificationService.showSaveSuccess();
    } catch (final Exception ex) {
        LOGGER.error("Error adding entity", ex);
        CNotificationService.showException("Error adding entity", ex);
    }
});
```

**✅ CORRECT - Delegating to named method:**
```java
buttonAdd.addClickListener(e -> on_buttonAdd_clicked());

// Separate method that can be easily overridden
protected void on_buttonAdd_clicked() {
    try {
        Objects.requireNonNull(entity, "Entity cannot be null");
        final CEntity newEntity = createNewEntity();
        service.save(newEntity);
        refreshGrid();
        CNotificationService.showSaveSuccess();
    } catch (final Exception ex) {
        LOGGER.error("Error adding entity", ex);
        CNotificationService.showException("Error adding entity", ex);
    }
}
```

## 13. Component Creation Pattern with Factory Methods

### Rule: Use create_xxx Factory Methods for Button Creation

When creating buttons and other components in a base class, use factory methods following the `create_{componentName}` pattern. This allows subclasses to easily override the creation without duplicating event wiring.

**Format**: `create_{componentName}()` - e.g., `create_buttonAdd()`, `create_buttonDelete()`

#### ✅ CORRECT - Base class with factory pattern:
```java
public abstract class CComponentListBase {
    protected CButton buttonAdd;
    protected CButton buttonDelete;

    protected void createToolbar() {
        buttonAdd = create_buttonAdd();
        buttonDelete = create_buttonDelete();
        toolbar.add(buttonAdd, buttonDelete);
    }

    /** Factory method for add button - can be overridden by subclasses. */
    protected CButton create_buttonAdd() {
        final CButton button = new CButton(VaadinIcon.PLUS.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> on_buttonAdd_clicked());
        return button;
    }

    /** Factory method for delete button - can be overridden by subclasses. */
    protected CButton create_buttonDelete() {
        final CButton button = new CButton(VaadinIcon.TRASH.create());
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.addClickListener(e -> on_buttonDelete_clicked());
        button.setEnabled(false);
        return button;
    }

    /** Handle add button click - can be overridden by subclasses. */
    protected void on_buttonAdd_clicked() {
        // Default implementation
    }
}
```

#### Subclass override example:
```java
public class CComponentListSprintItems extends CComponentListBase {

    @Override
    protected CButton create_buttonAdd() {
        // Custom button with different icon for sprint item selection
        final CButton button = new CButton(VaadinIcon.LIST_SELECT.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setTooltipText("Add items to sprint");
        button.addClickListener(e -> on_buttonAdd_clicked());
        return button;
    }

    @Override
    protected void on_buttonAdd_clicked() {
        // Custom implementation - opens entity selection dialog instead
        openEntitySelectionDialog();
    }
}
```

## 14. Component Inheritance Rules

### Rule: All Custom Components Must Extend C-Prefixed Base Classes

All custom components must extend the appropriate C-prefixed base class from the project. Never use raw Vaadin components directly.

| Vaadin Component | Use Instead |
|------------------|-------------|
| `Button` | `CButton` |
| `Dialog` | `CDialog` or extend it |
| `VerticalLayout` | `CVerticalLayout` |
| `HorizontalLayout` | `CHorizontalLayout` |
| `Grid<T>` | `CGrid<T>` |
| `TextField` | `CTextField` |
| `FormLayout` | `CFormLayout` |
| `FlexLayout` | `CFlexLayout` |
| `TabSheet` | `CTabSheet` |
| `Tab` | `CTab` |
| `Div` | `CDiv` |
| `Span` | `CSpan` |
| `H3` | `CH3` |
| `Accordion` | `CAccordion` |
| `Scroller` | `CScroller` |

#### ✅ CORRECT
```java
// Using C-prefixed components
private final CButton buttonAdd = new CButton(VaadinIcon.PLUS.create());
private final CVerticalLayout layoutMain = new CVerticalLayout();
private final CHorizontalLayout layoutToolbar = new CHorizontalLayout();
private final CGrid<CEntity> gridItems = new CGrid<>(CEntity.class);
```

#### ❌ INCORRECT
```java
// Using raw Vaadin components directly
private final Button addButton = new Button(VaadinIcon.PLUS.create());  // ❌
private final VerticalLayout mainLayout = new VerticalLayout();          // ❌
private final HorizontalLayout toolbar = new HorizontalLayout();         // ❌
private final Grid<CEntity> itemGrid = new Grid<>(CEntity.class);        // ❌
```

### Rule: All Dialogs Must Extend CDialog

When creating custom dialogs, always extend `CDialog` and implement the required abstract methods:
- `getDialogTitleString()` - Returns the dialog header title
- `getFormTitleString()` - Returns the form title
- `getFormIcon()` - Returns the icon for the dialog
- `setupContent()` - Sets up the dialog content
- `setupButtons()` - Sets up the dialog buttons

**Exception**: Complex utility dialogs like `CDialogEntitySelection` that have their own initialization pattern may extend `Dialog` directly but must follow all other naming conventions.

## Enforcement

These standards should be enforced through:
1. Code reviews
2. Static analysis tools
3. Team coding guidelines
4. Automated checks where possible

**Last Updated:** 2025-11-29
**Version:** 1.1
