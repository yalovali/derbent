# Circular Dependency Resolution Pattern

## Problem

When a default constructor was removed from `CUser`, a circular dependency error occurred:

```
The dependencies of some of the beans in the application context form a cycle:

   CActivityPriorityService
┌─────┐
|  primarySessionService (CSessionServiceConfig)
↑     ↓
|  CSessionService (CWebSessionService)
↑     ↓
|  CSecurityConfig
↑     ↓
|  CUserService
└─────┘
```

## Root Cause

The circular dependency was caused by constructor injection in `CUserService`:

1. `CSecurityConfig` needs `CUserService` (for UserDetailsService)
2. `CUserService` constructor required `ISessionService` parameter
3. `ISessionService` (via `CSessionServiceConfig`) is created after `CSecurityConfig`
4. This created a circular dependency chain

## Solution

The solution follows the **Configuration class with @PostConstruct pattern** that was already established in the codebase:

### Step 1: Modified CEntityNamedService

Added a two-parameter constructor (without ISessionService) to `CEntityNamedService`:

```java
public abstract class CEntityNamedService<EntityClass extends CEntityNamed<EntityClass>> 
        extends CAbstractService<EntityClass> {

    /** Constructor without session service. Use this constructor when session service 
     * will be injected via setter method to avoid circular dependencies. */
    public CEntityNamedService(final IAbstractNamedRepository<EntityClass> repository, 
                              final Clock clock) {
        super(repository, clock);
    }

    public CEntityNamedService(final IAbstractNamedRepository<EntityClass> repository, 
                              final Clock clock, 
                              final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
}
```

### Step 2: Modified CUserService Constructor

Changed the constructor to NOT take `ISessionService` as a parameter:

```java
@Service
public class CUserService extends CEntityNamedService<CUser> implements UserDetailsService {
    private ISessionService sessionService;

    // BEFORE: Constructor with sessionService parameter (causes circular dependency)
    // public CUserService(final IUserRepository repository, final Clock clock, 
    //                     final ISessionService sessionService) {
    //     super(repository, clock, sessionService);
    // }

    // AFTER: Constructor without sessionService parameter
    public CUserService(final IUserRepository repository, final Clock clock) {
        super(repository, clock);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    // Setter for session service (called by UserServiceConfiguration)
    @Override
    public void setSessionService(final ISessionService sessionService) { 
        this.sessionService = sessionService; 
    }
}
```

### Step 3: Using UserServiceConfiguration

The existing `UserServiceConfiguration` class already handles the setter injection via `@PostConstruct`:

```java
@Configuration
public class UserServiceConfiguration {
    private final CUserService userService;
    private final ISessionService sessionService;

    public UserServiceConfiguration(final CUserService userService, 
                                   final ISessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostConstruct
    public void configureServices() {
        userService.setSessionService(sessionService);
        LOGGER.info("UserServiceConfiguration: Successfully configured CUserService with CSessionService");
    }
}
```

## How It Works

1. **Bean Creation Phase**: 
   - Spring creates `CUserService` bean using the two-parameter constructor (no sessionService)
   - Spring creates `ISessionService` bean
   - Spring creates `UserServiceConfiguration` bean with both dependencies

2. **Post-Construction Phase**:
   - After all beans are created, Spring calls methods annotated with `@PostConstruct`
   - `UserServiceConfiguration.configureServices()` is called
   - This method injects the sessionService into CUserService via setter

3. **Result**:
   - No circular dependency during bean creation
   - All dependencies are properly wired after construction

## Pattern Summary

This pattern follows the established guidelines in the codebase:

- Use **Constructor Injection** for required dependencies that don't create circular dependencies
- Use **Setter Injection with @PostConstruct** for dependencies that would create circular dependencies
- Create a `@Configuration` class to wire dependencies in the `@PostConstruct` phase

## Related Configuration Classes

Similar patterns exist in the codebase:

- `SessionConfiguration` - handles circular dependency between SessionService and LayoutService
- `UserServiceConfiguration` - handles circular dependency between UserService and SessionService

## References

- Spring Framework Documentation: [Circular Dependencies](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-dependency-resolution)
- Related files:
  - `src/main/java/tech/derbent/users/config/UserServiceConfiguration.java`
  - `src/main/java/tech/derbent/session/config/SessionConfiguration.java`
  - `src/main/java/tech/derbent/api/services/CAbstractService.java`
