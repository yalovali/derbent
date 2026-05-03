package tech.derbent.plm.decisions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.domain.CDecisionType;

/**
 * Imports CDecision rows from Excel (project items).
 *
 * Extends CAbstractExcelImportHandler so @AMetaData displayNames are automatically registered as
 * column aliases. LocalDateTime fields (implementationdate, reviewdate) accept both "yyyy-MM-dd"
 * and ISO datetime — date-only input is treated as midnight.
 */
@Service
@Profile({"derbent", "default"})
public class CDecisionImportHandler extends CAbstractExcelImportHandler<CDecision> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionImportHandler.class);
    private static final DateTimeFormatter[] DATETIME_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    };

    private final CDecisionService decisionService;
    private final CDecisionTypeService decisionTypeService;
    private final CProjectItemStatusService statusService;
    private final IUserRepository userRepository;

    public CDecisionImportHandler(final CDecisionService decisionService, final CDecisionTypeService decisionTypeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        this.decisionService = decisionService;
        this.decisionTypeService = decisionTypeService;
        this.statusService = statusService;
        this.userRepository = userRepository;
    }

    @Override
    public Class<CDecision> getEntityClass() { return CDecision.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        // WHY: "Decision Type" and "Type" are common header synonyms for entitytype token.
        return Map.of(
                "Decision Type", "entitytype",
                "Type", "entitytype");
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
        final CDecision decision = decisionService.findByNameAndProject(name, project)
                .orElseGet(() -> new CDecision(name, project));

        row.optionalString("description").ifPresent(decision::setDescription);

        final String statusName = row.string("status");
        if (!statusName.isBlank()) {
            final var status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
            if (status == null) {
                return CImportRowResult.error(rowNumber, "Status '" + statusName + "' not found", rowData);
            }
            decision.setStatus(status);
        }

        final String typeName = row.string("entitytype");
        if (!typeName.isBlank()) {
            final CDecisionType type = decisionTypeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
            if (type == null) {
                return CImportRowResult.error(rowNumber, "Decision Type '" + typeName + "' not found", rowData);
            }
            decision.setEntityType(type);
        }

        final String costStr = row.string("estimatedcost");
        if (!costStr.isBlank()) {
            try {
                decision.setEstimatedCost(new BigDecimal(costStr));
            } catch (final Exception e) {
                return CImportRowResult.error(rowNumber, "Invalid Estimated Cost: " + costStr, rowData);
            }
        }

        final String implStr = row.string("implementationdate");
        if (!implStr.isBlank()) {
            final LocalDateTime impl = parseDateTime(implStr);
            if (impl == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse Implementation Date '" + implStr + "' (use yyyy-MM-dd or yyyy-MM-ddTHH:mm)", rowData);
            }
            decision.setImplementationDate(impl);
        }
        final String reviewStr = row.string("reviewdate");
        if (!reviewStr.isBlank()) {
            final LocalDateTime review = parseDateTime(reviewStr);
            if (review == null) {
                return CImportRowResult.error(rowNumber,
                        "Cannot parse Review Date '" + reviewStr + "' (use yyyy-MM-dd or yyyy-MM-ddTHH:mm)", rowData);
            }
            decision.setReviewDate(review);
        }

        final String assignedToLogin = row.string("assignedto");
        if (!assignedToLogin.isBlank()) {
            final var userOpt = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin);
            if (userOpt.isEmpty()) {
                return CImportRowResult.error(rowNumber, "Assigned To user '" + assignedToLogin + "' not found in company.", rowData);
            }
            decision.setAssignedTo(userOpt.get());
        }

        if (!options.isDryRun()) {
            try {
                decisionService.save(decision);
            } catch (final Exception e) {
                LOGGER.error("Failed to save decision '{}' reason={}", name, e.getMessage());
                return CImportRowResult.error(rowNumber, "Save failed: " + e.getMessage(), rowData);
            }
        }
        LOGGER.debug("Imported decision '{}' (row {})", name, rowNumber);
        return CImportRowResult.success(rowNumber, name);
    }

    /** Parses a datetime string; falls back to date-only (midnight). Uses CImportParsers for date part. */
    private static LocalDateTime parseDateTime(final String value) {
        for (final DateTimeFormatter fmt : DATETIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, fmt);
            } catch (final DateTimeParseException ignored) { /* try next */ }
        }
        // WHY: workbook authors often supply date-only values in datetime columns.
        return CImportParsers.tryParseLocalDate(value).map(d -> d.atStartOfDay()).orElse(null);
    }
}
