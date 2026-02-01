# Quick Reference: Testing Patterns

**SSC WAS HERE!!** 🌟 - Zero unit tests, fail-fast exception detection!

## The Two Types (ONLY)

```
1. TEST CLASS → Has @Test methods → Extends CBaseUITest
2. COMPONENT TESTER → NO @Test methods → Extends CBaseComponentTester
```

## Creating a Test Class

```java
package automated_tests.tech.derbent.ui.automation.tests;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DisplayName("My Entity Test")
public class CMyEntityTest extends CBaseUITest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CMyEntityTest.class);
    
    @Test
    @DisplayName("Test CRUD operations")
    void testCrud() {
        loginToApplication();
        navigateToDynamicPageByEntityType("CMyEntity");
        // Test logic...
    }
}
```

## Creating a Component Tester

```java
package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Page;

public class CMyComponentTester extends CBaseComponentTester {
    
    @Override
    public String getComponentName() {
        return "My Component";
    }
    
    @Override
    public boolean canTest(Page page) {
        return elementExists(page, "#my-component");
    }
    
    @Override
    public void test(Page page) {
        LOGGER.info("Testing {}", getComponentName());
        // Component testing logic...
    }
}
```

## Exception Detection (Automatic)

All these methods automatically detect exceptions:

```java
// Wait methods
waitMs(page, 500);           // Auto-detects exceptions after wait
wait_500(page);              // Auto-detects
wait_1000(page);             // Auto-detects
wait_2000(page);             // Auto-detects

// Dialog methods
waitForDialogToClose(page);  // Auto-detects during wait
waitForDialogWithText(page, "Success");  // Auto-detects

// Grid methods
waitForGridCellText(grid, "Data");  // Auto-detects
waitForGridCellGone(grid, "Old");   // Auto-detects

// Action methods
clickFirstGridRow(page);     // Auto-detects after click
confirmDialogIfPresent(page); // Auto-detects after confirm
```

## What Gets Detected

✅ Exception dialogs (`vaadin-dialog-overlay:has-text('Exception')`)  
✅ Error dialogs (`vaadin-dialog-overlay:has-text('Error')`)  
✅ Error notifications (`vaadin-notification[theme*='error']`)  
✅ Error messages (`.error-message:visible`)

**Result**: Test IMMEDIATELY fails with detailed context!

## Running Tests

```bash
# Single entity type (fastest - recommended during dev)
./run-playwright-tests.sh activity

# Menu navigation
./run-playwright-tests.sh menu

# Comprehensive (all pages - run before commit)
./run-playwright-tests.sh comprehensive
```

## Anti-Patterns (FORBIDDEN)

❌ **Unit tests** - NO service/repository tests  
❌ **Component tester with @Test** - Testers are helpers, not tests  
❌ **Test without CBaseUITest** - MUST extend base class  
❌ **Manual exception checking** - Use automatic detection

## File Locations

```
src/test/java/automated_tests/tech/derbent/ui/automation/
├── CBaseUITest.java              # Test base class
├── tests/                        # All test classes here
│   ├── CActivityCrudTest.java
│   └── CMenuNavigationTest.java
└── components/                   # All component testers here
    ├── CBaseComponentTester.java # Component tester base
    └── CAttachmentComponentTester.java
```

## Code Review Checklist

Test Class:
- [ ] Extends CBaseUITest
- [ ] Has @SpringBootTest
- [ ] Has @Test methods
- [ ] Logger references correct class
- [ ] Located in tests/

Component Tester:
- [ ] Extends CBaseComponentTester
- [ ] NO @SpringBootTest
- [ ] NO @Test methods
- [ ] Implements canTest(), test(), getComponentName()
- [ ] Located in components/

## Remember

**ZERO unit tests allowed!**  
**Exception detection is automatic!**  
**Only 2 types of test code!**

---

**Documentation**: See `TESTING_PATTERN_ENFORCEMENT_SUMMARY.md` for complete details
