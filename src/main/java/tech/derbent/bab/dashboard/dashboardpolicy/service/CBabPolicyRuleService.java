package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.time.Clock;
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
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;

/** CPolicyRuleService - Service for BAB policy rule entities. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete service with @Service annotation. Provides business logic for policy rule management: - Rule configuration and validation - Node
 * relationship management (source, destination, trigger, action) - Execution order and priority management - Rule completeness validation -
 * Drag-and-drop operation support - Rule execution tracking - Calimero rule export support */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyRuleService extends CEntityOfProjectService<CBabPolicyRule> implements IEntityRegistrable, IEntityWithView {

	/** Rule statistics data class. */
	public static record RuleStatistics(long totalRules, long activeRules, long completeRules, long executedRules) {}

	private static final int DEFAULT_RULE_PRIORITY = 50;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRuleService.class);
	private static final int MAX_RULE_PRIORITY = 100;
	// Rule validation constants
	private static final int MIN_RULE_PRIORITY = 1;

	public CBabPolicyRuleService(final IBabPolicyRuleRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Copy entity fields from source to target. */
	@Override
	public void copyEntityFieldsTo(final CBabPolicyRule source, final CEntityDB<?> target, final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		// STEP 2: Type-check target
		if (!(target instanceof final CBabPolicyRule targetRule)) {
			return;
		}
		// STEP 3: Copy rule-specific fields using DIRECT setter/getter
		targetRule.setRulePriority(source.getRulePriority());
		targetRule.setLogEnabled(source.getLogEnabled());
		// Copy node relationships if included in options
		if (options.includesRelations()) {
			targetRule.setSourceNode(source.getSourceNode());
			targetRule.setDestinationNode(source.getDestinationNode());
			targetRule.setTriggerEntityString(source.getTriggerEntityString());
			targetRule.setActionEntityName(source.getActionEntityName());
			targetRule.setExecutionOrder(source.getExecutionOrder());
		}
		LOGGER.debug("Copied policy rule '{}' with options: {}", source.getName(), options);
	}

	@Override
	public Class<CBabPolicyRule> getEntityClass() { return CBabPolicyRule.class; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyRuleInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyRule.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (!(entity instanceof final CBabPolicyRule rule)) {
			return;
		}
		// Set default rule priority if not set
		if (rule.getRulePriority() == null) {
			rule.setRulePriority(DEFAULT_RULE_PRIORITY);
		}
		// Initialize log enabled setting
		if (rule.getLogEnabled() == null) {
			rule.setLogEnabled(false);
		}
	}

	/** Check if rule is complete for execution. */
	@Transactional (readOnly = true)
	public boolean isRuleComplete(final CBabPolicyRule rule) {
		Check.notNull(rule, "Rule cannot be null");
		return (rule.getSourceNode() != null) && (rule.getDestinationNode() != null) && (rule.getTriggerEntityString() != null)
				&& !rule.getTriggerEntityString().trim().isEmpty() && (rule.getActionEntityName() != null)
				&& !rule.getActionEntityName().trim().isEmpty();
	}

	/** Set node reference for rule.
	 * @deprecated Use direct entity setters instead: setSourceNode(), setDestinationNode() */
	@Deprecated
	@Transactional
	public void setNodeReference(final CBabPolicyRule rule, final String nodeType, final String nodeName) {
		Check.notNull(rule, "Rule cannot be null");
		Check.notBlank(nodeType, "Node type cannot be blank");
		// This method is deprecated - it was designed for String-based node references
		// For now, it just clears the node reference if nodeName is null
		// Proper usage: Use setSourceNode(entity) or setDestinationNode(entity) directly
		if (nodeName == null || nodeName.trim().isEmpty()) {
			switch (nodeType.toUpperCase()) {
			case "SOURCE" -> rule.setSourceNode(null);
			case "DESTINATION" -> rule.setDestinationNode(null);
			case "TRIGGER" -> rule.setTriggerEntityString(null);
			case "ACTION" -> rule.setActionEntityName(null);
			default ->
				throw new CValidationException("Invalid node type '%s'. Valid types are: SOURCE, DESTINATION, TRIGGER, ACTION".formatted(nodeType));
			}
		} else {
			LOGGER.warn("setNodeReference() with String nodeName is deprecated. Use setSourceNode(entity) or setDestinationNode(entity) instead.");
			// Cannot set entity from String name alone - would need node lookup
			throw new UnsupportedOperationException(
					"Setting node by name is no longer supported. Use setSourceNode(entity) or setDestinationNode(entity) instead.");
		}
		save(rule);
		LOGGER.info("Cleared {} node for rule '{}'", nodeType.toLowerCase(), rule.getName());
	}

	@Override
	protected void validateEntity(final CBabPolicyRule entity) {
		super.validateEntity(entity);
		// Required fields - explicit validation for critical rule fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		// String length validation - MANDATORY helper usage
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		if (entity.getDescription() != null) {
			validateStringLength(entity.getDescription(), "Description", CEntityConstants.MAX_LENGTH_DESCRIPTION);
		}
		// Policy relationship validation
		// Rule priority validation
		if (entity.getRulePriority() == null) {
			throw new CValidationException("Rule priority is required");
		}
		validateNumericField(entity.getRulePriority(), "Rule Priority", MAX_RULE_PRIORITY);
		if ((entity.getRulePriority() < MIN_RULE_PRIORITY) || (entity.getRulePriority() > MAX_RULE_PRIORITY)) {
			throw new CValidationException("Rule priority must be between %d and %d".formatted(MIN_RULE_PRIORITY, MAX_RULE_PRIORITY));
		}
		validateNodeReferences(entity);
		validateRuleCompleteness(entity);
		validateUniqueNameInProject((IBabPolicyRuleRepository) repository, entity, entity.getName(), entity.getProject());
	}

	/** Validate node references. */
	private void validateNodeReferences(final CBabPolicyRule entity) {
		// Node entities are validated by database constraints (nullable=true)
		// No additional validation needed for null entity references
		// Validate string fields (trigger and action)
		if ((entity.getTriggerEntityString() != null) && entity.getTriggerEntityString().trim().isEmpty()) {
			throw new CValidationException("Trigger entity name cannot be empty");
		}
		if ((entity.getActionEntityName() != null) && entity.getActionEntityName().trim().isEmpty()) {
			throw new CValidationException("Action entity name cannot be empty");
		}
		// Validate that source and destination are different
		if ((entity.getSourceNode() != null) && (entity.getDestinationNode() != null)
				&& entity.getSourceNode().getId().equals(entity.getDestinationNode().getId())) {
			throw new CValidationException("Source node and destination node cannot be the same");
		}
		// Validate that trigger and action are different (if both are set)
		if ((entity.getTriggerEntityString() != null) && (entity.getActionEntityName() != null)
				&& entity.getTriggerEntityString().trim().equals(entity.getActionEntityName().trim())) {
			LOGGER.warn("Trigger entity and action entity are the same for rule '{}'. This may create a feedback loop.", entity.getName());
		}
	}

	/** Validate rule completeness for execution. */
	private void validateRuleCompleteness(final CBabPolicyRule entity) {
		if (!((entity.getIsActive() != null) && entity.getIsActive())) {
			return;
		}
		// Active rules should have all required fields
		final boolean isComplete = (entity.getSourceNode() != null) && (entity.getDestinationNode() != null)
				&& (entity.getTriggerEntityString() != null) && !entity.getTriggerEntityString().trim().isEmpty()
				&& (entity.getActionEntityName() != null) && !entity.getActionEntityName().trim().isEmpty();
		if (!isComplete) {
			LOGGER.warn(
					"Active rule '{}' is incomplete. All four node references (source, destination, trigger, action) should be set for execution.",
					entity.getName());
		}
	}
}
