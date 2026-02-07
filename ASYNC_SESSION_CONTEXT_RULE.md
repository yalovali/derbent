# ASYNC SESSION CONTEXT RULE (MANDATORY)

**Date**: 2026-02-07  
**Status**: MANDATORY - All async operations MUST follow this pattern  
**Priority**: CRITICAL - Prevents runtime NullPointerException bugs

---

## Two Critical Issues with Async UI Operations

### Issue 1: ThreadLocal Session Storage
### Issue 2: UI Refresh Requires Push or Polling

Both issues MUST be addressed for async operations to work correctly.

---

## Issue 1: ThreadLocal Storage in Async Contexts

### What Happens (The Bug)

```java
// ❌ WRONG - Session returns NULL in async context
private void on_buttonHello_clicked() {
    CompletableFuture.supplyAsync(() -> {
        // THIS FAILS - sessionService.getActiveProject() returns NULL!
        CProject_Bab project = sessionService.getActiveProject().get();  
        // NullPointerException or empty Optional
        return doSomething(project);
    });
}
```

### Why It Happens

| Thread | Session Context | Result |
|--------|----------------|--------|
| **UI Thread** | ✅ Available (ThreadLocal) | `sessionService.getActiveProject()` works |
| **Async Thread Pool** | ❌ NOT Available | `sessionService.getActiveProject()` returns `Optional.empty()` |

**Root Cause**: 
- Vaadin session uses **ThreadLocal storage**
- `CompletableFuture.supplyAsync()` executes on a **different thread** (ForkJoinPool)
- **ThreadLocal data does NOT transfer** between threads

---

## The Solution: Capture Before Async (MANDATORY Pattern)

### ✅ CORRECT Pattern

```java
private void on_buttonHello_clicked() {
    final UI ui = getUI().orElse(null);
    Check.notNull(ui, "UI instance not available");
    
    // STEP 1: Capture data on UI thread (session context available)
    final CProject_Bab babProject;
    final CClientProject clientProject;
    try {
        // Get from session BEFORE async - we're on UI thread
        Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
        
        if (projectOpt.isEmpty()) {
            CNotificationService.showError("No active project");
            return;  // Early return
        }
        
        babProject = (CProject_Bab) projectOpt.get();
        clientProject = clientProjectService.getOrCreateClient(babProject);
        
    } catch (Exception e) {
        LOGGER.error("Failed to get project/client", e);
        CNotificationService.showError("Failed to get project: " + e.getMessage());
        return;
    }
    
    // STEP 2: Use captured variables in async
    CompletableFuture.supplyAsync(() -> {
        // No session access needed - using captured variables
        return clientProject.sayHello();
    }).whenComplete((response, error) -> ui.access(() -> {
        // STEP 3: Update UI on UI thread
        if (error != null) {
            CNotificationService.showError("Error: " + error.getMessage());
            return;
        }
        CNotificationService.showSuccess("Success!");
    }));
}
```

---

## Mandatory Rules

### Rule 1: Capture Session Data BEFORE Async

**ALWAYS capture session-dependent data on the UI thread BEFORE entering async context**:

```java
// ✅ DO - Capture on UI thread
final CUser currentUser = sessionService.getActiveUser().orElseThrow();
final CProject currentProject = sessionService.getActiveProject().orElseThrow();
final CCompany currentCompany = sessionService.getActiveCompany().orElseThrow();

CompletableFuture.supplyAsync(() -> {
    // Use captured variables - no session access
    return process(currentUser, currentProject, currentCompany);
});
```

### Rule 2: Use Final Variables for Captured Data

**Captured variables MUST be `final` or effectively final**:

```java
// ✅ DO - Explicitly final
final CProject project = sessionService.getActiveProject().get();

// ✅ DO - Effectively final (never reassigned)
CUser user = sessionService.getActiveUser().get();
CompletableFuture.supplyAsync(() -> doWork(user));

// ❌ DON'T - Not effectively final
CProject project = sessionService.getActiveProject().get();
project = anotherProject;  // Reassignment breaks capture
CompletableFuture.supplyAsync(() -> doWork(project));  // COMPILE ERROR
```

### Rule 3: Early Validation and Return

**Validate session data immediately, fail fast with user-friendly messages**:

