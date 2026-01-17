# Testing Navigation Pattern Update

**Date:** 2026-01-17  
**Status:** ✅ COMPLETE

## Summary

Successfully updated all testing patterns to use **CPageTestAuxillary button navigation** instead of side menu navigation, and merged all testing rules into the main coding standards document.

## Changes Made

### 1. Merged Testing Rules into Coding Standards ✅

**File Updated:** `docs/architecture/coding-standards.md`

- Added comprehensive "Testing Standards and Patterns" section
- Merged all content from `TESTING_RULES.md`
- Added 400+ lines of testing guidelines
- Removed duplicate `TESTING_RULES.md` file

**Key Sections Added:**
- Core Testing Principles (browser visibility, logging, navigation)
- Comprehensive CRUD Testing Pattern
- Component-Specific Testing (Kanban, Attachments, Comments)
- Test Helper Methods (mandatory for all test classes)
- Test Reporting Format
- Test Execution Strategy

### 2. Navigation Pattern Change ✅

**Old Pattern (DEPRECATED):**
```java
// ❌ Don't use side menu navigation
navigateToViewByText("Activities");
clickMenuItem("Activities");
```

**New Pattern (MANDATORY):**
```java
// ✅ Use CPageTestAuxillary button navigation
page.navigate("http://localhost:" + port + "/cpagetestauxillary");
wait_500();
page.locator("#test-aux-btn-activities-0").click();
wait_1000();
```

**Rationale:**
- **Deterministic**: Button IDs are stable (`test-aux-btn-{sanitized-title}-{index}`)
- **No Side Menu Issues**: Avoids Vaadin side menu rendering/timing problems
- **Filtering Support**: Can skip passed tests via keyword filtering
- **Metadata Available**: `data-route`, `data-title`, `data-button-index` attributes

### 3. Playwright Logging Standard ✅

**Mandatory Pattern:**
```bash
# All Playwright tests must log to /tmp/playwright.log
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest 2>&1 | tee /tmp/playwright.log

# Monitor in real-time
tail -f /tmp/playwright.log
```

**Benefits:**
- Centralized logging location
- Real-time monitoring capability
- Consistent across all test runs
- Easy debugging and analysis

### 4. Test Filtering Pattern ✅

**Keyword Filtering:**
```bash
# Test only user-related pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=user 2>&1 | tee /tmp/playwright.log

# Test only financial pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=budget 2>&1 | tee /tmp/playwright.log

# Test only test management pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=test 2>&1 | tee /tmp/playwright.log
```

**Button ID Targeting:**
```bash
# Test specific page by button ID
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.targetButtonId=test-aux-btn-activities-0 2>&1 | tee /tmp/playwright.log
```

**Direct Route Targeting:**
```bash
# Test specific route directly
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.targetRoute=cdynamicpagerouter/activities 2>&1 | tee /tmp/playwright.log
```

## Test Results

### User Pages Test ✅

**Command:**
```bash
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=user 2>&1 | tee /tmp/playwright.log
```

**Results:**
- ✅ Total buttons tested: 4
- ✅ Pages visited: 4
- ✅ Pages with grids: 3
- ✅ Pages with CRUD toolbars: 3
- ✅ Screenshots captured: 33
- ✅ Test duration: 88.26 seconds
- ✅ Status: **BUILD SUCCESS**

**Pages Tested:**
1. User Icons ✅
2. User Projects ✅
3. User Roles ✅
4. Users ✅

**CRUD Operations Tested:**
- ✅ Refresh button
- ✅ Create + Save (New entity)
- ✅ Update + Save (Modify existing)
- ✅ Delete (with confirmation)
- ✅ Grid sorting
- ✅ Grid filtering
- ✅ Row selection
- ⚠️ Delete skipped on sample data (safe pattern)

## Updated Files

