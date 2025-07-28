# Cancel Button Implementation in Derbent Views

## Overview
This document describes the implementation of cancel buttons across all views in the Derbent application, ensuring users can reject changes consistently as required by the coding guidelines.

## Implementation Pattern

### Views Extending CAbstractMDPage
Most views extend `CAbstractMDPage` which provides built-in cancel button functionality through:
- `createCancelButton(String buttonText)` - Creates a cancel button that clears form and refreshes grid
- `createDetailsTabButtonLayout()` - Includes New, Save, **Cancel**, and Delete buttons by default

Example views:
- `CUsersView`
- `CActivitiesView` 
- `CCompanyView`
- `CCompanySettingsView`
- All other standard entity management views

### Custom Views Extending CAbstractPage
Views that extend `CAbstractPage` and implement custom button layouts must manually include cancel buttons:

#### CSystemSettingsView
- **Issue**: Originally missing cancel button
- **Fix**: Added cancel button to `createButtonLayout()` method
- **Functionality**: `cancelChanges()` method reloads fresh settings from service to reject unsaved changes

### Dialog Implementation
All dialogs extend base classes that include cancel functionality:
- `CDBEditDialog` - Has "Save" and **"Cancel"** buttons in `setupButtons()`
- `CConfirmationDialog` - Has "Yes" and **"No"** buttons (No acts as cancel)
- `CBaseInfoDialog` - Has **"OK"** button for information-only dialogs

## Cancel Button Behavior

### Standard Cancel Behavior (CAbstractMDPage)
```java
protected CButton createCancelButton(final String buttonText) {
    final CButton cancel = CButton.createTertiary(buttonText, e -> {
        clearForm();
        refreshGrid();
    });
    return cancel;
}
```

### Custom Cancel Behavior (CSystemSettingsView)
```java
private void cancelChanges() {
    // Reload fresh settings from database to reject unsaved changes
    final var freshSettings = systemSettingsService.getOrCreateSystemSettings();
    currentSettings = freshSettings;
    binder.readBean(currentSettings);
    // Show confirmation
    Notification.show("Changes cancelled - form reverted to saved state");
}
```

## Testing
- `CancelButtonConsistencyTest` - Verifies base class cancel button infrastructure
- `CSystemSettingsViewCancelButtonTest` - Verifies custom cancel button implementation

## Guidelines for New Views
1. **Extend CAbstractMDPage when possible** - Gets cancel button functionality for free
2. **If extending CAbstractPage** - Must manually implement cancel button in button layout
3. **Dialogs** - Extend CDBEditDialog or appropriate base class for automatic cancel support
4. **Cancel functionality should**:
   - Clear unsaved changes
   - Revert form to last saved state
   - Show user feedback (notification)
   - Not perform any destructive operations