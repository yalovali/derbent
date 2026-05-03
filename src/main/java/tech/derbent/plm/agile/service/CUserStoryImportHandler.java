package tech.derbent.plm.agile.service;

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
import tech.derbent.api.imports.service.CAbstractExcelImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.domain.CUserStoryType;

/** Imports {@link CUserStory} rows from Excel into the active project. */
@Service
@Profile({"derbent", "default"})
public class CUserStoryImportHandler extends CAbstractExcelImportHandler<CUserStory> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserStoryImportHandler.class);

	private final CUserStoryService userStoryService;
	private final CUserStoryTypeService typeService;
	private final CProjectItemStatusService statusService;
	private final IUserRepository userRepository;

	public CUserStoryImportHandler(final CUserStoryService userStoryService, final CUserStoryTypeService typeService,
			final CProjectItemStatusService statusService, final IUserRepository userRepository) {
		this.userStoryService = userStoryService;
		this.typeService = typeService;
		this.statusService = statusService;
		this.userRepository = userRepository;
	}

	@Override
	public Class<CUserStory> getEntityClass() { return CUserStory.class; }

	@Override
	public Map<String, String> getColumnAliases() {
		// WHY: item type columns are domain-specific ("User Story Type", "Ticket Type", etc.) but the field token is consistently "entityType".
		return Map.of(
				"User Story Type", "entitytype",
				"Type", "entitytype",
				"Progress %", "progresspercentage",
				"Story Points", "storypoint");
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("name");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final CExcelRow row = row(rowData);
		final String name = row.string("name");
		if (name.isBlank()) {
			return CImportRowResult.error(rowNumber, "Name is required", rowData);
		}
		if (project.getCompany() == null) {
			return CImportRowResult.error(rowNumber, "Project company is required", rowData);
		}

		// WHY: system_init.xlsx is imported automatically after DB reset and can also be imported manually.
		final CUserStory story = userStoryService.findByNameAndProject(name, project)
				.orElseGet(() -> new CUserStory(name, project));

		row.optionalString("description").ifPresent(story::setDescription);
		row.optionalString("acceptancecriteria").ifPresent(story::setAcceptanceCriteria);
		row.optionalString("notes").ifPresent(story::setNotes);
		row.optionalString("results").ifPresent(story::setResults);

		row.optionalLong("storypoint").ifPresent(story::setStoryPoint);
		row.optionalLocalDate("startdate").ifPresent(story::setStartDate);
		row.optionalLocalDate("duedate").ifPresent(story::setDueDate);
		row.optionalLocalDate("completiondate").ifPresent(story::setCompletionDate);
		row.optionalInt("progresspercentage").ifPresent(story::setProgressPercentage);

		final String statusName = row.string("status");
		if (!statusName.isBlank()) {
			final CProjectItemStatus status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
			if (status == null) {
				return CImportRowResult.error(rowNumber,
						"Status '" + statusName + "' not found. Create it before importing (or enable auto-create lookups).", rowData);
			}
			story.setStatus(status);
		}

		final String typeName = row.string("entitytype");
		if (!typeName.isBlank()) {
			final CUserStoryType type = typeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
			if (type == null) {
				return CImportRowResult.error(rowNumber,
						"User Story Type '" + typeName + "' not found. Create it before importing (or enable auto-create lookups).",
						rowData);
			}
			story.setEntityType(type);
		}

		final String assignedToLogin = row.string("assignedto");
		if (!assignedToLogin.isBlank()) {
			final CUser user = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin).orElse(null);
			if (user == null) {
				return CImportRowResult.error(rowNumber,
						"Assigned user '" + assignedToLogin + "' not found in company. Create it before importing.", rowData);
			}
			story.setAssignedTo(user);
		}

		if (!options.isDryRun()) {
			userStoryService.save(story);
		}
		LOGGER.debug("Imported user story '{}' (row {})", name, rowNumber);
		return CImportRowResult.success(rowNumber, name);
	}
}
