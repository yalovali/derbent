package tech.derbent.api.imports.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.registry.CEntityRegistry;

/**
 * Base importer for all {@link CTypeEntity} implementations.
 *
 * <p>RULE: This handler level owns importing fields declared on {@link CTypeEntity} (color,
 * sortOrder, level, canHaveChildren, attributeNonDeletable). Child handlers must not duplicate this
 * mapping.</p>
 *
 * <p>WHY: Both simple type handlers (priorities, etc.) and workflow-capable type handlers share the
 * same {@link CTypeEntity} fields. Placing shared logic here keeps the import handler hierarchy
 * parallel to the entity hierarchy:
 * <pre>
 *   Entity:  CEntityOfCompany → CTypeEntity → (concrete type)
 *   Handler: CEntityOfCompanyImportHandler → CAbstractTypeEntityImportHandler → (concrete handler)
 * </pre>
 * and eliminates duplication between {@link CAbstractSimpleTypeImportHandler} and
 * {@link CAbstractWorkflowTypeImportHandler}.</p>
 */
public abstract class CAbstractTypeEntityImportHandler<T extends CTypeEntity<T>>
		extends CEntityOfCompanyImportHandler<T> {

	/**
	 * Applies color from the Excel row or falls back to the entity class's registry default.
	 *
	 * <p>WHY: blank color columns should use entity-class defaults rather than leaving color null.
	 * This logic must run <em>after</em> {@code applyMetaFieldsDeclaredOn} so the default only
	 * fires when the row had no color value.</p>
	 */
	protected final void applyColorAndIcon(final T entity, final CExcelRow row) {
		final Optional<String> excelColor = row.optionalString("color");
		if (excelColor.isPresent() && !excelColor.get().isBlank()) {
			entity.setColor(excelColor.get());
		} else {
			final String defaultColor = CEntityRegistry.getDefaultColor(getEntityClass());
			if (defaultColor != null && !defaultColor.isBlank()) {
				entity.setColor(defaultColor);
			}
		}
	}

	/**
	 * Applies all fields owned by {@link CTypeEntity}: named fields, company binding, and all
	 * scalar type-entity fields (sortOrder, level, canHaveChildren, attributeNonDeletable) via
	 * {@link #applyMetaFieldsDeclaredOn}. Color is applied last so registry defaults fill blanks.
	 *
	 * <p>WHY: calling this method from subclass {@code importRow()} keeps each handler level
	 * responsible only for its own additional fields (e.g. workflow resolution).</p>
	 */
	protected final void applyTypeEntityFields(final T entity, final CExcelRow row,
			final CCompany company) {
		applyEntityNamedFields(entity, row);
		applyEntityOfCompanyFields(entity, company);
		// WHY: applyMetaFieldsDeclaredOn handles all scalar @AMetaData fields declared directly on
		// CTypeEntity (sortOrder, level, canHaveChildren, attributeNonDeletable, color) without
		// hard-coding field names. Non-scalar fields (e.g. workflow) are skipped automatically.
		applyMetaFieldsDeclaredOn(entity, row, CTypeEntity.class);
		// Color needs special default-fallback handling; run after the meta pass.
		applyColorAndIcon(entity, row);
	}

	/**
	 * Adds "Attribute Non Deletable" as an alias in addition to the @AMetaData-derived
	 * "Non Deletable" alias (which comes from {@code buildMetaAliases}).
	 *
	 * <p>WHY: human-authored Excel sheets sometimes use the verbose field name as a header. The
	 * short form "Non Deletable" is already covered via {@code @AMetaData(displayName)};
	 * only the long-form alias needs to be added explicitly.</p>
	 */
	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		final Map<String, String> aliases = new LinkedHashMap<>(super.getAdditionalColumnAliases());
		aliases.put("Attribute Non Deletable", "attributenondeletable");
		return Map.copyOf(aliases);
	}
}
