# Dual List Selector Component Usage Guide

## Overview

The Dual List Selector Component provides a better user experience for selecting and ordering multiple items compared to the standard MultiSelectComboBox. It displays available items on the left and selected items on the right, with buttons for adding, removing, and reordering selections.

## Components

### 1. CDualListSelectorComponent<T>

A generic component that can work with any type of data.

**Features:**
- Two-panel interface (available items / selected items)
- Add/Remove buttons for moving items between lists
- Up/Down buttons for ordering selected items
- Implements `HasValue` and `HasValueAndElement` for Vaadin binder integration
- Customizable item label generators
- Read-only mode support

**Basic Usage:**

```java
// Create component
CDualListSelectorComponent<CUser> userSelector = 
    new CDualListSelectorComponent<>("Available Users", "Selected Users");

// Set available items
List<CUser> allUsers = userService.list();
userSelector.setItems(allUsers);

// Configure display
userSelector.setItemLabelGenerator(user -> user.getName());

// Get selected items
Set<CUser> selectedUsers = userSelector.getValue();
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
// - A CDualListSelectorComponent for "Assigned Users" 
// - A MultiSelectComboBox for "Tags"
```

### Manual Usage

If you need to use the component outside of CFormBuilder:

```java
// Create the component
CDualListSelectorComponent<CUser> userSelector = 
    new CDualListSelectorComponent<>("Available Users", "Team Members");

// Configure
userSelector.setItemLabelGenerator(user -> user.getName());
userSelector.setItems(userService.list());

// Add value change listener
userSelector.addValueChangeListener(event -> {
    Set<CUser> selectedUsers = event.getValue();
    System.out.println("Selected " + selectedUsers.size() + " users");
});

// Bind to binder (optional)
binder.bind(userSelector, CTask::getAssignedUsers, CTask::setAssignedUsers);
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
