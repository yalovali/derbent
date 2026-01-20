package tech.derbent.api.companies.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.service.IUserCompanySettingsRepository;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCompanyService extends CEntityNamedService<CCompany> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyService.class);
	@Autowired
	private IUserCompanySettingsRepository userCompanySettingsRepository;

	public CCompanyService(final ICompanyRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing company deletion. Prevents deletion if: 1. Current user belongs to the company (cannot delete own company)
	 * 2. Company has associated users
	 * @param company the company entity to check
	 * @return null if company can be deleted, error message otherwise */
	/** Checks dependencies before allowing company deletion. Prevents deletion of user's own company and companies with active users. Always calls
	 * super.checkDeleteAllowed() first to ensure all parent-level checks (null validation) are performed.
	 * @param entity the company entity to check
	 * @return null if company can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CCompany entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Rule 1: Check if current user belongs to this company (user cannot delete their own company)
			if (sessionService != null && sessionService.getActiveUser().isPresent()) {
				final CCompany currentUserCompany = sessionService.getCurrentCompany();
				if (currentUserCompany != null && currentUserCompany.getId().equals(entity.getId())) {
					return "You cannot delete your own company. Please switch to another company first.";
				}
			}
			// Rule 2: Check if company has any users
			final long userCount = userCompanySettingsRepository.countByCompany_Id(entity.getId());
			if (userCount > 0) {
				return String.format("Cannot delete company. It is associated with %d user(s). Please remove all users first.", userCount);
			}
			return null; // Company can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for company: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	public List<CCompany> findActiveCompanies() {
		try {
			final List<CCompany> companies = ((ICompanyRepository) repository).findByActive(true);
			return companies;
		} catch (final Exception e) {
			LOGGER.error("Error finding active companies", e);
			throw e;
		}
	}

	public Optional<CCompany> findByTaxNumber(final String taxNumber) {
		Check.notBlank(taxNumber, "Tax number cannot be null or empty");
		try {
			final Optional<CCompany> company = ((ICompanyRepository) repository).findByTaxNumber(taxNumber.trim());
			return company;
		} catch (final Exception e) {
			LOGGER.error("Error finding company by tax number: {}", taxNumber, e);
			throw e;
		}
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CCompany> getAvailableCompanyForUser(Long id) {
		Check.notNull(id, "ID must not be null");
		return ((ICompanyRepository) repository).findCompaniesNotAssignedToUser(id);
	}

	@Override
	public Class<CCompany> getEntityClass() { return CCompany.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CCompanyInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCompany.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CCompany entity) {
		super.initializeNewEntity(entity);
		Check.notNull(entity, "Entity cannot be null");
	}

	public List<CCompany> searchCompaniesByName(final String searchTerm) {
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			LOGGER.debug("Empty search term, returning all companies");
			return ((ICompanyRepository) repository).findAllOrderByName();
		}
		try {
			final List<CCompany> companies = ((ICompanyRepository) repository).findByNameContainingIgnoreCase(searchTerm.trim());
			LOGGER.debug("Found {} companies matching search term: {}", companies.size(), searchTerm);
			return companies;
		} catch (final Exception e) {
			LOGGER.error("Error searching companies by name: {}", searchTerm, e);
			throw e;
		}
	}

	@Override
	protected void validateEntity(final CCompany entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		// Name is checked in CAbstractService via @Column(nullable=false) but explicitly here for clarity
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		// Name must be unique
		final Optional<CCompany> existingName = ((ICompanyRepository) repository).findByName(entity.getName());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME);
		}
		// Tax number should be unique if provided
		if (entity.getTaxNumber() != null && !entity.getTaxNumber().isBlank()) {
			final Optional<CCompany> existingTax = ((ICompanyRepository) repository).findByTaxNumber(entity.getTaxNumber());
			if (existingTax.isPresent() && !existingTax.get().getId().equals(entity.getId())) {
				throw new IllegalArgumentException("A company with this tax number already exists");
			}
		}
	}
}
