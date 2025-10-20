package tech.derbent.app.meetings.view;

import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.utils.Check;
import tech.derbent.app.meetings.domain.CMeeting;

/** CMeetingCard - UI component representing a meeting card in Kanban boards. Layer: View (MVC) Displays meeting information in a compact card format
 * suitable for kanban columns. */
public class CMeetingCard extends Div {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingCard.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
	private final CMeeting meeting;

	/** Constructor for CMeetingCard.
	 * @param meeting the meeting to display */
	public CMeetingCard(final CMeeting meeting) {
		Check.notNull(meeting, "Meeting cannot be null");
		this.meeting = meeting;
		initializeCard();
	}

	/** Gets the meeting displayed by this card.
	 * @return the meeting */
	public CMeeting getMeeting() { return meeting; }

	/** Initializes the card components and layout. */
	private void initializeCard() {
		// Set CSS class for styling
		addClassName("meeting-card");
		// Create title
		final H4 title = new H4(meeting.getName() != null ? meeting.getName() : "Unnamed Meeting");
		title.addClassName("meeting-card-title");
		// Create description if available
		if ((meeting.getDescription() != null) && !meeting.getDescription().trim().isEmpty()) {
			final Span description = new Span(meeting.getDescription());
			description.addClassName("meeting-card-description");
			add(title, description);
		} else {
			add(title);
		}
		// Create meeting date info
		if (meeting.getMeetingDate() != null) {
			final Span dateInfo = new Span("📅 " + meeting.getMeetingDate().format(DATE_FORMATTER));
			dateInfo.addClassName("meeting-card-date");
			add(dateInfo);
		}
		// Create meeting type info
		if (meeting.getMeetingType() != null) {
			final Span typeInfo = new Span("Type: " + meeting.getMeetingType().getName());
			typeInfo.addClassName("meeting-card-type");
			add(typeInfo);
		}
		// Create participants count
		if ((meeting.getParticipants() != null) && !meeting.getParticipants().isEmpty()) {
			final Span participantsInfo = new Span("👥 " + meeting.getParticipants().size() + " participants");
			participantsInfo.addClassName("meeting-card-participants");
			add(participantsInfo);
		}
		// Create status badge
		if (meeting.getStatus() != null) {
			final Span statusBadge = new Span(meeting.getStatus().getName());
			statusBadge.addClassName("meeting-card-status");
			add(statusBadge);
		}
	}
}
