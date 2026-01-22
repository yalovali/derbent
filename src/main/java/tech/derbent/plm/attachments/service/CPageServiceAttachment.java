package tech.derbent.plm.attachments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.attachments.domain.CAttachment;

/**
 * Page service for CAttachment entities.
 * 
 * Provides dynamic page functionality for attachment management views.
 */
public class CPageServiceAttachment extends CPageServiceDynamicPage<CAttachment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceAttachment.class);
	public CPageServiceAttachment(final IPageServiceImplementer<CAttachment> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", 
					this.getClass().getSimpleName(), CAttachment.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
					this.getClass().getSimpleName(), CAttachment.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
