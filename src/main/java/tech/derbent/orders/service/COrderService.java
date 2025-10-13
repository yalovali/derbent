package tech.derbent.orders.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.orders.domain.CCurrency;
import tech.derbent.orders.domain.COrder;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder> {

	private final CCurrencyService currencyService;
	private final COrderTypeService orderTypeService;
	private final COrderStatusService orderStatusService;

	COrderService(final IOrderRepository repository, final Clock clock, final ISessionService sessionService, final CCurrencyService currencyService,
			final COrderTypeService orderTypeService, final COrderStatusService orderStatusService) {
		super(repository, clock, sessionService);
		this.currencyService = currencyService;
		this.orderTypeService = orderTypeService;
		this.orderStatusService = orderStatusService;
	}

	@Override
	public String checkDeleteAllowed(final COrder order) {
		return super.checkDeleteAllowed(order);
	}

	public List<COrder> findByRequestor(final CUser requestor) {
		LOGGER.info("findByRequestor called with requestor: {}", requestor != null ? requestor.getName() : "null");
		Check.notNull(requestor, "Requestor cannot be null");
		return ((COrderService) repository).findByRequestor(requestor);
	}

	public List<COrder> findByResponsible(final CUser responsible) {
		LOGGER.info("findByResponsible called with responsible: {}", responsible != null ? responsible.getName() : "null");
		Check.notNull(responsible, "Responsible user cannot be null");
		return ((COrderService) repository).findByResponsible(responsible);
	}

	@Override
	protected Class<COrder> getEntityClass() { return COrder.class; }

	@Override
	public void initializeNewEntity(final COrder entity) {
		super.initializeNewEntity(entity);
		// Get current user and project from session
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order"));
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize order"));
		// Initialize numeric fields with 0 values
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		// Initialize date fields
		entity.setOrderDate(LocalDate.now(clock));
		entity.setRequiredDate(LocalDate.now(clock).plusDays(7)); // Default required date one week from now
		// Initialize user fields with current user
		entity.setRequestor(currentUser);
		entity.setResponsible(currentUser);
		// Initialize currency - get first available currency for the project
		final List<CCurrency> availableCurrencies = currencyService.listByProject(currentProject);
		if (availableCurrencies.isEmpty()) {
			throw new CInitializationException("No currency defined for project '" + currentProject.getName()
					+ "'. Please create at least one currency before creating an order.");
		}
		entity.setCurrency(availableCurrencies.get(0));
		// Initialize order type - get first available order type for the project
		final List<COrderType> availableOrderTypes = orderTypeService.listByProject(currentProject);
		if (availableOrderTypes.isEmpty()) {
			throw new CInitializationException("No order type defined for project '" + currentProject.getName()
					+ "'. Please create at least one order type before creating an order.");
		}
		entity.setOrderType(availableOrderTypes.get(0));
		// Initialize status - get first available order status for the project
		final List<COrderStatus> availableStatuses = orderStatusService.listByProject(currentProject);
		if (availableStatuses.isEmpty()) {
			throw new CInitializationException("No order status defined for project '" + currentProject.getName()
					+ "'. Please create at least one order status before creating an order.");
		}
		entity.setStatus(availableStatuses.get(0));
	}
}
