# Critical Bugs Discovered by Playwright CRUD Tests

**Discovery Date**: 2026-01-14  
**Discovered By**: CRecentFeaturesCrudTest automated test suite  
**Test Run**: recent-features test scenario

## Summary

Playwright CRUD tests discovered 2 critical bugs preventing successful test execution. These are application bugs, not test bugs. The tests correctly identified issues that would affect users in production.

---

## Bug #1: Issue Initializer Duplicate Key Violation

### Severity: 🔴 CRITICAL

### Description
CIssueInitializerService attempts to create duplicate Issues with the same (project_id, summary) combination, violating the database unique constraint `cissue_ux_project_summary`.

### Error Details
```
ERROR: could not execute statement 
[ERROR: duplicate key value violates unique constraint "cissue_ux_project_summary"
Detail: Key (project_id, summary)=(22, Issue-1) already exists.] 
[insert into cissue (assigned_to_id, closed_at, company_id, created_at, created_by_id, 
description, due_date, entity_type_id, has_child_relations, issue_priority, issue_resolution, 
issue_severity, name, parent_activity_id, project_id, project_order, sprint_order, start_date, 
status_id, summary, updated_at, updated_by_id, issue_id) 
values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, default)]
```

### Stack Trace Location
```
tech.derbent.plm.issues.issue.service.CIssueInitializerService.initializeSample()
├─ tech.derbent.api.screens.service.CInitializerServiceBase.initializeProjectEntity()
│  └─ tech.derbent.plm.issues.issue.service.CIssueService.save()
│     └─ IIssueRepository.save()
│        └─ Database constraint violation
└─ Called from: CDataInitializer.loadSampleData()
```

### Impact
- ❌ Sample data initialization fails
- ❌ Application login with "DB Full" reset fails
- ❌ All Playwright tests fail (cannot complete login)
- ❌ Manual testing with fresh database fails
- ❌ Users cannot initialize sample data

### Affected Components
- **CIssueInitializerService** (`src/main/java/tech/derbent/app/issues/issue/service/CIssueInitializerService.java`)
- **CDataInitializer** (`src/main/java/tech/derbent/api/config/CDataInitializer.java`)
- **Login Page** - Database reset functionality

### Root Cause
The Issue initializer creates multiple issues with identical summary values for the same project, but the database has a unique constraint:
```sql
CONSTRAINT cissue_ux_project_summary UNIQUE (project_id, summary)
```

### Reproduction Steps
1. Login to application
2. Click "DB Full" button to reset database
3. Wait for sample data initialization
4. **ERROR OCCURS**: Duplicate key constraint violation

### Proposed Solutions

#### Option 1: Check Before Insert (Recommended)
```java
public static void initializeSample(final CProject project, final boolean minimal) {
    final String[][] data = { /* ... */ };
    
    for (String[] row : data) {
        String summary = row[0];
        
        // Check if issue already exists
        List<CIssue> existing = service.findByProjectAndSummary(project, summary);
        if (!existing.isEmpty()) {
            LOGGER.info("Issue already exists: {}, skipping", summary);
            continue;
        }
        
        // Create new issue
        CIssue issue = new CIssue();
        issue.setSummary(summary);
        // ... set other fields
        service.save(issue);
    }
}
```

#### Option 2: Clear Before Initialize
```java
public static void initializeSample(final CProject project, final boolean minimal) {
    // Clear existing issues for this project
    List<CIssue> existing = service.findByProject(project);
    for (CIssue issue : existing) {
        service.delete(issue);
    }
    
    // Now create fresh issues
    final String[][] data = { /* ... */ };
    // ... create issues
}
```

#### Option 3: Use Unique Summary Values
```java
public static void initializeSample(final CProject project, final boolean minimal) {
    final String[][] data = {
        { "Issue-1-" + System.currentTimeMillis(), "..." },
        { "Issue-2-" + UUID.randomUUID().toString().substring(0, 8), "..." },
        // ...
    };
    // ... create issues
}
```

### Recommended Fix
**Use Option 1** (Check Before Insert) because:
- ✅ Most robust - handles re-initialization gracefully
- ✅ No data loss - preserves existing issues
- ✅ Clean database - no timestamp/UUID clutter
- ✅ Follows existing patterns in other initializers

---

## Bug #2: CFormBuilder Cannot Handle Set<> Field Types

### Severity: 🟡 HIGH

### Description
CFormBuilder throws an exception when processing entity fields of type `Set<>` (e.g., `Set<CAttachment> attachments`). This causes entity detail views to crash.

### Error Details
```
ERROR: Component field [attachments], unsupported field type [Set] for field [Attachments]
    at CFormBuilder.logFail(CFormBuilder.java:308)
    at CFormBuilder.createComponentForField(CFormBuilder.java:396)
```

### Stack Trace Location
```
tech.derbent.api.annotations.CFormBuilder.createComponentForField()
├─ Detects field type is Set<CAttachment>
├─ No handler for Set<> type
└─ Throws IllegalArgumentException
    └─ Caught by: CPanelDetails.processLine()
        └─ Caught by: CPageBaseProjectAware.buildScreen()
            └─ Caught by: CDynamicPageBase.rebuildEntityDetailsById()
                └─ Results in exception dialog shown to user
```

### Impact
- ❌ Entity detail pages with Set<> fields crash
- ❌ Navigation to Activities, Issues, Meetings shows error dialog
- ❌ Users cannot view entity details
- ❌ Tests fail during navigation phase

