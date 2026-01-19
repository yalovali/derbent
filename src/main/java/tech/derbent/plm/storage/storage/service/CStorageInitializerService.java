package tech.derbent.plm.storage.storage.service;

import java.math.BigDecimal;
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
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeService;
import tech.derbent.api.config.CSpringContext;

public class CStorageInitializerService extends CInitializerServiceBase {

    private static final Class<?> clazz = CStorage.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageInitializerService.class);
    private static final String menuOrder = Menu_Order_PLM + ".40";
    private static final String menuTitle = MenuTitle_PLM + ".Storage";
    private static final String pageDescription = "Manage storage locations, warehouses, rooms, and bins";
    private static final String pageTitle = "Storage Location Management";
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

            // Location Details Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Location Details"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "address"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "building"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "floor"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "zone"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "binCode"));

            // Hierarchy Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Hierarchy"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentStorage"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsibleUser"));

            // Capacity Management Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Capacity Management"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "capacity"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "capacityUnit"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currentUtilization"));

            // Storage Conditions Section
            detailSection.addScreenLine(CDetailLinesService.createSection("Storage Conditions"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "temperatureControl"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "climateControl"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "secureStorage"));
            detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));

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
            LOGGER.error("Error creating storage location view.");
            throw e;
        }
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of(
                "id",
                "name",
                "entityType",
                "status",
                "building",
                "floor",
                "zone",
                "binCode",
                "parentStorage",
                "capacity",
                "capacityUnit",
                "currentUtilization",
                "responsibleUser",
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

        final CStorageService service = CSpringContext.getBean(CStorageService.class);
        final CStorageTypeService typeService = CSpringContext.getBean(CStorageTypeService.class);

        // Get storage types
        final List<CStorageType> types = typeService.findAll();
        if (types.isEmpty()) {
            LOGGER.warn("No storage types available for sample data");
            return;
        }

        final CStorageType warehouseType = types.stream()
                .filter(t -> "Warehouse".equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElse(types.get(0));

        final CStorageType roomType = types.stream()
                .filter(t -> "Room".equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElse(types.get(0));

        final CStorageType shelfType = types.stream()
                .filter(t -> "Shelf".equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElse(types.get(0));

        // Create Main Warehouse
        final CStorage mainWarehouse = new CStorage("Main Warehouse", project);
        mainWarehouse.setEntityType(warehouseType);
        mainWarehouse.setDescription("Primary storage warehouse for all materials and supplies");
        mainWarehouse.setAddress("123 Industrial Park Drive, Building A");
        mainWarehouse.setBuilding("Building A");
        mainWarehouse.setCapacity(BigDecimal.valueOf(10000));
        mainWarehouse.setCapacityUnit("m3");
        mainWarehouse.setCurrentUtilization(BigDecimal.valueOf(4500));
        mainWarehouse.setTemperatureControl("Climate Controlled");
        mainWarehouse.setSecureStorage(Boolean.TRUE);
        mainWarehouse.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(mainWarehouse);
        service.save(mainWarehouse);
        LOGGER.info("Created storage location: {}", mainWarehouse.getName());

        // Create Storage Room 101
        final CStorage room101 = new CStorage("Storage Room 101", project);
        room101.setEntityType(roomType);
        room101.setDescription("General consumables and office supplies");
        room101.setBuilding("Building A");
        room101.setFloor("1st Floor");
        room101.setZone("Zone A");
        room101.setParentStorage(mainWarehouse);
        room101.setCapacity(BigDecimal.valueOf(500));
        room101.setCapacityUnit("m3");
        room101.setCurrentUtilization(BigDecimal.valueOf(320));
        room101.setTemperatureControl("Room Temperature");
        room101.setSecureStorage(Boolean.FALSE);
        room101.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(room101);
        service.save(room101);
        LOGGER.info("Created storage location: {}", room101.getName());

        // Create Storage Room 102 (Refrigerated)
        final CStorage room102 = new CStorage("Storage Room 102", project);
        room102.setEntityType(roomType);
        room102.setDescription("Temperature-sensitive materials");
        room102.setBuilding("Building A");
        room102.setFloor("1st Floor");
        room102.setZone("Zone B");
        room102.setParentStorage(mainWarehouse);
        room102.setCapacity(BigDecimal.valueOf(300));
        room102.setCapacityUnit("m3");
        room102.setCurrentUtilization(BigDecimal.valueOf(180));
        room102.setTemperatureControl("Refrigerated (2-8°C)");
        room102.setClimateControl("Humidity Controlled");
        room102.setSecureStorage(Boolean.TRUE);
        room102.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(room102);
        service.save(room102);
        LOGGER.info("Created storage location: {}", room102.getName());

        // Create Shelves in Room 101
        final CStorage shelf101A = new CStorage("Shelf 101-A", project);
        shelf101A.setEntityType(shelfType);
        shelf101A.setDescription("Office supplies - pens, paper, folders");
        shelf101A.setBuilding("Building A");
        shelf101A.setFloor("1st Floor");
        shelf101A.setZone("Zone A");
        shelf101A.setBinCode("101-A-01");
        shelf101A.setParentStorage(room101);
        shelf101A.setCapacity(BigDecimal.valueOf(50));
        shelf101A.setCapacityUnit("items");
        shelf101A.setCurrentUtilization(BigDecimal.valueOf(32));
        shelf101A.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(shelf101A);
        service.save(shelf101A);
        LOGGER.info("Created storage location: {}", shelf101A.getName());

        final CStorage shelf101B = new CStorage("Shelf 101-B", project);
        shelf101B.setEntityType(shelfType);
        shelf101B.setDescription("Cleaning supplies and sanitizers");
        shelf101B.setBuilding("Building A");
        shelf101B.setFloor("1st Floor");
        shelf101B.setZone("Zone A");
        shelf101B.setBinCode("101-A-02");
        shelf101B.setParentStorage(room101);
        shelf101B.setCapacity(BigDecimal.valueOf(50));
        shelf101B.setCapacityUnit("items");
        shelf101B.setCurrentUtilization(BigDecimal.valueOf(28));
        shelf101B.setIsActive(Boolean.TRUE);
        service.initializeNewEntity(shelf101B);
        service.save(shelf101B);
        LOGGER.info("Created storage location: {}", shelf101B.getName());

        LOGGER.info("Sample storage locations created successfully");
    }
}
