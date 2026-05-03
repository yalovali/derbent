package tech.derbent.api.imports.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generates sample Excel workbooks demonstrating the full import format.
 *
 * <p>SHEET ORDER: reference data (types, priorities, workflows, statuses) must come before entity
 * data because importers fail on unknown FK names.</p>
 *
 * <p>Two full-project factory methods are provided:</p>
 * <ul>
 *   <li>{@link #createPlmProjectWorkbook()} — PLM project (Smart Building IoT Platform v2.0)</li>
 *   <li>{@link #createBabProjectWorkbook()} — BAB/IoT project (Building Automation System v1.0)</li>
 * </ul>
 * The legacy {@link #createSampleWorkbook()} is kept for backward compatibility.
 *
 * <p>Date columns use "yyyy-MM-dd" string format which is accepted by all importer date parsers.
 * The Activity sheet additionally demonstrates Excel formula evaluation for estimated hours and
 * a formula-based due date (G7+14).</p>
 */
public final class CSampleImportExcelGenerator {

    private CSampleImportExcelGenerator() { /* utility class */ }

    // ── public API ───────────────────────────────────────────────────────────

    /** Creates the legacy two-sheet sample (Activity + Issue). Kept for backward compatibility. */
    public static Workbook createSampleWorkbook() {
        final Workbook wb = new XSSFWorkbook();
        createActivitySheet(wb);
        createIssueSheet(wb);
        return wb;
    }

    /**
     * Creates a comprehensive PLM project workbook for "Smart Building IoT Platform v2.0".
     *
     * Sheet order respects import dependency:
     * reference data → agile hierarchy → project items → child entities.
     */
    public static Workbook createPlmProjectWorkbook() {
        final Workbook wb = new XSSFWorkbook();
        addReferenceSheets(wb);
        addPlmAgileSheets(wb);
        createActivitySheet(wb);
        createPlmMeetingSheet(wb);
        createPlmDecisionSheet(wb);
        createIssueSheet(wb);
        createPlmTicketSheet(wb);
        createPlmCommentSheet(wb);
        createPlmLinkSheet(wb);
        return wb;
    }

    /**
     * Creates a comprehensive BAB project workbook for "Building Automation System v1.0".
     *
     * BAB IoT project uses standard PLM entities for project management;
     * device/node configuration is handled by the BAB initializer, not Excel import.
     */
    public static Workbook createBabProjectWorkbook() {
        final Workbook wb = new XSSFWorkbook();
        addReferenceSheets(wb);
        addBabAgileSheets(wb);
        createBabActivitySheet(wb);
        createBabMeetingSheet(wb);
        createBabDecisionSheet(wb);
        createBabIssueSheet(wb);
        createBabCommentSheet(wb);
        createBabLinkSheet(wb);
        return wb;
    }

    /** Writes any workbook to the given stream and closes it. */
    public static void writeSampleWorkbook(final OutputStream out) throws IOException {
        try (final Workbook wb = createSampleWorkbook()) {
            wb.write(out);
        }
    }

    // ── shared style factories ────────────────────────────────────────────────

    static CellStyle createHeaderStyle(final Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    static CellStyle createCommentStyle(final Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    static CellStyle createDateStyle(final Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy-mm-dd"));
        return style;
    }

    // ── common sheet groups ───────────────────────────────────────────────────

    private static void addReferenceSheets(final Workbook wb) {
        createWorkflowEntitySheet(wb);
        createStatusSheet(wb);
        createActivityTypeSheet(wb);
        createIssueTypeSheet(wb);
        createMeetingTypeSheet(wb);
        createDecisionTypeSheet(wb);
        createSprintTypeSheet(wb);
        createActivityPrioritySheet(wb);
        createEpicTypeSheet(wb);
        createFeatureTypeSheet(wb);
        createUserStoryTypeSheet(wb);
        createRequirementTypeSheet(wb);
        createMilestoneTypeSheet(wb);
        createDeliverableTypeSheet(wb);
        createTicketTypeSheet(wb);
        createTicketPrioritySheet(wb);
    }

    private static void addPlmAgileSheets(final Workbook wb) {
        createSprintSheet(wb);
        createPlmEpicSheet(wb);
        createPlmFeatureSheet(wb);
        createPlmUserStorySheet(wb);
        createRequirementSheet(wb);
        createMilestoneSheet(wb);
        createDeliverableSheet(wb);
    }

    private static void addBabAgileSheets(final Workbook wb) {
        createSprintSheet(wb);
        createBabEpicSheet(wb);
        createBabFeatureSheet(wb);
        createBabUserStorySheet(wb);
        createRequirementSheet(wb);
        createMilestoneSheet(wb);
        createDeliverableSheet(wb);
    }

    // ── reference data sheets (shared by both project types) ─────────────────

    private static void createWorkflowEntitySheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Workflow Entity");
        comment(sheet, 0, "# Workflow definitions — controls allowed status transitions");
        comment(sheet, 1, "# Columns: Name (required)");
        header(wb, sheet, 2, "Name");
        row(sheet, 3, "Default Workflow");
        row(sheet, 4, "Bug Workflow");
        row(sheet, 5, "Agile Sprint Workflow");
        autoSize(sheet, 1);
    }

    private static void createStatusSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Status");
        comment(sheet, 0, "# Project item statuses — shared across all entity types in the company");
        comment(sheet, 1, "# finalStatus=true means the item is done (affects sprint metrics and Kanban)");
        header(wb, sheet, 2, "Name", "Color", "Icon", "Final Status");
        row(sheet, 3, "To Do",       "#6C757D", "vaadin:circle",       "false");
        row(sheet, 4, "In Progress", "#007BFF", "vaadin:cog",          "false");
        row(sheet, 5, "In Review",   "#FD7E14", "vaadin:eye",          "false");
        row(sheet, 6, "Done",        "#28A745", "vaadin:check-circle", "true");
        row(sheet, 7, "Cancelled",   "#DC3545", "vaadin:close-circle", "true");
        row(sheet, 8, "Blocked",     "#6F42C1", "vaadin:ban",          "false");
        autoSize(sheet, 4);
    }

    private static void createActivityTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Activity Type");
        comment(sheet, 0, "# Activity types — categorize work items; Workflow column references Workflow Entity sheet");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Workflow");
        row(sheet, 2, "Design",        "#6F42C1", "10", "Default Workflow");
        row(sheet, 3, "Development",   "#007BFF", "20", "Default Workflow");
        row(sheet, 4, "Testing",       "#17A2B8", "30", "Default Workflow");
        row(sheet, 5, "Documentation", "#6C757D", "40", "Default Workflow");
        row(sheet, 6, "Review",        "#FD7E14", "50", "Default Workflow");
        row(sheet, 7, "Deployment",    "#28A745", "60", "Default Workflow");
        row(sheet, 8, "Research",      "#DC3545", "70", "Default Workflow");
        autoSize(sheet, 4);
    }

    private static void createIssueTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Issue Type");
        comment(sheet, 0, "# Issue types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Workflow");
        row(sheet, 2, "Bug",                    "#DC3545", "10", "Bug Workflow");
        row(sheet, 3, "Improvement",            "#007BFF", "20", "Default Workflow");
        row(sheet, 4, "Feature Request",        "#28A745", "30", "Default Workflow");
        row(sheet, 5, "Task",                   "#6C757D", "40", "Default Workflow");
        row(sheet, 6, "Security Vulnerability", "#6F42C1", "50", "Bug Workflow");
        autoSize(sheet, 4);
    }

    private static void createMeetingTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Meeting Type");
        comment(sheet, 0, "# Meeting types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order");
        row(sheet, 2, "Sprint Planning",    "#007BFF", "10");
        row(sheet, 3, "Sprint Review",      "#28A745", "20");
        row(sheet, 4, "Retrospective",      "#FD7E14", "30");
        row(sheet, 5, "Architecture Review","#6F42C1", "40");
        row(sheet, 6, "Client Demo",        "#17A2B8", "50");
        row(sheet, 7, "Stakeholder Update", "#DC3545", "60");
        autoSize(sheet, 3);
    }

    private static void createDecisionTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Decision Type");
        comment(sheet, 0, "# Decision types. requiresApproval=true gates the decision until approved.");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Requires Approval");
        row(sheet, 2, "Architecture",       "#6F42C1", "10", "false");
        row(sheet, 3, "Technology Choice",  "#007BFF", "20", "false");
        row(sheet, 4, "Budget Approval",    "#DC3545", "30", "true");
        row(sheet, 5, "Scope Change",       "#FD7E14", "40", "true");
        row(sheet, 6, "Risk Acceptance",    "#17A2B8", "50", "false");
        autoSize(sheet, 4);
    }

    private static void createSprintTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Sprint Type");
        comment(sheet, 0, "# Sprint types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order");
        row(sheet, 2, "Development Sprint", "#007BFF", "10");
        row(sheet, 3, "Hardening Sprint",   "#FD7E14", "20");
        row(sheet, 4, "Release Sprint",     "#28A745", "30");
        autoSize(sheet, 3);
    }

    private static void createActivityPrioritySheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Activity Priority");
        comment(sheet, 0, "# Activity priorities — shared across the company");
        header(wb, sheet, 1, "Name", "Color", "Sort Order");
        row(sheet, 2, "Critical", "#DC3545", "10");
        row(sheet, 3, "High",     "#FD7E14", "20");
        row(sheet, 4, "Medium",   "#007BFF", "30");
        row(sheet, 5, "Low",      "#6C757D", "40");
        autoSize(sheet, 3);
    }

    private static void createEpicTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Epic Type");
        comment(sheet, 0, "# Epic types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Workflow");
        row(sheet, 2, "Feature Epic",        "#6F42C1", "10", "Agile Sprint Workflow");
        row(sheet, 3, "Technical Epic",      "#007BFF", "20", "Agile Sprint Workflow");
        row(sheet, 4, "Infrastructure Epic", "#28A745", "30", "Agile Sprint Workflow");
        autoSize(sheet, 4);
    }

    private static void createFeatureTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Feature Type");
        comment(sheet, 0, "# Feature types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Workflow");
        row(sheet, 2, "UI Feature",           "#28A745", "10", "Agile Sprint Workflow");
        row(sheet, 3, "Backend Feature",      "#007BFF", "20", "Agile Sprint Workflow");
        row(sheet, 4, "Integration Feature",  "#FD7E14", "30", "Agile Sprint Workflow");
        row(sheet, 5, "Security Feature",     "#DC3545", "40", "Agile Sprint Workflow");
        autoSize(sheet, 4);
    }

    private static void createUserStoryTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "User Story Type");
        comment(sheet, 0, "# User story types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Workflow");
        row(sheet, 2, "Functional Story",     "#007BFF", "10", "Agile Sprint Workflow");
        row(sheet, 3, "Non-Functional Story", "#6C757D", "20", "Agile Sprint Workflow");
        row(sheet, 4, "Spike",               "#FD7E14", "30", "Agile Sprint Workflow");
        autoSize(sheet, 4);
    }

    private static void createRequirementTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Requirement Type");
        comment(sheet, 0, "# Requirement types (generic hierarchy-aware project items)");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow");
        row(sheet, 2, "Business Requirement", "#7B5EA7", "10", "-1", "false", "false", "Default Workflow");
        row(sheet, 3, "Technical Requirement", "#007BFF", "20", "-1", "false", "false", "Default Workflow");
        autoSize(sheet, 7);
    }

    private static void createMilestoneTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Milestone Type");
        comment(sheet, 0, "# Milestone types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow");
        row(sheet, 2, "Release Milestone", "#4B4382", "10", "-1", "false", "false", "Default Workflow");
        row(sheet, 3, "Internal Milestone", "#6F42C1", "20", "-1", "false", "false", "Default Workflow");
        autoSize(sheet, 7);
    }

    private static void createDeliverableTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Deliverable Type");
        comment(sheet, 0, "# Deliverable types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow");
        row(sheet, 2, "Document", "#BC8F8F", "10", "-1", "false", "false", "Default Workflow");
        row(sheet, 3, "Software Release", "#28A745", "20", "-1", "false", "false", "Default Workflow");
        autoSize(sheet, 7);
    }

    private static void createTicketTypeSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Ticket Type");
        comment(sheet, 0, "# Support ticket types");
        header(wb, sheet, 1, "Name", "Color", "Sort Order");
        row(sheet, 2, "Incident",        "#DC3545", "10");
        row(sheet, 3, "Service Request", "#007BFF", "20");
        row(sheet, 4, "Change Request",  "#FD7E14", "30");
        row(sheet, 5, "Problem",         "#6F42C1", "40");
        autoSize(sheet, 3);
    }

    private static void createTicketPrioritySheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Ticket Priority");
        comment(sheet, 0, "# Ticket priorities");
        header(wb, sheet, 1, "Name", "Color", "Sort Order");
        row(sheet, 2, "P1 - Critical", "#DC3545", "10");
        row(sheet, 3, "P2 - High",     "#FD7E14", "20");
        row(sheet, 4, "P3 - Medium",   "#007BFF", "30");
        row(sheet, 5, "P4 - Low",      "#6C757D", "40");
        autoSize(sheet, 3);
    }

    // ── sprint sheet (shared layout) ──────────────────────────────────────────

    private static void createSprintSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Sprint");
        comment(sheet, 0, "# Sprints — time-boxed iterations. Dates: yyyy-MM-dd");
        comment(sheet, 1, "# Sprint Type must match an entry in the Sprint Type sheet");
        header(wb, sheet, 2,
                "Name", "Description", "Status", "Sprint Type", "Start Date", "End Date",
                "Sprint Goal", "Definition of Done", "Velocity");
        row(sheet, 3,
                "Sprint 1 - Foundation",
                "Setup project, auth, and CI/CD. Establish coding standards and deployment pipeline.",
                "Done", "Development Sprint", "2026-01-06", "2026-01-17",
                "Deploy working login and project creation flow to staging",
                "All AC green; code reviewed; no P1 bugs; deployed to staging",
                "42");
        row(sheet, 4,
                "Sprint 2 - Core PLM",
                "Implement Activity, Issue, Meeting, Decision entities with full CRUD and Kanban.",
                "Done", "Development Sprint", "2026-01-20", "2026-01-31",
                "Users can track activities and issues end-to-end",
                "All entity CRUD tested; grid sorting/filtering works; RBAC enforced",
                "38");
        row(sheet, 5,
                "Sprint 3 - Agile Boards",
                "Sprint planning board, backlog, Gantt chart, and agile hierarchy (Epic/Feature/Story).",
                "In Progress", "Development Sprint", "2026-02-03", "2026-02-14",
                "Working agile board with drag-and-drop sprint planning",
                "Sprint board functional; hierarchy visible; story points calculable",
                "");
        row(sheet, 6,
                "Sprint 4 - Reporting & Export",
                "Export to Excel and PDF, dashboard charts, burndown, and KPI widgets.",
                "To Do", "Development Sprint", "2026-02-17", "2026-02-28",
                "Management can export project status and view burndown chart",
                "Excel export for all types; PDF renders; charts load < 2s",
                "");
        autoSize(sheet, 9);
    }

    // ── PLM agile hierarchy sheets ────────────────────────────────────────────

    private static void createPlmEpicSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Epic");
        comment(sheet, 0, "# Epics — top-level product themes (Epic → Feature → User Story). Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Epic Type", "Start Date", "Due Date",
                "Story Points", "Progress %", "Estimated Hours", "Acceptance Criteria", "Assigned To");
        row(sheet, 2,
                "Security & Authentication",
                "End-to-end user authentication and authorization: login, MFA, RBAC, sessions, audit log.",
                "In Progress", "Feature Epic", "2026-01-06", "2026-02-14",
                "55", "60", "120",
                "OWASP top-10 controls in place; pen-test passed; all roles verified on staging",
                "admin");
        row(sheet, 3,
                "Project Management Core",
                "CRUD for all PLM entities (activity, issue, meeting, decision) with Kanban and grids.",
                "In Progress", "Feature Epic", "2026-01-20", "2026-02-14",
                "89", "45", "200",
                "All entity types importable, exportable, filterable by project; grid p95 < 500ms",
                "admin");
        row(sheet, 4,
                "Agile Planning Suite",
                "Sprint management, backlog grooming, Gantt chart, and agile hierarchy navigation.",
                "To Do", "Feature Epic", "2026-02-03", "2026-02-28",
                "120", "0", "160",
                "Sprint board with drag-and-drop; burndown chart; hierarchy drill-down functional",
                "");
        row(sheet, 5,
                "Import / Export Engine",
                "Excel-driven bulk import for all entity types; sample workbook generator; export to Excel/PDF.",
                "In Progress", "Technical Epic", "2026-01-06", "2026-02-28",
                "34", "75", "80",
                "All entity types importable; sample workbook covers every sheet; no data loss on re-import",
                "admin");
        row(sheet, 6,
                "BAB Integration",
                "Connect PLM project management with BAB IoT device monitoring and field bus node data.",
                "To Do", "Infrastructure Epic", "2026-03-03", "2026-03-31",
                "40", "0", "60",
                "Live device status visible in project dashboard; threshold alerts create issues automatically",
                "");
        autoSize(sheet, 11);
    }

    private static void createPlmFeatureSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Feature");
        comment(sheet, 0, "# Features — mid-level delivery units under an Epic. Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Feature Type", "Start Date", "Due Date",
                "Story Points", "Progress %", "Acceptance Criteria", "Assigned To");
        row(sheet, 2,
                "User Authentication",
                "JWT-based login with access/refresh token rotation, MFA via TOTP, remember-me device.",
                "Done", "Security Feature", "2026-01-06", "2026-01-17",
                "21", "100",
                "Login works on all modern browsers; tokens expire correctly; MFA enrollment tested",
                "admin");
        row(sheet, 3,
                "Role-Based Access Control",
                "Company Admin, Project Manager, Developer, Viewer roles; per-project role overrides.",
                "In Progress", "Security Feature", "2026-01-13", "2026-01-31",
                "13", "60",
                "All four roles verified; unauthorised calls return 403; menu items hidden correctly",
                "admin");
        row(sheet, 4,
                "Audit Trail",
                "Immutable log of entity create/update/delete with actor, timestamp, and field diff.",
                "To Do", "Backend Feature", "2026-01-27", "2026-02-07",
                "8", "0",
                "All write operations logged; log is read-only in UI; exports to CSV",
                "");
        row(sheet, 5,
                "Activity CRUD & Kanban",
                "Full activity lifecycle with status transitions, Kanban column drag-and-drop, and filters.",
                "In Progress", "UI Feature", "2026-01-20", "2026-02-07",
                "34", "50",
                "Grid, form, Kanban and Gantt all work; status workflow enforced; multi-user refresh ok",
                "admin");
        row(sheet, 6,
                "Excel Import Engine",
                "Multi-sheet Excel import: type resolution, upsert semantics, dry-run, rollback, job history.",
                "In Progress", "Integration Feature", "2026-01-06", "2026-02-14",
                "21", "75",
                "All entity types importable; no FK resolution errors on re-run; job log persisted",
                "admin");
        autoSize(sheet, 10);
    }

    private static void createPlmUserStorySheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "User Story");
        comment(sheet, 0, "# User Stories — implementation units assigned to sprints. As a [role] I want [goal] so that [benefit].");
        comment(sheet, 1, "# Dates: yyyy-MM-dd");
        header(wb, sheet, 2,
                "Name", "Description", "Status", "User Story Type", "Start Date", "Due Date",
                "Story Points", "Progress %", "Estimated Hours", "Acceptance Criteria", "Assigned To");
        row(sheet, 3,
                "Login with username and password",
                "As a user I want to log in with email + password so that I can access my projects.",
                "Done", "Functional Story", "2026-01-06", "2026-01-10",
                "3", "100", "8",
                "POST /auth/login returns 200 + JWT; invalid credentials return 401; lockout after 5 fails",
                "admin");
        row(sheet, 4,
                "Enable MFA on my account",
                "As a security-conscious user I want to enrol a TOTP authenticator so that my account is 2FA protected.",
                "Done", "Functional Story", "2026-01-08", "2026-01-13",
                "5", "100", "12",
                "QR code displayed; TOTP validates; backup codes generated and hash-stored; recovery flow works",
                "admin");
        row(sheet, 5,
                "Assign roles to users within a project",
                "As a project manager I want to grant/revoke per-project roles so that access is least-privilege.",
                "In Progress", "Functional Story", "2026-01-20", "2026-01-27",
                "5", "80", "10",
                "Role dropdown visible; save persists; affected user sees changed menu on next login",
                "admin");
        row(sheet, 6,
                "Create and update activities via Excel import",
                "As a PM I want to bulk-import activities from Excel so that I can migrate a project plan in minutes.",
                "In Progress", "Functional Story", "2026-01-27", "2026-02-07",
                "8", "60", "16",
                "Import accepts .xlsx; activity rows upserted; dry-run shows preview; errors listed per row",
                "admin");
        row(sheet, 7,
                "View Kanban board with drag-and-drop status change",
                "As a developer I want to drag activity cards between status columns so that I can update status without opening a form.",
                "To Do", "Functional Story", "2026-02-03", "2026-02-10",
                "5", "0", "8",
                "Cards render; drag triggers status update; persists on refresh; works on mobile touch",
                "");
        row(sheet, 8,
                "Export project activities to Excel",
                "As a PM I want to export all activities to Excel so that I can share progress reports with stakeholders.",
                "To Do", "Functional Story", "2026-02-17", "2026-02-21",
                "3", "0", "6",
                "Excel file downloads; includes all visible columns; respects current filter; no broken formulas",
                "");
        row(sheet, 9,
                "Activity grid loads under 500ms for 500 items",
                "As a power user I want the activity grid to load quickly even on large projects so that I stay productive.",
                "To Do", "Non-Functional Story", "2026-02-10", "2026-02-14",
                "3", "0", "8",
                "JMeter baseline: p95 < 500ms with 500 rows and 5 concurrent users",
                "");
        autoSize(sheet, 11);
    }

    private static void createRequirementSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Requirement");
        comment(sheet, 0, "# Requirements — hierarchy-aware project items. Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Requirement Type", "Start Date", "Due Date",
                "Source", "Acceptance Criteria", "Assigned To");
        row(sheet, 2,
                "Login must support MFA",
                "The system shall support MFA (TOTP) for all user accounts.",
                "In Progress", "Technical Requirement", "2026-01-08", "2026-01-31",
                "Security policy", "MFA enrolment and login flows are implemented and tested.",
                "admin");
        row(sheet, 3,
                "Export project status report",
                "The system shall export project items (activities/issues/stories) to Excel.",
                "To Do", "Business Requirement", "2026-02-10", "2026-02-28",
                "Stakeholder request", "Export includes current filters and visible columns.",
                "");
        autoSize(sheet, 9);
    }

    private static void createMilestoneSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Milestone");
        comment(sheet, 0, "# Milestones — key dates/achievements. (Dates are typically derived from linked work items.)");
        header(wb, sheet, 1, "Name", "Description", "Status", "Milestone Type", "Assigned To");
        row(sheet, 2, "MVP released", "First usable end-to-end release deployed to staging.", "To Do", "Release Milestone", "admin");
        row(sheet, 3, "Security review complete", "OWASP checklist + pen-test completed.", "To Do", "Internal Milestone", "");
        autoSize(sheet, 5);
    }

    private static void createDeliverableSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Deliverable");
        comment(sheet, 0, "# Deliverables — artefacts produced by the project.");
        header(wb, sheet, 1, "Name", "Description", "Status", "Deliverable Type", "Assigned To");
        row(sheet, 2, "Architecture document", "System architecture, data model, and service boundaries.", "In Progress", "Document", "admin");
        row(sheet, 3, "Release v1.0", "Packaged build + release notes.", "To Do", "Software Release", "");
        autoSize(sheet, 5);
    }

    // ── PLM flat project item sheets ──────────────────────────────────────────

    static void createActivitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Activity");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        addCommentRow(sheet, 0, commentStyle, "# Activity import template — PLM project: Smart Building IoT Platform v2.0");
        addCommentRow(sheet, 1, commentStyle, "# Status, Activity Type, and Priority must exist first (import those sheets first)");
        addCommentRow(sheet, 2, commentStyle, "# Assigned To = login name. Dates: yyyy-MM-dd or dd/MM/yyyy");
        addCommentRow(sheet, 3, commentStyle, "# Estimated Hours supports Excel formulas (e.g. =4+4)");
        addHeaderRow(sheet, 4, headerStyle,
                "Name", "Description", "Status", "Activity Type", "Priority",
                "Start Date", "Due Date", "Completion Date",
                "Progress %", "Story Points", "Estimated Hours", "Assigned To",
                "Acceptance Criteria", "Notes", "Results");

        addActivityRow(sheet, 5, dateStyle,
                "Design login screen",
                "Create high-fidelity wireframes for login, registration, MFA enrollment, and password reset pages.",
                "Done", "Design", "High",
                date(2026, 1, 6), date(2026, 1, 8), date(2026, 1, 8),
                "100", "3", "8", "admin",
                "All screens reviewed by UX lead; error states and empty states covered",
                "Figma link: figma.company.com/plm-auth", "Approved in design review 2026-01-08");
        addActivityRow(sheet, 6, dateStyle,
                "Implement JWT authentication",
                "Spring Security JWT with access token (15min) + refresh token (7d) + refresh rotation.",
                "Done", "Development", "Critical",
                date(2026, 1, 8), date(2026, 1, 13), date(2026, 1, 12),
                "100", "8", null, "admin",
                "Tokens expire correctly; refresh rotates; logout invalidates; concurrent login capped at 3",
                "Uses jjwt library; keys loaded from application.yml", "Security review passed");
        // demonstrates formula evaluation in the import engine
        sheet.getRow(6).getCell(10).setCellFormula("8+8");

        addActivityRow(sheet, 7, dateStyle,
                "Implement MFA with TOTP",
                "Time-based OTP (RFC 6238) using Google Authenticator protocol. Backup codes hash-stored.",
                "Done", "Development", "High",
                date(2026, 1, 9), date(2026, 1, 14), date(2026, 1, 14),
                "100", "5", "12", "admin",
                "TOTP validates within ±30s window; backup codes bcrypt-hashed; recovery flow tested",
                "Backup code store: separate table, one-use flag", "");
        addActivityRow(sheet, 8, dateStyle,
                "Implement RBAC — four base roles",
                "Company Admin, Project Manager, Developer, Viewer roles with Spring method security @PreAuthorize.",
                "In Progress", "Development", "High",
                date(2026, 1, 20), date(2026, 1, 27), null,
                "70", "5", "10", "admin",
                "All @PreAuthorize gates tested; Viewer cannot mutate; Admin can impersonate per-project",
                "Per-project role overrides deferred to Sprint 2", "");
        addActivityRow(sheet, 9, dateStyle,
                "Build activity CRUD with Vaadin grid",
                "Entity form, detail panel, grid with sort/filter, context menu with 7 actions.",
                "In Progress", "Development", "High",
                date(2026, 1, 20), date(2026, 1, 31), null,
                "50", "8", "20", "admin",
                "Grid loads < 500ms for 200 rows; form validates; all context menu actions functional",
                "Reuse CAbstractMasterDetailView pattern", "");
        addActivityRow(sheet, 10, dateStyle,
                "Implement Excel import engine refactor",
                "Reflection-based CAbstractExcelImportHandler; @AMetaData alias auto-detection; new Epic/Feature handlers.",
                "In Progress", "Development", "Medium",
                date(2026, 1, 27), date(2026, 2, 7), null,
                "75", "8", "20", "admin",
                "All PLM entity types importable; no FK errors on re-import; dry-run preview works",
                "CEpicImportHandler, CFeatureImportHandler added; CMeeting/CDecision migrated to abstract base", "");
        addActivityRow(sheet, 11, dateStyle,
                "Write integration tests for import service",
                "H2 + @SpringBootTest tests covering all sheets; dry-run; rollback-on-error; upsert idempotency.",
                "To Do", "Testing", "Medium",
                date(2026, 2, 3), date(2026, 2, 10), null,
                "0", "5", "12", "",
                "All entity types covered; rollback tested; duplicate import creates no extra rows",
                "", "");
        addActivityRow(sheet, 12, dateStyle,
                "Sprint planning board — drag and drop",
                "Vaadin DnD between sprint backlog and sprint columns; real-time status update.",
                "To Do", "Development", "Medium",
                date(2026, 2, 3), date(2026, 2, 14), null,
                "0", "5", "12", "",
                "Cards render; drag triggers save; multi-user refresh within 5s; mobile touch works",
                "", "");
        addActivityRow(sheet, 13, dateStyle,
                "Gantt chart for project timeline",
                "Read-only Gantt view showing activities, sprints, and milestones sorted by start date.",
                "To Do", "Development", "Low",
                date(2026, 2, 10), date(2026, 2, 21), null,
                "0", "3", "8", "",
                "Timeline renders for 100-item project; zoom in/out; export to PNG",
                "", "");
        addActivityRow(sheet, 14, dateStyle,
                "Document import API for all entity types",
                "Update import architecture doc; add sheet-order reference; add lessons-learned section.",
                "To Do", "Documentation", "Low",
                date(2026, 2, 17), date(2026, 2, 21), null,
                "0", "2", "4", "",
                "Covers every supported sheet; explains alias resolution; has worked example",
                "", "");
        // WHY: G7+14 demonstrates Apache POI formula evaluation in the importer.
        addActivityRow(sheet, 15, dateStyle,
                "Verify formula date evaluation",
                "Due date is set via Excel formula G7+14 to test POI formula evaluation in the importer.",
                "To Do", "Testing", "Low",
                date(2026, 2, 1), date(2026, 2, 1), null,
                "0", "1", "1", "",
                "Due date parsed correctly from formula result", "", "");
        sheet.getRow(15).getCell(6).setCellFormula("G7+14");
        sheet.getRow(15).getCell(6).setCellStyle(dateStyle);

        for (int col = 0; col <= 14; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createPlmMeetingSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Meeting");
        comment(sheet, 0, "# Meetings. Dates: yyyy-MM-dd. Times: HH:mm");
        comment(sheet, 1, "# Participants and Attendees: comma-separated login names (e.g. admin,jane.doe)");
        header(wb, sheet, 2,
                "Name", "Description", "Status", "Meeting Type", "Start Date", "Start Time",
                "End Date", "End Time", "Location", "Agenda", "Minutes", "Participants",
                "Related Activity", "Story Points", "Assigned To");
        row(sheet, 3,
                "Sprint 1 Planning",
                "Select backlog items for Sprint 1; estimate story points; assign owners.",
                "Done", "Sprint Planning", "2026-01-05", "09:00", "2026-01-05", "11:00",
                "Conference Room A",
                "1. Review Definition of Ready\n2. Select sprint backlog\n3. Assign story points\n4. Set sprint goal",
                "Team committed to 42 story points. Sprint goal: deploy working login and project creation to staging.",
                "admin", "Design login screen", "3", "admin");
        row(sheet, 4,
                "Architecture Review — Auth Module",
                "Review JWT + MFA design; security threat model; key rotation strategy.",
                "Done", "Architecture Review", "2026-01-07", "14:00", "2026-01-07", "15:30",
                "Video Call — Zoom",
                "1. JWT threat model review\n2. Refresh token rotation strategy\n3. MFA library selection\n4. Key management",
                "Approved JWT + TOTP approach. Decision: rotate refresh tokens on use. Key rotation via Vault.",
                "admin", "Implement JWT authentication", "5", "admin");
        row(sheet, 5,
                "Sprint 1 Retrospective",
                "What went well, what to improve, action items for Sprint 2.",
                "Done", "Retrospective", "2026-01-17", "16:00", "2026-01-17", "17:00",
                "Conference Room B",
                "1. What went well\n2. What to improve\n3. Action items",
                "Went well: auth delivery on time, design quality high.\nImprove: PR review turnaround (2d avg → 1d target).",
                "admin", "", "2", "admin");
        row(sheet, 6,
                "Client Demo — Sprint 2 Preview",
                "Demo current state of activity grid, Kanban board, and issue management to client.",
                "To Do", "Client Demo", "2026-02-07", "10:00", "2026-02-07", "11:30",
                "Client HQ — Room 201",
                "1. Demo login & RBAC\n2. Activity grid and Kanban\n3. Issue management\n4. Import live demo\n5. Q&A",
                "", "admin", "", "5", "admin");
        autoSize(sheet, 15);
    }

    private static void createPlmDecisionSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Decision");
        comment(sheet, 0, "# Architectural and technical decisions. Dates: yyyy-MM-dd or yyyy-MM-ddTHH:mm");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Decision Type",
                "Estimated Cost", "Implementation Date", "Review Date", "Assigned To");
        row(sheet, 2,
                "Use JWT for stateless authentication",
                "Stateless JWT (access 15min + refresh 7d) preferred over session-based auth for scalability.\n\n"
                + "Rationale: horizontal scaling without shared session store; mobile-friendly; Spring Security native support.\n\n"
                + "Alternatives considered: sessions (rejected: sticky sessions required), OAuth2 (deferred: adds complexity).",
                "Done", "Technology Choice", "0", "2026-01-10", "2026-07-10", "admin");
        row(sheet, 3,
                "PostgreSQL for production, H2 for tests",
                "PostgreSQL 15 is the production database. H2 in-memory for unit/integration tests to enable CI without external DB.\n\n"
                + "Rationale: PostgreSQL JSONB + full-text search needed for future features. H2 identical DDL via Hibernate auto-ddl.\n\n"
                + "Watch out: TRUNCATE...CASCADE behaviour differs between H2 and PG; test both if adding new FK constraints.",
                "Done", "Technology Choice", "0", "2026-01-06", "2026-12-31", "admin");
        row(sheet, 4,
                "Spring Boot 3 + Vaadin 24 as the full-stack",
                "Server-side rendered SPA using Vaadin Flow for the UI layer; Spring Boot 3 for the backend.\n\n"
                + "Rationale: team expertise; type-safe UI components; built-in REST via Spring; JPA + validation native.\n\n"
                + "Trade-off: heavier frontend bundle vs pure API + SPA; acceptable for internal B2B tooling.",
                "Done", "Architecture", "0", "2026-01-01", "2026-06-30", "admin");
        row(sheet, 5,
                "Metadata-driven import — @AMetaData alias auto-detection",
                "All import handlers must extend CAbstractExcelImportHandler so @AMetaData displayNames are automatically "
                + "registered as column aliases.\n\nRationale: prevents alias drift when field names change; developers do not "
                + "need to maintain two lists.\n\nAction: migrate CMeetingImportHandler and CDecisionImportHandler from direct "
                + "IEntityImportHandler — DONE in Sprint 2.",
                "Done", "Architecture", "0", "2026-02-01", "2026-06-30", "admin");
        row(sheet, 6,
                "Add CEpicImportHandler and CFeatureImportHandler",
                "Gap in import coverage: CEpic and CFeature had no handlers despite having all required service infrastructure.\n\n"
                + "Decision: create CEpicImportHandler and CFeatureImportHandler following the CUserStoryImportHandler pattern.\n\n"
                + "Dependency: CEpicTypeImportHandler and CFeatureTypeImportHandler also created so reference data can be seeded.",
                "Done", "Architecture", "0", "2026-02-03", "2026-06-30", "admin");
        row(sheet, 7,
                "Cloud deployment on AWS ECS with Aurora PostgreSQL",
                "Production deployment: AWS ECS Fargate (auto-scaling) + Aurora PostgreSQL Serverless v2.\n\n"
                + "Cost estimate: ~$400/month for 2 ECS tasks + Aurora minimum capacity (0.5 ACU).\n\n"
                + "Rationale: serverless Aurora scales to zero out of hours; ECS eliminates EC2 patching; "
                + "blue/green deploy via CodeDeploy.",
                "To Do", "Budget Approval", "4800", "2026-03-01", "2027-03-01", "admin");
        autoSize(sheet, 8);
    }

    static void createIssueSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Issue");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        addCommentRow(sheet, 0, commentStyle, "# Issues — bugs, improvements, and security findings");
        addCommentRow(sheet, 1, commentStyle, "# Issue Type must match an entry in the Issue Type sheet");
        addCommentRow(sheet, 2, commentStyle, "# Linked Activity: must match an existing activity name in the same project");
        addHeaderRow(sheet, 3, headerStyle, "Name", "Description", "Status", "Issue Type", "Due Date",
                "Linked Activity", "Assigned To");
        addIssueRow(sheet, 4, dateStyle,
                "Login button broken on Safari 16",
                "Login button does not respond to tap on Safari 16 / iPhone 15 Pro (iOS 19).\n"
                + "Repro: 1) Open app on Safari\n2) Enter credentials\n3) Tap Login — no response.\n"
                + "Expected: navigation to dashboard.",
                "In Progress", "Bug", date(2026, 1, 15), "Implement JWT authentication", "admin");
        addIssueRow(sheet, 5, dateStyle,
                "Dashboard slow with 100+ projects",
                "Project list page takes > 8s to load when company has > 100 projects. Query not paginated.",
                "To Do", "Improvement", date(2026, 1, 31), "Build activity CRUD with Vaadin grid", "admin");
        addIssueRow(sheet, 6, dateStyle,
                "Missing export button in activity grid",
                "Activity grid has no Excel export button. PMs need this for weekly status reports.",
                "To Do", "Feature Request", date(2026, 2, 14), "Build activity CRUD with Vaadin grid", "");
        addIssueRow(sheet, 7, dateStyle,
                "JWT secret hardcoded in application.yml",
                "JWT signing secret is a plain string in application.yml instead of being injected from Vault.",
                "In Progress", "Security Vulnerability", date(2026, 1, 20), "Implement JWT authentication", "admin");
        addIssueRow(sheet, 8, dateStyle,
                "TOTP backup codes stored as plaintext",
                "MFA backup codes are stored unhashed in the database. Should be bcrypt-hashed like passwords.",
                "To Do", "Security Vulnerability", date(2026, 1, 25), "Implement MFA with TOTP", "admin");

        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createPlmTicketSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Ticket");
        comment(sheet, 0, "# Support tickets. Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Ticket Type", "Priority",
                "Due Date", "Context Information", "Result", "Assigned To");
        row(sheet, 2,
                "Cannot log in after password reset",
                "User reports: after password reset email, new password rejected on login.\nAccount not in lockout. Reproduced in staging.",
                "In Progress", "Incident", "P2 - High", "2026-01-22",
                "Triggered after Sprint 1 deploy on 2026-01-18", "", "admin");
        row(sheet, 3,
                "Request: bulk import for existing project plan",
                "Customer wants to import their 200-activity MS Project plan. Needs Excel template and guidance.",
                "To Do", "Service Request", "P3 - Medium", "2026-02-14",
                "Customer migrating from MS Project; provided sample file attached to ticket", "", "admin");
        row(sheet, 4,
                "PDF reports show garbled characters for Turkish text",
                "PDF export has encoding issue with Turkish characters (ğ, ş, ı, ç, ö, ü). Affects title and description fields.",
                "To Do", "Bug", "P2 - High", "2026-02-07",
                "Reproduced on Windows + macOS; only affects PDF, not Excel export", "", "");
        autoSize(sheet, 9);
    }

    private static void createPlmCommentSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Comment");
        comment(sheet, 0, "# Comments — attached to any entity that supports them");
        comment(sheet, 1, "# Owner Type: Activity | Issue | Meeting | Decision | Epic | Feature | User Story");
        comment(sheet, 2, "# Author: login name (blank = system/import user). Important: true | false");
        header(wb, sheet, 3, "Owner Type", "Owner Name", "Comment Text", "Author", "Important");
        row(sheet, 4,
                "Activity", "Implement JWT authentication",
                "JWT secret must be rotated before go-live. Added item to security pre-launch checklist.",
                "admin", "true");
        row(sheet, 5,
                "Activity", "Implement JWT authentication",
                "Code review complete. Left two inline suggestions about null-check on claims map — see PR #42.",
                "admin", "false");
        row(sheet, 6,
                "Issue", "Login button broken on Safari 16",
                "Reproduced on iPhone 15 Pro / iOS 19. Root cause: Safari FastClick workaround swallows click event. "
                + "Fix: remove FastClick dependency (PR #48 ready).",
                "admin", "true");
        row(sheet, 7,
                "Issue", "JWT secret hardcoded in application.yml",
                "Created Vault path secret/plm/jwt-secret with 256-bit key. PR #47 ready for review.",
                "admin", "true");
        row(sheet, 8,
                "Decision", "Use JWT for stateless authentication",
                "Verified with security team: refresh token rotation on use is industry best practice. Approved.",
                "admin", "false");
        row(sheet, 9,
                "Epic", "Security & Authentication",
                "Penetration test scheduled for 2026-02-20 with external vendor. "
                + "Ensure all auth tasks done by 2026-02-14.",
                "admin", "true");
        row(sheet, 10,
                "Activity", "Implement RBAC — four base roles",
                "Spring @PreAuthorize annotations added to all mutation endpoints. "
                + "Viewer role verified: cannot call POST/PUT/DELETE.",
                "admin", "false");
        autoSize(sheet, 5);
    }

    private static void createPlmLinkSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Link");
        comment(sheet, 0, "# Links — directed relationships between any two project items");
        comment(sheet, 1, "# Link Type: Related | Implements | Blocks | Duplicates | Depends On");
        comment(sheet, 2, "# Bidirectional=true creates a reverse link automatically");
        header(wb, sheet, 3,
                "Source Type", "Source Name", "Target Type", "Target Name",
                "Link Type", "Description", "Bidirectional");
        row(sheet, 4,
                "Activity", "Implement RBAC — four base roles",
                "Feature", "Role-Based Access Control",
                "Implements", "This activity delivers the RBAC feature", "false");
        row(sheet, 5,
                "Issue", "Login button broken on Safari 16",
                "Activity", "Implement JWT authentication",
                "Related", "Bug introduced during JWT refactor", "false");
        row(sheet, 6,
                "Issue", "JWT secret hardcoded in application.yml",
                "Activity", "Implement JWT authentication",
                "Blocks", "Must fix before activity can be marked Done", "false");
        row(sheet, 7,
                "Decision", "Use JWT for stateless authentication",
                "Activity", "Implement JWT authentication",
                "Implements", "Activity implements this architectural decision", "false");
        row(sheet, 8,
                "Decision", "Add CEpicImportHandler and CFeatureImportHandler",
                "Activity", "Implement Excel import engine refactor",
                "Implements", "Handlers are part of the import engine refactor", "false");
        row(sheet, 9,
                "Epic", "Security & Authentication",
                "Epic", "Import / Export Engine",
                "Related", "Import engine security review needed before auth module sign-off", "false");
        autoSize(sheet, 7);
    }

    // ── BAB flat project item sheets ──────────────────────────────────────────

    private static void createBabActivitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Activity");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        addCommentRow(sheet, 0, commentStyle, "# Activities — BAB Building Automation System v1.0");
        addHeaderRow(sheet, 1, headerStyle,
                "Name", "Description", "Status", "Activity Type", "Priority",
                "Start Date", "Due Date", "Completion Date",
                "Progress %", "Story Points", "Estimated Hours", "Assigned To",
                "Acceptance Criteria", "Notes", "Results");
        addActivityRow(sheet, 2, dateStyle,
                "Design CBabDevice entity and JPA mapping",
                "Schema for CBabDevice: name, MAC, IP, firmware version, health status. Index on company_id + mac.",
                "Done", "Design", "High",
                date(2026, 1, 6), date(2026, 1, 7), date(2026, 1, 7),
                "100", "3", "6", "admin",
                "Flyway migration passes on both H2 and PostgreSQL", "CEntityOfCompany base used", "Reviewed and approved");
        addActivityRow(sheet, 3, dateStyle,
                "Implement CBabDevice service and CRUD view",
                "Spring service + Vaadin grid/form for device management. Health status auto-updates via @Scheduled.",
                "In Progress", "Development", "High",
                date(2026, 1, 8), date(2026, 1, 21), null,
                "70", "8", "20", "admin",
                "Grid paginates 200 devices < 300ms; form validates MAC format; delete cascades to nodes",
                "Schedule interval configurable via application.yml", "");
        addActivityRow(sheet, 4, dateStyle,
                "Implement CAN bus node (CBabNodeCAN)",
                "CAN socket init, bitrate configuration, frame filter, and async frame capture loop.",
                "In Progress", "Development", "Critical",
                date(2026, 1, 20), date(2026, 2, 7), null,
                "30", "13", "32", "admin",
                "10 frames/s captured; bitrate applied without CAN restart; filter masks verified on bench",
                "Uses SocketCAN via JNA; tested on Raspberry Pi CM4", "");
        addActivityRow(sheet, 5, dateStyle,
                "Write unit tests for Modbus service",
                "Mock Modbus TCP server; test register read/write; test polling interval; test all error codes.",
                "To Do", "Testing", "Medium",
                date(2026, 2, 3), date(2026, 2, 10), null,
                "0", "5", "10", "",
                "All register types covered; error codes 0x01–0x0B handled; polling retry on timeout",
                "", "");
        addActivityRow(sheet, 6, dateStyle,
                "Deploy BAB module to staging environment",
                "Docker Compose setup with PostgreSQL + BAB Spring Boot app. Health-check endpoint for CI.",
                "To Do", "Deployment", "Medium",
                date(2026, 2, 10), date(2026, 2, 14), null,
                "0", "3", "6", "",
                "docker-compose up starts all services; /actuator/health returns UP; smoke test passes",
                "", "");
        for (int col = 0; col <= 14; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createBabMeetingSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Meeting");
        comment(sheet, 0, "# Meetings — BAB Building Automation System v1.0. Dates: yyyy-MM-dd. Times: HH:mm");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Meeting Type", "Start Date", "Start Time",
                "End Date", "End Time", "Location", "Agenda", "Minutes", "Participants",
                "Related Activity", "Story Points", "Assigned To");
        row(sheet, 2,
                "CAN Bus Architecture Review",
                "Review CAN socket implementation using SocketCAN + JNA; discuss error handling and reconnect.",
                "Done", "Architecture Review", "2026-01-15", "14:00", "2026-01-15", "16:00",
                "Lab Room — Building 3",
                "1. SocketCAN frame format\n2. JNA binding approach\n3. Error recovery (bus-off)\n4. Thread safety",
                "Approved JNA approach over JNI for maintainability. Decision: bus-off recovery via device restart.",
                "admin", "Implement CAN bus node (CBabNodeCAN)", "5", "admin");
        row(sheet, 3,
                "Sprint 2 Planning — Protocol Integration",
                "Plan Sprint 2: Modbus TCP implementation + ROS2 topic subscription.",
                "To Do", "Sprint Planning", "2026-02-03", "09:00", "2026-02-03", "11:00",
                "Conference Room A",
                "1. Review Sprint 1 velocity\n2. Modbus implementation plan\n3. ROS2 dependencies\n4. Risk review",
                "", "admin", "", "3", "admin");
        autoSize(sheet, 15);
    }

    private static void createBabDecisionSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Decision");
        comment(sheet, 0, "# Decisions — BAB Building Automation System v1.0");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Decision Type",
                "Estimated Cost", "Implementation Date", "Review Date", "Assigned To");
        row(sheet, 2,
                "Use SocketCAN + JNA for CAN bus communication",
                "Native CAN socket access via JNA binding to the Linux SocketCAN API.\n\n"
                + "Rationale: SocketCAN is the de-facto Linux standard; JNA avoids JNI compile step; "
                + "Raspberry Pi CM4 target hardware confirmed SocketCAN support.\n\n"
                + "Alternative rejected: socketcan-j library — unmaintained since 2019.",
                "Done", "Technology Choice", "0", "2026-01-20", "2026-12-31", "admin");
        row(sheet, 3,
                "Deploy IoT gateway as Docker container on ARM64",
                "Package the Spring Boot BAB service as a multi-arch Docker image (amd64 + arm64) for Raspberry Pi CM4 targets.\n\n"
                + "Rationale: consistent deployment; easy OTA update; rollback via image tag.\n\n"
                + "Cost: ~$20/month per gateway node for ARM hosting on customer premises.",
                "Done", "Architecture", "240", "2026-02-01", "2026-12-31", "admin");
        row(sheet, 4,
                "Use InfluxDB for time-series sensor data storage",
                "CAN/Modbus readings stored in InfluxDB 2.x. PostgreSQL used only for device metadata.\n\n"
                + "Rationale: InfluxDB compresses time-series 10× vs PostgreSQL; Flux query language; Grafana connector.\n\n"
                + "Trade-off: additional infrastructure component; mitigated by InfluxDB Cloud option.",
                "To Do", "Architecture", "1200", "2026-02-15", "2026-12-31", "admin");
        autoSize(sheet, 8);
    }

    private static void createBabEpicSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Epic");
        comment(sheet, 0, "# Epics — BAB Building Automation System v1.0. Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Epic Type", "Start Date", "Due Date",
                "Story Points", "Progress %", "Estimated Hours", "Acceptance Criteria", "Assigned To");
        row(sheet, 2,
                "Device Management",
                "Full lifecycle management of IoT gateway devices: CRUD, health monitoring, firmware OTA, factory reset.",
                "In Progress", "Feature Epic", "2026-01-06", "2026-02-28",
                "89", "40", "200",
                "All devices discoverable; OTA update applies without data loss; health poll < 5s",
                "admin");
        row(sheet, 3,
                "Protocol Integration",
                "Support CAN bus, Modbus RTU/TCP, Ethernet raw, and ROS2 topics on the gateway node.",
                "In Progress", "Technical Epic", "2026-01-20", "2026-03-14",
                "120", "25", "280",
                "All 4 protocols reading live data; stored to time-series; errors do not crash gateway",
                "admin");
        row(sheet, 4,
                "Monitoring Dashboard",
                "Real-time device status widget; alert rules; historical trend charts; SLA reporting.",
                "To Do", "Feature Epic", "2026-02-17", "2026-03-28",
                "55", "0", "120",
                "Dashboard auto-refreshes; alert fires within 30s of threshold breach; SLA CSV exportable",
                "");
        autoSize(sheet, 11);
    }

    private static void createBabFeatureSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Feature");
        comment(sheet, 0, "# Features — BAB Building Automation System v1.0. Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "Feature Type", "Start Date", "Due Date",
                "Story Points", "Progress %", "Acceptance Criteria", "Assigned To");
        row(sheet, 2,
                "Device CRUD and Health Check",
                "Register, edit, delete, and view status of IoT gateway devices. Heartbeat ping every 30s.",
                "In Progress", "Backend Feature", "2026-01-06", "2026-01-31",
                "34", "60",
                "Device added via form; heartbeat GREEN/RED visible; delete prompts confirmation",
                "admin");
        row(sheet, 3,
                "CAN Bus Node Configuration",
                "Configure CAN bus bitrate, node ID, and filter masks. Live frame capture for debug.",
                "In Progress", "Integration Feature", "2026-01-20", "2026-02-14",
                "21", "30",
                "CAN node created; bitrate applied; 10 frames captured per second on test bench",
                "admin");
        row(sheet, 4,
                "Modbus RTU / TCP Gateway",
                "Read holding registers, input registers, coils, and discrete inputs via Modbus polling.",
                "To Do", "Integration Feature", "2026-02-03", "2026-02-28",
                "21", "0",
                "All four Modbus register types readable; polling interval 1s–60s; error code logged",
                "");
        row(sheet, 5,
                "Real-Time Device Status Dashboard Widget",
                "Vaadin component showing device name, IP, last seen, protocol, and health status.",
                "To Do", "UI Feature", "2026-02-17", "2026-03-07",
                "13", "0",
                "Widget refreshes every 5s; RED/YELLOW/GREEN indicator; click navigates to device detail",
                "");
        autoSize(sheet, 10);
    }

    private static void createBabUserStorySheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "User Story");
        comment(sheet, 0, "# User Stories — BAB Building Automation System v1.0. Dates: yyyy-MM-dd");
        header(wb, sheet, 1,
                "Name", "Description", "Status", "User Story Type", "Start Date", "Due Date",
                "Story Points", "Progress %", "Estimated Hours", "Acceptance Criteria", "Assigned To");
        row(sheet, 2,
                "Register a new IoT gateway device",
                "As a facilities engineer I want to register a new gateway device so that I can monitor building systems through it.",
                "Done", "Functional Story", "2026-01-06", "2026-01-10",
                "5", "100", "8",
                "Device form saves; device appears in grid; MAC address validated as unique",
                "admin");
        row(sheet, 3,
                "View real-time device health status",
                "As a building operator I want to see device health at a glance so that I can respond to failures immediately.",
                "In Progress", "Functional Story", "2026-01-13", "2026-01-24",
                "5", "60", "10",
                "Status updates within 30s; offline devices shown RED; email alert sent after 5min offline",
                "admin");
        row(sheet, 4,
                "Configure CAN bus node bitrate",
                "As a commissioning engineer I want to set the CAN bus bitrate for each node so that I can match the field device network.",
                "To Do", "Functional Story", "2026-01-27", "2026-02-07",
                "3", "0", "6",
                "Bitrate dropdown (125k/250k/500k/1M); applies on save; confirmed on live test bench",
                "");
        row(sheet, 5,
                "Dashboard shows all device statuses at a glance",
                "As a facility manager I want a single page showing all device health so that I can supervise without drilling into each device.",
                "To Do", "Functional Story", "2026-02-17", "2026-02-28",
                "8", "0", "16",
                "All devices shown; sort by status; filter by protocol; refresh auto; export to CSV",
                "");
        autoSize(sheet, 11);
    }

    private static void createBabIssueSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Issue");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        addCommentRow(sheet, 0, commentStyle, "# Issues — BAB Building Automation System v1.0");
        addHeaderRow(sheet, 1, headerStyle,
                "Name", "Description", "Status", "Issue Type", "Due Date", "Linked Activity", "Assigned To");
        addIssueRow(sheet, 2, dateStyle,
                "CAN bus drops frames under high load",
                "At 1000 frames/s the CAN socket drops ~5% of frames. Tested on Raspberry Pi CM4 with 70% CPU load.",
                "In Progress", "Bug", date(2026, 1, 31), "Implement CAN bus node (CBabNodeCAN)", "admin");
        addIssueRow(sheet, 3, dateStyle,
                "Modbus poll hangs on connection timeout",
                "If Modbus TCP target is unreachable, the poll loop blocks for 30s. Should be configurable < 5s.",
                "To Do", "Bug", date(2026, 2, 14), "Write unit tests for Modbus service", "");
        addIssueRow(sheet, 4, dateStyle,
                "Device health status not updating after network restore",
                "After a device goes offline and reconnects, the health status stays RED until application restart.",
                "To Do", "Bug", date(2026, 2, 7), "Implement CBabDevice service and CRUD view", "admin");
        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createBabCommentSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Comment");
        comment(sheet, 0, "# Comments — BAB Building Automation System v1.0");
        header(wb, sheet, 1, "Owner Type", "Owner Name", "Comment Text", "Author", "Important");
        row(sheet, 2,
                "Issue", "CAN bus drops frames under high load",
                "Profiled with async-profiler: frame drops in JNA buffer copy. "
                + "Switch to direct ByteBuffer allocation to reduce GC pressure.",
                "admin", "true");
        row(sheet, 3,
                "Decision", "Use SocketCAN + JNA for CAN bus communication",
                "Confirmed: SocketCAN kernel module loaded on all production Raspberry Pi CM4 units. No additional kernel config required.",
                "admin", "false");
        row(sheet, 4,
                "Activity", "Implement CAN bus node (CBabNodeCAN)",
                "Test bench: Raspberry Pi CM4 + MCP2515 CAN controller + loopback cable. Frame rate tested to 10k frames/s.",
                "admin", "false");
        autoSize(sheet, 5);
    }

    private static void createBabLinkSheet(final Workbook wb) {
        final Sheet sheet = newSheet(wb, "Link");
        comment(sheet, 0, "# Links — BAB Building Automation System v1.0");
        header(wb, sheet, 1,
                "Source Type", "Source Name", "Target Type", "Target Name",
                "Link Type", "Description", "Bidirectional");
        row(sheet, 2,
                "Issue", "CAN bus drops frames under high load",
                "Activity", "Implement CAN bus node (CBabNodeCAN)",
                "Blocks", "Must fix before activity can be marked Done", "false");
        row(sheet, 3,
                "Decision", "Use SocketCAN + JNA for CAN bus communication",
                "Activity", "Implement CAN bus node (CBabNodeCAN)",
                "Implements", "Activity implements this technical decision", "false");
        row(sheet, 4,
                "Decision", "Use InfluxDB for time-series sensor data storage",
                "Feature", "CAN Bus Node Configuration",
                "Related", "CAN data lands in InfluxDB; schema must be agreed before feature closes", "false");
        autoSize(sheet, 7);
    }

    // ── low-level row builders ────────────────────────────────────────────────

    static void addRow(final Sheet sheet, final int rowIndex, final CellStyle style, final String... values) {
        final Row r = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            final var cell = r.createCell(i);
            cell.setCellValue(values[i] != null ? values[i] : "");
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    // private helpers used by the cleaner sheet builders

    private static Sheet newSheet(final Workbook wb, final String name) {
        return wb.createSheet(name);
    }

    private static void comment(final Sheet sheet, final int rowIndex, final String text) {
        addRow(sheet, rowIndex, createCommentStyle(sheet.getWorkbook()), text);
    }

    private static void header(final Workbook wb, final Sheet sheet, final int rowIndex, final String... values) {
        addRow(sheet, rowIndex, createHeaderStyle(wb), values);
    }

    private static void row(final Sheet sheet, final int rowIndex, final String... values) {
        addRow(sheet, rowIndex, null, values);
    }

    private static void addCommentRow(final Sheet sheet, final int rowIndex, final CellStyle style, final String text) {
        final Row r = sheet.createRow(rowIndex);
        final var cell = r.createCell(0);
        cell.setCellValue(text);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private static void addHeaderRow(final Sheet sheet, final int rowIndex, final CellStyle style, final String... values) {
        addRow(sheet, rowIndex, style, values);
    }

    private static void addActivityRow(final Sheet sheet, final int rowIndex, final CellStyle dateStyle,
            final String name, final String description, final String status, final String activityType,
            final String priority, final LocalDate startDate, final LocalDate dueDate,
            final LocalDate completionDate, final String progressPercentage, final String storyPoint,
            final String estimatedHours, final String assignedTo, final String acceptanceCriteria,
            final String notes, final String results) {
        final Row r = sheet.createRow(rowIndex);
        r.createCell(0).setCellValue(name);
        r.createCell(1).setCellValue(description != null ? description : "");
        r.createCell(2).setCellValue(status != null ? status : "");
        r.createCell(3).setCellValue(activityType != null ? activityType : "");
        r.createCell(4).setCellValue(priority != null ? priority : "");
        setDate(r, 5, startDate, dateStyle);
        setDate(r, 6, dueDate, dateStyle);
        setDate(r, 7, completionDate, dateStyle);
        r.createCell(8).setCellValue(progressPercentage != null ? progressPercentage : "");
        r.createCell(9).setCellValue(storyPoint != null ? storyPoint : "");
        final var hoursCell = r.createCell(10);
        if (estimatedHours != null) {
            hoursCell.setCellValue(estimatedHours);
        }
        r.createCell(11).setCellValue(assignedTo != null ? assignedTo : "");
        r.createCell(12).setCellValue(acceptanceCriteria != null ? acceptanceCriteria : "");
        r.createCell(13).setCellValue(notes != null ? notes : "");
        r.createCell(14).setCellValue(results != null ? results : "");
    }

    private static void addIssueRow(final Sheet sheet, final int rowIndex, final CellStyle dateStyle,
            final String name, final String description, final String status, final String issueType,
            final LocalDate dueDate, final String linkedActivity, final String assignedTo) {
        final Row r = sheet.createRow(rowIndex);
        r.createCell(0).setCellValue(name);
        r.createCell(1).setCellValue(description != null ? description : "");
        r.createCell(2).setCellValue(status != null ? status : "");
        r.createCell(3).setCellValue(issueType != null ? issueType : "");
        setDate(r, 4, dueDate, dateStyle);
        r.createCell(5).setCellValue(linkedActivity != null ? linkedActivity : "");
        r.createCell(6).setCellValue(assignedTo != null ? assignedTo : "");
    }

    private static void setDate(final Row row, final int col, final LocalDate date, final CellStyle dateStyle) {
        final var cell = row.createCell(col);
        cell.setCellStyle(dateStyle);
        if (date != null) {
            cell.setCellValue(java.sql.Date.valueOf(date));
        }
    }

    private static LocalDate date(final int year, final int month, final int day) {
        return LocalDate.of(year, month, day);
    }

    private static void autoSize(final Sheet sheet, final int columnCount) {
        for (int col = 0; col < columnCount; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    /** Command-line entry point: generates sample workbooks in current directory. */
    public static void main(final String[] args) throws IOException {
        try (final FileOutputStream out = new FileOutputStream("sample_plm_project.xlsx")) {
            createPlmProjectWorkbook().write(out);
        }
        System.out.println("Generated: sample_plm_project.xlsx");
        try (final FileOutputStream out = new FileOutputStream("sample_bab_project.xlsx")) {
            createBabProjectWorkbook().write(out);
        }
        System.out.println("Generated: sample_bab_project.xlsx");
        try (final FileOutputStream out = new FileOutputStream("sample_import.xlsx")) {
            writeSampleWorkbook(out);
        }
        System.out.println("Generated: sample_import.xlsx (legacy two-sheet sample)");
    }
}
