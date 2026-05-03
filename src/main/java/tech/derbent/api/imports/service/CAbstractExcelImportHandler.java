package tech.derbent.api.imports.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;

/**
 * Base class for entity import handlers.
 *
 * WHY: large workbooks (system_init.xlsx) must be authorable by humans; we therefore accept both Java
 * field names and {@link AMetaData#displayName()} header labels without forcing each handler to
 * hard-code dozens of aliases.
 */
public abstract class CAbstractExcelImportHandler<T> implements IEntityImportHandler<T> {

    @Override
    public Set<String> getSupportedSheetNames() {
        return CImportSheetNames.forEntity(getEntityClass());
    }

    /**
     * Returns header aliases for this entity.
     *
     * WHY: display names come from @AMetaData (shared with the UI) so the import format stays stable
     * even when developers refactor field names.
     */
    @Override
    public final Map<String, String> getColumnAliases() {
        final Map<String, String> aliases = new LinkedHashMap<>();
        aliases.putAll(buildMetaAliases(getEntityClass()));
        aliases.putAll(getAdditionalColumnAliases());
        return aliases;
    }

    /** Override only for non-metadata synonyms (e.g. "Type" → entityType). */
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of();
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of();
    }

    protected final CExcelRow row(final Map<String, String> rowData) {
        return new CExcelRow(rowData);
    }

	protected final Optional<CImportRowResult> validateProjectHasCompany(final CProject<?> project, final int rowNumber,
			final Map<String, String> rowData) {
		if (project.getCompany() == null) {
			return Optional.of(CImportRowResult.error(rowNumber, "Project company is required", rowData));
		}
		return Optional.empty();
	}

    private static Map<String, String> buildMetaAliases(final Class<?> entityClass) {
        final Map<String, String> aliases = new LinkedHashMap<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            for (final Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                final AMetaData meta = field.getAnnotation(AMetaData.class);
                if (meta == null || meta.hidden()) {
                    continue;
                }
                final String displayName = meta.displayName();
                if (displayName == null || displayName.isBlank()) {
                    continue;
                }
                aliases.put(displayName, CExcelRow.normalizeToken(field.getName()));
            }
            current = current.getSuperclass();
        }
        return aliases;
    }
}
