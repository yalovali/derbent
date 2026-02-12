# ISessionService Access Guide

**Date**: 2026-02-12  
**Status**: ✅ **COMPREHENSIVE GUIDE**

## What is ISessionService?

`ISessionService` provides access to:
- **Active user** - Currently logged-in user
- **Active company** - Current company context
- **Active project** - Current project context
- **Session storage** - User-specific key-value storage
- **Project listeners** - Project change notifications

## Access Patterns (Choose Based on Class Type)

### Pattern 1: Constructor Injection (PREFERRED - Services)

**When to use**: Spring-managed beans (@Service, @Component)

**✅ BEST PRACTICE**:
```java
@Service
public class CMyService extends CAbstractService<CMyEntity> {
    
    // 1. Declare as final field
    private final ISessionService sessionService;
    
    // 2. Inject via constructor
    public CMyService(
        final IMyRepository repository,
        final Clock clock,
        final ISessionService sessionService) {  // ✅ Injected by Spring
        
        super(repository, clock, sessionService);
        this.sessionService = sessionService;
    }
    
    // 3. Use throughout class
    @Override
    public void initializeNewEntity(final CMyEntity entity) {
        super.initializeNewEntity(entity);
        
        // Get active user
        final CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        
        entity.setCreatedBy(currentUser);
        
        // Get active project
        final CProject<?> currentProject = sessionService.getActiveProject()
            .orElseThrow(() -> new IllegalStateException("No active project"));
        
        entity.setProject(currentProject);
    }
}
```

**Benefits**:
- ✅ Type-safe
- ✅ Testable (can mock in tests)
- ✅ Spring-managed lifecycle
- ✅ Singleton shared across app
- ✅ Thread-safe

### Pattern 2: CSpringContext.getBean() (Static Access - Views/Components)

**When to use**: Non-Spring-managed classes (Vaadin components, dialogs, utilities)

**✅ CORRECT**:
```java
public class CMyDialog extends CDialog {
    
    private final ISessionService sessionService;
    
    public CMyDialog() {
        super();
        
        // Get session service via Spring context
        this.sessionService = CSpringContext.getBean(ISessionService.class);
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Use session service
        final CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        
        final Span userLabel = new Span("Current User: " + currentUser.getName());
        add(userLabel);
    }
}
```

**When NOT to use**:
- ❌ In @Service classes (use constructor injection instead)
- ❌ In @Component classes (use constructor injection instead)

### Pattern 3: Pass as Parameter (Component Factory Methods)

**When to use**: Creating components from service methods

**✅ CORRECT**:
```java
@Service
public class CPageServiceSystemSettings extends CPageServiceBase<CSystemSettings_Derbent> {
    
    private final ISessionService sessionService;
    
    public CPageServiceSystemSettings(
        final IPageServiceImplementer<CSystemSettings_Derbent> view,
        final ISessionService sessionService) {
        
        super(view);
        this.sessionService = sessionService;
    }
    
    /**
     * Create component with session service injected.
     * Called by CFormBuilder via @AMetaData createComponentMethod.
     */
    public Component createComponentEmailTest() {
        // Pass session service to component
        return new CComponentEmailTest(sessionService);
    }
}
```

**Component receives it**:
```java
public class CComponentEmailTest extends VerticalLayout {
    
    private final ISessionService sessionService;
    
    public CComponentEmailTest(final ISessionService sessionService) {
        this.sessionService = sessionService;  // Store for later use
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Use session service
        final CProject<?> project = sessionService.getActiveProject()
            .orElseThrow(() -> new IllegalStateException("No active project"));
        
        add(new Span("Current Project: " + project.getName()));
    }
}
```

### Pattern 4: Interface Default Method (Component Value Persistence)

**When to use**: Components implementing `IHasValuePersistence`

**✅ CORRECT**:
```java
public class CTextField extends TextField implements IHasValuePersistence<String> {
    
    // Interface provides default method
    @Override
    public ISessionService getSessionService() {
        return CSpringContext.getBean(ISessionService.class);
    }
    
    // Use via interface method
    private void saveValue() {
        final ISessionService sessionService = getSessionService();
        sessionService.setSessionValue("myField", getValue());
    }
}
```

## Common Use Cases

### Use Case 1: Get Active User

```java
// In service (injected)
final CUser currentUser = sessionService.getActiveUser()
    .orElseThrow(() -> new IllegalStateException("No active user"));

entity.setCreatedBy(currentUser);
```

### Use Case 2: Get Active Project

```java
// In service (injected)
final CProject<?> currentProject = sessionService.getActiveProject()
    .orElseThrow(() -> new IllegalStateException("No active project"));

entity.setProject(currentProject);
```

### Use Case 3: Get Active Company

```java
// In service (injected)
final CCompany currentCompany = sessionService.getActiveCompany()
    .orElseThrow(() -> new IllegalStateException("No active company"));

entity.setCompany(currentCompany);
```

### Use Case 4: Store Component Values

```java
// In component (static access)
final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);

// Store value
sessionService.setSessionValue("myComponent.lastValue", "test");

// Retrieve value
final Optional<String> lastValue = sessionService.getSessionValue("myComponent.lastValue");
lastValue.ifPresent(value -> textField.setValue(value));
```

### Use Case 5: Listen for Project Changes

```java
public class CMyComponent implements IProjectChangeListener {
    
    private final ISessionService sessionService;
    
    public CMyComponent(final ISessionService sessionService) {
        this.sessionService = sessionService;
        
        // Register as listener
        sessionService.addProjectChangeListener(this);
    }
    
    @Override
    public void onProjectChanged(final CProject<?> newProject) {
        // React to project change
        refreshData(newProject);
    }
    
    // Don't forget to unregister!
    public void destroy() {
        sessionService.removeProjectChangeListener(this);
    }
}
```

