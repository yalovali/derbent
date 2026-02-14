package tech.derbent.api.screens.service;

import java.lang.reflect.Field;
import java.util.function.ObjIntConsumer;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.session.service.ISessionService;

public abstract class CInitializerServiceBase {
	protected static final String Menu_Order_CRM = "5";
	protected static final String Menu_Order_DEVELOPMENT = "9999.";
	protected static final String Menu_Order_FINANCE = "10";
	protected static final String Menu_Order_POLICIES = "60";
	protected static final String Menu_Order_PRODUCTS = "20";
	protected static final String Menu_Order_PROJECT = "1";
	protected static final String Menu_Order_ROLES = "400";
	protected static final String Menu_Order_SETUP = "400";
	protected static final String Menu_Order_SYSTEM = "500";
	protected static final String Menu_Order_TESTS = "15";
	protected static final String Menu_Order_TYPES = "130";
	protected static final String MenuTitle_CRM = "CRM";
	protected static final String MenuTitle_DEVELOPMENT = "Development.";
	protected static final String MenuTitle_FINANCE = "Finance";
	protected static final String MenuTitle_POLICIES = "Policies";
	protected static final String MenuTitle_PRODUCTS = "Products";
	protected static final String MenuTitle_PROJECT = "Project";
	protected static final String MenuTitle_ROLES = "Roles";
	protected static final String MenuTitle_SETUP = "Setup";
	protected static final String MenuTitle_SYSTEM = "System";
	protected static final String MenuTitle_TESTS = "Tests";
	protected static final String MenuTitle_TYPES = "Types";

	protected static CGridEntity createBaseGridEntity(final CProject<?> project, final Class<?> clazz) {
		final String baseViewName;
		try {
			baseViewName = (String) clazz.getField("VIEW_NAME").get(null);
			final CGridEntity grid = new CGridEntity(baseViewName, project);
			grid.setDescription(baseViewName + " Grid");
			final Class<?> bean = CEntityRegistry.getEntityServiceClass(clazz.getSimpleName());
			grid.setDataServiceBeanName(bean.getSimpleName());
			grid.setAttributeNonDeletable(true);
			return grid;
		} catch (final Exception e) {
			throw new RuntimeException("Error accessing VIEW_NAME field in class " + clazz.getName(), e);
		}
	}

	protected static CDetailSection createBaseScreenEntity(final CProject<?> project, final Class<?> clazz) throws Exception {
		try {
			final String baseViewName = (String) clazz.getField("VIEW_NAME").get(null);
			return createBaseScreenEntity(project, clazz, baseViewName);
		} catch (final Exception e) {
			throw new Exception("Error accessing VIEW_NAME or getViewClassStatic field in class " + clazz.getName(), e);
		}
	}

	protected static CDetailSection createBaseScreenEntity(final CProject<?> project, final Class<?> clazz, final String baseViewName) {
		final CDetailSection scr = new CDetailSection(baseViewName, project);
		scr.setEntityType(clazz.getSimpleName());
		scr.setHeaderText(baseViewName);
		scr.setActive(Boolean.TRUE);
		scr.setScreenTitle(baseViewName);
		scr.setDescription(baseViewName);
		scr.setAttributeNonDeletable(true);
		scr.setDefaultSection(null);
		return scr;
	}

	protected static CPageEntity createPageEntity(final Class<?> entityClass, final CProject<?> project, final CGridEntity grid,
			final CDetailSection detailSection, final String menuLocation, final String pageTitle, final String description, final String order)
			throws Exception {
		final CPageEntity page = new CPageEntity(grid.getName(), project);
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
		// Get PageService class name directly from entity registry
		final Class<?> pageServiceClass = CEntityRegistry.getPageServiceClass(entityClass);
		final String pageServiceName = pageServiceClass != null ? pageServiceClass.getSimpleName() : null;
		Check.notNull(pageServiceName, "Page service name not found for entity class " + entityClass.getName());
		page.setPageService(pageServiceName);
		return page;
	}

	/** Helper method to find a field in class hierarchy.
	 * @param clazz     the class to search
	 * @param fieldName the field name to find
	 * @return the field or null if not found */
	private static Field findFieldInHierarchy(final Class<?> clazz, final String fieldName) {
		Class<?> currentClazz = clazz;
		while (currentClazz != null) {
			try {
				return currentClazz.getDeclaredField(fieldName);
			} catch (final NoSuchFieldException e) {
				currentClazz = currentClazz.getSuperclass();
			}
		}
		return null;
	}

	@SuppressWarnings ("unused")
	private static <EntityClass extends CTypeEntity<EntityClass>> CTypeEntityService<?>
			getEntityTypeService(final IHasStatusAndWorkflow<?> statusItem) {
		final Field field = findFieldInHierarchy(statusItem.getClass(), "entityType");
		Check.notNull(field, "Field 'entityType' not found in class hierarchy of " + statusItem.getClass().getName());
		final Class<?> returnType = field.getType();
		final Class<?> serviceClass = CEntityRegistry.getEntityServiceClass(returnType.getSimpleName());
		Check.notNull(serviceClass, "Service class not found for " + returnType.getSimpleName());
		// if (!CTypeEntityService.class.isAssignableFrom(serviceClass)) {
		// throw new IllegalStateException("Service class " + serviceClass.getName() + " is not a CTypeEntityService");
		// }
		Check.instanceOf(serviceClass, CTypeEntityService.class, "Service class " + serviceClass.getName() + " is not a CTypeEntityService");
		final CTypeEntityService<?> typeService = (CTypeEntityService<?>) CSpringContext.getBean(serviceClass);
		Check.notNull(typeService, "Could not get bean of type " + serviceClass.getName());
		return typeService;
	}

	public static void initBase(final Class<?> clazz, final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService, final CDetailSection detailSection,
			final CGridEntity grid, final String menuTitle, final String pageTitle, final String pageDescription, final boolean showInQuickToolbar,
			final String order) throws Exception {
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

	@SuppressWarnings ("unchecked")
	protected static <EntityClass extends CEntityOfCompany<EntityClass>> void initializeCompanyEntity(final String[][] nameAndDescription,
			final CEntityOfCompanyService<EntityClass> service, final CCompany company, final boolean minimal,
			final ObjIntConsumer<EntityClass> customizer) throws Exception {
		try {
			// set session Company
			final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
			sessionService.setActiveCompany(company);
			int index = 0;
			for (final String[] typeData : nameAndDescription) {
				final CEntityOfCompany<EntityClass> item = service.newEntity(typeData[0], company);
				item.setDescription(typeData[1]);
				if (customizer != null) {
					customizer.accept((EntityClass) item, index);
				}
				service.save((EntityClass) item);
				index++;
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	protected static <EntityClass extends CEntityOfProject<EntityClass>> void initializeProjectEntity(final String[][] nameAndDescription,
			final CEntityOfProjectService<EntityClass> service, final CProject<?> project, final boolean minimal,
			final ObjIntConsumer<EntityClass> customizer) throws Exception {
		try {
			int index = 0;
			for (final String[] typeData : nameAndDescription) {
				final CEntityOfProject<EntityClass> item = service.newEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				// Use service initialization instead of manual setup
				service.initializeNewEntity(item);
				// Apply color after service initialization to avoid overriding defaults
				try {
					item.getClass().getMethod("setColor", String.class).invoke(item, CColorUtils.getRandomColor(true));
				} catch (final NoSuchMethodException ignore) {
					// no color setter present
				}
				// last-chance specialization
				if (customizer != null) {
					customizer.accept((EntityClass) item, index);
				}
				service.save((EntityClass) item);
				index++;
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			throw e;
		}
	}
}
