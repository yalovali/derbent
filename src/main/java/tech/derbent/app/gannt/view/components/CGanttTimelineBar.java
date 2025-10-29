package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.components.CDiv;
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
		getStyle().remove("position");
		getStyle().set("flex-shrink", "0");
		// getStyle().set("padding-top", "2px");
		// getStyle().set("padding-bottom", "2px");
		// getStyle().set("position", "relative");
		if (!item.hasDates() || (timelineStart == null) || (timelineEnd == null)) {
			return;
		}
		createBar(item, timelineStart, timelineEnd, totalWidth);
	}

	private void createBar(final CGanttItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		final LocalDate itemStart = item.getStartDate();
		final LocalDate itemEnd = item.getEndDate();
		final long totalDays = ChronoUnit.DAYS.between(timelineStart, timelineEnd) + 1;
		if (totalDays <= 0) {
			return;
		}
		final Div bar = new Div();
		final long startOffset = ChronoUnit.DAYS.between(timelineStart, itemStart);
		final long itemDuration = ChronoUnit.DAYS.between(itemStart, itemEnd) + 1;
		final int leftPx = (int) ((startOffset * totalWidth) / (double) totalDays);
		final int widthPx = (int) ((itemDuration * totalWidth) / (double) totalDays);
		bar.getStyle().set("position", "absolute");
		bar.getStyle().set("left", leftPx + "px");
		bar.getStyle().set("width", widthPx + "px");
		formatBar(item, bar);
		final int progress = item.getProgressPercentage();
		createDisplayText(item, leftPx, progress);
		// final String displayText = String.format("%s (%s) - %d%%", item.getEntity().getName(), item.getResponsibleName(), progress);
		// bar.setText(displayText);
		formatProgress(bar, progress);
		createToolTip(item, itemStart, itemEnd, bar, progress);
		add(bar);
	}

	private void createDisplayText(final CGanttItem item, final int leftPx, final int progress) {
		final String displayText = String.format("%s (%s) - %d%%", item.getEntity().getName(), item.getResponsibleName(), progress);
		final CDiv label = new CDiv(displayText);
		label.setText(displayText);
		label.getStyle().set("position", "absolute");
		label.getStyle().set("left", leftPx + 5 + "px");
		label.getStyle().set("top", "5");
		label.getStyle().set("z-index", "1"); // on top of bar
		label.getStyle().set("background", "transparent");
		label.getStyle().set("color", "#000"); // or use contrasting color
		label.getStyle().set("font-size", "12px");
		label.getStyle().set("white-space", "nowrap");
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
