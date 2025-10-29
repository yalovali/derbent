package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.api.views.components.CVerticalLayout;

@Tag ("div")
public class CGanttTimelineHeader extends CVerticalLayout {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<LocalDate, Integer> dateToPixelMap = new LinkedHashMap<>();
	private final LocalDate endDate;
	private final LocalDate startDate;
	private final int totalWidth;
	private final boolean useRandomColors = false;

	public CGanttTimelineHeader(final LocalDate startDate, final LocalDate endDate, final int totalWidth) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalWidth = totalWidth;
		addClassName("gantt-timeline-header");
		setWidth(totalWidth + "px");
		// setHeight("80px");
		renderTimeline();
	}

	private Div createMarker(final CHorizontalLayout container, final String text, final int width) {
		final Div div = new Div();
		div.getStyle().set("width", width + "px");
		div.setHeightFull();
		// put a border bottom and left
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

	private void renderTimeline() {
		final long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		if (totalDays <= 0) {
			return;
		}
		for (int i = 0; i < totalDays; i++) {
			final LocalDate current = startDate.plusDays(i);
			final int pixel = (int) ((i * totalWidth) / (double) totalDays);
			dateToPixelMap.put(current, pixel);
		}
		final CHorizontalLayout yearContainer = new CHorizontalLayout();
		final CHorizontalLayout quarterContainer = new CHorizontalLayout();
		final CHorizontalLayout monthContainer = new CHorizontalLayout();
		final CHorizontalLayout weekContainer = new CHorizontalLayout();
		add(yearContainer);
		add(quarterContainer);
		add(monthContainer);
		add(weekContainer);
		renderYearMarkers(totalDays, yearContainer);
		renderQuarterMarkers(totalDays, quarterContainer);
		renderMonthMarkers(totalDays, monthContainer);
		renderWeekLines(totalDays, weekContainer);
		renderTodayMarker();
	}

	private void renderTodayMarker() {
		final LocalDate today = LocalDate.now();
		if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
			final int todayPixel = dateToPixelMap.get(today);
			final Div todayLine = new Div();
			todayLine.getStyle().set("position", "absolute");
			todayLine.getStyle().set("left", todayPixel + "px");
			todayLine.getStyle().set("top", "0");
			todayLine.getStyle().set("bottom", "0");
			todayLine.getStyle().set("width", "4px");
			todayLine.getStyle().set("background", "red");
			todayLine.getStyle().set("z-index", "10");
			add(todayLine);
		}
	}

	private void renderWeekLines(final long totalDays, final CHorizontalLayout container) {
		LocalDate current = startDate.with(java.time.DayOfWeek.MONDAY);
		while (!current.isAfter(endDate)) {
			final int width = (int) ((7 * totalWidth) / (double) totalDays);
			final int weekNumber = current.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
			// final int weekYear = current.get(IsoFields.WEEK_BASED_YEAR);
			// final String title = "W" + weekNumber + " " + weekYear;
			// convert to string like W01
			final String title = String.valueOf(weekNumber); // + " " + weekYear;
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
}
