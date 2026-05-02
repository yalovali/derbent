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
import tech.derbent.plm.activities.domain.CActivityType;

/** Imports CActivityType rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CActivityTypeImportHandler implements IEntityImportHandler<CActivityType> {

    private final CActivityTypeService activityTypeService;

    public CActivityTypeImportHandler(final CActivityTypeService activityTypeService) {
        this.activityTypeService = activityTypeService;
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
                "Color", "color");
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
        final CActivityType type = new CActivityType(name, project.getCompany());
        final String color = rowData.getOrDefault("color", "").trim();
        if (!color.isBlank()) {
            type.setColor(color);
        }
        if (!options.isDryRun()) {
            activityTypeService.save(type);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
