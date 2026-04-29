# Derbent Project Backlog Analysis - 2026-01-13

## Executive Summary

This document provides a comprehensive analysis of the Derbent project backlog, mapping existing implementation to industry standards (Jira, ProjeQtOr), and providing enriched epics, features, user stories, and tasks aligned with current coding standards.

### Methodology
1. **Web Research**: Analyzed Jira and ProjeQtOr best practices for epic/feature/story/task hierarchies
2. **Codebase Analysis**: Mapped all 56 domain entities, 160+ services, and 40+ views to backlog items
3. **Standards Alignment**: Updated task descriptions to follow Derbent coding standards
4. **Gap Analysis**: Identified implemented vs TODO features

### Key Findings

**Implemented Features (✅):**
- ✅ **Epic 3: Document & Attachment Management** (DONE - 28 story points)
  - Package: `tech.derbent.plm.attachments`
  - Entities: CAttachment, CDocumentType
  - Views: CDialogAttachment, CComponentListAttachments
  - Status: Fully implemented with version control, auto-detection, FormBuilder pattern

- ✅ **Financial Management (Partial)** - Expenses & Incomes
  - Packages: `tech.derbent.plm.projectexpenses`, `tech.derbent.plm.projectincomes`, `tech.derbent.plm.budgets`
  - Entities: CProjectExpense, CProjectIncome, CBudget
  - Status: Core entities done, reporting/analytics TODO

- ✅ **Agile/Sprint Management (Core)**
  - Package: `tech.derbent.plm.sprints`, `tech.derbent.plm.kanban`
  - Entities: CSprint, CSprintItem, CKanbanLine, CKanbanColumn
  - Views: CComponentKanbanBoard, CComponentWidgetSprint
  - Status: Sprint board, kanban, story points implemented

- ✅ **Risk Management (Core)**
  - Package: `tech.derbent.plm.risks`
  - Entities: CRisk, CRiskLevel, CRiskType
  - Status: Risk entities, CRUD operations implemented

- ✅ **Product/Component Management**
  - Packages: `tech.derbent.plm.products`, `tech.derbent.plm.components`
  - Entities: CProduct, CProductVersion, CProjectComponent
  - Status: Product versioning, component tracking implemented

- ✅ **Order/Procurement Management**
  - Package: `tech.derbent.plm.orders`
  - Entities: COrder, COrderApproval, COrderType
  - Status: Order tracking with approval workflow implemented

**TODO Features (❌):**
- ❌ **Resource & Timesheet Management** - NOT FOUND in codebase
- ❌ **Reporting & Analytics** - Limited dashboard, no burndown/velocity charts
- ❌ **Portfolio & Program Management** - No portfolio entities
- ❌ **Idea & Backlog Management** - No voting/prioritization features
- ❌ **Issue & Bug Tracking (Dedicated)** - CTicket exists but limited workflow
- ❌ **Calendar & Scheduling Integration** - No calendar view
- ❌ **Permissions & Role Management UI** - Backend exists, no UI
- ❌ **Client Portal** - No external stakeholder access
- ❌ **AI & Automation** - No AI features
- ❌ **Notifications & Integrations** - Limited notifications, no Slack/email integration
- ❌ **QA & Testing Automation** - Playwright tests exist, no integration in UI
- ❌ **I18n & Accessibility** - Single language (English assumed), limited accessibility

---

## Detailed Epic Analysis

### Epic 1: Financial Management – Budgeting, Incomes & Expenses

**Status**: ⚡ PARTIAL (Core entities done, analytics TODO)

**Implemented**:
- ✅ CProjectExpense entity with type, status, approval workflow
- ✅ CProjectIncome entity with type, status
- ✅ CBudget entity with type classification
- ✅ CCurrency support for multi-currency
- ✅ CRUD views for expenses, incomes, budgets
- ✅ GridViews with entity column helpers (status, assigned to, etc.)

**Location**:
```
src/main/java/tech/derbent/app/
├── projectexpenses/
│   ├── domain/CProjectExpense.java
│   ├── domain/CProjectExpenseType.java
│   └── service/CProjectExpenseService.java
├── projectincomes/
│   ├── domain/CProjectIncome.java
│   ├── domain/CProjectIncomeType.java
│   └── service/CProjectIncomeService.java
└── budgets/
    ├── domain/CBudget.java
    ├── domain/CBudgetType.java
    └── service/CBudgetService.java
```

**TODO (28 story points)**:
- ❌ **Budget vs Actual Comparison Dashboard** (8 SP)
- ❌ **Financial Alerts** (budget exceeded notifications) (5 SP)
- ❌ **Cost Rollup** (activity → project → portfolio) (8 SP)
- ❌ **Financial Reports** (export to Excel, PDF) (7 SP)

**Updated Features**:

#### Feature 1.1: Budget Planning & Tracking (13 SP)
- ✅ **US1.1.1**: As a project manager, I can create project budgets with categories (DONE)
- ❌ **US1.1.2**: As a PM, I can see budget vs actual cost comparison (8 SP)
- ❌ **US1.1.3**: As a PM, I receive alerts when budget threshold exceeded (5 SP)

