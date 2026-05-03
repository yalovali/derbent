package tech.derbent.api.screens.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CGridEntity;

/** Imports CGridEntity rows from Excel (project-scoped "view" configuration). */
@Service
public class CGridEntityImportHandler implements IEntityImportHandler<CGridEntity> {

    private final CGridEntityService gridEntityService;

    public CGridEntityImportHandler(final CGridEntityService gridEntityService) {
        this.gridEntityService = gridEntityService;
    }

    @Override
    public Class<CGridEntity> getEntityClass() { return CGridEntity.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CGridEntity");
        names.add("GridEntity");
        names.add("Grid Entity");
        names.add("Grid Entities");
        try {
            final String singular = CEntityRegistry.getEntityTitleSingular(CGridEntity.class);
            final String plural = CEntityRegistry.getEntityTitlePlural(CGridEntity.class);
            if (singular != null && !singular.isBlank()) {
                names.add(singular);
            }
            if (plural != null && !plural.isBlank()) {
                names.add(plural);
            }
        } catch (final Exception ignored) { /* registry may not be ready */ }
        return names;
    }

    @Override
    public Map<String, String> getColumnAliases() {
        return Map.of(
                "Name", "name",
                "Data Service Bean", "dataservicebeanname",
                "Column Fields", "columnfields",
                "Editable Column Fields", "editablecolumnfields",
                "None Grid", "attributenone");
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name", "dataservicebeanname");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
            final CImportOptions options) {
        final String name = rowData.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        final String beanName = rowData.getOrDefault("dataservicebeanname", "").trim();
        if (beanName.isBlank()) {
            return CImportRowResult.error(rowNumber, "Data Service Bean is required", rowData);
        }

        // WHY: view configuration should be re-runnable; we upsert by name to avoid duplicate bootstrap runs failing.
        final CGridEntity entity = gridEntityService.findByNameAndProject(name, project).orElseGet(() -> new CGridEntity(name, project));
        entity.setDataServiceBeanName(beanName);

        final String colFields = rowData.getOrDefault("columnfields", "").trim();
        if (!colFields.isBlank()) {
            entity.setColumnFields(splitCsv(colFields));
        }

        final String editable = rowData.getOrDefault("editablecolumnfields", "").trim();
        if (!editable.isBlank()) {
            entity.setEditableColumnFields(splitCsv(editable));
        }

        final String noneGrid = rowData.getOrDefault("attributenone", "").trim();
        if (!noneGrid.isBlank()) {
            // WHY: accept common Excel boolean variants.
            entity.setAttributeNone(Set.of("true", "yes", "1").contains(noneGrid.toLowerCase()));
        }

        if (!options.isDryRun()) {
            gridEntityService.save(entity);
        }
        return CImportRowResult.success(rowNumber, name);
    }

    private static List<String> splitCsv(final String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }
}
