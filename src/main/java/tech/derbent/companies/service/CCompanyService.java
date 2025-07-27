package tech.derbent.companies.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.companies.domain.CCompany;

/**
 * CCompanyService - Business logic layer for CCompany entities Layer: Service (MVC)
 * Extends CAbstractService to provide standard CRUD operations with additional business
 * logic
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true) // Default to read-only transactions for better
									// performance
public class CCompanyService extends CAbstractNamedEntityService<CCompany> {

	private static final Logger logger = LoggerFactory.getLogger(CCompanyService.class);

	private final CCompanyRepository companyRepository;

	/**
	 * Constructor for CCompanyService
	 * @param repository the CCompanyRepository instance
	 * @param clock      the Clock instance for time-related operations
	 */
	public CCompanyService(final CCompanyRepository repository, final Clock clock) {
		super(repository, clock);
		this.companyRepository = repository;
	}

	/**
	 * Creates a new company entity with the given name and sets it as enabled by default.
	 * Overrides the base createEntity to add company-specific logic (enabled flag).
	 * @param name the company name
	 * @throws RuntimeException         if name is "fail" (for testing error handling)
	 * @throws IllegalArgumentException if name is null or empty
	 */
	@Override
	@Transactional
	public void createEntity(final String name) {
		logger.debug("createEntity called with name: {} for CCompany", name);
		// Use parent validation and creation logic
		super.createEntity(name);
		// Find the created entity to set company-specific properties
		final var entity = findByName(name).orElseThrow(
			() -> new RuntimeException("Created company not found: " + name));
		// Set company-specific default values
		entity.setEnabled(true);
		companyRepository.saveAndFlush(entity);
		logger.info("Company entity created successfully with name: {}", name);
	}

	@Override
	protected CCompany createNewEntityInstance() {
		final CCompany company = new CCompany();
		company.setEnabled(true); // Default to enabled
		return company;
	}

	/**
	 * Soft delete - disables the company instead of deleting it
	 * @param id the company ID to disable
	 */
	@Transactional
	public void disableCompany(final Long id) {
		logger.debug("disableCompany called with id: {}", id);

		if (id == null) {
			logger.warn("Attempt to disable company with null id");
			throw new IllegalArgumentException("Company ID cannot be null");
		}

		try {
			final Optional<CCompany> companyOptional = companyRepository.findById(id);

			if (companyOptional.isEmpty()) {
				logger.warn("Company not found for disabling with id: {}", id);
				throw new EntityNotFoundException("Company not found with id: " + id);
			}
			final CCompany company = companyOptional.get();
			company.setEnabled(false);
			companyRepository.saveAndFlush(company);
			logger.info("Company disabled successfully with id: {}", id);
		} catch (final Exception e) {
			logger.error("Error disabling company with id: {}", id, e);
			throw new RuntimeException("Failed to disable company", e);
		}
	}

	/**
	 * Re-enables a disabled company
	 * @param id the company ID to enable
	 */
	@Transactional
	public void enableCompany(final Long id) {
		logger.debug("enableCompany called with id: {}", id);

		if (id == null) {
			logger.warn("Attempt to enable company with null id");
			throw new IllegalArgumentException("Company ID cannot be null");
		}

		try {
			final Optional<CCompany> companyOptional = companyRepository.findById(id);

			if (companyOptional.isEmpty()) {
				logger.warn("Company not found for enabling with id: {}", id);
				throw new EntityNotFoundException("Company not found with id: " + id);
			}
			final CCompany company = companyOptional.get();
			company.setEnabled(true);
			companyRepository.saveAndFlush(company);
			logger.info("Company enabled successfully with id: {}", id);
		} catch (final Exception e) {
			logger.error("Error enabling company with id: {}", id, e);
			throw new RuntimeException("Failed to enable company", e);
		}
	}

	/**
	 * Finds a company by tax number
	 * @param taxNumber the tax identification number
	 * @return Optional containing the company if found
	 */
	public Optional<CCompany> findByTaxNumber(final String taxNumber) {
		logger.debug("findByTaxNumber called with taxNumber: {}", taxNumber);

		if ((taxNumber == null) || taxNumber.trim().isEmpty()) {
			logger.warn("Attempt to find company with null or empty tax number");
			return Optional.empty();
		}

		try {
			final Optional<CCompany> company =
				companyRepository.findByTaxNumber(taxNumber.trim());
			logger.debug("Company with tax number {} found: {}", taxNumber,
				company.isPresent());
			return company;
		} catch (final Exception e) {
			logger.error("Error finding company by tax number: {}", taxNumber, e);
			throw new RuntimeException("Failed to find company by tax number", e);
		}
	}

	/**
	 * Finds all enabled companies
	 * @return List of enabled companies
	 */
	public List<CCompany> findEnabledCompanies() {
		logger.debug("findEnabledCompanies called");

		try {
			final List<CCompany> companies = companyRepository.findByEnabled(true);
			logger.debug("Found {} enabled companies", companies.size());
			return companies;
		} catch (final Exception e) {
			logger.error("Error finding enabled companies", e);
			throw new RuntimeException("Failed to retrieve enabled companies", e);
		}
	}

	/**
	 * Finds companies by name containing the search term (case-insensitive)
	 * @param searchTerm the search term
	 * @return List of companies matching the search term
	 */
	public List<CCompany> searchCompaniesByName(final String searchTerm) {
		logger.debug("searchCompaniesByName called with searchTerm: {}", searchTerm);

		if ((searchTerm == null) || searchTerm.trim().isEmpty()) {
			logger.debug("Empty search term, returning all companies");
			return companyRepository.findAllOrderByName();
		}

		try {
			final List<CCompany> companies =
				companyRepository.findByNameContainingIgnoreCase(searchTerm.trim());
			logger.debug("Found {} companies matching search term: {}", companies.size(),
				searchTerm);
			return companies;
		} catch (final Exception e) {
			logger.error("Error searching companies by name: {}", searchTerm, e);
			throw new RuntimeException("Failed to search companies by name", e);
		}
	}
}