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

    static void createActivitySheet(final Workbook wb) {
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
        addRow(sheet, 4, headerStyle,
                "Name", "Description", "Status", "Activity Type", "Priority",
                "Start   Date", "Due   Date", "Completion Date",
                "Progress   %", "Story Points", "Estimated   Hours", "Assigned To",
                "Acceptance Criteria", "Notes", "Results");

        // Sample rows (use initializer-backed activity types; avoid non-existing types like 'Meeting')
        addActivityRow(sheet, 5, dateStyle,
                "Design login screen",
                "Create wireframes for the login page\n- include MFA enrollment\n- validate error states",
                "To Do",
                "Design",
                "High",
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 6, 15),
                null,
                "0",
                "3",
                "8",
                "",
                "UI matches specs and error states are covered",
                "Created from Excel system init",
                "");

        addActivityRow(sheet, 6, dateStyle,
                "Implement authentication",
                "JWT-based auth implementation (access + refresh tokens)",
                "In Progress",
                "Development",
                "Critical",
                LocalDate.of(2025, 6, 12),
                LocalDate.of(2025, 6, 20),
                null,
                "35",
                "8",
                null,
                "admin",
                "Tokens validated; refresh rotation; logout invalidates tokens",
                "Security review pending",
                "");
        // formula in Estimated Hours
        sheet.getRow(6).getCell(10).setCellFormula("4+4");

        addActivityRow(sheet, 7, dateStyle,
                "Write unit tests",
                "Cover authentication service with tests; include edge cases (Safari, iOS)",
                "To Do",
                "Testing",
                "Medium",
                LocalDate.of(2025, 6, 15),
                LocalDate.of(2025, 6, 25),
                null,
                "0",
                "5",
                "4.5",
                "admin",
                "Happy path + edge cases pass on CI",
                "Use Playwright + unit test mix",
                "");
        // WHY: keep the default template importable on top of sample data; 'qa' user may not exist in all environments.

        addActivityRow(sheet, 8, dateStyle,
                "Document rollout plan",
                "Release notes + runbook update",
                "",
                "Documentation",
                "Low",
                LocalDate.of(2025, 6, 16),
                LocalDate.of(2025, 6, 18),
                null,
                "0",
                "2",
                "2",
                "",
                "Runbook includes rollback steps",
                "",
                "" );

        // Formula-based due date (tests formula evaluation + date formatting)
        addActivityRow(sheet, 9, dateStyle,
                "Verify formula date",
                "Due date is generated via formula: G7+14 (safe POI-evaluable date arithmetic)",
                "To Do",
                "Testing",
                "Low",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 1),
                null,
                "0",
                "1",
                "1",
                "",
                "Date formula evaluated on import",
                "",
                "");
        // WHY: Apache POI does not support every Excel function (e.g. TODAY) equally; date math is reliable.
        sheet.getRow(9).getCell(6).setCellFormula("G7+14");
        sheet.getRow(9).getCell(6).setCellStyle(dateStyle);

        // Auto-size columns
        for (int col = 0; col <= 14; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    static void createIssueSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Issue");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        final CellStyle dateStyle = createDateStyle(wb);

        addRow(sheet, 0, commentStyle, "# Issue import template");
        addRow(sheet, 1, commentStyle,
                "# Columns: Name (required), Description, Status, Issue Type, Due Date, Linked Activity, Assigned To");
        addRow(sheet, 2, commentStyle,
                "# Issue Type must match an existing issue type (e.g. Bug, Improvement, Task, Feature Request, Documentation)");

        // Header (intentionally includes extra spaces)
        addRow(sheet, 3, headerStyle, "Name", "Description", "Status", "Issue  Type", "Due   Date", "Linked Activity", "Assigned To");

        addIssueRow(sheet, 4, dateStyle,
                "Login button broken on Safari",
                "Login button does not respond on Safari 16\nRepro: iPhone 15 Pro / iOS 19",
                "To Do",
                "Bug",
                LocalDate.of(2025, 6, 10),
                "Implement authentication",
                "admin");

        addIssueRow(sheet, 5, dateStyle,
                "Improve dashboard load time",
                "Dashboard takes > 5s to load with 100+ projects",
                "To Do",
                "Improvement",
                LocalDate.of(2025, 6, 30),
                "Write unit tests",
                "admin");

        addIssueRow(sheet, 6, dateStyle,
                "Missing export button in reports",
                "Add CSV export to all report pages",
                "To Do",
                "Feature Request",
                LocalDate.of(2025, 7, 5),
                "Document rollout plan",
                "");

        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    static void addRow(final Sheet sheet, final int rowIndex, final CellStyle style, final String... values) {
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
            final String name, final String description, final String status, final String activityType, final String priority,
            final LocalDate startDate, final LocalDate dueDate, final LocalDate completionDate,
            final String progressPercentage, final String storyPoint,
            final String estimatedHours, final String assignedTo,
            final String acceptanceCriteria, final String notes, final String results) {
        final Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue(status);
        row.createCell(3).setCellValue(activityType);
        row.createCell(4).setCellValue(priority);

        final var startCell = row.createCell(5);
        startCell.setCellStyle(dateStyle);
        if (startDate != null) {
            startCell.setCellValue(java.sql.Date.valueOf(startDate));
        }

        final var dueCell = row.createCell(6);
        dueCell.setCellStyle(dateStyle);
        if (dueDate != null) {
            dueCell.setCellValue(java.sql.Date.valueOf(dueDate));
        }

        final var completionCell = row.createCell(7);
        completionCell.setCellStyle(dateStyle);
        if (completionDate != null) {
            completionCell.setCellValue(java.sql.Date.valueOf(completionDate));
        }

        row.createCell(8).setCellValue(progressPercentage != null ? progressPercentage : "");
        row.createCell(9).setCellValue(storyPoint != null ? storyPoint : "");

        final var hoursCell = row.createCell(10);
        if (estimatedHours != null) {
            hoursCell.setCellValue(estimatedHours);
        }
        row.createCell(11).setCellValue(assignedTo != null ? assignedTo : "");
        row.createCell(12).setCellValue(acceptanceCriteria != null ? acceptanceCriteria : "");
        row.createCell(13).setCellValue(notes != null ? notes : "");
        row.createCell(14).setCellValue(results != null ? results : "");
    }

    private static void addIssueRow(final Sheet sheet, final int rowIndex, final CellStyle dateStyle,
            final String name, final String description, final String status, final String issueType,
            final LocalDate dueDate, final String linkedActivity, final String assignedTo) {
        final Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue(description);
        row.createCell(2).setCellValue(status);
        row.createCell(3).setCellValue(issueType);
        final var dueCell = row.createCell(4);
        dueCell.setCellValue(java.sql.Date.valueOf(dueDate));
        dueCell.setCellStyle(dateStyle);
        row.createCell(5).setCellValue(linkedActivity);
        row.createCell(6).setCellValue(assignedTo);
    }

    /** Command-line entry point: generates sample.xlsx in current directory. */
    public static void main(final String[] args) throws IOException {
        try (final FileOutputStream out = new FileOutputStream("sample_import.xlsx")) {
            writeSampleWorkbook(out);
        }
        System.out.println("Generated: sample_import.xlsx");
    }
}
