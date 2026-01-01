# UI, CSS, and Layout Coding Standards

## Overview

This document defines the UI, CSS, and layout coding standards for the Derbent project. These standards ensure consistent visual appearance, maintainable styling, and predictable layout behavior across the application.

**Target Audience**: Developers, AI agents (GitHub Copilot, Codex), code reviewers

## Core Principles

### 1. **CSS-First for Global Styles** (Mandatory)
Use CSS files for global, reusable styles. Use inline styles (via `getStyle()`) only for dynamic, component-specific styling.

### 2. **Lumo Variables for Consistency** (Mandatory)
Always use Vaadin Lumo theme variables for spacing, colors, and sizing to maintain consistency and theme compatibility.

### 3. **C-Prefixed Components** (Mandatory)
Always use C-prefixed custom components (`CButton`, `CVerticalLayout`, etc.) instead of raw Vaadin components.

---

## CSS Patterns and Best Practices

### CSS File Organization

All CSS files are located in `src/main/frontend/themes/default/`:

```
themes/default/
├── styles.css          # Main stylesheet (imports all others)
├── dev-login.css       # Development login page
├── custom-login.css    # Production login page
├── kanban.css          # Kanban board specific
├── gantt.css           # Gantt chart specific
├── project-details.css # Project details page
├── accordion.css       # Accordion component
├── basedetails.css     # Base details layout
└── grid.css            # Grid component
```

**Rule**: Create feature-specific CSS files and import them in `styles.css`.

### CSS Custom Properties (CSS Variables)

#### Global Font Size Overrides

```css
:root {
    /* Override Lumo font size variables - reduced by ~15-20% */
    --lumo-font-size-xxxl: 2.5rem;    /* Was 3rem */
    --lumo-font-size-xxl: 1.875rem;   /* Was 2.25rem */
    --lumo-font-size-xl: 1.125rem;    /* Was 1.375rem */
    --lumo-font-size-l: 0.9375rem;    /* Was 1.125rem */
    --lumo-font-size-m: 0.875rem;     /* Was 1rem - base body text */
    --lumo-font-size-s: 0.75rem;      /* Was 0.875rem */
    --lumo-font-size-xs: 0.6875rem;   /* Was 0.8125rem */
    --lumo-font-size-xxs: 0.625rem;   /* Was 0.75rem */
}
```

**Rule**: Customize Lumo variables in `:root` for global changes. Do NOT hardcode font sizes in components.

### Lumo Theme Variables Reference

#### Spacing Variables

```css
/* Use Lumo spacing variables for consistency */
var(--lumo-space-xs)    /* Extra small: 0.25rem (4px) */
var(--lumo-space-s)     /* Small: 0.5rem (8px) */
var(--lumo-space-m)     /* Medium: 1rem (16px) */
var(--lumo-space-l)     /* Large: 1.5rem (24px) */
var(--lumo-space-xl)    /* Extra large: 2rem (32px) */
```

#### Color Variables

```css
/* Base colors */
var(--lumo-base-color)           /* Main background */
var(--lumo-contrast-10pct)       /* Light borders */
var(--lumo-contrast-20pct)       /* Medium borders */
var(--lumo-contrast-50pct)       /* Dark borders */

/* Theme colors */
var(--lumo-primary-color)        /* Primary brand color */
var(--lumo-primary-color-10pct)  /* Light primary */
var(--lumo-primary-color-50pct)  /* Medium primary */
var(--lumo-error-color)          /* Error/danger red */
var(--lumo-success-color)        /* Success green */
var(--lumo-warning-color)        /* Warning orange */
```

#### Border Radius Variables

```css
var(--lumo-border-radius-s)      /* Small: 0.25rem */
var(--lumo-border-radius-m)      /* Medium: 0.5rem */
var(--lumo-border-radius-l)      /* Large: 0.75rem */
```

### Component-Specific CSS Classes

#### Pattern: Use Descriptive Class Names

```css
/* ✅ GOOD - Descriptive, component-specific */
.crud-toolbar {
    width: 100% !important;
    min-height: 50px;
    padding: var(--lumo-space-xs) var(--lumo-space-s);
    background: var(--lumo-base-color);
    border-bottom: 1px solid var(--lumo-contrast-10pct);
}

.hierarchical-side-menu {
    width: 100%;
    background: transparent;
    border-radius: var(--lumo-border-radius-m);
}

/* ❌ BAD - Generic, unclear purpose */
.menu {
    width: 100%;
}

.toolbar {
    padding: 8px;
}
```

