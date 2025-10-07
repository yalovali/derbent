# Quick Reference: ISessionService Dependency Injection

## Quick Answer

**Q: Does having ISessionService in service constructors violate separation of concerns?**

**A: NO ✅ - This is the correct and recommended approach.**

---

## Pattern Cheat Sheet

### ✅ CORRECT Patterns

#### Pattern 1: Constructor Injection (Preferred - 95% of cases)

```java
@Service
public class CProjectService extends CAbstractNamedEntityService<CProject> {
    
    public CProjectService(final IProjectRepository repository, 
                          final Clock clock, 
                          final ISessionService sessionService,
                          final ApplicationEventPublisher eventPublisher) {
        super(repository, clock, sessionService);
        this.eventPublisher = eventPublisher;
    }
    
    public List<CProject> findAll() {
        // Use session to get context
        CCompany company = sessionService.getCurrentCompany();
        return repository.findByCompanyId(company.getId());
    }
}
```

**When to use:**
- Service needs session for business logic
- No circular dependencies
- Default choice for most services

**Examples in codebase:**
- CProjectService
- CActivityService  
- CMeetingService
- CDecisionService
- Most services (40+ services)

---

#### Pattern 2: Setter Injection (Exception - 5% of cases)

```java
@Service
public class CUserService extends CAbstractNamedEntityService<CUser> {
    
    private ISessionService sessionService;
    
    public CUserService(final IUserRepository repository, final Clock clock) {
        super(repository, clock);
    }
    
    public void setSessionService(final ISessionService sessionService) {
        this.sessionService = sessionService;
    }
}
```

**Configuration class needed:**
```java
@Configuration
public class UserServiceConfiguration {
    
    @PostConstruct
    public void configureServices() {
        userService.setSessionService(sessionService);
    }
}
```

**When to use:**
- Circular dependency exists (UserService ↔ SessionService)
- Rare edge cases only

**Examples in codebase:**
- CUserService (due to circular dependency with session)

---

### ❌ WRONG Patterns (Anti-Patterns)

#### Anti-Pattern 1: Session in Entity Constructor

```java
// ❌ NEVER DO THIS!
public class CProject extends CEntityNamed<CProject> {
    
    private ISessionService sessionService;
    
    // WRONG!
    public CProject(String name, ISessionService sessionService) {
        super(CProject.class, name);
        this.sessionService = sessionService;  // ❌ Violates SoC
    }
}
```

**Why wrong:**
- Entities are domain objects, not application components
- Breaks JPA serialization
- Can't be used in batch jobs or tests
- Violates Single Responsibility Principle

---

#### Anti-Pattern 2: Static Session Access

```java
// ❌ NEVER DO THIS!
@Service
public class CProjectService {
    
    public List<CProject> findAll() {
        // WRONG! Hidden dependency
        CCompany company = SessionHolder.getCurrentCompany();  // ❌
        return repository.findByCompanyId(company.getId());
    }
}
```

**Why wrong:**
- Hidden dependency (not in constructor)
- Can't test with mocks
- Tight coupling to concrete implementation
- Violates Dependency Injection principle

---

#### Anti-Pattern 3: Session in Every Method

```java
// ❌ NEVER DO THIS!
@Service
public class CProjectService {
    
    // WRONG! Repetitive and burdensome
    public List<CProject> findAll(ISessionService sessionService) {  // ❌
        CCompany company = sessionService.getCurrentCompany();
        return repository.findByCompanyId(company.getId());
    }
    
    public CProject save(CProject project, ISessionService sessionService) {  // ❌
        // Every method needs the parameter!
        return repository.save(project);
    }
}
```

**Why wrong:**
- Repetitive code
- Burdensome for callers
- Makes API harder to use
- Just moves the problem around

---

## Decision Tree

```
Do you need ISessionService?
│
├─ In an ENTITY? ────────────────► ❌ NO! Never use in entities
│
├─ In a SERVICE?
│  │
│  ├─ Is there a circular dependency? 
│  │  │
│  │  ├─ YES ─────────────────────► ✅ Use setter injection
│  │  │                                + Configuration class
│  │  │
│  │  └─ NO ──────────────────────► ✅ Use constructor injection
│  │                                   (preferred)
│
└─ In a VIEW? ───────────────────────► ✅ Yes, use constructor injection
                                         or @Autowired
```

---

## Testing Cheat Sheet

### Testing Services with Constructor Injection

