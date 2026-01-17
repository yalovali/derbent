package tech.derbent.app.customers.customer.service;

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
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.app.customers.customer.domain.CCustomer;
import tech.derbent.app.customers.customertype.service.CCustomerTypeService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:briefcase", title = "CRM.Customers")
@PermitAll
public class CCustomerService extends CProjectItemService<CCustomer> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomerService.class);
	private final CCustomerTypeService customerTypeService;

	CCustomerService(final ICustomerRepository repository, final Clock clock, final ISessionService sessionService,
			final CCustomerTypeService customerTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.customerTypeService = customerTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CCustomer entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CCustomer> getEntityClass() {
		return CCustomer.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CCustomerInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceCustomer.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CCustomer entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new customer entity");
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize customer"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, customerTypeService, projectItemStatusService);
		LOGGER.debug("Customer initialization complete");
	}
}
