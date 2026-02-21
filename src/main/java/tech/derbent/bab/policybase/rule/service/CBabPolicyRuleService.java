package tech.derbent.bab.policybase.rule.service;

import java.time.Clock;
import java.util.HashSet;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.rule.domain.CBabPolicyRule;
import tech.derbent.bab.policybase.rule.view.CComponentPolicyRuleActions;

/** Service for BAB policy rules (single source/filter + many destination-aware actions). */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyRuleService extends CEntityOfProjectService<CBabPolicyRule> implements IEntityRegistrable, IEntityWithView {

	public static record RuleStatistics(long totalRules, long activeRules, long completeRules, long executedRules) {}

	private static final int DEFAULT_RULE_PRIORITY = 50;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyRuleService.class);
	private static final int MAX_RULE_PRIORITY = 100;
	private static final int MIN_RULE_PRIORITY = 1;

	public CBabPolicyRuleService(final IBabPolicyRuleRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public void copyEntityFieldsTo(final CBabPolicyRule source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof final CBabPolicyRule targetRule)) {
			return;
		}
		targetRule.setRulePriority(source.getRulePriority());
		targetRule.setLogEnabled(source.getLogEnabled());
		if (options.includesRelations()) {
			targetRule.setSourceNode(source.getSourceNode());
			targetRule.setTrigger(source.getTrigger());
			targetRule.setActions(new HashSet<>(source.getActions()));
			targetRule.setFilter(source.getFilter());
			targetRule.setExecutionOrder(source.getExecutionOrder());
		}
		LOGGER.debug("Copied policy rule '{}' with options: {}", source.getName(), options);
	}

	public Component createPolicyRuleActionsComponent() {
		try {
			return new CComponentPolicyRuleActions(this, getService(CBabPolicyActionService.class), getService(CPageEntityService.class),
					sessionService);
		} catch (final Exception e) {
			LOGGER.error("Failed to create policy rule actions component", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading actions component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	public Class<CBabPolicyRule> getEntityClass() { return CBabPolicyRule.class; }

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
		if (rule.getRulePriority() == null) {
			rule.setRulePriority(DEFAULT_RULE_PRIORITY);
		}
		if (rule.getLogEnabled() == null) {
			rule.setLogEnabled(false);
		}
	}

	@Transactional (readOnly = true)
	public boolean isRuleComplete(final CBabPolicyRule rule) {
		Check.notNull(rule, "Rule cannot be null");
		return rule.getSourceNode() != null && rule.getTrigger() != null && !rule.getActions().isEmpty();
	}

	@Override
	protected void validateEntity(final CBabPolicyRule entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		if (entity.getDescription() != null) {
			validateStringLength(entity.getDescription(), "Description", CEntityConstants.MAX_LENGTH_DESCRIPTION);
		}
		if (entity.getRulePriority() == null) {
			throw new CValidationException("Rule priority is required");
		}
		validateNumericField(entity.getRulePriority(), "Rule Priority", MAX_RULE_PRIORITY);
		if (entity.getRulePriority() < MIN_RULE_PRIORITY || entity.getRulePriority() > MAX_RULE_PRIORITY) {
			throw new CValidationException("Rule priority must be between %d and %d".formatted(MIN_RULE_PRIORITY, MAX_RULE_PRIORITY));
		}
		validatePolicyComponentReferences(entity);
		validateRuleCompleteness(entity);
		validateUniqueNameInProject((IBabPolicyRuleRepository) repository, entity, entity.getName(), entity.getProject());
	}

	private <T> T getService(final Class<T> serviceClass) {
		return CSpringContext.getBean(serviceClass);
	}

	private void validatePolicyComponentReferences(final CBabPolicyRule entity) {
		final Long projectId = entity.getProject().getId();
		if (entity.getTrigger() != null && !Objects.equals(entity.getTrigger().getProject().getId(), projectId)) {
			throw new CValidationException("Trigger must belong to the same project as the policy rule");
		}
		for (final CBabPolicyAction action : entity.getActions()) {
			if (!Objects.equals(action.getProject().getId(), projectId)) {
				throw new CValidationException("All actions must belong to the same project as the policy rule");
			}
			if (action.getDestinationNode() == null) {
				throw new CValidationException("Each action must have a destination node");
			}
			if (action.getActionMask() == null) {
				throw new CValidationException("Each action must define an action mask");
			}
		}
		if (entity.getFilter() != null) {
			if (entity.getFilter().getParentNode() == null) {
				throw new CValidationException("Filter must be attached to a parent node");
			}
			if (entity.getFilter().getParentNode().getProject() == null) {
				throw new CValidationException("Filter parent node must belong to a project");
			}
			if (!Objects.equals(entity.getFilter().getParentNode().getProject().getId(), projectId)) {
				throw new CValidationException("Filter must belong to the same project as the policy rule");
			}
			if (entity.getSourceNode() == null) {
				throw new CValidationException("Source node must be selected when a filter is set");
			}
			final Long sourceNodeId = entity.getSourceNode().getId();
			final Long filterNodeId = entity.getFilter().getParentNode().getId();
			final boolean sameNode = sourceNodeId != null && filterNodeId != null ? Objects.equals(sourceNodeId, filterNodeId)
					: entity.getSourceNode() == entity.getFilter().getParentNode();
			if (!sameNode) {
				throw new CValidationException("Filter must belong to the selected source node");
			}
		}
	}

	private void validateRuleCompleteness(final CBabPolicyRule entity) {
		if (!(entity.getActive() != null && entity.getActive())) {
			return;
		}
		final boolean isComplete = entity.getSourceNode() != null && entity.getTrigger() != null && !entity.getActions().isEmpty();
		if (!isComplete) {
			LOGGER.warn("Active rule '{}' is incomplete. Source, trigger, and at least one action should be set for execution.", entity.getName());
		}
	}
}
