# Hierarchical Side Menu Implementation

## Overview

This document details the implementation of a hierarchical side menu system for the Derbent application. The menu supports up to 4 levels of navigation with sliding animations, back button functionality, and automatic parsing of route annotations.

## Architecture

### Main Components

1. **CHierarchicalSideMenu.java** - The main menu component class
2. **MainLayout.java** - Integration point in createSlidingHeader() function
3. **styles.css** - CSS animations and styling

### Design Principles

- **Modular Design**: Menu implementation is separated into its own class following the C prefix convention
- **MVC Architecture**: Menu acts as a View component with proper separation of concerns
- **Hierarchical Navigation**: Supports up to 4 levels with breadcrumb-style navigation
- **Animation Support**: Smooth sliding transitions between menu levels
- **Route Integration**: Automatically parses @Menu annotations to build menu structure

## Implementation Details

### Class Structure

#### CHierarchicalSideMenu
```java
public final class CHierarchicalSideMenu extends Div
```

**Key Features:**
- Extends Div for proper Vaadin component integration
- Supports up to 4 levels of menu hierarchy (MAX_MENU_LEVELS = 4)
- Automatic menu building from route annotations
- Sliding animations with CSS transitions
- Back button navigation
- Proper logging and error handling

#### Inner Classes

1. **CMenuLevel** - Represents a single level in the menu hierarchy
   - Stores level key, display name, and parent reference
   - Manages navigation and menu items
   - Creates UI components for the level

2. **CMenuItem** - Represents individual menu items
   - Handles both navigation items (to sub-levels) and leaf items (to routes)
   - Creates clickable UI components with icons and text
   - Manages click events for navigation

### Menu Hierarchy Parsing

The system parses menu entries from `@Menu` annotations with titles in the format:
```
parentItem2.childItem1.childofchileitem1.finalItem
```

**Parsing Logic:**
1. Split title by dots (.) to get hierarchy levels
2. Ensure all parent levels exist, creating them if necessary
3. Add navigation items for intermediate levels
4. Add final menu items as leaf nodes with actual routes

**Example Menu Structure:**
```
Settings                    (Level 1)
├── Projects               (Level 2 - Navigation)
│   ├── Project Types      (Level 3 - Route)
│   └── Project Settings   (Level 3 - Route)
├── Meetings               (Level 2 - Navigation)
│   ├── Meeting Types      (Level 3 - Route)
│   └── Meeting Settings   (Level 3 - Route)
└── Activities             (Level 2 - Navigation)
    ├── Activity Types     (Level 3 - Route)
    └── Activity Settings  (Level 3 - Route)
```

### Navigation Flow

1. **Initial State**: Menu starts at root level showing top-level items
2. **Navigation Down**: Clicking navigation items slides to sub-level with animation
3. **Navigation Up**: Back button returns to parent level
4. **Route Navigation**: Clicking leaf items navigates to actual pages
5. **State Management**: Navigation path is tracked for proper back button functionality

### Integration with MainLayout

The menu is integrated into the existing MainLayout.java through the `createSlidingHeader()` function:

```java
private Div createSlidingHeader() {
    // Original header content (logo, app name, version)
    final var slidingHeader = new Div();
    // ... existing header content ...
    
    // Add hierarchical side menu
    final var hierarchicalMenu = new CHierarchicalSideMenu();
    hierarchicalMenu.addClassNames(Margin.Top.MEDIUM);
    
    // Combine into complete header
    final var completeHeader = new Div();
    completeHeader.add(slidingHeader, hierarchicalMenu);
    
    return completeHeader;
}
```

**Key Design Decisions:**
- **Minimal Changes**: Only the createSlidingHeader() function was modified
- **Backward Compatibility**: Original menu remains functional
- **Clean Separation**: New menu is a separate component

## Styling and Animations

### Icon Color Methodology

The menu uses a consistent color system for icons based on functionality, implemented in the `getIconColor()` method of `CHierarchicalSideMenu`:

```java
private String getIconColor(final String iconName) {
    // Remove vaadin: prefix if present
    final String cleanIconName = iconName.replace("vaadin:", "").toLowerCase();
    
    // Functional color groupings
    // Meetings - Green
    if ("group".equals(cleanIconName) || "calendar".equals(cleanIconName)) {
        return "#28a745";
    }
    // Activities - Blue  
    if ("calendar-clock".equals(cleanIconName) || "tasks".equals(cleanIconName) || "flag".equals(cleanIconName)) {
        return "#007bff";
    }
    // Projects - Orange
    if ("briefcase".equals(cleanIconName) || "folder".equals(cleanIconName) || "dashboard".equals(cleanIconName) || "grid-big".equals(cleanIconName)) {
        return "#fd7e14";
    }
    // Users - Purple
    if ("users".equals(cleanIconName)) {
        return "#6f42c1";
    }
    // Settings & Administration - Gray
    if ("cogs".equals(cleanIconName) || "building".equals(cleanIconName) || "tags".equals(cleanIconName)) {
        return "#6c757d";
    }
    // Decisions - Red
    if ("gavel".equals(cleanIconName)) {
        return "#dc3545";
    }
    // Commerce - Teal
    if ("cart".equals(cleanIconName)) {
        return "#20c997";
    }
    // Navigation & General - Primary
    if ("home".equals(cleanIconName) || "cubes".equals(cleanIconName) || "arrow-left".equals(cleanIconName) || 
        "tree-table".equals(cleanIconName) || "chevron-right".equals(cleanIconName)) {
        return "var(--lumo-primary-color)";
    }
    
    // Default fallback
    return "var(--lumo-primary-color)";
}
```

