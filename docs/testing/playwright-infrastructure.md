# Playwright Test Infrastructure Documentation

## Overview

This document describes the comprehensive Playwright test infrastructure implemented for the Derbent application following the strict coding guidelines defined in `copilot-java-strict-coding-rules.md`.

## Test Infrastructure Components

### 1. Base Test Class - CBaseUITest

Enhanced with 25+ auxiliary methods for comprehensive UI testing:

#### Core Navigation Methods
- `loginToApplication()` - Standard login with admin/test123 credentials
- `navigateToViewByText(viewName)` - Navigate using hierarchical menu
- `navigateToViewByClass(viewClass)` - Navigate using @Route annotations

#### ID-Based Component Interaction
- `clickById(id)` - Click elements by ID with timeout and error handling
- `fillById(id, value)` - Fill form fields by ID
- `elementExistsById(id)` - Check element existence
- `waitForElementById(id, timeout)` - Wait for element visibility
- `getTextById(id)` - Retrieve text content by ID

#### Grid Testing Utilities
- `clickGridRowByIndex(index)` - Click specific grid rows
- `getGridRowCount()` - Get total grid row count
- Advanced grid interaction testing with sorting and filtering

#### Form and ComboBox Testing
- `selectComboBoxOptionById(comboBoxId, optionText)` - ComboBox selection
- `testFormValidationById(saveButtonId)` - Validation testing
- `testComboBoxById(comboBoxId, expectedOption)` - ComboBox content testing

#### CRUD Operations Testing
- `testCRUDOperationsInView(viewName, newButtonId, saveButtonId, deleteButtonId)` - Complete CRUD workflow testing

#### Advanced Testing Methods
- `testResponsiveDesign(viewName)` - Multi-viewport testing (mobile, tablet, desktop)
- `testAccessibilityBasics(viewName)` - ARIA attributes and keyboard navigation
- Screenshot capture with automatic naming and timestamping

### 2. View-Specific Test Classes

#### CActivitiesViewPlaywrightTest (8 test methods)
- `testActivitiesViewLoading()` - View loading and grid presence
- `testActivitiesGridInteractions()` - Grid selection and sorting
- `testActivitiesCRUDOperations()` - Complete CRUD workflow
- `testActivitiesFormValidation()` - Form validation scenarios
- `testActivitiesComboBoxes()` - Activity Type and Status ComboBox testing
- `testActivitiesNavigation()` - Cross-view navigation
- `testActivitiesResponsiveDesign()` - Multi-viewport responsive testing
- `testActivitiesCompleteWorkflow()` - End-to-end activity creation workflow

#### CProjectsViewPlaywrightTest (9 test methods)
- Enhanced with grid filtering tests
- Multiple ComboBox testing
- Date picker interactions
- Project-specific workflow validation

#### CMeetingsViewPlaywrightTest (9 test methods)
- Date/time picker specialized testing
- Meeting-specific form validation
- Participant management testing
- Calendar integration testing

#### CUsersViewPlaywrightTest (10 test methods)
- Profile picture upload testing
- Email validation testing
- Role and company ComboBox testing
- User-specific workflow validation

#### CApplicationWorkflowPlaywrightTest (8 test methods)
- `testCompleteApplicationLoginAndNavigation()` - Full application navigation
- `testCrossViewDataConsistency()` - Data consistency across views
- `testApplicationResponsiveness()` - Comprehensive responsive design
- `testApplicationAccessibilityComprehensive()` - Complete accessibility testing
- `testApplicationErrorHandling()` - Error scenario testing
- `testApplicationPerformance()` - Performance measurement
- `testApplicationDataFlow()` - Complete data flow (User → Project → Activity → Meeting)
- `testApplicationLogoutAndSecurity()` - Security and logout testing

### 3. GUI Component ID System

#### Systematic ID Generation
All GUI components now have automatic ID generation using `CAuxillaries.setId()`:

- **CButton**: All buttons get IDs like `cbutton-save`, `cbutton-cancel`
- **CDialog**: Dialogs get IDs like `cdialog-confirmation`
- **Form Components**: All form fields get descriptive IDs
  - TextField: `textfield-name`, `textfield-description`
  - ComboBox: `combobox-activitytype`, `combobox-status`
  - DatePicker: `datepicker-startdate`, `datepicker-duedate`
  - NumberField: `numberfield-budget`, `numberfield-hours`
  - Checkbox: `checkbox-active`, `checkbox-completed`

