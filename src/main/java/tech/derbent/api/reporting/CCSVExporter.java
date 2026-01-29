package tech.derbent.api.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Using deprecated StreamResource - Vaadin 24 migration in progress
// TODO: Replace with StreamResourceWriter when Vaadin provides stable API
import com.vaadin.flow.server.StreamResource;
import tech.derbent.api.utils.Check;

/** CCSVExporter - Utility for exporting data to CSV format.
 * <p>
 * Follows RFC 4180 CSV standard with proper escaping and null handling.
 * </p>
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>RFC 4180 compliant CSV generation</li>
 * <li>Proper quote escaping</li>
 * <li>Null value handling</li>
 * <li>UTF-8 encoding with BOM for Excel compatibility</li>
 * <li>Automatic filename generation with timestamp</li>
 * <li>StreamResource for Vaadin download</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage:</b>
 *
 * <pre>
 * List&lt;CActivity&gt; data = activityService.findAll();
 * List&lt;CReportFieldDescriptor&gt; fields = CReportFieldDescriptor.discoverFields(CActivity.class);
 * StreamResource csv = CCSVExporter.exportToCSV(data, fields, "activities");
 * Anchor download = new Anchor(csv, "");
 * download.getElement().setAttribute("download", true);
 * Button downloadBtn = new Button("Download CSV");
 * download.add(downloadBtn);
 * </pre>
 * </p>
 * Layer: Reporting (API) */
public final class CCSVExporter {

	private static final String CSV_DELIMITER = ",";
	private static final String CSV_NEWLINE = "\r\n";
	private static final String CSV_QUOTE = "\"";
	private static final String CSV_QUOTE_ESCAPED = "\"\"";
	private static final Logger LOGGER = LoggerFactory.getLogger(CCSVExporter.class);
	private static final byte[] UTF8_BOM = {
			(byte) 0xEF, (byte) 0xBB, (byte) 0xBF
	};

	/** Escapes a CSV value according to RFC 4180.
	 * <p>
	 * Rules:
	 * <ul>
	 * <li>Wrap in quotes if value contains comma, quote, or newline</li>
	 * <li>Escape quotes by doubling them</li>
	 * <li>Null or empty values return empty string</li>
	 * </ul>
	 * </p>
	 * @param value the value to escape
	 * @return escaped CSV value */
	private static String escapeCsvValue(final String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		// Check if quoting is needed
		final boolean needsQuoting = value.contains(CSV_DELIMITER) || value.contains(CSV_QUOTE) || value.contains("\n") || value.contains("\r");
		if (needsQuoting) {
			// Escape quotes by doubling them, then wrap in quotes
			final String escaped = value.replace(CSV_QUOTE, CSV_QUOTE_ESCAPED);
			return CSV_QUOTE + escaped + CSV_QUOTE;
		}
		return value;
	}

	/** Exports data to CSV format and returns a StreamResource for download.
	 * @param data         the list of entities to export
	 * @param fields       the field descriptors defining columns
	 * @param baseFileName the base filename (without extension)
	 * @return StreamResource for downloading the CSV file */
	public static <T> StreamResource exportToCSV(final List<T> data, final List<CReportFieldDescriptor> fields, final String baseFileName) {
		Check.notNull(data, "Data list cannot be null");
		Check.notNull(fields, "Fields list cannot be null");
		Check.notNull(baseFileName, "Base filename cannot be null");
		if (fields.isEmpty()) {
			throw new IllegalArgumentException("At least one field must be selected for export");
		}
		final String filename = generateFilename(baseFileName);
		return new StreamResource(filename, () -> {
			try {
				return generateCSVStream(data, fields);
			} catch (final IOException e) {
				LOGGER.error("Error generating CSV stream", e);
				throw new RuntimeException("Failed to generate CSV file", e);
			}
		});
	}

	/** Generates the CSV content as an InputStream.
	 * @param data   the data to export
	 * @param fields the field descriptors
	 * @return InputStream containing CSV data
	 * @throws IOException if writing fails */
	private static <T> ByteArrayInputStream generateCSVStream(final List<T> data, final List<CReportFieldDescriptor> fields) throws IOException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Write UTF-8 BOM for Excel compatibility
		outputStream.write(UTF8_BOM);
		try (final OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			// Write header row
			writeHeader(writer, fields);
			// Write data rows
			for (final T entity : data) {
				if (entity != null) {
					writeDataRow(writer, entity, fields);
				}
			}
			writer.flush();
		}
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	/** Generates a filename with timestamp.
	 * @param baseFileName the base filename
	 * @return filename with timestamp and .csv extension */
	private static String generateFilename(final String baseFileName) {
		final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		return String.format("%s_%s.csv", baseFileName, timestamp);
	}

	/** Writes a single data row.
	 * @param writer the writer
	 * @param entity the entity to write
	 * @param fields the field descriptors
	 * @throws IOException if writing fails */
	private static <T> void writeDataRow(final OutputStreamWriter writer, final T entity, final List<CReportFieldDescriptor> fields)
			throws IOException {
		boolean first = true;
		for (final CReportFieldDescriptor field : fields) {
			if (!first) {
				writer.write(CSV_DELIMITER);
			}
			first = false;
			final String value = field.extractValue(entity);
			writer.write(escapeCsvValue(value));
		}
		writer.write(CSV_NEWLINE);
	}

	/** Writes the CSV header row.
	 * @param writer the writer
	 * @param fields the field descriptors
	 * @throws IOException if writing fails */
	private static void writeHeader(final OutputStreamWriter writer, final List<CReportFieldDescriptor> fields) throws IOException {
		boolean first = true;
		for (final CReportFieldDescriptor field : fields) {
			if (!first) {
				writer.write(CSV_DELIMITER);
			}
			first = false;
			// Build header: "Group - Field" or just "Field"
			final String header;
			if (field.getGroupName() != null && !field.getGroupName().isEmpty()) {
				header = field.getGroupName() + " - " + field.getDisplayName();
			} else {
				header = field.getDisplayName();
			}
			writer.write(escapeCsvValue(header));
		}
		writer.write(CSV_NEWLINE);
	}

	private CCSVExporter() {
		// Utility class - prevent instantiation
	}
}
