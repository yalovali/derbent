# CDialogEntitySelection Refactoring - Component Extraction

## Overview
Extracted reusable `CComponentEntitySelection` component from `CDialogEntitySelection` dialog to enable entity selection functionality to be used in any widget, not just dialogs.

## Changes Made

### 1. New Component: `CComponentEntitySelection`
**Location:** `src/main/java/tech/derbent/api/ui/component/enhanced/CComponentEntitySelection.java`

**Key Features:**
- Extends `Composite<CVerticalLayout>` following existing component patterns
- Generic and reusable - can be embedded in dialogs, pages, panels, or widgets
- Contains all UI elements:
  - Entity type selection dropdown
  - Grid with multi/single selection
  - Search/filter toolbar (ID, Name, Description, Status)
  - Selection indicator with count
  - Reset button
- Contains all business logic:
  - Filtering logic
  - Selection state management
  - Reflection-based entity property access (with caching)
  - Already-selected items handling (hide or show modes)
- Communicates with parent via `Consumer<Set<EntityClass>>` callback

**Usage Example:**
```java
// Create entity types configuration
List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes = Arrays.asList(
    new CComponentEntitySelection.EntityTypeConfig<>("Activities", CActivity.class, activityService),
    new CComponentEntitySelection.EntityTypeConfig<>("Meetings", CMeeting.class, meetingService)
);

// Create items provider
CComponentEntitySelection.ItemsProvider<CProjectItem<?>> itemsProvider = config -> {
    if (config.getEntityClass() == CActivity.class) {
        return activityService.findAll();
    } else if (config.getEntityClass() == CMeeting.class) {
        return meetingService.findAll();
    }
    return new ArrayList<>();
};

// Create selection callback
Consumer<Set<CProjectItem<?>>> onSelectionChanged = selectedItems -> {
    System.out.println("Selection changed: " + selectedItems.size() + " items selected");
    // Handle selection changes
};

// Create component
CComponentEntitySelection<CProjectItem<?>> component = new CComponentEntitySelection<>(
    entityTypes,
    itemsProvider,
    onSelectionChanged,
    true  // multi-select mode
);

// Add to your layout
layout.add(component);
```

### 2. Refactored Dialog: `CDialogEntitySelection`
**Location:** `src/main/java/tech/derbent/api/ui/dialogs/CDialogEntitySelection.java`

**Changes:**
- Now wraps `CComponentEntitySelection` instead of containing all logic
- Maintains full backward compatibility with wrapper types
- Dialog-specific functionality remains (buttons, title, modal behavior)
- All entity selection logic delegated to component

**Backward Compatibility:**
The dialog maintains its public API by re-exporting types with wrapper classes:

1. **`AlreadySelectedMode` enum**: 
   - Re-exported in dialog with conversion methods
   - `toComponentMode()` and `fromComponentMode()` for type conversion

2. **`EntityTypeConfig<E>` class**:
   - Wraps component's `EntityTypeConfig`
   - Provides same public API
   - `toComponentConfig()` for internal conversion

3. **`ItemsProvider<T>` interface**:
   - Re-exported in dialog
   - `toComponentProvider()` for conversion to component's interface

**Type Conversion Flow:**
```
Dialog Type -> Component Type (in setupContent)
  ↓
CDialogEntitySelection.EntityTypeConfig -> CComponentEntitySelection.EntityTypeConfig
CDialogEntitySelection.ItemsProvider -> CComponentEntitySelection.ItemsProvider  
CDialogEntitySelection.AlreadySelectedMode -> CComponentEntitySelection.AlreadySelectedMode
```

### 3. No Changes Required for Existing Code
All existing usages continue to work without modification:
- `CComponentListSprintItems` - uses dialog types, works as before
- `CComponentListEntityBase` - uses dialog types, works as before
- `IEntitySelectionDialogSupport` - references dialog types, works as before

## Benefits

1. **Reusability**: Component can now be used in any context:
   - In dialogs (as before)
   - In page sections
   - In split panels
   - In tabs
   - In any custom widget

2. **Separation of Concerns**:
   - Dialog handles modal behavior, buttons, title
   - Component handles entity selection logic
   - Clear responsibility boundaries

3. **Maintainability**:
   - Single source of truth for entity selection logic
   - Easier to test component independently
   - Easier to enhance or fix bugs in one place

4. **Backward Compatibility**:
   - Existing code works without changes
   - No breaking changes to public APIs
   - Smooth migration path

## Testing Notes

- ✅ Code compiles successfully
- ✅ Test code compiles successfully
- ✅ No changes needed in existing usages
- ⚠️  Manual testing recommended:
  - Test entity selection dialog in sprint items
  - Test filters and selection persistence
  - Test already-selected items modes
  - Test multi-select and single-select modes

## Future Enhancements

Now that the component is extracted, potential enhancements include:

1. **Direct Component Usage**: Use component directly in pages that don't need modal dialogs
2. **Embedded Selection**: Add entity selection capability to master-detail views
3. **Custom Layouts**: Create custom layouts with entity selection embedded
4. **Additional Callbacks**: Add more events (filter changed, entity type changed, etc.)
5. **Configuration Methods**: Add fluent builder pattern for easier configuration

## Example: Using Component Without Dialog

```java
// In a page or panel class
public class MyEntityManagementPage extends VerticalLayout {
    
    private CComponentEntitySelection<CActivity> selectionComponent;
    
    public MyEntityManagementPage(CActivityService activityService) {
        // Setup entity types
        List<CComponentEntitySelection.EntityTypeConfig<?>> types = List.of(
            new CComponentEntitySelection.EntityTypeConfig<>("Activities", CActivity.class, activityService)
        );
        
        // Create component directly in page
        selectionComponent = new CComponentEntitySelection<>(
            types,
            config -> activityService.findAll(),
            this::onSelectionChanged,
            true
        );
        
        // Add to page layout
        add(new H2("Select Activities"));
        add(selectionComponent);
        selectionComponent.setHeight("600px");
    }
    
    private void onSelectionChanged(Set<CActivity> selectedItems) {
        // Handle selection in page
        System.out.println("Selected: " + selectedItems);
        // Update related components, etc.
    }
}
```

## Migration Guide (For Future Direct Component Usage)

If you want to use the component directly instead of through the dialog:

**Before (using dialog):**
```java
CDialogEntitySelection<CActivity> dialog = new CDialogEntitySelection<>(
    "Select Activities",
    entityTypes,
    itemsProvider,
    this::handleSelection,
    true
);
dialog.open();
```

**After (using component directly):**
```java
CComponentEntitySelection<CActivity> component = new CComponentEntitySelection<>(
    entityTypes,
    itemsProvider,
    selectedItems -> handleSelection(new ArrayList<>(selectedItems)),
    true
);
layout.add(component);
```

Note: When using component directly, the selection callback receives `Set<EntityClass>` instead of `List<EntityClass>` as in the dialog.
