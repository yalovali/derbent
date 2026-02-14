# Testing Section Update for copilot-instructions.md

**Date**: 2026-02-14  
**Purpose**: Replace Section 8 (Testing Standards) with enforced Playwright-only pattern

---

## Replace Lines 5479-5880 with:

```markdown
## 8. Testing Standards (MANDATORY - ZERO TOLERANCE)

### 8.1 THE ONE TRUE TESTING PATTERN

**CRITICAL RULE**: There is ONLY ONE way to test in Derbent. NO unit tests. NO individual page tests. NO exceptions.

#### The Pattern

```
Login → Initialize DB → Filter Page → Navigate → Detect Components → Test → Report
```

**Entry Point**: `CPageComprehensiveTest` (ONLY test class for pages)  
**Test Command**: `mvn test -Dtest=CPageComprehensiveTest [filters]`

#### Architecture

```
CPageComprehensiveTest (Orchestrator)
  └─ Extends: CBaseUITest
  └─ Uses: 18+ IComponentTester implementations
  
IComponentTester (Interface)
  └─ Methods: canTest(), test(), getComponentName()
  └─ Implemented by: Component-specific testers
  
Component Testers (NO @Test methods)
  └─ Extend: CBaseComponentTester
  └─ Examples: CCrudToolbarComponentTester, CGridComponentTester
```

### 8.2 Standard Test Commands (MANDATORY)

**Test Single Page by Name** (RECOMMENDED):
```bash
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="Page Name" \
  -Dspring.profiles.active=test,derbent \
  -Dplaywright.headless=false
```

**Test Multiple Pages by Keyword**:
```bash
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.routeKeyword=keyword \
  -Dtest.runAllMatches=true \
  -Dspring.profiles.active=test,bab
```

**Test All Pages** (Full Comprehensive):
```bash
mvn test -Dtest=CPageComprehensiveTest \
  -Dspring.profiles.active=test,bab
```

### 8.3 Filter Parameters

| Parameter | Match Type | Example |
|-----------|------------|---------|
| `-Dtest.targetButtonText="X"` | Exact button text | `-Dtest.targetButtonText="Activities"` |
| `-Dtest.targetButtonId="X"` | Button ID | `-Dtest.targetButtonId="test-aux-btn-5"` |
| `-Dtest.routeKeyword=X` | Route substring | `-Dtest.routeKeyword="Interface"` |
| `-Dtest.runAllMatches=true` | Test all matches | Test multiple pages |
| `-Dplaywright.headless=false` | Show browser | Visible debugging |
| `-Dplaywright.slowmo=500` | Slow down (ms) | 500ms delay per action |

### 8.4 Test Flow (Automatic)

```
Phase 1: Login & DB Init
  ↓
Phase 2: Navigate to CPageTestAuxillary (test infrastructure)
  ↓
Phase 3: Discover navigation buttons
  ↓
Phase 4: Filter buttons by parameters
  ↓
Phase 5: For each matched page:
   ├─ Navigate to page
   ├─ Detect components (18+ testers)
   ├─ Test each component
   ├─ Test CRUD operations
   ├─ Test grid functionality
   └─ Generate coverage report
  ↓
Phase 6: Summary report (CSV + Markdown)
```

### 8.5 Component Testers (Automatic Detection)

**18 Component Testers** automatically detect and test:

| Tester | Component | Tests |
|--------|-----------|-------|
| CCrudToolbarComponentTester | CRUD toolbar | New, Save, Delete, Refresh |
| CGridComponentTester | Entity grid | Structure, selection, sorting |
| CAttachmentComponentTester | Attachments | Upload, download, delete |
| CCommentComponentTester | Comments | Add, edit, delete, mark important |
| CLinkComponentTester | Links | Create, edit, remove links |
| CCalimeroStatusComponentTester | Calimero status | Service status, config |
| CInterfaceListComponentTester | Network interfaces | Activate/deactivate |
| CUserComponentTester | User field | User selection |
| CProjectComponentTester | Project field | Project selection |
| CStatusFieldComponentTester | Status field | Status transitions |
| CDatePickerComponentTester | Date picker | Date selection |
| CReportComponentTester | Reports | Generate reports |
| CCloneToolbarComponentTester | Clone toolbar | Clone operations |
| CProjectUserSettingsComponentTester | Settings | Settings CRUD |
| ... (18 total)

### 8.6 FORBIDDEN Testing Methods (ZERO TOLERANCE)

**Code Review REJECTION Criteria**:

❌ **FORBIDDEN #1**: Unit Tests
```java
public class CActivityServiceTest {  // ❌ REJECT!
    @Test
    void testService() { }
}
```

❌ **FORBIDDEN #2**: Individual Page Test Classes
```java
public class CActivityPageTest extends CBaseUITest {  // ❌ REJECT!
    @Test
    void testPage() { }
}
```

❌ **FORBIDDEN #3**: Component Testers with @Test
```java
public class CMyComponentTester extends CBaseComponentTester {
    @Test  // ❌ REJECT!
    void testComponent() { }
}
```

❌ **FORBIDDEN #4**: Direct Playwright Scripts
```bash
npx playwright test activity.spec.ts  # ❌ REJECT!
```

❌ **FORBIDDEN #5**: Bash Script Testing
```bash
./run-playwright-tests.sh menu  # ❌ REJECT!
```

**CORRECT ALTERNATIVE FOR ALL**:
```bash
mvn test -Dtest=CPageComprehensiveTest -Dtest.targetButtonText="Page Name"
```

### 8.7 AI Agent Rules (MANDATORY)

#### ✅ CORRECT Agent Behavior

**User**: "Test the Activities page"

**Agent**:
```bash
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="Activities" \
  -Dspring.profiles.active=test,derbent \
  -Dplaywright.headless=false
