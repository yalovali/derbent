# Value Persistence Pattern - Simplified Guide

## Overview

The value persistence system has been simplified to follow a consistent pattern across all components, inspired by the excellent automatic persistence in `CComboBox`.

## Key Changes

### 1. Session Clearing Bug Fixed ✓

**Problem:** Session was clearing on F5 refresh, causing all persisted values to be lost.

**Solution:** Modified `CWebSessionService.setActiveUser()` to only clear session when user actually changes:

```java
// Only clear session if changing user
final CUser existing = (CUser) VaadinSession.getCurrent().getAttribute(ACTIVE_USER_KEY);
if (existing != null && !existing.getId().equals(user.getId())) {
    clearSession(); // only if switching users
}
```

### 2. Logout Now Clears Session ✓

Added `clearSession()` call to logout in MainLayout:

```java
final MenuItem menuItem = userMenuItem.getSubMenu().addItem("Logout", e -> {
    sessionService.clearSession(); // Clear session on logout
    authenticationContext.logout();
});
```

### 3. Unified Persistence Pattern ✓

Created `IHasValuePersistence<T>` interface that provides default implementations for automatic persistence:

```java
public interface IHasValuePersistence<T> extends HasValue<HasValue.ValueChangeEvent<T>, T> {
    // Default implementations for save/restore
    // Component just needs to provide key, enabled flag, and logger
}
```

## Persistence Patterns - Before and After

### Pattern 1: CComboBox (Already Perfect ✓)

**Usage:**
```java
CComboBox<EntityType> comboBox = new CComboBox<>("Type");
comboBox.setItems(availableTypes);
// One line to enable automatic persistence
comboBox.enablePersistence("myView_typeFilter", 
    storedName -> findType(storedName));
```

**Benefits:**
- Automatic save on user changes
- Automatic restore on component attach
- Simple one-line enablement

### Pattern 2: CTextField (NEW - Now Matches CComboBox ✓)

**Before:** Had to use CValueStorageHelper manually

**After:**
```java
CTextField textField = new CTextField("Name Filter");
// One line to enable automatic persistence (just like CComboBox!)
textField.enablePersistence("myView_nameFilter");
```

**Implementation:** CTextField now has the same automatic persistence pattern as CComboBox:
- `enablePersistence(String storageKey)` method
- Automatic save on value change (user changes only)
- Automatic restore on attach

### Pattern 3: CValueStorageHelper (Utility for Complex Cases)

**When to Use:** For components that don't have built-in persistence or need custom logic

**Usage:**
```java
ComboBox<ComplexObject> comboBox = new ComboBox<>("Filter");
CValueStorageHelper.valuePersist_enable(
    comboBox,
    "myView_complexFilter",
    obj -> obj.getId().toString(),  // Custom serializer
    id -> findById(Long.parseLong(id))  // Custom deserializer
);
```

### Pattern 4: CComponentGridSearchToolbar (Composite Component ✓)

**Already Simplified:** Uses CValueStorageHelper internally for all its text fields

**Usage:**
```java
CComponentGridSearchToolbar toolbar = new CComponentGridSearchToolbar();
toolbar.setId("activities_gridSearchToolbar"); // MUST set ID first
toolbar.valuePersist_enable();  // Enables persistence for ALL filters
```

### Pattern 5: CAbstractFilterToolbar (Framework Component ✓)

**Already Simplified:** Automatically enables persistence in `buildClearButton()`

**Usage:**
```java
public class CMyFilterToolbar extends CAbstractFilterToolbar<MyEntity> {
    public CMyFilterToolbar() {
        setId("myView_filterToolbar"); // MUST set ID first
        addFilterComponent(new CSprintFilter());
        addFilterComponent(new CStatusFilter());
        buildClearButton(); // Automatically enables persistence!
    }
}
```

## Component Comparison