## Decision Tree: Which Pattern to Use?

```
Is your class a Spring-managed bean (@Service, @Component)?
├─ YES → Use Constructor Injection (Pattern 1)
│        ✅ PREFERRED - Type-safe, testable
│
└─ NO → Is it a Vaadin component/dialog?
    ├─ YES → Use CSpringContext.getBean() (Pattern 2)
    │        ✅ In constructor or initialization
    │
    └─ NO → Is it created by a service factory method?
            └─ YES → Pass as parameter (Pattern 3)
                     ✅ Service injects, component stores
```

## Real-World Examples

### Example 1: Service with Session Access

```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    
    private final ISessionService sessionService;
    
    public CActivityService(
        final IActivityRepository repository,
        final Clock clock,
        final ISessionService sessionService,
        final CActivityTypeService typeService) {
        
        super(repository, clock, sessionService);
        this.sessionService = sessionService;
    }
    
    @Override
    public void initializeNewEntity(final CActivity entity) {
        super.initializeNewEntity(entity);
        
        // Get current context
        final CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new CInitializationException("No active user"));
        final CProject<?> currentProject = sessionService.getActiveProject()
            .orElseThrow(() -> new CInitializationException("No active project"));
        
        // Initialize entity
        entity.setAssignedTo(currentUser);
        entity.setProject(currentProject);
    }
}
```

### Example 2: Dialog with Session Access

```java
public class CLdapTestDialog extends CDialog {
    
    private final ISessionService sessionService;
    
    public CLdapTestDialog() {
        super();
        
        // Static access for non-Spring component
        this.sessionService = CSpringContext.getBean(ISessionService.class);
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Use session service to get context
        final Optional<CUser> userOpt = sessionService.getActiveUser();
        if (userOpt.isPresent()) {
            final CUser user = userOpt.get();
            add(new Span("Testing as: " + user.getLogin()));
        }
    }
}
```

### Example 3: Component Created by Service

```java
// Service class
@Service
public class CPageServiceSystemSettings extends CPageServiceBase<CSystemSettings_Derbent> {
    
    private final ISessionService sessionService;
    
    public CPageServiceSystemSettings(
        final IPageServiceImplementer<CSystemSettings_Derbent> view,
        final ISessionService sessionService) {
        
        super(view);
        this.sessionService = sessionService;
    }
    
    // Factory method passes sessionService to component
    public Component createComponentLdapTest() {
        return new CComponentLdapTest(sessionService);
    }
}

// Component class
public class CComponentLdapTest extends CComponentBase<Integer> {
    
    private final ISessionService sessionService;
    
    public CComponentLdapTest(final ISessionService sessionService) {
        this.sessionService = sessionService;
        initializeComponents();
    }
    
    @Override
    protected void initializeComponents() {
        final CUser user = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No user"));
        
        // Use user info
    }
}
```

## Common Mistakes (DON'T DO THIS)

### ❌ WRONG: Storing User State in Service

```java
@Service
public class CBadService {
    // ❌ WRONG - Services are SINGLETON!
    // All users will overwrite this!
    private CUser currentUser;
    
    public void doSomething() {
        // ❌ BAD - Shared by all users!
        currentUser = sessionService.getActiveUser().orElse(null);
    }
}
```

### ❌ WRONG: Constructor Injection in Non-Spring Class

```java
// ❌ WRONG - Not a Spring bean, Spring won't inject!
public class CMyDialog extends CDialog {
    
    private final ISessionService sessionService;
    
    // ❌ WRONG - Spring won't call this constructor!
    public CMyDialog(final ISessionService sessionService) {
        this.sessionService = sessionService;
    }
}
```

**Fix**: Use `CSpringContext.getBean()` instead

### ❌ WRONG: Static Access in Service

```java
@Service
public class CBadService extends CAbstractService<CEntity> {
    
    // ❌ WRONG - Use constructor injection instead!
    public void doSomething() {
        final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
    }
}
```

**Fix**: Inject via constructor

## Thread Safety

**IMPORTANT**: `ISessionService` is **user-scoped**, NOT application-scoped

- ✅ Each HTTP session has its own `ISessionService` instance
- ✅ Thread-safe for single-user requests
- ❌ **DON'T store user data in service fields** (services are singleton!)
- ✅ **DO get user context from sessionService each time**

```java
// ✅ CORRECT - Get user per request
public void processRequest() {
    final CUser user = sessionService.getActiveUser()
        .orElseThrow(() -> new IllegalStateException("No user"));
    
    // Use user for this request only
}

// ❌ WRONG - Store user in field (shared by all users!)
private CUser currentUser;  // DON'T DO THIS!
```

## Quick Reference

| Class Type | Access Pattern | Example |
|------------|---------------|---------|
| **@Service** | Constructor injection | `public MyService(ISessionService sessionService)` |
| **@Component** | Constructor injection | `public MyComponent(ISessionService sessionService)` |
| **Dialog** | Static access | `CSpringContext.getBean(ISessionService.class)` |
| **Vaadin Component** | Static access | `CSpringContext.getBean(ISessionService.class)` |
| **Factory-created** | Pass as parameter | `new MyComponent(sessionService)` |

## Related Documentation

- `AGENTS.md` Section 5.5 - Multi-User Safety
- `CSpringContext.java` - Spring context access utilities
- `ISessionService.java` - Interface definition

## Conclusion

**Choose the right pattern**:
1. ✅ **Spring beans** → Constructor injection (preferred)
2. ✅ **Non-Spring classes** → `CSpringContext.getBean()`
3. ✅ **Factory-created** → Pass as parameter
4. ✅ **Never store user state** in service fields!

**Remember**: Services are singleton, ISessionService is user-scoped!
