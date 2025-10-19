package tech.derbent.orders.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.orders.domain.COrder;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class COrderInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Order Information";
	private static final Class<?> clazz = COrder.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(COrderInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".9";
	private static final String menuTitle = MenuTitle_PROJECT + ".Orders";
	private static final String pageDescription = "Order management with approval workflow";
	private static final String pageTitle = "Order Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Classification"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "orderType"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "orderNumber"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Schedule"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "orderDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiredDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deliveryDate"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Financials"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualCost"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Provider"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "providerCompanyName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "providerContactName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "providerEmail"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deliveryAddress"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Responsibility"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requestor"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsible"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating order view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("name", "orderType", "status", "orderDate", "requiredDate", "deliveryDate", "providerCompanyName",
				"estimatedCost", "actualCost", "project"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}
}
