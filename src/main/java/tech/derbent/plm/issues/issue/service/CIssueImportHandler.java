package tech.derbent.plm.issues.issue.service;

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
import tech.derbent.plm.activities.service.CActivityService;
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
public class CIssueImportHandler extends CAbstractExcelImportHandler<CIssue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CIssueImportHandler.class);

    private final CIssueService issueService;
    private final CIssueTypeService issueTypeService;
    private final CProjectItemStatusService statusService;
    private final CActivityService activityService;
    private final IUserRepository userRepository;

    public CIssueImportHandler(final CIssueService issueService, final CIssueTypeService issueTypeService,
            final CProjectItemStatusService statusService, final CActivityService activityService,
            final IUserRepository userRepository) {
        this.issueService = issueService;
        this.issueTypeService = issueTypeService;
        this.statusService = statusService;
        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    @Override
    public Class<CIssue> getEntityClass() { return CIssue.class; }

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
        final CIssue issue = issueService.findByNameAndProject(name, project)
                .orElseGet(() -> new CIssue(name, project));
        final String statusName = rowData.getOrDefault("status", "").trim();
        if (!statusName.isBlank()) {
            final var statusOpt = statusService.findByNameAndCompany(statusName, project.getCompany());
            if (statusOpt.isPresent()) {
                issue.setStatus(statusOpt.get());
            } else {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing.", rowData);
            }
        }
        final String typeName = rowData.getOrDefault("entitytype", "").trim();
        if (!typeName.isBlank()) {
            final var typeOpt = issueTypeService.findByNameAndCompany(typeName, project.getCompany());
            if (typeOpt.isPresent()) {
                issue.setEntityType(typeOpt.get());
            } else {
                return CImportRowResult.error(rowNumber,
                        "Issue Type '" + typeName + "' not found. Create it before importing.", rowData);
            }
        }
        // Resolve optional linked activity by name
        final String linkedActivityName = rowData.getOrDefault("linkedactivity", "").trim();
        if (!linkedActivityName.isBlank()) {
            // WHY: issues often reference an implementation task; resolving by name keeps authoring simple.
            final var actOpt = activityService.findByNameAndProject(linkedActivityName, project);
            if (actOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Linked Activity '" + linkedActivityName + "' not found in project.", rowData);
            }
            issue.setLinkedActivity(actOpt.get());
        }
        // Resolve optional assignee by login
        final String assignedToLogin = rowData.getOrDefault("assignedto", "").trim();
        if (!assignedToLogin.isBlank()) {
            final var userOpt = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin);
            if (userOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber,
                        "Assigned To user '" + assignedToLogin + "' not found in company.", rowData);
            }
            issue.setAssignedTo(userOpt.get());
        }
        final String description = rowData.getOrDefault("description", "").trim();
        if (!description.isBlank()) {
            issue.setDescription(description);
        }
        final String dueDateStr = rowData.getOrDefault("duedate", "").trim();
        if (!dueDateStr.isBlank()) {
            final LocalDate dueDate = CImportParsers.tryParseLocalDate(dueDateStr).orElse(null);
            if (dueDate == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse date '" + dueDateStr + "'. Use yyyy-MM-dd or dd/MM/yyyy.", rowData);
            }
            issue.setDueDate(dueDate);
        }
        if (!options.isDryRun()) {
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
}
