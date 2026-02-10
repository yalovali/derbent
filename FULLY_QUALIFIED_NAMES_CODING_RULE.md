# Fully-Qualified Class Names - Coding Rule

**Date**: 2026-02-10  
**Status**: ✅ **MANDATORY - ZERO TOLERANCE**  
**Enforcement**: Code Review Rejection for Violations

## Rule Statement

**CRITICAL RULE**: ALL class references in code MUST use short names with import statements. Fully-qualified class names are **FORBIDDEN** in code.

## Scope

### ✅ MANDATORY Enforcement (100%)

**tech.derbent.* classes** - Zero tolerance:
```java
// ❌ FORBIDDEN - Will be REJECTED in code review
tech.derbent.api.entity.domain.CEntityDB<?> entity;
new tech.derbent.api.reporting.CDialogReportConfiguration(...);
final tech.derbent.api.projects.domain.CProject<?> project;

// ✅ CORRECT - MANDATORY pattern
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.reporting.CDialogReportConfiguration;
import tech.derbent.api.projects.domain.CProject;

CEntityDB<?> entity;
new CDialogReportConfiguration(...);
final CProject<?> project;
```

### ⚠️ RECOMMENDED (Best Practice)

**java.* and org.* classes** - Strongly recommended:
```java
// ⚠️ DISCOURAGED - Use imports instead
final java.util.List<String> items = new java.util.ArrayList<>();
final java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
catch (final org.hibernate.LazyInitializationException e) { }

// ✅ BETTER - Standard Java practice
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import org.hibernate.LazyInitializationException;

final List<String> items = new ArrayList<>();
final BigDecimal amount = BigDecimal.ZERO;
catch (final LazyInitializationException e) { }
```

## Acceptable Exceptions

**ONLY these cases are allowed:**

### 1. String Literals (Configuration)
```java
// ✅ ACCEPTABLE - String value for configuration
System.setProperty("org.atmosphere.websocket.support", "false");
System.setProperty("org.atmosphere.container.servlet", 
    "org.atmosphere.container.BlockingIOCometSupport");
```

### 2. Reflection (Dynamic Loading)
```java
// ✅ ACCEPTABLE - String value for reflection
Class<?> clazz = Class.forName("tech.derbent.plm.activities.domain.CActivity");
Method method = obj.getClass().getMethod("methodName");
```

### 3. Annotation Parameters (Rare)
```java
// ✅ ACCEPTABLE - Annotation string parameter
@MyMenu(order = "1.5", 
    icon = "class:tech.derbent.plm.gannt.view.CGanntViewEntityView", 
    title = "Gannt View")
```

### 4. JavaDoc References
```java
/**
 * See also {@link tech.derbent.api.entity.domain.CEntityDB}
 * @see java.util.List
 */
```

## Rationale

1. **Readability**: Short names are easier to read and understand
2. **Maintainability**: Imports are easier to refactor than scattered qualified names
3. **Line Length**: Reduces line length violations
4. **IDE Support**: Better autocomplete and navigation
5. **Professional Standard**: Industry best practice in Java development
6. **Consistency**: All modern codebases follow this pattern

## Verification Commands

### Pre-Commit Checks
```bash
# 1. Check Derbent classes (MUST return 0)
find src/main/java -name "*.java" -exec grep -H "tech\.derbent\.[a-z]*\.[a-z]*\.[a-z]*\.[C-Z]" {} \; | \
  grep -v "^[^:]*:import " | grep -v "^[^:]*:package " | \
  grep -v "System.setProperty\|Class.forName\|@MyMenu.*icon" | wc -l

# Expected: 0

# 2. Check Java/org classes (minimize count)
find src/main/java -name "*.java" -exec grep -Hn "new java\.\|final java\.\|final org\." {} \; | \
  grep -v "^[^:]*:[0-9]*:import " | grep -v "System.setProperty" | wc -l

# Expected: < 30 (current baseline, aim to reduce)
```

