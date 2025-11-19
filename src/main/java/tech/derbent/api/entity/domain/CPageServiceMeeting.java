package tech.derbent.api.entity.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceWithWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.meetings.domain.CMeeting;

public class CPageServiceMeeting extends CPageServiceWithWorkflow<CMeeting> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMeeting.class);
	Long serialVersionUID = 1L;

	public CPageServiceMeeting(IPageServiceImplementer<CMeeting> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CMeeting.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CMeeting.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	public void createNewInstance() {}
}
