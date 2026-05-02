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
        final Workbook wb = new XSSFWorkbook();
        createStatusSheet(wb);
        createActivityTypeSheet(wb);
        createIssueTypeSheet(wb);
        CSampleImportExcelGenerator.createActivitySheet(wb);
        CSampleImportExcelGenerator.createIssueSheet(wb);
        return wb;
    }

    /** Writes the bootstrapping workbook to the given output stream and closes the workbook. */
    public static void writeSystemInitWorkbook(final OutputStream out) throws IOException {
        try (final Workbook wb = createSystemInitWorkbook()) {
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
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Name (required), Color");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Name", "Color");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Design", "#5E35B1");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Development", "#1565C0");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Testing", "#00897B");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Documentation", "#6D4C41");

        for (int col = 0; col <= 1; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createIssueTypeSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Issue Type");
        final CellStyle headerStyle = CSampleImportExcelGenerator.createHeaderStyle(wb);
        final CellStyle commentStyle = CSampleImportExcelGenerator.createCommentStyle(wb);

        CSampleImportExcelGenerator.addRow(sheet, 0, commentStyle, "# Issue Types");
        CSampleImportExcelGenerator.addRow(sheet, 1, commentStyle, "# Columns: Name (required), Color");
        CSampleImportExcelGenerator.addRow(sheet, 2, headerStyle, "Name", "Color");

        CSampleImportExcelGenerator.addRow(sheet, 3, null, "Bug", "#D32F2F");
        CSampleImportExcelGenerator.addRow(sheet, 4, null, "Improvement", "#1976D2");
        CSampleImportExcelGenerator.addRow(sheet, 5, null, "Task", "#455A64");
        CSampleImportExcelGenerator.addRow(sheet, 6, null, "Feature Request", "#7B1FA2");

        for (int col = 0; col <= 1; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    /** Command-line entry point: generates system_init.xlsx in current directory. */
    public static void main(final String[] args) throws IOException {
        try (final FileOutputStream out = new FileOutputStream("system_init.xlsx")) {
            writeSystemInitWorkbook(out);
        }
        System.out.println("Generated: system_init.xlsx");
    }
}
