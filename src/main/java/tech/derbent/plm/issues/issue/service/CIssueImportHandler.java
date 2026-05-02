package tech.derbent.plm.issues.issue.service;

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
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeService;

/**
 * Handles import of CIssue rows from Excel.
 *
 * Supported columns: name (required), description, status, type (Issue Type), dueDate
 * Relations resolved by name.
 */
@Service
@Profile({"derbent", "default"})
public class CIssueImportHandler implements IEntityImportHandler<CIssue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CIssueImportHandler.class);
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("d.M.yyyy"),
    };

    private final CIssueService issueService;
    private final CIssueTypeService issueTypeService;
    private final CProjectItemStatusService statusService;

    public CIssueImportHandler(final CIssueService issueService, final CIssueTypeService issueTypeService,
            final CProjectItemStatusService statusService) {
        this.issueService = issueService;
        this.issueTypeService = issueTypeService;
        this.statusService = statusService;
    }

    @Override
    public Class<CIssue> getEntityClass() { return CIssue.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CIssue");
        names.add("Issue");
        try {
            names.add(CEntityRegistry.getEntityTitleSingular(CIssue.class));
            names.add(CEntityRegistry.getEntityTitlePlural(CIssue.class));
        } catch (final Exception ignored) { /* registry may not be ready */ }
        return names;
    }

    @Override
    public Map<String, String> getColumnAliases() {
        return Map.of(
            "Issue Type", "entitytype",
            "Type",       "entitytype",
            "Due Date",   "duedate",
            "Description","description",
            "Status",     "status",
            "Name",       "name"
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
        final CIssue issue = new CIssue(name, project);
        final String statusName = rowData.getOrDefault("status", "").trim();
        if (!statusName.isBlank()) {
            final var statusOpt = statusService.findByNameAndCompany(statusName, project.getCompany());
            if (statusOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing.", rowData);
            }
            issue.setStatus(statusOpt.get());
        }
        final String typeName = rowData.getOrDefault("entitytype", "").trim();
        if (!typeName.isBlank()) {
            final var typeOpt = issueTypeService.findByNameAndCompany(typeName, project.getCompany());
            if (typeOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Issue Type '" + typeName + "' not found. Create it before importing.", rowData);
            }
            issue.setEntityType(typeOpt.get());
        }
        final String description = rowData.getOrDefault("description", "").trim();
        if (!description.isBlank()) {
            issue.setDescription(description);
        }
        final String dueDateStr = rowData.getOrDefault("duedate", "").trim();
        if (!dueDateStr.isBlank()) {
            final LocalDate dueDate = parseDate(dueDateStr);
            if (dueDate == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse date '" + dueDateStr + "'. Use yyyy-MM-dd or dd/MM/yyyy.", rowData);
            }
            issue.setDueDate(dueDate);
        }
        if (!dryRun) {
            try {
                issueService.save(issue);
            } catch (final Exception e) {
                LOGGER.error("Failed to save issue '{}' reason={}", name, e.getMessage());
                return CImportRowResult.error(rowNumber, "Save failed: " + e.getMessage(), rowData);
            }
        }
        LOGGER.debug("Imported issue '{}' (row {})", name, rowNumber);
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
