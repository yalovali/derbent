package tech.derbent.app.risklevel.risklevel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.risklevel.risklevel.domain.CRiskLevel;

public class CPageServiceRiskLevel extends CPageServiceDynamicPage<CRiskLevel> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRiskLevel.class);
	Long serialVersionUID = 1L;

	public CPageServiceRiskLevel(IPageServiceImplementer<CRiskLevel> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CRiskLevel.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CRiskLevel.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
