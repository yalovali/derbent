# Button Functionality Testing Guide

## Overview
This guide provides instructions for testing New, Save, and Delete buttons across all pages in the Derbent application, both manually and automatically.

## Automated Testing

### Running the Automated Test

The `CButtonFunctionalityTest` class provides comprehensive automated testing of all buttons across the application.

```bash
# Run button functionality test only
./run-playwright-tests.sh buttons

# Run all tests including button functionality
./run-playwright-tests.sh all
```

### What the Automated Test Does

The test performs the following operations on each page:

1. **Navigation**: Systematically navigates to all menu items
2. **New Button Testing**:
   - Checks if New button is present
   - Verifies button is visible and enabled
   - Clicks button and verifies form/dialog appears
   - Captures screenshot of opened form
3. **Save Button Testing**:
   - Fills form fields with test data
   - Verifies Save button is present, visible, and enabled
   - Clicks Save button
   - Verifies form closes or notification appears
   - Captures screenshot of save operation
4. **Delete Button Testing**:
   - Selects a row in the grid (if data exists)
   - Verifies Delete button is present, visible, and enabled
   - Clicks Delete button
   - Handles confirmation dialog (cancels to preserve data)
   - Captures screenshot of delete confirmation

### Test Output

The test generates:
- **Detailed logs** showing button status for each page
- **Summary statistics**: 
  - Total pages tested
  - Pages with New button
  - Pages with Save button
  - Pages with Delete button
- **Screenshots** in `target/screenshots/` directory:
  - `button-test-<page>-initial.png`: Initial page state
  - `button-test-<page>-new-clicked.png`: After clicking New
  - `button-test-<page>-save-clicked.png`: After clicking Save
  - `button-test-<page>-delete-clicked.png`: After clicking Delete
  - `button-test-<page>-edit-form.png`: Edit form state

### Example Output

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 Testing Page 1 of 15: Projects
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ✅ New button found on page: Projects
   🖱️ Testing New button click responsiveness
   ✅ New button is responsive - form/dialog appeared
   ✅ Save button found on page: Projects
   🖱️ Testing Save button click responsiveness
   ✅ Save button is responsive - form closed
   ✅ Delete button found on page: Projects
   🖱️ Testing Delete button click responsiveness
   ✅ Delete button is responsive - confirmation dialog appeared
   🔴 Cancelled deletion to preserve test data
📊 Page Summary: New=✅, Save=✅, Delete=✅

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 FINAL SUMMARY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Total Pages Tested: 15
Pages with New Button: 12
Pages with Save Button: 12
Pages with Delete Button: 10
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## Manual Testing

