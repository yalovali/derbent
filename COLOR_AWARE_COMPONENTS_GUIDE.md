# Color-Aware Components Usage Guide

This document explains how to use the new meta annotations and specialized superclasses for color-aware and icon-aware UI components in the Derbent framework.

## Overview

The framework now provides a clean, annotation-driven approach to create enhanced ComboBox and Grid components for status entities and other entity types. This eliminates code duplication and provides consistent behavior across the application.

## Enhanced Features

### Color Support
- Automatic background colors for status entities
- Automatic contrast text color calculation
- Customizable styling options

### Icon Support (NEW)
- Automatic icons for entity types (users, companies, projects, etc.)
- Icon display alongside colors
- Consistent icon styling

## Meta Annotations

### @StatusEntity

Mark domain entities as status entities to enable automatic color detection:

```java
@StatusEntity(category = "decision", colorField = "color", nameField = "name")
@Entity
public class CDecisionStatus extends CStatus {
    // Entity implementation
}
```

### @ColorAwareComboBox

Use on entity fields to create enhanced ComboBox components:

```java
@MetaData(displayName = "Decision Status", required = false, order = 4)
@ColorAwareComboBox(roundedCorners = true, autoContrast = true)
private CDecisionStatus decisionStatus;

// For user selection with icons
@MetaData(displayName = "Assigned User", required = false, order = 5)
@ColorAwareComboBox
private CUser assignedUser;
```

### @ColorAwareGrid

Use on methods that add status columns to grids:

```java
@ColorAwareGrid(columnHeader = "Status", columnKey = "status")
public void addStatusColumn() {
    grid.addColorAwareStatusColumn(Decision::getStatus, "Status", "status");
}
```

## Specialized Superclasses

### CColorAwareComboBox

A specialized ComboBox that automatically renders entities with colors and icons:

```java
// Create a color-aware ComboBox for status entities
CColorAwareComboBox<CDecisionStatus> statusComboBox = 
    new CColorAwareComboBox<>(CDecisionStatus.class, "Status");
statusComboBox.setItems(statusList);

// Create an icon-aware ComboBox for users
CColorAwareComboBox<CUser> userComboBox = 
    new CColorAwareComboBox<>(CUser.class, "Select User");
userComboBox.setItems(userList);

// Configure styling
statusComboBox.setRoundedCorners(true);
statusComboBox.setAutoContrast(true);
statusComboBox.setPadding("6px 10px");
```

### CColorAwareGrid

A specialized Grid with enhanced color-aware status column support:

```java
// Create a color-aware Grid
CColorAwareGrid<CDecision> grid = new CColorAwareGrid<>(CDecision.class);

// Add a color-aware status column
grid.addColorAwareStatusColumn(
    CDecision::getStatus, 
    "Decision Status", 
    "status"
);

// Configure styling
grid.setRoundedCorners(true);
grid.setCenterAlign(true);
grid.setFontWeight("600");
```

## Utility Class: CColorUtils

Centralized utility methods for color and icon operations:

```java
// Check if an entity is a status entity
boolean isStatus = CColorUtils.isStatusEntity(CDecisionStatus.class);

// Extract color from an entity
String color = CColorUtils.getColorFromEntity(statusEntity);

// Get appropriate text color for contrast
String textColor = CColorUtils.getContrastTextColor("#3498db");

// Get display text from an entity
String displayText = CColorUtils.getDisplayTextFromEntity(entity);

// Normalize color format
String normalizedColor = CColorUtils.normalizeColor("3498db"); // Returns "#3498db"

// Get color with fallback
String safeColor = CColorUtils.getColorWithFallback(entity, "#95a5a6");

// NEW: Icon support
VaadinIcon icon = CColorUtils.getIconForEntity(userEntity);
boolean hasIcon = CColorUtils.shouldDisplayIcon(userEntity);
Icon iconComponent = CColorUtils.createIconForEntity(userEntity);
```

## Supported Entity Types for Icons

The framework automatically provides icons for the following entity types:
- **Users** (`CUser`, entities containing "User"): `VaadinIcon.USER`
- **Companies** (entities containing "Company"): `VaadinIcon.BUILDING`
- **Projects** (entities containing "Project"): `VaadinIcon.FOLDER`
- **Meetings** (entities containing "Meeting"): `VaadinIcon.CALENDAR`
- **Activities** (entities containing "Activity"): `VaadinIcon.TASKS`
- **Decisions** (entities containing "Decision"): `VaadinIcon.CHECK_CIRCLE`
- **Comments** (entities containing "Comment"): `VaadinIcon.COMMENT`

## Migration from Old Implementation

### Before (Old Approach)
```java
// Manual color-aware ComboBox creation
ComboBox<CDecisionStatus> comboBox = new ComboBox<>();
comboBox.setRenderer(new ComponentRenderer<>(item -> {
    Span span = new Span(item.getName());
    span.getStyle().set("background-color", item.getColor());
    // Manual contrast calculation...
    return span;
}));

// Manual user ComboBox with no icons
ComboBox<CUser> userComboBox = new ComboBox<>();
userComboBox.setItemLabelGenerator(CUser::getName);
```

### After (New Approach)
```java
// Automatic enhanced ComboBox (via annotation detection)
// Just use @ColorAwareComboBox annotation on the field
// CEntityFormBuilder automatically creates CColorAwareComboBox

// For status entities - gets colors automatically
CColorAwareComboBox<CDecisionStatus> statusComboBox = 
    new CColorAwareComboBox<>(CDecisionStatus.class);

// For user entities - gets icons automatically  
CColorAwareComboBox<CUser> userComboBox = 
    new CColorAwareComboBox<>(CUser.class);
```

## Configuration Options

### ComboBox Configuration
- `roundedCorners`: Apply rounded corners (default: true)
- `padding`: CSS padding value (default: "4px 8px")
- `autoContrast`: Auto-calculate text color (default: true)
- `minWidth`: Minimum width (default: "100%")

### Grid Configuration
- `roundedCorners`: Apply rounded corners (default: true)
- `padding`: CSS padding value (default: "4px 8px")
- `autoContrast`: Auto-calculate text color (default: true)
- `minWidth`: Minimum width for cells (default: "80px")
- `centerAlign`: Center-align text (default: true)
- `fontWeight`: Font weight (default: "500")

## Status Entity Requirements

For automatic color detection, status entities must:

1. Extend `CStatus` or `CTypeEntity`
2. Have a `getColor()` method returning a color string
3. Optionally be annotated with `@StatusEntity`
4. Have colors in hex format (e.g., "#3498db")

## Examples in the Codebase

### Status Entities
- `CDecisionStatus` - Decision statuses with colors
- `CActivityStatus` - Activity statuses with colors  
- `CMeetingStatus` - Meeting statuses with colors

### Usage Examples
- `CDecision.decisionStatus` field uses `@ColorAwareComboBox`
- `CActivity.status` field uses `@ColorAwareComboBox`
- `CMeeting.status` field uses `@ColorAwareComboBox`
- All status views use `grid.addStatusColumn()` for color-aware grid columns

## Benefits

1. **Code Reuse**: Eliminated duplication between ComboBox and Grid color rendering
2. **Consistency**: Unified styling and behavior across all enhanced components
3. **Maintainability**: Centralized color and icon logic in `CColorUtils`
4. **Type Safety**: Specialized generic classes provide compile-time type checking
5. **Configuration**: Annotation-driven configuration for easy customization
6. **Automatic Detection**: Framework automatically detects status entities and icon-capable entities
7. **Icon Support**: Automatic icons for common entity types (users, companies, projects, etc.)
8. **Backward Compatibility**: Existing code continues to work without changes