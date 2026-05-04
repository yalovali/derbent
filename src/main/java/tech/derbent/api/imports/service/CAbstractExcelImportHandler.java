package tech.derbent.api.imports.service;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import tech.derbent.api.annotations.AMetaData;

/** Base class for entity import handlers. WHY: large workbooks (system_init.xlsx) must be authorable by humans; we therefore accept both Java field
 * names and {@link AMetaData#displayName()} header labels without forcing each handler to hard-code dozens of aliases. */
public abstract class CAbstractExcelImportHandler<T> implements IEntityImportHandler<T> {

	private static final ConcurrentMap<Class<?>, Map<String, Method>> WRITE_METHOD_CACHE = new ConcurrentHashMap<>();

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

	private static Method getWriteMethod(final Class<?> beanClass, final String propertyName) {
		final Map<String, Method> setters = WRITE_METHOD_CACHE.computeIfAbsent(beanClass, c -> {
			try {
				final Map<String, Method> map = new LinkedHashMap<>();
				for (final PropertyDescriptor pd : Introspector.getBeanInfo(c).getPropertyDescriptors()) {
					if (pd.getWriteMethod() != null) {
						map.put(pd.getName(), pd.getWriteMethod());
					}
				}
				return Map.copyOf(map);
			} catch (final Exception e) {
				return Map.of();
			}
		});
		return setters.get(propertyName);
	}

	/** Parses a literal string value (from {@link AMetaData#defaultValue()}) into the target type without
	 * needing a row context. Returns {@code null} for unsupported types or parse failures. */
	private static Object parseLiteralValue(final Class<?> type, final String literal) {
		try {
			if (type == String.class) {
				return literal;
			}
			if (type == Boolean.class || type == boolean.class) {
				return Boolean.parseBoolean(literal);
			}
			if (type == Integer.class || type == int.class) {
				return Integer.parseInt(literal);
			}
			if (type == Long.class || type == long.class) {
				return Long.parseLong(literal);
			}
			if (type == java.math.BigDecimal.class) {
				return new java.math.BigDecimal(literal);
			}
			if (type == LocalDate.class) {
				return LocalDate.parse(literal);
			}
		} catch (final Exception e) {
			// silently skip unparseable defaults
		}
		return null;
	}

	private static Object parseScalarValue(final Class<?> type, final String raw, final CExcelRow row,
			final String token) {
		try {
			if (type == String.class) {
				return raw;
			}
			if (type == Integer.class || type == int.class) {
				return row.optionalInt(token)
						.orElseThrow(() -> new IllegalArgumentException("Invalid integer: " + raw));
			}
			if (type == Long.class || type == long.class) {
				return row.optionalLong(token).orElseThrow(() -> new IllegalArgumentException("Invalid long: " + raw));
			}
			if (type == java.math.BigDecimal.class) {
				return row.optionalBigDecimal(token)
						.orElseThrow(() -> new IllegalArgumentException("Invalid decimal: " + raw));
			}
			if (type == Boolean.class || type == boolean.class) {
				return row.optionalBoolean(token).orElse(Boolean.FALSE);
			}
			if (type == LocalDate.class) {
				return row.optionalLocalDate(token)
						.orElseThrow(() -> new IllegalArgumentException("Invalid date: " + raw));
			}
			if (type == LocalTime.class) {
				return row.optionalLocalTime(token)
						.orElseThrow(() -> new IllegalArgumentException("Invalid time: " + raw));
			}
			if (type == LocalDateTime.class) {
				final LocalDateTime dt = row.optionalLocalDateTime(token).orElse(null);
				if (dt != null) {
					return dt;
				}
				final LocalDate d = row.optionalLocalDate(token).orElse(null);
				if (d != null) {
					return d.atStartOfDay();
				}
				throw new IllegalArgumentException("Invalid datetime: " + raw);
			}
			if (type.isEnum()) {
				final String normalized = raw.trim().toLowerCase().replaceAll("[\\s_]+", "");
				for (final Object constant : type.getEnumConstants()) {
					final String key = constant.toString().trim().toLowerCase().replaceAll("[\\s_]+", "");
					if (key.equals(normalized)) {
						return constant;
					}
				}
				throw new IllegalArgumentException("Invalid enum value: " + raw);
			}
			return null;
		} catch (final RuntimeException e) {
			// Re-throw with token context to make Excel fixes easy.
			throw new IllegalArgumentException("Invalid value for " + token + ": '" + raw + "'", e);
		}
	}

