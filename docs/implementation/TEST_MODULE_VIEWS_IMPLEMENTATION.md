# Validation Module Views Implementation Plan

## Current Status

The validation entities have **partial implementation**:
- ✅ Domain entities exist (CValidationCase, CValidationSuite, CValidationSession, CValidationStep)
- ✅ Services exist (CValidationCaseService, etc.)
- ✅ Page services exist (CPageServiceValidationCase, etc.)
- ✅ Initializer services exist (CValidationCaseInitializerService, etc.)
- ✅ Grid definitions exist
- ✅ Detail section definitions exist
- ✅ Sample data generation exists
- ✅ **Single-page validation execution view exists** (`CComponentValidationExecution`)
- ⏳ **Additional dashboards and designer enhancements are pending**

## Understanding the View Pattern

### Standard View (Grid + Detail)
Default for most entities - shows a grid list and detail panel:
```java
CDetailSection detailSection = createBasicView(project);
CGridEntity grid = createGridEntity(project);
initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
    detailSection, grid, menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
```

### Single-Page View (Full-Screen Component)
For entities needing larger custom components (like Kanban Board):
```java
// Create a minimal detail section with custom component
CDetailSection singlePageSection = createExecutionView(project);
CGridEntity singlePageGrid = createGridEntity(project);

// Mark grid as "none" so it doesn't show
singlePageGrid.setAttributeNone(true);

// Register as separate page
initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
    singlePageSection, singlePageGrid, 
    menuTitle + ".Execute Validation", "Validation Execution", "Execute validation step-by-step", 
    true, menuOrder + ".1");
```

## Kanban Example Analysis

From `CKanbanLineInitializerService.java`:

### View 1: Standard Kanban Line Management
- **Purpose**: CRUD operations on kanban lines
- **Menu**: "Setup.Kanban Lines"
- **Components**: Grid + Detail form
- **Used for**: Managing kanban line definitions, columns, configuration

### View 2: Sprint Board (Single-Page)
- **Purpose**: Visual kanban board for sprint management
- **Menu**: "Setup.Kanban Lines Sprint Board"
- **Components**: Full-screen `CComponentKanbanBoard`
- **Used for**: Drag-drop sprint item management
- **Key difference**: `kanbanGrid.setAttributeNone(true)` - no grid shown

## Validation Module Views Needed

### Current Implementation ✅
All validation entities have:
1. Standard grid + detail views
2. Menu items under "Tests" parent
3. CRUD operations

### Enhancement Opportunities ⏳

#### 1. Validation Execution View (Single-Page) ✅
**Entity**: CValidationSession
**Purpose**: Execute validation step-by-step with real-time result recording
**Component**: `CComponentValidationExecution` (implemented)

Current capabilities (expand as needed):
- Step-by-step validation execution UI
- Pass/Fail/Skip/Block buttons per step
- Actual result text areas
- Progress indicators
- Attachment upload (screenshots, logs)
- Real-time statistics

**Implementation**:
```java
public static void initialize(final CProject project, ...) {
    // Standard view for validation session management
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
        detailSection, grid, menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
    
    // Single-page execution view
    final CDetailSection execSection = createExecutionView(project);
    final CGridEntity execGrid = createGridEntity(project);
    execGrid.setAttributeNone(true); // No grid
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
        execSection, execGrid,
        menuTitle + ".Execute Validation", "Validation Execution", "Execute validation step-by-step",
        true, menuOrder + ".1");
}
```

#### 2. Validation Dashboard View (Single-Page)
**Entity**: CValidationSuite or standalone CValidationMetrics
**Purpose**: Visual validation metrics and coverage dashboard
**Component needed**: `CComponentValidationDashboard`

Should provide:
- Validation coverage charts
- Pass/fail rate trends
- Validation execution history
- Coverage by feature matrix
- Failed validation analysis

#### 3. Validation Case Designer (Enhanced Detail)
**Entity**: CValidationCase
**Purpose**: Rich validation case editing with inline step management
**Component enhancement**: Better `CValidationStep` inline editor

Should provide:
- Drag-drop step reordering
- Rich text editing for steps
- Validation data templates
- Expected result formatting

## Implementation Priority

