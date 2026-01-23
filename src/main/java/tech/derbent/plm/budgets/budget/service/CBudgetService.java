package tech.derbent.plm.budgets.budget.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.budgets.budget.domain.CBudget;
import tech.derbent.plm.budgets.budgettype.service.CBudgetTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Budgets")
@PermitAll
public class CBudgetService extends CProjectItemService<CBudget> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetService.class);
	private final CBudgetTypeService budgetTypeService;

	CBudgetService(final IBudgetRepository repository, final Clock clock, final ISessionService sessionService,
			final CBudgetTypeService budgetTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.budgetTypeService = budgetTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CBudget entity) {
		return super.checkDeleteAllowed(entity);
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
	public void initializeNewEntity(final CBudget entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new budget entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize budget"));
		entity.initializeDefaults_IHasStatusAndWorkflow(currentProject, budgetTypeService, projectItemStatusService);
		// Numeric fields initialized in Entity.initializeDefaults()
		LOGGER.debug("Budget initialization complete");
	}

	@Override
	protected void validateEntity(final CBudget entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Budget type is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		final Optional<CBudget> existingName = ((IBudgetRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		validateNumericField(entity.getBudgetAmount(), "Budget Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getEarnedValue(), "Earned Value", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getPlannedValue(), "Planned Value", new BigDecimal("9999999999.99"));
		if (entity.getAlertThreshold() != null
				&& (entity.getAlertThreshold().compareTo(BigDecimal.ZERO) < 0 || entity.getAlertThreshold().compareTo(new BigDecimal("100")) > 0)) {
			throw new IllegalArgumentException("Alert Threshold must be between 0 and 100");
		}
	}

	private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
		if (value != null) {
			if (value.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(fieldName + " must be positive");
			}
			if (value.compareTo(max) > 0) {
				throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
			}
		}
	}
}
