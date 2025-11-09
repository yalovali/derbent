# Bean Access Patterns in Derbent

## Overview

This document explains when to use constructor dependency injection vs. static bean access via `CSpringContext` in the Derbent multi-user web application.

## TL;DR - Quick Decision Guide

**Default:** Use **Constructor Dependency Injection** for 99% of cases.

**Exception:** Use `CSpringContext.getBean()` only when:
1. Class is manually instantiated (not Spring-managed), OR
2. Bean name is determined at runtime from configuration

## Current Usage Analysis

### Constructor Dependency Injection (Preferred) ✅

**Usage:** 367+ classes  
**Pattern:** Spring manages lifecycle and dependencies

```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    private final IActivityRepository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    // ✅ CORRECT: All dependencies injected via constructor
    public CActivityService(
            IActivityRepository repository,
            Clock clock,
            ISessionService sessionService) {
        super(repository, clock);
        this.sessionService = sessionService;
    }
}
```

**Benefits:**
- ✅ Explicit dependencies (clear what class needs)
- ✅ Easy to test (mock dependencies)
- ✅ Immutable (dependencies set once)
- ✅ Spring-managed lifecycle
- ✅ Fails fast if dependency missing

### Static Bean Access via CSpringContext ⚠️

**Usage:** 34 occurrences  
**Pattern:** Service Locator - manual bean lookup

**Legitimate Use Cases:**

#### 1. Manual Instantiation (CDataInitializer)

```java
public class CDataInitializer {
    // Class is instantiated with 'new' keyword in multiple places
    // Not managed by Spring, but needs access to many services
    
    public CDataInitializer(final ISessionService sessionService) {
        // ✅ ACCEPTABLE: Manual instantiation context
        this.gridEntityService = CSpringContext.getBean(CGridEntityService.class);
        this.projectService = CSpringContext.getBean(CProjectService.class);
        // ... 28 more services
    }
}

// Usage in CCustomLoginView:
final CDataInitializer init = new CDataInitializer(sessionService);
init.initializeSampleData();
```

