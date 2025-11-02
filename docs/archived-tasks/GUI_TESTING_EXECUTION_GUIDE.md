# GUI Testing with Screenshots - Execution Guide

## 🎯 Objective
Run Playwright browser automation tests to validate CRUD operations across all main entity screens and capture screenshots for visual verification and documentation.

## ⚠️ Current Environment Limitation

### Build Issue Identified
The GitHub Actions runner environment cannot access required Maven repositories:
- **maven.vaadin.com**: DNS resolution fails ("No address associated with hostname")
- **storedobject.com**: DNS resolution fails
- **Impact**: Cannot download required dependencies (`so-components`, `so-charts`, `so-helper`)

This is a **network configuration issue** in the CI environment, not a code problem.

## ✅ Recommended Solution: Local Execution

### Prerequisites
- Java 17+ installed
- Maven 3.8+ installed  
- Network access to Maven repositories
- Chrome/Chromium browser (installed automatically by Playwright)

### Step-by-Step Execution

#### 1. Clone and Navigate to Repository
```bash
git clone https://github.com/yalovali/derbent.git
cd derbent
```

#### 2. Verify Build
```bash
# Clean and compile the project
mvn clean compile

# This should complete successfully if network access is available
```

#### 3. Run Playwright Tests

**Option A: Run All Tests (Most Comprehensive)**
```bash
./run-playwright-tests.sh all
```

**Option B: Run Specific Test Suites**

```bash
# Type and Status CRUD operations (Activity Types, Meeting Types, Status entities)
./run-playwright-tests.sh status-types

# Comprehensive views (Navigation, grid functionality, form validation)
./run-playwright-tests.sh comprehensive

# Button functionality across all pages
./run-playwright-tests.sh buttons

# Menu navigation
./run-playwright-tests.sh menu

# Company-aware login
./run-playwright-tests.sh login
```

#### 4. View Generated Screenshots

After test execution, screenshots are generated in:
```
target/screenshots/
```

**Expected Screenshots:**
- Login screen captures
- CRUD operation stages (Create, Read, Update, Delete)
- Form validation states
- Grid data views  
- Navigation menu states
- Error/success notifications
- Dialog interactions

#### 5. Review Test Results

Test output will show:
- ✅ Tests that passed with screenshot captures
- ❌ Tests that failed (if any) with error screenshots
- 📸 Total number of screenshots generated

Example output:
```
✅ Type and Status CRUD test completed successfully!
📸 Generated 45 screenshots in target/screenshots/
Screenshots include:
  - activity-type-crud-create.png
  - activity-type-crud-update.png
  - activity-status-initial.png
  - meeting-type-after-save.png
  ...
```

## 📋 Test Coverage

### Primary Test Suites

1. **Type and Status CRUD Tests** (`CTypeStatusCrudTest`)
   - Activity Types CRUD operations
   - Meeting Types CRUD operations  
   - Decision Types CRUD operations
   - Order Types CRUD operations
   - Activity Status (ProjectItemStatus) CRUD operations
   - Approval Status CRUD operations
   - **Screenshots per entity**: ~6-8 (initial, after new, filled, after save, after update, etc.)

2. **Comprehensive Dynamic Views Tests** (`CComprehensiveDynamicViewsTest`)
   - Complete navigation coverage
   - Dynamic page loading validation
   - Grid functionality across views
   - Form validation testing
   - **Screenshots**: 15-20 across different views

3. **Button Functionality Tests** (`CButtonFunctionalityTest`)
   - New button presence and responsiveness
   - Save button functionality after form fills
   - Delete button functionality with data selection
   - Refresh button operations
   - **Screenshots**: 10-15 across multiple pages

4. **Workflow Status CRUD Tests** (`CWorkflowStatusCrudTest`)
   - Workflow entity management
   - Status transition testing
   - **Screenshots**: 8-10

### CRUD Operations Validated

For each entity, tests verify:
- ✅ **Create**: New button click → Form fill → Save → Notification verification → Grid refresh
- ✅ **Read**: Entity selection → Data display in form fields
- ✅ **Update**: Edit existing → Modify fields → Save → Notification verification  
- ✅ **Delete**: Select entity → Delete attempt → Validation (non-deletable entities show error)

### Screenshot Capture Points

Screenshots are automatically captured at:
1. **Initial page load** - Shows view in starting state
2. **After clicking New** - Shows empty form ready for input
3. **After filling form** - Shows completed form before save
4. **After save operation** - Shows success notification and updated grid
5. **After update operation** - Shows modification results
6. **Error states** - Shows validation errors or constraint violations
7. **Delete attempts** - Shows delete confirmation or prevention messages

## 🎨 Screenshot Organization

Screenshots follow naming convention:
```
<entity>-<operation>-<state>.png

Examples:
- activity-type-initial.png
- activity-type-after-new.png  
- activity-type-filled.png
- activity-type-after-save.png
- meeting-type-crud-create.png
- approval-status-delete-error.png
```

## 📊 Expected Deliverables

After running tests locally, you should have:

1. **~50-80 screenshots** documenting:
   - All major entity CRUD operations
   - Navigation flows
   - Form states
   - Success/error notifications
   - Grid interactions

2. **Test execution report** in console showing:
   - Test pass/fail status
   - Screenshot generation confirmation
   - Any validation errors encountered

3. **Visual documentation** of:
   - Complete CRUD workflows
   - UI consistency across entities
   - Error handling and validation
   - User interaction flows

## 🚀 Quick Start Commands

```bash
# Full test suite with all screenshots
./run-playwright-tests.sh all

# View screenshots after completion
ls -lh target/screenshots/
open target/screenshots/  # macOS
xdg-open target/screenshots/  # Linux
explorer target/screenshots/  # Windows
```

## 📝 Notes

- Tests run in **headless mode** by default (no browser window shown)
- To run with **visible browser** for debugging:
  ```bash
  ./run-playwright-tests.sh comprehensive -Dplaywright.headless=false
  ```
- Each test suite takes approximately **2-5 minutes** to complete
- Total execution time for all tests: **10-15 minutes**
- Screenshots are automatically organized by test and scenario

## 🐛 Troubleshooting

### If build fails with dependency errors:
```bash
# Clear Maven cache and retry
rm -rf ~/.m2/repository/org/vaadin/addons/so
mvn clean compile
```

### If tests fail to start browser:
```bash
# Install Playwright browsers manually
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

### If screenshots are not generated:
```bash
# Ensure screenshots directory exists
mkdir -p target/screenshots

# Run with verbose output
./run-playwright-tests.sh status-types -X
```

## 📚 Additional Resources

- **Test Implementation**: `src/test/java/automated_tests/tech/derbent/ui/automation/`
- **Base Test Class**: `CBaseUITest.java` - Contains 25+ helper methods
- **Test Documentation**: `PLAYWRIGHT_TEST_EXECUTION_REPORT.md`
- **Testing Guide**: `TESTING_GUIDE.md`

## 🎯 Alternative: Docker Execution (If Available)

If Docker is available and properly configured:

```bash
# Build Docker image for testing
docker build -f Dockerfile.playwright -t derbent-playwright .

# Run tests in container
docker run -v $(pwd)/target/screenshots:/app/target/screenshots derbent-playwright

# Screenshots will be available in ./target/screenshots/
```

---

**For CI/CD Integration**: To enable automated testing in GitHub Actions, the CI environment needs network access to Maven repositories or a local Maven mirror with cached dependencies.
