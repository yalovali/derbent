package tech.derbent.bab.policybase.action.domain;

import java.util.Map;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.action.service.CPageServiceBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** CBabPolicyAction - Destination-aware action entity for BAB policy rules. */
@Entity
@Table (name = "cbab_policy_action", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"policy_rule_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_action_id"))
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabPolicyAction extends CEntityNamed<CBabPolicyAction> implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable {

	public static final String DEFAULT_COLOR = "#4CAF50";
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "Policy Actions";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Action";
	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyAction.class);
	public static final String VIEW_NAME = "Policy Actions View";

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		return Map.of("CBabPolicyAction", Set.of("placeHolder_createComponentActionMaskDetails", "policyRule"));
	}

	@OneToOne (
			mappedBy = "policyAction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY,
			targetEntity = CBabPolicyActionMaskBase.class
	)
	@AMetaData (
			displayName = "Action Mask", required = false, readOnly = false,
			description = "Action-mask child owned by this action", hidden = false, dataProviderBean = "pageservice",
			dataProviderMethod = "getComboValuesOfActionMaskForDestinationNode"
		)
	private CBabPolicyActionMaskBase<?> actionMask;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_action_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this action", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_action_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this action", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "destination_node_id", nullable = true)
	@AMetaData (
			displayName = "Destination Node", required = false, readOnly = false, description = "Destination node where this action is executed",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfDestinationNodeForProject",
			setBackgroundFromColor = true, useIcon = true, hideNavigateToButton = true
	)
	private CBabNodeEntity<?> destinationNode;
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
	private Boolean logEnabled = true;
	@Transient
	@AMetaData (
			displayName = "Action Mask Details", required = false, readOnly = true, description = "Dynamic details view for selected action mask",
			hidden = false, dataProviderBean = "pageservice", createComponentMethod = "createComponentActionMaskDetails", captionVisible = false
	)
	private CBabPolicyAction placeHolder_createComponentActionMaskDetails = null;
	@ManyToOne (fetch = FetchType.LAZY, optional = false)
	@JoinColumn (name = "policy_rule_id", nullable = false)
	@AMetaData (
			displayName = "Policy Rule", required = true, readOnly = true, description = "Owning policy rule for this action", hidden = true,
			dataProviderBean = "none", hideNavigateToButton = true, hideEditButton = true
	)
	private CBabPolicyRule policyRule;
	@Column (name = "timeout_seconds", nullable = false)
	@AMetaData (
			displayName = "Timeout (seconds)", required = false, readOnly = false, description = "Maximum execution time in seconds before timeout",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfTimeoutSeconds"
	)
	private Integer timeoutSeconds = 30;

	/** Default constructor for JPA. */
	protected CBabPolicyAction() {
		// JPA constructors do NOT call initializeDefaults()
	}

	public CBabPolicyAction(final String name, final CBabPolicyRule policyRule) {
		super(CBabPolicyAction.class, name);
		setPolicyRule(policyRule);
		initializeDefaults();
	}

	@PostLoad
	protected void ensureActionMaskParents() {
		if (actionMask != null) {
			actionMask.setPolicyAction(this);
		}
	}

	public CBabPolicyActionMaskBase<?> getActionMask() { return actionMask; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public CBabNodeEntity<?> getDestinationNode() { return destinationNode; }

	@Override
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	/** Get action execution description. */
	public String getExecutionDescription() {
		final String nodeName = destinationNode != null ? destinationNode.getName() : "No destination";
		final String maskName = actionMask != null ? actionMask.getName() : "No mask";
		return "Mask " + maskName + " on " + nodeName;
	}

	@Override
	public Set<CLink> getLinks() { return links; }

	public Boolean getLogEnabled() { return logEnabled; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyAction.class; }

	public CBabPolicyAction getPlaceHolder_createComponentActionMaskDetails() { return this; }

	public CBabPolicyRule getPolicyRule() { return policyRule; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyActionService.class; }

	public Integer getTimeoutSeconds() { return timeoutSeconds; }

	/** Initialize intrinsic defaults. */
	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isConfigured() { return destinationNode != null && actionMask != null; }

	public void setActionMask(final CBabPolicyActionMaskBase<?> actionMask) {
		if (this.actionMask != null && this.actionMask != actionMask) {
			this.actionMask.setPolicyAction(null);
		}
		this.actionMask = actionMask;
		if (this.actionMask != null && this.actionMask.getPolicyAction() != this) {
			this.actionMask.setPolicyAction(this);
		}
		if (this.actionMask != null) {
			this.actionMask.markUiOwnershipContextFromCurrentOwner();
		}
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDestinationNode(final CBabNodeEntity<?> destinationNode) {
		this.destinationNode = destinationNode;
		if (this.actionMask != null && (this.actionMask.getUiOwnerNodeKey() == null || this.actionMask.getUiOwnerNodeKey().isBlank())) {
			// Stamp once when ownership context is missing.
			// Do not restamp on node change; page service manages per-node mask selection/caching.
			this.actionMask.markUiOwnershipContextFromCurrentOwner();
		}
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setLogEnabled(final Boolean logEnabled) { this.logEnabled = logEnabled; }

	public void setPlaceHolder_createComponentActionMaskDetails(final CBabPolicyAction value) {
		placeHolder_createComponentActionMaskDetails = value;
	}

	public void setPolicyRule(final CBabPolicyRule policyRule) { this.policyRule = policyRule; }

	public void setTimeoutSeconds(final Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