### Affected Components
- **CFormBuilder** (`src/main/java/tech/derbent/api/annotations/CFormBuilder.java`)
- **All entities with Set<> fields**:
  - CActivity.attachments
  - CIssue.attachments
  - CMeeting.attachments
  - CRisk.attachments
  - CSprint.attachments
  - CProject.attachments
  - CUser.attachments
  - CDecision.attachments
  - COrder.attachments

### Root Cause
CFormBuilder.createComponentForField() only handles these field types:
- String (TextField)
- Integer, Long (IntegerField)
- Date, LocalDate, LocalDateTime (DatePicker, DateTimePicker)
- Boolean (Checkbox)
- Enum (ComboBox)
- Entity references with @ManyToOne (ComboBox)

It does NOT handle:
- ❌ Set<T>
- ❌ List<T>
- ❌ Collection<T>

### Reproduction Steps
1. Login to application
2. Navigate to Activities page
3. Click on any activity to view details
4. **ERROR OCCURS**: "Error handling entity selection" dialog appears

### Proposed Solutions

#### Option 1: Hide Set<> Fields from Form (Quick Fix)
```java
@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
@AMetaData(
    displayName = "Attachments",
    hidden = true  // ← Add this
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Pros**:
- ✅ Quick fix - just add annotation
- ✅ No CFormBuilder changes needed
- ✅ Attachments still work via separate component

**Cons**:
- ⚠️ Requires updating 9 entity classes
- ⚠️ Inconsistent - field exists but hidden

#### Option 2: Add Set<> Support to CFormBuilder (Proper Fix)
```java
// In CFormBuilder.createComponentForField()
if (field.getType().equals(Set.class) || field.getType().equals(List.class)) {
    // Collections should not be rendered as form fields
    // They are typically handled by separate grid components
    LOGGER.debug("Skipping collection field: {}", fieldName);
    return null;  // Return null to skip this field
}
```

**Pros**:
- ✅ Proper fix - handles all collection types
- ✅ No entity changes needed
- ✅ Clear intent - collections handled separately
- ✅ Follows separation of concerns

**Cons**:
- ⚠️ Requires testing CFormBuilder changes
- ⚠️ May affect other collection fields

#### Option 3: Custom Component for Set<> Fields
```java
// In CFormBuilder.createComponentForField()
if (field.getType().equals(Set.class)) {
    // Create a read-only badge showing count
    Span badge = new Span(String.format("%d items", getSetSize(entity, field)));
    badge.getElement().getThemeList().add("badge");
    return badge;
}
```

**Pros**:
- ✅ Provides visual feedback
- ✅ User knows field exists
- ✅ No crash on collection fields

**Cons**:
- ⚠️ Most complex solution
- ⚠️ Requires additional helper methods

### Recommended Fix
**Use Option 2** (Add Set<> Support) because:
- ✅ Most robust - handles all future collection fields
- ✅ No entity modifications needed
- ✅ Clear separation - collections handled by dedicated components
- ✅ Follows best practices

---

## Verification After Fixes

### Step 1: Apply Fixes
```bash
# Fix Bug #1: Issue initializer
vim src/main/java/tech/derbent/app/issues/issue/service/CIssueInitializerService.java

# Fix Bug #2: CFormBuilder
vim src/main/java/tech/derbent/api/annotations/CFormBuilder.java
```

### Step 2: Compile
```bash
source ./bin/setup-java-env.sh
mvn clean compile
```

### Step 3: Rerun Tests
```bash
# Run recent features tests
./run-playwright-tests.sh recent-features

# Expected: All tests should pass
# - testIssueCrudOperations: ✅ PASS
# - testTeamCrudOperations: ✅ PASS
# - testAttachmentOperationsOnActivity: ✅ PASS
# - testCommentsOnIssue: ✅ PASS
```

### Step 4: Manual Verification
```bash
# Start application
mvn spring-boot:run -Dspring.profiles.active=h2

# Test scenarios:
# 1. Login and click "DB Full" → Should complete without errors
# 2. Navigate to Issues → Should load without exception dialog
# 3. Click on an Issue → Should show details without crash
# 4. Navigate to Activities → Should work correctly
# 5. Select Activity and view details → Should not crash
```

---

## Test Suite That Discovered Bugs

**Test File**: `src/test/java/automated_tests/tech/derbent/ui/automation/CRecentFeaturesCrudTest.java`

**Test Methods**:
1. `testIssueCrudOperations()` - Discovered Bug #1
2. `testTeamCrudOperations()` - Discovered Bug #2
3. `testAttachmentOperationsOnActivity()` - Discovered Bug #2
4. `testCommentsOnIssue()` - Discovered Bug #2

**Test Execution**:
```bash
./run-playwright-tests.sh recent-features
```

## Related Documentation

- **Test Patterns**: `docs/testing/RECENT_FEATURES_CRUD_TEST_PATTERNS.md`
- **CRUD Validation**: `docs/testing/crud-operations-validation-report.md`
- **Playwright Usage**: `docs/testing/PLAYWRIGHT_USAGE.md`

---

**Priority**: 🔴 **URGENT** - Blocks all automated testing and sample data initialization

**Assigned To**: Development Team  
**Target Fix Date**: 2026-01-14  
**Follow-up**: Rerun test suite after fixes applied
