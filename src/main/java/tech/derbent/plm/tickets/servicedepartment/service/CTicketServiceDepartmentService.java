package tech.derbent.plm.tickets.servicedepartment.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CEntityConstants;
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
@Menu (icon = "vaadin:sitemap", title = "Settings.Service Departments")
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
		if (superCheck != null) {
			return superCheck;
		}
		return null;
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
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getDescription() != null && entity.getDescription().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Description cannot exceed %d characters", 2000));
		}
		// 3. Unique Checks
		// Note: Repository needs to cast to specific interface if method not in base
		final Optional<CTicketServiceDepartment> existing =
				((ITicketServiceDepartmentRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
