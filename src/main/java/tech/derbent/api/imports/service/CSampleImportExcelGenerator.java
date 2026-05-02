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
 * Generates a sample Excel workbook that demonstrates the import format.
 * Used for documentation and as a downloadable template from the import view.
 */
public final class CSampleImportExcelGenerator {

    private CSampleImportExcelGenerator() { /* utility class */ }

    /** Creates a sample workbook with Activity and Issue sheets pre-filled with demo data. */
    public static Workbook createSampleWorkbook() {
        final Workbook wb = new XSSFWorkbook();
        createActivitySheet(wb);
        createIssueSheet(wb);
        return wb;
    }

    /** Writes the sample workbook to the given output stream and closes the workbook. */
    public static void writeSampleWorkbook(final OutputStream out) throws IOException {
        try (final Workbook wb = createSampleWorkbook()) {
            wb.write(out);
        }
    }

    private static CellStyle createHeaderStyle(final Workbook wb) {
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

    private static CellStyle createCommentStyle(final Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle createDateStyle(final Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy-mm-dd"));
        return style;
    }

    private static void createActivitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Activity");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        // Comment rows (start with #)
        addRow(sheet, 0, commentStyle, "# Activity import template");
        addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Description, Status, Activity Type, Due Date, Estimated Hours, Assigned To");
        addRow(sheet, 2, commentStyle,
                "# Tip: Due Date can be an Excel date cell; Estimated Hours can be a formula (e.g. =4+4). ");
        addRow(sheet, 3, commentStyle, "# Status and Activity Type must already exist in the system");

        // Header (intentionally includes extra spaces to test normalization)
        addRow(sheet, 4, headerStyle, "Name", "Description", "Status", "Activity Type", "Due   Date",
                "Estimated   Hours", "Assigned To");

        // Sample rows (use initializer-backed activity types; avoid non-existing types like 'Meeting')
        addActivityRow(sheet, 5, dateStyle,
                "Design login screen",
                "Create wireframes for the login page\n- include MFA enrollment\n- validate error states",
                "To Do",
                "Design",
                LocalDate.of(2025, 6, 15),
                "8",
                "");

        addActivityRow(sheet, 6, dateStyle,
                "Implement authentication",
                "JWT-based auth implementation (access + refresh tokens)",
                "In Progress",
                "Development",
                LocalDate.of(2025, 6, 20),
                null,
                "admin");
        // formula in Estimated Hours
        sheet.getRow(6).getCell(5).setCellFormula("4+4");

        addActivityRow(sheet, 7, dateStyle,
                "Write unit tests",
                "Cover authentication service with tests; include edge cases (Safari, iOS)",
                "To Do",
                "Testing",
                LocalDate.of(2025, 6, 25),
                "4.5",
                "qa");

        addActivityRow(sheet, 8, dateStyle,
                "Document rollout plan",
                "Release notes + runbook update",
                "",
                "Documentation",
                LocalDate.of(2025, 6, 18),
                "2",
                "");

        // Auto-size columns
        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void createIssueSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Issue");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        addRow(sheet, 0, commentStyle, "# Issue import template");
        addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Description, Status, Issue Type, Due Date");
        addRow(sheet, 2, commentStyle,
                "# Issue Type must match an existing issue type (e.g. Bug, Improvement, Task, Feature Request, Documentation)");

        // Header (intentionally includes extra spaces)
        addRow(sheet, 3, headerStyle, "Name", "Description", "Status", "Issue  Type", "Due   Date");

        addIssueRow(sheet, 4, dateStyle,
                "Login button broken on Safari",
                "Login button does not respond on Safari 16\nRepro: iPhone 15 Pro / iOS 19",
                "To Do",
                "Bug",
                LocalDate.of(2025, 6, 10));

        addIssueRow(sheet, 5, dateStyle,
                "Improve dashboard load time",
                "Dashboard takes > 5s to load with 100+ projects",
                "To Do",
                "Improvement",
                LocalDate.of(2025, 6, 30));

        addIssueRow(sheet, 6, dateStyle,
                "Missing export button in reports",
                "Add CSV export to all report pages",
                "To Do",
                "Feature Request",
                LocalDate.of(2025, 7, 5));

        for (int col = 0; col <= 4; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private static void addRow(final Sheet sheet, final int rowIndex, final CellStyle style, final String... values) {
        final Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            final var cell = row.createCell(i);
            cell.setCellValue(values[i]);
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    private static void addActivityRow(final Sheet sheet, final int rowIndex, final CellStyle dateStyle,
            final String name, final String description, final String status, final String activityType,
            final LocalDate dueDate, final String estimatedHours, final String assignedTo) {
        final Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue(status);
        row.createCell(3).setCellValue(activityType);
        final var dueCell = row.createCell(4);
        dueCell.setCellValue(java.sql.Date.valueOf(dueDate));
        dueCell.setCellStyle(dateStyle);
        final var hoursCell = row.createCell(5);
        if (estimatedHours != null) {
            hoursCell.setCellValue(estimatedHours);
        }
        row.createCell(6).setCellValue(assignedTo);
    }

    private static void addIssueRow(final Sheet sheet, final int rowIndex, final CellStyle dateStyle,
            final String name, final String description, final String status, final String issueType,
            final LocalDate dueDate) {
        final Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue(status);
        row.createCell(3).setCellValue(issueType);
        final var dueCell = row.createCell(4);
        dueCell.setCellValue(java.sql.Date.valueOf(dueDate));
        dueCell.setCellStyle(dateStyle);
    }

    /** Command-line entry point: generates sample.xlsx in current directory. */
    public static void main(final String[] args) throws IOException {
        try (final FileOutputStream out = new FileOutputStream("sample_import.xlsx")) {
            writeSampleWorkbook(out);
        }
        System.out.println("Generated: sample_import.xlsx");
    }
}
