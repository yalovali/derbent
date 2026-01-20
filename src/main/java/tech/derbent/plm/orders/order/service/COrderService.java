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
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
import tech.derbent.plm.orders.order.domain.COrder;
import tech.derbent.plm.orders.type.service.COrderTypeService;

import java.util.Optional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;

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

	@Override
	protected void validateEntity(final COrder entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getCurrency(), "Currency is required");
		Check.notNull(entity.getEntityType(), "Order type is required");
		Check.notNull(entity.getOrderDate(), "Order Date is required");
		Check.notNull(entity.getRequestor(), "Requestor is required");
		Check.notBlank(entity.getProviderCompanyName(), "Provider Company Name is required");
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getProviderCompanyName().length() > 200) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Provider Company Name cannot exceed %d characters", 200));
		}
		if (entity.getProviderContactName() != null && entity.getProviderContactName().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Provider Contact Name cannot exceed %d characters", 100));
		}
		if (entity.getProviderEmail() != null && entity.getProviderEmail().length() > 150) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Provider Email cannot exceed %d characters", 150));
		}
		if (entity.getOrderNumber() != null && entity.getOrderNumber().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Order Number cannot exceed %d characters", 50));
		}
		if (entity.getDeliveryAddress() != null && entity.getDeliveryAddress().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Delivery Address cannot exceed %d characters", 500));
		}
		
		// 3. Unique Checks
		final Optional<COrder> existingName = ((IOrderRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		
		// 4. Numeric Checks
		validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("99999999999.99"));
		validateNumericField(entity.getEstimatedCost(), "Estimated Cost", new BigDecimal("99999999999.99"));
		
		// 5. Date Logic
		if (entity.getOrderDate() != null && entity.getRequiredDate() != null && entity.getRequiredDate().isBefore(entity.getOrderDate())) {
			throw new IllegalArgumentException("Required Date cannot be before Order Date");
		}
		if (entity.getOrderDate() != null && entity.getDeliveryDate() != null && entity.getDeliveryDate().isBefore(entity.getOrderDate())) {
			throw new IllegalArgumentException("Delivery Date cannot be before Order Date");
		}
	}
	
	private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
		if (value != null) {
			if (value.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(fieldName + " must be positive");
			}
			if (value.compareTo(max) > 0) {
				throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
			}
		}
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

	@SuppressWarnings ("null")
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
