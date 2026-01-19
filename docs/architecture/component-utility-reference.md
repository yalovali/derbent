# Component and Utility Reference Index

## Overview

This document provides a comprehensive index of all reusable components, utilities, dialogs, and helper classes in the Derbent project. Use this reference BEFORE creating new methods to avoid duplication.

**Target Audience**: Developers, AI agents (GitHub Copilot, Codex), code reviewers

**Related Documentation**:
- [Method Placement Guidelines](method-placement-guidelines.md) - Rules for where methods should be defined
- [Coding Standards](coding-standards.md) - General coding standards
- [Component Coding Standards](../development/component-coding-standards.md) - Component creation guidelines

## Quick Reference Table

| Need to... | Use This Class | Example |
|------------|----------------|---------|
| Display entity label | `CLabelEntity` | `CLabelEntity.createLabel(entity)` |
| Display user label | `CLabelEntity` | `CLabelEntity.createUserLabel(user)` |
| Get entity color | `CColorUtils` | `CColorUtils.getColorFromEntity(entity)` |
| Get contrast text color | `CColorUtils` | `CColorUtils.getContrastTextColor(bgColor)` |
| Create styled icon | `CColorUtils` | `CColorUtils.createStyledIcon("vaadin:plus")` |
| Validate not null | `Check` | `Check.notNull(entity, "message")` |
| Validate not blank | `Check` | `Check.notBlank(name, "message")` |
| Validate instanceof | `Check` | `Check.instanceOf(obj, Type.class, "message")` |
| Create span | `CSpan` | `new CSpan("text")` |
| Create div | `CDiv` | `new CDiv()` |
| Create button | `CButton` | `new CButton("label")` |
| Create layout | `CVerticalLayout` / `CHorizontalLayout` | `new CVerticalLayout()` |
| Show notification | `CNotificationService` | `notificationService.showSuccess("msg")` |
| Show dialog | `CDialog` descendants | `new CDialogConfirmation()` |
| Format date | `CLabelEntity` | `CLabelEntity.createDateLabel(date)` |
| Handle images | `CImageUtils` | `CImageUtils.resizeImage(data, w, h)` |

---

## 1. UI Components (C-Prefixed)

### 1.1 Basic Layout Components

#### CVerticalLayout
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced vertical layout container  
**Use Instead Of**: `com.vaadin.flow.component.orderedlayout.VerticalLayout`

```java
// ✅ CORRECT
final CVerticalLayout layout = new CVerticalLayout();
layout.add(component1, component2);

// ❌ WRONG
final VerticalLayout layout = new VerticalLayout();  // Don't use raw Vaadin
```

#### CHorizontalLayout
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced horizontal layout container  
**Use Instead Of**: `com.vaadin.flow.component.orderedlayout.HorizontalLayout`

```java
// ✅ CORRECT
final CHorizontalLayout layout = new CHorizontalLayout();
layout.setAlignItems(Alignment.CENTER);
```

#### CFlexLayout
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced flex layout with flexible positioning  
**Use Instead Of**: `com.vaadin.flow.component.orderedlayout.FlexLayout`

#### CFormLayout
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced form layout for input fields  
**Use Instead Of**: `com.vaadin.flow.component.formlayout.FormLayout`

### 1.2 Basic Content Components

#### CSpan
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced span element for inline text  
**Use Instead Of**: `com.vaadin.flow.component.html.Span`

```java
// ✅ CORRECT
final CSpan text = new CSpan("Display text");
final CSpan styled = new CSpan("Text", 200);  // With width

// ❌ WRONG
final Span text = new Span("Display text");  // Don't use raw Vaadin
```

#### CDiv
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced div container  
**Use Instead Of**: `com.vaadin.flow.component.html.Div`

```java
// ✅ CORRECT
final CDiv container = new CDiv();
container.add(component);

// ❌ WRONG
final Div container = new Div();  // Don't use raw Vaadin
```

#### CDivVertical
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Div with vertical orientation  
**Use Instead Of**: Manual div styling

#### CH1, CH2, CH3
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced header elements  
**Use Instead Of**: `com.vaadin.flow.component.html.H1`, `H2`, `H3`

