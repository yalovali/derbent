# CMessageWithDetailsDialog - Visual Guide

## Dialog States

### State 1: Initial View (Details Hidden)

```
┌─────────────────────────────────────────────────────┐
│ Error Details                                   [X] │
├─────────────────────────────────────────────────────┤
│                                                     │
│   ⚠️  Error Details                                 │
│                                                     │
│   Unable to save your changes. Please               │
│   try again or contact support.                     │
│                                                     │
│   Exception: RuntimeException                       │
│   (in smaller, italicized text)                     │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│          [🔽 Show Details]        [OK]              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**Height**: Auto (compact, approximately 250px)

### State 2: Expanded View (Details Shown)

```
┌─────────────────────────────────────────────────────┐
│ Error Details                                   [X] │
├─────────────────────────────────────────────────────┤
│                                                     │
│   ⚠️  Error Details                                 │
│                                                     │
│   Unable to save your changes. Please               │
│   try again or contact support.                     │
│                                                     │
│   Exception: RuntimeException                       │
│   (in smaller, italicized text)                     │
│                                                     │
│   ─────────────────────────────────────────────    │
│                                                     │
│ ┌─────────────────────────────────────────────┐   │
│ │ java.lang.RuntimeException: Database error  │   │
│ │     at com.example.Service.save(...)        │   │
│ │     at com.example.Controller.handle(...)   │ ▲ │
│ │     at javax.servlet.http.HttpServlet...    │ █ │
│ │     at org.apache.catalina.core...          │ ▼ │
│ │ Caused by: java.sql.SQLException...         │   │
│ │     at org.postgresql.jdbc...               │   │
│ │     ... 45 more                              │   │
│ └─────────────────────────────────────────────┘   │
│      (Scrollable text area, max 300px)            │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│          [🔼 Hide Details]        [OK]              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**Height**: Fixed 600px (expanded to show details)

## Visual Elements Description

### Header Section
- **Title Bar**: "Error Details" with close button (X)
- **Icon**: ⚠️ Exclamation circle icon (warning color)
- **Title**: "Error Details" heading

### Content Section

#### Always Visible
1. **User Message**
   - Font size: 16px
   - Alignment: Center
   - Color: Default body text color
   - Example: "Unable to save your changes. Please try again or contact support."

2. **Exception Type**
   - Font size: 12px
   - Alignment: Center
   - Style: Italic
   - Color: Secondary text color (lighter/grayed out)
   - Format: "Exception: [ExceptionClassName]"

#### Conditionally Visible (When Expanded)
3. **Separator Line**
   - Horizontal line
   - Color: Light gray (--lumo-contrast-10pct)
   - Margin: 16px top and bottom

4. **Stack Trace Area**
   - Component: TextArea (read-only)
   - Width: Full width of dialog
   - Max Height: 300px
   - Font: Monospace
   - Font size: 12px
   - Scrollable: Yes (vertical scrollbar when content exceeds 300px)

### Footer Section (Buttons)
1. **Show/Hide Details Button**
   - Style: Tertiary (less prominent)
   - Icon: Angle-down 🔽 (when hidden) / Angle-up 🔼 (when shown)
   - Text: "Show Details" / "Hide Details"
   - Action: Toggles exception details visibility

2. **OK Button**
   - Style: Primary (prominent)
   - Auto-focus: Yes
   - Action: Closes the dialog

## Color Scheme

The dialog uses the application's Vaadin Lumo theme:
- **Border**: 2px solid #1976D2 (blue)
- **Border radius**: 12px (rounded corners)
- **Box shadow**: 0 4px 20px with blue tint (rgba(25, 118, 210, 0.3))
- **Background**: Subtle gradient (linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%))
- **Icon color**: Warning color (--lumo-warning-color)

## Behavior

### Height Animation
- **Collapsed**: `height: auto` (approximately 250-280px depending on message length)
- **Expanded**: `height: 600px` (fixed height to accommodate details)
- The height change is instant (no CSS transition, but feels smooth due to browser rendering)

### User Interaction Flow
1. User sees the error message
2. User reads the friendly message
3. User notices the "Exception: [Type]" hint
4. If curious or need to report error, user clicks "Show Details"
5. Dialog expands, showing full stack trace
6. User can scroll through the details
7. User can copy details if needed (text area is read-only but copyable)
8. User clicks "Hide Details" to collapse, or "OK" to close

## Accessibility

- **Keyboard Navigation**: Dialog can be closed with ESC key
- **Focus Management**: OK button has autofocus for quick dismissal
- **Screen Readers**: Title and content are properly structured
- **Modal**: Dialog is modal (blocks interaction with background)

## Responsive Design

- **Width**: Fixed at 500px (from CDialog base class)
- **Height**: Dynamic (auto when collapsed, 600px when expanded)
- **Content**: Text area scrolls vertically when content exceeds max height
- **Mobile**: May need viewport adjustment for small screens (inherited behavior)

## Usage Example with Visual Result

```java
// Code
try {
    database.saveUser(user);
} catch (SQLException e) {
    notificationService.showMessageWithDetails(
        "Unable to save user data. Please check your connection.",
        e
    );
}
```

**Result**: User sees a clean dialog with:
- Friendly message about saving user data
- Subtle hint showing "SQLException"
- Option to view full stack trace
- Quick OK button to dismiss
