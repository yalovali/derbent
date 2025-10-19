# Multi-User Development Checklist

## Purpose

This checklist ensures that all code changes in Derbent are multi-user safe and ready for production deployment with concurrent users.

## Pre-Development Checklist

Before starting any new feature or service:

- [ ] Read `/docs/architecture/multi-user-singleton-advisory.md`
- [ ] Understand the difference between singleton and session scope
- [ ] Review existing services for patterns to follow
- [ ] Identify if your feature requires user-specific state

## Service Development Checklist

When creating or modifying a service class:

### Basic Structure
- [ ] Service is annotated with `@Service`
- [ ] Service extends appropriate base class (`CAbstractService`, `CEntityNamedService`, etc.)
- [ ] Constructor only accepts dependencies (repositories, clock, other services)
- [ ] No mutable instance fields (except Logger, which is safe)

### State Management
- [ ] No fields storing user-specific data (currentUser, activeProject, etc.)
- [ ] No fields storing user-specific collections (user lists, caches, etc.)
- [ ] All user context retrieved from `ISessionService` per-method-call
- [ ] Any temporary state is local to method scope

### Static Fields
- [ ] No static mutable collections (static Map, static List, etc.)
- [ ] Static fields are only constants (static final String, static final int)
- [ ] Logger is the only non-constant static field (which is safe)

### Session Access
- [ ] User context accessed via `sessionService.getActiveUser()`
- [ ] Company context accessed via `sessionService.getCurrentCompany()`
- [ ] Project context accessed via `sessionService.getActiveProject()`
- [ ] Never cache these values in instance fields

### Transaction Management
- [ ] Read operations marked `@Transactional(readOnly = true)`
- [ ] Write operations marked `@Transactional`
- [ ] No transaction boundaries crossed with session state

## View/Component Development Checklist

When creating or modifying Vaadin views and components:

### Component State
- [ ] Component state is instance-based (each user gets their own component instance)
- [ ] No static fields storing UI state
- [ ] Component properly attached/detached from UI

### Session Integration
- [ ] Register listeners when component attached
- [ ] Unregister listeners when component detached
- [ ] Use `sessionService` to access user context
- [ ] Update UI in response to session changes

### UI Updates
- [ ] Use `UI.access()` for asynchronous UI updates
- [ ] Handle UI detach properly in async operations
- [ ] Check if UI is still attached before updating

## Code Review Checklist

When reviewing pull requests, verify:

### Service Layer Review
- [ ] No instance fields storing user-specific data
- [ ] No static mutable state
- [ ] Session context accessed correctly
- [ ] Proper transaction boundaries
- [ ] Thread-safe operations

### View Layer Review
- [ ] No static UI state
- [ ] Proper listener registration/cleanup
- [ ] UI.access() used correctly
- [ ] Component lifecycle handled properly

### Data Access Review
- [ ] Queries filter by company/user where appropriate
- [ ] No data leakage between users
- [ ] Proper data isolation
- [ ] Security annotations present

## Testing Checklist

### Unit Tests
- [ ] Tests don't assume any pre-existing state
- [ ] Mock session service provides appropriate context
- [ ] Test data is isolated per test
- [ ] No shared mutable test fixtures

### Integration Tests
- [ ] Test with multiple concurrent users
- [ ] Verify data isolation between users
- [ ] Test session expiry scenarios
- [ ] Verify no data leakage

### Manual Testing
- [ ] Test with multiple browser sessions
- [ ] Verify each user sees only their data
- [ ] Test concurrent operations
- [ ] Test session timeout handling

## Common Anti-Patterns to Avoid

### ❌ Instance Field for User Context

```java
@Service
public class BadService {
    private CUser currentUser;  // WRONG!
    
    public void setUser(CUser user) {
        this.currentUser = user;  // Will be shared across all users
    }
}
```

### ❌ Static Collection for User Data

```java
@Service
public class BadService {
    private static Map<Long, String> userPreferences = new HashMap<>();  // WRONG!
    // Shared across all users, not thread-safe
}
```

