package tech.derbent.api.imports.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
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

    private static void createActivitySheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Activity");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        // Comment rows (start with #)
        addRow(sheet, 0, commentStyle, "# Activity import template");
        addRow(sheet, 1, commentStyle, "# Columns: name (required), description, status, Type, Due Date, Estimated Hours");
        addRow(sheet, 2, commentStyle, "# status and Type must already exist in the system");
        // Header
        addRow(sheet, 3, headerStyle, "name", "description", "status", "Type", "Due Date", "Estimated Hours");
        // Sample data
        addRow(sheet, 4, null, "Design login screen", "Create wireframes for the login page", "Open", "Feature", "2025-06-15", "8");
        addRow(sheet, 5, null, "Implement authentication", "JWT-based auth implementation", "In Progress", "Task", "2025-06-20", "16");
        addRow(sheet, 6, null, "Write unit tests", "Cover authentication service with tests", "Open", "Task", "2025-06-25", "4");
        addRow(sheet, 7, null, "Code review meeting", "Sprint planning and code review session", "", "Meeting", "2025-06-18", "2");
        // Auto-size columns
        for (int col = 0; col <= 5; col++) { sheet.autoSizeColumn(col); }
    }

    private static void createIssueSheet(final Workbook wb) {
        final Sheet sheet = wb.createSheet("Issue");
        final CellStyle headerStyle = createHeaderStyle(wb);
        final CellStyle commentStyle = createCommentStyle(wb);
        addRow(sheet, 0, commentStyle, "# Issue import template");
        addRow(sheet, 1, commentStyle, "# Columns: name (required), description, status, Issue Type, Due Date");
        addRow(sheet, 2, commentStyle, "# All relation fields must already exist before import");
        addRow(sheet, 3, headerStyle, "name", "description", "status", "Issue Type", "Due Date");
        addRow(sheet, 4, null, "Login button broken on Safari", "Login button does not respond on Safari 16", "Open", "Bug", "2025-06-10");
        addRow(sheet, 5, null, "Slow dashboard load time", "Dashboard takes > 5s to load with 100+ projects", "Open", "Performance", "2025-06-30");
        addRow(sheet, 6, null, "Missing export button in reports", "Add CSV export to all report pages", "Open", "Feature Request", "2025-07-05");
        for (int col = 0; col <= 4; col++) { sheet.autoSizeColumn(col); }
    }

    private static void addRow(final Sheet sheet, final int rowIndex, final CellStyle style, final String... values) {
        final Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            final var cell = row.createCell(i);
            cell.setCellValue(values[i]);
            if (style != null) cell.setCellStyle(style);
        }
    }

    /** Command-line entry point: generates sample.xlsx in current directory. */
    public static void main(final String[] args) throws IOException {
        try (final FileOutputStream out = new FileOutputStream("sample_import.xlsx")) {
            writeSampleWorkbook(out);
        }
        System.out.println("Generated: sample_import.xlsx");
    }
}
