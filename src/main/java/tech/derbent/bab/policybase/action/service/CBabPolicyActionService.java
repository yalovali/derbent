package tech.derbent.bab.policybase.action.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskService;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.policybase.node.file.CBabFileInputNodeService;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNode;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNodeService;
import tech.derbent.bab.policybase.node.ip.CBabHttpServerNodeService;
import tech.derbent.bab.policybase.node.modbus.CBabModbusNodeService;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;
import tech.derbent.bab.policybase.node.ros.CBabROSNodeService;
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
		if (options.includesRelations()) {
			targetAction.setActionMasks(new HashSet<>(source.getActionMasks()));
		}
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
			throw new CValidationException("Create at least one supported destination node (CAN, File Output, ROS) before adding actions");
		}
		final CBabNodeEntity<?> destinationNode = supportedNodes.get(0);
		final String name = getUniqueNameFromList("Action", listByPolicyRule(policyRule));
		final CBabPolicyAction action = new CBabPolicyAction(name, policyRule);
		action.setDestinationNode(destinationNode);
		final CBabPolicyActionMaskBase<?> defaultMask = createDefaultMaskForAction(action, destinationNode);
		action.setActionMasks(new HashSet<>(List.of(defaultMask)));
		action.setActionMask(defaultMask);
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
		if (destinationNode == null || actionMask == null || actionMask.getPolicyAction() == null
				|| actionMask.getPolicyAction().getDestinationNode() == null) {
			return false;
		}
		final Long destinationId = destinationNode.getId();
		final CBabNodeEntity<?> maskDestinationNode = actionMask.getPolicyAction().getDestinationNode();
		final Long parentId = maskDestinationNode.getId();
		if (destinationId != null && parentId != null) {
			return Objects.equals(destinationId, parentId);
		}
		return destinationNode == maskDestinationNode;
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
		if (destinationNode == null || destinationNode.getProject() == null) {
			return List.of();
		}
		return actionMaskService.listByProject(destinationNode.getProject()).stream()
				.filter(mask -> isActionMaskAllowedForDestinationNode(destinationNode, mask))
				.sorted(Comparator.comparing(CBabPolicyActionMaskBase<?>::getExecutionOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(CBabPolicyActionMaskBase::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyActionMaskBase<?>> listMasksForAction(final CBabPolicyAction action) {
		if (action == null) {
			return List.of();
		}
		final List<CBabPolicyActionMaskBase<?>> masks = action.getId() != null ? new ArrayList<>(actionMaskService.listByPolicyAction(action))
				: action.getActionMasks() != null ? new ArrayList<>(action.getActionMasks()) : new ArrayList<>();
		final CBabNodeEntity<?> destinationNode = action.getDestinationNode();
		return masks.stream().filter(mask -> destinationNode == null || isActionMaskAllowedForDestinationNode(destinationNode, mask))
				.sorted(Comparator.comparing(CBabPolicyActionMaskBase<?>::getExecutionOrder, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(CBabPolicyActionMaskBase::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	@Transactional (readOnly = true)
	public List<CBabNodeEntity<?>> listSupportedDestinationNodes(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final Map<Long, CBabNodeEntity<?>> nodesById = new LinkedHashMap<>();
		try {
			addNodesById(nodesById, CSpringContext.getBean(CBabHttpServerNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("HTTP server node service not available while listing destination nodes: {}", e.getMessage());
		}
		try {
			addNodesById(nodesById, CSpringContext.getBean(CBabFileInputNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("File input node service not available while listing destination nodes: {}", e.getMessage());
		}
		try {
			addNodesById(nodesById, CSpringContext.getBean(CBabFileOutputNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("File output node service not available while listing destination nodes: {}", e.getMessage());
		}
		try {
			addNodesById(nodesById, CSpringContext.getBean(CBabCanNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("CAN node service not available while listing destination nodes: {}", e.getMessage());
		}
		try {
			addNodesById(nodesById, CSpringContext.getBean(CBabModbusNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("Modbus node service not available while listing destination nodes: {}", e.getMessage());
		}
		try {
			addNodesById(nodesById, CSpringContext.getBean(CBabROSNodeService.class).listByProject(project));
		} catch (final Exception e) {
			LOGGER.debug("ROS node service not available while listing destination nodes: {}", e.getMessage());
		}
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
		if (entity != null && entity.getActionMask() != null && !entity.getActionMasks().contains(entity.getActionMask())) {
			final HashSet<CBabPolicyActionMaskBase<?>> masks = new HashSet<>(entity.getActionMasks());
			masks.add(entity.getActionMask());
			entity.setActionMasks(masks);
		}
		final CBabPolicyAction saved = super.save(entity);
		if (saved == null || saved.getId() == null) {
			return saved;
		}
		return ((IBabPolicyActionRepository) repository).findById(saved.getId()).orElse(saved);
	}

	private void addNodesById(final Map<Long, CBabNodeEntity<?>> nodesById, final List<? extends CBabNodeEntity<?>> nodes) {
		if (nodes == null) {
			return;
		}
		nodes.stream().filter(Objects::nonNull).forEach(node -> {
			if (!(node instanceof CBabCanNode || node instanceof CBabFileOutputNode || node instanceof CBabROSNode)) {
				return;
			}
			if (node.getId() != null) {
				nodesById.putIfAbsent(node.getId(), node);
			}
		});
	}

	private CBabPolicyActionMaskBase<?> createDefaultMaskForAction(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode) {
		if (destinationNode instanceof CBabCanNode) {
			return new CBabPolicyActionMaskCAN(action.getName() + " CAN Mask", action);
		}
		if (destinationNode instanceof CBabFileOutputNode) {
			return new CBabPolicyActionMaskFile(action.getName() + " File Mask", action);
		}
		if (destinationNode instanceof CBabROSNode) {
			return new CBabPolicyActionMaskROS(action.getName() + " ROS Mask", action);
		}
		throw new CValidationException("No action-mask type is supported for destination node type '%s'"
				.formatted(destinationNode.getClass().getSimpleName()));
	}

	private void validateActionMaskCompatibility(final CBabPolicyAction entity) {
		if (!isActionMaskAllowedForDestinationNode(entity.getDestinationNode(), entity.getActionMask())) {
			throw new CValidationException("Selected action mask must belong to selected destination node");
		}
	}

	private void validateActionMaskOwnership(final CBabPolicyAction entity) {
		if (entity.getActionMask() == null) {
			return;
		}
		final CBabPolicyAction ownerAction = entity.getActionMask().getPolicyAction();
		if (ownerAction == null) {
			throw new CValidationException("Selected action mask must be linked to a policy action");
		}
		final Long actionId = entity.getId();
		final Long ownerActionId = ownerAction.getId();
		final boolean sameAction = actionId != null && ownerActionId != null ? Objects.equals(actionId, ownerActionId) : ownerAction == entity;
		if (!sameAction) {
			throw new CValidationException("Selected action mask must belong to this policy action");
		}
	}

	private void validateActionMaskProject(final CBabPolicyAction entity) {
		if (entity.getActionMask() == null || entity.getActionMask().getPolicyAction() == null
				|| entity.getActionMask().getPolicyAction().getPolicyRule() == null) {
			return;
		}
		final Long actionProjectId = entity.getPolicyRule().getProject().getId();
		final Long maskProjectId = entity.getActionMask().getPolicyAction().getPolicyRule().getProject() != null
				? entity.getActionMask().getPolicyAction().getPolicyRule().getProject().getId()
				: null;
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
		validateActionMaskOwnership(entity);
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
