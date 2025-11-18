# Component Callback Pattern

## Overview

The Component Callback Pattern provides a declarative way to bind UI component events (change, click, focus, blur) to callback methods in your CPageService classes. This eliminates boilerplate event listener code and provides direct access to component values.

## Pattern Format

Define methods in your CPageService subclass following this naming convention:

```
on_[fieldName]_[action]
```

Where:
- `fieldName` is the name of the form field/component
- `action` is the event type: `change`, `changed`, `click`, `focus`, or `blur`

## Supported Method Signatures

### Old Signature (Backward Compatible)
```java
protected void on_fieldName_action() {
    // No parameters - original format
}
```

### New Signature (Recommended)
```java
// With component only
protected void on_fieldName_action(Component component) {
    // Access to the component
}

// With component and value (recommended)
protected void on_fieldName_action(Component component, Object value) {
    // Access to both component and its new value
}
```

## Supported Events

### change / changed
Triggered when the value of a field changes (TextField, TextArea, ComboBox, etc.)

### click
Triggered when a Button is clicked

### focus
Triggered when a field receives focus

### blur
Triggered when a field loses focus

## Component Accessor Methods

The following helper methods are available in CPageService to access components:

### Generic Access
```java
// Get any component by name
Component component = getComponentByName("fieldName");

// Get typed component
TextField field = getComponent("name", TextField.class);
```

### Type-Specific Access
```java
TextField textField = getTextField("name");
TextArea textArea = getTextArea("description");
ComboBox<?> comboBox = getComboBox("status");
Checkbox checkbox = getCheckbox("active");
DatePicker datePicker = getDatePicker("dueDate");
```

### Value Access
```java
// Get current value
Object value = getComponentValue("fieldName");

// Set value programmatically
setComponentValue("fieldName", newValue);
```

## Complete Example

Here's a comprehensive example from `CPageServiceActivity`:

```java
public class CPageServiceActivity extends CPageServiceWithWorkflow<CActivity> {
    
    @Override
    public void bind() {
        super.bind();
        detailsBuilder = view.getDetailsBuilder();
        if (detailsBuilder != null) {
            formBuilder = detailsBuilder.getFormBuilder();
        }
        // This call activates all on_* methods
        bindMethods(this);
    }

    // Example 1: Old signature (backward compatible)
    protected void on_name_changed() {
        LOGGER.debug("Activity name changed to: {}", getCurrentEntity().getName());
    }

    // Example 2: New signature with component and value
    protected void on_name_change(Component component, Object value) {
        LOGGER.info("=== Name field changed ===");
        LOGGER.info("Component type: {}", component.getClass().getSimpleName());
        LOGGER.info("New value: {}", value);
        
        // Use helper methods to access the component
        TextField nameField = getTextField("name");
        if (nameField != null) {
            LOGGER.info("Current value: {}", nameField.getValue());
        }
    }

    // Example 3: Monitor status changes before save
    protected void on_status_change(Component component, Object value) {
        LOGGER.info("=== Status changed (BEFORE save) ===");
        LOGGER.info("New status value: {}", value);
        
        // Access the combobox
        ComboBox<?> statusCombo = getComboBox("status");
        if (statusCombo != null) {
            LOGGER.info("Status has {} items", statusCombo.getListDataView().getItemCount());
        }
    }

    // Example 4: Focus event
    protected void on_description_focus(Component component) {
        LOGGER.info("Description field focused");
        // Could show help text, load suggestions, etc.
    }

    // Example 5: Blur event for validation
    protected void on_description_blur(Component component) {
        LOGGER.info("Description field lost focus");
        Object value = getComponentValue("description");
        if (value != null && !value.toString().trim().isEmpty()) {
            LOGGER.info("Description has content: {}", value);
        }
    }
}
```

## Use Cases

### Real-time Validation
```java
protected void on_email_blur(Component component, Object value) {
    String email = (String) value;
    if (!isValidEmail(email)) {
        CNotificationService.showWarning("Invalid email format");
    }
}
```

### Auto-save on Focus Loss
```java
protected void on_notes_blur(Component component, Object value) {
    try {
        actionSave();
        LOGGER.info("Auto-saved notes");
    } catch (Exception e) {
        LOGGER.error("Auto-save failed", e);
    }
}
```

### Dependent Field Updates
```java
protected void on_country_change(Component component, Object value) {
    // When country changes, update the list of cities
    setComponentValue("city", null);
    ComboBox<City> cityCombo = getComboBox("city");
    if (cityCombo != null) {
        cityCombo.setItems(getCitiesForCountry(value));
    }
}
```

### Monitoring User Actions
```java
protected void on_price_change(Component component, Object value) {
    Double price = (Double) value;
    LOGGER.info("User changed price to: {}", price);
    
    // Update calculated fields
    updateTotalPrice();
}
```

## Implementation Details

### Binding Process
1. When `bindMethods(this)` is called in the `bind()` method
2. The framework scans all methods in your class for the `on_*` pattern
3. For each matching method, it:
   - Extracts the field name and action type
   - Finds the corresponding component in the FormBuilder
   - Attaches the appropriate event listener
   - Invokes your method with the correct parameters when the event occurs

### Method Signature Detection
The framework automatically detects which signature your method uses:
- 0 parameters: Old format, method is called with no arguments
- 1 parameter: Component-only format, component is passed
- 2+ parameters: Full format, component and value are passed

### Thread Safety
All callbacks are executed on the Vaadin UI thread, so they can safely modify UI components.

## Best Practices

1. **Use descriptive method names**: The field name should match exactly as defined in your entity
2. **Handle null values**: Always check if components or values are null
3. **Log important events**: Use appropriate log levels (DEBUG for development, INFO for important events)
4. **Avoid heavy processing**: Callbacks should be fast; delegate heavy work to background threads
5. **Use type-specific accessors**: Use `getTextField()` instead of generic `getComponentByName()` when possible
6. **Validate user input**: Use blur events for validation before save

## Troubleshooting

### Callback Not Firing
- Verify the method name exactly matches the pattern: `on_[fieldName]_[action]`
- Ensure `bindMethods(this)` is called in your `bind()` method
- Check that the field name matches the entity field name (case-sensitive)
- Verify the component exists in the form (check FormBuilder logs)

### Type Cast Exceptions
- Use appropriate type-specific getters: `getTextField()`, `getComboBox()`, etc.
- Check component type before casting values
- Handle null returns gracefully

### Values Not Updated
- For change events, the value parameter contains the NEW value
- The component may not be updated yet; use the value parameter instead of reading from component
- For blur/focus events, use `getComponentValue()` to read the current value
