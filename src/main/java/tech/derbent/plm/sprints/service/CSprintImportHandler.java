package tech.derbent.plm.sprints.service;

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
import tech.derbent.api.imports.service.CImportParsers;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintType;

/** Imports {@link CSprint} rows from Excel into the active project. */
@Service
@Profile({"derbent", "default"})
public class CSprintImportHandler extends CAbstractExcelImportHandler<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintImportHandler.class);

	private final CSprintService sprintService;
	private final CSprintTypeService typeService;
	private final CProjectItemStatusService statusService;

	public CSprintImportHandler(final CSprintService sprintService, final CSprintTypeService typeService,
			final CProjectItemStatusService statusService) {
		this.sprintService = sprintService;
		this.typeService = typeService;
		this.statusService = statusService;
	}

	@Override
	public Class<CSprint> getEntityClass() { return CSprint.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Type", "entitytype");
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
		final CSprint sprint = sprintService.findByNameAndProject(name, project)
				.orElseGet(() -> new CSprint(name, project));

		row.optionalString("description").ifPresent(sprint::setDescription);
		row.optionalString("sprintgoal").ifPresent(sprint::setSprintGoal);
		row.optionalString("definitionofdone").ifPresent(sprint::setDefinitionOfDone);
		row.optionalString("retrospectivenotes").ifPresent(sprint::setRetrospectiveNotes);

		row.optionalString("color").ifPresent(sprint::setColor);
		row.optionalInt("velocity").ifPresent(sprint::setVelocity);
		row.optionalLocalDate("startdate").ifPresent(sprint::setStartDate);

		final String endDateRaw = row.string("enddate");
		if (!endDateRaw.isBlank()) {
			final var endDate = CImportParsers.tryParseLocalDate(endDateRaw).orElse(null);
			if (endDate == null) {
				return CImportRowResult.error(rowNumber, "Invalid end date: " + endDateRaw, rowData);
			}
			sprint.setEndDate(endDate);
		}

		final String statusName = row.string("status");
		if (!statusName.isBlank()) {
			final CProjectItemStatus status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
			if (status == null) {
				return CImportRowResult.error(rowNumber,
						"Status '" + statusName + "' not found. Create it before importing.", rowData);
			}
			sprint.setStatus(status);
		}

		final String typeName = row.string("entitytype");
		if (!typeName.isBlank()) {
			final CSprintType type = typeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
			if (type == null) {
				return CImportRowResult.error(rowNumber,
						"Sprint Type '" + typeName + "' not found. Create it before importing.", rowData);
			}
			sprint.setEntityType(type);
		}

		if (!options.isDryRun()) {
			sprintService.save(sprint);
		}
		LOGGER.debug("Imported sprint '{}' (row {})", name, rowNumber);
		return CImportRowResult.success(rowNumber, name);
	}
}
