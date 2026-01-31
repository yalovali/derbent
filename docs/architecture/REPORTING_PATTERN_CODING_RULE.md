# Reporting Pattern - Mandatory Coding Rule

## Overview
All PageService classes **MUST** implement the `actionReport()` method to support CSV export functionality. This is a **MANDATORY** pattern for all page services in the Derbent codebase that display grid-based data.

## Why This Matters
- Provides consistent CSV export across all entity types
- Enables users to extract data from any grid view
- Supports custom field selection through dialog interface
- Maintains data integrity through proper field extraction
- Works seamlessly with the reporting infrastructure (CReportHelper, CDialogReportConfiguration)

## The Pattern Hierarchy

### 1. Base Class: CPageService
```java
// CPageService provides the default implementation
@SuppressWarnings("static-method")
public void actionReport() throws Exception {
    LOGGER.warn("Report action not implemented for this view");
    CNotificationService.showWarning("Report feature is not available for this view");
}
```

### 2. Concrete PageService: YOUR PAGE SERVICE
```java
/**
 * Handle report action - generates CSV report from grid data.
 * MANDATORY: All page services with grid views must override this method.
 * 
 * @throws Exception if report generation fails
 */
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered for YourEntity");
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<YourEntity> gridView = (CGridViewBaseDBEntity<YourEntity>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

## Mandatory Template for ALL PageServices

```java
/**
 * Handle report action - generates CSV report from grid data.
 * MANDATORY: All page services with grid views must override this method.
 * 
 * @throws Exception if report generation fails
 */
@Override
public void actionReport() throws Exception {
    // RULE 1: Log the action
    LOGGER.debug("Report action triggered for {EntityName}");
    
    // RULE 2: Check if view supports grid reporting
    if (getView() instanceof CGridViewBaseDBEntity) {
        // RULE 3: Cast to grid view with proper generic type
        final CGridViewBaseDBEntity<{EntityClass}> gridView = 
            (CGridViewBaseDBEntity<{EntityClass}>) getView();
        
        // RULE 4: Delegate to grid's report generation method
        gridView.generateGridReport();
    } else {
        // RULE 5: Fallback to parent implementation
        super.actionReport();
    }
}
```

## Implementation Rules

### ✅ ALWAYS Do These:
1. **Override actionReport()** in your PageService class
2. **Log the action** using LOGGER.debug() with entity name
3. **Check view type** using instanceof CGridViewBaseDBEntity
4. **Use correct generic type** when casting to CGridViewBaseDBEntity<EntityClass>
5. **Call gridView.generateGridReport()** to trigger the export workflow
6. **Add import** for CGridViewBaseDBEntity if not present
7. **Fallback to super.actionReport()** if view doesn't support grid reporting

### ❌ NEVER Do These:
- Don't skip the instanceof check (causes ClassCastException)
- Don't use raw types (loses type safety)
- Don't implement custom CSV generation (use framework)
- Don't catch exceptions silently (let them propagate)
- Don't forget to import CGridViewBaseDBEntity

## Infrastructure Components

### 1. CCrudToolbar - Report Button
```java
// Report button automatically added to all CRUD toolbars
reportButton = CButton.createTertiary("Report", VaadinIcon.DOWNLOAD.create(), 
    event -> on_actionReport());
reportButton.setId("cbutton-report");  // Stable ID for testing
```

### 2. CGridViewBaseDBEntity - Base Grid Method
```java
/** Generates a CSV report from the grid data. */
public void generateGridReport() throws Exception {
    final List<EntityClass> items = getGridItemsForReport();
    CReportHelper.generateReport(items, entityClass);
}

/** Provides grid items for report generation. */
protected List<EntityClass> getGridItemsForReport() {
    if (masterViewSection instanceof CMasterViewSectionGrid) {
        final CMasterViewSectionGrid<EntityClass> gridSection = 
            (CMasterViewSectionGrid<EntityClass>) masterViewSection;
        return gridSection.getAllItems();
    }
    return List.of();
}
```

### 3. CReportHelper - Report Generation Workflow
```java
/** Opens field selection dialog and generates CSV report. */
public static <T extends CEntityDB<T>> void generateReport(
        final List<T> entities, 
        final Class<T> entityClass) throws Exception {
    
    // Discover all available fields via reflection
    final List<CReportFieldDescriptor> allFields = 
        CReportFieldDescriptor.discoverFields(entityClass);
    
    // Open field selection dialog
    final CDialogReportConfiguration dialog = 
        new CDialogReportConfiguration(allFields, selectedFields -> {
            try {
                generateAndDownloadCSV(entities, selectedFields, entityClass);
            } catch (final Exception e) {
                CNotificationService.showException("Failed to generate report", e);
            }
        });
    dialog.open();
}
```

### 4. CDialogReportConfiguration - Field Selection Dialog
```java
/** Dialog for selecting fields to include in CSV export.
 * Features:
 * - Grouped field selection with checkboxes
 * - Select All / Deselect All per group
 * - Two-column layout for 6+ fields
 * - Minimum one field validation
 * - Max-width 800px for readability
 */
