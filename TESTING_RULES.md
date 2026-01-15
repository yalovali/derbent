# Official Testing Rules and Patterns
**Version:** 1.0  
**Date:** 2026-01-16  
**Status:** MANDATORY - All tests must follow these rules

## 🎯 Core Testing Principles

### 1. Browser Visibility - MANDATORY
```bash
# ✅ CORRECT - Browser ALWAYS visible by default
PLAYWRIGHT_HEADLESS=false ./run-comprehensive-with-logging.sh

# ❌ WRONG - Don't run headless during development
PLAYWRIGHT_HEADLESS=true ./run-playwright-tests.sh
```

**Rule:** Browser must be VISIBLE during test development and debugging.  
**Rationale:** Visual feedback is essential for understanding test behavior and debugging failures.  
**Default:** `CBaseUITest.java` line 1427: `playwright.headless` defaults to `false`

### 2. Exception Handling - MANDATORY
```java
// ✅ CORRECT - Always throw exceptions, never ignore
catch (Exception e) {
    LOGGER.error("Error: {}", e.getMessage(), e);  // Log with stack trace
    CNotificationService.showError("Error occurred"); // Notify user
    throw new RuntimeException("Context info", e);   // Throw, don't ignore
}

// ❌ WRONG - Never silently ignore exceptions
catch (Exception e) {
    LOGGER.warn("Error: {}", e.getMessage());  // Only warning
    // No throw - execution continues
}
```

**Rule:** All exceptions must be logged, shown to user, and thrown.  
**Rationale:** Silent failures mask problems and make debugging impossible.  
**Implementation:** See `CFormBuilder.java` lines 1135-1240

### 3. Fail-Fast on Errors - MANDATORY
```bash
# ✅ CORRECT - Stop immediately on exceptions
./scripts/check-test-exceptions.sh mvn test ...

# ❌ WRONG - Continue after errors
mvn test ... || true  # Don't use || true
```

**Rule:** Tests must stop immediately when exceptions occur.  
**Rationale:** Continuing after errors wastes time and produces misleading results.  
**Pattern:** Exception monitoring script detects and stops on: ERROR, Exception, CRITICAL, FATAL

### 4. Live Logging - MANDATORY
```bash
# ✅ CORRECT - Log to shared file accessible during test run
./run-comprehensive-with-logging.sh

# Monitor in another terminal
tail -f target/test-logs/comprehensive-live-*.log
```

**Rule:** All test output must be logged to accessible file during execution.  
**Rationale:** Enables real-time monitoring and debugging.  
**Location:** `target/test-logs/comprehensive-live-TIMESTAMP.log`

## 📋 Comprehensive CRUD Testing Pattern

### Entity Test Checklist (MANDATORY for all entities)

```java
@Test
void testEntity() {
    // 1. NAVIGATE
    navigateToEntityPage("entity-name");
    
    // 2. VERIFY GRID LOADS
    waitForGridLoad();
    
    // 3. TEST CREATE
    clickButton("New");
    fillRequiredFields();
    clickButton("Save");
    verifySuccessNotification();
    verifyEntityAppearsInGrid();
    
    // 4. TEST READ/SELECT
    selectFirstGridRow();
    verifyFormPopulated();
    
    // 5. TEST UPDATE
    clickButton("Edit");
    modifyFields();
    clickButton("Save");
    verifySuccessNotification();
    verifyUpdatesInGrid();
    
    // 6. TEST ATTACHMENTS SECTION (if present)
    testAttachmentsSection();
    
    // 7. TEST COMMENTS SECTION (if present)
    testCommentsSection();
    
    // 8. TEST STATUS WORKFLOWS (if present)
    testStatusTransitions();
    
    // 9. TEST CUSTOM ACTIONS (if present)
    testCustomButtons();
    
    // 10. TEST DELETE (if not protected)
    clickButton("Delete");
    confirmDialog();
    verifyEntityRemovedFromGrid();
    
    // 11. SCREENSHOT AT EACH STEP
    takeScreenshot("entity-operation-step");
}
```

### Attachments Section Testing (MANDATORY if entity supports attachments)