**Tasks for US1.1.2 (Budget vs Actual Dashboard)**:
```java
// Task 1.1.2.1: Create CBudgetAnalysisService (3 SP)
package tech.derbent.plm.budgets.service;

@Service
public class CBudgetAnalysisService {
    
    /**
     * Calculate actual costs from project expenses and time tracking.
     * PATTERN: Use CAbstractService base class
     * RETURN: Map<CBudget, BigDecimal> with actual costs per budget
     */
    public Map<CBudget, BigDecimal> calculateActualCosts(CProject project) {
        // TODO: Sum CProjectExpense.amount where expense.project = project
        // TODO: Group by budget category if linked
        // TODO: Return map of budget → actual cost
    }
    
    /**
     * Compare budget vs actual with variance calculation.
     * RETURN: List<BudgetComparison> with planned, actual, variance, percentage
     */
    public List<BudgetComparison> compareBudgetVsActual(CProject project) {
        // TODO: Get all budgets for project
        // TODO: Calculate actual costs per budget
        // TODO: Calculate variance (actual - planned)
        // TODO: Calculate percentage (actual / planned * 100)
        // TODO: Return comparison data
    }
}

// Task 1.1.2.2: Create CComponentBudgetComparison widget (3 SP)
// PATTERN: Extend CComponentBase
// GRID: Use CGrid with addEntityColumn helpers
// COLUMNS: Budget Name, Planned (currency), Actual (currency), Variance, % Used
// FEATURES: Color-code rows (green <80%, yellow 80-100%, red >100%)

// Task 1.1.2.3: Add to dashboard and project detail view (2 SP)
// LOCATION: CDashboardView, project detail tabs
// PATTERN: Use IPageServiceAutoRegistrable for auto-registration
```

#### Feature 1.2: Expense & Income Tracking (8 SP)
- ✅ **US1.2.1**: As a team member, I can log project expenses with receipts (DONE)
- ✅ **US1.2.2**: As a PM, I can record project income from clients (DONE)
- ❌ **US1.2.3**: As an accountant, I can export financial data to Excel (8 SP)

**Tasks for US1.2.3 (Excel Export)**:
```java
// Task 1.2.3.1: Create CExcelExportService (4 SP)
// PATTERN: Use Apache POI library
// METHODS: exportExpenses(), exportIncomes(), exportBudgets()
// FORMAT: XLSX with formatting, formulas for totals

// Task 1.2.3.2: Add export buttons to grid toolbars (2 SP)
// PATTERN: Use CButton with VaadinIcon.DOWNLOAD
// LOCATION: ProjectExpense, ProjectIncome, Budget views
// HANDLER: on_buttonExport_clicked() → call exportService

// Task 1.2.3.3: Implement StreamResource for download (2 SP)
// PATTERN: Similar to CAttachmentService download pattern
// TRIGGER: Click downloads XLSX file with timestamp in name
```

#### Feature 1.3: Multi-Currency Support (7 SP)
- ✅ **US1.3.1**: As a PM, I can define project currencies (DONE - CCurrency entity exists)
- ❌ **US1.3.2**: As a PM, I can convert expenses between currencies (5 SP)
- ❌ **US1.3.3**: As a PM, I can see consolidated financials in base currency (2 SP)

**Tasks for US1.3.2 (Currency Conversion)**:
```java
// Task 1.3.2.1: Add exchange rate to CCurrency entity (1 SP)
@Column(name = "exchange_rate")
private BigDecimal exchangeRate;  // Rate to base currency

// Task 1.3.2.2: Create CCurrencyConversionService (2 SP)
public BigDecimal convert(BigDecimal amount, CCurrency from, CCurrency to);

// Task 1.3.2.3: Update expense/income calculations (2 SP)
// MODIFY: CBudgetAnalysisService to apply currency conversion
// DISPLAY: Show original amount + converted amount in grids
```

---

### Epic 2: Resource & Timesheet Management

**Status**: ❌ TODO (NOT IMPLEMENTED)

**Gap Analysis**: No resource allocation, availability tracking, or timesheet entities found in codebase.

**Recommended Structure**:
```
src/main/java/tech/derbent/app/
├── resources/
│   ├── domain/CResource.java (extends CEntityNamed)
│   ├── domain/CResourceType.java (enum: HUMAN, EQUIPMENT, FACILITY)
│   ├── domain/CResourceAllocation.java (resource → activity mapping)
│   └── service/CResourceService.java
└── timesheets/
    ├── domain/CTimesheet.java
    ├── domain/CTimesheetEntry.java (work log per day)
    └── service/CTimesheetService.java
```

#### Feature 2.1: Resource Definition & Management (8 SP)
- ❌ **US2.1.1**: As an admin, I can define resources (people, equipment) (5 SP)
- ❌ **US2.1.2**: As an admin, I can set resource availability calendars (3 SP)

**Tasks for US2.1.1 (Resource Entity)**:
```java
// Task 2.1.1.1: Create CResource entity (2 SP)
@Entity
@Table(name = "cresource")
public class CResource extends CEntityOfCompany<CResource> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @AMetaData(displayName = "User", description = "Linked user account if human resource")
    private CUser user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    @AMetaData(displayName = "Resource Type", required = true)
    private EResourceType resourceType;  // HUMAN, EQUIPMENT, FACILITY
    
    @Column(name = "hourly_cost")
    @AMetaData(displayName = "Hourly Cost", description = "Cost per hour for budget calculations")
    private BigDecimal hourlyCost;
    
    @Column(name = "availability_hours_per_day")
    @AMetaData(displayName = "Daily Availability", description = "Available hours per day")
    private Integer availabilityHoursPerDay = 8;
    
    // PATTERN: Follow CUser, CActivity patterns
    // INTERFACES: Implement IHasStatusAndWorkflow for resource status
}

// Task 2.1.1.2: Create CResourceService with validation (1.5 SP)
// PATTERN: Extend CEntityOfCompanyService
// VALIDATION: Ensure user not linked to multiple resources

// Task 2.1.1.3: Create CRUD views (1.5 SP)
// PATTERN: Use CDialogDBEdit with FormBuilder
// GRID: Use addEntityColumn for user, addColumnEntityNamed for status
```

#### Feature 2.2: Resource Allocation & Planning (7 SP)
- ❌ **US2.2.1**: As a PM, I can assign resources to activities with % allocation (5 SP)
- ❌ **US2.2.2**: As a PM, I can see resource capacity vs allocation chart (2 SP)

