# Session Management Architecture

## Overview

The Derbent application uses **Vaadin Session** for managing user-specific session state. This document explains the architecture, patterns, and best practices for session management.

## Key Principles

### 1. Session Scope: Per-User ✓

**Important**: VaadinSession is **per-user**, NOT per-application.

```java
// Each user has their own isolated VaadinSession
VaadinSession session = VaadinSession.getCurrent();
```

- **Each logged-in user** gets their own `VaadinSession` instance
- Session data is **isolated** between users
- Session data is **stored in-memory** or in a distributed cache (depending on configuration)
- Session data is **cleared** when the user logs out or the session expires

### 2. Entity Independence ✓

**Critical**: Domain entities must NOT depend on session management.

```java
// ✅ CORRECT - Entity constructors are clean
public CActivity(String name, CProject project) {
    super(CActivity.class, name, project);
    initializeDefaults();
}

// ❌ WRONG - Do NOT pass session to entities
public CActivity(String name, CProject project, ISessionService session) {
    // This breaks separation of concerns!
}
```

**Why?**
- Entities are JPA domain objects that should be serializable
- Entities may be detached from Hibernate session
- Entities should not have UI or session concerns
- This maintains proper separation between domain and application layers

### 3. Service Layer Session Access ✓

Services access session through **dependency injection** of the `ISessionService` interface.

```java
public class CActivityService extends CEntityOfProjectService<CActivity> {
    
    // Session service injected via constructor or setter
    protected @Nullable ISessionService sessionService;
    
    public CActivityService(IActivityRepository repository, Clock clock) {
        super(repository, clock);
    }
    
    // Use session service to get current user/project context
    public List<CActivity> getActivitiesForCurrentUser() {
        if (sessionService != null) {
            Optional<CUser> currentUser = sessionService.getActiveUser();
            Optional<CProject> currentProject = sessionService.getActiveProject();
            // ... use context for filtering
        }
        return repository.findAll();
    }
}
```

## Architecture Components

### ISessionService Interface

The common interface for all session service implementations:

```java
public interface ISessionService {
    // User management
    Optional<CUser> getActiveUser();
    void setActiveUser(CUser user);
    
    // Project management
    Optional<CProject> getActiveProject();
    void setActiveProject(CProject project);
    List<CProject> getAvailableProjects();
    
    // Company management
    Optional<CCompany> getActiveCompany();
    CCompany getCurrentCompany();
    
    // Active entity tracking
    Long getActiveId(String entityType);
    void setActiveId(String entityType, Long id);
    void deleteAllActiveIds();
    
    // Listeners for UI updates
    void addProjectChangeListener(IProjectChangeListener listener);
    void removeProjectChangeListener(IProjectChangeListener listener);
    void addProjectListChangeListener(IProjectListChangeListener listener);
    void removeProjectListChangeListener(IProjectListChangeListener listener);
    
    // Session lifecycle
    void clearSession();
    void notifyProjectListChanged();
    void handleProjectListChange(ProjectListChangeEvent event);
    void setLayoutService(CLayoutService layoutService);
}
```

### CWebSessionService (Web Applications)

Used in normal web application mode with Vaadin UI.

**Key Features:**
- Uses `VaadinSession.getCurrent()` for per-user session storage
- Stores session data as session attributes
- Thread-safe using `ConcurrentHashMap.newKeySet()` for listeners
- Handles UI updates via `UI.access()` for thread safety
- Automatically loads user from Spring Security context

**Profile:** Active when `!reset-db` profile

```java
@Service("CSessionService")
@ConditionalOnWebApplication
@Profile("!reset-db")
public class CWebSessionService implements ISessionService {
    
    @Override
    public Optional<CUser> getActiveUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return Optional.empty();
        }
        CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
        if (activeUser == null) {
            // Try to load from Spring Security context
            Optional<User> authenticatedUser = 
                authenticationContext.getAuthenticatedUser(User.class);
            if (authenticatedUser.isPresent()) {
                String username = authenticatedUser.get().getUsername();
                activeUser = userRepository.findByUsername(username).orElse(null);
                setActiveUser(activeUser);
            }
        }
        return Optional.ofNullable(activeUser);
    }
}
```

