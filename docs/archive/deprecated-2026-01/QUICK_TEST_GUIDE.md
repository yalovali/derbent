# Quick Test Guide - Kanban Filter Fix

## Quick Verification Steps

### Prerequisites
1. Application running: `mvn spring-boot:run -Dspring-boot.run.profiles=h2-local-development`
2. Browser open at: http://localhost:8080

### Test Scenario 1: Basic Filter Preservation
**Steps:**
1. Login with "DB Minimal" button
2. Navigate to: Sprints → Kanban Board (or similar menu path)
3. Observe filter toolbar at top with: Sprint, Type (Activity/Meeting/All types), Responsible
4. Change "Type" filter from "Activity" to "Meeting"
5. Observe that only Meeting items are displayed
6. Drag any Meeting item from one column to another
7. **VERIFY**: Type filter still shows "Meeting" (NOT reset to "Activity") ✅
8. **VERIFY**: Only Meeting items are still displayed ✅

### Test Scenario 2: "All types" Filter Preservation
**Steps:**
1. Change "Type" filter to "All types"
2. Observe that all items (Activities + Meetings) are displayed
3. Drag any item from one column to another
4. **VERIFY**: Type filter still shows "All types" ✅
5. **VERIFY**: All items are still displayed ✅

### Test Scenario 3: Multiple Drag Operations
**Steps:**
1. Select "Meeting" from Type filter
2. Drag a Meeting item from column A to column B
3. **VERIFY**: Filter still shows "Meeting" ✅
4. Drag another Meeting item from column B to column C
5. **VERIFY**: Filter still shows "Meeting" ✅
6. Drag the first item back from column B to column A
7. **VERIFY**: Filter still shows "Meeting" ✅

## What Was Fixed

### Before the Fix
```
Select "Meeting" → Drag item → Filter resets to "Activity" ❌
Select "All types" → Drag item → Filter resets to "Activity" ❌
```

### After the Fix
```
Select "Meeting" → Drag item → Filter stays "Meeting" ✅
Select "All types" → Drag item → Filter stays "All types" ✅
Select "Activity" → Drag item → Filter stays "Activity" ✅
```

## Expected Behavior

### Normal Operations (Filter Should Preserve)
- ✅ Dragging items between columns
- ✅ Moving items to/from backlog
- ✅ Changing sprint selection (if implemented)
- ✅ Any board refresh operations

### Special Cases (May Reset)
- ⚠️ Page refresh (F5) - handled by persistence layer
- ⚠️ Navigating away and back - handled by persistence layer

## If Something Goes Wrong

### Filter Still Resets
1. Check browser console for JavaScript errors
2. Check server logs for exceptions
3. Verify `CEntityTypeFilter.updateTypeOptions()` is called
4. Add debug logging to trace filter value changes

### Debug Logging
Add to `application.properties`:
```properties
logging.level.tech.derbent.api.ui.component.filter=DEBUG
```

Then look for logs like:
```
DEBUG CEntityTypeFilter - Captured entity class: CMeeting
DEBUG CEntityTypeFilter - Restored selection to: Meeting
```

## Code Changes Summary

**File**: `src/main/java/tech/derbent/api/ui/component/filter/CEntityTypeFilter.java`
**Method**: `updateTypeOptions(List<?> items)`
**Lines**: ~174-223

**Key Change**:
```java
// OLD (buggy):
comboBox.setItems(typeOptions);
final TypeOption currentValue = comboBox.getValue();  // Always null!

// NEW (fixed):
final Class<?> selectedEntityClass = comboBox.getValue().getEntityClass();
comboBox.setItems(typeOptions);
// Find and restore matching TypeOption by entity class
```

## Success Criteria

The fix is working correctly if:
- ✅ Filter selection is preserved during all drag-drop operations
- ✅ No console errors during drag-drop
- ✅ Board displays correct items after filter preservation
- ✅ User experience feels smooth and predictable

## Quick Smoke Test

**60-second test to verify the fix works:**
1. Login
2. Go to Kanban Board
3. Select "Meeting" filter
4. Drag any item
5. Check if filter still shows "Meeting"
6. If yes → **FIX WORKS!** ✅
7. If no → Check console and logs
