package tech.derbent.bab.policybase.action.domain;

import java.util.HashSet;
import java.util.Set;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.action.service.CPageServiceBabPolicyAction;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * CBabPolicyAction - Action entity for BAB policy rules.
 * 
 * Defines what actions should be executed when policy rule triggers fire.
 * Supports various action types like:
 * - Forward (route data to destination)
 * - Transform (modify data structure/content)
 * - Store (save data to database/file)
 * - Notify (send notifications/alerts)
 * - Execute (run external commands/scripts)
 * - Filter (apply data filtering)
 * 
 * Features:
 * - Node type filtering (enable/disable for specific node types)
 * - Action type selection with configuration JSON
 * - Parameter templates and validation
 * - Execution settings (timeout, retry, priority)
 * - Output handling and logging
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation
 */
@Entity
@Table(name = "cbab_policy_action", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "project_id", "name"
    })
})
@AttributeOverride(name = "id", column = @Column(name = "bab_policy_action_id"))
@Profile("bab")
public class CBabPolicyAction extends CEntityOfProject<CBabPolicyAction> 
        implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable {
    
    // Entity constants (MANDATORY)
    public static final String DEFAULT_COLOR = "#4CAF50"; // Green - Actions/Operations
    public static final String DEFAULT_ICON = "vaadin:cogs";
    public static final String ENTITY_TITLE_PLURAL = "Policy Actions";
    public static final String ENTITY_TITLE_SINGULAR = "Policy Action";
    public static final String VIEW_NAME = "Policy Actions View";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyAction.class);
    
    // Action type enumeration values
    public static final String ACTION_TYPE_FORWARD = "forward";
    public static final String ACTION_TYPE_TRANSFORM = "transform";
    public static final String ACTION_TYPE_STORE = "store";
    public static final String ACTION_TYPE_NOTIFY = "notify";
    public static final String ACTION_TYPE_EXECUTE = "execute";
    public static final String ACTION_TYPE_FILTER = "filter";
    public static final String ACTION_TYPE_VALIDATE = "validate";
    public static final String ACTION_TYPE_LOG = "log";
    
    // Core action configuration
    @Column(name = "action_type", nullable = false, length = 50)
    @NotNull(message = "Action type is required")
    @Size(max = 50, message = "Action type cannot exceed 50 characters")
    @AMetaData(
        displayName = "Action Type",
        required = true,
        readOnly = false,
        description = "Type of action to execute (forward, transform, store, notify, execute, filter, validate, log)",
        hidden = false,
        maxLength = 50
    )
    private String actionType = ACTION_TYPE_FORWARD;
    
    @Column(name = "configuration_json", columnDefinition = "TEXT")
    @AMetaData(
        displayName = "Action Configuration",
        required = false,
        readOnly = false,
        description = "JSON configuration for action parameters and settings",
        hidden = false
    )
    private String configurationJson;
    
    @Column(name = "template_json", columnDefinition = "TEXT")
    @AMetaData(
        displayName = "Action Template",
        required = false,
        readOnly = false,
        description = "JSON template for action data transformation",
        hidden = false
    )
    private String templateJson;
    
    // Execution settings - initialized at declaration (RULE 6)
    @Column(name = "execution_priority", nullable = false)
    @AMetaData(
        displayName = "Execution Priority",
        required = false,
        readOnly = false,
        description = "Action execution priority (0-100, higher = higher priority)",
        hidden = false
    )
    private Integer executionPriority = 50;
    
    @Column(name = "execution_order", nullable = false)
    @AMetaData(
        displayName = "Execution Order",
        required = false,
        readOnly = false,
        description = "Order in which actions are executed (lower numbers execute first)",
        hidden = false
    )
    private Integer executionOrder = 0;
    
    @Column(name = "timeout_seconds", nullable = false)
    @AMetaData(
        displayName = "Timeout (seconds)",
        required = false,
        readOnly = false,
        description = "Maximum execution time in seconds before timeout",
        hidden = false
    )
    private Integer timeoutSeconds = 30;
    
    @Column(name = "retry_count", nullable = false)
    @AMetaData(
        displayName = "Retry Count",
        required = false,
        readOnly = false,
        description = "Number of retry attempts on action failure",
        hidden = false
    )
    private Integer retryCount = 3;
    
    @Column(name = "retry_delay_seconds", nullable = false)
    @AMetaData(
        displayName = "Retry Delay (seconds)",
        required = false,
        readOnly = false,
        description = "Delay between retry attempts in seconds",
        hidden = false
    )
    private Integer retryDelaySeconds = 5;
    
    // State and operational settings - initialized at declaration (RULE 6)
    @Column(name = "is_enabled", nullable = false)
    @AMetaData(
        displayName = "Enabled",
        required = true,
        readOnly = false,
        description = "Whether this action is currently enabled and active",
        hidden = false
    )
    private Boolean isEnabled = true;
    
    @Column(name = "log_execution", nullable = false)
    @AMetaData(
        displayName = "Log Execution",
        required = false,
        readOnly = false,
        description = "Enable logging for action execution events",
        hidden = false
    )
    private Boolean logExecution = true;
    
    @Column(name = "log_input", nullable = false)
    @AMetaData(
        displayName = "Log Input",
        required = false,
        readOnly = false,
        description = "Log action input data (may contain sensitive information)",
        hidden = false
    )
    private Boolean logInput = false;
    
    @Column(name = "log_output", nullable = false)
    @AMetaData(
        displayName = "Log Output",
        required = false,
        readOnly = false,
        description = "Log action output data",
        hidden = false
    )
    private Boolean logOutput = true;
    
    @Column(name = "async_execution", nullable = false)
    @AMetaData(
        displayName = "Async Execution",
        required = false,
        readOnly = false,
        description = "Execute action asynchronously (non-blocking)",
        hidden = false
    )
    private Boolean asyncExecution = false;
    
    // Node type filtering - enable/disable for specific node types
    @Column(name = "can_node_enabled", nullable = false)
    @AMetaData(
        displayName = "CAN Nodes",
        required = false,
        readOnly = false,
        description = "Enable this action for CAN communication nodes",
        hidden = false
    )
    private Boolean canNodeEnabled = true;
    
    @Column(name = "modbus_node_enabled", nullable = false)
    @AMetaData(
        displayName = "Modbus Nodes",
        required = false,
        readOnly = false,
        description = "Enable this action for Modbus communication nodes",
        hidden = false
    )
    private Boolean modbusNodeEnabled = true;
    
    @Column(name = "http_node_enabled", nullable = false)
    @AMetaData(
        displayName = "HTTP Nodes",
        required = false,
        readOnly = false,
        description = "Enable this action for HTTP server nodes",
        hidden = false
    )
    private Boolean httpNodeEnabled = true;
    
    @Column(name = "file_node_enabled", nullable = false)
    @AMetaData(
        displayName = "File Nodes",
        required = false,
        readOnly = false,
        description = "Enable this action for file input nodes",
        hidden = false
    )
    private Boolean fileNodeEnabled = true;
    
    @Column(name = "syslog_node_enabled", nullable = false)
    @AMetaData(
        displayName = "Syslog Nodes",
        required = false,
        readOnly = false,
        description = "Enable this action for syslog nodes",
        hidden = false
    )
    private Boolean syslogNodeEnabled = true;
    
    @Column(name = "ros_node_enabled", nullable = false)
    @AMetaData(
        displayName = "ROS Nodes",
        required = false,
        readOnly = false,
        description = "Enable this action for ROS communication nodes",
        hidden = false
    )
    private Boolean rosNodeEnabled = true;
    
    // Standard composition fields - initialized at declaration (RULE 5)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "bab_policy_action_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments and notes for this action",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponent"
    )
    private Set<CComment> comments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "bab_policy_action_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this action",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "bab_policy_action_id")
    @AMetaData(
        displayName = "Links",
        required = false,
        readOnly = false,
        description = "Related links for this action",
        hidden = false,
        dataProviderBean = "CLinkService",
        createComponentMethod = "createComponent"
    )
    private Set<CLink> links = new HashSet<>();
    
    /** Default constructor for JPA. */
    protected CBabPolicyAction() {
        super();
        // JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    public CBabPolicyAction(final String name, final CProject<?> project) {
        super(CBabPolicyAction.class, name, project);
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    /** Initialize intrinsic defaults (RULE 3). */
    private final void initializeDefaults() {
        // Generate default configuration JSON if not set
        if (configurationJson == null || configurationJson.isEmpty()) {
            generateDefaultConfigurationJson();
        }
        
        // Generate default template JSON if not set
        if (templateJson == null || templateJson.isEmpty()) {
            generateDefaultTemplateJson();
        }
        
        // MANDATORY: Call service initialization at end (RULE 3)
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    /** Generate default configuration JSON based on action type. */
    private void generateDefaultConfigurationJson() {
        switch (actionType) {
            case ACTION_TYPE_FORWARD -> configurationJson = """
                {
                    "type": "forward",
                    "destination": "",
                    "preserveHeaders": true,
                    "enabled": true
                }
                """;
            case ACTION_TYPE_TRANSFORM -> configurationJson = """
                {
                    "type": "transform",
                    "transformationType": "json",
                    "template": "",
                    "enabled": true
                }
                """;
            case ACTION_TYPE_STORE -> configurationJson = """
                {
                    "type": "store",
                    "storageType": "database",
                    "path": "",
                    "compression": false,
                    "enabled": true
                }
                """;
            case ACTION_TYPE_NOTIFY -> configurationJson = """
                {
                    "type": "notify",
                    "notificationType": "email",
                    "recipients": [],
                    "template": "",
                    "enabled": true
                }
                """;
            case ACTION_TYPE_EXECUTE -> configurationJson = """
                {
                    "type": "execute",
                    "command": "",
                    "arguments": [],
                    "workingDirectory": "",
                    "environment": {},
                    "enabled": true
                }
                """;
            case ACTION_TYPE_FILTER -> configurationJson = """
                {
                    "type": "filter",
                    "filterType": "json",
                    "conditions": [],
                    "logicOperator": "AND",
                    "enabled": true
                }
                """;
            case ACTION_TYPE_VALIDATE -> configurationJson = """
                {
                    "type": "validate",
                    "validationType": "schema",
                    "schema": "",
                    "onFailure": "reject",
                    "enabled": true
                }
                """;
            case ACTION_TYPE_LOG -> configurationJson = """
                {
                    "type": "log",
                    "level": "info",
                    "destination": "file",
                    "format": "json",
                    "enabled": true
                }
                """;
            default -> configurationJson = """
                {
                    "type": "forward",
                    "enabled": true
                }
                """;
        }
    }
    
    /** Generate default template JSON based on action type. */
    private void generateDefaultTemplateJson() {
        switch (actionType) {
            case ACTION_TYPE_TRANSFORM -> templateJson = """
                {
                    "input": "{{input}}",
                    "timestamp": "{{timestamp}}",
                    "source": "{{source}}"
                }
                """;
            case ACTION_TYPE_NOTIFY -> templateJson = """
                {
                    "subject": "BAB Policy Notification",
                    "body": "Event occurred: {{event}}\\nTimestamp: {{timestamp}}\\nSource: {{source}}"
                }
                """;
            case ACTION_TYPE_FILTER -> templateJson = """
                {
                    "include": ["*"],
                    "exclude": [],
                    "transform": {}
                }
                """;
            case ACTION_TYPE_VALIDATE -> templateJson = """
                {
                    "type": "object",
                    "properties": {
                        "data": {"type": "object"},
                        "timestamp": {"type": "string"}
                    },
                    "required": ["data", "timestamp"]
                }
                """;
            case ACTION_TYPE_LOG -> templateJson = """
                {
                    "timestamp": "{{timestamp}}",
                    "level": "{{level}}",
                    "message": "{{message}}",
                    "data": "{{data}}"
                }
                """;
            default -> templateJson = """
                {
                    "template": "default"
                }
                """;
        }
    }
    
    // Business logic methods
    
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
        return ACTION_TYPE_TRANSFORM.equals(actionType) || 
               ACTION_TYPE_NOTIFY.equals(actionType) ||
               ACTION_TYPE_FILTER.equals(actionType) ||
               ACTION_TYPE_VALIDATE.equals(actionType) ||
               ACTION_TYPE_LOG.equals(actionType);
    }
    
    /** Check if action supports async execution. */
    public boolean supportsAsyncExecution() {
        return !ACTION_TYPE_FORWARD.equals(actionType); // Forward must be synchronous
    }
    
    /** Get action execution mode description. */
    public String getExecutionDescription() {
        final String mode = (asyncExecution != null && asyncExecution) ? "asynchronous" : "synchronous";
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
    
    // Interface implementations
    @Override
    public Set<CComment> getComments() {
        return comments;
    }
    
    @Override
    public void setComments(final Set<CComment> comments) {
        this.comments = comments;
    }
    
    @Override
    public Set<CAttachment> getAttachments() {
        return attachments;
    }
    
    @Override
    public void setAttachments(final Set<CAttachment> attachments) {
        this.attachments = attachments;
    }
    
    @Override
    public Set<CLink> getLinks() {
        return links;
    }
    
    @Override
    public void setLinks(final Set<CLink> links) {
        this.links = links;
    }
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getServiceClass() {
        return CBabPolicyActionService.class;
    }
    
    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceBabPolicyAction.class;
    }
    
    // Getters and setters
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(final String actionType) {
        this.actionType = actionType;
        // Regenerate default configurations when type changes
        if (configurationJson == null || configurationJson.isEmpty()) {
            generateDefaultConfigurationJson();
        }
        if (templateJson == null || templateJson.isEmpty()) {
            generateDefaultTemplateJson();
        }
    }
    
    public String getConfigurationJson() {
        return configurationJson;
    }
    
    public void setConfigurationJson(final String configurationJson) {
        this.configurationJson = configurationJson;
    }
    
    public String getTemplateJson() {
        return templateJson;
    }
    
    public void setTemplateJson(final String templateJson) {
        this.templateJson = templateJson;
    }
    
    public Integer getExecutionPriority() {
        return executionPriority;
    }
    
    public void setExecutionPriority(final Integer executionPriority) {
        this.executionPriority = executionPriority;
    }
    
    public Integer getExecutionOrder() {
        return executionOrder;
    }
    
    public void setExecutionOrder(final Integer executionOrder) {
        this.executionOrder = executionOrder;
    }
    
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(final Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(final Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getRetryDelaySeconds() {
        return retryDelaySeconds;
    }
    
    public void setRetryDelaySeconds(final Integer retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(final Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    public Boolean getLogExecution() {
        return logExecution;
    }
    
    public void setLogExecution(final Boolean logExecution) {
        this.logExecution = logExecution;
    }
    
    public Boolean getLogInput() {
        return logInput;
    }
    
    public void setLogInput(final Boolean logInput) {
        this.logInput = logInput;
    }
    
    public Boolean getLogOutput() {
        return logOutput;
    }
    
    public void setLogOutput(final Boolean logOutput) {
        this.logOutput = logOutput;
    }
    
    public Boolean getAsyncExecution() {
        return asyncExecution;
    }
    
    public void setAsyncExecution(final Boolean asyncExecution) {
        this.asyncExecution = asyncExecution;
    }
    
    public Boolean getCanNodeEnabled() {
        return canNodeEnabled;
    }
    
    public void setCanNodeEnabled(final Boolean canNodeEnabled) {
        this.canNodeEnabled = canNodeEnabled;
    }
    
    public Boolean getModbusNodeEnabled() {
        return modbusNodeEnabled;
    }
    
    public void setModbusNodeEnabled(final Boolean modbusNodeEnabled) {
        this.modbusNodeEnabled = modbusNodeEnabled;
    }
    
    public Boolean getHttpNodeEnabled() {
        return httpNodeEnabled;
    }
    
    public void setHttpNodeEnabled(final Boolean httpNodeEnabled) {
        this.httpNodeEnabled = httpNodeEnabled;
    }
    
    public Boolean getFileNodeEnabled() {
        return fileNodeEnabled;
    }
    
    public void setFileNodeEnabled(final Boolean fileNodeEnabled) {
        this.fileNodeEnabled = fileNodeEnabled;
    }
    
    public Boolean getSyslogNodeEnabled() {
        return syslogNodeEnabled;
    }
    
    public void setSyslogNodeEnabled(final Boolean syslogNodeEnabled) {
        this.syslogNodeEnabled = syslogNodeEnabled;
    }
    
    public Boolean getRosNodeEnabled() {
        return rosNodeEnabled;
    }
    
    public void setRosNodeEnabled(final Boolean rosNodeEnabled) {
        this.rosNodeEnabled = rosNodeEnabled;
    }
}