public class CDialogReportConfiguration extends CDialog {
    // Dialog implementation with proper field grouping
}
```

### 5. CReportFieldDescriptor - Field Discovery
```java
/** Describes a field that can be exported in a CSV report.
 * Automatically discovers fields via reflection and @AMetaData annotations.
 */
public class CReportFieldDescriptor {
    /** Discover all exportable fields from an entity class. */
    public static List<CReportFieldDescriptor> discoverFields(Class<?> entityClass) {
        // Reflection-based field discovery
    }
}
```

### 6. CCSVExporter - CSV Generation
```java
/** Exports entities to CSV format with proper escaping and formatting. */
public class CCSVExporter {
    public static StreamResource exportToCSV(
            List<?> entities, 
            List<CReportFieldDescriptor> fields, 
            String baseFileName) {
        // CSV generation with proper escaping
    }
}
```

## Example Implementations

### Example 1: Simple Entity (CActivity)
```java
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered for CActivity");
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CActivity> gridView = 
            (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

### Example 2: Type Entity (CActivityType)
```java
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered for CActivityType");
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CActivityType> gridView = 
            (CGridViewBaseDBEntity<CActivityType>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

### Example 3: Storage Entity (CStorage)
```java
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered for CStorage");
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CStorage> gridView = 
            (CGridViewBaseDBEntity<CStorage>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

### Example 4: Company Entity (CUser)
```java
@Override
public void actionReport() throws Exception {
    LOGGER.debug("Report action triggered for CUser");
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CUser> gridView = 
            (CGridViewBaseDBEntity<CUser>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

## User Workflow

### 1. User Clicks Report Button
- User clicks "Report" button in CCrudToolbar
- Button triggers `pageService.actionReport()`

### 2. Field Selection Dialog Opens
- CDialogReportConfiguration displays all available fields
- Fields are grouped by category (Base, Status, Assigned To, etc.)
- User selects which fields to export
- User can Select All / Deselect All per group

### 3. CSV Generation
- Selected fields are passed to CCSVExporter
- Data is extracted from grid items
- CSV file is generated with proper formatting
- File is automatically downloaded

### 4. Success Notification
- User sees "Exporting N records to CSV" notification
- Browser triggers file download
- CSV file opens in user's spreadsheet application

## Field Discovery and Grouping

### Automatic Field Discovery
Fields are discovered via reflection using @AMetaData annotations:

```java
@Column(nullable = false, length = 255)
@Size(max = 255)
@NotBlank(message = "Name is required")
@AMetaData(
    displayName = "Activity Name",    // Used as CSV column header
    required = true,
    readOnly = false,
    description = "Activity name",
    hidden = false,                    // Hidden fields not exported
    order = 10,
    maxLength = 255
)
private String name;
```

### Field Grouping
Fields are automatically grouped by their relationships:
- **Base**: Core fields (id, name, description)
- **Status**: Status and workflow fields
- **Assigned To**: User assignments
- **Dates**: Created, modified, due dates
- **Custom**: Entity-specific fields
- **Relations**: Foreign key relationships

## Testing

### Automated Testing
The CReportComponentTester validates:
- Report button presence and enabled state
- Field selection dialog functionality
- Checkbox selection/deselection
- Select All / Deselect All buttons
- CSV generation trigger

```bash
# Run adaptive tests (includes report testing)
mvn test -Dtest=CPageTestComprehensive
```

### Manual Testing Checklist
- [ ] Report button visible in toolbar
- [ ] Report button enabled when grid has data
- [ ] Dialog opens with field selection
- [ ] Fields grouped correctly
- [ ] Select All / Deselect All works
- [ ] At least one field must be selected
- [ ] CSV generates with correct data
- [ ] CSV downloads automatically
- [ ] Success notification displays

## Checklist for New PageServices

When creating a new PageService, you **MUST**:

- [ ] Override `actionReport()` method
- [ ] Add LOGGER.debug() with entity name
- [ ] Check `instanceof CGridViewBaseDBEntity`
- [ ] Cast with correct generic type `CGridViewBaseDBEntity<YourEntity>`
- [ ] Call `gridView.generateGridReport()`
- [ ] Add fallback to `super.actionReport()`
- [ ] Import `tech.derbent.api.grid.view.CGridViewBaseDBEntity`
- [ ] Test report button appears in UI
- [ ] Test field selection dialog opens
- [ ] Test CSV exports correctly

## Common Mistakes to Avoid

### ❌ Mistake 1: Missing instanceof Check
```java
// WRONG - will fail if view is not CGridViewBaseDBEntity!
@Override
public void actionReport() throws Exception {
    final CGridViewBaseDBEntity<CActivity> gridView = 
        (CGridViewBaseDBEntity<CActivity>) getView();  // ❌ ClassCastException!
    gridView.generateGridReport();
}
```

```java
// CORRECT
@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {  // ✅ Type-safe
        final CGridViewBaseDBEntity<CActivity> gridView = 
            (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

### ❌ Mistake 2: Using Raw Types
```java
// WRONG - loses type safety!
@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity gridView =  // ❌ Raw type!
            (CGridViewBaseDBEntity) getView();
        gridView.generateGridReport();
    }
}
```

```java
// CORRECT
@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CActivity> gridView =  // ✅ Generic type
            (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    }
}
```

### ❌ Mistake 3: Catching Exceptions Silently
```java
// WRONG - hides errors from user!
@Override
public void actionReport() throws Exception {
    try {
        if (getView() instanceof CGridViewBaseDBEntity) {
            final CGridViewBaseDBEntity<CActivity> gridView = 
                (CGridViewBaseDBEntity<CActivity>) getView();
            gridView.generateGridReport();
        }
    } catch (Exception e) {
        // ❌ Silent failure!
    }
}
```

```java
// CORRECT - let exceptions propagate to CNotificationService
@Override
public void actionReport() throws Exception {
    // No try-catch needed - CCrudToolbar handles exceptions
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CActivity> gridView = 
            (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();
    }
}
```

### ❌ Mistake 4: Missing Import
```java
// WRONG - CGridViewBaseDBEntity not imported!
@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {  // ❌ Compilation error!
        // ...
    }
}
```

```java
// CORRECT - import added
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;  // ✅

@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {  // ✅ Works
        // ...
    }
}
```

### ❌ Mistake 5: No Fallback to Super
```java
// WRONG - no fallback when view doesn't support reporting!
@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CActivity> gridView = 
            (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    }
    // ❌ No else clause - silent failure for non-grid views
}
```

```java
// CORRECT - fallback to parent implementation
@Override
public void actionReport() throws Exception {
    if (getView() instanceof CGridViewBaseDBEntity) {
        final CGridViewBaseDBEntity<CActivity> gridView = 
            (CGridViewBaseDBEntity<CActivity>) getView();
        gridView.generateGridReport();
    } else {
        super.actionReport();  // ✅ Shows appropriate warning
    }
}
```

## Summary

The Reporting pattern is **MANDATORY** for all PageServices with grid views. It:

1. ✅ Provides consistent CSV export across all entities
2. ✅ Uses dialog-based field selection for user control
3. ✅ Leverages reflection for automatic field discovery
4. ✅ Groups fields by category for better UX
5. ✅ Handles proper CSV formatting and escaping
6. ✅ Integrates with automated testing framework
7. ✅ Follows established Derbent coding patterns

**Remember**:
- Always override `actionReport()` in PageServices with grids
- Always check instanceof before casting
- Always use correct generic types
- Always call super.actionReport() in else clause
- Always import CGridViewBaseDBEntity
- Test with CPageTestComprehensive framework

This is not optional - it's a **core architectural pattern** that must be followed for all PageServices in the Derbent codebase that display grid-based data.

## Coverage Status

### ✅ Implemented (82/85 PageServices)
- All API/Framework services (14)
- All Base services (4)
- All PLM business entities (64)

### ⏭️ Intentionally Skipped (3/85 PageServices)
- CPageServiceUtility (utility class, no entity)
- CPageServiceBabDevice (placeholder)
- CPageServiceBabNode (placeholder)

**Coverage**: 96% of applicable PageServices

## Related Documentation
- `docs/architecture/COPY_TO_PATTERN_CODING_RULE.md` - Copy pattern documentation
- `docs/REPORTING_PATTERN_IMPLEMENTATION.md` - Implementation summary
- `AGENTS.md` - Master coding guidelines
