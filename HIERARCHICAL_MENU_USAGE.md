# Hierarchical Side Menu Usage Guide

## Overview

The Hierarchical Side Menu provides up to 4 levels of navigation with sliding animations and back button functionality. It automatically parses `@Menu` annotations from your route views.

## Quick Start

### 1. Add Menu Annotation to Your View

```java
@Route("my/deep/route/path")
@Menu(order = 1, icon = "vaadin:folder", title = "Level1.Level2.Level3.Level4")
@PermitAll
public class MyView extends VerticalLayout {
    // Your view implementation
}
```

### 2. Menu Hierarchy Format

Use dots (.) to separate menu levels in the title:

- **2 levels**: `"Settings.Projects"` → Settings → Projects
- **3 levels**: `"Settings.Projects.Configuration"` → Settings → Projects → Configuration  
- **4 levels**: `"Settings.Projects.Configuration.Advanced"` → Settings → Projects → Configuration → Advanced

### 3. Example Menu Structures

```java
// Administration menu with 4 levels
@Menu(title = "Administration.Users.Permissions.Roles")
@Menu(title = "Administration.Users.Permissions.Groups")
@Menu(title = "Administration.System.Configuration.Database")
@Menu(title = "Administration.System.Configuration.Security")
@Menu(title = "Administration.Reports.Analytics.UserActivity")
@Menu(title = "Administration.Reports.Analytics.SystemUsage")

// Project management with 3 levels
@Menu(title = "Projects.Management.Overview")
@Menu(title = "Projects.Management.Timeline")
@Menu(title = "Projects.Settings.General")
@Menu(title = "Projects.Settings.Permissions")

// Simple 2-level structure (existing format still works)
@Menu(title = "Settings.Profile")
@Menu(title = "Settings.Preferences")
```

## Features

### Navigation
- **Click** navigation items (with arrows) to go deeper
- **Click** the **back button** (←) to go up one level
- **Click** final menu items to navigate to actual pages

### Visual Feedback
- **Hover effects**: Items highlight and slide slightly
- **Level indicators**: Progressive indentation and color coding
- **Smooth animations**: 0.3s slide transitions between levels

### Responsive Design
- **Scrollable**: Long menus scroll vertically
- **Max width**: 300px to maintain usability
- **Mobile friendly**: Touch-friendly buttons and spacing

## Integration

The hierarchical menu is automatically available in the sliding header area of MainLayout. It coexists with the original side navigation menu.

### Location
- **Main Menu**: Left sidebar (original 2-level menu)
- **Hierarchical Menu**: Sliding header area (new 4-level menu)

## Examples in This Project

Check out these example views to see the hierarchical menu in action:

1. **4-Level Example**: `Examples.Hierarchy.DeepMenu.Sample`
   - Route: `/examples/hierarchy/deep/sample`
   - Demonstrates maximum depth navigation

2. **3-Level Example**: `Examples.Settings.Advanced`
   - Route: `/examples/settings/advanced`
   - Shows mid-level hierarchy

## Best Practices

### Menu Organization
- Use logical groupings for level 1 (e.g., "Administration", "Projects", "Reports")
- Keep level 2 for major categories (e.g., "Users", "System", "Analytics")
- Use level 3 for specific areas (e.g., "Permissions", "Configuration")
- Reserve level 4 for detailed options (e.g., "Roles", "Groups", "Advanced")

### Naming Conventions
- Use clear, descriptive names
- Keep menu titles concise (1-2 words per level)
- Use consistent terminology across similar areas
- Consider alphabetical ordering for better user experience

### Icon Selection
- Use meaningful icons that represent the content
- Maintain consistency within menu branches
- Consider using Vaadin's built-in icon set for uniformity

## Technical Notes

- **Maximum Levels**: 4 (enforced by MAX_MENU_LEVELS constant)
- **Animation Duration**: 0.3s (configurable in CSS)
- **Parsing**: Automatic from MenuConfiguration.getMenuEntries()
- **State Management**: Navigation path tracked internally

## Troubleshooting

### Menu Not Appearing
- Ensure your view has a proper `@Menu` annotation
- Check that the route is accessible (correct security annotations)
- Verify the menu title format uses dots correctly

### Navigation Issues
- Confirm menu titles don't exceed 4 levels
- Check for typos in menu title format
- Ensure parent levels are properly created

### Styling Issues
- Custom CSS should target `.hierarchical-side-menu` class
- Check browser console for CSS errors
- Verify all required CSS classes are loaded

For detailed technical documentation, see `src/docs/HIERARCHICAL_SIDE_MENU_IMPLEMENTATION.md`.