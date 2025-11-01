# CCrudToolbar Refactoring - Testing Guide

## Overview
This document outlines the comprehensive testing strategy for the CCrudToolbar refactoring.

## Build and Format

### 1. Apply Code Formatting
```bash
mvn spotless:apply
```

### 2. Check Code Formatting
```bash
mvn spotless:check
```

### 3. Compile the Project
```bash
mvn clean compile
```
Expected: Should compile without errors

## Unit Testing Strategy

### Test 1: Minimal Constructor Creation
**Purpose**: Verify toolbar can be created with just a binder

```java
@Test
public void testMinimalConstructor() {
    CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);
    CCrudToolbar toolbar = new CCrudToolbar(binder);
    
    assertNotNull(toolbar);
    assertNotNull(toolbar.getBinder());
    
    // Verify buttons exist but are disabled
    // (Buttons should be visible but disabled when not configured)
}
```

### Test 2: Static Factory Method
**Purpose**: Verify static factory method works

```java
@Test
public void testStaticFactoryMethod() {
    CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);
    CCrudToolbar toolbar = CCrudToolbar.create(binder);
    
    assertNotNull(toolbar);
    assertNotNull(toolbar.getBinder());
}
```

### Test 3: Post-Construction Configuration
**Purpose**: Verify configuration via setters

```java
@Test
public void testPostConstructionConfiguration() {
    CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);
    CCrudToolbar toolbar = CCrudToolbar.create(binder);
    
    // Configure
    toolbar.setEntityService(activityService);
    toolbar.setEntityClass(CActivity.class);
    toolbar.setNewEntitySupplier(() -> new CActivity());
    toolbar.setNotificationService(notificationService);
    
    // Verify configuration applied
    // Test buttons are now enabled appropriately
}
```

### Test 4: Dynamic Entity Type Reconfiguration
**Purpose**: Verify toolbar can be reconfigured for different entity types

```java
@Test
public void testReconfigureForEntityType() {
    CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);
    CCrudToolbar toolbar = CCrudToolbar.create(binder);
    
    // Configure for Activity
    toolbar.reconfigureForEntityType(CActivity.class, activityService);
    
    // Verify activity configuration
    // ...
    
    // Reconfigure for Meeting
    toolbar.reconfigureForEntityType(CMeeting.class, meetingService);
    
    // Verify meeting configuration
    // Status combobox should be recreated if workflow support differs
}
```

### Test 5: Backward Compatibility
**Purpose**: Verify deprecated constructor still works

```java
@Test
public void testDeprecatedConstructor() {
    IContentOwner mockPage = mock(IContentOwner.class);
    when(mockPage.getEntityService()).thenReturn(activityService);
    when(mockPage.getNotificationService()).thenReturn(notificationService);
    
    CEnhancedBinder<CActivity> binder = new CEnhancedBinder<>(CActivity.class);
    CCrudToolbar toolbar = new CCrudToolbar(mockPage, binder);
    
    assertNotNull(toolbar);
    // Verify auto-configuration from IContentOwner
}
```

## Integration Testing

### Test 6: Simple Entity Page Integration
**Test Page**: Activities, Projects, Users, Meetings

**Steps**:
1. Navigate to Activities page
2. Verify toolbar is displayed
3. Click "New" button - should create new entity with defaults
4. Fill in required fields
5. Click "Save" - should save successfully
6. Select an existing entity
7. Modify fields
8. Click "Save" - should update successfully
9. Click "Delete" - should show confirmation
10. Confirm delete - should delete successfully
11. Click "Refresh" - should reload data

**Expected**: All operations work correctly with proper notifications

### Test 7: Dynamic Page Integration
**Test Page**: CDynamicPageViewWithSections

**Steps**:
1. Navigate to a dynamic page with grid and details
2. Select different entities from grid
3. Verify toolbar updates for each entity
4. Test CRUD operations for each entity type
5. Verify status combobox appears for workflow-enabled entities
6. Test status transitions

**Expected**: Toolbar correctly handles different entity types

### Test 8: Status Combobox Behavior
**Test Entities**: CActivity, CMeeting (IHasStatusAndWorkflow)

**Steps**:
1. Create new activity
2. Verify initial status is set
3. Verify status combobox shows valid transitions
4. Change status via combobox
5. Verify workflow validation works
6. Try invalid transition - should show error
7. Verify entity is saved with new status

**Expected**: Status combobox works correctly with workflow validation

## Manual UI Testing

### Test 9: Visual Verification
**Purpose**: Verify UI appears correctly

**Pages to Test**:
- Activities Management (/cdynamicpagerouter/page:3)
- Meetings Management (/cdynamicpagerouter/page:4)
- Projects Management (/cdynamicpagerouter/page:1)
- Users Management (/cdynamicpagerouter/page:12)

**Screenshots Required**:
For each page, capture:
1. Initial page load (toolbar visible)
2. New entity created (form populated)
3. Status combobox populated (for workflow entities)
4. Save success notification
5. Delete confirmation dialog
6. After delete (grid updated)

### Test 10: Button State Verification
**Purpose**: Verify buttons enable/disable correctly

**Test Scenarios**:
1. **No entity selected**: 
   - Create: Enabled (if supplier set)
   - Save: Disabled
   - Delete: Disabled
   - Refresh: Enabled (if callback set)

