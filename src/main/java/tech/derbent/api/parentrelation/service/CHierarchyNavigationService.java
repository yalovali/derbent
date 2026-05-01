package tech.derbent.api.parentrelation.service;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.ProxyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;

/**
 * Central hierarchy helper that resolves level/type semantics without relying on agile class names.
 *
 * <p>This service is intentionally project-item centric so parent selectors, child dialogs, grid filters,
 * and Gnnt timelines all evaluate the exact same hierarchy rules.</p>
 */
@Service
@Profile({
		"derbent", "default", "test"
})
public class CHierarchyNavigationService {

	private static final Comparator<CProjectItem<?, ?>> ITEM_COMPARATOR =
			Comparator.comparingInt(CHierarchyNavigationService::getSortLevel)
					.thenComparing(item -> {
						final String title = CEntityRegistry.getEntityTitleSingular(ProxyUtils.getUserClass(item.getClass()));
						return title != null ? title : ProxyUtils.getUserClass(item.getClass()).getSimpleName();
					}, String.CASE_INSENSITIVE_ORDER)
					.thenComparing(CProjectItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
					.thenComparing(CProjectItem::getId, Comparator.nullsLast(Long::compareTo));

	private static final Logger LOGGER = LoggerFactory.getLogger(CHierarchyNavigationService.class);

	private static int getSortLevel(final CProjectItem<?, ?> item) {
		final int level = getEntityLevel(item);
		return level >= 0 ? level : Integer.MAX_VALUE - 1;
	}

	public static String buildEntityKey(final CProjectItem<?, ?> entity) {
		if (entity == null || entity.getId() == null) {
			return null;
		}
		return ProxyUtils.getUserClass(entity.getClass()).getSimpleName() + ":" + entity.getId();
	}

	public static String buildParentKey(final CProjectItem<?, ?> entity) {
		if (!(entity instanceof IHasParentRelation hasParentRelation)) {
			return null;
		}
		if (hasParentRelation.getParentRelation() == null || hasParentRelation.getParentRelation().getParentItemId() == null
				|| hasParentRelation.getParentRelation().getParentItemType() == null
				|| hasParentRelation.getParentRelation().getParentItemType().isBlank()) {
			return null;
		}
		return hasParentRelation.getParentRelation().getParentItemType() + ":" + hasParentRelation.getParentRelation().getParentItemId();
	}

	public static boolean canEntityHaveParent(final CProjectItem<?, ?> entity) {
		return entity instanceof IHasParentRelation && getEntityLevel(entity) != 0;
	}

	public static boolean canHaveChildren(final CProjectItem<?, ?> entity) {
		return resolveEntityType(entity).map(type -> type.getLevel() != null && type.getLevel() >= 0 && type.getCanHaveChildren()).orElse(false);
	}

	public static int getEntityLevel(final CProjectItem<?, ?> entity) {
		return resolveEntityType(entity).map(CTypeEntity::getLevel).orElse(-1);
	}

	public static boolean isSameEntity(final Object left, final Object right) {
		if (!(left instanceof CProjectItem<?, ?> leftItem) || !(right instanceof CProjectItem<?, ?> rightItem)) {
			return false;
		}
		final String leftKey = buildEntityKey(leftItem);
		final String rightKey = buildEntityKey(rightItem);
		return leftKey != null && leftKey.equals(rightKey);
	}

	public static Optional<CTypeEntity<?>> resolveEntityType(final CProjectItem<?, ?> entity) {
		if (entity == null) {
			return Optional.empty();
		}
		try {
			final Object entityType = entity.getClass().getMethod("getEntityType").invoke(entity);
			if (entityType instanceof CTypeEntity<?> typeEntity) {
				return Optional.of(typeEntity);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not resolve entity type for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
		}
		return Optional.empty();
	}

	public static CProjectItem<?, ?> resolveAncestorAtLevel(final Object entity, final int targetLevel) {
		if (!(entity instanceof CProjectItem<?, ?> projectItem)) {
			return null;
		}
		CProjectItem<?, ?> current = projectItem;
		final Set<String> visitedKeys = new HashSet<>();
		while (current != null) {
			final String currentKey = buildEntityKey(current);
			if (currentKey != null && !visitedKeys.add(currentKey)) {
				LOGGER.warn("Circular hierarchy detected while resolving ancestor for {}", currentKey);
				return null;
			}
			if (getEntityLevel(current) == targetLevel) {
				return current;
			}
			if (!(current instanceof IHasParentRelation hasParentRelation)) {
				return null;
			}
			current = hasParentRelation.getParentItem();
		}
		return null;
	}

	@Transactional(readOnly = true)
	public List<CProjectItem<?, ?>> getAllDescendants(final CProjectItem<?, ?> rootItem) {
		Check.notNull(rootItem, "Root item cannot be null");
		Check.notNull(rootItem.getProject(), "Root item project cannot be null");
		final List<CProjectItem<?, ?>> descendants = new ArrayList<>();
		final Deque<CProjectItem<?, ?>> stack = new ArrayDeque<>(listChildren(rootItem));
		final Set<String> visitedKeys = new HashSet<>();
		final String rootKey = buildEntityKey(rootItem);
		if (rootKey != null) {
			visitedKeys.add(rootKey);
		}
		while (!stack.isEmpty()) {
			final CProjectItem<?, ?> current = stack.pop();
			final String currentKey = buildEntityKey(current);
			if (currentKey == null || !visitedKeys.add(currentKey)) {
				continue;
			}
			descendants.add(current);
			stack.addAll(listChildren(current));
		}
		descendants.sort(ITEM_COMPARATOR);
		return descendants;
	}

	@Transactional(readOnly = true)
	public List<Class<? extends CProjectItem<?, ?>>> listCreatableChildEntityClasses(final CProjectItem<?, ?> parent) {
		Check.notNull(parent, "Parent item cannot be null");
		final List<Class<? extends CProjectItem<?, ?>>> classes = new ArrayList<>();
		for (final Class<? extends CProjectItem<?, ?>> entityClass : listHierarchyEntityClasses()) {
			final Optional<CProjectItem<?, ?>> previewChild = createPreviewItem(entityClass);
			if (previewChild.isPresent() && isValidParentCandidate(previewChild.get(), parent)) {
				classes.add(entityClass);
			}
		}
		classes.sort(Comparator.comparing(entityClass -> {
			final String title = CEntityRegistry.getEntityTitleSingular(entityClass);
			return title != null ? title : entityClass.getSimpleName();
		}, String.CASE_INSENSITIVE_ORDER));
		return classes;
	}

	@Transactional(readOnly = true)
	public List<CProjectItem<?, ?>> listChildren(final CProjectItem<?, ?> parent) {
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(parent.getProject(), "Parent project cannot be null");
		final String parentKey = buildEntityKey(parent);
		if (parentKey == null) {
			return List.of();
		}
		return listHierarchyItems(parent.getProject()).stream().filter(item -> parentKey.equals(buildParentKey(item))).sorted(ITEM_COMPARATOR).toList();
	}

	@Transactional(readOnly = true)
	public List<Class<? extends CProjectItem<?, ?>>> listHierarchyEntityClasses() {
		final List<Class<? extends CProjectItem<?, ?>>> entityClasses = new ArrayList<>();
		for (final String entityKey : CEntityRegistry.getAllRegisteredEntityKeys()) {
			try {
				final Class<?> entityClass = CEntityRegistry.getEntityClass(entityKey);
				if (entityClass == null || Modifier.isAbstract(entityClass.getModifiers())
						|| !CProjectItem.class.isAssignableFrom(entityClass)
						|| !IHasParentRelation.class.isAssignableFrom(entityClass)) {
					continue;
				}
				@SuppressWarnings("unchecked")
				final Class<? extends CProjectItem<?, ?>> typedClass = (Class<? extends CProjectItem<?, ?>>) entityClass;
				entityClasses.add(typedClass);
			} catch (final Exception e) {
				LOGGER.debug("Skipping hierarchy class lookup for {}: {}", entityKey, e.getMessage());
			}
		}
		return entityClasses;
	}

	@Transactional(readOnly = true)
	public List<CProjectItem<?, ?>> listHierarchyItems(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<CProjectItem<?, ?>> items = new ArrayList<>();
		for (final Class<? extends CProjectItem<?, ?>> entityClass : listHierarchyEntityClasses()) {
			try {
				final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
				if (serviceClass == null) {
					continue;
				}
				final Object serviceBean = CSpringContext.getBean(serviceClass);
				if (!(serviceBean instanceof CEntityOfProjectService<?> projectService)) {
					continue;
				}
				for (final Object rawItem : projectService.listByProject(project)) {
					if (rawItem instanceof CProjectItem<?, ?> projectItem) {
						items.add(projectItem);
						if (projectItem instanceof final ISprintableItem sprintableItem) {
							// Sprint planning drag/drop happens outside of this @Transactional method.
							// Touch the nested sprint reference here so later UI interactions do not hit LazyInitializationException.
							final var sprintItem = sprintableItem.getSprintItem();
							final var sprint = sprintItem != null ? sprintItem.getSprint() : null;
							if (sprint != null) {
								// Name + status are needed by sprint boards/filters after this method returns.
								sprint.getName();
								if (sprint.getStatus() != null) {
									sprint.getStatus().getName();
									sprint.getStatus().getFinalStatus();
								}
							}
						}
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Skipping hierarchy items for {}: {}", entityClass.getSimpleName(), e.getMessage());
			}
		}
		items.sort(ITEM_COMPARATOR);
		return items;
	}

	@Transactional(readOnly = true)
	public List<CProjectItem<?, ?>> listItemsAtLevel(final CProject<?> project, final int level) {
		return listHierarchyItems(project).stream().filter(item -> getEntityLevel(item) == level).toList();
	}

	@Transactional(readOnly = true)
	public List<CProjectItem<?, ?>> listParentCandidates(final CProjectItem<?, ?> child) {
		Check.notNull(child, "Child item cannot be null");
		Check.notNull(child.getProject(), "Child project cannot be null");
		return listHierarchyItems(child.getProject()).stream().filter(candidateParent -> isValidParentCandidate(child, candidateParent))
				.sorted(ITEM_COMPARATOR).toList();
	}

	@Transactional(readOnly = true)
	public List<CProjectItem<?, ?>> listSelectableChildCandidates(final CProjectItem<?, ?> parent) {
		Check.notNull(parent, "Parent item cannot be null");
		Check.notNull(parent.getProject(), "Parent project cannot be null");
		final Set<String> existingChildKeys = listChildren(parent).stream().map(CHierarchyNavigationService::buildEntityKey).filter(key -> key != null)
				.collect(Collectors.toSet());
		return listHierarchyItems(parent.getProject()).stream()
				.filter(candidateChild -> !existingChildKeys.contains(buildEntityKey(candidateChild)))
				.filter(candidateChild -> isValidParentCandidate(candidateChild, parent))
				.sorted(ITEM_COMPARATOR).toList();
	}

	public boolean isValidParentCandidate(final CProjectItem<?, ?> child, final CProjectItem<?, ?> candidateParent) {
		if (child == null || candidateParent == null || child.getProject() == null || candidateParent.getProject() == null) {
			return false;
		}
		if (!child.getProject().equals(candidateParent.getProject())) {
			return false;
		}
		if (isSameEntity(child, candidateParent)) {
			return false;
		}
		if (!canHaveChildren(candidateParent)) {
			return false;
		}
		final int childLevel = getEntityLevel(child);
		final int parentLevel = getEntityLevel(candidateParent);
		if (parentLevel < 0) {
			return false;
		}
		if (childLevel == 0) {
			return false;
		}
		if (childLevel > 0 && parentLevel != childLevel - 1) {
			return false;
		}
		if (child.getId() != null && candidateParent.getId() != null) {
			final String parentKey = buildEntityKey(candidateParent);
			final boolean isDescendant = getAllDescendants(child).stream().map(CHierarchyNavigationService::buildEntityKey)
					.anyMatch(parentKey::equals);
			if (isDescendant) {
				return false;
			}
		}
		return true;
	}

	private Optional<CProjectItem<?, ?>> createPreviewItem(final Class<? extends CProjectItem<?, ?>> entityClass) {
		try {
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			if (serviceClass == null) {
				return Optional.empty();
			}
			final Object serviceBean = CSpringContext.getBean(serviceClass);
			if (!(serviceBean instanceof CEntityOfProjectService<?> projectService)) {
				return Optional.empty();
			}
			final Object previewItem = projectService.newEntity("Preview " + entityClass.getSimpleName());
			if (previewItem instanceof CProjectItem<?, ?> projectItem) {
				return Optional.of(projectItem);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create hierarchy preview for {}: {}", entityClass.getSimpleName(), e.getMessage());
		}
		return Optional.empty();
	}
}
