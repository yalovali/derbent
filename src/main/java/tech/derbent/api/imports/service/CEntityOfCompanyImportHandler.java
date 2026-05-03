package tech.derbent.api.imports.service;

import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

/**
 * Base importer for company-scoped entities.
 *
 * <p>RULE: This handler level owns importing fields declared on {@link CEntityOfCompany}
 * (i.e. the {@code company} reference). Child handlers must not duplicate this mapping.</p>
 *
 * <p>PARENT COLUMN: Excel rows may include a "company" column. When present, the row is resolved
 * to that specific company; when absent, the session/context company is used.</p>
 */
public abstract class CEntityOfCompanyImportHandler<T extends CEntityOfCompany<T>> extends CEntityNamedImportHandler<T> {

	/**
	 * Resolves the effective company for a row.
	 * If the "company" column is blank, returns the session company unchanged.
	 * If specified, looks it up by name. Returns empty if not found.
	 */
	protected final Optional<CCompany> resolveCompanyFromRow(final CExcelRow row, final CCompany sessionCompany,
			final CImportProjectResolver resolver) {
		final String companyName = row.string("company");
		if (companyName.isBlank()) {
			return Optional.of(sessionCompany);
		}
		return resolver.findCompanyByName(companyName);
	}

	protected void applyEntityOfCompanyFields(final T entity, final CCompany company) {
		if (entity.getCompany() == null) {
			entity.setCompany(company);
		}
	}
}
