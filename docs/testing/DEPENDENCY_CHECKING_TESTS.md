# Dependency Checking Playwright Tests

## Overview

This document describes the Playwright test cases for the Dependency Checking System, which prevents deletion of entities that have dependencies.

## Test Strategy

### Test Levels

1. **Basic Validation** - Verify error messages appear
2. **Functional Validation** - Verify deletion is blocked/allowed correctly
3. **Integration Validation** - Verify across multiple entity types

## Test Cases

### User Dependency Tests

#### TC-USER-001: Last User Protection
**Objective**: Verify that the last user in a company cannot be deleted

**Preconditions**:
- Company exists with exactly one user
- User is logged in

**Steps**:
1. Navigate to Users view
2. Select the only user in the company
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "Cannot delete the last user in the company. At least one user must remain."
- User is not deleted
- Delete confirmation dialog does not appear

#### TC-USER-002: Self-Deletion Prevention
**Objective**: Verify users cannot delete their own account

**Preconditions**:
- User is logged in
- At least 2 users exist in the company

**Steps**:
1. Navigate to Users view
2. Select current logged-in user
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "You cannot delete your own user account while logged in."
- User is not deleted
- Delete confirmation dialog does not appear

#### TC-USER-003: Successful User Deletion
**Objective**: Verify users can be deleted when no dependencies exist

**Preconditions**:
- At least 2 users exist in the company
- Selected user is not the current user

**Steps**:
1. Navigate to Users view
2. Select a user (not current user)
3. Click Delete button
4. Confirm deletion in dialog

**Expected Result**:
- Confirmation dialog appears
- After confirmation, user is deleted
- Success notification appears
- User no longer appears in list

### Company Dependency Tests

#### TC-COMPANY-001: Own Company Protection
**Objective**: Verify users cannot delete their current company

**Preconditions**:
- User is logged into a company
- Multiple companies exist

**Steps**:
1. Navigate to Companies view
2. Select current company
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "You cannot delete your own company. Please switch to another company first."
- Company is not deleted
- Delete confirmation dialog does not appear

#### TC-COMPANY-002: Company with Users
**Objective**: Verify companies with users cannot be deleted

**Preconditions**:
- Company exists with associated users
- Current user is in a different company

**Steps**:
1. Navigate to Companies view
2. Select company with users
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "Cannot delete company. It is associated with X user(s). Please remove all users first."
- X matches actual user count
- Company is not deleted

#### TC-COMPANY-003: Empty Company Deletion
**Objective**: Verify companies without users can be deleted

**Preconditions**:
- Company exists with no associated users
- Current user is in a different company

**Steps**:
1. Navigate to Companies view
2. Select empty company
3. Click Delete button
4. Confirm deletion

**Expected Result**:
- Confirmation dialog appears
- Company is deleted successfully
- Success notification appears

### Type/Status Dependency Tests

#### TC-TYPE-001: Non-Deletable Type
**Objective**: Verify system types cannot be deleted

**Preconditions**:
- Type entity exists with attributeNonDeletable = true

**Steps**:
1. Navigate to Type management view (e.g., Activity Types)
2. Select non-deletable type
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "This [type] is marked as non-deletable and cannot be removed from the system."
- Type is not deleted
- Delete confirmation dialog does not appear

#### TC-TYPE-002: Type in Use
**Objective**: Verify types in use cannot be deleted

**Preconditions**:
- Activity type exists
- At least one activity uses this type

**Steps**:
1. Navigate to Activity Types view
2. Select type that is in use
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "Cannot delete activity type. It is being used by X activit[y|ies]."
- X matches actual usage count
- Type is not deleted
- Message uses correct singular/plural form

#### TC-TYPE-003: Unused Type Deletion
**Objective**: Verify unused types can be deleted

**Preconditions**:
- Activity type exists
- No activities use this type
- Type is not marked as non-deletable

**Steps**:
1. Navigate to Activity Types view
2. Select unused type
3. Click Delete button
4. Confirm deletion

**Expected Result**:
- Confirmation dialog appears
- Type is deleted successfully
- Success notification appears

### Status Dependency Tests

#### TC-STATUS-001: Status in Use
**Objective**: Verify statuses in use cannot be deleted

**Preconditions**:
- Activity status exists
- At least one activity uses this status

**Steps**:
1. Navigate to Activity Status view
2. Select status that is in use
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "Cannot delete activity status. It is being used by X activit[y|ies]."
- Status is not deleted

#### TC-STATUS-002: Unused Status Deletion
**Objective**: Verify unused statuses can be deleted

**Preconditions**:
- Activity status exists
- No activities use this status
- Status is not marked as non-deletable

