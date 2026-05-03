package tech.derbent.api.imports.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generates a multi-sheet workbook intended for bootstrapping a fresh system.
 *
 * Sheet order matters because the importer processes sheets in workbook order.
 * We put reference/type data first (Statuses, Activity Types, Issue Types) so later item sheets can resolve relations.
 */
public final class CSystemInitExcelGenerator {

    private CSystemInitExcelGenerator() { /* utility class */ }

    /** Creates a bootstrapping workbook: reference data sheets first, then sample items. */
    public static Workbook createSystemInitWorkbook() {
        return createSystemInitWorkbook(false);
    }

    /**
     * @param minimal when true, excludes heavy sample item sheets and keeps only core reference/view config.
     */
    public static Workbook createSystemInitWorkbook(final boolean minimal) {
        final Workbook wb = new XSSFWorkbook();
        createStatusSheet(wb);
        createWorkflowEntitySheet(wb);
        createWorkflowStatusRelationSheet(wb);
        createActivityPrioritySheet(wb);
        createActivityTypeSheet(wb);
        createIssueTypeSheet(wb);
        createMeetingTypeSheet(wb);
        createDecisionTypeSheet(wb);
        createGridEntitySheet(wb);
        createPageEntitySheet(wb);
        if (!minimal) {
            CSampleImportExcelGenerator.createActivitySheet(wb);
            CSampleImportExcelGenerator.createIssueSheet(wb);
            createMeetingSheet(wb);
            createDecisionSheet(wb);
            createCommentSheet(wb);
            createLinkSheet(wb);
            createAgileParentRelationSheet(wb);
        }
        return wb;
    }

    /** Writes the bootstrapping workbook to the given output stream and closes the workbook. */
    public static void writeSystemInitWorkbook(final OutputStream out) throws IOException {
        writeSystemInitWorkbook(out, false);
    }

    public static void writeSystemInitWorkbook(final OutputStream out, final boolean minimal) throws IOException {
        try (final Workbook wb = createSystemInitWorkbook(minimal)) {
            wb.write(out);
        }
    }

