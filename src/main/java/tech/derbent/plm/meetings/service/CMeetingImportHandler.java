package tech.derbent.plm.meetings.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CAbstractExcelImportHandler;
import tech.derbent.api.imports.service.CImportParsers;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.domain.CMeetingType;

/**
 * Imports CMeeting rows from Excel (project items).
 *
 * Extends CAbstractExcelImportHandler so @AMetaData displayNames are automatically registered as
 * column aliases; column aliases here cover additional synonyms not present in the entity.
 */
@Service
@Profile({"derbent", "default"})
public class CMeetingImportHandler extends CAbstractExcelImportHandler<CMeeting> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingImportHandler.class);

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
    protected Map<String, String> getAdditionalColumnAliases() {
        // WHY: "Meeting Type" and "Type" are common user-facing synonyms for the entitytype token.
        return Map.of(
                "Meeting Type", "entitytype",
                "Type", "entitytype",
                "Related Activity", "relatedactivity");
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
            final CImportOptions options) {
        final var row = row(rowData);
        final String name = row.string("name");
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        // WHY: upsert-by-name keeps the workbook re-runnable without constraint failures.
        final CMeeting meeting = meetingService.findByNameAndProject(name, project)
                .orElseGet(() -> new CMeeting(name, project));

        row.optionalString("description").ifPresent(meeting::setDescription);
        row.optionalString("location").ifPresent(meeting::setLocation);
        row.optionalString("agenda").ifPresent(meeting::setAgenda);
        row.optionalString("minutes").ifPresent(meeting::setMinutes);
        row.optionalString("linkedelement").ifPresent(meeting::setLinkedElement);
        row.optionalLong("storypoint").ifPresent(meeting::setStoryPoint);

        final String statusName = row.string("status");
        if (!statusName.isBlank()) {
            final var status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
            if (status == null) {
                return CImportRowResult.error(rowNumber, "Status '" + statusName + "' not found", rowData);
            }
            meeting.setStatus(status);
        }

        final String typeName = row.string("entitytype");
        if (!typeName.isBlank()) {
            final CMeetingType type = meetingTypeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
            if (type == null) {
                return CImportRowResult.error(rowNumber, "Meeting Type '" + typeName + "' not found", rowData);
            }
            meeting.setEntityType(type);
        }

        final String startDateRaw = row.string("startdate");
        if (!startDateRaw.isBlank()) {
            final var startDate = CImportParsers.tryParseLocalDate(startDateRaw).orElse(null);
            if (startDate == null) {
                return CImportRowResult.error(rowNumber, "Cannot parse start date '" + startDateRaw + "'", rowData);
            }
            meeting.setStartDate(startDate);
        }
        final String startTimeRaw = row.string("starttime");
        if (!startTimeRaw.isBlank()) {
            final var startTime = CImportParsers.tryParseLocalTime(startTimeRaw).orElse(null);
            if (startTime == null) {
                return CImportRowResult.error(rowNumber, "Cannot parse start time '" + startTimeRaw + "' (use HH:mm)", rowData);
            }
            meeting.setStartTime(startTime);
        }
        final String endDateRaw = row.string("enddate");
        if (!endDateRaw.isBlank()) {
            final var endDate = CImportParsers.tryParseLocalDate(endDateRaw).orElse(null);
            if (endDate == null) {
                return CImportRowResult.error(rowNumber, "Cannot parse end date '" + endDateRaw + "'", rowData);
            }
            meeting.setEndDate(endDate);
        }
        final String endTimeRaw = row.string("endtime");
        if (!endTimeRaw.isBlank()) {
            final var endTime = CImportParsers.tryParseLocalTime(endTimeRaw).orElse(null);
            if (endTime == null) {
                return CImportRowResult.error(rowNumber, "Cannot parse end time '" + endTimeRaw + "' (use HH:mm)", rowData);
            }
            meeting.setEndTime(endTime);
        }

        final String relatedActivityName = row.string("relatedactivity");
        if (!relatedActivityName.isBlank()) {
            final var actOpt = activityService.findByNameAndProject(relatedActivityName, project);
            if (actOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber, "Related Activity '" + relatedActivityName + "' not found", rowData);
            }
            meeting.setRelatedActivity(actOpt.get());
        }

        final String assignedToLogin = row.string("assignedto");
        if (!assignedToLogin.isBlank()) {
            final var userOpt = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin);
            if (userOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber, "Assigned To user '" + assignedToLogin + "' not found in company.", rowData);
            }
            meeting.setAssignedTo(userOpt.get());
        }

        final String participantsStr = row.string("participants");
        if (!participantsStr.isBlank()) {
            final Set<CUser> participants = resolveUsersCsv(participantsStr, project.getCompany().getId(), rowNumber);
            if (participants == null) {
                return CImportRowResult.error(rowNumber, "Participants contains unknown users", rowData);
            }
            meeting.setParticipants(participants);
        }
        final String attendeesStr = row.string("attendees");
        if (!attendeesStr.isBlank()) {
            final Set<CUser> attendees = resolveUsersCsv(attendeesStr, project.getCompany().getId(), rowNumber);
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

    private Set<CUser> resolveUsersCsv(final String value, final Long companyId, final int rowNumber) {
        final Set<CUser> users = new HashSet<>();
        for (final String token : value.split("[,;]")) {
            final String login = token.trim();
            if (login.isBlank()) {
                continue;
            }
            final var userOpt = userRepository.findByUsernameIgnoreCase(companyId, login);
            if (userOpt.isEmpty()) {
                LOGGER.warn("Import row {}: user '{}' not found", rowNumber, login);
                return null;
            }
            users.add(userOpt.get());
        }
        return users;
    }
}
