package tech.derbent.app.gannt.projectgannt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;
import tech.derbent.app.meetings.service.CMeetingService;

public class CPageServiceProjectGannt extends CPageServiceDynamicPage<CGanntViewEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectGannt.class);
	protected CProjectItemStatusService projectItemStatusService;

	public CPageServiceProjectGannt(final IPageServiceImplementer<CGanntViewEntity> view, final CActivityService activityService,
			final CMeetingService meetingService) {
		super(view);
		// public CPageServiceActivity(final IPageServiceImplementer<CActivity> view) {
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionChangeStatus(final CProjectItemStatus newStatus) {}

	@Override
	public void actionCreate() throws Exception {}

	@Override
	public void actionDelete() throws Exception {}

	@Override
	@SuppressWarnings ({})
	public void actionRefresh() throws Exception {}

	@Override
	@SuppressWarnings ({})
	public void actionSave() throws Exception {}
}
