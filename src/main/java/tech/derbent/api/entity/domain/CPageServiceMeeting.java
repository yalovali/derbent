package tech.derbent.api.entity.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.view.CComponentWidgetMeeting;

public class CPageServiceMeeting extends CPageServiceDynamicPage<CMeeting>
		implements IPageServiceHasStatusAndWorkflow<CMeeting>, IComponentWidgetEntityProvider<CMeeting>, ISprintItemPageService<CMeeting> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMeeting.class);
	Long serialVersionUID = 1L;
	// Declare the field required by the interface
	private CProjectItemStatusService statusService;

	public CPageServiceMeeting(IPageServiceImplementer<CMeeting> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CMeeting");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CMeeting> gridView = (CGridViewBaseDBEntity<CMeeting>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	public void createNewInstance() { /*****/
	}

	/** Creates a widget component for displaying the given meeting entity.
	 * @param entity the meeting to create a widget for
	 * @return the CComponentWidgetMeeting component */
	@Override
	public Component getComponentWidget(final CMeeting entity) {
		return new CComponentWidgetMeeting(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	/** Creates a widget component for displaying the meeting as a sprint item.
	 * @param entity the meeting to create a sprint item widget for
	 * @return the CComponentWidgetMeeting component */
	@Override
	public Component getSprintItemWidget(final CMeeting entity) {
		return new CComponentWidgetMeeting(entity);
	}
}
