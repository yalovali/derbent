package tech.derbent.api.imports.service;

import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

/**
 * Base importer for company-scoped entities.
 */
public abstract class CEntityOfCompanyImportHandler<T extends CEntityOfCompany<T>> extends CEntityNamedImportHandler<T> {
    // Intentionally empty.
}
