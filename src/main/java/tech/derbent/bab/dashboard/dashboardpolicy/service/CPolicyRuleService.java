package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CDashboardPolicy;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CPolicyRule;
import tech.derbent.base.session.service.ISessionService;

/**
 * CPolicyRuleService - Service for BAB policy rule entities.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete service with @Service annotation.
 * 
 * Provides business logic for policy rule management:
 * - Rule configuration and validation
 * - Node relationship management (source, destination, trigger, action)
 * - Execution order and priority management
 * - Rule completeness validation
 * - Drag-and-drop operation support
 * - Rule execution tracking
 * - Calimero rule export support
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CPolicyRuleService extends CEntityOfProjectService<CPolicyRule> 
    implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyRuleService.class);
    
    // Rule validation constants
    private static final int MIN_RULE_PRIORITY = 1;
    private static final int MAX_RULE_PRIORITY = 100;
    private static final int DEFAULT_RULE_PRIORITY = 50;
    
    private static final int MIN_EXECUTION_ORDER = 0;
    private static final int MAX_EXECUTION_ORDER = 9999;
    
    // Rule execution status values
    private static final String EXECUTION_STATUS_SUCCESS = "SUCCESS";
    private static final String EXECUTION_STATUS_FAILED = "FAILED";
    private static final String EXECUTION_STATUS_PENDING = "PENDING";
    private static final String EXECUTION_STATUS_SKIPPED = "SKIPPED";
    
    public CPolicyRuleService(final IPolicyRuleRepository repository,
                             final Clock clock,
                             final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public Class<CPolicyRule> getEntityClass() {
        return CPolicyRule.class;
    }
    
    @Override
    protected void validateEntity(final CPolicyRule entity) {
        super.validateEntity(entity);
        
        // Required fields - explicit validation for critical rule fields
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
        Check.notBlank(entity.getPolicyName(), "Policy name is required");
        
        // String length validation - MANDATORY helper usage
        validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
        
        if (entity.getDescription() != null) {
            validateStringLength(entity.getDescription(), "Description", CEntityConstants.MAX_LENGTH_DESCRIPTION);
        }
        
        // Policy relationship validation
        final CDashboardPolicy policy = entity.getPolicy();
        if (policy != null && !policy.getProject().equals(entity.getProject())) {
            throw new CValidationException("Policy rule must belong to the same project as its policy");
        }
        
        // Rule priority validation
        if (entity.getRulePriority() == null) {
            throw new CValidationException("Rule priority is required");
        }
        
        validateNumericField(entity.getRulePriority(), "Rule Priority", MAX_RULE_PRIORITY);
        
        if (entity.getRulePriority() < MIN_RULE_PRIORITY || entity.getRulePriority() > MAX_RULE_PRIORITY) {
            throw new CValidationException(String.format(
                "Rule priority must be between %d and %d", MIN_RULE_PRIORITY, MAX_RULE_PRIORITY));
        }
        
        // Execution order validation
        if (entity.getExecutionOrder() != null) {
            validateNumericField(entity.getExecutionOrder(), "Execution Order", MAX_EXECUTION_ORDER);
            
            if (entity.getExecutionOrder() < MIN_EXECUTION_ORDER) {
                throw new CValidationException(String.format(
                    "Execution order must be at least %d", MIN_EXECUTION_ORDER));
            }
        }
        
        // Execution count validation
        if (entity.getExecutionCount() != null && entity.getExecutionCount() < 0) {
            throw new CValidationException("Execution count cannot be negative");
        }
        
        // Execution status validation
        if (entity.getLastExecutionStatus() != null) {
            validateStringLength(entity.getLastExecutionStatus(), "Last Execution Status", 20);
            validateExecutionStatus(entity.getLastExecutionStatus());
        }
        
        // Node relationship validation
        validateNodeReferences(entity);
        
        // Rule completeness validation
        validateRuleCompleteness(entity);
        
        // Unique name validation within policy - MANDATORY helper usage
        validateUniqueNameInProject(
            (IPolicyRuleRepository) repository,
            entity, entity.getName(), entity.getProject());
    }
    
    /**
     * Validate node references.
     */
    private void validateNodeReferences(final CPolicyRule entity) {
        // Node names should be non-empty strings when set
        if (entity.getSourceNodeName() != null && entity.getSourceNodeName().trim().isEmpty()) {
            throw new CValidationException("Source node name cannot be empty");
        }
        
        if (entity.getDestinationNodeName() != null && entity.getDestinationNodeName().trim().isEmpty()) {
            throw new CValidationException("Destination node name cannot be empty");
        }
        
        if (entity.getTriggerEntityString() != null && entity.getTriggerEntityString().trim().isEmpty()) {
            throw new CValidationException("Trigger entity name cannot be empty");
        }
        
        if (entity.getActionEntityName() != null && entity.getActionEntityName().trim().isEmpty()) {
            throw new CValidationException("Action entity name cannot be empty");
        }
        
        // Validate that source and destination are different
        if (entity.getSourceNodeName() != null && entity.getDestinationNodeName() != null && 
            entity.getSourceNodeName().trim().equals(entity.getDestinationNodeName().trim())) {
            throw new CValidationException("Source node and destination node cannot be the same");
        }
        
        // Validate that trigger and action are different (if both are set)
        if (entity.getTriggerEntityString() != null && entity.getActionEntityName() != null && 
            entity.getTriggerEntityString().trim().equals(entity.getActionEntityName().trim())) {
            LOGGER.warn("Trigger entity and action entity are the same for rule '{}'. This may create a feedback loop.", 
                       entity.getName());
        }
    }
    
    /**
     * Validate rule completeness for execution.
     */
    private void validateRuleCompleteness(final CPolicyRule entity) {
        if (entity.getIsActive() != null && entity.getIsActive()) {
            // Active rules should have all required fields
            final boolean isComplete = entity.getSourceNodeName() != null && !entity.getSourceNodeName().trim().isEmpty() && 
                                     entity.getDestinationNodeName() != null && !entity.getDestinationNodeName().trim().isEmpty() && 
                                     entity.getTriggerEntityString() != null && !entity.getTriggerEntityString().trim().isEmpty() && 
                                     entity.getActionEntityName() != null && !entity.getActionEntityName().trim().isEmpty();
            
            if (!isComplete) {
                LOGGER.warn("Active rule '{}' is incomplete. All four node references (source, destination, trigger, action) should be set for execution.", 
                           entity.getName());
            }
        }
    }
    
    /**
     * Validate execution status.
     */
    private void validateExecutionStatus(final String executionStatus) {
        final String upperStatus = executionStatus.toUpperCase();
        final String[] validStatuses = {EXECUTION_STATUS_SUCCESS, EXECUTION_STATUS_FAILED, EXECUTION_STATUS_PENDING, EXECUTION_STATUS_SKIPPED};
        
        for (final String validStatus : validStatuses) {
            if (validStatus.equals(upperStatus)) {
                return;
            }
        }
        
        throw new CValidationException(String.format(
            "Invalid execution status '%s'. Valid statuses are: %s", 
            executionStatus, String.join(", ", validStatuses)));
    }
    
    @Override
    public void initializeNewEntity(final Object entity) {
        super.initializeNewEntity(entity);
        
        if (!(entity instanceof CPolicyRule)) {
            return;
        }
        
        final CPolicyRule rule = (CPolicyRule) entity;
        
        LOGGER.debug("Initializing new policy rule entity");
        
        // Set default rule priority if not set
        if (rule.getRulePriority() == null) {
            rule.setRulePriority(DEFAULT_RULE_PRIORITY);
        }
        
        // Set default execution order (will be calculated when policy is set)
        if (rule.getExecutionOrder() == null && rule.getPolicyName() != null) {
            final CDashboardPolicy policy = rule.getPolicy();
            if (policy != null) {
                final Integer nextOrder = ((IPolicyRuleRepository) repository).findNextExecutionOrderForPolicy(policy.getName(), policy.getProject());
                rule.setExecutionOrder(nextOrder != null ? nextOrder : 0);
            }
        }
        
        // Initialize log enabled setting
        if (rule.getLogEnabled() == null) {
            rule.setLogEnabled(false);
        }
        
        // Initialize execution statistics
        if (rule.getExecutionCount() == null) {
            rule.setExecutionCount(0L);
        }
        
        LOGGER.debug("Policy rule entity initialization complete for: {}", rule.getName());
    }
    
    /**
     * Copy entity fields from source to target.
     */
    @Override
    public void copyEntityFieldsTo(final CPolicyRule source, final CEntityDB<?> target, final CCloneOptions options) {
        // STEP 1: ALWAYS call parent first
        super.copyEntityFieldsTo(source, target, options);
        
        // STEP 2: Type-check target
        if (!(target instanceof CPolicyRule)) {
            return;
        }
        final CPolicyRule targetRule = (CPolicyRule) target;
        
        // STEP 3: Copy rule-specific fields using DIRECT setter/getter
        targetRule.setRulePriority(source.getRulePriority());
        targetRule.setLogEnabled(source.getLogEnabled());
        
        // Copy execution order but increment to avoid conflicts
        if (source.getExecutionOrder() != null) {
            targetRule.setExecutionOrder(source.getExecutionOrder() + 1);
        }
        
        // Copy node relationships if included in options
        if (options.includesRelations()) {
            targetRule.setSourceNodeName(source.getSourceNodeName());
            targetRule.setDestinationNodeName(source.getDestinationNodeName());
            targetRule.setTriggerEntityString(source.getTriggerEntityString());
            targetRule.setActionEntityName(source.getActionEntityName());
            targetRule.setPolicyName(source.getPolicyName());
        }
        
        // Reset execution statistics for copy
        targetRule.setExecutionCount(0L);
        targetRule.setLastExecutionStatus(null);
        targetRule.setLastExecutionDate(null);
        
        LOGGER.debug("Copied policy rule '{}' with options: {}", source.getName(), options);
    }
    
    // Policy rule management operations
    
    /**
     * Find rules by policy with complete eager loading.
     */
    @Transactional(readOnly = true)
    public List<CPolicyRule> findByPolicy(final CDashboardPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRuleRepository) repository).findByPolicyWithNodes(policy.getName(), policy.getProject());
    }
    
    /**
     * Find active rules by policy.
     */
    @Transactional(readOnly = true)
    public List<CPolicyRule> findActiveByPolicy(final CDashboardPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRuleRepository) repository).findActiveByPolicy(policy.getName(), policy.getProject());
    }
    
    /**
     * Find rules using specific node.
     */
    @Transactional(readOnly = true)
    public List<CPolicyRule> findAllUsingNode(final String nodeName, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(nodeName, "Node name cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IPolicyRuleRepository) repository).findAllUsingNode(nodeName, project);
    }
    
    /**
     * Find complete rules (all four node references set).
     */
    @Transactional(readOnly = true)
    public List<CPolicyRule> findCompleteRules(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((IPolicyRuleRepository) repository).findCompleteRules(project);
    }
    
    /**
     * Find incomplete rules.
     */
    @Transactional(readOnly = true)
    public List<CPolicyRule> findIncompleteRules(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((IPolicyRuleRepository) repository).findIncompleteRules(project);
    }
    
    /**
     * Find rules by execution status.
     */
    @Transactional(readOnly = true)
    public List<CPolicyRule> findByExecutionStatus(final String status, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(status, "Status cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IPolicyRuleRepository) repository).findByExecutionStatus(status, project);
    }
    
    /**
     * Count rules by policy.
     */
    @Transactional(readOnly = true)
    public long countByPolicy(final CDashboardPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRuleRepository) repository).countByPolicy(policy.getName(), policy.getProject());
    }
    
    /**
     * Count active rules by policy.
     */
    @Transactional(readOnly = true)
    public long countActiveByPolicy(final CDashboardPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRuleRepository) repository).countActiveByPolicy(policy.getName(), policy.getProject());
    }
    
    /**
     * Count complete rules by policy.
     */
    @Transactional(readOnly = true)
    public long countCompleteByPolicy(final CDashboardPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRuleRepository) repository).countCompleteByPolicy(policy.getName(), policy.getProject());
    }
    
    /**
     * Set node reference for rule.
     */
    @Transactional
    public void setNodeReference(final CPolicyRule rule, final String nodeType, final String nodeName) {
        Check.notNull(rule, "Rule cannot be null");
        Check.notBlank(nodeType, "Node type cannot be blank");
        
        // nodeName can be null to clear the reference
        final String trimmedNodeName = (nodeName != null && !nodeName.trim().isEmpty()) ? nodeName.trim() : null;
        
        switch (nodeType.toUpperCase()) {
            case "SOURCE" -> rule.setSourceNodeName(trimmedNodeName);
            case "DESTINATION" -> rule.setDestinationNodeName(trimmedNodeName);
            case "TRIGGER" -> rule.setTriggerEntityString(trimmedNodeName);
            case "ACTION" -> rule.setActionEntityName(trimmedNodeName);
            default -> throw new CValidationException(String.format(
                "Invalid node type '%s'. Valid types are: SOURCE, DESTINATION, TRIGGER, ACTION", nodeType));
        }
        
        save(rule);
        
        LOGGER.info("Set {} node to '{}' for rule '{}'", 
                   nodeType.toLowerCase(), trimmedNodeName != null ? trimmedNodeName : "null", rule.getName());
    }
    
    /**
     * Update rule execution order.
     */
    @Transactional
    public void updateExecutionOrder(final CPolicyRule rule, final Integer executionOrder) {
        Check.notNull(rule, "Rule cannot be null");
        Check.notNull(executionOrder, "Execution order cannot be null");
        
        if (executionOrder < MIN_EXECUTION_ORDER || executionOrder > MAX_EXECUTION_ORDER) {
            throw new CValidationException(String.format(
                "Execution order must be between %d and %d", MIN_EXECUTION_ORDER, MAX_EXECUTION_ORDER));
        }
        
        rule.setExecutionOrder(executionOrder);
        save(rule);
        
        LOGGER.info("Updated execution order for rule '{}' to: {}", rule.getName(), executionOrder);
    }
    
    /**
     * Update rule execution statistics.
     */
    @Transactional
    public void updateExecutionStatistics(final CPolicyRule rule, final String status, final Long executionCount) {
        Check.notNull(rule, "Rule cannot be null");
        
        if (status != null) {
            validateExecutionStatus(status);
            rule.setLastExecutionStatus(status);
            rule.setLastExecutionDate(java.time.LocalDateTime.now());
        }
        
        if (executionCount != null) {
            rule.setExecutionCount(executionCount);
        }
        
        save(rule);
        
        LOGGER.debug("Updated execution statistics for rule '{}': status={}, count={}", 
                    rule.getName(), status, executionCount);
    }
    
    /**
     * Check if rule is complete for execution.
     */
    @Transactional(readOnly = true)
    public boolean isRuleComplete(final CPolicyRule rule) {
        Check.notNull(rule, "Rule cannot be null");
        
        return rule.getSourceNodeName() != null && !rule.getSourceNodeName().trim().isEmpty() && 
               rule.getDestinationNodeName() != null && !rule.getDestinationNodeName().trim().isEmpty() && 
               rule.getTriggerEntityString() != null && !rule.getTriggerEntityString().trim().isEmpty() && 
               rule.getActionEntityName() != null && !rule.getActionEntityName().trim().isEmpty();
    }
    
    /**
     * Get next available execution order for policy.
     */
    @Transactional(readOnly = true)
    public Integer getNextExecutionOrder(final CDashboardPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        
        final Integer nextOrder = ((IPolicyRuleRepository) repository).findNextExecutionOrderForPolicy(policy.getName(), policy.getProject());
        return nextOrder != null ? nextOrder : 0;
    }
    
    /**
     * Reorder rules in policy.
     */
    @Transactional
    public void reorderRulesInPolicy(final CDashboardPolicy policy, final List<CPolicyRule> orderedRules) {
        Check.notNull(policy, "Policy cannot be null");
        Check.notNull(orderedRules, "Ordered rules cannot be null");
        
        // Validate all rules belong to the policy
        for (final CPolicyRule rule : orderedRules) {
            final String expectedPolicyName = policy.getName();
            if (!expectedPolicyName.equals(rule.getPolicyName())) {
                throw new CValidationException(String.format(
                    "Rule '%s' does not belong to policy '%s'", rule.getName(), policy.getName()));
            }
        }
        
        // Update execution orders
        for (int i = 0; i < orderedRules.size(); i++) {
            final CPolicyRule rule = orderedRules.get(i);
            rule.setExecutionOrder(i);
            save(rule);
        }
        
        LOGGER.info("Reordered {} rules in policy '{}'", orderedRules.size(), policy.getName());
    }
    
    /**
     * Get rule statistics for dashboard.
     */
    @Transactional(readOnly = true)
    public RuleStatistics getRuleStatistics(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        
        final List<Long> stats = ((IPolicyRuleRepository) repository).getRuleStatistics(project);
        
        if (stats.size() >= 4) {
            return new RuleStatistics(
                stats.get(0) != null ? stats.get(0) : 0L,  // total
                stats.get(1) != null ? stats.get(1) : 0L,  // active
                stats.get(2) != null ? stats.get(2) : 0L,  // complete
                stats.get(3) != null ? stats.get(3) : 0L   // executed
            );
        }
        
        return new RuleStatistics(0L, 0L, 0L, 0L);
    }
    
    /**
     * Rule statistics data class.
     */
    public static record RuleStatistics(
        long totalRules,
        long activeRules,
        long completeRules,
        long executedRules
    ) {}
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getInitializerServiceClass() { 
        return Object.class; // Placeholder - will be updated in Phase 8
    }
    
    @Override
    public Class<?> getPageServiceClass() { 
        return Object.class; // Placeholder - will be updated in Phase 8
    }
    
    @Override
    public Class<?> getServiceClass() { 
        return this.getClass(); 
    }
}