**Why acceptable:**
- Class is NOT a Spring bean (no @Service/@Component)
- Instantiated on-demand with `new` keyword
- Cannot use constructor DI (Spring doesn't manage it)
- Alternative would require passing 30+ parameters

#### 2. Dynamic Bean Lookup (CComponentGridEntity)

```java
public class CComponentGridEntity {
    // Bean name comes from database configuration at runtime
    
    public void initializeGrid(CGridEntity gridEntity) {
        // ✅ ACCEPTABLE: Bean name determined dynamically
        String beanName = gridEntity.getDataServiceBeanName(); // e.g., "activityService"
        CAbstractService<?> service = CSpringContext.getBean(beanName);
    }
}
```

**Why acceptable:**
- Bean name determined at runtime from configuration
- Cannot inject statically (don't know which service until runtime)
- Configuration-driven architecture requires dynamic lookup

## Anti-Patterns to Avoid

### ❌ DON'T: Use Static Access in Spring-Managed Beans

```java
@Service  // ❌ BAD: Spring bean using service locator
public class CBadExampleService {
    private final CActivityService activityService;
    
    public CBadExampleService() {
        // ❌ WRONG: Spring bean should use constructor injection
        this.activityService = CSpringContext.getBean(CActivityService.class);
    }
}
```

**Should be:**

```java
@Service  // ✅ GOOD: Spring bean using constructor DI
public class CGoodExampleService {
    private final CActivityService activityService;
    
    // ✅ CORRECT: Let Spring inject the dependency
    public CGoodExampleService(CActivityService activityService) {
        this.activityService = activityService;
    }
}
```

### ❌ DON'T: Use @Autowired Field Injection

```java
@Service
public class CBadFieldInjection {
    // ❌ DISCOURAGED: Field injection
    @Autowired
    private CActivityService activityService;
}
```

**Why avoid:**
- Cannot make field `final`
- Dependencies not explicit
- Harder to test
- Can cause circular dependencies

## Multi-User Safety Analysis

### Both Patterns Are Safe ✅

**CSpringContext pattern is safe because:**
1. CSpringContext itself is stateless
2. Only provides access to singleton beans
3. No mutable static state
4. Thread-safe by design

**Constructor DI pattern is safe because:**
1. Dependencies are immutable (final)
2. Services are stateless singletons
3. No user-specific state in service fields
4. All user state stored in VaadinSession

### Multi-User Best Practices

```java
@Service
public class CUserService {
    // ✅ GOOD: Only immutable dependencies
    private final IUserRepository repository;
    private final Clock clock;
    
    public List<CUser> findByCompany() {
        // ✅ GOOD: Get context from session each time
        CCompany company = sessionService.getCurrentCompany();
        return repository.findByCompany_Id(company.getId());
    }
}
```

**See also:** `docs/architecture/multi-user-singleton-advisory.md`

## Decision Tree

```
Need to access a Spring bean?
│
├─ Is your class a Spring bean (@Service/@Component)?
│  │
│  ├─ YES → Use Constructor DI ✅
│  │
│  └─ NO → Is it manually instantiated?
│     │
│     └─ YES → CSpringContext.getBean() acceptable ⚠️
│
└─ Is bean name determined at runtime?
   │
   └─ YES → CSpringContext.getBean(name) acceptable ⚠️
```

## Testing Considerations

### Constructor DI: Easy to Test ✅

```java
@Test
void testActivityService() {
    // ✅ Easy to mock dependencies
    IActivityRepository mockRepo = mock(IActivityRepository.class);
    Clock mockClock = mock(Clock.class);
    ISessionService mockSession = mock(ISessionService.class);
    
    CActivityService service = new CActivityService(mockRepo, mockClock, mockSession);
    // Test away!
}
```

### Static Access: Harder to Test ⚠️

```java
@Test
void testDataInitializer() {
    // ⚠️ Requires Spring context to be running
    // Cannot easily mock CSpringContext.getBean()
    // Must use @SpringBootTest
}
```

## Guidelines for New Code

1. **Default to Constructor DI** for all Spring-managed beans
2. **Use CSpringContext only when:**
   - Class is manually instantiated (not Spring-managed)
   - Bean name determined at runtime from configuration
   - Absolutely no other choice (rare)
3. **Never use CSpringContext in:**
   - @Service classes
   - @Component classes
   - @Controller classes
   - Vaadin views and components managed by Spring

## Refactoring Guide

If you find inappropriate use of CSpringContext in a Spring-managed bean:

### Step 1: Identify the Problem

```java
@Service  // This is a Spring bean!
public class CProblematicService {
    private final CUserService userService;
    
    public CProblematicService() {
        // ❌ Problem: Using service locator in Spring bean
        this.userService = CSpringContext.getBean(CUserService.class);
    }
}
```

### Step 2: Refactor to Constructor DI

```java
@Service
public class CFixedService {
    private final CUserService userService;
    
    // ✅ Solution: Add parameter to constructor
    public CFixedService(CUserService userService) {
        this.userService = userService;
    }
}
```

### Step 3: Update Tests

```java
@Test
void testFixedService() {
    // Now easy to test!
    CUserService mockService = mock(CUserService.class);
    CFixedService service = new CFixedService(mockService);
}
```

## Current Project Status

### ✅ Compliant Usage

After analysis, Derbent's current usage is **CORRECT**:

1. **CDataInitializer** - Manually instantiated, acceptable use of CSpringContext
2. **CComponentGridEntity** - Dynamic bean lookup, acceptable use
3. **All 367+ services** - Using constructor DI correctly

### No Refactoring Needed

The 34 occurrences of `CSpringContext.getBean()` are all in legitimate contexts where constructor DI is not feasible.

## References

- [Spring Framework: Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Multi-User Singleton Advisory](multi-user-singleton-advisory.md)
- [Service Layer Patterns](service-layer-patterns.md)
- [Coding Standards](coding-standards.md)

## Revision History

- 2025-10-25: Initial version - Bean access pattern analysis
