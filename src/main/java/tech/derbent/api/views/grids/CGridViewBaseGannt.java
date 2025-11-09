package tech.derbent.api.views.grids;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.views.CProjectAwareMDPage;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanttItem;
import tech.derbent.app.gannt.view.CMasterViewSectionGannt;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CGridViewBaseGannt.class);
	private static final long serialVersionUID = 1L;
	protected final CActivityService activityService;
	protected final CMeetingService meetingService;
	protected final CPageEntityService pageEntityService;

	protected CGridViewBaseGannt(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService, final CActivityService activityService,
			final CMeetingService meetingService, final CPageEntityService pageEntityService) throws Exception {
		super(entityClass, entityService, sessionService, screenService);
		this.activityService = activityService;
		this.meetingService = meetingService;
		this.pageEntityService = pageEntityService;
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// No detail form for Gantt view - CGanttItem is read-only DTO
	}

	// override this to create a Gannt chart
	@Override
	protected void createMasterComponent() throws Exception {
		// Pass required dependencies to CMasterViewSectionGannt constructor with page entity service for navigation
		masterViewSection =
				new CMasterViewSectionGannt<EntityClass>(entityClass, this, sessionService, activityService, meetingService, pageEntityService);
	}

	/** Override to handle CGanttItem selection - it's a DTO wrapper, not the actual entity. Selection is logged but no form editing occurs since
	 * CGanttItem is read-only. */
	@Override
	protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
		final EntityClass value = event.getSelectedItem();
		// Check if selected item is CGanttItem (DTO wrapper)
		if (value != null && value.getClass().equals(CGanttItem.class)) {
			final CGanttItem ganttItem = (CGanttItem) ((Object) value);
			LOGGER.debug("Gantt item selected: {} (Entity Type: {}, Entity ID: {})", ganttItem.getEntity().getName(), ganttItem.getEntityType(),
					ganttItem.getEntityId());
			// CGanttItem is a read-only DTO - no form population or editing
			// Future: Could navigate to actual entity page based on entityType and entityId
			return;
		}
		// Standard entity selection handling for non-DTO items
		super.onSelectionChanged(event);
	}
}
