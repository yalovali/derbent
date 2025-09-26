# User Profile Image Testing - Playwright Implementation

## Overview

This implementation adds comprehensive Playwright tests for user profile image functionality, including clicking, editing, uploading, and CRUD operations using ID and XPath selectors.

## New Test Classes

### 1. CUserProfileImageTest
**File:** `src/test/java/automated_tests/tech/derbent/ui/automation/CUserProfileImageTest.java`

**Features:**
- Tests user profile image click and edit functionality
- Validates profile image CRUD operations (Create, Read, Update, Delete)
- Uses both ID and XPath selectors for robust component identification
- Includes comprehensive selector testing for different strategies

**Key Test Methods:**
- `testUserProfileImageClickAndEdit()` - Tests clicking on profile image and editing
- `testProfileImageCRUDOperations()` - Tests complete CRUD workflow
- `testProfileDialogSelectors()` - Tests various component selectors

### 2. Enhanced CCrudFunctionsTest
**File:** `src/test/java/automated_tests/tech/derbent/ui/automation/CCrudFunctionsTest.java`

**Enhancements:**
- Added user profile access testing to `testUsersCRUD()`
- New `testUserProfileAccess()` method for profile functionality
- New `testProfileImageComponents()` method for component testing

### 3. CCompletePlaywrightTestSuite
**File:** `src/test/java/automated_tests/tech/derbent/ui/automation/CCompletePlaywrightTestSuite.java`

**Features:**
- Comprehensive test suite runner for all Playwright tests
- Includes navigation, CRUD, user profile, grid, and UI component tests
- Tests multiple selector strategies (ID, XPath, class-based)
- Proper fallback behavior when browser is not available

## Component Selectors Used

### ID Selectors
- `#user-menu-item` - User menu button
- `#profile-picture-preview` - Profile picture preview image
- `#profile-picture-upload` - Profile picture upload component

### XPath Selectors
- `//vaadin-button[contains(text(), 'Delete')]` - Delete button
- `//img[contains(@id, 'profile')]` - Profile images
- `//vaadin-upload[contains(@id, 'profile')]` - Upload components
- `//vaadin-menu-bar-button[contains(@class, 'menu-bar-button')]` - Menu buttons

### Class/Tag Selectors
- `vaadin-dialog-overlay` - Dialog containers
- `vaadin-grid` - Data grids
- `text=Edit Profile` - Text-based element selection

## Running the Tests

### Using the Shell Script (Recommended)
```bash
# Run user profile image tests specifically
./run-playwright-tests.sh user-profile

# Run complete test suite including profile tests
./run-playwright-tests.sh complete-suite

# Run all focused tests (includes profile tests)
./run-playwright-tests.sh focused

# Show all available options
./run-playwright-tests.sh help
```

### Using Maven Directly
```bash
# Run user profile image test
mvn test -Dtest=CUserProfileImageTest

# Run enhanced CRUD test (includes profile testing)
mvn test -Dtest=CCrudFunctionsTest

# Run complete test suite
mvn test -Dtest=CCompletePlaywrightTestSuite
```

## Test Behavior

### With Browser Available
- Full Playwright testing with real browser interactions
- Screenshots generated in `target/screenshots/`
- Complete user interface testing including profile image operations

### Without Browser (CI Environment)
- Tests run with graceful fallback
- Infrastructure and class structure verification
- No browser dependencies while still validating test logic

## Key Features Tested

### User Profile Access
1. **Navigation to Profile**
   - Click user menu using ID selector
   - Navigate to "Edit Profile" option
   - Verify profile dialog opens

2. **Profile Image Interactions**
   - Click on profile picture preview
   - Test upload component functionality
   - Verify delete button availability

3. **CRUD Operations**
   - **Create:** Upload new profile image
   - **Read:** Display profile image preview
   - **Update:** Change existing profile image
   - **Delete:** Remove profile image

### Selector Strategy Testing
- Tests multiple ways to find UI components
- Fallback mechanisms when primary selectors fail
- Validation of both ID-based and XPath-based element location

## Screenshots

Tests generate screenshots at key points:
- `user-profile-logged-in.png` - After successful login
- `profile-dialog-opened.png` - When profile dialog opens
- `profile-image-display.png` - Profile image preview
- `profile-upload-clicked.png` - Upload component interaction
- `profile-image-deleted.png` - After deletion attempt

## Integration with Existing Framework

The new tests fully integrate with the existing `CBaseUITest` framework:
- Uses established helper methods (`loginToApplication()`, `clickSave()`, etc.)
- Follows existing screenshot and logging patterns
- Maintains consistency with other Playwright tests
- Extends browser availability checking mechanisms

## Error Handling

- Graceful browser unavailability handling
- Comprehensive logging for debugging
- Screenshot capture on errors
- Fallback selector strategies
- Timeout handling for UI operations

## Future Enhancements

Potential areas for expansion:
1. Image format validation testing
2. File size limit testing  
3. Multiple image upload scenarios
4. Profile image accessibility testing
5. Mobile responsive profile image testing

## Dependencies

The tests use the existing project dependencies:
- Spring Boot Test
- Playwright for Java
- JUnit 5
- SLF4J for logging

No additional dependencies were required for this implementation.