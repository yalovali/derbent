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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.dashboard.dashboardpolicy.service.CBabPolicyRuleService;
import tech.derbent.bab.dashboard.dashboardpolicy.service.CPageServiceBabPolicyRule;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/** CBabPolicyRule - Individual policy rule entity for BAB Actions Dashboard. Layer: Domain (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Concrete entity with @Entity annotation. Represents a single communication rule within a policy, defining: - Source and
 * destination virtual network nodes - Trigger and action entities for rule execution - Filter and action configurations - Logging and priority
 * settings Used in drag-and-drop rule builder UI where nodes are dropped onto rule grid cells. */
@Entity
@Table (name = "cbab_policy_rule", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_rule_id"))
@Profile ("bab")
public class CBabPolicyRule extends CEntityOfProject<CBabPolicyRule> implements IHasComments, IEntityRegistrable {
	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#607D8B"; // Blue Grey - Rules/Logic
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Policy Rules";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Rule";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRule.class);
	public static final String VIEW_NAME = "Policy Rules View";
	@Column (name = "action_config", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Action Configuration", required = false, readOnly = false, description = "JSON configuration for rule action execution",
			hidden = false
	)
	private String actionConfigJson;
	// Comments composition for rule documentation
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_rule_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this policy rule", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	
	// Network node relationships for rule definition - real entity references
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "source_node_id", nullable = true)
	@AMetaData (
			displayName = "Source Node", required = false, readOnly = false, description = "Source network node for this rule",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getAvailableNodesForProject", setBackgroundFromColor = true,
			useIcon = true
	)
	private CBabNodeEntity<?> sourceNode;
	
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "destination_node_id", nullable = true)
	@AMetaData (
			displayName = "Destination Node", required = false, readOnly = false, description = "Destination network node for this rule",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getAvailableNodesForProject", setBackgroundFromColor = true,
			useIcon = true
	)
	private CBabNodeEntity<?> destinationNode;
	
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
	@Column (name = "log_enabled", nullable = false)
	@AMetaData (
			displayName = "Logging Enabled", required = false, readOnly = false, description = "Enable logging for rule execution and events",
			hidden = false
	)
	private Boolean logEnabled = true;
	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order in which rules are executed (lower numbers execute first)", hidden = false
	)
	private Integer executionOrder = 0;
	@Column (name = "rule_priority", nullable = false)
	@AMetaData (
			displayName = "Rule Priority", required = false, readOnly = false,
			description = "Rule execution priority (0-100, higher = higher priority)", hidden = false
	)
	private Integer rulePriority = 50;
	// Policy components relationships - many-to-many for flexible rule composition
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "cbab_policy_rule_triggers",
		joinColumns = @JoinColumn(name = "policy_rule_id"),
		inverseJoinColumns = @JoinColumn(name = "policy_trigger_id")
	)
	@AMetaData(
		displayName = "Triggers", required = false, readOnly = false,
		description = "Policy triggers that activate this rule",
		hidden = false, dataProviderBean = "pageservice",
		dataProviderMethod = "getAvailablePolicyTriggers", setBackgroundFromColor = true, useIcon = true
	)
	private Set<CBabPolicyTrigger> triggers = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "cbab_policy_rule_actions",
		joinColumns = @JoinColumn(name = "policy_rule_id"),
		inverseJoinColumns = @JoinColumn(name = "policy_action_id")
	)
	@AMetaData(
		displayName = "Actions", required = false, readOnly = false,
		description = "Policy actions executed by this rule",
		hidden = false, dataProviderBean = "pageservice",
		dataProviderMethod = "getAvailablePolicyActions", setBackgroundFromColor = true, useIcon = true
	)
	private Set<CBabPolicyAction> actions = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "cbab_policy_rule_filters",
		joinColumns = @JoinColumn(name = "policy_rule_id"),
		inverseJoinColumns = @JoinColumn(name = "policy_filter_id")
	)
	@AMetaData(
		displayName = "Filters", required = false, readOnly = false,
		description = "Policy filters applied by this rule",
		hidden = false, dataProviderBean = "pageservice",
		dataProviderMethod = "getAvailablePolicyFilters", setBackgroundFromColor = true, useIcon = true
	)
	private Set<CBabPolicyFilter> filters = new HashSet<>();
	
	@Column(name = "trigger_config", columnDefinition = "TEXT")
	@AMetaData(
		displayName = "Trigger Configuration", required = false, readOnly = false, description = "JSON configuration for rule trigger conditions",
		hidden = false
	)
	private String triggerConfigJson;
	/** Default constructor for JPA. */
	protected CBabPolicyRule() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabPolicyRule(final String name, final CProject<?> project) {
		super(CBabPolicyRule.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	/** Clear a specific node reference based on rule cell type.
	 * @param cellType the type of cell to clear (source, destination, trigger, action) */
	public void clearNodeReference(final String cellType) {
		switch (cellType.toLowerCase()) {
		case "source" -> sourceNode = null;
		case "destination" -> destinationNode = null;
		case "trigger" -> triggers.clear();
		case "action" -> actions.clear();
		case "filter" -> filters.clear();
		default -> throw new IllegalArgumentException("Unexpected value: " + cellType.toLowerCase());
		}
		updateLastModified();
	}

	/** Generate default JSON configurations for filter, action, and trigger. */
	private void generateDefaultConfigurations() {
		if ((filterConfigJson == null) || filterConfigJson.isEmpty()) {
			filterConfigJson = """
					{
					    "enabled": true,
					    "conditions": [],
					    "logicOperator": "AND"
					}
					""";
		}
		if ((actionConfigJson == null) || actionConfigJson.isEmpty()) {
			actionConfigJson = """
					{
					    "actionType": "FORWARD",
					    "parameters": {},
					    "timeout": 30,
					    "retryCount": 3
					}
					""";
		}
		if ((triggerConfigJson == null) || triggerConfigJson.isEmpty()) {
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

	// Interface implementations
	@Override
	public Set<CComment> getComments() { return comments; }

	/** Get rule completion percentage.
	 * @return completion percentage (0-100) */
	public int getCompletionPercentage() {
		int completedComponents = 0;
		if (sourceNode != null) {
			completedComponents++;
		}
		if (destinationNode != null) {
			completedComponents++;
		}
		if (!triggers.isEmpty()) {
			completedComponents++;
		}
		if (!actions.isEmpty()) {
			completedComponents++;
		}
		return (completedComponents * 100) / 4;
	}

	public CBabNodeEntity<?> getSourceNode() { return sourceNode; }
	
	public CBabNodeEntity<?> getDestinationNode() { return destinationNode; }

	public Integer getExecutionOrder() { return executionOrder; }

	public String getFilterConfigJson() { return filterConfigJson; }

	public Boolean getIsActive() { return isActive; }

	public Boolean getLogEnabled() { return logEnabled; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyRule.class; }

	public Integer getRulePriority() { return rulePriority; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return CBabPolicyRuleService.class; }

	public String getTriggerConfigJson() { return triggerConfigJson; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		if (rulePriority == null) {
			rulePriority = 50;
		}
		generateDefaultConfigurations();
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isActive() { return (isActive != null) && isActive; }

	/** Check if rule is complete and ready for execution.
	 * @return true if rule has all required components */
	public boolean isComplete() {
		return (sourceNode != null) && (destinationNode != null)
				&& !triggers.isEmpty() && !actions.isEmpty();
	}

	/** Check if rule is partially configured.
	 * @return true if at least source or destination is set */
	public boolean isPartiallyConfigured() {
		return (sourceNode != null)
				|| (destinationNode != null)
				|| !triggers.isEmpty()
				|| !actions.isEmpty()
				|| !filters.isEmpty();
	}

	public void setActionConfigJson(final String actionConfigJson) {
		this.actionConfigJson = actionConfigJson;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setSourceNode(final CBabNodeEntity<?> sourceNode) {
		this.sourceNode = sourceNode;
		updateLastModified();
	}

	public void setDestinationNode(final CBabNodeEntity<?> destinationNode) {
		this.destinationNode = destinationNode;
		updateLastModified();
	}

	public void setExecutionOrder(final Integer executionOrder) {
		this.executionOrder = executionOrder;
		updateLastModified();
	}

	public void setFilterConfigJson(final String filterConfigJson) {
		this.filterConfigJson = filterConfigJson;
		updateLastModified();
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
		updateLastModified();
	}

	public void setLogEnabled(final Boolean logEnabled) {
		this.logEnabled = logEnabled;
		updateLastModified();
	}

	public void setRulePriority(final Integer rulePriority) {
		this.rulePriority = rulePriority;
		updateLastModified();
	}

	public void setTriggerConfigJson(final String triggerConfigJson) {
		this.triggerConfigJson = triggerConfigJson;
		updateLastModified();
	}
	
	// New collection getters and setters for policy components
	public Set<CBabPolicyTrigger> getTriggers() {
		return triggers;
	}
	
	public void setTriggers(final Set<CBabPolicyTrigger> triggers) {
		this.triggers = triggers;
		updateLastModified();
	}
	
	public Set<CBabPolicyAction> getActions() {
		return actions;
	}
	
	public void setActions(final Set<CBabPolicyAction> actions) {
		this.actions = actions;
		updateLastModified();
	}
	
	public Set<CBabPolicyFilter> getFilters() {
		return filters;
	}
	
	public void setFilters(final Set<CBabPolicyFilter> filters) {
		this.filters = filters;
		updateLastModified();
	}
	
	// Business logic methods for policy components
	
	/** Add a trigger to this rule. */
	public void addTrigger(final CBabPolicyTrigger trigger) {
		if (trigger != null) {
			triggers.add(trigger);
			updateLastModified();
		}
	}
	
	/** Remove a trigger from this rule. */
	public void removeTrigger(final CBabPolicyTrigger trigger) {
		if (trigger != null) {
			triggers.remove(trigger);
			updateLastModified();
		}
	}
	
	/** Add an action to this rule. */
	public void addAction(final CBabPolicyAction action) {
		if (action != null) {
			actions.add(action);
			updateLastModified();
		}
	}
	
	/** Remove an action from this rule. */
	public void removeAction(final CBabPolicyAction action) {
		if (action != null) {
			actions.remove(action);
			updateLastModified();
		}
	}
	
	/** Add a filter to this rule. */
	public void addFilter(final CBabPolicyFilter filter) {
		if (filter != null) {
			filters.add(filter);
			updateLastModified();
		}
	}
	
	/** Remove a filter from this rule. */
	public void removeFilter(final CBabPolicyFilter filter) {
		if (filter != null) {
			filters.remove(filter);
			updateLastModified();
		}
	}
	
	/** Check if rule has complete configuration (triggers, actions, and filters). */
	public boolean hasCompleteConfiguration() {
		return !triggers.isEmpty() && !actions.isEmpty();
		// Filters are optional
	}
	
	/** Get count of all components. */
	public int getComponentCount() {
		return triggers.size() + actions.size() + filters.size();
	}
}