#### CSS Class Naming Convention

| Pattern | Purpose | Example |
|---------|---------|---------|
| `{component}-{element}` | Component sub-element | `.crud-toolbar`, `.kanban-card` |
| `{feature}-{component}` | Feature-specific component | `.hierarchical-menu-item` |
| `{state}` | Component state | `.selected`, `.disabled`, `.expanded` |

### CSS Animation Patterns

```css
/* Define reusable animations */
@keyframes slideInRight {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

/* Apply with class */
.slide-in {
    animation: slideInRight 0.3s ease-out forwards;
}
```

**Rule**: Define animations at the top level, apply via classes.

---

## Java Component Styling Patterns

### Using getStyle() for Dynamic Styling

#### When to Use getStyle()

✅ **USE getStyle() for**:
- Dynamic values (colors, sizes based on data)
- Component-specific one-off styles
- Styles that change based on state

❌ **DON'T USE getStyle() for**:
- Global, reusable styles (use CSS instead)
- Theme-level changes (use CSS custom properties)
- Static styles that never change

#### Pattern: Dynamic Icon Styling

```java
// ✅ CORRECT - Dynamic color based on entity
public static Icon createStyledIcon(String iconString, String color) {
    final Icon icon = createStyledIcon(iconString);
    Check.notNull(icon, "Icon cannot be null");
    Check.notBlank(color, "Color cannot be null or blank");
    icon.getStyle().set("color", color);
    return icon;
}

// Dynamic sizing for icons
icon.getStyle().set("width", "24px");
icon.getStyle().set("height", "24px");
icon.getStyle().set("min-width", "24px");
icon.getStyle().set("min-height", "24px");
icon.getStyle().set("margin-right", "6px");
icon.getStyle().set("flex-shrink", "0"); // Prevent shrinking
```

#### Pattern: Entity Color Display

```java
// ✅ CORRECT - Apply entity color dynamically
public static void applyEntityColor(Component component, CEntity<?> entity) {
    try {
        String color = getColorFromEntity(entity);
        component.getStyle().set("background-color", color);
        
        // Calculate contrasting text color
        String textColor = getContrastColor(color);
        component.getStyle().set("color", textColor);
    } catch (Exception e) {
        LOGGER.warn("Could not apply entity color", e);
    }
}
```

### Layout Component Usage

#### CVerticalLayout Pattern

```java
// ✅ CORRECT - Use CVerticalLayout with Lumo utilities
CVerticalLayout layout = new CVerticalLayout();
layout.setWidth("100%");
layout.setPadding(false);
layout.setSpacing(true);
layout.setAlignItems(FlexComponent.Alignment.STRETCH);

// Add components
layout.add(header, content, footer);
```

#### CHorizontalLayout Pattern

```java
// ✅ CORRECT - Use CHorizontalLayout for toolbars
CHorizontalLayout toolbar = new CHorizontalLayout();
toolbar.setWidth("100%");
toolbar.setSpacing(true);
toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

// Add buttons
toolbar.add(buttonNew, buttonEdit, buttonDelete);
```

### Standard Component Sizing

#### Width Standards

```java
// Full width - common for containers
component.setWidth("100%");

// Minimum width - for buttons with text
button.setMinWidth("120px");

// Fixed width - for specific layouts
component.setWidth("300px");

// Auto width - let content determine size
component.setWidthFull();  // or setWidth("auto")
```

#### Height Standards

```java
// Full height - fill parent
component.setHeight("100%");

// Minimum height - prevent collapse
component.setMinHeight("50px");

// Fixed height - specific requirements
component.setHeight("400px");

// Auto height - let content determine size
component.setHeightFull();
```

### Spacing and Padding Standards

#### Using Lumo Spacing in Java

```java
// ✅ CORRECT - Use Lumo spacing variables
layout.getStyle().set("padding", "var(--lumo-space-m)");
layout.getStyle().set("gap", "var(--lumo-space-s)");
layout.getStyle().set("margin", "var(--lumo-space-xs)");

// ❌ INCORRECT - Hardcoded values
layout.getStyle().set("padding", "16px");
layout.getStyle().set("margin", "8px");
```

#### Layout Spacing Methods

