# Manual Testing Guide for Double-Click and ESC Key Features

## Purpose
This guide provides instructions for manually testing the newly implemented double-click functionality and verifying ESC key support in the Derbent application.

## Prerequisites
- Derbent application running (use `mvn spring-boot:run -Ph2-local-development`)
- Sample data loaded (use "DB Minimal" or "DB Sample" button on login screen)
- Login credentials (e.g., admin/test123)

## Test Case 1: Double-Click to Edit in Relation Grids

### Test Steps

#### Testing User-Project Relations
1. Start the application
2. Login with valid credentials
3. Navigate to Users management
4. Select a user that has project relationships
5. Locate the "Projects" relation grid
6. **Test**: Double-click on any row in the projects grid
7. **Expected**: Edit dialog should open for that project relationship

#### Testing Workflow-Status Relations
1. Navigate to Administration → Workflow settings
2. Select a workflow
3. Locate the status relations grid
4. **Test**: Double-click on any status relation row
5. **Expected**: Edit dialog should open for that status relationship

### Success Criteria
- ✅ Double-clicking a row opens the edit dialog immediately
- ✅ The edit dialog shows the correct data for the clicked row
- ✅ No console errors appear
- ✅ Single-click still selects the row without opening dialog

## Test Case 2: ESC Key to Close Dialogs

### Test Steps

#### Test with Edit Dialog
1. Open any relation grid (as in Test Case 1)
2. Click the "Edit" button or double-click a row to open the edit dialog
3. **Test**: Press the ESC key
4. **Expected**: Dialog should close without saving

#### Test with Add Dialog
1. In any relation grid, click the "Add" button
2. Fill in some data (don't save)
3. **Test**: Press the ESC key
4. **Expected**: Dialog should close without saving the data

#### Test with Confirmation Dialog
1. Try to delete a relation
2. A confirmation dialog should appear
3. **Test**: Press the ESC key
4. **Expected**: Confirmation dialog should close, deletion cancelled

### Success Criteria
- ✅ ESC key closes all types of dialogs
- ✅ No data is saved when ESC is pressed
- ✅ Application returns to previous state
- ✅ No console errors appear

## Test Case 3: Interaction Between Features

### Test Steps
1. Open a relation grid
2. Double-click a row to open edit dialog
3. Make some changes to the data
4. Press ESC key
5. **Expected**: Dialog closes without saving changes
6. Double-click the same row again
7. **Expected**: Original data is still shown (no changes were saved)

### Success Criteria
- ✅ ESC cancels any changes made before closing
- ✅ Double-click continues to work after ESC is used
- ✅ Data integrity is maintained

## Test Case 4: Edge Cases

### Test Steps

#### Empty Grid
1. Find or create an entity with no relations
2. The relation grid should be empty
3. **Test**: Try to double-click in the empty grid
4. **Expected**: Nothing happens, no errors

#### Read-Only Mode (if applicable)
1. Navigate to a read-only view
2. Try to double-click a relation row
3. **Expected**: Either nothing happens or view-only dialog opens (depending on implementation)

### Success Criteria
- ✅ Application handles edge cases gracefully
- ✅ No exceptions or errors in console
- ✅ User receives appropriate feedback

## Screenshot Locations

When running manual tests, capture screenshots at these key moments:

1. `relation-grid-before-double-click.png` - Grid before interaction
2. `relation-grid-double-click-opens-dialog.png` - Dialog opened by double-click
3. `dialog-with-esc-key-hint.png` - Dialog showing it can be closed with ESC
4. `dialog-closed-by-esc.png` - Grid after ESC closed the dialog

## Automated Testing Note

The Playwright test framework can be used for automated testing:
```bash
./run-playwright-tests.sh mock
```

This will generate screenshots automatically in `target/screenshots/` directory.

## Reporting Issues

If any test fails, please report with:
1. Test case number and step
2. Expected vs actual behavior
3. Console error messages (if any)
4. Screenshots of the issue
5. Browser and version used

## Related Files

- Implementation: `CAbstractEntityRelationPanel.java`
- Implementation: `CComponentRelationPanelBase.java`
- Dialog base: `CDialog.java`
- Documentation: `DOUBLE_CLICK_ESC_IMPLEMENTATION.md`
