package tech.derbent.app.budgets.budget.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.budgets.budget.domain.CBudget;
import tech.derbent.app.budgets.budgettype.service.CBudgetTypeService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

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
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize budget"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, budgetTypeService, projectItemStatusService);
		LOGGER.debug("Budget initialization complete");
	}
}