```java
// Built-in Vaadin methods (preferred when available)
layout.setSpacing(true);   // Use theme default spacing
layout.setPadding(false);  // Remove default padding
layout.setMargin(true);    // Add margin

// Custom spacing using CSS
layout.getStyle().set("gap", "var(--lumo-space-m)");
```

---

## Color and Icon Standards

### CRUD Operation Color Constants

Defined in `CColorUtils.java`:

```java
// CRUD Button Color Constants
public static final String CRUD_CREATE_COLOR = "#4B7F82";  // CDE Green
public static final String CRUD_UPDATE_COLOR = "#FFEAAA";  // CDE Yellow
public static final String CRUD_DELETE_COLOR = "#91856C";  // OpenWindows Border Dark
public static final String CRUD_SAVE_COLOR = "#6B5FA7";    // CDE Purple
public static final String CRUD_CANCEL_COLOR = "#8E8E8E";  // CDE Dark Gray
public static final String CRUD_READ_COLOR = "#4966B0";    // OpenWindows Selection Blue

// CRUD Button Icon Constants
public static final String CRUD_CREATE_ICON = "vaadin:plus";
public static final String CRUD_EDIT_ICON = "vaadin:edit";
public static final String CRUD_DELETE_ICON = "vaadin:trash";
public static final String CRUD_SAVE_ICON = "vaadin:check";
public static final String CRUD_CANCEL_ICON = "vaadin:close";
public static final String CRUD_VIEW_ICON = "vaadin:eye";
public static final String CRUD_CLONE_ICON = "vaadin:copy";
```

**Rule**: Always use these constants for CRUD operations. Do NOT define custom colors for standard operations.

### Icon Sizing Standards

```java
// Icon size constants
public static int ICON_SIZE_NORMAL = 32;
private static final String DEFAULT_ICON_SIZE = "16px";
private static final String DEFAULT_ICON_MARGIN = "6px";

// Icon size classes (Vaadin LumoUtility)
IconSize.SMALL    // 16px
IconSize.MEDIUM   // 24px
IconSize.LARGE    // 32px
```

**Pattern**: Use `IconSize` enum for consistent sizing:

```java
// ✅ CORRECT
Icon icon = VaadinIcon.PLUS.create();
icon.addClassNames(IconSize.MEDIUM);

// ✅ ALSO CORRECT - via CColorUtils
Icon icon = CColorUtils.createStyledIcon("vaadin:plus");
icon.addClassNames(IconSize.SMALL);
```

### Entity Display Patterns

#### Entity with Icon Pattern (Deprecated - Use CLabelEntity)

```java
// ⚠️ DEPRECATED - Use CLabelEntity instead
@Deprecated
public static HorizontalLayout getEntityWithIcon(CEntityNamed<?> entity) {
    Check.notNull(entity, "Entity cannot be null");
    final HorizontalLayout layout = new HorizontalLayout();
    layout.setAlignItems(FlexComponent.Alignment.CENTER);
    layout.setSpacing(true);
    
    // Add icon
    final Icon icon = getIconForEntity(entity);
    if (icon != null) {
        icon.setSize("24px");
        layout.add(icon);
    }
    
    // Add entity name
    final Span entityName = new Span(entity.getName());
    layout.add(entityName);
    
    return layout;
}
```

**Rule**: Use `CLabelEntity` class for consistent entity display instead of manual layouts.

#### Entity Color Application

```java
// Get color from entity
public static String getColorFromEntity(CEntity<?> entity) throws Exception {
    Check.notNull(entity, "entity cannot be null");
    
    // Check for color property
    if (entity instanceof CStatus) {
        return ((CStatus) entity).getColor();
    }
    
    // Check static DEFAULT_COLOR constant
    String color = CEntityRegistry.getDefaultColor(entity.getClass());
    if (color != null) {
        return color;
    }
    
    return DEFAULT_COLOR;
}

// Calculate contrasting text color
public static String getContrastColor(String backgroundColor) throws Exception {
    Check.notBlank(backgroundColor, "backgroundColor cannot be null or blank");
    
    double brightness = getBrightness(backgroundColor);
    // Return white text for dark backgrounds, black for light
    return brightness < 0.5 ? DEFAULT_LIGHT_TEXT : DEFAULT_DARK_TEXT;
}
```

---

## Button Creation Patterns

### Factory Methods for Standard Buttons

Always use factory methods from `CButton` class:

