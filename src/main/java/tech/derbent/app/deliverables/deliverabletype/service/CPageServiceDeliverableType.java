package tech.derbent.app.deliverables.deliverabletype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.deliverables.deliverabletype.domain.CDeliverableType;

public class CPageServiceDeliverableType extends CPageServiceDynamicPage<CDeliverableType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDeliverableType.class);
	Long serialVersionUID = 1L;

	public CPageServiceDeliverableType(IPageServiceImplementer<CDeliverableType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CDeliverableType.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CDeliverableType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
