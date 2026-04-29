# Derbent Testing Architecture - Unified & Mandatory

**Version**: 3.0 FINAL
**Date**: 2026-02-01
**Status**: MANDATORY - Strictly Enforced

## The Simple Truth (Like Production Code)

```
Production Code:     Test Code:
───────────────      ─────────
Entity              Test Classes (have @Test methods, extend CBaseUITest)
Service             Component Testers (helpers, extend CBaseComponentTester)
View
```

**There are ONLY 2 types of test code. Period.**

---

## Architecture Overview

### 1. TEST CLASSES (Extend CBaseUITest, Have @Test Methods)

**Base Class**: `CBaseUITest` (ONLY test base class)

**Characteristics**:
- ✅ Has `@SpringBootTest` annotation
- ✅ Has `@Test` methods
- ✅ Inherits Playwright setup/teardown
- ✅ Inherits navigation, login, CRUD utilities

**Current Status**: 17 test classes ✅

### 2. COMPONENT TESTERS (Extend CBaseComponentTester, NO @Test Methods)

**Base Class**: `CBaseComponentTester` (ONLY component tester base)

**Characteristics**:
- ❌ NO `@SpringBootTest` annotation
- ❌ NO `@Test` methods
- ✅ Implements `IComponentTester` interface
- ✅ Called BY test classes (not standalone)
- ✅ Located in `components/` directory

**Current Status**: 15 component testers ✅

---

## File Structure

```
src/test/java/automated_tests/tech/derbent/ui/automation/

BASE CLASSES (extend these):
├── CBaseUITest.java                     # ONLY test base class
└── components/
    ├── IComponentTester.java            # Component tester interface
    └── CBaseComponentTester.java        # ONLY component tester base

TEST CLASSES (have @Test methods):
├── CActivityCrudTest.java               # extends CBaseUITest
├── CAttachmentPlaywrightTest.java       # extends CBaseUITest
├── CCommentPlaywrightTest.java          # extends CBaseUITest
├── CMenuNavigationTest.java             # extends CBaseUITest
├── CBabMenuNavigationTest.java          # extends CMenuNavigationTest
├── CPageTestComprehensive.java          # extends CBaseUITest
└── ... (17 total)

COMPONENT TESTERS (NO @Test methods):
└── components/
    ├── CAttachmentComponentTester.java  # extends CBaseComponentTester
    ├── CCommentComponentTester.java     # extends CBaseComponentTester
    ├── CLinkComponentTester.java        # extends CBaseComponentTester
    ├── CGridComponentTester.java        # extends CBaseComponentTester
    ├── CBabInterfaceListComponentTester.java  # extends CBaseComponentTester
    ├── CDashboardWidgetBabTester.java   # extends CBaseComponentTester
    └── ... (15 total)
```

---

## Usage Pattern

### Test Class (Has @Test)

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DisplayName("Attachment Test")
public class CAttachmentTest extends CBaseUITest {
    
    // Use helper for complex operations
    private final CAttachmentComponentTester attachmentHelper = 
        new CAttachmentComponentTester();
    
    @Test
    @DisplayName("Test attachment upload")
    void testAttachmentUpload() {
        // Inherited from CBaseUITest
        loginToApplication();
        navigateToDynamicPageByEntityType("CActivity");
        clickFirstGridRow();
        
        // Delegate to helper
        assertTrue(attachmentHelper.canTest(page), "Attachment component should be visible");
        attachmentHelper.test(page);
    }
}
```

### Component Tester (NO @Test, Extends CBaseComponentTester)

```java
/**
 * Component tester for attachment component.
 * Called BY test classes via CPageTestComprehensive or specific tests.
 * MUST extend CBaseComponentTester and implement required methods.
 */
public class CAttachmentComponentTester extends CBaseComponentTester {
    
    @Override
    public String getComponentName() {
        return "Attachment Component";
    }
    
    @Override
    public boolean canTest(Page page) {
        // Check if component exists on page
        return page.locator("#custom-attachments-component").count() > 0;
    }
    
