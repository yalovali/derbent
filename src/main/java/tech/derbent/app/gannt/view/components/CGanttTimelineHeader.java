package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class CGanttTimelineHeader extends Div {

    private static final long serialVersionUID = 1L;

    public static final String WIDTH_UNIT = "px";

    private final LinkedHashMap<LocalDate, Integer> dateToPixelMap = new LinkedHashMap<>();
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int totalWidth;

    public CGanttTimelineHeader(LocalDate startDate, LocalDate endDate, int totalWidth) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalWidth = totalWidth;
        addClassName("gantt-timeline-header");
        setWidth(totalWidth + WIDTH_UNIT);
        setHeight("80px");
        getStyle().set("position", "relative");
        renderTimeline();
    }

    public LinkedHashMap<LocalDate, Integer> getDateToPixelMap() {
        return dateToPixelMap;
    }

    private void renderTimeline() {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays <= 0) return;

        for (int i = 0; i < totalDays; i++) {
            LocalDate current = startDate.plusDays(i);
            int pixel = (int) ((i * totalWidth) / (double) totalDays);
            dateToPixelMap.put(current, pixel);
        }

        renderYears(totalDays);
        renderMonths(totalDays);
        renderQuarters(totalDays);

        renderMonthLines(totalDays);
        renderQuarterLines(totalDays);
    }

    private void renderYears(long totalDays) {
        LocalDate current = startDate.withDayOfYear(1);
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
            marker.getStyle().set("left", left + WIDTH_UNIT);
            marker.getStyle().set("width", width + WIDTH_UNIT);
            marker.setText(String.valueOf(current.getYear()));
            add(marker);
            current = current.plusYears(1);
        }
    }

    private void renderMonths(long totalDays) {
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
            marker.getStyle().set("left", left + WIDTH_UNIT);
            marker.getStyle().set("width", width + WIDTH_UNIT);
            marker.setText(ym.format(DateTimeFormatter.ofPattern("LLL yyyy")));
            add(marker);
            current = current.plusMonths(1);
        }
    }

    private void renderQuarters(long totalDays) {
        LocalDate current = startDate.withMonth(((startDate.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            int year = current.getYear();
            int quarter = ((current.getMonthValue() - 1) / 3) + 1;
            LocalDate quarterEnd = current.plusMonths(3).minusDays(1);
            LocalDate visibleStart = current.isBefore(startDate) ? startDate : current;
            LocalDate visibleEnd = quarterEnd.isAfter(endDate) ? endDate : quarterEnd;
            long offset = ChronoUnit.DAYS.between(startDate, visibleStart);
            long duration = ChronoUnit.DAYS.between(visibleStart, visibleEnd) + 1;
            int left = (int) ((offset * totalWidth) / (double) totalDays);
            int width = (int) ((duration * totalWidth) / (double) totalDays);

            Div marker = new Div();
            marker.addClassName("gantt-timeline-quarter");
            marker.getStyle().set("left", left + WIDTH_UNIT);
            marker.getStyle().set("width", width + WIDTH_UNIT);
            marker.setText("Q" + quarter + " " + year);
            add(marker);
            current = current.plusMonths(3);
        }
    }

    private void renderMonthLines(long totalDays) {
        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            int offset = (int) ((ChronoUnit.DAYS.between(startDate, current) * totalWidth) / (double) totalDays);
            Div line = new Div();
            line.addClassName("gantt-grid-line");
            line.addClassName("month");
            line.getStyle().set("left", offset + WIDTH_UNIT);
            add(line);
            current = current.plusMonths(1);
        }
    }

    private void renderQuarterLines(long totalDays) {
        LocalDate current = startDate.withMonth(((startDate.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            int offset = (int) ((ChronoUnit.DAYS.between(startDate, current) * totalWidth) / (double) totalDays);
            Div line = new Div();
            line.addClassName("gantt-grid-line");
            line.addClassName("quarter");
            line.getStyle().set("left", offset + WIDTH_UNIT);
            add(line);
            current = current.plusMonths(3);
        }
    }
}