	/** Applies simple (scalar) fields declared on {@code declaringClass} by reflecting over its {@link AMetaData}-annotated fields and assigning values
	 * from the current row.
	 * <p>
	 * RULE: Call this at the same inheritance level as the entity fields themselves. For example, {@code CAgileEntityImportHandler} should pass
	 * {@code CAgileEntity.class}. This keeps field ownership aligned with the domain model hierarchy.
	 * </p>
	 * <p>
	 * WHY: reduces duplicated {@code row.optionalX(...).ifPresent(entity::setX)} blocks while keeping the import format stable (header aliases are
	 * already derived from {@link AMetaData#displayName()}).
	 * </p>
	 */
	protected final void applyMetaFieldsDeclaredOn(final T entity, final CExcelRow row, final Class<?> declaringClass) {
		if (entity == null || row == null || declaringClass == null) {
			return;
		}
		for (final Field field : declaringClass.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			final AMetaData meta = field.getAnnotation(AMetaData.class);
			// WHY: skip readOnly fields (e.g. id) — the import must not override Hibernate-managed keys.
			if (meta == null || meta.hidden() || meta.autoCalculate() || meta.readOnly()) {
				continue;
			}
			final String token = CExcelRow.normalizeToken(field.getName());
			final String raw = row.string(token);
			if (raw.isBlank()) {
				// WHY: when the cell is blank and @AMetaData(defaultValue) declares a non-empty default,
				// apply it so NOT NULL columns (e.g. active=true) get their intended value even when the
				// Excel author leaves the cell empty.
				final String defaultVal = meta.defaultValue();
				if (!defaultVal.isBlank()) {
					final Object parsed = parseLiteralValue(field.getType(), defaultVal);
					if (parsed != null) {
						final Method setter = getWriteMethod(entity.getClass(), field.getName());
						if (setter != null) {
							try {
								setter.invoke(entity, parsed);
							} catch (final Exception e) {
								throw new IllegalArgumentException(
										"Failed to set default for '" + field.getName() + "': " + e.getMessage(), e);
							}
						}
					}
				}
				continue;
			}
			final Object parsed = parseScalarValue(field.getType(), raw, row, token);
			if (parsed == null) {
				continue;
			}
			final Method setter = getWriteMethod(entity.getClass(), field.getName());
			if (setter == null) {
				continue;
			}
			try {
				setter.invoke(entity, parsed);
			} catch (final Exception e) {
				throw new IllegalArgumentException("Failed to set '" + field.getName() + "': " + e.getMessage(), e);
			}
		}
	}

	/** Override only for non-metadata synonyms (e.g. "Type" → entityType). */
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of();
	}

	/** Returns header aliases for this entity. WHY: display names come from @AMetaData (shared with the UI) so the import format stays stable even when
	 * developers refactor field names. */
	@Override
	public final Map<String, String> getColumnAliases() {
		final Map<String, String> aliases = new LinkedHashMap<>();
		aliases.putAll(buildMetaAliases(getEntityClass()));
		aliases.putAll(getAdditionalColumnAliases());
		return aliases;
	}

	@Override
	public Set<String> getRequiredColumns() { return Set.of(); }

	@Override
	public Set<String> getSupportedSheetNames() { return CImportSheetNames.forEntity(getEntityClass()); }

	protected final CExcelRow row(final Map<String, String> rowData) {
		return new CExcelRow(rowData);
	}
}
