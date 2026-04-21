package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

/**
 * Visual timeline bar component for Gnnt items.
 */
public class CGnntTimelineBar extends CHorizontalLayout {

	private static final long serialVersionUID = 1L;

	private static void createDisplayText(final CHorizontalLayout barLayout, final CGnntItem item, final int startPx, final int progress,
			final int totalWidth) {
		final String displayText = "%s (%s) - %d%%".formatted(item.getEntity().getName(), item.getResponsibleName(), progress);
		final CDiv label = new CDiv(displayText);
		label.setText(displayText);
		int safeLeft = totalWidth - (startPx + 10);
		if (safeLeft < 0) {
			safeLeft = 0;
		}
		label.getStyle().set("z-index", "1");
		label.getStyle().set("background", "transparent");
		label.getStyle().set("color", "#000");
		label.getStyle().set("font-size", "12px");
		label.getStyle().set("white-space", "nowrap");
		label.getStyle().set("max-width", safeLeft + "px");
		label.getStyle().set("overflow", "hidden");
		label.getStyle().set("text-overflow", "ellipsis");
		label.setSizeUndefined();
		barLayout.add(label);
	}

	private static void createToolTip(final CGnntItem item, final LocalDate itemStart, final LocalDate itemEnd, final CDiv bar, final int progress) {
		final String tooltip = "%s%n%s%nProgress: %d%%%nDuration: %d days%nStart: %s%nEnd: %s".formatted(item.getEntity().getName(),
				item.getResponsibleName(), progress, item.getDurationDays(), itemStart, itemEnd);
		bar.getElement().setAttribute("title", tooltip);
	}

	private static void formatBar(final CGnntItem item, final CDiv bar) {
		bar.getStyle().set("height", "90%");
		bar.getStyle().set("display", "flex");
		bar.getStyle().set("align-items", "center");
		bar.getStyle().set("justify-content", "center");
		bar.getStyle().set("border", "1px solid #000");
		final String backgroundColor = item.getColorCode();
		bar.getStyle().set("background-color", backgroundColor);
		bar.getStyle().set("color", CColorUtils.getContrastTextColor(backgroundColor));
		bar.getStyle().set("font-size", "12px");
		bar.getStyle().set("border-radius", "4px");
		bar.setHeight("90%");
	}

	private static void formatProgress(final CDiv bar, final int progress) {
		if (!((progress > 0) && (progress < 100))) {
			return;
		}
		final Div progressOverlay = new Div();
		progressOverlay.getStyle().set("position", "absolute");
		progressOverlay.getStyle().set("left", "0");
		progressOverlay.getStyle().set("top", "0");
		progressOverlay.getStyle().set("bottom", "0");
		progressOverlay.getStyle().set("width", progress + "%");
		progressOverlay.getStyle().set("box-sizing", "border-box");
		progressOverlay.getStyle().set("min-width", "0");
		progressOverlay.getStyle().set("background-color", "rgba(255,255,255,0.2)");
		progressOverlay.getStyle().set("pointer-events", "none");
		bar.getElement().insertChild(0, progressOverlay.getElement());
	}

	public CGnntTimelineBar(final CGnntItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		addClassName("gantt-timeline-bar-container");
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

	private void createBar(final CGnntItem item, final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
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
		final CHorizontalLayout barLayout = new CHorizontalLayout();
		final CDiv bar = new CDiv();
		barLayout.add(bar);
		final long startOffset = ChronoUnit.DAYS.between(timelineStart, visibleStart);
		final long visibleDuration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
		final int leftPx = Math.max(0, (int) Math.round((startOffset * totalWidth) / (double) totalDays));
		int widthPx = (int) Math.round((visibleDuration * totalWidth) / (double) totalDays);
		widthPx = Math.max(widthPx, 2);
		if ((leftPx + widthPx) > totalWidth) {
			widthPx = Math.max(2, totalWidth - leftPx);
		}
		barLayout.setSizeUndefined();
		barLayout.getStyle().set("position", "absolute");
		barLayout.getStyle().set("left", leftPx + "px");
		barLayout.setMargin(false);
		barLayout.setPadding(false);
		barLayout.setSpacing(true);
		barLayout.setWidthFull();
		barLayout.setHeightFull();
		bar.getStyle().set("width", widthPx + "px");
		formatBar(item, bar);
		final int progress = item.getProgressPercentage();
		createDisplayText(barLayout, item, leftPx + widthPx, progress, totalWidth);
		formatProgress(bar, progress);
		createToolTip(item, visibleStart, visibleEnd, bar, progress);
		add(barLayout);
	}
}
