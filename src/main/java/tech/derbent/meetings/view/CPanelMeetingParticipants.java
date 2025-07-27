package tech.derbent.meetings.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelMeetingParticipants - Panel for grouping participant-related fields of CMeeting
 * entity. Layer: View (MVC) Groups fields: participants
 */
public class CPanelMeetingParticipants extends CPanelMeetingBase {

	private static final long serialVersionUID = 1L;

	private MultiSelectComboBox<CUser> participantsField;

	private final CUserService userService;

	public CPanelMeetingParticipants(final CMeeting currentEntity,
		final BeanValidationBinder<CMeeting> beanValidationBinder,
		final CMeetingService entityService, final CMeetingTypeService meetingTypeService,
		final CUserService userService) {
		super("Participants", currentEntity, beanValidationBinder, entityService,
			meetingTypeService);
		this.userService = userService;
	}

	private void createParticipantsField() {
		participantsField = new MultiSelectComboBox<>("Participants");
		participantsField.setHelperText("Select users participating in the meeting");
		participantsField.setWidthFull();

		// Load users from userService
		try {
			final var users =
				userService.list(org.springframework.data.domain.Pageable.unpaged());
			participantsField.setItems(users);
			participantsField.setItemLabelGenerator(user -> user.getName() != null
				? user.getName() : "User #" + user.getId());
			LOGGER.debug("Loaded {} users for participants selection", users.size());
		} catch (final Exception e) {
			LOGGER.error("Error loading users for participants field: {}", e.getMessage(),
				e);
			participantsField.setItems();
		}
		// Manual binding for participants field with proper type handling
		getBinder().forField(participantsField)
			.withConverter(
				(final Set<CUser> selectedUsers) -> selectedUsers != null
					? new HashSet<>(selectedUsers) : new HashSet<CUser>(),
				(final Set<CUser> participantsSet) -> participantsSet != null
					? participantsSet : Set.<CUser>of())
			.bind(CMeeting::getParticipants, CMeeting::setParticipants);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Participants fields - attendee information
		setEntityFields(List.of("participants"));
		createParticipantsField();
		getBaseLayout().add(participantsField);
	}
}