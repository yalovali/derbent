package tech.derbent.plm.tickets.servicedepartment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
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
    public void bind() {
        try {
            LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTicketServiceDepartment.class.getSimpleName());
            Check.notNull(getView(), "View must not be null to bind page service.");
            super.bind();
        } catch (final Exception e) {
            LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CTicketServiceDepartment.class.getSimpleName(),
                    e.getMessage());
            throw e;
        }
    }
}
