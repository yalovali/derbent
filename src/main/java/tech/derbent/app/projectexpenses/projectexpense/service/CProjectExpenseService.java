package tech.derbent.app.projectexpenses.projectexpense.service;

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
import tech.derbent.app.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.app.projectexpenses.projectexpensetype.service.CProjectExpenseTypeService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.ProjectExpenses")
@PermitAll
public class CProjectExpenseService extends CProjectItemService<CProjectExpense> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectExpenseService.class);
	private final CProjectExpenseTypeService projectexpenseTypeService;

	CProjectExpenseService(final IProjectExpenseRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectExpenseTypeService projectexpenseTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.projectexpenseTypeService = projectexpenseTypeService;
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
	public void initializeNewEntity(final CProjectExpense entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new projectexpense entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize projectexpense"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, projectexpenseTypeService, projectItemStatusService);
		LOGGER.debug("ProjectExpense initialization complete");
	}
}
