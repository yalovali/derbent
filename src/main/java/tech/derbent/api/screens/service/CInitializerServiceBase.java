package tech.derbent.api.screens.service;

import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.services.pageservice.CPageServiceUtility;
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
			Class<?> bean = CEntityRegistry.getEntityServiceClass(clazz.getSimpleName());
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
		String pageServiceName = CPageServiceUtility.getPageServiceNameForEntityClass(entityClass);
		if (pageServiceName != null) {
			page.setPageService(pageServiceName);
		}
		return page;
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
