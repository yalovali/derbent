package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.utils.Check;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.page.view.CDynamicPageBase;

public class CPageServiceMeetingType extends CPageServiceDynamicPage<CMeetingType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMeetingType.class);
	Long serialVersionUID = 1L;

	public CPageServiceMeetingType(CDynamicPageBase view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CMeetingType.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CMeetingType.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
