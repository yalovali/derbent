package tech.derbent.bab.policybase.filter.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.domain.IJsonNetworkSerializable;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterService;
import tech.derbent.bab.policybase.filter.service.CPageServiceBabPolicyFilter;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** CBabPolicyFilter - Filter entity for BAB policy rules. Defines data filtering and transformation rules for BAB nodes. Supports various filter
 * types like: - CSV (file row/column filtering) - JSON (object path filtering) - XML (element filtering) - REGEX (pattern matching) - RANGE (numeric
 * filtering) - CONDITION (logical filtering) Features: - Node type filtering (enable/disable for specific node types) - Filter type selection with
 * configuration JSON - Condition templates and validation rules - Input/output transformation settings - Performance optimization options Layer:
 * Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern: Concrete entity with @Entity annotation */
@Entity
@Table (name = "cbab_policy_filter", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_filter_id"))
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabPolicyFilter extends CEntityOfProject<CBabPolicyFilter>
		implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable, IJsonNetworkSerializable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#9C27B0"; // Purple - Filters/Processing
	public static final String DEFAULT_ICON = "vaadin:filter";
	public static final String ENTITY_TITLE_PLURAL = "Policy Filters";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Filter";
	public static final String FILTER_TYPE_CONDITION = "condition";
	// Filter type enumeration values
	public static final String FILTER_TYPE_CSV = "csv";
	public static final String FILTER_TYPE_JSON = "json";
	public static final String FILTER_TYPE_RANGE = "range";
	public static final String FILTER_TYPE_REGEX = "regex";
	public static final String FILTER_TYPE_TRANSFORM = "transform";
	public static final String FILTER_TYPE_VALIDATE = "validate";
	public static final String FILTER_TYPE_XML = "xml";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilter.class);
	public static final String VIEW_NAME = "Policy Filters View";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this filter", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "cache_enabled", nullable = false)
	@AMetaData (
			displayName = "Cache Enabled", required = false, readOnly = false, description = "Enable caching of filter results for performance",
			hidden = false
	)
	private Boolean cacheEnabled = true;
	@Column (name = "cache_size_limit", nullable = false)
	@AMetaData (
			displayName = "Cache Size Limit", required = false, readOnly = false, description = "Maximum number of cached filter results",
			hidden = false
	)
	private Integer cacheSizeLimit = 1000;
	// Node type filtering - enable/disable for specific node types
	@Column (name = "can_node_enabled", nullable = false)
	@AMetaData (
			displayName = "CAN Nodes", required = false, readOnly = false, description = "Enable this filter for CAN communication nodes",
			hidden = false
	)
	private Boolean canNodeEnabled = true;
	@Column (name = "case_sensitive", nullable = false)
	@AMetaData (
			displayName = "Case Sensitive", required = false, readOnly = false, description = "Whether string comparisons are case sensitive",
			hidden = false
	)
	private Boolean caseSensitive = false;
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this filter", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order in which filters are applied (lower numbers execute first)", hidden = false
	)
	private Integer executionOrder = 0;
	@Column (name = "file_node_enabled", nullable = false)
	@AMetaData (
			displayName = "File Nodes", required = false, readOnly = false, description = "Enable this filter for file input nodes", hidden = false
	)
	private Boolean fileNodeEnabled = true;
	// Core filter configuration
	@Column (name = "filter_type", nullable = false, length = 50)
	@NotNull (message = "Filter type is required")
	@Size (max = 50, message = "Filter type cannot exceed 50 characters")
	@AMetaData (
				displayName = "Filter Type", required = true, readOnly = false,
				description = "Type of filter to apply (csv, json, xml, regex, range, condition, transform, validate)", hidden = false, maxLength = 50,
				dataProviderBean = "pageservice", dataProviderMethod = "getAvailableFilterTypes"
	)
	private String filterType = FILTER_TYPE_CONDITION;
	@Column (name = "http_node_enabled", nullable = false)
	@AMetaData (
			displayName = "HTTP Nodes", required = false, readOnly = false, description = "Enable this filter for HTTP server nodes", hidden = false
	)
	private Boolean httpNodeEnabled = true;
	// State and operational settings - initialized at declaration (RULE 6)
	@Column (name = "is_enabled", nullable = false)
	@AMetaData (
			displayName = "Enabled", required = true, readOnly = false, description = "Whether this filter is currently enabled and active",
			hidden = false
	)
	private Boolean isEnabled = true;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this filter", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// Processing settings - initialized at declaration (RULE 6)
	@Column (name = "logic_operator", nullable = false, length = 10)
	@AMetaData (
				displayName = "Logic Operator", required = false, readOnly = false,
				description = "Logical operator for combining conditions (AND, OR, NOT)", hidden = false, maxLength = 10,
				dataProviderBean = "pageservice", dataProviderMethod = "getAvailableLogicOperators"
	)
	private String logicOperator = "AND";
	@Column (name = "log_matches", nullable = false)
	@AMetaData (
			displayName = "Log Matches", required = false, readOnly = false, description = "Log data that matches filter conditions", hidden = false
	)
	private Boolean logMatches = false;
	@Column (name = "log_rejections", nullable = false)
	@AMetaData (
			displayName = "Log Rejections", required = false, readOnly = false, description = "Log data that is rejected by filter conditions",
			hidden = false
	)
	private Boolean logRejections = true;
	@Column (name = "max_processing_time_ms", nullable = false)
	@AMetaData (
			displayName = "Max Processing Time (ms)", required = false, readOnly = false,
			description = "Maximum processing time in milliseconds before timeout", hidden = false
	)
	private Integer maxProcessingTimeMs = 5000;
	@Column (name = "modbus_node_enabled", nullable = false)
	@AMetaData (
			displayName = "Modbus Nodes", required = false, readOnly = false, description = "Enable this filter for Modbus communication nodes",
			hidden = false
	)
	private Boolean modbusNodeEnabled = true;
	@Column (name = "null_handling", nullable = false, length = 20)
	@AMetaData (
				displayName = "Null Handling", required = false, readOnly = false,
				description = "How to handle null values (ignore, reject, pass, default)", hidden = false, maxLength = 20,
				dataProviderBean = "pageservice", dataProviderMethod = "getAvailableNullHandlingStrategies"
	)
	private String nullHandling = "ignore";
	@Column (name = "ros_node_enabled", nullable = false)
	@AMetaData (
			displayName = "ROS Nodes", required = false, readOnly = false, description = "Enable this filter for ROS communication nodes",
			hidden = false
	)
	private Boolean rosNodeEnabled = true;
	@Column (name = "syslog_node_enabled", nullable = false)
	@AMetaData (displayName = "Syslog Nodes", required = false, readOnly = false, description = "Enable this filter for syslog nodes", hidden = false)
	private Boolean syslogNodeEnabled = true;

	/** Default constructor for JPA. */
	protected CBabPolicyFilter() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabPolicyFilter(final String name, final CProject<?> project) {
		super(CBabPolicyFilter.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }
	// Business logic methods

	public Boolean getCacheEnabled() { return cacheEnabled; }

	public Integer getCacheSizeLimit() { return cacheSizeLimit; }

	public Boolean getCanNodeEnabled() { return canNodeEnabled; }

	public Boolean getCaseSensitive() { return caseSensitive; }

	// Interface implementations
	@Override
	public Set<CComment> getComments() { return comments; }

	public Integer getExecutionOrder() { return executionOrder; }

	public Boolean getFileNodeEnabled() { return fileNodeEnabled; }

	// Getters and setters
	public String getFilterType() { return filterType; }

	public Boolean getHttpNodeEnabled() { return httpNodeEnabled; }

	public Boolean getIsEnabled() { return isEnabled; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public String getLogicOperator() { return logicOperator; }

	public Boolean getLogMatches() { return logMatches; }

	public Boolean getLogRejections() { return logRejections; }

	public Integer getMaxProcessingTimeMs() { return maxProcessingTimeMs; }

	public Boolean getModbusNodeEnabled() { return modbusNodeEnabled; }

	public String getNullHandling() { return nullHandling; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilter.class; }

	/** Get filter processing description. */
	public String getProcessingDescription() {
		final String caching = cacheEnabled != null && cacheEnabled ? "cached" : "non-cached";
		return switch (filterType) {
		case FILTER_TYPE_CSV -> "Filter CSV rows/columns (" + caching + ")";
		case FILTER_TYPE_JSON -> "Filter JSON paths/objects (" + caching + ")";
		case FILTER_TYPE_XML -> "Filter XML elements/attributes (" + caching + ")";
		case FILTER_TYPE_REGEX -> "Apply regex pattern matching (" + caching + ")";
		case FILTER_TYPE_RANGE -> "Apply numeric range filtering (" + caching + ")";
		case FILTER_TYPE_CONDITION -> "Apply conditional logic (" + caching + ")";
		case FILTER_TYPE_TRANSFORM -> "Transform data structure (" + caching + ")";
		case FILTER_TYPE_VALIDATE -> "Validate data format (" + caching + ")";
		default -> "Unknown filter type (" + caching + ")";
		};
	}

	public Boolean getRosNodeEnabled() { return rosNodeEnabled; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return CBabPolicyFilterService.class; }

	public Boolean getSyslogNodeEnabled() { return syslogNodeEnabled; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if filter is enabled for a specific node type. */
	public boolean isEnabledForNodeType(final String nodeType) {
		if (nodeType == null) {
			return false;
		}
		return switch (nodeType.toLowerCase()) {
		case "can" -> canNodeEnabled != null && canNodeEnabled;
		case "modbus", "tcp_modbus" -> modbusNodeEnabled != null && modbusNodeEnabled;
		case "http", "http_server" -> httpNodeEnabled != null && httpNodeEnabled;
		case "file", "file_input" -> fileNodeEnabled != null && fileNodeEnabled;
		case "syslog" -> syslogNodeEnabled != null && syslogNodeEnabled;
		case "ros" -> rosNodeEnabled != null && rosNodeEnabled;
		default -> false;
		};
	}

	/** Check if filter requires transformation configuration. */
	public boolean requiresTransformation() {
		return FILTER_TYPE_TRANSFORM.equals(filterType) || FILTER_TYPE_CSV.equals(filterType) || FILTER_TYPE_JSON.equals(filterType)
				|| FILTER_TYPE_XML.equals(filterType);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setCacheEnabled(final Boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }

	public void setCacheSizeLimit(final Integer cacheSizeLimit) { this.cacheSizeLimit = cacheSizeLimit; }

	public void setCanNodeEnabled(final Boolean canNodeEnabled) { this.canNodeEnabled = canNodeEnabled; }

	public void setCaseSensitive(final Boolean caseSensitive) { this.caseSensitive = caseSensitive; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setExecutionOrder(final Integer executionOrder) { this.executionOrder = executionOrder; }

	public void setFileNodeEnabled(final Boolean fileNodeEnabled) { this.fileNodeEnabled = fileNodeEnabled; }

	public void setFilterType(final String filterType) { this.filterType = filterType; }

	public void setHttpNodeEnabled(final Boolean httpNodeEnabled) { this.httpNodeEnabled = httpNodeEnabled; }

	public void setIsEnabled(final Boolean isEnabled) { this.isEnabled = isEnabled; }

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setLogicOperator(final String logicOperator) { this.logicOperator = logicOperator; }

	public void setLogMatches(final Boolean logMatches) { this.logMatches = logMatches; }

	public void setLogRejections(final Boolean logRejections) { this.logRejections = logRejections; }

	public void setMaxProcessingTimeMs(final Integer maxProcessingTimeMs) { this.maxProcessingTimeMs = maxProcessingTimeMs; }

	public void setModbusNodeEnabled(final Boolean modbusNodeEnabled) { this.modbusNodeEnabled = modbusNodeEnabled; }

	public void setNullHandling(final String nullHandling) { this.nullHandling = nullHandling; }

	public void setRosNodeEnabled(final Boolean rosNodeEnabled) { this.rosNodeEnabled = rosNodeEnabled; }

	public void setSyslogNodeEnabled(final Boolean syslogNodeEnabled) { this.syslogNodeEnabled = syslogNodeEnabled; }

	/** Check if filter supports regex patterns. */
	public boolean supportsRegex() {
		return FILTER_TYPE_REGEX.equals(filterType) || FILTER_TYPE_CONDITION.equals(filterType);
	}
}