**Tasks for US2.2.1 (Resource Allocation)**:
```java
// Task 2.2.1.1: Create CResourceAllocation entity (2 SP)
@Entity
@Table(name = "cresource_allocation")
public class CResourceAllocation extends CEntityDB<CResourceAllocation> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    @AMetaData(displayName = "Resource", required = true, 
               dataProviderBean = "CResourceService")
    private CResource resource;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @AMetaData(displayName = "Activity", required = true)
    private CActivity activity;
    
    @Column(name = "allocation_percentage")
    @AMetaData(displayName = "Allocation %", description = "Percentage of resource time allocated")
    private Integer allocationPercentage = 100;
    
    @Column(name = "start_date")
    @AMetaData(displayName = "Start Date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    @AMetaData(displayName = "End Date")
    private LocalDate endDate;
    
    // PATTERN: Master-detail relationship (Activity is master)
    // CASCADE: CascadeType.ALL from Activity
}

// Task 2.2.1.2: Add @OneToMany to CActivity (0.5 SP)
@OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
@AMetaData(displayName = "Resource Allocations", hidden = true)
private Set<CResourceAllocation> resourceAllocations = new HashSet<>();

// Task 2.2.1.3: Create CComponentListResourceAllocations (2 SP)
// PATTERN: Extend CComponentListEntityBase
// GRID COLUMNS: Resource Name (entity), Allocation %, Start Date, End Date
// BUTTONS: Add, Edit, Delete (selection-aware)
// DOUBLE-CLICK: Opens edit dialog

// Task 2.2.1.4: Add to Activity detail view (0.5 SP)
// TAB: "Resources" tab in activity detail
// COMPONENT: CComponentListResourceAllocations
```

#### Feature 2.3: Timesheet Entry & Tracking (6 SP)
- ❌ **US2.3.1**: As a team member, I can enter daily work hours per activity (4 SP)
- ❌ **US2.3.2**: As a PM, I can see actual vs planned work hours (2 SP)

**Tasks for US2.3.1 (Timesheet Entry)**:
```java
// Task 2.3.1.1: Create CTimesheet & CTimesheetEntry entities (2 SP)
@Entity
@Table(name = "ctimesheet")
public class CTimesheet extends CEntityDB<CTimesheet> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
    
    @Column(name = "week_start_date")
    private LocalDate weekStartDate;  // Monday of the week
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ETimesheetStatus status;  // DRAFT, SUBMITTED, APPROVED, REJECTED
    
    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CTimesheetEntry> entries = new HashSet<>();
}

@Entity
@Table(name = "ctimesheet_entry")
public class CTimesheetEntry extends CEntityDB<CTimesheetEntry> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private CTimesheet timesheet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private CActivity activity;
    
    @Column(name = "date")
    private LocalDate date;
    
    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;
    
    @Column(name = "description")
    @Size(max = 1000)
    private String description;
}

// Task 2.3.1.2: Create weekly timesheet grid view (2 SP)
// LAYOUT: 7-day grid (Mon-Sun columns) with activities as rows
// PATTERN: Custom grid with inline editing
// SAVE: Batch save all entries on submit
```

---

### Epic 3: Document & Attachment Management

**Status**: ✅ DONE (28 story points completed)

**Implementation Details**:
```
Location: src/main/java/tech/derbent/app/attachments/
├── domain/
│   ├── CAttachment.java (extends CEntityDB, implements versioning)
│   └── CDocumentType.java (extends CEntityNamed)
├── service/
│   ├── CAttachmentService.java (file storage, version control)
│   └── CDocumentTypeService.java (type management, auto-detection)
└── view/
    ├── CDialogAttachment.java (unified upload/edit dialog)
    └── CComponentListAttachments.java (grid component)
```

**Key Features Implemented**:
1. ✅ **File Upload with Metadata** (CDialogAttachment)
   - Single dialog for upload and edit modes
   - FormBuilder-based form generation
   - Auto-detection of document type from file extension
   - Version number, description, document type fields

2. ✅ **Version Control** (CAttachment entity)
   - previousVersion reference for version chains
   - getVersionHistory() method to traverse versions
   - uploadNewVersion() method with increment
   - Delete prevention for attachments with newer versions

3. ✅ **File Storage** (CAttachmentStorage service)
   - File system storage with organized structure
   - Upload, download, delete operations
   - MIME type detection and validation

4. ✅ **Grid Display** (CComponentListAttachments)
   - Selection-aware toolbar (edit, download, delete buttons)
   - Double-click to edit support
   - Columns: Icon, Version, Filename, Size, Type, Category, Date, User
   - Entity column helpers for proper rendering

5. ✅ **Integration Pattern** (IHasAttachments interface)
   - Clean interface-based integration
   - Works with any entity (not just CEntityDB)
   - Factory method for easy instantiation
   - Auto-registration with page service

**Coding Standards Applied**:
- ✅ C-prefix convention (CAttachment, CDialogAttachment)
- ✅ Single dialog for create and edit (no separate upload/edit dialogs)
- ✅ FormBuilder usage for all form fields
- ✅ Entity column helpers in grid (addEntityColumn, addColumnEntityNamed)
- ✅ Selection-aware buttons (disabled by default, enabled on selection)
- ✅ Double-click to edit support
- ✅ Proper binder.writeBean() in save methods
- ✅ CNotificationService for all notifications
- ✅ Proper error handling with try-catch

**Remaining Enhancements** (Nice-to-have, not critical):
- ❌ In-browser preview for common file types (PDF, images) (5 SP)
- ❌ Full-text search across document content (8 SP)
- ❌ Document approval workflow (5 SP)

---

### Epic 4: Reporting & Analytics

**Status**: ❌ TODO (Analytics and dashboards minimal)

