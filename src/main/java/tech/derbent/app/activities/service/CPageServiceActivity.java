package tech.derbent.app.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceWithWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;

public class CPageServiceActivity extends CPageServiceWithWorkflow<CActivity> {
	public Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivity.class);
	Long serialVersionUID = 1L;

	public CPageServiceActivity(final IPageServiceImplementer<CActivity> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CActivity.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
			detailsBuilder = view.getDetailsBuilder();
			if (detailsBuilder != null) {
				formBuilder = detailsBuilder.getFormBuilder();
			}
			bindMethods(this);
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CActivity.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
