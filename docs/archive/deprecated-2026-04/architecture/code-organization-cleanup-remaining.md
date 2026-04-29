# Code Organization Cleanup - Remaining Tasks

## Overview

This document tracks remaining code organization issues discovered during the comprehensive audit and provides guidance for incremental cleanup.

**Date**: 2026-01-03  
**Related PRs**: copilot/add-coding-rule-for-unrelated-methods

## Completed Work

### ✅ Documentation Created
1. **`docs/architecture/method-placement-guidelines.md`** - Comprehensive rules for where methods belong
2. **`docs/architecture/component-utility-reference.md`** - Complete index of all components, utilities, dialogs
3. Updated **`docs/architecture/coding-standards.md`** - Added mandatory rules section

### ✅ Critical Fixes Applied
1. **Misplaced Methods**:
   - Moved `createCompactUserLabel()` from `CComponentKanbanPostit` to `CLabelEntity`
   - Replaced private `getColorForEntity()` in `CLabelEntity` with `CColorUtils.getColorFromEntity()`

2. **Component Usage - Kanban Components**:
   - `CComponentKanbanPostit`: Replaced raw `Span` with `CSpan`
   - `CComponentKanbanColumn`: Replaced raw `Span` with `CSpan` (2 instances)
   - `CComponentWidgetSprint`: Replaced raw `Span` with `CSpan`

## Remaining Tasks

### Priority 1: High-Visibility Components (Recommend Next)

These are in user-facing components and should be addressed soon:

#### MainLayout.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/view/MainLayout.java
Line 77:  final var appName = new Span("Derbent");
Line 215: final var appName = new Span("Derbent");

// Impact: Application header/branding
// Priority: HIGH - User always sees this
// Recommendation: Replace with CSpan
```

#### CViewToolbar.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CViewToolbar.java
Line 124: new Div(new Span("Active Project:"), projectComboBox)
Line 396: usernameSpan = new Span(username);

// Impact: Toolbar visible on every page
// Priority: HIGH - User always sees this
// Recommendation: Replace Div with CDiv, Span with CSpan
```

### Priority 2: Enhanced Components (Moderate Priority)

These are in reusable enhanced components:

#### CHierarchicalSideMenu.java (5 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CHierarchicalSideMenu.java
Line 98:  final Span itemText = new Span(name);
Line 392: final Span itemText = new Span(item.name);
Line 432: final Span noResults = new Span("No matching items found");
Line 597: final Span levelTitle = new Span(level.getDisplayName());
Line 612: final Span title = new Span("Search Results");

// Impact: Navigation menu used throughout application
// Priority: MEDIUM
// Recommendation: Replace all with CSpan
```

#### CDialogPictureSelector.java (1 instance)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CDialogPictureSelector.java
Line 85: dropLabel = new Span("Drop image here or click to upload");

// Impact: Image upload dialogs
// Priority: MEDIUM
// Recommendation: Replace with CSpan
```

#### CComponentEntitySelection.java (1 instance)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CComponentEntitySelection.java
Line 415: labelSelectedCount = new Span("0 selected");

// Impact: Entity selection components
// Priority: MEDIUM
// Recommendation: Replace with CSpan
```

#### CDashboardStatCard.java (1 instance)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CDashboardStatCard.java
Line 47: this.titleLabel = new Span(title);

// Impact: Dashboard statistics cards
// Priority: MEDIUM
// Recommendation: Replace with CSpan
```

### Priority 3: Basic Components (Lower Priority)

These are in renderer/utility contexts where raw Span might be acceptable:

#### CColorAwareComboBox.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/basic/CColorAwareComboBox.java
Line 152: return new Span("N/A");
Line 155: return new Span("Invalid Entity");

// Context: ComboBox renderers
// Priority: LOW - Internal rendering
// Recommendation: Consider leaving as-is or batch with other renderer updates
```

#### CColorPickerComboBox.java (3 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/basic/CColorPickerComboBox.java
Line 72: Span colorItem = new Span();
Line 75: Span colorSquare = new Span();
Line 78: Span colorText = new Span(colorValue.toUpperCase());

// Context: ComboBox color picker rendering
// Priority: LOW - Internal rendering
// Recommendation: Consider leaving as-is or batch update
```

#### CEntityLabel.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/basic/CEntityLabel.java
Line 119: add(new Span("N/A"));
Line 127: final Span textSpan = new Span(displayText);

// Context: Entity label component (consider deprecating in favor of CLabelEntity)
// Priority: LOW - May be replaced by CLabelEntity usage
// Recommendation: Deprecate CEntityLabel, migrate to CLabelEntity
```

### Priority 4: Screen/Dialog Components (Can Be Batched)

#### CDualListSelectorComponent.java (3 instances)
```java
// Location: src/main/java/tech/derbent/api/screens/view/CDualListSelectorComponent.java
Line 129: return new Span("N/A");
Line 143: return new Span(text);
Line 148: return new Span(fallbackText);

// Context: Dual list selector renderers
// Priority: LOW
```

#### CDialogDetailLinesEdit.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/screens/view/CDialogDetailLinesEdit.java
Line 58: tabEntitySpan = new Span();
Line 59: tabSectionSpan = new Span();

// Context: Dialog tabs
// Priority: LOW
```

