package tech.derbent.app.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.view.CComponentWidgetActivity;

public class CPageServiceActivity extends CPageServiceDynamicPage<CActivity>
		implements IPageServiceHasStatusAndWorkflow<CActivity>, IComponentWidgetEntityProvider<CActivity>, ISprintItemPageService<CActivity> {

	public Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivity.class);
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;
	Long serialVersionUID = 1L;

	public CPageServiceActivity(final IPageServiceImplementer<CActivity> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}
	// NOTE: createAttachmentsComponent() method removed
	// Attachments are now created automatically via CAttachmentComponentFactory
	// Referenced in CActivity's @OneToMany field @AMetaData:
	// createComponentMethodBean = "CAttachmentComponentFactory"
	// createComponentMethod = "createComponent"

	/** Handle report action - generates CSV report from grid data.
	 * @throws Exception if report generation fails */
	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CActivity");
		// Check if view supports grid reporting
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CActivity> gridView = (CGridViewBaseDBEntity<CActivity>) getView();
			gridView.generateGridReport();
		} else {
			// Fallback to parent implementation (shows warning)
			super.actionReport();
		}
	}

	/** Creates a widget component for displaying the given activity entity.
	 * @param entity the activity to create a widget for
	 * @return the CActivityWidget component */
	@Override
	public CComponentWidgetEntity<CActivity> getComponentWidget(final CActivity entity) {
		return new CComponentWidgetActivity(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }

	/** Creates a widget component for displaying the activity as a sprint item.
	 * @param entity the activity to create a sprint item widget for
	 * @return the CActivityWidget component */
	@Override
	public Component getSprintItemWidget(final CActivity entity) {
		return new CComponentWidgetActivity(entity);
	}

	public void on_description_blur(final Component component, final Object value) {
		LOGGER.info("function: on_description_blur for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_description_focus(final Component component, final Object value) {
		LOGGER.info("function: on_description_focus for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_name_change(final Component component, final Object value) {
		LOGGER.info("function: on_name_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_status_change(final Component component, final Object value) {
		LOGGER.info("function: on_status_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}
}
