package tech.derbent.bab.policybase.trigger.service;

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
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;

/** CBabPolicyTriggerService - Service for managing BAB policy triggers. Provides business logic for trigger entities including: - CRUD operations
 * with validation - Trigger type management - Node compatibility checking - Execution scheduling support - Cron expression validation Layer: Service
 * (MVC) Active when: 'bab' profile is active Following Derbent pattern: Service with @Service annotation */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyTriggerService extends CEntityOfProjectService<CBabPolicyTrigger> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyTriggerService.class);

	public CBabPolicyTriggerService(final IBabPolicyTriggerRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CBabPolicyTrigger entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		return superCheck != null ? superCheck : null;
	}

	/** Copy entity fields for cloning operations. */
	@Override
	public void copyEntityFieldsTo(final CBabPolicyTrigger source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof CBabPolicyTrigger targetTrigger)) {
			return;
		}
		// Copy trigger-specific fields
		targetTrigger.setTriggerType(source.getTriggerType());
		targetTrigger.setCronExpression(source.getCronExpression());
		targetTrigger.setConditionJson(source.getConditionJson());
		targetTrigger.setExecutionPriority(source.getExecutionPriority());
		targetTrigger.setExecutionOrder(source.getExecutionOrder());
		targetTrigger.setTimeoutSeconds(source.getTimeoutSeconds());
		targetTrigger.setLogExecution(source.getLogExecution());
		targetTrigger.setRetryCount(source.getRetryCount());
		// Copy node type settings
		targetTrigger.setCanNodeEnabled(source.getCanNodeEnabled());
		targetTrigger.setModbusNodeEnabled(source.getModbusNodeEnabled());
		targetTrigger.setHttpNodeEnabled(source.getHttpNodeEnabled());
		targetTrigger.setFileNodeEnabled(source.getFileNodeEnabled());
		targetTrigger.setSyslogNodeEnabled(source.getSyslogNodeEnabled());
		targetTrigger.setRosNodeEnabled(source.getRosNodeEnabled());
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	/** Find triggers by trigger type. */
	@Transactional (readOnly = true)
	public List<CBabPolicyTrigger> findByTriggerType(final CProject<?> project, final String triggerType) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(triggerType, "Trigger type cannot be blank");
		return ((IBabPolicyTriggerRepository) repository).findByProjectAndTriggerType(project, triggerType);
	}

	/** Find enabled triggers for execution. */
	@Transactional (readOnly = true)
	public List<CBabPolicyTrigger> findEnabledTriggers(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyTriggerRepository) repository).findEnabledByProject(project);
	}

	/** Find periodic triggers for scheduling. */
	@Transactional (readOnly = true)
	public List<CBabPolicyTrigger> findPeriodicTriggers(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyTriggerRepository) repository).findPeriodicTriggers(project);
	}

	/** Find startup triggers. */
	@Transactional (readOnly = true)
	public List<CBabPolicyTrigger> findStartupTriggers(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IBabPolicyTriggerRepository) repository).findStartupTriggers(project);
	}

	/** Find triggers compatible with specific node type. */
	@Transactional (readOnly = true)
	public List<CBabPolicyTrigger> findTriggersForNodeType(final CProject<?> project, final String nodeType) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(nodeType, "Node type cannot be blank");
		return ((IBabPolicyTriggerRepository) repository).findEnabledForNodeType(project, nodeType);
	}

	@Override
	public Class<CBabPolicyTrigger> getEntityClass() { return CBabPolicyTrigger.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyTriggerInitializerService.class; }
	// Business logic methods

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyTrigger.class; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Validate cron expression format (basic validation). */
	private void validateCronExpression(final String cronExpression) {
		if (cronExpression == null) {
			return;
		}
		final String[] parts = cronExpression.trim().split("\\s+");
		if (parts.length != 6) {
			throw new CValidationException("Cron expression must have 6 parts: second minute hour day month weekday");
		}
		// Basic validation - each part should not be empty
		for (final String part : parts) {
			if (part.trim().isEmpty()) {
				throw new CValidationException("Cron expression parts cannot be empty");
			}
		}
	}

	@Override
	protected void validateEntity(final CBabPolicyTrigger entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notBlank(entity.getTriggerType(), "Trigger type is required");
		// 2. String Length Validation
		validateStringLength(entity.getName(), "Name", 255);
		validateStringLength(entity.getTriggerType(), "Trigger Type", 50);
		if (entity.getCronExpression() != null) {
			validateStringLength(entity.getCronExpression(), "Cron Expression", 255);
		}
		// 3. Numeric Validation
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
		// 4. Unique Name Validation
		validateUniqueNameInProject((IBabPolicyTriggerRepository) repository, entity, entity.getName(), entity.getProject());
		// 5. Business Logic Validation
		validateTriggerTypeSpecificFields(entity);
		validateNodeTypeConfiguration(entity);
	}

	/** Validate node type configuration - at least one node type must be enabled. */
	private void validateNodeTypeConfiguration(final CBabPolicyTrigger entity) {
		final boolean anyNodeTypeEnabled = entity.getCanNodeEnabled() != null && entity.getCanNodeEnabled()
				|| entity.getModbusNodeEnabled() != null && entity.getModbusNodeEnabled()
				|| entity.getHttpNodeEnabled() != null && entity.getHttpNodeEnabled()
				|| entity.getFileNodeEnabled() != null && entity.getFileNodeEnabled()
				|| entity.getSyslogNodeEnabled() != null && entity.getSyslogNodeEnabled()
				|| entity.getRosNodeEnabled() != null && entity.getRosNodeEnabled();
		if (!anyNodeTypeEnabled) {
			throw new CValidationException("At least one node type must be enabled for the trigger");
		}
	}

	/** Validate trigger type specific fields. */
	private void validateTriggerTypeSpecificFields(final CBabPolicyTrigger entity) {
		if (!CBabPolicyTrigger.TRIGGER_TYPE_PERIODIC.equals(entity.getTriggerType())) {
			return;
		}
		if (entity.getCronExpression() == null || entity.getCronExpression().trim().isEmpty()) {
			throw new CValidationException("Cron expression is required for periodic triggers");
		}
		validateCronExpression(entity.getCronExpression());
	}
}
