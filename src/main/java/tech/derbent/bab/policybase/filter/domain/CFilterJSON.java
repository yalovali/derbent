package tech.derbent.bab.policybase.filter.domain;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * CFilterJSON - JSON file filter configuration for file input nodes.
 * 
 * NOT a JPA entity - configuration object for runtime filtering.
 * Specifies JSON paths, array limits, and field selection.
 * 
 * Features:
 * - JSONPath filtering (extract specific paths)
 * - Array element limits (prevent memory overflow)
 * - Field inclusion/exclusion
 * - Nested object depth limits
 * 
 * Example usage:
 * <pre>
 * CFilterJSON filter = new CFilterJSON();
 * filter.setName("Sensor Data Filter");
 * filter.setComment("Filters sensor JSON messages");
 * filter.setRootPath("$.data.sensors"); // Extract from data.sensors
 * filter.setMaxArrayElements(100);      // Limit arrays to 100 elements
 * filter.setIncludedFields(List.of("temperature", "humidity", "timestamp"));
 * 
 * String json = filter.toJson();
 * // Store in node configuration
 * </pre>
 */
public class CFilterJSON extends CFilterBase {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFilterJSON.class);
	
	public static final String FILTER_TYPE = "JSON";
	
	// JSON-specific fields
	private String rootPath = "$";                     // JSONPath root ($ = document root)
	private Integer maxArrayElements = 0;              // Max elements per array (0 = unlimited)
	private Integer maxNestingDepth = 0;               // Max nesting depth (0 = unlimited)
	private List<String> includedFields = new ArrayList<>();  // Fields to include (empty = all)
	private List<String> excludedFields = new ArrayList<>();  // Fields to exclude
	private Boolean validateSchema = false;            // Validate against JSON schema
	private Boolean flattenArrays = false;             // Flatten nested arrays
	
	/**
	 * Default constructor for JSON filter.
	 */
	public CFilterJSON() {
		super();
	}
	
	/**
	 * Create JSON filter from JSON string.
	 * 
	 * @param jsonString JSON string representation
	 * @return CFilterJSON instance
	 */
	public static CFilterJSON createFromJson(final String jsonString) {
		final CFilterJSON filter = new CFilterJSON();
		filter.fromJson(jsonString);
		return filter;
	}
	
	/**
	 * Create JSON filter from JsonObject.
	 * 
	 * @param json JsonObject representation
	 * @return CFilterJSON instance
	 */
	public static CFilterJSON createFromJsonObject(final JsonObject json) {
		final CFilterJSON filter = new CFilterJSON();
		filter.fromJson(json);
		return filter;
	}
	
	@Override
	public String getFilterType() {
		return FILTER_TYPE;
	}
	
	@Override
	protected void addSpecificFieldsToJson(final JsonObject json) {
		// Add JSON-specific fields
		json.addProperty("rootPath", rootPath != null ? rootPath : "$");
		json.addProperty("maxArrayElements", maxArrayElements != null ? maxArrayElements : 0);
		json.addProperty("maxNestingDepth", maxNestingDepth != null ? maxNestingDepth : 0);
		json.addProperty("validateSchema", validateSchema != null ? validateSchema : false);
		json.addProperty("flattenArrays", flattenArrays != null ? flattenArrays : false);
		
		// Add included fields array
		final JsonArray includedArray = new JsonArray();
		if (includedFields != null) {
			for (final String field : includedFields) {
				includedArray.add(field);
			}
		}
		json.add("includedFields", includedArray);
		
		// Add excluded fields array
		final JsonArray excludedArray = new JsonArray();
		if (excludedFields != null) {
			for (final String field : excludedFields) {
				excludedArray.add(field);
			}
		}
		json.add("excludedFields", excludedArray);
	}
	
	@Override
	protected void parseSpecificFieldsFromJson(final JsonObject json) {
		try {
			// Parse JSON-specific fields
			if (json.has("rootPath") && !json.get("rootPath").isJsonNull()) {
				rootPath = json.get("rootPath").getAsString();
			}
			
			if (json.has("maxArrayElements") && !json.get("maxArrayElements").isJsonNull()) {
				maxArrayElements = json.get("maxArrayElements").getAsInt();
			}
			
			if (json.has("maxNestingDepth") && !json.get("maxNestingDepth").isJsonNull()) {
				maxNestingDepth = json.get("maxNestingDepth").getAsInt();
			}
			
			if (json.has("validateSchema") && !json.get("validateSchema").isJsonNull()) {
				validateSchema = json.get("validateSchema").getAsBoolean();
			}
			
			if (json.has("flattenArrays") && !json.get("flattenArrays").isJsonNull()) {
				flattenArrays = json.get("flattenArrays").getAsBoolean();
			}
			
			// Parse included fields array
			if (json.has("includedFields") && json.get("includedFields").isJsonArray()) {
				includedFields = new ArrayList<>();
				final JsonArray includedArray = json.getAsJsonArray("includedFields");
				for (final JsonElement element : includedArray) {
					if (!element.isJsonNull()) {
						includedFields.add(element.getAsString());
					}
				}
			}
			
			// Parse excluded fields array
			if (json.has("excludedFields") && json.get("excludedFields").isJsonArray()) {
				excludedFields = new ArrayList<>();
				final JsonArray excludedArray = json.getAsJsonArray("excludedFields");
				for (final JsonElement element : excludedArray) {
					if (!element.isJsonNull()) {
						excludedFields.add(element.getAsString());
					}
				}
			}
			
		} catch (final Exception e) {
			LOGGER.error("Failed to parse JSON-specific fields: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Check if a field should be included based on filter settings.
	 * 
	 * @param fieldName field name to check
	 * @return true if field should be included
	 */
	public boolean shouldIncludeField(final String fieldName) {
		// Check exclusions first
		if (excludedFields != null && excludedFields.contains(fieldName)) {
			return false;
		}
		
		// If no inclusions specified, include all (except excluded)
		if (includedFields == null || includedFields.isEmpty()) {
			return true;
		}
		
		// Check if field is in inclusion list
		return includedFields.contains(fieldName);
	}
	
	// Getters and setters
	public String getRootPath() {
		return rootPath;
	}
	
	public void setRootPath(final String rootPath) {
		this.rootPath = rootPath;
	}
	
	public Integer getMaxArrayElements() {
		return maxArrayElements;
	}
	
	public void setMaxArrayElements(final Integer maxArrayElements) {
		this.maxArrayElements = maxArrayElements;
	}
	
	public Integer getMaxNestingDepth() {
		return maxNestingDepth;
	}
	
	public void setMaxNestingDepth(final Integer maxNestingDepth) {
		this.maxNestingDepth = maxNestingDepth;
	}
	
	public List<String> getIncludedFields() {
		return includedFields;
	}
	
	public void setIncludedFields(final List<String> includedFields) {
		this.includedFields = includedFields != null ? new ArrayList<>(includedFields) : new ArrayList<>();
	}
	
	public List<String> getExcludedFields() {
		return excludedFields;
	}
	
	public void setExcludedFields(final List<String> excludedFields) {
		this.excludedFields = excludedFields != null ? new ArrayList<>(excludedFields) : new ArrayList<>();
	}
	
	public Boolean getValidateSchema() {
		return validateSchema;
	}
	
	public void setValidateSchema(final Boolean validateSchema) {
		this.validateSchema = validateSchema;
	}
	
	public Boolean getFlattenArrays() {
		return flattenArrays;
	}
	
	public void setFlattenArrays(final Boolean flattenArrays) {
		this.flattenArrays = flattenArrays;
	}
	
	@Override
	public String toString() {
		return String.format("CFilterJSON{name='%s', rootPath='%s', maxArrayElements=%d, fields=%s}", 
			getName(), 
			rootPath,
			maxArrayElements != null ? maxArrayElements : 0,
			includedFields != null && !includedFields.isEmpty() ? includedFields : "all");
	}
}
