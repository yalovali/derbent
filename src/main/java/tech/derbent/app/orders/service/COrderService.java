package tech.derbent.app.orders.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.app.orders.domain.CCurrency;
import tech.derbent.app.orders.domain.COrder;
import tech.derbent.app.orders.domain.COrderStatus;
import tech.derbent.app.orders.domain.COrderType;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder> {

	private final CCurrencyService currencyService;
	private final COrderStatusService orderStatusService;
	private final COrderTypeService orderTypeService;

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
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order"));
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize order"));
		entity.setActualCost(BigDecimal.ZERO);
		entity.setEstimatedCost(BigDecimal.ZERO);
		entity.setOrderDate(LocalDate.now(clock));
		entity.setRequiredDate(LocalDate.now(clock).plusDays(7)); // Default required date one week from now
		entity.setRequestor(currentUser);
		entity.setResponsible(currentUser);
		final List<CCurrency> availableCurrencies = currencyService.listByProject(currentProject);
		Check.notEmpty(availableCurrencies, "No currencies available for project " + currentProject.getName());
		entity.setCurrency(availableCurrencies.get(0));
		final List<COrderType> availableOrderTypes = orderTypeService.listByProject(currentProject);
		Check.notEmpty(availableOrderTypes, "No order types available for project " + currentProject.getName());
		entity.setOrderType(availableOrderTypes.get(0));
		final List<COrderStatus> availableStatuses = orderStatusService.listByProject(currentProject);
		Check.notEmpty(availableStatuses, "No order statuses available for project " + currentProject.getName());
		entity.setStatus(availableStatuses.get(0));
	}
}
