# Manual Testing Guide for Field Selection Improvements

## Overview
This guide provides step-by-step instructions for manually testing the improvements made to `CComponentFieldSelection` and the dual admin user creation feature.

## Prerequisites
- Database reset (to ensure clean state with new users)
- Application running with H2 profile: `mvn spring-boot:run -Dspring.profiles.active=h2`

## Test 1: Verify Dual Admin Users

### Steps:
1. Start the application fresh (with empty database)
2. Wait for data initialization to complete
3. Navigate to Users view
4. For each company, verify there are 2 admin users:
   - Username: `admin` with phone `+90-462-751-1001`
   - Username: `admin2` with phone `+90-462-751-1002`
5. Verify both users have admin company roles

### Expected Results:
- ✅ 4 companies × 2 admin users = 8 admin users total
- ✅ Each admin user has distinct phone number
- ✅ Both admins can log in with password: `test123`

## Test 2: Insert Below Selection Feature

### Steps:
1. Log in as any admin user
2. Navigate to any form that uses `CComponentFieldSelection` (e.g., User form with activities field)
3. **Initial Setup:**
   - Available grid should show all items
   - Selected grid should be empty
   - Add button should be **disabled** (no selection in available grid)
   
4. **Test Add to Empty List:**
   - Click on "Item A" in available grid
   - Add button should become **enabled**
   - Click Add button
   - Expected: Item A moves to selected grid (at position 0)
   - Add button should be **disabled** again (nothing selected in available grid)

5. **Test Add to End (No Selection):**
   - Click on "Item B" in available grid
   - Click Add button
   - Expected: Item B appears at end of selected list: [Item A, Item B]
   - Item B should be **automatically selected** in selected grid

6. **Test Insert Below Selection:**
   - Click on "Item A" in **selected grid** (select first item)
   - Click on "Item C" in available grid
   - Click Add button
   - Expected: Item C is inserted **after** Item A: [Item A, Item C, Item B]
   - Item C should be **automatically selected** in selected grid

7. **Test Insert Below Middle Item:**
   - Click on "Item C" in **selected grid** (select middle item)
   - Click on "Item D" in available grid
   - Click Add button
   - Expected: Item D is inserted **after** Item C: [Item A, Item C, Item D, Item B]
   - Item D should be **automatically selected** in selected grid

8. **Test Insert Below Last Item:**
   - Click on "Item B" in **selected grid** (select last item)
   - Click on "Item E" in available grid
   - Click Add button
   - Expected: Item E is inserted **after** Item B (effectively at end): [Item A, Item C, Item D, Item B, Item E]
   - Item E should be **automatically selected** in selected grid

9. **Test Move Up/Down Still Works:**
   - Select any item in selected grid
   - Click Move Up button
   - Expected: Item moves up one position (if not already at top)
   - Click Move Down button
   - Expected: Item moves down one position (if not already at bottom)

10. **Test Remove:**
    - Select any item in selected grid
    - Click Remove button
    - Expected: Item moves back to available grid
    - Item is **removed** from selected grid

11. **Test Save:**
    - Configure selected items in desired order
    - Save the form/entity
    - Refresh or reopen the form
    - Expected: Selected items and their **order are preserved**

### Expected Results:
- ✅ Items are inserted below current selection (not at end)
- ✅ Newly added items are automatically selected
- ✅ Add button is disabled when no item is selected in available grid
- ✅ Order is preserved when saving and reloading
- ✅ Move up/down functionality still works correctly

## Test 3: Grid Styling Consistency

### Steps:
1. Open multiple views with grids:
   - CComponentFieldSelection grids
   - Other entity grids (CComponentRelationBase, etc.)
2. Compare visual styling:
   - Border radius: 8px
   - Border: 1px solid #E0E0E0
   - Selection mode: SINGLE
   - Width: 100% of container

### Expected Results:
- ✅ All grids have consistent styling
- ✅ CComponentFieldSelection grids match other grids
- ✅ Selection mode works consistently

## Test 4: Binder Integration

### Steps:
1. Create or edit an entity with a field using `CComponentFieldSelection`
2. Add several items to the selected list in specific order
3. Save the entity
4. Verify data in database (order should be preserved if field has `@OrderColumn`)
5. Reload the form
6. Verify selected items appear in the same order

### Expected Results:
- ✅ Data is saved to database correctly
- ✅ Order is preserved for `@OrderColumn` fields
- ✅ Binder value change events fire correctly
- ✅ Form validation works as expected

## Test 5: Read-Only Mode

### Steps:
1. Open a form with `CComponentFieldSelection` in read-only mode
2. Verify all buttons are disabled:
   - Add button
   - Remove button
   - Move Up button
   - Move Down button
3. Verify grids have selection mode NONE (cannot select items)

### Expected Results:
- ✅ All buttons are disabled in read-only mode
- ✅ Grids do not allow selection
- ✅ Component displays data but prevents editing

## Test 6: Double-Click Functionality

### Steps:
1. Open a form with `CComponentFieldSelection`
2. Double-click an item in available grid
3. Expected: Item moves to selected grid
4. Double-click an item in selected grid
5. Expected: Item moves back to available grid

### Expected Results:
- ✅ Double-click on available item adds it to selected
- ✅ Double-click on selected item removes it
- ✅ Works in same way as using buttons

## Regression Testing

Verify that existing functionality still works:
- ✅ All unit tests pass (10/10 in CComponentFieldSelectionTest)
- ✅ Code compiles without errors or warnings
- ✅ Spotless formatting is correct
- ✅ No console errors in browser
- ✅ Other components using grids still work correctly

## Known Issues / Notes

1. **Insert-below-selection** only works when an item is selected in the selected grid. If nothing is selected, items are added to the end (this is expected behavior).

2. **Automatic selection** of newly added items makes it easy to continue adding items in sequence without manually selecting.

3. **Profile pictures**: Both admin and admin2 use the same profile picture (admin.svg). This can be changed by updating the `PROFILE_PICTURE_MAPPING` if needed.

## Reporting Issues

If any test fails, please report:
1. Which test failed
2. Expected behavior
3. Actual behavior
4. Steps to reproduce
5. Browser console errors (if any)
6. Server logs (if relevant)

## Success Criteria

All tests should pass:
- ✅ Dual admin users created for each company
- ✅ Insert-below-selection works as expected
- ✅ Grid styling is consistent
- ✅ Binder integration works correctly
- ✅ Read-only mode works correctly
- ✅ Double-click functionality works
- ✅ No regressions in existing functionality
