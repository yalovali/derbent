package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Set;

/**
 * Base class for entity import handlers.
 *
 * WHY: most handlers share the same boilerplate for supported sheet names and empty alias/required
 * sets; centralizing that keeps each handler small and focused.
 */
public abstract class CAbstractExcelImportHandler<T> implements IEntityImportHandler<T> {

    @Override
    public Set<String> getSupportedSheetNames() {
        return CImportSheetNames.forEntity(getEntityClass());
    }

    @Override
    public Map<String, String> getColumnAliases() {
        return Map.of();
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of();
    }

    protected final CExcelRow row(final Map<String, String> rowData) {
        return new CExcelRow(rowData);
    }
}
