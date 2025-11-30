package tech.derbent.app.meetings.view;

import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.app.meetings.domain.CMeeting;

/** CComponentWidgetMeeting - Widget component for displaying Meeting entities in grids.
 * <p>
 * This widget displays meeting information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Meeting name with calendar icon and color</li>
 * <li><b>Row 2:</b> Meeting agenda (truncated with ellipsis) and location</li>
 * <li><b>Row 3:</b> Status badge, responsible user, date/time range</li>
 * </ul>
 * </p>
 * <p>
 * Extends CComponentWidgetEntityOfProject and adds meeting-specific information like location and time.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject */
public class CComponentWidgetMeeting extends CComponentWidgetEntityOfProject<CMeeting> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetMeeting.class);
	private static final long serialVersionUID = 1L;

	/** Creates a new meeting widget for the specified meeting.
	 * @param meeting the meeting to display in the widget */
	public CComponentWidgetMeeting(final CMeeting meeting) {
		super(meeting);
		addEditAction();
		addDeleteAction();
	}

	/** Creates the second line with meeting agenda and location. */
	@Override
	protected void createSecondLine() {
		final CMeeting meeting = getEntity();
		if (meeting == null) {
			super.createSecondLine();
			return;
		}
		// Use agenda if available, otherwise fall back to description
		String content = meeting.getAgenda();
		if (content == null || content.isEmpty()) {
			content = meeting.getDescription();
		}
		if (content != null && content.length() > MAX_DESCRIPTION_LENGTH) {
			content = content.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
		}
		if (content == null || content.isEmpty()) {
			content = "(No Agenda)";
		}
		final CHorizontalLayout secondLine = new CHorizontalLayout();
		secondLine.setSpacing(true);
		secondLine.getStyle().set("font-size", "10pt");
		secondLine.getStyle().set("color", "#666");
		// Add agenda/description
		final Span agendaSpan = new Span(content);
		secondLine.add(agendaSpan);
		// Add location if available
		final String location = meeting.getLocation();
		if (location != null && !location.isEmpty()) {
			secondLine.add(createLocationDisplay(location));
		}
		layoutLineTwo.add(secondLine);
	}

	/** Creates the third line with status, responsible, date and time. */
	@Override
	protected void createThirdLine() {
		// Call parent implementation for status, responsible, and date range
		super.createThirdLine();
		// Add time display if available
		final CMeeting meeting = getEntity();
		if (meeting != null) {
			final LocalTime startTime = meeting.getStartTime();
			final LocalTime endTime = meeting.getEndTime();
			if (startTime != null || endTime != null) {
				layoutLineThree.add(createTimeDisplay(startTime, endTime));
			}
		}
	}

	/** Creates a styled location display component.
	 * @param location the location text to display
	 * @return the location display component */
	protected CHorizontalLayout createLocationDisplay(final String location) {
		final CHorizontalLayout locationLayout = new CHorizontalLayout();
		locationLayout.setSpacing(true);
		locationLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		locationLayout.getStyle().set("font-size", "10pt");
		locationLayout.getStyle().set("color", "#888");
		locationLayout.getStyle().set("margin-left", "8px");
		try {
			// Add location icon
			final Icon icon = CColorUtils.createStyledIcon("vaadin:map-marker");
			if (icon != null) {
				icon.getStyle().set("width", "14px");
				icon.getStyle().set("height", "14px");
				icon.getStyle().set("color", "#888");
				locationLayout.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create location icon: {}", e.getMessage());
		}
		// Add location text
		final Span locationSpan = new Span(location);
		locationLayout.add(locationSpan);
		return locationLayout;
	}

	/** Creates a styled time display component.
	 * @param startTime the start time (can be null)
	 * @param endTime   the end time (can be null)
	 * @return the time display component */
	protected CHorizontalLayout createTimeDisplay(final LocalTime startTime, final LocalTime endTime) {
		final CHorizontalLayout timeLayout = new CHorizontalLayout();
		timeLayout.setSpacing(true);
		timeLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		timeLayout.getStyle().set("font-size", "10pt");
		timeLayout.getStyle().set("color", "#666");
		try {
			// Add clock icon
			final Icon icon = CColorUtils.createStyledIcon("vaadin:clock");
			if (icon != null) {
				icon.getStyle().set("width", "14px");
				icon.getStyle().set("height", "14px");
				icon.getStyle().set("color", "#666");
				timeLayout.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create clock icon: {}", e.getMessage());
		}
		// Format time range
		final StringBuilder timeRange = new StringBuilder();
		if (startTime != null) {
			timeRange.append(startTime.toString());
		}
		if (startTime != null && endTime != null) {
			timeRange.append(" - ");
		}
		if (endTime != null) {
			timeRange.append(endTime.toString());
		}
		final Span timeSpan = new Span(timeRange.toString());
		timeLayout.add(timeSpan);
		return timeLayout;
	}
}
