# Refactoring: "Responsible" → "AssignedTo" Migration Guide

## Overview
The user has removed the redundant "responsible" field from entities and standardized on using `assignedTo` which already exists in `CProjectItem`. This document outlines the comprehensive refactoring needed.

## Files Created
- ✅ `CAssignedToFilter.java` - New filter class replacing `CResponsibleUserFilter`

## Files That Need Updates

### 1. Filter Component Usages
**File:** `CComponentKanbanBoardFilterToolbar.java`
- Change import: `CResponsibleUserFilter` → `CAssignedToFilter`
- Change field: `responsibleUserFilter` → `assignedToFilter`
- Change instantiation: `new CResponsibleUserFilter()` → `new CAssignedToFilter()`
- Change getter method name: `getResponsibleUserFilter()` → `getAssignedToFilter()`

**File:** `CComponentKanbanBoard.java`
- Change import: `CResponsibleUserFilter` → `CAssignedToFilter`
- Update FILTER_KEY references: `CResponsibleUserFilter.FILTER_KEY` → `CAssignedToFilter.FILTER_KEY`
- Update enum references: `CResponsibleUserFilter.ResponsibleFilterMode` → `CAssignedToFilter.AssignedToFilterMode`
- Rename method: `matchesResponsibleUser()` → `matchesAssignedToUser()`
- Rename method: `matchesResponsibleFilter()` → `matchesAssignedToFilter()`
- Update variable: `responsibleMode` → `assignedToMode`

### 2. Gantt Component Updates
**File:** `CGanntItem.java`
- Rename method: `getResponsibleName()` → `getAssignedToName()`
- Update implementation to use `getAssignedTo().getName()`

**File:** `CGanntGrid.java`
- Update column: `CGanntItem::getResponsibleName` → `CGanntItem::getAssignedToName`
- Update column header: "Responsible" → "Assigned To"
- Update column ID: "responsible" → "assignedTo"

**File:** `CGanntTimelineBar.java`
- Update method call: `item.getResponsibleName()` → `item.getAssignedToName()`

### 3. Widget Component Updates
**File:** `CComponentWidgetSprintItem.java`
- Update comment: "responsible user" → "assigned to user"

**File:** `CComponentWidgetSprint.java`
- Update comment: "responsible user" → "assigned to user"

**File:** `CComponentWidgetMeeting.java`
- Update comment: "responsible user" → "assigned to user"

**File:** `CComponentWidgetEntityOfProject.java`
- Already uses `getAssignedTo()` - no changes needed ✅

### 4. Documentation Updates

**File:** `docs/architecture/coding-standards.md`
- Update filter example: `CResponsibleUserFilter` → `CAssignedToFilter`
- Update table: change filter key from "responsibleUser" → "assignedTo"

**File:** `docs/architecture/DATABASE_COMPOSITION_PATTERN.md`
- Update field name in examples: `responsible` → `assignedTo`
- Update column name: `responsible_id` → `assigned_to_id` (if showing SQL)

**File:** `docs/implementation/GANTT_DESIGN_PATTERN.md`
- Update text label reference: "responsible" → "assigned to"

**File:** `docs/development/universal-filter-toolbar-framework.md`
- Update filter key: "responsibleUser" → "assignedTo"
- Update class name: `CResponsibleUserFilter` → `CAssignedToFilter`

**File:** `docs/components/UNIVERSAL_ENTITY_TYPE_FILTER.md`
- Update example usage: `CResponsibleUserFilter` → `CAssignedToFilter`

### 5. Interface/Comment Updates
**File:** `IGanttDisplayable.java`
- Update JavaDoc: "responsible for" → "assigned to"

**File:** `IFilterComponent.java`
- Update example: "responsible user" → "assigned to"

**File:** `CAbstractFilterToolbar.java`
- Update comment: "responsible user" → "assigned to"

**File:** `CUniversalFilterToolbar.java`
- Update comment: "responsible user" → "assigned to"
- Update example: `CResponsibleUserFilter` → `CAssignedToFilter`

**File:** `CValueStorageHelper.java`
- Update example: "filter_responsible" → "filter_assignedTo"
- Update example: `ResponsibleFilterMode` → `AssignedToFilterMode`

### 6. Test Files
**File:** `CEntityDBMatchesFilterTest.java`
- Already uses `assignedTo` field - no changes needed ✅

### 7. Files to Delete
- ❌ `CResponsibleUserFilter.java` - Delete after migration complete

## Implementation Steps

1. ✅ Create `CAssignedToFilter.java`
2. Update all imports and usages in application code
3. Update method names (`matchesResponsibleUser` → `matchesAssignedToUser`, etc.)
4. Update all documentation files
5. Update UI labels and column headers
6. Test compilation
7. Test filter functionality
8. Delete old `CResponsibleUserFilter.java`

## Testing Checklist
- [ ] Kanban board filtering works correctly
- [ ] "Assigned To" filter shows correct label
- [ ] Filter persistence works (values saved/restored)
- [ ] Gantt chart displays "Assigned To" column
- [ ] Gantt timeline shows assigned user names
- [ ] All widgets display assigned user correctly
- [ ] Documentation is consistent

## Migration Impact
- **Filter persistence**: Old "responsibleUser" stored values will be ignored; users will see default "All items"
- **UI labels**: Users will see "Assigned To" instead of "Responsible"
- **Code readability**: Terminology now consistent with domain model (`assignedTo` field)

## Next Steps
1. Complete the file updates listed above
2. Run full compilation test
3. Run Playwright UI tests for Kanban board
4. Update any sample data initialization if needed
5. Update user documentation/release notes

