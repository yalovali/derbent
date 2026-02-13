package tech.derbent.bab.policybase.filter.service;

import java.time.Clock;
import java.util.List;
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
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;

/** CBabPolicyFilterService - Service for managing BAB policy filters. Provides business logic for filter entities including: - CRUD operations with
 * validation - Filter type management - Node compatibility checking - Condition and transformation validation - Performance optimization settings
 * Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Service with @Service annotation */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyFilterService extends CEntityOfProjectService<CBabPolicyFilter> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterService.class);

	public CBabPolicyFilterService(final IBabPolicyFilterRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CBabPolicyFilter entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Add business-specific delete checks here
		// For example, check if filter is being used by any policy rules
		return null; // Delete allowed
	}

	/** Copy entity fields for cloning operations. */
	@Override
	public void copyEntityFieldsTo(final CBabPolicyFilter source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof CBabPolicyFilter)) {
			return;
		}
		final CBabPolicyFilter targetFilter = (CBabPolicyFilter) target;
		// Copy filter-specific fields
		targetFilter.setFilterType(source.getFilterType());
		targetFilter.setConfigurationJson(source.getConfigurationJson());
		targetFilter.setConditionsJson(source.getConditionsJson());
		targetFilter.setTransformationJson(source.getTransformationJson());
		targetFilter.setLogicOperator(source.getLogicOperator());
		targetFilter.setCaseSensitive(source.getCaseSensitive());
		targetFilter.setNullHandling(source.getNullHandling());
		targetFilter.setExecutionOrder(source.getExecutionOrder());
		targetFilter.setMaxProcessingTimeMs(source.getMaxProcessingTimeMs());
		targetFilter.setLogMatches(source.getLogMatches());
		targetFilter.setLogRejections(source.getLogRejections());
		targetFilter.setCacheEnabled(source.getCacheEnabled());
		targetFilter.setCacheSizeLimit(source.getCacheSizeLimit());
		// Copy node type settings
		targetFilter.setCanNodeEnabled(source.getCanNodeEnabled());
		targetFilter.setModbusNodeEnabled(source.getModbusNodeEnabled());
		targetFilter.setHttpNodeEnabled(source.getHttpNodeEnabled());
		targetFilter.setFileNodeEnabled(source.getFileNodeEnabled());
		targetFilter.setSyslogNodeEnabled(source.getSyslogNodeEnabled());
		targetFilter.setRosNodeEnabled(source.getRosNodeEnabled());
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	/** Find filters by filter type. */
	@Transactional (readOnly = true)
	public List<CBabPolicyFilter> findByFilterType(final CProject<?> project, final String filterType) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(filterType, "Filter type cannot be blank");
		return ((IBabPolicyFilterRepository) repository).findByProjectAndFilterType(project, filterType);
	}

	/** Find filters with caching enabled. */
	@Transactional (readOnly = true)
	public List<CBabPolicyFilter> findCachedFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyFilterRepository) repository).findCachedFilters(project);
	}

	/** Find enabled filters for processing. */
	@Transactional (readOnly = true)
	public List<CBabPolicyFilter> findEnabledFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyFilterRepository) repository).findEnabledByProject(project);
	}

	/** Find filters compatible with specific node type. */
	@Transactional (readOnly = true)
	public List<CBabPolicyFilter> findFiltersForNodeType(final CProject<?> project, final String nodeType) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(nodeType, "Node type cannot be blank");
		return ((IBabPolicyFilterRepository) repository).findEnabledForNodeType(project, nodeType);
	}

	/** Find transformation filters. */
	@Transactional (readOnly = true)
	public List<CBabPolicyFilter> findTransformationFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyFilterRepository) repository).findTransformationFilters(project);
	}

	@Override
	public Class<CBabPolicyFilter> getEntityClass() { return CBabPolicyFilter.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyFilterInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilter.class; }
	// Business logic methods

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CBabPolicyFilter entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notBlank(entity.getFilterType(), "Filter type is required");
		// 2. String Length Validation
		validateStringLength(entity.getName(), "Name", 255);
		validateStringLength(entity.getFilterType(), "Filter Type", 50);
		if (entity.getLogicOperator() != null) {
			validateStringLength(entity.getLogicOperator(), "Logic Operator", 10);
		}
		if (entity.getNullHandling() != null) {
			validateStringLength(entity.getNullHandling(), "Null Handling", 20);
		}
		// 3. Numeric Validation
		if (entity.getExecutionOrder() != null && entity.getExecutionOrder() < 0) {
			throw new CValidationException("Execution order must be non-negative");
		}
		if (entity.getMaxProcessingTimeMs() != null && entity.getMaxProcessingTimeMs() <= 0) {
			throw new CValidationException("Max processing time must be positive");
		}
		if (entity.getCacheSizeLimit() != null && entity.getCacheSizeLimit() <= 0) {
			throw new CValidationException("Cache size limit must be positive");
		}
		// 4. Unique Name Validation
		validateUniqueNameInProject((IBabPolicyFilterRepository) repository, entity, entity.getName(), entity.getProject());
		// 5. Business Logic Validation
		validateFilterTypeSpecificFields(entity);
		validateNodeTypeConfiguration(entity);
		validateLogicOperator(entity);
		validateNullHandling(entity);
	}

	/** Validate filter type specific fields. */
	private void validateFilterTypeSpecificFields(final CBabPolicyFilter entity) {}

	/** Validate logic operator. */
	private void validateLogicOperator(final CBabPolicyFilter entity) {
		if (entity.getLogicOperator() == null) {
			return;
		}
		final String operator = entity.getLogicOperator().toUpperCase();
		if (!operator.equals("AND") && !operator.equals("OR") && !operator.equals("NOT")) {
			throw new CValidationException("Logic operator must be AND, OR, or NOT");
		}
	}

	/** Validate node type configuration - at least one node type must be enabled. */
	private void validateNodeTypeConfiguration(final CBabPolicyFilter entity) {
		final boolean anyNodeTypeEnabled = entity.getCanNodeEnabled() != null && entity.getCanNodeEnabled()
				|| entity.getModbusNodeEnabled() != null && entity.getModbusNodeEnabled()
				|| entity.getHttpNodeEnabled() != null && entity.getHttpNodeEnabled()
				|| entity.getFileNodeEnabled() != null && entity.getFileNodeEnabled()
				|| entity.getSyslogNodeEnabled() != null && entity.getSyslogNodeEnabled()
				|| entity.getRosNodeEnabled() != null && entity.getRosNodeEnabled();
		if (!anyNodeTypeEnabled) {
			throw new CValidationException("At least one node type must be enabled for the filter");
		}
	}

	/** Validate null handling strategy. */
	private void validateNullHandling(final CBabPolicyFilter entity) {
		if (entity.getNullHandling() == null) {
			return;
		}
		final String strategy = entity.getNullHandling().toLowerCase();
		if (!strategy.equals("ignore") && !strategy.equals("reject") && !strategy.equals("pass") && !strategy.equals("default")) {
			throw new CValidationException("Null handling must be ignore, reject, pass, or default");
		}
	}
}
