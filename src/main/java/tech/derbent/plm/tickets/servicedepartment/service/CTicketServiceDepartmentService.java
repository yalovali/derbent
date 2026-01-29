package tech.derbent.plm.tickets.servicedepartment.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.tickets.servicedepartment.domain.CTicketServiceDepartment;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
@Transactional
public class CTicketServiceDepartmentService extends CEntityOfCompanyService<CTicketServiceDepartment>
		implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketServiceDepartmentService.class);

	public CTicketServiceDepartmentService(final IEntityOfCompanyRepository<CTicketServiceDepartment> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CTicketServiceDepartment entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		return superCheck != null ? superCheck : null;
	}

	@Transactional (readOnly = true)
	public List<CTicketServiceDepartment> findActiveByCompany(final CCompany company) {
		return ((ITicketServiceDepartmentRepository) repository).findActiveByCompany(company);
	}

	@Transactional (readOnly = true)
	public List<CTicketServiceDepartment> findByCompany(final CCompany company) {
		return ((ITicketServiceDepartmentRepository) repository).findByCompany(company);
	}

	@Transactional (readOnly = true)
	public List<CTicketServiceDepartment> findByManager(final CUser manager) {
		LOGGER.debug("Finding departments managed by: {}", manager != null ? manager.getLogin() : "null");
		return ((ITicketServiceDepartmentRepository) repository).findByManager(manager);
	}

	@Transactional (readOnly = true)
	public List<CTicketServiceDepartment> findByResponsibleUser(final CUser user) {
		LOGGER.debug("Finding departments for responsible user: {}", user != null ? user.getLogin() : "null");
		return ((ITicketServiceDepartmentRepository) repository).findByResponsibleUser(user);
	}

	@Override
	public Class<CTicketServiceDepartment> getEntityClass() { return CTicketServiceDepartment.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CTicketServiceDepartmentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceTicketServiceDepartment.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CTicketServiceDepartment entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getDescription(), "Description", 2000);
		
		// 3. Unique Checks - use base class helper
		validateUniqueNameInCompany((ITicketServiceDepartmentRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