```java
// ✅ CORRECT - Use factory methods
CButton buttonNew = CButton.createNewButton("New", e -> onCreate());
CButton buttonEdit = CButton.createEditButton("Edit", e -> onEdit());
CButton buttonDelete = CButton.createDeleteButton("Delete", e -> onDelete());
CButton buttonSave = CButton.createSaveButton("Save", e -> onSave());
CButton buttonCancel = CButton.createCancelButton("Cancel", e -> onCancel());

// Custom primary button
CButton buttonCustom = CButton.createPrimary("Custom", 
    VaadinIcon.COG.create(), e -> onCustomAction());

// ❌ INCORRECT - Manual button creation
Button button = new Button("New", VaadinIcon.PLUS.create());
button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
```

### Button Sizing Pattern

```java
// CButton automatically sets minimum width for buttons with text
protected void initializeComponent() {
    CAuxillaries.setId(this);
    if (!getText().isEmpty()) {
        setMinWidth("120px");  // Standard minimum width
    }
}
```

**Rule**: All buttons with text should have a minimum width of 120px for consistency.

### Button Theme Variants

```java
// Standard theme variants from CButton factory methods
ButtonVariant.LUMO_SUCCESS    // Green - Create/New operations
ButtonVariant.LUMO_PRIMARY    // Blue - Primary actions (Save)
ButtonVariant.LUMO_ERROR      // Red - Delete/Remove operations
ButtonVariant.LUMO_CONTRAST   // Gray - Edit/Update operations
ButtonVariant.LUMO_TERTIARY   // Transparent - Secondary actions
```

---

## Grid and List Display Standards

### Grid Column Configuration

```java
// Standard grid setup
CGrid<CEntity> grid = new CGrid<>(CEntity.class);
grid.setWidth("100%");
grid.setHeight("100%");

// Add columns with proper headers
grid.addColumn(CEntity::getName)
    .setHeader("Name")
    .setSortable(true)
    .setResizable(true);

grid.addColumn(entity -> formatDate(entity.getCreated()))
    .setHeader("Created")
    .setAutoWidth(true);

// Color-coded status column
grid.addComponentColumn(entity -> {
    Span statusBadge = new Span(entity.getStatus().getName());
    statusBadge.getStyle().set("background-color", entity.getStatus().getColor());
    statusBadge.getStyle().set("color", getContrastColor(entity.getStatus().getColor()));
    statusBadge.getStyle().set("padding", "4px 8px");
    statusBadge.getStyle().set("border-radius", "var(--lumo-border-radius-s)");
    return statusBadge;
}).setHeader("Status");
```

### Grid Styling via CSS

```css
/* Grid customization in CSS */
vaadin-grid {
    --lumo-font-size: var(--lumo-font-size-m);
}

vaadin-grid-cell-content {
    padding: var(--lumo-space-xs) var(--lumo-space-s);
}
```

---

## Toolbar and Action Bar Standards

### CRUD Toolbar Pattern

```css
/* Standard CRUD toolbar CSS */
.crud-toolbar {
    width: 100% !important;
    min-height: 50px;
    padding: var(--lumo-space-xs) var(--lumo-space-s);
    background: var(--lumo-base-color);
    border-bottom: 1px solid var(--lumo-contrast-10pct);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: var(--lumo-space-xs);
}
```

```java
// Java implementation
CHorizontalLayout toolbar = new CHorizontalLayout();
toolbar.addClassName("crud-toolbar");
toolbar.setWidth("100%");
toolbar.add(buttonNew, buttonEdit, buttonDelete, buttonSave, buttonCancel);
```

**Rule**: Toolbars should have consistent styling via CSS classes, not inline styles.

---

## Responsive Design Patterns

### Flexible Layouts

```java
// ✅ CORRECT - Responsive flex layout
CVerticalLayout mainLayout = new CVerticalLayout();
mainLayout.setWidth("100%");
mainLayout.setFlexGrow(1, contentArea);  // Content grows
mainLayout.setFlexGrow(0, toolbar);      // Toolbar fixed

// Responsive grid
CGrid<CEntity> grid = new CGrid<>(CEntity.class);
grid.setWidthFull();
grid.setHeightFull();
grid.setColumnReorderingAllowed(true);
```

### Media Queries in CSS

