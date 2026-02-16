package tech.derbent.bab.policybase.filter.domain;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * CFilterXML - XML file filter configuration for file input nodes.
 * 
 * NOT a JPA entity - configuration object for runtime filtering.
 * Specifies XPath queries, element limits, and attribute selection.
 * 
 * Features:
 * - XPath filtering (extract specific elements)
 * - Element depth limits
 * - Attribute inclusion/exclusion
 * - Namespace handling
 * 
 * Example usage:
 * <pre>
 * CFilterXML filter = new CFilterXML();
 * filter.setName("SOAP Message Filter");
 * filter.setComment("Filters SOAP envelope data");
 * filter.setXPathQuery("//soap:Body/sensor:reading"); // Extract sensor readings
 * filter.setMaxElementDepth(5);                       // Limit nesting
 * filter.setIncludedAttributes(List.of("timestamp", "value", "unit"));
 * 
 * String json = filter.toJson();
 * // Store in node configuration
 * </pre>
 */
public class CFilterXML extends CFilterBase {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CFilterXML.class);
	
	public static final String FILTER_TYPE = "XML";
	
	// XML-specific fields
	private String xpathQuery = "/";                   // XPath query (/ = root)
	private Integer maxElementDepth = 0;               // Max element nesting (0 = unlimited)
	private Integer maxElements = 0;                   // Max elements to process (0 = unlimited)
	private List<String> includedAttributes = new ArrayList<>();  // Attributes to include (empty = all)
	private List<String> excludedAttributes = new ArrayList<>();  // Attributes to exclude
	private Boolean preserveNamespaces = true;         // Preserve namespace prefixes
	private Boolean validateXSD = false;               // Validate against XSD schema
	private Boolean stripWhitespace = true;            // Strip text whitespace
	
	/**
	 * Default constructor for XML filter.
	 */
	public CFilterXML() {
		super();
	}
	
	/**
	 * Create XML filter from JSON string.
	 * 
	 * @param jsonString JSON string representation
	 * @return CFilterXML instance
	 */
	public static CFilterXML createFromJson(final String jsonString) {
		final CFilterXML filter = new CFilterXML();
		filter.fromJson(jsonString);
		return filter;
	}
	
	/**
	 * Create XML filter from JsonObject.
	 * 
	 * @param json JsonObject representation
	 * @return CFilterXML instance
	 */
	public static CFilterXML createFromJsonObject(final JsonObject json) {
		final CFilterXML filter = new CFilterXML();
		filter.fromJson(json);
		return filter;
	}
	
	@Override
	public String getFilterType() {
		return FILTER_TYPE;
	}
	
	@Override
	protected void addSpecificFieldsToJson(final JsonObject json) {
		// Add XML-specific fields
		json.addProperty("xpathQuery", xpathQuery != null ? xpathQuery : "/");
		json.addProperty("maxElementDepth", maxElementDepth != null ? maxElementDepth : 0);
		json.addProperty("maxElements", maxElements != null ? maxElements : 0);
		json.addProperty("preserveNamespaces", preserveNamespaces != null ? preserveNamespaces : true);
		json.addProperty("validateXSD", validateXSD != null ? validateXSD : false);
		json.addProperty("stripWhitespace", stripWhitespace != null ? stripWhitespace : true);
		
		// Add included attributes array
		final JsonArray includedArray = new JsonArray();
		if (includedAttributes != null) {
			for (final String attr : includedAttributes) {
				includedArray.add(attr);
			}
		}
		json.add("includedAttributes", includedArray);
		
		// Add excluded attributes array
		final JsonArray excludedArray = new JsonArray();
		if (excludedAttributes != null) {
			for (final String attr : excludedAttributes) {
				excludedArray.add(attr);
			}
		}
		json.add("excludedAttributes", excludedArray);
	}
	
	@Override
	protected void parseSpecificFieldsFromJson(final JsonObject json) {
		try {
			// Parse XML-specific fields
			if (json.has("xpathQuery") && !json.get("xpathQuery").isJsonNull()) {
				xpathQuery = json.get("xpathQuery").getAsString();
			}
			
			if (json.has("maxElementDepth") && !json.get("maxElementDepth").isJsonNull()) {
				maxElementDepth = json.get("maxElementDepth").getAsInt();
			}
			
			if (json.has("maxElements") && !json.get("maxElements").isJsonNull()) {
				maxElements = json.get("maxElements").getAsInt();
			}
			
			if (json.has("preserveNamespaces") && !json.get("preserveNamespaces").isJsonNull()) {
				preserveNamespaces = json.get("preserveNamespaces").getAsBoolean();
			}
			
			if (json.has("validateXSD") && !json.get("validateXSD").isJsonNull()) {
				validateXSD = json.get("validateXSD").getAsBoolean();
			}
			
			if (json.has("stripWhitespace") && !json.get("stripWhitespace").isJsonNull()) {
				stripWhitespace = json.get("stripWhitespace").getAsBoolean();
			}
			
			// Parse included attributes array
			if (json.has("includedAttributes") && json.get("includedAttributes").isJsonArray()) {
				includedAttributes = new ArrayList<>();
				final JsonArray includedArray = json.getAsJsonArray("includedAttributes");
				for (final JsonElement element : includedArray) {
					if (!element.isJsonNull()) {
						includedAttributes.add(element.getAsString());
					}
				}
			}
			
			// Parse excluded attributes array
			if (json.has("excludedAttributes") && json.get("excludedAttributes").isJsonArray()) {
				excludedAttributes = new ArrayList<>();
				final JsonArray excludedArray = json.getAsJsonArray("excludedAttributes");
				for (final JsonElement element : excludedArray) {
					if (!element.isJsonNull()) {
						excludedAttributes.add(element.getAsString());
					}
				}
			}
			
		} catch (final Exception e) {
			LOGGER.error("Failed to parse XML-specific fields: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * Check if an attribute should be included based on filter settings.
	 * 
	 * @param attributeName attribute name to check
	 * @return true if attribute should be included
	 */
	public boolean shouldIncludeAttribute(final String attributeName) {
		// Check exclusions first
		if (excludedAttributes != null && excludedAttributes.contains(attributeName)) {
			return false;
		}
		
		// If no inclusions specified, include all (except excluded)
		if (includedAttributes == null || includedAttributes.isEmpty()) {
			return true;
		}
		
		// Check if attribute is in inclusion list
		return includedAttributes.contains(attributeName);
	}
	
	// Getters and setters
	public String getXpathQuery() {
		return xpathQuery;
	}
	
	public void setXpathQuery(final String xpathQuery) {
		this.xpathQuery = xpathQuery;
	}
	
	public Integer getMaxElementDepth() {
		return maxElementDepth;
	}
	
	public void setMaxElementDepth(final Integer maxElementDepth) {
		this.maxElementDepth = maxElementDepth;
	}
	
	public Integer getMaxElements() {
		return maxElements;
	}
	
	public void setMaxElements(final Integer maxElements) {
		this.maxElements = maxElements;
	}
	
	public List<String> getIncludedAttributes() {
		return includedAttributes;
	}
	
	public void setIncludedAttributes(final List<String> includedAttributes) {
		this.includedAttributes = includedAttributes != null ? new ArrayList<>(includedAttributes) : new ArrayList<>();
	}
	
	public List<String> getExcludedAttributes() {
		return excludedAttributes;
	}
	
	public void setExcludedAttributes(final List<String> excludedAttributes) {
		this.excludedAttributes = excludedAttributes != null ? new ArrayList<>(excludedAttributes) : new ArrayList<>();
	}
	
	public Boolean getPreserveNamespaces() {
		return preserveNamespaces;
	}
	
	public void setPreserveNamespaces(final Boolean preserveNamespaces) {
		this.preserveNamespaces = preserveNamespaces;
	}
	
	public Boolean getValidateXSD() {
		return validateXSD;
	}
	
	public void setValidateXSD(final Boolean validateXSD) {
		this.validateXSD = validateXSD;
	}
	
	public Boolean getStripWhitespace() {
		return stripWhitespace;
	}
	
	public void setStripWhitespace(final Boolean stripWhitespace) {
		this.stripWhitespace = stripWhitespace;
	}
	
	@Override
	public String toString() {
		return String.format("CFilterXML{name='%s', xpath='%s', maxDepth=%d, attributes=%s}", 
			getName(), 
			xpathQuery,
			maxElementDepth != null ? maxElementDepth : 0,
			includedAttributes != null && !includedAttributes.isEmpty() ? includedAttributes : "all");
	}
}
