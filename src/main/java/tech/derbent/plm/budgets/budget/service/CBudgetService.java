package tech.derbent.plm.budgets.budget.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.budgets.budget.domain.CBudget;
import tech.derbent.plm.budgets.budgettype.service.CBudgetTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CBudgetService extends CProjectItemService<CBudget> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetService.class);
	private final CBudgetTypeService typeService;

	CBudgetService(final IBudgetRepository repository, final Clock clock, final ISessionService sessionService,
			final CBudgetTypeService budgetTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = budgetTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CBudget entity) {
		return super.checkDeleteAllowed(entity);
	}

	/** Copy CBudget-specific fields from source to target entity. Uses direct setter/getter calls for clarity.
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CBudget source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof CBudget targetBudget)) {
			return;
		}
		// Copy basic fields using direct setter/getter
		targetBudget.setActualCost(source.getActualCost());
		targetBudget.setAlertThreshold(source.getAlertThreshold());
		targetBudget.setBudgetAmount(source.getBudgetAmount());
		targetBudget.setEarnedValue(source.getEarnedValue());
		targetBudget.setPlannedValue(source.getPlannedValue());
		// Conditional: relations
		if (options.includesRelations()) {
			targetBudget.setCurrency(source.getCurrency());
		}
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public Class<CBudget> getEntityClass() { return CBudget.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBudgetInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBudget.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CBudget entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Budget type is required");
		// 2. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IBudgetRepository) repository, entity, entity.getName().trim(), entity.getProject());
		// 3. Numeric Checks - USE STATIC HELPER
		validateNumericField(entity.getBudgetAmount(), "Budget Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getEarnedValue(), "Earned Value", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getPlannedValue(), "Planned Value", new BigDecimal("9999999999.99"));
		// Alert Threshold range check
		if (entity.getAlertThreshold() != null) {
			validateNumericRange(entity.getAlertThreshold(), "Alert Threshold", BigDecimal.ZERO, new BigDecimal("100"));
		}
	}
}