| Component | Pattern | Complexity | Automatic? |
|-----------|---------|------------|------------|
| CComboBox | Built-in `enablePersistence()` | ⭐ Simple | ✅ Yes |
| CTextField | Built-in `enablePersistence()` | ⭐ Simple | ✅ Yes |
| CValueStorageHelper | Utility method | ⭐⭐ Moderate | ✅ Yes |
| CComponentGridSearchToolbar | Composite | ⭐ Simple | ✅ Yes |
| CAbstractFilterToolbar | Framework | ⭐ Simple | ✅ Yes |

## Best Practices

### 1. Always Use Unique Storage Keys

```java
// ✅ GOOD - Unique per component and view
textField.enablePersistence("activitiesView_nameFilter");
comboBox.enablePersistence("activitiesView_statusFilter");

// ❌ BAD - Generic keys can conflict
textField.enablePersistence("nameFilter");
comboBox.enablePersistence("filter");
```

### 2. Set Component ID Before Enabling Persistence

For composite components like CComponentGridSearchToolbar:

```java
// ✅ GOOD
toolbar.setId("activities_gridSearchToolbar");
toolbar.valuePersist_enable();

// ❌ BAD - Will throw IllegalStateException
toolbar.valuePersist_enable(); // No ID set!
```

### 3. Use Built-in Persistence When Available

```java
// ✅ GOOD - Use CTextField's built-in persistence
CTextField textField = new CTextField("Name");
textField.enablePersistence("myView_name");

// ❌ BAD - Unnecessary complexity
TextField textField = new TextField("Name");
CValueStorageHelper.valuePersist_enable(textField, "myView_name");
```

### 4. Only Save User Changes

The system automatically filters out programmatic changes:

```java
// This will NOT trigger save (not from client)
textField.setValue("default");

// This WILL trigger save (user typed it)
// User types in the field -> isFromClient() = true -> saves
```

## Migration Guide

### If You're Using Manual SessionService Calls

**Before:**
```java
private void saveValue() {
    sessionService.setSessionValue("myKey", textField.getValue());
}

private void restoreValue() {
    sessionService.getSessionValue("myKey").ifPresent(textField::setValue);
}

textField.addValueChangeListener(e -> saveValue());
textField.addAttachListener(e -> restoreValue());
```

**After:**
```java
textField.enablePersistence("myKey");
// That's it! Automatic save/restore
```

### If You're Using IHasSelectedValueStorage Interface

**Before:**
```java
public class MyComponent implements IHasSelectedValueStorage {
    @Override
    public void saveCurrentValue() {
        // Complex manual save logic
    }
    
    @Override
    public void restoreCurrentValue() {
        // Complex manual restore logic
    }
}
```

**After:** If your component extends HasValue, consider using `IHasValuePersistence<T>` instead:

```java
public class MyComponent extends SomeVaadinComponent 
        implements IHasValuePersistence<String> {
    private String persistenceKey;
    private boolean persistenceEnabled;
    
    // Just implement getters/setters
    // Default implementations handle everything else!
}
```

## Testing Value Persistence

### Test 1: F5 Refresh (Should NOT Clear)

1. Set a filter value (e.g., name = "test")
2. Press F5 to refresh the page
3. ✅ Expected: Filter value "test" should persist

### Test 2: Logout (Should Clear)

1. Set a filter value (e.g., name = "test")
2. Click Logout
3. Login again
4. ✅ Expected: Filter value should be cleared (default state)

### Test 3: User Switch (Should Clear)

1. Login as User A
2. Set a filter value (e.g., name = "test")
3. Logout and login as User B
4. ✅ Expected: Filter value should be cleared (User B's session)

## Summary

The persistence system has been significantly simplified:

1. **Session clearing bug fixed** - F5 no longer clears session
2. **Logout properly clears** - Session cleared on logout and user switch
3. **CTextField now has automatic persistence** - Matches CComboBox pattern
4. **IHasValuePersistence interface** - Reusable pattern for new components
5. **Consistent patterns** - All components follow the same approach

All components now use automatic persistence with minimal code, following the excellent pattern established by CComboBox.
