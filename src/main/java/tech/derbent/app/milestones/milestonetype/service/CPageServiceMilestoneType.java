package tech.derbent.app.milestones.milestonetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.milestones.milestonetype.domain.CMilestoneType;

public class CPageServiceMilestoneType extends CPageServiceDynamicPage<CMilestoneType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMilestoneType.class);
	Long serialVersionUID = 1L;

	public CPageServiceMilestoneType(IPageServiceImplementer<CMilestoneType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CMilestoneType.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CMilestoneType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
