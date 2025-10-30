package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.app.gannt.domain.CGanttItem;

/** CGanttTimelineBar - Visual timeline bar component for Gantt items. Displays colorful, responsive bars with progress indicators, task owners, and
 * hover effects. Bars are scaled proportionally to the timeline range and synchronized with the timeline header for proper alignment. */
public class CGanttTimelineBar extends CHorizontalLayout {

	private static final long serialVersionUID = 1L;

	public CGanttTimelineBar(final CGanttItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		setWidth(totalWidth + "px");
		setHeight("100%");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("position", "relative");
		getStyle().set("flex-shrink", "0");
		if (!item.hasDates() || (timelineStart == null) || (timelineEnd == null) || timelineStart.isAfter(timelineEnd)) {
			return;
		}
		createBar(item, timelineStart, timelineEnd, totalWidth);
	}

	private void createBar(final CGanttItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		final LocalDate itemStart = item.getStartDate();
		final LocalDate itemEnd = item.getEndDate();
		final long totalDays = ChronoUnit.DAYS.between(timelineStart, timelineEnd) + 1;
		if ((totalDays <= 0) || (itemStart == null) || (itemEnd == null)) {
			return;
		}
		final LocalDate visibleStart = itemStart.isBefore(timelineStart) ? timelineStart : itemStart;
		final LocalDate visibleEnd = itemEnd.isAfter(timelineEnd) ? timelineEnd : itemEnd;
		if (visibleEnd.isBefore(timelineStart) || visibleStart.isAfter(timelineEnd)) {
			return;
		}
		final Div bar = new Div();
		final long startOffset = ChronoUnit.DAYS.between(timelineStart, visibleStart);
		final long visibleDuration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
		final int leftPx = Math.max(0, (int) Math.round((startOffset * totalWidth) / (double) totalDays));
		int widthPx = (int) Math.round((visibleDuration * totalWidth) / (double) totalDays);
		widthPx = Math.max(widthPx, 2);
		if ((leftPx + widthPx) > totalWidth) {
			widthPx = Math.max(2, totalWidth - leftPx);
		}
		bar.getStyle().set("position", "absolute");
		bar.getStyle().set("left", leftPx + "px");
		bar.getStyle().set("width", widthPx + "px");
		formatBar(item, bar);
		final int progress = item.getProgressPercentage();
		createDisplayText(item, leftPx, widthPx, progress, totalWidth);
		formatProgress(bar, progress);
		createToolTip(item, visibleStart, visibleEnd, bar, progress);
		add(bar);
	}

	private void createDisplayText(final CGanttItem item, final int leftPx, final int widthPx, final int progress, final int totalWidth) {
		final String displayText = String.format("%s (%s) - %d%%", item.getEntity().getName(), item.getResponsibleName(), progress);
		final CDiv label = new CDiv(displayText);
		label.setText(displayText);
		label.getStyle().set("position", "absolute");
		final int safeLeft = Math.min(leftPx + 5, Math.max(0, totalWidth - widthPx));
		label.getStyle().set("left", safeLeft + "px");
		label.getStyle().set("top", "5px");
		label.getStyle().set("z-index", "1");
		label.getStyle().set("background", "transparent");
		label.getStyle().set("color", "#000"); // or use contrasting color
		label.getStyle().set("font-size", "12px");
		label.getStyle().set("white-space", "nowrap");
		label.getStyle().set("max-width", widthPx + "px");
		label.getStyle().set("overflow", "hidden");
		label.getStyle().set("text-overflow", "ellipsis");
		label.setSizeUndefined();
		add(label);
	}

	private void createToolTip(final CGanttItem item, final LocalDate itemStart, final LocalDate itemEnd, final Div bar, final int progress) {
		final String tooltip = String.format("%s\n%s\nProgress: %d%%\nDuration: %d days\nStart: %s\nEnd: %s", item.getEntity().getName(),
				item.getResponsibleName(), progress, item.getDurationDays(), itemStart, itemEnd);
		bar.getElement().setAttribute("title", tooltip);
	}

	private void formatBar(final CGanttItem item, final Div bar) {
		bar.getStyle().set("height", "90%");
		bar.getStyle().set("display", "flex");
		bar.getStyle().set("align-items", "center");
		bar.getStyle().set("justify-content", "center");
		bar.getStyle().set("border", "1px solid #000");
		final String backgroundColor = item.getColorCode();
		bar.getStyle().set("background-color", backgroundColor);
		bar.getStyle().set("color", CColorUtils.getContrastTextColor(backgroundColor));
		bar.getStyle().set("font-size", "12px");
		bar.getStyle().set("white-space", "nowrap");
		bar.getStyle().set("overflow", "hidden");
		bar.getStyle().set("text-overflow", "ellipsis");
		bar.getStyle().remove("white-space");
		bar.getStyle().remove("overflow");
		bar.getStyle().remove("text-overflow");
		bar.getStyle().set("border-radius", "4px");
		bar.setHeight("90%");
	}

	private void formatProgress(final Div bar, final int progress) {
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
	}
}