**Current State**:
- ✅ CDashboardView exists with basic widgets
- ✅ CComponentWidgetActivity, CComponentWidgetMeeting, CComponentWidgetSprint
- ❌ No burndown charts
- ❌ No velocity charts  
- ❌ No financial dashboards
- ❌ No custom report builder

#### Feature 4.1: Agile Metrics & Charts (13 SP)
- ❌ **US4.1.1**: As a Scrum Master, I can view sprint burndown chart (5 SP)
- ❌ **US4.1.2**: As a PM, I can view team velocity over time (4 SP)
- ❌ **US4.1.3**: As a PM, I can view cumulative flow diagram (4 SP)

**Tasks for US4.1.1 (Sprint Burndown Chart)**:
```java
// Task 4.1.1.1: Create CSprintMetricsService (2 SP)
@Service
public class CSprintMetricsService {
    
    /**
     * Calculate daily story point completion for burndown chart.
     * RETURN: Map<LocalDate, Integer> with remaining story points per day
     */
    public Map<LocalDate, Integer> calculateBurndown(CSprint sprint) {
        // TODO: Get sprint start/end dates
        // TODO: Get all sprint items with completion dates
        // TODO: Calculate remaining story points per day
        // TODO: Return time series data
    }
    
    /**
     * Calculate ideal burndown line for comparison.
     * RETURN: Map<LocalDate, BigDecimal> with ideal remaining points per day
     */
    public Map<LocalDate, BigDecimal> calculateIdealBurndown(CSprint sprint) {
        // TODO: Total story points / sprint days
        // TODO: Linear decrease from total to zero
    }
}

// Task 4.1.1.2: Create CComponentBurndownChart using Vaadin Charts (2 SP)
// LIBRARY: Add Vaadin Charts dependency to pom.xml
// CHART TYPE: Line chart with two series (actual, ideal)
// X-AXIS: Sprint days
// Y-AXIS: Story points remaining

// Task 4.1.1.3: Add to sprint detail view (1 SP)
// LOCATION: Sprint detail tabs
// TAB: "Metrics" tab with burndown chart
```

#### Feature 4.2: Financial Dashboards (10 SP)
- ❌ **US4.2.1**: As a PM, I can view project cost dashboard (5 SP)
- ❌ **US4.2.2**: As a CFO, I can view portfolio financial summary (5 SP)

#### Feature 4.3: Custom Report Builder (10 SP)
- ❌ **US4.3.1**: As an admin, I can create custom reports with filters (7 SP)
- ❌ **US4.3.2**: As a PM, I can schedule automated report emails (3 SP)

---

### Epic 5: Portfolio & Program Management

**Status**: ❌ TODO (No portfolio entities)

**Gap**: No way to group projects into portfolios or programs.

**Recommended Structure**:
```java
// CPortfolio entity
@Entity
@Table(name = "cportfolio")
public class CPortfolio extends CEntityOfCompany<CPortfolio> {
    
    @Column(name = "budget")
    private BigDecimal budget;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @AMetaData(displayName = "Portfolio Owner", dataProviderBean = "CUserService")
    private CUser owner;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private Set<CProject> projects = new HashSet<>();
}

// Add to CProject:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "portfolio_id")
@AMetaData(displayName = "Portfolio", dataProviderBean = "CPortfolioService")
private CPortfolio portfolio;
```

#### Feature 5.1: Portfolio Definition & Organization (8 SP)
- ❌ **US5.1.1**: As an executive, I can create portfolios grouping related projects (5 SP)
- ❌ **US5.1.2**: As a PM, I can assign projects to portfolios (3 SP)

#### Feature 5.2: Portfolio Dashboards (12 SP)
- ❌ **US5.2.1**: As an executive, I can view portfolio health dashboard (6 SP)
- ❌ **US5.2.2**: As an executive, I can view portfolio budget vs actual (6 SP)

#### Feature 5.3: Cross-Project Dependencies (8 SP)
- ❌ **US5.3.1**: As a PM, I can define dependencies between projects (5 SP)
- ❌ **US5.3.2**: As a PM, I see warnings when dependent project delayed (3 SP)

---

### Epic 6: Idea & Backlog Management

**Status**: ❌ TODO (No idea/voting features)

**Current Backlog**: Sprint backlog exists (CSprintItem), but no product backlog or idea management.

#### Feature 6.1: Idea Capture & Voting (10 SP)
- ❌ **US6.1.1**: As a stakeholder, I can submit feature ideas (4 SP)
- ❌ **US6.1.2**: As a stakeholder, I can vote on ideas (3 SP)
- ❌ **US6.1.3**: As a PM, I can see top-voted ideas (3 SP)

**Recommended Structure**:
```java
@Entity
@Table(name = "cidea")
public class CIdea extends CEntityOfCompany<CIdea> {
    
    @Column(name = "title")
    @AMetaData(displayName = "Idea Title", required = true, maxLength = 255)
    private String title;
    
    @Column(name = "description", length = 2000)
    @AMetaData(displayName = "Description", maxLength = 2000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    private CUser submittedBy;
    
    @Column(name = "vote_count")
    private Integer voteCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EIdeaStatus status;  // SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, IMPLEMENTED
    
    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CIdeaVote> votes = new HashSet<>();
}

@Entity
@Table(name = "cidea_vote")
public class CIdeaVote extends CEntityDB<CIdeaVote> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id", nullable = false)
    private CIdea idea;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
    
    @Column(name = "vote_date")
    private LocalDateTime voteDate;
}
```

#### Feature 6.2: Product Backlog Board (11 SP)
- ❌ **US6.2.1**: As a PO, I can maintain prioritized product backlog (6 SP)
- ❌ **US6.2.2**: As a PO, I can drag-drop to reorder backlog (5 SP)

---

### Epic 7: Issue & Bug Tracking

**Status**: ⚡ PARTIAL (CTicket exists but limited)

