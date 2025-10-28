package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import tech.derbent.app.gannt.domain.CGanttItem;

/** CGanttTimelineBar - Visual timeline bar component for Gantt items. Displays colorful, responsive bars with progress indicators, task owners, and
 * hover effects. Bars are scaled proportionally to the timeline range and synchronized with the timeline header for proper alignment. */
public class CGanttTimelineBar extends Div {

	private static final long serialVersionUID = 1L;

	public CGanttTimelineBar(final CGanttItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		addClassName("gantt-timeline-bar-container");
		if (!item.hasDates() || (timelineStart == null) || (timelineEnd == null)) {
			// No dates available, show empty placeholder
			addClassName("no-dates");
			return;
		}
		final LocalDate itemStart = item.getStartDate();
		final LocalDate itemEnd = item.getEndDate();
		// Calculate total timeline duration in days
		final long totalDays = ChronoUnit.DAYS.between(timelineStart, timelineEnd);
		if (totalDays <= 0) {
			return;
		}
		// Calculate item position and width relative to timeline
		final long startOffset = ChronoUnit.DAYS.between(timelineStart, itemStart);
		final long itemDuration = ChronoUnit.DAYS.between(itemStart, itemEnd) + 1;
		// Calculate percentages for positioning
		final double leftPercent = (startOffset * 100.0) / totalDays;
		final double widthPercent = (itemDuration * 100.0) / totalDays;
		// Create the timeline bar with enhanced Gantt chart styling
		final Div bar = new Div();
		bar.addClassName("gantt-timeline-bar");
		// Apply color from entity
		final String color = item.getColorCode();
		bar.getStyle().set("background-color", color);
		bar.getStyle().set("left", String.format("%.2f%%", leftPercent));
		bar.getStyle().set("width", String.format("%.2f%%", widthPercent));
		// Add start and end markers for better visual distinction
		final Div startMarker = new Div();
		startMarker.addClassName("gantt-bar-start-marker");
		bar.add(startMarker);
		final Div endMarker = new Div();
		endMarker.addClassName("gantt-bar-end-marker");
		bar.add(endMarker);
		// Add task information to the bar
		final Span taskInfo = new Span();
		taskInfo.addClassName("gantt-task-info");
		// Show task name, owner, and progress
		final int progress = item.getProgressPercentage();
		final String displayText = String.format("%s (%s) - %d%%", item.getEntity().getName(), item.getResponsibleName(), progress);
		taskInfo.setText(displayText);
		bar.add(taskInfo);
		// Add progress indicator overlay
		if ((progress > 0) && (progress < 100)) {
			final Div progressOverlay = new Div();
			progressOverlay.addClassName("gantt-progress-overlay");
			progressOverlay.getStyle().set("width", progress + "%");
			bar.getElement().insertChild(0, progressOverlay.getElement());
		}
		// Add tooltip with detailed information
		final String tooltip = String.format("%s\n%s\nProgress: %d%%\nDuration: %d days\nStart: %s\nEnd: %s", item.getEntity().getName(),
				item.getResponsibleName(), progress, item.getDurationDays(), itemStart, itemEnd);
		bar.getElement().setAttribute("title", tooltip);
		// Add hover effect class
		bar.addClassName("hoverable");
		add(bar);
	}
}