    private static void createStatusSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Status");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Project Item Statuses (used by Activities, Issues, etc.)");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Name (required), Final Status (true/false), Color, Icon");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Name", "Final Status", "Color", "Icon");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "To Do", "false", "#1976D2", "vaadin:flag");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "In Progress", "false", "#F9A825", "vaadin:flag");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Done", "true", "#2E7D32", "vaadin:flag");

        for (int col = 0; col <= 3; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createActivityTypeSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Activity Type");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Activity Types");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Color, Sort Order, Level, Can Have Children, Non Deletable, Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Design", "#5E35B1", "10", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Development", "#1565C0", "20", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Testing", "#00897B", "30", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Documentation", "#6D4C41", "40", "-1", "false", "false", "Agile Item Workflow");

        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createIssueTypeSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Issue Type");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Issue Types");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Color, Sort Order, Level, Can Have Children, Non Deletable, Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Bug", "#D32F2F", "10", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Improvement", "#1976D2", "20", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Task", "#455A64", "30", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Feature Request", "#7B1FA2", "40", "-1", "false", "false", "Agile Item Workflow");

        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createWorkflowEntitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Workflow Entity");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Workflows");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Name (required)");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Name");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Default Workflow");

        sheet.autoSizeColumn(0);
    }

    private static void createWorkflowStatusRelationSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Workflow Status Relation");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Workflow Status Transitions");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Workflow (required), From Status (required), To Status (required), Is Initial Status, Roles (csv)");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Workflow", "From Status", "To Status", "Is Initial Status", "Roles");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Agile Item Workflow", "To Do", "In Progress", "true", "");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Agile Item Workflow", "In Progress", "Done", "false", "");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Default Workflow", "To Do", "In Progress", "true", "");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Default Workflow", "In Progress", "Done", "false", "");

        for (int col = 0; col <= 4; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createActivityPrioritySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Activity Priority");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Activity Priorities");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Color, Sort Order, Priority Level, Is Default");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Color", "Sort Order", "Priority Level", "Is Default");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Critical", "#B71C1C", "10", "1", "false");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "High", "#D32F2F", "20", "2", "false");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Medium", "#F9A825", "30", "3", "true");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Low", "#1976D2", "40", "4", "false");

        for (int col = 0; col <= 4; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createGridEntitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Grid Entity");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Grid Entities (view configuration)");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Data Service Bean (required), Column Fields (csv), Editable Column Fields (csv), None Grid");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Name", "Data Service Bean", "Column Fields",
                "Editable Column Fields", "None Grid");

        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Activities Grid", "CActivityService", "id,name,status,dueDate,assignedTo", "name,status,dueDate", "false");
        CSampleImportExcelGenerator.addRow(sheet, 4, null,
                "Issues Grid", "CIssueService", "id,name,status,dueDate,assignedTo,linkedActivity", "name,status,dueDate", "false");

        for (int col = 0; col <= 4; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createPageEntitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Page Entity");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Page Entities (navigation pages)");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name, Menu Title, Menu Order, Page Title, Page Service, Icon, Requires Authentication, Grid Entity, Content");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Name", "Menu Title", "Menu Order", "Page Title", "Page Service", "Icon",
                "Requires Authentication", "Grid Entity", "Content");

        // WHY: menu titles must be unique within project; pick stable names under the "Project" root.
        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Activities Page", "Project.Activities (Excel)", "10.201", "Activities (Excel)", "CPageServiceActivity", "vaadin:tasks", "true",
                "Activities Grid", "");
        CSampleImportExcelGenerator.addRow(sheet, 4, null,
                "Issues Page", "Project.Issues (Excel)", "10.202", "Issues (Excel)", "CPageServiceIssue", "vaadin:bug", "true",
                "Issues Grid", "");

        for (int col = 0; col <= 8; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createMeetingTypeSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Meeting Type");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Meeting Types");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Color, Sort Order, Level, Can Have Children, Non Deletable, Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Sprint Planning", "#DAA520", "10", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Sprint Retrospective", "#DAA520", "20", "-1", "false", "false", "Agile Item Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Project Review", "#DAA520", "30", "-1", "false", "false", "Agile Item Workflow");

        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createDecisionTypeSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Decision Type");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Decision Types");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Color, Sort Order, Level, Can Have Children, Non Deletable, Workflow, Requires Approval");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Color", "Sort Order", "Level", "Can Have Children", "Non Deletable", "Workflow", "Requires Approval");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Technical", "#91856C", "10", "-1", "false", "false", "Agile Item Workflow", "false");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Strategic", "#91856C", "20", "-1", "false", "false", "Agile Item Workflow", "true");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Budget", "#91856C", "30", "-1", "false", "false", "Agile Item Workflow", "true");

        for (int col = 0; col <= 7; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createMeetingSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Meeting");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Meeting import sheet (project items)");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Description, Status, Meeting Type, Start Date, Start Time, End Date, End Time, Location, Agenda, Minutes, Linked Element, Participants, Attendees, Related Activity, Story Points, Assigned To");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Description", "Status", "Meeting Type", "Start Date", "Start Time", "End Date", "End Time",
                "Location", "Agenda", "Minutes", "Linked Element", "Participants", "Attendees", "Related Activity", "Story Points", "Assigned To");

        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Sprint Planning - Week 24",
                "Plan sprint scope and break down identity rollout work",
                "To Do",
                "Sprint Planning",
                "2025-06-10",
                "09:00",
                "2025-06-10",
                "10:30",
                "Zoom",
                "- Review backlog\n- Pick sprint goal\n- Assign owners",
                "",
                "https://confluence.local/sprint24",
                "admin",
                "admin",
                "Implement authentication",
                "2",
                "admin");

        for (int col = 0; col <= 16; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createDecisionSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Decision");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Decision import sheet (project items)");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Description, Status, Decision Type, Estimated Cost, Implementation Date, Review Date, Assigned To");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Name", "Description", "Status", "Decision Type", "Estimated Cost", "Implementation Date", "Review Date", "Assigned To");

        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Choose auth provider",
                "Pick the identity provider and document migration constraints",
                "In Progress",
                "Technical",
                "1200",
                "2025-06-18T09:00",
                "2025-06-25T09:00",
                "admin");

        for (int col = 0; col <= 7; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createCommentSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Comment");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Comments (child entities)\n# Owner Type/Name resolve entities by name in the active project");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Owner Type, Owner Name, Comment Text, Author, Important");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Owner Type", "Owner Name", "Comment Text", "Author", "Important");

        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Activity", "Implement authentication", "Keep refresh token rotation in scope.", "admin", "true");
        CSampleImportExcelGenerator.addRow(sheet, 4, null,
                "Meeting", "Sprint Planning - Week 24", "Agenda finalized; focus on MFA story first.", "admin", "false");
        CSampleImportExcelGenerator.addRow(sheet, 5, null,
                "Decision", "Choose auth provider", "Compare SSO support and audit logging.", "admin", "false");

        for (int col = 0; col <= 4; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createLinkSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Link");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Links (child entities)\n# Bidirectional=true creates a reverse link if target supports links");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Source Type, Source Name, Target Type, Target Name, Link Type, Description, Bidirectional");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle,
                "Source Type", "Source Name", "Target Type", "Target Name", "Link Type", "Description", "Bidirectional");

        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Decision", "Choose auth provider",
                "Activity", "Implement authentication",
                "Depends On",
                "Implementation must follow the provider choice",
                "true");

        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createAgileParentRelationSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Agile Parent Relation");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle,
                "# Agile hierarchy links (Owner -> Parent)\n# Parent may be blank to explicitly mark the owner as a root item");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Owner Type, Owner Name, Parent Type, Parent Name");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Owner Type", "Owner Name", "Parent Type", "Parent Name");

        // WHY: UserStory sample items are still created by code initializers; Excel sets the leaf relations to those stable names.
        CSampleImportExcelGenerator.addRow(sheet, 3, null,
                "Activity", "Design login screen",
                "User Story", "As an account owner I can enroll MFA for my workspace admins");
        CSampleImportExcelGenerator.addRow(sheet, 4, null,
                "Meeting", "Sprint Planning - Week 24",
                "User Story", "As an account owner I can enroll MFA for my workspace admins");
        CSampleImportExcelGenerator.addRow(sheet, 5, null,
                "Decision", "Choose auth provider",
                "User Story", "As an account owner I can enroll MFA for my workspace admins");

        for (int col = 0; col <= 3; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    /** Command-line entry point: generates system_init.xlsx and system_init_min.xlsx in current directory. */
    public static void main(final String[] args) throws IOException {
        try (final FileOutputStream out = new FileOutputStream("system_init.xlsx")) {
            writeSystemInitWorkbook(out, false);
        }
        try (final FileOutputStream out = new FileOutputStream("system_init_min.xlsx")) {
            writeSystemInitWorkbook(out, true);
        }
        System.out.println("Generated: system_init.xlsx");
        System.out.println("Generated: system_init_min.xlsx");
    }
}