#### CComponentUserProjectRelationBase.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CComponentUserProjectRelationBase.java
Line 124: return new Span(getDisplayText(settings, "project"));
Line 133: return new Span(getDisplayText(settings, "user"));

// Context: Relation component renderers
// Priority: LOW
```

### Priority 5: Grid/Form Components (Internal Use)

#### CComponentStoryPoint.java (1 instance)
```java
// Location: src/main/java/tech/derbent/api/grid/view/CComponentStoryPoint.java
Line 65: valueSpan = new Span(formatValue(item.getStoryPoint()));

// Context: Grid cell renderer
// Priority: LOW
```

#### CComponentId.java (1 instance)
```java
// Location: src/main/java/tech/derbent/api/grid/view/CComponentId.java
Line 21: final Span idSpan = new Span(idValue != null ? String.valueOf(idValue) : ID_PLACEHOLDER);

// Context: Grid cell renderer
// Priority: LOW
```

#### CFormBuilder.java (3 instances)
```java
// Location: src/main/java/tech/derbent/api/annotations/CFormBuilder.java
Line 653: return new Span("No icon selected");
Line 666: final Span placeholder = new Span("?");
Line 670: final Span label = new Span(iconName);

// Context: Form builder - generates forms dynamically
// Priority: LOW
```

#### CComponentFieldSelection.java (2 instances)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CComponentFieldSelection.java
Line 125: return new Span("N/A");
Line 136: return new Span(text);
Line 140: return new Span(fallbackText);

// Context: Field selection renderers
// Priority: LOW
```

#### CComponentListSelection.java (1 instance)
```java
// Location: src/main/java/tech/derbent/api/ui/component/enhanced/CComponentListSelection.java
Line 140: return new Span("N/A");

// Context: List selection renderer
// Priority: LOW
```

### Priority 6: Application-Specific Components

#### CCommentView.java (2 instances)
```java
// Location: src/main/java/tech/derbent/app/comments/view/CCommentView.java
Line 205: final Span eventDate = new Span(comment.getEventDate().toString());
Line 220: final Span prioritySpan = new Span(comment.getPriorityName());

// Context: Comment display
// Priority: LOW
```

#### CGanntTimelineHeader.java (1 instance)
```java
// Location: src/main/java/tech/derbent/app/gannt/ganntviewentity/view/components/CGanntTimelineHeader.java
Line 76: private final Span windowSummary = new Span();

// Context: Gantt chart header
// Priority: LOW
```

### Test Files

#### CUserIconTestPage.java (3 instances)
```java
// Location: src/main/java/tech/derbent/base/users/view/CUserIconTestPage.java
Lines: 39, 41, 43

// Context: Test/demo page
// Priority: VERY LOW - Test pages can use raw components
// Recommendation: Leave as-is or update when touching file
```

## Div Usage Summary

Total instances: ~40 (not fully enumerated here)

Most Div usage is in `CViewToolbar.java`:
```java
Line 124: new Div(new Span("Active Project:"), projectComboBox)
```

Recommendation: Address Div usage when updating the files for Span replacements.

## Recommended Cleanup Strategy

### Phase 1 (NEXT): High-Visibility Components
- [ ] MainLayout.java (application branding)
- [ ] CViewToolbar.java (toolbar on every page)
- [ ] CHierarchicalSideMenu.java (navigation menu)

### Phase 2: Enhanced Components
- [ ] CDialogPictureSelector.java
- [ ] CComponentEntitySelection.java
- [ ] CDashboardStatCard.java

### Phase 3: Batch Updates
- [ ] All renderer components (CColorAwareComboBox, CColorPickerComboBox, etc.)
- [ ] All grid components (CComponentStoryPoint, CComponentId)
- [ ] All dialog components

### Phase 4: Evaluate CEntityLabel
- [ ] Determine if CEntityLabel should be deprecated
- [ ] If yes, migrate usages to CLabelEntity
- [ ] Update documentation

## Notes for Future Developers

### When to Replace vs. When to Leave

**ALWAYS Replace**:
- User-facing text display
- Component fields (e.g., `private Span userLabel`)
- Main application views and pages

**Consider Leaving**:
- ComboBox/Grid renderers (though replacing is better)
- Internal utility contexts
- Test pages

### Pattern for Replacement

```java
// Before
import com.vaadin.flow.component.html.Span;
private Span labelText;
labelText = new Span("text");

// After
import tech.derbent.api.ui.component.basic.CSpan;
private CSpan labelText;
labelText = new CSpan("text");
```

### Batch Replacement Command (USE WITH CAUTION)

```bash
# For a single file (review changes before committing):
sed -i 's/new Span(/new CSpan(/g' path/to/file.java
sed -i 's/import com.vaadin.flow.component.html.Span;/import tech.derbent.api.ui.component.basic.CSpan;/g' path/to/file.java
sed -i 's/\bSpan\b/CSpan/g' path/to/file.java  # Replace Span type declarations

# ALWAYS review git diff after batch replacement
# ALWAYS compile and test after batch replacement
```

## Related Documentation

- [Method Placement Guidelines](method-placement-guidelines.md)
- [Component and Utility Reference](component-utility-reference.md)
- [Coding Standards](coding-standards.md)

---

**Version**: 1.0  
**Last Updated**: 2026-01-03  
**Maintainer**: Derbent Development Team
