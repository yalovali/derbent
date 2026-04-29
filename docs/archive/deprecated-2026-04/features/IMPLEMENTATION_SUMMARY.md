# Component Callback Pattern - Implementation Summary

## Overview
Successfully implemented an enhanced component callback pattern in the Derbent project that allows developers to declaratively bind UI component events to callback methods with direct access to component instances and values.

## Problem Statement
The original request was to:
1. Check the existing `on_name_changed` pattern in `CPageServiceActivity`
2. Extend it to support accessing component values and instances
3. Add helper methods like `getComponentByName()`, `getEditBox()`, `getCombobox()`
4. Enable real-time monitoring of component values (e.g., print on every edit, status changes before save)
5. Make the pattern easy and generic to use

## Solution Implemented

### 1. Enhanced Callback Binding (`CPageService.java`)

#### Before
- Only supported methods with no parameters: `on_fieldName_action()`
- No access to component or value from callback
- Limited to `click` and `change` events

#### After
- Supports three method signatures:
  ```java
  on_fieldName_action()                          // Old format (backward compatible)
  on_fieldName_action(Component component)       // With component access
  on_fieldName_action(Component component, Object value)  // With component and value
  ```
- Automatically detects signature and calls appropriately
- Supports multiple event types: `change`, `changed`, `click`, `focus`, `blur`

### 2. Component Accessor Methods

Added comprehensive helper methods to `CPageService`:

```java
// Generic access
Component getComponentByName(String fieldName)
<T extends Component> T getComponent(String fieldName, Class<T> componentType)

// Type-specific accessors
TextField getTextField(String fieldName)
TextArea getTextArea(String fieldName)
ComboBox<T> getComboBox(String fieldName)  // Handles CNavigableComboBox unwrapping
Checkbox getCheckbox(String fieldName)
DatePicker getDatePicker(String fieldName)

// Value accessors
Object getComponentValue(String fieldName)
void setComponentValue(String fieldName, Object value)
```

### 3. Visibility Fix
Changed `getCurrentEntity()` from `private` to `protected` to allow subclasses to access it.

### 4. Comprehensive Examples (`CPageServiceActivity.java`)

Created 5 complete examples demonstrating different patterns:

1. **on_name_changed()** - Old format (backward compatibility)
2. **on_name_change(Component, Object)** - New format with value access
3. **on_status_change(Component, Object)** - Real-time status monitoring before save
4. **on_description_focus(Component)** - Focus event for UX enhancements
5. **on_description_blur(Component)** - Blur event for validation

## Files Modified

1. **src/main/java/tech/derbent/api/services/pageservice/CPageService.java**
   - Enhanced `bindComponent()` method with signature detection
   - Added component accessor methods (8 new methods)
   - Changed `getCurrentEntity()` visibility
   - Added support for focus/blur events

2. **src/main/java/tech/derbent/app/activities/service/CPageServiceActivity.java**
   - Fixed compilation error
   - Added comprehensive callback examples
   - Demonstrated all new features

3. **docs/features/component-callback-pattern.md** (NEW)
   - Complete documentation with examples
   - Usage patterns and best practices
   - Troubleshooting guide

## Key Features

### Backward Compatibility
All existing code using the old `on_fieldName_action()` signature continues to work without modifications.

### Type Safety
Type-specific accessor methods provide compile-time type checking and eliminate the need for casting.

### Smart Component Handling
The `getComboBox()` method intelligently handles both direct `ComboBox` instances and `CNavigableComboBox` (which wraps a ComboBox), automatically unwrapping as needed.

### Event Support
Supports common UI events:
- **change/changed**: Value changes (TextField, ComboBox, etc.)
- **click**: Button clicks
- **focus**: Component gains focus
- **blur**: Component loses focus

### Error Handling
All methods include proper null checking and logging to help developers debug issues.

## Usage Example

```java
public class CPageServiceProduct extends CPageServiceDynamicPage<CProduct> {
    
    @Override
    public void bind() {
        super.bind();
        detailsBuilder = view.getDetailsBuilder();
        if (detailsBuilder != null) {
            formBuilder = detailsBuilder.getFormBuilder();
        }
        bindMethods(this);  // Activates all on_* callback methods
    }

    // Real-time price monitoring
    protected void on_price_change(Component component, Object value) {
        LOGGER.info("Price changed to: {}", value);
        Double price = (Double) value;
        
        // Update dependent fields
        Double tax = price * 0.08;
        setComponentValue("tax", tax);
        setComponentValue("total", price + tax);
    }

    // Validation on blur
    protected void on_email_blur(Component component, Object value) {
        String email = (String) value;
        if (!email.contains("@")) {
            CNotificationService.showWarning("Invalid email format");
        }
    }

    // Auto-save feature
    protected void on_notes_blur(Component component) {
        try {
            actionSave();
            LOGGER.info("Auto-saved notes");
        } catch (Exception e) {
            LOGGER.error("Auto-save failed", e);
        }
    }
}
```

## Testing

### Compilation
✅ Successfully compiled with `mvn clean compile`
- No compilation errors
- All type constraints satisfied
- Backward compatibility maintained

### Code Quality
✅ Follows project coding standards
- Proper JavaDoc documentation
- Consistent naming conventions
- Error handling and logging
- Type safety

## Benefits

1. **Declarative Syntax**: Define callbacks by method name, no manual event listener setup
2. **Direct Value Access**: Get component values directly in callbacks without querying entity
3. **Real-time Monitoring**: Monitor changes before they're saved to the entity
4. **Easy Component Access**: Type-safe helper methods for all common component types
5. **Flexible Signatures**: Support both old and new patterns for maximum flexibility
6. **Minimal Boilerplate**: Eliminate repetitive event listener code

## Future Enhancements

Possible extensions (not implemented):
- Support for more event types (keypress, mouseenter, etc.)
- Pattern matching for field groups (e.g., `on_price*_change` for price, priceUnit, etc.)
- Custom event data passing beyond component and value
- Integration with validation framework
- Debouncing support for rapid-fire events

## Related Documentation

- [Component Callback Pattern Guide](./component-callback-pattern.md) - Complete usage guide
- [CPageService API](../../src/main/java/tech/derbent/api/services/pageservice/CPageService.java) - Source code with JavaDoc
- [CPageServiceActivity Example](../../src/main/java/tech/derbent/app/activities/service/CPageServiceActivity.java) - Working examples

## Conclusion

The implementation successfully addresses all requirements from the problem statement:
- ✅ Pattern extended with component and value access
- ✅ Helper methods for component retrieval implemented
- ✅ Real-time value monitoring enabled
- ✅ Easy and generic pattern achieved
- ✅ Comprehensive examples provided
- ✅ Full documentation created
- ✅ Backward compatibility maintained

The pattern is now production-ready and can be used across all PageService implementations in the Derbent project.
