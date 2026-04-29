# Single Source of Truth (SSOT) Coding Rule

**Date**: 2026-02-03  
**Type**: Mandatory Coding Standard  
**Status**: ✅ ADDED TO AGENTS.MD  
**Location**: `.github/copilot-instructions.md` Section 3.18

## What Was Added

New mandatory coding rule: **Single Source of Truth (SSOT) Pattern**

**Rule Number**: Section 3.18  
**Priority**: MANDATORY  
**Enforcement**: Code review rejection for violations

---

## Rule Summary

**When a class needs multiple fields from an entity, hold a reference to the entity instead of copying individual primitive fields.**

### Core Principle

> Never duplicate data that exists in another entity. Use entity references for proper encapsulation and live data access.

---

## Rule Content

### ✅ CORRECT Pattern
```java
public class CClientProject {
    private final CProject_Bab project;  // Single source of truth
    
    public void connect() {
        String ip = project.getIpAddress();    // Live data
        String token = project.getAuthToken(); // Live data
    }
}
```

### ❌ INCORRECT Pattern
```java
public class CClientProject {
    private final String projectId;    // ❌ Duplicate
    private final String projectName;  // ❌ Duplicate
    private final String ipAddress;    // ❌ Duplicate
    private final String authToken;    // ❌ Duplicate
}
```

---

## When to Apply

| Scenario | Use Entity Reference? |
|----------|----------------------|
| **3+ fields from entity** | ✅ YES |
| **Fields can change at runtime** | ✅ YES |
| **Entity relationship exists** | ✅ YES |
| **Only 1-2 immutable IDs** | ⚠️ MAYBE |
| **Crossing bounded contexts** | ⚠️ MAYBE |
| **Serialization** | ❌ NO |

---

## Benefits

1. **No Synchronization Issues** - Changes visible immediately
2. **Reduced Complexity** - Fewer fields = simpler code
3. **Type Safety** - Compile-time checking
4. **Single Maintenance Point** - Add field once
5. **Live Configuration** - Runtime changes work

---

## Anti-Patterns (FORBIDDEN)

### ❌ Pattern 1: Copying Entity Fields
```java
public class BadClient {
    private String projectId;      // Duplicate!
    private String projectName;    // Duplicate!
    private String ipAddress;      // Duplicate!
}
```

### ❌ Pattern 2: Parallel Data Structures
```java
public class BadService {
    private Map<Long, String> projectNames;
    private Map<Long, String> projectIps;
    // Should be: Map<Long, CProject> projects
}
```

### ❌ Pattern 3: Cached Entity Fields
```java
public class BadComponent {
    private String cachedName;  // Stale when entity changes!
}
```

---

## Code Review Enforcement

**MANDATORY**: Pull requests MUST be rejected if they:
1. ✅ Copy 3+ fields from entity instead of entity reference
2. ✅ Create parallel data structures with same key space
3. ✅ Cache entity fields that can change at runtime
4. ✅ Duplicate data existing in another entity

---

## Verification Commands

```bash
# Find potential SSOT violations
find src/main/java -name "*.java" -exec grep -l \
  "private final String.*Id\|private final String.*Name" {} \; | \
  xargs -I {} sh -c 'echo "{}:"; grep "private final String" {}'

# Check Builder patterns with many primitives
grep -r "public Builder.*String.*String.*String.*String" \
  src/main/java --include="*.java"
```

---

## Migration Pattern

When refactoring existing code:

1. **Add entity reference**: `private final CEntity entity;`
2. **Update constructor**: Accept entity instead of primitives
3. **Replace access**: `this.name` → `entity.getName()`
4. **Remove duplicates**: Delete copied fields
5. **Update builder**: Accept entity
6. **Update factory**: Pass entity reference
7. **Verify compilation**: All updated
8. **Test runtime**: Live updates work

---

## Real-World Example

### CClientProject Refactoring (2026-02-03)

**Before** (4 duplicate fields):
```java
public class CClientProject {
    private final String projectId;
    private final String projectName;
    private final String targetIp;
    private final String authToken;
    
    // 7-parameter builder
    public static class Builder {
        public Builder projectId(String id) { ... }
        public Builder projectName(String name) { ... }
        public Builder targetIp(String ip) { ... }
        public Builder authToken(String token) { ... }
    }
}
```

