# Fixing LazyInitializationException in toString() Methods

## Problem

When entity `toString()` methods directly access lazy-loaded relationships, they throw `LazyInitializationException` when called outside of a Hibernate transaction:

```java
@Override
public String toString() {
    // ❌ BAD: Direct access to lazy-loaded fields
    return String.format("UserProjectSettings[user id=%s, project id=%s, role=%s]",
        user.getId(),          // LazyInitializationException!
        project.getId(),       // LazyInitializationException!
        role                   // LazyInitializationException!
    );
}
```

This commonly occurs when:
- Logging entity state: `LOGGER.debug("Processing: {}", settings);`
- Debugging in IDE (watches/evaluations)
- Using entity in string concatenation
- Any situation where toString() is called outside a transaction

## Solution

Use `CSpringAuxillaries` utility methods to safely access lazy-loaded fields:

```java
import tech.derbent.api.annotations.CSpringAuxillaries;

@Override
public String toString() {
    // ✅ GOOD: Safe access to lazy-loaded fields
    return String.format("UserProjectSettings[user id=%s, project id=%s, role=%s, permission=%s]",
        user != null ? CSpringAuxillaries.safeGetId(user) : null,      // Safe ID access
        project != null ? CSpringAuxillaries.safeGetId(project) : null, // Safe ID access
        CSpringAuxillaries.safeToString(role),                          // Safe toString
        permission                                                       // Direct field OK
    );
}
```

## CSpringAuxillaries Utility Methods

### `safeGetId(CEntityDB<?> entity)`
- Safely gets the ID of an entity
- Requires non-null entity (use null check before calling)
- Returns `null` if ID cannot be accessed
- Handles lazy-loaded proxies gracefully

### `safeToString(Object entity)`
- Safely converts entity to string
- Handles `null` entities (returns `"N/A"`)
- Detects unloaded proxies (returns `"ClassName[Proxy]"`)
- Catches exceptions (returns `"ClassName[Error]"`)

### `isLoaded(Object entity)`
- Checks if an entity is loaded (not a proxy)
- Useful for conditional logic based on load state

## Implementation Pattern

For entities with lazy-loaded relationships:

1. **Import the utility class:**
   ```java
   import tech.derbent.api.annotations.CSpringAuxillaries;
   ```

2. **Use null-safe access for IDs:**
   ```java
   user != null ? CSpringAuxillaries.safeGetId(user) : null
   ```

3. **Use safeToString for objects:**
   ```java
   CSpringAuxillaries.safeToString(role)
   ```

4. **Direct access is fine for:**
   - Basic types (String, Integer, etc.)
   - Non-lazy fields
   - Fields that are eagerly loaded

## Examples

### CUserProjectSettings
```java
@Override
public String toString() {
    return String.format("UserProjectSettings[user id=%s, project id=%s, role=%s, permission=%s]",
        user != null ? CSpringAuxillaries.safeGetId(user) : null,
        project != null ? CSpringAuxillaries.safeGetId(project) : null,
        CSpringAuxillaries.safeToString(role),
        permission);
}
```

### CUserCompanySetting
```java
@Override
public String toString() {
    return String.format("UserCompanySettings[user=%s, company=%s, ownership=%s, role=%s, active=%s]",
        user != null ? CSpringAuxillaries.safeToString(user) : "null",
        company != null ? CSpringAuxillaries.safeToString(company) : "null",
        getOwnershipLevel(), role, isActive());
}
```

## Benefits

1. **No LazyInitializationException**: toString() works in all contexts
2. **Better Debugging**: Can inspect entities in debugger without errors
3. **Safer Logging**: Log statements don't crash when entities are detached
4. **Consistent Behavior**: All entities handle lazy loading the same way

## Testing

Test that toString() works in multiple contexts:

```java
@Test
@DisplayName("Test toString() does not throw LazyInitializationException outside transaction")
public void testToStringOutsideTransaction() {
    CUserProjectSettings settings = service.getById(id).orElseThrow();
    
    // Should not throw exception even though we're outside transaction
    String result = assertDoesNotThrow(() -> settings.toString());
    
    assertNotNull(result);
    assertTrue(result.contains("UserProjectSettings"));
}
```

## Related Documentation

- `docs/lazy-loading-pattern.md` - Comprehensive lazy loading patterns
- `src/main/java/tech/derbent/api/annotations/CSpringAuxillaries.java` - Utility class implementation
- `src/test/java/tech/derbent/users/domain/CUserProjectSettingsToStringTest.java` - Test examples
