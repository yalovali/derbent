package tech.derbent.bab.policybase.actionmask.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.IBabPolicyActionRepository;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.actionmask.domain.ROutputActionMapping;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Shared business logic for action mask entities. */
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public abstract class CBabPolicyActionMaskBaseService<MaskType extends CBabPolicyActionMaskBase<MaskType>>
		extends CEntityNamedService<MaskType> {

	private final IBabPolicyActionRepository actionRepository;

	protected CBabPolicyActionMaskBaseService(final IPolicyActionMaskEntityRepository<MaskType> repository, final Clock clock,
			final ISessionService sessionService, final IBabPolicyActionRepository actionRepository) {
		super(repository, clock, sessionService);
		this.actionRepository = actionRepository;
	}

	@Override
	public void copyEntityFieldsTo(final MaskType source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!getEntityClass().isInstance(target)) {
			return;
		}
		final MaskType targetMask = getEntityClass().cast(target);
		targetMask.setPolicyAction(source.getPolicyAction());
		targetMask.setExecutionOrder(source.getExecutionOrder());
		targetMask.setOutputMethod(source.getOutputMethod());
		targetMask.setOutputActionMappings(source.getOutputActionMappings());
		copyTypeSpecificFieldsTo(source, targetMask, options);
	}

	public List<MaskType> findEnabledByPolicyAction(final CBabPolicyAction policyAction) {
		Check.notNull(policyAction, "Policy action cannot be null");
		final List<MaskType> masks = ((IPolicyActionMaskEntityRepository<MaskType>) repository).findEnabledByPolicyAction(policyAction);
		masks.forEach(mask -> mask.setPolicyAction(policyAction));
		return masks;
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<MaskType> getById(final Long id) {
		return super.getById(id).map(this::attachPolicyActionIfMissing);
	}

	public List<MaskType> listByPolicyAction(final CBabPolicyAction policyAction) {
		Check.notNull(policyAction, "Policy action cannot be null");
		final List<MaskType> masks = ((IPolicyActionMaskEntityRepository<MaskType>) repository).findByPolicyAction(policyAction);
		masks.forEach(mask -> mask.setPolicyAction(policyAction));
		return masks;
	}

	@Override
	@Transactional (readOnly = true)
	public Page<MaskType> listForPageView(final Pageable pageable, final String searchText) throws Exception {
		final Page<MaskType> page = super.listForPageView(pageable, searchText);
		page.getContent().forEach(this::attachPolicyActionIfMissing);
		return page;
	}

	public List<MaskType> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<MaskType> masks = ((IPolicyActionMaskEntityRepository<MaskType>) repository).listByProject(project);
		final var actionsById = actionRepository.findByProject(project).stream().filter(action -> action != null && action.getId() != null)
				.collect(Collectors.toMap(CBabPolicyAction::getId, Function.identity(), (left, right) -> left));
		masks.forEach(mask -> {
			if (mask == null || mask.getPolicyAction() != null || mask.getPolicyActionId() == null) {
				return;
			}
			final CBabPolicyAction policyAction = actionsById.get(mask.getPolicyActionId());
			if (policyAction != null) {
				mask.setPolicyAction(policyAction);
			}
		});
		return masks;
	}

	@Override
	@Transactional
	public MaskType save(final MaskType entity) {
		final MaskType prepared = attachPolicyActionIfMissing(entity);
		final MaskType saved = super.save(prepared);
		return attachPolicyActionIfMissing(saved);
	}

	@Override
	protected void validateEntity(final MaskType entity) {
		Check.notNull(entity, "Entity cannot be null");
		final CBabPolicyAction policyAction = attachPolicyActionIfMissing(entity).getPolicyAction();
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(policyAction, "Policy action is required");
		Check.notNull(policyAction.getPolicyRule(), "Policy rule is required");
		Check.notNull(policyAction.getPolicyRule().getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(policyAction.getDestinationNode(), "Destination node is required");
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		validateStringLength(entity.getOutputMethod(), "Output method", 60);
		validateOutputActionMappings(entity.getOutputActionMappings());
		if (entity.getExecutionOrder() != null && entity.getExecutionOrder() < 0) {
			throw new CValidationException("Execution order must be non-negative");
		}
		validateDestinationNodeType(entity);
		validateUniqueNameInPolicyAction(entity);
		validateTypeSpecificFields(entity);
	}

	protected abstract void copyTypeSpecificFieldsTo(MaskType source, MaskType target, CCloneOptions options);

	protected abstract void validateTypeSpecificFields(MaskType entity);

	private MaskType attachPolicyActionIfMissing(final MaskType entity) {
		if (entity == null || entity.getPolicyAction() != null || entity.getPolicyActionId() == null) {
			return entity;
		}
		final CBabPolicyAction policyAction = actionRepository.findById(entity.getPolicyActionId()).orElse(null);
		if (policyAction != null) {
			entity.setPolicyAction(policyAction);
		}
		return entity;
	}

	private void validateDestinationNodeType(final MaskType entity) {
		final Class<? extends CBabNodeEntity<?>> allowedNodeType = entity.getAllowedNodeType();
		final CBabNodeEntity<?> destinationNode = entity.getPolicyAction() != null ? entity.getPolicyAction().getDestinationNode() : null;
		if (allowedNodeType == null || destinationNode == null) {
			return;
		}
		if (!allowedNodeType.isAssignableFrom(destinationNode.getClass())) {
			throw new CValidationException(
					"Action mask type %s can only belong to node type %s".formatted(entity.getMaskKind(), allowedNodeType.getSimpleName()));
		}
	}

	private void validateUniqueNameInPolicyAction(final MaskType entity) {
		final IPolicyActionMaskEntityRepository<MaskType> maskRepository = (IPolicyActionMaskEntityRepository<MaskType>) repository;
		maskRepository.findByNameAndPolicyAction(entity.getName(), entity.getPolicyAction())
				.filter(existing -> !Objects.equals(existing.getId(), entity.getId())).ifPresent(existing -> {
					throw new CValidationException("Action mask name '%s' already exists for policy action '%s'"
							.formatted(entity.getName(), entity.getPolicyAction().getName()));
				});
	}

	private void validateOutputActionMappings(final List<ROutputActionMapping> mappings) {
		if (mappings == null || mappings.isEmpty()) {
			return;
		}
		for (final ROutputActionMapping mapping : mappings) {
			if (mapping == null) {
				continue;
			}
			validateStringLength(mapping.outputName(), "Output mapping source name", 255);
			validateStringLength(mapping.outputDataType(), "Output mapping source data type", 120);
			validateStringLength(mapping.targetProtocolVariableName(), "Output mapping target name", 255);
			validateStringLength(mapping.targetProtocolVariableDataType(), "Output mapping target data type", 120);
		}
	}
}
