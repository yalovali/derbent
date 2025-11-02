# Visual CRUD Testing Examples

This document provides concrete examples of what the Playwright test screenshots will show for CRUD operations.

## 📸 Screenshot Examples by Operation

### CREATE Operation Example: Activity Type

**Scenario**: Creating a new Activity Type called "Sprint Planning"

**Screenshot Sequence**:

1. **`activity-type-initial.png`**
   ```
   Shows:
   - Grid with existing Activity Types
   - Toolbar with buttons: New, Save (disabled), Edit (disabled), Delete (disabled), Refresh
   - Empty details panel on right side
   ```

2. **`activity-type-after-new.png`**
   ```
   Shows:
   - New button is now disabled
   - Save button is now enabled
   - Empty form fields ready for input:
     * Name: [empty text field]
     * Description: [empty text area]
     * Color: [color picker]
     * Sort Order: [number field]
   - Grid shows existing data (no selection)
   ```

3. **`activity-type-filled.png`**
   ```
   Shows:
   - Form filled with test data:
     * Name: "Sprint Planning"
     * Description: "Type for sprint planning meetings"
     * Color: [Blue selected]
     * Sort Order: 10
   - Save button highlighted/ready to click
   - Grid unchanged
   ```

4. **`activity-type-after-save.png`**
   ```
   Shows:
   - Success notification (green): "Data saved successfully"
   - Grid now includes new "Sprint Planning" row
   - New row is selected/highlighted
   - Form shows saved data
   - New button is enabled again
   - Edit and Delete buttons are now enabled
   ```

---

### READ Operation Example: Meeting Type

**Scenario**: Viewing an existing Meeting Type

**Screenshot**:

**`meeting-type-read.png`**
```
Shows:
- Grid with Meeting Type "Status Review" selected (highlighted row)
- Details panel showing:
  * Name: "Status Review"
  * Description: "Weekly status review meeting"
  * Duration: "60 minutes"
  * Color: Orange
  * Required Participants: 3
- All form fields are populated (read-only or editable depending on edit mode)
- Edit button enabled
- Delete button enabled
```

---

### UPDATE Operation Example: Decision Type

**Scenario**: Updating a Decision Type from "Minor" to "Low Priority"

**Screenshot Sequence**:

1. **`decision-type-before-update.png`**
   ```
   Shows:
   - Selected Decision Type: "Minor"
   - Form fields showing current values:
     * Name: "Minor"
     * Description: "Minor decisions"
     * Approval Required: No
   - Edit button visible
   ```

2. **`decision-type-modified.png`**
   ```
   Shows:
   - Name field changed to: "Low Priority"
   - Description updated to: "Low priority decisions"
   - Approval Required changed to: Yes
   - Save button highlighted (changes pending)
   - Form has visual indicator of unsaved changes
   ```

3. **`decision-type-after-update.png`**
   ```
   Shows:
   - Success notification: "Data updated successfully"
   - Grid row updated with new name "Low Priority"
   - Form shows saved values
   - Row still selected in grid
   ```

---

### DELETE VALIDATION Example: Order Type (Protected Entity)

**Scenario**: Attempting to delete an Order Type that's referenced by orders

**Screenshot Sequence**:

1. **`order-type-delete-attempt.png`**
   ```
   Shows:
   - Order Type "Purchase Order" selected in grid
   - Delete button clicked (or confirmation dialog shown)
   - Cursor over Delete button or dialog visible
   ```

2. **`order-type-delete-error.png`**
   ```
   Shows:
   - Error notification (red): "Cannot delete: Order Type is referenced by 15 active orders"
   - Or error dialog with message:
     "This Order Type cannot be deleted"
     "Reason: Referenced by existing orders"
     [OK button]
   - Entity still present in grid
   - Selection maintained
   ```

**Alternative Scenario**: Delete of unreferenced entity would show confirmation dialog

---

### REFRESH Operation Example: Approval Status

**Screenshot Sequence**:

1. **`approval-status-before-refresh.png`**
   ```
   Shows:
   - Grid with current Approval Status entries
   - One status selected: "Pending Review"
   - Current data state
   ```

2. **`approval-status-after-refresh.png`**
   ```
   Shows:
   - Refresh notification: "Data refreshed successfully"
   - Grid reloaded (may show updated data if modified by another user)
   - Same entity still selected
   - Timestamp or indicator showing data was reloaded
   ```

---

## 🎨 Form State Screenshots

### Form Validation Example: Required Field

**`activity-status-validation-error.png`**
```
Shows:
- Name field empty (red border)
- Error message below field: "Name is required"
- Save button disabled
- Validation icon (!) next to field label
- Color field empty (red border)
- Error message: "Color must be selected"
```

### ComboBox Selection Example

**`meeting-type-combobox-open.png`**
```
Shows:
- Meeting Category ComboBox expanded
- Dropdown showing options:
  * Team Meeting
  * Client Meeting
  * Planning Session
  * Review Meeting
  * [+ 5 more options]
- One option highlighted (hover state)
- Searchable field at top showing "Client" (filtering list)
```

