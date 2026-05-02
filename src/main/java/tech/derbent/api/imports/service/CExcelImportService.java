package tech.derbent.api.imports.service;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.ZoneId;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportResult;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.domain.CImportSheetResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;

/**
 * Orchestrates Excel workbook import: sheet detection, header parsing,
 * row processing, and transaction management.
 *
 * Transaction boundary: the entire import runs in one transaction.
 * Dry-run and rollback-on-error use setRollbackOnly() so results can still be returned.
 */
@Service
public class CExcelImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CExcelImportService.class);

    private final CImportHandlerRegistry handlerRegistry;

    public CExcelImportService(final CImportHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    /**
     * Main entry point. Parses the workbook, processes each sheet, and returns
     * a complete CImportResult. Runs in a single transaction; dry-run and
     * rollback-on-error mark the transaction for rollback without throwing.
     *
     * @param inputStream Excel (.xlsx) bytes
     * @param options     import configuration
     * @param project     active project context for all imported entities
     */
    @Transactional
    public CImportResult importExcel(final InputStream inputStream, final CImportOptions options,
            final CProject<?> project) {
        Check.notNull(inputStream, "Input stream cannot be null");
        Check.notNull(options, "Import options cannot be null");
        Check.notNull(project, "Project cannot be null");
        final CImportResult result = new CImportResult(options.isDryRun());
        try {
            processWorkbook(inputStream, options, project, result);
        } catch (final Exception e) {
            LOGGER.error("Excel import failed globally reason={}", e.getMessage(), e);
            result.setGlobalErrorMessage("Import failed: " + e.getMessage());
            // Roll back on unexpected exception
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.setRolledBack(true);
            return result;
        }
        // Apply rollback policy after all rows have been processed
        if (options.isDryRun() || (options.isRollbackOnError() && result.getTotalErrors() > 0)) {
            result.setRolledBack(true);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    private void processWorkbook(final InputStream inputStream, final CImportOptions options,
            final CProject<?> project, final CImportResult result) throws Exception {
        try (final Workbook workbook = new XSSFWorkbook(inputStream)) {
            final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                final Sheet sheet = workbook.getSheetAt(i);
                final String sheetName = sheet.getSheetName();
                LOGGER.debug("Processing sheet: {}", sheetName);
                final var handlerOpt = handlerRegistry.findHandler(sheetName);
                if (handlerOpt.isEmpty()) {
                    if (!options.isSkipUnknownSheets()) {
                        final CImportSheetResult unrecognized = new CImportSheetResult(sheetName, null, false);
                        unrecognized.setHeaderErrorMessage("No import handler registered for sheet '" + sheetName + "'");
                        result.addSheetResult(unrecognized);
                    }
                    continue;
                }
                final IEntityImportHandler<?> handler = handlerOpt.get();
                final CImportSheetResult sheetResult = processSheet(sheet, handler, project, options, evaluator);
                result.addSheetResult(sheetResult);
            }
        }
    }

    private CImportSheetResult processSheet(final Sheet sheet, final IEntityImportHandler<?> handler,
            final CProject<?> project, final CImportOptions options, final FormulaEvaluator evaluator) {
        final String entityTypeName = handler.getEntityClass().getSimpleName();
        final CImportSheetResult sheetResult = new CImportSheetResult(sheet.getSheetName(), entityTypeName, true);
        // Find header row (first non-comment row)
        int headerRowIndex = -1;
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            final Row row = sheet.getRow(r);
            if (row == null) continue;
            if (!isCommentRow(row, evaluator)) {
                headerRowIndex = r;
                break;
            }
        }
        if (headerRowIndex < 0) {
            sheetResult.setHeaderErrorMessage("Sheet has no header row");
            return sheetResult;
        }
        // Build column index → canonical field token map
        final Map<Integer, String> columnMapping = buildColumnMapping(sheet.getRow(headerRowIndex), handler, evaluator);
        if (columnMapping.isEmpty()) {
            sheetResult.setHeaderErrorMessage("No recognized columns found in header row");
            return sheetResult;
        }
        LOGGER.debug("Sheet '{}' column mapping: {}", sheet.getSheetName(), columnMapping);
        // Process data rows
        for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
            final Row row = sheet.getRow(r);
            if (row == null) {
                sheetResult.addRowResult(CImportRowResult.skipped(r + 1));
                continue;
            }
            if (isCommentRow(row, evaluator)) {
                sheetResult.addRowResult(CImportRowResult.skipped(r + 1));
                continue;
            }
            if (isBlankRow(row, evaluator)) {
                sheetResult.addRowResult(CImportRowResult.skipped(r + 1));
                continue;
            }
            final Map<String, String> rowData = extractRowData(row, columnMapping, evaluator);
            // Validate required columns
            final String missingCol = checkRequiredColumns(rowData, handler);
            if (missingCol != null) {
                sheetResult.addRowResult(CImportRowResult.error(r + 1, "Required column missing or blank: " + missingCol, rowData));
                continue;
            }
            final CImportRowResult rowResult = handler.importRow(rowData, project, r + 1, options);
            sheetResult.addRowResult(rowResult);
        }
        return sheetResult;
    }

    /** Returns the canonical field token for a missing required column, or null if all present. */
    private String checkRequiredColumns(final Map<String, String> rowData, final IEntityImportHandler<?> handler) {
        for (final String required : handler.getRequiredColumns()) {
            final String value = rowData.get(required);
            if (value == null || value.isBlank()) {
                return required;
            }
        }
        return null;
    }

    /**
     * Maps header cell values to canonical field tokens.
     * Matching is case-insensitive; whitespace is stripped.
     * Handler aliases are checked first; then the header is lower-cased and whitespace collapsed.
     */
    private static String normalizeHeaderKey(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replaceAll("\\s+", "");
    }

    private Map<Integer, String> buildColumnMapping(final Row headerRow, final IEntityImportHandler<?> handler,
            final FormulaEvaluator evaluator) {
        final Map<Integer, String> mapping = new LinkedHashMap<>();
        if (headerRow == null) {
            return mapping;
        }
        final Map<String, String> aliases = handler.getColumnAliases();
        final int firstCell = headerRow.getFirstCellNum();
        if (firstCell < 0) {
            return mapping;
        }
        for (int col = firstCell; col < headerRow.getLastCellNum(); col++) {
            final Cell cell = headerRow.getCell(col);
            if (cell == null) {
                continue;
            }
            final String header = getCellStringValue(cell, evaluator).trim();
            if (header.isBlank()) {
                continue;
            }
            final String normalizedHeader = normalizeHeaderKey(header);
            String canonical = null;
            for (final Map.Entry<String, String> entry : aliases.entrySet()) {
                if (normalizeHeaderKey(entry.getKey()).equals(normalizedHeader)) {
                    canonical = entry.getValue();
                    break;
                }
            }
            if (canonical == null) {
                canonical = normalizedHeader;
            }
            mapping.put(col, canonical);
        }
        return mapping;
    }

    private Map<String, String> extractRowData(final Row row, final Map<Integer, String> columnMapping,
            final FormulaEvaluator evaluator) {
        final Map<String, String> data = new LinkedHashMap<>();
        for (final Map.Entry<Integer, String> entry : columnMapping.entrySet()) {
            final Cell cell = row.getCell(entry.getKey());
            final String value = cell != null ? getCellStringValue(cell, evaluator).trim() : "";
            data.put(entry.getValue(), value);
        }
        return data;
    }

    /** Reads cell value as String, evaluating formula cells when evaluator is provided. */
    public static String getCellStringValue(final Cell cell, final FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        final Cell resolved = evaluator != null && cell.getCellType() == CellType.FORMULA
                ? evaluator.evaluateInCell(cell) : cell;
        return switch (resolved.getCellType()) {
            case STRING -> resolved.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(resolved)) {
                    final var date = resolved.getDateCellValue();
                    if (date == null) {
                        yield "";
                    }
                    yield date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                }
                final double d = resolved.getNumericCellValue();
                // Avoid ".0" suffix for whole numbers
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(resolved.getBooleanCellValue());
            case BLANK, _NONE -> "";
            default -> "";
        };
    }

    private boolean isCommentRow(final Row row, final FormulaEvaluator evaluator) {
        final int firstCell = row.getFirstCellNum();
        if (firstCell < 0) {
            return false;
        }
        final Cell first = row.getCell(firstCell);
        if (first == null) {
            return false;
        }
        return getCellStringValue(first, evaluator).startsWith("#");
    }

    private boolean isBlankRow(final Row row, final FormulaEvaluator evaluator) {
        final int firstCell = row.getFirstCellNum();
        if (firstCell < 0) {
            return true;
        }
        for (int col = firstCell; col < row.getLastCellNum(); col++) {
            final Cell cell = row.getCell(col);
            if (cell != null && !getCellStringValue(cell, evaluator).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
