package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.view.CDynamicPageBase;

public class CPageServiceProjectItemStatus extends CPageServiceDynamicPage<CProjectItemStatus> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectItemStatus.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectItemStatus(CDynamicPageBase view) {
		super(view);
	}

	@Override
	public void actionCreate() {
		// TODO Auto-generated method stub
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProjectItemStatus.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CProjectItemStatus.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
