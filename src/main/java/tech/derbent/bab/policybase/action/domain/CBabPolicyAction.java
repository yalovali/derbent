package tech.derbent.bab.policybase.action.domain;

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
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.action.service.CPageServiceBabPolicyAction;
import tech.derbent.bab.policybase.domain.IJsonNetworkSerializable;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** CBabPolicyAction - Action entity for BAB policy rules. Defines what actions should be executed when policy rule triggers fire. Supports various
 * action types like: - Forward (route data to destination) - Transform (modify data structure/content) - Store (save data to database/file) - Notify
 * (send notifications/alerts) - Execute (run external commands/scripts) - Filter (apply data filtering) Features: - Node type filtering
 * (enable/disable for specific node types) - Action type selection with configuration JSON - Parameter templates and validation - Execution settings
 * (timeout, retry, priority) - Output handling and logging Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete entity with @Entity annotation */
@Entity
@Table (name = "cbab_policy_action", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_action_id"))
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabPolicyAction extends CEntityOfProject<CBabPolicyAction>
		implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable, IJsonNetworkSerializable {

	public static final String ACTION_TYPE_EXECUTE = "execute";
	public static final String ACTION_TYPE_FILTER = "filter";
	// Action type enumeration values
	public static final String ACTION_TYPE_FORWARD = "forward";
	public static final String ACTION_TYPE_LOG = "log";
	public static final String ACTION_TYPE_NOTIFY = "notify";
	public static final String ACTION_TYPE_STORE = "store";
	public static final String ACTION_TYPE_TRANSFORM = "transform";
	public static final String ACTION_TYPE_VALIDATE = "validate";
	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#4CAF50"; // Green - Actions/Operations
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "Policy Actions";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Action";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyAction.class);
	public static final String VIEW_NAME = "Policy Actions View";
	// Core action configuration
	@Column (name = "action_type", nullable = false, length = 50)
	@NotNull (message = "Action type is required")
	@Size (max = 50, message = "Action type cannot exceed 50 characters")
	@AMetaData (
				displayName = "Action Type", required = true, readOnly = false,
				description = "Type of action to execute (forward, transform, store, notify, execute, filter, validate, log)", hidden = false,
				maxLength = 50, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfActionType"
	)
	private String actionType = ACTION_TYPE_FORWARD;
	@Column (name = "async_execution", nullable = false)
	@AMetaData (
			displayName = "Async Execution", required = false, readOnly = false, description = "Execute action asynchronously (non-blocking)",
			hidden = false
	)
	private Boolean asyncExecution = false;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_action_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this action", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// Node type filtering - enable/disable for specific node types
	@Column (name = "can_node_enabled", nullable = false)
	@AMetaData (
			displayName = "CAN Nodes", required = false, readOnly = false, description = "Enable this action for CAN communication nodes",
			hidden = false
	)
	private Boolean canNodeEnabled = true;
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_action_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this action", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order in which actions are executed (lower numbers execute first)", hidden = false
	)
	private Integer executionOrder = 0;
	// Execution settings - initialized at declaration (RULE 6)
	@Column (name = "execution_priority", nullable = false)
	@AMetaData (
			displayName = "Execution Priority", required = false, readOnly = false,
			description = "Action execution priority (0-100, higher = higher priority)", hidden = false
	)
	private Integer executionPriority = 50;
	@Column (name = "file_node_enabled", nullable = false)
	@AMetaData (
			displayName = "File Nodes", required = false, readOnly = false, description = "Enable this action for file input nodes", hidden = false
	)
	private Boolean fileNodeEnabled = true;
	@Column (name = "http_node_enabled", nullable = false)
	@AMetaData (
			displayName = "HTTP Nodes", required = false, readOnly = false, description = "Enable this action for HTTP server nodes", hidden = false
	)
	private Boolean httpNodeEnabled = true;
	// State and operational settings - initialized at declaration (RULE 6)
	@Column (name = "is_enabled", nullable = false)
	@AMetaData (
			displayName = "Enabled", required = true, readOnly = false, description = "Whether this action is currently enabled and active",
			hidden = false
	)
	private Boolean isEnabled = true;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_action_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this action", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (name = "log_execution", nullable = false)
	@AMetaData (
			displayName = "Log Execution", required = false, readOnly = false, description = "Enable logging for action execution events",
			hidden = false
	)
	private Boolean logExecution = true;
	@Column (name = "log_input", nullable = false)
	@AMetaData (
			displayName = "Log Input", required = false, readOnly = false, description = "Log action input data (may contain sensitive information)",
			hidden = false
	)
	private Boolean logInput = false;
	@Column (name = "log_output", nullable = false)
	@AMetaData (displayName = "Log Output", required = false, readOnly = false, description = "Log action output data", hidden = false)
	private Boolean logOutput = true;
	@Column (name = "modbus_node_enabled", nullable = false)
	@AMetaData (
			displayName = "Modbus Nodes", required = false, readOnly = false, description = "Enable this action for Modbus communication nodes",
			hidden = false
	)
	private Boolean modbusNodeEnabled = true;
	@Column (name = "retry_count", nullable = false)
	@AMetaData (
			displayName = "Retry Count", required = false, readOnly = false, description = "Number of retry attempts on action failure",
			hidden = false
	)
	private Integer retryCount = 3;
	@Column (name = "retry_delay_seconds", nullable = false)
	@AMetaData (
			displayName = "Retry Delay (seconds)", required = false, readOnly = false, description = "Delay between retry attempts in seconds",
			hidden = false
	)
	private Integer retryDelaySeconds = 5;
	@Column (name = "ros_node_enabled", nullable = false)
	@AMetaData (
			displayName = "ROS Nodes", required = false, readOnly = false, description = "Enable this action for ROS communication nodes",
			hidden = false
	)
	private Boolean rosNodeEnabled = true;
	@Column (name = "syslog_node_enabled", nullable = false)
	@AMetaData (displayName = "Syslog Nodes", required = false, readOnly = false, description = "Enable this action for syslog nodes", hidden = false)
	private Boolean syslogNodeEnabled = true;
	@Column (name = "timeout_seconds", nullable = false)
	@AMetaData (
			displayName = "Timeout (seconds)", required = false, readOnly = false, description = "Maximum execution time in seconds before timeout",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfTimeoutSeconds"
	)
	private Integer timeoutSeconds = 30;

	/** Default constructor for JPA. */
	protected CBabPolicyAction() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabPolicyAction(final String name, final CProject<?> project) {
		super(CBabPolicyAction.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Getters and setters
	public String getActionType() { return actionType; }
	// Business logic methods

	public Boolean getAsyncExecution() { return asyncExecution; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Boolean getCanNodeEnabled() { return canNodeEnabled; }

	// Interface implementations
	@Override
	public Set<CComment> getComments() { return comments; }

	/** Get action execution mode description. */
	public String getExecutionDescription() {
		final String mode = asyncExecution != null && asyncExecution ? "asynchronous" : "synchronous";
		return switch (actionType) {
		case ACTION_TYPE_FORWARD -> "Forward data to destination (" + mode + ")";
		case ACTION_TYPE_TRANSFORM -> "Transform data structure (" + mode + ")";
		case ACTION_TYPE_STORE -> "Store data persistently (" + mode + ")";
		case ACTION_TYPE_NOTIFY -> "Send notifications (" + mode + ")";
		case ACTION_TYPE_EXECUTE -> "Execute external command (" + mode + ")";
		case ACTION_TYPE_FILTER -> "Apply data filtering (" + mode + ")";
		case ACTION_TYPE_VALIDATE -> "Validate data structure (" + mode + ")";
		case ACTION_TYPE_LOG -> "Log event data (" + mode + ")";
		default -> "Unknown action type (" + mode + ")";
		};
	}

	public Integer getExecutionOrder() { return executionOrder; }

	public Integer getExecutionPriority() { return executionPriority; }

	public Boolean getFileNodeEnabled() { return fileNodeEnabled; }

	public Boolean getHttpNodeEnabled() { return httpNodeEnabled; }

	public Boolean getIsEnabled() { return isEnabled; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Boolean getLogExecution() { return logExecution; }

	public Boolean getLogInput() { return logInput; }

	public Boolean getLogOutput() { return logOutput; }

	public Boolean getModbusNodeEnabled() { return modbusNodeEnabled; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyAction.class; }

	public Integer getRetryCount() { return retryCount; }

	public Integer getRetryDelaySeconds() { return retryDelaySeconds; }

	public Boolean getRosNodeEnabled() { return rosNodeEnabled; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return CBabPolicyActionService.class; }

	public Boolean getSyslogNodeEnabled() { return syslogNodeEnabled; }

	public Integer getTimeoutSeconds() { return timeoutSeconds; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if action is enabled for a specific node type. */
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

	/** Check if action requires template configuration. */
	public boolean requiresTemplate() {
		return ACTION_TYPE_TRANSFORM.equals(actionType) || ACTION_TYPE_NOTIFY.equals(actionType) || ACTION_TYPE_FILTER.equals(actionType)
				|| ACTION_TYPE_VALIDATE.equals(actionType) || ACTION_TYPE_LOG.equals(actionType);
	}

	public void setActionType(final String actionType) { this.actionType = actionType; }

	public void setAsyncExecution(final Boolean asyncExecution) { this.asyncExecution = asyncExecution; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setCanNodeEnabled(final Boolean canNodeEnabled) { this.canNodeEnabled = canNodeEnabled; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setExecutionOrder(final Integer executionOrder) { this.executionOrder = executionOrder; }

	public void setExecutionPriority(final Integer executionPriority) { this.executionPriority = executionPriority; }

	public void setFileNodeEnabled(final Boolean fileNodeEnabled) { this.fileNodeEnabled = fileNodeEnabled; }

	public void setHttpNodeEnabled(final Boolean httpNodeEnabled) { this.httpNodeEnabled = httpNodeEnabled; }

	public void setIsEnabled(final Boolean isEnabled) { this.isEnabled = isEnabled; }

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setLogExecution(final Boolean logExecution) { this.logExecution = logExecution; }

	public void setLogInput(final Boolean logInput) { this.logInput = logInput; }

	public void setLogOutput(final Boolean logOutput) { this.logOutput = logOutput; }

	public void setModbusNodeEnabled(final Boolean modbusNodeEnabled) { this.modbusNodeEnabled = modbusNodeEnabled; }

	public void setRetryCount(final Integer retryCount) { this.retryCount = retryCount; }

	public void setRetryDelaySeconds(final Integer retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }

	public void setRosNodeEnabled(final Boolean rosNodeEnabled) { this.rosNodeEnabled = rosNodeEnabled; }

	public void setSyslogNodeEnabled(final Boolean syslogNodeEnabled) { this.syslogNodeEnabled = syslogNodeEnabled; }

	public void setTimeoutSeconds(final Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

	/** Check if action supports async execution. */
	public boolean supportsAsyncExecution() {
		return !ACTION_TYPE_FORWARD.equals(actionType); // Forward must be synchronous
	}
}