```java
// ✅ CORRECT
final CH2 header = new CH2("Section Title");
final CH3 subheader = new CH3("Subsection");

// ❌ WRONG
final H2 header = new H2("Section Title");  // Don't use raw Vaadin
```

### 1.3 Interactive Components

#### CButton
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced button with consistent styling  
**Use Instead Of**: `com.vaadin.flow.component.button.Button`

```java
// ✅ CORRECT
final CButton buttonSave = new CButton("Save");
buttonSave.addClickListener(e -> on_buttonSave_clicked());

// With icon
final CButton buttonAdd = new CButton(VaadinIcon.PLUS.create());
```

#### CTextField
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced text input field  
**Use Instead Of**: `com.vaadin.flow.component.textfield.TextField`

#### CColorAwareComboBox<T>
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: ComboBox that shows entity icons and colors  
**Use For**: Selecting entities with visual indicators

```java
final CColorAwareComboBox<CStatus> comboBox = new CColorAwareComboBox<>();
comboBox.setItems(statusList);
```

#### CColorPickerComboBox
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: ComboBox for selecting colors  
**Use For**: Color selection with visual preview

### 1.4 Container Components

#### CTabSheet
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced tabbed container  
**Use Instead Of**: `com.vaadin.flow.component.tabs.TabSheet`

#### CTab
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Individual tab component  
**Use Instead Of**: `com.vaadin.flow.component.tabs.Tab`

#### CAccordion
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Enhanced accordion/collapsible panels  
**Use Instead Of**: `com.vaadin.flow.component.accordion.Accordion`

#### CScroller
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Scrollable container  
**Use Instead Of**: `com.vaadin.flow.component.orderedlayout.Scroller`

---

## 2. Specialized UI Components

### 2.1 Entity Display Components

#### CLabelEntity
**Package**: `tech.derbent.api.grid.view`  
**Purpose**: Unified label component for displaying entities  
**Key Features**: Automatic icon, color, and formatting based on entity type

**Factory Methods**:
```java
// Entity labels
CLabelEntity.createLabel(entity)           // Standard entity label
CLabelEntity.createPlainLabel(entity)      // Plain text, no decoration
CLabelEntity.createH2Label(entity)         // H2 header with entity
CLabelEntity.createH3Label(entity)         // H3 header with entity

// User labels
CLabelEntity.createUserLabel(user)         // User with avatar and name
CLabelEntity.createCompactUserLabel(user)  // Compact user display (if added)

// Date labels
CLabelEntity.createDateLabel(date)              // Single date with icon
CLabelEntity.createDateRangeLabel(start, end)   // Date range with icon

// Text labels
CLabelEntity.createH2Label(text)           // H2 header with text
CLabelEntity.createH3Label(text)           // H3 header with text
```

**Usage Examples**:
```java
// Display entity with icon and color
final CLabelEntity statusLabel = new CLabelEntity(activity.getStatus());
layout.add(statusLabel);

// Display user
final CLabelEntity userLabel = CLabelEntity.createUserLabel(user);
layout.add(userLabel);

// Display date
final CLabelEntity dateLabel = CLabelEntity.createDateLabel(activity.getStartDate());
layout.add(dateLabel);
```

#### CEntityLabel
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Alternative entity label component  
**Note**: Consider using `CLabelEntity` for consistency

#### CComponentId
**Package**: `tech.derbent.api.grid.view`  
**Purpose**: Component for displaying entity IDs  

#### CComponentStoryPoint
**Package**: `tech.derbent.api.grid.view`  
**Purpose**: Component for displaying story points  

### 2.2 Grid and List Components

#### CGrid<T>
**Package**: `tech.derbent.api.grid.domain`  
**Purpose**: Enhanced grid for displaying entity lists  
**Use Instead Of**: `com.vaadin.flow.component.grid.Grid`

#### CComponentGridEntity
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Grid specifically for entities with built-in CRUD

### 2.3 Widget Components

#### CComponentWidgetEntity<T>
**Package**: `tech.derbent.api.grid.widget`  
**Purpose**: Base widget class for displaying entities  
**Use For**: Creating custom entity display widgets

