package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

@Tag ("div")
public class CGanttTimelineHeader extends Div {

	private static final String FONT_SIZE = "10px";
	private static final int HEADER_HEIGHT_PX = 60;
	private static final int LAYER_HEIGHT = 20;
	private static final String LINE_COLOR = "#ddd";
	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<LocalDate, Integer> dateToPixelMap = new LinkedHashMap<>();
	private final LocalDate endDate;
	private final LocalDate startDate;
	private final int totalWidth;

	public CGanttTimelineHeader(LocalDate startDate, LocalDate endDate, int totalWidth) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalWidth = totalWidth;
		addClassName("gantt-timeline-header");
		setWidth(totalWidth + "px");
		setHeight(HEADER_HEIGHT_PX + "px");
		getStyle().set("position", "relative");
		renderTimeline();
	}

	public LinkedHashMap<LocalDate, Integer> getDateToPixelMap() { return dateToPixelMap; }

	private void renderLayer(LocalDate start, LocalDate end, long totalDays, ChronoUnit unit, int top, String pattern) {
		LocalDate current = start;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		while (!current.isAfter(end)) {
			LocalDate next = current.plus(1, unit);
			LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
			LocalDate visibleEnd = next.minusDays(1).isAfter(endDate) ? endDate : next.minusDays(1);
			long offset = ChronoUnit.DAYS.between(startDate, visibleStart);
			long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			int left = (int) ((offset * totalWidth) / (double) totalDays);
			int width = (int) ((duration * totalWidth) / (double) totalDays);
			String label = formatter.format(current);
			Div marker = new Div();
			marker.addClassName("gantt-time-marker");
			marker.getStyle().set("position", "absolute");
			marker.getStyle().set("top", top + "px");
			marker.getStyle().set("left", left + "px");
			marker.getStyle().set("width", width + "px");
			marker.getStyle().set("height", LAYER_HEIGHT + "px");
			marker.getStyle().set("border-right", "1px solid " + LINE_COLOR);
			marker.getStyle().set("box-sizing", "border-box");
			Span labelSpan = new Span(label);
			labelSpan.getStyle().set("font-size", FONT_SIZE);
			labelSpan.getStyle().set("line-height", LAYER_HEIGHT + "px");
			labelSpan.getStyle().set("display", "inline-block");
			labelSpan.getStyle().set("padding-left", "4px");
			marker.add(labelSpan);
			add(marker);
			current = next;
		}
	}

	private void renderQuarters(LocalDate start, LocalDate end, long totalDays, int topOffset) {
		LocalDate current = start.withDayOfMonth(1);
		while (!current.isAfter(end)) {
			int year = current.getYear();
			int month = current.getMonthValue();
			int quarter = ((month - 1) / 3) + 1;
			LocalDate quarterStart = LocalDate.of(year, ((quarter - 1) * 3) + 1, 1);
			LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);
			if (quarterEnd.isBefore(start)) {
				current = quarterEnd.plusDays(1);
				continue;
			}
			LocalDate visibleStart = quarterStart.isBefore(startDate) ? startDate : quarterStart;
			LocalDate visibleEnd = quarterEnd.isAfter(endDate) ? endDate : quarterEnd;
			long offset = ChronoUnit.DAYS.between(startDate, visibleStart);
			long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
			int left = (int) ((offset * totalWidth) / (double) totalDays);
			int width = (int) ((duration * totalWidth) / (double) totalDays);
			String label = "Q" + quarter + " " + year;
			Div marker = new Div();
			marker.addClassName("gantt-time-marker");
			marker.getStyle().set("position", "absolute");
			marker.getStyle().set("top", topOffset + "px");
			marker.getStyle().set("left", left + "px");
			marker.getStyle().set("width", width + "px");
			marker.getStyle().set("height", LAYER_HEIGHT + "px");
			marker.getStyle().set("border-right", "1px solid " + LINE_COLOR);
			marker.getStyle().set("box-sizing", "border-box");
			Span labelSpan = new Span(label);
			labelSpan.getStyle().set("font-size", FONT_SIZE);
			labelSpan.getStyle().set("line-height", LAYER_HEIGHT + "px");
			labelSpan.getStyle().set("display", "inline-block");
			labelSpan.getStyle().set("padding-left", "4px");
			marker.add(labelSpan);
			add(marker);
			current = quarterEnd.plusDays(1);
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
		renderLayer(startDate, endDate, totalDays, ChronoUnit.YEARS, 0, "yyyy");
		renderLayer(startDate, endDate, totalDays, ChronoUnit.MONTHS, LAYER_HEIGHT, "LLL yyyy");
		renderQuarters(startDate, endDate, totalDays, LAYER_HEIGHT * 2);
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
}
