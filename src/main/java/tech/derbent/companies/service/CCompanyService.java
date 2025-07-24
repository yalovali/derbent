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
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.companies.domain.CCompany;

/**
 * CCompanyService - Business logic layer for CCompany entities
 * Layer: Service (MVC)
 * Extends CAbstractService to provide standard CRUD operations with additional business logic
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true) // Default to read-only transactions for better performance
public class CCompanyService extends CAbstractService<CCompany> {

    private static final Logger logger = LoggerFactory.getLogger(CCompanyService.class);
    private final CCompanyRepository companyRepository;

    /**
     * Constructor for CCompanyService
     * @param repository the CCompanyRepository instance
     * @param clock the Clock instance for time-related operations
     */
    public CCompanyService(final CCompanyRepository repository, final Clock clock) {
        super(repository, clock);
        this.companyRepository = repository;
        logger.info("CCompanyService initialized successfully");
    }

    /**
     * Creates a new company entity with the given name
     * @param name the company name
     * @throws RuntimeException if name is "fail" (for testing error handling)
     */
    @Transactional
    public void createEntity(final String name) {
        logger.debug("createEntity called with name: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempt to create company with null or empty name");
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        
        if ("fail".equals(name)) {
            logger.warn("Test failure requested for name: {}", name);
            throw new RuntimeException("This is for testing the error handler");
        }
        
        final var entity = new CCompany();
        entity.setName(name.trim());
        entity.setEnabled(true); // Default to enabled
        
        try {
            companyRepository.saveAndFlush(entity);
            logger.info("Company entity created successfully with name: {}", name);
        } catch (final Exception e) {
            logger.error("Failed to create company entity with name: {}", name, e);
            throw new RuntimeException("Failed to create company: " + e.getMessage(), e);
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
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Empty search term, returning all companies");
            return companyRepository.findAllOrderByName();
        }
        
        try {
            final List<CCompany> companies = companyRepository.findByNameContainingIgnoreCase(searchTerm.trim());
            logger.debug("Found {} companies matching search term: {}", companies.size(), searchTerm);
            return companies;
        } catch (final Exception e) {
            logger.error("Error searching companies by name: {}", searchTerm, e);
            throw new RuntimeException("Failed to search companies by name", e);
        }
    }

    /**
     * Finds a company by exact name match
     * @param name the exact company name
     * @return Optional containing the company if found
     */
    public Optional<CCompany> findByName(final String name) {
        logger.debug("findByName called with name: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempt to find company with null or empty name");
            return Optional.empty();
        }
        
        try {
            final Optional<CCompany> company = companyRepository.findByName(name.trim());
            logger.debug("Company {} found: {}", name, company.isPresent());
            return company;
        } catch (final Exception e) {
            logger.error("Error finding company by name: {}", name, e);
            throw new RuntimeException("Failed to find company by name", e);
        }
    }

    /**
     * Finds a company by tax number
     * @param taxNumber the tax identification number
     * @return Optional containing the company if found
     */
    public Optional<CCompany> findByTaxNumber(final String taxNumber) {
        logger.debug("findByTaxNumber called with taxNumber: {}", taxNumber);
        
        if (taxNumber == null || taxNumber.trim().isEmpty()) {
            logger.warn("Attempt to find company with null or empty tax number");
            return Optional.empty();
        }
        
        try {
            final Optional<CCompany> company = companyRepository.findByTaxNumber(taxNumber.trim());
            logger.debug("Company with tax number {} found: {}", taxNumber, company.isPresent());
            return company;
        } catch (final Exception e) {
            logger.error("Error finding company by tax number: {}", taxNumber, e);
            throw new RuntimeException("Failed to find company by tax number", e);
        }
    }

    /**
     * Validates if a company name is unique (excluding the current company being updated)
     * @param name the company name to validate
     * @param currentId the ID of the current company being updated (null for new companies)
     * @return true if the name is unique, false otherwise
     */
    public boolean isNameUnique(final String name, final Long currentId) {
        logger.debug("isNameUnique called with name: {}, currentId: {}", name, currentId);
        
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Name uniqueness check called with null or empty name");
            return false;
        }
        
        try {
            final Optional<CCompany> existingCompany = companyRepository.findByName(name.trim());
            if (existingCompany.isEmpty()) {
                logger.debug("Name {} is unique", name);
                return true;
            }
            
            // If we're updating an existing company, check if it's the same company
            if (currentId != null && existingCompany.get().getId().equals(currentId)) {
                logger.debug("Name {} belongs to current company being updated", name);
                return true;
            }
            
            logger.debug("Name {} is not unique", name);
            return false;
        } catch (final Exception e) {
            logger.error("Error checking name uniqueness for: {}", name, e);
            throw new RuntimeException("Failed to check name uniqueness", e);
        }
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
}