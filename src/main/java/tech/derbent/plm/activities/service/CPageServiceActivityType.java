package tech.derbent.plm.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.activities.domain.CActivityType;

public class CPageServiceActivityType extends CPageServiceDynamicPage<CActivityType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivityType.class);
	Long serialVersionUID = 1L;

	public CPageServiceActivityType(IPageServiceImplementer<CActivityType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CActivityType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CActivityType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
