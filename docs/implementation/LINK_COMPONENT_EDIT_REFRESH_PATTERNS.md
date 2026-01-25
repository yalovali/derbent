# Link Component Edit & Refresh Patterns

**Date**: 2026-01-25  
**Status**: MANDATORY - Critical patterns for dialog edit mode and grid refresh  
**Related**: LINK_COMPONENT_ENHANCEMENT_SUMMARY.md, DIALOG_PATTERN_COMPLIANCE_SUMMARY.md

---

## Table of Contents

1. [Overview](#overview)
2. [Edit Mode Selection Pattern](#edit-mode-selection-pattern)
3. [Grid Refresh Pattern](#grid-refresh-pattern)
4. [Dialog Fixed Sizing Pattern](#dialog-fixed-sizing-pattern)
5. [Target Label Update Pattern](#target-label-update-pattern)
6. [Common Issues & Solutions](#common-issues--solutions)

---

## Overview

This document describes critical patterns discovered during link component development for:
- **Restoring selection in edit dialogs** (preventing field clearing during initialization)
- **Refreshing grids after edits** (ensuring UI reflects database state)
- **Fixed dialog sizing** (preventing resize/scroll issues with dynamic content)
- **Dynamic header labels** (showing selection state to users)

---

## Edit Mode Selection Pattern

### Problem

When opening a dialog in edit mode with a selection component (e.g., `CComponentEntitySelection`):

1. Dialog constructor receives entity with `targetType=CActivity, targetId=5` ✅
2. `createFormFields()` creates the selection component
3. Component's `setEntityType()` loads grid and fires `onSelectionChanged(empty)` 🔥
4. Handler sees empty selection and **clears target fields** ❌
5. `populateForm()` runs but fields are already null ❌

**Root Cause**: Selection change handler fires DURING component initialization, before we restore the actual selection.

### Solution: Check Edit Mode in Handler

```java
private void onTargetSelectionChanged(final Set<CEntityDB<?>> selectedItems) {
    LOGGER.debug("[DialogLink] Target selection changed: {} items selected (isNew: {})", 
            selectedItems != null ? selectedItems.size() : 0, isNew);
    
    if (selectedItems == null || selectedItems.isEmpty()) {
        // CRITICAL: Don't clear fields during form initialization in edit mode
        // When dialog opens in edit mode, createFormFields() triggers grid load which fires
        // this event with empty selection BEFORE we restore the actual selection
        if (!isNew) {
            LOGGER.debug("[DialogLink] Ignoring selection clear in edit mode during initialization");
            return;  // ← KEY: Exit early in edit mode
        }
        
        // Only clear in new mode (user intentionally deselected)
        getEntity().setTargetEntityId(null);
        getEntity().setTargetEntityType(null);
        LOGGER.debug("[DialogLink] Target entity cleared");
        return;
    }
    
    // Update entity with selection
    final CEntityDB<?> selected = selectedItems.iterator().next();
    getEntity().setTargetEntityId(selected.getId());
    getEntity().setTargetEntityType(selected.getClass().getSimpleName());
    LOGGER.debug("[DialogLink] Target entity set: {} #{}", 
        selected.getClass().getSimpleName(), selected.getId());
}
```

### Execution Flow

#### ❌ Before Fix
```
1. Dialog opens with entity (targetType=CActivity, targetId=5)
2. createFormFields() creates selection component
3. → onSelectionChanged(empty) fires
4. → Handler clears: targetType=null, targetId=null
5. populateForm() cannot restore selection (data lost)
```

#### ✅ After Fix
```
1. Dialog opens with entity (targetType=CActivity, targetId=5)
2. createFormFields() creates selection component
3. → onSelectionChanged(empty) fires
4. → Handler checks !isNew → returns early (no clearing)
5. populateForm() restores selection successfully
```

### Key Rules

| Scenario | Selection Event | Handler Action |
|----------|----------------|----------------|
| **New mode, user deselects** | Empty | Clear entity fields ✅ |
| **New mode, initialization** | Empty | Clear entity fields ✅ (already empty) |
| **Edit mode, initialization** | Empty | Ignore (return early) ✅ |
| **Edit mode, user deselects** | Empty | Ignore (user can't deselect in single-select) ⚠️ |
| **Any mode, user selects** | 1 item | Update entity fields ✅ |

**Trade-off**: In edit mode, user cannot clear selection by deselecting grid item. They must cancel and create new link. This is acceptable for link integrity.

---

## Grid Refresh Pattern

### Problem

After editing a link in the dialog:
- Grid shows old data (cached from initial load)
- Changes to `targetEntityType`, `description`, etc. not visible
- User must manually reload master entity to see updates

### Solution: Reload Master Entity After Save

```java
protected void on_buttonEdit_clicked() {
    try {
        final CLink selected = grid.asSingleSelect().getValue();
        Check.notNull(selected, "No link selected");
        
        // Load fresh entity from database
        final CLink refreshedLink = linkService.getById(selected.getId())
            .orElseThrow(() -> new IllegalStateException("Link not found: " + selected.getId()));
        
        final CDialogLink dialog = new CDialogLink(linkService, sessionService, refreshedLink, link -> {
            try {
                linkService.save(link);
                
                // CRITICAL: Reload master entity from database to get updated link collection
                reloadMasterEntity();
                
                // Refresh grid to show updated data
                refreshGrid();
                notifyRefreshListeners(link);
                CNotificationService.showSuccess("Link updated successfully");
            } catch (final Exception e) {
                LOGGER.error("Error saving link", e);
                CNotificationService.showException("Error saving link", e);
            }
        }, false);
        dialog.open();
    } catch (final Exception e) {
        LOGGER.error("Error opening edit dialog", e);
        CNotificationService.showException("Error opening edit dialog", e);
    }
}
```

### Reload Master Entity Implementation

```java
/** Reload master entity from database to get fresh data.
 * CRITICAL: Links are stored in master entity's collection. After save,
 * the collection in memory is stale and must be reloaded from DB. */
private void reloadMasterEntity() {
    try {
        if (masterEntity == null || masterEntity.getId() == null) {
            LOGGER.warn("[ComponentLink] Cannot reload master entity - entity is null or not persisted");
            return;
        }
        
        final Class<?> entityClass = masterEntity.getClass();
        final CAbstractService<?> service = CSpringContext.getServiceClassForEntity(masterEntity);
        
        final Optional<?> reloadedOpt = service.getById(masterEntity.getId());
        if (reloadedOpt.isEmpty()) {
            LOGGER.error("[ComponentLink] Master entity not found after reload: {} #{}", 
                entityClass.getSimpleName(), masterEntity.getId());
            return;
        }
        
        // Cast to IHasLinks and update reference
        if (reloadedOpt.get() instanceof IHasLinks) {
            this.masterEntity = (IHasLinks) reloadedOpt.get();
            LOGGER.debug("[ComponentLink] Master entity reloaded: {} #{}", 
                entityClass.getSimpleName(), masterEntity.getId());
        }
    } catch (final Exception e) {
        LOGGER.error("[ComponentLink] Error reloading master entity", e);
    }
}
```

### Why This is Necessary

**JPA/Hibernate Behavior**:
- Links are stored in master entity's `@OneToMany` collection
- When you save a link via `linkService.save()`, Hibernate updates the database
- BUT the in-memory master entity object still has the OLD collection
- Grid is bound to the in-memory collection → shows stale data

**Without Reload**:
```
1. Load masterEntity (Activity #8) with links collection
2. Edit link #2 (change description)
3. Save link #2 (database updated)
4. Grid still shows old collection from step 1 ❌
```

**With Reload**:
```
1. Load masterEntity (Activity #8) with links collection
2. Edit link #2 (change description)
3. Save link #2 (database updated)
4. Reload masterEntity from database (fresh collection)
5. refreshGrid() updates UI with new collection ✅
```

### Key Rules

- ✅ **ALWAYS** reload master entity after saving link
- ✅ **ALWAYS** call `refreshGrid()` after reload
- ✅ **ALWAYS** notify refresh listeners for cascading updates
- ❌ **DON'T** assume in-memory collection is current
- ❌ **DON'T** manually update in-memory collection (error-prone)

---

## Dialog Fixed Sizing Pattern

### Problem

Dialogs with dynamic content (grids that resize based on filters) cause:
- Dialog shrinks/grows as user filters → disorienting
- Horizontal scroll appears when grid is wide → unusable
- Inconsistent UI experience

### Solution: Fixed Dialog Dimensions

```java
@Override
protected void setupDialog() throws Exception {
    super.setupDialog();
    // Fixed dialog width and height to prevent resizing and horizontal scroll
    setWidth("700px");
    setHeight("650px");
}
```

### Sizing Guidelines

| Dialog Content | Recommended Width | Recommended Height |
|----------------|-------------------|-------------------|
| **Simple form** (2-3 fields) | 500px | 400px |
| **Medium form** (5-10 fields) | 600px | 550px |
| **With selection grid** | 700px | 650px |
| **Complex multi-step** | 800px | 700px |

**Rationale**:
- **Width**: 700px accommodates grid columns without scroll
- **Height**: 650px shows ~8-10 grid rows without scroll
- **Fixed**: User knows dialog size won't change mid-interaction

### Grid Height Within Dialog

```java
// In createTargetSelectionComponent()
targetSelection.setDynamicHeight("320px");  // Fixed grid height
```

**Formula**: Dialog height - (header + form fields + buttons + padding) = grid height
- Example: 650px - (80px + 150px + 80px + 20px) = 320px

---

## Target Label Update Pattern

### Problem

User cannot see what target entity is selected without scrolling to grid or checking selection indicator.

### Solution: Header Label with Dynamic Updates

#### 1. Add Label Field

```java
private Span targetInfoLabel; // Header label showing current target selection
```

#### 2. Create Header Layout

```java
private void createFormFields() throws Exception {
    // Header with Source and Target labels (read-only, side by side)
    final HorizontalLayout headerLayout = new HorizontalLayout();
    headerLayout.setWidthFull();
    headerLayout.setSpacing(true);
    headerLayout.getStyle().set("margin-bottom", "8px");
    
    // Source entity display (read-only)
    if (getEntity().getSourceEntityType() != null && getEntity().getSourceEntityId() != null) {
        final Class<?> sourceClass = CEntityRegistry.getEntityClass(getEntity().getSourceEntityType());
        final String sourceDisplay = String.format("%s #%d", 
            CEntityRegistry.getEntityTitleSingular(sourceClass), 
            getEntity().getSourceEntityId());
        final Span sourceLabel = new Span("Source: " + sourceDisplay);
        sourceLabel.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-style", "italic");
        headerLayout.add(sourceLabel);
    }
    
    // Target entity display (shows selected target, updated dynamically)
    targetInfoLabel = new Span("Target: (not selected)");
    targetInfoLabel.getStyle()
        .set("font-size", "0.875rem")
        .set("color", "var(--lumo-error-color)")
        .set("font-style", "italic");
    headerLayout.add(targetInfoLabel);
    
    // Update target label if in edit mode with existing target
    if (!isNew && getEntity().getTargetEntityType() != null && getEntity().getTargetEntityId() != null) {
        updateTargetInfoLabel(getEntity().getTargetEntityType(), getEntity().getTargetEntityId());
    }
    
    formLayout.add(headerLayout);
}
```

#### 3. Update Label on Selection Change

```java
private void onTargetSelectionChanged(final Set<CEntityDB<?>> selectedItems) {
    if (selectedItems == null || selectedItems.isEmpty()) {
        // ... existing logic ...
        updateTargetInfoLabel(null, null);  // ← Update label
        return;
    }
    
    final CEntityDB<?> selected = selectedItems.iterator().next();
    getEntity().setTargetEntityId(selected.getId());
    getEntity().setTargetEntityType(selected.getClass().getSimpleName());
    updateTargetInfoLabel(selected.getClass().getSimpleName(), selected.getId());  // ← Update label
}
```

#### 4. Update Method with Color Coding

```java
/** Update the target info label in the header.
 * @param targetType the target entity type (simple class name)
 * @param targetId the target entity ID */
private void updateTargetInfoLabel(final String targetType, final Long targetId) {
    if (targetInfoLabel == null) {
        return;
    }
    
    if (targetType == null || targetId == null) {
        targetInfoLabel.setText("Target: (not selected)");
        targetInfoLabel.getStyle().set("color", "var(--lumo-error-color)");  // Red
        return;
    }
    
    try {
        final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
        final String displayName = CEntityRegistry.getEntityTitleSingular(entityClass);
        targetInfoLabel.setText(String.format("Target: %s #%d", displayName, targetId));
        targetInfoLabel.getStyle().set("color", "var(--lumo-success-color)");  // Green
    } catch (final Exception e) {
        LOGGER.debug("Could not format target label: {}", e.getMessage());
        targetInfoLabel.setText(String.format("Target: %s #%d", targetType, targetId));
        targetInfoLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");  // Gray
    }
}
```

### Visual States

| State | Text | Color | When |
|-------|------|-------|------|
| **Not selected** | `Target: (not selected)` | Red | New mode, no selection |
| **Selected** | `Target: Activity #5` | Green | Valid selection |
| **Error** | `Target: CActivity #5` | Gray | Class name fallback |

---

## Common Issues & Solutions

### Issue 1: Fields Cleared in Edit Mode

**Symptom**: When editing, target fields become null and selection doesn't restore.

**Diagnosis**:
```
[DialogLink] Refreshed link from DB - targetType: CActivity ✅
[DialogLink] Target entity cleared ❌
[DialogLink] BEFORE readBean - targetType: null ❌
```

**Solution**: Check `!isNew` in selection change handler (see [Edit Mode Selection Pattern](#edit-mode-selection-pattern))

---

### Issue 2: Grid Shows Stale Data After Edit

**Symptom**: After editing link, grid still shows old description/type.

**Diagnosis**:
- Link saved to database ✅
- Grid not updated ❌
- Refresh button works ✅ (proves data is in DB)

**Solution**: Call `reloadMasterEntity()` after save (see [Grid Refresh Pattern](#grid-refresh-pattern))

---

### Issue 3: Dialog Resizes with Filters

**Symptom**: Dialog shrinks when filtering reduces grid rows, grows when adding rows.

**Diagnosis**:
- Default responsive dialog behavior
- Grid height not fixed

**Solution**: Override `setupDialog()` with fixed dimensions (see [Dialog Fixed Sizing Pattern](#dialog-fixed-sizing-pattern))

---

### Issue 4: Horizontal Scroll in Dialog

**Symptom**: Grid wider than dialog, horizontal scrollbar appears.

**Diagnosis**:
- Default dialog width (600px) too narrow for grid columns
- Grid grows beyond dialog bounds

**Solution**: Increase dialog width to 700px and set fixed height

---

### Issue 5: Binder Clearing Unbound Fields

**Symptom**: Fields have values before `readBean()`, null after.

**Diagnosis**:
```java
LOGGER.debug("BEFORE readBean - targetType: {}", getEntity().getTargetEntityType());  // CActivity
binder.readBean(getEntity());
LOGGER.debug("AFTER readBean - targetType: {}", getEntity().getTargetEntityType());   // null
```

**Solution**: Save/restore unbound fields around `readBean()`
```java
final String savedTargetType = getEntity().getTargetEntityType();
final Long savedTargetId = getEntity().getTargetEntityId();

binder.readBean(getEntity());

getEntity().setTargetEntityType(savedTargetType);
getEntity().setTargetEntityId(savedTargetId);
```

**Note**: This was investigated but turned out NOT to be the root cause. The actual issue was the selection handler clearing fields (Issue #1).

---

## Pattern Checklist

When implementing edit dialogs with selection components:

- [ ] **Selection Handler**: Check `!isNew` before clearing fields on empty selection
- [ ] **Grid Refresh**: Call `reloadMasterEntity()` after save in callback
- [ ] **Dialog Sizing**: Override `setupDialog()` with fixed width/height
- [ ] **Header Label**: Add dynamic label showing selection state
- [ ] **Update Label**: Call `updateLabel()` in selection change handler
- [ ] **Logging**: Add debug logs to trace entity state through initialization
- [ ] **Testing**: Test both new and edit modes, verify selection restored

---

## References

- **AGENTS.md** §6.2 - Dialog UI Design Rules
- **AGENTS.md** §6.5 - Navigation Rules
- **LINK_COMPONENT_ENHANCEMENT_SUMMARY.md** - Link component architecture
- **DIALOG_PATTERN_COMPLIANCE_SUMMARY.md** - Dialog patterns

---

**CRITICAL**: These patterns are MANDATORY for any component using selection dialogs in edit mode. Failure to follow leads to data loss and UX issues.
