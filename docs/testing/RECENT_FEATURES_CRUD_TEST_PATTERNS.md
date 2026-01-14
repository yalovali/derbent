# Recent Features CRUD Test Patterns

## Overview

This document describes the testing patterns used for validating CRUD operations on recent features (Issues, Teams, Attachments, Comments) implemented in the last 3 days.

## Test Execution Date

**Date**: 2026-01-14  
**Test Suite**: CRecentFeaturesCrudTest  
**Coverage**: Issues, Teams, Attachments, Comments

## Testing Pattern

### 1. Base Class: CBaseUITest

All Playwright tests extend `CBaseUITest` which provides:

- **Browser Management**: Chromium browser initialization and cleanup
- **Navigation Helpers**: Methods to navigate to entity pages
- **CRUD Button Helpers**: Methods to click New, Edit, Save, Delete, Refresh buttons
- **Form Field Helpers**: Methods to fill text fields, textareas, comboboxes
- **Grid Interaction**: Methods to select grid rows, verify data
- **Screenshot Capture**: Automatic screenshot capture for debugging
- **Fail-Fast Checks**: Exception dialog detection

### 2. Standard CRUD Test Pattern

Each CRUD test follows this pattern:

```java
@Test
@DisplayName("✅ Entity Name - Complete CRUD Lifecycle")
void testEntityCrudOperations() {
    if (!isBrowserAvailable()) {
        // Skip test if browser not available (CI environment)
        org.junit.jupiter.api.Assumptions.assumeTrue(false);
        return;
    }

    try {
        // 1. LOGIN
        loginToApplication();
        takeScreenshot("login", false);

        // 2. NAVIGATE to entity view
        final boolean navigated = navigateToDynamicPageByEntityType("CEntityName");
        assertTrue(navigated, "Failed to navigate");
        wait_2000();
        takeScreenshot("entity-view", false);

        // 3. VERIFY GRID LOADED
        page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(15000));
        takeScreenshot("grid-loaded", false);

        // 4. TEST CREATE
        clickNew();
        wait_1000();
        fillFirstTextField("Test Entity Name");
        // Fill other fields...
        clickSave();
        wait_2000();
        performFailFastCheck("After entity create");
        takeScreenshot("created", false);

        // 5. TEST READ
        clickRefresh();
        wait_1000();
        final Locator grid = page.locator("vaadin-grid").first();
        assertTrue(grid.isVisible(), "Grid should be visible");
        takeScreenshot("grid-refreshed", false);

        // 6. TEST UPDATE
        clickFirstGridRow();
        wait_1000();
        clickEdit();
        wait_1000();
        fillFirstTextField("Test Entity - UPDATED");
        clickSave();
        wait_2000();
        performFailFastCheck("After entity update");
        takeScreenshot("updated", false);

        // 7. TEST DELETE
        clickFirstGridRow();
        wait_500();
        clickDelete();
        wait_500();
        
        // Confirm deletion dialog
        final Locator confirmYes = page.locator("#cbutton-yes");
        if (confirmYes.count() > 0) {
            confirmYes.first().click();
            wait_1000();
        }
        performFailFastCheck("After entity delete");
        takeScreenshot("deleted", false);

    } catch (final Exception e) {
        LOGGER.error("❌ Test failed", e);
        takeScreenshot("error", true);
        throw new AssertionError("Test failed", e);
    }
}
```

### 3. Key Helper Methods from CBaseUITest

#### Navigation Methods
```java
// Navigate by entity type (preferred for dynamic pages)
protected boolean navigateToDynamicPageByEntityType(String entityType);

// Examples:
navigateToDynamicPageByEntityType("CIssue");    // Issues view
navigateToDynamicPageByEntityType("CTeam");     // Teams view
navigateToDynamicPageByEntityType("CActivity"); // Activities view
```

#### CRUD Button Methods
```java
protected void clickNew();      // Click New button
protected void clickEdit();     // Click Edit button
protected void clickSave();     // Click Save button
protected void clickDelete();   // Click Delete button
protected void clickRefresh();  // Click Refresh button
```

#### Form Field Methods
```java
// Fill first text field (usually name/title)
protected void fillFirstTextField(String value);

// Fill first text area (usually description)
protected void fillFirstTextArea(String value);

// Fill field by ID
protected void fillFieldById(String elementId, String value);

// Fill field by entity class and field name
protected void fillFieldById(Class<?> entityClass, String fieldName, String value);
```

#### Grid Interaction Methods
```java
// Click first row in grid
protected void clickFirstGridRow();

// Verify grid has data
protected boolean verifyGridHasData();

// Apply search filter
protected void applyGridSearchFilter(String query);
```

