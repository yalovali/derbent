package tech.derbent.api.views.grids;

import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.views.CProjectAwareMDPage;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.view.CMasterViewSectionGannt;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;
	protected final CActivityService activityService;
	protected final CMeetingService meetingService;
	protected final CPageEntityService pageEntityService;

	protected CGridViewBaseGannt(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService, final CActivityService activityService,
			final CMeetingService meetingService, final CPageEntityService pageEntityService) {
		super(entityClass, entityService, sessionService, screenService);
		this.activityService = activityService;
		this.meetingService = meetingService;
		this.pageEntityService = pageEntityService;
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	// override this to create a Gannt chart
	@Override
	protected void createMasterComponent() {
		// Pass required dependencies to CMasterViewSectionGannt constructor with page entity service for navigation
		masterViewSection =
				new CMasterViewSectionGannt<EntityClass>(entityClass, this, sessionService, activityService, meetingService, pageEntityService);
	}
}
