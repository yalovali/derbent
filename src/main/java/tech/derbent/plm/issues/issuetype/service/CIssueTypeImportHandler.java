package tech.derbent.plm.issues.issuetype.service;

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
import tech.derbent.plm.issues.issuetype.domain.CIssueType;

/** Imports CIssueType rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CIssueTypeImportHandler implements IEntityImportHandler<CIssueType> {

    private final CIssueTypeService issueTypeService;

    public CIssueTypeImportHandler(final CIssueTypeService issueTypeService) {
        this.issueTypeService = issueTypeService;
    }

    @Override
    public Class<CIssueType> getEntityClass() { return CIssueType.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CIssueType");
        names.add("IssueType");
        names.add("Issue Type");
        names.add("Issue Types");
        try {
            names.add(CEntityRegistry.getEntityTitleSingular(CIssueType.class));
            names.add(CEntityRegistry.getEntityTitlePlural(CIssueType.class));
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
            return CImportRowResult.error(rowNumber, "Project company is required to create issue types", rowData);
        }
        final CIssueType type = new CIssueType(name, project.getCompany());
        final String color = rowData.getOrDefault("color", "").trim();
        if (!color.isBlank()) {
            type.setColor(color);
        }
        if (!options.isDryRun()) {
            issueTypeService.save(type);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
