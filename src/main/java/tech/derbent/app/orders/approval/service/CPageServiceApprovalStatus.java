package tech.derbent.app.orders.approval.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.orders.approval.domain.CApprovalStatus;

public class CPageServiceApprovalStatus extends CPageServiceDynamicPage<CApprovalStatus> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceApprovalStatus.class);
	Long serialVersionUID = 1L;

	public CPageServiceApprovalStatus(final IPageServiceImplementer<CApprovalStatus> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CApprovalStatus.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CApprovalStatus.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
