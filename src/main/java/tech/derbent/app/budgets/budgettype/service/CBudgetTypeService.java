package tech.derbent.app.budgets.budgettype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.budgets.budget.service.IBudgetRepository;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CBudgetTypeService extends CTypeEntityService<CBudgetType> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBudgetTypeService.class);
	@Autowired
	private IBudgetRepository budgetRepository;

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
	public void initializeNewEntity(final CBudgetType entity) {
		super.initializeNewEntity(entity);
		CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		long typeCount = ((IBudgetTypeRepository) repository).countByProject(activeProject);
		String autoName = String.format("BudgetType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
