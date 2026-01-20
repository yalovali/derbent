package tech.derbent.api.reporting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.utils.Check;

/** CReportFieldDescriptor - Describes a field that can be included in a report.
 * <p>
 * Represents a single field or nested field path for CSV export. Supports grouped fields (e.g., "Status.Name", "Status.Color").
 * </p>
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Field metadata extraction from @AMetaData</li>
 * <li>Nested field support (e.g., status.name)</li>
 * <li>Collection detection</li>
 * <li>Display name generation</li>
 * <li>Value extraction with null safety</li>
 * </ul>
 * </p>
 * Layer: Reporting (API) */
public class CReportFieldDescriptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CReportFieldDescriptor.class);

	private static void addNestedEntityFields(final List<CReportFieldDescriptor> descriptors, final Field parentField,
			final String parentDisplayName) {
		try {
			final Class<?> entityType = parentField.getType();
			final String[] commonFields = {
					"name", "description", "color", "icon"
			};
			for (final String nestedFieldName : commonFields) {
				try {
					final Field nestedField = findField(entityType, nestedFieldName);
					if (nestedField != null) {
						nestedField.setAccessible(true);
						final String nestedPath = parentField.getName() + "." + nestedFieldName;
						final String nestedDisplayName = formatFieldName(nestedFieldName);
						descriptors.add(new CReportFieldDescriptor(nestedPath, nestedDisplayName, parentDisplayName, nestedField.getType(),
								nestedField, false, false));
					}
				} catch ( final Exception e) {
					// Field doesn't exist, skip
				}
			}
		} catch ( final Exception e) {
			LOGGER.debug("Error adding nested fields for: {}", parentField.getName());
		}
	}

	public static List<CReportFieldDescriptor> discoverFields(final Class<? extends CEntityDB<?>> entityClass) {
		Objects.requireNonNull(entityClass, "Entity class cannot be null");
		final List<CReportFieldDescriptor> descriptors = new ArrayList<>();
		final List<Field> allFields = getAllFields(entityClass);
		for (final Field field : allFields) {
			try {
				field.setAccessible(true);
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
					continue;
				}
				final AMetaData metadata = field.getAnnotation(AMetaData.class);
				final String displayName =
						metadata != null && !metadata.displayName().isEmpty() ? metadata.displayName() : formatFieldName(field.getName());
				if (metadata != null && metadata.hidden()) {
					continue;
				}
				final boolean isCollection = Collection.class.isAssignableFrom(field.getType());
				final boolean isComplex = CEntityDB.class.isAssignableFrom(field.getType());
				descriptors.add(new CReportFieldDescriptor(field.getName(), displayName, "Base (" + entityClass.getSimpleName().substring(1) + ")",
						field.getType(), field, isCollection, isComplex));
				if (isComplex && !isCollection) {
					addNestedEntityFields(descriptors, field, displayName);
				}
			} catch (final Exception e) {
				LOGGER.debug("Skipping field '{}': {}", field.getName(), e.getMessage());
			}
		}
		return descriptors;
	}

	private static Field findField(final Class<?> clazz, final String fieldName) {
		Class<?> current = clazz;
		while (current != null) {
			try {
				return current.getDeclaredField(fieldName);
			} catch ( final NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	private static String formatFieldName(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty()) {
			return fieldName;
		}
		final String withSpaces = fieldName.replaceAll("([A-Z])", " $1").trim();
		return Character.toUpperCase(withSpaces.charAt(0)) + withSpaces.substring(1);
	}

	private static String formatValue(final Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection<?>) value;
			if (collection.isEmpty()) {
				return "";
			}
			return collection.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining("; "));
		}
		if (value instanceof CEntityDB) {
			return value.toString();
		}
		return value.toString();
	}

	private static List<Field> getAllFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			fields.addAll(Arrays.asList(current.getDeclaredFields()));
			current = current.getSuperclass();
		}
		return fields;
	}

	private static Object getFieldValue(final Object object, final String fieldName) throws Exception {
		Objects.requireNonNull(object, "Object cannot be null");
		Objects.requireNonNull(fieldName, "Field name cannot be null");
		final String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		try {
			final Method getter = object.getClass().getMethod(getterName);
			getter.setAccessible(true);
			return getter.invoke(object);
		} catch ( final NoSuchMethodException e) {
			final String booleanGetterName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			try {
				final Method booleanGetter = object.getClass().getMethod(booleanGetterName);
				booleanGetter.setAccessible(true);
				return booleanGetter.invoke(object);
			} catch ( final NoSuchMethodException e2) {
				throw new Exception("No getter found for field: " + fieldName);
			}
		}
	}

	private final String displayName;
	private final Field field;
	private final String fieldPath;
	private final Class<?> fieldType;
	private final String groupName;
	private final boolean isCollection;
	private final boolean isComplex;
	private final String[] pathSegments;

	public CReportFieldDescriptor(final String fieldPath, final String displayName, final String groupName, final Class<?> fieldType,
			final Field field, final boolean isCollection, final boolean isComplex) {
		Check.notNull(fieldPath, "Field path cannot be null");
		Check.notNull(displayName, "Display name cannot be null");
		Check.notNull(fieldType, "Field type cannot be null");
		this.fieldPath = fieldPath;
		this.displayName = displayName;
		this.groupName = groupName;
		this.fieldType = fieldType;
		this.field = field;
		this.isCollection = isCollection;
		this.isComplex = isComplex;
		pathSegments = fieldPath.split("\\.");
	}

	public String extractValue(final Object entity) {
		if (entity == null) {
			return "";
		}
		try {
			Object currentValue = entity;
			for (final String segment : pathSegments) {
				if (currentValue == null) {
					return "";
				}
				currentValue = getFieldValue(currentValue, segment);
			}
			return formatValue(currentValue);
		} catch (final Exception e) {
			LOGGER.debug("Error extracting value for field '{}': {}", fieldPath, e.getMessage());
			return "";
		}
	}

	public String getDisplayName() { return displayName; }

	public Field getField() { return field; }

	public String getFieldPath() { return fieldPath; }

	public Class<?> getFieldType() { return fieldType; }

	public String getGroupName() { return groupName; }

	public String[] getPathSegments() { return pathSegments; }

	public boolean isCollection() { return isCollection; }

	public boolean isComplex() { return isComplex; }
}