**Current State**:
- ✅ CTicket, CTicketType entities exist
- ❌ No dedicated bug workflow (severity, priority, triage)
- ❌ No bug-specific views (just generic ticket CRUD)

**Enhancement Required**:
```java
// Add to CTicket:
@Enumerated(EnumType.STRING)
@Column(name = "severity")
@AMetaData(displayName = "Severity", required = true)
private EIssueSeverity severity;  // CRITICAL, HIGH, MEDIUM, LOW

@Enumerated(EnumType.STRING)
@Column(name = "priority")
@AMetaData(displayName = "Priority", required = true)
private EIssuePriority priority;  // P0, P1, P2, P3, P4

@Column(name = "reproduction_steps", length = 2000)
@AMetaData(displayName = "Steps to Reproduce")
private String reproductionSteps;

@Column(name = "environment")
@AMetaData(displayName = "Environment", maxLength = 500)
private String environment;  // Browser, OS, version info
```

#### Feature 7.1: Enhanced Bug Tracking (10 SP)
- ❌ **US7.1.1**: As a QA, I can log bugs with severity and reproduction steps (5 SP)
- ❌ **US7.1.2**: As a developer, I can link bugs to code commits (5 SP)

#### Feature 7.2: Bug Triage Workflow (10 SP)
- ❌ **US7.2.1**: As a team lead, I can triage bugs by severity/priority (5 SP)
- ❌ **US7.2.2**: As a PM, I see bug metrics dashboard (open, closed, by severity) (5 SP)

---

### Epic 8: Calendar & Scheduling Integration

**Status**: ❌ TODO (No calendar view)

**Current State**:
- ✅ Meeting entities exist (CMeeting)
- ✅ Sprint dates exist (CSprint start/end dates)
- ❌ No unified calendar view
- ❌ No drag-drop rescheduling
- ❌ No conflict detection

#### Feature 8.1: Unified Calendar View (8 SP)
- ❌ **US8.1.1**: As a team member, I can see all meetings/sprints in calendar (5 SP)
- ❌ **US8.1.2**: As a PM, I can drag-drop to reschedule meetings (3 SP)

**Recommended Implementation**:
```java
// Use FullCalendar Vaadin component
// LIBRARY: Add FullCalendar Flow dependency

@Route("calendar")
public class CCalendarView extends CAbstractPage {
    
    private FullCalendar calendar;
    
    @Override
    protected void setupContent() {
        calendar = new FullCalendar();
        calendar.setBusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        
        // Load meetings
        List<CMeeting> meetings = meetingService.findAll();
        meetings.forEach(meeting -> {
            CalendarEvent event = new CalendarEvent();
            event.setTitle(meeting.getName());
            event.setStart(meeting.getStartDate());
            event.setEnd(meeting.getEndDate());
            event.setColor("#3788d8");
            calendar.addEvent(event);
        });
        
        // Load sprints
        List<CSprint> sprints = sprintService.findAll();
        sprints.forEach(sprint -> {
            CalendarEvent event = new CalendarEvent();
            event.setTitle("Sprint: " + sprint.getName());
            event.setStart(sprint.getStartDate());
            event.setEnd(sprint.getEndDate());
            event.setColor("#28a745");
            event.setAllDay(true);
            calendar.addEvent(event);
        });
        
        // Drag-drop handler
        calendar.addEventMovedListener(e -> {
            // Update meeting or sprint dates
            rescheduleEvent(e.getEvent(), e.getNewStart(), e.getNewEnd());
        });
        
        add(calendar);
    }
}
```

#### Feature 8.2: Conflict Detection & Resolution (9 SP)
- ❌ **US8.2.1**: As a PM, I see warnings when scheduling conflicts occur (5 SP)
- ❌ **US8.2.2**: As a PM, I can auto-find available time slots (4 SP)

---

### Epic 9: Permissions & Role Management UI

**Status**: ❌ TODO (Backend exists, no UI)

**Current State**:
- ✅ CRole, CUserProjectRole entities exist
- ✅ CUserService with role management methods
- ❌ No UI for role/permission management
- ❌ No permission scheme configuration

#### Feature 9.1: Role Management UI (8 SP)
- ❌ **US9.1.1**: As an admin, I can create/edit roles with permissions (5 SP)
- ❌ **US9.1.2**: As an admin, I can assign roles to users per project (3 SP)

**Recommended UI**:
```java
@Route("admin/roles")
public class CRoleManagementView extends CAbstractPage {
    
    // Left side: Role list grid
    private CGrid<CRole> roleGrid;
    
    // Right side: Permission matrix
    private CheckboxGroup<String> permissionsGroup;
    
    // Permissions: VIEW_PROJECT, EDIT_PROJECT, DELETE_PROJECT, 
    //              VIEW_ACTIVITY, EDIT_ACTIVITY, DELETE_ACTIVITY, etc.
    
    // Save button updates CRole.permissions field
}
```

---

### Epic 10: Client Portal & Stakeholder Collaboration

**Status**: ❌ TODO

**No external user access** in current codebase.

#### Feature 10.1: Client Access Portal (10 SP)
- ❌ **US10.1.1**: As a client, I can view my project status dashboard (5 SP)
- ❌ **US10.1.2**: As a client, I can approve deliverables (5 SP)

**Recommended Structure**:
```java
@Entity
@Table(name = "cstakeholder")
public class CStakeholder extends CEntityDB<CStakeholder> {
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private EStakeholderRole role;  // CLIENT, PARTNER, AUDITOR
    
    @ManyToMany
    @JoinTable(
        name = "cstakeholder_project",
        joinColumns = @JoinColumn(name = "stakeholder_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<CProject> projects = new HashSet<>();
    
    @Column(name = "access_level")
    @Enumerated(EnumType.STRING)
    private EAccessLevel accessLevel;  // READ_ONLY, COMMENT, APPROVE
}

// Separate route for client portal
@Route("portal")
public class CClientPortalView extends Div {
    // Minimal UI, no admin features
    // Show only projects where user is stakeholder
}
```

