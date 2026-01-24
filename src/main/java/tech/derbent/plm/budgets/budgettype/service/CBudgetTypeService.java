package tech.derbent.plm.budgets.budgettype.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.budgets.budget.service.IBudgetRepository;
import tech.derbent.plm.budgets.budgettype.domain.CBudgetType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CBudgetTypeService extends CTypeEntityService<CBudgetType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetTypeService.class);
	@Autowired
	private final IBudgetRepository budgetRepository;

	public CBudgetTypeService(final IBudgetTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IBudgetRepository budgetRepository) {
		super(repository, clock, sessionService);
		this.budgetRepository = budgetRepository;
	}

	@Override
	public String checkDeleteAllowed(final CBudgetType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = budgetRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for budget type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CBudgetType> getEntityClass() { return CBudgetType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBudgetTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBudgetType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IBudgetTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("BudgetType %02d", typeCount + 1);
		((CEntityNamed<?>) entity).setName(autoName);
	}

	@Override
	protected void validateEntity(final CBudgetType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CBudgetType> existing = ((IBudgetTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