### CSessionService (Non-Web Applications)

Used for database reset and other non-web operations.

**Key Features:**
- Simple in-memory storage without Vaadin dependencies
- No UI notifications (no-op for listener methods)
- Auto-selects first available user/project for convenience
- Minimal functionality for batch operations

**Profile:** Active when `reset-db` profile

```java
@Profile("reset-db")
@Service
public class CSessionService implements ISessionService {
    
    private CUser activeUser;
    private CProject activeProject;
    private CCompany activeCompany;
    
    @Override
    public Optional<CUser> getActiveUser() {
        if (activeUser == null) {
            // For reset operations, auto-select first user
            List<CUser> allUsers = userRepository.findAll();
            activeUser = allUsers.isEmpty() ? null : allUsers.get(0);
        }
        return Optional.ofNullable(activeUser);
    }
}
```

## Usage Patterns

### Pattern 1: Getting Current User/Project in Services

```java
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    
    @Transactional(readOnly = true)
    public List<CActivity> getMyActivities() {
        if (sessionService == null) {
            LOGGER.warn("Session service not available");
            return Collections.emptyList();
        }
        
        Optional<CUser> currentUser = sessionService.getActiveUser();
        Optional<CProject> currentProject = sessionService.getActiveProject();
        
        if (currentUser.isEmpty() || currentProject.isEmpty()) {
            return Collections.emptyList();
        }
        
        return repository.findByProjectAndAssignedUser(
            currentProject.get(), 
            currentUser.get()
        );
    }
}
```

### Pattern 2: Tracking Active Entity in Views

```java
public class CActivityView extends CAbstractEntityDBPage<CActivity> {
    
    @Override
    protected void onGridSelectionChanged(CActivity selectedActivity) {
        super.onGridSelectionChanged(selectedActivity);
        
        if (selectedActivity != null && sessionService != null) {
            // Track the selected activity in session
            sessionService.setActiveId("CActivity", selectedActivity.getId());
        }
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Restore previously selected activity
        if (sessionService != null) {
            Long activeId = sessionService.getActiveId("CActivity");
            if (activeId != null) {
                service.getById(activeId).ifPresent(this::selectInGrid);
            }
        }
    }
}
```

### Pattern 3: Listening for Project Changes

```java
public class CActivityKanbanView extends Div implements IProjectChangeListener {
    
    @Autowired
    private ISessionService sessionService;
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Register for project change notifications
        if (sessionService != null) {
            sessionService.addProjectChangeListener(this);
        }
        
        refreshData();
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Unregister to prevent memory leaks
        if (sessionService != null) {
            sessionService.removeProjectChangeListener(this);
        }
        
        super.onDetach(detachEvent);
    }
    
    @Override
    public void onProjectChanged(CProject newProject) {
        // Refresh the kanban board when project changes
        getUI().ifPresent(ui -> ui.access(() -> {
            refreshData();
        }));
    }
}
```

### Pattern 4: Getting Session Service in Components

```java
// ✅ CORRECT - Use interface type
public class CMyComponent extends Div {
    private ISessionService sessionService;
    
    public CMyComponent() {
        if (ApplicationContextProvider.getApplicationContext() != null) {
            sessionService = ApplicationContextProvider.getApplicationContext()
                .getBean(ISessionService.class);
        }
    }
}

// ❌ WRONG - Don't use concrete type
public class CMyComponent extends Div {
    private ISessionService sessionService;
    
    public CMyComponent() {
        if (ApplicationContextProvider.getApplicationContext() != null) {
            // Wrong: Getting concrete class instead of interface
            sessionService = ApplicationContextProvider.getApplicationContext()
                .getBean(CSessionService.class);
        }
    }
}
```

## Best Practices

### DO ✅

