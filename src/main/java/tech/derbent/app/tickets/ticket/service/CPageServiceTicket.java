package tech.derbent.app.tickets.ticket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.tickets.ticket.domain.CTicket;

public class CPageServiceTicket extends CPageServiceDynamicPage<CTicket> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTicket.class);
	Long serialVersionUID = 1L;

	public CPageServiceTicket(IPageServiceImplementer<CTicket> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTicket.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CTicket.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
