package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

/** CGanttTimelineHeader - Timeline header component for Gantt chart columns. Displays major time markers (years, months) divided proportionally across
 * the timeline range. Synchronizes with CGanttTimelineBar components to ensure proper alignment of task bars with time periods. */
public class CGanttTimelineHeader extends Div {

	private static final long serialVersionUID = 1L;
	private final LocalDate timelineEnd;
	private final LocalDate timelineStart;
	private final int totalWidth;

	/** Constructor for CGanttTimelineHeader.
	 * @param timelineStart The start date of the timeline range
	 * @param timelineEnd   The end date of the timeline range
	 * @param totalWidth    The total width in pixels for the timeline */
	public CGanttTimelineHeader(final LocalDate timelineStart, final LocalDate timelineEnd, final int totalWidth) {
		this.timelineStart = timelineStart;
		this.timelineEnd = timelineEnd;
		this.totalWidth = totalWidth;
		addClassName("gantt-timeline-header");
		if ((timelineStart == null) || (timelineEnd == null)) {
			// No valid timeline range
			addClassName("no-timeline");
			return;
		}
		renderTimelineMarkers();
	}

	/** Create a month marker division on the timeline.
	 * @param month        The YearMonth to display
	 * @param leftPercent  The left position as percentage
	 * @param widthPercent The width as percentage
	 * @param isYear       Whether this is a year boundary marker */
	private void createMonthMarker(final YearMonth month, final double leftPercent, final double widthPercent, final boolean isYear) {
		final Div marker = new Div();
		marker.addClassName("gantt-timeline-marker");
		if (isYear) {
			marker.addClassName("gantt-timeline-year-marker");
		}
		marker.getStyle().set("left", String.format("%.2f%%", leftPercent));
		marker.getStyle().set("width", String.format("%.2f%%", widthPercent));
		// Create label for the month
		final Span label = new Span();
		label.addClassName("gantt-timeline-label");
		// Format: "Jan 2024" for January, "Feb" for other months, "2024" for year markers
		String labelText;
		if (isYear) {
			labelText = String.valueOf(month.getYear());
		} else if (month.getMonthValue() == 1) {
			// January - show month and year
			labelText = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));
		} else {
			// Other months - show month only
			labelText = month.format(DateTimeFormatter.ofPattern("MMM"));
		}
		label.setText(labelText);
		marker.add(label);
		add(marker);
	}

	/** Render timeline markers for the timeline range. Creates major divisions for years and months. */
	private void renderTimelineMarkers() {
		// Calculate total timeline duration in days
		final long totalDays = ChronoUnit.DAYS.between(timelineStart, timelineEnd);
		if (totalDays <= 0) {
			return;
		}
		// Determine the scale based on timeline duration
		// For timelines > 365 days, show years; for > 90 days show months; otherwise show weeks
		if (totalDays > 365) {
			renderYearMarkers(totalDays);
		} else if (totalDays > 90) {
			renderMonthMarkers(totalDays);
		} else {
			renderWeekMarkers(totalDays);
		}
	}

	/** Render markers for month divisions.
	 * @param totalDays The total number of days in the timeline */
	private void renderMonthMarkers(final long totalDays) {
		LocalDate currentDate = timelineStart.withDayOfMonth(1); // Start at beginning of month
		int yearBoundary = timelineStart.getYear();
		while (!currentDate.isAfter(timelineEnd)) {
			final YearMonth month = YearMonth.from(currentDate);
			final LocalDate monthEnd = currentDate.plusMonths(1).minusDays(1);
			// Calculate the visible portion of this month within the timeline
			final LocalDate visibleStart = currentDate.isBefore(timelineStart) ? timelineStart : currentDate;
			final LocalDate visibleEnd = monthEnd.isAfter(timelineEnd) ? timelineEnd : monthEnd;
			final long startOffset = ChronoUnit.DAYS.between(timelineStart, visibleStart);
			final long monthDuration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			final double leftPercent = (startOffset * 100.0) / totalDays;
			final double widthPercent = (monthDuration * 100.0) / totalDays;
			// Mark year boundaries
			final boolean isYearBoundary = (month.getMonthValue() == 1) && (month.getYear() > yearBoundary);
			if (isYearBoundary) {
				yearBoundary = month.getYear();
			}
			createMonthMarker(month, leftPercent, widthPercent, isYearBoundary);
			currentDate = currentDate.plusMonths(1);
		}
	}

	/** Render markers for week divisions.
	 * @param totalDays The total number of days in the timeline */
	private void renderWeekMarkers(final long totalDays) {
		// Find the first Monday on or after the timeline start
		LocalDate currentDate = timelineStart;
		while (currentDate.getDayOfWeek().getValue() != 1) {
			currentDate = currentDate.plusDays(1);
		}
		int weekNumber = 1;
		while (!currentDate.isAfter(timelineEnd)) {
			final LocalDate weekEnd = currentDate.plusDays(6);
			// Calculate the visible portion of this week within the timeline
			final LocalDate visibleStart = currentDate.isBefore(timelineStart) ? timelineStart : currentDate;
			final LocalDate visibleEnd = weekEnd.isAfter(timelineEnd) ? timelineEnd : weekEnd;
			final long startOffset = ChronoUnit.DAYS.between(timelineStart, visibleStart);
			final long weekDuration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			final double leftPercent = (startOffset * 100.0) / totalDays;
			final double widthPercent = (weekDuration * 100.0) / totalDays;
			// Create week marker
			final Div marker = new Div();
			marker.addClassName("gantt-timeline-marker");
			marker.addClassName("gantt-timeline-week-marker");
			marker.getStyle().set("left", String.format("%.2f%%", leftPercent));
			marker.getStyle().set("width", String.format("%.2f%%", widthPercent));
			// Create label for the week
			final Span label = new Span();
			label.addClassName("gantt-timeline-label");
			label.setText("W" + weekNumber);
			marker.add(label);
			add(marker);
			currentDate = currentDate.plusWeeks(1);
			weekNumber++;
		}
	}

	/** Render markers for year divisions.
	 * @param totalDays The total number of days in the timeline */
	private void renderYearMarkers(final long totalDays) {
		LocalDate currentDate = timelineStart.withDayOfYear(1); // Start at beginning of year
		while (!currentDate.isAfter(timelineEnd)) {
			final int year = currentDate.getYear();
			final LocalDate yearEnd = currentDate.plusYears(1).minusDays(1);
			// Calculate the visible portion of this year within the timeline
			final LocalDate visibleStart = currentDate.isBefore(timelineStart) ? timelineStart : currentDate;
			final LocalDate visibleEnd = yearEnd.isAfter(timelineEnd) ? timelineEnd : yearEnd;
			final long startOffset = ChronoUnit.DAYS.between(timelineStart, visibleStart);
			final long yearDuration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			final double leftPercent = (startOffset * 100.0) / totalDays;
			final double widthPercent = (yearDuration * 100.0) / totalDays;
			// Create year marker
			final Div marker = new Div();
			marker.addClassName("gantt-timeline-marker");
			marker.addClassName("gantt-timeline-year-marker");
			marker.getStyle().set("left", String.format("%.2f%%", leftPercent));
			marker.getStyle().set("width", String.format("%.2f%%", widthPercent));
			// Create label for the year
			final Span label = new Span();
			label.addClassName("gantt-timeline-label");
			label.setText(String.valueOf(year));
			marker.add(label);
			add(marker);
			currentDate = currentDate.plusYears(1);
		}
	}
}
