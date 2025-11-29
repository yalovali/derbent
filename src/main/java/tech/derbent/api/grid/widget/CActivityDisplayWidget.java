package tech.derbent.api.grid.widget;

import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entity.domain.CEntity;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.app.activities.domain.CActivity;

/**
 * Display widget for CActivity entities.
 * Shows activity information in a rich format with:
 * - Name and description on first row
 * - Creation date, responsible user icon, story points, and status below
 */
public class CActivityDisplayWidget extends CAbstractEntityDisplayWidget<CActivity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityDisplayWidget.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

	public CActivityDisplayWidget() {
		super(CActivity.class);
	}

	@Override
	protected Component createEntityWidget(final CActivity activity) {
		final CVerticalLayout widget = new CVerticalLayout(false, false, false);
		widget.addClassName("activity-display-widget");
		widget.getStyle().set("padding", "8px");
		widget.getStyle().set("gap", "4px");

		// First row: Name and Description
		widget.add(createHeaderRow(activity.getName(), activity.getDescription()));

		// Second row: Details (date, responsible, progress, status)
		widget.add(createDetailsRow(activity));

		return widget;
	}

	/**
	 * Creates the details row with date, responsible, progress, and status.
	 *
	 * @param activity the activity entity
	 * @return the details row component
	 */
	private Component createDetailsRow(final CActivity activity) {
		final CHorizontalLayout details = new CHorizontalLayout(false, false, false);
		details.addClassName("widget-details-row");
		details.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		details.getStyle().set("gap", "12px");
		details.getStyle().set("flex-wrap", "wrap");

		// Start date
		if (activity.getStartDate() != null) {
			details.add(createDetailRow(VaadinIcon.CALENDAR, activity.getStartDate().format(DATE_FORMATTER)));
		}

		// Due date
		if (activity.getDueDate() != null) {
			details.add(createDetailRow(VaadinIcon.CALENDAR_CLOCK, activity.getDueDate().format(DATE_FORMATTER)));
		}

		// Responsible user
		if (activity.getAssignedTo() != null) {
			final String assigneeName = activity.getAssignedTo().getName();
			details.add(createDetailRow(VaadinIcon.USER, assigneeName != null ? assigneeName : "Unassigned"));
		}

		// Progress percentage
		if (activity.getProgressPercentage() != null) {
			details.add(createProgressIndicator(activity.getProgressPercentage()));
		}

		// Priority badge
		if (activity.getPriority() != null) {
			final String priorityName = activity.getPriority().getName();
			final String priorityColor = safeGetColor(activity.getPriority());
			details.add(createStatusBadge(priorityName, priorityColor));
		}

		// Status badge
		if (activity.getStatus() != null) {
			final String statusName = activity.getStatus().getName();
			final String statusColor = safeGetColor(activity.getStatus());
			details.add(createStatusBadge(statusName, statusColor));
		}

		// Activity type badge
		if (activity.getEntityType() != null) {
			final String typeName = activity.getEntityType().getName();
			final String typeColor = safeGetColor(activity.getEntityType());
			details.add(createStatusBadge(typeName, typeColor));
		}

		return details;
	}

	/**
	 * Safely gets the color from an entity, returning a default color on error.
	 *
	 * @param entity the entity to get color from
	 * @return the color hex code or default gray
	 */
	private String safeGetColor(final CEntity<?> entity) {
		try {
			return CColorUtils.getColorFromEntity(entity);
		} catch (final Exception e) {
			LOGGER.debug("Could not get color from entity: {}", e.getMessage());
			return "#808080"; // Default gray
		}
	}
}