#### CComponentWidgetEntityOfProject<T>
**Package**: `tech.derbent.api.grid.widget`  
**Purpose**: Widget for project-related entities  

---

## 3. Dialog Classes

### 3.1 Basic Dialogs

#### CDialog
**Package**: `tech.derbent.api.ui.component.basic`  
**Purpose**: Base enhanced dialog class  
**Use Instead Of**: `com.vaadin.flow.component.dialog.Dialog`

#### CDialogConfirmation
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Confirmation dialog for destructive actions  

```java
final CDialogConfirmation dialog = new CDialogConfirmation(
    "Delete Activity?",
    "This action cannot be undone.",
    () -> deleteActivity()
);
dialog.open();
```

#### CDialogInformation
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Information/message dialog  

#### CDialogException
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog for displaying exceptions  

```java
try {
    // operation
} catch (Exception e) {
    new CDialogException(e).open();
}
```

#### CDialogMessageWithDetails
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog with collapsible details section  

#### CDialogProgress
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Progress indicator dialog  

### 3.2 Entity Editing Dialogs

#### CDialogDBEdit
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Generic database entity edit dialog  

#### CDialogClone
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog for cloning entities  

#### CDialogDBRelation
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog for managing entity relationships  

### 3.3 Selection Dialogs

#### CDialogEntitySelection
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog for selecting entities from a list  

```java
final CDialogEntitySelection<CUser> dialog = new CDialogEntitySelection<>(
    "Select User",
    userList,
    selected -> assignUser(selected)
);
dialog.open();
```

#### CDialogFieldSelection
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog for field-based selection  

#### CDialogKanbanStatusSelection
**Package**: `tech.derbent.plm.kanban.kanbanline.view`  
**Purpose**: Specialized dialog for Kanban status selection  

### 3.4 Specialized Dialogs

#### CDialogUserProfile
**Package**: `tech.derbent.base.users.view`  
**Purpose**: User profile editing dialog  

#### CDialogPictureSelector
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Image selection/upload dialog  

#### CDialogDetailLinesEdit
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dialog for editing detail lines (master-detail pattern)  

#### CDialogKanbanColumnEdit
**Package**: `tech.derbent.plm.kanban.kanbanline.view`  
**Purpose**: Kanban column configuration dialog  

#### CDialogUserProjectRelation
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Managing user-project relationships  

#### CDialogProjectUserSettings
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Project-specific user settings  

#### CDialogUserProjectSettings
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: User-specific project settings  

---

## 4. Utility Classes

### 4.1 Color and Icon Utilities

#### CColorUtils
**Package**: `tech.derbent.api.utils`  
**Purpose**: Comprehensive color and icon utilities  
**Type**: `public final class` (static methods only)

**Color Operations**:
```java
// Get contrast text color for readability
String textColor = CColorUtils.getContrastTextColor(backgroundColor);

// Get color from entity
String color = CColorUtils.getColorFromEntity(entity);

// Generate random colors
String darkColor = CColorUtils.getRandomColor(true);
String lightColor = CColorUtils.getRandomColor(false);
String webColor = CColorUtils.getRandomFromWebColors(true);
```

**Icon Operations**:
```java
// Create styled icons
Icon icon = CColorUtils.createStyledIcon("vaadin:plus");
Icon coloredIcon = CColorUtils.createStyledIcon("vaadin:user", "#FF0000");

// Get icon for entity
Icon entityIcon = CColorUtils.getIconForEntity(entity);

// Get static icon filename
String iconName = CColorUtils.getStaticIconFilename(CActivity.class);
String iconColor = CColorUtils.getStaticIconColorCode(CActivity.class);
```

**Display Utilities**:
```java
// Get display text from entity
String text = CColorUtils.getDisplayTextFromEntity(entity);

// Create styled headers
Span header = CColorUtils.createStyledHeader("Title", "#FF0000");
```

