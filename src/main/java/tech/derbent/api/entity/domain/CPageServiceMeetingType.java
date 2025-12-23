package tech.derbent.api.entity.domain;

import tech.derbent.api.utils.Check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.meetings.domain.CMeetingType;

public class CPageServiceMeetingType extends CPageServiceDynamicPage<CMeetingType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMeetingType.class);
	Long serialVersionUID = 1L;

	public CPageServiceMeetingType(IPageServiceImplementer<CMeetingType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CMeetingType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CMeetingType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
