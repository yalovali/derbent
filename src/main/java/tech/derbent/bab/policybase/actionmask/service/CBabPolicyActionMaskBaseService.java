package tech.derbent.bab.policybase.actionmask.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Shared business logic for action mask entities. */
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public abstract class CBabPolicyActionMaskBaseService<MaskType extends CBabPolicyActionMaskBase<MaskType>>
		extends CEntityNamedService<MaskType> {

	protected CBabPolicyActionMaskBaseService(final IPolicyActionMaskEntityRepository<MaskType> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public void copyEntityFieldsTo(final MaskType source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!getEntityClass().isInstance(target)) {
			return;
		}
		final MaskType targetMask = getEntityClass().cast(target);
		targetMask.setParentNode(source.getParentNode());
		targetMask.setExecutionOrder(source.getExecutionOrder());
		targetMask.setMaskConfigurationJson(source.getMaskConfigurationJson());
		targetMask.setMaskTemplateJson(source.getMaskTemplateJson());
		copyTypeSpecificFieldsTo(source, targetMask, options);
	}

	public List<MaskType> findEnabledByParentNode(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		return ((IPolicyActionMaskEntityRepository<MaskType>) repository).findEnabledByParentNode(parentNode);
	}

	public List<MaskType> listByParentNode(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		return ((IPolicyActionMaskEntityRepository<MaskType>) repository).findByParentNode(parentNode);
	}

	public List<MaskType> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPolicyActionMaskEntityRepository<MaskType>) repository).listByProject(project);
	}

	@Override
	protected void validateEntity(final MaskType entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getParentNode(), "Parent node is required");
		Check.notNull(entity.getParentNode().getProject(), ValidationMessages.PROJECT_REQUIRED);
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		if (entity.getExecutionOrder() != null && entity.getExecutionOrder() < 0) {
			throw new CValidationException("Execution order must be non-negative");
		}
		validateParentNodeType(entity);
		validateUniqueNameInParentNode(entity);
		validateTypeSpecificFields(entity);
	}

	protected abstract void copyTypeSpecificFieldsTo(MaskType source, MaskType target, CCloneOptions options);

	protected abstract void validateTypeSpecificFields(MaskType entity);

	private void validateParentNodeType(final MaskType entity) {
		final Class<? extends CBabNodeEntity<?>> allowedNodeType = entity.getAllowedNodeType();
		if (allowedNodeType == null || entity.getParentNode() == null) {
			return;
		}
		if (!allowedNodeType.isAssignableFrom(entity.getParentNode().getClass())) {
			throw new CValidationException(
					"Action mask type %s can only belong to node type %s".formatted(entity.getMaskKind(), allowedNodeType.getSimpleName()));
		}
	}

	private void validateUniqueNameInParentNode(final MaskType entity) {
		final IPolicyActionMaskEntityRepository<MaskType> maskRepository = (IPolicyActionMaskEntityRepository<MaskType>) repository;
		maskRepository.findByNameAndParentNode(entity.getName(), entity.getParentNode()).ifPresent(existing -> {
			if (!Objects.equals(existing.getId(), entity.getId())) {
				throw new CValidationException("Action mask name '%s' already exists for destination node '%s'"
						.formatted(entity.getName(), entity.getParentNode().getName()));
			}
		});
	}
}
