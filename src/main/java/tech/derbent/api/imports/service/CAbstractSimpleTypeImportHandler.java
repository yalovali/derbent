package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;

/** Base importer for simple {@link CTypeEntity} reference data without workflows. WHY: priorities/levels/etc have the same "upsert by name" + scalar
 * field parsing logic. */
public abstract class CAbstractSimpleTypeImportHandler<T extends CTypeEntity<T>>
		extends CEntityOfCompanyImportHandler<T> {

	/** Hook for entity-specific fields (e.g. priorityLevel, isDefault). */
	protected void applyExtraFields(@SuppressWarnings ("unused") final T entity,
			@SuppressWarnings ("unused") final CExcelRow row, @SuppressWarnings ("unused") final CProject<?> project,
			@SuppressWarnings ("unused") final int rowNumber) {
		// default: no extra fields
	}

	protected abstract T createNew(String name, CCompany company);
	protected abstract Optional<T> findByNameAndCompany(String name, CCompany company);

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final CExcelRow row = row(rowData);
		final var nameError = validateEntityNamed(row, rowNumber, rowData);
		if (nameError.isPresent()) {
			return nameError.get();
		}
		final String name = row.string("name");
		final CCompany company = project.getCompany();
		// WHY: templates are imported both automatically (after DB reset) and manually; upsert-by-name keeps them re-runnable.
		final T type = findByNameAndCompany(name, company).orElseGet(() -> createNew(name, company));
		applyEntityNamedFields(type, row);
		applyEntityOfCompanyFields(type, company);
		row.optionalString("color").ifPresent(type::setColor);
		row.optionalInt("sortorder").ifPresent(type::setSortOrder);
		applyExtraFields(type, row, project, rowNumber);
		if (!options.isDryRun()) {
			save(type);
		}
		return CImportRowResult.success(rowNumber, name);
	}

	protected abstract void save(T entity);
}