```java
// ✅ DO - Validate and return early
final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
if (projectOpt.isEmpty()) {
    CNotificationService.showError("No active project");
    return;  // Stop here
}

// ✅ DO - Type validation
if (!(projectOpt.get() instanceof CProject_Bab)) {
    CNotificationService.showError("Active project is not a BAB project");
    return;  // Stop here
}

// Continue with async...
```

### Rule 4: Use Existing Service Singletons

**ALWAYS use existing service registries instead of creating new instances**:

```java
// ✅ DO - Use existing client from registry
final CClientProject client = clientProjectService.getOrCreateClient(babProject);

// ❌ DON'T - Create new instance
final CClientProject client = new CClientProject(babProject, httpService);
```

**Benefits**:
- Reuses existing connections
- Maintains statistics
- Proper resource lifecycle management
- One instance per project (singleton pattern)

---

## Session-Dependent Services (ThreadLocal)

**These services CANNOT be used inside async contexts** (must be captured before):

| Service | What to Capture | Example |
|---------|----------------|---------|
| `ISessionService` | Active user, project, company | `sessionService.getActiveUser()` |
| `SecurityContextHolder` | Authentication | `SecurityContextHolder.getContext().getAuthentication()` |
| `VaadinSession` | Session attributes | `VaadinSession.getCurrent().getAttribute()` |
| `UI.getCurrent()` | Current UI instance | `getUI().orElse(null)` |

**Pattern**:
```java
// Capture on UI thread
final CUser user = sessionService.getActiveUser().orElseThrow();
final UI ui = getUI().orElseThrow();

// Use in async
CompletableFuture.supplyAsync(() -> doWork(user))
    .whenComplete((result, error) -> ui.access(() -> updateUI(result)));
```

---

## Import Statements vs Fully-Qualified Names (MANDATORY)

### Rule 5: ALWAYS Use Import Statements

**NEVER use fully-qualified class names in code** (except in JavaDoc where they add clarity):

```java
// ✅ CORRECT - Import and use short name
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroResponse;

private void on_buttonHello_clicked() {
    final CProject_Bab babProject;  // Short name
    final CClientProject clientProject;  // Short name
    // ...
    CompletableFuture.supplyAsync(() -> {
        return clientProject.sayHello();  // Clean and readable
    }).whenComplete((response, error) -> {
        // CCalimeroResponse used here
    });
}

// ❌ INCORRECT - Fully-qualified names clutter code (should use imports)
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.http.clientproject.domain.CClientProject;

private void on_buttonHello_clicked() {
    final CProject_Bab babProject;  // Clean with imports
    final CClientProject clientProject;  // Clean with imports
    // ...
}
```

**Rationale**:
- Improves code readability
- Reduces line length violations
- Makes refactoring easier
- Standard practice in professional Java development
- IDE provides import assistance

**Exceptions** (where fully-qualified is acceptable):
```java
/**
 * Creates HTTP client for {@link tech.derbent.bab.project.domain.CProject_Bab}.
 * 
 * @param project the BAB project (see {@link tech.derbent.bab.project.domain.CProject_Bab})
 * @return client instance
 */
```

---

## Common Mistakes and Fixes

### Mistake 1: Accessing Session in Async

```java
// ❌ WRONG
CompletableFuture.supplyAsync(() -> {
    CUser user = sessionService.getActiveUser().get();  // NULL!
    return processUser(user);
});

// ✅ CORRECT
final CUser user = sessionService.getActiveUser().get();
CompletableFuture.supplyAsync(() -> processUser(user));
```

### Mistake 2: Creating New Service Instances

```java
// ❌ WRONG - Creates new instance every time
CompletableFuture.supplyAsync(() -> {
    CClientProject client = new CClientProject(project, httpService);
    return client.sayHello();
});

// ✅ CORRECT - Reuses existing from registry
final CClientProject client = clientProjectService.getOrCreateClient(project);
CompletableFuture.supplyAsync(() -> client.sayHello());
```

### Mistake 3: Using Fully-Qualified Names

```java
// ❌ WRONG - Too verbose, hard to read
CompletableFuture.supplyAsync(() -> {
    final CCalimeroRequest request = CCalimeroRequest.builder()
        .type("system")
        .operation("hello")
        .build();
    return CCalimeroResponse.fromJson(response);
});

// ✅ CORRECT - Clean, with imports at top of file
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;

CompletableFuture.supplyAsync(() -> {
    final CCalimeroRequest request = CCalimeroRequest.builder()
        .type("system")
        .operation("hello")
        .build();
    return CCalimeroResponse.fromJson(response);
});
```

