package tech.derbent.plm.customers.customer.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.customers.customer.domain.CCustomer;
import tech.derbent.plm.customers.customertype.service.CCustomerTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:briefcase", title = "CRM.Customers")
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
	public Class<CCustomer> getEntityClass() { return CCustomer.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CCustomerInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCustomer.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CCustomer entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new customer entity");
		@NonNull
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize customer"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, customerTypeService, projectItemStatusService);
		LOGGER.debug("Customer initialization complete");
	}

	@Override
	protected void validateEntity(final CCustomer entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Customer type is required");
		Check.notBlank(entity.getCompanyName(), "Company Name is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getCompanyName().length() > 200) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Company Name cannot exceed %d characters", 200));
		}
		if (entity.getIndustry() != null && entity.getIndustry().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Industry cannot exceed %d characters", 100));
		}
		if (entity.getCompanySize() != null && entity.getCompanySize().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Company Size cannot exceed %d characters", 50));
		}
		if (entity.getWebsite() != null && entity.getWebsite().length() > 200) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Website cannot exceed %d characters", 200));
		}
		if (entity.getPrimaryContactName() != null && entity.getPrimaryContactName().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Primary Contact Name cannot exceed %d characters", 100));
		}
		if (entity.getPrimaryContactEmail() != null && entity.getPrimaryContactEmail().length() > 150) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Primary Contact Email cannot exceed %d characters", 150));
		}
		if (entity.getPrimaryContactPhone() != null && entity.getPrimaryContactPhone().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Primary Contact Phone cannot exceed %d characters", 50));
		}
		if (entity.getBillingAddress() != null && entity.getBillingAddress().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Billing Address cannot exceed %d characters", 500));
		}
		if (entity.getShippingAddress() != null && entity.getShippingAddress().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Shipping Address cannot exceed %d characters", 500));
		}
		if (entity.getCustomerNotes() != null && entity.getCustomerNotes().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Customer Notes cannot exceed %d characters", 2000));
		}
		// 3. Unique Checks
		final Optional<CCustomer> existingName = ((ICustomerRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// 4. Numeric Checks
		validateNumericField(entity.getAnnualRevenue(), "Annual Revenue", new BigDecimal("99999999999.99"));
		validateNumericField(entity.getLifetimeValue(), "Lifetime Value", new BigDecimal("99999999999.99"));
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
}
