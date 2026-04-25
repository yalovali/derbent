package tech.derbent.plm.sprints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CComponentListSprintItems;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.view.CComponentWidgetSprint;

/** CPageServiceSprint - Page service for Sprint management UI. Handles UI events and interactions for sprint views. */
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint>
		implements IComponentWidgetEntityProvider<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprint.class);

	private final CActivityService activityService;
	private CComponentListSprintItems componentItemsSelection;
	private final CMeetingService meetingService;
	private final CProjectItemStatusService statusService;
	private final CSprintItemService sprintItemService;

	public CPageServiceSprint(final IPageServiceImplementer<CSprint> view) {
		super(view);
		// Keep dependencies resolved once (thread-safe services); no per-user state is stored.
		statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		sprintItemService = CSpringContext.getBean(CSprintItemService.class);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CSprint");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CSprint> gridView = (CGridViewBaseDBEntity<CSprint>) getView();
			gridView.generateGridReport();
			return;
		}
		super.actionReport();
	}

	public CComponentListSprintItems createSpritActivitiesComponent() {
		if (componentItemsSelection == null) {
			componentItemsSelection = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
			// Legacy sprint item list remains available in the Sprint detail view.
			componentItemsSelection.drag_setDragEnabled(true);
			componentItemsSelection.drag_setDropEnabled(true);
			registerComponent(componentItemsSelection.getComponentName(), componentItemsSelection);
		}
		return componentItemsSelection;
	}


	/** Creates a widget component for displaying the given sprint entity.
	 * @param item the sprint to create a widget for
	 * @return the CComponentWidgetSprint component */
	@Override
	public Component buildDataProviderComponentWidget(final CSprint item) {
		return new CComponentWidgetSprint(item);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return statusService;
	}

	@Override
	public void populateForm() {
		LOGGER.debug("populateForm called - sprint item list receives entity updates via IContentOwner interface");
	}
}
