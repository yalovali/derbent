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

## Selective Route Testing (CPageTestAuxillary)

Use the `test.routeKeyword` system property whenever you only want the comprehensive suite to open a specific view. The filter checks both the
button title and the route, so any substring match (case-insensitive) will be executed. This keeps the BAB Dashboard verification loop under a minute
because only the BAB targets are opened while every section-level component test still runs.

```bash
# Example: Run the comprehensive test for the BAB Dashboard only
SPRING_PROFILES_ACTIVE="test,bab" PLAYWRIGHT_SCHEMA="BAB Gateway" \
MAVEN_OPTS="-Dtest.routeKeyword=bab dashboard" ./run-playwright-tests.sh comprehensive
```

- `SPRING_PROFILES_ACTIVE` switches the application into the BAB schema so the dashboard view is registered.
- `PLAYWRIGHT_SCHEMA` ensures the login flow selects the BAB Gateway company for seed data.
- The filter also accepts toolbar captions: `MAVEN_OPTS="-Dtest.routeKeyword=interface list"` will open the section that exposes
  `CComponentInterfaceList`.
- If you provide both `test.routeKeyword` and `test.targetRoute`, the route takes precedence and only that view is visited.

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

### BAB Dashboard Interface Component Coverage
- The new `CBabInterfaceListComponentTester` exercises the interface list widget (CComponentInterfaceList) that appears in BAB dashboard project
  sections.
- `CPageTestAuxillaryComprehensiveTest` automatically detects the widget by ID (`custom-interfaces-component`) while walking each section on the
  page. When present it verifies the header, toolbar, refresh action, and grid columns before checking for data.
- `CBabInterfaceListPlaywrightTest` now reuses the same tester so the standalone dashboard test and the comprehensive suite share the identical
  assertions. That removes the risk of the two tests drifting apart.
- Calimero connectivity is optional. The tester fails if the structure is missing, but it only logs warnings when the grid is empty because the
  Calimero server is offline.
- To focus on the BAB dashboard only, combine the route filter with the schema flag:

```bash
SPRING_PROFILES_ACTIVE="test,bab" PLAYWRIGHT_SCHEMA="BAB Gateway" \
MAVEN_OPTS="-Dtest.routeKeyword=interface" ./run-playwright-tests.sh comprehensive
```

### BAB Calimero Service Guard
- The BAB profile now exposes a Calimero control card on `CSystemSettingsView_Bab` with a status chip (`#calimero-status-indicator`) and a restart
  button (`#cbutton-calimero-restart`).
- All BAB Playwright suites (base UI tests and `CBabInterfaceListPlaywrightTest`) automatically navigate to `/csystemsettingsview` after login,
  inspect the status chip, and click the restart button when the service is stopped. The chip exposes `data-running`/`data-enabled` attributes so the
  tests can wait deterministically for the service to reach the "running" state.
- Tests must keep this guard step because the dashboard widgets read live Calimero data. If the process is not running, component checks would report
  false negatives.
- When running manually, make sure the `calimeroExecutablePath` is valid before pressing the restart button; the UI will surface descriptive errors if
  the binary cannot be launched.
- The system settings view now auto-restarts the Calimero binary after each database reset (both "Reset DB Full" and "Reset DB Min") so manual reset
  workflows stay aligned with the Playwright expectations.

#### Startup Sequence (BAB vs. PLM)
BAB test runs **must** insert an extra hop after the login flow compared to PLM tests:

1. **Reset DB on the login screen** (`cbutton-db-full`/`cbutton-db-min`) and wait for the progress dialog + info dialog to finish, just like PLM.
2. **Log in with the BAB schema/company**. After the shell loads, immediately navigate to `/csystemsettingsview`.
3. **Locate the Calimero start/restart button** (`#cbutton-calimero-restart`, caption “Restart Calimero”). Click it after every database reset even if the chip already says “running”; this keeps the background binary synchronized with the freshly reloaded data.
4. **Verify the status chip** (`#calimero-status-indicator`) flips to `data-running="true"` and shows the green “running” message. If it stays stopped, capture the toast/error and stop the test run.
5. **Return to the original page** (the test harness records `page.url()` before the guard runs) and resume the rest of the scenario.

PLM suites skip step 2–4 entirely, so this BAB-only guard is the point where the two profiles diverge.

#### BAB Dashboard Component Walkthrough
- After the Calimero guard passes, continue with the standard `CPageTestAuxillaryComprehensiveTest` flow but focus the keyword filter on the BAB dashboard (for example `-Dtest.routeKeyword="bab dashboard"`).
- The dashboard verification now requires enumerating **every** widget on the view. The base tester walks cards, grids, tabs, and KPIs exactly like PLM but keeps a BAB-specific extra pass for `#custom-interfaces-component` to ensure the Calimero-backed widget is rendered.
- The `CBabInterfaceListComponentTester` performs the detailed inspection. Manual test sessions should mirror its sequence:
  1. Confirm the Interface List tab/accordion is visible and can be expanded.
  2. Validate the header text mentions “Interface”.
  3. Locate the toolbar (`#custom-interfaces-toolbar`) and press the Refresh button (ID `custom-interfaces-refresh-button` or any refresh icon).
  4. Verify the grid renders all expected columns (`Name`, `Type`, `Status`, `MAC`, `MTU`, `DHCP`) and logs warnings if any are missing.
  5. Check the grid populates data when Calimero is running; if the server is offline the test records a warning instead of failing.
- Treat this walkthrough as mandatory whenever a BAB dashboard test case runs—the goal is to “detect all components on the view” before completing the test.

### Tab-Walk Component Architecture (Testing Pattern)
- `CPageTestAuxillaryComprehensiveTest` now treats every VAADIN tab, tabsheet, or accordion as a **required stop**. Each tab is activated sequentially with a 1-second delay so we can visually confirm the content before the next step runs.
- The base tester exposes a switch (`-Dtest.enableComponentTests=true`) that toggles component detection while tab walking:
  - When disabled (default) the suite simply walks the tabs and logs “↳ Viewing tab …”.
  - When enabled, the tester runs the registered component testers on each tab view. Component detection is profile-aware, so BAB-only testers (for example `CBabInterfaceListComponentTester`) only run when those components are present, while Derbent/PLM testers (attachments, comments, links) still run everywhere else.
- Component registers live inside `CPageTestAuxillaryComprehensiveTest.runComponentTestsOnCurrentView`. Add new testers there, and they will automatically participate in the tab walk architecture for both Derbent and BAB profiles.
- **Never run component testers outside the tab walk sequence**. The architecture guarantees every tab is restored to its neutral state before moving to the next test, preventing stale dialogs or side-effects from leaking into other tabs.

### Route Keyword Enforcement
- `test.routeKeyword` now performs an **exact, case-insensitive match** against both the button title and the destination route. This prevents similarly named views from being pulled into a filtered run.
- When you want to test a single page, pass the full button title or route, for example:

```bash
MAVEN_OPTS='-Dtest.routeKeyword="BAB Setup"' \
SPRING_PROFILES_ACTIVE="test,bab" PLAYWRIGHT_SCHEMA="BAB Gateway" \
./run-playwright-tests.sh comprehensive
```

- By default only the first exact match runs. Set `-Dtest.runAllMatches=true` if you intentionally want to iterate over multiple buttons with the same title.
- This filtering is mandatory for high-cost suites (like BAB views). Without the keyword the suite reverts to standard Derbent coverage and still takes effect on every button registered in `CPageTestAuxillary`.

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