**Constants** (for CRUD buttons):
```java
CColorUtils.CRUD_CREATE_COLOR    // "#4B7F82" - CDE Green
CColorUtils.CRUD_CREATE_ICON     // "vaadin:plus"
CColorUtils.CRUD_DELETE_COLOR    // "#91856C" - OpenWindows Border Dark
CColorUtils.CRUD_DELETE_ICON     // "vaadin:trash"
CColorUtils.CRUD_SAVE_COLOR      // "#6B5FA7" - CDE Purple
CColorUtils.CRUD_SAVE_ICON       // "vaadin:check"
CColorUtils.CRUD_CANCEL_COLOR    // "#8E8E8E" - CDE Dark Gray
CColorUtils.CRUD_CANCEL_ICON     // "vaadin:close"
```

**Deprecated Methods**:
- `getEntityWithIcon(entity)` → Use `CLabelEntity` instead

### 4.2 Validation Utilities

#### Check
**Package**: `tech.derbent.api.utils`  
**Purpose**: Fast-fail validation utilities  
**Type**: `public final class` (static methods only)

**Null Checking**:
```java
Check.notNull(entity, "Entity cannot be null");
Check.notNull(user, "User is required");
```

**String Validation**:
```java
Check.notBlank(name, "Name cannot be blank");
Check.notEmpty(text, "Text cannot be empty");
```

**Collection Validation**:
```java
Check.notEmpty(list, "List cannot be empty");
Check.notEmpty(map, "Map cannot be empty");
```

**Boolean Validation**:
```java
Check.isTrue(value > 0, "Value must be positive");
Check.isFalse(flag, "Flag must be false");
```

**Type Checking**:
```java
Check.instanceOf(obj, CEntityNamed.class, "Must be CEntityNamed");
Check.instanceOf(item, ISprintableItem.class, "Must implement ISprintableItem");
```

**Usage Pattern**:
```java
public void processActivity(final CActivity activity) {
    Check.notNull(activity, "Activity cannot be null");
    Check.notBlank(activity.getName(), "Activity name is required");
    Check.instanceOf(activity, ISprintableItem.class, 
        "Activity must implement ISprintableItem");
    // Safe to proceed
}
```

### 4.3 Image Utilities

#### CImageUtils
**Package**: `tech.derbent.api.utils`  
**Purpose**: Image processing and manipulation  

**Methods**:
```java
// Resize image
byte[] resized = CImageUtils.resizeImage(originalData, width, height);

// Convert formats (check for available methods)
```

### 4.4 General Utilities

#### CAuxillaries
**Package**: `tech.derbent.api.utils`  
**Purpose**: Miscellaneous utility methods  

**Methods**:
```java
// Format width (example)
String width = CAuxillaries.formatWidthPx(200);  // "200px"
```

#### CPageableUtils
**Package**: `tech.derbent.api.utils`  
**Purpose**: Utilities for pagination  

#### CValueStorageHelper
**Package**: `tech.derbent.api.utils`  
**Purpose**: Helpers for value storage/retrieval  

#### CRouteDiscoveryService
**Package**: `tech.derbent.api.utils`  
**Purpose**: Service for discovering application routes  

#### CSolarisColorPalette
**Package**: `tech.derbent.api.utils`  
**Purpose**: Solaris/CDE color palette constants  

### 4.5 Panel Utilities

#### CPanelDetails
**Package**: `tech.derbent.api.utils`  
**Purpose**: Utilities for detail panels  

---

## 5. Notification and Exception Handling

### 5.1 Notification Service

#### CNotificationService
**Package**: `tech.derbent.api.ui.notifications`  
**Purpose**: Unified notification system  
**Type**: Service class (inject via `@Autowired`)

**Toast Notifications**:
```java
@Autowired
private CNotificationService notificationService;

// Success notifications (green, 2 seconds)
notificationService.showSuccess("Operation completed");
notificationService.showSaveSuccess();
notificationService.showDeleteSuccess();
notificationService.showCreateSuccess();

// Error notifications (red, 8 seconds)
notificationService.showError("Operation failed");
notificationService.showSaveError();
notificationService.showDeleteError();
notificationService.showOptimisticLockingError();

// Warning notifications (orange, 5 seconds)
notificationService.showWarning("Please review");

// Info notifications (blue, 5 seconds)
notificationService.showInfo("Process started");
```

