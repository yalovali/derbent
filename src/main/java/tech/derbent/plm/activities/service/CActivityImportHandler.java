package tech.derbent.plm.activities.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityType;

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
public class CActivityImportHandler implements IEntityImportHandler<CActivity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityImportHandler.class);
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("d.M.yyyy"),
    };

    private final CActivityService activityService;
    private final CActivityTypeService activityTypeService;
    private final CProjectItemStatusService statusService;
    private final IUserRepository userRepository;

    public CActivityImportHandler(final CActivityService activityService,
            final CActivityTypeService activityTypeService,
            final CProjectItemStatusService statusService,
            final IUserRepository userRepository) {
        this.activityService = activityService;
        this.activityTypeService = activityTypeService;
        this.statusService = statusService;
        this.userRepository = userRepository;
    }

    @Override
    public Class<CActivity> getEntityClass() { return CActivity.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CActivity");
        names.add("Activity");
        // Add registry names if available
        try {
            names.add(CEntityRegistry.getEntityTitleSingular(CActivity.class));
            names.add(CEntityRegistry.getEntityTitlePlural(CActivity.class));
        } catch (final Exception ignored) { /* registry may not be ready at bean creation */ }
        return names;
    }

    @Override
    public Map<String, String> getColumnAliases() {
        // alias (any case) → canonical token used as rowData key
        return Map.of(
            "Activity Type",   "entitytype",
            "Type",            "entitytype",
            "Due Date",        "duedate",
            "Estimated Hours", "estimatedhours",
            "Assigned To",     "assignedto",
            "Description",     "description",
            "Status",          "status",
            "Name",            "name"
        );
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
        final CActivity activity = new CActivity(name, project);
        // Resolve optional status by name
        final String statusName = rowData.getOrDefault("status", "").trim();
        if (!statusName.isBlank()) {
            final var statusOpt = statusService.findByNameAndCompany(statusName, project.getCompany());
            if (statusOpt.isPresent()) {
                activity.setStatus(statusOpt.get());
            } else if (options.isAutoCreateLookups() && !options.isDryRun()) {
                // WHY: bootstrapping workbooks are easier to maintain if they can declare new lookup values inline.
                final CProjectItemStatus created = statusService.save(new CProjectItemStatus(statusName, project.getCompany()));
                activity.setStatus(created);
            } else {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing (or enable auto-create lookups).", rowData);
            }
        }
        // Resolve optional activity type by name
        final String typeName = rowData.getOrDefault("entitytype", "").trim();
        if (!typeName.isBlank()) {
            final var typeOpt = activityTypeService.findByNameAndCompany(typeName, project.getCompany());
            if (typeOpt.isPresent()) {
                activity.setEntityType(typeOpt.get());
            } else if (options.isAutoCreateLookups() && !options.isDryRun()) {
                // WHY: types are reference data; creating them during import keeps the workbook self-contained.
                final CActivityType created = activityTypeService.save(new CActivityType(typeName, project.getCompany()));
                activity.setEntityType(created);
            } else {
                return CImportRowResult.error(rowNumber,
                        "Activity Type '" + typeName + "' not found. Create it before importing (or enable auto-create lookups).", rowData);
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
        // Optional due date
        final String dueDateStr = rowData.getOrDefault("duedate", "").trim();
        if (!dueDateStr.isBlank()) {
            final LocalDate dueDate = parseDate(dueDateStr);
            if (dueDate == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse date '" + dueDateStr + "'. Use yyyy-MM-dd or dd/MM/yyyy.", rowData);
            }
            activity.setDueDate(dueDate);
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

    private LocalDate parseDate(final String value) {
        for (final DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, fmt);
            } catch (final DateTimeParseException ignored) { /* try next format */ }
        }
        return null;
    }
}
