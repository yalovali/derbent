package tech.derbent.plm.customers.customer.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.customers.customer.domain.CCustomer;
import tech.derbent.plm.customers.customertype.service.CCustomerTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CCustomerService extends CProjectItemService<CCustomer> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomerService.class);
	private final CCustomerTypeService typeService;

	CCustomerService(final ICustomerRepository repository, final Clock clock, final ISessionService sessionService,
			final CCustomerTypeService customerTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = customerTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CCustomer entity) {
		return super.checkDeleteAllowed(entity);
	}

	/**
	 * Copy CCustomer-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CCustomer source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);

		if (!(target instanceof CCustomer targetCustomer)) {
			return;
		}

		// Copy basic fields using direct setter/getter
		targetCustomer.setAnnualRevenue(source.getAnnualRevenue());
		targetCustomer.setBillingAddress(source.getBillingAddress());
		targetCustomer.setCompanyName(source.getCompanyName());
		targetCustomer.setCompanySize(source.getCompanySize());
		targetCustomer.setCustomerNotes(source.getCustomerNotes());
		targetCustomer.setIndustry(source.getIndustry());
		targetCustomer.setLifetimeValue(source.getLifetimeValue());
		targetCustomer.setPrimaryContactEmail(source.getPrimaryContactEmail());
		targetCustomer.setPrimaryContactName(source.getPrimaryContactName());
		targetCustomer.setPrimaryContactPhone(source.getPrimaryContactPhone());
		targetCustomer.setShippingAddress(source.getShippingAddress());
		targetCustomer.setWebsite(source.getWebsite());

		// Conditional: dates
		if (!options.isResetDates()) {
			targetCustomer.setLastInteractionDate(source.getLastInteractionDate());
			targetCustomer.setRelationshipStartDate(source.getRelationshipStartDate());
		}

		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public Class<CCustomer> getEntityClass() { return CCustomer.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CCustomerInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCustomer.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CCustomer entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Customer type is required");
		Check.notBlank(entity.getCompanyName(), "Company Name is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getCompanyName(), "Company Name", 200);
		validateStringLength(entity.getIndustry(), "Industry", 100);
		validateStringLength(entity.getCompanySize(), "Company Size", 50);
		validateStringLength(entity.getWebsite(), "Website", 200);
		validateStringLength(entity.getPrimaryContactName(), "Primary Contact Name", 100);
		validateStringLength(entity.getPrimaryContactEmail(), "Primary Contact Email", 150);
		validateStringLength(entity.getPrimaryContactPhone(), "Primary Contact Phone", 50);
		validateStringLength(entity.getBillingAddress(), "Billing Address", 500);
		validateStringLength(entity.getShippingAddress(), "Shipping Address", 500);
		validateStringLength(entity.getCustomerNotes(), "Customer Notes", 2000);
		
		// 3. Unique Checks
		validateUniqueNameInProject((ICustomerRepository) repository, entity, entity.getName(), entity.getProject());
		// 4. Numeric Checks
		validateNumericField(entity.getAnnualRevenue(), "Annual Revenue", new BigDecimal("99999999999.99"));
		validateNumericField(entity.getLifetimeValue(), "Lifetime Value", new BigDecimal("99999999999.99"));
	}
}