---

## 📊 Grid Interaction Screenshots

### Sorting Example

**`comprehensive-grid-sort-name-asc.png`**
```
Shows:
- Activity Types grid
- "Name" column header with ↑ arrow (ascending sort)
- Grid rows sorted alphabetically:
  1. Bug Fix
  2. Code Review
  3. Development
  4. Documentation
  5. Sprint Planning
  6. Testing
- Sort indicator clear in column header
```

### Filtering Example

**`comprehensive-grid-filter-search.png`**
```
Shows:
- Search field at top of grid with text: "meeting"
- Grid filtered to show only matching entries:
  * Meeting Minutes Review
  * Project Meeting Type
  * Status Review Meeting
- Row count indicator: "Showing 3 of 25 items"
- Clear filter button visible
```

---

## 🔔 Notification Screenshots

### Success Notification

**Example in screenshots**:
```
[Green notification card]
✓ Data saved successfully
```

**Appears in screenshots**:
- Position: Bottom-left or top-center of page
- Duration: Visible for 2-3 seconds
- Color: Green background with white text
- Icon: Checkmark (✓)

### Error Notification

**Example in screenshots**:
```
[Red notification card]
✗ Cannot delete: Entity is referenced by 15 items
```

**Appears in screenshots**:
- Position: Center or top-center of page
- Duration: Visible for 8 seconds
- Color: Red background with white text
- Icon: X or warning symbol

### Warning Notification

**Example in screenshots**:
```
[Orange notification card]
⚠ Changes will affect all related records
```

---

## 🖱️ Button State Screenshots

### New Button States

1. **Enabled** (`buttons-new-enabled.png`):
   ```
   [New] button - blue/primary color, cursor shows pointer
   ```

2. **Disabled** (`buttons-new-disabled.png`):
   ```
   [New] button - gray, cursor shows not-allowed
   ```

3. **Pressed** (`buttons-new-pressed.png`):
   ```
   [New] button - darker shade, ripple effect visible
   ```

### Save Button States

1. **Enabled with changes** (`buttons-save-enabled-dirty.png`):
   ```
   [Save] button - highlighted, pulsing indicator showing unsaved changes
   ```

2. **Disabled (no changes)** (`buttons-save-disabled-clean.png`):
   ```
   [Save] button - gray, no indicator
   ```

---

## 📱 Navigation Screenshots

### Menu Navigation Example

**`menu-navigation-expanded.png`**
```
Shows:
- Left sidebar with menu items:
  * [+] Configuration
    * Activity Types ← currently selected (highlighted)
    * Meeting Types
    * Status Management
    * Workflow Configuration
  * [+] Projects
    * Project Overview
    * Project Planning
  * [+] Activities
    * Activity Management
    * Activity Dashboard
- Main content area showing Activity Types view
- Breadcrumb: Home > Configuration > Activity Types
```

---

## 🎯 Multi-Step Workflow Screenshots

### Complete CRUD Cycle: Creating and Updating an Entity

**Full sequence for "Approval Status" entity**:

1. `approval-status-01-initial.png` - Empty view, no selection
2. `approval-status-02-after-new.png` - New clicked, empty form
3. `approval-status-03-filling-name.png` - Name field being filled
4. `approval-status-04-filling-description.png` - Description being added
5. `approval-status-05-selecting-color.png` - Color picker open
6. `approval-status-06-form-complete.png` - All fields filled
7. `approval-status-07-after-save.png` - Success notification, grid updated
8. `approval-status-08-entity-selected.png` - New entity selected and displayed
9. `approval-status-09-editing.png` - Edit mode, modifying fields
10. `approval-status-10-after-update.png` - Update success, changes reflected

---

## 📐 Responsive Design Screenshots

### Desktop View (1920×1080)

**`comprehensive-desktop-view.png`**
```
Shows:
- Full sidebar visible on left (250px width)
- Main content area with grid (1200px width)
- Details panel on right (470px width)
- All toolbar buttons visible with text labels
- Grid shows 8-10 columns
```

### Tablet View (768×1024)

**`comprehensive-tablet-view.png`**
```
Shows:
- Sidebar collapsed to icons only (60px width)
- Main content area expanded
- Details panel below grid instead of beside
- Toolbar buttons show icons only (text hidden)
- Grid shows 4-6 main columns
```

---

## 🔍 Detailed Form Screenshots

### Complex Form Example: Activity Type with All Fields

**`activity-type-form-complete.png`**
```
Shows:
Form fields top to bottom:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Name: [Sprint Planning        ]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Description:
┌─────────────────────────────┐
│ Activity type for sprint    │
│ planning sessions           │
└─────────────────────────────┘
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Color: [🎨 Blue #0066CC       ]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Sort Order: [10               ]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Active: ☑ Yes  ☐ No
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Default: ☐ Yes  ☑ No
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[Save] [Cancel] [Delete]
```

