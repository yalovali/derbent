# Font Size Settings - Visual UX Guide

## User Interface Location

The font size setting is located in the System Settings page:

**Navigation Path**: Setup → System Settings → UI and Theming Settings section

## Form Field

```
┌─────────────────────────────────────────────────────┐
│ Font Size Scale *                                   │
│ ┌─────────────────────────────────────────────────┐ │
│ │ medium                                       ▼  │ │
│ └─────────────────────────────────────────────────┘ │
│ Font size scale for the application UI              │
│ (small, medium, large)                               │
└─────────────────────────────────────────────────────┘
```

## Dropdown Options

When clicked, the dropdown shows three options:

```
┌─────────────────────────────────────┐
│ small                               │ ← ~25% smaller than default
├─────────────────────────────────────┤
│ ☑ medium                            │ ← Current default
├─────────────────────────────────────┤
│ large                               │ ← ~20% larger than default
└─────────────────────────────────────┘
```

## Visual Comparison

### Small Font Size
```
Perfect for large screens (27"+) or users who want maximum information density

Example Menu Item:    ⌂ Activities
Example Button Text:  Save Settings
Example Body Text:    Configure system-wide settings that apply to all companies...
```

### Medium Font Size (Default)
```
Balanced for most screen sizes (15-24") and standard viewing distances

Example Menu Item:    ⌂ Activities
Example Button Text:  Save Settings
Example Body Text:    Configure system-wide settings that apply to all companies...
```

### Large Font Size
```
Optimal for accessibility, small screens, or users who prefer larger text

Example Menu Item:    ⌂ Activities
Example Button Text:  Save Settings
Example Body Text:    Configure system-wide settings that apply to all companies...
```

## UX Flow Diagram

```
┌──────────────┐
│ User Login   │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────┐
│ MainLayout loads            │
│ - Reads font size from DB   │
│ - Applies CSS variables     │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Application renders with    │
│ selected font size          │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ User navigates to           │
│ System Settings             │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Changes Font Size Scale     │
│ dropdown to "large"         │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Clicks "Save Settings"      │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Backend:                    │
│ - Validates & saves to DB   │
│ - Stores in session         │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Frontend:                   │
│ - Injects new CSS variables │
│ - UI updates instantly      │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ User sees larger fonts      │
│ WITHOUT page reload         │
└─────────────────────────────┘
```

## Real-World Example: Activities Grid

### Before (Medium)
```
┌────────────────────────────────────────────────────────────────┐
│ Activities                                          [+ Add]     │
├────────────┬─────────────┬───────────┬──────────────────────────┤
│ Name       │ Status      │ Priority  │ Assigned To              │
├────────────┼─────────────┼───────────┼──────────────────────────┤
│ Task 1     │ In Progress │ High      │ John Doe                 │
│ Task 2     │ Open        │ Medium    │ Jane Smith               │
│ Task 3     │ Completed   │ Low       │ Bob Johnson              │
└────────────┴─────────────┴───────────┴──────────────────────────┘
```

### After (Large)
```
┌─────────────────────────────────────────────────────────────────┐
│ Activities                                           [+ Add]     │
├─────────────┬──────────────┬────────────┬──────────────────────────┤
│ Name        │ Status       │ Priority   │ Assigned To              │
├─────────────┼──────────────┼────────────┼──────────────────────────┤
│ Task 1      │ In Progress  │ High       │ John Doe                 │
│ Task 2      │ Open         │ Medium     │ Jane Smith               │
│ Task 3      │ Completed    │ Low        │ Bob Johnson              │
└─────────────┴──────────────┴────────────┴──────────────────────────┘
```

## Affected UI Elements

✓ Menu items
✓ Button labels
✓ Form field labels
✓ Grid headers and data
✓ Dialog titles and content
✓ Notification messages
✓ Toolbar text
✓ Breadcrumbs
✓ Status badges
✓ Help text and descriptions

## Accessibility Benefits

1. **Visual Impairment**: Larger fonts reduce eye strain
2. **Aging Users**: Improved readability for presbyopia
3. **Distance Viewing**: Better for presentations or shared screens
4. **High DPI Displays**: Prevents text from appearing too small
5. **User Preference**: Personal comfort and reading speed

## Performance Considerations

- **No Page Reload**: Changes apply instantly
- **Minimal Overhead**: Only CSS variable changes, no DOM manipulation
- **Browser Native**: Uses CSS custom properties supported by all modern browsers
- **Persistent**: Setting saved in database, retrieved once per session
- **Session Cache**: Stored in VaadinSession for quick access

## Browser Compatibility

Works on all browsers supported by Vaadin 24:
- Chrome/Edge 90+
- Firefox 88+
- Safari 15+
- Opera 76+

## Testing Scenarios

1. **Change and Save**: Verify font size changes immediately
2. **Page Navigation**: Ensure font size persists across page changes
3. **Browser Refresh**: Confirm setting loads from database
4. **Multiple Users**: Each user can have different settings (when per-user settings implemented)
5. **Invalid Values**: System falls back to "medium" for invalid/missing values

## Known Limitations

1. **System-Wide Only**: Currently affects all users (per-user settings planned for future)
2. **Three Fixed Sizes**: No custom or fine-tuned sizing (can be extended in future)
3. **Requires Admin Access**: Only available in System Settings (currently)
