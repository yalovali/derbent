package tech.derbent.meetings.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelMeetingParticipants - Panel for grouping participant-related fields of CMeeting
 * entity. Layer: View (MVC) Groups fields: participants, attendees
 */
public class CPanelMeetingParticipants extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	private MultiSelectComboBox<CUser> participantsField;

	private MultiSelectComboBox<CUser> attendeesField;

	private final CUserService userService;

	public CPanelMeetingParticipants(final CMeeting currentEntity,
		final CEnhancedBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService, final CMeetingTypeService meetingTypeService,
		final CUserService userService) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		super("Participants & Attendees", currentEntity, beanValidationBinder,
			entityService, meetingTypeService);
		this.userService = userService;
		initPanel();
	}

	private void createAttendeesField() {
		attendeesField = new MultiSelectComboBox<>("Attendees");
		attendeesField.setHelperText("Select users who actually attended the meeting");
		attendeesField.setWidthFull();

		// Load users from userService
		try {
			final var users = userService.list(Pageable.unpaged());
			attendeesField.setItems(users);
			attendeesField.setItemLabelGenerator(user -> user.getName() != null
				? user.getName() : "User #" + user.getId());
			getBinder().forField(attendeesField).bind(CMeeting::getAttendees,
				CMeeting::setAttendees);
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind attendees field: {} - using simple binding fallback",
				e.getMessage());
		}
	}

	private void createParticipantsField() {
		participantsField = new MultiSelectComboBox<>("Participants");
		participantsField
			.setHelperText("Select users invited to participate in the meeting");
		participantsField.setWidthFull();

		// Load users from userService
		try {
			final List<CUser> users = userService.list(Pageable.unpaged());
			// users.forEach(user -> LOGGER.debug("User: {} - {}", user.getClass(),
			// user));
			participantsField.setItems(users);
			participantsField.setItemLabelGenerator(user -> {
				// LOGGER.debug("LabelGenerator input: {}", user.getClass());
				return user.getName() != null ? user.getName() : "User #" + user.getId();
			});
			getBinder().forField(participantsField).bind(CMeeting::getParticipants,
				CMeeting::setParticipants);
			LOGGER.debug("Successfully bound participants field");
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind participants field: {} - using simple binding fallback",
				e.getMessage());
		}
	}

	@Override
	protected void updatePanelEntityFields() {
		// Participants and attendees fields - meeting people information
		setEntityFields(List.of());
		// setEntityFields(new ArrayList<>());
		createParticipantsField();
		createAttendeesField();
		addToContent(participantsField);
		addToContent(attendeesField);
	}
}
