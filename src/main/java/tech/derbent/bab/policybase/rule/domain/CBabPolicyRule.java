package tech.derbent.bab.policybase.rule.domain;

import java.util.HashSet;
import java.util.Map;
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
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.rule.service.CBabPolicyRuleService;
import tech.derbent.bab.policybase.rule.service.CPageServiceBabPolicyRule;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/** Policy rule with single source/filter and multiple destination-aware actions. */
@Entity
@Table (name = "cbab_policy_rule", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_rule_id"))
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabPolicyRule extends CEntityOfProject<CBabPolicyRule> implements IHasComments, IEntityRegistrable {

	public static final String DEFAULT_COLOR = "#607D8B";
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Policy Rules";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Rule";
	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRule.class);
	public static final String VIEW_NAME = "Policy Rules View";

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		return Map.of();
	}

	@OneToMany (mappedBy = "policyRule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Actions", required = false, readOnly = false, description = "Destination-aware actions executed by this rule",
			hidden = false, createComponentMethod = "createPolicyRuleActionsComponent", dataProviderBean = "CBabPolicyRuleService",
			setBackgroundFromColor = true, useIcon = true
	)
	private final Set<CBabPolicyAction> actions = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_rule_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this policy rule", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_filter_id", nullable = true)
	@AMetaData (
			displayName = "Filter", required = false, readOnly = false, description = "Policy filter applied by this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfPolicyFilter", setBackgroundFromColor = true, useIcon = true,
			hideNavigateToButton = true
	)
	private CBabPolicyFilterBase<?> filter;
	@Column (name = "log_enabled", nullable = false)
	@AMetaData (
			displayName = "Logging Enabled", required = false, readOnly = false, description = "Enable logging for rule execution and events",
			hidden = false
	)
	private Boolean logEnabled = true;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "source_node_id", nullable = true)
	@AMetaData (
			displayName = "Source Node", required = false, readOnly = false, description = "Source network node for this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfSourceNodeForProject", setBackgroundFromColor = true,
			hideNavigateToButton = true, useIcon = true
	)
	private CBabNodeEntity<?> sourceNode;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "policy_trigger_id", nullable = true)
	@AMetaData (
			displayName = "Trigger", required = false, readOnly = false, description = "Policy trigger that activates this rule", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfPolicyTrigger", setBackgroundFromColor = true, useIcon = true,
			hideNavigateToButton = true
	)
	private CBabPolicyTrigger trigger;

	protected CBabPolicyRule() {
		// JPA constructor
	}

	public CBabPolicyRule(final String name, final CProject<?> project) {
		super(CBabPolicyRule.class, name, project);
		initializeDefaults();
	}

	public Set<CBabPolicyAction> getActions() { return actions; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public int getCompletionPercentage() {
		int completedComponents = 0;
		if (sourceNode != null) {
			completedComponents++;
		}
		if (trigger != null) {
			completedComponents++;
		}
		if (!actions.isEmpty()) {
			completedComponents++;
		}
		return completedComponents * 100 / 3;
	}

	@Override
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	public CBabPolicyFilterBase<?> getFilter() { return filter; }

	public Boolean getLogEnabled() { return logEnabled; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyRule.class; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyRuleService.class; }

	public CBabNodeEntity<?> getSourceNode() { return sourceNode; }

	public CBabPolicyTrigger getTrigger() { return trigger; }

	public boolean hasCompleteConfiguration() {
		return trigger != null && sourceNode != null && !actions.isEmpty();
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setActions(final Set<CBabPolicyAction> actions) {
		this.actions.clear();
		if (actions != null) {
			actions.stream().filter(action -> action != null).forEach(action -> {
				action.setPolicyRule(this);
				this.actions.add(action);
			});
		}
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setFilter(final CBabPolicyFilterBase<?> filter) {
		this.filter = filter;
		updateLastModified();
	}

	public void setLogEnabled(final Boolean logEnabled) {
		this.logEnabled = logEnabled;
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
