# Playwright Test Filtering Pattern (MANDATORY)

**Status**: MANDATORY for all AI agents and developers  
**Date**: 2026-02-01  
**Rule**: NEVER create new test files for testing individual pages

## Core Rule

**CRITICAL**: When asked to "test a page" or "run Playwright test for X", ALWAYS use **CPageComprehensiveTest** with filters. NEVER create a new test file.

## Test Infrastructure

| Test Class | Purpose | Usage |
|------------|---------|-------|
| **CPageComprehensiveTest** | Unified page testing framework | ALL page testing (navigation + components + CRUD + grid) |
| **CBaseUITest** | Base test class | Login, authentication, common utilities |
| **Component Testers** | IComponentTester implementations | Automatic component detection and testing |

## Filter Parameters

### Primary Filters (Use ONE)

| Parameter | Match Type | When to Use | Example |
|-----------|------------|-------------|---------|
| `-Dtest.targetButtonText="Page Name"` | **Exact match** on button display text | **RECOMMENDED** - User-friendly | `-Dtest.targetButtonText="BAB System Settings"` |
| `-Dtest.targetButtonId="button-id"` | Exact match on button ID | When you know the ID | `-Dtest.targetButtonId="test-aux-btn-configure-5"` |
| `-Dtest.routeKeyword=keyword` | Substring match on route/title | Multiple related pages | `-Dtest.routeKeyword="BAB"` |
| `-Dtest.targetRoute="/route"` | Exact route match | Specific route testing | `-Dtest.targetRoute="/system-settings"` |

### Secondary Parameters

| Parameter | Purpose | Default | Example |
|-----------|---------|---------|---------|
| `-Dspring.profiles.active=test,bab` | Profile selection | test | BAB profile for BAB pages |
| `-Dplaywright.headless=false` | Show browser | false | Visible debugging |
| `-Dplaywright.slowmo=500` | Slow down actions (ms) | 0 | 500ms delay per action |
| `-Dtest.runAllMatches=true` | Test all matching pages | false | Test multiple pages |

## Standard Test Commands

### Test Single Page by Name (RECOMMENDED)

```bash
# BAB System Settings
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="BAB System Settings" \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=500

# Activities Page  
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="Activities" \
  -Dspring.profiles.active=test,derbent \
  -Dplaywright.headless=false
```

### Test Single Page by ID

```bash
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonId="test-aux-btn-configure-5" \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false
```

### Test Multiple Pages by Keyword

```bash
# All BAB pages
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dtest.routeKeyword="BAB" \
  -Dtest.runAllMatches=true \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false

# All Interface pages
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dtest.routeKeyword="Interface" \
  -Dtest.runAllMatches=true \
  -Dspring.profiles.active=test,bab
```

### Full Comprehensive Test (All Pages)

```bash
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false
```

## AI Agent Rules

### ✅ CORRECT Agent Behavior

When user says: **"Test BAB System Settings page"**

Agent response:
```bash
cd /home/yasin/git/derbent && mvn test \
  -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="BAB System Settings" \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=500
```

### ❌ FORBIDDEN Agent Behavior

**NEVER** do any of these:

1. ❌ Create new test file: `CSystemSettingsBabTest.java`
2. ❌ Create new test method: `testBabSystemSettings()`
3. ❌ Run `npx playwright test` directly
4. ❌ Use bash script without Maven: `./run-playwright-tests.sh`
5. ❌ Suggest writing new Playwright tests

## Test Flow (CPageComprehensiveTest)

```
1. Login (CBaseUITest)
   ↓
2. Navigate to CPageTestAuxillary (test infrastructure page)
   ↓
3. Discover navigation buttons
   ↓
4. Filter buttons by targetButtonText/targetButtonId/routeKeyword
   ↓
5. For each matching button:
   a. Click button → Navigate to page
   b. Component Detection (IComponentTester)
   c. CRUD Testing (New/Save/Delete)
   d. Grid Testing (Structure/Selection/Sorting)
   e. Coverage Report (CSV + Markdown)
   ↓
6. Test Summary
```

## Component Testing (Automatic)

CPageComprehensiveTest automatically detects and tests these components:

| Component | Tester Class | What It Tests |
|-----------|--------------|---------------|
| **Attachments** | `CAttachmentComponentTester` | Upload, download, delete |
| **Comments** | `CCommentComponentTester` | Add, edit, delete comments |
| **Links** | `CLinkComponentTester` | Add, edit, remove links |
| **CRUD Toolbar** | `CCrudToolbarComponentTester` | New, Save, Delete, Refresh buttons |
| **Grid** | `CGridComponentTester` | Structure, selection, sorting, filtering |
| **Calimero Status** | `CCalimeroStatusComponentTester` | Service status, start/stop, config |
| **Interface List** | `CInterfaceListComponentTester` | Activate/deactivate, selection |
| **User** | `CUserComponentTester` | User field interactions |
| **Project** | `CProjectComponentTester` | Project selection |
| **Status Field** | `CStatusFieldComponentTester` | Status transitions |

