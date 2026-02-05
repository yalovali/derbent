package tech.derbent.bab.dashboard.dashboardpolicy.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CDashboardPolicy;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/** CPolicyRule - Individual policy rule entity for BAB Actions Dashboard. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Concrete entity with @Entity annotation. Represents a single communication rule within a policy, defining: - Source and destination
 * virtual network nodes - Trigger and action entities for rule execution - Filter and action configurations - Logging and priority settings Used in
 * drag-and-drop rule builder UI where nodes are dropped onto rule grid cells. */
@Entity
@Table (name = "CPolicyRule", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "policy_name", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "policy_rule_id"))
@Profile ("bab")
public class CPolicyRule extends CEntityOfProject<CPolicyRule> implements IHasComments, IEntityRegistrable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#607D8B"; // Blue Grey - Rules/Logic
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Policy Rules";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Rule";
	private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyRule.class);
	public static final String VIEW_NAME = "Policy Rules View";
	@Column (name = "action_config", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Action Configuration", required = false, readOnly = false, description = "JSON configuration for rule action execution",
			hidden = false
	)
	private String actionConfigJson;
	@Column(name = "action_entity_name", length = 255)
	@AMetaData (
			displayName = "Action Entity", required = false, readOnly = false, description = "Entity that executes the rule action", hidden = false,
			dataProviderBean = "nodeEntityService", maxLength = 255
	)
	private String actionEntityName;
	// Comments composition for rule documentation
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_rule_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this policy rule", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column(name = "destination_node_name", length = 255)
	@AMetaData (
			displayName = "Destination Node", required = false, readOnly = false,
			description = "Destination network node for this rule (drag from node list)", hidden = false, dataProviderBean = "nodeEntityService",
			maxLength = 255
	)
	private String destinationNodeName;
	// Rule execution statistics
	@Column (name = "execution_count", nullable = false)
	@AMetaData (
			displayName = "Execution Count", required = false, readOnly = true, description = "Number of times this rule has been executed",
			hidden = false
	)
	private Long executionCount = 0L;
	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false, description = "Order of execution within same priority level",
			hidden = false
	)
	private Integer executionOrder = 0;
	// Configuration JSON fields for Calimero integration
	@Column (name = "filter_config", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Filter Configuration", required = false, readOnly = false,
			description = "JSON configuration for rule filtering conditions", hidden = false
	)
	private String filterConfigJson;
	// Rule operational settings - initialized at declaration (RULE 6)
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Active", required = true, readOnly = false, description = "Whether this rule is currently active and enforced",
			hidden = false
	)
	private Boolean isActive = true;
	@Column (name = "last_execution_date")
	@AMetaData (displayName = "Last Execution", required = false, readOnly = true, description = "Timestamp of last rule execution", hidden = false)
	private java.time.LocalDateTime lastExecutionDate;
	@Column (name = "last_execution_status", length = 20)
	@AMetaData (
			displayName = "Last Execution Status", required = false, readOnly = true,
			description = "Result of last rule execution (SUCCESS, FAILED, SKIPPED)", hidden = false, maxLength = 20
	)
	private String lastExecutionStatus;
	@Column (name = "log_enabled", nullable = false)
	@AMetaData (
			displayName = "Logging Enabled", required = false, readOnly = false, description = "Enable logging for rule execution and events",
			hidden = false
	)
	private Boolean logEnabled = true;
	@Column(name = "policy_name", length = 255)
	@AMetaData (
			displayName = "Policy", required = true, readOnly = false, description = "Parent policy containing this rule", hidden = false,
			dataProviderBean = "CPolicyService", dataProviderMethod = "listByProject", maxLength = 255
	)
	private String policyName;
	@Column (name = "rule_priority", nullable = false)
	@AMetaData (
			displayName = "Rule Priority", required = false, readOnly = false,
			description = "Rule execution priority (0-100, higher = higher priority)", hidden = false
	)
	private Integer rulePriority = 50;
	// Network node relationships for rule definition
	@Column(name = "source_node_name", length = 255)
	@AMetaData (
			displayName = "Source Node", required = false, readOnly = false, description = "Source network node for this rule (drag from node list)",
			hidden = false, dataProviderBean = "nodeEntityService", maxLength = 255
	)
	private String sourceNodeName;
	@Column (name = "trigger_config", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Trigger Configuration", required = false, readOnly = false, description = "JSON configuration for rule trigger conditions",
			hidden = false
	)
	private String triggerConfigJson;
	@Column(name = "trigger_entity_string", length = 255)
	@AMetaData (
			displayName = "Trigger Entity", required = false, readOnly = false, description = "Entity that triggers this rule execution",
			hidden = false, dataProviderBean = "nodeEntityService", maxLength = 255
	)
	private String triggerEntityString;

	/** Default constructor for JPA. */
	protected CPolicyRule() {
		super();
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CPolicyRule(final String name, final CProject<?> project, final CDashboardPolicy policy) {
		super(CPolicyRule.class, name, project);
		setPolicy(policy); // Use setter to set both policy and policyName
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	/** Clear a specific node reference based on rule cell type.
	 * @param cellType the type of cell to clear (source, destination, trigger, action) */
	public void clearNodeReference(String cellType) {
		switch (cellType.toLowerCase()) {
		case "source" -> sourceNodeName = null;
		case "destination" -> destinationNodeName = null;
		case "trigger" -> triggerEntityString = null;
		case "action" -> actionEntityName = null;
		}
		updateLastModified();
	}

	/** Generate default JSON configurations for filter, action, and trigger. */
	private void generateDefaultConfigurations() {
		if (filterConfigJson == null || filterConfigJson.isEmpty()) {
			filterConfigJson = """
					{
					    "enabled": true,
					    "conditions": [],
					    "logicOperator": "AND"
					}
					""";
		}
		if (actionConfigJson == null || actionConfigJson.isEmpty()) {
			actionConfigJson = """
					{
					    "actionType": "FORWARD",
					    "parameters": {},
					    "timeout": 30,
					    "retryCount": 3
					}
					""";
		}
		if (triggerConfigJson == null || triggerConfigJson.isEmpty()) {
			triggerConfigJson = """
					{
					    "triggerType": "EVENT",
					    "eventTypes": [],
					    "conditions": {}
					}
					""";
		}
	}

	public String getActionConfigJson() { return actionConfigJson; }

	public String getActionEntityName() { return actionEntityName; }

	// Interface implementations
	@Override
	public Set<CComment> getComments() { return comments; }

	/** Get rule completion percentage.
	 * @return completion percentage (0-100) */
	public int getCompletionPercentage() {
		int completedComponents = 0;
		if (sourceNodeName != null && !sourceNodeName.trim().isEmpty()) {
			completedComponents++;
		}
		if (destinationNodeName != null && !destinationNodeName.trim().isEmpty()) {
			completedComponents++;
		}
		if (triggerEntityString != null && !triggerEntityString.trim().isEmpty()) {
			completedComponents++;
		}
		if (actionEntityName != null && !actionEntityName.trim().isEmpty()) {
			completedComponents++;
		}
		return (completedComponents * 100) / 4;
	}

	public String getDestinationNodeName() { return destinationNodeName; }

	public Long getExecutionCount() { return executionCount; }

	public Integer getExecutionOrder() { return executionOrder; }

	public String getFilterConfigJson() { return filterConfigJson; }

	public Boolean getIsActive() { return isActive; }

	public java.time.LocalDateTime getLastExecutionDate() { return lastExecutionDate; }

	public String getLastExecutionStatus() { return lastExecutionStatus; }

	public Boolean getLogEnabled() { return logEnabled; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	// Policy rule specific getters and setters
	public String getPolicyName() { return policyName; }
	
	public void setPolicyName(String policyName) { 
		this.policyName = policyName; 
		updateLastModified();
	}
	
	/**
	 * Get the policy entity by name lookup.
	 * @return CDashboardPolicy entity or null if not found
	 */
	public CDashboardPolicy getPolicy() {
		if (policyName == null || policyName.trim().isEmpty()) {
			return null;
		}
		try {
			final var policyService = CSpringContext.getBean(tech.derbent.bab.dashboard.dashboardpolicy.service.CDashboardPolicyService.class);
			final java.util.List<CDashboardPolicy> policies = policyService.listByProject(getProject());
			return policies.stream()
				.filter(p -> policyName.trim().equals(p.getName()))
				.findFirst()
				.orElse(null);
		} catch (Exception e) {
			LOGGER.warn("Error looking up policy by name '{}': {}", policyName, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Set policy by entity (sets name).
	 * @param policy the policy entity
	 */
	public void setPolicy(CDashboardPolicy policy) {
		if (policy != null) {
			this.policyName = policy.getName();
		} else {
			this.policyName = null;
		}
		updateLastModified();
	}

	public Integer getRulePriority() { return rulePriority; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	public String getSourceNodeName() { return sourceNodeName; }

	public String getTriggerConfigJson() { return triggerConfigJson; }

	public String getTriggerEntityString() { return triggerEntityString; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults (already done in field declarations)
		// Rule specific defaults
		if (rulePriority == null) {
			rulePriority = 50;
		}
		if (executionOrder == null) {
			executionOrder = 0;
		}
		if (executionCount == null) {
			executionCount = 0L;
		}
		// Generate initial configuration JSON structures
		generateDefaultConfigurations();
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isActive() { return isActive != null && isActive; }

	/** Check if rule is complete and ready for execution.
	 * @return true if rule has all required components */
	public boolean isComplete() {
		return sourceNodeName != null && !sourceNodeName.trim().isEmpty() && 
		       destinationNodeName != null && !destinationNodeName.trim().isEmpty() && 
		       triggerEntityString != null && !triggerEntityString.trim().isEmpty() && 
		       actionEntityName != null && !actionEntityName.trim().isEmpty();
	}

	/** Check if rule is partially configured.
	 * @return true if at least source or destination is set */
	public boolean isPartiallyConfigured() {
		return (sourceNodeName != null && !sourceNodeName.trim().isEmpty()) || 
		       (destinationNodeName != null && !destinationNodeName.trim().isEmpty()) || 
		       (triggerEntityString != null && !triggerEntityString.trim().isEmpty()) || 
		       (actionEntityName != null && !actionEntityName.trim().isEmpty());
	}

	/** Increment execution count and update last execution timestamp.
	 * @param status execution status */
	public void recordExecution(String status) {
		executionCount = (executionCount != null ? executionCount : 0L) + 1L;
		lastExecutionDate = java.time.LocalDateTime.now();
		lastExecutionStatus = status;
		updateLastModified();
	}

	public void setActionConfigJson(String actionConfigJson) {
		this.actionConfigJson = actionConfigJson;
		updateLastModified();
	}

	public void setActionEntityName(String actionEntityName) {
		this.actionEntityName = actionEntityName;
		updateLastModified();
	}

	@Override
	public void setComments(Set<CComment> comments) { this.comments = comments; }

	public void setDestinationNodeName(String destinationNodeName) {
		this.destinationNodeName = destinationNodeName;
		updateLastModified();
	}

	public void setExecutionCount(Long executionCount) {
		this.executionCount = executionCount;
		updateLastModified();
	}

	public void setExecutionOrder(Integer executionOrder) {
		this.executionOrder = executionOrder;
		updateLastModified();
	}

	public void setFilterConfigJson(String filterConfigJson) {
		this.filterConfigJson = filterConfigJson;
		updateLastModified();
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
		updateLastModified();
	}

	public void setLastExecutionDate(java.time.LocalDateTime lastExecutionDate) {
		this.lastExecutionDate = lastExecutionDate;
		updateLastModified();
	}

	public void setLastExecutionStatus(String lastExecutionStatus) {
		this.lastExecutionStatus = lastExecutionStatus;
		updateLastModified();
	}

	public void setLogEnabled(Boolean logEnabled) {
		this.logEnabled = logEnabled;
		updateLastModified();
	}

	public void setRulePriority(Integer rulePriority) {
		this.rulePriority = rulePriority;
		updateLastModified();
	}

	public void setSourceNodeName(String sourceNodeName) {
		this.sourceNodeName = sourceNodeName;
		updateLastModified();
	}

	public void setTriggerConfigJson(String triggerConfigJson) {
		this.triggerConfigJson = triggerConfigJson;
		updateLastModified();
	}

	public void setTriggerEntityString(String triggerEntityString) {
		this.triggerEntityString = triggerEntityString;
		updateLastModified();
	}
}
