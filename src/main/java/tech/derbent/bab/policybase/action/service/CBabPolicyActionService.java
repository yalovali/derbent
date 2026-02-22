package tech.derbent.bab.policybase.action.service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskService;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.rule.service.CBabPolicyRuleService;

/** Service for destination-aware BAB policy actions owned by policy rules. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyActionService extends CEntityNamedService<CBabPolicyAction> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionService.class);
	private final CBabPolicyActionMaskService actionMaskService;

	public CBabPolicyActionService(final IBabPolicyActionRepository repository, final Clock clock, final ISessionService sessionService,
			final CBabPolicyActionMaskService actionMaskService) {
		super(repository, clock, sessionService);
		this.actionMaskService = actionMaskService;
	}

	@Override
	public String checkDeleteAllowed(final CBabPolicyAction entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		return superCheck != null ? superCheck : null;
	}

	@Override
	public void copyEntityFieldsTo(final CBabPolicyAction source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof final CBabPolicyAction targetAction)) {
			return;
		}
		targetAction.setPolicyRule(source.getPolicyRule());
		targetAction.setDestinationNode(source.getDestinationNode());
		targetAction.setActionMask(source.getActionMask());
		targetAction.setExecutionPriority(source.getExecutionPriority());
		targetAction.setExecutionOrder(source.getExecutionOrder());
		targetAction.setAsyncExecution(source.getAsyncExecution());
		targetAction.setTimeoutSeconds(source.getTimeoutSeconds());
		targetAction.setRetryCount(source.getRetryCount());
		targetAction.setRetryDelaySeconds(source.getRetryDelaySeconds());
		targetAction.setLogExecution(source.getLogExecution());
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Transactional
	public CBabPolicyAction createDraftActionForRule(final CBabPolicyRule policyRule) {
		Check.notNull(policyRule, "Policy rule cannot be null");
		Check.notNull(policyRule.getId(), "Please save policy rule before creating actions");
		Check.notNull(policyRule.getProject(), ValidationMessages.PROJECT_REQUIRED);
		final List<CBabNodeEntity<?>> supportedNodes = listSupportedDestinationNodes(policyRule.getProject());
		if (supportedNodes.isEmpty()) {
			throw new CValidationException("Create at least one destination node with an action mask before adding actions");
		}
		final CBabNodeEntity<?> destinationNode = supportedNodes.get(0);
		final List<CBabPolicyActionMaskBase<?>> masksForNode = listMasksForDestinationNode(destinationNode);
		if (masksForNode.isEmpty()) {
			throw new CValidationException("Create at least one action mask for destination node '%s'".formatted(destinationNode.getName()));
		}
		final String name = getUniqueNameFromList("Action", listByPolicyRule(policyRule));
		final CBabPolicyAction action = new CBabPolicyAction(name, policyRule);
		action.setDestinationNode(destinationNode);
		action.setActionMask(masksForNode.get(0));
		return save(action);
	}

	@Override
	public Class<CBabPolicyAction> getEntityClass() { return CBabPolicyAction.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyActionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyAction.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Transactional (readOnly = true)
	public boolean isActionMaskAllowedForDestinationNode(final CBabNodeEntity<?> destinationNode, final CBabPolicyActionMaskBase<?> actionMask) {
		if (destinationNode == null || actionMask == null || actionMask.getParentNode() == null) {
			return false;
		}
		final Long destinationId = destinationNode.getId();
		final Long parentId = actionMask.getParentNode().getId();
		if (destinationId != null && parentId != null) {
			return Objects.equals(destinationId, parentId);
		}
		return destinationNode == actionMask.getParentNode();
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyAction> listByActionMask(final CProject<?> project, final CBabPolicyActionMaskBase<?> actionMask) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(actionMask, "Action mask cannot be null");
		return listByProject(project).stream().filter(action -> Objects.equals(action.getActionMask(), actionMask)).toList();
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyAction> listByDestinationNode(final CProject<?> project, final CBabNodeEntity<?> destinationNode) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(destinationNode, "Destination node cannot be null");
		return listByProject(project).stream().filter(action -> Objects.equals(action.getDestinationNode(), destinationNode)).toList();
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyAction> listByPolicyRule(final CBabPolicyRule policyRule) {
		Check.notNull(policyRule, "Policy rule cannot be null");
		return ((IBabPolicyActionRepository) repository).findByPolicyRule(policyRule);
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyAction> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyActionRepository) repository).findByProject(project);
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyActionMaskBase<?>> listMasksForDestinationNode(final CBabNodeEntity<?> destinationNode) {
		if (destinationNode == null) {
			return List.of();
		}
		return actionMaskService.listByParentNode(destinationNode);
	}

	@Transactional (readOnly = true)
	public List<CBabNodeEntity<?>> listSupportedDestinationNodes(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final Map<Long,
				CBabNodeEntity<?>> nodesById = actionMaskService.listByProject(project).stream().map(CBabPolicyActionMaskBase::getParentNode)
						.filter(Objects::nonNull).filter(node -> node.getId() != null)
						.collect(Collectors.toMap(CBabNodeEntity::getId, node -> node, (left, right) -> left));
		return nodesById.values().stream().sorted(Comparator.comparing(CBabNodeEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	@Override
	@Transactional
	public CBabPolicyAction newEntity() throws Exception {
		return newEntity("New " + getEntityClass().getSimpleName());
	}

	@Override
	@Transactional
	public CBabPolicyAction newEntity(final String name) throws Exception {
		Check.notBlank(name, "Name cannot be null or empty");
		Check.notNull(sessionService, "Session service is not available");
		final CProject<?> activeProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CValidationException("No active project in session for creating policy action"));
		final CBabPolicyRule ownerRule = CSpringContext.getBean(CBabPolicyRuleService.class).listByProject(activeProject).stream().findFirst()
				.orElseThrow(() -> new CValidationException("Create a policy rule before creating actions"));
		return new CBabPolicyAction(name.trim(), ownerRule);
	}

	@Override
	@Transactional
	public CBabPolicyAction save(final CBabPolicyAction entity) {
		final CBabPolicyAction saved = super.save(entity);
		if (saved == null || saved.getId() == null) {
			return saved;
		}
		return ((IBabPolicyActionRepository) repository).findById(saved.getId()).orElse(saved);
	}

	private void validateActionMaskCompatibility(final CBabPolicyAction entity) {
		if (!isActionMaskAllowedForDestinationNode(entity.getDestinationNode(), entity.getActionMask())) {
			throw new CValidationException("Selected action mask must belong to selected destination node");
		}
	}

	private void validateActionMaskProject(final CBabPolicyAction entity) {
		if (entity.getActionMask() == null || entity.getActionMask().getParentNode() == null) {
			return;
		}
		final Long actionProjectId = entity.getPolicyRule().getProject().getId();
		final Long maskProjectId =
				entity.getActionMask().getParentNode().getProject() != null ? entity.getActionMask().getParentNode().getProject().getId() : null;
		if (!Objects.equals(actionProjectId, maskProjectId)) {
			throw new CValidationException("Action mask must belong to same project as action rule");
		}
	}

	private void validateDestinationNodeProject(final CBabPolicyAction entity) {
		if (entity.getDestinationNode() == null) {
			return;
		}
		final Long actionProjectId = entity.getPolicyRule().getProject().getId();
		final Long nodeProjectId = entity.getDestinationNode().getProject() != null ? entity.getDestinationNode().getProject().getId() : null;
		if (!Objects.equals(actionProjectId, nodeProjectId)) {
			throw new CValidationException("Destination node must belong to same project as action rule");
		}
	}

	@Override
	protected void validateEntity(final CBabPolicyAction entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getPolicyRule(), "Policy rule is required");
		Check.notNull(entity.getPolicyRule().getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getDestinationNode(), "Destination node is required");
		Check.notNull(entity.getActionMask(), "Action mask is required");
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		if (entity.getExecutionPriority() != null) {
			validateNumericField(entity.getExecutionPriority(), "Execution Priority", 100);
		}
		if (entity.getExecutionOrder() != null && entity.getExecutionOrder() < 0) {
			throw new CValidationException("Execution order must be non-negative");
		}
		if (entity.getTimeoutSeconds() != null && entity.getTimeoutSeconds() <= 0) {
			throw new CValidationException("Timeout must be positive");
		}
		if (entity.getRetryCount() != null && entity.getRetryCount() < 0) {
			throw new CValidationException("Retry count must be non-negative");
		}
		if (entity.getRetryDelaySeconds() != null && entity.getRetryDelaySeconds() < 0) {
			throw new CValidationException("Retry delay must be non-negative");
		}
		validateUniqueNameInRule(entity);
		validateDestinationNodeProject(entity);
		validateActionMaskProject(entity);
		validateActionMaskCompatibility(entity);
	}

	private void validateUniqueNameInRule(final CBabPolicyAction entity) {
		final IBabPolicyActionRepository actionRepository = (IBabPolicyActionRepository) repository;
		actionRepository.findByNameAndPolicyRule(entity.getName(), entity.getPolicyRule()).filter(existing -> !Objects.equals(existing.getId(), entity.getId())).ifPresent(existing -> {
			throw new CValidationException(
					"Action name '%s' already exists in rule '%s'".formatted(entity.getName(), entity.getPolicyRule().getName()));
		});
	}
}
