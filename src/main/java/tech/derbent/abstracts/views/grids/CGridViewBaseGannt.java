package tech.derbent.abstracts.views.grids;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.gannt.view.CMasterViewSectionGannt;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;
	protected final CActivityService activityService;
	protected final CMeetingService meetingService;

	protected CGridViewBaseGannt(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final CSessionService sessionService, final CDetailSectionService screenService, final CActivityService activityService,
			final CMeetingService meetingService) {
		super(entityClass, entityService, sessionService, screenService);
		this.activityService = activityService;
		this.meetingService = meetingService;
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	// override this to create a Gannt chart
	@Override
	protected void createMasterComponent() {
		// Pass required dependencies to CMasterViewSectionGannt constructor
		// Use null services if not available - CMasterViewSectionGannt will handle gracefully
		masterViewSection = new CMasterViewSectionGannt<EntityClass>(entityClass, this, sessionService, activityService, meetingService);
	}
}
