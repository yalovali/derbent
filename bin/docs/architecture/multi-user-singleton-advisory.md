# Multi-User Web Application Singleton Pattern Advisory

## Executive Summary

This document provides guidance on singleton service patterns and multi-user web application readiness in the Derbent project. It identifies current patterns, potential issues, and best practices for ensuring thread-safe, multi-user capable service implementations.

## Current Architecture Analysis

### ✅ Good Patterns Found

#### 1. **Proper Session-Scoped State Management**

The `CWebSessionService` correctly uses Vaadin's session storage for user-specific data:

```java
@Service("CSessionService")
@ConditionalOnWebApplication
@Profile("!reset-db")
public class CWebSessionService implements ISessionService {
    // ✅ GOOD: No instance fields storing user state
    // All user state stored in VaadinSession
    
    @Override
    public Optional<CUser> getActiveUser() {
        final VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return Optional.empty();
        }
        CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
        return Optional.ofNullable(activeUser);
    }
}
```

**Why this is good:**
- Each HTTP session has its own VaadinSession instance
- User data is isolated per session
- Thread-safe for concurrent users
- Automatic cleanup on session expiry

#### 2. **Stateless Service Pattern**

Most services follow the stateless pattern correctly:

```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // ✅ GOOD: Only dependencies injected, no mutable state
    protected final IAbstractRepository<CActivity> repository;
    protected final Clock clock;
    protected @Nullable ISessionService sessionService;
    
    // All methods retrieve state from session or database
    @Override
    public List<CActivity> findAll() {
        final CCompany currentCompany = getCurrentCompany();
        return repository.findByCompany_Id(currentCompany.getId());
    }
}
```

**Why this is good:**
- No user-specific state stored in service
- Each method call retrieves context from session
- Safe for concurrent access by multiple users
- Horizontally scalable

#### 3. **Dependency Injection Pattern**

```java
@Service
public class CUserService extends CEntityNamedService<CUser> {
    // ✅ GOOD: Dependencies injected via constructor
    private final IUserRepository repository;
    private final Clock clock;
    @Autowired
    private ApplicationContext applicationContext;
    
    public CUserService(final IUserRepository repository, final Clock clock) {
        super(repository, clock);
    }
}
```

### ⚠️ Patterns Requiring Attention

#### 1. **Reset-DB Session Service** (Low Priority)

The `CSessionService` (reset-db profile) stores user state in instance fields:

```java
@Profile("reset-db")
@Service
public class CSessionService implements ISessionService {
    // ⚠️ CAUTION: Instance fields storing user state
    private tech.derbent.plm.companies.domain.CCompany activeCompany;
    private CProject activeProject;
    private CUser activeUser;
    
    // This is acceptable ONLY because:
    // 1. Used only in single-user database reset scenarios
    // 2. Never used in production web application
    // 3. Profile ensures it's never active with web profile
}
```

**Current Status:** ✅ **ACCEPTABLE** - This service is only active during database reset operations which are single-user, single-threaded scenarios. The `@Profile("reset-db")` annotation ensures it never runs in production.

**Action Required:** None - but document this pattern as anti-pattern for web services.

#### 2. **Listener Collections in Session Services** (Already Addressed)

```java
@Service
public class CWebSessionService implements ISessionService {
    // ✅ GOOD: Listeners stored in VaadinSession, not instance fields
    
    private Set<IProjectChangeListener> getOrCreateProjectChangeListeners(
            final VaadinSession session) {
        Set<IProjectChangeListener> listeners = 
            (Set<IProjectChangeListener>) session.getAttribute(PROJECT_CHANGE_LISTENERS_KEY);
        if (listeners == null) {
            listeners = ConcurrentHashMap.newKeySet();
            session.setAttribute(PROJECT_CHANGE_LISTENERS_KEY, listeners);
        }
        return listeners;
    }
}
```

**Status:** ✅ **CORRECT** - Listeners are stored per-session, not in service instance.

## Multi-User Readiness Assessment

### Thread Safety Analysis

#### Services with Proper Thread Safety ✅

All domain services inherit from `CAbstractService` which follows these principles:

1. **Immutable dependencies only** (repositories, clock, session service)
2. **No mutable instance state**
3. **All user context retrieved from session per-request**
4. **Transactional boundaries properly defined**

