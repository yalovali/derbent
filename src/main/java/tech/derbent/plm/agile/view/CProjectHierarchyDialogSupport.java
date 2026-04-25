package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.view.CDialogDynamicPage;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/**
 * Shared dialog flow for hierarchy-aware create/edit/add-existing actions.
 */
public final class CProjectHierarchyDialogSupport {

	private static final String ALL_TYPES_DISPLAY_NAME = "All Types";
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectHierarchyDialogSupport.class);

	private final CHierarchyNavigationService hierarchyNavigationService;
	private final CParentRelationService parentRelationService;

	public CProjectHierarchyDialogSupport(final CParentRelationService parentRelationService,
			final CHierarchyNavigationService hierarchyNavigationService, final ISessionService sessionService) {
		Check.notNull(parentRelationService, "parentRelationService cannot be null");
		Check.notNull(hierarchyNavigationService, "hierarchyNavigationService cannot be null");
		Check.notNull(sessionService, "sessionService cannot be null");
		this.parentRelationService = parentRelationService;
		this.hierarchyNavigationService = hierarchyNavigationService;
	}

	public void openAddExistingDialog(final String dialogTitle, final CProjectItem<?> parent,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter, final Runnable onSuccess) {
		try {
			Check.notNull(parent, "Parent cannot be null");
			Check.notNull(parent.getId(), "Parent must be saved before adding children");
			final List<EntityTypeConfig<?>> entityTypes = createFilterableTypes(getExistingChildClasses(parent, classFilter));
			if (entityTypes.isEmpty()) {
				CNotificationService.showWarning("No compatible existing child items were found in this project");
				return;
			}
			final CDialogEntitySelection<CProjectItem<?>> dialog =
					new CDialogEntitySelection<>(dialogTitle, entityTypes, config -> listAvailableItemsForSelection(parent, config), items -> {
						items.forEach(item -> attachChildToParent(item, parent));
						runSafely(onSuccess);
					}, true);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open add-existing hierarchy dialog reason={}", e.getMessage(), e);
			CNotificationService.showException("Failed to add existing items", e);
		}
	}

	public void openCreateDialog(final CProject<?> project, final CProjectItem<?> parent,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter, final Runnable onSuccess) {
		try {
			final List<EntityTypeConfig<?>> entityTypes =
					createFilterableTypes(parent != null ? getCreatableChildClasses(parent, classFilter) : getCreatableRootClasses(project, classFilter));
			if (entityTypes.isEmpty()) {
				CNotificationService.showWarning(parent == null ? "No compatible root item types are available for this project"
						: "No child-capable entity types match this hierarchy context");
				return;
			}
			final List<EntityTypeConfig<?>> concreteTypes = entityTypes.stream().filter(config -> !isAllTypesConfig(config)).toList();
			if (concreteTypes.size() == 1) {
				createNewChildEntity(project, parent, concreteTypes.get(0), onSuccess);
				return;
			}
			final CDialogAgileChildTypeSelection dialog = new CDialogAgileChildTypeSelection(concreteTypes,
					config -> createNewChildEntity(project, parent, config, onSuccess));
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to create hierarchy item reason={}", e.getMessage(), e);
			CNotificationService.showException("Failed to create item", e);
		}
	}

	public void openEditDialog(final CProjectItem<?> entity, final Runnable onSaveSuccess) throws Exception {
		openEditDialog(entity, onSaveSuccess, null);
	}

	public void openEditDialog(final CProjectItem<?> entity, final Runnable onSaveSuccess, final Runnable onCancel) throws Exception {
		Check.notNull(entity, "Entity cannot be null");
		final String route = CDialogDynamicPage.buildDynamicRouteForEntity(entity);
		final CDialogDynamicPage dialog = new CDialogDynamicPage(route);
		dialog.configureInlineSaveCancelMode(onSaveSuccess, onCancel);
		dialog.open();
	}

	public boolean hasCreatableItems(final CProject<?> project, final CProjectItem<?> parent,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter) {
		return !(parent != null ? getCreatableChildClasses(parent, classFilter) : getCreatableRootClasses(project, classFilter)).isEmpty();
	}

	public boolean hasSelectableExistingChildren(final CProjectItem<?> parent,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter) {
		return !getExistingChildClasses(parent, classFilter).isEmpty();
	}

	private void attachChildToParent(final CProjectItem<?> child, final CProjectItem<?> parent) {
		Check.notNull(child, "child cannot be null");
		Check.notNull(parent, "parent cannot be null");
		parentRelationService.setParent(child, parent);
		saveEntity(child);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private EntityTypeConfig<?> createAllTypesConfig(final EntityTypeConfig<?> firstType) {
		return new EntityTypeConfig(ALL_TYPES_DISPLAY_NAME, firstType.getEntityClass(), firstType.getService());
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private EntityTypeConfig<?> createEntityTypeConfig(final Class<? extends CProjectItem<?>> entityClass) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		final CAbstractService<?> service = (CAbstractService<?>) CSpringContext.getBean(serviceClass);
		return EntityTypeConfig.createWithRegistryName((Class) entityClass, (CAbstractService) service);
	}

	private List<EntityTypeConfig<?>> createFilterableTypes(final List<Class<? extends CProjectItem<?>>> entityClasses) {
		if (entityClasses == null || entityClasses.isEmpty()) {
			return List.of();
		}
		final List<Class<? extends CProjectItem<?>>> sortedClasses = entityClasses.stream()
				.sorted(Comparator.comparing(entityClass -> {
					final String title = CEntityRegistry.getEntityTitleSingular(entityClass);
					return title != null ? title : entityClass.getSimpleName();
				}, String.CASE_INSENSITIVE_ORDER))
				.toList();
		final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		for (final Class<? extends CProjectItem<?>> entityClass : sortedClasses) {
			entityTypes.add(createEntityTypeConfig(entityClass));
		}
		if (entityTypes.size() <= 1) {
			return entityTypes;
		}
		final List<EntityTypeConfig<?>> filterableTypes = new ArrayList<>();
		filterableTypes.add(createAllTypesConfig(entityTypes.get(0)));
		filterableTypes.addAll(entityTypes);
		return filterableTypes;
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private void createNewChildEntity(final CProject<?> project, final CProjectItem<?> parent, final EntityTypeConfig<?> config,
			final Runnable onSuccess) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(config, "config cannot be null");
		try {
			final CAbstractService service = config.getService();
			Check.isTrue(service instanceof CEntityOfProjectService<?>, "Hierarchy item service must extend CEntityOfProjectService");
			final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) service;
			final Object created = projectService.newEntity("New " + config.getDisplayName(), project);
			Check.isTrue(created instanceof CProjectItem, "New entity is not a project item: " + created.getClass().getSimpleName());
			final CProjectItem<?> child = (CProjectItem<?>) created;
			final Object saved = service.save(child);
			Check.isTrue(saved instanceof CProjectItem, "Saved entity is not a project item");
			final CProjectItem<?> savedChild = (CProjectItem<?>) saved;
			if (parent != null) {
				parentRelationService.setParent(savedChild, parent);
				service.save(savedChild);
			}
			openEditDialog(savedChild, onSuccess, () -> {
				try {
					service.delete(savedChild);
					runSafely(onSuccess);
				} catch (final Exception e) {
					LOGGER.error("Failed to delete cancelled hierarchy entity reason={}", e.getMessage(), e);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Failed to create new hierarchy entity reason={}", e.getMessage(), e);
			CNotificationService.showException("Failed to create new item", e);
		}
	}

	private List<Class<? extends CProjectItem<?>>> getCreatableChildClasses(final CProjectItem<?> parent,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter) {
		if (parent == null) {
			return List.of();
		}
		return hierarchyNavigationService.listCreatableChildEntityClasses(parent).stream().filter(entityClass -> matchesFilter(entityClass, classFilter))
				.toList();
	}

	private List<Class<? extends CProjectItem<?>>> getCreatableRootClasses(final CProject<?> project,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter) {
		if (project == null) {
			return List.of();
		}
		final List<Class<? extends CProjectItem<?>>> classes = new ArrayList<>();
		for (final Class<? extends CProjectItem<?>> entityClass : hierarchyNavigationService.listHierarchyEntityClasses()) {
			try {
				final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
				final Object serviceBean = serviceClass != null ? CSpringContext.getBean(serviceClass) : null;
				if (!(serviceBean instanceof CEntityOfProjectService<?> projectService)) {
					continue;
				}
				final Object previewEntity = projectService.newEntity("Preview " + entityClass.getSimpleName(), project);
				if (!(previewEntity instanceof CProjectItem<?> projectItem) || CHierarchyNavigationService.canEntityHaveParent(projectItem)
						|| !matchesFilter(entityClass, classFilter)) {
					continue;
				}
				classes.add(entityClass);
			} catch (final Exception e) {
				LOGGER.debug("Could not resolve root hierarchy class {} reason={}", entityClass.getSimpleName(), e.getMessage());
			}
		}
		return classes.stream().distinct().toList();
	}

	private List<Class<? extends CProjectItem<?>>> getExistingChildClasses(final CProjectItem<?> parent,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter) {
		if (parent == null) {
			return List.of();
		}
		return hierarchyNavigationService.listSelectableChildCandidates(parent).stream().map(this::resolveProjectItemClass).distinct()
				.filter(entityClass -> matchesFilter(entityClass, classFilter)).toList();
	}

	private boolean isAllTypesConfig(final EntityTypeConfig<?> config) {
		return config != null && ALL_TYPES_DISPLAY_NAME.equals(config.getDisplayName());
	}

	private List<CProjectItem<?>> listAvailableItemsForSelection(final CProjectItem<?> parent, final EntityTypeConfig<?> config) {
		if (parent == null || config == null) {
			return List.of();
		}
		final List<CProjectItem<?>> candidates = hierarchyNavigationService.listSelectableChildCandidates(parent);
		if (isAllTypesConfig(config)) {
			return candidates;
		}
		return candidates.stream().filter(candidate -> config.getEntityClass().isAssignableFrom(candidate.getClass())).toList();
	}

	private boolean matchesFilter(final Class<? extends CProjectItem<?>> entityClass,
			final Predicate<Class<? extends CProjectItem<?>>> classFilter) {
		return classFilter == null || classFilter.test(entityClass);
	}

	private void runSafely(final Runnable action) {
		if (action != null) {
			action.run();
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends CProjectItem<?>> resolveProjectItemClass(final CProjectItem<?> item) {
		return (Class<? extends CProjectItem<?>>) item.getClass();
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private void saveEntity(final CProjectItem<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService service = (CAbstractService) CSpringContext.getBean(serviceClass);
		service.save(entity);
	}
}
