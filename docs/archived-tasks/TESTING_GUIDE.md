# CrudToolbar Fix - Testing Guide

## Quick Start

Once Maven network connectivity is restored, run:
```bash
./verify-crud-toolbar-fix.sh
```

This will:
1. ✅ Compile the code
2. ✅ Apply and verify code formatting
3. ✅ Start the application
4. ✅ Verify application is accessible

## Manual UI Testing

### Prerequisites
- Application must be running: `mvn spring-boot:run -Dspring.profiles.active=h2`
- Browser opened to: http://localhost:8080
- Logged in with: admin/test123

### Test Cases

#### 1. Activities Management
**URL:** `/cdynamicpagerouter/page:3`

**Test Steps:**
1. Click "DB Minimal" button to load sample data
2. Click "New" button
   - ✅ Verify: Form clears, new activity is created
   - ✅ Verify: Status combobox is populated with initial status
3. Fill in activity details and click "Save"
   - ✅ Verify: Success notification appears
   - ✅ Verify: Data is saved to grid
4. Select an activity and modify it
   - ✅ Verify: Status combobox shows valid workflow transitions
   - ✅ Try changing status through combobox
   - ✅ Verify: Status change saves successfully
5. Click "Refresh" button
   - ✅ Verify: Data reloads from database
6. Try to delete an activity with dependencies
   - ✅ Verify: Error message appears preventing deletion
7. **Take screenshot:** `activities-crud-toolbar.png`

#### 2. Meetings Management
**URL:** `/cdynamicpagerouter/page:4`

**Test Steps:**
1. Click "New" button
   - ✅ Verify: New meeting form appears
2. Fill in meeting details and click "Save"
   - ✅ Verify: Meeting is saved
3. Click "Refresh" button
   - ✅ Verify: Data reloads
4. Select a meeting and click "Delete"
   - ✅ Verify: Confirmation dialog appears
   - ✅ Verify: Delete works or shows dependency error
5. **Take screenshot:** `meetings-crud-toolbar.png`

#### 3. Projects Management
**URL:** `/cdynamicpagerouter/page:1`

**Test Steps:**
1. Click "New" button
   - ✅ Verify: New project form appears
2. Fill in project details and click "Save"
   - ✅ Verify: Project is saved
3. Try to delete a project with activities
   - ✅ Verify: Error message about dependencies appears
4. Click "Refresh" button
   - ✅ Verify: Project data reloads
5. **Take screenshot:** `projects-crud-toolbar.png`

#### 4. Users Management
**URL:** `/cdynamicpagerouter/page:12`

**Test Steps:**
1. Click "New" button
   - ✅ Verify: New user form appears
2. Fill in user details and click "Save"
   - ✅ Verify: User is saved
3. Click "Refresh" button
   - ✅ Verify: User data reloads
4. **Take screenshot:** `users-crud-toolbar.png`

### Screenshot Checklist

For each screenshot, ensure visible:
- ✅ CRUD toolbar with New, Save, Delete, Refresh buttons
- ✅ Status combobox (for entities with workflows)
- ✅ Grid showing data
- ✅ Detail form with fields
- ✅ Success/error notifications if applicable

### Expected Results

All CRUD operations should work:
- ✅ **Create (New)**: Creates new entity, clears form, initializes with defaults
- ✅ **Read**: Grid displays entities, form shows selected entity details
- ✅ **Update (Save)**: Saves changes, shows success notification
- ✅ **Delete**: Removes entity or shows dependency error
- ✅ **Refresh**: Reloads data from database

## Automated Testing

### Run Playwright Tests
```bash
./run-playwright-tests.sh mock
```

Expected: All tests pass, screenshots generated in `target/screenshots/`

### Run Comprehensive Tests
```bash
./run-playwright-tests.sh comprehensive
```

Expected: All CRUD operations tested across multiple entity types

## Common Issues

### Issue: Application won't start
**Solution:** Check if port 8080 is in use: `lsof -i :8080`

### Issue: CRUD buttons not visible
**Check:**
1. Is the entity service initialized?
2. Is currentBinder set correctly?
3. Check browser console for JavaScript errors

### Issue: Delete button always disabled
**Check:**
1. Is the entity saved (has ID)?
2. Is dependency checker configured?

### Issue: Status combobox not appearing
**Check:**
1. Does entity implement IHasStatusAndWorkflow?
2. Is workflow configured for entity type?

## Reporting Results

After testing, update the PR with:
1. ✅ Compilation status
2. ✅ Test results summary
3. 📸 Screenshots showing CRUD operations working
4. ❌ Any issues found (with details)

Example PR comment:
```
## Testing Results

### Compilation: ✅ SUCCESS

### UI Testing: ✅ PASSED
- Activities: All CRUD operations working
- Meetings: All CRUD operations working
- Projects: Dependency checking working
- Users: All CRUD operations working

### Screenshots
See attached screenshots showing CRUD toolbar on all major entity screens.

### Playwright Tests: ✅ PASSED
All automated tests passed with screenshots generated.

### Conclusion
CrudToolbar fix is working correctly. All compilation errors resolved.
CRUD operations function as expected on all major entity screens.
```

## Verification Checklist

- [ ] Code compiles without errors
- [ ] Code formatting is correct (spotless:check passes)
- [ ] Application starts successfully
- [ ] Activities CRUD operations work
- [ ] Meetings CRUD operations work
- [ ] Projects CRUD operations work
- [ ] Users CRUD operations work
- [ ] Status combobox shows valid transitions
- [ ] Dependency checking prevents invalid deletes
- [ ] Playwright tests pass
- [ ] Screenshots captured for all major screens
- [ ] No console errors in browser
- [ ] PR updated with test results
