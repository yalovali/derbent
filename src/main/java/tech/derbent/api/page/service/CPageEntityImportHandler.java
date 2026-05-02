package tech.derbent.api.page.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.page.domain.CPageEntity;

/** Imports CPageEntity rows from Excel (project-scoped navigation/view configuration). */
@Service
public class CPageEntityImportHandler implements IEntityImportHandler<CPageEntity> {

    private final CPageEntityService pageEntityService;
    private final CGridEntityService gridEntityService;

    public CPageEntityImportHandler(final CPageEntityService pageEntityService, final CGridEntityService gridEntityService) {
        this.pageEntityService = pageEntityService;
        this.gridEntityService = gridEntityService;
    }

    @Override
    public Class<CPageEntity> getEntityClass() { return CPageEntity.class; }

    @Override
    public Set<String> getSupportedSheetNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add("CPageEntity");
        names.add("PageEntity");
        names.add("Page Entity");
        names.add("Page Entities");
        try {
            names.add(CEntityRegistry.getEntityTitleSingular(CPageEntity.class));
            names.add(CEntityRegistry.getEntityTitlePlural(CPageEntity.class));
        } catch (final Exception ignored) { /* registry may not be ready */ }
        return names;
    }

    @Override
    public Map<String, String> getColumnAliases() {
        return Map.of(
                "Name", "name",
                "Menu Title", "menutitle",
                "Menu Order", "menuorder",
                "Page Title", "pagetitle",
                "Page Service", "pageservice",
                "Icon", "icon",
                "Requires Authentication", "requiresauthentication",
                "Grid Entity", "gridentity",
                "Content", "content");
    }

    @Override
    public Set<String> getRequiredColumns() {
        // pageService is required by CPageEntityService validation
        return Set.of("name", "menutitle", "pagetitle", "pageservice");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
            final CImportOptions options) {
        final String name = rowData.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }

        // WHY: view configuration should be re-runnable; we upsert by name to avoid duplicate bootstrap runs failing.
        final CPageEntity page = pageEntityService.findByNameAndProject(name, project).orElseGet(() -> new CPageEntity(name, project));

        final String menuTitle = rowData.getOrDefault("menutitle", "").trim();
        page.setMenuTitle(menuTitle);

        final String menuOrder = rowData.getOrDefault("menuorder", "").trim();
        if (!menuOrder.isBlank()) {
            page.setMenuOrder(menuOrder);
        }

        final String pageTitle = rowData.getOrDefault("pagetitle", "").trim();
        page.setPageTitle(pageTitle);

        final String pageService = rowData.getOrDefault("pageservice", "").trim();
        page.setPageService(pageService);

        final String icon = rowData.getOrDefault("icon", "").trim();
        if (!icon.isBlank()) {
            page.setIconString(icon);
        }

        final String requiresAuth = rowData.getOrDefault("requiresauthentication", "").trim();
        if (!requiresAuth.isBlank()) {
            // WHY: accept common Excel boolean variants.
            page.setRequiresAuthentication(Set.of("true", "yes", "1").contains(requiresAuth.toLowerCase()));
        }

        final String gridEntityName = rowData.getOrDefault("gridentity", "").trim();
        if (!gridEntityName.isBlank()) {
            final CGridEntity grid = gridEntityService.findByNameAndProject(gridEntityName, project).orElse(null);
            if (grid == null) {
                return CImportRowResult.error(rowNumber, "Grid Entity '" + gridEntityName + "' not found in project", rowData);
            }
            page.setGridEntity(grid);
        }

        final String content = rowData.getOrDefault("content", "").trim();
        if (!content.isBlank()) {
            page.setContent(content);
        }

        if (!options.isDryRun()) {
            pageEntityService.save(page);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
