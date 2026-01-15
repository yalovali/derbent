package tech.derbent.app.orders.order.service;

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
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.orders.order.domain.COrder;

public class COrderInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = COrder.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(COrderInitializerService.class);
	private static final String menuOrder = Menu_Order_FINANCE + ".9";
	private static final String menuTitle = MenuTitle_FINANCE + ".Orders";
	private static final String pageDescription = "Order management with approval workflow";
	private static final String pageTitle = "Order Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Classification"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
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
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			
			// Attachments section - standard section for ALL entities
			tech.derbent.app.attachments.service.CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			
			// Comments section - standard section for discussion entities
			tech.derbent.app.comments.service.CCommentInitializerService.addCommentsSection(detailSection, clazz);
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating order view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "entityType", "status", "orderDate", "requiredDate", "deliveryDate", "providerCompanyName",
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

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final COrderService orderService = CSpringContext.getBean(COrderService.class);
		if (orderService == null) {
			throw new CInitializationException("COrderService not available for sample data initialization");
		}
		int created = 0;
		for (int i = 1; i <= (minimal ? 1 : 2); i++) {
			final COrder order = orderService.newEntity("Order " + i, project);
			orderService.initializeNewEntity(order);
			order.setProviderCompanyName("Provider " + i);
			order.setOrderNumber("ORD-" + i);
			orderService.save(order);
			created++;
		}
		LOGGER.info("Initialized {} sample orders for project {}", created, project.getName());
	}
}
