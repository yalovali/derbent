# CSV Report Export Feature

## Overview

The CSV Report Export feature allows users to export grid data to CSV files. Users can select which fields to include in the report through a field selection dialog.

## Components

### 1. CReportService
**Location**: `tech.derbent.api.reports.service.CReportService`

Core service for CSV generation:
- Generates CSV content from entity lists
- Handles field value extraction (simple and complex/nested fields)
- Formats values based on type (dates, decimals, collections, entity references)
- Triggers CSV file download in browser

### 2. CDialogReportFieldSelection
**Location**: `tech.derbent.api.reports.dialog.CDialogReportFieldSelection`

Field selection dialog:
- Groups fields into "Base Fields" and "Related Fields"
- Uses 2-column layout for 6+ fields
- Includes Select All/Deselect All toggle
- Follows UI coding standards (max-width 600px, custom gaps)

### 3. CReportHelper
**Location**: `tech.derbent.api.reports.service.CReportHelper`

Utility class with static helper methods:
- Opens field selection dialog
- Coordinates CSV generation and download
- Generates timestamped filenames

### 4. CCrudToolbar Enhancement
**Location**: `tech.derbent.api.ui.component.enhanced.CCrudToolbar`

Added "Report" button:
- Tertiary style button with download icon
- Positioned after "Copy To" button
- Calls `actionReport()` on page service

### 5. CPageService Integration
**Location**: `tech.derbent.api.services.pageservice.CPageService`

Added `actionReport()` method:
- Default implementation shows warning
- Can be overridden in concrete page services

### 6. CGridViewBaseDBEntity Support
**Location**: `tech.derbent.api.grid.view.CGridViewBaseDBEntity`

Added grid reporting support:
- `generateGridReport()` method - triggers report generation
- `getGridItemsForReport()` method - retrieves all grid items

### 7. CMasterViewSectionGrid Enhancement
**Location**: `tech.derbent.api.grid.view.CMasterViewSectionGrid`

Added `getAllItems()` method:
- Retrieves all items from grid's data provider
- Used for report generation

## Implementation for Page Services

To add report functionality to a page service that uses a grid view:

```java
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered");
    
    // Check if view supports grid reporting
    if (getView() instanceof CGridViewBaseDBEntity) {
        @SuppressWarnings("unchecked")
        final CGridViewBaseDBEntity<YourEntity> gridView = 
            (CGridViewBaseDBEntity<YourEntity>) getView();
        gridView.generateGridReport();
    } else {
        // Fallback to parent implementation (shows warning)
        super.actionReport();
    }
}
```

## Example Implementation

See `CPageServiceActivity` for a complete example implementation.

## User Workflow

1. User clicks "Report" button in toolbar
2. Field selection dialog appears showing:
   - Base Fields (name, description, dates, etc.)
   - Related Fields (status, workflow, etc.)
3. User selects/deselects fields
4. User clicks "Generate Report"
5. CSV file downloads automatically

## CSV Format

- Header row with field display names
- Data rows with field values
- Proper CSV escaping (quotes for commas/newlines)
- Date format: `yyyy-MM-dd`
- DateTime format: `yyyy-MM-dd HH:mm:ss`
- Collections: Shows count (e.g., "3 item(s)")
- Entity references: Shows name if available, or toString()

## Field Introspection

The system uses `CEntityFieldService` to:
- Discover all entity fields via reflection
- Read `@AMetaData` annotations
- Separate simple fields from complex/relation fields
- Skip hidden fields

## Future Enhancements

Potential improvements:
- Custom field formatters
- Export to other formats (Excel, JSON)
- Scheduled/automated reports
- Report templates
- Filter/sort before export
- Large dataset pagination