#### Wait and Screenshot Methods
```java
protected void wait_500();      // Wait 500ms
protected void wait_1000();     // Wait 1 second
protected void wait_2000();     // Wait 2 seconds

// Take screenshot with counter
protected void takeScreenshot(String name, boolean isFailure);

// Fail-fast exception check
protected void performFailFastCheck(String controlPoint);
```

### 4. Attachment Testing Pattern

Attachments require special handling for file upload/download:

```java
@Test
void testAttachmentOperationsOnActivity() {
    // 1. Navigate to entity with attachments
    navigateToDynamicPageByEntityType("CActivity");
    clickFirstGridRow();
    
    // 2. Locate attachments container
    final Locator attachmentsContainer = locateAttachmentsContainer();
    attachmentsContainer.scrollIntoViewIfNeeded();
    
    // 3. TEST UPLOAD
    final Locator uploadButton = locateAttachmentToolbarButton(
        attachmentsContainer, "vaadin:upload");
    uploadButton.click();
    
    // Wait for upload dialog
    final Locator dialog = waitForDialogWithText("Upload File");
    
    // Create temp file
    final Path tempFile = Files.createTempFile("test-", ".txt");
    Files.writeString(tempFile, "Test content");
    
    // Upload file
    dialog.locator("vaadin-upload input[type='file']")
        .setInputFiles(tempFile);
    
    final Locator uploadBtn = dialog.locator("#cbutton-upload");
    waitForButtonEnabled(uploadBtn);
    uploadBtn.click();
    waitForDialogToClose();
    
    // Verify attachment in grid
    final Locator attachmentsGrid = locateAttachmentsGrid(attachmentsContainer);
    waitForGridCellText(attachmentsGrid, tempFile.getFileName().toString());
    
    // 4. TEST DOWNLOAD
    final Locator cell = attachmentsGrid.locator("vaadin-grid-cell-content")
        .filter(new Locator.FilterOptions().setHasText(fileName));
    cell.first().click();
    
    final Locator downloadButton = locateAttachmentToolbarButton(
        attachmentsContainer, "vaadin:download");
    downloadButton.click();
    
    // 5. TEST DELETE
    final Locator deleteButton = locateAttachmentToolbarButton(
        attachmentsContainer, "vaadin:trash");
    deleteButton.click();
    
    // Confirm deletion
    final Locator confirmYes = page.locator("#cbutton-yes");
    if (confirmYes.count() > 0) {
        confirmYes.first().click();
    }
    waitForDialogToClose();
    
    // Verify deleted
    waitForGridCellGone(attachmentsGrid, fileName);
}

// Helper methods
private Locator locateAttachmentsContainer() {
    openAttachmentsSectionIfNeeded(); // Handle tabs/accordions
    return page.locator("#custom-attachments-component").first();
}

private Locator locateAttachmentsGrid(Locator container) {
    return container.locator("vaadin-grid")
        .filter(new Locator.FilterOptions().setHasText("File Name")).first();
}

private Locator locateAttachmentToolbarButton(Locator container, String iconName) {
    return container.locator("vaadin-button")
        .filter(new Locator.FilterOptions()
            .setHas(page.locator("vaadin-icon[icon='" + iconName + "']")))
        .first();
}
```

### 5. Comments Testing Pattern

```java
@Test
void testCommentsOnIssue() {
    // 1. Create test entity
    navigateToDynamicPageByEntityType("CIssue");
    clickNew();
    fillFirstTextField("Issue for Comment Test");
    clickSave();
    clickRefresh();
    
    // 2. Select entity
    clickFirstGridRow();
    
    // 3. Open comments section (tab or accordion)
    openCommentsSectionIfNeeded();
    
    // 4. Locate comments container
    final Locator commentsContainer = locateCommentsContainer();
    commentsContainer.scrollIntoViewIfNeeded();
    
    // 5. Add comment
    final Locator addCommentButton = commentsContainer
        .locator("vaadin-button")
        .filter(new Locator.FilterOptions()
            .setHas(page.locator("vaadin-icon[icon='vaadin:plus']")));
    
    if (addCommentButton.count() > 0) {
        addCommentButton.first().click();
        
        final Locator commentField = page.locator("vaadin-text-area");
        commentField.first().fill("Test comment text");
        
        final Locator saveCommentButton = page.locator("#cbutton-save");
        saveCommentButton.first().click();
        
        performFailFastCheck("After comment add");
    }
}
```

## Test Execution

### Run Tests

```bash
# Run recent features tests
./run-playwright-tests.sh recent-features

# Run with visible browser for debugging
PLAYWRIGHT_HEADLESS=false ./run-playwright-tests.sh recent-features

# Run with slow motion for observation
PLAYWRIGHT_SLOWMO=500 ./run-playwright-tests.sh recent-features

# Run in headless mode (CI/CD)
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true ./run-playwright-tests.sh recent-features
```

