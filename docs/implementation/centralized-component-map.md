# Centralized Component Map Architecture

## Overview

This document describes the centralized component map architecture implemented to enable CPageService classes to access all UI components across different panels and sections, regardless of where they are created in the form hierarchy.

## Problem Statement

Previously, components created in different panels (CPanelDetails) were stored in separate component maps local to each panel. This made it difficult for CPageService classes to:

1. Access components from different sections/panels
2. Bind event handlers to components in any panel
3. Programmatically interact with form fields across the entire form

## Solution

### Architecture Changes

#### 1. CDetailsBuilder - Central Component Registry

**File**: `src/main/java/tech/derbent/api/views/CDetailsBuilder.java`

Added a centralized `Map<String, Component> componentMap` field that stores ALL components from ALL panels:

```java
// Centralized component map - stores all components from all panels
private final Map<String, Component> componentMap;
```

This map is:
- Initialized in the constructor: `componentMap = new HashMap<>();`
- Passed to CFormBuilder during form building
- Passed to CPanelDetails when processing detail lines
- Accessible via getter: `getComponentMap()`

#### 2. CFormBuilder - External Component Map Support

**File**: `src/main/java/tech/derbent/api/annotations/CFormBuilder.java`

Added new constructor accepting an external component map:

```java
public CFormBuilder(final IContentOwner contentOwner, 
                    final Class<?> entityClass, 
                    final CEnhancedBinder<EntityClass> binder,
                    final Map<String, Component> externalComponentMap) throws Exception {
    Objects.requireNonNull(externalComponentMap, "External component map cannot be null");
    componentMap = externalComponentMap;  // Use external map instead of creating new one
    // ... rest of initialization
}
```

Key changes:
- When an external componentMap is provided, it's used instead of creating a new internal map
- The `addFieldLine()` method now properly uses the provided componentMap parameter
- All components are added to the centralized map as they are created

#### 3. CPanelDetails - Centralized Map Integration

**File**: `src/main/java/tech/derbent/api/utils/CPanelDetails.java`

Updated `processLine()` method signature to accept and use the centralized componentMap:

```java
public void processLine(final IContentOwner contentOwner, 
                       final int counter, 
                       final CDetailSection screen, 
                       final CDetailLines line,
                       final CFormBuilder<?> formBuilder, 
                       final Map<String, Component> centralComponentMap) throws Exception {
    // Pass centralized map to formBuilder instead of local map
    formBuilder.addFieldLine(contentOwner, screen.getEntityType(), line, 
                            getBaseLayout(), centralComponentMap, horizontalLayoutMap);
}
```

#### 4. CPageService - Unified Component Access

**File**: `src/main/java/tech/derbent/api/services/pageservice/CPageService.java`

Enhanced to prioritize the centralized component map:

**bindMethods()** - Now uses detailsBuilder's centralized map:
```java
protected void bindMethods(final CPageService<?> page) {
    // Combine form components and custom components
    final Map<String, Component> allComponents = new HashMap<>();
    
    // Get components from detailsBuilder's centralized map if available
    if (detailsBuilder != null && detailsBuilder.getComponentMap() != null) {
        allComponents.putAll(detailsBuilder.getComponentMap());
        LOGGER.debug("Added {} components from detailsBuilder's centralized map", 
                    detailsBuilder.getComponentMap().size());
    }
    
    // Also include formBuilder components for backward compatibility
    if (formBuilder != null) {
        allComponents.putAll(formBuilder.getComponentMap());
    }
    
    // Add custom registered components (these take precedence)
    allComponents.putAll(customComponents);
    // ... rest of method binding logic
}
```

**getComponentByName()** - Checks centralized map first:
```java
protected Component getComponentByName(final String fieldName) {
    // First check detailsBuilder's centralized map (most comprehensive)
    if (detailsBuilder != null && detailsBuilder.getComponentMap() != null) {
        final Component component = detailsBuilder.getComponentMap().get(fieldName);
        if (component != null) {
            return component;
        }
    }
    
    // Fall back to formBuilder for backward compatibility
    if (formBuilder == null) {
        LOGGER.warn("FormBuilder is null; cannot retrieve component '{}'", fieldName);
        return null;
    }
    return formBuilder.getComponentMap().get(fieldName);
}
```

## Benefits

### 1. Unified Component Access
CPageService classes can now access ANY component by name, regardless of which panel it was created in:

```java
// In CPageService subclass
protected void on_myField_change(Component component, Object value) {
    // Works even if myField is in a different panel/section
    TextField anotherField = getTextField("anotherFieldInDifferentPanel");
    anotherField.setValue("Updated!");
}
```

