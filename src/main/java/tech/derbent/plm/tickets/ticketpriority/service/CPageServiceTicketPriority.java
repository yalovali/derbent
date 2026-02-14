package tech.derbent.plm.tickets.ticketpriority.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;

/** CPageServiceTicketPriority - Page service for ticket priority management views. Provides UI page initialization and configuration for ticket
 * priorities. */
public class CPageServiceTicketPriority extends CPageServiceDynamicPage<CTicketPriority> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceTicketPriority.class);
	public CPageServiceTicketPriority(final IPageServiceImplementer<CTicketPriority> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CTicketPriority");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CTicketPriority> gridView = (CGridViewBaseDBEntity<CTicketPriority>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
