package tech.derbent.plm.agile.service;

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
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CEpicType;

/**
 * Imports {@link CEpic} rows from Excel into the active project.
 *
 * Column aliases are built automatically from @AMetaData; additional synonyms are declared here.
 * Epics are the top-level items in the agile hierarchy (Epic → Feature → User Story).
 */
@Service
@Profile({"derbent", "default"})
public class CEpicImportHandler extends CAbstractExcelImportHandler<CEpic> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CEpicImportHandler.class);

    private final CEpicService epicService;
    private final CEpicTypeService typeService;
    private final CProjectItemStatusService statusService;
    private final IUserRepository userRepository;

    public CEpicImportHandler(final CEpicService epicService, final CEpicTypeService typeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        this.epicService = epicService;
        this.typeService = typeService;
        this.statusService = statusService;
        this.userRepository = userRepository;
    }

    @Override
    public Class<CEpic> getEntityClass() { return CEpic.class; }

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
        final var row = row(rowData);
        final String name = row.string("name");
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        if (project.getCompany() == null) {
            return CImportRowResult.error(rowNumber, "Project company is required", rowData);
        }
        // WHY: system_init.xlsx is re-runnable; upsert-by-name avoids unique constraint failures.
        final CEpic epic = epicService.findByNameAndProject(name, project)
                .orElseGet(() -> new CEpic(name, project));

        row.optionalString("description").ifPresent(epic::setDescription);
        row.optionalString("acceptancecriteria").ifPresent(epic::setAcceptanceCriteria);
        row.optionalString("notes").ifPresent(epic::setNotes);
        row.optionalString("results").ifPresent(epic::setResults);
        row.optionalLong("storypoint").ifPresent(epic::setStoryPoint);
        row.optionalLocalDate("startdate").ifPresent(epic::setStartDate);
        row.optionalLocalDate("duedate").ifPresent(epic::setDueDate);
        row.optionalLocalDate("completiondate").ifPresent(epic::setCompletionDate);
        row.optionalInt("progresspercentage").ifPresent(epic::setProgressPercentage);
        row.optionalBigDecimal("estimatedhours").ifPresent(epic::setEstimatedHours);
        row.optionalBigDecimal("estimatedcost").ifPresent(epic::setEstimatedCost);
        row.optionalBigDecimal("actualhours").ifPresent(epic::setActualHours);
        row.optionalBigDecimal("actualcost").ifPresent(epic::setActualCost);

        final String statusName = row.string("status");
        if (!statusName.isBlank()) {
            final var status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
            if (status == null) {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing.", rowData);
            }
            epic.setStatus(status);
        }

        final String typeName = row.string("entitytype");
        if (!typeName.isBlank()) {
            final CEpicType type = typeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
            if (type == null) {
                return CImportRowResult.error(rowNumber,
                        "Epic Type '" + typeName + "' not found. Create it before importing.", rowData);
            }
            epic.setEntityType(type);
        }

        final String assignedToLogin = row.string("assignedto");
        if (!assignedToLogin.isBlank()) {
            final CUser user = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin).orElse(null);
            if (user == null) {
                return CImportRowResult.error(rowNumber,
                        "Assigned user '" + assignedToLogin + "' not found in company. Create it before importing.", rowData);
            }
            epic.setAssignedTo(user);
        }

        if (!options.isDryRun()) {
            epicService.save(epic);
        }
        LOGGER.debug("Imported epic '{}' (row {})", name, rowNumber);
        return CImportRowResult.success(rowNumber, name);
    }
}
