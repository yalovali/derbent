package tech.derbent.plm.storage.storageitem.service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.ObjIntConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.plm.storage.storage.service.CStorageService;

public class CStorageItemInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CStorageItem.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItemInitializerService.class);
    private static final String menuOrder = Menu_Order_PRODUCTS + ".50";
    private static final String menuTitle = "Storage Items";
    private static final String pageDescription = "Manage storage items";
    private static final String pageTitle = "Storage Items";
    private static final boolean showInQuickToolbar = false;

    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        Check.notNull(project, "project cannot be null");
        final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "storage"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsibleUser"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sku"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "barcode"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "unitOfMeasure"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currentQuantity"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "minimumStockLevel"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "reorderQuantity"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maximumStockLevel"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "unitCost"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "provider"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "leadTimeDays"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "batchNumber"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "trackExpiration"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "expirationDate"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiresSpecialHandling"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "handlingInstructions"));
        detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastRestockedDate"));
        CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
        CCommentInitializerService.addDefaultSection(detailSection, clazz);
        detailSection.debug_printScreenInformation();
        return detailSection;
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of("id", "name", "storage", "entityType", "sku", "barcode", "currentQuantity", "unitOfMeasure", "minimumStockLevel",
                "reorderQuantity"));
        return grid;
    }

    public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                pageDescription, showInQuickToolbar, menuOrder);
    }

    public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
        final String[][] nameAndDescriptions = {
                {"Laptop Battery Pack", "Replacement battery pack for laptops"},
                {"RJ45 Cable Pack", "Cat6 Ethernet cables bundle"},
                {"Secure USB Drive", "Encrypted USB drives for sensitive data"}
        };
        initializeProjectEntity(nameAndDescriptions,
                (CEntityOfProjectService<CStorageItem>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
                (ObjIntConsumer<CStorageItem>) (entity, index) -> {
            final CStorageItem item = (CStorageItem) entity;
            final CStorageService storageService = CSpringContext.getBean(CStorageService.class);
            final CStorageItemTypeService typeService = CSpringContext.getBean(CStorageItemTypeService.class);
            // final CProviderService providerService = CSpringContext.getBean(CProviderService.class);
                    item.setStorage(storageService.getRandom(project));
                    item.setEntityType(typeService.getRandom(project.getCompany()));
                    // item.setProvider(providerService.getRandom(project));
                    item.setProvider(null);
                    item.setSku("SKU-" + (1000 + index));
                    item.setBarcode("BC-" + (1000 + index));
                    item.setUnitOfMeasure("pcs");
                    item.setCurrentQuantity(BigDecimal.valueOf(50L - (index * 5L)));
                    item.setMinimumStockLevel(BigDecimal.TEN);
                    item.setReorderQuantity(BigDecimal.valueOf(25));
                    item.setTrackExpiration(index == 2);
                    if (Boolean.TRUE.equals(item.getTrackExpiration())) {
                        item.setExpirationDate(LocalDate.now().plusMonths(12));
                    }
                });
    }
}
