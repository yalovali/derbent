package tech.derbent.api.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

/**
 * CPageServiceEmailQueued - Page service for email queue management interface.
 * 
 * Layer: Service (MVC)
 * Profile: derbent (PLM framework)
 * 
 * Provides page-level services for email queue:
 * - Dynamic page rendering
 * - Form generation from metadata
 * - Grid configuration
 * - Custom component integration (if needed)
 * 
 * This service bridges the email queue entity with the dynamic page system,
 * enabling automatic UI generation from entity annotations.
 * 
 * @see CEmailQueued
 * @see CEmailQueuedService
 * @see CEmailQueuedInitializerService
 */
@Service
@Profile("derbent")
public class CPageServiceEmailQueued extends CPageServiceDynamicPage<CEmailQueued> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceEmailQueued.class);

	public CPageServiceEmailQueued(final IPageServiceImplementer<CEmailQueued> view) {
		super(view);
		LOGGER.debug("CPageServiceEmailQueued initialized");
	}

	@Override
	protected Class<CEmailQueued> getEntityClass() {
		return CEmailQueued.class;
	}

	// Custom component methods can be added here if needed
	// Example:
	// public Component createComponentRetryStatus() { ... }
	// Then referenced in @AMetaData: createComponentMethod = "createComponentRetryStatus"
}
