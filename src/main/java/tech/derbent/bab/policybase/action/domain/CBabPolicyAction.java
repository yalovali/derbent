package tech.derbent.bab.policybase.action.domain;

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
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.action.service.CPageServiceBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
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
				"project_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_action_id"))
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabPolicyAction extends CEntityOfProject<CBabPolicyAction> implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable {

	public static final String DEFAULT_COLOR = "#4CAF50";
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "Policy Actions";
	public static final String ENTITY_TITLE_SINGULAR = "Policy Action";
	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyAction.class);
	public static final String VIEW_NAME = "Policy Actions View";

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		return Map.of();
	}

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "action_mask_id", nullable = false)
	@NotNull (message = "Action mask is required")
	@AMetaData (
			displayName = "Action Mask", required = true, readOnly = false, description = "Action mask configuration applied on destination node",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfActionMaskForDestinationNode",
			setBackgroundFromColor = true, useIcon = true
	)
	private CBabPolicyActionMaskBase<?> actionMask;
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
			setBackgroundFromColor = true, useIcon = true
	)
	private CBabNodeEntity<?> destinationNode;
	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order in which actions are executed (lower numbers execute first)", hidden = false
	)
	private Integer executionOrder = 0;
	@Column (name = "execution_priority", nullable = false)
	@AMetaData (
			displayName = "Execution Priority", required = false, readOnly = false,
			description = "Action execution priority (0-100, higher = higher priority)", hidden = false
	)
	private Integer executionPriority = 50;
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

	public CBabPolicyAction(final String name, final CProject<?> project) {
		super(CBabPolicyAction.class, name, project);
		initializeDefaults();
	}

	public CBabPolicyActionMaskBase<?> getActionMask() { return actionMask; }

	public Boolean getAsyncExecution() { return asyncExecution; }

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
		final String mode = asyncExecution != null && asyncExecution ? "asynchronous" : "synchronous";
		final String nodeName = destinationNode != null ? destinationNode.getName() : "No destination";
		final String maskName = actionMask != null ? actionMask.getName() : "No mask";
		return "Mask " + maskName + " on " + nodeName + " (" + mode + ")";
	}

	public Integer getExecutionOrder() { return executionOrder; }

	public Integer getExecutionPriority() { return executionPriority; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Boolean getLogExecution() { return logExecution; }

	public Boolean getLogInput() { return logInput; }

	public Boolean getLogOutput() { return logOutput; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyAction.class; }

	public Integer getRetryCount() { return retryCount; }

	public Integer getRetryDelaySeconds() { return retryDelaySeconds; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyActionService.class; }

	public Integer getTimeoutSeconds() { return timeoutSeconds; }

	/** Initialize intrinsic defaults. */
	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public boolean isConfigured() { return destinationNode != null && actionMask != null; }

	public void setActionMask(final CBabPolicyActionMaskBase<?> actionMask) { this.actionMask = actionMask; }

	public void setAsyncExecution(final Boolean asyncExecution) { this.asyncExecution = asyncExecution; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDestinationNode(final CBabNodeEntity<?> destinationNode) { this.destinationNode = destinationNode; }

	public void setExecutionOrder(final Integer executionOrder) { this.executionOrder = executionOrder; }

	public void setExecutionPriority(final Integer executionPriority) { this.executionPriority = executionPriority; }

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setLogExecution(final Boolean logExecution) { this.logExecution = logExecution; }

	public void setLogInput(final Boolean logInput) { this.logInput = logInput; }

	public void setLogOutput(final Boolean logOutput) { this.logOutput = logOutput; }

	public void setRetryCount(final Integer retryCount) { this.retryCount = retryCount; }

	public void setRetryDelaySeconds(final Integer retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }

	public void setTimeoutSeconds(final Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
