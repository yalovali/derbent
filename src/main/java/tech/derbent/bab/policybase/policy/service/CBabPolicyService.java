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
import tech.derbent.bab.policybase.policy.domain.CBabPolicy;
import tech.derbent.base.session.service.ISessionService;

/** CPolicyService - Service for BAB policy entities. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Concrete
 * service with @Service annotation. Provides business logic for policy management: - Policy configuration validation - Rule relationship management -
 * Priority level enforcement - Calimero policy export and application - Policy versioning and status tracking - JSON generation for Calimero
 * gateway */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyService extends CEntityOfProjectService<CBabPolicy> implements IEntityRegistrable, IEntityWithView {

	/** Policy statistics data class. */
	public static record PolicyStatistics(long totalPolicies, long activePolicies, long appliedPolicies, long failedPolicies) {}

	private static final int DEFAULT_PRIORITY_LEVEL = 50;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyService.class);
	private static final int MAX_PRIORITY_LEVEL = 100;
	private static final int MIN_PRIORITY_LEVEL = 1;

	public CBabPolicyService(final IPolicyRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Activate policy and update status. */
	@Transactional
	public void activatePolicy(final CBabPolicy policy) {
		Check.notNull(policy, "Policy cannot be null");
		// Validate policy has rules before activation
		final long activeRuleCount = countActiveRules(policy);
		if (activeRuleCount == 0) {
			throw new CValidationException("Cannot activate policy '%s' - no active rules configured".formatted(policy.getName()));
		}
		policy.setIsActive(true);
		save(policy);
		LOGGER.info("Activated policy '{}' with {} active rules", policy.getName(), activeRuleCount);
	}

	/** Copy entity fields from source to target. */
	@Override
	public void copyEntityFieldsTo(final CBabPolicy source, final CEntityDB<?> target, final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		// STEP 2: Type-check target
		if (!(target instanceof final CBabPolicy targetPolicy)) {
			return;
		}
		// STEP 3: Copy policy-specific fields using DIRECT setter/getter
		targetPolicy.setPriorityLevel(source.getPriorityLevel());
		// Copy version with increment for new policy
		if (source.getPolicyVersion() != null) {
			targetPolicy.setPolicyVersion(generateNextVersion(source.getPolicyVersion()));
		}
		targetPolicy.setLastAppliedDate(null);
		// Copy rules if included in options
		if (options.includesRelations() && source.getRules() != null) {
			// Note: Rules will be copied by their own copy mechanism
			// This just ensures the collection is initialized
			LOGGER.debug("Policy rules will be copied separately via rule copy mechanism");
		}
		LOGGER.debug("Copied policy '{}' with options: {}", source.getName(), options);
	}

	/** Count active rules in policy. */
	@Transactional (readOnly = true)
	public long countActiveRules(final CBabPolicy policy) {
		Check.notNull(policy, "Policy cannot be null");
		return ((IPolicyRepository) repository).countActiveRulesByPolicy(policy.getId());
	}

	/** Deactivate policy and update status. */
	@Transactional
	public void deactivatePolicy(final CBabPolicy policy) {
		Check.notNull(policy, "Policy cannot be null");
		policy.setIsActive(false);
		save(policy);
		LOGGER.info("Deactivated policy '{}'", policy.getName());
	}

	/** Find active policies by project. */
	@Transactional (readOnly = true)
	public List<CBabPolicy> findActiveByProject(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IPolicyRepository) repository).findActiveByProject(project);
	}
	// Policy management operations

	/** Generate next version number for copied policy. */
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
				return "%d.%d".formatted(major, minor + 1);
			} catch (final NumberFormatException e) {
				return currentVersion + "_COPY";
			}
		}
		return currentVersion + "_COPY";
	}

	@Override
	public Class<CBabPolicy> getEntityClass() { return CBabPolicy.class; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicy.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (!(entity instanceof final CBabPolicy policy)) {
			return;
		}
		LOGGER.debug("Initializing new policy entity");
		// Set default priority level if not set
		if (policy.getPriorityLevel() == null) {
			policy.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
		}
		if (policy.getPolicyVersion() == null || policy.getPolicyVersion().isEmpty()) {
			policy.setPolicyVersion("1.0");
		}
		LOGGER.debug("Policy entity initialization complete for: {}", policy.getName());
	}

	@Override
	protected void validateEntity(final CBabPolicy entity) {
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
			throw new CValidationException("Priority level must be between %d and %d".formatted(MIN_PRIORITY_LEVEL, MAX_PRIORITY_LEVEL));
		}
		// Unique name validation - MANDATORY helper usage
		validateUniqueNameInProject((IPolicyRepository) repository, entity, entity.getName(), entity.getProject());
		// Policy rules validation
		validatePolicyRules(entity);
	}

	/** Validate policy rules configuration. */
	private void validatePolicyRules(final CBabPolicy entity) {
		// Check if policy has rules and validate their completeness
		if (entity.getRules() != null && !entity.getRules().isEmpty()) {
			final long activeRuleCount = entity.getRules().stream().filter(rule -> rule.getIsActive() != null && rule.getIsActive()).count();
			if (activeRuleCount == 0 && entity.getIsActive() != null && entity.getIsActive()) {
				LOGGER.warn("Policy '{}' is active but has no active rules", entity.getName());
			}
		} else {
			if (entity.getIsActive() != null && entity.getIsActive()) {
				LOGGER.warn("Policy '{}' is active but has no rules configured", entity.getName());
			}
		}
	}

	/** Validate policy version format. */
	private void validatePolicyVersionFormat(final String policyVersion) {
		// Basic version format validation (e.g., 1.0, 2.1.3, v1.0)
		if (!policyVersion.matches("^(v?\\d+(\\.\\d+)*|LATEST|DRAFT)$")) {
			throw new CValidationException("Policy version must follow format: 1.0, 2.1.3, v1.0, LATEST, or DRAFT");
		}
	}
}
