package tech.derbent.bab.policybase.filter.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * CFilterCSV - CSV file filter configuration for file input nodes.
 * 
 * NOT a JPA entity - configuration object for runtime filtering.
 * Specifies which rows and columns to process from CSV files.
 * 
 * Features:
 * - Start line filtering (skip headers)
 * - Maximum line limit (prevent memory overflow)
 * - Column selection (specific columns only)
 * - Delimiter configuration (comma or semicolon)
 * - Line range validation
 * 
 * Example usage:
 * <pre>
 * CFilterCSV filter = new CFilterCSV();
 * filter.setName("Production Data Filter");
 * filter.setComment("Processes sensor data CSV files");
 * filter.setStartLineNumber(2);        // Skip first line (header)
 * filter.setMaxLineNumber(1000);       // Process max 1000 lines
 * filter.setColumnNumbers(List.of(1, 3, 5)); // Only columns 1, 3, 5
 * filter.setDelimiter(",");             // Comma-separated
 * 
 * // Apply filter to CSV processing
 * String json = filter.toJson();
 * // Store in node configuration
 * </pre>
 * 
 * CSV line numbering:
 * - Line numbers start at 1 (not 0)
 * - startLineNumber=1 means process from first line
 * - startLineNumber=2 means skip first line (header)
 * - maxLineNumber=0 means no limit (process all lines)
 */
