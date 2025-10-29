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
		setWidth(totalWidth + "px");
		setHeight("100%");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("position", "relative");
		addClassName("gantt-timeline-bar-container");
		if (!item.hasDates() || (timelineStart == null) || (timelineEnd == null)) {
			addClassName("no-dates");
			return;
		}
		final LocalDate itemStart = item.getStartDate();
		final LocalDate itemEnd = item.getEndDate();
		final long totalDays = ChronoUnit.DAYS.between(timelineStart, timelineEnd) + 1;
		if (totalDays <= 0) {
			return;
		}
		final long startOffset = ChronoUnit.DAYS.between(timelineStart, itemStart);
		final long itemDuration = ChronoUnit.DAYS.between(itemStart, itemEnd) + 1;
		final int leftPx = (int) ((startOffset * totalWidth) / (double) totalDays);
		final int widthPx = (int) ((itemDuration * totalWidth) / (double) totalDays);
		final Div bar = new Div();
		bar.addClassName("gantt-timeline-bar");
		bar.getStyle().set("position", "absolute");
		bar.getStyle().set("left", leftPx + "px");
		bar.getStyle().set("width", widthPx + "px");
		bar.getStyle().set("height", "20px");
		bar.getStyle().set("top", "50%");
		bar.getStyle().set("transform", "translateY(-50%)");
		final String color = item.getColorCode();
		bar.getStyle().set("background-color", color);
		final Div startMarker = new Div();
		startMarker.addClassName("gantt-bar-start-marker");
		bar.add(startMarker);
		final Div endMarker = new Div();
		endMarker.addClassName("gantt-bar-end-marker");
		bar.add(endMarker);
		final Span taskInfo = new Span();
		taskInfo.addClassName("gantt-task-info");
		final int progress = item.getProgressPercentage();
		final String displayText = String.format("%s (%s) - %d%%", item.getEntity().getName(), item.getResponsibleName(), progress);
		taskInfo.setText(displayText);
		bar.add(taskInfo);
		if ((progress > 0) && (progress < 100)) {
			final Div progressOverlay = new Div();
			progressOverlay.addClassName("gantt-progress-overlay");
			progressOverlay.getStyle().set("width", progress + "%");
			bar.getElement().insertChild(0, progressOverlay.getElement());
		}
		final String tooltip = String.format("%s\n%s\nProgress: %d%%\nDuration: %d days\nStart: %s\nEnd: %s", item.getEntity().getName(),
				item.getResponsibleName(), progress, item.getDurationDays(), itemStart, itemEnd);
		bar.getElement().setAttribute("title", tooltip);
		bar.addClassName("hoverable");
		add(bar);
	}
}