### Find Violations
```bash
# Find Derbent violations with line numbers
find src/main/java -name "*.java" -exec grep -Hn "tech\.derbent\.[a-z]*\.[a-z]*\.[a-z]*\.[C-Z]" {} \; | \
  grep -v "^[^:]*:[0-9]*:import " | grep -v "System.setProperty" | grep -v "Class.forName"

# List files with most Java qualified names
find src/main/java -name "*.java" -exec sh -c '
  count=$(grep -c "new java\.\|final java\." "$1" | grep -v import || echo 0)
  [ $count -gt 0 ] && echo "$count: $1"
' _ {} \; | sort -rn | head -10
```

## Code Review Enforcement

### ❌ IMMEDIATE REJECTION

Pull requests containing these patterns will be **REJECTED**:

1. **New Derbent qualified names**:
   ```java
   // ❌ REJECT - Any new tech.derbent.* fully-qualified names
   tech.derbent.api.entity.service.CAbstractService<?> service;
   new tech.derbent.api.reporting.CDialogReportConfiguration(...);
   ```

2. **Derbent qualified names in new files**:
   ```java
   // ❌ REJECT - New files must use imports only
   public tech.derbent.plm.activities.domain.CActivity createActivity() { }
   ```

### ⚠️ REQUEST CHANGES

Pull requests containing these patterns should be **FLAGGED**:

1. **New Java/org qualified names**:
   ```java
   // ⚠️ FLAG - Request to add import instead
   final java.util.List<String> items = new java.util.ArrayList<>();
   ```

### ✅ APPROVE

These patterns are acceptable:

1. **String literals**: `System.setProperty("org.atmosphere...")`
2. **Reflection**: `Class.forName("tech.derbent...")`
3. **Annotations**: `@Menu(icon = "class:tech.derbent...")`

## Current Compliance Status

| Category | Status | Count |
|----------|--------|-------|
| **Derbent classes** | ✅ **100% COMPLIANT** | 0 violations |
| **Java/org classes** | ⚠️ **~30 instances** | Baseline established |
| **Build Status** | ✅ **SUCCESS** | All code compiles |

**Verified**: 2026-02-10

## Migration Guide

If you encounter violations, fix them:

### Step 1: Add Import
```java
// Add at top of file (after package, before class)
import tech.derbent.api.entity.domain.CEntityDB;
import java.util.List;
```

### Step 2: Replace Qualified Name
```java
// Before
tech.derbent.api.entity.domain.CEntityDB<?> entity;

// After
CEntityDB<?> entity;
```

### Step 3: Verify
```bash
# Run verification commands
./mvnw compile -Pagents -DskipTests
```

## IDE Support

### IntelliJ IDEA
1. Place cursor on qualified name
2. Press `Alt+Enter`
3. Select "Import class"
4. Run "Optimize Imports" on file (`Ctrl+Alt+O`)

### Eclipse
1. Place cursor on qualified name
2. Press `Ctrl+Shift+M` (Add Import)
3. Run "Organize Imports" (`Ctrl+Shift+O`)

### VS Code
1. Place cursor on qualified name
2. Press `Ctrl+.` (Quick Fix)
3. Select "Import class"

## Related Documentation

- `.github/copilot-instructions.md` - Section 3.5: Import Organization
- `FULLY_QUALIFIED_NAMES_FIX.md` - Implementation history (Derbent classes)
- `JAVA_ORG_QUALIFIED_NAMES_STATUS.md` - Java/org assessment
- `AGENTS.md` - Master playbook reference

## Summary

✅ **MANDATORY**: Use imports for ALL tech.derbent.* classes  
⚠️ **RECOMMENDED**: Use imports for java.* and org.* classes  
✅ **ACCEPTABLE**: String literals (config, reflection, annotations)  
❌ **FORBIDDEN**: Fully-qualified names in executable code

**Zero tolerance for tech.derbent.* violations in code reviews.**

---

**Effective Date**: 2026-02-10  
**Last Updated**: 2026-02-10  
**Enforcement Level**: MANDATORY (Derbent), RECOMMENDED (Java/org)
