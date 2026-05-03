package tech.derbent.plm.meetings.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.domain.CMeetingType;

/** Imports CMeeting rows from Excel (project items). */
@Service
@Profile({"derbent", "default"})
public class CMeetingImportHandler implements IEntityImportHandler<CMeeting> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingImportHandler.class);
	private static final DateTimeFormatter[] DATE_FORMATS = {
			DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy"),
			DateTimeFormatter.ofPattern("MM/dd/yyyy"),
			DateTimeFormatter.ofPattern("d.M.yyyy"),
	};
	private static final DateTimeFormatter[] TIME_FORMATS = {
			DateTimeFormatter.ofPattern("HH:mm"),
			DateTimeFormatter.ofPattern("HH:mm:ss"),
	};

	private final CMeetingService meetingService;
	private final CMeetingTypeService meetingTypeService;
	private final CProjectItemStatusService statusService;
	private final CActivityService activityService;
	private final IUserRepository userRepository;

	public CMeetingImportHandler(final CMeetingService meetingService,
			final CMeetingTypeService meetingTypeService,
			final CProjectItemStatusService statusService,
			final CActivityService activityService,
			final IUserRepository userRepository) {
		this.meetingService = meetingService;
		this.meetingTypeService = meetingTypeService;
		this.statusService = statusService;
		this.activityService = activityService;
		this.userRepository = userRepository;
	}

	@Override
	public Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CMeeting");
		names.add("Meeting");
		names.add("Meetings");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CMeeting.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CMeeting.class);
			if (singular != null && !singular.isBlank()) {
				names.add(singular);
			}
			if (plural != null && !plural.isBlank()) {
				names.add(plural);
			}
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	@Override
	public Map<String, String> getColumnAliases() {
		return Map.ofEntries(
				Map.entry("Name", "name"),
				Map.entry("Description", "description"),
				Map.entry("Status", "status"),
				Map.entry("Meeting Type", "entitytype"),
				Map.entry("Type", "entitytype"),
				Map.entry("Start Date", "startdate"),
				Map.entry("Start Time", "starttime"),
				Map.entry("End Date", "enddate"),
				Map.entry("End Time", "endtime"),
				Map.entry("Location", "location"),
				Map.entry("Agenda", "agenda"),
				Map.entry("Minutes", "minutes"),
				Map.entry("Linked Element", "linkedelement"),
				Map.entry("Participants", "participants"),
				Map.entry("Attendees", "attendees"),
				Map.entry("Related Activity", "relatedactivity"),
				Map.entry("Story Points", "storypoint"),
				Map.entry("Assigned To", "assignedto")
		);
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("name");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final String name = rowData.getOrDefault("name", "").trim();
		if (name.isBlank()) {
			return CImportRowResult.error(rowNumber, "Name is required", rowData);
		}
		final CMeeting meeting = meetingService.findByNameAndProject(name, project)
				.orElseGet(() -> new CMeeting(name, project));

		final String description = rowData.getOrDefault("description", "").trim();
		if (!description.isBlank()) {
			meeting.setDescription(description);
		}

		final String statusName = rowData.getOrDefault("status", "").trim();
		if (!statusName.isBlank()) {
			final CProjectItemStatus status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
			if (status == null) {
				return CImportRowResult.error(rowNumber, "Status '" + statusName + "' not found", rowData);
			}
			meeting.setStatus(status);
		}

		final String typeName = rowData.getOrDefault("entitytype", "").trim();
		if (!typeName.isBlank()) {
			final CMeetingType type = meetingTypeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
			if (type == null) {
				return CImportRowResult.error(rowNumber, "Meeting Type '" + typeName + "' not found", rowData);
			}
			meeting.setEntityType(type);
		}

		final String startDateStr = rowData.getOrDefault("startdate", "").trim();
		if (!startDateStr.isBlank()) {
			final LocalDate startDate = parseDate(startDateStr);
			if (startDate == null) {
				return CImportRowResult.error(rowNumber, "Cannot parse start date '" + startDateStr + "'", rowData);
			}
			meeting.setStartDate(startDate);
		}
		final String startTimeStr = rowData.getOrDefault("starttime", "").trim();
		if (!startTimeStr.isBlank()) {
			final LocalTime startTime = parseTime(startTimeStr);
			if (startTime == null) {
				return CImportRowResult.error(rowNumber, "Cannot parse start time '" + startTimeStr + "' (use HH:mm)", rowData);
			}
			meeting.setStartTime(startTime);
		}
		final String endDateStr = rowData.getOrDefault("enddate", "").trim();
		if (!endDateStr.isBlank()) {
			final LocalDate endDate = parseDate(endDateStr);
			if (endDate == null) {
				return CImportRowResult.error(rowNumber, "Cannot parse end date '" + endDateStr + "'", rowData);
			}
			meeting.setEndDate(endDate);
		}
		final String endTimeStr = rowData.getOrDefault("endtime", "").trim();
		if (!endTimeStr.isBlank()) {
			final LocalTime endTime = parseTime(endTimeStr);
			if (endTime == null) {
				return CImportRowResult.error(rowNumber, "Cannot parse end time '" + endTimeStr + "' (use HH:mm)", rowData);
			}
			meeting.setEndTime(endTime);
		}

		final String location = rowData.getOrDefault("location", "").trim();
		if (!location.isBlank()) {
			meeting.setLocation(location);
		}
		final String agenda = rowData.getOrDefault("agenda", "").trim();
		if (!agenda.isBlank()) {
			meeting.setAgenda(agenda);
		}
		final String minutes = rowData.getOrDefault("minutes", "").trim();
		if (!minutes.isBlank()) {
			meeting.setMinutes(minutes);
		}
		final String linkedElement = rowData.getOrDefault("linkedelement", "").trim();
		if (!linkedElement.isBlank()) {
			meeting.setLinkedElement(linkedElement);
		}

		final String relatedActivityName = rowData.getOrDefault("relatedactivity", "").trim();
		if (!relatedActivityName.isBlank()) {
			final var actOpt = activityService.findByNameAndProject(relatedActivityName, project);
			if (actOpt.isEmpty()) {
				return CImportRowResult.error(rowNumber, "Related Activity '" + relatedActivityName + "' not found", rowData);
			}
			meeting.setRelatedActivity(actOpt.get());
		}

		final String storyPointStr = rowData.getOrDefault("storypoint", "").trim();
		if (!storyPointStr.isBlank()) {
			try {
				meeting.setStoryPoint(Long.valueOf(storyPointStr));
			} catch (final Exception e) {
				return CImportRowResult.error(rowNumber, "Invalid story points: " + storyPointStr, rowData);
			}
		}

		final String assignedToLogin = rowData.getOrDefault("assignedto", "").trim();
		if (!assignedToLogin.isBlank()) {
			final var userOpt = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin);
			if (userOpt.isEmpty()) {
				return CImportRowResult.error(rowNumber,
						"Assigned To user '" + assignedToLogin + "' not found in company.", rowData);
			}
			meeting.setAssignedTo(userOpt.get());
		}

		final String participantsStr = rowData.getOrDefault("participants", "").trim();
		if (!participantsStr.isBlank()) {
			final Set<CUser> participants = resolveUsersCsv(participantsStr, project.getCompany().getId(), rowNumber, rowData);
			if (participants == null) {
				return CImportRowResult.error(rowNumber, "Participants contains unknown users", rowData);
			}
			meeting.setParticipants(participants);
		}
		final String attendeesStr = rowData.getOrDefault("attendees", "").trim();
		if (!attendeesStr.isBlank()) {
			final Set<CUser> attendees = resolveUsersCsv(attendeesStr, project.getCompany().getId(), rowNumber, rowData);
			if (attendees == null) {
				return CImportRowResult.error(rowNumber, "Attendees contains unknown users", rowData);
			}
			meeting.setAttendees(attendees);
		}

		if (!options.isDryRun()) {
			try {
				meetingService.save(meeting);
			} catch (final Exception e) {
				LOGGER.error("Failed to save meeting '{}' reason={}", name, e.getMessage());
				return CImportRowResult.error(rowNumber, "Save failed: " + e.getMessage(), rowData);
			}
		}
		LOGGER.debug("Imported meeting '{}' (row {})", name, rowNumber);
		return CImportRowResult.success(rowNumber, name);
	}

	private Set<CUser> resolveUsersCsv(final String value, final Long companyId, final int rowNumber,
			final Map<String, String> rowData) {
		final Set<CUser> users = new HashSet<>();
		for (final String token : value.split("[,;]")) {
			final String login = token.trim();
			if (login.isBlank()) {
				continue;
			}
			final var userOpt = userRepository.findByUsernameIgnoreCase(companyId, login);
			if (userOpt.isEmpty()) {
				// WHY: user logins are the only stable identifier available in templated init workbooks.
				LOGGER.warn("Import row error (sheet=Meeting, row={}): user '{}' not found", rowNumber, login);
				return null;
			}
			users.add(userOpt.get());
		}
		return users;
	}

	private static LocalDate parseDate(final String value) {
		for (final DateTimeFormatter fmt : DATE_FORMATS) {
			try {
				return LocalDate.parse(value, fmt);
			} catch (final DateTimeParseException ignored) { /* try next */ }
		}
		return null;
	}

	private static LocalTime parseTime(final String value) {
		for (final DateTimeFormatter fmt : TIME_FORMATS) {
			try {
				return LocalTime.parse(value, fmt);
			} catch (final DateTimeParseException ignored) { /* try next */ }
		}
		return null;
	}
}