## Expected Notifications During Tests

### ✅ Normal/Expected

| Notification | When | Reason |
|--------------|------|--------|
| "Failed to load CPU info" | BAB pages without Calimero | Calimero server not authenticated |
| "Calimero connection failed" | BAB Dashboard load | No active Calimero connection in test |
| "No items found" | Empty grids | Test data not yet created |

### ❌ Errors to Investigate

| Notification | When | Action |
|--------------|------|--------|
| "Exception" dialog | Page load | Fix code - real error |
| "NullPointerException" | Component load | Fix initialization |
| "Database Error" | CRUD operations | Fix validation/constraints |

## Common Scenarios

### Scenario 1: Testing New Entity Page

```bash
# Step 1: Test page navigation and component detection
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="New Entity Name" \
  -Dspring.profiles.active=test,derbent \
  -Dplaywright.headless=false

# Step 2: Check test output for detected components
# Step 3: Review coverage report in test-results/playwright/coverage/
```

### Scenario 2: Testing BAB Component

```bash
# Step 1: Ensure Calimero is running (optional - tests work without it)
ps aux | grep calimero

# Step 2: Run test
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="BAB System Settings" \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false

# Step 3: Observe component interactions in browser
```

### Scenario 3: Debugging Test Failures

```bash
# Run with visible browser and slow motion
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="Failing Page" \
  -Dplaywright.headless=false \
  -Dplaywright.slowmo=1000 \
  -X  # Maven debug output

# Check logs
tail -f target/test-logs/live-test-run.log
```

## Troubleshooting

### Port 8080 Already in Use

```bash
# Kill existing process
lsof -ti:8080 | xargs kill -9

# Then run test
mvn test -Dtest=CPageComprehensiveTest ...
```

### Test Hangs or Times Out

1. Check if application started:
   ```bash
   curl http://localhost:8080
   ```

2. Check for exception dialogs in logs:
   ```bash
   tail -f target/test-logs/live-test-run.log | grep -i exception
   ```

3. Increase timeout (in test code):
   ```java
   page.waitForSelector("#element", new WaitForSelectorOptions().setTimeout(30000));
   ```

### Browser Not Showing

```bash
# Ensure headless=false
mvn test -Dtest=CPageComprehensiveTest \
  -Dplaywright.headless=false \
  ... other parameters
```

## Test Output and Reports

### Console Output

```
🎯 Comprehensive Page Testing Framework
📋 Phase 1 - Navigation
   Found 42 navigation buttons
   Filtering by: targetButtonText="BAB System Settings"
   Matched 1 button(s)

📄 Testing Page: BAB System Settings
📍 Phase 2 - Component Detection
   ✓ Detected 5 components
   ✓ CCalimeroStatusComponentTester
   ✓ CCrudToolbarComponentTester
   ✓ ...
   
🎯 Phase 3 - CRUD Testing
   ✓ New button works
   ✓ Save button works
   
📊 Phase 4 - Grid Testing
   ✓ Grid structure validated
   ✓ Selection works
   
✅ Test PASSED - Duration: 45s
```

### Coverage Reports

**Location**: `test-results/playwright/coverage/`

- `test-coverage-YYYY-MM-DD_HH-MM-SS.csv` - Machine-readable
- `test-summary-YYYY-MM-DD_HH-MM-SS.md` - Human-readable

**Metrics**:
- Pages tested
- Components detected per page
- CRUD operations success rate
- Grid validation results
- Total duration
- Pass/fail status per page

## AGENTS.md Integration

**This pattern is MANDATORY** and referenced in:

1. `AGENTS.md` - Section 7.7: Testing Standards
2. `PLAYWRIGHT_TESTING_GUIDE.md` - Detailed usage guide
3. `BAB_COMPONENT_TESTING_GUIDE.md` - BAB-specific testing

**Rule enforcement**: All pull requests containing new test files for individual pages will be REJECTED. Use CPageComprehensiveTest with filters instead.

## Quick Reference Card

```
┌─────────────────────────────────────────────────────────────────┐
│ PLAYWRIGHT TEST FILTERING - QUICK REFERENCE                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ ✅ CORRECT: Use existing test with filter                       │
│   mvn test -Dtest=CPageComprehensiveTest \                     │
│     -Dtest.targetButtonText="Page Name"                        │
│                                                                 │
│ ❌ WRONG: Create new test file                                  │
│   src/test/java/.../CPageNameTest.java                        │
│                                                                 │
│ Parameters:                                                     │
│   -Dtest.targetButtonText="X"  → Exact button text match      │
│   -Dtest.targetButtonId="X"     → Button ID match              │
│   -Dtest.routeKeyword=X         → Route substring match        │
│   -Dspring.profiles.active=X    → Profile (test,bab/derbent)  │
│   -Dplaywright.headless=false   → Show browser                 │
│   -Dplaywright.slowmo=500       → Slow down (ms)               │
│                                                                 │
│ Output: test-results/playwright/coverage/                      │
└─────────────────────────────────────────────────────────────────┘
```
