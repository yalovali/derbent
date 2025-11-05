# CRUD Operations - Visual Guide

This document describes the CRUD (Create, Read, Update, Delete) operations implemented in the CPageService pattern.

## Overview

All CRUD operations are fully implemented in the base `CPageService` class and work consistently across all dynamic pages (Activities, Meetings, Projects, Decisions, Orders, Users, etc.).

## 1. CREATE Operation (New Button)

### Flow
```
User clicks "New" → CPageService.actionCreate() → Service creates entity → Form populates → Success notification
```

### What Happens
1. **Service creates entity**: `getEntityService().newEntity()`
2. **Service initializes entity**: `getEntityService().initializeNewEntity()`
   - Sets `active = true`
   - Sets project (for project entities)
   - Sets default values (for type entities)
   - Generates default name (for users)
3. **Form populates**: `view.populateForm()` - Form fields show empty/default values
4. **Notification shown**: "New [EntityType] created. Fill in the details and click Save."

### Visual Result
- **Form fields**: Appear empty/with defaults, ready for data entry
- **New button**: Disabled (can't create another until current is saved/cancelled)
- **Save button**: Enabled (ready to save the new entity)
- **Grid**: No change (entity not yet saved)

### Code
```java
public void actionCreate() {
    final EntityClass newEntity = getEntityService().newEntity();
    getEntityService().initializeNewEntity(newEntity);
    setCurrentEntity(newEntity);
    view.populateForm();  // ← Form refreshes with new entity
    getNotificationService().showSuccess("New entity created...");
}
```

## 2. SAVE Operation (Save Button)

### Flow
```
User clicks "Save" → Form validation → Binder writes data → Service saves → Grid refreshes → Form refreshes → Success notification
```

### What Happens
1. **Binder writes form data to entity**: `view.getBinder().writeBean(entity)`
   - Validates all fields
   - Throws `ValidationException` if validation fails
2. **Service saves to database**: `getEntityService().save(entity)`
   - Generates ID for new entities
   - Updates version for optimistic locking
3. **Grid refreshes**: `view.refreshGrid()` - Shows new/updated row
4. **Form refreshes**: `view.populateForm()` - Shows saved entity with generated ID
5. **Notification shown**: "Data saved successfully"

### Visual Result
- **Form fields**: Remain populated with saved data (now has ID if new)
- **Grid**: Shows new row or updates existing row
- **Save button**: Remains enabled
- **Notification**: Green success message at bottom

### Error Handling
- **Validation errors**: Orange warning - "Please check that all required fields are filled"
- **Optimistic locking errors**: Red error - "Data was modified by another user"
- **General errors**: Red error with specific message

### Code
```java
public void actionSave() {
    final EntityClass entity = getCurrentEntity();
    if (view.getBinder() != null) {
        view.getBinder().writeBean(entity);  // ← Writes and validates
    }
    final EntityClass savedEntity = getEntityService().save(entity);
    setCurrentEntity(savedEntity);
    view.refreshGrid();      // ← Grid updates
    view.populateForm();     // ← Form refreshes
    getNotificationService().showSaveSuccess();
}
```

## 3. READ Operation (Grid Click)

### Flow
```
User clicks grid row → Entity selected → Details rebuild (if needed) → Form populates
```

### What Happens
1. **Entity selected from grid**: `setCurrentEntity(selectedEntity)`
2. **Check if details need rebuilding**: Based on entity type
3. **Rebuild details if needed**: `rebuildEntityDetails(entityClass)`
   - Creates/updates form fields for entity type
   - Uses binder for data binding
4. **Populate form**: `populateForm()` - Fills fields with entity data

### Visual Result
- **Form fields**: Populated with selected entity's data
- **Grid row**: Highlighted/selected
- **Buttons**: All enabled (Edit, Save, Delete, Refresh)

### Error Handling (Fixed)
Previously, if `rebuildEntityDetails()` failed, form wouldn't populate. Now:
- Nested try-catch allows continuation even if rebuild fails
- Form still populates using existing binder
- Error logged but UI doesn't break

### Code
```java
private void onEntitySelected(event) {
    setCurrentEntity(selectedEntity);
    if (VIEW_NAME_CHANGED) {
        try {
            rebuildEntityDetails(selectedEntity.getClass());
        } catch (Exception e) {
            LOGGER.error("Rebuild failed, continuing with current binder");
        }
    }
    populateForm();  // ← Always executes, even if rebuild failed
}
```

## 4. DELETE Operation (Delete Button)

### Flow
```
User clicks "Delete" → Confirmation dialog → Service deletes → Grid refreshes → Form clears → Success notification
```

### What Happens
1. **Confirmation dialog shown**: "Delete selected item?"
   - User can confirm or cancel
2. **Service deletes from database**: `getEntityService().delete(entity.getId())`
3. **Current entity cleared**: `setCurrentEntity(null)`
4. **Grid refreshes**: `view.refreshGrid()` - Removes deleted row
5. **Form clears**: `view.populateForm()` - Empty form
6. **Notification shown**: "Item deleted successfully"

### Visual Result
- **Confirmation dialog**: Modal dialog with Yes/No buttons
- **Grid**: Selected row disappears after confirmation
- **Form**: Cleared (no entity selected)
- **Notification**: Green success message

### Error Handling
- **Delete restrictions**: Red error if entity can't be deleted (has dependencies)
- **Not found**: Handled gracefully if entity already deleted

### Code
```java
public void actionDelete() {
    final EntityClass entity = getCurrentEntity();
    getNotificationService().showConfirmationDialog("Delete?", () -> {
        getEntityService().delete(entity.getId());
        setCurrentEntity(null);
        view.refreshGrid();      // ← Grid updates
        view.populateForm();     // ← Form clears
        getNotificationService().showDeleteSuccess();
    });
}
```

## 5. REFRESH Operation (Refresh Button)

### Flow
```
User clicks "Refresh" → Service reloads entity → Form repopulates → Info notification
```

### What Happens
1. **Service reloads from database**: `getEntityService().getById(entity.getId())`
   - Gets fresh data, discarding unsaved changes
2. **Entity refreshed**: `view.onEntityRefreshed(reloaded)`
   - Updates current entity
   - Calls `populateForm()` internally
3. **Notification shown**: "Entity refreshed successfully"

### Visual Result
- **Form fields**: Repopulated with fresh data from database
- **Unsaved changes**: Lost (intentional - refresh discards changes)
- **Grid**: No change (unless entity was modified elsewhere)
- **Notification**: Blue info message

### Use Cases
- Discard unsaved changes
- Reload after external modifications
- Reset form to saved state

### Code
```java
public void actionRefresh() {
    final EntityClass entity = getCurrentEntity();
    final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
    if (reloaded != null) {
        view.onEntityRefreshed(reloaded);  // ← Updates and populates
        getNotificationService().showInfo("Entity refreshed");
    }
}
```

## Notification Colors

- **Green**: Success (Save, Delete, Create)
- **Blue**: Information (Refresh)
- **Orange**: Warning (Validation errors, missing selection)
- **Red**: Error (Save failed, delete failed, exceptions)

## Testing

All CRUD operations are validated by:
- `CTypeStatusCrudTest.java` - Tests all operations on Type/Status entities
- `CWorkflowStatusCrudTest.java` - Tests workflow-specific operations
- `CMeetingDynamicPageTest.java` - Tests meeting-specific grid selection

Tests verify:
- ✅ Form populates after Create
- ✅ Grid refreshes after Save
- ✅ Form updates on grid selection
- ✅ Confirmation shown for Delete
- ✅ Form repopulates after Refresh
- ✅ Notifications appear for all operations

## Summary

All CRUD operations now:
1. **Work consistently** across all entity types
2. **Refresh UI properly** (grid + form)
3. **Show notifications** to user
4. **Handle errors gracefully**
5. **Use service layer** for initialization and persistence
6. **Validate data** before saving
7. **Confirm dangerous actions** (delete)

The pattern is complete, tested, and production-ready.
