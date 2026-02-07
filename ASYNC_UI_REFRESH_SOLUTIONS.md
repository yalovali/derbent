# Async UI Refresh Solutions (Vaadin)

**Date**: 2026-02-07  
**Problem**: UI updates from async operations (`CompletableFuture.supplyAsync()`) don't appear until user interacts with the page  
**Root Cause**: `ui.access()` requires Push or polling to be enabled for immediate updates

---

## Solution 1: Enable Push (RECOMMENDED - Production Grade)

**Pros**:
- ✅ Real-time updates (WebSocket-based)
- ✅ Most efficient for frequent updates
- ✅ Standard Vaadin pattern for async operations
- ✅ Works for all async operations automatically

**Cons**:
- ⚠️ Requires server support for WebSockets
- ⚠️ Slightly more complex deployment

### Implementation:

**Option 1A: Application-level Push (affects all views)**
```java
// In Application.java or MainLayout.java
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;

@Push(PushMode.AUTOMATIC)  // or PushMode.MANUAL
public class Application implements AppShellConfigurator {
    // ...
}
```

**Option 1B: View-level Push (specific views only)**
```java
import com.vaadin.flow.component.page.Push;

@Push  // Enables push for this view
public class CSystemSettingsView_Bab extends CAbstractPage {
    // ...
}
```

**Option 1C: Component-level Push (current recommendation)**
Since `CComponentCalimeroStatus` is embedded in a form/page that's dynamically generated, enable Push on the **parent page** where the component is used.

**How it works**:
- `@Push` enables WebSocket connection
- `ui.access()` immediately pushes updates to browser
- No user interaction needed

---

## Solution 2: UI Polling (SIMPLE - Quick Fix)

**Pros**:
- ✅ Very simple to implement (one line)
- ✅ Works without WebSocket support
- ✅ Good for prototyping/development

**Cons**:
- ⚠️ Wasteful (polls even when no updates)
- ⚠️ Higher server load with many users
- ⚠️ Slight delay (poll interval)

### Implementation:

**In component's initializeComponents()**:
```java
@Override
protected void initializeComponents() {
    configureComponent();
    
    // Enable UI polling - checks for updates every 500ms
    getUI().ifPresent(ui -> ui.setPollInterval(500));  // 500ms interval
    
    createCalimeroControlCard();
    updateCalimeroStatus();
}
```

**How it works**:
- UI polls server every 500ms
- If async operation updated UI, changes appear on next poll
- Simple but less efficient than Push

---

## Solution 3: Manual UI.push() (EXPLICIT - Best Control)

**Pros**:
- ✅ Explicit control over when updates happen
- ✅ Works with PushMode.MANUAL
- ✅ More efficient than automatic push

**Cons**:
- ⚠️ Requires manual push() call in each async operation
- ⚠️ Still requires @Push annotation

### Implementation:

**Enable manual Push**:
```java
@Push(PushMode.MANUAL)
public class CSystemSettingsView_Bab extends CAbstractPage {
    // ...
}
```

**Update async completion**:
```java
CompletableFuture.supplyAsync(() -> {
    return clientProject.sayHello();
}).whenComplete((response, error) -> ui.access(() -> {
    // Update UI components
    buttonHello.setEnabled(true);
    buttonHello.setText("Hello");
    calimeroHealthStatusIndicator.setText("Health: Success");
    
    // EXPLICIT PUSH - sends updates to browser
    ui.push();  // ← Add this line
}));
```

**How it works**:
- `@Push(PushMode.MANUAL)` enables push infrastructure
- `ui.push()` explicitly sends accumulated changes
- More control than automatic push

---

## Solution 4: Immediate Completion on UI Thread (NO ASYNC)

**Pros**:
- ✅ No push/polling needed
- ✅ Simplest code
- ✅ Immediate updates

