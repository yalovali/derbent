package tech.derbent.api.screens.service;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.services.pageservice.CPageServiceUtility;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

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

	protected static CGridEntity createBaseGridEntity(final CProject project, final Class<?> clazz) {
		String baseViewName;
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

	protected static CDetailSection createBaseScreenEntity(final CProject project, final Class<?> clazz) throws Exception {
		try {
			final String baseViewName = (String) clazz.getField("VIEW_NAME").get(null);
			return createBaseScreenEntity(project, clazz, baseViewName, 0);
		} catch (final Exception e) {
			throw new Exception("Error accessing VIEW_NAME or getViewClassStatic field in class " + clazz.getName(), e);
		}
	}

	protected static CDetailSection createBaseScreenEntity(final CProject project, final Class<?> clazz, final String baseViewName, final int dummy) {
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

	protected static CPageEntity createPageEntity(final Class<?> entityClass, final CProject project, final CGridEntity grid,
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
		final String pageServiceName = CPageServiceUtility.getPageServiceNameForEntityClass(entityClass);
		if (pageServiceName != null) {
			page.setPageService(pageServiceName);
		}
		return page;
	}

	public static void initBase(final Class<?> clazz, final CProject project, final CGridEntityService gridEntityService,
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
	protected static <EntityClass extends CEntityOfProject<EntityClass>> void initializeProjectEntity(final String[][] nameAndDescription,
			final CEntityOfProjectService<EntityClass> service, final CProject project, final boolean minimal) throws Exception {
		try {
			for (final String[] typeData : nameAndDescription) {
				final CEntityOfProject<EntityClass> item = service.newEntity(typeData[0], project);
				item.setDescription(typeData[1]);
				item.setActive(Boolean.TRUE);
				
				// Try to set color field if it exists
				try {
					item.getClass().getMethod("setColor", String.class).invoke(item, CColorUtils.getRandomColor(true));
				} catch (final NoSuchMethodException e) {
					// Color field doesn't exist, ignore
				}
				
				// Handle CTypeEntity (has workflow)
				if (item instanceof CTypeEntity<?>) {
					final CTypeEntity<?> typeItem = (CTypeEntity<?>) item;
					final CWorkflowEntityService workflowEntityService = CSpringContext.getBean(CWorkflowEntityService.class);
					// pick random workflow for project
					typeItem.setWorkflow(workflowEntityService.getRandomByProject(project));
				}
				
				// Handle entities with status and workflow (need entityType)
				if (item instanceof IHasStatusAndWorkflow) {
					final IHasStatusAndWorkflow<?> statusItem = (IHasStatusAndWorkflow<?>) item;
					// Try to get entityType from metadata and set it
					try {
						// Get the entityType field
						final java.lang.reflect.Field entityTypeField = findFieldInHierarchy(item.getClass(), "entityType");
						if (entityTypeField != null) {
							entityTypeField.setAccessible(true);
							// Get metadata annotation
							final tech.derbent.api.annotations.AMetaData metadata = entityTypeField.getAnnotation(tech.derbent.api.annotations.AMetaData.class);
							if (metadata != null && !metadata.dataProviderBean().isEmpty()) {
								// Get service bean
								final String beanName = metadata.dataProviderBean();
								final Object serviceBean = CSpringContext.getBean(beanName);
								// Call getRandom(project) method
								final java.lang.reflect.Method getRandomMethod = serviceBean.getClass().getMethod("getRandom", CProject.class);
								final Object randomType = getRandomMethod.invoke(serviceBean, project);
								statusItem.setEntityType((CTypeEntity<?>) randomType);
							}
						}
					} catch (final Exception e) {
						// EntityType not set, entity will work without it
					}
				}
				
				service.save((EntityClass) item);
				if (minimal) {
					return;
				}
			}
		} catch (final Exception e) {
			throw e;
		}
	}
	
	/** Helper method to find a field in class hierarchy.
	 * @param clazz     the class to search
	 * @param fieldName the field name to find
	 * @return the field or null if not found */
	private static java.lang.reflect.Field findFieldInHierarchy(Class<?> clazz, final String fieldName) {
		while (clazz != null) {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (final NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}
}
