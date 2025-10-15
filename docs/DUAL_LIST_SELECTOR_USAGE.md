# Dual List Selector Component Usage Guide

## Overview

The Dual List Selector Component provides a better user experience for selecting and ordering multiple items compared to the standard MultiSelectComboBox. It displays available items on the left and selected items on the right, with buttons for adding, removing, and reordering selections.

**NEW:** The component now supports color-aware rendering for entities! When used with `CEntityNamed` entities (such as statuses, types, activities, users, etc.), items are displayed with their associated colors and icons, providing the same visual experience as `CColorAwareComboBox`.

## Components

### 1. CDualListSelectorComponent<T>

A generic component that can work with any type of data, with special support for colored entity rendering.

**Features:**
- Two-panel interface (available items / selected items)
- Add/Remove buttons for moving items between lists
- Up/Down buttons for ordering selected items
- **Color-aware rendering for CEntityNamed entities** (automatically displays colors and icons)
- Implements `HasValue` and `HasValueAndElement` for Vaadin binder integration
- Customizable item label generators for non-entity types
- Read-only mode support

**Basic Usage with Colored Entities:**

```java
// Create component with entity support
CDualListSelectorComponent<CStatus> statusSelector = 
    new CDualListSelectorComponent<>("Available Statuses", "Selected Statuses");

// Set available items (colors and icons will be displayed automatically)
List<CStatus> allStatuses = statusService.list();
statusSelector.setItems(allStatuses);

// Get selected items
Set<CStatus> selectedStatuses = statusSelector.getValue();
```

**Basic Usage with Strings:**

```java
// Create component
CDualListSelectorComponent<String> stringSelector = 
    new CDualListSelectorComponent<>("Available Options", "Selected Options");

// Set available items
List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");
stringSelector.setItems(options);

// Configure display (optional for strings)
stringSelector.setItemLabelGenerator(String::toUpperCase);

// Get selected items
Set<String> selectedOptions = stringSelector.getValue();
```

### Color-Aware Rendering

The component automatically detects when items are `CEntityNamed` entities (which include color and icon properties) and renders them with their associated visual styling:

- **Entities with colors:** Status entities, type entities, activities, meetings, etc. are displayed with their configured background colors
- **Automatic text contrast:** Text color is automatically adjusted (light/dark) for readability
- **Icons:** Entity icons are displayed alongside the text
- **Rounded corners:** Visual polish with rounded corner styling
- **Fallback rendering:** Non-entity items (strings, numbers, etc.) are displayed as simple text

**Example with colored entities:**

```java
// Works with any CEntityNamed entity type
CDualListSelectorComponent<CActivityType> typeSelector = 
    new CDualListSelectorComponent<>("Available Types", "Selected Types");
typeSelector.setItems(activityTypeService.list());
// Types will be displayed with their configured colors and icons

CDualListSelectorComponent<CUser> userSelector = 
    new CDualListSelectorComponent<>("Available Users", "Team Members");
userSelector.setItems(userService.list());
// Users will be displayed with their profile colors and icons
```

### 2. CFieldSelectionComponent

A specialized version for selecting entity fields used in grid configuration. Wraps CDualListSelectorComponent and provides field-specific functionality.

**Usage:**

```java
CFieldSelectionComponent fieldSelector = 
    new CFieldSelectionComponent("Available Fields", "tech.derbent.activities.domain.CActivity");

// Get selected fields
List<FieldSelection> selections = fieldSelector.getSelectedFields();
```

## Integration with CFormBuilder

The Dual List Selector can be automatically used by CFormBuilder for Set fields when marked with the `useDualListSelector` annotation.

### Using the Annotation

```java
@Entity
public class CProject extends CEntityDB<CProject> {
    
    @ManyToMany
    @AMetaData(
        displayName = "Team Members",
        dataProviderBean = "userService",
        dataProviderMethod = "list",
        useDualListSelector = true  // Use dual list selector instead of MultiSelectComboBox
    )
    private Set<CUser> teamMembers = new LinkedHashSet<>();
    
    // getters and setters
}
```

### How It Works

When CFormBuilder processes the entity:

1. It detects the field is a `Set` type
2. It checks if `useDualListSelector = true` in the `@AMetaData` annotation
3. If true, it creates a `CDualListSelectorComponent` instead of `MultiSelectComboBox`
4. The component is automatically bound to the field via the binder

### Choosing Between Components

