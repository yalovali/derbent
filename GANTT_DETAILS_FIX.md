# Gantt Item Details Update Fix

## Problem Statement
When clicking on gantt items, the details section does not update itself. It does not switch the details content between CActivity and CMeeting when clicked, and it does not refresh the content. This issue occurred after recent changes to fix the save refresh issue.

## Root Cause Analysis

### The Problematic Flow
1. **User clicks on a gantt item** (e.g., CActivity with ID=1)
2. **`onSelectionChanged()`** is called in `CGridViewBaseGannt.java`
3. **`updateDetailsComponent()`** is called to display the details
4. **PROBLEM**: `updateDetailsComponent()` first checks if there's a `currentActualEntity` in the page service (lines 204-206)
5. **If `currentActualEntity` exists from a previous selection or save operation**, it uses that **stale entity** instead of fetching the entity from the newly clicked CGanttItem
6. **Result**: Details don't update when clicking on different items

### Code Evidence

#### Before Fix - `CGridViewBaseGannt.java` (lines 169-183)
```java
@Override
protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
    final EntityClass value = event.getSelectedItem();
    if (value != null) {
        Check.instanceOf(value, CGanttItem.class, "Selected item is not a CGanttItem");
        setCurrentEntity(value);
        populateForm();  // <-- This calls updateDetailsComponent()
        return;
    } else {
        super.onSelectionChanged(event);
    }
}
```

#### The Problem in `updateDetailsComponent()` (lines 196-232)
```java
@Override
protected void updateDetailsComponent() throws Exception {
    LOGGER.debug("Updating details component for Gantt view");
    getBaseDetailsLayout().removeAll();
    
    // PROBLEM: First tries to get entity from page service cache
    CProjectItem<?> ganttEntity = null;
    if (getPageService() instanceof CPageServiceProjectGannt) {
        ganttEntity = ((CPageServiceProjectGannt) getPageService()).getCurrentActualEntity();  // <-- Gets stale entity!
    }
    
    // Only fetches fresh entity if cache is null
    if (ganttEntity == null) {
        if (getCurrentEntity() == null) {
            return;
        }
        ganttEntity = ((CGanttItem) getCurrentEntity()).getGanntItem(activityService, meetingService);
    }
    // ... rest of the method
}
```

### Why This Happened

The `currentActualEntity` in `CPageServiceProjectGannt` is set in various scenarios:
1. **After saving an entity** (`actionSave()` line 216)
2. **After creating a new entity** (`actionCreate()` line 47)
3. **After refreshing** (`actionRefresh()` lines 114, 157)
4. **When updating details** (`updateDetailsComponent()` line 230)

Once set, it stays there until explicitly cleared. So when you:
1. Click on Activity-1 → `currentActualEntity` = Activity-1
2. Save it → `currentActualEntity` = Activity-1 (refreshed version)
3. Click on Meeting-2 → **Still shows Activity-1** because `updateDetailsComponent()` finds it in the cache

## The Fix

### Solution
Clear the `currentActualEntity` in the page service **BEFORE** calling `populateForm()`, so that `updateDetailsComponent()` will be forced to fetch the fresh entity from the newly selected CGanttItem.

### After Fix - `CGridViewBaseGannt.java` (lines 169-187)
```java
@Override
protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
    final EntityClass value = event.getSelectedItem();
    if (value != null) {
        Check.instanceOf(value, CGanttItem.class, "Selected item is not a CGanttItem");
        setCurrentEntity(value);
        
        // Clear the cached entity in page service to force refresh from the selected item
        // This ensures details update correctly when switching between different gantt items
        if (getPageService() instanceof CPageServiceProjectGannt) {
            ((CPageServiceProjectGannt) getPageService()).setCurrentActualEntity(null);
        }
        
        populateForm();
        return;
    } else {
        super.onSelectionChanged(event);
    }
}
```

## Testing the Fix

### Manual Testing Steps
1. **Start the application**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=h2-local-development
   ```

2. **Navigate to Gantt chart view**
   - Login with admin/test123
   - Navigate to Project Gantt view

3. **Test scenario: Switch between different entity types**
   - Click on a CActivity item
   - **Verify**: Details section shows Activity form with correct data
   - Click on a CMeeting item
   - **Verify**: Details section switches to Meeting form with correct data
   - Click back on the CActivity item
   - **Verify**: Details section switches back to Activity form with correct data

4. **Test scenario: After save operation**
   - Click on an Activity item
   - Edit and save it
   - Click on a different Meeting item
   - **Verify**: Details section switches to the Meeting (not staying on Activity)
   - Click on a different Activity item
   - **Verify**: Details section switches to the new Activity

### Expected Behavior
✅ **After Fix:**
- Clicking any gantt item **immediately updates** the details section
- Switching between CActivity and CMeeting **correctly changes** the form type
- After save operations, clicking different items **works correctly**
- Details section **always shows the clicked item**, not a cached/stale entity

❌ **Before Fix:**
- Clicking different items often showed the **same entity** (stale/cached)
- Details section did not **switch between entity types** properly
- After save, details would be **stuck** on the saved entity

## Technical Details

### Key Classes Involved
1. **`CGridViewBaseGannt.java`** - The view that handles gantt item selection
2. **`CGanttItem.java`** - DTO wrapper for gantt display items
3. **`CPageServiceProjectGannt.java`** - Page service that manages current entity state
4. **`CMasterViewSectionGannt.java`** - Master section that contains the gantt grid

### The Fix in Context
The fix is **minimal and surgical**:
- Only 4 lines added to `onSelectionChanged()`
- No changes to other methods or classes
- Follows the existing pattern (checking `instanceof` before casting)
- Includes clear explanatory comments

### Why This Fix Works
1. When a gantt item is clicked, we **explicitly clear** the cached entity
2. This forces `updateDetailsComponent()` to take the **"if (ganttEntity == null)"** branch
3. Which then **fetches the fresh entity** from the CGanttItem: 
   ```java
   ganttEntity = ((CGanttItem) getCurrentEntity()).getGanntItem(activityService, meetingService);
   ```
4. The fresh entity is then used to populate the form
5. At the end of `updateDetailsComponent()`, the fresh entity is **cached back** for future operations

## Conclusion

This fix resolves the issue where gantt item clicks were not updating the details section. The root cause was a cached entity in the page service that was not being cleared on new selections. The fix is minimal, targeted, and follows existing code patterns.

The fix ensures that:
- ✅ Clicking different gantt items updates the details
- ✅ Switching between entity types (Activity/Meeting) works correctly  
- ✅ Save operations don't break subsequent selections
- ✅ Details always reflect the currently selected item
