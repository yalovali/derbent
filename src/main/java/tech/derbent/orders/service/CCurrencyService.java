package tech.derbent.orders.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.CCurrency;

/**
 * CCurrencyService - Service layer for CCurrency entity.
 * Layer: Service (MVC)
 * 
 * Handles business logic for currency operations including creation,
 * validation, and management of currency entities with currency code
 * and symbol support.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CCurrencyService extends CAbstractNamedEntityService<CCurrency> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCurrencyService.class);

    /**
     * Constructor for CCurrencyService.
     * 
     * @param repository the CCurrencyRepository to use for data access
     * @param clock the Clock instance for time-related operations
     */
    CCurrencyService(final CCurrencyRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Creates a new currency entity with name, currency code and symbol.
     * 
     * @param name the name of the currency
     * @param currencyCode the ISO 4217 currency code
     * @param currencySymbol the currency symbol
     */
    @Transactional
    public void createEntity(final String name, final String currencyCode, final String currencySymbol) {
        LOGGER.info("createEntity called with name: {}, code: {}, symbol: {}", name, currencyCode, currencySymbol);

        // Standard test failure logic for error handler testing
        if ("fail".equals(name)) {
            LOGGER.warn("Test failure requested for name: {}", name);
            throw new RuntimeException("This is for testing the error handler");
        }

        // Validate name using parent validation
        validateEntityName(name);
        
        // Validate currency code
        if (currencyCode == null || currencyCode.trim().length() != 3) {
            throw new IllegalArgumentException("Currency code must be exactly 3 characters");
        }

        final CCurrency entity = new CCurrency(name, currencyCode.toUpperCase(), currencySymbol);
        repository.saveAndFlush(entity);
        LOGGER.info("Currency created successfully with name: {} and code: {}", name, currencyCode);
    }

    /**
     * Creates a new currency entity with name and description.
     * 
     * @param name the name of the currency
     * @param description the description of the currency
     */
    @Transactional
    public void createEntity(final String name, final String description) {
        LOGGER.info("createEntity called with name: {} and description: {}", name, description);

        // Standard test failure logic for error handler testing
        if ("fail".equals(name)) {
            LOGGER.warn("Test failure requested for name: {}", name);
            throw new RuntimeException("This is for testing the error handler");
        }

        // Validate name using parent validation
        validateEntityName(name);
        final CCurrency entity = new CCurrency();
        entity.setName(name);
        entity.setDescription(description);
        repository.saveAndFlush(entity);
        LOGGER.info("Currency created successfully with name: {}", name);
    }

    @Override
    protected CCurrency createNewEntityInstance() {
        return new CCurrency();
    }
}