**Modal Dialogs**:
```java
// Error dialog with exception
notificationService.showErrorDialog(exception);

// Warning dialog
notificationService.showWarningDialog("Important message");

// Confirmation dialog with callback
notificationService.showConfirmationDialog("Delete?", () -> deleteItem());
```

**Static Methods** (for utility classes):
```java
import tech.derbent.api.ui.notifications.CNotifications;

CNotifications.showSuccess("Success");
CNotifications.showError("Error");
CNotifications.showSaveSuccess();
```

**MANDATORY**: NEVER use `Notification.show()` directly. Always use `CNotificationService`.

### 5.2 Exception Classes

#### CValidationException
**Package**: `tech.derbent.api.exceptions`  
**Purpose**: Validation errors that should be shown to users  

```java
if (!isValid(input)) {
    throw new CValidationException("Input is invalid");
}
```

**Handling**:
```java
try {
    service.save(entity);
} catch (CValidationException e) {
    notificationService.showValidationException(e);
}
```

---

## 6. Service Layer Classes

### 6.1 Base Service Classes

#### CAbstractService<T>
**Package**: `tech.derbent.api.services`  
**Purpose**: Base service for all entity services  
**Extend For**: Creating entity-specific services

```java
@Service
public class CActivityService extends CAbstractService<CActivity> {
    
    public CActivityService(IActivityRepository repository, 
                           Clock clock, 
                           ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
}
```

#### CEntityOfProjectService<T>
**Package**: `tech.derbent.api.services`  
**Purpose**: Base service for project-related entities  

#### CInitializerServiceBase
**Package**: `tech.derbent.api.services`  
**Purpose**: Base for sample data initialization  

### 6.2 Session Service

#### ISessionService
**Package**: `tech.derbent.api.services`  
**Purpose**: Manages user session state  

```java
@Autowired
private ISessionService sessionService;

// Get current user
CUser user = sessionService.getActiveUser().orElseThrow();

// Get current company
CCompany company = sessionService.getCurrentCompany();

// Get current project
CProject project = sessionService.getCurrentProject();
```

---

## 7. Enhanced Components

### 7.1 Selection Components

#### CComponentEntitySelection<T>
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Component for selecting entities with search  

#### CComponentFieldSelection
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Component for field-based selection  

#### CComponentListSelection<T>
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: List-based selection component  

#### CDualListSelectorComponent<T>
**Package**: `tech.derbent.api.screens.view`  
**Purpose**: Dual-list selector (available/selected)  

### 7.2 Other Enhanced Components

#### CViewToolbar
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Standard application toolbar  

#### CHierarchicalSideMenu
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Hierarchical navigation menu  

#### CDashboardStatCard
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Dashboard statistics card  

#### CPictureSelector
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Picture upload/selection component  

#### CComponentUserProjectRelationBase
**Package**: `tech.derbent.api.ui.component.enhanced`  
**Purpose**: Base for user-project relation components  

---

## 8. View/Page Base Classes

### 8.1 Abstract Page Classes

#### CAbstractPage
**Package**: `tech.derbent.api.views`  
**Purpose**: Base class for all view pages  

#### CAbstractEntityDBPage<T>
**Package**: `tech.derbent.api.views`  
**Purpose**: Base for entity CRUD pages  

#### CAbstractNamedEntityPage<T>
**Package**: `tech.derbent.api.entity.view`  
**Purpose**: Base for named entity pages  

### 8.2 Specialized Page Classes

#### CCustomizedMDPage
**Package**: `tech.derbent.api.views`  
**Purpose**: Customized markdown page  

#### CAbstractEntityRelationPanel
**Package**: `tech.derbent.api.views`  
**Purpose**: Base for entity relationship panels  

---

## 9. Interfaces

### 9.1 Entity Interfaces

#### IHasIcon
**Package**: `tech.derbent.api.interfaces`  
**Purpose**: Entity has icon capability  

```java
if (entity instanceof IHasIcon) {
    Icon icon = ((IHasIcon) entity).getIcon();
}
```

#### IHasColor
**Package**: `tech.derbent.api.interfaces`  
**Purpose**: Entity has color capability  

```java
if (entity instanceof IHasColor) {
    String color = ((IHasColor) entity).getColor();
}
```

