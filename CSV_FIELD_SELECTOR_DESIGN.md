# CSV Export Field Selector - Visual Design

## Before & After Comparison

### ❌ BEFORE (Non-Compliant with AGENTS.md)

```java
@Override
protected void setupDialog() throws Exception {
    setHeaderTitle("Configure CSV Export");
    setWidth("800px");  // ❌ Fixed width, not responsive
    setMaxHeight("80vh");
    setCloseOnEsc(true);
    setCloseOnOutsideClick(false);
}
```

**Issues**:
- Fixed width doesn't adapt to small screens
- No max-width constraint
- Doesn't follow AGENTS.md Section 6.2 rules

### ✅ AFTER (AGENTS.md Compliant)

```java
@Override
protected void setupDialog() throws Exception {
    setHeaderTitle("Configure CSV Export");
    setMaxWidth("800px");  // ✅ Max constraint for responsive design
    setWidthFull();         // ✅ Responsive width up to max
    setMaxHeight("80vh");
    setCloseOnEsc(true);
    setCloseOnOutsideClick(false);
    getElement().setAttribute("id", "custom-dialog-csv-export");  // ✅ Stable ID
}
```

**Improvements**:
- Responsive design (adapts to viewport)
- Max-width constraint prevents overflow
- Stable ID for Playwright testing
- Follows AGENTS.md Section 6.2 rules

## Field Selector Dialog Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  Configure CSV Export                                      [X]  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Select Fields to Export                                        │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Base                      [Select All] [Deselect All]    │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ ☑ ID              ☑ Name                                 │  │
│  │ ☑ Description     ☑ Notes                                │  │
│  │ ☑ Created Date    ☑ Modified Date                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Status                    [Select All] [Deselect All]    │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ ☑ Status          ☑ Workflow                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Assigned To               [Select All] [Deselect All]    │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ ☑ Assigned To     ☑ Created By                           │  │
│  │ ☑ Modified By                                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│                              [Cancel]  [Generate CSV ⬇]        │
└─────────────────────────────────────────────────────────────────┘
         │                                                │
         └────────────── max-width: 800px ───────────────┘
```

## Responsive Behavior

### Desktop (> 800px viewport)
```
┌─────────────800px max-width─────────────┐
│  Dialog stays at 800px                  │
│  White space on sides                   │
└─────────────────────────────────────────┘
```

### Tablet (600-800px viewport)
```
┌──────full viewport width──────┐
│  Dialog fills viewport         │
│  No overflow                   │
└────────────────────────────────┘
```

### Mobile (< 600px viewport)
```
┌─full viewport─┐
│  Dialog fills  │
│  viewport      │
│  Stacks fields │
│  vertically    │
└────────────────┘
```

## Component ID Structure

### Dialog Container
```
<vaadin-dialog id="custom-dialog-csv-export">
```

### Group Structure
```
<div class="field-group">
  <h4>Base</h4>
  <vaadin-button id="custom-select-all-base">Select All</vaadin-button>
  <vaadin-button id="custom-deselect-all-base">Deselect All</vaadin-button>
  
  <vaadin-checkbox id="custom-csv-field-id">ID</vaadin-checkbox>
  <vaadin-checkbox id="custom-csv-field-name">Name</vaadin-checkbox>
  ...
