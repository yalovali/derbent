package tech.derbent.app.activities.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import tech.derbent.api.entity.domain.CEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.base.users.domain.CUser;

/** CActivityWidget - Custom widget for displaying CActivity entities in a rich visual format.
 * <p>
 * This widget displays activity information including: - Activity name and description - Status with color coding - Start and due dates - Progress
 * percentage with visual bar - Priority indicator - Assigned user - Estimated hours
 * </p>
 * <p>
 * The widget provides static value providers for use in grids and other contexts.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntity
 * @see CActivity */
public class CComponentWidgetActivity extends CComponentWidgetEntity<CActivity> {

	private static final long serialVersionUID = 1L;

	// =============== STATIC FACTORY METHODS ===============

	/** Creates a widget for the specified activity.
	 * @param activity the activity to create a widget for
	 * @return the widget component */
	public static Component create(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null when creating widget");
		return new CComponentWidgetActivity(activity);
	}

	/** Returns a value provider that creates activity widgets.
	 * @return a value provider for grid columns */
	public static ValueProvider<CActivity, Component> widgetProvider() {
		return activity -> create(activity);
	}

	// =============== ACTIVITY-SPECIFIC VALUE PROVIDERS ===============

	/** Returns a value provider for activity status display text.
	 * @return a value provider for status names */
	public static ValueProvider<CActivity, String> statusNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getStatus() == null)) {
				return "";
			}
			return activity.getStatus().getName();
		};
	}

	/** Returns a value provider for activity status with color.
	 * @return a value provider that returns a styled status span */
	public static ValueProvider<CActivity, Component> statusBadgeProvider() {
		return activity -> createStatusBadge(activity != null ? activity.getStatus() : null);
	}

	/** Returns a value provider for activity priority display text.
	 * @return a value provider for priority names */
	public static ValueProvider<CActivity, String> priorityNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getPriority() == null)) {
				return "";
			}
			return activity.getPriority().getName();
		};
	}

	/** Returns a value provider for activity priority with color.
	 * @return a value provider that returns a styled priority span */
	public static ValueProvider<CActivity, Component> priorityBadgeProvider() {
		return activity -> createStatusBadge(activity != null ? activity.getPriority() : null);
	}

	/** Returns a value provider for activity type display text.
	 * @return a value provider for type names */
	public static ValueProvider<CActivity, String> typeNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getEntityType() == null)) {
				return "";
			}
			return activity.getEntityType().getName();
		};
	}

	/** Returns a value provider for start date.
	 * @return a value provider for formatted start dates */
	public static ValueProvider<CActivity, String> startDateProvider() {
		return dateValueProvider(CActivity::getStartDate);
	}

	/** Returns a value provider for due date.
	 * @return a value provider for formatted due dates */
	public static ValueProvider<CActivity, String> dueDateProvider() {
		return dateValueProvider(CActivity::getDueDate);
	}

	/** Returns a value provider for progress percentage text.
	 * @return a value provider for progress text */
	public static ValueProvider<CActivity, String> progressTextProvider() {
		return progressValueProvider(CActivity::getProgressPercentage);
	}

	/** Returns a value provider for progress bar component.
	 * @return a value provider that returns a progress bar */
	public static ValueProvider<CActivity, Component> progressBarProvider() {
		return activity -> createProgressBar(activity);
	}

	/** Returns a value provider for assigned user name.
	 * @return a value provider for user names */
	public static ValueProvider<CActivity, String> assignedToNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getAssignedTo() == null)) {
				return "";
			}
			return activity.getAssignedTo().getName();
		};
	}

	/** Returns a value provider for estimated hours.
	 * @return a value provider for hours formatted as string */
	public static ValueProvider<CActivity, String> estimatedHoursProvider() {
		return hoursValueProvider(CActivity::getEstimatedHours);
	}

	/** Returns a value provider for actual hours.
	 * @return a value provider for hours formatted as string */
	public static ValueProvider<CActivity, String> actualHoursProvider() {
		return hoursValueProvider(CActivity::getActualHours);
	}

	/** Returns a value provider for remaining hours.
	 * @return a value provider for hours formatted as string */
	public static ValueProvider<CActivity, String> remainingHoursProvider() {
		return hoursValueProvider(CActivity::getRemainingHours);
	}

	// =============== HELPER METHODS FOR UI COMPONENTS ===============

	/** Creates a styled status badge component.
	 * @param statusEntity the status entity (can be null)
	 * @return a styled span component */
	private static Component createStatusBadge(final Object statusEntity) {
		if (statusEntity == null) {
			return new Span("");
		}
		try {
			final String displayText = CColorUtils.getDisplayTextFromEntity(statusEntity);
			String color = null;
			if (statusEntity instanceof CEntity<?>) {
				color = CColorUtils.getColorFromEntity((CEntity<?>) statusEntity);
			}
			final Span statusSpan = new Span(displayText);
			statusSpan.addClassNames(FontSize.XSMALL);
			statusSpan.getStyle().set("padding", "2px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			if ((color != null) && !color.isBlank()) {
				statusSpan.getStyle().set("background-color", color);
				statusSpan.getStyle().set("color", CColorUtils.getContrastTextColor(color));
			} else {
				statusSpan.getStyle().set("background-color", "#e9ecef");
				statusSpan.getStyle().set("color", "#495057");
			}
			return statusSpan;
		} catch (final Exception e) {
			return new Span("");
		}
	}

	/** Creates a progress bar component.
	 * @param activity the activity
	 * @return a progress bar component */
	private static Component createProgressBar(final CActivity activity) {
		final CDiv container = new CDiv();
		container.getStyle().set("width", "100px");
		container.getStyle().set("display", "flex");
		container.getStyle().set("align-items", "center");
		container.getStyle().set("gap", "4px");
		final int progress = (activity != null) && (activity.getProgressPercentage() != null) ? activity.getProgressPercentage() : 0;
		final ProgressBar progressBar = new ProgressBar();
		progressBar.setMin(0);
		progressBar.setMax(100);
		progressBar.setValue(progress);
		progressBar.setWidth("70px");
		progressBar.setHeight("8px");
		// Set color based on progress
		if (progress >= 100) {
			progressBar.getStyle().set("--lumo-primary-color", "#28a745"); // Green for complete
		} else if (progress >= 50) {
			progressBar.getStyle().set("--lumo-primary-color", "#ffc107"); // Yellow for in progress
		} else {
			progressBar.getStyle().set("--lumo-primary-color", "#17a2b8"); // Blue for started
		}
		final Span percentSpan = new Span(progress + "%");
		percentSpan.addClassNames(FontSize.XSMALL);
		container.add(progressBar, percentSpan);
		return container;
	}

	// =============== INSTANCE CONSTRUCTOR AND METHODS ===============

	/** Creates a new activity widget for the specified activity.
	 * @param activity the activity to display */
	public CComponentWidgetActivity(final CActivity activity) {
		super(activity);
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
		final CActivityPriority priority = activity.getPriority();
		if (priority != null) {
			addStatusBadge(priority, "Priority");
		}
		// Activity type badge
		final CActivityType entityType = (CActivityType) activity.getEntityType();
		if (entityType != null) {
			addStatusBadge(entityType, "Type");
		}
		// Progress with visual bar
		addProgressRow(activity.getProgressPercentage());
		// Start date
		addDateRow("Start", activity.getStartDate(), VaadinIcon.CALENDAR);
		// Due date with overdue indicator
		addDueDateRow(activity);
		// Time tracking summary
		addTimeTrackingRow(activity);
		// Assigned user
		final CUser assignedTo = activity.getAssignedTo();
		if (assignedTo != null) {
			addUserRow(assignedTo, "Assigned to");
		}
	}

	/** Adds a progress row with visual progress bar.
	 * @param progress the progress percentage (0-100) */
	protected void addProgressRow(final Integer progress) {
		if ((progress == null) || (progress == 0)) {
			return;
		}
		final CDiv row = createInfoRow();
		final Icon icon = new Icon(VaadinIcon.PROGRESSBAR);
		icon.setSize("14px");
		icon.addClassName(TextColor.SECONDARY);
		row.add(icon);
		// Mini progress bar
		final ProgressBar progressBar = new ProgressBar();
		progressBar.setMin(0);
		progressBar.setMax(100);
		progressBar.setValue(progress);
		progressBar.setWidth("50px");
		progressBar.setHeight("6px");
		// Set color based on progress
		if (progress >= 100) {
			progressBar.getStyle().set("--lumo-primary-color", "#28a745"); // Green
		} else if (progress >= 50) {
			progressBar.getStyle().set("--lumo-primary-color", "#ffc107"); // Yellow
		} else {
			progressBar.getStyle().set("--lumo-primary-color", "#17a2b8"); // Blue
		}
		row.add(progressBar);
		final Span percentSpan = new Span(progress + "%");
		percentSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
		row.add(percentSpan);
		layoutSecondary.add(row);
	}

	/** Adds a due date row with overdue indicator.
	 * @param activity the activity */
	protected void addDueDateRow(final CActivity activity) {
		final LocalDate dueDate = activity.getDueDate();
		if (dueDate == null) {
			return;
		}
		final CDiv row = createInfoRow();
		final Icon dateIcon = new Icon(VaadinIcon.CALENDAR_CLOCK);
		dateIcon.setSize("14px");
		// Check if overdue
		final boolean isOverdue = activity.isOverdue();
		if (isOverdue) {
			dateIcon.getStyle().set("color", "#dc3545"); // Red for overdue
		} else {
			dateIcon.addClassName(TextColor.SECONDARY);
		}
		row.add(dateIcon);
		final Span labelSpan = new Span("Due: ");
		labelSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
		final Span dateSpan = new Span(dueDate.format(DATE_FORMATTER));
		dateSpan.addClassNames(FontSize.XSMALL);
		if (isOverdue) {
			dateSpan.getStyle().set("color", "#dc3545");
			dateSpan.getStyle().set("font-weight", "bold");
		}
		row.add(labelSpan, dateSpan);
		// Add overdue badge if applicable
		if (isOverdue) {
			final Span overdueSpan = new Span("OVERDUE");
			overdueSpan.addClassNames(FontSize.XSMALL);
			overdueSpan.getStyle().set("background-color", "#dc3545");
			overdueSpan.getStyle().set("color", "#ffffff");
			overdueSpan.getStyle().set("padding", "1px 6px");
			overdueSpan.getStyle().set("border-radius", "4px");
			overdueSpan.getStyle().set("margin-left", "4px");
			row.add(overdueSpan);
		}
		layoutSecondary.add(row);
	}

	/** Adds a time tracking summary row.
	 * @param activity the activity */
	protected void addTimeTrackingRow(final CActivity activity) {
		final BigDecimal estimated = activity.getEstimatedHours();
		final BigDecimal actual = activity.getActualHours();
		// Only show if there's meaningful data
		if (((estimated == null) || (estimated.compareTo(BigDecimal.ZERO) == 0))
				&& ((actual == null) || (actual.compareTo(BigDecimal.ZERO) == 0))) {
			return;
		}
		final CDiv row = createInfoRow();
		final Icon clockIcon = new Icon(VaadinIcon.CLOCK);
		clockIcon.setSize("14px");
		clockIcon.addClassName(TextColor.SECONDARY);
		row.add(clockIcon);
		final StringBuilder timeText = new StringBuilder();
		if ((actual != null) && (actual.compareTo(BigDecimal.ZERO) > 0)) {
			timeText.append(actual).append("h spent");
		}
		if ((estimated != null) && (estimated.compareTo(BigDecimal.ZERO) > 0)) {
			if (timeText.length() > 0) {
				timeText.append(" / ");
			}
			timeText.append(estimated).append("h est");
		}
		final Span timeSpan = new Span(timeText.toString());
		timeSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
		row.add(timeSpan);
		layoutSecondary.add(row);
	}

	@Override
	protected String getEntityDescription() { return getEntity().getDescription(); }
}
