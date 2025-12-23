# Dual List Selector Component Usage Guide

## Overview

The Dual List Selector Component provides a better user experience for selecting and ordering multiple items compared to the standard MultiSelectComboBox. It displays available items on the left and selected items on the right, with buttons for adding, removing, and reordering selections.

**Features:**
- **Color-aware rendering** for entities with proper validation and error handling
- **Comprehensive parameter validation** using Check utilities
- **Clear button labels and tooltips** for better usability
- **Robust exception handling** with detailed logging
- **Icon-based buttons** for intuitive interface
- **List-based ordering preservation** for fields with @OrderColumn annotation support

## Components

### 1. CDualListSelectorComponent<T>

A generic component that can work with any type of data, with special support for colored entity rendering and comprehensive validation.

**Features:**
- Two-panel interface (available items / selected items)
- Add/Remove buttons with icons and tooltips for moving items between lists
- Up/Down buttons with icons and tooltips for ordering selected items
- **Color-aware rendering for CEntityNamed entities** (automatically displays colors and icons)
- **Parameter validation** - All public methods validate inputs with Objects.requireNonNull/notBlank
- **Exception handling** - Proper error handling with logging and meaningful exceptions
- Implements `HasValue` and `HasValueAndElement` for Vaadin binder integration
- Customizable item label generators for non-entity types
- Read-only mode support

**Validation and Error Handling:**

The component includes comprehensive validation:
- Title parameters are validated with `Check.notBlank`
- Listener parameters are validated with `Objects.requireNonNull`
- ListBox parameters are validated with `Objects.requireNonNull`
- All exceptions are logged with SLF4J Logger
- Meaningful error messages for all validation failures

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

### 2. CComponentFieldSelection<MasterEntity, DetailEntity>

A specialized component for selecting entity relationship fields with full ordering support. This component is used by CFormBuilder for fields with `useDualListSelector = true`.

**Key Features:**
- **List-based** instead of Set-based to preserve ordering
- **@OrderColumn support** - maintains item order for JPA @OrderColumn fields
- **Automatic separation** - splits source items into selected and available lists based on entity's current value
- **Color-aware rendering** for CEntityNamed entities
- **Full Vaadin binder integration** with List<DetailEntity> type

**Important: List vs Set**

As of the latest update, `CComponentFieldSelection` now uses `List<DetailEntity>` instead of `Set<DetailEntity>` for its value type. This change ensures proper ordering preservation for entity fields with `@OrderColumn` annotation.

**Usage Pattern:**
1. Call `setSourceItems(allAvailableItems)` to provide complete list of selectable items
2. Binder automatically calls `setValue(entity.getFieldValue())` to set currently selected items
3. Component separates items into selected and available lists
4. Order of selected items is preserved from the entity's list field

**Example with @OrderColumn:**

```java
@Entity
public class CUser extends CEntityNamed<CUser> {
    @OneToMany(fetch = FetchType.LAZY)
    @OrderColumn(name = "item_index")  // Order matters!
    @AMetaData(
        displayName = "Activities", 
        useDualListSelector = true,
        dataProviderBean = "CActivityService",
        dataProviderMethod = "listByUser"
    )
    private List<CActivity> activities;  // List, not Set!
}
```

When CFormBuilder creates the component:
```java
// Automatically created by CFormBuilder
CComponentFieldSelection<CUser, CActivity> activitySelector = 
    new CComponentFieldSelection<>("Available Activities", "Selected Activities");

// All available activities are set
activitySelector.setSourceItems(activityService.listByUser());

// Binder sets the user's current activities in order
// Order is preserved: [Activity3, Activity1, Activity5]
binder.bind(activitySelector, CUser::getActivities, CUser::setActivities);
```

**Manual Usage:**

```java
CComponentFieldSelection<CProject, CUser> teamSelector = 
    new CComponentFieldSelection<>("Available Team Members", "Selected Team Members");

// Set all available users
teamSelector.setSourceItems(userService.list());

// Manually set selected items in specific order
List<CUser> orderedTeam = Arrays.asList(projectLead, developer1, developer2);
teamSelector.setValue(orderedTeam);

// Get value preserves order
List<CUser> selectedTeam = teamSelector.getValue();
// Returns: [projectLead, developer1, developer2]
```

### 3. CFieldSelectionComponent

A specialized version for selecting entity fields used in grid configuration. Wraps CDualListSelectorComponent and provides field-specific functionality.

**Usage:**

```java
CFieldSelectionComponent fieldSelector = 
    new CFieldSelectionComponent("Available Fields", "tech.derbent.activities.domain.CActivity");

// Get selected fields
List<FieldSelection> selections = fieldSelector.getSelectedFields();
```

## Integration with CFormBuilder

The Dual List Selector can be automatically used by CFormBuilder for List or Set fields when marked with the `useDualListSelector` annotation.

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

**For fields with ordering (@OrderColumn):**

```java
@Entity
public class CUser extends CEntityNamed<CUser> {
    
    @OneToMany(fetch = FetchType.LAZY)
    @OrderColumn(name = "item_index")  // JPA maintains order in this column
    @AMetaData(
        displayName = "Activities",
        dataProviderBean = "CActivityService",
        dataProviderMethod = "listByUser",
        useDualListSelector = true  // Use CComponentFieldSelection for ordering support
    )
    private List<CActivity> activities;  // Must be List for @OrderColumn
    
    // getters and setters
}
```

### How It Works

When CFormBuilder processes the entity:

1. It detects the field is a `List` or `Set` type
2. It checks if `useDualListSelector = true` in the `@AMetaData` annotation
3. If true, it creates a `CComponentFieldSelection` instead of `MultiSelectComboBox`
4. The component is automatically bound to the field via the binder
5. For `List` fields with `@OrderColumn`, the component preserves item order

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

### CComponentFieldSelection Tests

Comprehensive unit tests are provided in `CComponentFieldSelectionTest.java` covering:
- Component initialization
- Source items management
- **Order preservation** for List-based values
- Value setting with specific ordering
- Clear operations
- Read-only mode
- Selected items retrieval
- List separation (selected vs. available)
- Null value handling

Run tests with:
```bash
mvn test -Dtest=CComponentFieldSelectionTest
```

Key test: `testSetValuePreservesOrder()` verifies that when items are set in a specific order (e.g., "Item 3", "Item 1", "Item 4"), that exact order is maintained through `getValue()`, which is critical for `@OrderColumn` support.

### CDualListSelectorComponent Tests

Unit tests are also provided in `CDualListSelectorComponentTest.java` for the legacy Set-based component covering:
- Component initialization
- Item management
- Value handling
- Read-only mode
- Selection operations

Run tests with:
```bash
mvn test -Dtest=CDualListSelectorComponentTest
```

## Migration Notes

If you have existing fields using `CComponentFieldSelection` or dual list selectors:

1. **Set-based fields (no @OrderColumn)**: No changes needed. The component will work with Sets by converting to/from Lists internally.

2. **List-based fields with @OrderColumn**: Ensure your entity field is declared as `List<DetailEntity>` not `Set<DetailEntity>`. The component now properly preserves order.

3. **Binding**: The component now implements `HasValue<..., List<DetailEntity>>` instead of `HasValue<..., Set<DetailEntity>>`. Vaadin's binder handles type conversion automatically for compatible field types.

Example migration:
```java
// Before (loses order)
@OrderColumn(name = "item_index")
private Set<CActivity> activities;  // Wrong! Set doesn't preserve order

// After (preserves order)
@OrderColumn(name = "item_index")  
private List<CActivity> activities;  // Correct! List preserves order
```
