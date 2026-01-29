package tech.derbent.plm.orders.order.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
import tech.derbent.plm.orders.order.domain.COrder;
import tech.derbent.plm.orders.type.service.COrderTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderService extends CEntityOfProjectService<COrder>
		implements IEntityRegistrable, IEntityWithView, IHasStatusAndWorkflowService<COrder> {

	private static final Logger LOGGER = LoggerFactory.getLogger(COrderService.class);
	private final CCurrencyService currencyService;
	private final CProjectItemStatusService entityStatusService;
	private final COrderTypeService typeService;

	COrderService(final IOrderRepository repository, final Clock clock, final ISessionService sessionService, final CCurrencyService currencyService,
			final COrderTypeService orderTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService);
		this.currencyService = currencyService;
		typeService = orderTypeService;
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

	/**
	 * Copy COrder-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final COrder source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof COrder)) {
			return;
		}
		final COrder targetOrder = (COrder) target;
		
		// Copy basic fields
		targetOrder.setProviderCompanyName(source.getProviderCompanyName());
		targetOrder.setProviderContactName(source.getProviderContactName());
		targetOrder.setProviderEmail(source.getProviderEmail());
		targetOrder.setOrderNumber(source.getOrderNumber());
		targetOrder.setDeliveryAddress(source.getDeliveryAddress());
		targetOrder.setEstimatedCost(source.getEstimatedCost());
		targetOrder.setActualCost(source.getActualCost());
		
		// Copy dates conditionally
		if (!options.isResetDates()) {
			targetOrder.setOrderDate(source.getOrderDate());
			targetOrder.setRequiredDate(source.getRequiredDate());
			targetOrder.setDeliveryDate(source.getDeliveryDate());
		}
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetOrder.setCurrency(source.getCurrency());
			targetOrder.setRequestor(source.getRequestor());
			
			// Copy collections
			if (source.getApprovals() != null) {
				targetOrder.setApprovals(new java.util.ArrayList<>(source.getApprovals()));
			}
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final COrder entityCasted = (COrder) entity;
		// LOGGER.debug("Initializing new order entity");
		final CUser currentUser =
				sessionService.getActiveUser().orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order"));
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize order"));
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				entityStatusService);
		// Initialize order-specific fields with sensible defaults (Context-aware)
		entityCasted.setRequestor(currentUser);
		entityCasted.setAssignedTo(currentUser);
		// Set default currency (Context-aware DB lookup)
		final List<CCurrency> availableCurrencies = currencyService.listByProject(currentProject);
		if (!availableCurrencies.isEmpty()) {
			entityCasted.setCurrency(availableCurrencies.get(0));
		}
		// Note: Dates (Order Date, Required Date) are initialized in COrder.initializeDefaults()
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
		// 3. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IOrderRepository) repository, entity, entity.getName(), entity.getProject());
		
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
}