#### ID Generation Logic
IDs are generated using component class name + descriptive text/purpose:
```java
// Example: CButton with text "Save" → ID: "cbutton-save"
// Example: ComboBox for Activity Type → ID: "combobox-activity-type"
```

## Test Execution Guidelines

### 1. Running Individual View Tests

```bash
# Run Activities view tests
mvn test -Dtest="CActivitiesViewPlaywrightTest" -Dspring.profiles.active=test

# Run Projects view tests  
mvn test -Dtest="CProjectsViewPlaywrightTest" -Dspring.profiles.active=test

# Run specific test method
mvn test -Dtest="CActivitiesViewPlaywrightTest#testActivitiesGridInteractions" -Dspring.profiles.active=test
```

### 2. Running Complete Test Suite

```bash
# Run all Playwright tests
./run-playwright-tests.sh all

# Run specific test category
./run-playwright-tests.sh crud
./run-playwright-tests.sh responsive
./run-playwright-tests.sh accessibility
```

### 3. Browser Configuration

Tests are configured to run in non-headless mode for debugging:
```java
browser = playwright.chromium()
    .launch(new BrowserType.LaunchOptions()
        .setHeadless(false)  // Visible browser for debugging
        .setSlowMo(100));    // Slow motion for visibility
```

## Test Coverage Areas

### 1. Functional Testing
- ✅ CRUD operations for all entities
- ✅ Form validation and error handling
- ✅ Grid interactions (selection, sorting, filtering)
- ✅ ComboBox content verification and selection
- ✅ Navigation between views
- ✅ Cross-view data consistency

### 2. UI/UX Testing
- ✅ Responsive design across 4 viewport sizes
- ✅ Accessibility compliance (ARIA, keyboard navigation)
- ✅ Visual regression testing via screenshots
- ✅ Form layout and field arrangement

### 3. Performance Testing
- ✅ Application load time measurement
- ✅ Login performance testing
- ✅ View navigation timing
- ✅ Form submission performance

### 4. Security Testing
- ✅ Login/logout functionality
- ✅ Protected route access verification
- ✅ Session management testing
- ✅ Authentication state validation

## Following Coding Guidelines

### 1. Strict Adherence to copilot-java-strict-coding-rules.md
- ✅ Always fail all tests with fail assertion initially
- ✅ Generate Playwright tests for all views and functions
- ✅ Create auxiliary functions for simpler commands
- ✅ Always try to use selection by ID not by CSS or tag
- ✅ Insert ID to used components in test in Java
- ✅ Keep tests in non-headless chromium execution
- ✅ Write separate test classes for each view
- ✅ Write short if blocks, quick returns for maintainability
- ✅ Don't have repeating blocks
- ✅ Test against grid selection changes for every page/view
- ✅ Test contents of every ComboBox

### 2. Logging and Documentation
- ✅ Comprehensive logging at DEBUG and INFO levels
- ✅ Screenshot capture for all major test steps
- ✅ Detailed JavaDoc documentation for all test methods
- ✅ Progress logging with emoji indicators for clarity

### 3. Error Handling
- ✅ Graceful handling of missing elements
- ✅ Timeout management for all interactions
- ✅ Comprehensive exception catching and logging
- ✅ Meaningful error messages for debugging

## Test Maintenance

### 1. Adding New Tests
When adding new views or functionality:
1. Create view-specific test class following naming convention
2. Implement all 8-10 standard test methods
3. Add view to CApplicationWorkflowPlaywrightTest
4. Update documentation

### 2. Test Data Management
- Tests use H2 in-memory database for isolation
- Each test creates its own test data with timestamps
- No dependencies between tests
- Clean database state for each test class

### 3. Screenshot Management
Screenshots are automatically saved to `target/screenshots/` with descriptive names:
- `{view-name}-{test-step}-{timestamp}.png`
- Organized by test method and functionality
- Useful for visual debugging and regression testing

## Benefits of This Infrastructure

1. **Comprehensive Coverage**: Tests cover all major application functionality
2. **Maintainable**: Auxiliary functions reduce code duplication
3. **Debuggable**: Non-headless execution with screenshots for debugging
4. **Scalable**: Easy to add new tests following established patterns
5. **Compliant**: Follows all project coding guidelines strictly
6. **Automated**: Can be integrated into CI/CD pipelines
7. **Visual**: Screenshot capture provides visual test verification
8. **Performance-Aware**: Includes performance measurement capabilities

This infrastructure provides a solid foundation for comprehensive UI testing of the Derbent application while maintaining high code quality standards.