package tech.derbent.bab.policybase.actionmask.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.ProxyUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.actionmask.domain.ROutputActionMapping;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Shared business logic for action mask entities. */
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public abstract class CBabPolicyActionMaskBaseService<MaskType extends CBabPolicyActionMaskBase<MaskType>>
		extends CEntityNamedService<MaskType> {

	@PersistenceContext
	private EntityManager entityManager;

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
		targetMask.setPolicyAction(source.getPolicyAction());
		targetMask.setExecutionOrder(source.getExecutionOrder());
		targetMask.setOutputMethod(source.getOutputMethod());
		targetMask.setOutputActionMappings(source.getOutputActionMappings());
		copyTypeSpecificFieldsTo(source, targetMask, options);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<MaskType> getById(final Long id) {
		return super.getById(id).map(this::initializeParentContext);
	}

	@Override
	@Transactional (readOnly = true)
	public Page<MaskType> listForPageView(final Pageable pageable, final String searchText) throws Exception {
		final Page<MaskType> page = super.listForPageView(pageable, searchText);
		page.getContent().forEach(this::initializeParentContext);
		return page;
	}

	@Override
	@Transactional
	public MaskType save(final MaskType entity) {
		final MaskType saved = super.save(entity);
		return initializeParentContext(saved);
	}

	@Override
	protected void validateEntity(final MaskType entity) {
		Check.notNull(entity, "Entity cannot be null");
		final CBabPolicyAction policyAction = entity.getPolicyAction();
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

	private void validateDestinationNodeType(final MaskType entity) {
		final Class<? extends CBabNodeEntity<?>> allowedNodeType = entity.getAllowedNodeType();
		final CBabNodeEntity<?> destinationNode = entity.getPolicyAction() != null ? entity.getPolicyAction().getDestinationNode() : null;
		if (allowedNodeType == null || destinationNode == null) {
			return;
		}
		final Class<?> destinationNodeClass = resolveDestinationNodeClass(destinationNode);
		if (destinationNodeClass == null || !allowedNodeType.isAssignableFrom(destinationNodeClass)) {
			throw new CValidationException(
					"Action mask type %s can only belong to node type %s (destinationRuntimeType=%s, destinationResolvedType=%s)"
							.formatted(entity.getMaskKind(), allowedNodeType.getSimpleName(), destinationNode.getClass().getSimpleName(),
									destinationNodeClass != null ? destinationNodeClass.getSimpleName() : null));
		}
	}

	private Class<?> resolveDestinationNodeClass(final CBabNodeEntity<?> destinationNode) {
		if (destinationNode == null) {
			return null;
		}
		final Class<?> directClass = ProxyUtils.getUserClass(destinationNode);
		try {
			final Class<?> hibernateClass = ProxyUtils.getUserClass(Hibernate.getClass(destinationNode));
			if (hibernateClass != null && hibernateClass != CBabNodeEntity.class) {
				return hibernateClass;
			}
		} catch (final RuntimeException e) {
			// Fall through to best-effort class resolution below.
		}
		try {
			final Object unproxied = Hibernate.unproxy(destinationNode);
			if (unproxied instanceof final CBabNodeEntity<?> unproxiedNode) {
				final Class<?> unproxiedClass = ProxyUtils.getUserClass(unproxiedNode);
				if (unproxiedClass != null) {
					return unproxiedClass;
				}
			}
		} catch (final RuntimeException e) {
			// Keep validation deterministic using available class information.
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

	private void validateUniqueNameInPolicyAction(final MaskType entity) {
		final CBabPolicyAction policyAction = entity.getPolicyAction();
		if (policyAction == null) {
			return;
		}
		final CBabPolicyActionMaskBase<?> existing = policyAction.getActionMask();
		if (existing == null || existing == entity) {
			return;
		}
		if (existing.getName() != null && entity.getName() != null && existing.getName().equals(entity.getName())
				&& (entity.getId() == null || !entity.getId().equals(existing.getId()))) {
			throw new CValidationException(
					"Action mask name '%s' already exists for policy action '%s'".formatted(entity.getName(), entity.getPolicyAction().getName()));
		}
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

	private MaskType initializeParentContext(final MaskType entity) {
		if (entity == null) {
			return null;
		}
		final CBabPolicyAction policyAction = entity.getPolicyAction();
		if (policyAction != null) {
			Hibernate.initialize(policyAction);
			final var policyRule = policyAction.getPolicyRule();
			Hibernate.initialize(policyRule);
			if (policyRule != null) {
				final var sourceFilter = policyRule.getFilter();
				Hibernate.initialize(sourceFilter);
				if (sourceFilter != null) {
					Hibernate.initialize(sourceFilter.getParentNode());
				}
			}
			Hibernate.initialize(policyAction.getDestinationNode());
		}
		return entity;
	}
}
