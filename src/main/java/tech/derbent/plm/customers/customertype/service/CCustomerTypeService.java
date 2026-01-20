package tech.derbent.plm.customers.customertype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.plm.customers.customer.service.ICustomerRepository;
import tech.derbent.plm.customers.customertype.domain.CCustomerType;
import tech.derbent.base.session.service.ISessionService;

import java.util.Optional;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCustomerTypeService extends CTypeEntityService<CCustomerType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomerTypeService.class);
	private final ICustomerRepository customerRepository;

	public CCustomerTypeService(final ICustomerTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final ICustomerRepository customerRepository) {
		super(repository, clock, sessionService);
		this.customerRepository = customerRepository;
	}

	@Override
	public String checkDeleteAllowed(final CCustomerType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = customerRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d customer%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for customer type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	protected void validateEntity(final CCustomerType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CCustomerType> existing = ((ICustomerTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}

	@Override
	public Class<CCustomerType> getEntityClass() { return CCustomerType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CCustomerTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCustomerType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CCustomerType entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((ICustomerTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("CustomerType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
