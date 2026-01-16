# Circular Bean Dependency Fix - Summary

## Problem
The application failed to start with the following circular dependency error:

```
The dependencies of some of the beans in the application context form a cycle:

   CDetailLinesService defined in file [...]
┌─────┐
|  primarySessionService defined in class path resource [tech/derbent/base/session/service/CSessionServiceConfig.class]
↑     ↓
|  CSessionService defined in file [tech/derbent/base/session/service/CWebSessionService.class]
↑     ↓
|  CSecurityConfig defined in file [tech/derbent/base/login/service/CSecurityConfig.class]
↑     ↓
|  CUserService defined in file [tech/derbent/base/users/service/CUserService.class]
└─────┘
```

## Root Cause Analysis

The circular dependency was caused by the following chain:

1. **CSessionServiceConfig.primarySessionService()** - Bean method that takes `CWebSessionService` as a parameter
   - This causes Spring to eagerly instantiate `CWebSessionService` 

2. **CWebSessionService** constructor requires `AuthenticationContext` (from Vaadin Spring Security)
   - `AuthenticationContext` requires Spring Security to be fully configured

3. **CSecurityConfig** (Spring Security configuration) requires `CUserService`
   - Needed for user authentication

4. **CUserService** constructor requires `ISessionService`
   - Tries to inject the session service (which points back to step 1)

This created an unresolvable circular dependency loop.

## Solution

The fix involved two minimal changes:

### Change 1: Remove CSessionServiceConfig.java
**File Deleted:** `src/main/java/tech/derbent/base/session/service/CSessionServiceConfig.java`

The `@Bean` method with parameter injection was causing eager instantiation. Since `CWebSessionService` and `CSessionService` already have mutually exclusive profiles (`@Profile("!reset-db")` vs `@Profile("reset-db")`), there's no ambiguity.

### Change 2: Add @Primary to CWebSessionService
**File Modified:** `src/main/java/tech/derbent/base/session/service/CWebSessionService.java`

Added `@Primary` annotation directly to the service class:

```java
@Service ("CSessionService")
@Primary  // Added this annotation
@ConditionalOnWebApplication
@Profile ("!reset-db")
public class CWebSessionService implements ISessionService {
```

This makes `CWebSessionService` the primary bean when multiple implementations exist, without requiring a separate configuration class.

### Change 3: Use @Lazy injection in CUserService
**File Modified:** `src/main/java/tech/derbent/base/users/service/CUserService.java`

Added `@Lazy` annotation to the `ISessionService` parameter:

```java
public CUserService(final IEntityOfCompanyRepository<CUser> repository, 
                    final Clock clock, 
                    @Lazy final ISessionService sessionService) {
```

The `@Lazy` annotation breaks the circular dependency by:
- Creating a proxy for `ISessionService` instead of the actual bean during construction
- Deferring the actual bean creation until the first method call on the proxy
- This allows `CUserService` to be instantiated before `ISessionService` is fully initialized

## Verification

### Compilation
```bash
mvn clean compile
# BUILD SUCCESS - 520 source files compiled
```

### Application Startup
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2-local-development
# No circular dependency errors!
# Application starts (though may fail later for unrelated database issues)
```

### No Circular Dependency Errors
Confirmed that the error message about circular dependencies no longer appears in the startup logs.

## Impact Analysis

### What Changed
- **Removed:** Redundant bean configuration class
- **Added:** `@Primary` annotation to clarify bean selection
- **Added:** `@Lazy` injection to break the dependency cycle

### What Didn't Change
- Session service functionality remains identical
- User service functionality remains identical
- Security configuration unchanged
- No changes to business logic or domain models

### Why This Works
1. **@Primary** annotation ensures the correct bean is selected without needing a separate config
2. **@Lazy** injection defers proxy creation, breaking the circular dependency at Spring initialization
3. The proxy pattern means the actual bean is created on first use, after all dependencies are resolved
4. This is a standard Spring pattern for resolving circular dependencies

## Files Modified

1. **Deleted**: `src/main/java/tech/derbent/base/session/service/CSessionServiceConfig.java`
2. **Modified**: `src/main/java/tech/derbent/base/session/service/CWebSessionService.java`
   - Added `@Primary` annotation
   - Added import for `org.springframework.context.annotation.Primary`
3. **Modified**: `src/main/java/tech/derbent/base/users/service/CUserService.java`
   - Added `@Lazy` annotation to constructor parameter
   - Added import for `org.springframework.context.annotation.Lazy`

## Conclusion

The circular dependency has been successfully resolved with minimal, surgical changes. The application can now start without the circular bean dependency error. The fix follows Spring best practices for handling circular dependencies using lazy initialization.
