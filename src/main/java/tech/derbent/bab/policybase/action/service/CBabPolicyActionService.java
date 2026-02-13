package tech.derbent.bab.policybase.action.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;

/**
 * CBabPolicyActionService - Service for managing BAB policy actions.
 * 
 * Provides business logic for action entities including:
 * - CRUD operations with validation
 * - Action type management
 * - Node compatibility checking
 * - Execution mode configuration
 * - Template and configuration validation
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Service with @Service annotation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabPolicyActionService extends CEntityOfProjectService<CBabPolicyAction>
        implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionService.class);

    public CBabPolicyActionService(
            final IBabPolicyActionRepository repository,
            final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    @Override
    public Class<CBabPolicyAction> getEntityClass() {
        return CBabPolicyAction.class;
    }

    // IEntityRegistrable implementation
    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CBabPolicyActionInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceBabPolicyAction.class;
    }

    @Override
    protected void validateEntity(final CBabPolicyAction entity) {
        super.validateEntity(entity);

        // 1. Required Fields
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
        Check.notBlank(entity.getActionType(), "Action type is required");

        // 2. String Length Validation
        validateStringLength(entity.getName(), "Name", 255);
        validateStringLength(entity.getActionType(), "Action Type", 50);

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
        if (entity.getRetryDelaySeconds() != null && entity.getRetryDelaySeconds() < 0) {
            throw new CValidationException("Retry delay must be non-negative");
        }

        // 4. Unique Name Validation
        validateUniqueNameInProject(
            (IBabPolicyActionRepository) repository,
            entity,
            entity.getName(),
            entity.getProject());

        // 5. Business Logic Validation
        validateActionTypeSpecificFields(entity);
        validateNodeTypeConfiguration(entity);
        validateAsyncExecutionConfiguration(entity);
    }

    /**
     * Copy entity fields from source to target.
     */
    @Override
    public void copyEntityFieldsTo(final CBabPolicyAction source, final CEntityDB<?> target, final CCloneOptions options) {
        super.copyEntityFieldsTo(source, target, options);
        
        if (!(target instanceof CBabPolicyAction)) {
            return;
        }
        final CBabPolicyAction targetAction = (CBabPolicyAction) target;
        
        // Copy action-specific fields
        targetAction.setActionType(source.getActionType());
        targetAction.setExecutionPriority(source.getExecutionPriority());
        targetAction.setExecutionOrder(source.getExecutionOrder());
        targetAction.setAsyncExecution(source.getAsyncExecution());
        targetAction.setTimeoutSeconds(source.getTimeoutSeconds());
        targetAction.setRetryCount(source.getRetryCount());
        targetAction.setRetryDelaySeconds(source.getRetryDelaySeconds());
        targetAction.setLogInput(source.getLogInput());
        targetAction.setLogOutput(source.getLogOutput());
        targetAction.setLogExecution(source.getLogExecution());
        targetAction.setConfigurationJson(source.getConfigurationJson());
        
        // Copy node type enablement flags
        targetAction.setCanNodeEnabled(source.getCanNodeEnabled());
        targetAction.setModbusNodeEnabled(source.getModbusNodeEnabled());
        targetAction.setHttpNodeEnabled(source.getHttpNodeEnabled());
        targetAction.setFileNodeEnabled(source.getFileNodeEnabled());
        targetAction.setSyslogNodeEnabled(source.getSyslogNodeEnabled());
        targetAction.setRosNodeEnabled(source.getRosNodeEnabled());
        
        LOGGER.debug("Copied {} '{}' with options: {}", 
                     getClass().getSimpleName(), source.getName(), options);
    }

    /**
     * Validate action type specific fields.
     */
    private void validateActionTypeSpecificFields(final CBabPolicyAction entity) {
        if (CBabPolicyAction.ACTION_TYPE_EXECUTE.equals(entity.getActionType())) {
            // Execute actions should have command configuration
            if (entity.getConfigurationJson() == null || 
                entity.getConfigurationJson().trim().isEmpty() ||
                !entity.getConfigurationJson().contains("command")) {
                throw new CValidationException("Execute actions require command configuration");
            }
        }

        if (CBabPolicyAction.ACTION_TYPE_NOTIFY.equals(entity.getActionType())) {
            // Notify actions should have template
            if (entity.getTemplateJson() == null || entity.getTemplateJson().trim().isEmpty()) {
                throw new CValidationException("Notify actions require template configuration");
            }
        }

        if (CBabPolicyAction.ACTION_TYPE_TRANSFORM.equals(entity.getActionType())) {
            // Transform actions should have template
            if (entity.getTemplateJson() == null || entity.getTemplateJson().trim().isEmpty()) {
                throw new CValidationException("Transform actions require template configuration");
            }
        }
    }

    /**
     * Validate node type configuration - at least one node type must be enabled.
     */
    private void validateNodeTypeConfiguration(final CBabPolicyAction entity) {
        final boolean anyNodeTypeEnabled = 
            (entity.getCanNodeEnabled() != null && entity.getCanNodeEnabled()) ||
            (entity.getModbusNodeEnabled() != null && entity.getModbusNodeEnabled()) ||
            (entity.getHttpNodeEnabled() != null && entity.getHttpNodeEnabled()) ||
            (entity.getFileNodeEnabled() != null && entity.getFileNodeEnabled()) ||
            (entity.getSyslogNodeEnabled() != null && entity.getSyslogNodeEnabled()) ||
            (entity.getRosNodeEnabled() != null && entity.getRosNodeEnabled());

        if (!anyNodeTypeEnabled) {
            throw new CValidationException(
                "At least one node type must be enabled for the action");
        }
    }

    /**
     * Validate async execution configuration.
     */
    private void validateAsyncExecutionConfiguration(final CBabPolicyAction entity) {
        // Forward actions cannot be async
        if (CBabPolicyAction.ACTION_TYPE_FORWARD.equals(entity.getActionType()) &&
            entity.getAsyncExecution() != null && entity.getAsyncExecution()) {
            throw new CValidationException("Forward actions cannot be executed asynchronously");
        }
    }

    @Override
    public String checkDeleteAllowed(final CBabPolicyAction entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) {
            return superCheck;
        }

        // Add business-specific delete checks here
        // For example, check if action is being used by any policy rules

        return null; // Delete allowed
    }

    // Business logic methods

    /**
     * Find actions by action type.
     */
    @Transactional(readOnly = true)
    public List<CBabPolicyAction> findByActionType(final CProject<?> project, final String actionType) {
        Check.notNull(project, "Project cannot be null");
        Check.notBlank(actionType, "Action type cannot be blank");

        return ((IBabPolicyActionRepository) repository).findByProjectAndActionType(project, actionType);
    }

    /**
     * Find enabled actions for execution.
     */
    @Transactional(readOnly = true)
    public List<CBabPolicyAction> findEnabledActions(final CProject<?> project) {
        Check.notNull(project, "Project cannot be null");

        return ((IBabPolicyActionRepository) repository).findEnabledByProject(project);
    }

    /**
     * Find actions compatible with specific node type.
     */
    @Transactional(readOnly = true)
    public List<CBabPolicyAction> findActionsForNodeType(final CProject<?> project, final String nodeType) {
        Check.notNull(project, "Project cannot be null");
        Check.notBlank(nodeType, "Node type cannot be blank");

        return ((IBabPolicyActionRepository) repository).findEnabledForNodeType(project, nodeType);
    }

    /**
     * Find synchronous actions.
     */
    @Transactional(readOnly = true)
    public List<CBabPolicyAction> findSynchronousActions(final CProject<?> project) {
        Check.notNull(project, "Project cannot be null");

        return ((IBabPolicyActionRepository) repository).findSynchronousActions(project);
    }

    /**
     * Find asynchronous actions.
     */
    @Transactional(readOnly = true)
    public List<CBabPolicyAction> findAsynchronousActions(final CProject<?> project) {
        Check.notNull(project, "Project cannot be null");

        return ((IBabPolicyActionRepository) repository).findAsynchronousActions(project);
    }

    /**
     * Copy entity fields for cloning operations.
     */
    @Override
    public void copyEntityFieldsTo(final CBabPolicyAction source, final CEntityDB<?> target, final CCloneOptions options) {
        super.copyEntityFieldsTo(source, target, options);

        if (!(target instanceof CBabPolicyAction)) {
            return;
        }
        final CBabPolicyAction targetAction = (CBabPolicyAction) target;

        // Copy action-specific fields
        targetAction.setActionType(source.getActionType());
        targetAction.setConfigurationJson(source.getConfigurationJson());
        targetAction.setTemplateJson(source.getTemplateJson());
        targetAction.setExecutionPriority(source.getExecutionPriority());
        targetAction.setExecutionOrder(source.getExecutionOrder());
        targetAction.setTimeoutSeconds(source.getTimeoutSeconds());
        targetAction.setRetryCount(source.getRetryCount());
        targetAction.setRetryDelaySeconds(source.getRetryDelaySeconds());
        targetAction.setLogExecution(source.getLogExecution());
        targetAction.setLogInput(source.getLogInput());
        targetAction.setLogOutput(source.getLogOutput());
        targetAction.setAsyncExecution(source.getAsyncExecution());

        // Copy node type settings
        targetAction.setCanNodeEnabled(source.getCanNodeEnabled());
        targetAction.setModbusNodeEnabled(source.getModbusNodeEnabled());
        targetAction.setHttpNodeEnabled(source.getHttpNodeEnabled());
        targetAction.setFileNodeEnabled(source.getFileNodeEnabled());
        targetAction.setSyslogNodeEnabled(source.getSyslogNodeEnabled());
        targetAction.setRosNodeEnabled(source.getRosNodeEnabled());

        LOGGER.debug("Copied {} '{}' with options: {}", 
                     getClass().getSimpleName(), source.getName(), options);
    }
}