package tech.derbent.plm.storage.transaction.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;

public final class CStorageTransactionInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CStorageTransaction.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageTransactionInitializerService.class);
    private static final String menuOrder = Menu_Order_PRODUCTS + ".60";
    private static final String menuTitle = "Storage Transactions";
    private static final String pageDescription = "Review storage transactions";
    private static final String pageTitle = "Storage Transactions";
    private static final boolean showInQuickToolbar = false;

    private CStorageTransactionInitializerService() {
        // utility
    }

    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        Check.notNull(project, "project cannot be null");
        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "storageItem"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "transactionType"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "quantity"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "quantityBefore"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "quantityAfter"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "transactionDate"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "user"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "reference"));
        detailSection.debug_printScreenInformation();
        return detailSection;
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of("id", "name", "storageItem", "transactionType", "quantity", "transactionDate", "user"));
        return grid;
    }

    public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                pageDescription, showInQuickToolbar, menuOrder);
    }
}