```css
/* Responsive adjustments */
@media (max-width: 768px) {
    .crud-toolbar {
        flex-direction: column;
        align-items: stretch;
    }
    
    .hierarchical-menu-item {
        padding: 8px 12px;
        font-size: var(--lumo-font-size-s);
    }
}
```

---

## Common Pitfalls and Solutions

### Pitfall 1: Hardcoded Sizes

❌ **WRONG**:
```java
component.getStyle().set("padding", "16px");
component.getStyle().set("margin", "8px");
```

✅ **CORRECT**:
```java
component.getStyle().set("padding", "var(--lumo-space-m)");
component.getStyle().set("margin", "var(--lumo-space-s)");
```

### Pitfall 2: Inconsistent Button Styling

❌ **WRONG**:
```java
Button button = new Button("Delete");
button.getStyle().set("background-color", "red");
button.getStyle().set("color", "white");
```

✅ **CORRECT**:
```java
CButton button = CButton.createDeleteButton("Delete", e -> onDelete());
```

### Pitfall 3: Missing Component Width

❌ **WRONG**:
```java
// Layout doesn't fill parent
CVerticalLayout layout = new CVerticalLayout();
layout.add(content);
```

✅ **CORRECT**:
```java
CVerticalLayout layout = new CVerticalLayout();
layout.setWidth("100%");  // Explicitly set width
layout.add(content);
```

### Pitfall 4: Not Using Theme Variables

❌ **WRONG**:
```css
.custom-menu {
    background: #f0f0f0;
    color: #333333;
    border: 1px solid #cccccc;
}
```

✅ **CORRECT**:
```css
.custom-menu {
    background: var(--lumo-base-color);
    color: var(--lumo-body-text-color);
    border: 1px solid var(--lumo-contrast-10pct);
}
```

---

## Checklist for UI Components

Use this checklist when creating or reviewing UI components:

- [ ] Component uses C-prefixed class (CButton, CVerticalLayout, etc.)
- [ ] Width/height explicitly set (typically "100%" for containers)
- [ ] Spacing uses Lumo variables (`var(--lumo-space-*)`)
- [ ] Colors use Lumo variables or entity color constants
- [ ] Icons use standard size classes (IconSize.SMALL/MEDIUM/LARGE)
- [ ] Buttons use factory methods (CButton.createNewButton, etc.)
- [ ] Dynamic styles use `getStyle().set()` with clear purpose
- [ ] Global styles defined in CSS files, not inline
- [ ] CSS class names are descriptive and consistent
- [ ] Entity display uses standard patterns (CLabelEntity, etc.)

---

## Related Documentation

- [Coding Standards](coding-standards.md) - General coding patterns
- [Component Coding Standards](../development/component-coding-standards.md) - Component development rules
- [GitHub Copilot Guidelines](../development/copilot-guidelines.md) - AI assistant patterns

---

## AI Agent Guidelines

### For GitHub Copilot and Codex

When generating UI code:

1. **Always use C-prefixed components**: `CButton`, `CVerticalLayout`, `CHorizontalLayout`
2. **Use factory methods**: `CButton.createNewButton()` instead of manual construction
3. **Reference Lumo variables**: Use `var(--lumo-space-m)` instead of hardcoded values
4. **Check constants**: Use `CColorUtils.CRUD_*_COLOR` and `CColorUtils.CRUD_*_ICON`
5. **Set explicit widths**: Always set width on container components
6. **Use consistent patterns**: Follow examples in this document

### Pattern Recognition Prompts

```java
// When you see: "Create a toolbar with buttons"
// Generate:
CHorizontalLayout toolbar = new CHorizontalLayout();
toolbar.addClassName("crud-toolbar");
toolbar.setWidth("100%");
CButton buttonNew = CButton.createNewButton("New", e -> onCreate());
CButton buttonEdit = CButton.createEditButton("Edit", e -> onEdit());
toolbar.add(buttonNew, buttonEdit);

// When you see: "Style an icon with entity color"
// Generate:
Icon icon = CColorUtils.createStyledIcon(entity.getDefaultIcon(), 
    CColorUtils.getColorFromEntity(entity));
icon.addClassNames(IconSize.MEDIUM);

// When you see: "Create a vertical layout"
// Generate:
CVerticalLayout layout = new CVerticalLayout();
layout.setWidth("100%");
layout.setSpacing(true);
layout.setPadding(false);
```

---

**Version**: 1.0  
**Last Updated**: 2026-01-01  
**Status**: Active
