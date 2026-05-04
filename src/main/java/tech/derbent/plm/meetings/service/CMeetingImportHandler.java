package tech.derbent.plm.meetings.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.domain.CMeetingType;

/** Imports CMeeting rows from Excel (project items). Extends CAbstractExcelImportHandler so @AMetaData displayNames are automatically registered as
 * column aliases; column aliases here cover additional synonyms not present in the entity. */
@Service
@Profile ({
		"derbent", "default"
})
public class CMeetingImportHandler extends CProjectItemImportHandler<CMeeting, CMeetingType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMeetingImportHandler.class);
	private final CActivityService activityService;
	private final CMeetingService meetingService;
	private final CMeetingTypeService meetingTypeService;

	public CMeetingImportHandler(final CMeetingService meetingService, final CMeetingTypeService meetingTypeService,
			final CProjectItemStatusService statusService, final CActivityService activityService,
			final IUserRepository userRepository, final CImportProjectResolver projectResolver) {
		super(statusService, userRepository, projectResolver);
		this.meetingService = meetingService;
		this.meetingTypeService = meetingTypeService;
		this.activityService = activityService;
	}

	@Override
	protected void applyExtraFields(final CMeeting entity, final CExcelRow row, final CProject<?> project,
			final int rowNumber, final Map<String, String> rowData) {
		applyMetaFieldsDeclaredOn(entity, row, CMeeting.class);
		final String relatedActivityName = row.string("relatedactivity");
		if (!relatedActivityName.isBlank()) {
			final var act = activityService.findByNameAndProject(relatedActivityName, project).orElse(null);
			if (act == null) {
				throw new IllegalArgumentException("Related Activity '" + relatedActivityName + "' not found");
			}
			entity.setRelatedActivity(act);
		}
		final String participantsStr = row.string("participants");
		if (!participantsStr.isBlank()) {
			entity.setParticipants(resolveUsersCsv(participantsStr, project.getCompany().getId(), rowNumber));
		}
		final String attendeesStr = row.string("attendees");
		if (!attendeesStr.isBlank()) {
			entity.setAttendees(resolveUsersCsv(attendeesStr, project.getCompany().getId(), rowNumber));
		}
	}

	@Override
	protected CMeeting createNew(final String name, final CProject<?> project) {
		return new CMeeting(name, project);
	}

	@Override
	protected Optional<CMeeting> findByNameAndProject(final String name, final CProject<?> project) {
		return meetingService.findByNameAndProject(name, project);
	}

	@Override
	protected Optional<CMeetingType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return meetingTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		// WHY: "Meeting Type" and "Related Activity" are additional synonyms; "Type" is inherited from parent.
		final java.util.LinkedHashMap<String, String> aliases = new java.util.LinkedHashMap<>(super.getAdditionalColumnAliases());
		aliases.put("Meeting Type", "entitytype");
		aliases.put("Related Activity", "relatedactivity");
		return Map.copyOf(aliases);
	}

	@Override
	public Class<CMeeting> getEntityClass() { return CMeeting.class; }

	@Override
	protected Class<CMeetingType> getTypeClass() { return CMeetingType.class; }

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
				throw new IllegalArgumentException("User '" + login + "' not found");
			}
			users.add(userOpt.get());
		}
		return users;
	}

	@Override
	protected void save(final CMeeting entity) {
		meetingService.save(entity);
	}
}
