package tech.derbent.plm.activities.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.activities.domain.CActivityType;

/** Imports CActivityType rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CActivityTypeImportHandler implements IEntityImportHandler<CActivityType> {

    private final CActivityTypeService activityTypeService;
    private final CWorkflowEntityService workflowEntityService;

    public CActivityTypeImportHandler(final CActivityTypeService activityTypeService,
            final CWorkflowEntityService workflowEntityService) {
        this.activityTypeService = activityTypeService;
        this.workflowEntityService = workflowEntityService;
    }

    @Override
    public Class<CActivityType> getEntityClass() { return CActivityType.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CActivityType");
        names.add("ActivityType");
        names.add("Activity Type");
        names.add("Activity Types");
        try {
            names.add(CEntityRegistry.getEntityTitleSingular(CActivityType.class));
            names.add(CEntityRegistry.getEntityTitlePlural(CActivityType.class));
        } catch (final Exception ignored) { /* registry may not be ready */ }
        return names;
    }

    @Override
    public Map<String, String> getColumnAliases() {
        return Map.of(
                "Name", "name",
                "Color", "color",
                "Sort Order", "sortorder",
                "Level", "level",
                "Can Have Children", "canhavechildren",
                "Non Deletable", "attributenondeletable",
                "Workflow", "workflow");
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
            final CImportOptions options) {
        final String name = rowData.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        if (project.getCompany() == null) {
            return CImportRowResult.error(rowNumber, "Project company is required to create activity types", rowData);
        }
        // WHY: system init Excel is intended to be re-runnable (and also safe on top of code-initialized reference data).
        // Upsert-by-name avoids unique constraint violations.
        final CActivityType type = activityTypeService.findByNameAndCompany(name, project.getCompany())
                .orElseGet(() -> new CActivityType(name, project.getCompany()));
        final String color = rowData.getOrDefault("color", "").trim();
        if (!color.isBlank()) {
            type.setColor(color);
        }
        final String sortOrderStr = rowData.getOrDefault("sortorder", "").trim();
        if (!sortOrderStr.isBlank()) {
            try {
                type.setSortOrder(Integer.valueOf(sortOrderStr));
            } catch (final Exception e) {
                return CImportRowResult.error(rowNumber, "Invalid sort order: " + sortOrderStr, rowData);
            }
        }
        final String levelStr = rowData.getOrDefault("level", "").trim();
        if (!levelStr.isBlank()) {
            try {
                type.setLevel(Integer.valueOf(levelStr));
            } catch (final Exception e) {
                return CImportRowResult.error(rowNumber, "Invalid level: " + levelStr, rowData);
            }
        }
        final String canHaveChildrenStr = rowData.getOrDefault("canhavechildren", "").trim();
        if (!canHaveChildrenStr.isBlank()) {
            type.setCanHaveChildren(Set.of("true", "yes", "1").contains(canHaveChildrenStr.toLowerCase()));
        }
        final String nonDeletableStr = rowData.getOrDefault("attributenondeletable", "").trim();
        if (!nonDeletableStr.isBlank()) {
            type.setAttributeNonDeletable(Set.of("true", "yes", "1").contains(nonDeletableStr.toLowerCase()));
        }
        final String workflowName = rowData.getOrDefault("workflow", "").trim();
        if (!workflowName.isBlank()) {
            final var wf = workflowEntityService.findByNameAndCompany(workflowName, project.getCompany()).orElse(null);
            if (wf == null) {
                return CImportRowResult.error(rowNumber, "Workflow '" + workflowName + "' not found", rowData);
            }
            type.setWorkflow(wf);
        }
        if (!options.isDryRun()) {
            activityTypeService.save(type);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
