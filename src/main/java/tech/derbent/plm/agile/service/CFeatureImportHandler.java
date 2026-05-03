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
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CFeatureType;

/**
 * Imports {@link CFeature} rows from Excel into the active project.
 *
 * Features sit below Epics in the agile hierarchy (Epic → Feature → User Story).
 * Column aliases are built automatically from @AMetaData; additional synonyms are declared here.
 */
@Service
@Profile({"derbent", "default"})
public class CFeatureImportHandler extends CAbstractExcelImportHandler<CFeature> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CFeatureImportHandler.class);

    private final CFeatureService featureService;
    private final CFeatureTypeService typeService;
    private final CProjectItemStatusService statusService;
    private final IUserRepository userRepository;

    public CFeatureImportHandler(final CFeatureService featureService, final CFeatureTypeService typeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        this.featureService = featureService;
        this.typeService = typeService;
        this.statusService = statusService;
        this.userRepository = userRepository;
    }

    @Override
    public Class<CFeature> getEntityClass() { return CFeature.class; }

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
        final CFeature feature = featureService.findByNameAndProject(name, project)
                .orElseGet(() -> new CFeature(name, project));

        row.optionalString("description").ifPresent(feature::setDescription);
        row.optionalString("acceptancecriteria").ifPresent(feature::setAcceptanceCriteria);
        row.optionalString("notes").ifPresent(feature::setNotes);
        row.optionalString("results").ifPresent(feature::setResults);
        row.optionalLong("storypoint").ifPresent(feature::setStoryPoint);
        row.optionalLocalDate("startdate").ifPresent(feature::setStartDate);
        row.optionalLocalDate("duedate").ifPresent(feature::setDueDate);
        row.optionalLocalDate("completiondate").ifPresent(feature::setCompletionDate);
        row.optionalInt("progresspercentage").ifPresent(feature::setProgressPercentage);
        row.optionalBigDecimal("estimatedhours").ifPresent(feature::setEstimatedHours);
        row.optionalBigDecimal("estimatedcost").ifPresent(feature::setEstimatedCost);
        row.optionalBigDecimal("actualhours").ifPresent(feature::setActualHours);
        row.optionalBigDecimal("actualcost").ifPresent(feature::setActualCost);

        final String statusName = row.string("status");
        if (!statusName.isBlank()) {
            final var status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
            if (status == null) {
                return CImportRowResult.error(rowNumber,
                        "Status '" + statusName + "' not found. Create it before importing.", rowData);
            }
            feature.setStatus(status);
        }

        final String typeName = row.string("entitytype");
        if (!typeName.isBlank()) {
            final CFeatureType type = typeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
            if (type == null) {
                return CImportRowResult.error(rowNumber,
                        "Feature Type '" + typeName + "' not found. Create it before importing.", rowData);
            }
            feature.setEntityType(type);
        }

        final String assignedToLogin = row.string("assignedto");
        if (!assignedToLogin.isBlank()) {
            final CUser user = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin).orElse(null);
            if (user == null) {
                return CImportRowResult.error(rowNumber,
                        "Assigned user '" + assignedToLogin + "' not found in company. Create it before importing.", rowData);
            }
            feature.setAssignedTo(user);
        }

        if (!options.isDryRun()) {
            featureService.save(feature);
        }
        LOGGER.debug("Imported feature '{}' (row {})", name, rowNumber);
        return CImportRowResult.success(rowNumber, name);
    }
}
