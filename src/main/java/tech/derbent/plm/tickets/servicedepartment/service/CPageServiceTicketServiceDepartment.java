package tech.derbent.plm.tickets.servicedepartment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.tickets.servicedepartment.domain.CTicketServiceDepartment;

/**
 * CPageServiceTicketServiceDepartment - Page service for service department management views.
 * Provides UI page initialization and configuration for service departments.
 */
public class CPageServiceTicketServiceDepartment extends CPageServiceDynamicPage<CTicketServiceDepartment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceTicketServiceDepartment.class);
    public CPageServiceTicketServiceDepartment(final IPageServiceImplementer<CTicketServiceDepartment> view) {
        super(view);
    }

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CTicketServiceDepartment");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CTicketServiceDepartment> gridView = (CGridViewBaseDBEntity<CTicketServiceDepartment>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
