package tech.derbent.plm.tickets.ticketpriority.service;

import org.springframework.stereotype.Service;
import tech.derbent.api.page.service.CPageService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;

/**
 * CPageServiceTicketPriority - Page service for ticket priority management views.
 * Provides UI page initialization and configuration for ticket priorities.
 */
@Service
public class CPageServiceTicketPriority extends CPageService {

	private final CTicketPriorityService ticketPriorityService;

	public CPageServiceTicketPriority(final CTicketPriorityService ticketPriorityService) {
		this.ticketPriorityService = ticketPriorityService;
	}

	@Override
	public Class<?> getEntityClass() { return CTicketPriority.class; }

	@Override
	public void initializeProject(final CProject<?> project, final boolean minimal) throws Exception {
		CTicketPriorityInitializerService.initializeSample(project, minimal);
	}
}
