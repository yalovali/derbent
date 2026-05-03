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
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    public CExcelImportService(final CImportHandlerRegistry handlerRegistry, final EntityManager entityManager) {
        this.handlerRegistry = handlerRegistry;
        this.entityManager = entityManager;
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
        processWorkbook(inputStream, options, project, result);
        // Apply rollback policy after all rows have been processed
        if (options.isDryRun() || (options.isRollbackOnError() && result.getTotalErrors() > 0)) {
            result.setRolledBack(true);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    private void processWorkbook(final InputStream inputStream, final CImportOptions options,
            final CProject<?> project, final CImportResult result) {
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

                // WHY: later sheets commonly resolve relations by querying the database (Issue → Activity, ParentRelation → Ticket, etc.).
                // Within a single transaction, Hibernate may not flush inserts before those queries, yielding false "not found" errors.
                if (!options.isDryRun()) {
                    entityManager.flush();
                }
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Excel import failed: " + e.getMessage(), e);
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
            if (row == null) {
				continue;
			}
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
            if (shouldSkipByCompanyOrProject(rowData, project)) {
                sheetResult.addRowResult(CImportRowResult.skipped(r + 1));
                continue;
            }
            // Validate required columns
            final String missingCol = checkRequiredColumns(rowData, handler);
            if (missingCol != null) {
                sheetResult.addRowResult(CImportRowResult.error(r + 1, "Required column missing or blank: " + missingCol, rowData));
                continue;
            }
            final CImportRowResult rowResult = handler.importRow(rowData, project, r + 1, options);
            // WHY: Excel init is often used interactively; row-level logs make CI/Playwright failures diagnosable.
            if (rowResult != null && rowResult.isError()) {
                LOGGER.warn("Import row error (sheet={}, row={}): {}", sheet.getSheetName(), r + 1, rowResult.getErrorMessage());
            }
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

    private static boolean shouldSkipByCompanyOrProject(final Map<String, String> rowData, final CProject<?> project) {
        if (rowData == null || project == null) {
            return false;
        }
        final String companyToken = rowData.getOrDefault("company", "").trim();
        if (!companyToken.isBlank() && !isWildcard(companyToken)) {
            final var company = project.getCompany();
            if (company != null && !companyToken.equalsIgnoreCase(company.getName())) {
                return true;
            }
        }
        final String projectToken = rowData.getOrDefault("project", "").trim();
        if (!projectToken.isBlank() && !isWildcard(projectToken)) {
            if (!projectToken.equalsIgnoreCase(project.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWildcard(final String token) {
        final String v = token == null ? "" : token.trim().toLowerCase();
        return v.equals("*") || v.equals("all") || v.equals("any");
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
