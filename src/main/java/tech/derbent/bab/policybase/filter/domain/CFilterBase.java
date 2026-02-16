package tech.derbent.bab.policybase.filter.domain;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tech.derbent.api.utils.Check;

/**
 * CFilterBase - Base class for node input filters with JSON serialization.
 * 
 * NOT a JPA entity - configuration objects without database persistence.
 * Used by BAB nodes to filter input data of specific types (CSV, JSON, XML, etc.).
 * 
 * Pattern:
 * - Serializable for Vaadin session storage
 * - toJson()/fromJson() for configuration persistence
 * - Recursive parsing for nested filter structures
 * - Type-safe filter implementations via subclasses
 * 
 * Example usage:
 * <pre>
 * // Create CSV filter
 * CFilterCSV csvFilter = new CFilterCSV();
 * csvFilter.setName("Production Data Filter");
 * csvFilter.setComment("Filters production CSV files");
 * csvFilter.setStartLineNumber(2); // Skip header
 * csvFilter.setMaxLineNumber(1000); // Limit to 1000 lines
 * 
 * // Serialize to JSON
 * String json = csvFilter.toJson();
 * 
 * // Deserialize from JSON
 * CFilterCSV restored = CFilterCSV.fromJson(json);
 * </pre>
 */
public abstract class CFilterBase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFilterBase.class);
	
	// Common fields for all filters
	private String name = "";
	private String comment = "";
	private Boolean enabled = true;
	
	/**
	 * Default constructor for filter base.
	 */
	protected CFilterBase() {
		super();
	}
	
	/**
	 * Get filter type discriminator for JSON serialization.
	 * Subclasses MUST override to return unique type identifier.
	 * 
	 * @return filter type (e.g., "CSV", "JSON", "XML")
	 */
	public abstract String getFilterType();
	
	/**
	 * Convert this filter to JSON string.
	 * Includes common fields (name, comment, enabled) and type-specific fields.
	 * Subclasses override to add specific fields.
	 * 
	 * @return JSON string representation
	 */
	public String toJson() {
		try {
			final JsonObject json = new JsonObject();
			
			// Add common fields
			json.addProperty("filterType", getFilterType());
			json.addProperty("name", name != null ? name : "");
			json.addProperty("comment", comment != null ? comment : "");
			json.addProperty("enabled", enabled != null ? enabled : true);
			
			// Let subclasses add type-specific fields
			addSpecificFieldsToJson(json);
			
			return json.toString();
		} catch (final Exception e) {
			LOGGER.error("Failed to serialize filter to JSON: {}", e.getMessage(), e);
			return "{}";
		}
	}
	
	/**
	 * Subclasses override to add type-specific fields to JSON.
	 * 
	 * @param json JsonObject to add fields to
	 */
	protected abstract void addSpecificFieldsToJson(final JsonObject json);
	
	/**
	 * Parse JSON string and populate this filter.
	 * Parses common fields and delegates to subclass for specific fields.
	 * 
	 * @param jsonString JSON string to parse
	 */
	public void fromJson(final String jsonString) {
		try {
			Check.notBlank(jsonString, "JSON string cannot be blank");
			
			final JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
			fromJson(json);
		} catch (final Exception e) {
			LOGGER.error("Failed to parse filter from JSON string: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Parse JsonObject and populate this filter.
	 * Parses common fields and delegates to subclass for specific fields.
	 * 
	 * @param json JsonObject to parse
	 */
	public void fromJson(final JsonObject json) {
		try {
			if (json == null) {
				LOGGER.warn("Null JSON object passed to fromJson()");
				return;
			}
			
			// Parse common fields
			if (json.has("name") && !json.get("name").isJsonNull()) {
				name = json.get("name").getAsString();
			}
			
			if (json.has("comment") && !json.get("comment").isJsonNull()) {
				comment = json.get("comment").getAsString();
			}
			
			if (json.has("enabled") && !json.get("enabled").isJsonNull()) {
				enabled = json.get("enabled").getAsBoolean();
			}
			
			// Let subclasses parse type-specific fields
			parseSpecificFieldsFromJson(json);
			
			LOGGER.debug("Parsed filter '{}' (type: {})", name, getFilterType());
		} catch (final Exception e) {
			LOGGER.error("Failed to parse filter from JSON: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Subclasses override to parse type-specific fields from JSON.
	 * 
	 * @param json JsonObject containing fields to parse
	 */
	protected abstract void parseSpecificFieldsFromJson(final JsonObject json);
	
	// Getters and setters
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public String toString() {
		return String.format("%s{name='%s', enabled=%s}", getFilterType(), name, enabled);
	}
}