#### ISprintableItem
**Package**: `tech.derbent.api.interfaces`  
**Purpose**: Entity can be added to sprints  

### 9.2 Event Interfaces

#### IHasSelectionNotification
**Package**: `tech.derbent.api.interfaces`  
**Purpose**: Component supports selection events  

#### IGridRefreshListener<T>
**Package**: `tech.derbent.api.interfaces`  
**Purpose**: Listener for grid refresh events  

---

## 10. Form Building

### CFormBuilder
**Package**: `tech.derbent.api.annotations`  
**Purpose**: Automatic form generation from entity annotations  

**Use With**: `@AMetaData` annotations on entity fields

```java
@AMetaData(
    displayName = "Activity Name",
    required = true,
    description = "The name of the activity"
)
private String name;
```

---

## Usage Guidelines for AI Agents

### Before Creating ANY Method

1. **Check this reference first** - Is there already a class for this?
2. **Search the codebase** - Use grep/glob to find similar functionality
3. **Use existing components** - Don't reinvent what exists
4. **Follow C-prefix rule** - Never use raw Vaadin components

### Common Scenarios

| I need to... | Use this... | NOT this... |
|-------------|-------------|-------------|
| Display an entity | `CLabelEntity.createLabel(entity)` | Custom label logic |
| Show user info | `CLabelEntity.createUserLabel(user)` | Custom user display |
| Get entity color | `CColorUtils.getColorFromEntity(entity)` | Entity.getColor() directly |
| Validate input | `Check.notNull()`, `Check.notBlank()` | Manual null checks |
| Show notification | `CNotificationService.showSuccess()` | `Notification.show()` |
| Create a span | `new CSpan("text")` | `new Span("text")` |
| Create a button | `new CButton("label")` | `new Button("label")` |
| Handle exceptions | `CNotificationService.showErrorDialog(ex)` | Custom exception display |

### Decision Tree for Component Usage

```
Need to display something?
├─ Entity? → CLabelEntity
├─ User? → CLabelEntity.createUserLabel()
├─ Date? → CLabelEntity.createDateLabel()
├─ Icon? → CColorUtils.createStyledIcon()
├─ Text? → CSpan
├─ Container? → CDiv or CVerticalLayout/CHorizontalLayout
└─ Button? → CButton

Need to calculate something?
├─ Color-related? → CColorUtils
├─ Validation? → Check
├─ Image processing? → CImageUtils
└─ General utility? → CAuxillaries

Need user interaction?
├─ Dialog? → CDialog* classes
├─ Selection? → CComponentEntitySelection or CDialogEntitySelection
├─ Form input? → Use appropriate CTextField, etc.
└─ Notification? → CNotificationService
```

---

## Anti-Patterns to Avoid

### ❌ DON'T: Create Utility Methods in Components

```java
// ❌ WRONG - In CComponentKanbanPostit
private String calculateContrastColor(String bg) { /* ... */ }
private Avatar createAvatar(CUser user) { /* ... */ }
```

**Do**: Use `CColorUtils` and `CLabelEntity`

### ❌ DON'T: Use Raw Vaadin Components

```java
// ❌ WRONG
final Span span = new Span("text");
final Div container = new Div();
final Button button = new Button("Save");
```

**Do**: Use C-prefixed components

### ❌ DON'T: Create Manual Notifications

```java
// ❌ WRONG
Notification.show("Success", 3000, Position.TOP_CENTER);
```

**Do**: Use `CNotificationService`

### ❌ DON'T: Duplicate Entity Display Logic

```java
// ❌ WRONG - Custom entity display in every component
String text = entity.getName() + " - " + entity.getDescription();
```

**Do**: Use `CLabelEntity` or `CColorUtils.getDisplayTextFromEntity()`

---

## Related Documentation

- [Method Placement Guidelines](method-placement-guidelines.md) - Where methods should be defined
- [Coding Standards](coding-standards.md) - General coding standards  
- [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md) - UI-specific guidelines
- [Component Coding Standards](../development/component-coding-standards.md) - Component creation

---

**Version**: 1.0  
**Last Updated**: 2026-01-03  
**Maintainer**: Derbent Development Team
