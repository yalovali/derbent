# ComboBox Background Color Fix

## Problem
The colorful background in `CColorAwareComboBox` was being applied to the entire component rectangle using the `background-color` CSS property. This made the color extend beyond the input field borders, creating an undesirable visual appearance.

## Solution
Changed the CSS property from `background-color` to `--vaadin-input-field-background` to ensure the background color stays within the input field borders.

## Technical Details

### Before (Incorrect Implementation)
```java
// Line 204: Applied color to entire component
getElement().getStyle().set("background-color", backgroundColor);
```

This caused the background color to fill the entire ComboBox component rectangle, including padding and areas outside the input border.

### After (Correct Implementation)  
```java
// Line 203: Apply color only within input field borders
getElement().getStyle().set("--vaadin-input-field-background", backgroundColor);
```

This uses Vaadin's CSS custom property that specifically targets the input field background, keeping the color within the borders.

### Code Changes
**File:** `src/main/java/tech/derbent/api/ui/component/basic/CColorAwareComboBox.java`

**Changes Made:**
1. Line 185: Removed `getElement().getStyle().remove("background-color");` - no longer needed
2. Line 203: Changed `set("background-color", backgroundColor)` to `set("--vaadin-input-field-background", backgroundColor)`

### Why This Works
Vaadin's ComboBox component uses the `--vaadin-input-field-background` CSS variable to style the input field's background. By using this variable instead of the generic `background-color` property:

1. The color is applied only to the actual input field area
2. The color respects the input field's borders and border-radius
3. The component maintains proper visual hierarchy
4. Consistent with Vaadin's theming system

### Consistency with Existing Code
The `CColorPickerComboBox` class (line 143) already uses this correct approach:
```java
colorField.getElement().getStyle().set("--vaadin-input-field-background", colorValue);
```

This fix brings `CColorAwareComboBox` in line with the existing correct implementation.

## Impact
- **Visual:** Background colors now properly confined within ComboBox input borders
- **User Experience:** Cleaner, more professional appearance
- **Consistency:** Matches the implementation in `CColorPickerComboBox`
- **Breaking Changes:** None - purely visual improvement

## Testing
To test the fix:
1. Navigate to any page with status ComboBoxes (e.g., Activities, Meetings)
2. Select a status with a color (e.g., "In Progress", "Completed")
3. Verify the background color stays within the input field borders
4. Verify text contrast is still readable

## Related Files
- `src/main/java/tech/derbent/api/ui/component/basic/CColorAwareComboBox.java` - Fixed implementation
- `src/main/java/tech/derbent/api/ui/component/basic/CColorPickerComboBox.java` - Reference implementation