```java
private void testAttachmentsSection() {
    // Navigate to attachments
    clickTabOrSection("Attachments");
    
    // Test upload
    uploadFile("test-document.pdf");
    verifyAttachmentInList("test-document.pdf");
    
    // Test download
    downloadAttachment("test-document.pdf");
    verifyFileDownloaded();
    
    // Test delete
    deleteAttachment("test-document.pdf");
    confirmDialog();
    verifyAttachmentRemoved("test-document.pdf");
    
    takeScreenshot("attachments-section");
}
```

### Comments Section Testing (MANDATORY if entity supports comments)

```java
private void testCommentsSection() {
    // Navigate to comments
    clickTabOrSection("Comments");
    
    // Test add
    fillCommentField("Test comment " + timestamp);
    clickButton("Add Comment");
    verifyCommentInList("Test comment");
    
    // Test edit
    selectComment("Test comment");
    clickButton("Edit");
    fillCommentField("Updated comment");
    clickButton("Save");
    verifyCommentUpdated("Updated comment");
    
    // Test delete
    selectComment("Updated comment");
    clickButton("Delete");
    confirmDialog();
    verifyCommentRemoved("Updated comment");
    
    takeScreenshot("comments-section");
}
```

## 🆕 New Entities - Special Testing Focus

### Entities Added This Week (MANDATORY DEEP TESTING)

**Financial Entities:**
1. Budget (`/cdynamicpagerouter/budgets`)
2. Budget Types (`/cdynamicpagerouter/budget-types`)
3. Invoices (`/cdynamicpagerouter/invoices`)
4. Invoice Items (`/cdynamicpagerouter/invoice-items`)
5. Payments (`/cdynamicpagerouter/payments`)
6. Orders (`/cdynamicpagerouter/orders`)
7. Currencies (`/cdynamicpagerouter/currencies`)

**Test Management Entities:**
8. Test Cases (`/cdynamicpagerouter/test-cases`)
9. Test Scenarios (`/cdynamicpagerouter/test-scenarios`)
10. Test Runs (`/cdynamicpagerouter/test-runs`)
11. Test Steps (`/cdynamicpagerouter/test-steps`)
12. Test Case Results (`/cdynamicpagerouter/test-case-results`)

**Team/Issue Entities:**
13. Issues (`/cdynamicpagerouter/issues`)
14. Issue Types (`/cdynamicpagerouter/issue-types`)
15. Teams (`/cdynamicpagerouter/teams`)

**Extra Testing Required:**
- ✅ All GUI actions and buttons
- ✅ All form validations
- ✅ All status transitions
- ✅ All relationships (parent-child, many-to-many)
- ✅ Attachments if supported
- ✅ Comments if supported
- ✅ Custom business logic

## 🛠️ Base Classes and Helper Methods

### Test Base Class Hierarchy

```java
CBaseUITest (abstract)
    ├─ setupTestEnvironment()
    ├─ takeScreenshot()
    ├─ navigateToPage()
    ├─ clickButton()
    ├─ fillField()
    ├─ verifyNotification()
    └─ ... 25+ helper methods

CPageTestNewEntities extends CBaseUITest
    ├─ testFinancialEntities()
    ├─ testTestManagementEntities()
    ├─ testTeamIssueEntities()
    ├─ testEntityCrudWithSections()
    ├─ testAttachmentsSection()
    └─ testCommentsSection()

CPageTestAuxillaryComprehensiveTest extends CBaseUITest
    ├─ testAllPages()
    ├─ testCrudOperations()
    └─ testWithSections()
```

### Required Helper Methods (MANDATORY)

Every test class must provide:

```java
// Navigation
protected void navigateToEntityPage(String entityName);
protected void clickTabOrSection(String sectionName);

// Actions
protected void clickButton(String buttonText);
protected void fillField(String fieldId, String value);
protected void selectGridRow(int rowIndex);

// Verifications
protected void verifyNotification(String type, String message);
protected void verifyGridContains(String text);
protected void verifyFormPopulated();

// Screenshots
protected void takeScreenshot(String name);

// Sections
protected void testAttachmentsSection();
protected void testCommentsSection();
protected void testStatusTransitions();
```

## 📊 Test Reporting Format (MANDATORY)

