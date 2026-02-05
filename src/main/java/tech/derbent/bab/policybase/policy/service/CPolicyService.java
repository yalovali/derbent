package tech.derbent.bab.policybase.policy.service;

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
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CPolicy;
import tech.derbent.bab.dashboard.dashboardpolicy.service.IPolicyRepository;
import tech.derbent.base.session.service.ISessionService;

/**
 * CPolicyService - Service for BAB policy entities.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete service with @Service annotation.
 * 
 * Provides business logic for policy management:
 * - Policy configuration validation
 * - Rule relationship management
 * - Priority level enforcement
 * - Calimero policy export and application
 * - Policy versioning and status tracking
 * - JSON generation for Calimero gateway
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CPolicyService extends CEntityOfProjectService<CPolicy> 
    implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyService.class);
    
    // Policy validation constants
    private static final int MIN_PRIORITY_LEVEL = 1;
    private static final int MAX_PRIORITY_LEVEL = 100;
    private static final int DEFAULT_PRIORITY_LEVEL = 50;
    
    // Policy status values
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_APPLIED = "APPLIED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PENDING = "PENDING";
    
    public CPolicyService(final IPolicyRepository repository,
                         final Clock clock,
                         final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public Class<CPolicy> getEntityClass() {
        return CPolicy.class;
    }
    
    @Override
    protected void validateEntity(final CPolicy entity) {
        super.validateEntity(entity);
        
        // Required fields - explicit validation for critical policy fields
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
        
        // String length validation - MANDATORY helper usage
        validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
        
        if (entity.getDescription() != null) {
            validateStringLength(entity.getDescription(), "Description", CEntityConstants.MAX_LENGTH_DESCRIPTION);
        }
        
        if (entity.getPolicyVersion() != null) {
            validateStringLength(entity.getPolicyVersion(), "Policy Version", 20);
            validatePolicyVersionFormat(entity.getPolicyVersion());
        }
        
        // Priority level validation
        if (entity.getPriorityLevel() == null) {
            throw new CValidationException("Priority level is required");
        }
        
        validateNumericField(entity.getPriorityLevel(), "Priority Level", MAX_PRIORITY_LEVEL);
        
        if (entity.getPriorityLevel() < MIN_PRIORITY_LEVEL || entity.getPriorityLevel() > MAX_PRIORITY_LEVEL) {
            throw new CValidationException(String.format(
                "Priority level must be between %d and %d", MIN_PRIORITY_LEVEL, MAX_PRIORITY_LEVEL));
        }
        
        // Application status validation
        if (entity.getApplicationStatus() != null) {
            validateStringLength(entity.getApplicationStatus(), "Application Status", 20);
            validateApplicationStatus(entity.getApplicationStatus());
        }
        
        // Auto-apply validation
        if (entity.getAutoApply() != null && entity.getAutoApply() && 
            (entity.getIsActive() == null || !entity.getIsActive())) {
            LOGGER.warn("Policy '{}' has auto-apply enabled but is not active. Auto-apply requires active status.", 
                       entity.getName());
        }
        
        // Unique name validation - MANDATORY helper usage
        validateUniqueNameInProject(
            (IPolicyRepository) repository,
            entity, entity.getName(), entity.getProject());
        
        // Policy rules validation
        validatePolicyRules(entity);
    }
    
    /**
     * Validate policy version format.
     */
    private void validatePolicyVersionFormat(final String policyVersion) {
        // Basic version format validation (e.g., 1.0, 2.1.3, v1.0)
        if (!policyVersion.matches("^(v?\\d+(\\.\\d+)*|LATEST|DRAFT)$")) {
            throw new CValidationException(
                "Policy version must follow format: 1.0, 2.1.3, v1.0, LATEST, or DRAFT");
        }
    }
    
    /**
     * Validate application status.
     */
    private void validateApplicationStatus(final String applicationStatus) {
        final String upperStatus = applicationStatus.toUpperCase();
        final String[] validStatuses = {STATUS_DRAFT, STATUS_ACTIVE, STATUS_APPLIED, STATUS_FAILED, STATUS_PENDING};
        
        for (final String validStatus : validStatuses) {
            if (validStatus.equals(upperStatus)) {
                return;
            }
        }
        
        throw new CValidationException(String.format(
            "Invalid application status '%s'. Valid statuses are: %s", 
            applicationStatus, String.join(", ", validStatuses)));
    }
    
    /**
     * Validate policy rules configuration.
     */
    private void validatePolicyRules(final CPolicy entity) {
        // Check if policy has rules and validate their completeness
        if (entity.getRules() != null && !entity.getRules().isEmpty()) {
            final long activeRuleCount = entity.getRules().stream()
                .filter(rule -> rule.getIsActive() != null && rule.getIsActive())
                .count();
            
            if (activeRuleCount == 0 && entity.getIsActive() != null && entity.getIsActive()) {
                LOGGER.warn("Policy '{}' is active but has no active rules", entity.getName());
            }
        } else {
            if (entity.getIsActive() != null && entity.getIsActive()) {
                LOGGER.warn("Policy '{}' is active but has no rules configured", entity.getName());
            }
        }
    }
    
    @Override
    public void initializeNewEntity(final Object entity) {
        super.initializeNewEntity(entity);
        
        if (!(entity instanceof CPolicy)) {
            return;
        }
        
        final CPolicy policy = (CPolicy) entity;
        
        LOGGER.debug("Initializing new policy entity");
        
        // Set default priority level if not set
        if (policy.getPriorityLevel() == null) {
            policy.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
        }
        
        // Set default application status
        if (policy.getApplicationStatus() == null || policy.getApplicationStatus().isEmpty()) {
            policy.setApplicationStatus(STATUS_DRAFT);
        }
        
        // Set default version
        if (policy.getPolicyVersion() == null || policy.getPolicyVersion().isEmpty()) {
            policy.setPolicyVersion("1.0");
        }
        
        // Initialize auto-apply setting
        if (policy.getAutoApply() == null) {
            policy.setAutoApply(false);
        }
        
        LOGGER.debug("Policy entity initialization complete for: {}", policy.getName());
    }
    
    /**
     * Copy entity fields from source to target.
     */
    @Override
    public void copyEntityFieldsTo(final CPolicy source, final CEntityDB<?> target, final CCloneOptions options) {
        // STEP 1: ALWAYS call parent first
        super.copyEntityFieldsTo(source, target, options);
        
        // STEP 2: Type-check target
        if (!(target instanceof CPolicy)) {
            return;
        }
        final CPolicy targetPolicy = (CPolicy) target;
        
        // STEP 3: Copy policy-specific fields using DIRECT setter/getter
        targetPolicy.setPriorityLevel(source.getPriorityLevel());
        targetPolicy.setAutoApply(source.getAutoApply());
        
        // Copy version with increment for new policy
        if (source.getPolicyVersion() != null) {
            targetPolicy.setPolicyVersion(generateNextVersion(source.getPolicyVersion()));
        }
        
        // Reset application status for copy
        targetPolicy.setApplicationStatus(STATUS_DRAFT);
        targetPolicy.setLastAppliedDate(null);
        
        // Copy rules if included in options
        if (options.includesRelations() && source.getRules() != null) {
            // Note: Rules will be copied by their own copy mechanism
            // This just ensures the collection is initialized
            LOGGER.debug("Policy rules will be copied separately via rule copy mechanism");
        }
        
        LOGGER.debug("Copied policy '{}' with options: {}", source.getName(), options);
    }
    
    /**
     * Generate next version number for copied policy.
     */
    private String generateNextVersion(final String currentVersion) {
        if (currentVersion == null || "DRAFT".equals(currentVersion)) {
            return "1.0";
        }
        
        if ("LATEST".equals(currentVersion)) {
            return "LATEST_COPY";
        }
        
        // Try to increment version number
        if (currentVersion.matches("^\\d+\\.\\d+$")) {
            final String[] parts = currentVersion.split("\\.");
            try {
                final int major = Integer.parseInt(parts[0]);
                final int minor = Integer.parseInt(parts[1]);
                return String.format("%d.%d", major, minor + 1);
            } catch (final NumberFormatException e) {
                return currentVersion + "_COPY";
            }
        }
        
        return currentVersion + "_COPY";
    }
    
    // Policy management operations
    
    /**
     * Find active policies by project.
     */
    @Transactional(readOnly = true)
    public List<CPolicy> findActiveByProject(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((IPolicyRepository) repository).findActiveByProject(project);
    }
    
    /**
     * Find policies by application status.
     */
    @Transactional(readOnly = true)
    public List<CPolicy> findByApplicationStatus(final String status, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(status, "Status cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IPolicyRepository) repository).findByApplicationStatusAndProject(status, project);
    }
    
    /**
     * Find policies ready for application.
     */
    @Transactional(readOnly = true)
    public List<CPolicy> findReadyForApplication(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((IPolicyRepository) repository).findReadyForApplication(project);
    }
    
    /**
     * Find policies with auto-apply enabled.
     */
    @Transactional(readOnly = true)
    public List<CPolicy> findAutoApplyPolicies(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((IPolicyRepository) repository).findAutoApplyPolicies(project);
    }
    
    /**
     * Find high priority policies.
     */
    @Transactional(readOnly = true)
    public List<CPolicy> findHighPriorityPolicies(final Integer minPriority, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(minPriority, "Min priority cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return ((IPolicyRepository) repository).findHighPriorityPolicies(minPriority, project);
    }
    
    /**
     * Count active rules in policy.
     */
    @Transactional(readOnly = true)
    public long countActiveRules(final CPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRepository) repository).countActiveRulesByPolicy(policy.getId());
    }
    
    /**
     * Count total rules in policy.
     */
    @Transactional(readOnly = true)
    public long countTotalRules(final CPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        return ((IPolicyRepository) repository).countTotalRulesByPolicy(policy.getId());
    }
    
    /**
     * Find policies using specific node.
     */
    @Transactional(readOnly = true)
    public List<CPolicy> findPoliciesUsingNode(final Long nodeId, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(nodeId, "Node ID cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return ((IPolicyRepository) repository).findPoliciesUsingNode(nodeId, project);
    }
    
    /**
     * Activate policy and update status.
     */
    @Transactional
    public void activatePolicy(final CPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        
        // Validate policy has rules before activation
        final long activeRuleCount = countActiveRules(policy);
        if (activeRuleCount == 0) {
            throw new CValidationException(String.format(
                "Cannot activate policy '%s' - no active rules configured", policy.getName()));
        }
        
        policy.setIsActive(true);
        policy.setApplicationStatus(STATUS_ACTIVE);
        save(policy);
        
        LOGGER.info("Activated policy '{}' with {} active rules", policy.getName(), activeRuleCount);
    }
    
    /**
     * Deactivate policy and update status.
     */
    @Transactional
    public void deactivatePolicy(final CPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        
        policy.setIsActive(false);
        policy.setApplicationStatus(STATUS_DRAFT);
        save(policy);
        
        LOGGER.info("Deactivated policy '{}'", policy.getName());
    }
    
    /**
     * Update policy application status.
     */
    @Transactional
    public void updateApplicationStatus(final CPolicy policy, final String status) {
        Check.notNull(policy, "Policy cannot be null");
        Check.notBlank(status, "Status cannot be blank");
        
        // Validate status
        validateApplicationStatus(status);
        
        policy.setApplicationStatus(status);
        
        // Update timestamps based on status
        if (STATUS_APPLIED.equals(status.toUpperCase())) {
            policy.setLastAppliedDate(java.time.LocalDateTime.now());
        }
        
        save(policy);
        
        LOGGER.info("Updated policy '{}' application status to: {}", policy.getName(), status);
    }
    
    /**
     * Generate policy JSON for Calimero export.
     */
    @Transactional(readOnly = true)
    public String generatePolicyJson(final CPolicy policy) {
        Check.notNull(policy, "Policy cannot be null");
        
        final StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append(String.format("  \"policyId\": \"%s\",\n", policy.getId()));
        json.append(String.format("  \"policyName\": \"%s\",\n", policy.getName()));
        json.append(String.format("  \"version\": \"%s\",\n", policy.getPolicyVersion()));
        json.append(String.format("  \"priority\": %d,\n", policy.getPriorityLevel()));
        json.append(String.format("  \"active\": %s,\n", policy.getIsActive()));
        json.append(String.format("  \"autoApply\": %s,\n", policy.getAutoApply()));
        json.append(String.format("  \"applicationStatus\": \"%s\",\n", policy.getApplicationStatus()));
        
        // Add rules section
        json.append("  \"rules\": [\n");
        
        if (policy.getRules() != null && !policy.getRules().isEmpty()) {
            final var activeRules = policy.getRules().stream()
                .filter(rule -> rule.getIsActive() != null && rule.getIsActive())
                .sorted((r1, r2) -> {
                    // Sort by execution order, then by priority
                    int orderCompare = Integer.compare(
                        r1.getExecutionOrder() != null ? r1.getExecutionOrder() : Integer.MAX_VALUE,
                        r2.getExecutionOrder() != null ? r2.getExecutionOrder() : Integer.MAX_VALUE);
                    
                    if (orderCompare != 0) return orderCompare;
                    
                    return Integer.compare(
                        r2.getRulePriority() != null ? r2.getRulePriority() : 0,
                        r1.getRulePriority() != null ? r1.getRulePriority() : 0);
                })
                .toList();
            
            for (int i = 0; i < activeRules.size(); i++) {
                final var rule = activeRules.get(i);
                json.append("    {\n");
                json.append(String.format("      \"ruleId\": \"%s\",\n", rule.getId()));
                json.append(String.format("      \"ruleName\": \"%s\",\n", rule.getName()));
                json.append(String.format("      \"executionOrder\": %d,\n", rule.getExecutionOrder()));
                json.append(String.format("      \"priority\": %d,\n", rule.getRulePriority()));
                json.append(String.format("      \"sourceNode\": \"%s\",\n", 
                           rule.getSourceNodeName() != null ? rule.getSourceNodeName() : ""));
                json.append(String.format("      \"destinationNode\": \"%s\",\n", 
                           rule.getDestinationNodeName() != null ? rule.getDestinationNodeName() : ""));
                json.append(String.format("      \"triggerEntity\": \"%s\",\n", 
                           rule.getTriggerEntityString() != null ? rule.getTriggerEntityString() : ""));
                json.append(String.format("      \"actionEntity\": \"%s\",\n", 
                           rule.getActionEntityName() != null ? rule.getActionEntityName() : ""));
                json.append(String.format("      \"logEnabled\": %s\n", rule.getLogEnabled()));
                json.append(i < activeRules.size() - 1 ? "    },\n" : "    }\n");
            }
        }
        
        json.append("  ],\n");
        json.append("  \"generatedAt\": \"").append(java.time.LocalDateTime.now()).append("\"\n");
        json.append("}");
        
        final String result = json.toString();
        LOGGER.debug("Generated policy JSON for '{}': {} characters", policy.getName(), result.length());
        
        return result;
    }
    
    /**
     * Get policy statistics for dashboard.
     */
    @Transactional(readOnly = true)
    public PolicyStatistics getPolicyStatistics(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        
        final List<Long> stats = ((IPolicyRepository) repository).getPolicyStatistics(project);
        
        if (stats.size() >= 4) {
            return new PolicyStatistics(
                stats.get(0) != null ? stats.get(0) : 0L,  // total
                stats.get(1) != null ? stats.get(1) : 0L,  // active
                stats.get(2) != null ? stats.get(2) : 0L,  // applied
                stats.get(3) != null ? stats.get(3) : 0L   // failed
            );
        }
        
        return new PolicyStatistics(0L, 0L, 0L, 0L);
    }
    
    /**
     * Policy statistics data class.
     */
    public static record PolicyStatistics(
        long totalPolicies,
        long activePolicies,
        long appliedPolicies,
        long failedPolicies
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