package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.app.gannt.domain.CGanttItem;

/** CGanttTimelineBar - Visual timeline bar component for Gantt items. Displays colorful, responsive bars with progress indicators, task owners, and
 * hover effects. Bars are scaled proportionally to the timeline range and synchronized with the timeline header for proper alignment. */
public class CGanttTimelineBar extends Div {
	private static final long serialVersionUID = 1L;

	public CGanttTimelineBar(final CGanttItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		setWidth(totalWidth + "px");
		setHeight("26px");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("position", "relative");
		if (!item.hasDates() || (timelineStart == null) || (timelineEnd == null)) {
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
		bar.getStyle().set("position", "absolute");
		bar.getStyle().set("left", leftPx + "px");
		bar.getStyle().set("width", widthPx + "px");
		bar.getStyle().set("height", "100%");
		bar.getStyle().set("display", "flex");
		bar.getStyle().set("align-items", "center");
		bar.getStyle().set("justify-content", "center");
		bar.getStyle().set("border-left", "1px solid #000");
		bar.getStyle().set("border-bottom", "1px solid #000");
		final String backgroundColor = item.getColorCode();
		bar.getStyle().set("background-color", backgroundColor);
		bar.getStyle().set("color", CColorUtils.getContrastTextColor(backgroundColor));
		bar.getStyle().set("font-size", "12px");
		bar.getStyle().set("white-space", "nowrap");
		bar.getStyle().set("overflow", "hidden");
		bar.getStyle().set("text-overflow", "ellipsis");
		// Optional: Padding inside the bar
		bar.getStyle().set("padding", "0 4px");
		// Tooltip and label
		final int progress = item.getProgressPercentage();
		final String displayText = String.format("%s (%s) - %d%%", item.getEntity().getName(), item.getResponsibleName(), progress);
		bar.setText(displayText);
		// Progress overlay
		if ((progress > 0) && (progress < 100)) {
			final Div progressOverlay = new Div();
			progressOverlay.getStyle().set("position", "absolute");
			progressOverlay.getStyle().set("left", "0");
			progressOverlay.getStyle().set("top", "0");
			progressOverlay.getStyle().set("bottom", "0");
			progressOverlay.getStyle().set("width", progress + "%");
			progressOverlay.getStyle().set("background-color", "rgba(255,255,255,0.2)");
			progressOverlay.getStyle().set("pointer-events", "none");
			bar.getElement().insertChild(0, progressOverlay.getElement());
		}
		final String tooltip = String.format("%s\n%s\nProgress: %d%%\nDuration: %d days\nStart: %s\nEnd: %s", item.getEntity().getName(),
				item.getResponsibleName(), progress, item.getDurationDays(), itemStart, itemEnd);
		bar.getElement().setAttribute("title", tooltip);
		add(bar);
	}
}
