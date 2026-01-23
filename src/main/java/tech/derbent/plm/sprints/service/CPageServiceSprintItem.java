package tech.derbent.plm.sprints.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.view.CComponentWidgetSprintItem;

/** CPageServiceSprintItem - Page service for SprintItem management UI. Handles UI events and interactions for sprint item views. */
public class CPageServiceSprintItem extends CPageServiceDynamicPage<CSprintItem> implements IComponentWidgetEntityProvider<CSprintItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprintItem.class);

	public CPageServiceSprintItem(final IPageServiceImplementer<CSprintItem> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CSprintItem");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CSprintItem> gridView = (CGridViewBaseDBEntity<CSprintItem>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	/** Creates a widget component for displaying the given sprint item entity.
	 * @param entity the sprint item to create a widget for
	 * @return the CComponentWidgetSprintItem component */
	@Override
	public Component getComponentWidget(final CSprintItem entity) {
		return new CComponentWidgetSprintItem(entity);
	}
}