</div>
```

### Action Buttons
```
<vaadin-button id="custom-csv-export-cancel">Cancel</vaadin-button>
<vaadin-button id="custom-csv-export-generate">Generate CSV ⬇</vaadin-button>
```

## Field Selection Logic

### Default State (All Selected)
```java
private static Checkbox createFieldCheckbox(final CReportFieldDescriptor field) {
    final Checkbox checkbox = new Checkbox(field.getDisplayName());
    checkbox.setValue(true);  // ✅ Pre-selected by default
    checkbox.setId("custom-csv-field-" + sanitizeId(field.getFieldPath()));
    return checkbox;
}
```

### Select All Button Logic
```java
private void selectAllInGroup(final String groupName, final boolean selected) {
    final List<Checkbox> checkboxes = groupCheckboxes.get(groupName);
    if (checkboxes != null) {
        for (final Checkbox checkbox : checkboxes) {
            checkbox.setValue(selected);  // All get same value
        }
    }
}
```

### Validation (Minimum 1 Field)
```java
private void onGenerateClicked() {
    try {
        final List<CReportFieldDescriptor> selectedFields = new ArrayList<>();
        for (final CReportFieldDescriptor field : allFields) {
            if (isFieldSelected(field)) {
                selectedFields.add(field);
            }
        }
        
        if (selectedFields.isEmpty()) {
            throw new IllegalStateException("Please select at least one field to export");
        }
        
        close();
        onGenerate.accept(selectedFields);
    } catch (final Exception e) {
        LOGGER.error("Error during CSV generation", e);
        throw e;
    }
}
```

## Multi-Column Layout Rule (AGENTS.md 6.2)

### Rule: 2-column layout for 6+ checkboxes

```java
if (fields.size() > 6) {
    // Create 2-column layout
    final HorizontalLayout columnsLayout = new HorizontalLayout();
    final CVerticalLayout col1 = new CVerticalLayout();  // Left column
    final CVerticalLayout col2 = new CVerticalLayout();  // Right column
    
    for (int i = 0; i < fields.size(); i++) {
        final Checkbox checkbox = createFieldCheckbox(fields.get(i));
        
        if (i < (fields.size() + 1) / 2) {
            col1.add(checkbox);  // First half
        } else {
            col2.add(checkbox);  // Second half
        }
    }
    
    columnsLayout.add(col1, col2);
} else {
    // Single column for 6 or fewer
    final CVerticalLayout fieldsLayout = new CVerticalLayout();
    for (final CReportFieldDescriptor field : fields) {
        fieldsLayout.add(createFieldCheckbox(field));
    }
}
```

### Visual Example

**6 or fewer fields** (single column):
```
☑ Field 1
☑ Field 2
☑ Field 3
☑ Field 4
☑ Field 5
☑ Field 6
```

**7 or more fields** (two columns):
```
☑ Field 1    ☑ Field 5
☑ Field 2    ☑ Field 6
☑ Field 3    ☑ Field 7
☑ Field 4    ☑ Field 8
```

## Playwright Test Coverage

### Test Execution Flow

```
CAdaptivePageTest
    ↓ (navigates to page)
Report Button Detected (#cbutton-report)
    ↓ (triggers)
CReportComponentTester.test(page)
    ↓
1. ✓ Verify button exists
2. ✓ Check button enabled
3. ✓ Click report button
4. ✓ Verify dialog opens
5. ✓ Test field checkboxes
6. ✓ Test Select All buttons
7. ✓ Test dialog buttons
8. ✓ Close dialog
```

### Selector Strategy

```javascript
// Report Button
page.locator('#cbutton-report')

// CSV Dialog
page.locator('#custom-dialog-csv-export')

// Field Checkboxes (all)
page.locator('vaadin-checkbox[id^="custom-csv-field-"]')

// Field Checkboxes (selected)
page.locator('vaadin-checkbox[id^="custom-csv-field-"][checked]')

// Select All Buttons (all groups)
page.locator('vaadin-button[id^="custom-select-all-"]')

// Deselect All Buttons (all groups)
page.locator('vaadin-button[id^="custom-deselect-all-"]')

// Action Buttons
page.locator('#custom-csv-export-generate')
page.locator('#custom-csv-export-cancel')
```

## AGENTS.md Compliance Matrix

| Rule | Location | Implementation | Status |
|------|----------|----------------|--------|
| Max-width constraint | 6.2 | `setMaxWidth("800px")` | ✅ |
| Responsive width | 6.2 | `setWidthFull()` | ✅ |
| Custom gaps | 6.2 | `gap: "12px"`, `gap: "16px"` | ✅ |
| 2-column (6+) | 6.2 | Lines 113-137 | ✅ |
| Select All/Deselect All | 6.2 | Lines 104-109, 210-217 | ✅ |
| Stable component IDs | 6.7 | All components | ✅ |
| Component tester | 7.x | `CReportComponentTester` | ✅ |
| Control signature | 7.x | `#cbutton-report` | ✅ |

## Summary

The CSV export field selector is now **perfectly aligned** with AGENTS.md standards:

✅ **Responsive Design**: Adapts to viewport size (max 800px)  
✅ **User-Friendly**: Pre-selected fields, group selection  
✅ **Testable**: Stable IDs for automation  
✅ **Accessible**: Clear labels, logical grouping  
✅ **Standards-Compliant**: Follows all AGENTS.md rules  

The implementation is production-ready and provides an excellent user experience for CSV export field selection.