#### Feature 10.2: Stakeholder Feedback (10 SP)
- ❌ **US10.2.1**: As a client, I can comment on deliverables (5 SP)
- ❌ **US10.2.2**: As a PM, I receive notifications of client feedback (5 SP)

---

### Epic 11: AI & Automation

**Status**: ❌ TODO

**No AI features** in current codebase.

#### Feature 11.1: Intelligent Recommendations (10 SP)
- ❌ **US11.1.1**: As a PM, I receive AI suggestions for task priority (5 SP)
- ❌ **US11.1.2**: As a PM, I get risk alerts based on project patterns (5 SP)

**Recommended Approach**:
- Use external AI API (OpenAI, Azure AI)
- Simple rule-based recommendations initially
- ML model for risk prediction later

#### Feature 11.2: Natural Language Summaries (8 SP)
- ❌ **US11.2.1**: As an executive, I can view AI-generated project summaries (5 SP)
- ❌ **US11.2.2**: As a PM, I can ask questions about project data in natural language (3 SP)

#### Feature 11.3: Predictive Analytics (8 SP)
- ❌ **US11.3.1**: As a PM, I see predicted project completion date (5 SP)
- ❌ **US11.3.2**: As a PM, I see predicted budget overrun risk (3 SP)

---

### Epic 12: Notifications & Integrations

**Status**: ⚡ PARTIAL (Basic notifications exist via CNotificationService)

**Current State**:
- ✅ CNotificationService for toast notifications
- ❌ No email notifications
- ❌ No Slack/Teams integration
- ❌ No webhook support

#### Feature 12.1: Email Notifications (8 SP)
- ❌ **US12.1.1**: As a user, I receive email when assigned to task (4 SP)
- ❌ **US12.1.2**: As a user, I can configure notification preferences (4 SP)

**Recommended Implementation**:
```java
@Service
public class CEmailNotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendTaskAssignmentEmail(CUser user, CActivity activity) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Assigned to: " + activity.getName());
        message.setText("You have been assigned to activity: " + activity.getName());
        mailSender.send(message);
    }
    
    // Template-based emails with Thymeleaf
}

// Add to application.properties:
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
```

#### Feature 12.2: Slack Integration (8 SP)
- ❌ **US12.2.1**: As a team, we receive Slack notifications for project updates (5 SP)
- ❌ **US12.2.2**: As an admin, I can configure Slack webhook URL (3 SP)

---

### Epic 13: Quality Assurance & Testing Automation

**Status**: ⚡ PARTIAL (Playwright tests exist, validation module is implemented; automation integration is pending)

**Current State**:
- ✅ Comprehensive Playwright test suite (comprehensive, menu, crud, all-views)
- ✅ Screenshot capture functionality
- ✅ Automated test execution scripts
- ✅ Validation management entities in app
- ✅ Validation execution UI for recording results

#### Feature 13.1: Validation Case Management (7 SP)
- ❌ **US13.1.1**: As a QA, I can create validation cases linked to requirements (4 SP)
- ❌ **US13.1.2**: As a QA, I can organize validation cases in validation suites (3 SP)

**Recommended Structure**:
```java
@Entity
@Table(name = "cvalidationcase")
public class CValidationCase extends CEntityOfProject<CValidationCase> {
    
    @OneToMany(mappedBy = "validationCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @AMetaData(displayName = "Validation Steps")
    private Set<CValidationStep> validationSteps = new HashSet<>();
    
    @Column(name = "expected_result", length = 1000)
    @AMetaData(displayName = "Expected Result")
    private String expectedResult;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id")
    @AMetaData(displayName = "Requirement")
    private CActivity requirement;  // Or CRequirement if added
    
    @Enumerated(EnumType.STRING)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validationcasetype_id")
    private CValidationCaseType validationCaseType;
}

@Entity
@Table(name = "cvalidationexecution")
public class CValidationExecution extends CEntityDB<CValidationExecution> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validationcase_id", nullable = false)
    private CValidationCase validationCase;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_id")
    private CUser executedBy;
    
    @Column(name = "validation_date")
    private LocalDateTime validationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private CValidationResult result;  // PASSED, FAILED, BLOCKED, SKIPPED
    
    @Column(name = "actual_result", length = 2000)
    private String actualResult;
}
```

#### Feature 13.2: Test Automation Integration (6 SP)
- ❌ **US13.2.1**: As a QA, I can trigger Playwright tests from UI (3 SP)
- ❌ **US13.2.2**: As a QA, I can view test execution results and screenshots (3 SP)

---

### Epic 14: Internationalization (I18n) & Accessibility

**Status**: ❌ TODO

**Current State**:
- ❌ All UI text hardcoded in English
- ❌ No translation infrastructure
- ❌ Limited accessibility features

#### Feature 14.1: Multi-Language Support (8 SP)
- ❌ **US14.1.1**: As an admin, I can configure application language (3 SP)
- ❌ **US14.1.2**: As a user, I can switch UI language (5 SP)

**Recommended Implementation**:
```java
// Use Vaadin I18N provider
public class CustomI18NProvider implements I18NProvider {
    
    private static final ResourceBundle BUNDLE_EN = 
        ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH);
    private static final ResourceBundle BUNDLE_TR = 
        ResourceBundle.getBundle("i18n.messages", new Locale("tr"));
    
    @Override
    public List<Locale> getProvidedLocales() {
        return Arrays.asList(Locale.ENGLISH, new Locale("tr"));
    }
    
    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        ResourceBundle bundle = locale.getLanguage().equals("tr") ? BUNDLE_TR : BUNDLE_EN;
        return MessageFormat.format(bundle.getString(key), params);
    }
}

// resources/i18n/messages_en.properties
button.save=Save
button.cancel=Cancel
activity.name=Activity Name

// resources/i18n/messages_tr.properties
button.save=Kaydet
button.cancel=İptal
activity.name=Aktivite Adı
```

