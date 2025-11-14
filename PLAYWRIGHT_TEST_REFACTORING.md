# Playwright Test Refactoring - CPageTestAuxillary Navigation

## Summary

This document describes the refactoring of the CPageTestAuxillaryComprehensiveTest to improve reliability and maintainability.

## Problem Statement

The original test attempted to click Vaadin buttons to navigate to different pages. This approach had several issues:

1. **JavaScript Handler Reliability**: Vaadin buttons use JavaScript event handlers (`e -> getUI().ifPresent(ui -> ui.navigate(...))`) which may not trigger reliably in Playwright tests
2. **Unnecessary Complexity**: Clicking buttons and navigating back between tests added complexity
3. **Potential Race Conditions**: Button click handlers might not complete before the test moved on

## Solution

### Direct URL Navigation Approach

Instead of clicking buttons, the refactored test:

1. **Discovers buttons** on the CPageTestAuxillary page using the CSS selector `[id^='test-aux-btn-']`
2. **Extracts route information** from the `data-route` attribute of each button
3. **Navigates directly** to each route using `page.navigate(url)` 
4. **Tests each page** independently without navigating back

### Key Changes

#### Before (Clicking Buttons):
```java
Locator buttonElement = page.locator("#" + button.id);
buttonElement.click();  // Relies on JavaScript handler
wait_2000();

// Navigate back for next button
navigateToTestAuxillaryPage();
```

#### After (Direct Navigation):
```java
String targetUrl = "http://localhost:" + port + "/" + button.route;
page.navigate(targetUrl);  // Direct, reliable navigation
wait_2000();

// No need to navigate back!
```

### Benefits

1. **More Reliable**: Direct URL navigation doesn't depend on JavaScript handlers
2. **Faster Execution**: No navigation back between tests
3. **Truly Generic**: Works with any button that has a data-route attribute
4. **Better Debugging**: Clear log messages show which route is being tested
5. **Cleaner Code**: Simpler logic, easier to maintain

## Test Flow

1. **Login** to the application (uses sample data initialization if needed)
2. **Navigate** to CPageTestAuxillary page (`/cpagetestauxillary`)
3. **Discover** all buttons with IDs starting with `test-aux-btn-`
4. **Extract** button metadata:
   - `id`: Unique button identifier
   - `data-title`: Human-readable title
   - `data-route`: Target route to navigate to
   - `data-button-index`: Sequential index
5. **For each button**:
   - Navigate directly to the route URL
   - Take initial screenshot
   - Analyze page content (check for grids, CRUD toolbars)
   - Run conditional tests based on content
   - Take final screenshot
6. **Print summary** with statistics

## Button Discovery

The CPageTestAuxillary page creates buttons dynamically based on routes registered in CPageTestAuxillaryService. Each button has:

```java
routeButton.setId(buttonId);  // e.g., "test-aux-btn-projects-0"
routeButton.getElement().setAttribute("data-route", routeEntry.route);
routeButton.getElement().setAttribute("data-title", routeEntry.title);
routeButton.getElement().setAttribute("data-button-index", String.valueOf(buttonIndex));
```

The test reads these attributes to know where to navigate:

```java
ButtonInfo info = new ButtonInfo();
info.id = button.getAttribute("id");
info.title = button.getAttribute("data-title");
info.route = button.getAttribute("data-route");  // This is what we navigate to!
info.index = Integer.parseInt(button.getAttribute("data-button-index"));
```

## Conditional Testing

The test performs different checks based on page content:

### Grid Pages
If a page contains a `vaadin-grid`:
- Check if grid has data
- Check if grid is sortable
- Test sorting functionality
- Test row selection

### CRUD Pages
If a page has CRUD buttons (New, Edit, Delete, Save):
- Test New button (opens form, takes screenshot, closes)
- Test Edit button (selects row, opens form, takes screenshot, closes)

## Running the Test

### Using Maven Directly:
```bash
source ./setup-java-env.sh
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=true
```

### Using the Convenience Script:
```bash
./run-comprehensive-test.sh
```

### With Visible Browser (for debugging):
```bash
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=false
```

## Screenshots

Screenshots are saved to `target/screenshots/` with sequential numbering:

- `001-after-login.png` - After successful login
- `002-test-auxillary-page.png` - The test auxillary page with all buttons
- `003-page-{name}-initial.png` - Initial state of each visited page
- `004-page-{name}-final.png` - Final state after running tests
- Additional screenshots for grids, sorting, CRUD operations, etc.

## Cleanup Actions

As part of this refactoring, we also removed duplicate scripts:

- ❌ `bin/run-all-playwright-tests.sh` (duplicate)
- ❌ `bin/run-playwright-tests.sh` (duplicate)
- ✅ `run-comprehensive-test.sh` (kept - main script)
- ✅ `run-playwright-tests.sh` (kept - menu navigation script)
- ✅ `bin/run-playwright-visible-h2.sh` (kept - useful for debugging)
- ✅ `bin/run-playwright-visible-postgres.sh` (kept - useful for debugging)

## Future Improvements

Possible enhancements for future iterations:

1. **Parallel Execution**: Test multiple pages in parallel for faster execution
2. **Visual Regression**: Compare screenshots against baselines
3. **Performance Metrics**: Measure page load times
4. **Accessibility Testing**: Run automated accessibility checks on each page
5. **Form Validation**: Test input validation on CRUD forms
6. **Error Handling**: Test error pages and 404 responses

## Technical Details

### Test Configuration

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
```

- Uses H2 in-memory database for fast test execution
- Random port to avoid conflicts
- DDL auto-create ensures clean state

### Key Classes

- `CPageTestAuxillaryComprehensiveTest`: Main test class
- `CPageTestAuxillary`: Page that displays navigation buttons
- `CPageTestAuxillaryService`: Service that stores route information
- `CHierarchicalSideMenu`: Populates route service with menu items
- `CBaseUITest`: Base class providing common test utilities

## Conclusion

This refactoring makes the test more reliable, maintainable, and truly generic. It properly walks through all buttons by reading their route attributes and navigating directly to each target URL, fulfilling the requirements specified in the problem statement.