---

## Testing for Session Context Issues

### Manual Testing

1. **Set breakpoint** inside `CompletableFuture.supplyAsync(() -> ...)`
2. **Inspect** `VaadinSession.getCurrent()` → should be NULL
3. **Inspect** `sessionService.getActiveProject()` → should return `Optional.empty()`
4. **Verify** captured variables work correctly

### Code Review Checklist

- [ ] All session data captured BEFORE `CompletableFuture.supplyAsync()`
- [ ] Captured variables are `final` or effectively final
- [ ] No `sessionService.getActive*()` calls inside async context
- [ ] No `SecurityContextHolder.getContext()` calls inside async context
- [ ] Early validation with user-friendly error messages
- [ ] Using service registries (not creating new instances)
- [ ] Using import statements (not fully-qualified names)

---

## Real-World Example: Health Check Button

### Implementation: CComponentCalimeroStatus.on_buttonHello_clicked()

```java
private void on_buttonHello_clicked() {
    LOGGER.debug("Hello button clicked - checking Calimero server health");
    final UI ui = getUI().orElse(null);
    Check.notNull(ui, "UI instance not available for health check");
    
    // STEP 1: Capture on UI thread (session available)
    final CProject_Bab babProject;
    final CClientProject clientProject;
    try {
        final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
        
        if (projectOpt.isEmpty()) {
            CNotificationService.showError("No active project");
            return;
        }
        
        if (!(projectOpt.get() instanceof CProject_Bab)) {
            CNotificationService.showError("Active project is not a BAB project");
            return;
        }
        
        babProject = (CProject_Bab) projectOpt.get();
        clientProject = clientProjectService.getOrCreateClient(babProject);
        
    } catch (Exception e) {
        LOGGER.error("Failed to get project/client", e);
        CNotificationService.showError("Failed to get project: " + e.getMessage());
        return;
    }
    
    buttonHello.setEnabled(false);
    buttonHello.setText("Checking...");
    
    // STEP 2: Async health check with captured variables
    CompletableFuture.supplyAsync(() -> {
        try {
            LOGGER.debug("Executing health check for project '{}'", babProject.getName());
            return clientProject.sayHello();
        } catch (Exception e) {
            LOGGER.error("Health check failed", e);
            return CCalimeroResponse.error("Health check failed: " + e.getMessage());
        }
    }).whenComplete((response, error) -> ui.access(() -> {
        // STEP 3: UI update on UI thread
        buttonHello.setEnabled(true);
        buttonHello.setText("Hello");
        
        if (error != null) {
            LOGGER.error("❌ Health check error", error);
            CNotificationService.showError("Health check failed: " + error.getMessage());
            return;
        }
        
        if (response.isSuccess()) {
            LOGGER.info("✅ Calimero server is healthy");
            CNotificationService.showSuccess("✅ Calimero server is UP and responding!");
        } else {
            LOGGER.warn("⚠️ Calimero health check failed: {}", response.getErrorMessage());
            if (response.getStatus() == 401 || response.getErrorMessage().toLowerCase().contains("auth")) {
                CNotificationService.showError("❌ Authentication failed - check credentials");
            } else {
                CNotificationService.showError("❌ Health check failed: " + response.getErrorMessage());
            }
        }
    }));
}
```

---

## Related Patterns

### Pattern 1: Async with Multiple Session Objects

```java
// Capture all needed data
final CUser user = sessionService.getActiveUser().orElseThrow();
final CProject project = sessionService.getActiveProject().orElseThrow();
final CCompany company = sessionService.getActiveCompany().orElseThrow();

CompletableFuture.supplyAsync(() -> 
    processData(user, project, company)
);
```

### Pattern 2: Async with Service Registry

```java
// Get singleton from registry
final CClientProject client = clientProjectService.getOrCreateClient(project);

CompletableFuture.supplyAsync(() -> 
    client.fetchData()
);
```

### Pattern 3: Async with UI Updates