### Phase 1: Validation Execution View (HIGH) ✅ Document Created
This is the most critical missing piece - users need to execute validation!

**Tasks**:
- [ ] Create `CComponentValidationExecution` component
- [ ] Implement step-by-step execution logic
- [ ] Add result recording (pass/fail/skip/block)
- [ ] Integrate attachment uploads
- [ ] Update `CValidationSessionInitializerService` with execution view
- [ ] Add execution page service methods

### Phase 2: Enhanced Validation Step Management (MEDIUM)
Improve inline validation step editing in validation case detail view

**Tasks**:
- [ ] Add drag-drop reordering for validation steps
- [ ] Improve step editor UI
- [ ] Add step templates
- [ ] Validate validation step ordering

### Phase 3: Validation Dashboard (LOW)
Nice-to-have analytics dashboard

**Tasks**:
- [ ] Create `CComponentValidationDashboard` component
- [ ] Implement metrics calculations
- [ ] Add chart visualizations
- [ ] Create dashboard page

## Component Architecture

### CComponentValidationExecution Structure
```
CComponentValidationExecution (extends HasValue<CValidationSession>)
├── Header: Validation Session Info
│   ├── Validation suite name
│   ├── Execution timestamp
│   └── Environment/build info
├── Progress Bar
│   ├── Total validation cases
│   ├── Passed/Failed counts
│   └── Current validation case indicator
├── Validation Case Executor (Main Area)
│   ├── Validation Case Info
│   │   ├── Name, description
│   │   └── Preconditions
│   ├── Validation Steps List
│   │   ├── Step 1: Action + Expected Result
│   │   │   ├── Actual Result textbox
│   │   │   └── Pass/Fail/Skip/Block buttons
│   │   ├── Step 2: ...
│   │   └── Step N: ...
│   └── Navigation
│       ├── Previous Validation Case
│       ├── Next Validation Case
│       └── Complete Execution
└── Footer: Actions
    ├── Attach Evidence button
    ├── Add Comment button
    └── Save Progress button
```

### Page Service Methods
```java
public class CPageServiceValidationSession extends CPageServiceDynamicPage<CValidationSession> {
    private CComponentValidationExecution componentValidationExecution;
    
    public CComponentValidationExecution createValidationExecutionComponent() {
        if (componentValidationExecution == null) {
            componentValidationExecution = new CComponentValidationExecution();
            componentValidationExecution.registerWithPageService(this);
        }
        return componentValidationExecution;
    }
    
    public void on_validationExecution_stepCompleted(Component component, Object value) {
        // Handle step completion, save result
    }
    
    public void on_validationExecution_validationCaseCompleted(Component component, Object value) {
        // Handle validation case completion, move to next
    }
}
```

## AMetaData Component Methods

Validation entities already define component creation methods in metadata:

```java
// CValidationCase.java - validation steps
@AMetaData(
    displayName = "Validation Steps",
    dataProviderBean = "CValidationStepService",
    createComponentMethod = "createComponentListValidationSteps"
)
private Set<CValidationStep> validationSteps;

// CValidationSession.java - validation case results  
@AMetaData(
    displayName = "Validation Case Results",
    dataProviderBean = "CValidationCaseResultService",
    createComponentMethod = "createComponentListValidationCaseResults"
)
private Set<CValidationCaseResult> validationCaseResults;
```

These methods are implemented in the respective services.

## Next Steps

1. **Review existing implementations**: Verify `createComponentListValidationSteps` and `createComponentListValidationCaseResults`
2. **Extend execution component**: Add UX enhancements to `CComponentValidationExecution`
3. **Validate initializers**: Ensure the single-page execution view remains registered
4. **Test the workflow**: Execute a validation suite end-to-end
5. **Document patterns**: Update coding standards with validation execution patterns

## Related Files
- `src/main/java/tech/derbent/app/kanban/kanbanline/service/CPageServiceKanbanLine.java` - Reference for complex page service
- `src/main/java/tech/derbent/app/kanban/kanbanline/view/CComponentKanbanBoard.java` - Reference for large custom component
- `src/main/java/tech/derbent/app/validation/validationsession/service/CValidationSessionInitializerService.java` - Where to add execution view
- `docs/architecture/view-layer-patterns.md` - View patterns documentation
