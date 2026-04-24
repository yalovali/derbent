package tech.derbent.plm.agile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.parentrelation.service.CHierarchyPageSupport;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.agile.view.CComponentWidgetUserStory;

/**
 * Dynamic page service for user stories.
 *
 * <p>The page delegates report/export behavior to the base dynamic page service and focuses on
 * widget rendering plus the generic hierarchy placeholders used in the detail screen.</p>
 */
public class CPageServiceUserStory extends CPageServiceDynamicPage<CUserStory>
		implements IComponentWidgetEntityProvider<CUserStory>, ISprintItemPageService<CUserStory> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceUserStory.class);
	private CComponentAgileChildren componentAgileChildren;
	private CProjectItemStatusService statusService;

	public CPageServiceUserStory(final IPageServiceImplementer<CUserStory> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated reason={}", e.getMessage());
		}
	}

	@Override
	public CComponentWidgetEntity<CUserStory> buildDataProviderComponentWidget(final CUserStory entity) {
		return new CComponentWidgetUserStory(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	@Override
	public Component getSprintItemWidget(final CUserStory entity) {
		return new CComponentWidgetUserStory(entity);
	}

	public Component createComponentParentChildren() {
		if (componentAgileChildren == null) {
			componentAgileChildren = (CComponentAgileChildren) CHierarchyPageSupport.createChildrenComponent();
		}
		return componentAgileChildren;
	}

	public Component createComponentParent() {
		// User stories share the same hierarchy component contract as requirements and other project items.
		return CHierarchyPageSupport.createParentComponent();
	}
}
