package tech.derbent.companies.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.view.CComponentCompanyUserSettings;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.CUserService;

/** CCompanyService - Business logic layer for CCompany entities Layer: Service (MVC) Extends CAbstractService to provide standard CRUD operations
 * with additional business logic */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCompanyService extends CAbstractNamedEntityService<CCompany> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyService.class);
	@Autowired
	private ApplicationContext applicationContext;

	/** Constructor for CCompanyService
	 * @param repository the CCompanyRepository instance
	 * @param clock      the Clock instance for time-related operations */
	public CCompanyService(final ICompanyRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Soft delete - disables the company instead of deleting it
	 * @param id the company ID to disable */
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

	/** Re-enables a disabled company
	 * @param id the company ID to enable */
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

	/** Finds a company by tax number
	 * @param taxNumber the tax identification number
	 * @return Optional containing the company if found */
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

	/** Finds all enabled companies
	 * @return List of enabled companies */
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

	@Override
	protected Class<CCompany> getEntityClass() { return CCompany.class; }

	/** Finds companies by name containing the search term (case-insensitive)
	 * @param searchTerm the search term
	 * @return List of companies matching the search term */
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

	public Component createCompanyUserSettingsComponent() {
		LOGGER.debug("Creating enhanced company user settings component");
		try {
			// Get services from ApplicationContext to avoid circular dependency
			CUserService userService = applicationContext.getBean(CUserService.class);
			CUserCompanySettingsService userCompanySettingsService = applicationContext.getBean(CUserCompanySettingsService.class);
			// Create wrapper div for the component - this will be replaced by actual component during binding
			Div wrapper = new Div();
			wrapper.addClassName("component-company-user-settings-wrapper");
			wrapper.getElement().setAttribute("data-component-type", "CComponentCompanyUserSettings");
			LOGGER.debug("Successfully created company user settings component wrapper");
			return wrapper;
		} catch (Exception e) {
			LOGGER.error("Failed to create company user settings component: {}", e.getMessage(), e);
			Div errorDiv = new Div();
			errorDiv.setText("Error loading company user settings component");
			errorDiv.addClassName("error-component");
			return errorDiv;
		}
	}
}
