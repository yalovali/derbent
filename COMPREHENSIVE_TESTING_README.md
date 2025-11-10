# Comprehensive Page Testing - Quick Reference

## What This Is

A Playwright test suite that automatically tests ALL pages accessible via CPageTestAuxillary navigation buttons. No hardcoding, fully dynamic.

## Quick Start

```bash
# Run the comprehensive test
./run-comprehensive-test.sh

# Or use Maven directly
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=true
```

## What It Tests

For each navigation button on CPageTestAuxillary:
1. ✅ Navigates to the target page
2. ✅ Checks if page has a grid → runs grid tests
3. ✅ Checks if page has CRUD toolbar → runs CRUD tests
4. ✅ Captures screenshots at key points
5. ✅ Continues even if one page fails

## Check Functions (Generic)

```java
checkGridExists()           // Does page have a grid?
checkGridHasData()          // Does grid contain data?
checkGridIsSortable()       // Can columns be sorted?
checkCrudToolbarExists()    // Does page have CRUD buttons?
checkCrudButtonExists(text) // Does specific button exist?
```

## Test Functions (Conditional)

```java
runGridTests(pageName)         // Test sorting, selection, row count
runCrudToolbarTests(pageName)  // Test New, Edit buttons
testGridSorting(pageName)      // Sort ascending/descending
testGridRowSelection(pageName) // Select first row
testNewButton(pageName)        // Click New, check form, close
testEditButton(pageName)       // Select row, click Edit, close
```

## Output

- **Console**: Detailed progress with emojis (🚀 🎯 ✅ ❌)
- **Screenshots**: `target/screenshots/` with sequential numbering
- **Summary**: Total buttons, pages visited, grids found, CRUD toolbars found

## Key Features

| Feature | Description |
|---------|-------------|
| **Dynamic** | Automatically discovers all buttons, no hardcoding |
| **Generic** | Check functions work with any page type |
| **Fast** | 500ms-2s timeouts, efficient execution |
| **Complete** | Tests EVERY button, no skipping |
| **Safe** | Continues testing even if one page fails |
| **Detailed** | Clear logging and progress indicators |

## How It Works

### 1. Button Discovery
```java
// Finds all buttons with ID prefix
List<ButtonInfo> buttons = discoverNavigationButtons();
// Uses selector: [id^='test-aux-btn-']
```

### 2. Conditional Testing
```java
// For each button's target page:
if (checkGridExists()) {
    runGridTests(pageName);
}
if (checkCrudToolbarExists()) {
    runCrudToolbarTests(pageName);
}
```

### 3. Metadata Support
```html
<!-- CPageTestAuxillary provides -->
<vaadin-button 
  id="test-aux-btn-projects-0"
  data-route="cdynamicpagerouter/page:1"
  data-title="Projects"
  data-button-index="0">
</vaadin-button>
```

## Architecture

```
CPageTestAuxillary
  ↓ (provides buttons with IDs)
CPageTestAuxillaryComprehensiveTest
  ↓ (discovers buttons)
ButtonInfo[] buttons
  ↓ (for each button)
Navigate to page
  ↓
Check page content
  ├─→ Has grid? → runGridTests()
  └─→ Has CRUD? → runCrudToolbarTests()
```

## Files

| File | Purpose |
|------|---------|
| `CPageTestAuxillary.java` | Navigation hub with button metadata |
| `CPageTestAuxillaryComprehensiveTest.java` | Main test suite |
| `run-comprehensive-test.sh` | Convenience script |
| `docs/testing/comprehensive-page-testing.md` | Complete documentation |

## Example Output

```
🚀 Starting CPageTestAuxillary Comprehensive Test
================================================
📝 Step 1: Logging into application...
✅ Login successful - application shell detected
🧭 Step 2: Navigating to CPageTestAuxillary page...
✅ Successfully navigated to CPageTestAuxillary page
🔍 Step 3: Discovering navigation buttons...
📊 Found 25 navigation buttons to test
🧪 Step 4: Testing each navigation button's target page...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎯 Testing button 1/25: Projects
   Route: cdynamicpagerouter/page:1
   Button ID: test-aux-btn-projects-0
🖱️  Clicking button: Projects
🔍 Analyzing page content...
   Grid present: true
   CRUD toolbar present: true
📊 Running grid tests...
   ✓ Grid has data: true
   ✓ Grid is sortable: true
   ✓ Grid row count: 15
   🔄 Testing grid sorting...
      ✓ Sorted ascending
      ✓ Sorted descending
   🖱️  Testing grid row selection...
      ✓ Selected first row
🔧 Running CRUD toolbar tests...
   CRUD Buttons available:
      New: true
      Edit: true
      Delete: true
      Save: false
      Cancel: true
   ➕ Testing New button...
      ✓ Clicked New button
      Dialog/Form appeared: true
      ✓ Closed form via Cancel button
   ✏️  Testing Edit button...
      ✓ Selected row for editing
      ✓ Clicked Edit button
      Edit form appeared: true
      ✓ Closed edit form via Cancel button
✅ Completed testing button 1/25: Projects
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ Test suite completed successfully!
📊 Summary:
   Total buttons tested: 25
   Pages visited: 25
   Pages with grids: 18
   Pages with CRUD toolbars: 15
   Screenshots captured: 156
```

## Adding New Check Functions

```java
/** Check if page has my feature.
 * @return true if feature is present */
private boolean checkMyFeatureExists() {
    try {
        Locator elements = page.locator("my-feature-selector");
        return elements.count() > 0;
    } catch (Exception e) {
        LOGGER.debug("Error checking for my feature: {}", e.getMessage());
        return false;
    }
}
```

## Integration with CI/CD

```yaml
- name: Run Comprehensive Tests
  run: ./run-comprehensive-test.sh

- name: Upload Screenshots
  uses: actions/upload-artifact@v3
  with:
    name: test-screenshots
    path: target/screenshots/
```

## Documentation

- 📖 Full Guide: `docs/testing/comprehensive-page-testing.md`
- 📋 Coding Standards: `docs/architecture/coding-standards.md` (Test Auxiliary Pattern section)
- 🧪 Base Test Class: `CBaseUITest.java`

## Support

For issues or questions:
1. Check `docs/testing/comprehensive-page-testing.md` (Troubleshooting section)
2. Review test logs for detailed error messages
3. Check screenshots in `target/screenshots/`