### Documentation
1. ✅ `docs/architecture/coding-standards.md` - Added 400+ lines of testing standards
2. ❌ `TESTING_RULES.md` - **REMOVED** (merged into coding-standards.md)

### Test Implementation
- ✅ `CPageTestAuxillaryComprehensiveTest.java` - Already follows the pattern
- ✅ `CPageTestAuxillary.java` - Provides stable button IDs
- ✅ `CPageTestAuxillaryService.java` - Dynamic route discovery

## Coding Rules Summary

### MANDATORY Testing Patterns

1. **Browser Visible**: Always run tests with visible browser during development
2. **Log to /tmp/playwright.log**: All output must be logged to this file
3. **Navigate via CPageTestAuxillary**: Use button IDs, not side menu
4. **Filter by Keywords**: Skip passed tests using route/button/keyword filtering
5. **Throw Exceptions**: Never silently ignore errors
6. **Fail-Fast**: Stop immediately on exceptions
7. **Comprehensive CRUD**: Test create, read, update, delete
8. **Test Components**: Kanban, attachments, comments when present
9. **Screenshot Everything**: Capture at each major step
10. **Deterministic IDs**: All buttons/fields have stable IDs

## Benefits Achieved

### 1. Reliability
- ✅ No more side menu timing issues
- ✅ Stable, deterministic button IDs
- ✅ Consistent navigation across all tests

### 2. Efficiency
- ✅ Filter tests by keyword to skip passed pages
- ✅ Target specific pages by button ID or route
- ✅ Real-time log monitoring during test runs

### 3. Maintainability
- ✅ Single source of truth (CPageTestAuxillary)
- ✅ All testing rules in one document (coding-standards.md)
- ✅ Consistent patterns across all tests

### 4. Debugging
- ✅ Centralized logging to /tmp/playwright.log
- ✅ Screenshots at each step
- ✅ Clear, emoji-based log format

## Next Steps

### Recommended Test Runs

```bash
# 1. Test all financial pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=budget 2>&1 | tee /tmp/playwright.log

# 2. Test all activity pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=activity 2>&1 | tee /tmp/playwright.log

# 3. Test all issue pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=issue 2>&1 | tee /tmp/playwright.log

# 4. Test all test management pages
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=test-case 2>&1 | tee /tmp/playwright.log

# 5. Run all pages (comprehensive)
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest 2>&1 | tee /tmp/playwright.log
```

### Test Monitoring

```bash
# In terminal 1: Run tests
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dtest.routeKeyword=budget 2>&1 | tee /tmp/playwright.log

# In terminal 2: Monitor live
tail -f /tmp/playwright.log | grep "INFO"

# Check test results
ls -lh target/screenshots/
```

## Documentation References

- **Main Coding Standards**: `docs/architecture/coding-standards.md` (Testing section added)
- **CPageTestAuxillary Implementation**: `src/main/java/tech/derbent/api/views/CPageTestAuxillary.java`
- **Comprehensive Test**: `src/test/java/automated_tests/tech/derbent/ui/automation/CPageTestAuxillaryComprehensiveTest.java`

## Compliance Checklist

- ✅ Browser visible by default
- ✅ All output logged to `/tmp/playwright.log`
- ✅ Navigation via CPageTestAuxillary buttons
- ✅ Keyword filtering supported
- ✅ Button ID targeting supported
- ✅ Direct route targeting supported
- ✅ Exception handling follows fail-fast pattern
- ✅ CRUD operations tested comprehensively
- ✅ Screenshots captured at each step
- ✅ Deterministic IDs used throughout
- ✅ Testing rules merged into coding standards
- ✅ Duplicate documentation removed

---

**Status**: ✅ All testing patterns updated and documented  
**Test Run**: ✅ User pages tested successfully (4/4 pages passed)  
**Documentation**: ✅ Merged into coding-standards.md  
**Log File**: ✅ Available at /tmp/playwright.log (159K)  
**Screenshots**: ✅ 33 screenshots captured in target/screenshots/
