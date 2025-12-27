package tech.derbent.app.gannt.ganntviewentity.view.components;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.Objects;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

@Tag ("div")
public class CGanntTimelineHeader extends CVerticalLayout {

	public enum CTimelineScale {

		AUTO("Auto"), MONTH("Months"), QUARTER("Quarters"), WEEK("Weeks"), YEAR("Years");

		private final String label;

		CTimelineScale(final String label) {
			this.label = label;
		}

		public String getLabel() { return label; }
	}

	public record CGanttTimelineRange(LocalDate startDate, LocalDate endDate) { /*****/
	}

	@FunctionalInterface
	public interface IGanttTimelineChangeListener {

		void onTimelineRangeChange(CGanttTimelineRange range);
	}

	@FunctionalInterface
	public interface IGanttWidthChangeListener {

		void onWidthChange(int newWidth);
	}

	private static final long MIN_DURATION_DAYS = 7;
	private static final long serialVersionUID = 1L;

	private static CButton createControlButton(final String iconName, final String tooltip, final Runnable action) {
		final CButton button = new CButton("", CColorUtils.createStyledIcon(iconName));
		button.addClickListener(event -> action.run());
		button.getElement().setProperty("title", tooltip);
		button.addClassName("gantt-timeline-control-button");
		return button;
	}

	private final IGanttTimelineChangeListener changeListener;
	private final CHorizontalLayout controlBar = new CHorizontalLayout();
	private CTimelineScale currentScale = CTimelineScale.AUTO;
	private final LinkedHashMap<LocalDate, Integer> dateToPixelMap = new LinkedHashMap<>();
	private LocalDate endDate;
	private final LocalDate fullRangeEnd;
	private final LocalDate fullRangeStart;
	private final Select<CTimelineScale> scaleSelector = new Select<>();
	private LocalDate startDate;
	private final Div timelineWrapper = new Div();
	private final int totalWidth;
	private final boolean useRandomColors = false;
	private final IGanttWidthChangeListener widthChangeListener;
	private final Span windowSummary = new Span();

	public CGanntTimelineHeader(final LocalDate startDate, final LocalDate endDate, final int totalWidth,
			final IGanttTimelineChangeListener changeListener, final IGanttWidthChangeListener widthChangeListener) {
		Check.notNull(startDate, "startDate cannot be null");
		Check.notNull(endDate, "endDate cannot be null");
		Check.isTrue(!startDate.isAfter(endDate), "startDate must be before or equal to endDate");
		this.startDate = startDate;
		this.endDate = endDate;
		fullRangeStart = startDate;
		fullRangeEnd = endDate;
		this.totalWidth = totalWidth;
		this.changeListener = changeListener;
		this.widthChangeListener = widthChangeListener;
		addClassName("gantt-timeline-header");
		setWidth(totalWidth + "px");
		// setHeightUndefined();
		setPadding(false);
		setSpacing(false);
		configureControlBar();
		configureTimelineWrapper();
		add(controlBar, timelineWrapper);
		renderTimeline(false);
	}

	private void configureControlBar() {
		controlBar.addClassName("gantt-timeline-controls");
		controlBar.setWidthFull();
		controlBar.setAlignItems(Alignment.CENTER);
		controlBar.setMargin(false);
		controlBar.setPadding(false);
		controlBar.setSpacing(false);
		final CButton scrollBack = createControlButton("vaadin:angle-left", "Scroll left", () -> on_actionScroll(-1));
		final CButton scrollForward = createControlButton("vaadin:angle-right", "Scroll right", () -> on_actionScroll(1));
		final CButton zoomIn = createControlButton("vaadin:search-plus", "Zoom in", () -> on_actionZoom(0.7));
		final CButton zoomOut = createControlButton("vaadin:search-minus", "Zoom out", () -> on_actionZoom(1.5));
		final CButton reset =
				createControlButton("vaadin:refresh", "Reset to full range", () -> on_actioAapplyRange(fullRangeStart, fullRangeEnd, true));
		final CButton focusMiddle = createControlButton("vaadin:crosshairs", "Focus to middle of timeline", () -> on_actionFocusToMiddle());
		final CButton increaseWidth = createControlButton("vaadin:expand", "Increase timeline width", () -> on_actionAdjustWidth(100));
		final CButton decreaseWidth = createControlButton("vaadin:compress", "Decrease timeline width", () -> on_actionAdjustWidth(-100));
		scaleSelector.setItems(CTimelineScale.values());
		scaleSelector.setValue(currentScale);
		scaleSelector.setItemLabelGenerator(CTimelineScale::getLabel);
		scaleSelector.addValueChangeListener(event -> {
			final CTimelineScale newValue = Objects.requireNonNullElse(event.getValue(), CTimelineScale.AUTO);
			setScale(newValue);
		});
		scaleSelector.addClassName("gantt-timeline-scale-select");
		windowSummary.addClassName("gantt-timeline-summary");
		updateWindowSummary();
		controlBar.add(scrollBack, scrollForward, zoomIn, zoomOut, reset, focusMiddle, decreaseWidth, increaseWidth, scaleSelector, windowSummary);
	}

