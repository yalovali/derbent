package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;

/**
 * Base importer for company-scoped entities.
 *
 * <p>RULE: This handler level owns importing fields declared on {@link CEntityOfCompany}
 * (i.e. the {@code company} reference). Child handlers must not duplicate this mapping.</p>
 */
public abstract class CEntityOfCompanyImportHandler<T extends CEntityOfCompany<T>> extends CEntityNamedImportHandler<T> {

	protected final Optional<CImportRowResult> validateProjectHasCompany(final CProject<?> project, final int rowNumber,
			final Map<String, String> rowData) {
		if (project.getCompany() == null) {
			return Optional.of(CImportRowResult.error(rowNumber, "Project company is required", rowData));
		}
		return Optional.empty();
	}

	protected void applyEntityOfCompanyFields(final T entity, final CCompany company) {
		if (entity.getCompany() == null) {
			entity.setCompany(company);
		}
	}
}