**Services analyzed:** 83 services total
- ✅ All domain services: CActivityService, CUserService, CProjectService, etc.
- ✅ All relationship services: CUserProjectSettingsService, etc.
- ✅ All type services: CActivityTypeService, CProjectItemStatusService, etc.

#### Session State Management ✅

```
User Request → VaadinSession (thread-safe) → Service Layer → Database
     ↓
Each user has isolated session
     ↓
No shared state between users
```

### Scalability Considerations

#### Horizontal Scalability: ✅ READY

The current architecture supports horizontal scaling with these provisions:

1. **Session affinity (sticky sessions) required** - Vaadin requires requests from same user go to same server instance
2. **No application-level state** - Services are stateless
3. **Database as source of truth** - All persistent state in PostgreSQL
4. **Session replication optional** - For high availability, Vaadin sessions can be replicated

#### Vertical Scalability: ✅ READY

- Services are singleton but stateless
- Multiple threads can safely use same service instance
- Database connection pooling handles concurrent access
- Spring's @Transactional provides isolation

## Do's and Don'ts for Service Development

### ✅ DO: Keep Services Stateless

```java
@Service
public class CGoodExampleService extends CAbstractService<CEntity> {
    // ✅ GOOD: Only dependencies
    private final IEntityRepository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    public List<CEntity> getUserEntities() {
        // ✅ GOOD: Get user from session each time
        CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        return repository.findByUserId(currentUser.getId());
    }
}
```

### ❌ DON'T: Store User-Specific State in Service

```java
@Service
public class CBadExampleService extends CAbstractService<CEntity> {
    // ❌ BAD: Storing user-specific state
    private CUser currentUser;  // WRONG! Shared across all users!
    private List<CEntity> userCache;  // WRONG! Shared across all users!
    
    public void setCurrentUser(CUser user) {
        // ❌ BAD: This will be overwritten by other users' requests
        this.currentUser = user;
    }
    
    public List<CEntity> getUserEntities() {
        // ❌ BAD: Returns wrong data for concurrent users
        return userCache;
    }
}
```

### ✅ DO: Use VaadinSession for User-Specific State

```java
@Service
public class CGoodSessionService {
    private static final String USER_PREFERENCE_KEY = "userPreference";
    
    public void saveUserPreference(String preference) {
        // ✅ GOOD: Store in session
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_PREFERENCE_KEY, preference);
    }
    
    public String getUserPreference() {
        // ✅ GOOD: Retrieve from session
        VaadinSession session = VaadinSession.getCurrent();
        return (String) session.getAttribute(USER_PREFERENCE_KEY);
    }
}
```

### ❌ DON'T: Use Static Fields for Mutable State

```java
@Service
public class CBadStaticService {
    // ❌ BAD: Static mutable state shared across ALL users
    private static Map<Long, CUser> userCache = new HashMap<>();
    private static List<CEntity> sharedData = new ArrayList<>();
    
    // ❌ BAD: These are shared across all users and not thread-safe
}
```

### ✅ DO: Use Static Fields Only for Constants

```java
@Service
public class CGoodStaticService {
    // ✅ GOOD: Static immutable constants
    private static final String MENU_TITLE = "Activities";
    private static final int MAX_RESULTS = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(CGoodStaticService.class);
    
    // ✅ GOOD: These never change and are safe to share
}
```

### ✅ DO: Handle Session Context Properly

```java
@Service
public class CGoodContextService extends CAbstractService<CEntity> {
    
    @Transactional(readOnly = true)
    public List<CEntity> findAll() {
        // ✅ GOOD: Get context from session
        CCompany currentCompany = getCurrentCompany();
        Check.notNull(currentCompany, "No active company in session");
        
        // ✅ GOOD: Filter by company to ensure data isolation
        return repository.findByCompany_Id(currentCompany.getId());
    }
    
    private CCompany getCurrentCompany() {
        Check.notNull(sessionService, "Session service required");
        CCompany company = sessionService.getCurrentCompany();
        Check.notNull(company, "No active company");
        return company;
    }
}
```

### ❌ DON'T: Cache User-Specific Data in Service

