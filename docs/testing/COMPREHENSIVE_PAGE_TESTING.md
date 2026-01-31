# Comprehensive Page Testing Framework

**Test Class**: `CPageTestComprehensive`  
**Status**: Active (as of 2026-01-31)  
**Replaces**: CPageTestComprehensive (deprecated), CPageTestComprehensive (deprecated)

## Overview

The Comprehensive Page Testing Framework provides **unified, automated testing** for all pages in the Derbent application. It automatically discovers page features and tests them without hardcoding.

### Key Features

✅ **Zero Hardcoding** - Works with ANY page structure  
✅ **Automatic Discovery** - Finds tabs, components, CRUD buttons, grids  
✅ **Intelligent Testing** - Different strategies per component type  
✅ **Comprehensive Coverage** - All features tested in one run  
✅ **Fail-Fast Detection** - Immediate failure on exceptions  
✅ **Detailed Reporting** - CSV + Markdown coverage reports

---

## Quick Start

### Basic Usage

```bash
# Test all pages (comprehensive)
mvn test -Dtest=CPageTestComprehensive

# Test pages matching keyword
mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=activity

# Test specific page by button ID
mvn test -Dtest=CPageTestComprehensive -Dtest.targetButtonId=test-aux-btn-activities-0

# Show browser (visible mode)
PLAYWRIGHT_HEADLESS=false mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=user
```

### Using the Script

```bash
# Quick menu navigation
./run-playwright-tests.sh menu

# Test activity pages
./run-playwright-tests.sh activity

# Test all pages
./run-playwright-tests.sh comprehensive

# Interactive configuration
INTERACTIVE_MODE=true ./run-playwright-tests.sh user
```

---

## What Gets Tested

The framework automatically tests these aspects for each page:

### 1. Navigation ✅
- Navigates to page via CPageTestAuxillary button
- Verifies page loads successfully

### 2. Tab/Accordion Walking ✅
- Detects all tabs and accordions
- Clicks through each tab automatically
- Tests components in EACH tab independently

### 3. Component Detection ✅
Automatically detects and tests:
- CRUD Toolbar (New, Save, Delete, Refresh buttons)
- Grid component
- Attachment component
- Comment component
- Link component
- Clone button
- Report button
- Custom components (extensible)

### 4. CRUD Operations ✅
When CRUD toolbar detected:
- **New Button**: Creates entity, opens form
- **Save Button**: Persists entity, validates fields
- **Delete Button**: Removes entity, confirms deletion
- **Grid Validation**: Verifies count changes (1 → 2 → 1)

### 5. Grid Operations ✅
When grid detected:
- **Structure**: Validates rows present
- **Selection**: Tests row selection and details panel
- **Sorting**: Tests column sorting (if applicable)
- **Filtering**: Tests filter row (if applicable)
- **Pagination**: Tests page navigation (if applicable)

### 6. Coverage Reports ✅
Generates after each run:
- **CSV Report**: Machine-readable metrics (15+ fields)
- **Markdown Report**: Human-readable summary with statistics

---

## Test Parameters

### Filtering Options

| Parameter | Description | Example |
|-----------|-------------|---------|
| `-Dtest.routeKeyword=<keyword>` | Filter pages by keyword | `-Dtest.routeKeyword=activity` |
| `-Dtest.targetButtonId=<id>` | Test specific button | `-Dtest.targetButtonId=test-aux-btn-activities-0` |
| (no filter) | Test ALL pages | `mvn test -Dtest=CPageTestComprehensive` |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PLAYWRIGHT_HEADLESS` | `false` | Show browser (`false`) or run headless (`true`) |
| `PLAYWRIGHT_SHOW_CONSOLE` | `true` | Show console output |
| `PLAYWRIGHT_SKIP_SCREENSHOTS` | `false` | Disable screenshots for faster execution |
| `PLAYWRIGHT_SLOWMO` | `0` | Delay between actions in ms (for debugging) |
| `PLAYWRIGHT_VIEWPORT_WIDTH` | `1920` | Browser viewport width |
| `PLAYWRIGHT_VIEWPORT_HEIGHT` | `1080` | Browser viewport height |
| `INTERACTIVE_MODE` | `false` | Show configuration menu before test |

---

## Common Use Cases

### Test Single Entity Type

```bash
# Activities
./run-playwright-tests.sh activity

