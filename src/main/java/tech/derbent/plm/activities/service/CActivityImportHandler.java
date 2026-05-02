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
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.plm.activities.domain.CActivity;

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

    public CActivityImportHandler(final CActivityService activityService,
            final CActivityTypeService activityTypeService,
            final CProjectItemStatusService statusService) {
        this.activityService = activityService;
        this.activityTypeService = activityTypeService;
        this.statusService = statusService;
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
            final int rowNumber, final boolean dryRun) {
        final String name = rowData.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        final CActivity activity = new CActivity(name, project);
        // Resolve optional status by name
        final String statusName = rowData.getOrDefault("status", "").trim();
        if (!statusName.isBlank()) {
            final var statusOpt = statusService.findByNameAndCompany(statusName, project.getCompany());
            if (statusOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing.", rowData);
            }
            activity.setStatus(statusOpt.get());
        }
        // Resolve optional activity type by name
        final String typeName = rowData.getOrDefault("entitytype", "").trim();
        if (!typeName.isBlank()) {
            final var typeOpt = activityTypeService.findByNameAndCompany(typeName, project.getCompany());
            if (typeOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Activity Type '" + typeName + "' not found. Create it before importing.", rowData);
            }
            activity.setEntityType(typeOpt.get());
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
        if (!dryRun) {
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
