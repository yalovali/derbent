package tech.derbent.plm.meetings.view;

import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.plm.meetings.domain.CMeeting;

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

	private static final long serialVersionUID = 1L;

	/** Creates a new meeting widget for the specified meeting.
	 * @param meeting the meeting to display in the widget */
	public CComponentWidgetMeeting(final CMeeting meeting) {
		super(meeting);
	}

	/** Creates the second line with meeting agenda and location.
	 * @throws Exception */
	@Override
	protected void createSecondLine() throws Exception {
		super.createSecondLine();
		final String location = getEntity().getLocation();
		layoutLineTwo.add(new CLabelEntity("Location: " + (location != null ? location : "N/A")));
	}
}
