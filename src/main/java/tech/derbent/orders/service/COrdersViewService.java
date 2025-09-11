package tech.derbent.orders.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.orders.domain.COrder;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesSampleBase;
import tech.derbent.screens.service.CDetailLinesService;

public class COrdersViewService extends CDetailLinesSampleBase {

	public static final String BASE_PANEL_NAME = "Orders Information";
	private static Logger LOGGER = LoggerFactory.getLogger(COrdersViewService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final Class<?> clazz = COrder.class;
			CDetailSection scr = createBaseScreenEntity(project, clazz);
			// create screen lines
			scr.addScreenLine(CDetailLinesService.createSection(COrdersViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "orderType"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CDetailLinesService.createSection("Schedule"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "orderDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiredDate"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deliveryDate"));
			scr.addScreenLine(CDetailLinesService.createSection("Provieder Information"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "providerCompanyName"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "providerContactName"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "providerEmail"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requestor"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsible"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "actualCost"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "orderNumber"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deliveryAddress"));
			// scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "approvals"));
			scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
