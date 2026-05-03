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
        createGridEntitySheet(wb);
        createPageEntitySheet(wb);
        if (!minimal) {
            CSampleImportExcelGenerator.createActivitySheet(wb);
            CSampleImportExcelGenerator.createIssueSheet(wb);
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

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Design", "#5E35B1", "10", "-1", "false", "false", "Default Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Development", "#1565C0", "20", "-1", "false", "false", "Default Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Testing", "#00897B", "30", "-1", "false", "false", "Default Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Documentation", "#6D4C41", "40", "-1", "false", "false", "Default Workflow");

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

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Bug", "#D32F2F", "10", "-1", "false", "false", "Default Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Improvement", "#1976D2", "20", "-1", "false", "false", "Default Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Task", "#455A64", "30", "-1", "false", "false", "Default Workflow");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Feature Request", "#7B1FA2", "40", "-1", "false", "false", "Default Workflow");

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

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Default Workflow");

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

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Default Workflow", "To Do", "In Progress", "true", "");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Default Workflow", "In Progress", "Done", "false", "");

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
