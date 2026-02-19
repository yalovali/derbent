package tech.derbent.bab.policybase.rule.domain;

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
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.domain.IJsonNetworkSerializable;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.rule.service.CBabPolicyRuleService;
import tech.derbent.bab.policybase.rule.service.CPageServiceBabPolicyRule;
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
@JsonFilter ("babScenarioFilter")
public class CBabPolicyRule extends CEntityOfProject<CBabPolicyRule> implements IHasComments, IEntityRegistrable, IJsonNetworkSerializable {

	// Entity constants (MANDATORY)
	public static final String DEFAULT_COLOR = "#607D8B"; // Blue Grey - Rules/Logic
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Policy Rules";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Rule";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRule.class);
	public static final String VIEW_NAME = "Policy Rules View";
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (
			name = "cbab_policy_rule_actions", joinColumns = @JoinColumn (name = "policy_rule_id"),
			inverseJoinColumns = @JoinColumn (name = "policy_action_id")
	)
	@AMetaData (
			displayName = "Actions", required = false, readOnly = false, description = "Policy actions executed by this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfPolicyAction", setBackgroundFromColor = true, useIcon = true
	)
	private Set<CBabPolicyAction> actions = new HashSet<>();
	// Comments composition for rule documentation
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_rule_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this policy rule", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "destination_node_id", nullable = true)
	@AMetaData (
			displayName = "Destination Node", required = false, readOnly = false, description = "Destination network node for this rule",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfDestinationNodeForProject",
			setBackgroundFromColor = true, useIcon = true
	)
	private CBabNodeEntity<?> destinationNode;
	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order in which rules are executed (lower numbers execute first)", hidden = false
	)
	private Integer executionOrder = 0;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_filter_id", nullable = true)
	@AMetaData (
			displayName = "Filter", required = false, readOnly = false, description = "Policy filter applied by this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfPolicyFilter", setBackgroundFromColor = true, useIcon = true
	)
	private CBabPolicyFilterBase<?> filter;
	@Column (name = "log_enabled", nullable = false)
	@AMetaData (
			displayName = "Logging Enabled", required = false, readOnly = false, description = "Enable logging for rule execution and events",
			hidden = false
	)
	private Boolean logEnabled = true;
	@Column (name = "rule_priority", nullable = false)
	@AMetaData (
			displayName = "Rule Priority", required = false, readOnly = false,
			description = "Rule execution priority (0-100, higher = higher priority)", hidden = false
	)
	private Integer rulePriority = 50;
	// Network node relationships for rule definition - real entity references
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "source_node_id", nullable = true)
	@AMetaData (
			displayName = "Source Node", required = false, readOnly = false, description = "Source network node for this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfSourceNodeForProject", setBackgroundFromColor = true,
			useIcon = true
	)
	private CBabNodeEntity<?> sourceNode;
	// Policy component relationships
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_trigger_id", nullable = true)
	@AMetaData (
			displayName = "Trigger", required = false, readOnly = false, description = "Policy trigger that activates this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfPolicyTrigger", setBackgroundFromColor = true, useIcon = true
	)
	private CBabPolicyTrigger trigger;

	/** Default constructor for JPA. */
	protected CBabPolicyRule() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabPolicyRule(final String name, final CProject<?> project) {
		super(CBabPolicyRule.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public Set<CBabPolicyAction> getActions() { return actions; }

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
		if (trigger != null) {
			completedComponents++;
		}
		if (!actions.isEmpty()) {
			completedComponents++;
		}
		return completedComponents * 100 / 4;
	}

	public CBabNodeEntity<?> getDestinationNode() { return destinationNode; }

	public Integer getExecutionOrder() { return executionOrder; }

	public CBabPolicyFilterBase<?> getFilter() { return filter; }

	public Boolean getLogEnabled() { return logEnabled; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyRule.class; }

	public Integer getRulePriority() { return rulePriority; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return CBabPolicyRuleService.class; }

	public CBabNodeEntity<?> getSourceNode() { return sourceNode; }

	public CBabPolicyTrigger getTrigger() { return trigger; }

	/** Check if rule has complete configuration (trigger and actions). */
	public boolean hasCompleteConfiguration() {
		return trigger != null && !actions.isEmpty();
		// Filter is optional
	}

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		if (rulePriority == null) {
			rulePriority = 50;
		}
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setActions(final Set<CBabPolicyAction> actions) {
		this.actions = actions;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDestinationNode(final CBabNodeEntity<?> destinationNode) {
		this.destinationNode = destinationNode;
		updateLastModified();
	}

	public void setExecutionOrder(final Integer executionOrder) {
		this.executionOrder = executionOrder;
		updateLastModified();
	}

	public void setFilter(final CBabPolicyFilterBase<?> filter) {
		this.filter = filter;
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

	public void setSourceNode(final CBabNodeEntity<?> sourceNode) {
		this.sourceNode = sourceNode;
		updateLastModified();
	}

	public void setTrigger(final CBabPolicyTrigger trigger) {
		this.trigger = trigger;
		updateLastModified();
	}
}
