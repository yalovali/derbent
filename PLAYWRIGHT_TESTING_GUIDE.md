# Playwright Testing Guide - Fail-Fast Pattern

## Quick Start

```bash
# Run comprehensive tests with visible browser and live logging
PLAYWRIGHT_HEADLESS=false PLAYWRIGHT_SHOW_CONSOLE=true ./run-playwright-tests.sh comprehensive
```

## Monitor Tests in Real-Time

Open a second terminal and run:

```bash
# Watch live test logs (exception monitoring)
tail -f /tmp/derbent-test-exceptions.log

# Or watch the project log file
tail -f target/test-logs/live-test-run.log
```

## Test Execution Pattern

### 1. Fail-Fast on Exceptions
- Script automatically detects exceptions in logs
- Stops immediately when exception found
- No timeout waiting - instant failure reporting

### 2. Fix and Restart Loop
```bash
# Tests run until exception occurs
# Fix the exception in code
# Re-run tests automatically
# Repeat until all tests pass
```

### 3. Browser Visibility
- Browser is ALWAYS visible with `PLAYWRIGHT_HEADLESS=false`
- Set in script: `HEADLESS_MODE="${PLAYWRIGHT_HEADLESS:-false}"`
- Default is visible mode for debugging

## Current Status

### Fixed Issues
- ✅ Entity name conflict (CValidationExecution)
- ✅ Invalid JPQL with FETCH in Page methods
- ✅ Live logging to target/test-logs/
- ✅ Fail-fast exception detection

### Remaining Issues
- ❌ IValidationCaseResultRepository has invalid queries
- ❌ Multiple repositories need query cleanup
- ❌ Comprehensive test pattern documentation pending

## Testing Strategy for New Entities

### Entities to Test (One Week Old)
- Budget management
- Issue tracking
- Team management  
- Finance (Invoices, Payments)
- Validation Execution (Validation Cases, Validation Sessions, Validation Suites)

### CRUD Testing Pattern
1. Navigate to entity page
2. Click "New" button
3. Fill form and save
4. Verify entity appears in grid
5. Click "Edit" button
6. Modify entity and save
7. Verify changes persist
8. Test "Delete" button (if not protected)
9. **NEW**: Test Attachments section
10. **NEW**: Test Comments section
11. **NEW**: Test any custom actions/buttons

### Attachment/Comment Testing
```java
// Pattern for testing sections
protected void testAttachmentSection(Page page) {
    // Click attachments tab/section
    page.locator("button:has-text('Attachments')").click();
    
    // Verify attachment upload works
    page.locator("input[type='file']").setInputFiles(Paths.get("test-file.pdf"));
    
    // Verify attachment appears in list
    assertThat(page.locator(".attachment-list .attachment-item")).isVisible();
    
    // Test download
    // Test delete
}

protected void testCommentSection(Page page) {
    page.locator("button:has-text('Comments')").click();
    page.locator("textarea[placeholder*='comment']").fill("Test comment");
    page.locator("button:has-text('Add Comment')").click();
    assertThat(page.locator(".comment-list .comment-item")).containsText("Test comment");
}
```

## Base Test Classes Pattern

```java
public abstract class CBaseUITest {
    protected Page page;
    protected Browser browser;
    
    @BeforeEach
    void setup() {
        // Setup browser with fail-fast exception detection
        // Configure visible mode
        // Initialize page
    }
    
    // Helper methods for common operations
    protected void navigateToPage(String menuItem);
    protected void clickButton(String buttonText);
    protected void fillForm(Map<String, String> fieldValues);
    protected void verifyGridContains(String text);
    protected void testCrudOperations(String entityName);
    protected void testAttachmentsSection();
    protected void testCommentsSection();
}

public class CFinancialEntitiesTest extends CBaseUITest {
    @Test
    void testInvoiceCrudWithAttachments() {
        navigateToPage("Invoices");
        testCrudOperations("Invoice");
        testAttachmentsSection();
        testCommentsSection();
    }
}
```

## Exception Detection Rules

### Stop Immediately On:
- ❌ Any `Exception` in logs
- ❌ Any `ERROR` level logs
- ❌ Any `FATAL` logs
- ❌ Dialog with error message appears
- ❌ Spring context initialization failure

### Pattern in check-test-exceptions.sh:
```bash
EXCEPTION_PATTERNS="ERROR|Exception|CRITICAL|FATAL|BindingException|RuntimeException"
```

## Coding Rules for Tests

1. **Always use existing helper methods** - Don't reinvent navigation/verification
2. **Test attachments and comments** - Required for all entities that support them
3. **Use descriptive test names** - `testInvoiceCrudWithAttachmentsAndComments()`
4. **Capture screenshots on failure** - Automatic in base class
5. **Log each test step** - Use logger for debugging
6. **Verify visible elements** - Don't assume, always check `isVisible()`
7. **Test ALL GUI actions** - Every button, every dropdown, every field

## Repository Query Rules (For Developers)

### ❌ NEVER DO THIS:
```java
@Query("SELECT e FROM Entity e LEFT JOIN FETCH e.lazy WHERE ...")
Page<Entity> listByProject(Project p, Pageable pageable);
```

### ✅ DO THIS INSTEAD:
```java
@Query("SELECT e FROM Entity e WHERE ...")
Page<Entity> listByProject(Project p, Pageable pageable);

// For List results, FETCH is okay
@Query("SELECT e FROM Entity e LEFT JOIN FETCH e.lazy WHERE ...")
List<Entity> findAllWithLazy();
```

## Next Steps

1. Fix remaining repository query issues
2. Verify browser visibility in test run
3. Create specialized test classes for new entities
4. Document comprehensive test patterns
5. Create reporting format for test results
6. Add to .github/copilot-instructions.md as default testing rule

## Monitoring Commands

```bash
# Watch live test execution
tail -f /tmp/derbent-test-exceptions.log

# Watch project logs
tail -f target/test-logs/*.log

# Search for exceptions in completed tests
grep -r "Exception" target/test-logs/

# View latest test log
ls -t target/test-logs/*.log | head -1 | xargs cat
```
