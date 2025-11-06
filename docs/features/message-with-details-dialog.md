# Message with Details Dialog

## Overview

The `CMessageWithDetailsDialog` provides a user-friendly way to display error messages with expandable technical details. This dialog shows a clear, readable message to users while allowing them to view full exception stack traces when needed for troubleshooting.

## Features

- **User-Friendly Message**: Displays a clear, non-technical message that users can understand
- **Expandable Details**: "Show Details" button reveals full exception stack trace
- **Clean UI**: Uses appropriate icons and styling consistent with the application theme
- **Dynamic Height**: Dialog automatically expands when details are shown and shrinks when hidden
- **Scrollable Details**: Exception details are shown in a scrollable text area (max 300px height)
- **Monospace Font**: Stack traces are displayed in a monospace font for better readability

## Usage

### Using CNotificationService (Recommended)

When you have access to dependency injection (in views, services, etc.), use the `CNotificationService`:

```java
@Autowired
private CNotificationService notificationService;

public void someMethod() {
    try {
        // Your code that might throw an exception
        performComplexOperation();
    } catch (Exception e) {
        // Show a friendly message with technical details available
        notificationService.showMessageWithDetails(
            "Unable to complete the operation. Please try again or contact support.",
            e
        );
    }
}
```

### Using CNotifications (Static Context)

When dependency injection is not available (utility classes, static methods):

```java
import tech.derbent.api.ui.notifications.CNotifications;

public static void utilityMethod() {
    try {
        // Your code that might throw an exception
        performOperation();
    } catch (Exception e) {
        // Show message with details (currently logs only)
        CNotifications.showMessageWithDetails(
            "Operation failed. Please check the logs.",
            e
        );
    }
}
```

## Example Scenarios

### Database Connection Error

```java
try {
    connectToDatabase();
} catch (SQLException e) {
    notificationService.showMessageWithDetails(
        "Unable to connect to the database. Please check your connection settings.",
        e
    );
}
```

### File Processing Error

```java
try {
    processFile(file);
} catch (IOException e) {
    notificationService.showMessageWithDetails(
        "Error processing file '" + file.getName() + "'. The file may be corrupted or in an unsupported format.",
        e
    );
}
```

### API Call Failure

```java
try {
    callExternalAPI();
} catch (Exception e) {
    notificationService.showMessageWithDetails(
        "Failed to communicate with the external service. Please try again later.",
        e
    );
}
```

## UI Appearance

### Initial State (Details Hidden)

The dialog shows:
- **Title**: "Error Details"
- **Icon**: Exclamation circle icon (error indicator)
- **Message**: Your user-friendly message
- **Exception Type**: Subtle hint showing the exception class name
- **Buttons**: "Show Details" and "OK"

### Expanded State (Details Shown)

When user clicks "Show Details":
- Dialog height expands to 600px
- Exception stack trace appears in scrollable text area
- Button text changes to "Hide Details"
- Button icon changes to up arrow
- Separator line appears above details

## Implementation Details

### Dialog Class

- **Package**: `tech.derbent.api.ui.dialogs`
- **Class**: `CMessageWithDetailsDialog`
- **Extends**: `CDialog`
- **Style**: Uses Vaadin components with consistent application theme

### Key Components

1. **Message Display**: Centered text with 16px font size
2. **Exception Type**: Smaller, italic text showing exception class
3. **Details Area**: Read-only `TextArea` with monospace font
4. **Toggle Button**: Tertiary button with angle-down/angle-up icon
5. **Separator**: Visual separator between message and details

## Best Practices

### When to Use This Dialog

✅ **Use when:**
- An operation fails and you want to provide both user-friendly and technical information
- Users might need to report the error to support (they can see full details)
- You want to avoid overwhelming non-technical users with stack traces
- Debugging information could be helpful but isn't always needed

❌ **Don't use when:**
- Simple error messages are sufficient (use `showError()` instead)
- You just need to display exception details (use `showErrorDialog()` instead)
- The error is a warning or validation issue (use `showWarning()` instead)

### Writing Good User Messages

Good message examples:
- "Unable to save your changes. Please try again or contact support."
- "The file could not be uploaded. Check the file format and size."
- "Connection to the server was lost. Please check your internet connection."

Avoid:
- Technical jargon: "SQLException: Connection pool exhausted"
- Vague messages: "An error occurred"
- Blame: "You entered invalid data"

## Code Example: Complete Usage Pattern

```java
@Service
public class DataImportService {
    
    @Autowired
    private CNotificationService notificationService;
    
    public void importData(File file) {
        try {
            // Validate file
            if (!isValidFormat(file)) {
                notificationService.showWarning("Please select a valid CSV or Excel file.");
                return;
            }
            
            // Process file
            List<Record> records = parseFile(file);
            saveRecords(records);
            
            // Success
            notificationService.showSuccess("Data imported successfully!");
            
        } catch (IOException e) {
            // File I/O error - show message with details
            notificationService.showMessageWithDetails(
                "Unable to read the file '" + file.getName() + 
                "'. The file may be locked, corrupted, or in use by another program.",
                e
            );
        } catch (SQLException e) {
            // Database error - show message with details
            notificationService.showMessageWithDetails(
                "Unable to save the imported data to the database. " +
                "Please try again or contact your system administrator.",
                e
            );
        } catch (Exception e) {
            // Unexpected error - show message with details
            notificationService.showMessageWithDetails(
                "An unexpected error occurred during import. " +
                "Please contact support if the problem persists.",
                e
            );
        }
    }
    
    private boolean isValidFormat(File file) {
        // Implementation
        return true;
    }
    
    private List<Record> parseFile(File file) throws IOException {
        // Implementation
        return new ArrayList<>();
    }
    
    private void saveRecords(List<Record> records) throws SQLException {
        // Implementation
    }
}
```

## Related Components

- `CNotificationService`: Main service for all notifications
- `CNotifications`: Static utility for notifications without DI
- `CExceptionDialog`: Simple exception dialog without custom message
- `CWarningDialog`: Dialog for warnings
- `CInformationDialog`: Dialog for informational messages

## Technical Notes

- The dialog uses Vaadin's `Dialog` component as its base
- Stack traces are formatted using `PrintWriter` and `StringWriter`
- Height transitions are handled via direct height property changes
- The dialog is modal and can be closed with ESC key
- Logger outputs debug messages for dialog lifecycle events