	private void configureTimelineWrapper() {
		timelineWrapper.addClassName("gantt-timeline-wrapper");
		timelineWrapper.getStyle().set("position", "relative");
		timelineWrapper.setWidth(totalWidth + "px");
	}

	private Div createMarker(final CHorizontalLayout container, final String text, final int width) {
		final Div div = new Div();
		div.addClassName("gantt-timeline-marker");
		div.getStyle().set("width", width + "px");
		div.setHeightFull();
		div.getStyle().set("border-left", "1px solid #000");
		div.getStyle().set("border-bottom", "1px solid #000");
		if (useRandomColors) {
			final String backgroundColor = CColorUtils.getRandomColor(false);
			div.getStyle().set("background-color", backgroundColor);
			div.getStyle().set("color", CColorUtils.getContrastTextColor(backgroundColor));
		}
		div.setText(text);
		container.add(div);
		return div;
	}

	private CHorizontalLayout createRowContainer(final String className) {
		final CHorizontalLayout container = new CHorizontalLayout();
		container.addClassName("gantt-timeline-row");
		container.addClassName(className);
		container.setPadding(false);
		container.setSpacing(false);
		container.setWidth(totalWidth + "px");
		return container;
	}

	public LinkedHashMap<LocalDate, Integer> getDateToPixelMap() { return dateToPixelMap; }

	private void on_actioAapplyRange(final LocalDate desiredStart, final LocalDate desiredEnd, final boolean notifyChange) {
		Check.notNull(desiredStart, "desiredStart cannot be null");
		Check.notNull(desiredEnd, "desiredEnd cannot be null");
		LocalDate normalizedStart = desiredStart;
		LocalDate normalizedEnd = desiredEnd;
		if (normalizedStart.isAfter(normalizedEnd)) {
			final LocalDate swap = normalizedStart;
			normalizedStart = normalizedEnd;
			normalizedEnd = swap;
		}
		final long maxDuration = ChronoUnit.DAYS.between(fullRangeStart, fullRangeEnd) + 1;
		long desiredDuration = ChronoUnit.DAYS.between(normalizedStart, normalizedEnd) + 1;
		if (desiredDuration < MIN_DURATION_DAYS) {
			desiredDuration = MIN_DURATION_DAYS;
			normalizedEnd = normalizedStart.plusDays(desiredDuration - 1);
		}
		if (desiredDuration > maxDuration) {
			normalizedStart = fullRangeStart;
			normalizedEnd = fullRangeEnd;
			desiredDuration = maxDuration;
		}
		if (normalizedStart.isBefore(fullRangeStart)) {
			normalizedStart = fullRangeStart;
			normalizedEnd = normalizedStart.plusDays(desiredDuration - 1);
		}
		if (normalizedEnd.isAfter(fullRangeEnd)) {
			normalizedEnd = fullRangeEnd;
			normalizedStart = normalizedEnd.minusDays(desiredDuration - 1);
			if (normalizedStart.isBefore(fullRangeStart)) {
				normalizedStart = fullRangeStart;
			}
		}
		startDate = normalizedStart;
		endDate = normalizedEnd;
		renderTimeline(notifyChange);
	}

	/** Adjust the width of the timeline display.
	 * @param deltaPixels The change in pixels (positive to increase, negative to decrease) */
	private void on_actionAdjustWidth(final int deltaPixels) {
		if (widthChangeListener != null) {
			// Calculate new width (constrained to reasonable bounds)
			final int newWidth = Math.max(400, Math.min(1600, totalWidth + deltaPixels));
			widthChangeListener.onWidthChange(newWidth);
		}
	}

