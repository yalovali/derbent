package tech.derbent.api.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

/**
 * CPageServiceEmailSent - Page service for sent email archive interface.
 * 
 * Layer: Service (MVC)
 * Profile: derbent (PLM framework)
 * 
 * Provides page-level services for email archive:
 * - Dynamic page rendering
 * - Form generation from metadata
 * - Grid configuration
 * - Read-only archive views
 * - Custom reporting components (if needed)
 * 
 * This service bridges the sent email entity with the dynamic page system,
 * enabling automatic UI generation from entity annotations.
 * 
 * @see CEmailSent
 * @see CEmailSentService
 * @see CEmailSentInitializerService
 */
@Service
@Profile("derbent")
public class CPageServiceEmailSent extends CPageServiceDynamicPage<CEmailSent> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceEmailSent.class);

	public CPageServiceEmailSent(final IPageServiceImplementer<CEmailSent> view) {
		super(view);
		LOGGER.debug("CPageServiceEmailSent initialized");
	}

	@Override
	protected Class<CEmailSent> getEntityClass() {
		return CEmailSent.class;
	}

	// Custom component methods can be added here if needed
	// Example:
	// public Component createComponentDeliveryTimeline() { ... }
	// public Component createComponentEmailPreview() { ... }
	// Then referenced in @AMetaData: createComponentMethod = "createComponentDeliveryTimeline"
}
