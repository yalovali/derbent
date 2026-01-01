# Fix for CComponentBacklog Compact Mode Initialization

## Problem

The `CComponentKanbanColumnBacklog` creates a `CComponentBacklog` with `compactMode=true`, but the toolbar was displaying in full mode with all filters visible instead of the intended compact mode with hidden filters.

## Root Cause

Java field initialization order problem:

1. `CComponentBacklog` constructor receives `compactMode=true`
2. Constructor calls `super(...)` as first statement (Java requirement)
3. Parent `CComponentEntitySelection` constructor executes
4. Parent calls `setupComponent()` which calls the overridden `create_gridSearchToolbar()`
5. **At this point**, the `compactMode` field in `CComponentBacklog` hasn't been initialized yet
6. The field still has its default value of `false`
7. The toolbar is created with `config.showAll()` instead of hiding filters
8. Only after super() completes does `this.compactMode = compactMode` execute

## Solution

Use a ThreadLocal to pass the `compactMode` value during constructor chain:

1. Added `private static final ThreadLocal<Boolean> COMPACT_MODE_INIT` field
2. Created helper method `createEntityTypesWithCompactMode(compactMode)` that:
   - Sets `COMPACT_MODE_INIT.set(compactMode)` 
   - Returns the entity types list for chaining in super() call
3. Modified constructor to call `super(createEntityTypesWithCompactMode(compactMode), ...)`
4. Updated `create_gridSearchToolbar()` to:
   - Retrieve compactMode from `COMPACT_MODE_INIT.get()`
   - Fall back to `false` if ThreadLocal is not set
   - Add debug logging to track the flow
5. Added finally block to always clean up ThreadLocal: `COMPACT_MODE_INIT.remove()`

## Code Changes

### Before (Buggy)
```java
public CComponentBacklog(final CProject project, final boolean compactMode) {
    super(createEntityTypes(), ...);  // compactMode not available yet!
    this.compactMode = compactMode;   // Too late - toolbar already created
    ...
}

protected CComponentFilterToolbar create_gridSearchToolbar() {
    if (compactMode) {  // ALWAYS FALSE - field not initialized yet!
        config.hideAll();
    }
    ...
}
```

### After (Fixed)
```java
private static final ThreadLocal<Boolean> COMPACT_MODE_INIT = new ThreadLocal<>();

private static List<EntityTypeConfig<?>> createEntityTypesWithCompactMode(final boolean compactMode) {
    COMPACT_MODE_INIT.set(compactMode);  // Store BEFORE super()
    return createEntityTypes();
}

public CComponentBacklog(final CProject project, final boolean compactMode) {
    super(createEntityTypesWithCompactMode(compactMode), ...);  // ThreadLocal set first
    try {
        this.compactMode = compactMode;
        ...
    } finally {
        COMPACT_MODE_INIT.remove();  // Always cleanup to prevent memory leaks
    }
}

protected CComponentFilterToolbar create_gridSearchToolbar() {
    final Boolean compactModeFromInit = COMPACT_MODE_INIT.get();  // Retrieve from ThreadLocal
    final boolean isCompactMode = (compactModeFromInit != null) ? compactModeFromInit : false;
    
    if (isCompactMode) {  // NOW WORKS CORRECTLY!
        config.hideAll();
    }
    ...
}
```

## Why ThreadLocal?

ThreadLocal is the standard Java pattern for passing values through constructor chains when:
- Fields can't be initialized before `super()` call
- Method is called from parent constructor before subclass fields are initialized
- Need to pass context through multiple constructor calls
- Must ensure thread safety and prevent memory leaks

## Testing

The fix ensures that:
1. When `CComponentKanbanColumnBacklog` creates `CComponentBacklog(project, true)`:
   - ThreadLocal stores `true` before parent constructor runs
   - `create_gridSearchToolbar()` retrieves `true` from ThreadLocal
   - Toolbar is configured with `config.hideAll()` for compact mode
   - Debug logs show: "compactMode: true (from ThreadLocal: yes)"
   
2. When creating backlog in normal mode `CComponentBacklog(project, false)`:
   - ThreadLocal stores `false`
   - Toolbar is configured with `config.showAll()` for normal mode
   - No regression in existing functionality

3. Memory safety:
   - ThreadLocal is always cleaned up in finally block
   - No risk of memory leaks in thread pool environments

## Verification

To verify the fix is working:
1. Navigate to Kanban board in the application
2. Observe the backlog column (first column on the left)
3. Check that the toolbar only shows the "Entity Type" combobox
4. Verify that ID, Name, Description, Status filters are hidden
5. Check logs for: "Configured toolbar for COMPACT mode - all filters hidden"

## Related Files

- `CComponentBacklog.java` - Fixed initialization pattern
- `CComponentKanbanColumnBacklog.java` - Creates backlog with compactMode=true
- `CComponentEntitySelection.java` - Parent class that calls setupComponent()
- `CComponentGridSearchToolbar.java` - Toolbar with configurable filters
