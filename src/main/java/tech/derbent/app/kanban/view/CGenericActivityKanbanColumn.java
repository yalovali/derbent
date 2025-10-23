package tech.derbent.app.kanban.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.ui.CBaseKanbanColumn;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.view.CActivityCard;

/** CGenericActivityKanbanColumn - Generic Kanban column for activities using the base kanban classes. Layer: View (MVC) This implementation uses the
 * new generic kanban base classes to provide drag-and-drop functionality for activity cards. */
public class CGenericActivityKanbanColumn extends CBaseKanbanColumn<CActivity, CProjectItemStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGenericActivityKanbanColumn.class);
	private static final long serialVersionUID = 1L;

	/** Constructor for CGenericActivityKanbanColumn.
	 * @param status     the activity status this column represents
	 * @param activities the list of activities for this status */
	public CGenericActivityKanbanColumn(final CProjectItemStatus status, final List<CActivity> activities) {
		super(status, activities);
		LOGGER.debug("Created CGenericActivityKanbanColumn for status: {} with {} activities", status.getName(), activities.size());
	}

	@Override
	protected Component createEntityCard(final CActivity entity) {
		try {
			return new CActivityCard(entity);
		} catch (final Exception e) {
			LOGGER.error("Error creating activity card for: {}", entity.getName(), e);
			// Return a simple fallback component
			return new com.vaadin.flow.component.html.Div("Error loading activity: " + entity.getName());
		}
	}
}