### Prerequisites
1. Start the application:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```
2. Navigate to http://localhost:8080
3. Login with credentials: `admin` / `test123`
4. Click "DB Minimal" button to load sample data (if needed)

### Manual Testing Checklist

For each page in the application, perform the following tests:

#### 1. Activities Page
- [ ] **New Button**
  - Navigate to Activities page
  - Click "New" button
  - Verify form dialog opens
  - Check form has required fields (Name, Status, Type)
- [ ] **Save Button**
  - Fill in activity name: "Test Activity"
  - Select status from dropdown
  - Select type from dropdown
  - Click "Save" button
  - Verify success notification appears
  - Verify activity appears in grid
- [ ] **Delete Button**
  - Select an activity row in the grid
  - Click "Delete" button
  - Verify confirmation dialog appears
  - Click "Cancel" to keep the data
  - Try again and click "Yes" to delete
  - Verify success notification appears
  - Verify activity is removed from grid

#### 2. Meetings Page
- [ ] **New Button**
  - Navigate to Meetings page
  - Click "New" button
  - Verify form dialog opens
- [ ] **Save Button**
  - Fill in meeting title
  - Select status
  - Fill in date/time if required
  - Click "Save" button
  - Verify meeting is saved
- [ ] **Delete Button**
  - Select a meeting
  - Click "Delete" button
  - Verify deletion works correctly

#### 3. Projects Page
- [ ] **New Button**
  - Navigate to Projects page
  - Click "New" button
  - Verify form opens
- [ ] **Save Button**
  - Fill in project name
  - Fill in other required fields
  - Click "Save" button
  - Verify project is created
- [ ] **Delete Button**
  - Select a project
  - Test delete functionality

#### 4. Users Page
- [ ] **New Button**
  - Navigate to Users page
  - Click "New" button
  - Verify user form opens
- [ ] **Save Button**
  - Fill in username
  - Fill in email
  - Set password
  - Click "Save" button
  - Verify user is created
- [ ] **Delete Button**
  - Select a user
  - Test delete functionality

#### 5. Companies Page
- [ ] **New Button**: Test company creation
- [ ] **Save Button**: Test company save
- [ ] **Delete Button**: Test company deletion

#### 6. Status Types Page
- [ ] **New Button**: Test status type creation
- [ ] **Save Button**: Test status type save
- [ ] **Delete Button**: Test status type deletion

#### 7. Activity Types Page
- [ ] **New Button**: Test activity type creation
- [ ] **Save Button**: Test activity type save
- [ ] **Delete Button**: Test activity type deletion

### Button Behavior Checklist

For each button on each page, verify:

- [ ] **Visibility**: Button is clearly visible on the page
- [ ] **Positioning**: Button is in the expected location (toolbar or form)
- [ ] **Enabled State**: Button is enabled when it should be
- [ ] **Disabled State**: Button is disabled when appropriate (e.g., Save without selection)
- [ ] **Click Response**: Button responds immediately when clicked
- [ ] **Visual Feedback**: Button shows visual feedback (hover, active states)
- [ ] **Icon**: Button has appropriate icon if applicable
- [ ] **Tooltip**: Button has helpful tooltip on hover (if applicable)
- [ ] **Keyboard Access**: Button can be activated via keyboard (Tab + Enter)
- [ ] **Mobile Friendly**: Button is appropriately sized for touch on mobile

### Common Button Issues to Check

1. **New Button Issues**:
   - Button not appearing on pages that should have it
   - Form not opening when clicked
   - Multiple clicks required to open form
   - Form opening behind other content

2. **Save Button Issues**:
   - Button not enabled even with valid data
   - Button enabled with invalid data
   - No feedback after clicking
   - Form not closing after successful save
   - Data not persisting after save

3. **Delete Button Issues**:
   - Button not disabled when no row is selected
   - No confirmation dialog appearing
   - Data deleted without confirmation
   - Wrong item being deleted
   - Error when deleting items with dependencies

### Responsiveness Testing

Test button functionality at different screen sizes:

- [ ] **Desktop (1920x1080)**: All buttons visible and functional
- [ ] **Tablet (1024x768)**: Buttons adapt to smaller screen
- [ ] **Mobile (375x667)**: Buttons remain accessible and usable

### Performance Testing

For each button:
- [ ] **Response Time**: Button responds within 200ms of click
- [ ] **Animation**: Any button animations are smooth
- [ ] **Loading State**: Long operations show loading indicator
- [ ] **Error Recovery**: Button recovers properly from errors

## Troubleshooting

### Issue: New Button Not Appearing
- Check if the view has a grid/list component
- Verify user has permission to create items
- Check browser console for JavaScript errors

### Issue: Save Button Not Working
- Verify all required fields are filled
- Check form validation messages
- Look for error notifications
- Check browser console for errors

### Issue: Delete Button Not Working
- Verify a row is selected in the grid
- Check if item has dependencies preventing deletion
- Look for error notifications about why deletion failed

## Reporting Issues

When reporting button functionality issues, include:

1. **Page Name**: Which page has the issue
2. **Button Type**: New, Save, or Delete
3. **Steps to Reproduce**: Exact steps to reproduce the issue
4. **Expected Behavior**: What should happen
5. **Actual Behavior**: What actually happens
6. **Screenshots**: Screenshots showing the issue
7. **Console Errors**: Any errors from browser console
8. **Environment**: Browser, OS, screen size

## Screenshots Location

All automated test screenshots are saved to:
```
target/screenshots/button-test-*.png
```

Review these screenshots to visually verify button functionality across all pages.

## Additional Resources

- **Playwright Test Source**: `src/test/java/automated_tests/tech/derbent/ui/automation/CButtonFunctionalityTest.java`
- **Test Base Class**: `src/test/java/automated_tests/tech/derbent/ui/automation/CBaseUITest.java`
- **Test Runner Script**: `run-playwright-tests.sh`

## Continuous Integration

The button functionality test runs automatically in CI/CD pipeline:
- On every pull request
- On every merge to main branch
- Nightly builds

View test results in GitHub Actions:
- Navigate to repository → Actions tab
- Find the latest workflow run
- Check "Button Functionality Test" step
- Download screenshots artifact for visual verification
