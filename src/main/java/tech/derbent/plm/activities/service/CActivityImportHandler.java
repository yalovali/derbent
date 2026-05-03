package tech.derbent.plm.activities.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;

/**
 * Handles import of CActivity rows from Excel.
 *
 * Supported columns (case-insensitive, aliases mapped):
 *   name (required), description, status, type (Activity Type), dueDate, estimatedHours
 *
 * Relations resolved by name: status → CProjectItemStatusService, type → CActivityTypeService
 */
@Service
@Profile({"derbent", "default"})
public class CActivityImportHandler extends CAbstractExcelImportHandler<CActivity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityImportHandler.class);

    private final CActivityService activityService;
    private final CActivityTypeService activityTypeService;
    private final CActivityPriorityService priorityService;
    private final CProjectItemStatusService statusService;
    private final IUserRepository userRepository;

    public CActivityImportHandler(final CActivityService activityService,
            final CActivityTypeService activityTypeService,
            final CActivityPriorityService priorityService,
            final CProjectItemStatusService statusService,
            final IUserRepository userRepository) {
        this.activityService = activityService;
        this.activityTypeService = activityTypeService;
        this.priorityService = priorityService;
        this.statusService = statusService;
        this.userRepository = userRepository;
    }

    @Override
    public Class<CActivity> getEntityClass() { return CActivity.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of("Type", "entitytype");
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project,
            final int rowNumber, final CImportOptions options) {
        final String name = rowData.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        // WHY: system_init.xlsx is imported automatically after DB reset and can also be imported manually;
        // upsert-by-name keeps the workbook re-runnable without unique constraint failures.
        final CActivity activity = activityService.findByNameAndProject(name, project)
                .orElseGet(() -> new CActivity(name, project));
        // Resolve optional status by name
        final String statusName = rowData.getOrDefault("status", "").trim();
        if (!statusName.isBlank()) {
            final var statusOpt = statusService.findByNameAndCompany(statusName, project.getCompany());
            if (statusOpt.isPresent()) {
                activity.setStatus(statusOpt.get());
            } else {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing.", rowData);
            }
        }
        // Resolve optional activity type by name
        final String typeName = rowData.getOrDefault("entitytype", "").trim();
        if (!typeName.isBlank()) {
            final var typeOpt = activityTypeService.findByNameAndCompany(typeName, project.getCompany());
            if (typeOpt.isPresent()) {
                activity.setEntityType(typeOpt.get());
            } else {
                return CImportRowResult.error(rowNumber,
                        "Activity Type '" + typeName + "' not found. Create it before importing.", rowData);
            }
        }
        // Resolve optional assignee by login
        final String assignedToLogin = rowData.getOrDefault("assignedto", "").trim();
        if (!assignedToLogin.isBlank()) {
            // WHY: we resolve by login (not display name) because logins are stable and unique within a company.
            final var userOpt = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin);
            if (userOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Assigned To user '" + assignedToLogin + "' not found in company.", rowData);
            }
            activity.setAssignedTo(userOpt.get());
        }
        // Optional description
        final String description = rowData.getOrDefault("description", "").trim();
        if (!description.isBlank()) {
            activity.setDescription(description);
        }
        // Optional priority by name
        final String priorityName = rowData.getOrDefault("priority", "").trim();
        if (!priorityName.isBlank()) {
            final var priorityOpt = priorityService.findByNameAndCompany(priorityName, project.getCompany());
            if (priorityOpt.isPresent()) {
                activity.setPriority(priorityOpt.get());
            } else {
                return CImportRowResult.error(rowNumber,
                        "Priority '" + priorityName + "' not found. Create it before importing.", rowData);
            }
        }
        // Optional acceptance criteria / notes / results
        final String acceptanceCriteria = rowData.getOrDefault("acceptancecriteria", "").trim();
        if (!acceptanceCriteria.isBlank()) {
            activity.setAcceptanceCriteria(acceptanceCriteria);
        }
        final String notes = rowData.getOrDefault("notes", "").trim();
        if (!notes.isBlank()) {
            activity.setNotes(notes);
        }
        final String results = rowData.getOrDefault("results", "").trim();
        if (!results.isBlank()) {
            activity.setResults(results);
        }
        // Optional dates
        final String startDateStr = rowData.getOrDefault("startdate", "").trim();
        if (!startDateStr.isBlank()) {
            final LocalDate startDate = CImportParsers.tryParseLocalDate(startDateStr).orElse(null);
            if (startDate == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse start date '" + startDateStr + "'. Use yyyy-MM-dd or dd/MM/yyyy.", rowData);
            }
            activity.setStartDate(startDate);
        }
        final String dueDateStr = rowData.getOrDefault("duedate", "").trim();
        if (!dueDateStr.isBlank()) {
            final LocalDate dueDate = CImportParsers.tryParseLocalDate(dueDateStr).orElse(null);
            if (dueDate == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse due date '" + dueDateStr + "'. Use yyyy-MM-dd or dd/MM/yyyy.", rowData);
            }
            activity.setDueDate(dueDate);
        }
        final String completionDateStr = rowData.getOrDefault("completiondate", "").trim();
        if (!completionDateStr.isBlank()) {
            final LocalDate completionDate = CImportParsers.tryParseLocalDate(completionDateStr).orElse(null);
            if (completionDate == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse completion date '" + completionDateStr + "'. Use yyyy-MM-dd or dd/MM/yyyy.", rowData);
            }
            activity.setCompletionDate(completionDate);
        }
        // Optional progress / story points
        final String progressStr = rowData.getOrDefault("progresspercentage", "").trim();
        if (!progressStr.isBlank()) {
            try {
                activity.setProgressPercentage(Integer.valueOf(progressStr));
            } catch (final Exception e) {
                return CImportRowResult.error(rowNumber, "Invalid progress %: " + progressStr, rowData);
            }
        }
        final String storyPointStr = rowData.getOrDefault("storypoint", "").trim();
        if (!storyPointStr.isBlank()) {
            try {
                activity.setStoryPoint(Long.valueOf(storyPointStr));
            } catch (final Exception e) {
                return CImportRowResult.error(rowNumber, "Invalid story points: " + storyPointStr, rowData);
            }
        }
        // Optional estimated hours
        final String hoursStr = rowData.getOrDefault("estimatedhours", "").trim();
        if (!hoursStr.isBlank()) {
            try {
                activity.setEstimatedHours(new BigDecimal(hoursStr));
            } catch (final NumberFormatException e) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse estimated hours '" + hoursStr + "'. Use a decimal number.", rowData);
            }
        }
        if (!options.isDryRun()) {
            try {
                activityService.save(activity);
            } catch (final Exception e) {
                LOGGER.error("Failed to save activity '{}' reason={}", name, e.getMessage());
                return CImportRowResult.error(rowNumber, "Save failed: " + e.getMessage(), rowData);
            }
        }
        LOGGER.debug("Imported activity '{}' (row {})", name, rowNumber);
        return CImportRowResult.success(rowNumber, name);
    }
}
