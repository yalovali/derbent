package tech.derbent.api.reports.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamResource;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.Check;

/** Service for generating CSV reports from entity data. Handles CSV formatting, field value extraction, and file downloads. */
@Service
public class CReportService {

	private static final String CSV_NEWLINE = "\n";
	private static final String CSV_QUOTE = "\"";
	private static final String CSV_SEPARATOR = ",";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final Logger LOGGER = LoggerFactory.getLogger(CReportService.class);

	/** Triggers a download of the CSV file in the browser.
	 * @param csvContent the CSV content as a string
	 * @param fileName   the file name for the download */
	public static void downloadCSV(final String csvContent, final String fileName) {
		Check.notBlank(csvContent, "CSV content cannot be blank");
		Check.notBlank(fileName, "File name cannot be blank");
		final StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(csvContent.getBytes()));
		resource.setContentType("text/csv");
		resource.setCacheTime(0);
		// Register the resource and trigger download via JavaScript
		final UI currentUI = UI.getCurrent();
		final var registration = currentUI.getSession().getResourceRegistry().registerResource(resource);
		currentUI.getPage().open(registration.getResourceUri().toString(), "_blank");
		LOGGER.info("CSV download triggered: {}", fileName);
	}

	/** Escapes a CSV value by wrapping in quotes if necessary and escaping internal quotes. */
	private static String escapeCsvValue(final String value) {
		if (value == null) {
			return "";
		}
		// Check if value needs quoting
		if (value.contains(CSV_SEPARATOR) || value.contains(CSV_QUOTE) || value.contains("\n") || value.contains("\r")) {
			// Escape internal quotes by doubling them
			final String escaped = value.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE);
			return CSV_QUOTE + escaped + CSV_QUOTE;
		}
		return value;
	}

	/** Finds a field in a class hierarchy. */
	private static Field findField(final Class<?> clazz, final String fieldName) {
		Class<?> currentClass = clazz;
		while (currentClass != null && currentClass != Object.class) {
			try {
				return currentClass.getDeclaredField(fieldName);
			} catch (@SuppressWarnings ("unused") final NoSuchFieldException e) {
				currentClass = currentClass.getSuperclass();
			}
		}
		return null;
	}

	/** Finds a getter method for a field. */
	private static Method findGetter(final Class<?> clazz, final String fieldName) {
		try {
			// Try "get" prefix
			final String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			return clazz.getMethod(getterName);
		} catch (@SuppressWarnings ("unused") final NoSuchMethodException e) {
			try {
				// Try "is" prefix for boolean fields
				final String isGetterName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
				return clazz.getMethod(isGetterName);
			} catch (@SuppressWarnings ("unused") final NoSuchMethodException ex) {
				return null;
			}
		}
	}

	/** Writes the CSV header row. */
	private static void writeCSVHeader(final StringWriter writer, final List<EntityFieldInfo> selectedFields) {
		final List<String> headers = selectedFields.stream().map(field -> escapeCsvValue(field.getDisplayName())).collect(Collectors.toList());
		writer.append(String.join(CSV_SEPARATOR, headers));
		writer.append(CSV_NEWLINE);
	}

	/** Extracts the value of a field from an entity. Handles simple fields and complex/relation fields. */
	private String extractFieldValue(final CEntityDB<?> entity, final EntityFieldInfo fieldInfo, final Class<?> entityClass) throws Exception {
		try {
			final String fieldName = fieldInfo.getFieldName();
			Check.notBlank(fieldName, "Field name cannot be blank");
			// Handle nested fields (e.g., "status.name")
			if (fieldName.contains(".")) {
				return extractNestedFieldValue(entity, fieldName);
			}
			// Get the field value using reflection
			final Field field = findField(entityClass, fieldName);
			if (field == null) {
				LOGGER.debug("Field not found: {} in class {}", fieldName, entityClass.getSimpleName());
				return "";
			}
			field.setAccessible(true);
			final Object value = field.get(entity);
			return formatFieldValue(value);
		} catch (final Exception e) {
			LOGGER.debug("Error extracting field value: {} - {}", fieldInfo.getFieldName(), e.getMessage());
			return "";
		}
	}

	/** Extracts nested field value (e.g., "status.name" from entity.getStatus().getName()). */
	private String extractNestedFieldValue(final CEntityDB<?> entity, final String fieldPath) {
		try {
			final String[] parts = fieldPath.split("\\.");
			Object currentValue = entity;
			for (final String part : parts) {
				if (currentValue == null) {
					return "";
				}
				// Try getter method first
				final Method getter = findGetter(currentValue.getClass(), part);
				if (getter != null) {
					getter.setAccessible(true);
					currentValue = getter.invoke(currentValue);
				} else {
					// Try direct field access
					final Field field = findField(currentValue.getClass(), part);
					if (field != null) {
						field.setAccessible(true);
						currentValue = field.get(currentValue);
					} else {
						return "";
					}
				}
			}
			return formatFieldValue(currentValue);
		} catch (final Exception e) {
			LOGGER.debug("Error extracting nested field value: {} - {}", fieldPath, e.getMessage());
			return "";
		}
	}

	/** Formats a field value for CSV output based on its type. */
	private String formatFieldValue(final Object value) {
		if (value == null) {
			return "";
		}
		// Handle LocalDate
		if (value instanceof LocalDate) {
			return ((LocalDate) value).format(DATE_FORMATTER);
		}
		// Handle LocalDateTime
		if (value instanceof LocalDateTime) {
			return ((LocalDateTime) value).format(DATETIME_FORMATTER);
		}
		// Handle BigDecimal
		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).toPlainString();
		}
		// Handle Collections (show count)
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection<?>) value;
			return collection.size() + " item(s)";
		}
		// Handle CEntityDB references (show name or toString)
		if (value instanceof CEntityDB) {
			try {
				final Method nameMethod = value.getClass().getMethod("getName");
				final Object name = nameMethod.invoke(value);
				return name != null ? name.toString() : value.toString();
			} catch (@SuppressWarnings ("unused") final Exception e) {
				return value.toString();
			}
		}
		// Default: toString
		return value.toString();
	}

	/** Generates a CSV report for the given entities with selected fields.
	 * @param entities       the list of entities to export
	 * @param selectedFields the list of field information to include in the report
	 * @param entityClass    the entity class
	 * @return CSV content as a string
	 * @throws Exception if CSV generation fails */
	public String generateCSV(final List<? extends CEntityDB<?>> entities, final List<EntityFieldInfo> selectedFields, final Class<?> entityClass)
			throws Exception {
		Check.notNull(entities, "Entities list cannot be null");
		Check.notNull(selectedFields, "Selected fields list cannot be null");
		Check.notNull(entityClass, "Entity class cannot be null");
		final StringWriter writer = new StringWriter();
		try {
			// Write CSV header
			writeCSVHeader(writer, selectedFields);
			// Write data rows
			for (final CEntityDB<?> entity : entities) {
				writeCSVRow(writer, entity, selectedFields, entityClass);
			}
			LOGGER.info("Generated CSV report with {} rows and {} columns", entities.size(), selectedFields.size());
			return writer.toString();
		} catch (final Exception e) {
			LOGGER.error("Error generating CSV report: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Writes a single CSV data row. */
	private void writeCSVRow(final StringWriter writer, final CEntityDB<?> entity, final List<EntityFieldInfo> selectedFields,
			final Class<?> entityClass) throws Exception {
		final List<String> values = new ArrayList<>();
		for (final EntityFieldInfo fieldInfo : selectedFields) {
			final String value = extractFieldValue(entity, fieldInfo, entityClass);
			values.add(escapeCsvValue(value));
		}
		writer.append(String.join(CSV_SEPARATOR, values));
		writer.append(CSV_NEWLINE);
	}
}
