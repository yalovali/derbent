package tech.derbent.plm.tickets.servicedepartment.service;

import org.springframework.stereotype.Service;
import tech.derbent.api.page.service.CPageService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.tickets.servicedepartment.domain.CTicketServiceDepartment;

/**
 * CPageServiceTicketServiceDepartment - Page service for service department management views.
 * Provides UI page initialization and configuration for service departments.
 */
@Service
public class CPageServiceTicketServiceDepartment extends CPageService {

	private final CTicketServiceDepartmentService serviceDepartmentService;

	public CPageServiceTicketServiceDepartment(final CTicketServiceDepartmentService serviceDepartmentService) {
		this.serviceDepartmentService = serviceDepartmentService;
	}

	@Override
	public Class<?> getEntityClass() { return CTicketServiceDepartment.class; }

	@Override
	public void initializeProject(final CProject<?> project, final boolean minimal) throws Exception {
		CTicketServiceDepartmentInitializerService.initializeSample(project.getCompany(), minimal);
	}
}