```java
final UI ui = getUI().orElseThrow();

CompletableFuture.supplyAsync(() -> 
    longRunningOperation()
).whenComplete((result, error) -> ui.access(() -> {
    // ALWAYS update UI inside ui.access()
    updateGrid(result);
    CNotificationService.showSuccess("Complete");
}));
```

---

## Enforcement

**Code Review**: REJECT any pull request that:
1. Accesses `sessionService` inside `CompletableFuture.supplyAsync()`
2. Accesses `SecurityContextHolder` inside async context
3. Creates new service instances instead of using registries
4. Uses fully-qualified class names instead of imports (except JavaDoc)
5. Missing early validation with user-friendly error messages

**Verification Command**:
```bash
# Find async session access violations
grep -r "CompletableFuture.supplyAsync" src/main/java --include="*.java" -A 10 | \
  grep "sessionService\|SecurityContextHolder" && \
  echo "⚠️ VIOLATION: Session access in async context" || \
  echo "✅ No violations found"

# Find fully-qualified names (excluding imports and JavaDoc)
grep -r "tech\.derbent\.[a-z]*\.[a-z]*\.domain\.[A-Z]" src/main/java --include="*.java" | \
  grep -v "import\|@see\|@link\|{@link" && \
  echo "⚠️ VIOLATION: Fully-qualified names instead of imports" || \
  echo "✅ No violations found"
```

---

## Summary

### The Golden Rules

1. ✅ **Capture session data BEFORE async** - ThreadLocal doesn't transfer
2. ✅ **Use final variables** - Required for lambda capture
3. ✅ **Validate early, return fast** - User-friendly error messages
4. ✅ **Use service registries** - Don't create new instances
5. ✅ **Import statements always** - Never fully-qualified names in code
6. ✅ **UI updates in ui.access()** - Thread-safe UI operations
7. ✅ **Enable Push or Polling** - UI must refresh after async completion

### The Anti-Patterns (FORBIDDEN)

1. ❌ `sessionService.getActive*()` inside `CompletableFuture.supplyAsync()`
2. ❌ Creating new service instances instead of using registries
3. ❌ Non-final captured variables
4. ❌ Missing early validation
5. ❌ Fully-qualified class names instead of imports
6. ❌ Direct UI updates from async thread
7. ❌ Async UI updates without Push/Polling enabled

---

## Issue 2: UI Refresh Requires Push or Polling

### The Problem

`ui.access()` updates UI state on the server, but browser doesn't refresh until:
- User interacts with page (click, type, etc.)
- OR Push is enabled (WebSocket)
- OR Polling is enabled (periodic check)

**Symptom**: Async operation completes, but UI shows old state until you click something.

### The Solution (Choose One)

**Option A: Application-Level Push (IMPLEMENTED - Production Grade)**
```java
// In Application.java
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;

@Push(PushMode.AUTOMATIC)  // Enables WebSocket-based push for all views
public class Application implements AppShellConfigurator {
    // ...
}
```

**Benefits**:
- ✅ Works for ALL async operations in entire application
- ✅ Real-time updates via WebSocket
- ✅ No per-component configuration needed
- ✅ Production-ready solution

**Status**: ✅ **ACTIVE** - Push enabled at application level in `Application.java`

---

**Option B: UI Polling (Development Alternative - NOT RECOMMENDED)**
```java
@Override
protected void initializeComponents() {
    configureComponent();
    
    // Enable UI polling - checks for updates every 500ms
    getUI().ifPresent(ui -> {
        ui.setPollInterval(500);  // 500ms interval
        LOGGER.debug("UI polling enabled for async operation support");
    });
    
    createComponents();
}
```

**Drawbacks**:
- ⚠️ Wasteful (polls even when no updates)
- ⚠️ Higher server load
- ⚠️ Slight delay (poll interval)

**Status**: ❌ **NOT USED** - Replaced by application-level Push

For complete UI refresh solutions, see `ASYNC_UI_REFRESH_SOLUTIONS.md`.

---

## References

- **Implementation**: `CComponentCalimeroStatus.on_buttonHello_clicked()`
- **Service Registry**: `CClientProjectService.getOrCreateClient()`
- **UI Refresh Solutions**: `ASYNC_UI_REFRESH_SOLUTIONS.md`
- **Related Rule**: `AGENTS.md` Section 3.8 (Import Organization)
- **Date Created**: 2026-02-07
- **Date Updated**: 2026-02-07 (Added UI refresh requirement)
