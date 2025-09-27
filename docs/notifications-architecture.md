# Derbent Notification System Architecture

## Overview
The Derbent project now features a unified notification and messaging system that consolidates all user feedback mechanisms into a consistent, stylish, and maintainable solution. This system replaces scattered notification calls throughout the codebase with a centralized service-based approach.

## Components

### 1. CNotificationService (`tech.derbent.api.ui.notifications.CNotificationService`)
**Primary service class for all notifications and user messages.**

- **Layer**: Service (MVC)
- **Scope**: Spring Service (`@Service`)
- **Usage**: Inject into views and components via dependency injection

#### Key Features:
- **Toast Notifications**: Temporary overlay messages with consistent styling
- **Modal Dialogs**: Information, warning, error, and confirmation dialogs
- **Consistent Styling**: Uses Lumo theme variants for visual consistency
- **Smart Positioning**: Context-appropriate positioning for different message types
- **Appropriate Durations**: Varied display times based on message importance

#### Methods:
```java
// Toast notifications
void showSuccess(String message)    // Green, bottom-start, 2s
void showError(String message)      // Red, middle, 8s
void showWarning(String message)    // Orange, top-center, 5s
void showInfo(String message)       // Blue, bottom-start, 5s

// Modal dialogs
void showInfoDialog(String message)
void showWarningDialog(String message)
void showErrorDialog(Exception exception)
void showErrorDialog(String message)
void showConfirmationDialog(String message, Runnable onConfirm)

// Convenience methods
void showSaveSuccess()
void showDeleteSuccess()
void showCreateSuccess()
void showSaveError()
void showDeleteError()
void showCreateError()
void showOptimisticLockingError()
void showValidationWarning(String fieldName)
```

### 2. CNotifications (`tech.derbent.api.ui.notifications.CNotifications`)
**Static utility class for backwards compatibility.**

- **Layer**: Utility (MVC)
- **Usage**: Static methods when dependency injection is not available
- **Note**: Simplified implementation that logs messages (can be enhanced in future)

#### Example Usage:
```java
// Static access (backwards compatibility)
CNotifications.showSuccess("Operation completed");
CNotifications.showError("Something went wrong");
CNotifications.showSaveSuccess();
```

## Integration with Existing Classes

### CAbstractEntityDBPage
- Added `CNotificationService notificationService` field with optional injection
- Enhanced `showNotification()` and `showErrorNotification()` methods
- Smart fallback to direct Vaadin calls when service not available
- Updated specific calls to use convenience methods

### CCrudToolbar
- Added `CNotificationService notificationService` field with optional injection
- Enhanced error/success notification methods with service integration
- Updated create/delete error handling to use convenience methods

### CSystemSettingsView
- Added `@Autowired(required = false) CNotificationService notificationService`
- Added helper methods for consistent notification display
- Updated key notification calls to use the service

## Design Principles

### 1. Consistency
- **Visual Styling**: All notifications use Lumo theme variants
- **Positioning**: Context-appropriate placement (success at bottom, errors in center)
- **Durations**: Standardized timing (short: 2s, medium: 5s, long: 8s)

### 2. Backwards Compatibility
- Existing code continues to work without modification
- Gradual migration path through fallback mechanisms
- Optional dependency injection prevents breaking changes

### 3. Maintainability
- Centralized notification logic
- Consistent API across the application
- Easy to modify styling and behavior globally

### 4. Flexibility
- Service-based approach for dependency injection
- Static utility for cases without DI
- Custom notification options available

## Migration Guide

### For New Code
```java
@Component
public class MyView extends CAbstractPage {
    
    @Autowired
    private CNotificationService notificationService;
    
    private void handleSave() {
        try {
            // Save logic
            notificationService.showSaveSuccess();
        } catch (Exception e) {
            notificationService.showErrorDialog(e);
        }
    }
}
```

### For Existing Code
**Option 1: Add service injection (recommended)**
```java
@Autowired(required = false)
private CNotificationService notificationService;

// Then use service with fallback
if (notificationService != null) {
    notificationService.showSuccess("Success!");
} else {
    showNotification("Success!"); // fallback
}
```

**Option 2: Use static utility**
```java
// Replace direct Notification.show() calls
CNotifications.showSuccess("Success!");
CNotifications.showError("Error occurred!");
```

## Styling Details

### Toast Notifications
- **Success**: `LUMO_SUCCESS` variant, green theme, bottom-start position
- **Error**: `LUMO_ERROR` variant, red theme, middle position
- **Warning**: `LUMO_CONTRAST` variant, orange theme, top-center position
- **Info**: `LUMO_PRIMARY` variant, blue theme, bottom-start position

### Durations
- **Short (2000ms)**: Success messages, quick confirmations
- **Medium (5000ms)**: Info and warning messages
- **Long (8000ms)**: Error messages requiring user attention

### Dialogs
- **Information**: Blue info icon, OK button
- **Warning**: Yellow warning icon, OK button
- **Error**: Red error icon, OK button, exception details
- **Confirmation**: Question icon, Yes/No buttons with callbacks

## Best Practices

### When to Use Each Type
- **Success Toast**: Save operations, deletions, quick confirmations
- **Error Toast**: Non-critical errors, validation warnings
- **Warning Toast**: Important notices, potential issues
- **Info Toast**: General information, status updates
- **Error Dialog**: Critical errors, exceptions with details
- **Warning Dialog**: Important warnings requiring acknowledgment
- **Info Dialog**: Detailed information, help text
- **Confirmation Dialog**: Destructive operations, important decisions

### Message Guidelines
- Keep messages concise and actionable
- Use consistent language and tone
- Include context when helpful
- Avoid technical jargon for user-facing messages

## Future Enhancements

### Potential Improvements
1. **Toast Queuing**: Handle multiple notifications gracefully
2. **Persistent Notifications**: For critical messages that shouldn't auto-dismiss
3. **Rich Content**: Support for HTML, icons, and custom styling
4. **Animation Effects**: Enhanced visual transitions
5. **Sound Support**: Audio cues for different notification types
6. **User Preferences**: Allow users to configure notification behavior
7. **Integration Logging**: Centralized logging of all user notifications

### Migration Strategy
1. **Phase 1**: Service injection in new components (current)
2. **Phase 2**: Gradual migration of existing direct calls
3. **Phase 3**: Enhanced features and customization options
4. **Phase 4**: Removal of fallback mechanisms (breaking change)

## Technical Notes

### Dependencies
- Spring Framework for dependency injection
- Vaadin Flow for UI components
- SLF4J for logging
- Tech.derbent.api.utils.Check for validation

### Thread Safety
- Service is stateless and thread-safe
- Static utility methods are thread-safe
- UI operations must be performed on the UI thread (Vaadin handles this)

### Performance
- Minimal overhead for service calls
- Efficient toast notification rendering
- Lazy dialog creation only when needed

This notification system provides a solid foundation for consistent user feedback throughout the Derbent application while maintaining flexibility for future enhancements.