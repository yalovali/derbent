package tech.derbent.bab.policybase.trigger.domain;

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
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerService;
import tech.derbent.bab.policybase.trigger.service.CPageServiceBabPolicyTrigger;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * CBabPolicyTrigger - Trigger condition entity for BAB policy rules.
 * 
 * Defines when a policy rule should be executed with conditions like:
 * - Periodic (scheduled execution)
 * - At start (on system/service startup)
 * - Manual (user-initiated)
 * - Always (continuous monitoring)
 * - Once (single execution)
 * 
 * Features:
 * - Node type filtering (enable/disable for specific node types)
 * - Trigger type selection (periodic, at_start, manual, always, once)
 * - Cron expression support for periodic triggers
 * - Condition JSON for complex trigger logic
 * - Priority and execution order settings
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation
 */
@Entity
@Table(name = "cbab_policy_trigger", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "project_id", "name"
    })
})
@AttributeOverride(name = "id", column = @Column(name = "bab_policy_trigger_id"))
@Profile("bab")
public class CBabPolicyTrigger extends CEntityOfProject<CBabPolicyTrigger> 
        implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable {
    
    // Entity constants (MANDATORY)
    public static final String DEFAULT_COLOR = "#FF9800"; // Orange - Triggers/Events
    public static final String DEFAULT_ICON = "vaadin:play";
    public static final String ENTITY_TITLE_PLURAL = "Policy Triggers";
    public static final String ENTITY_TITLE_SINGULAR = "Policy Trigger";
    public static final String VIEW_NAME = "Policy Triggers View";
    
    @SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyTrigger.class);
    
    // Trigger type enumeration values
    public static final String TRIGGER_TYPE_PERIODIC = "periodic";
    public static final String TRIGGER_TYPE_AT_START = "at_start";
    public static final String TRIGGER_TYPE_MANUAL = "manual";
    public static final String TRIGGER_TYPE_ALWAYS = "always";
    public static final String TRIGGER_TYPE_ONCE = "once";
    
    // Core trigger configuration
    @Column(name = "trigger_type", nullable = false, length = 50)
    @NotNull(message = "Trigger type is required")
    @Size(max = 50, message = "Trigger type cannot exceed 50 characters")
    @AMetaData(
        displayName = "Trigger Type",
        required = true,
        readOnly = false,
        description = "When this trigger should execute (periodic, at_start, manual, always, once)",
        hidden = false,
        maxLength = 50
    )
    private String triggerType = TRIGGER_TYPE_MANUAL;
    
    @Column(name = "cron_expression", length = 255)
    @Size(max = 255, message = "Cron expression cannot exceed 255 characters")
    @AMetaData(
        displayName = "Cron Expression",
        required = false,
        readOnly = false,
        description = "Cron expression for periodic triggers (e.g., '0 0 * * * *' for hourly)",
        hidden = false,
        maxLength = 255
    )
    private String cronExpression;
    
    @Column(name = "condition_json", columnDefinition = "TEXT")
    @AMetaData(
        displayName = "Trigger Conditions",
        required = false,
        readOnly = false,
        description = "JSON configuration for trigger conditions and logic",
        hidden = false
    )
    private String conditionJson;
    
    // Priority and execution settings - initialized at declaration (RULE 6)
    @Column(name = "execution_priority", nullable = false)
    @AMetaData(
        displayName = "Execution Priority",
        required = false,
        readOnly = false,
        description = "Trigger execution priority (0-100, higher = higher priority)",
        hidden = false
    )
    private Integer executionPriority = 50;
    
    @Column(name = "execution_order", nullable = false)
    @AMetaData(
        displayName = "Execution Order",
        required = false,
        readOnly = false,
        description = "Order in which triggers are processed (lower numbers execute first)",
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
    
    // State and operational settings - initialized at declaration (RULE 6)
    @Column(name = "is_enabled", nullable = false)
    @AMetaData(
        displayName = "Enabled",
        required = true,
        readOnly = false,
        description = "Whether this trigger is currently enabled and active",
        hidden = false
    )
    private Boolean isEnabled = true;
    
    @Column(name = "log_execution", nullable = false)
    @AMetaData(
        displayName = "Log Execution",
        required = false,
        readOnly = false,
        description = "Enable logging for trigger execution events",
        hidden = false
    )
    private Boolean logExecution = true;
    
    @Column(name = "retry_count", nullable = false)
    @AMetaData(
        displayName = "Retry Count",
        required = false,
        readOnly = false,
        description = "Number of retry attempts on trigger failure",
        hidden = false
    )
    private Integer retryCount = 3;
    
    // Node type filtering - enable/disable for specific node types
    @Column(name = "can_node_enabled", nullable = false)
    @AMetaData(
        displayName = "CAN Nodes",
        required = false,
        readOnly = false,
        description = "Enable this trigger for CAN communication nodes",
        hidden = false
    )
    private Boolean canNodeEnabled = true;
    
    @Column(name = "modbus_node_enabled", nullable = false)
    @AMetaData(
        displayName = "Modbus Nodes",
        required = false,
        readOnly = false,
        description = "Enable this trigger for Modbus communication nodes",
        hidden = false
    )
    private Boolean modbusNodeEnabled = true;
    
    @Column(name = "http_node_enabled", nullable = false)
    @AMetaData(
        displayName = "HTTP Nodes",
        required = false,
        readOnly = false,
        description = "Enable this trigger for HTTP server nodes",
        hidden = false
    )
    private Boolean httpNodeEnabled = true;
    
    @Column(name = "file_node_enabled", nullable = false)
    @AMetaData(
        displayName = "File Nodes",
        required = false,
        readOnly = false,
        description = "Enable this trigger for file input nodes",
        hidden = false
    )
    private Boolean fileNodeEnabled = true;
    
    @Column(name = "syslog_node_enabled", nullable = false)
    @AMetaData(
        displayName = "Syslog Nodes",
        required = false,
        readOnly = false,
        description = "Enable this trigger for syslog nodes",
        hidden = false
    )
    private Boolean syslogNodeEnabled = true;
    
    @Column(name = "ros_node_enabled", nullable = false)
    @AMetaData(
        displayName = "ROS Nodes",
        required = false,
        readOnly = false,
        description = "Enable this trigger for ROS communication nodes",
        hidden = false
    )
    private Boolean rosNodeEnabled = true;
    
    // Standard composition fields - initialized at declaration (RULE 5)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "bab_policy_trigger_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments and notes for this trigger",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponent"
    )
    private Set<CComment> comments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "bab_policy_trigger_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this trigger",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "bab_policy_trigger_id")
    @AMetaData(
        displayName = "Links",
        required = false,
        readOnly = false,
        description = "Related links for this trigger",
        hidden = false,
        dataProviderBean = "CLinkService",
        createComponentMethod = "createComponent"
    )
    private Set<CLink> links = new HashSet<>();
    
    /** Default constructor for JPA. */
    protected CBabPolicyTrigger() {
        // JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    public CBabPolicyTrigger(final String name, final CProject<?> project) {
        super(CBabPolicyTrigger.class, name, project);
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    /** Initialize intrinsic defaults (RULE 3). */
    private final void initializeDefaults() {
        // Generate default condition JSON if not set
        if (conditionJson == null || conditionJson.isEmpty()) {
            generateDefaultConditionJson();
        }
        
        // MANDATORY: Call service initialization at end (RULE 3)
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    /** Generate default condition JSON based on trigger type. */
    private void generateDefaultConditionJson() {
        switch (triggerType) {
            case TRIGGER_TYPE_PERIODIC -> conditionJson = """
                {
                    "type": "periodic",
                    "interval": 3600,
                    "unit": "seconds",
                    "enabled": true
                }
                """;
            case TRIGGER_TYPE_AT_START -> conditionJson = """
                {
                    "type": "at_start",
                    "delay": 0,
                    "enabled": true
                }
                """;
            case TRIGGER_TYPE_MANUAL -> conditionJson = """
                {
                    "type": "manual",
                    "requireConfirmation": true,
                    "enabled": true
                }
                """;
            case TRIGGER_TYPE_ALWAYS -> conditionJson = """
                {
                    "type": "always",
                    "checkInterval": 1000,
                    "enabled": true
                }
                """;
            case TRIGGER_TYPE_ONCE -> conditionJson = """
                {
                    "type": "once",
                    "executedAt": null,
                    "enabled": true
                }
                """;
            default -> conditionJson = """
                {
                    "type": "manual",
                    "enabled": true
                }
                """;
        }
    }
    
    // Business logic methods
    
    /** Check if trigger is enabled for a specific node type. */
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
    
    /** Check if trigger requires cron expression. */
    public boolean requiresCronExpression() {
        return TRIGGER_TYPE_PERIODIC.equals(triggerType);
    }
    
    /** Check if trigger can be executed manually. */
    public boolean isManuallyExecutable() {
        return TRIGGER_TYPE_MANUAL.equals(triggerType) || 
               TRIGGER_TYPE_ONCE.equals(triggerType);
    }
    
    /** Get trigger execution frequency description. */
    public String getExecutionDescription() {
        return switch (triggerType) {
            case TRIGGER_TYPE_PERIODIC -> "Executes on schedule: " + 
                (cronExpression != null ? cronExpression : "No schedule set");
            case TRIGGER_TYPE_AT_START -> "Executes once at system startup";
            case TRIGGER_TYPE_MANUAL -> "Executes when manually triggered";
            case TRIGGER_TYPE_ALWAYS -> "Executes continuously";
            case TRIGGER_TYPE_ONCE -> "Executes once only";
            default -> "Unknown execution type";
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
        return CBabPolicyTriggerService.class;
    }
    
    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceBabPolicyTrigger.class;
    }
    
    // Getters and setters
    public String getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(final String triggerType) {
        this.triggerType = triggerType;
        // Regenerate default condition JSON when type changes
        if (conditionJson == null || conditionJson.isEmpty()) {
            generateDefaultConditionJson();
        }
    }
    
    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(final String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    public String getConditionJson() {
        return conditionJson;
    }
    
    public void setConditionJson(final String conditionJson) {
        this.conditionJson = conditionJson;
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
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(final Integer retryCount) {
        this.retryCount = retryCount;
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