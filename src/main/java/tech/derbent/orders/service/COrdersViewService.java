package tech.derbent.orders.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.orders.domain.COrder;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenLinesService;

public class COrdersViewService {
	public static final String BASE_VIEW_NAME = "Orders View";
	public static final String BASE_PANEL_NAME = "Orders Information";
	private static Logger LOGGER = LoggerFactory.getLogger(COrdersViewService.class);

	public static CScreen createBasicView(final CProject project) {
		try {
			final CScreen scr = new CScreen();
			final Class<?> clazz = COrder.class;
			final String entityType = clazz.getSimpleName().replaceFirst("^C", "");
			scr.setProject(project);
			scr.setEntityType(clazz.getSimpleName());
			scr.setHeaderText(entityType + " View");
			scr.setIsActive(Boolean.TRUE);
			scr.setScreenTitle(entityType + " View");
			scr.setName(BASE_VIEW_NAME);
			scr.setDescription(entityType + " View Details");
			// create screen lines
			scr.addScreenLine(CScreenLinesService.createSection(COrdersViewService.BASE_PANEL_NAME));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "name"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "description"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "project"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "orderType"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "status"));
			scr.addScreenLine(CScreenLinesService.createSection("Schedule"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdBy"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "createdDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "orderDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "requiredDate"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "deliveryDate"));
			scr.addScreenLine(CScreenLinesService.createSection("Provieder Information"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "providerCompanyName"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "providerContactName"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "providerEmail"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "requestor"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "responsible"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "currency"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "estimatedCost"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "actualCost"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "orderNumber"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "deliveryAddress"));
			// scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "approvals"));
			scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, "assignedTo"));
			scr.debug_printScreenInformation();
			return scr;
		} catch (final Exception e) {
			LOGGER.error("Error creating basic user view: {}", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
