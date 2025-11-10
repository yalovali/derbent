package tech.derbent.api.screens.service;

import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public abstract class CInitializerServiceBase {

	protected static final String Menu_Order_PROJECT = "1";
	protected static final String Menu_Order_ROLES = "40";
	protected static final String Menu_Order_SETUP = "20";
	protected static final String Menu_Order_SYSTEM = "10";
	protected static final String Menu_Order_TYPES = "30";
	protected static final String MenuTitle_PROJECT = "Project";
	protected static final String MenuTitle_ROLES = "Roles";
	protected static final String MenuTitle_SETUP = "Setup";
	protected static final String MenuTitle_SYSTEM = "System";
	protected static final String MenuTitle_TYPES = "Types";

	protected static CGridEntity createBaseGridEntity(CProject project, Class<?> clazz) {
		String baseViewName;
		try {
			baseViewName = (String) clazz.getField("VIEW_NAME").get(null);
			CGridEntity grid = new CGridEntity(baseViewName, project);
			grid.setDescription(baseViewName + " Grid");
			Class<?> bean = CAuxillaries.getEntityServiceClasses(clazz.getSimpleName());
			grid.setDataServiceBeanName(bean.getSimpleName());
			grid.setAttributeNonDeletable(true);
			return grid;
		} catch (Exception e) {
			throw new RuntimeException("Error accessing VIEW_NAME field in class " + clazz.getName(), e);
		}
	}

	protected static CDetailSection createBaseScreenEntity(CProject project, Class<?> clazz) throws Exception {
		try {
			String baseViewName = (String) clazz.getField("VIEW_NAME").get(null);
			return createBaseScreenEntity(project, clazz, baseViewName, 0);
		} catch (Exception e) {
			throw new Exception("Error accessing VIEW_NAME or getViewClassStatic field in class " + clazz.getName(), e);
		}
	}

	protected static CDetailSection createBaseScreenEntity(CProject project, Class<?> clazz, String baseViewName, int dummy) {
		final CDetailSection scr = new CDetailSection();
		scr.setProject(project);
		scr.setEntityType(clazz.getSimpleName());
		scr.setHeaderText(baseViewName);
		scr.setActive(Boolean.TRUE);
		scr.setScreenTitle(baseViewName);
		scr.setName(baseViewName);
		scr.setDescription(baseViewName);
		scr.setAttributeNonDeletable(true);
		scr.setDefaultSection(null);
		return scr;
	}

	protected static CPageEntity createPageEntity(Class<?> entityClass, CProject project, CGridEntity grid, CDetailSection detailSection,
			String menuLocation, String pageTitle, String description, String order) throws Exception {
		CPageEntity page = new CPageEntity(grid.getName(), project);
		page.setDescription(description);
		page.setMenuTitle(menuLocation);
		page.setPageTitle(pageTitle);
		page.setGridEntity(grid);
		page.setDetailSection(detailSection);
		page.setContent("");
		page.setAttributeNonDeletable(true);
		page.setRequiresAuthentication(true);
		page.setIcon(CColorUtils.getStaticIconFilename(entityClass));
		page.setColor(CColorUtils.getStaticIconColorCode(entityClass));
		page.setMenuOrder(order);
		// Set the pageService based on entity class
		String pageServiceName = getPageServiceNameForEntityClass(entityClass);
		if (pageServiceName != null) {
			page.setPageService(pageServiceName);
		}
		return page;
	}

	/** Maps entity class to corresponding PageService class name
	 * @param entityClass The entity class
	 * @return The PageService class name, or null if not mapped */
	protected static String getPageServiceNameForEntityClass(Class<?> entityClass) {
		String className = entityClass.getSimpleName();
		switch (className) {
		// Main entities
		case "CActivity":
			return "CPageServiceActivity";
		case "CComment":
			return "CPageServiceComment";
		case "CCompany":
			return "CPageServiceCompany";
		case "CDecision":
			return "CPageServiceDecision";
		case "CMeeting":
			return "CPageServiceMeeting";
		case "COrder":
			return "CPageServiceOrder";
		case "CProject":
			return "CPageServiceProject";
		case "CRisk":
			return "CPageServiceRisk";
		case "CUser":
			return "CPageServiceUser";
		case "CSystemSettings":
			return "CPageServiceSystemSettings";
		// Type/Status entities
		case "CActivityPriority":
			return "CPageServiceActivityPriority";
		case "CProjectItemStatus":
			return "CPageServiceProjectItemStatus";
		case "CRiskType":
			return "CPageServiceRiskType";
		case "CActivityType":
			return "CPageServiceActivityType";
		case "CCommentPriority":
			return "CPageServiceCommentPriority";
		case "CDecisionStatus":
			return "CPageServiceDecisionStatus";
		case "CDecisionType":
			return "CPageServiceDecisionType";
		case "CMeetingStatus":
			return "CPageServiceMeetingStatus";
		case "CMeetingType":
			return "CPageServiceMeetingType";
		case "COrderStatus":
			return "CPageServiceOrderStatus";
		case "COrderType":
			return "CPageServiceOrderType";
		case "COrderApproval":
			return "CPageServiceOrderApproval";
		case "CApprovalStatus":
			return "CPageServiceApprovalStatus";
		case "CCurrency":
			return "CPageServiceCurrency";
		case "CRiskStatus":
			return "CPageServiceRiskStatus";
		case "CUserCompanyRole":
			return "CPageServiceUserCompanyRole";
		case "CUserCompanySetting":
			return "CPageServiceUserCompanySetting";
		case "CUserProjectRole":
			return "CPageServiceUserProjectRole";
		case "CUserProjectSettings":
			return "CPageServiceUserProjectSettings";
		// System entities
		case "CPageEntity":
			return "CPageServicePageEntity";
		case "CGridEntity":
			return "CPageServiceGridEntity";
		case "CWorkflowEntity":
			return "CPageServiceWorkflowEntity";
		default:
			// Return null for entities that don't have a PageService yet
			return null;
		}
	}

	public static void initBase(Class<?> clazz, final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService, final CDetailSection detailSection,
			final CGridEntity grid, String menuTitle, String pageTitle, String pageDescription, boolean showInQuickToolbar, String order)
			throws Exception {
		Check.notNull(project, "project cannot be null");
		Check.notNull(gridEntityService, "gridEntityService cannot be null");
		Check.notNull(detailSectionService, "detailSectionService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		detailSectionService.save(detailSection);
		gridEntityService.save(grid);
		final CPageEntity page = createPageEntity(clazz, project, grid, detailSection, menuTitle, pageTitle, pageDescription, order);
		page.setAttributeShowInQuickToolbar(showInQuickToolbar);
		pageEntityService.save(page);
	}
}
