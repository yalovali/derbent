package tech.derbent.orders.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.CCurrency;

/**
 * CCurrencyService - Service layer for CCurrency entity. Layer: Service (MVC) Handles
 * business logic for currency operations including creation, validation, and management
 * of currency entities with currency code and symbol support.
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CCurrencyService extends CAbstractNamedEntityService<CCurrency> {

	/**
	 * Constructor for CCurrencyService.
	 * @param repository the CCurrencyRepository to use for data access
	 * @param clock      the Clock instance for time-related operations
	 */
	CCurrencyService(final CCurrencyRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Override
	protected Class<CCurrency> getEntityClass() { return CCurrency.class; }
}