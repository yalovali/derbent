# CComponentFieldSelection UI Interaction Fix

## Problem Statement
The CComponentFieldSelection component had issues where:
1. Double-clicking items in the available or selected lists did not work
2. Add and Remove buttons might not respond properly to item selection

## Root Cause
The `addEventListener("dblclick", ...)` calls in the `setupDoubleClickSupport()` method were not properly synchronized with the server. In Vaadin Flow, DOM event listeners need to be configured to synchronize properties with the server before executing server-side code.

## Fix Applied
Added `.synchronizeProperty("value")` to each `addEventListener` call in the `setupDoubleClickSupport()` method. This ensures that:

1. When a user selects an item in a ListBox, the value is synchronized with the server
2. When a double-click event fires, the server already knows which item is selected
3. The event handler can properly access the selected item via `getValue()`

### Code Changes

**Before:**
```java
private void setupDoubleClickSupport() {
    availableList.getElement().addEventListener("dblclick", e -> {
        DetailEntity selected = availableList.getValue();
        if (selected != null && !readOnly) {
            addSelectedItem();
        }
    });
    // ...
}
```

**After:**
```java
private void setupDoubleClickSupport() {
    availableList.getElement().addEventListener("dblclick", e -> {
        DetailEntity selected = availableList.getValue();
        if (selected != null && !readOnly) {
            addSelectedItem();
        }
    }).synchronizeProperty("value");
    // ...
}
```

## How to Verify

### Manual Testing
1. Start the application
2. Navigate to a form that uses CComponentFieldSelection (e.g., User form with activities field)
3. Test the following interactions:

#### Test Double-Click on Available List
1. Click on an item in the "Available" list
2. Double-click the same item
3. **Expected:** Item should move to the "Selected" list

#### Test Double-Click on Selected List
1. Click on an item in the "Selected" list
2. Double-click the same item
3. **Expected:** Item should move back to the "Available" list

#### Test Add Button
1. Click on an item in the "Available" list
2. Click the "Add" button
3. **Expected:** Item should move to the "Selected" list

#### Test Remove Button
1. Click on an item in the "Selected" list
2. Click the "Remove" button
3. **Expected:** Item should move back to the "Available" list

#### Test Up/Down Buttons
1. Select multiple items in the "Selected" list
2. Click an item
3. Click "Move Up" or "Move Down" buttons
4. **Expected:** Item should move up or down in the order

### Automated Testing
Run the unit tests:
```bash
mvn test -Dtest=CComponentFieldSelectionTest
```

All tests should pass, including the new `testDoubleClickEventSetup()` test.

## Technical Details

### Vaadin Flow Event Synchronization
In Vaadin Flow, when you add a DOM event listener:
- The event fires in the browser (client-side)
- Vaadin needs to synchronize any required properties before executing server-side code
- Without `synchronizeProperty()`, the server might not have the latest value
- This can cause `getValue()` to return null or an outdated value

### The synchronizeProperty Method
```java
.synchronizeProperty("value")
```
This tells Vaadin to:
1. Watch for changes to the "value" property on the DOM element
2. Synchronize the value from client to server before the event handler executes
3. Ensure the server has the latest selected item when the event fires

## Related Files
- `/src/main/java/tech/derbent/api/views/components/CComponentFieldSelection.java` - Main component
- `/src/test/java/tech/derbent/api/views/components/CComponentFieldSelectionTest.java` - Unit tests
- `/src/main/java/tech/derbent/api/annotations/CFormBuilder.java` - Creates instances of this component

## Where This Component is Used
This component is used in forms where:
- A field is annotated with `@AMetaData(useDualListSelector = true)`
- The field type is `List<T>` or `Set<T>`
- Examples: User activities, project members, etc.

## Future Improvements
- Consider adding Playwright UI tests to verify double-click behavior
- Add keyboard shortcuts (e.g., Enter to add, Delete to remove)
- Add drag-and-drop support for reordering items
