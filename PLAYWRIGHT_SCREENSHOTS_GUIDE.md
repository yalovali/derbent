# Playwright Tests and Screenshots Guide

This guide explains how to run Playwright tests and generate screenshots in the Derbent application.

## 🎯 Quick Start

### Option 1: Mock Tests (Recommended for Demo)
```bash
./run-playwright-tests.sh mock
```
This runs mock tests that demonstrate screenshot functionality without browser dependencies.

### Option 2: Real Playwright Tests with Docker (Recommended for CI/CD)
```bash
./run-playwright-tests.sh docker
```
This runs real Playwright tests using Docker with proper browser setup.

### Option 3: Local Playwright Tests
```bash
./run-playwright-tests.sh all
```
This attempts to run real Playwright tests with local browser setup.

## 📸 Screenshot Functionality

### Automatic Screenshot Generation
- Screenshots are automatically saved to `target/screenshots/`
- Each screenshot is timestamped for uniqueness
- Screenshots are taken during key test moments and on failures

### Screenshot Types Generated
1. **View Screenshots** - Each application view (Projects, Users, Activities, Meetings, Decisions)
2. **Workflow Screenshots** - Complete application workflows
3. **Accessibility Screenshots** - Accessibility compliance verification
4. **Error Screenshots** - Captured when tests fail for debugging

### Screenshot Examples
```
target/screenshots/
├── accessibility-projects-1754536885849.png
├── workflow-users-view-1754536768444.png
├── workflow-new-project-form-1754536768647.png
├── workflow-project-form-filled-1754536768686.png
└── mock-activities-1754536885245.png
```

## 🧪 Test Types Available

### Comprehensive Test Suite
- **41+ test methods** across 5 test classes
- **Complete UI coverage** of all application views
- **End-to-end workflows** with screenshot documentation

### Test Categories
1. **Authentication Tests** - Login/logout functionality
2. **Navigation Tests** - Between all views
3. **CRUD Tests** - Create, Read, Update, Delete operations
4. **Grid Tests** - Data grid interactions
5. **Search Tests** - Search functionality
6. **Accessibility Tests** - WCAG compliance
7. **Responsive Tests** - Mobile/tablet/desktop
8. **Workflow Tests** - Complete user journeys

## 🔧 Running Specific Tests

### Individual Test Categories
```bash
./run-playwright-tests.sh accessibility  # Accessibility tests
./run-playwright-tests.sh workflow       # Complete workflow tests
./run-playwright-tests.sh login         # Login/logout tests
./run-playwright-tests.sh crud          # CRUD operation tests
./run-playwright-tests.sh navigation    # Navigation tests
```

### Test Class Specific
```bash
./run-playwright-tests.sh colors        # User color and entry views
```

## 🏃‍♂️ Execution Methods

### Method 1: Mock Tests (Always Works)
- **Pros**: No browser dependencies, always succeeds, demonstrates functionality
- **Cons**: Not real browser testing
- **Use case**: Demo, CI/CD without Docker, quick verification
```bash
./run-playwright-tests.sh mock
```

### Method 2: Docker Tests (Recommended)
- **Pros**: Real browser testing, consistent environment, proper Playwright setup
- **Cons**: Requires Docker
- **Use case**: CI/CD pipelines, comprehensive testing
```bash
./run-playwright-tests.sh docker
```

### Method 3: Local Tests
- **Pros**: Direct execution, no containers
- **Cons**: Browser setup dependencies
- **Use case**: Local development with proper browser setup
```bash
./run-playwright-tests.sh all
```

## 📋 Prerequisites

### For Mock Tests
- Java 17+
- Maven
- No additional dependencies

### For Docker Tests
- Docker installed and running
- Sufficient disk space for Playwright image

### For Local Tests
- Java 17+
- Maven
- System browser (Chromium recommended)
- Display server (Xvfb for headless)

## 🔍 Viewing Results

### Screenshot Location
All screenshots are saved to:
```
target/screenshots/
```

### Test Reports
Maven test reports are available in:
```
target/surefire-reports/
```

### Log Output
Test execution provides detailed logging:
- ✅ Success indicators
- 📸 Screenshot generation confirmation
- ⚠️ Warning messages for skipped tests
- ❌ Error details for failures

## 🛠️ Maintenance Commands

### Clean Up
```bash
./run-playwright-tests.sh clean
```
Removes test artifacts, screenshots, and reports.

### Install Browsers
```bash
./run-playwright-tests.sh install
```
Attempts to install Playwright browsers locally.

## 📊 Example Output

### Successful Mock Test Run
```
🚀 Derbent Playwright UI Test Automation Runner
===============================================
🎭 Running mock Playwright tests with screenshots...
✅ Mock tests completed successfully!
📸 Generated 38 mock screenshots in target/screenshots/
Screenshots created:
target/screenshots/accessibility-projects-1754536885849.png
target/screenshots/workflow-users-view-1754536768444.png
target/screenshots/workflow-new-project-form-1754536768647.png
...
```

### Screenshot Statistics
- **Mock Tests**: Generate 30-40 screenshots demonstrating all features
- **Real Tests**: Generate screenshots based on test execution and failures
- **File Size**: Typically 15-35KB per screenshot
- **Format**: PNG with timestamp

## 🎯 Best Practices

### For Development
1. Use `mock` tests for quick verification
2. Use `docker` tests for comprehensive validation
3. Review screenshots in `target/screenshots/` after each run
4. Clean up regularly with `clean` command

### For CI/CD
1. Prefer Docker-based execution for consistency
2. Archive screenshot artifacts for debugging
3. Set appropriate timeouts for test execution
4. Monitor screenshot generation for test health

### For Debugging
1. Screenshots are automatically taken on test failures
2. Check `target/screenshots/` for debug information
3. Use timestamp in filename to correlate with test logs
4. Review mock screenshots to understand expected UI

## 🚨 Troubleshooting

### Browser Installation Issues
If local tests fail with browser errors:
1. Try the Docker approach: `./run-playwright-tests.sh docker`
2. Use mock tests to verify infrastructure: `./run-playwright-tests.sh mock`
3. Install system browser: `sudo apt-get install chromium-browser`

### No Screenshots Generated
1. Check `target/screenshots/` directory exists
2. Verify write permissions
3. Check test logs for error messages
4. Try mock tests first to verify basic functionality

### Tests Timing Out
1. Increase Maven test timeout settings
2. Use headless mode for faster execution
3. Run individual test categories instead of all tests
4. Check system resources (memory, CPU)

## 🎉 Success Indicators

### What Success Looks Like
- ✅ Tests complete without failures
- 📸 Screenshots are generated in `target/screenshots/`
- 🧪 Test logs show successful navigation and interactions
- 📊 All specified test methods execute

### Screenshot Verification
- Screenshots should show proper UI rendering
- Timestamps should be recent
- File sizes should be reasonable (15-35KB)
- Multiple view types should be represented

This comprehensive setup ensures that Playwright tests can run successfully and generate meaningful screenshots regardless of the environment constraints.