```

**User**: "Test BAB policy pages"

**Agent**:
```bash
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.routeKeyword="Policy" \
  -Dtest.runAllMatches=true \
  -Dspring.profiles.active=test,bab
```

#### ❌ FORBIDDEN Agent Behavior

**NEVER** do ANY of these:

1. ❌ Create new test file
2. ❌ Suggest unit tests
3. ❌ Run bash scripts
4. ❌ Use npx playwright
5. ❌ Create component tester with @Test
6. ❌ Suggest JUnit tests
7. ❌ Test service methods directly

### 8.8 Adding New Component Tester (ONLY Extension Allowed)

**Template**:
```java
public class CMyComponentTester extends CBaseComponentTester {
    // NO @Test annotation!
    
    @Override
    public String getComponentName() {
        return "My Component";
    }
    
    @Override
    public boolean canTest(Page page) {
        return page.locator("#custom-my-component").count() > 0;
    }
    
    @Override
    public void test(Page page) {
        LOGGER.info("Testing {}", getComponentName());
        // Component-specific tests
        performFailFastCheck(page);  // After every operation
    }
}
```

**Register in CPageComprehensiveTest**:
```java
componentTesters.add(new CMyComponentTester());
```

**That's it!** Component automatically tested on ALL pages.

### 8.9 Test Output & Reports

**Console Output**:
```
🎯 Comprehensive Page Testing Framework
📋 Phase 1 - Navigation: Found 42 buttons, matched 1
📄 Testing Page: Activities
📍 Phase 2 - Detected 8 components
🎯 Phase 3 - Testing components...
   ✓ CRUD Toolbar: PASS
   ✓ Grid Component: PASS
   ✓ Attachment Component: PASS
✅ Test PASSED - Duration: 45s
```

**Coverage Reports**: `test-results/playwright/coverage/`
- `test-coverage-TIMESTAMP.csv` (machine-readable)
- `test-summary-TIMESTAMP.md` (human-readable)

### 8.10 Testing Rules Summary

**THE ONE RULE**: Use `CPageComprehensiveTest` with filters. NO exceptions.

**Commands**:
```bash
# Single page
mvn test -Dtest=CPageComprehensiveTest -Dtest.targetButtonText="Page"

# Multiple pages
mvn test -Dtest=CPageComprehensiveTest -Dtest.routeKeyword=X -Dtest.runAllMatches=true

# All pages
mvn test -Dtest=CPageComprehensiveTest
```

**Violations** (ZERO TOLERANCE):
1. ❌ Unit tests
2. ❌ Individual page test classes
3. ❌ Component testers with @Test
4. ❌ Direct Playwright scripts
5. ❌ Bash script testing

**Enforcement**: ALL pull requests violating these rules will be REJECTED immediately.

**Reference**: See `TESTING_PATTERN_ENFORCEMENT_COMPLETE.md` for complete details.
```

---

## Summary of Changes

**Removed**:
- All unit test references
- Individual page test class examples
- Bash script testing commands
- Direct Playwright script usage
- Confusing multi-pattern options

**Added**:
- Clear single pattern enforcement
- Zero tolerance policy
- AI agent-specific rules
- Comprehensive filter parameter guide
- Forbidden pattern examples with alternatives

**Enforcement**:
- ZERO TOLERANCE for violations
- Immediate PR rejection criteria
- Clear AI agent behavior rules
- Complete testing flow documentation
