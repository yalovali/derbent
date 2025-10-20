package tech.derbent.app.workflow.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.app.activities.service.CActivityInitializerService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

public class CWorkflowEntityInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Workflow Information";
	static final Class<?> clazz = CWorkflowEntity.class;
	static Map<String, EntityFieldInfo> fields;
	static EntityFieldInfo info;
	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".21";
	private static final String menuTitle = MenuTitle_PROJECT + ".Workflows";
	private static final String pageDescription = "Workflow management for projects";
	private static final String pageTitle = "Workflow Management";
	private static final boolean showInQuickToolbar = true;
}
