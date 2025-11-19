package tech.derbent.app.deliverables.deliverable.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.deliverables.deliverable.domain.CDeliverable;

public class CPageServiceDeliverable extends CPageServiceDynamicPage<CDeliverable> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDeliverable.class);
	Long serialVersionUID = 1L;

	public CPageServiceDeliverable(IPageServiceImplementer<CDeliverable> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CDeliverable.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CDeliverable.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