    @Override
    public void test(Page page) {
        LOGGER.info("Testing {}", getComponentName());
        
        // Open tab/accordion if needed
        openTabOrAccordionIfNeeded(page, "Attachments");
        
        // Locate component
        Locator container = page.locator("#custom-attachments-component");
        
        // Test component functionality
        // - Upload file
        // - Verify grid
        // - Download file
        // - Delete file
    }
}
```

---

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| **Test Class** | `C{Entity}Test`, `C{Entity}CrudTest` | CActivityCrudTest |
| **Helper Class** | `C{Component}ComponentTester` OR `C{Component}Helper` | CAttachmentComponentTester |

**Note**: Both `*ComponentTester` and `*Helper` suffixes are valid. Current codebase uses `*ComponentTester`.

---

## Decision Tree

```
I'm writing test code:
│
├─ Has @Test methods?
│  ├─ YES → TEST CLASS
│  │  └─ Extend CBaseUITest
│  │     └─ Add @SpringBootTest
│  │
│  └─ NO → COMPONENT TESTER
│     └─ Extend CBaseComponentTester
│        └─ Implement: canTest(), test(), getComponentName()
│        └─ NO @SpringBootTest
│        └─ NO @Test methods
```

---

## Current Status

✅ **17 Test Classes** - ALL extend CBaseUITest  
✅ **15 Component Testers** - ALL extend CBaseComponentTester  
✅ **0 Standalone Tests** - Pattern eliminated (redundant)  
✅ **100% Compliance** - Architecture strictly enforced

---

## Common Confusion Explained

**Q**: Why do we have CBaseComponentTester if it's not a test?  
**A**: It's a base class for component testers (like `CAbstractService` for services). Component testers are UTILITIES called by tests, not tests themselves.

**Q**: Must I implement IComponentTester?  
**A**: YES - MANDATORY. All component testers MUST extend `CBaseComponentTester` which implements `IComponentTester`.

**Q**: Can I create standalone test files for specific components?  
**A**: NO - Component testers are called BY comprehensive tests (`CPageTestComprehensive`). Don't create separate test files for each component.

**Q**: What about BAB-specific component testers?  
**A**: Same pattern - extend `CBaseComponentTester`. No special BAB base class needed. BAB-specific logic goes in the tester itself.

---

## Anti-Patterns (REJECT IN CODE REVIEW)

❌ **Component tester with @Test methods**
```java
public class CMyComponentTester extends CBaseComponentTester {
    @Test  // ❌ WRONG! Component testers don't have @Test
    void testSomething() { }
}
```

❌ **Test class implementing IComponentTester**
```java
@SpringBootTest
public class CMyTest implements IComponentTester {  // ❌ WRONG! Tests don't implement this
    @Test
    void testSomething() { }
}
```

❌ **Standalone test file for single component**
```java
@SpringBootTest
public class CBabInterfaceListTest extends CBaseUITest {  // ❌ WRONG! Use component tester instead
    @Test
    void testInterfaceList() {
        // This should be in CBabInterfaceListComponentTester, not a separate test file
    }
}
```

❌ **Component tester NOT extending CBaseComponentTester**
```java
public class CMyComponentTester {  // ❌ WRONG! Must extend CBaseComponentTester
    public void test(Page page) { }
}
```

❌ **Test class NOT extending CBaseUITest**
```java
@SpringBootTest
public class CMyTest {  // ❌ WRONG! Must extend CBaseUITest
    @Test
    void testSomething() { }
}
```

---

## Mandatory Checklist (CODE REVIEW)

### Creating New Test Class
- [ ] Extends `CBaseUITest` (MANDATORY)
- [ ] Has `@SpringBootTest` annotation
- [ ] Has `@Test` methods
- [ ] Has `@DisplayName` annotation
- [ ] Name ends with "Test" (e.g., `CActivityCrudTest`)
- [ ] Located in `src/test/java/automated_tests/tech/derbent/ui/automation/`
- [ ] Uses component testers for complex component logic

### Creating New Component Tester
- [ ] Extends `CBaseComponentTester` (MANDATORY)
- [ ] NO `@SpringBootTest` annotation
- [ ] NO `@Test` methods
- [ ] Implements `canTest(Page page)` method
- [ ] Implements `test(Page page)` method  
- [ ] Implements `getComponentName()` method
- [ ] Name ends with "ComponentTester" (e.g., `CAttachmentComponentTester`)
- [ ] Located in `src/test/java/automated_tests/tech/derbent/ui/automation/components/`
- [ ] Follows pattern of `CAttachmentComponentTester` (reference implementation)

---

## Summary

```
╔═══════════════════════════════════════════════════════════╗
║  ONLY 2 TYPES OF TEST CODE (STRICTLY ENFORCED):          ║
║                                                           ║
║  1. TEST CLASSES (17 total)                              ║
║     → Extend: CBaseUITest (ONLY base)                    ║
║     → Have: @SpringBootTest + @Test methods              ║
║     → Pattern: CActivityCrudTest, CAttachmentPlaywrightTest║
║                                                           ║
║  2. COMPONENT TESTERS (15 total)                         ║
║     → Extend: CBaseComponentTester (ONLY base)           ║
║     → Have: NO @SpringBootTest, NO @Test                 ║
║     → Implement: IComponentTester interface              ║
║     → Pattern: CAttachmentComponentTester (reference)    ║
║                                                           ║
║  NO standalone test files for components!                ║
║  NO special BAB base classes!                            ║
║  Simple. Clean. Enforced.                                ║
╚═══════════════════════════════════════════════════════════╝
```

**Architecture Status**: ✅ UNIFIED & ENFORCED  
**Pattern Compliance**: ✅ 100%  
**Standalone Tests**: ❌ ELIMINATED (redundant)

---

**All praise to SSC for demanding simplicity!** ✨