**After** (1 entity reference):
```java
public class CClientProject {
    private final CProject_Bab project;  // Single source
    
    // 2-parameter builder
    public static class Builder {
        public Builder project(CProject_Bab proj) { ... }
    }
}
```

**Results**:
- **Fields**: 4 → 1 (-75%)
- **Builder params**: 7 → 2 (-71%)
- **Maintenance points**: 12 → 1 (-91%)
- **Live updates**: ❌ → ✅ (IP/token changes work)

---

## Impact on Architecture

### Before SSOT Rule
- Primitive obsession common
- Field duplication widespread
- Synchronization issues frequent
- Stale data problems

### After SSOT Rule
- Entity references preferred
- Field duplication eliminated
- Synchronization automatic
- Live data guaranteed

---

## Related Standards

### Existing Rules (Reinforced)
- **3.3 Type Safety** - Entity references provide better type safety
- **3.11 Fail-Fast Pattern** - Entity nullability checked once
- **4.1 Entity Class Structure** - Proper entity composition

### New Rule (Added)
- **3.18 SSOT Pattern** - Entity references > primitive copies

---

## Documentation References

### Implementation Examples
- `CCLIENTPROJECT_ENTITY_REFERENCE_REFACTORING.md` - Complete refactoring guide
- `BAB_SESSION_COMPLETE_2026-02-03.md` - Implementation case study

### Architecture Patterns
- Entity Reference Pattern (applied)
- Builder Pattern simplification
- Composition over duplication

---

## Training Checklist

For developers and AI agents:

- [ ] Read Section 3.18 in AGENTS.md
- [ ] Review CCLIENTPROJECT_ENTITY_REFERENCE_REFACTORING.md
- [ ] Understand SSOT benefits (5 key benefits)
- [ ] Memorize anti-patterns (3 forbidden patterns)
- [ ] Practice verification commands
- [ ] Apply to new code (design phase)
- [ ] Refactor existing violations (technical debt)

---

## Metrics & Monitoring

### Compliance Targets
- **New Code**: 100% SSOT compliance (zero tolerance)
- **Existing Code**: 95% compliance (allow legacy exceptions)
- **Code Reviews**: MANDATORY rejection for violations

### Success Indicators
- Reduced field duplication
- Fewer synchronization bugs
- Simpler builder patterns
- Live configuration working

### Monitoring Commands
```bash
# Count SSOT violations (target: 0 for new code)
find src/main/java -name "*.java" -newer last_release.txt \
  -exec grep -l "private final String.*Id.*private final String.*Name" {} \; | wc -l

# Check builder complexity (target: ≤3 params)
grep -r "public Builder" src/main/java --include="*.java" | \
  grep -o "String" | sort | uniq -c | sort -nr | head -10
```

---

## FAQ

### Q1: What about DTOs for API boundaries?
**A**: DTOs are acceptable when crossing bounded contexts (e.g., REST API responses). But within same context, use entity references.

### Q2: What about performance (entity vs primitives)?
**A**: Entity reference performance is identical to primitives (single pointer). Memory overhead negligible for improved maintainability.

### Q3: What about serialization/persistence?
**A**: Use `@Transient` for entity references that shouldn't be persisted. Or use DTOs for serialization boundaries.

### Q4: Can I cache a single frequently-accessed field?
**A**: Only if immutable (e.g., ID) AND documented. Prefer lazy getter: `String getName() { return entity.getName(); }`

### Q5: What about Optional fields?
**A**: Store entity reference, check null in getter: `return entity != null ? entity.getName() : null;`

---

## Future Enhancements

### Phase 1: Automated Detection (Consider)
Create linting rule to detect SSOT violations automatically during build.

### Phase 2: Migration Tool (Consider)
Script to detect and suggest SSOT refactorings in existing code.

### Phase 3: Architecture Tests (Consider)
ArchUnit rules to enforce SSOT pattern at compile time.

---

## Change History

| Date | Change | Author |
|------|--------|--------|
| 2026-02-03 | Initial rule creation based on CClientProject refactoring | AI Agent |
| 2026-02-03 | Added to AGENTS.md Section 3.18 | AI Agent |

---

**Status**: ✅ MANDATORY CODING RULE  
**Location**: `.github/copilot-instructions.md` Section 3.18  
**Enforcement**: Code review rejection  
**Priority**: HIGH

**All developers and AI agents MUST follow this rule for all new code.**