### Test Configuration

Environment variables:
- `PLAYWRIGHT_HEADLESS` - Run in headless mode (default: false)
- `PLAYWRIGHT_SKIP_SCREENSHOTS` - Disable screenshots (default: false)
- `PLAYWRIGHT_SLOWMO` - Delay between actions in ms (default: 0)
- `PLAYWRIGHT_VIEWPORT_WIDTH` - Browser width (default: 1920)
- `PLAYWRIGHT_VIEWPORT_HEIGHT` - Browser height (default: 1080)

## Bugs Discovered

### Critical Bug #1: CIssue Database Constraint Violation

**Description**: When initializing sample Issues, a database constraint violation occurs during save operation.

**Error**: 
```
ERROR: could not execute statement [ERROR: duplicate key value violates unique constraint "cissue_ux_project_summary"
Detail: Key (project_id, summary)=(22, Issue-1) already exists.] 
[insert into cissue (...) values (...)]
```

**Impact**: 
- Sample data initialization fails
- Tests cannot complete login process
- All Issue-related tests fail

**Root Cause**: Issue initializer (CIssueInitializerService) attempts to create duplicate issues with same project_id and summary.

**Fix Needed**: 
1. Check if issue with same summary already exists before creating
2. OR: Clear existing issues before initialization
3. OR: Use unique summary values (append timestamp or UUID)

### Bug #2: CFormBuilder Unsupported Field Type

**Description**: CFormBuilder cannot handle Set<CAttachment> fields in entity forms.

**Error**:
```
ERROR: Component field [attachments], unsupported field type [Set] for field [Attachments]
```

**Impact**: 
- Entity detail views with attachments field crash
- Navigation to certain pages causes exceptions

**Root Cause**: CFormBuilder doesn't support java.util.Set collection type.

**Fix Needed**:
1. Add Set support to CFormBuilder.createComponentForField()
2. OR: Use @AMetaData(hidden = true) on attachments field
3. OR: Handle attachments separately (not in main form)

## Test Results Summary

| Test | Status | Duration | Issue |
|------|--------|----------|-------|
| testIssueCrudOperations | ❌ FAILED | 19.27s | DB constraint violation during initialization |
| testTeamCrudOperations | ❌ FAILED | 5.07s | Exception dialog during navigation (attachments field) |
| testAttachmentOperationsOnActivity | ❌ FAILED | 4.90s | Exception dialog during navigation (attachments field) |
| testCommentsOnIssue | ❌ FAILED | 4.86s | Exception dialog during navigation (attachments field) |

**Total Test Time**: 51.30 seconds  
**Failures**: 4/4 tests  
**Root Cause**: Application bugs (not test bugs)

## Recommendations

### 1. Fix Application Bugs First

Before tests can pass, the following bugs must be fixed:
- ✅ **CRITICAL**: Fix Issue initializer duplicate key violation
- ✅ **HIGH**: Add CFormBuilder support for Set<> fields OR hide attachments from form

### 2. Test Rerun After Fixes

After fixing bugs, rerun:
```bash
./run-playwright-tests.sh recent-features
```

### 3. Continuous Integration

Add to CI/CD pipeline:
```bash
# Fast validation (headless, no screenshots)
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true ./run-playwright-tests.sh recent-features
```

### 4. Regular Test Maintenance

- Update tests when UI components change
- Add new tests for new features
- Keep test patterns consistent with CBaseUITest
- Document any custom helpers added

## References

- **Base Test Class**: `src/test/java/automated_tests/tech/derbent/ui/automation/CBaseUITest.java`
- **Recent Features Test**: `src/test/java/automated_tests/tech/derbent/ui/automation/CRecentFeaturesCrudTest.java`
- **Attachment Test**: `src/test/java/automated_tests/tech/derbent/ui/automation/CAttachmentPlaywrightTest.java`
- **Test Script**: `run-playwright-tests.sh`
- **Test Documentation**: `docs/testing/`

## Test Pattern Best Practices

1. **Always extend CBaseUITest** - Never write standalone Playwright tests
2. **Use helper methods** - Don't duplicate Playwright API calls
3. **Follow naming convention** - testEntityNameCrudOperations()
4. **Include all CRUD operations** - Create, Read, Update, Delete
5. **Add fail-fast checks** - Use performFailFastCheck() after operations
6. **Capture screenshots** - Use takeScreenshot() at key points
7. **Handle dialogs** - Always wait for dialogs to open/close
8. **Use descriptive assertions** - Include meaningful error messages
9. **Test in isolation** - Each test should be independent
10. **Clean up after test** - Delete created test data when possible

---

**Last Updated**: 2026-01-14  
**Test Author**: Automated via Copilot  
**Next Review**: After bug fixes applied
