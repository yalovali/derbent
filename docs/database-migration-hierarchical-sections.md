# Database Migration Guide: Hierarchical Section/Tab Architecture

## Overview
This migration adds support for hierarchical section/tab nesting up to 3 levels deep in the detail section builder.

## Database Changes

### CDetailLines Table

**Field Change:**
- **Removed**: `is_required` BOOLEAN (Note: This was incorrectly named for the `sectionAsTab` field)
- **Added**: `container_type` VARCHAR(20)

### Migration SQL

#### For PostgreSQL:
```sql
-- Add new container_type column
ALTER TABLE cdetaillines 
ADD COLUMN container_type VARCHAR(20) NULL;

-- Migrate existing data (if any sectionAsTab data exists)
-- Note: The old column name in the schema was confusing - it was named 'is_required' but used for sectionAsTab
UPDATE cdetaillines 
SET container_type = 'TAB' 
WHERE is_required = true AND relationFieldName = 'Section';

UPDATE cdetaillines 
SET container_type = 'SECTION' 
WHERE is_required = false AND relationFieldName = 'Section';

-- Drop the old boolean column (if it exists with correct usage)
-- ALTER TABLE cdetaillines DROP COLUMN is_required;
```

#### For H2 (Development):
```sql
-- Add new container_type column
ALTER TABLE cdetaillines 
ADD COLUMN container_type VARCHAR(20) NULL;

-- Migrate existing data if needed
UPDATE cdetaillines 
SET container_type = 'SECTION' 
WHERE relationFieldName = 'Section' AND container_type IS NULL;
```

## New Constants

### CEntityFieldService
- `CONTAINER_TYPE_SECTION = "SECTION"` - Defines an accordion section
- `CONTAINER_TYPE_TAB = "TAB"` - Defines a tab
- `CONTAINER_TYPE_SECTION_END = "SECTION_END"` - Marks the end of a container

## Usage Examples

### Example 1: Simple Section
```java
CDetailSection screen = new CDetailSection("Activity View", project);
screen.setEntityType("CActivity");

// Add a section
screen.addScreenLine(CDetailLinesService.createSection("Basic Info", CEntityFieldService.CONTAINER_TYPE_SECTION));
screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "name"));
screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "description"));
screen.addScreenLine(CDetailLinesService.createSectionEnd());
```

### Example 2: Section with Tab Inside
```java
CDetailSection screen = new CDetailSection("Activity View", project);
screen.setEntityType("CActivity");

// Main section (accordion)
screen.addScreenLine(CDetailLinesService.createSection("Details", CEntityFieldService.CONTAINER_TYPE_SECTION));
screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "name"));

// Tab inside the section
screen.addScreenLine(CDetailLinesService.createSection("Advanced", CEntityFieldService.CONTAINER_TYPE_TAB));
screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "priority"));
screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "status"));
screen.addScreenLine(CDetailLinesService.createSectionEnd()); // Close tab

screen.addScreenLine(CDetailLinesService.createSectionEnd()); // Close section
```

### Example 3: Complex 3-Level Hierarchy
```java
// From problem statement:
// Section (accordion)
//   Tab (inside section)
//     field1, field2
//   Section (another accordion inside main section, below tab)
//     field3, field4

CDetailSection screen = new CDetailSection("Activity View", project);
screen.setEntityType("CActivity");

// Level 1: Main section
screen.addScreenLine(CDetailLinesService.createSection("Main Section", CEntityFieldService.CONTAINER_TYPE_SECTION));

  // Level 2: Tab inside section
  screen.addScreenLine(CDetailLinesService.createSection("Tab Section", CEntityFieldService.CONTAINER_TYPE_TAB));
  screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "name"));
  screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "description"));
  screen.addScreenLine(CDetailLinesService.createSectionEnd()); // Close tab

  // Level 2: Another section below the tab (sibling)
  screen.addScreenLine(CDetailLinesService.createSection("Sub Section", CEntityFieldService.CONTAINER_TYPE_SECTION));
  screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "priority"));
  screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "status"));
  screen.addScreenLine(CDetailLinesService.createSectionEnd()); // Close sub-section

screen.addScreenLine(CDetailLinesService.createSectionEnd()); // Close main section
```

### Example 4: Multiple Tabs at Same Level
```java
CDetailSection screen = new CDetailSection("Activity View", project);
screen.setEntityType("CActivity");

// Section containing multiple tabs
screen.addScreenLine(CDetailLinesService.createSection("Details", CEntityFieldService.CONTAINER_TYPE_SECTION));

  // Tab 1
  screen.addScreenLine(CDetailLinesService.createSection("Basic", CEntityFieldService.CONTAINER_TYPE_TAB));
  screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "name"));
  screen.addScreenLine(CDetailLinesService.createSectionEnd());

  // Tab 2
  screen.addScreenLine(CDetailLinesService.createSection("Advanced", CEntityFieldService.CONTAINER_TYPE_TAB));
  screen.addScreenLine(CDetailLinesService.createLineFromDefaults(CActivity.class, "priority"));
  screen.addScreenLine(CDetailLinesService.createSectionEnd());

screen.addScreenLine(CDetailLinesService.createSectionEnd()); // Close main section
```

## Important Notes

1. **Maximum Nesting**: The system supports up to 3 levels of nesting. Attempting to nest deeper will log an error and skip the nested container.

2. **SECTION_END is Required**: Always close containers with `CDetailLinesService.createSectionEnd()` to properly manage the container stack.

3. **Tab Rendering**: 
   - Tabs inside sections create a TabSheet component within the section's base layout
   - Sections inside tabs are added to the tab's base layout as accordions

4. **Backward Compatibility**: The old user preference `attributeDisplaySectionsAsTabs` is still supported through the backward-compatible constructor in `CPanelDetails`.

5. **Container Type Defaults**: If `containerType` is null or empty, it defaults to "SECTION".

## Testing

Run the comprehensive test suite:
```bash
mvn test -Dtest=CDetailsBuilderHierarchicalTest
```

All 7 test scenarios should pass:
- Simple single section
- Section containing tab
- Tab containing section  
- Three-level hierarchy
- Max nesting level enforcement
- Multiple sections at same level
- Problem statement example

## Rollback

To rollback this change:
1. Revert the code changes in the affected classes
2. Run the following SQL:
```sql
ALTER TABLE cdetaillines DROP COLUMN container_type;
-- Optionally restore the old column if needed
```