**Color Groups By Functionality:**
- **Meetings (Green #28a745)**: group, calendar
- **Activities (Blue #007bff)**: calendar-clock, tasks, flag
- **Projects (Orange #fd7e14)**: briefcase, folder, dashboard, grid-big
- **Users (Purple #6f42c1)**: users
- **Settings & Administration (Gray #6c757d)**: cogs, building, tags
- **Decisions (Red #dc3545)**: gavel
- **Commerce (Teal #20c997)**: cart
- **Navigation & General (Theme Primary)**: home, cubes, arrow-left, tree-table, chevron-right

**Implementation Details:**
- Colors are applied in the `CMenuItem.createComponent()` method using:
  ```java
  final String iconColor = getIconColor(iconName);
  icon.getStyle().set("color", iconColor);
  ```
- The system automatically handles Vaadin icon prefixes
- Default color for unknown icons is the primary theme color

### CSS Classes

1. **`.hierarchical-side-menu`** - Main container styling
2. **`.hierarchical-header`** - Header with back button area
3. **`.hierarchical-level-container`** - Container for current menu level
4. **`.hierarchical-menu-item`** - Individual menu item styling
5. **`.hierarchical-back-button`** - Back button specific styling

### Animation System

**Sliding Animations:**
- **slideInRight**: New levels slide in from the right
- **slideInLeft**: Back navigation slides from the left
- **Duration**: 0.3s ease-out transitions
- **Hover Effects**: Items lift and highlight on hover

**Level Indicators:**
- Different padding and border colors for each level depth
- Visual hierarchy through progressive indentation
- Color coding: Level 4 uses error color to indicate maximum depth

### Responsive Design

- **Max Width**: 300px to prevent menu from becoming too wide
- **Scrollable**: Vertical scrolling for levels with many items
- **Flexible Height**: Adapts to content while maintaining usability

## Usage Examples

### Adding Menu Items via Annotations

```java
@Route("projects/settings")
@Menu(order = 1, icon = "vaadin:cog", title = "Settings.Projects.Configuration")
@PermitAll
public class ProjectSettingsView extends VerticalLayout {
    // View implementation
}
```

This creates:
- Settings (Level 1)
- └── Projects (Level 2)
    - └── Configuration (Level 3 - Route to /projects/settings)

### Complex Hierarchy Example

```java
@Menu(title = "Administration.Users.Permissions.Roles")
@Menu(title = "Administration.Users.Permissions.Groups") 
@Menu(title = "Administration.System.Logs.Audit")
@Menu(title = "Administration.System.Logs.Error")
```

Creates a 4-level hierarchy under Administration.

## Testing and Validation

### Build Validation
```bash
mvn clean compile
```

### Menu Structure Validation
- Check menu levels are created correctly
- Verify navigation works between levels
- Confirm back button functionality
- Test route navigation to actual pages

### UI/UX Testing
- Verify sliding animations work smoothly
- Check responsive behavior
- Test hover and click interactions
- Validate accessibility

## Compliance with Coding Guidelines

### C Prefix Convention
- **CHierarchicalSideMenu**: Main component class
- **CMenuLevel**: Inner class for menu levels  
- **CMenuItem**: Inner class for menu items
- **CButton**: Used for back button (following existing pattern)

### Logging Standards
- Logger initialized in constructor with proper class reference
- Debug logging for menu building and navigation
- Warning logs for error conditions
- Info logs for major operations

### Error Handling
- Null checks for menu entries and parameters
- Graceful degradation for missing menu items
- Proper exception handling in navigation

### Documentation
- Comprehensive JavaDoc for all public methods
- Inline comments explaining complex logic
- Architecture documentation (this file)

## Future Enhancements

### Potential Improvements
1. **Icons for Levels**: Different icons for different hierarchy levels
2. **Search Functionality**: Filter menu items by text
3. **Favorites**: Pin frequently used items to top level
4. **Persistence**: Remember last visited level between sessions
5. **Keyboard Navigation**: Arrow key support for accessibility

### Performance Optimizations
1. **Lazy Loading**: Only build levels when first accessed
2. **Virtual Scrolling**: For levels with many items
3. **Caching**: Cache built menu structures

## Integration Notes

### Original Menu Preservation
The original side navigation menu created by `createSideNav()` remains fully functional. The hierarchical menu is an additional component that coexists with the original system.

### Migration Path
Teams can gradually migrate from the original 2-level menu system to the new 4-level hierarchical system by:
1. Updating route annotations to use deeper hierarchy
2. Testing new menu functionality
3. Eventually removing original menu if desired

### Backward Compatibility
Existing menu entries with 2-level hierarchy (e.g., "Settings.Projects") continue to work without modification in the new system.

## Summary

The hierarchical side menu implementation provides a scalable, user-friendly navigation system that supports up to 4 levels of menu hierarchy while maintaining clean code architecture and following project conventions. The system is designed for easy maintenance, extension, and integration with existing Vaadin components.