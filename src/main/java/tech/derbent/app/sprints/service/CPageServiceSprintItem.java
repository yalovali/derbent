package tech.derbent.app.sprints.service;

import com.vaadin.flow.component.Component;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.view.CComponentWidgetSprintItem;

/** CPageServiceSprintItem - Page service for SprintItem management UI. Handles UI events and interactions for sprint item views. */
public class CPageServiceSprintItem extends CPageServiceDynamicPage<CSprintItem> implements IComponentWidgetEntityProvider<CSprintItem> {

	public CPageServiceSprintItem(final IPageServiceImplementer<CSprintItem> view) {
		super(view);
	}

	/** Creates a widget component for displaying the given sprint item entity.
	 * @param entity the sprint item to create a widget for
	 * @return the CComponentWidgetSprintItem component */
	@Override
	public Component getComponentWidget(final CSprintItem entity) {
		return new CComponentWidgetSprintItem(entity);
	}
}
