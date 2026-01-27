package tech.derbent.plm.projectexpenses.projectexpense.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.plm.projectexpenses.projectexpensetype.service.CProjectExpenseTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CProjectExpenseService extends CProjectItemService<CProjectExpense> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectExpenseService.class);
	private final CProjectExpenseTypeService typeService;

	CProjectExpenseService(final IProjectExpenseRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectExpenseTypeService projectexpenseTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = projectexpenseTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectExpense entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProjectExpense> getEntityClass() { return CProjectExpense.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectExpenseInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectExpense.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CProjectExpense entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Expense type is required");
		
		// 2. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IProjectExpenseRepository) repository, entity, entity.getName().trim(), entity.getProject());
		
		// 3. Numeric Check - USE STATIC HELPER
		if (entity.getAmount() != null) {
			validateNumericField(entity.getAmount(), "Amount", new BigDecimal("9999999999.99"));
		}
	}
}
