package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.imports.domain.CImportRowResult;

/**
 * Base importer for {@link CEntityNamed} entities.
 *
 * <p>RULE: This handler level owns importing fields declared on {@link CEntityNamed}
 * (e.g. {@code name}, {@code description}). Child handlers must not duplicate this mapping.</p>
 */
public abstract class CEntityNamedImportHandler<T extends CEntityNamed<T>> extends CEntityImportHandler<T> {

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("name");
	}

	protected final Optional<CImportRowResult> validateEntityNamed(final CExcelRow row, final int rowNumber,
			final Map<String, String> rowData) {
		if (row.string("name").isBlank()) {
			return Optional.of(CImportRowResult.error(rowNumber, "Name is required", rowData));
		}
		return Optional.empty();
	}

	protected void applyEntityNamedFields(final T entity, final CExcelRow row) {
		// WHY: CEntityDB fields (e.g. active=true) must be explicitly applied so NOT NULL columns
		// are populated even when the Excel cell is blank — the defaultValue from @AMetaData is used.
		applyMetaFieldsDeclaredOn(entity, row, CEntityDB.class);
		applyMetaFieldsDeclaredOn(entity, row, CEntityNamed.class);
	}
}
