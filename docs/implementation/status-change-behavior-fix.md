# Status Change Behavior Fix - CCrudToolbar

## Overview
This document describes the fix applied to CCrudToolbar status change behavior to align with standard form field patterns.

## Problem Statement
Previously, when a user changed the status of an entity via the CCrudToolbar status combobox, the entity was **automatically saved** to the database. This behavior was inconsistent with how other form fields work and prevented users from making batch changes or discarding changes via the Refresh button.

## Solution
Changed status combobox behavior to only update the entity **in memory**. Users must explicitly click the Save button to persist changes.

## Behavior Comparison

### ❌ BEFORE (Auto-Save) - INCORRECT

```
User Action Flow:
1. User selects entity in grid
2. Form displays entity details
3. User changes status in combobox → CCrudToolbar.on_actionStatusChange()
                                   ↓
                    IPageServiceHasStatusAndWorkflow.actionChangeStatus()
                                   ↓
                    entity.setStatus(newStatus)
                                   ↓
                    service.save(entity) ← AUTO-SAVE ❌
                                   ↓
                    view.onEntitySaved() → Grid refreshes
                                   ↓
                    Notification: "Status changed to 'X'"

Result: Entity saved immediately, no way to undo
```

### ✅ AFTER (Manual Save) - CORRECT

```
User Action Flow:
1. User selects entity in grid
2. Form displays entity details
3. User changes status in combobox → CCrudToolbar.on_actionStatusChange()
                                   ↓
                    IPageServiceHasStatusAndWorkflow.actionChangeStatus()
                                   ↓
                    entity.setStatus(newStatus) ← In memory only ✓
                                   ↓
                    view.populateForm() → Form updates to show new status
                                   ↓
                    Notification: "Status set to 'X' (click Save to persist)"

4. User clicks Save button → CCrudToolbar.on_actionSave()
                          ↓
                    CPageService.actionSave()
                          ↓
                    service.save(entity) ← Explicit save ✓
                          ↓
                    view.onEntitySaved() → Grid refreshes
                          ↓
                    Notification: "Save successful"

Result: Entity saved only when user clicks Save, can be discarded via Refresh
```

## Code Changes

### File: `IPageServiceHasStatusAndWorkflow.java`

#### Before:
```java
// Status change is valid - apply it
final String oldStatusName = ...;
((IHasStatusAndWorkflow<?>) entity).setStatus(newStatus);
LOGGER.info("Status changed from '{}' to '{}' for entity ID: {}", ...);

// Save the entity to persist the status change ❌
final EntityClass savedEntity = getEntityService().save(entity);
LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
setCurrentEntity(savedEntity);

// Notify view that entity was saved - this triggers grid refresh
getView().onEntitySaved(savedEntity);
getView().populateForm();
CNotificationService.showInfo(String.format("Status changed to '%s'", newStatus.getName()));
```

#### After:
```java
// Status change is valid - apply it to entity in memory (does NOT auto-save) ✓
final String oldStatusName = ...;
((IHasStatusAndWorkflow<?>) entity).setStatus(newStatus);
LOGGER.info("Status set from '{}' to '{}' for entity (not saved yet)", ...);

// Update the current entity reference (no save - user must click Save button) ✓
setCurrentEntity(entity);

// Refresh the form to show the updated status value
getView().populateForm();
CNotificationService.showInfo(String.format("Status set to '%s' (click Save to persist)", newStatus.getName()));
```

## Benefits

### 1. Consistency with Form Fields
All form fields now behave identically:
- **Name field**: Change → in memory → Save button → persists
- **Description field**: Change → in memory → Save button → persists
- **Status field**: Change → in memory → Save button → persists ✓

### 2. User Control
Users have explicit control over when changes are saved:
- Can review changes before saving
- Can change multiple fields including status, then save once
- Clear feedback about unsaved state

### 3. Batch Changes
Users can make multiple changes in a single save operation:
```
1. Change status: "New" → "In Progress"
2. Update description
3. Assign to user
4. Click Save once → All changes persisted together
```

### 4. Undo Capability
Users can discard ALL unsaved changes:
```
1. Change status: "New" → "In Progress"
2. Update description
3. Change mind → Click Refresh button
4. All changes discarded, entity reloaded from database
```

## Testing Workflow

### Manual Test Scenario
1. **Navigate to Activities View**
   - Login to application
   - Go to Activities management page

2. **Select an Activity**
   - Click on an activity in the grid
   - Form displays activity details

3. **Change Status WITHOUT Saving**
   - Change status from current value to different value
   - **Expected**: Notification "Status set to 'X' (click Save to persist)"
   - **Expected**: Form shows new status value
   - **Expected**: Grid still shows OLD status (not saved yet)
   - **Expected**: Save button is enabled

4. **Discard Changes via Refresh**
   - Click Refresh button
   - **Expected**: Notification "Entity reloaded"
   - **Expected**: Form shows original status value
   - **Expected**: Status change discarded

5. **Change Status AND Save**
   - Change status again
   - Click Save button
   - **Expected**: Notification "Save successful"
   - **Expected**: Grid refreshes and shows NEW status
   - **Expected**: Status persisted to database

6. **Change Multiple Fields Including Status**
   - Select another activity
   - Change status
   - Change description
   - Click Save once
   - **Expected**: Both changes saved together
   - **Expected**: Grid shows updated status

### Automated Test (Future)
```java
@Test
public void testStatusChangeDoesNotAutoSave() {
    // Setup: Create activity with initial status
    CActivity activity = createActivity("Test", initialStatus);
    
    // Action: Change status via combobox
    statusCombobox.setValue(newStatus);
    
    // Assert: Entity in memory has new status
    assertEquals(newStatus, activity.getStatus());
    
    // Assert: Database still has old status (not saved)
    CActivity dbActivity = activityService.getById(activity.getId());
    assertEquals(initialStatus, dbActivity.getStatus());
    
    // Action: Click Save button
    saveButton.click();
    
    // Assert: Database now has new status (saved)
    CActivity savedActivity = activityService.getById(activity.getId());
    assertEquals(newStatus, savedActivity.getStatus());
}
```

## Migration Notes

### For Developers
If you have custom page services that override `actionChangeStatus()`, ensure they:
1. Do NOT call `service.save(entity)`
2. Do call `setCurrentEntity(entity)` to update the reference
3. Do call `getView().populateForm()` to refresh the form
4. Use notification: `"Status set to 'X' (click Save to persist)"`

### For Users
Users will notice:
1. Status changes are no longer saved immediately
2. Must click Save button to persist status changes
3. Can discard status changes via Refresh button
4. Can change status along with other fields in a single save operation

## Related Documentation
- [Notification Pattern Standards](../development/copilot-guidelines.md#notification-pattern-standards-mandatory)
- [Status Change Pattern](../development/copilot-guidelines.md#status-change-pattern-mandatory)
- [Coding Standards](../architecture/coding-standards.md)