### 2. Simplified Event Binding
The `on_{componentName}_{action}` pattern now works across all components:

```java
// Bind to component in any panel
protected void on_statusComboBox_change(Component component, Object value) {
    // Handle status change from any section
}
```

### 3. Cross-Panel Interactions
Components can easily interact with each other across panel boundaries:

```java
protected void on_projectField_change(Component component, Object value) {
    // Update components in other panels based on project selection
    ComboBox<CUser> userField = getComboBox("assignedUser");
    userField.setItems(getUsersForProject((CProject) value));
}
```

### 4. Backward Compatibility
Existing code continues to work:
- FormBuilder still maintains its own componentMap internally
- Custom registered components via `registerComponent()` still work
- Component lookup falls back to formBuilder if not found in centralized map

## Usage Example

### In a CPageService Implementation

```java
public class CPageServiceActivity extends CPageServiceDynamicPage<CActivity> {

    @Override
    public void bind() {
        super.bind();
        // After bind(), all components from all panels are accessible
    }
    
    // Handle component from Section 1
    protected void on_name_change(Component component, Object value) {
        LOGGER.debug("Name changed: {}", value);
        validateForm();
    }
    
    // Handle component from Section 2 (different panel)
    protected void on_status_change(Component component, Object value) {
        LOGGER.debug("Status changed: {}", value);
        // Access component from Section 1
        TextField nameField = getTextField("name");
        nameField.setEnabled(!"COMPLETED".equals(value));
    }
    
    // Handle component from Section 3 (yet another panel)
    protected void on_assignedUser_change(Component component, Object value) {
        // Access and update components from any section
        ComboBox<CProject> projectField = getComboBox("project");
        // ... logic
    }
}
```

## Data Flow

```
CDetailsBuilder (owns centralized componentMap)
    ↓
    ├─ Creates CFormBuilder(with external componentMap)
    │     ↓
    │     └─ Adds components to centralized map during form building
    │
    └─ Processes sections/panels
          ↓
          └─ CPanelDetails.processLine(with centralComponentMap parameter)
                ↓
                └─ CFormBuilder.addFieldLine(uses centralComponentMap)
                      ↓
                      └─ Components added to centralized map

CPageService
    ↓
    ├─ bindMethods() → Gets all components from detailsBuilder.getComponentMap()
    │
    └─ getComponentByName() → Looks up component in centralized map
```

## Testing Recommendations

### Manual Testing
1. Navigate to any dynamic page with multiple sections/panels
2. Verify components from all panels are accessible via CPageService methods
3. Test event binding across different sections
4. Verify custom registered components still work

### Automated Testing
Create tests that:
1. Build a form with multiple sections
2. Verify all components are in the centralized map
3. Test component retrieval across panel boundaries
4. Verify event binding works for components in different panels

## Migration Guide

### For Existing CPageService Classes

No changes required! The implementation is backward compatible.

### For New Features

To access components across panels:

```java
// Old way (still works, but limited to same panel)
TextField field = getTextField("fieldName");

// New way (works across all panels - automatic)
TextField field = getTextField("fieldNameInAnyPanel");
```

### Debugging

Enable debug logging to see component registration:

```java
// In application.properties
logging.level.tech.derbent.api.services.pageservice.CPageService=DEBUG
logging.level.tech.derbent.api.views.CDetailsBuilder=DEBUG
```

Look for log messages:
- "Added N components from detailsBuilder's centralized map"
- "Adding component for field 'X' to component map"

## Performance Considerations

- **Memory**: Single centralized map vs multiple panel-local maps - negligible difference
- **Lookup Speed**: HashMap lookup is O(1) - no performance impact
- **Initialization**: One-time cost during form building - no noticeable change

## Future Enhancements

Potential improvements for future iterations:

1. **Component Registry Service**: Extract component map to a dedicated service
2. **Type-Safe Access**: Add generic type parameters to component access methods
3. **Component Lifecycle Events**: Notify when components are registered/unregistered
4. **Validation Integration**: Automatic validation setup for components in centralized map

## Related Files

- `CDetailsBuilder.java` - Owns the centralized component map
- `CFormBuilder.java` - Uses external component map when provided
- `CPanelDetails.java` - Passes centralized map to form builder
- `CPageService.java` - Accesses components via centralized map
- `CPageGenericEntity.java` - Fixed compilation error (unrelated but included in same commit)

## Version History

- **2025-12-08**: Initial implementation of centralized component map architecture
