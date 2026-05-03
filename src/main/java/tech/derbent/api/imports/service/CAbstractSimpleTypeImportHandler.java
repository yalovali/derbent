package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;

/**
 * Base importer for simple {@link CTypeEntity} reference data without workflows.
 *
 * WHY: priorities/levels/etc have the same "upsert by name" + scalar field parsing logic.
 */
public abstract class CAbstractSimpleTypeImportHandler<T extends CTypeEntity<T>>
        extends CAbstractExcelImportHandler<T> {

    protected abstract Optional<T> findByNameAndCompany(String name, CCompany company);

    protected abstract T createNew(String name, CCompany company);

    protected abstract void save(T entity);

    /** Hook for entity-specific fields (e.g. priorityLevel, isDefault). */
    protected void applyExtraFields(final T entity, final CExcelRow row, final CProject<?> project, final int rowNumber) {
        // default: no extra fields
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
            final CImportOptions options) {
        final CExcelRow row = row(rowData);
        final String name = row.string("name");
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        if (project.getCompany() == null) {
            return CImportRowResult.error(rowNumber, "Project company is required to create reference types", rowData);
        }
        final CCompany company = project.getCompany();

        // WHY: templates are imported both automatically (after DB reset) and manually; upsert-by-name keeps them re-runnable.
        final T type = findByNameAndCompany(name, company).orElseGet(() -> createNew(name, company));

        row.optionalString("color").ifPresent(type::setColor);
        row.optionalInt("sortorder").ifPresent(type::setSortOrder);

        applyExtraFields(type, row, project, rowNumber);

        if (!options.isDryRun()) {
            save(type);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
