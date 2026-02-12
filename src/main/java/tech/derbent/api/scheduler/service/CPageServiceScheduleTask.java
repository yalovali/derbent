package tech.derbent.api.scheduler.service;

import org.springframework.security.access.prepost.PreAuthorize;
import tech.derbent.api.scheduler.domain.CScheduleTask;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

@PreAuthorize ("isAuthenticated()")
public class CPageServiceScheduleTask extends CPageServiceDynamicPage<CScheduleTask> {

	public CPageServiceScheduleTask(final IPageServiceImplementer<CScheduleTask> view) {
		super(view);
	}
}