**Cons**:
- ⚠️ Blocks UI thread during operation
- ⚠️ Poor user experience for slow operations
- ❌ NOT RECOMMENDED for network calls

### Implementation:

```java
private void on_buttonHello_clicked() {
    buttonHello.setEnabled(false);
    buttonHello.setText("Checking...");
    
    try {
        // Synchronous call on UI thread (blocks!)
        CCalimeroResponse response = clientProject.sayHello();
        
        // Update UI immediately
        if (response.isSuccess()) {
            calimeroHealthStatusIndicator.setText("Health: Success");
        } else {
            calimeroHealthStatusIndicator.setText("Health: Failed");
        }
    } finally {
        buttonHello.setEnabled(true);
        buttonHello.setText("Hello");
    }
}
```

**WARNING**: Only use for operations < 100ms. Network calls should be async.

---

## Solution 5: Hybrid - Optimistic Update + Async Confirmation

**Pros**:
- ✅ Immediate feedback (optimistic)
- ✅ Correct final state (async)
- ✅ Best user experience

**Cons**:
- ⚠️ More complex code
- ⚠️ Still needs Push or polling for async part

### Implementation:

```java
private void on_buttonHello_clicked() {
    // OPTIMISTIC UPDATE - immediate feedback
    buttonHello.setEnabled(false);
    buttonHello.setText("Checking...");
    calimeroHealthStatusIndicator.setText("Health: Checking...");
    calimeroHealthStatusIndicator.getStyle().set("background-color", "#90CAF9");
    
    // Capture context
    final UI ui = getUI().orElseThrow();
    final CClientProject clientProject = /* ... */;
    
    // ASYNC CONFIRMATION - real result
    CompletableFuture.supplyAsync(() -> clientProject.sayHello())
        .whenComplete((response, error) -> ui.access(() -> {
            // Update with real result
            if (response.isSuccess()) {
                calimeroHealthStatusIndicator.setText("Health: Success");
                calimeroHealthStatusIndicator.getStyle().set("background-color", "#81C784");
            } else {
                calimeroHealthStatusIndicator.setText("Health: Failed");
                calimeroHealthStatusIndicator.getStyle().set("background-color", "#E57373");
            }
            buttonHello.setEnabled(true);
            buttonHello.setText("Hello");
            
            ui.push();  // If using manual push
        }));
}
```

---

## Recommended Solution for Your Case

**✅ IMPLEMENTED: Application-Level Push**

```java
// In Application.java
@Push(PushMode.AUTOMATIC)
@SpringBootConfiguration
@EnableAutoConfiguration
@Theme("default")
public class Application implements AppShellConfigurator {
    // ...
}
```

**Benefits**:
- Real-time updates for ALL async operations
- WebSocket-based (efficient)
- No per-component configuration
- Production-ready

**Status**: Active in production. All async UI operations now update immediately.

---

## Testing the Fix

### Before Fix:
```bash
1. Click "Hello" button
2. Button shows "Checking..."
3. Wait 5 seconds
4. Nothing happens!
5. Click anywhere on page
6. Suddenly health status appears ← UI was waiting for interaction
```

### After Fix (with Push or Polling):
```bash
1. Click "Hello" button
2. Button shows "Checking..."
3. Wait 1-2 seconds
4. Health status updates automatically ← Push/polling triggered refresh
5. Button returns to "Hello"
```

---

## Code Review Checklist

When reviewing async UI code:

- [ ] All async operations use `ui.access()` for UI updates
- [ ] Either Push enabled OR polling enabled
- [ ] UI updates happen in `ui.access()` lambda
- [ ] Manual `ui.push()` if using `PushMode.MANUAL`
- [ ] Session data captured before async (see ASYNC_SESSION_CONTEXT_RULE.md)

---

## Related Documentation

- `ASYNC_SESSION_CONTEXT_RULE.md` - Session context in async operations
- Vaadin Documentation: https://vaadin.com/docs/latest/advanced/server-push
