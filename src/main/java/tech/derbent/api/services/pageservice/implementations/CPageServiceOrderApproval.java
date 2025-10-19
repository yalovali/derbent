package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.utils.Check;
import tech.derbent.orders.domain.COrderApproval;
import tech.derbent.page.view.CDynamicPageBase;

public class CPageServiceOrderApproval extends CPageServiceDynamicPage<COrderApproval> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceOrderApproval.class);
	Long serialVersionUID = 1L;

	public CPageServiceOrderApproval(CDynamicPageBase view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), COrderApproval.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					COrderApproval.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
