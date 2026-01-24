package tech.derbent.plm.storage.storage.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.ObjIntConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;
import tech.derbent.plm.storage.storagetype.service.CStorageTypeService;

public class CStorageInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CStorage.class;
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageInitializerService.class);
	private static final String menuOrder = Menu_Order_PRODUCTS + ".40";
	private static final String menuTitle = "Storage";
	private static final String pageDescription = "Manage storage locations";
	private static final String pageTitle = "Storage";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		Check.notNull(project, "project cannot be null");
		final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
		// Basic Information Section
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentStorage"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsibleUser"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
		// Capacity Section
		detailSection.addScreenLine(CDetailLinesService.createSection("Capacity"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "capacity"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "capacityUnit"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currentUtilization"));
		// Location Section
		detailSection.addScreenLine(CDetailLinesService.createSection("Location"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "address"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "building"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "floor"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "zone"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "binCode"));
		// Environmental Control Section
		detailSection.addScreenLine(CDetailLinesService.createSection("Environmental Control"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "temperatureControl"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "climateControl"));
		detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "secureStorage"));
		// Attachments section - standard section for ALL entities
		CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
		// Links section - standard section for ALL entities
		CLinkInitializerService.addDefaultSection(detailSection, clazz);
		// Comments section - standard section for discussion entities
		CCommentInitializerService.addDefaultSection(detailSection, clazz);
		detailSection.debug_printScreenInformation();
		return detailSection;
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "parentStorage", "capacity", "capacityUnit", "currentUtilization", "active"));
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
				{
						"Main Warehouse", "Primary storage location for finished goods"
				}, {
						"Component Room", "Room-level storage for components and parts"
				}, {
						"Secure Vault", "Restricted access storage for sensitive materials"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<CStorage>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(ObjIntConsumer<CStorage>) (entity, index) -> {
					final CStorage storage = entity;
					final CStorageTypeService typeService = CSpringContext.getBean(CStorageTypeService.class);
					final CStorageType type = typeService.getRandom(project.getCompany());
					storage.setEntityType(type);
					storage.setCapacity(new BigDecimal("1000").add(BigDecimal.valueOf(index * 250L)));
					storage.setCapacityUnit("units");
					storage.setCurrentUtilization(BigDecimal.ZERO);
					storage.setSecureStorage(index == 2);
					storage.setTemperatureControl(index == 2 ? "Controlled" : "Ambient");
					storage.setActive(Boolean.TRUE);
				});
	}
}