| Feature | MultiSelectComboBox | CDualListSelectorComponent |
|---------|-------------------|---------------------------|
| Space Efficiency | ✓ More compact | Less compact |
| Ordering Control | ✗ No ordering | ✓ Up/Down buttons |
| Visual Clarity | Moderate | ✓ Very clear |
| Large Lists | ✓ Better for 20+ items | Better for < 20 items |
| Mobile Friendly | ✓ More mobile-friendly | Less mobile-friendly |

**Recommendations:**
- Use `MultiSelectComboBox` (default) for:
  - Simple multi-select scenarios
  - Large lists (20+ items)
  - Mobile-first applications
  - Space-constrained UIs

- Use `CDualListSelectorComponent` (`useDualListSelector = true`) for:
  - When ordering matters
  - Smaller sets (< 20 items)
  - Desktop-focused applications
  - When visual clarity is important

## Complete Example

### Entity Definition

```java
@Entity
public class CTask extends CEntityDB<CTask> {
    
    @ManyToMany
    @AMetaData(
        displayName = "Assigned Users",
        dataProviderBean = "userService",
        dataProviderMethod = "list",
        useDualListSelector = true
    )
    private Set<CUser> assignedUsers = new LinkedHashSet<>();
    
    @ManyToMany
    @AMetaData(
        displayName = "Statuses",  
        dataProviderBean = "statusService",
        dataProviderMethod = "list",
        useDualListSelector = true  // Status entities will display with colors
    )
    private Set<CStatus> applicableStatuses = new LinkedHashSet<>();
    
    @ManyToMany
    @AMetaData(
        displayName = "Tags",
        dataProviderBean = "tagService",
        dataProviderMethod = "list",
        useDualListSelector = false  // Use MultiSelectComboBox (default)
    )
    private Set<CTag> tags = new LinkedHashSet<>();
    
    // getters and setters
}
```

### Form Creation

```java
// CFormBuilder will automatically create the correct component
CFormBuilder<CTask> formBuilder = new CFormBuilder<>(this, CTask.class, binder);

// The form will have:
// - A CDualListSelectorComponent for "Assigned Users" (with profile colors)
// - A CDualListSelectorComponent for "Statuses" (with status colors and icons)
// - A MultiSelectComboBox for "Tags"
```

### Manual Usage

If you need to use the component outside of CFormBuilder:

```java
// Create the component for colored entities
CDualListSelectorComponent<CStatus> statusSelector = 
    new CDualListSelectorComponent<>("Available Statuses", "Selected Statuses");

// Set items (colors and icons will be displayed automatically)
statusSelector.setItems(statusService.list());

// Add value change listener
statusSelector.addValueChangeListener(event -> {
    Set<CStatus> selectedStatuses = event.getValue();
    System.out.println("Selected " + selectedStatuses.size() + " statuses");
});

// Bind to binder (optional)
binder.bind(statusSelector, CTask::getApplicableStatuses, CTask::setApplicableStatuses);
```

**For non-entity types (strings, numbers, etc.):**

```java
// Create the component
CDualListSelectorComponent<String> tagSelector = 
    new CDualListSelectorComponent<>("Available Tags", "Selected Tags");

// Configure display
tagSelector.setItemLabelGenerator(tag -> "#" + tag);
tagSelector.setItems(Arrays.asList("bug", "feature", "enhancement"));

// Add value change listener
tagSelector.addValueChangeListener(event -> {
    Set<String> selectedTags = event.getValue();
    System.out.println("Selected tags: " + String.join(", ", selectedTags));
});
```

## CFieldSelectionDialog Updates

The `CFieldSelectionDialog` used for grid column selection has been updated to use the new `CDualListSelectorComponent` internally, providing a better user experience for:
- Selecting which fields to display
- Ordering fields in the grid
- Adding/removing fields dynamically

## Benefits

1. **Better UX**: Visual separation between available and selected items
2. **Ordering Support**: Easy reordering with Up/Down buttons
3. **Consistency**: Same pattern used across the application
4. **Flexibility**: Can be used with any data type
5. **Integration**: Works seamlessly with CFormBuilder and Vaadin binders
6. **Interchangeable**: Easy to switch between MultiSelectComboBox and DualListSelector via annotation
7. **Color-Aware**: Automatically displays entities with their colors and icons (like CColorAwareComboBox)
8. **Visual Clarity**: Status entities, types, and other colored entities are immediately recognizable

## Testing

Unit tests are provided in `CDualListSelectorComponentTest.java` covering:
- Component initialization
- Item management
- Value handling
- Read-only mode
- Selection operations

Run tests with:
```bash
mvn test -Dtest=CDualListSelectorComponentTest
```