1. **Use ISessionService interface** everywhere
   ```java
   @Autowired
   private ISessionService sessionService;
   ```

2. **Inject session service in services** via constructor or setter
   ```java
   public CActivityService(IActivityRepository repository, Clock clock,
                          ISessionService sessionService) {
       super(repository, clock);
       this.sessionService = sessionService;
   }
   ```

3. **Check for null before using** session service (it may be null in some contexts)
   ```java
   if (sessionService != null) {
       Optional<CUser> user = sessionService.getActiveUser();
   }
   ```

4. **Register and unregister listeners** properly to avoid memory leaks
   ```java
   @Override
   protected void onAttach(AttachEvent attachEvent) {
       sessionService.addProjectChangeListener(this);
   }
   
   @Override
   protected void onDetach(DetachEvent detachEvent) {
       sessionService.removeProjectChangeListener(this);
   }
   ```

5. **Use Optional properly** when getting session values
   ```java
   sessionService.getActiveUser().ifPresent(user -> {
       // Work with user
   });
   ```

### DON'T ❌

1. **Don't pass session to entity constructors**
   ```java
   // ❌ Wrong
   public CActivity(String name, CProject project, ISessionService session)
   
   // ✅ Correct
   public CActivity(String name, CProject project)
   ```

2. **Don't use concrete session service types**
   ```java
   // ❌ Wrong
   @Autowired
   private CSessionService sessionService;
   
   // ✅ Correct
   @Autowired
   private ISessionService sessionService;
   ```

3. **Don't access VaadinSession directly** in business code
   ```java
   // ❌ Wrong
   VaadinSession session = VaadinSession.getCurrent();
   session.setAttribute("myData", data);
   
   // ✅ Correct
   sessionService.setActiveId("MyEntity", entity.getId());
   ```

4. **Don't store large objects** in session
   ```java
   // ❌ Wrong - storing entire lists
   sessionService.setActiveId("activities", activityList);
   
   // ✅ Correct - storing only IDs
   sessionService.setActiveId("CActivity", activity.getId());
   ```

5. **Don't forget to clear session** on logout
   ```java
   // Automatically handled by CWebSessionService.clearSession()
   ```

## Thread Safety

The session service implementations are thread-safe:

1. **VaadinSession** is thread-safe by design
2. **Listener sets** use `ConcurrentHashMap.newKeySet()` for thread-safe operations
3. **UI updates** use `UI.access()` to ensure proper thread context
4. **Session attributes** are stored atomically

```java
// Thread-safe listener registration
private Set<IProjectChangeListener> getOrCreateProjectChangeListeners(VaadinSession session) {
    Set<IProjectChangeListener> listeners = 
        (Set<IProjectChangeListener>) session.getAttribute(PROJECT_CHANGE_LISTENERS_KEY);
    if (listeners == null) {
        listeners = ConcurrentHashMap.newKeySet();  // Thread-safe set
        session.setAttribute(PROJECT_CHANGE_LISTENERS_KEY, listeners);
    }
    return listeners;
}

// Thread-safe UI updates
private void notifyProjectChangeListeners(CProject newProject) {
    UI ui = UI.getCurrent();
    if (ui != null) {
        ui.access(() -> {  // Ensures proper UI thread context
            getCurrentProjectChangeListeners().forEach(listener -> {
                try {
                    listener.onProjectChanged(newProject);
                } catch (Exception e) {
                    LOGGER.error("Error notifying listener", e);
                }
            });
        });
    }
}
```

## Summary

The Derbent session management architecture provides:

✅ **Per-user session isolation** using VaadinSession  
✅ **Clean separation** between domain entities and session concerns  
✅ **Dependency injection** for session service access  
✅ **Thread-safe** operations and UI updates  
✅ **Profile-based** implementations for different environments  
✅ **Listener pattern** for reactive UI updates  

**Key Takeaway**: Domain entities remain independent of session management, while services use dependency injection to access session context when needed. This maintains proper separation of concerns and enables testability.
