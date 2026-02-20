package tech.derbent.bab.policybase.filter.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Shared business logic for policy-filter entities. */
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public abstract class CBabPolicyFilterBaseService<FilterType extends CBabPolicyFilterBase<FilterType>> extends CEntityNamedService<FilterType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterBaseService.class);

	protected CBabPolicyFilterBaseService(final IPolicyFilterEntityRepository<FilterType> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final FilterType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		return superCheck != null ? superCheck : null;
	}

	@Override
	public void copyEntityFieldsTo(final FilterType source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!getEntityClass().isInstance(target)) {
			return;
		}
		final FilterType targetFilter = getEntityClass().cast(target);
		copyCommonFields(source, targetFilter);
		copyTypeSpecificFieldsTo(source, targetFilter, options);
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	/** Find filters with cache enabled. */
	@Transactional (readOnly = true)
	public List<FilterType> findCachedFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPolicyFilterEntityRepository<FilterType>) repository).findCachedFilters(project);
	}

	/** Find enabled filters for runtime processing. */
	@Transactional (readOnly = true)
	public List<FilterType> findEnabledFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPolicyFilterEntityRepository<FilterType>) repository).findEnabledByProject(project);
	}

	/** List all filters reachable through nodes in a project. */
	@Transactional (readOnly = true)
	public List<FilterType> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPolicyFilterEntityRepository<FilterType>) repository).listByProject(project);
	}

	/** Find all filters that belong to a specific parent node. */
	@Transactional (readOnly = true)
	public List<FilterType> listByParentNode(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		return ((IPolicyFilterEntityRepository<FilterType>) repository).findByParentNode(parentNode);
	}

	/** Find enabled filters for a specific parent node. */
	@Transactional (readOnly = true)
	public List<FilterType> findEnabledFilters(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		return ((IPolicyFilterEntityRepository<FilterType>) repository).findEnabledByParentNode(parentNode);
	}

	/** Find enabled filters applicable to given node type. */
	@Transactional (readOnly = true)
	public List<FilterType> findFiltersForNodeType(final CProject<?> project, final String nodeType) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(nodeType, "Node type cannot be blank");
		return ((IPolicyFilterEntityRepository<FilterType>) repository).findEnabledForNodeType(project, nodeType);
	}

	@Override
	protected void validateEntity(final FilterType entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getParentNode(), "Parent node is required");
		Check.notNull(entity.getParentNode().getProject(), ValidationMessages.PROJECT_REQUIRED);
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		if (entity.getLogicOperator() != null) {
			validateStringLength(entity.getLogicOperator(), "Logic Operator", 10);
		}
		if (entity.getNullHandling() != null) {
			validateStringLength(entity.getNullHandling(), "Null Handling", 20);
		}
		if (entity.getExecutionOrder() != null && entity.getExecutionOrder() < 0) {
			throw new CValidationException("Execution order must be non-negative");
		}
		if (entity.getMaxProcessingTimeMs() != null && entity.getMaxProcessingTimeMs() <= 0) {
			throw new CValidationException("Max processing time must be positive");
		}
		if (entity.getCacheSizeLimit() != null && entity.getCacheSizeLimit() <= 0) {
			throw new CValidationException("Cache size limit must be positive");
		}
		validateNodeTypeConfiguration(entity);
		validateLogicOperator(entity);
		validateNullHandling(entity);
		validateParentNodeType(entity);
		validateUniqueNameInParentNode(entity);
		validateTypeSpecificFields(entity);
	}

	protected final void validateRegularExpression(final String expression, final String fieldName) {
		if (expression == null || expression.isBlank()) {
			throw new CValidationException(fieldName + " is required");
		}
		try {
			Pattern.compile(expression);
		} catch (final PatternSyntaxException ex) {
			throw new CValidationException("Invalid " + fieldName + ": " + ex.getDescription());
		}
	}

	private void copyCommonFields(final FilterType source, final FilterType target) {
		target.setParentNode(source.getParentNode());
		target.setLogicOperator(source.getLogicOperator());
		target.setCaseSensitive(source.getCaseSensitive());
		target.setNullHandling(source.getNullHandling());
		target.setExecutionOrder(source.getExecutionOrder());
		target.setMaxProcessingTimeMs(source.getMaxProcessingTimeMs());
		target.setLogMatches(source.getLogMatches());
		target.setLogRejections(source.getLogRejections());
		target.setCacheEnabled(source.getCacheEnabled());
		target.setCacheSizeLimit(source.getCacheSizeLimit());
		target.setCanNodeEnabled(source.getCanNodeEnabled());
		target.setModbusNodeEnabled(source.getModbusNodeEnabled());
		target.setHttpNodeEnabled(source.getHttpNodeEnabled());
		target.setFileNodeEnabled(source.getFileNodeEnabled());
		target.setSyslogNodeEnabled(source.getSyslogNodeEnabled());
		target.setRosNodeEnabled(source.getRosNodeEnabled());
	}

	protected abstract void copyTypeSpecificFieldsTo(FilterType source, FilterType target, CCloneOptions options);

	protected abstract void validateTypeSpecificFields(FilterType entity);

	private void validateParentNodeType(final FilterType entity) {
		final Class<? extends CBabNodeEntity<?>> allowedNodeType = entity.getAllowedNodeType();
		if (allowedNodeType == null || entity.getParentNode() == null) {
			return;
		}
		if (!allowedNodeType.isAssignableFrom(entity.getParentNode().getClass())) {
			throw new CValidationException(
					"Filter type %s can only belong to node type %s".formatted(entity.getFilterKind(), allowedNodeType.getSimpleName()));
		}
	}

	private void validateLogicOperator(final FilterType entity) {
		if (entity.getLogicOperator() == null) {
			return;
		}
		final String operator = entity.getLogicOperator().toUpperCase();
		if (!CBabPolicyFilterBase.LOGIC_OPERATOR_AND.equals(operator)
				&& !CBabPolicyFilterBase.LOGIC_OPERATOR_OR.equals(operator)
				&& !CBabPolicyFilterBase.LOGIC_OPERATOR_NOT.equals(operator)) {
			throw new CValidationException("Logic operator must be AND, OR, or NOT");
		}
	}

	private void validateNodeTypeConfiguration(final FilterType entity) {
		final boolean anyNodeTypeEnabled = Boolean.TRUE.equals(entity.getCanNodeEnabled())
				|| Boolean.TRUE.equals(entity.getModbusNodeEnabled())
				|| Boolean.TRUE.equals(entity.getHttpNodeEnabled())
				|| Boolean.TRUE.equals(entity.getFileNodeEnabled())
				|| Boolean.TRUE.equals(entity.getSyslogNodeEnabled())
				|| Boolean.TRUE.equals(entity.getRosNodeEnabled());
		if (!anyNodeTypeEnabled) {
			throw new CValidationException("At least one node type must be enabled for the filter");
		}
	}

	private void validateNullHandling(final FilterType entity) {
		if (entity.getNullHandling() == null) {
			return;
		}
		final String strategy = entity.getNullHandling().toLowerCase();
		if (!CBabPolicyFilterBase.NULL_HANDLING_IGNORE.equals(strategy)
				&& !CBabPolicyFilterBase.NULL_HANDLING_REJECT.equals(strategy)
				&& !CBabPolicyFilterBase.NULL_HANDLING_PASS.equals(strategy)
				&& !CBabPolicyFilterBase.NULL_HANDLING_DEFAULT.equals(strategy)) {
			throw new CValidationException("Null handling must be ignore, reject, pass, or default");
		}
	}

	private void validateUniqueNameInParentNode(final FilterType entity) {
		final IPolicyFilterEntityRepository<FilterType> filterRepository = (IPolicyFilterEntityRepository<FilterType>) repository;
		filterRepository.findByNameAndParentNode(entity.getName(), entity.getParentNode()).ifPresent(existing -> {
			if (!Objects.equals(existing.getId(), entity.getId())) {
				throw new CValidationException(
						"Filter name '%s' already exists for node '%s'".formatted(entity.getName(), entity.getParentNode().getName()));
			}
		});
	}
}
