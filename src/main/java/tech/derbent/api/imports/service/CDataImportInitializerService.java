package tech.derbent.api.imports.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyInitializerService;
import tech.derbent.api.imports.domain.CDataImport;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;

public class CDataImportInitializerService extends CEntityOfCompanyInitializerService {

    private static final Class<?> clazz = CDataImport.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CDataImportInitializerService.class);
    private static final String menuOrder = Menu_Order_SETUP + ".88";
    private static final String menuTitle = MenuTitle_SETUP + ".Import Jobs";
    private static final String pageDescription = "History of data import jobs";
    private static final String pageTitle = "Import Jobs";
    private static final boolean showInQuickToolbar = false;

    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        try {
            final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fileName"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "importedAt"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "importedBy"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dryRun"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "rolledBack"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalSuccess"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalErrors"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalSkipped"));
            return detailSection;
        } catch (final Exception e) {
            LOGGER.error("Error creating import job view.");
            throw e;
        }
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of("id", "name", "fileName", "importedAt", "importedBy",
                "totalSuccess", "totalErrors", "totalSkipped", "dryRun", "rolledBack"));
        return grid;
    }

    public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
            throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
                menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
    }
}