	/** Focus to the middle of the current full timeline range. */
	private void on_actionFocusToMiddle() {
		final long fullDuration = ChronoUnit.DAYS.between(fullRangeStart, fullRangeEnd) + 1;
		final LocalDate middleDate = fullRangeStart.plusDays(fullDuration / 2);
		// Create a window around the middle date
		final long windowSize = Math.max(MIN_DURATION_DAYS, fullDuration / 4);
		final long halfWindow = windowSize / 2;
		final LocalDate newStart = middleDate.minusDays(halfWindow);
		final LocalDate newEnd = middleDate.plusDays(halfWindow);
		on_actioAapplyRange(newStart, newEnd, true);
	}

	private void on_actionScroll(final int direction) {
		final long currentDuration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		final long shiftDays = Math.max(1, Math.round(currentDuration * 0.3));
		final LocalDate newStart = startDate.plusDays(direction * shiftDays);
		final LocalDate newEnd = endDate.plusDays(direction * shiftDays);
		on_actioAapplyRange(newStart, newEnd, true);
	}

	private void on_actionZoom(final double factor) {
		final long currentDuration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		long newDuration = Math.round(currentDuration * factor);
		newDuration = Math.max(MIN_DURATION_DAYS, newDuration);
		final long maxDuration = ChronoUnit.DAYS.between(fullRangeStart, fullRangeEnd) + 1;
		newDuration = Math.min(maxDuration, newDuration);
		final LocalDate center = startDate.plusDays(currentDuration / 2);
		final long halfWindow = newDuration / 2;
		final LocalDate newStart = center.minusDays(halfWindow);
		final LocalDate newEnd = newStart.plusDays(newDuration - 1);
		on_actioAapplyRange(newStart, newEnd, true);
	}

	private void renderMonthMarkers(final long totalDays, final CHorizontalLayout container) {
		LocalDate current = startDate.withDayOfMonth(1);
		while (!current.isAfter(endDate)) {
			final YearMonth ym = YearMonth.from(current);
			final LocalDate monthEnd = ym.atEndOfMonth();
			final LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			final LocalDate visibleEnd = monthEnd.isAfter(endDate) ? endDate : monthEnd;
			final long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			final int width = (int) ((duration * totalWidth) / (double) totalDays);
			createMarker(container, ym.format(DateTimeFormatter.ofPattern("MMM")), width);
			current = current.plusMonths(1);
		}
	}

	private void renderQuarterMarkers(final long totalDays, final CHorizontalLayout container) {
		LocalDate current = startDate.withMonth((((startDate.getMonthValue() - 1) / 3) * 3) + 1).withDayOfMonth(1);
		while (!current.isAfter(endDate)) {
			final int quarter = ((current.getMonthValue() - 1) / 3) + 1;
			final LocalDate quarterEnd = current.plusMonths(3).minusDays(1);
			final LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			final LocalDate visibleEnd = quarterEnd.isAfter(endDate) ? endDate : quarterEnd;
			final long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			final int width = (int) ((duration * totalWidth) / (double) totalDays);
			createMarker(container, "Q" + quarter + " " + current.getYear(), width);
			current = current.plusMonths(3);
		}
	}

	private void renderTimeline(final boolean notifyChange) {
		timelineWrapper.removeAll();
		dateToPixelMap.clear();
		final long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		if (totalDays <= 0) {
			return;
		}
		for (int i = 0; i < totalDays; i++) {
			final LocalDate current = startDate.plusDays(i);
			final int pixel = (int) ((i * totalWidth) / (double) totalDays);
			dateToPixelMap.put(current, pixel);
		}
		renderTimelineLayers(totalDays);
		renderTodayMarker();
		updateWindowSummary();
		if (notifyChange && (changeListener != null)) {
			changeListener.onTimelineRangeChange(new CGanttTimelineRange(startDate, endDate));
		}
	}

