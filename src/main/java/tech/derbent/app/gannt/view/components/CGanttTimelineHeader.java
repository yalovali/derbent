package tech.derbent.app.gannt.view.components;

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
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.api.views.components.CVerticalLayout;

@Tag ("div")
public class CGanttTimelineHeader extends CVerticalLayout {

	public enum CTimelineScale {

		AUTO("Auto"), MONTH("Months"), QUARTER("Quarters"), WEEK("Weeks"), YEAR("Years");

		private final String label;

		CTimelineScale(final String label) {
			this.label = label;
		}

		public String getLabel() { return label; }
	}

	public record CGanttTimelineRange(LocalDate startDate, LocalDate endDate) {}

	@FunctionalInterface
	public interface IGanttTimelineChangeListener {

		void onTimelineRangeChange(CGanttTimelineRange range);
	}

	private static final long MIN_DURATION_DAYS = 7;
	private static final long serialVersionUID = 1L;
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
	private final Span windowSummary = new Span();

	public CGanttTimelineHeader(final LocalDate startDate, final LocalDate endDate, final int totalWidth,
			final IGanttTimelineChangeListener changeListener) {
		Check.notNull(startDate, "startDate cannot be null");
		Check.notNull(endDate, "endDate cannot be null");
		Check.isTrue(!startDate.isAfter(endDate), "startDate must be before or equal to endDate");
		this.startDate = startDate;
		this.endDate = endDate;
		fullRangeStart = startDate;
		fullRangeEnd = endDate;
		this.totalWidth = totalWidth;
		this.changeListener = changeListener;
		addClassName("gantt-timeline-header");
		setWidth(totalWidth + "px");
		setPadding(false);
		setSpacing(false);
		configureControlBar();
		configureTimelineWrapper();
		add(controlBar, timelineWrapper);
		renderTimeline(false);
	}

	private void applyRange(LocalDate desiredStart, LocalDate desiredEnd, final boolean notifyChange) {
		Check.notNull(desiredStart, "desiredStart cannot be null");
		Check.notNull(desiredEnd, "desiredEnd cannot be null");
		if (desiredStart.isAfter(desiredEnd)) {
			final LocalDate swap = desiredStart;
			desiredStart = desiredEnd;
			desiredEnd = swap;
		}
		final long maxDuration = ChronoUnit.DAYS.between(fullRangeStart, fullRangeEnd) + 1;
		long desiredDuration = ChronoUnit.DAYS.between(desiredStart, desiredEnd) + 1;
		if (desiredDuration < MIN_DURATION_DAYS) {
			desiredDuration = MIN_DURATION_DAYS;
			desiredEnd = desiredStart.plusDays(desiredDuration - 1);
		}
		if (desiredDuration > maxDuration) {
			desiredStart = fullRangeStart;
			desiredEnd = fullRangeEnd;
			desiredDuration = maxDuration;
		}
		if (desiredStart.isBefore(fullRangeStart)) {
			desiredStart = fullRangeStart;
			desiredEnd = desiredStart.plusDays(desiredDuration - 1);
		}
		if (desiredEnd.isAfter(fullRangeEnd)) {
			desiredEnd = fullRangeEnd;
			desiredStart = desiredEnd.minusDays(desiredDuration - 1);
			if (desiredStart.isBefore(fullRangeStart)) {
				desiredStart = fullRangeStart;
			}
		}
		startDate = desiredStart;
		endDate = desiredEnd;
		renderTimeline(notifyChange);
	}

	private void configureControlBar() {
		controlBar.addClassName("gantt-timeline-controls");
		controlBar.setWidthFull();
		controlBar.setAlignItems(Alignment.CENTER);
		controlBar.setSpacing(true);
		final CButton scrollBack = createControlButton("vaadin:angle-left", "Scroll left", () -> scroll(-1));
		final CButton scrollForward = createControlButton("vaadin:angle-right", "Scroll right", () -> scroll(1));
		final CButton zoomIn = createControlButton("vaadin:search-plus", "Zoom in", () -> zoom(0.7));
		final CButton zoomOut = createControlButton("vaadin:search-minus", "Zoom out", () -> zoom(1.5));
		final CButton reset = createControlButton("vaadin:refresh", "Reset to full range", () -> applyRange(fullRangeStart, fullRangeEnd, true));
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
		controlBar.add(scrollBack, scrollForward, zoomIn, zoomOut, reset, scaleSelector, windowSummary);
	}

	private void configureTimelineWrapper() {
		timelineWrapper.addClassName("gantt-timeline-wrapper");
		timelineWrapper.getStyle().set("position", "relative");
		timelineWrapper.setWidth(totalWidth + "px");
	}

	private CButton createControlButton(final String iconName, final String tooltip, final Runnable action) {
		final CButton button = new CButton("", CColorUtils.createStyledIcon(iconName));
		button.addClickListener(event -> action.run());
		button.getElement().setProperty("title", tooltip);
		button.addClassName("gantt-timeline-control-button");
		return button;
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

	private void scroll(final int direction) {
		final long currentDuration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		final long shiftDays = Math.max(1, Math.round(currentDuration * 0.3));
		final LocalDate newStart = startDate.plusDays(direction * shiftDays);
		final LocalDate newEnd = endDate.plusDays(direction * shiftDays);
		applyRange(newStart, newEnd, true);
	}

	public void setRange(final LocalDate newStart, final LocalDate newEnd) {
		applyRange(newStart, newEnd, false);
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

	private void zoom(final double factor) {
		final long currentDuration = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		long newDuration = Math.round(currentDuration * factor);
		newDuration = Math.max(MIN_DURATION_DAYS, newDuration);
		final long maxDuration = ChronoUnit.DAYS.between(fullRangeStart, fullRangeEnd) + 1;
		newDuration = Math.min(maxDuration, newDuration);
		final LocalDate center = startDate.plusDays(currentDuration / 2);
		final long halfWindow = newDuration / 2;
		LocalDate newStart = center.minusDays(halfWindow);
		LocalDate newEnd = newStart.plusDays(newDuration - 1);
		applyRange(newStart, newEnd, true);
	}
}