---

## 📊 Grid Detail Screenshots

### Grid with Data Example

**`activity-type-grid-populated.png`**
```
Shows:
╔════════════════════╤═══════════╤═════════════╤══════╗
║ Name              │ Color     │ Sort Order  │ Act. ║
╠════════════════════╪═══════════╪═════════════╪══════╣
║ ▶ Bug Fix         │ 🔴 Red    │ 1          │ ✓    ║
║   Code Review     │ 🔵 Blue   │ 2          │ ✓    ║
║   Development     │ 🟢 Green  │ 3          │ ✓    ║
║   Documentation   │ 🟡 Yellow │ 4          │ ✓    ║
║   Sprint Planning │ 🔵 Blue   │ 5          │ ✓    ║
║   Testing         │ 🟣 Purple │ 6          │ ✓    ║
╚════════════════════╧═══════════╧═════════════╧══════╝

▶ = Selected row (highlighted)
Grid shows 6 of 15 total rows
[◀ 1 2 3 ▶] pagination controls
```

---

## 🎭 Dialog Screenshots

### Confirmation Dialog

**`order-type-delete-confirmation.png`**
```
Shows:
┌─────────────────────────────────┐
│   ⚠️  Confirm Delete            │
├─────────────────────────────────┤
│                                 │
│  Are you sure you want to      │
│  delete "Purchase Order"?       │
│                                 │
│  This action cannot be undone.  │
│                                 │
│  [Cancel]        [Yes, Delete] │
│                                 │
└─────────────────────────────────┘
```

### Error Dialog

**`activity-status-constraint-error.png`**
```
Shows:
┌─────────────────────────────────┐
│   ❌ Operation Failed            │
├─────────────────────────────────┤
│                                 │
│  Cannot delete Activity Status  │
│                                 │
│  Reason:                        │
│  • Referenced by 127 activities │
│  • Used in 3 active workflows   │
│                                 │
│  To delete this status, first   │
│  reassign all related records.  │
│                                 │
│            [OK]                 │
│                                 │
└─────────────────────────────────┘
```

---

## 💡 Test Scenarios with Expected Screenshots

### Scenario 1: New User Creating Their First Activity Type

**Screenshots Expected**:
1. Login screen
2. Empty dashboard
3. Navigate to Activity Types (menu highlighted)
4. Empty Activity Types grid (no data)
5. Click New button
6. Form appears empty
7. Fill name field
8. Fill description
9. Select color from picker
10. Click Save
11. Success notification
12. Grid now shows 1 entry
13. New entry is selected

**Total**: 13 screenshots documenting complete first-time user experience

---

### Scenario 2: Power User Bulk Creating Multiple Entities

**Screenshots Expected**:
1. Activity Type view with existing data
2. Create first new type
3. Success notification  
4. Create second new type
5. Success notification
6. Create third new type
7. Success notification
8. Grid showing all 3 new entries
9. Sorting by creation date
10. All entries visible together

**Total**: 10 screenshots showing efficiency testing

---

## 📋 Screenshot Checklist

When running tests, expect to see screenshots covering:

### For Each Entity Type:
- [ ] Initial view (grid with existing data or empty)
- [ ] After clicking New (form ready for input)
- [ ] Form with validation errors (if required fields empty)
- [ ] Form completely filled
- [ ] After successful save (notification + updated grid)
- [ ] Entity selected in grid (details displayed)
- [ ] During update (modified fields)
- [ ] After update save (notification + changes reflected)
- [ ] Delete attempt (confirmation or error)
- [ ] After refresh operation

### For Comprehensive Testing:
- [ ] Navigation menu in different states
- [ ] Grid with various data densities (empty, few items, many items)
- [ ] Grid sorting (ascending/descending different columns)
- [ ] Grid filtering (search results)
- [ ] Grid pagination (different pages)
- [ ] Form validation states
- [ ] ComboBox dropdowns (open/closed, with selections)
- [ ] All notification types (success, error, warning, info)
- [ ] All button states (enabled, disabled, pressed, focused)
- [ ] Responsive layouts (if testing different screen sizes)

---

## 🎯 Summary

Each Playwright test run generates **50-80 screenshots** that comprehensively document:
- **Create operations**: Form filling, validation, saving, success feedback
- **Read operations**: Data display, field population, selection states
- **Update operations**: Editing, modification, saving, change reflection
- **Delete operations**: Protection mechanisms, confirmations, error handling
- **UI States**: Buttons, notifications, grids, forms, dialogs, menus
- **User Flows**: Complete workflows from start to finish

These screenshots serve as:
1. **Visual regression testing** baseline
2. **User documentation** for application functionality
3. **Debugging artifacts** when tests fail
4. **Design validation** for UI consistency
5. **Feature demonstration** for stakeholders
