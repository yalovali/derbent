package tech.derbent.plm.assets.asset.service;

import java.util.List;
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
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.plm.assets.asset.domain.CAsset;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.service.CLinkInitializerService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CAssetInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CAsset.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CAssetInitializerService.class);
	private static final String menuOrder = Menu_Order_FINANCE + ".20";
	private static final String menuTitle = MenuTitle_FINANCE + ".Assets";
	private static final String pageDescription = "Asset management";
	private static final String pageTitle = "Asset Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			
			// Basic Information Section
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "brand"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "model"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serialNumber"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "inventoryNumber"));
			
			// Assignment and Location Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Assignment & Location"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "user"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "location"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "parentAsset"));
			
			// Provider and Dates Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Provider & Dates"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "provider"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "installationDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "decommissioningDate"));
			
			// Financial Information Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Financial Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "purchaseValue"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "untaxedAmount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fullAmount"));
			
			// Warranty and Depreciation Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Warranty & Depreciation"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "warrantyDuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "warrantyEndDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "depreciationPeriod"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "needInsurance"));
			
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			
			// Links section - standard section for ALL entities
			CLinkInitializerService.addDefaultSection(detailSection, clazz);
			
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			
			// Audit Section
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating asset view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "brand", "model", "serialNumber", "inventoryNumber", "status", 
				"assignedTo", "user", "location", "provider", "purchaseValue", "createdBy", "createdDate"));
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
						"Development Laptop - MacBook Pro", "High-performance laptop for software development with 16GB RAM and 512GB SSD"
				}, {
						"Production Server - AWS EC2", "Cloud server instance for production environment running Ubuntu 22.04 LTS"
				}, {
						"Office Printer - HP LaserJet", "Multi-function laser printer for office document printing and scanning"
				}, {
						"Network Switch - Cisco Catalyst", "48-port Gigabit Ethernet switch for office network infrastructure"
				}, {
						"Conference Room TV - Samsung", "65-inch 4K display for conference room presentations and video calls"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CAsset asset = (CAsset) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(asset.getProject().getCompany());
					asset.setAssignedTo(user);
					asset.setUser(user);
					
					// Set asset-specific fields based on index
					switch (index) {
					case 0: // MacBook Pro
						asset.setBrand("Apple");
						asset.setModel("MacBook Pro 16\" M2");
						asset.setSerialNumber("MB-2024-001");
						asset.setInventoryNumber("IT-LAP-001");
						asset.setLocation("Office - Floor 3, Desk 42");
						asset.setPurchaseValue(new java.math.BigDecimal("2500.00"));
						asset.setUntaxedAmount(new java.math.BigDecimal("2100.00"));
						asset.setFullAmount(new java.math.BigDecimal("2500.00"));
						asset.setWarrantyDuration(36);
						asset.setWarrantyEndDate(java.time.LocalDate.now().plusMonths(36));
						asset.setDepreciationPeriod(3);
						asset.setNeedInsurance(true);
						asset.setInstallationDate(java.time.LocalDate.now().minusMonths(6));
						break;
					case 1: // AWS Server
						asset.setBrand("Amazon Web Services");
						asset.setModel("EC2 t3.large");
						asset.setSerialNumber("AWS-EC2-2024-001");
						asset.setInventoryNumber("IT-SRV-001");
						asset.setLocation("AWS US-East-1 Data Center");
						asset.setPurchaseValue(new java.math.BigDecimal("5000.00"));
						asset.setUntaxedAmount(new java.math.BigDecimal("4200.00"));
						asset.setFullAmount(new java.math.BigDecimal("5000.00"));
						asset.setWarrantyDuration(12);
						asset.setWarrantyEndDate(java.time.LocalDate.now().plusMonths(12));
						asset.setDepreciationPeriod(5);
						asset.setNeedInsurance(true);
						asset.setInstallationDate(java.time.LocalDate.now().minusMonths(3));
						break;
					case 2: // HP Printer
						asset.setBrand("HP");
						asset.setModel("LaserJet Pro MFP M428fdw");
						asset.setSerialNumber("HP-PRN-2024-001");
						asset.setInventoryNumber("OFF-PRN-001");
						asset.setLocation("Office - Floor 2, Print Room");
						asset.setPurchaseValue(new java.math.BigDecimal("600.00"));
						asset.setUntaxedAmount(new java.math.BigDecimal("500.00"));
						asset.setFullAmount(new java.math.BigDecimal("600.00"));
						asset.setWarrantyDuration(24);
						asset.setWarrantyEndDate(java.time.LocalDate.now().plusMonths(24));
						asset.setDepreciationPeriod(5);
						asset.setNeedInsurance(false);
						asset.setInstallationDate(java.time.LocalDate.now().minusMonths(12));
						break;
					case 3: // Cisco Switch
						asset.setBrand("Cisco");
						asset.setModel("Catalyst 2960-X Series");
						asset.setSerialNumber("CSC-SW-2024-001");
						asset.setInventoryNumber("IT-NET-001");
						asset.setLocation("Server Room - Rack A1");
						asset.setPurchaseValue(new java.math.BigDecimal("1800.00"));
						asset.setUntaxedAmount(new java.math.BigDecimal("1500.00"));
						asset.setFullAmount(new java.math.BigDecimal("1800.00"));
						asset.setWarrantyDuration(60);
						asset.setWarrantyEndDate(java.time.LocalDate.now().plusMonths(60));
						asset.setDepreciationPeriod(7);
						asset.setNeedInsurance(true);
						asset.setInstallationDate(java.time.LocalDate.now().minusMonths(18));
						break;
					case 4: // Samsung TV
						asset.setBrand("Samsung");
						asset.setModel("QLED 4K Q80C 65\"");
						asset.setSerialNumber("SAM-TV-2024-001");
						asset.setInventoryNumber("OFF-AV-001");
						asset.setLocation("Conference Room A - Wall Mount");
						asset.setPurchaseValue(new java.math.BigDecimal("1200.00"));
						asset.setUntaxedAmount(new java.math.BigDecimal("1000.00"));
						asset.setFullAmount(new java.math.BigDecimal("1200.00"));
						asset.setWarrantyDuration(24);
						asset.setWarrantyEndDate(java.time.LocalDate.now().plusMonths(24));
						asset.setDepreciationPeriod(5);
						asset.setNeedInsurance(false);
						asset.setInstallationDate(java.time.LocalDate.now().minusMonths(8));
						break;
					}
				});
	}
}