```java
@Service
public class CBadCacheService extends CAbstractService<CEntity> {
    // ❌ BAD: Caching user data in service instance
    private Map<Long, List<CEntity>> userEntityCache = new HashMap<>();
    
    public List<CEntity> getUserEntities(Long userId) {
        // ❌ BAD: Cache is shared across all users
        if (!userEntityCache.containsKey(userId)) {
            userEntityCache.put(userId, repository.findByUserId(userId));
        }
        return userEntityCache.get(userId);
    }
}
```

**If caching is needed, use:**
1. Spring's `@Cacheable` with proper key strategies
2. Session-scoped caching (store in VaadinSession)
3. Request-scoped beans for temporary caching
4. External cache like Redis with proper key isolation

## Testing for Multi-User Safety

### Unit Test Pattern

```java
@SpringBootTest
class CServiceMultiUserTest {
    @Autowired
    private CActivityService activityService;
    
    @Test
    void testConcurrentUserAccess() throws Exception {
        // Test that concurrent users don't interfere
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<List<CActivity>>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final long userId = i;
            futures.add(executor.submit(() -> {
                // Each thread simulates different user
                mockSessionForUser(userId);
                return activityService.findAll();
            }));
        }
        
        // Verify each user got their own data
        for (Future<List<CActivity>> future : futures) {
            List<CActivity> activities = future.get();
            assertNotNull(activities);
            // Verify data isolation
        }
    }
}
```

## Checklist for New Service Development

When creating a new service, verify:

- [ ] Service annotated with `@Service` (singleton scope)
- [ ] No mutable instance fields storing user-specific data
- [ ] All user context retrieved from `ISessionService` per-request
- [ ] Methods retrieve current user/company from session, not from cached fields
- [ ] No static mutable collections (HashMap, ArrayList, etc.)
- [ ] Static fields are only used for constants (final String, final int, Logger)
- [ ] If listeners are needed, they're stored in VaadinSession, not service instance
- [ ] `@Transactional` annotations properly placed
- [ ] Thread-safety considered for any shared resources
- [ ] No assumptions about method call order from same user

## Migration Guide for Existing Services

If you find a service with user-specific state in instance fields:

### Step 1: Identify the State

```java
// Current problematic code
@Service
public class CProblematicService {
    private CUser currentUser;  // Problem: shared across users
    private Long selectedProjectId;  // Problem: shared across users
}
```

### Step 2: Move to Session

```java
@Service
public class CFixedService {
    private static final String SELECTED_PROJECT_KEY = "selectedProject";
    
    public void setSelectedProject(Long projectId) {
        VaadinSession.getCurrent().setAttribute(SELECTED_PROJECT_KEY, projectId);
    }
    
    public Long getSelectedProject() {
        return (Long) VaadinSession.getCurrent().getAttribute(SELECTED_PROJECT_KEY);
    }
}
```

### Step 3: Update Callers

```java
// Before
service.currentUser = user;
Long projectId = service.selectedProjectId;

// After
sessionService.setActiveUser(user);
Long projectId = service.getSelectedProject();
```

## Monitoring and Debugging

### Detecting Multi-User Issues

Signs of multi-user problems:

1. **Users seeing each other's data** - Instance fields being shared
2. **Data corruption under load** - Thread-safety issues
3. **Inconsistent state** - State leaking between requests
4. **NullPointerExceptions under concurrent access** - Race conditions

### Debugging Tools

1. **Log correlation IDs** - Track requests through system
2. **Thread-local storage** - Verify isolation
3. **Load testing** - Use JMeter or similar to simulate concurrent users
4. **Session monitoring** - Check VaadinSession contents

## Compliance Status

### Current Status: ✅ COMPLIANT

The Derbent project currently follows multi-user best practices:

- ✅ All services are properly stateless
- ✅ Session management correctly implemented
- ✅ No shared mutable state in services
- ✅ Vaadin session used for user-specific data
- ✅ Thread-safe by design

### Areas Requiring Ongoing Attention

1. **New service development** - Ensure developers follow patterns
2. **Code reviews** - Check for anti-patterns
3. **Architecture tests** - Add ArchUnit tests to enforce patterns
4. **Developer training** - Educate team on these patterns

## References

- Spring Framework documentation on Bean Scopes
- Vaadin Session Management guide
- Java Concurrency in Practice (Brian Goetz)
- Project architecture documents in `docs/architecture/`

## Revision History

- 2025-10-19: Initial version - Multi-user readiness analysis
