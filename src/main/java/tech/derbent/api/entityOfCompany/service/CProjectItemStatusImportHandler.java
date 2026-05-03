package tech.derbent.api.entityOfCompany.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CEntityOfCompanyImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;

/** Imports CProjectItemStatus rows from Excel (company-scoped reference data). */
@Service
public class CProjectItemStatusImportHandler extends CEntityOfCompanyImportHandler<CProjectItemStatus> {

    private final CProjectItemStatusService statusService;

    public CProjectItemStatusImportHandler(final CProjectItemStatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public Class<CProjectItemStatus> getEntityClass() { return CProjectItemStatus.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CProjectItemStatus");
        names.add("ProjectItemStatus");
        names.add("Status");
        names.add("Statuses");
        try {
            names.add(CEntityRegistry.getEntityTitleSingular(CProjectItemStatus.class));
            names.add(CEntityRegistry.getEntityTitlePlural(CProjectItemStatus.class));
        } catch (final Exception ignored) { /* registry may not be ready */ }
        return names;
    }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        return Map.of(
                "Name", "name",
                "Final Status", "finalstatus",
                "Is Final", "finalstatus",
                "Color", "color",
                "Icon", "icon");
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
            return CImportRowResult.error(rowNumber, "Project company is required to create statuses", rowData);
        }
        // WHY: system init Excel is intended to be re-runnable (and also safe on top of code-initialized reference data).
        // Upsert-by-name avoids unique constraint violations.
        final CProjectItemStatus status = statusService.findByNameAndCompany(name, project.getCompany())
                .orElseGet(() -> new CProjectItemStatus(name, project.getCompany()));

        final String finalStr = rowData.getOrDefault("finalstatus", "").trim();
        if (!finalStr.isBlank()) {
            // WHY: Excel authors commonly use true/false, yes/no, 1/0; accept the common variants.
            final boolean isFinal = Set.of("true", "yes", "1").contains(finalStr.toLowerCase());
            status.setFinalStatus(Boolean.valueOf(isFinal));
        }
        final String color = rowData.getOrDefault("color", "").trim();
        if (!color.isBlank()) {
            status.setColor(color);
        }
        final String icon = rowData.getOrDefault("icon", "").trim();
        if (!icon.isBlank()) {
            status.setIconString(icon);
        }

        if (!options.isDryRun()) {
            statusService.save(status);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