### ❌ Caching Without Isolation

```java
@Service
public class BadService {
    private List<CActivity> cachedActivities;  // WRONG!
    // One user's data will be shown to all users
}
```

## Correct Patterns to Follow

### ✅ Stateless Service

```java
@Service
public class GoodService extends CAbstractService<CEntity> {
    private final IEntityRepository repository;
    private final ISessionService sessionService;
    
    public GoodService(IEntityRepository repository, ISessionService sessionService, Clock clock) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.sessionService = sessionService;
    }
    
    public List<CEntity> getUserEntities() {
        // Get user from session each time
        CUser user = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        return repository.findByUserId(user.getId());
    }
}
```

### ✅ Session-Scoped State

```java
@Service
public class GoodService {
    private static final String PREFERENCE_KEY = "userPreference";
    
    public void savePreference(String preference) {
        VaadinSession.getCurrent().setAttribute(PREFERENCE_KEY, preference);
    }
    
    public String getPreference() {
        return (String) VaadinSession.getCurrent().getAttribute(PREFERENCE_KEY);
    }
}
```

## Quick Reference: Where to Store State

| State Type | Storage Location | Example |
|------------|------------------|---------|
| User identity | VaadinSession (via sessionService) | Current user, active company |
| User preferences | VaadinSession | UI settings, selected filters |
| Temporary UI state | Component instance fields | Form data, dialog state |
| Application config | Application properties / Database | System settings, feature flags |
| Business data | Database | Activities, projects, users |
| Constants | static final fields | Menu titles, default values |

## Emergency Multi-User Bug Fixes

If you discover a multi-user issue in production:

1. **Identify the Problem**
   - Find the instance/static field storing user data
   - Identify all places it's accessed

2. **Quick Fix**
   - Move state to VaadinSession
   - OR retrieve from database each time
   - OR make the data truly global (if appropriate)

3. **Test Fix**
   - Test with multiple browser sessions
   - Verify data isolation
   - Check for data leakage

4. **Deployment**
   - Deploy as hotfix
   - Monitor for issues
   - Add to regression tests

## Architecture Decision Records

When making decisions about state management:

### Use Singleton When:
- Service is truly stateless
- Service only contains dependencies and logic
- No user-specific data is stored
- **Result:** Most services should be singleton (current pattern)

### Use Session When:
- Need to store user-specific temporary data
- UI preferences or settings
- Wizard/multi-step form state
- **Result:** Use VaadinSession via sessionService

### Use Request Scope When:
- Need to store data for single request
- Temporary calculation results
- Request-specific logging context
- **Result:** Use local variables or request-scoped beans

### Use Application Scope When:
- True application-wide constants
- Application-wide configuration
- Shared lookup data (that never changes)
- **Result:** Use static final fields or configuration

## Additional Resources

- `/docs/architecture/multi-user-singleton-advisory.md` - Detailed patterns and examples
- `/docs/architecture/service-layer-patterns.md` - Service layer architecture
- `/docs/architecture/coding-standards.md` - General coding standards
- Spring Framework - Bean Scopes documentation
- Vaadin - Session Management documentation

## Questions to Ask

Before writing code, ask yourself:

1. **"Will this work if 100 users access it simultaneously?"**
   - If no, you need to fix the design

2. **"Am I storing user-specific data in a shared location?"**
   - If yes, move it to session or database

3. **"Could two users interfere with each other?"**
   - If yes, you have a data isolation problem

4. **"Is this state specific to one user or shared by all users?"**
   - User-specific → VaadinSession
   - Shared → Database or constant

5. **"Will this field's value change based on who's logged in?"**
   - If yes, it doesn't belong in the service

## Summary

**Golden Rule:** Services are stateless, state belongs in session or database.

**Remember:** Spring's `@Service` creates ONE instance shared by ALL users. Anything you store in instance fields is visible to EVERYONE.

**When in doubt:** Store in VaadinSession or retrieve from database each time.