# Users
./run-playwright-tests.sh user

# Meetings
./run-playwright-tests.sh meeting

# Storage
./run-playwright-tests.sh storage
```

### Debug Specific Page

```bash
# Visible browser + slow motion
PLAYWRIGHT_HEADLESS=false PLAYWRIGHT_SLOWMO=500 \
  mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=activity
```

### Fast CI/CD Testing

```bash
# Headless + no screenshots
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true \
  mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=activity
```

### Complete Coverage Report

```bash
# Test all pages and generate reports
./run-playwright-tests.sh comprehensive

# Reports saved to:
# - test-results/playwright/coverage/test-coverage-*.csv
# - test-results/playwright/coverage/test-summary-*.md
```

---

## Understanding Output

### Console Output

```
🎯 Comprehensive Page Testing Framework
Testing pages: 3 buttons found matching 'activity'

📑 Testing page: Activities (1/3)
   🎯 Navigating to page...
   ✅ Page loaded: Activities
   
   📑 Walking tabs/accordions...
   ✅ Tabs detected: 2
   
   🧩 Detecting components...
   ✅ Components detected: 6 types
      - CRUD Toolbar
      - CRUD Save Button
      - CRUD Delete Button
      - Clone Button
      - Grid
      - Report Button
   
   🔧 Testing CRUD operations...
      ✅ New button works
      ✅ Save button works (grid: 1 → 2 rows)
      ✅ Delete button works (grid: 2 → 1 rows)
   
   📊 Testing grid operations...
      ✅ Grid structure valid (40 rows)
      ✅ Row selection works
   
   ✅ Page test complete: Activities

📊 Generating coverage reports...
   ✅ CSV report: test-results/playwright/coverage/test-coverage-*.csv
   ✅ Markdown report: test-results/playwright/coverage/test-summary-*.md

✅ Tests run: 1, Failures: 0, Errors: 0
```

### Coverage Report (CSV)

```csv
Page Name,Route,Button ID,Status,Duration,Has Components,Component Count,Component Types,Has CRUD,Tested CRUD,Has Grid,Tested Grid,Grid Rows,Has Tabs,Tab Count,Error Message
Activities,cdynamicpagerouter/page:5,test-aux-btn-activities-0,PASS,0m 35s,true,6,CRUD Toolbar; Grid; Clone Button,true,true,true,true,40,true,2,
```

### Coverage Report (Markdown)

```markdown
# Comprehensive Page Test Report

**Generated**: 2026-01-31 20:30:00

## Summary Statistics
- Total Pages Tested: 3
- Passed: ✅ 3 (100%)
- Total Duration: 2m 15s

## Feature Coverage
- Components Detected: 3 (100%)
- CRUD Toolbars: 3 (100%)
- Grids: 3 (100%)
- Tabs/Accordions: 3 (100%)

## Test Results
| Page | Status | Duration | Components | CRUD | Grid | Tabs |
|------|--------|----------|------------|------|------|------|
| Activities | ✅ PASS | 0m 35s | ✓ (6) | ✓ | ✓ (40 rows) | ✓ (2) |
```

---

## Adding New Component Testers

The framework is extensible. To add testing for a new component type:

### 1. Create Component Tester Class

```java
public class CMyComponentTester extends CBaseComponentTester {
    
    private static final String COMPONENT_SELECTOR = "#my-component-id";
    
    @Override
    public boolean canTest(final Page page) {
        return elementExists(page, COMPONENT_SELECTOR);
    }
    
    @Override
    public String getComponentName() {
        return "My Custom Component";
    }
    