public class CFilterCSV extends CFilterBase {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFilterCSV.class);
	
	public static final String FILTER_TYPE = "CSV";
	public static final String DELIMITER_COMMA = ",";
	public static final String DELIMITER_SEMICOLON = ";";
	public static final String DEFAULT_CAPTURE_COLUMN_RANGE = "1";
	public static final String DEFAULT_LINE_REGULAR_EXPRESSION = ".*";

	// CSV-specific fields
	private Integer startLineNumber = 1;      // First line to process (1-based)
	private Integer maxLineNumber = 0;         // Maximum lines to process (0 = unlimited)
	private List<Integer> columnNumbers = new ArrayList<>();  // Column indices to include (1-based, empty = all)
	private String delimiter = DELIMITER_COMMA; // CSV delimiter
	private String captureColumnRange = DEFAULT_CAPTURE_COLUMN_RANGE; // Capture column range (N or N-M)
	private String lineRegularExpression = DEFAULT_LINE_REGULAR_EXPRESSION; // Regex for matching lines to process
	private Boolean hasHeader = true;          // Whether first line is header
	private Boolean trimWhitespace = true;     // Trim whitespace from fields
	private transient Pattern compiledLinePattern = Pattern.compile(DEFAULT_LINE_REGULAR_EXPRESSION);
	
	/**
	 * Default constructor for CSV filter.
	 */
	public CFilterCSV() {
		super();
	}
	
	/**
	 * Create CSV filter from JSON string.
	 * 
	 * @param jsonString JSON string representation
	 * @return CFilterCSV instance
	 */
	public static CFilterCSV createFromJson(final String jsonString) {
		final CFilterCSV filter = new CFilterCSV();
		filter.fromJson(jsonString);
		return filter;
	}
	
	/**
	 * Create CSV filter from JsonObject.
	 * 
	 * @param json JsonObject representation
	 * @return CFilterCSV instance
	 */
	public static CFilterCSV createFromJsonObject(final JsonObject json) {
		final CFilterCSV filter = new CFilterCSV();
		filter.fromJson(json);
		return filter;
	}
	
	@Override
	public String getFilterType() {
		return FILTER_TYPE;
	}
	
	@Override
	protected void addSpecificFieldsToJson(final JsonObject json) {
		// Add CSV-specific fields
		json.addProperty("startLineNumber", startLineNumber != null ? startLineNumber : 1);
		json.addProperty("maxLineNumber", maxLineNumber != null ? maxLineNumber : 0);
		json.addProperty("delimiter", delimiter != null ? delimiter : DELIMITER_COMMA);
		json.addProperty("columnSeparator", delimiter != null ? delimiter : DELIMITER_COMMA);
		json.addProperty("captureColumnRange", captureColumnRange != null ? captureColumnRange : DEFAULT_CAPTURE_COLUMN_RANGE);
		json.addProperty("lineRegularExpression", lineRegularExpression != null ? lineRegularExpression : DEFAULT_LINE_REGULAR_EXPRESSION);
		json.addProperty("hasHeader", hasHeader != null ? hasHeader : true);
		json.addProperty("trimWhitespace", trimWhitespace != null ? trimWhitespace : true);
		
		// Add column numbers array
		final JsonArray columnsArray = new JsonArray();
		if (columnNumbers != null) {
			for (final Integer col : columnNumbers) {
				columnsArray.add(col);
			}
		}
		json.add("columnNumbers", columnsArray);
	}
	
	@Override
	protected void parseSpecificFieldsFromJson(final JsonObject json) {
		try {
			// Parse CSV-specific fields
			if (json.has("startLineNumber") && !json.get("startLineNumber").isJsonNull()) {
				startLineNumber = json.get("startLineNumber").getAsInt();
			}
			
			if (json.has("maxLineNumber") && !json.get("maxLineNumber").isJsonNull()) {
				maxLineNumber = json.get("maxLineNumber").getAsInt();
			}
			
			if (json.has("columnSeparator") && !json.get("columnSeparator").isJsonNull()) {
				setDelimiter(json.get("columnSeparator").getAsString());
			} else if (json.has("delimiter") && !json.get("delimiter").isJsonNull()) {
				setDelimiter(json.get("delimiter").getAsString());
			}
			
			if (json.has("captureColumnRange") && !json.get("captureColumnRange").isJsonNull()) {
				captureColumnRange = json.get("captureColumnRange").getAsString();
			}
			
			if (json.has("lineRegularExpression") && !json.get("lineRegularExpression").isJsonNull()) {
				lineRegularExpression = json.get("lineRegularExpression").getAsString();
			}
			
			if (json.has("hasHeader") && !json.get("hasHeader").isJsonNull()) {
				hasHeader = json.get("hasHeader").getAsBoolean();
			}
			
			if (json.has("trimWhitespace") && !json.get("trimWhitespace").isJsonNull()) {
				trimWhitespace = json.get("trimWhitespace").getAsBoolean();
			}
			
			// Parse column numbers array
			if (json.has("columnNumbers") && json.get("columnNumbers").isJsonArray()) {
				columnNumbers = new ArrayList<>();
				final JsonArray columnsArray = json.getAsJsonArray("columnNumbers");
				for (final JsonElement element : columnsArray) {
					if (!element.isJsonNull()) {
						columnNumbers.add(element.getAsInt());
					}
				}
			}
			
			// Validate ranges
			validateConfiguration();
			
		} catch (final Exception e) {
			LOGGER.error("Failed to parse CSV-specific fields: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Validate filter configuration.
	 * Ensures line numbers and column numbers are valid.
	 */
	private void validateConfiguration() {
		// Validate start line number (must be >= 1)
		if (startLineNumber != null && startLineNumber < 1) {
			LOGGER.warn("Invalid startLineNumber {}, setting to 1", startLineNumber);
			startLineNumber = 1;
		}
		
		// Validate max line number (must be >= 0)
		if (maxLineNumber != null && maxLineNumber < 0) {
			LOGGER.warn("Invalid maxLineNumber {}, setting to 0 (unlimited)", maxLineNumber);
			maxLineNumber = 0;
		}
		
		// Validate that maxLineNumber >= startLineNumber (if maxLineNumber is set)
		if (maxLineNumber != null && maxLineNumber > 0 && 
		    startLineNumber != null && maxLineNumber < startLineNumber) {
			LOGGER.warn("maxLineNumber {} is less than startLineNumber {}, correcting", 
				maxLineNumber, startLineNumber);
			maxLineNumber = startLineNumber;
		}
		
		// Validate column numbers (must be >= 1)
		if (columnNumbers != null) {
			columnNumbers.removeIf(col -> {
				if (col < 1) {
					LOGGER.warn("Invalid column number {}, removing", col);
					return true;
				}
				return false;
			});
		}
		
		// Validate capture column range (must be N or N-M with positive values)
		if (captureColumnRange == null || captureColumnRange.isBlank()) {
			captureColumnRange = DEFAULT_CAPTURE_COLUMN_RANGE;
		}
		captureColumnRange = captureColumnRange.trim();
		if (!captureColumnRange.matches("^\\d+(?:-\\d+)?$")) {
			LOGGER.warn("Invalid captureColumnRange '{}', defaulting to {}", captureColumnRange, DEFAULT_CAPTURE_COLUMN_RANGE);
			captureColumnRange = DEFAULT_CAPTURE_COLUMN_RANGE;
		} else if (captureColumnRange.contains("-")) {
			try {
				final String[] parts = captureColumnRange.split("-", 2);
				final int startColumn = Integer.parseInt(parts[0]);
				final int endColumn = Integer.parseInt(parts[1]);
				if (startColumn < 1 || endColumn < 1 || endColumn < startColumn) {
					LOGGER.warn("Invalid captureColumnRange '{}', defaulting to {}", captureColumnRange, DEFAULT_CAPTURE_COLUMN_RANGE);
					captureColumnRange = DEFAULT_CAPTURE_COLUMN_RANGE;
				}
			} catch (final NumberFormatException ex) {
				LOGGER.warn("Invalid captureColumnRange '{}', defaulting to {}", captureColumnRange, DEFAULT_CAPTURE_COLUMN_RANGE);
				captureColumnRange = DEFAULT_CAPTURE_COLUMN_RANGE;
			}
		}
		
		// Validate line regex and cache compiled pattern
		if (lineRegularExpression == null || lineRegularExpression.isBlank()) {
			lineRegularExpression = DEFAULT_LINE_REGULAR_EXPRESSION;
		}
		try {
			compiledLinePattern = Pattern.compile(lineRegularExpression);
		} catch (final PatternSyntaxException ex) {
			LOGGER.warn("Invalid lineRegularExpression '{}', defaulting to {}", lineRegularExpression, DEFAULT_LINE_REGULAR_EXPRESSION);
			lineRegularExpression = DEFAULT_LINE_REGULAR_EXPRESSION;
			compiledLinePattern = Pattern.compile(DEFAULT_LINE_REGULAR_EXPRESSION);
		}
	}
	
	/**
	 * Check if a line number should be processed based on filter settings.
	 * 
	 * @param lineNumber line number to check (1-based)
	 * @return true if line should be processed
	 */
	public boolean shouldProcessLine(final int lineNumber) {
		// Check start line
		if (startLineNumber != null && lineNumber < startLineNumber) {
			return false;
		}
		
		// Check max line (0 = unlimited)
		if (maxLineNumber != null && maxLineNumber > 0 && lineNumber > maxLineNumber) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if a line should be processed using both line number and line content.
	 * 
	 * @param lineNumber line number to check (1-based)
	 * @param lineContent line content to validate against regex
	 * @return true if line should be processed
	 */
	public boolean shouldProcessLine(final int lineNumber, final String lineContent) {
		return shouldProcessLine(lineNumber) && matchesLineRegularExpression(lineContent);
	}
	
	/**
	 * Check if line content matches configured line regular expression.
	 * 
	 * @param lineContent line content to evaluate
	 * @return true if line matches the configured regex
	 */
	public boolean matchesLineRegularExpression(final String lineContent) {
		if (lineContent == null) {
			return false;
		}
		if (compiledLinePattern == null) {
			validateConfiguration();
		}
		return compiledLinePattern.matcher(lineContent).matches();
	}
	
	/**
	 * Check if a column should be included based on filter settings.
	 * 
	 * @param columnNumber column number to check (1-based)
	 * @return true if column should be included
	 */
	public boolean shouldIncludeColumn(final int columnNumber) {
		// Explicit column list has precedence when configured
		if (columnNumbers != null && !columnNumbers.isEmpty()) {
			return columnNumbers.contains(columnNumber);
		}
		
		// Fallback to capture column range
		return isColumnInCaptureRange(columnNumber);
	}
	
	private boolean isColumnInCaptureRange(final int columnNumber) {
		if (columnNumber < 1) {
			return false;
		}
		if (captureColumnRange == null || captureColumnRange.isBlank()) {
			return true;
		}
		try {
			if (!captureColumnRange.contains("-")) {
				return Integer.parseInt(captureColumnRange) == columnNumber;
			}
			final String[] parts = captureColumnRange.split("-", 2);
			final int startColumn = Integer.parseInt(parts[0]);
			final int endColumn = Integer.parseInt(parts[1]);
			return columnNumber >= startColumn && columnNumber <= endColumn;
		} catch (final NumberFormatException ex) {
			LOGGER.warn("Unable to evaluate captureColumnRange '{}', defaulting to include column {}", captureColumnRange, columnNumber);
			return true;
		}
	}
	
	/**
	 * Get total number of lines to process.
	 * 
	 * @return number of lines, or -1 if unlimited
	 */
	public int getTotalLinesToProcess() {
		if (maxLineNumber == null || maxLineNumber == 0) {
			return -1; // Unlimited
		}
		
		if (startLineNumber == null) {
			return maxLineNumber;
		}
		
		return maxLineNumber - startLineNumber + 1;
	}
	
	// Getters and setters
	public Integer getStartLineNumber() {
		return startLineNumber;
	}
	
	public void setStartLineNumber(final Integer startLineNumber) {
		this.startLineNumber = startLineNumber;
		validateConfiguration();
	}
	
	public Integer getMaxLineNumber() {
		return maxLineNumber;
	}
	
	public void setMaxLineNumber(final Integer maxLineNumber) {
		this.maxLineNumber = maxLineNumber;
		validateConfiguration();
	}
	
	public List<Integer> getColumnNumbers() {
		return columnNumbers;
	}
	
	public void setColumnNumbers(final List<Integer> columnNumbers) {
		this.columnNumbers = columnNumbers != null ? new ArrayList<>(columnNumbers) : new ArrayList<>();
		validateConfiguration();
	}
	
	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(final String delimiter) {
		if (DELIMITER_COMMA.equals(delimiter) || DELIMITER_SEMICOLON.equals(delimiter)) {
			this.delimiter = delimiter;
		} else {
			LOGGER.warn("Invalid delimiter '{}', keeping current '{}'", delimiter, this.delimiter);
		}
	}
	
	public String getColumnSeparator() {
		return delimiter;
	}
	
	public void setColumnSeparator(final String columnSeparator) {
		setDelimiter(columnSeparator);
	}
	
	public String getCaptureColumnRange() {
		return captureColumnRange;
	}
	
	public void setCaptureColumnRange(final String captureColumnRange) {
		this.captureColumnRange = captureColumnRange;
		validateConfiguration();
	}
	
	public String getLineRegularExpression() {
		return lineRegularExpression;
	}
	
	public void setLineRegularExpression(final String lineRegularExpression) {
		this.lineRegularExpression = lineRegularExpression;
		validateConfiguration();
	}
	
	public Boolean getHasHeader() {
		return hasHeader;
	}
	
	public void setHasHeader(final Boolean hasHeader) {
		this.hasHeader = hasHeader;
	}
	
	public Boolean getTrimWhitespace() {
		return trimWhitespace;
	}
	
	public void setTrimWhitespace(final Boolean trimWhitespace) {
		this.trimWhitespace = trimWhitespace;
	}
	
	@Override
	public String toString() {
		return String.format("CFilterCSV{name='%s', lines=%d-%d, columns=%s, captureRange='%s', delimiter='%s', lineRegex='%s'}",
			getName(), 
			startLineNumber != null ? startLineNumber : 1, 
			maxLineNumber != null && maxLineNumber > 0 ? maxLineNumber : -1,
			columnNumbers != null && !columnNumbers.isEmpty() ? columnNumbers : "all",
			captureColumnRange,
			delimiter,
			lineRegularExpression);
	}
}