**Steps**:
1. Navigate to Activity Status view
2. Select unused status
3. Click Delete button
4. Confirm deletion

**Expected Result**:
- Confirmation dialog appears
- Status is deleted successfully

### User Type Dependency Tests

#### TC-USERTYPE-001: User Type in Use
**Objective**: Verify user types in use cannot be deleted

**Preconditions**:
- User type exists
- At least one user has this type

**Steps**:
1. Navigate to User Types view
2. Select type that is in use
3. Click Delete button
4. Observe error notification

**Expected Result**:
- Error message: "Cannot delete user type. It is being used by X user[s]."
- User type is not deleted

## Playwright Test Implementation Example

```java
@Test
public void testLastUserDeletionPrevented() {
    // Login as the only user in a company
    loginAsUser("admin@1");
    
    // Navigate to users view
    navigateToView("Users");
    
    // Try to delete the user
    selectFirstEntityInGrid();
    clickDeleteButton();
    
    // Verify error message appears
    assertErrorNotification(
        "Cannot delete the last user in the company"
    );
    
    // Verify user still exists
    assertEntityExistsInGrid("admin");
    
    // Verify confirmation dialog did not appear
    assertNoConfirmationDialog();
}

@Test
public void testActivityTypeWithUsageDeletion() {
    // Setup: Create activity type and activity using it
    createActivityType("TestType");
    createActivity("TestActivity", "TestType");
    
    // Navigate to activity types
    navigateToView("Activity Types");
    
    // Try to delete the type
    selectEntityByName("TestType");
    clickDeleteButton();
    
    // Verify error with count
    assertErrorNotification(
        "Cannot delete activity type. It is being used by 1 activity."
    );
    
    // Verify type still exists
    assertEntityExistsInGrid("TestType");
}

@Test
public void testUnusedActivityTypeDeletion() {
    // Setup: Create activity type without activities
    createActivityType("UnusedType");
    
    // Navigate to activity types
    navigateToView("Activity Types");
    
    // Delete the type
    selectEntityByName("UnusedType");
    clickDeleteButton();
    
    // Confirm deletion
    confirmDeletionDialog();
    
    // Verify success
    assertSuccessNotification("Entity deleted successfully");
    assertEntityNotInGrid("UnusedType");
}
```

## Test Data Setup

### Required Test Data

1. **Companies**
   - Company with single user
   - Company with multiple users
   - Empty company

2. **Users**
   - Admin user (current user)
   - Regular user in same company
   - User in different company

3. **Activity Types**
   - Non-deletable type
   - Type in use by activities
   - Unused type

4. **Activity Status**
   - Status in use by activities
   - Unused status

## Automation Considerations

### Page Objects Required

1. **CCrudToolbarPage**
   - `clickDeleteButton()`
   - `clickCreateButton()`
   - `clickSaveButton()`

2. **NotificationPage**
   - `getErrorMessage()`
   - `getSuccessMessage()`
   - `assertErrorNotification(String expected)`

3. **ConfirmationDialogPage**
   - `confirmDeletion()`
   - `cancelDeletion()`
   - `isVisible()`

4. **GridPage**
   - `selectEntityByName(String name)`
   - `assertEntityExists(String name)`
   - `getRowCount()`

## Test Execution

### Prerequisites
- Application running with H2 database
- Sample data loaded
- Playwright dependencies installed

### Run Commands

```bash
# Run all dependency checking tests
./run-playwright-tests.sh mock

# Run specific test class
mvn test -Dtest=DependencyCheckingTest

# Run with visible browser
./run-playwright-visible-postgres.sh
```

## Expected Results Summary

| Test Case | Expected Behavior |
|-----------|------------------|
| Last user deletion | Blocked with error |
| Self deletion | Blocked with error |
| Other user deletion | Allowed with confirmation |
| Own company deletion | Blocked with error |
| Company with users | Blocked with error |
| Empty company deletion | Allowed with confirmation |
| Non-deletable type | Blocked with error |
| Type in use | Blocked with error |
| Unused type deletion | Allowed with confirmation |

## Troubleshooting

### Common Issues

1. **Test fails with "Element not found"**
   - Verify view navigation is complete
   - Check entity exists in grid
   - Increase wait times if needed

2. **Error message not appearing**
   - Verify dependency checking is enabled
   - Check service implementation
   - Review browser console for errors

3. **Incorrect usage count**
   - Verify test data setup
   - Check repository count query
   - Review transaction boundaries

## Related Documentation

- `DEPENDENCY_CHECKING_SYSTEM.md` - System architecture
- `PLAYWRIGHT_TEST_GUIDE.md` - General testing guide
- `CCrudToolbar.java` - Component under test
