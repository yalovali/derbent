package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Set;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;

/**
 * Contract for importing a specific entity type from an Excel row.
 *
 * Each implementing Spring bean registers itself for one entity type.
 * The import engine discovers all beans via Spring's collection injection.
 *
 * Column name matching is case-insensitive and ignores leading/trailing whitespace.
 * Handlers declare aliases so column headers like "Due Date" map to field "dueDate".
 *
 * Relations (status, type, parent) are resolved by display name, not ID.
 * If a named relation cannot be found the row is rejected with a clear error.
 */
public interface IEntityImportHandler<T> {

    /** Entity class that this handler creates/imports. */
    Class<T> getEntityClass();

    /**
     * All sheet name strings (case-insensitive) that this handler claims.
     * Should include at minimum: simple class name, class name without "C" prefix,
     * and any registered display names (singular + plural) from CEntityRegistry.
     */
    Set<String> getSupportedSheetNames();

    /**
     * Column header aliases.  Key = alias (any case), Value = canonical field token.
     * The canonical token is what importRow() uses when reading rowData.
     * This allows both "Due Date" and "dueDate" in the Excel header to map to "dueDate".
     */
    Map<String, String> getColumnAliases();

    /** Field tokens that must be present (non-blank) in a data row. */
    Set<String> getRequiredColumns();

    /**
     * Process one data row.
     *
     * @param rowData  map of canonicalFieldToken → cellStringValue (already normalised by import engine)
     * @param project  the currently active project
     * @param rowNumber 1-based sheet row number for error messages
     * @param options import options (dry-run, rollback policy, etc.)
     * @return per-row result; never null
     */
    CImportRowResult importRow(Map<String, String> rowData, CProject<?> project, int rowNumber, CImportOptions options);
}
