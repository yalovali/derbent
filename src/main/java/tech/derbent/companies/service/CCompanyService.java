package tech.derbent.companies.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCompanyService extends CAbstractNamedEntityService<CCompany> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyService.class);

	public CCompanyService(final ICompanyRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional
	public void disableCompany(final Long id) {
		Check.notNull(id, "Company ID cannot be null");
		try {
			final Optional<CCompany> companyOptional = repository.findById(id);
			Check.isTrue(companyOptional.isPresent(), "Company not found with id: " + id);
			final CCompany company = companyOptional.get();
			company.setEnabled(false);
			repository.saveAndFlush(company);
		} catch (final Exception e) {
			LOGGER.error("Error disabling company with id: {}", id, e);
			throw new RuntimeException("Failed to disable company", e);
		}
	}

	@Transactional
	public void enableCompany(final Long id) {
		Check.notNull(id, "Company ID cannot be null");
		try {
			final Optional<CCompany> companyOptional = repository.findById(id);
			Check.isTrue(companyOptional.isPresent(), "Company not found with id: " + id);
			final CCompany company = companyOptional.get();
			company.setEnabled(true);
			repository.saveAndFlush(company);
		} catch (final Exception e) {
			LOGGER.error("Error enabling company with id: {}", id, e);
			throw new RuntimeException("Failed to enable company", e);
		}
	}

	public Optional<CCompany> findByTaxNumber(final String taxNumber) {
		Check.notBlank(taxNumber, "Tax number cannot be null or empty");
		try {
			final Optional<CCompany> company = ((ICompanyRepository) repository).findByTaxNumber(taxNumber.trim());
			return company;
		} catch (final Exception e) {
			LOGGER.error("Error finding company by tax number: {}", taxNumber, e);
			throw new RuntimeException("Failed to find company by tax number", e);
		}
	}

	public List<CCompany> findEnabledCompanies() {
		LOGGER.debug("findEnabledCompanies called");
		try {
			final List<CCompany> companies = ((ICompanyRepository) repository).findByEnabled(true);
			LOGGER.debug("Found {} enabled companies", companies.size());
			return companies;
		} catch (final Exception e) {
			LOGGER.error("Error finding enabled companies", e);
			throw new RuntimeException("Failed to retrieve enabled companies", e);
		}
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CCompany> getAvailableCompanyForUser(Long id) {
		// TODO Auto-generated method stub
		Check.notNull(id, "ID must not be null");
		return ((ICompanyRepository) repository).findCompaniesNotAssignedToUser(id);
	}

	@Override
	protected Class<CCompany> getEntityClass() { return CCompany.class; }

	public List<CCompany> searchCompaniesByName(final String searchTerm) {
		LOGGER.debug("searchCompaniesByName called with searchTerm: {}", searchTerm);
		if ((searchTerm == null) || searchTerm.trim().isEmpty()) {
			LOGGER.debug("Empty search term, returning all companies");
			return ((ICompanyRepository) repository).findAllOrderByName();
		}
		try {
			final List<CCompany> companies = ((ICompanyRepository) repository).findByNameContainingIgnoreCase(searchTerm.trim());
			LOGGER.debug("Found {} companies matching search term: {}", companies.size(), searchTerm);
			return companies;
		} catch (final Exception e) {
			LOGGER.error("Error searching companies by name: {}", searchTerm, e);
			throw new RuntimeException("Failed to search companies by name", e);
		}
	}
}
