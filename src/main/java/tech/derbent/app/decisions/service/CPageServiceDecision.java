package tech.derbent.app.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceWithWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.decisions.domain.CDecision;

public class CPageServiceDecision extends CPageServiceWithWorkflow<CDecision> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDecision.class);
	Long serialVersionUID = 1L;

	public CPageServiceDecision(IPageServiceImplementer<CDecision> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CDecision.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CDecision.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