```java
@ExtendWith(MockitoExtension.class)
class CProjectServiceTest {
    
    @Mock private IProjectRepository repository;
    @Mock private Clock clock;
    @Mock private ISessionService sessionService;  // ← Easy to mock!
    @Mock private ApplicationEventPublisher eventPublisher;
    
    private CProjectService service;
    
    @BeforeEach
    void setUp() {
        // Create service with mocked dependencies
        service = new CProjectService(repository, clock, sessionService, eventPublisher);
    }
    
    @Test
    void testFindAll() {
        // Setup mock behavior
        CCompany testCompany = new CCompany("Test Corp");
        when(sessionService.getCurrentCompany()).thenReturn(testCompany);
        
        // Execute
        List<CProject> projects = service.findAll();
        
        // Verify
        verify(repository).findByCompanyId(testCompany.getId());
    }
}
```

---

## Common Questions

### Q1: "Isn't this tight coupling?"

**A: No** - We inject the **interface** ISessionService, not the concrete class.

```java
// ✅ CORRECT - Depends on interface
public CProjectService(ISessionService sessionService)

// ❌ WRONG - Depends on concrete class
public CProjectService(CWebSessionService sessionService)
```

---

### Q2: "Why not pass context in method parameters?"

**A:** Context is needed by **all** methods, not just some:

```java
// ❌ Repetitive and burdensome
public List<CProject> findAll(ISessionService session) { ... }
public CProject findById(Long id, ISessionService session) { ... }
public CProject save(CProject project, ISessionService session) { ... }
public void delete(CProject project, ISessionService session) { ... }

// ✅ Clean and simple
public CProjectService(ISessionService sessionService) {
    this.sessionService = sessionService;
}

public List<CProject> findAll() { ... }
public CProject findById(Long id) { ... }
public CProject save(CProject project) { ... }
public void delete(CProject project) { ... }
```

---

### Q3: "What about services that don't need session?"

**A:** They simply don't inject it:

```java
// Service that doesn't need session context
@Service
public class CActivityTypeService extends CAbstractNamedEntityService<CActivityType> {
    
    public CActivityTypeService(final IActivityTypeRepository repository, 
                               final Clock clock) {
        super(repository, clock);
        // No sessionService - not needed!
    }
}
```

---

### Q4: "How is this different from Repository injection?"

**A:** It's the **same pattern**!

```java
// Repository injection - ACCEPTED by everyone
public CProjectService(IProjectRepository repository) { ... }

// Session injection - SAME PATTERN!
public CProjectService(ISessionService sessionService) { ... }

Both are legitimate dependencies of the service layer.
```

---

## Verification Checklist

Use this checklist to verify proper separation of concerns:

### Services ✅
- [ ] Inject ISessionService via constructor (preferred)
- [ ] Or inject via setter for circular dependencies
- [ ] Use interface ISessionService, not concrete class
- [ ] Mark field as `protected @Nullable ISessionService`
- [ ] Check for null before using: `if (sessionService != null)`

### Entities ✅
- [ ] NO ISessionService in constructor
- [ ] NO ISessionService field
- [ ] NO session-related methods
- [ ] Pure JPA/domain objects
- [ ] Serializable and detachable

### Repositories ✅
- [ ] NO ISessionService dependency
- [ ] Pure data access interfaces
- [ ] Extend Spring Data repositories

### Views ✅
- [ ] MAY inject ISessionService (presentation layer)
- [ ] Use for UI state management
- [ ] Use for navigation context

---

## Key Takeaways

1. **Services CAN and SHOULD inject ISessionService**
   - They need context for business logic
   - This is proper separation of concerns

2. **Entities MUST NOT have ISessionService**
   - They are pure domain objects
   - Must remain serializable and JPA-compatible

3. **Use interface, not concrete class**
   - Enables testing with mocks
   - Allows different implementations

4. **Constructor injection is preferred**
   - Explicit dependencies
   - Immutable (final fields)
   - Easy to test

5. **This pattern is correct and proven**
   - Used by 40+ services in the codebase
   - Follows Spring Framework best practices
   - Documented in project architecture guides

---

## References

- `docs/architecture/SEPARATION_OF_CONCERNS_SESSION_ANALYSIS.md` - Detailed analysis
- `docs/architecture/VISUAL_SEPARATION_OF_CONCERNS.md` - Visual guide
- `docs/architecture/session-management.md` - Session management patterns
- `docs/VAADIN_SESSION_ANALYSIS.md` - Original session analysis

---

## TL;DR

✅ **ISessionService in service constructors is CORRECT**

✅ **This does NOT violate separation of concerns**

✅ **No changes needed to the current architecture**