### Log Format
```
INFO  (TestClass.java:line) methodName:📋 Starting test for: entity-name
INFO  (TestClass.java:line) methodName:   ➕ Testing CREATE operation...
INFO  (TestClass.java:line) methodName:   ✅ CREATE successful
INFO  (TestClass.java:line) methodName:   👁️  Testing READ operation...
INFO  (TestClass.java:line) methodName:   ✅ READ successful
INFO  (TestClass.java:line) methodName:   ✏️  Testing UPDATE operation...
INFO  (TestClass.java:line) methodName:   ✅ UPDATE successful
INFO  (TestClass.java:line) methodName:   📎 Testing ATTACHMENTS section...
INFO  (TestClass.java:line) methodName:   ✅ ATTACHMENTS functional
INFO  (TestClass.java:line) methodName:   💬 Testing COMMENTS section...
INFO  (TestClass.java:line) methodName:   ✅ COMMENTS functional
INFO  (TestClass.java:line) methodName:✅ Deep CRUD test completed for: entity-name
```

### Screenshot Naming Convention
```
entity-name-page.png           # Initial page load
entity-name-create-success.png # After successful create
entity-name-read-success.png   # After successful read
entity-name-update-success.png # After successful update
entity-name-attachments.png    # Attachments section
entity-name-comments.png       # Comments section
entity-name-delete-success.png # After successful delete
entity-name-failure.png        # On any failure
```

## 🔁 Repeat Until Success (MANDATORY)

```bash
#!/bin/bash
# Run tests repeatedly until all pass

while true; do
    ./run-comprehensive-with-logging.sh
    
    if [ $? -eq 0 ]; then
        echo "✅ ALL TESTS PASSED!"
        break
    else
        echo "❌ Tests failed, analyzing and fixing..."
        # Analyze logs
        # Fix issues
        # Commit fixes
        echo "🔄 Restarting tests..."
        sleep 5
    fi
done
```

**Rule:** Never give up until all tests pass.  
**Process:**
1. Run tests
2. On failure: Stop, analyze, fix
3. Commit fix
4. Restart from step 1
5. Repeat until success

## ✅ Coding Rules Summary

1. **Browser Visible:** Default to `false` (visible) in `CBaseUITest.java`
2. **Throw Exceptions:** Never silently ignore, always throw
3. **Log Everything:** ERROR level with full stack trace
4. **Notify Users:** Show error dialog via `CNotificationService`
5. **Fail-Fast:** Stop immediately on exceptions
6. **Live Logging:** Log to shared file during test run
7. **Comprehensive CRUD:** Test all operations + attachments + comments
8. **Screenshot Everything:** Capture at each step
9. **Test New Entities:** Deep validation of recent additions
10. **Repeat Until Success:** Don't stop until all tests pass

## 📁 Required Files

```
run-comprehensive-with-logging.sh   ✅ Main test script
run-new-entities-test.sh            ✅ Focused test for new entities
run-playwright-tests.sh             ✅ Base test runner
scripts/check-test-exceptions.sh    ✅ Exception monitoring
CBaseUITest.java                    ✅ Base test class
CPageTestNewEntities.java           ✅ New entities test
TESTING_RULES.md                    ✅ This document
```

## 🎯 Success Criteria

- ✅ Browser visible during all test runs
- ✅ All exceptions logged and thrown
- ✅ Tests stop immediately on errors
- ✅ Live log file accessible during run
- ✅ All CRUD operations tested
- ✅ Attachments sections tested
- ✅ Comments sections tested
- ✅ New entities (15) deeply tested
- ✅ Screenshots captured at each step
- ✅ Tests run repeatedly until all pass

## 📞 Support Commands

```bash
# Run comprehensive test with live logging
./run-comprehensive-with-logging.sh

# Monitor live output
tail -f target/test-logs/comprehensive-live-*.log

# Run focused test on new entities
./run-new-entities-test.sh

# Check test status
ps aux | grep "java.*test"

# View screenshots
ls -lh target/screenshots/

# Check exception log
tail -f /tmp/derbent-test-exceptions.log
```

---

**THESE RULES ARE MANDATORY - ALL TESTS MUST COMPLY**