#### Feature 14.2: Accessibility Compliance (WCAG 2.1 AA) (8 SP)
- ❌ **US14.2.1**: As a user, I can navigate entire app with keyboard only (5 SP)
- ❌ **US14.2.2**: As a visually impaired user, I can use screen readers (3 SP)

---

## Implementation Priority Recommendations

Based on business value, user demand, and technical dependencies:

### Phase 1: High Priority (Q1 2026)
1. **Resource & Timesheet Management** (Epic 2) - 21 SP
   - Critical for resource planning and cost tracking
   - Dependency for accurate project costing
   
2. **Reporting & Analytics** (Epic 4) - 33 SP
   - Burndown charts essential for Agile teams
   - Financial dashboards needed by management
   
3. **Email Notifications** (Epic 12.1) - 8 SP
   - Basic communication requirement
   - Easy to implement, high impact

### Phase 2: Medium Priority (Q2 2026)
4. **Portfolio Management** (Epic 5) - 28 SP
   - Needed for scaling beyond single projects
   - Executive visibility requirement

5. **Enhanced Bug Tracking** (Epic 7) - 20 SP
   - Improves existing ticket system
   - QA team requirement

6. **Calendar Integration** (Epic 8) - 17 SP
   - Scheduling conflicts resolution
   - Team coordination improvement

### Phase 3: Long-term (Q3-Q4 2026)
7. **Idea & Backlog Management** (Epic 6) - 21 SP
8. **Client Portal** (Epic 10) - 20 SP
9. **Permissions UI** (Epic 9) - 8 SP
10. **Slack Integration** (Epic 12.2) - 8 SP

### Phase 4: Future (2027+)
11. **AI & Automation** (Epic 11) - 26 SP
12. **Validation Case Management** (Epic 13) - 13 SP
13. **I18n & Accessibility** (Epic 14) - 16 SP

---

## Coding Standards Enforcement in Tasks

All task descriptions in the updated backlog will follow these patterns:

### Entity Creation Tasks
```java
// Template for entity creation task
Task X.Y.Z.1: Create C{EntityName} entity (N SP)

@Entity
@Table(name = "c{entity_name}")
public class C{EntityName} extends C{BaseClass}<C{EntityName}> {
    
    // PATTERN: Follow entity inheritance decision tree
    // - CEntityDB for database entities
    // - CEntityNamed if has name property
    // - CEntityOfCompany if company-scoped
    // - CEntityOfProject if project-scoped
    // - CProjectItem if work item with status workflow
    
    // ANNOTATIONS: Use @AMetaData for all fields
    @Column(name = "field_name")
    @AMetaData(
        displayName = "Field Label",
        required = true/false,
        readOnly = false,
        description = "Field purpose",
        maxLength = 255,
        dataProviderBean = "CServiceName" // For ComboBox
    )
    private Type fieldName;
    
    // RELATIONSHIPS: Use proper fetch types
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private CParent parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CChild> children = new HashSet<>();
}
```

### Service Creation Tasks
```java
// Template for service creation task
Task X.Y.Z.2: Create C{EntityName}Service (N SP)

@Service
public class C{EntityName}Service extends C{BaseServiceClass}<C{EntityName}> {
    
    // PATTERN: Extend appropriate base service
    // - CAbstractService for CEntityDB entities
    // - CEntityOfCompanyService for CEntityOfCompany entities
    // - CEntityOfProjectService for CEntityOfProject entities
    // - CProjectItemService for CProjectItem entities
    
    private final I{EntityName}Repository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    // CONSTRUCTOR: Inject dependencies
    public C{EntityName}Service(
            I{EntityName}Repository repository,
            Clock clock,
            ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.clock = clock;
        this.sessionService = sessionService;
    }
    
    // VALIDATION: Override validateBeforeSave for business rules
    @Override
    protected void validateBeforeSave(C{EntityName} entity) {
        super.validateBeforeSave(entity);
        Check.notNull(entity, "Entity cannot be null");
        Check.notBlank(entity.getName(), "Name is required");
        // Additional validation...
    }
    
    // STATELESS: No instance fields storing user data
    // TRANSACTIONAL: Use @Transactional for database operations
}
```

### Dialog Creation Tasks
```java
// Template for dialog creation task
Task X.Y.Z.3: Create C{EntityName}Dialog (N SP)

public class CDialog{EntityName} extends CDialogDBEdit<C{EntityName}> {
    
    // PATTERN: Single dialog for both create and edit
    // - Two constructors: isNew=true/false
    // - Use CFormBuilder for all form fields
    // - Call binder.writeBean() before save
    
    private final C{EntityName}Service service;
    private final CEnhancedBinder<C{EntityName}> binder;
    private final CFormBuilder<C{EntityName}> formBuilder;
    
    // Constructor for create mode
    public CDialog{EntityName}(
            C{EntityName}Service service,
            Consumer<C{EntityName}> onSave) throws Exception {
        super(new C{EntityName}(), onSave, true);  // isNew = true
        // Initialize binder and formBuilder
        setupDialog();
        populateForm();
    }
    
    // Constructor for edit mode
    public CDialog{EntityName}(
            C{EntityName} entity,
            C{EntityName}Service service,
            Consumer<C{EntityName}> onSave) throws Exception {
        super(entity, onSave, false);  // isNew = false
        // Initialize binder and formBuilder
        setupDialog();
        populateForm();
    }
    
    private void createFormFields() throws Exception {
        // Define fields to display
        final List<String> fields = List.of(
            "name", "description", "status", "assignedTo"
        );
        
        // FormBuilder creates fields from @AMetaData
        getDialogLayout().add(
            formBuilder.build(C{EntityName}.class, binder, fields)
        );
    }
    
    @Override
    protected void save() throws Exception {
        validateForm();
        
        // CRITICAL: Write form data back to entity
        binder.writeBean(getEntity());
        
        if (onSave != null) {
            onSave.accept(getEntity());
        }
        
        close();
        CNotificationService.showSaveSuccess();
    }
}
```

