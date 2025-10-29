package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

@Tag ("div")
public class CGanttTimelineHeader extends Div {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<LocalDate, Integer> dateToPixelMap = new LinkedHashMap<>();
	private final LocalDate endDate;
	private boolean showQuarters = true;
	private boolean showWeeks = true;
	private final LocalDate startDate;
	private final int totalWidth;

	public CGanttTimelineHeader(LocalDate startDate, LocalDate endDate, int totalWidth) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalWidth = totalWidth;
		addClassName("gantt-timeline-header");
		setWidth(totalWidth + "px");
		setHeight("80px");
		getStyle().set("position", "relative");
		renderTimeline();
	}

	public LinkedHashMap<LocalDate, Integer> getDateToPixelMap() { return dateToPixelMap; }

	private void renderMonthMarkers(long totalDays, Div container) {
		LocalDate current = startDate.withDayOfMonth(1);
		while (!current.isAfter(endDate)) {
			YearMonth ym = YearMonth.from(current);
			LocalDate monthEnd = ym.atEndOfMonth();
			LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			LocalDate visibleEnd = monthEnd.isAfter(endDate) ? endDate : monthEnd;
			long offset = ChronoUnit.DAYS.between(startDate, visibleStart);
			long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			int left = (int) ((offset * totalWidth) / (double) totalDays);
			int width = (int) ((duration * totalWidth) / (double) totalDays);
			Div marker = new Div();
			marker.addClassName("gantt-timeline-month");
			marker.getStyle().set("position", "absolute");
			marker.getStyle().set("left", left + "px");
			marker.getStyle().set("width", width + "px");
			marker.getStyle().set("height", "26px");
			Span label = new Span(ym.format(DateTimeFormatter.ofPattern("MMM yyyy")));
			label.getStyle().set("font-size", "10px");
			label.getStyle().set("padding-left", "4px");
			marker.add(label);
			container.add(marker);
			current = current.plusMonths(1);
		}
	}

	private void renderQuarterMarkers(long totalDays, Div container) {
		LocalDate current = startDate.withMonth(((startDate.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
		while (!current.isAfter(endDate)) {
			int quarter = ((current.getMonthValue() - 1) / 3) + 1;
			LocalDate quarterEnd = current.plusMonths(3).minusDays(1);
			LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			LocalDate visibleEnd = quarterEnd.isAfter(endDate) ? endDate : quarterEnd;
			long offset = ChronoUnit.DAYS.between(startDate, visibleStart);
			long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			int left = (int) ((offset * totalWidth) / (double) totalDays);
			int width = (int) ((duration * totalWidth) / (double) totalDays);
			Div labelDiv = new Div();
			labelDiv.addClassName("gantt-timeline-quarter");
			labelDiv.getStyle().set("position", "absolute");
			labelDiv.getStyle().set("left", left + "px");
			labelDiv.getStyle().set("width", width + "px");
			labelDiv.getStyle().set("height", "28px");
			labelDiv.setText("Q" + quarter + " " + current.getYear());
			container.add(labelDiv);
			current = current.plusMonths(3);
		}
	}

	private void renderTimeline() {
		long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		if (totalDays <= 0) {
			return;
		}
		for (int i = 0; i < totalDays; i++) {
			LocalDate current = startDate.plusDays(i);
			int pixel = (int) ((i * totalWidth) / (double) totalDays);
			dateToPixelMap.put(current, pixel);
		}
		Div yearContainer = new Div();
		yearContainer.addClassName("gantt-timeline-year-container");
		yearContainer.getStyle().set("position", "absolute");
		yearContainer.getStyle().set("top", "0");
		yearContainer.getStyle().set("height", "26px");
		yearContainer.getStyle().set("width", totalWidth + "px");
		add(yearContainer);
		renderYearMarkers(totalDays, yearContainer);
		if (showQuarters) {
			Div quarterContainer = new Div();
			quarterContainer.addClassName("gantt-timeline-quarter-container");
			quarterContainer.getStyle().set("position", "absolute");
			quarterContainer.getStyle().set("top", "26px");
			quarterContainer.getStyle().set("height", "28px");
			quarterContainer.getStyle().set("width", totalWidth + "px");
			add(quarterContainer);
			renderQuarterMarkers(totalDays, quarterContainer);
		}
		Div monthContainer = new Div();
		monthContainer.addClassName("gantt-timeline-month-container");
		monthContainer.getStyle().set("position", "absolute");
		monthContainer.getStyle().set("top", "54px");
		monthContainer.getStyle().set("height", "26px");
		monthContainer.getStyle().set("width", totalWidth + "px");
		add(monthContainer);
		renderMonthMarkers(totalDays, monthContainer);
		if (showWeeks) {
			renderWeekLines(totalDays);
		}
		renderTodayMarker();
	}

	private void renderTodayMarker() {
		LocalDate today = LocalDate.now();
		if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
			int todayPixel = dateToPixelMap.get(today);
			Div todayLine = new Div();
			todayLine.getStyle().set("position", "absolute");
			todayLine.getStyle().set("left", todayPixel + "px");
			todayLine.getStyle().set("top", "0");
			todayLine.getStyle().set("bottom", "0");
			todayLine.getStyle().set("width", "2px");
			todayLine.getStyle().set("background", "red");
			todayLine.getStyle().set("z-index", "10");
			todayLine.addClassName("gantt-today-marker");
			add(todayLine);
		}
	}

	private void renderWeekLines(long totalDays) {
		LocalDate current = startDate;
		while (!current.isAfter(endDate)) {
			int left = (int) ((ChronoUnit.DAYS.between(startDate, current) * totalWidth) / (double) totalDays);
			Div line = new Div();
			line.getStyle().set("position", "absolute");
			line.getStyle().set("left", left + "px");
			line.getStyle().set("top", "0");
			line.getStyle().set("bottom", "0");
			line.getStyle().set("width", "1px");
			line.getStyle().set("background", "#dcdcdc");
			line.getStyle().set("z-index", "1");
			line.getElement().setProperty("title", "Week of " + current);
			add(line);
			current = current.plusWeeks(1);
		}
	}

	private void renderYearMarkers(long totalDays, Div container) {
		LocalDate current = LocalDate.of(startDate.getYear(), 1, 1);
		while (!current.isAfter(endDate)) {
			LocalDate yearEnd = current.plusYears(1).minusDays(1);
			LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			LocalDate visibleEnd = yearEnd.isAfter(endDate) ? endDate : yearEnd;
			long offset = ChronoUnit.DAYS.between(startDate, visibleStart);
			long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			int left = (int) ((offset * totalWidth) / (double) totalDays);
			int width = (int) ((duration * totalWidth) / (double) totalDays);
			Div marker = new Div();
			marker.addClassName("gantt-timeline-year");
			marker.getStyle().set("position", "absolute");
			marker.getStyle().set("left", left + "px");
			marker.getStyle().set("width", width + "px");
			marker.getStyle().set("height", "26px");
			Span label = new Span(String.valueOf(current.getYear()));
			marker.add(label);
			container.add(marker);
			current = current.plusYears(1);
		}
	}
}