	private void renderTimelineLayers(final long totalDays) {
		if (shouldRenderRow(CTimelineScale.YEAR)) {
			final CHorizontalLayout yearContainer = createRowContainer("gantt-timeline-row-year");
			renderYearMarkers(totalDays, yearContainer);
			timelineWrapper.add(yearContainer);
		}
		if (shouldRenderRow(CTimelineScale.QUARTER)) {
			final CHorizontalLayout quarterContainer = createRowContainer("gantt-timeline-row-quarter");
			renderQuarterMarkers(totalDays, quarterContainer);
			timelineWrapper.add(quarterContainer);
		}
		if (shouldRenderRow(CTimelineScale.MONTH)) {
			final CHorizontalLayout monthContainer = createRowContainer("gantt-timeline-row-month");
			renderMonthMarkers(totalDays, monthContainer);
			timelineWrapper.add(monthContainer);
		}
		if (shouldRenderRow(CTimelineScale.WEEK)) {
			final CHorizontalLayout weekContainer = createRowContainer("gantt-timeline-row-week");
			renderWeekLines(totalDays, weekContainer);
			timelineWrapper.add(weekContainer);
		}
	}

	private void renderTodayMarker() {
		final LocalDate today = LocalDate.now();
		if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
			final Integer todayPixel = dateToPixelMap.get(today);
			if (todayPixel != null) {
				final Div todayLine = new Div();
				todayLine.addClassName("gantt-timeline-today-marker");
				todayLine.getStyle().set("position", "absolute");
				todayLine.getStyle().set("left", todayPixel + "px");
				todayLine.getStyle().set("top", "0");
				todayLine.getStyle().set("bottom", "0");
				todayLine.getStyle().set("width", "3px");
				todayLine.getStyle().set("background", "var(--lumo-error-color, red)");
				todayLine.getStyle().set("z-index", "10");
				timelineWrapper.add(todayLine);
			}
		}
	}

	private void renderWeekLines(final long totalDays, final CHorizontalLayout container) {
		LocalDate current = startDate.with(DayOfWeek.MONDAY);
		while (!current.isAfter(endDate)) {
			final LocalDate weekStart = current.isBefore(startDate) ? startDate : current;
			final LocalDate weekEnd = current.plusWeeks(1).minusDays(1).isAfter(endDate) ? endDate : current.plusWeeks(1).minusDays(1);
			final long duration = ChronoUnit.DAYS.between(weekStart, weekEnd) + 1;
			final int width = (int) ((duration * totalWidth) / (double) totalDays);
			final int weekNumber = current.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
			final String title = String.valueOf(weekNumber);
			createMarker(container, title, width);
			current = current.plusWeeks(1);
		}
	}

	private void renderYearMarkers(final long totalDays, final CHorizontalLayout container) {
		LocalDate current = LocalDate.of(startDate.getYear(), 1, 1);
		while (!current.isAfter(endDate)) {
			final LocalDate yearEnd = current.plusYears(1).minusDays(1);
			final LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			final LocalDate visibleEnd = yearEnd.isAfter(endDate) ? endDate : yearEnd;
			final long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			final int width = (int) ((duration * totalWidth) / (double) totalDays);
			createMarker(container, String.valueOf(current.getYear()), width);
			current = current.plusYears(1);
		}
	}

	public void setRange(final LocalDate newStart, final LocalDate newEnd) {
		on_actioAapplyRange(newStart, newEnd, false);
	}

	private void setScale(final CTimelineScale newScale) {
		if (currentScale == newScale) {
			return;
		}
		currentScale = newScale;
		renderTimeline(false);
	}

	private boolean shouldRenderRow(final CTimelineScale rowScale) {
		return (currentScale == CTimelineScale.AUTO) || (currentScale == rowScale)
				|| ((currentScale == CTimelineScale.YEAR) && (rowScale == CTimelineScale.QUARTER))
				|| ((currentScale == CTimelineScale.YEAR) && (rowScale == CTimelineScale.MONTH))
				|| ((currentScale == CTimelineScale.YEAR) && (rowScale == CTimelineScale.WEEK))
				|| ((currentScale == CTimelineScale.QUARTER) && (rowScale == CTimelineScale.MONTH))
				|| ((currentScale == CTimelineScale.QUARTER) && (rowScale == CTimelineScale.WEEK))
				|| ((currentScale == CTimelineScale.MONTH) && (rowScale == CTimelineScale.WEEK));
	}

	private void updateWindowSummary() {
		windowSummary.setText(startDate + " → " + endDate);
	}
}
