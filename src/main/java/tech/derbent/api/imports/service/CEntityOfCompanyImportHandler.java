package tech.derbent.api.imports.service;

import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

/**
 * Base importer for company-scoped entities.
 *
 * <p>RULE: This handler level owns importing fields declared on {@link CEntityOfCompany}
 * (i.e. the {@code company} reference). Child handlers must not duplicate this mapping.</p>
 */
public abstract class CEntityOfCompanyImportHandler<T extends CEntityOfCompany<T>> extends CEntityNamedImportHandler<T> {

	protected void applyEntityOfCompanyFields(final T entity, final CCompany company) {
		if (entity.getCompany() == null) {
			entity.setCompany(company);
		}
	}
}
