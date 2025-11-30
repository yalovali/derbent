package tech.derbent.app.activities.view;

import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.app.activities.domain.CActivity;

/** CActivityWidget - Custom widget for displaying CActivity entities in a rich visual format.
 * <p>
 * This widget displays activity information including: - Activity name and description - Status with color coding - Start and due dates - Progress
 * percentage - Priority indicator - Assigned user - Story points
 * </p>
 * <p>
 * The widget follows the project's coding guidelines and integrates with the grid widget system.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntity
 * @see CActivity */
public class CComponentWidgetActivity extends CComponentWidgetEntity<CActivity> {

	private static final long serialVersionUID = 1L;

	/** Creates a new activity widget for the specified activity.
	 * @param activity the activity to display */
	public CComponentWidgetActivity(final CActivity activity) {
		super(activity);
		add("asdfaf");
		// Add default actions
		addEditAction();
		addDeleteAction();
	}

	@Override
	protected void buildSecondaryContent() {
		final CActivity activity = getEntity();
		// Status badge
		final CProjectItemStatus status = activity.getStatus();
		if (status != null) {
			addStatusBadge(status, "Status");
		}
		// Priority badge
		if (activity.getPriority() != null) {
			addStatusBadge(activity.getPriority(), "Priority");
		}
		// Activity type badge
		if (activity.getEntityType() != null) {
			addStatusBadge(activity.getEntityType(), "Type");
		}
		// Start date
		addDateRow("Start", activity.getStartDate(), VaadinIcon.CALENDAR);
		// Due date
		addDateRow("Due", activity.getDueDate(), VaadinIcon.CALENDAR_CLOCK);
		// Progress info
		final Integer progress = activity.getProgressPercentage();
		if ((progress != null) && (progress > 0)) {
			addInfoRow(VaadinIcon.PROGRESSBAR, progress + "% complete");
		}
		// Assigned user
		if (activity.getAssignedTo() != null) {
			addUserRow(activity.getAssignedTo(), "Assigned to");
		}
		// Estimated hours
		if ((activity.getEstimatedHours() != null) && (activity.getEstimatedHours().doubleValue() > 0)) {
			addInfoRow(VaadinIcon.CLOCK, activity.getEstimatedHours() + "h estimated");
		}
	}

	@Override
	protected String getEntityDescription() { return getEntity().getDescription(); }
}