    @Override
    public void test(final Page page) {
        LOGGER.info("      🧪 Testing My Component...");
        // Your test logic here
        LOGGER.info("      ✅ My Component test complete");
    }
}
```

### 2. Register in CPageTestComprehensive

Add to `initializeControlSignatures()`:

```java
protected void initializeControlSignatures() {
    controlSignatures = new ArrayList<>();
    // ... existing signatures ...
    controlSignatures.add(new CMyComponentTester());  // Add your tester
}
```

That's it! The framework will automatically:
- Detect your component on pages
- Call your tester when found
- Include it in coverage reports

---

## Troubleshooting

### Browser Won't Open

```bash
# Install Playwright browsers
./run-playwright-tests.sh install

# Or manually
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

### Tests Timing Out

```bash
# Increase test timeout (default: 300s)
mvn test -Dtest=CPageTestComprehensive -Dplaywright.timeout=600
```

### Debug Failures

```bash
# Run with visible browser + slow motion + console output
PLAYWRIGHT_HEADLESS=false PLAYWRIGHT_SLOWMO=1000 PLAYWRIGHT_SHOW_CONSOLE=true \
  mvn test -Dtest=CPageTestComprehensive -Dtest.targetButtonId=<failing-button-id>

# Check logs
cat target/test-logs/playwright-test-latest.log

# Check screenshots
ls -lh target/screenshots/
```

### Component Not Detected

1. Check component has stable ID or CSS selector
2. Verify component visible on page
3. Create custom component tester if needed
4. Check logs for detection attempts

---

## Architecture

### Test Flow

```
CPageTestComprehensive
├── 1. Initialize control signatures (component testers)
├── 2. Discover test auxiliary buttons
├── 3. Filter buttons by keyword/ID (if specified)
├── 4. For each button:
│   ├── 4.1. Navigate to page
│   ├── 4.2. Walk tabs/accordions
│   │   └── Test components in each tab
│   ├── 4.3. Test CRUD operations (if toolbar found)
│   ├── 4.4. Test grid operations (if grid found)
│   └── 4.5. Record coverage metrics
└── 5. Generate coverage reports (CSV + Markdown)
```

### Component Tester Architecture

```
IComponentTester (interface)
├── canTest(Page page) → boolean
├── getComponentName() → String
└── test(Page page) → void

CBaseComponentTester (abstract base)
├── Common utilities (elementExists, waitMs, etc.)
└── Fail-fast exception checking

Component Implementations:
├── CCrudToolbarTester
├── CGridComponentTester
├── CAttachmentComponentTester
├── CCommentComponentTester
├── CLinkComponentTester
├── CCloneToolbarTester
├── CReportComponentTester
└── [Your Custom Testers]
```

---

## Best Practices

### DO ✅

- **Filter tests** - Use keywords for faster iteration
- **Watch first run** - Use `PLAYWRIGHT_HEADLESS=false` to understand behavior
- **Check coverage reports** - Review after each run
- **Add custom testers** - For specialized components
- **Use in CI/CD** - Headless mode with coverage tracking

### DON'T ❌

- **Run all pages unnecessarily** - Filter by keyword when debugging
- **Ignore failures** - Check logs and screenshots
- **Skip coverage reports** - They provide valuable metrics
- **Modify test class for page-specific logic** - Use component testers instead
- **Run without fail-fast** - Exception checking catches errors early

---

## Comparison to Old Approach

| Aspect | Old (CPageTestComprehensive + CPageTestAuxillary) | New (CPageTestComprehensive) |
|--------|----------------------------------------------|------------------------------|
| **Test Classes** | 2 separate classes (confusion) | 1 unified class |
| **Lines of Code** | 2,186 lines | 1,400 lines (36% reduction) |
| **Features** | Component OR CRUD OR Grid | Component AND CRUD AND Grid |
| **Tab Walking** | CPageTestComprehensive only | ✅ Unified |
| **Coverage Reports** | ❌ None | ✅ CSV + Markdown |
| **Test Command** | Different for each feature | One command for everything |
| **Maintenance** | Update 2 classes | Update 1 class |

---

## Support

For issues, questions, or enhancements:
1. Check this documentation
2. Review test logs: `target/test-logs/playwright-test-latest.log`
3. Check coverage reports: `test-results/playwright/coverage/`
4. Review AGENTS.md section 7 (Testing Standards)

---

**Last Updated**: 2026-01-31  
**Test Framework Version**: 1.0  
**Status**: Production Ready ✅
