package tech.derbent.plm.orders.order.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityOfProjectInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CProjectItemInitializerService;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.orders.order.domain.COrder;

public class COrderInitializerService extends CProjectItemInitializerService {

	private static final Class<?> clazz = COrder.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(COrderInitializerService.class);
	private static final String menuOrder = Menu_Order_FINANCE + ".9";
	private static final String menuTitle = MenuTitle_FINANCE + ".Orders";
	private static final String pageDescription = "Order management with approval workflow";
	private static final String pageTitle = "Order Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = CEntityOfProjectInitializerService.createBasicView(project, clazz);
			CProjectItemInitializerService.createScreenLines(detailSection, clazz, project, false);
			
			detailSection.addScreenLine(CDetailLinesService.createSection("Classification"));
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
			
			// Attachments section - standard section for ALL entities
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating order view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "status", "orderDate", "requiredDate", "deliveryDate",
				"providerCompanyName", "estimatedCost", "actualCost", "project"));
		grid.setEditableColumnFields(List.of("name", "status", "orderDate", "requiredDate", "estimatedCost"));
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

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final COrderService orderService = CSpringContext.getBean(COrderService.class);
		if (orderService == null) {
			throw new CInitializationException("COrderService not available for sample data initialization");
		}
		for (int i = 1; i <= (minimal ? 1 : 2); i++) {
			final COrder order = orderService.newEntity("Order " + i, project);
			orderService.initializeNewEntity(order);
			order.setProviderCompanyName("Provider " + i);
			order.setOrderNumber("ORD-" + i);
			orderService.save(order);
		}
		// LOGGER.info("Initialized {} sample orders for project {}", created, project.getName());
	}
}
