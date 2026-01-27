package tech.derbent.plm.projectexpenses.projectexpense.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
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
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		final Optional<CProjectExpense> existingName =
				((IProjectExpenseRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		if (entity.getAmount() != null) {
			if (entity.getAmount().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("Amount must be positive");
			}
			if (entity.getAmount().compareTo(new BigDecimal("9999999999.99")) > 0) {
				throw new IllegalArgumentException("Amount cannot exceed 9,999,999,999.99");
			}
		}
	}
}
