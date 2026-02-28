package tech.derbent.bab.policybase.action.service;

import java.time.Clock;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.ProxyUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;
import tech.derbent.bab.policybase.actionmask.domain.ROutputActionMapping;
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskCANService;
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskFileService;
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskROSService;
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
	@PersistenceContext
	private EntityManager entityManager;

	public CBabPolicyActionService(final IBabPolicyActionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
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

	private String buildMaskNameForDestination(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode) {
		final String actionName = action.getName() != null ? action.getName() : "Action";
		final String nodeNamePart = destinationNode.getName() != null && !destinationNode.getName().isBlank() ? destinationNode.getName()
				: destinationNode.getId() != null ? "Node-" + destinationNode.getId() : destinationNode.getClass().getSimpleName();
		final String nodeType = getDestinationNodeTypeName(destinationNode).replace("CBab", "").replace("Node", "");
		return actionName + " " + nodeType + " Mask [" + nodeNamePart + "]";
	}

	private String getDestinationNodeTypeName(final CBabNodeEntity<?> destinationNode) {
		if (destinationNode == null) {
			return "null";
		}
		final Class<?> resolvedNodeClass = resolveDestinationNodeClass(destinationNode);
		return resolvedNodeClass != null ? resolvedNodeClass.getSimpleName() : destinationNode.getClass().getSimpleName();
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
				targetAction.setActionMask(source.getActionMask());
			}
			targetAction.setTimeoutSeconds(source.getTimeoutSeconds());
			targetAction.setLogEnabled(source.getLogEnabled());
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
		// Persist action first to avoid transient FK cycles between action <-> selected mask.
		final CBabPolicyAction persistedAction = repository.save(action);
		final CBabPolicyActionMaskBase<?> defaultMask = createMaskForDestination(persistedAction, destinationNode);
		final CBabPolicyActionMaskBase<?> persistedMask = saveMaskForAction(defaultMask);
		persistedAction.setActionMask(persistedMask);
		return save(persistedAction);
	}

	public CBabPolicyActionMaskBase<?> createMaskForDestination(final CBabPolicyAction action, final CBabNodeEntity<?> destinationNode) {
		Check.notNull(action, "Policy action cannot be null");
		Check.notNull(destinationNode, "Destination node cannot be null");
		final String maskName = buildMaskNameForDestination(action, destinationNode);
		final Class<?> destinationNodeClass = resolveDestinationNodeClass(destinationNode);
		if (destinationNodeClass != null && CBabCanNode.class.isAssignableFrom(destinationNodeClass)) {
			return new CBabPolicyActionMaskCAN(maskName, action);
		}
		if (destinationNodeClass != null && CBabFileOutputNode.class.isAssignableFrom(destinationNodeClass)) {
			return new CBabPolicyActionMaskFile(maskName, action);
		}
		if (destinationNodeClass != null && CBabROSNode.class.isAssignableFrom(destinationNodeClass)) {
			return new CBabPolicyActionMaskROS(maskName, action);
		}
		throw new CValidationException(
				"No action-mask type is supported for destination node type '%s' (resolvedType='%s', nodeId=%s)"
						.formatted(destinationNode.getClass().getSimpleName(),
								destinationNodeClass != null ? destinationNodeClass.getSimpleName() : null, destinationNode.getId()));
	}

	private void deletePersistedMask(final CBabPolicyActionMaskBase<?> mask) {
		if (mask == null || mask.getId() == null) {
			return;
		}
		if (mask instanceof final CBabPolicyActionMaskCAN canMask) {
			CSpringContext.getBean(CBabPolicyActionMaskCANService.class).delete(canMask.getId());
			return;
		}
		if (mask instanceof final CBabPolicyActionMaskFile fileMask) {
			CSpringContext.getBean(CBabPolicyActionMaskFileService.class).delete(fileMask.getId());
			return;
		}
		if (mask instanceof final CBabPolicyActionMaskROS rosMask) {
			CSpringContext.getBean(CBabPolicyActionMaskROSService.class).delete(rosMask.getId());
			return;
		}
		throw new CValidationException("Unsupported action-mask type '%s'".formatted(mask.getClass().getSimpleName()));
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
		return listByProject(project).stream().filter(action -> {
			final CBabPolicyActionMaskBase<?> currentMask = action.getActionMask();
			if (currentMask == null) {
				return false;
			}
			return currentMask == actionMask
					|| currentMask.getId() != null && actionMask.getId() != null && Objects.equals(currentMask.getId(), actionMask.getId());
		}).toList();
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
	public List<CBabPolicyActionMaskBase<?>> listMasksForAction(final CBabPolicyAction action) {
		if (action == null) {
			return List.of();
		}
		final CBabPolicyActionMaskBase<?> mask = action.getActionMask();
		if (mask == null) {
			return List.of();
		}
		final CBabNodeEntity<?> destinationNode = action.getDestinationNode();
		if (destinationNode != null && !isActionMaskAllowedForDestinationNode(destinationNode, mask)) {
			return List.of();
		}
		return List.of(mask);
	}

	@Transactional (readOnly = true)
	public CBabPolicyActionMaskBase<?> refreshPersistedMaskById(final CBabPolicyActionMaskBase<?> mask) {
		if (mask == null || mask.getId() == null) {
			return mask;
		}
		if (mask instanceof final CBabPolicyActionMaskCAN canMask) {
			final CBabPolicyActionMaskCAN saved = CSpringContext.getBean(CBabPolicyActionMaskCANService.class).getById(canMask.getId()).orElse(null);
			return saved != null ? saved : mask;
		}
		if (mask instanceof final CBabPolicyActionMaskFile fileMask) {
			final CBabPolicyActionMaskFile saved = CSpringContext.getBean(CBabPolicyActionMaskFileService.class).getById(fileMask.getId()).orElse(null);
			return saved != null ? saved : mask;
		}
		if (mask instanceof final CBabPolicyActionMaskROS rosMask) {
			final CBabPolicyActionMaskROS saved = CSpringContext.getBean(CBabPolicyActionMaskROSService.class).getById(rosMask.getId()).orElse(null);
			return saved != null ? saved : mask;
		}
		return mask;
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

	@Transactional
	public CBabPolicyActionMaskBase<?> persistActionMaskForAction(final CBabPolicyAction action, final CBabPolicyActionMaskBase<?> mask) {
		Check.notNull(action, "Policy action cannot be null");
		Check.notNull(mask, "Action mask cannot be null");
		Check.notNull(action.getId(), "Policy action must be saved before persisting action mask");
		CBabPolicyActionMaskBase<?> maskToPersist = mask;
		if (!isMaskTypeAllowedForDestinationNode(action.getDestinationNode(), maskToPersist)) {
			LOGGER.warn(
					"Replacing incompatible action mask before persistence actionId={} destinationNodeId={} destinationNodeClass={} maskId={} maskKind={} allowedNodeType={}",
					action.getId(), action.getDestinationNode() != null ? action.getDestinationNode().getId() : null,
					action.getDestinationNode() != null ? action.getDestinationNode().getClass().getSimpleName() : null,
					maskToPersist.getId(), maskToPersist.getMaskKind(),
					maskToPersist.getAllowedNodeType() != null ? maskToPersist.getAllowedNodeType().getSimpleName() : null);
			maskToPersist = ensureCompatibleMaskForDestination(action);
			Check.notNull(maskToPersist, "Compatible action mask cannot be null after normalization");
		}
		maskToPersist.setPolicyAction(action);
		if (maskToPersist.getId() != null) {
			// Existing masks can be stale in the action page context (mask details are edited in a nested page).
			// Reload by ID and keep the persisted state to avoid overwriting nested-page changes (e.g. output mappings).
			final CBabPolicyActionMaskBase<?> refreshedMask = refreshPersistedMaskById(maskToPersist);
			Check.notNull(refreshedMask, "Existing action mask could not be refreshed by ID");
			refreshedMask.setPolicyAction(action);
			return refreshedMask;
		}
		if (maskToPersist.getId() == null) {
			final CBabPolicyAction managedAction = ((IBabPolicyActionRepository) repository).findById(action.getId())
					.orElseThrow(() -> new CValidationException("Policy action not found: id=%s".formatted(action.getId())));
			final CBabPolicyActionMaskBase<?> existingMask = managedAction.getActionMask();
			if (existingMask != null) {
				managedAction.setActionMask(null);
				repository.save(managedAction);
				deletePersistedMask(existingMask);
				entityManager.flush();
			}
		}
		final CBabPolicyActionMaskBase<?> persistedMask = saveMaskForAction(maskToPersist);
		action.setActionMask(persistedMask);
		return persistedMask;
	}

	@Override
	@Transactional
	public CBabPolicyAction save(final CBabPolicyAction entity) {
		Long existingMaskIdBeforeActionSave = null;
		List<ROutputActionMapping> expectedMappingsBeforeActionSave = List.of();
		if (entity != null) {
			LOGGER.info(
					"Saving policy action actionId={} actionName='{}' actionClass={} ruleId={} ruleClass={} destinationNodeId={} destinationNodeClass={} maskId={} maskName='{}' maskClass={} maskKind={}",
					entity.getId(), entity.getName(), entity.getClass().getSimpleName(),
					entity.getPolicyRule() != null ? entity.getPolicyRule().getId() : null,
					entity.getPolicyRule() != null ? entity.getPolicyRule().getClass().getSimpleName() : null,
					entity.getDestinationNode() != null ? entity.getDestinationNode().getId() : null,
					entity.getDestinationNode() != null ? entity.getDestinationNode().getClass().getSimpleName() : null,
					entity.getActionMask() != null ? entity.getActionMask().getId() : null,
					entity.getActionMask() != null ? entity.getActionMask().getName() : null,
					entity.getActionMask() != null ? entity.getActionMask().getClass().getSimpleName() : null,
					entity.getActionMask() != null ? entity.getActionMask().getMaskKind() : null);
		}
		if (entity != null) {
			ensureCompatibleMaskForDestination(entity);
			if (entity.getActionMask() != null) {
				// IMPORTANT REGRESSION GUARD (keep):
				// Action has cascade=ALL to actionMask. Mask details are edited/saved in nested mask pages.
				// Without refreshing by ID here, a stale action-page mask snapshot can be merged and overwrite
				// already-persisted mappings when the action dialog is saved.
				if (entity.getActionMask().getId() != null) {
					final CBabPolicyActionMaskBase<?> refreshedMask = refreshPersistedMaskById(entity.getActionMask());
					if (refreshedMask != null) {
						entity.setActionMask(refreshedMask);
						existingMaskIdBeforeActionSave = refreshedMask.getId();
						expectedMappingsBeforeActionSave = refreshedMask.getOutputActionMappings() != null
								? List.copyOf(refreshedMask.getOutputActionMappings())
								: List.of();
					}
				}
				entity.getActionMask().setPolicyAction(entity);
			}
		}
		CBabPolicyAction saved = super.save(entity);
		if (saved == null || saved.getId() == null) {
			return saved;
		}
		if (saved.getActionMask() != null) {
			final CBabPolicyActionMaskBase<?> currentMask = saved.getActionMask();
			if (currentMask.getId() == null) {
				final CBabPolicyActionMaskBase<?> persistedMask = persistActionMaskForAction(saved, currentMask);
				if (persistedMask != null) {
					saved.setActionMask(persistedMask);
				}
				} else {
					// Existing masks are edited/saved through their own page services.
					// Avoid re-saving here to prevent stale action-context instances from overwriting mask fields.
					final CBabPolicyActionMaskBase<?> refreshedMask = refreshPersistedMaskById(currentMask);
					if (refreshedMask != null) {
						assertMappingsPreservedAfterActionSave(existingMaskIdBeforeActionSave, expectedMappingsBeforeActionSave, refreshedMask);
						refreshedMask.setPolicyAction(saved);
						saved.setActionMask(refreshedMask);
					}
				}
			}
		return ((IBabPolicyActionRepository) repository).findById(saved.getId()).orElse(saved);
	}

	private void assertMappingsPreservedAfterActionSave(final Long expectedMaskId,
			final List<ROutputActionMapping> expectedMappings,
			final CBabPolicyActionMaskBase<?> refreshedMask) {
		if (expectedMaskId == null || refreshedMask == null || refreshedMask.getId() == null) {
			return;
		}
		// Fail-fast for silent data loss: action save must not mutate existing mask mappings.
		if (!Objects.equals(expectedMaskId, refreshedMask.getId())) {
			return;
		}
		final List<ROutputActionMapping> actualMappings = refreshedMask.getOutputActionMappings() != null
				? List.copyOf(refreshedMask.getOutputActionMappings())
				: List.of();
		if (Objects.equals(expectedMappings, actualMappings)) {
			return;
		}
		throw new CValidationException(
				"Action save changed mask output mappings unexpectedly (actionMaskId=%s expectedCount=%s actualCount=%s)"
						.formatted(refreshedMask.getId(), expectedMappings.size(), actualMappings.size()));
	}

	private CBabPolicyActionMaskBase<?> saveMaskForAction(final CBabPolicyActionMaskBase<?> mask) {
		Check.notNull(mask, "Action mask cannot be null");
		if (mask instanceof final CBabPolicyActionMaskCAN canMask) {
			return CSpringContext.getBean(CBabPolicyActionMaskCANService.class).save(canMask);
		}
		if (mask instanceof final CBabPolicyActionMaskFile fileMask) {
			return CSpringContext.getBean(CBabPolicyActionMaskFileService.class).save(fileMask);
		}
		if (mask instanceof final CBabPolicyActionMaskROS rosMask) {
			return CSpringContext.getBean(CBabPolicyActionMaskROSService.class).save(rosMask);
		}
		throw new CValidationException("Unsupported action-mask type '%s'".formatted(mask.getClass().getSimpleName()));
	}

	private CBabPolicyActionMaskBase<?> ensureCompatibleMaskForDestination(final CBabPolicyAction action) {
		if (action == null || action.getDestinationNode() == null) {
			return null;
		}
		final CBabPolicyActionMaskBase<?> currentMask = action.getActionMask();
		if (currentMask != null && isMaskTypeAllowedForDestinationNode(action.getDestinationNode(), currentMask)) {
			currentMask.setPolicyAction(action);
			return currentMask;
		}
		final CBabPolicyActionMaskBase<?> replacementMask = createMaskForDestination(action, action.getDestinationNode());
		replacementMask.setPolicyAction(action);
		action.setActionMask(replacementMask);
		LOGGER.warn(
				"Auto-replaced incompatible action mask actionId={} destinationNodeId={} destinationNodeClass={} previousMaskId={} previousMaskKind={} replacementMaskKind={}",
				action.getId(), action.getDestinationNode() != null ? action.getDestinationNode().getId() : null,
				action.getDestinationNode() != null ? action.getDestinationNode().getClass().getSimpleName() : null,
				currentMask != null ? currentMask.getId() : null, currentMask != null ? currentMask.getMaskKind() : null,
				replacementMask.getMaskKind());
		return replacementMask;
	}

	private Class<?> resolveDestinationNodeClass(final CBabNodeEntity<?> destinationNode) {
		if (destinationNode == null) {
			return null;
		}
		final Class<?> directClass = ProxyUtils.getUserClass(destinationNode);
		if (isSupportedDestinationNodeClass(directClass)) {
			return directClass;
		}
		try {
			final Class<?> hibernateClass = ProxyUtils.getUserClass(Hibernate.getClass(destinationNode));
			if (isSupportedDestinationNodeClass(hibernateClass)) {
				return hibernateClass;
			}
		} catch (final RuntimeException e) {
			LOGGER.debug("Unable to resolve destination node class using Hibernate metadata nodeId={} runtimeClass={} reason={}",
					destinationNode.getId(), destinationNode.getClass().getSimpleName(), e.getMessage());
		}
		if (destinationNode.getId() != null) {
			final CBabNodeEntity<?> reloadedNode = (CBabNodeEntity<?>) entityManager.find(CBabNodeEntity.class, destinationNode.getId());
			if (reloadedNode != null) {
				final Class<?> reloadedClass = ProxyUtils.getUserClass(reloadedNode);
				if (reloadedClass != null) {
					return reloadedClass;
				}
			}
		}
		return directClass;
	}

	private boolean isSupportedDestinationNodeClass(final Class<?> destinationNodeClass) {
		if (destinationNodeClass == null) {
			return false;
		}
		return CBabCanNode.class.isAssignableFrom(destinationNodeClass) || CBabFileOutputNode.class.isAssignableFrom(destinationNodeClass)
				|| CBabROSNode.class.isAssignableFrom(destinationNodeClass);
	}

	private boolean isMaskTypeAllowedForDestinationNode(final CBabNodeEntity<?> destinationNode, final CBabPolicyActionMaskBase<?> actionMask) {
		if (destinationNode == null || actionMask == null || actionMask.getAllowedNodeType() == null) {
			return false;
		}
		final Class<?> destinationNodeClass = resolveDestinationNodeClass(destinationNode);
		return destinationNodeClass != null && actionMask.getAllowedNodeType().isAssignableFrom(destinationNodeClass);
	}

	private void validateActionMaskCompatibility(final CBabPolicyAction entity) {
		final CBabPolicyActionMaskBase<?> selectedMask = entity.getActionMask();
		if (selectedMask == null || !isActionMaskAllowedForDestinationNode(entity.getDestinationNode(), selectedMask)
				|| !isMaskTypeAllowedForDestinationNode(entity.getDestinationNode(), selectedMask)) {
			throw new CValidationException("Selected action mask must belong to selected destination node and match destination node type");
		}
	}

	private void validateActionMaskOwnership(final CBabPolicyAction entity) {
		final CBabPolicyActionMaskBase<?> selectedMask = entity.getActionMask();
		if (selectedMask == null) {
			return;
		}
		final CBabPolicyAction ownerAction = selectedMask.getPolicyAction();
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
		final CBabPolicyActionMaskBase<?> selectedMask = entity.getActionMask();
		if (selectedMask == null || selectedMask.getPolicyAction() == null || selectedMask.getPolicyAction().getPolicyRule() == null) {
			return;
		}
		final Long actionProjectId = entity.getPolicyRule().getProject().getId();
		final Long maskProjectId = selectedMask.getPolicyAction().getPolicyRule().getProject() != null
				? selectedMask.getPolicyAction().getPolicyRule().getProject().getId() : null;
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
		Check.notBlank(entity.getActionMask().getName(), "Action mask name is required");
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		validateStringLength(entity.getActionMask().getName(), "Action mask name", CEntityConstants.MAX_LENGTH_NAME);
			if (entity.getTimeoutSeconds() != null && entity.getTimeoutSeconds() <= 0) {
				throw new CValidationException("Timeout must be positive");
			}
		validateUniqueNameInRule(entity);
		validateDestinationNodeProject(entity);
		validateActionMaskOwnership(entity);
		validateActionMaskProject(entity);
		validateActionMaskCompatibility(entity);
	}

	private void validateUniqueNameInRule(final CBabPolicyAction entity) {
		final IBabPolicyActionRepository actionRepository = (IBabPolicyActionRepository) repository;
		actionRepository.findByNameAndPolicyRule(entity.getName(), entity.getPolicyRule())
				.filter(existing -> !Objects.equals(existing.getId(), entity.getId())).ifPresent(existing -> {
					throw new CValidationException(
							"Action name '%s' already exists in rule '%s'".formatted(entity.getName(), entity.getPolicyRule().getName()));
				});
	}
}