2. **New entity (no ID)**:
   - Create: Enabled
   - Save: Enabled
   - Delete: Disabled (no ID yet)
   - Refresh: Enabled

3. **Existing entity selected**:
   - Create: Enabled
   - Save: Enabled
   - Delete: Enabled
   - Refresh: Enabled

4. **Entity with dependencies**:
   - Delete: Enabled but shows error when clicked

### Test 11: Notification Testing
**Purpose**: Verify notifications display correctly

**Operations to Test**:
1. Create new entity - should show success
2. Save entity - should show success
3. Delete entity - should show success
4. Try to delete entity with dependencies - should show error
5. Try to save invalid entity - should show validation error
6. Invalid workflow transition - should show error
7. Optimistic locking conflict - should show specific error

### Test 12: Error Handling
**Purpose**: Verify error scenarios are handled gracefully

**Scenarios**:
1. Save with validation errors
2. Delete entity with dependencies
3. Invalid workflow transition
4. Network/service errors
5. Optimistic locking conflicts
6. Missing required configuration

## Performance Testing

### Test 13: Reconfiguration Performance
**Purpose**: Verify reconfiguration doesn't cause lag

**Steps**:
1. Create page with toolbar
2. Rapidly switch between entity types (10 times)
3. Measure time for each reconfiguration
4. Verify UI remains responsive

**Expected**: Each reconfiguration < 100ms

### Test 14: Memory Leak Testing
**Purpose**: Verify no memory leaks from reconfiguration

**Steps**:
1. Create toolbar
2. Reconfigure for different entity types 100 times
3. Force garbage collection
4. Check memory usage

**Expected**: No significant memory increase

## Regression Testing

### Test 15: Existing Functionality
**Purpose**: Verify nothing broke

**Pages to Test**:
- All entity management pages
- All dynamic pages
- Pages with custom toolbar configuration

**Operations**:
- All CRUD operations
- All workflow transitions
- All custom buttons
- All validation scenarios

**Expected**: Everything works as before

## Playwright Automation Tests

### Test 16: Automated CRUD Flow
Create Playwright test to automate CRUD operations:

```javascript
test('CCrudToolbar CRUD operations', async ({ page }) => {
  // Navigate to activities page
  await page.goto('/cdynamicpagerouter/page:3');
  
  // Wait for toolbar
  await page.waitForSelector('.crud-toolbar');
  
  // Click New
  await page.click('text=New');
  
  // Fill form
  await page.fill('[name="name"]', 'Test Activity');
  
  // Click Save
  await page.click('text=Save');
  
  // Verify success notification
  await page.waitForSelector('.notification-success');
  
  // Verify entity in grid
  await page.waitForSelector('text=Test Activity');
  
  // Click Delete
  await page.click('text=Delete');
  
  // Confirm deletion
  await page.click('text=Confirm');
  
  // Verify success notification
  await page.waitForSelector('.notification-success');
});
```

## Test Checklist

### Before Testing
- [ ] Code formatting applied (mvn spotless:apply)
- [ ] Code compiles without errors
- [ ] All warnings addressed
- [ ] Documentation updated

### During Testing
- [ ] Test 1-5: Unit tests pass
- [ ] Test 6-8: Integration tests pass
- [ ] Test 9-12: Manual UI tests complete with screenshots
- [ ] Test 13-14: Performance tests pass
- [ ] Test 15: Regression tests pass
- [ ] Test 16: Playwright tests pass

### After Testing
- [ ] All tests documented
- [ ] Screenshots captured for UI changes
- [ ] Issues logged for any failures
- [ ] Performance metrics recorded
- [ ] Regression issues addressed

## Test Environment

### Setup
```bash
# Start application with H2 database
mvn spring-boot:run -Dspring.profiles.active=h2

# In separate terminal, run Playwright tests
./run-playwright-tests.sh mock
```

### Test Data
- Use DB Minimal sample data
- Create test entities for each type
- Ensure workflow configurations exist
- Ensure status types are configured

## Success Criteria

All tests must pass with:
- ✅ No compilation errors
- ✅ No runtime exceptions
- ✅ All CRUD operations working
- ✅ Status combobox working for workflow entities
- ✅ Proper notifications displayed
- ✅ Buttons enable/disable correctly
- ✅ Entity type reconfiguration works
- ✅ No memory leaks
- ✅ Acceptable performance (< 100ms reconfiguration)
- ✅ All screenshots captured
- ✅ No regression issues

## Known Limitations

1. **Dependency Resolution**: Maven may have issues with some repositories
2. **Build Time**: First build takes 12-15 seconds
3. **Test Environment**: Requires H2 profile for testing

## Troubleshooting

### Issue: Buttons stay disabled
**Solution**: Ensure entity service and supplier are set

### Issue: Status combobox not appearing
**Solution**: Verify entity implements IHasStatusAndWorkflow

### Issue: Reconfiguration not working
**Solution**: Check entity class and service are both set

### Issue: Notifications not showing
**Solution**: Ensure notification service is configured

## Next Steps

After all tests pass:
1. Capture screenshots of all major entity pages
2. Document any issues found
3. Create PR with test results
4. Update documentation with findings
5. Plan for production deployment
