package tech.derbent.bab.policybase.filter.domain;

import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.domain.IJsonNetworkSerializable;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** Abstract base entity for all BAB policy filter types. Holds common operational and node-compatibility settings while concrete subclasses carry
 * protocol-specific filter configuration. */
@Entity
@Table (name = "cbab_policy_filter", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_filter_id"))
@Inheritance (strategy = InheritanceType.JOINED)
@DiscriminatorColumn (name = "filter_kind", discriminatorType = DiscriminatorType.STRING)
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public abstract class CBabPolicyFilterBase<EntityClass extends CBabPolicyFilterBase<EntityClass>> extends CEntityOfProject<EntityClass>
		implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable, IJsonNetworkSerializable {

	public static final String LOGIC_OPERATOR_AND = "AND";
	public static final String LOGIC_OPERATOR_NOT = "NOT";
	public static final String LOGIC_OPERATOR_OR = "OR";
	public static final String NULL_HANDLING_DEFAULT = "default";
	public static final String NULL_HANDLING_IGNORE = "ignore";
	public static final String NULL_HANDLING_PASS = "pass";
	public static final String NULL_HANDLING_REJECT = "reject";

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this filter", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "cache_enabled", nullable = false)
	@AMetaData (
			displayName = "Cache Enabled", required = false, readOnly = false, description = "Enable result caching for this filter",
			hidden = false
	)
	private Boolean cacheEnabled = true;
	@Column (name = "cache_size_limit", nullable = false)
	@AMetaData (
			displayName = "Cache Size Limit", required = false, readOnly = false, description = "Maximum number of cached filter results",
			hidden = false
	)
	private Integer cacheSizeLimit = 1000;
	@Column (name = "can_node_enabled", nullable = false)
	@AMetaData (
			displayName = "CAN Nodes", required = false, readOnly = false, description = "Enable this filter for CAN nodes", hidden = false
	)
	private Boolean canNodeEnabled = true;
	@Column (name = "case_sensitive", nullable = false)
	@AMetaData (
			displayName = "Case Sensitive", required = false, readOnly = false, description = "Enable case-sensitive matching", hidden = false
	)
	private Boolean caseSensitive = false;
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
			description = "Execution order (lower numbers execute first)", hidden = false
	)
	private Integer executionOrder = 0;
	@Column (name = "file_node_enabled", nullable = false)
	@AMetaData (
			displayName = "File Nodes", required = false, readOnly = false, description = "Enable this filter for file input nodes", hidden = false
	)
	private Boolean fileNodeEnabled = true;
	@Column (name = "http_node_enabled", nullable = false)
	@AMetaData (
			displayName = "HTTP Nodes", required = false, readOnly = false, description = "Enable this filter for HTTP server nodes", hidden = false
	)
	private Boolean httpNodeEnabled = true;
	@Column (name = "is_enabled", nullable = false)
	@AMetaData (
			displayName = "Enabled", required = true, readOnly = false, description = "Whether this filter is currently active", hidden = false
	)
	private Boolean isEnabled = true;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this filter", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (name = "logic_operator", nullable = false, length = 10)
	@AMetaData (
			displayName = "Logic Operator", required = false, readOnly = false,
			description = "Logical operator for combining checks (AND, OR, NOT)", hidden = false, maxLength = 10,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfLogicOperator"
	)
	private String logicOperator = LOGIC_OPERATOR_AND;
	@Column (name = "log_matches", nullable = false)
	@AMetaData (
			displayName = "Log Matches", required = false, readOnly = false, description = "Log accepted payloads", hidden = false
	)
	private Boolean logMatches = false;
	@Column (name = "log_rejections", nullable = false)
	@AMetaData (
			displayName = "Log Rejections", required = false, readOnly = false, description = "Log rejected payloads", hidden = false
	)
	private Boolean logRejections = true;
	@Column (name = "max_processing_time_ms", nullable = false)
	@AMetaData (
			displayName = "Max Processing Time (ms)", required = false, readOnly = false,
			description = "Maximum processing time in milliseconds", hidden = false
	)
	private Integer maxProcessingTimeMs = 5000;
	@Column (name = "modbus_node_enabled", nullable = false)
	@AMetaData (
			displayName = "Modbus Nodes", required = false, readOnly = false, description = "Enable this filter for Modbus nodes", hidden = false
	)
	private Boolean modbusNodeEnabled = true;
	@Column (name = "null_handling", nullable = false, length = 20)
	@AMetaData (
			displayName = "Null Handling", required = false, readOnly = false,
			description = "How null values are handled (ignore, reject, pass, default)", hidden = false, maxLength = 20,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfNullHandlingStrategy"
	)
	private String nullHandling = NULL_HANDLING_IGNORE;
	@Column (name = "ros_node_enabled", nullable = false)
	@AMetaData (
			displayName = "ROS Nodes", required = false, readOnly = false, description = "Enable this filter for ROS nodes", hidden = false
	)
	private Boolean rosNodeEnabled = true;
	@Column (name = "syslog_node_enabled", nullable = false)
	@AMetaData (displayName = "Syslog Nodes", required = false, readOnly = false, description = "Enable this filter for syslog nodes", hidden = false)
	private Boolean syslogNodeEnabled = true;

	/** Default constructor for JPA. */
	protected CBabPolicyFilterBase() {
		// JPA constructor must not initialize business defaults.
	}

	protected CBabPolicyFilterBase(final Class<EntityClass> clazz, final String name, final CProject<?> project) {
		super(clazz, name, project);
	}

	public abstract String getFilterKind();

	public abstract Class<? extends CBabNodeEntity<?>> getAllowedNodeType();

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Boolean getCacheEnabled() { return cacheEnabled; }

	public Integer getCacheSizeLimit() { return cacheSizeLimit; }

	public Boolean getCanNodeEnabled() { return canNodeEnabled; }

	public Boolean getCaseSensitive() { return caseSensitive; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public Integer getExecutionOrder() { return executionOrder; }

	public Boolean getFileNodeEnabled() { return fileNodeEnabled; }

	public Boolean getHttpNodeEnabled() { return httpNodeEnabled; }

	public Boolean getIsEnabled() { return isEnabled; }

	public boolean isEnabledForNodeType(final String nodeType) {
		if (nodeType == null) {
			return false;
		}
		return switch (nodeType.toLowerCase()) {
		case "can" -> Boolean.TRUE.equals(canNodeEnabled);
		case "modbus", "tcp_modbus" -> Boolean.TRUE.equals(modbusNodeEnabled);
		case "http", "http_server" -> Boolean.TRUE.equals(httpNodeEnabled);
		case "file", "file_input" -> Boolean.TRUE.equals(fileNodeEnabled);
		case "syslog" -> Boolean.TRUE.equals(syslogNodeEnabled);
		case "ros" -> Boolean.TRUE.equals(rosNodeEnabled);
		default -> false;
		};
	}

	@Override
	public Set<CLink> getLinks() { return links; }

	public String getLogicOperator() { return logicOperator; }

	public Boolean getLogMatches() { return logMatches; }

	public Boolean getLogRejections() { return logRejections; }

	public Integer getMaxProcessingTimeMs() { return maxProcessingTimeMs; }

	public Boolean getModbusNodeEnabled() { return modbusNodeEnabled; }

	public String getNullHandling() { return nullHandling; }

	public Boolean getRosNodeEnabled() { return rosNodeEnabled; }

	public Boolean getSyslogNodeEnabled() { return syslogNodeEnabled; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setCacheEnabled(final Boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
		updateLastModified();
	}

	public void setCacheSizeLimit(final Integer cacheSizeLimit) {
		this.cacheSizeLimit = cacheSizeLimit;
		updateLastModified();
	}

	public void setCanNodeEnabled(final Boolean canNodeEnabled) {
		this.canNodeEnabled = canNodeEnabled;
		updateLastModified();
	}

	public void setCaseSensitive(final Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setExecutionOrder(final Integer executionOrder) {
		this.executionOrder = executionOrder;
		updateLastModified();
	}

	public void setFileNodeEnabled(final Boolean fileNodeEnabled) {
		this.fileNodeEnabled = fileNodeEnabled;
		updateLastModified();
	}

	public void setHttpNodeEnabled(final Boolean httpNodeEnabled) {
		this.httpNodeEnabled = httpNodeEnabled;
		updateLastModified();
	}

	public void setIsEnabled(final Boolean isEnabled) {
		this.isEnabled = isEnabled;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setLogicOperator(final String logicOperator) {
		this.logicOperator = logicOperator;
		updateLastModified();
	}

	public void setLogMatches(final Boolean logMatches) {
		this.logMatches = logMatches;
		updateLastModified();
	}

	public void setLogRejections(final Boolean logRejections) {
		this.logRejections = logRejections;
		updateLastModified();
	}

	public void setMaxProcessingTimeMs(final Integer maxProcessingTimeMs) {
		this.maxProcessingTimeMs = maxProcessingTimeMs;
		updateLastModified();
	}

	public void setModbusNodeEnabled(final Boolean modbusNodeEnabled) {
		this.modbusNodeEnabled = modbusNodeEnabled;
		updateLastModified();
	}

	public void setNullHandling(final String nullHandling) {
		this.nullHandling = nullHandling;
		updateLastModified();
	}

	public void setRosNodeEnabled(final Boolean rosNodeEnabled) {
		this.rosNodeEnabled = rosNodeEnabled;
		updateLastModified();
	}

	public void setSyslogNodeEnabled(final Boolean syslogNodeEnabled) {
		this.syslogNodeEnabled = syslogNodeEnabled;
		updateLastModified();
	}
}
