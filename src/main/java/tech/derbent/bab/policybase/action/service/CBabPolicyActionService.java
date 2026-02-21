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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
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

/** Service for destination-aware BAB policy actions. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyActionService extends CEntityOfProjectService<CBabPolicyAction> implements IEntityRegistrable, IEntityWithView {

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
		targetAction.setDestinationNode(source.getDestinationNode());
		targetAction.setActionMask(source.getActionMask());
		targetAction.setExecutionPriority(source.getExecutionPriority());
		targetAction.setExecutionOrder(source.getExecutionOrder());
		targetAction.setAsyncExecution(source.getAsyncExecution());
		targetAction.setTimeoutSeconds(source.getTimeoutSeconds());
		targetAction.setRetryCount(source.getRetryCount());
		targetAction.setRetryDelaySeconds(source.getRetryDelaySeconds());
		targetAction.setLogInput(source.getLogInput());
		targetAction.setLogOutput(source.getLogOutput());
		targetAction.setLogExecution(source.getLogExecution());
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
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
		return ((IBabPolicyActionRepository) repository).findByProjectAndActionMask(project, actionMask);
	}

	@Transactional (readOnly = true)
	public List<CBabPolicyAction> listByDestinationNode(final CProject<?> project, final CBabNodeEntity<?> destinationNode) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(destinationNode, "Destination node cannot be null");
		return ((IBabPolicyActionRepository) repository).findByProjectAndDestinationNode(project, destinationNode);
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
		final Map<Long, CBabNodeEntity<?>> nodesById = actionMaskService.listByProject(project).stream().map(CBabPolicyActionMaskBase::getParentNode)
				.filter(Objects::nonNull).filter(node -> node.getId() != null)
				.collect(Collectors.toMap(CBabNodeEntity::getId, node -> node, (left, right) -> left));
		return nodesById.values().stream().sorted(Comparator.comparing(CBabNodeEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	@Override
	protected void validateEntity(final CBabPolicyAction entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getDestinationNode(), "Destination node is required");
		Check.notNull(entity.getActionMask(), "Action mask is required");
		validateStringLength(entity.getName(), "Name", 255);
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
		validateUniqueNameInProject((IBabPolicyActionRepository) repository, entity, entity.getName(), entity.getProject());
		validateDestinationNodeProject(entity);
		validateActionMaskProject(entity);
		validateActionMaskCompatibility(entity);
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
		final Long actionProjectId = entity.getProject() != null ? entity.getProject().getId() : null;
		final Long maskProjectId = entity.getActionMask().getParentNode().getProject() != null
				? entity.getActionMask().getParentNode().getProject().getId() : null;
		if (!Objects.equals(actionProjectId, maskProjectId)) {
			throw new CValidationException("Action mask must belong to same project as action");
		}
	}

	private void validateDestinationNodeProject(final CBabPolicyAction entity) {
		if (entity.getDestinationNode() == null) {
			return;
		}
		final Long actionProjectId = entity.getProject() != null ? entity.getProject().getId() : null;
		final Long nodeProjectId = entity.getDestinationNode().getProject() != null ? entity.getDestinationNode().getProject().getId() : null;
		if (!Objects.equals(actionProjectId, nodeProjectId)) {
			throw new CValidationException("Destination node must belong to same project as action");
		}
	}
}
