package tech.derbent.administration.service;

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
import tech.derbent.administration.domain.CCompanySettings;
import tech.derbent.companies.domain.CCompany;

/**
 * CCompanySettingsService - Business logic layer for CCompanySettings entities.
 * Layer: Service (MVC)
 * 
 * Provides comprehensive business logic for managing company-wide administration settings,
 * including CRUD operations, validation, and specialized business methods for settings management.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true) // Default to read-only transactions for better performance
public class CCompanySettingsService extends CAbstractService<CCompanySettings> {

    private static final Logger logger = LoggerFactory.getLogger(CCompanySettingsService.class);
    private final CCompanySettingsRepository companySettingsRepository;

    /**
     * Constructor for CCompanySettingsService.
     * 
     * @param repository the CCompanySettingsRepository instance
     * @param clock the Clock instance for time-related operations
     */
    public CCompanySettingsService(final CCompanySettingsRepository repository, final Clock clock) {
        super(repository, clock);
        this.companySettingsRepository = repository;
        logger.info("CCompanySettingsService initialized successfully");
    }

    /**
     * Creates default company settings for a company.
     * If settings already exist, returns the existing settings.
     * 
     * @param company the company to create settings for
     * @return the created or existing CCompanySettings
     * @throws IllegalArgumentException if company is null
     */
    @Transactional
    public CCompanySettings createDefaultSettingsForCompany(final CCompany company) {
        logger.debug("createDefaultSettingsForCompany called with company: {}", 
                    company != null ? company.getName() : "null");
        
        if (company == null) {
            logger.warn("Attempt to create settings for null company");
            throw new IllegalArgumentException("Company cannot be null");
        }

        // Check if settings already exist
        final Optional<CCompanySettings> existingSettings = companySettingsRepository.findByCompany(company);
        if (existingSettings.isPresent()) {
            logger.info("Company settings already exist for company: {}", company.getName());
            return existingSettings.get();
        }

        try {
            final CCompanySettings newSettings = new CCompanySettings(company);
            final CCompanySettings savedSettings = companySettingsRepository.saveAndFlush(newSettings);
            logger.info("Default company settings created successfully for company: {}", company.getName());
            return savedSettings;
        } catch (final Exception e) {
            logger.error("Failed to create default settings for company: {}", company.getName(), e);
            throw new RuntimeException("Failed to create company settings: " + e.getMessage(), e);
        }
    }

    /**
     * Finds company settings by company.
     * 
     * @param company the company to find settings for
     * @return Optional containing the CCompanySettings if found, empty otherwise
     * @throws IllegalArgumentException if company is null
     */
    public Optional<CCompanySettings> findByCompany(final CCompany company) {
        logger.debug("findByCompany called with company: {}", 
                    company != null ? company.getName() : "null");
        
        if (company == null) {
            logger.warn("Attempt to find settings for null company");
            throw new IllegalArgumentException("Company cannot be null");
        }

        try {
            final Optional<CCompanySettings> result = companySettingsRepository.findByCompany(company);
            logger.debug("Found settings for company {}: {}", company.getName(), result.isPresent());
            return result;
        } catch (final Exception e) {
            logger.error("Error finding settings for company: {}", company.getName(), e);
            throw new RuntimeException("Failed to find company settings: " + e.getMessage(), e);
        }
    }

    /**
     * Finds company settings by company ID.
     * 
     * @param companyId the ID of the company
     * @return Optional containing the CCompanySettings if found, empty otherwise
     * @throws IllegalArgumentException if companyId is null or invalid
     */
    public Optional<CCompanySettings> findByCompanyId(final Long companyId) {
        logger.debug("findByCompanyId called with companyId: {}", companyId);
        
        if (companyId == null || companyId <= 0) {
            logger.warn("Invalid company ID provided: {}", companyId);
            throw new IllegalArgumentException("Company ID must be a positive number");
        }

        try {
            final Optional<CCompanySettings> result = companySettingsRepository.findByCompanyId(companyId);
            logger.debug("Found settings for company ID {}: {}", companyId, result.isPresent());
            return result;
        } catch (final Exception e) {
            logger.error("Error finding settings for company ID: {}", companyId, e);
            throw new RuntimeException("Failed to find company settings: " + e.getMessage(), e);
        }
    }

    /**
     * Gets or creates company settings for a company.
     * If settings don't exist, creates default settings.
     * 
     * @param company the company to get or create settings for
     * @return the existing or newly created CCompanySettings
     * @throws IllegalArgumentException if company is null
     */
    @Transactional
    public CCompanySettings getOrCreateSettingsForCompany(final CCompany company) {
        logger.debug("getOrCreateSettingsForCompany called with company: {}", 
                    company != null ? company.getName() : "null");
        
        if (company == null) {
            logger.warn("Attempt to get or create settings for null company");
            throw new IllegalArgumentException("Company cannot be null");
        }

        final Optional<CCompanySettings> existingSettings = findByCompany(company);
        if (existingSettings.isPresent()) {
            logger.debug("Returning existing settings for company: {}", company.getName());
            return existingSettings.get();
        } else {
            logger.info("Creating new settings for company: {}", company.getName());
            return createDefaultSettingsForCompany(company);
        }
    }

    /**
     * Updates company settings with validation.
     * 
     * @param settings the settings to update
     * @return the updated CCompanySettings
     * @throws IllegalArgumentException if settings or company is null
     * @throws EntityNotFoundException if settings don't exist in database
     */
    @Transactional
    public CCompanySettings updateSettings(final CCompanySettings settings) {
        logger.debug("updateSettings called with settings ID: {}", 
                    settings != null ? settings.getId() : "null");
        
        if (settings == null) {
            logger.warn("Attempt to update null settings");
            throw new IllegalArgumentException("Settings cannot be null");
        }

        if (settings.getCompany() == null) {
            logger.warn("Attempt to update settings with null company");
            throw new IllegalArgumentException("Settings must have an associated company");
        }

        if (settings.getId() == null) {
            logger.warn("Attempt to update settings without ID");
            throw new IllegalArgumentException("Settings must have an ID for update operation");
        }

        // Validate business rules
        validateSettingsBusinessRules(settings);

        try {
            // Check if entity exists
            if (!companySettingsRepository.existsById(settings.getId())) {
                logger.warn("Attempt to update non-existent settings with ID: {}", settings.getId());
                throw new EntityNotFoundException("Company settings not found with ID: " + settings.getId());
            }

            final CCompanySettings updatedSettings = companySettingsRepository.saveAndFlush(settings);
            logger.info("Company settings updated successfully for company: {}", settings.getCompany().getName());
            return updatedSettings;
        } catch (final EntityNotFoundException e) {
            throw e; // Re-throw EntityNotFoundException as-is
        } catch (final Exception e) {
            logger.error("Failed to update settings for company: {}", settings.getCompany().getName(), e);
            throw new RuntimeException("Failed to update company settings: " + e.getMessage(), e);
        }
    }

    /**
     * Finds all companies with email notifications enabled.
     * Useful for system-wide email processing.
     * 
     * @return List of CCompanySettings with email notifications enabled
     */
    public List<CCompanySettings> findCompaniesWithEmailNotificationsEnabled() {
        logger.debug("findCompaniesWithEmailNotificationsEnabled called");
        
        try {
            final List<CCompanySettings> result = companySettingsRepository.findByEmailNotificationsEnabled();
            logger.debug("Found {} companies with email notifications enabled", result.size());
            return result;
        } catch (final Exception e) {
            logger.error("Error finding companies with email notifications enabled", e);
            throw new RuntimeException("Failed to find companies with email notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Finds companies in a specific timezone.
     * 
     * @param timezone the timezone to search for
     * @return List of CCompanySettings in the specified timezone
     * @throws IllegalArgumentException if timezone is null or empty
     */
    public List<CCompanySettings> findCompaniesByTimezone(final String timezone) {
        logger.debug("findCompaniesByTimezone called with timezone: {}", timezone);
        
        if (timezone == null || timezone.trim().isEmpty()) {
            logger.warn("Invalid timezone provided: {}", timezone);
            throw new IllegalArgumentException("Timezone cannot be null or empty");
        }

        try {
            final List<CCompanySettings> result = companySettingsRepository.findByCompanyTimezone(timezone.trim());
            logger.debug("Found {} companies in timezone: {}", result.size(), timezone);
            return result;
        } catch (final Exception e) {
            logger.error("Error finding companies by timezone: {}", timezone, e);
            throw new RuntimeException("Failed to find companies by timezone: " + e.getMessage(), e);
        }
    }

    /**
     * Finds all company settings ordered by company name.
     * 
     * @return List of all CCompanySettings ordered by company name
     */
    public List<CCompanySettings> findAllSettingsOrderedByCompanyName() {
        logger.debug("findAllSettingsOrderedByCompanyName called");
        
        try {
            final List<CCompanySettings> result = companySettingsRepository.findAllOrderByCompanyName();
            logger.debug("Found {} company settings records", result.size());
            return result;
        } catch (final Exception e) {
            logger.error("Error finding all company settings", e);
            throw new RuntimeException("Failed to find company settings: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if company settings exist for a specific company ID.
     * 
     * @param companyId the ID of the company
     * @return true if settings exist, false otherwise
     * @throws IllegalArgumentException if companyId is null or invalid
     */
    public boolean existsByCompanyId(final Long companyId) {
        logger.debug("existsByCompanyId called with companyId: {}", companyId);
        
        if (companyId == null || companyId <= 0) {
            logger.warn("Invalid company ID provided: {}", companyId);
            throw new IllegalArgumentException("Company ID must be a positive number");
        }

        try {
            final boolean exists = companySettingsRepository.existsByCompanyId(companyId);
            logger.debug("Settings exist for company ID {}: {}", companyId, exists);
            return exists;
        } catch (final Exception e) {
            logger.error("Error checking if settings exist for company ID: {}", companyId, e);
            throw new RuntimeException("Failed to check settings existence: " + e.getMessage(), e);
        }
    }

    /**
     * Validates business rules for company settings.
     * 
     * @param settings the settings to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSettingsBusinessRules(final CCompanySettings settings) {
        logger.debug("validateSettingsBusinessRules called");
        
        // Validate working hours
        if (settings.getWorkingHoursPerDay() != null && 
            (settings.getWorkingHoursPerDay().doubleValue() <= 0 || 
             settings.getWorkingHoursPerDay().doubleValue() > 24)) {
            throw new IllegalArgumentException("Working hours per day must be between 0.1 and 24.0");
        }

        // Validate working days
        if (settings.getWorkingDaysPerWeek() != null && 
            (settings.getWorkingDaysPerWeek() < 1 || settings.getWorkingDaysPerWeek() > 7)) {
            throw new IllegalArgumentException("Working days per week must be between 1 and 7");
        }

        // Validate work hours
        if (settings.getStartWorkHour() != null && settings.getEndWorkHour() != null) {
            if (settings.getStartWorkHour() >= settings.getEndWorkHour()) {
                throw new IllegalArgumentException("Start work hour must be before end work hour");
            }
        }

        // Validate reminder days
        if (settings.getDueDateReminderDays() != null && settings.getDueDateReminderDays() < 0) {
            throw new IllegalArgumentException("Due date reminder days cannot be negative");
        }

        // Validate theme color format
        if (settings.getCompanyThemeColor() != null && 
            !settings.getCompanyThemeColor().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Company theme color must be a valid hex color (e.g., #1976d2)");
        }

        logger.debug("Settings validation passed successfully");
    }
}