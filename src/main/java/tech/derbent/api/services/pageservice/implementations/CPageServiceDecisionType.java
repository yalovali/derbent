package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.decisions.domain.CDecisionType;

public class CPageServiceDecisionType extends CPageServiceDynamicPage<CDecisionType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDecisionType.class);
	Long serialVersionUID = 1L;

	public CPageServiceDecisionType(IPageServiceImplementer view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CDecisionType.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CDecisionType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
