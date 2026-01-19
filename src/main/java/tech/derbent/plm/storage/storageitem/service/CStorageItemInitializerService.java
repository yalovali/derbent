package tech.derbent.plm.storage.storageitem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.providers.provider.domain.CProvider;
import tech.derbent.plm.providers.provider.service.CProviderService;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storage.service.CStorageService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;

public class CStorageItemInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CStorageItem.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItemInitializerService.class);
    private static final String menuOrder = Menu_Order_PLM + ".50";
    private static final String menuTitle = MenuTitle_PLM + ".StorageItems";
    private static final String pageDescription = "Manage consumable inventory items with quantity tracking";
    private static final String pageTitle = "Storage Item Management";
    private static final boolean showInQuickToolbar = true;

    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        Check.notNull(project, "project cannot be null");
        try {
            final CDetailSection detailSection = createBaseScreenEntity(project, clazz);

            // Basic Information Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Basic Information"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "storage"));

            // Item Identification Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Item Identification"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sku"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "barcode"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "manufacturer"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "modelNumber"));

            // Quantity Management Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Quantity Management"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currentQuantity"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "unitOfMeasure"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "minimumStockLevel"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "reorderQuantity"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maximumStockLevel"));

            // Cost Information Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Cost Information"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "unitCost"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));

            // Expiration Tracking Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Expiration Tracking"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "trackExpiration"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "batchNumber"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "expirationDate"));

            // Supplier Information Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Supplier Information"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "supplier"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "leadTimeDays"));

            // Item Properties Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Item Properties"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isConsumable"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiresSpecialHandling"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "handlingInstructions"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsibleUser"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastRestockedDate"));

            // Attachments and Comments
            detailSection.addScreenLine(CDetailLinesService.createSection("Attachments & Notes"));
            detailSection.addScreenLine(CDetailLinesService.createLineForAttachments());
            detailSection.addScreenLine(CDetailLinesService.createLineForComments());

            // Audit Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedBy"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

            detailSection.debug_printScreenInformation();
            return detailSection;
        } catch (final Exception e) {
            LOGGER.error("Error creating storage item view.");
            throw e;
        }
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of(
                "id",
                "name",
                "sku",
                "entityType",
                "status",
                "storage",
                "currentQuantity",
                "unitOfMeasure",
                "minimumStockLevel",
                "unitCost",
                "supplier",
                "expirationDate",
                "isActive",
                "createdBy",
                "createdDate"));
        return grid;
    }

    public static void initialize(
            final CProject<?> project,
            final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService,
            final CPageEntityService pageEntityService) throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
                menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
    }

    public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
        if (minimal) {
            return;
        }

        final CStorageItemService service = CSpringContext.getBean(CStorageItemService.class);
        final CStorageService storageService = CSpringContext.getBean(CStorageService.class);
        final CStorageItemTypeService typeService = CSpringContext.getBean(CStorageItemTypeService.class);
        final CProviderService providerService = CSpringContext.getBean(CProviderService.class);

        // Get storage locations
        final List<CStorage> storages = storageService.findAll();
        if (storages.isEmpty()) {
            LOGGER.warn("No storage locations available for sample data");
            return;
        }

        final CStorage shelf101A = storages.stream()
                .filter(s -> s.getName().contains("101-A"))
                .findFirst()
                .orElse(storages.get(0));

        final CStorage shelf101B = storages.stream()
                .filter(s -> s.getName().contains("101-B"))
                .findFirst()
                .orElse(storages.get(0));

        // Get types
        final List<CStorageItemType> types = typeService.findAll();
        final CStorageItemType officeSupplies = types.stream()
                .filter(t -> t.getName().contains("Office"))
                .findFirst()
                .orElse(!types.isEmpty() ? types.get(0) : null);

        final CStorageItemType cleaningSupplies = types.stream()
                .filter(t -> t.getName().contains("Cleaning"))
                .findFirst()
                .orElse(!types.isEmpty() ? types.get(0) : null);

        // Get provider
        final List<CProvider> providers = providerService.findAll();
        final CProvider provider = !providers.isEmpty() ? providers.get(0) : null;

        // Create sample items
        // 1. Office Paper
        final CStorageItem paper = new CStorageItem("Copy Paper A4", project, shelf101A);
        paper.setEntityType(officeSupplies);
        paper.setDescription("Standard white copy paper, 80gsm");
        paper.setSku("PAPER-A4-80");
        paper.setBarcode("8712345678901");
        paper.setManufacturer("PaperCo");
        paper.setModelNumber("A4-80-WHT");
        paper.setCurrentQuantity(BigDecimal.valueOf(50));
        paper.setUnitOfMeasure("boxes");
        paper.setMinimumStockLevel(BigDecimal.valueOf(10));
        paper.setReorderQuantity(BigDecimal.valueOf(25));
        paper.setMaximumStockLevel(BigDecimal.valueOf(100));
        paper.setUnitCost(BigDecimal.valueOf(15.50));
        paper.setSupplier(provider);
        paper.setLeadTimeDays(BigDecimal.valueOf(3));
        paper.setIsConsumable(Boolean.TRUE);
        paper.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(paper);
        service.save(paper);
        LOGGER.info("Created storage item: {}", paper.getName());

        // 2. Ballpoint Pens (Low Stock)
        final CStorageItem pens = new CStorageItem("Ballpoint Pens Blue", project, shelf101A);
        pens.setEntityType(officeSupplies);
        pens.setDescription("Blue ballpoint pens, medium point");
        pens.setSku("PEN-BP-BLUE");
        pens.setBarcode("8712345678902");
        pens.setManufacturer("PenMaster");
        pens.setModelNumber("BP-MED-BLU");
        pens.setCurrentQuantity(BigDecimal.valueOf(8));
        pens.setUnitOfMeasure("boxes");
        pens.setMinimumStockLevel(BigDecimal.valueOf(10));
        pens.setReorderQuantity(BigDecimal.valueOf(20));
        pens.setMaximumStockLevel(BigDecimal.valueOf(50));
        pens.setUnitCost(BigDecimal.valueOf(8.90));
        pens.setSupplier(provider);
        pens.setLeadTimeDays(BigDecimal.valueOf(2));
        pens.setIsConsumable(Boolean.TRUE);
        pens.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(pens);
        service.save(pens);
        LOGGER.info("Created storage item: {} (LOW STOCK)", pens.getName());

        // 3. Sanitizer Gel
        final CStorageItem sanitizer = new CStorageItem("Hand Sanitizer Gel", project, shelf101B);
        sanitizer.setEntityType(cleaningSupplies);
        sanitizer.setDescription("70% alcohol hand sanitizer gel");
        sanitizer.setSku("CLEAN-SAN-GEL");
        sanitizer.setBarcode("8712345678903");
        sanitizer.setManufacturer("CleanCo");
        sanitizer.setModelNumber("SAN-70-500ML");
        sanitizer.setCurrentQuantity(BigDecimal.valueOf(25));
        sanitizer.setUnitOfMeasure("bottles");
        sanitizer.setMinimumStockLevel(BigDecimal.valueOf(15));
        sanitizer.setReorderQuantity(BigDecimal.valueOf(30));
        sanitizer.setMaximumStockLevel(BigDecimal.valueOf(80));
        sanitizer.setUnitCost(BigDecimal.valueOf(3.50));
        sanitizer.setTrackExpiration(Boolean.TRUE);
        sanitizer.setExpirationDate(LocalDate.now().plusMonths(18));
        sanitizer.setBatchNumber("BATCH-2024-001");
        sanitizer.setSupplier(provider);
        sanitizer.setLeadTimeDays(BigDecimal.valueOf(5));
        sanitizer.setIsConsumable(Boolean.TRUE);
        sanitizer.setRequiresSpecialHandling(Boolean.TRUE);
        sanitizer.setHandlingInstructions("Keep away from heat and flames. Store in cool, dry place.");
        sanitizer.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(sanitizer);
        service.save(sanitizer);
        LOGGER.info("Created storage item: {}", sanitizer.getName());

        // 4. Trash Bags (Expiring Soon)
        final CStorageItem trashBags = new CStorageItem("Heavy Duty Trash Bags", project, shelf101B);
        trashBags.setEntityType(cleaningSupplies);
        trashBags.setDescription("Black heavy duty trash bags, 50 gallon");
        trashBags.setSku("CLEAN-BAG-50G");
        trashBags.setBarcode("8712345678904");
        trashBags.setManufacturer("BagCo");
        trashBags.setModelNumber("HD-50-BLK");
        trashBags.setCurrentQuantity(BigDecimal.valueOf(15));
        trashBags.setUnitOfMeasure("rolls");
        trashBags.setMinimumStockLevel(BigDecimal.valueOf(5));
        trashBags.setReorderQuantity(BigDecimal.valueOf(20));
        trashBags.setMaximumStockLevel(BigDecimal.valueOf(40));
        trashBags.setUnitCost(BigDecimal.valueOf(12.00));
        trashBags.setTrackExpiration(Boolean.TRUE);
        trashBags.setExpirationDate(LocalDate.now().plusDays(25));
        trashBags.setBatchNumber("BATCH-2023-Q4");
        trashBags.setSupplier(provider);
        trashBags.setLeadTimeDays(BigDecimal.valueOf(4));
        trashBags.setIsConsumable(Boolean.TRUE);
        trashBags.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(trashBags);
        service.save(trashBags);
        LOGGER.info("Created storage item: {} (EXPIRING SOON)", trashBags.getName());

        // 5. Printer Toner (High Value)
        final CStorageItem toner = new CStorageItem("Laser Printer Toner Cartridge", project, shelf101A);
        toner.setEntityType(officeSupplies);
        toner.setDescription("Black toner cartridge for HP LaserJet printers");
        toner.setSku("IT-TONER-HP-BLK");
        toner.setBarcode("8712345678905");
        toner.setManufacturer("HP");
        toner.setModelNumber("CF410A");
        toner.setCurrentQuantity(BigDecimal.valueOf(4));
        toner.setUnitOfMeasure("pieces");
        toner.setMinimumStockLevel(BigDecimal.valueOf(2));
        toner.setReorderQuantity(BigDecimal.valueOf(5));
        toner.setMaximumStockLevel(BigDecimal.valueOf(10));
        toner.setUnitCost(BigDecimal.valueOf(85.00));
        toner.setSupplier(provider);
        toner.setLeadTimeDays(BigDecimal.valueOf(7));
        toner.setIsConsumable(Boolean.TRUE);
        toner.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(toner);
        service.save(toner);
        LOGGER.info("Created storage item: {}", toner.getName());

        LOGGER.info("Sample storage items created successfully");
    }
}
