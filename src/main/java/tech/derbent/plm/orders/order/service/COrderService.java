package tech.derbent.plm.orders.order.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
import tech.derbent.plm.orders.order.domain.COrder;
import tech.derbent.plm.orders.type.service.COrderTypeService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(COrderService.class);
	private final CCurrencyService currencyService;
	private final CProjectItemStatusService entityStatusService;
	private final COrderTypeService entityTypeService;

	COrderService(final IOrderRepository repository, final Clock clock, final ISessionService sessionService, final CCurrencyService currencyService,
			final COrderTypeService orderTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService);
		this.currencyService = currencyService;
		entityTypeService = orderTypeService;
		entityStatusService = statusService;
	}

	@Override
	public String checkDeleteAllowed(final COrder order) {
		return super.checkDeleteAllowed(order);
	}

	public List<COrder> findByAssignedTo(final CUser responsible) {
		LOGGER.info("findByAssignedTo called with responsible: {}", responsible != null ? responsible.getName() : "null");
		Check.notNull(responsible, "Responsible user cannot be null");
		return ((COrderService) repository).findByAssignedTo(responsible);
	}

	public List<COrder> findByRequestor(final CUser requestor) {
		LOGGER.info("findByRequestor called with requestor: {}", requestor != null ? requestor.getName() : "null");
		Check.notNull(requestor, "Requestor cannot be null");
		return ((COrderService) repository).findByRequestor(requestor);
	}

	@Override
	public Class<COrder> getEntityClass() { return COrder.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return COrderInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceOrder.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final COrder entity) {
		super.initializeNewEntity(entity);
		// LOGGER.debug("Initializing new order entity");
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order"));
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize order"));
		// Initialize workflow-based status and type
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, entityTypeService, entityStatusService);
		// Initialize order-specific fields with sensible defaults
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		entity.setOrderDate(LocalDate.now(clock)); // Default: today
		entity.setRequiredDate(LocalDate.now(clock).plusDays(7)); // Default required date one week from now
		entity.setRequestor(currentUser);
		entity.setAssignedTo(currentUser);
		// Set default currency
		final List<CCurrency> availableCurrencies = currencyService.listByProject(currentProject);
		Check.notEmpty(availableCurrencies, "No currencies available for project " + currentProject.getName());
		entity.setCurrency(availableCurrencies.get(0));
		// LOGGER.debug("Order initialization complete with requestor: {}, currency: {}", currentUser.getName(),
		// availableCurrencies.get(0).getName());
	}
}