### Grid Component Creation Tasks
```java
// Template for grid component creation task
Task X.Y.Z.4: Create CComponentList{EntityName} (N SP)

public class CComponentList{EntityName} 
        extends CComponentListEntityBase<C{EntityName}, CParent> {
    
    // PATTERN: Extend CComponentListEntityBase
    // - Selection-aware buttons (disabled by default)
    // - Double-click to edit
    // - Entity column helpers for entity references
    
    private final C{EntityName}Service service;
    private CGrid<C{EntityName}> grid;
    private CButton buttonCreate;
    private CButton buttonEdit;
    private CButton buttonDelete;
    
    @Override
    public void configureGrid(final CGrid<C{EntityName}> grid) {
        // Standard columns
        grid.addIdColumn(C{EntityName}::getId, "ID", "id");
        grid.addShortTextColumn(C{EntityName}::getName, "Name", "name");
        
        // MANDATORY: Use entity column helpers for entity references
        grid.addColumnEntityNamed(C{EntityName}::getStatus, "Status");
        grid.addColumnEntityNamed(C{EntityName}::getAssignedTo, "Assigned To");
        grid.addColumnEntityCollection(C{EntityName}::getParticipants, "Participants");
        
        // Date columns
        grid.addDateTimeColumn(C{EntityName}::getCreated, "Created", "created");
    }
    
    private void createToolbar() {
        // Create button (always enabled)
        buttonCreate = new CButton(VaadinIcon.PLUS.create());
        buttonCreate.addClickListener(e -> on_buttonCreate_clicked());
        
        // Edit button (disabled until selection)
        buttonEdit = new CButton(VaadinIcon.EDIT.create());
        buttonEdit.setEnabled(false);
        buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
        
        // Delete button (disabled until selection)
        buttonDelete = new CButton(VaadinIcon.TRASH.create());
        buttonDelete.setEnabled(false);
        buttonDelete.addClickListener(e -> on_buttonDelete_clicked());
    }
    
    // MANDATORY: Selection listener updates button states
    protected void on_grid_selectionChanged(final C{EntityName} selected) {
        final boolean hasSelection = (selected != null);
        buttonEdit.setEnabled(hasSelection);
        buttonDelete.setEnabled(hasSelection);
    }
    
    // MANDATORY: Double-click opens edit dialog
    protected void on_grid_doubleClicked(final C{EntityName} entity) {
        if (entity != null) {
            on_buttonEdit_clicked();
        }
    }
}
```

---

## Summary Statistics

### Current Implementation Status
- **Total Entities**: 56 domain entities
- **Total Services**: 160+ service classes
- **Total Views**: 40+ view/UI classes
- **Completed Epics**: 1/14 (Epic 3: Document Management)
- **Partial Epics**: 4/14 (Financial, Agile, Risk, Issue Tracking)
- **TODO Epics**: 9/14

### Story Point Distribution
- **Completed**: 28 SP (Epic 3)
- **In Progress**: ~60 SP (partial epics)
- **TODO**: ~320 SP (remaining work)
- **Total Estimated**: ~408 SP

### Package Coverage
```
✅ Fully Implemented:
- attachments (2 entities, complete workflow)
- activities (3 entities, CRUD + kanban)
- meetings (2 entities, CRUD)
- sprints (4 entities, sprint board, kanban)
- risks (5 entities, CRUD)
- products (4 entities, versioning)
- orders (5 entities, approval workflow)
- budgets (2 entities, CRUD)
- projectexpenses (2 entities, CRUD)
- projectincomes (2 entities, CRUD)

⚡ Partially Implemented:
- tickets (2 entities, needs bug-specific workflow)
- comments (2 entities, needs threading)

❌ Missing Packages:
- resources (no entity)
- timesheets (no entity)
- portfolios (no entity)
- ideas (no entity)
- validation (no entity)
- stakeholders (no entity)
```

---

## Recommendations for AI Agents

When implementing tasks from this backlog:

1. **Always Check Entity Decision Tree**
   - Consult `docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md`
   - Use correct base class (CEntityDB → CEntityNamed → CEntityOfCompany → CEntityOfProject → CProjectItem)

2. **Always Use Coding Standards**
   - C-prefix for all custom classes
   - FormBuilder for all dialog forms
   - Entity column helpers for grids
   - Selection-aware buttons
   - Double-click to edit

3. **Always Follow Existing Patterns**
   - Look for similar entities before creating new ones
   - Copy-paste from working examples (CAttachment, CActivity, CMeeting)
   - Use same package structure (domain, service, view)

4. **Always Validate Against Guidelines**
   - Check `.cursorrules` before starting
   - Review `docs/architecture/coding-standards.md`
   - Consult `docs/architecture/entity-dialog-coding-standards.md`

5. **Always Test Thoroughly**
   - Run `mvn clean compile` before committing
   - Run Playwright tests if UI changes
   - Capture screenshots for documentation

---

## Next Steps

1. **Update PROJECT_BACKLOG.xlsx** with this analysis
2. **Create detailed task descriptions** for Priority 1 items
3. **Set up project tracking** in Derbent itself (dogfooding)
4. **Assign story points** to all user stories
5. **Create sprint plan** for Q1 2026

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-13  
**Author**: AI Analysis Engine  
**Review Status**: Pending human review
