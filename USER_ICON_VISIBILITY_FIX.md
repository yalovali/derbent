# User Icon Visibility Fix

## Problem
User icons (profile pictures and generated avatars) were present in the HTML DOM but not visible to users. The images were being hidden by Vaadin Icon's shadow DOM SVG elements.

## Root Cause
When creating a custom icon with an embedded image using Vaadin's `Icon` component:
1. The `Icon` component has a shadow DOM containing SVG elements
2. When we append an `<img>` element to the icon, it goes into the light DOM
3. The shadow DOM's SVG renders **on top** of the light DOM image, making it invisible

### HTML Structure (Before Fix)
```html
<vaadin-icon style="...">
  <template shadowrootmode="open">
    <svg>
      <g id="svg-group"><!----></g>
      <g id="use-group">
        <use></use>  <!-- Empty but still rendered -->
      </g>
    </svg>
  </template>
  <!-- Light DOM content (not visible because SVG is on top) -->
  <img src="data:image/png;base64,..." alt="User icon">
</vaadin-icon>
```

## Solution
Vaadin's Icon component has built-in CSS rules that hide the shadow DOM SVG when certain attributes are present:

```css
:host(:is([icon-class], [font-icon-content])) svg {
    display: none;
}
```

By setting the `icon-class` attribute on the Icon element, we trigger this CSS rule and hide the SVG, allowing our custom image to be visible.

### HTML Structure (After Fix)
```html
<vaadin-icon icon-class="user-icon-image" style="...">
  <template shadowrootmode="open">
    <svg style="display: none;">  <!-- Hidden by CSS rule -->
      <g id="svg-group"><!----></g>
      <g id="use-group">
        <use></use>
      </g>
    </svg>
  </template>
  <!-- Light DOM content (now visible) -->
  <img src="data:image/png;base64,..." alt="User icon">
</vaadin-icon>
```

## Implementation
The fix was implemented in `CUser.createIconFromImageData()`:

```java
private Icon createIconFromImageData(final byte[] imageData) {
    // ... validation ...
    
    final Icon icon = new Icon();
    
    // CRITICAL: Set icon-class attribute to hide the shadow DOM's default SVG
    // This allows our custom image to be visible instead of being hidden behind the SVG
    icon.getElement().setAttribute("icon-class", "user-icon-image");
    
    // ... create and append img element ...
    
    return CColorUtils.styleIcon(icon);
}
```

## Testing
To test this fix, verify that user icons are visible in:
1. `CLabelEntity` components displaying users
2. Grid columns showing user data
3. User selection dropdowns
4. Any other UI component that displays user entities

### Test with CLabelEntity
```java
// Create a user label and verify the icon is visible
final CUser user = userService.findById(1L);
final CLabelEntity label = CLabelEntity.createUserLabel(user);
// Icon should now be visible in the browser
```

## Impact
This fix affects all user icons throughout the application:
- User listings in grids
- User display in labels and badges
- User selection components
- Activity/task assignee displays
- Any other location where user entities are rendered with icons

## Alternative Approaches Considered
1. **Using Div instead of Icon**: Would break the `IHasIcon` interface contract
2. **CSS z-index manipulation**: Not reliable across browsers and contexts
3. **Removing shadow DOM programmatically**: Not supported by Vaadin/browser APIs
4. **Using font-icon-content attribute**: Works but semantically less clear than icon-class

The `icon-class` attribute approach is the cleanest solution that leverages Vaadin's built-in functionality.

## Related Files
- `src/main/java/tech/derbent/base/users/domain/CUser.java` - Contains the fix
- `src/main/java/tech/derbent/api/grid/view/CLabelEntity.java` - Uses user icons
- `src/main/java/tech/derbent/api/interfaces/IHasIcon.java` - Icon interface contract
- `src/main/java/tech/derbent/api/utils/CColorUtils.java` - Icon styling utilities
