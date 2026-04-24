package tech.derbent.plm.gnnt.gnntviewentity.service;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.ProxyUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.parentrelation.domain.CParentRelation;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntBoardFilterCriteria;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;

@Service
@Profile({"derbent", "default"})
@PreAuthorize("isAuthenticated()")
public class CGnntTimelineService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntTimelineService.class);

	private static final class CVisibleHierarchyNode {

		private final List<CVisibleHierarchyNode> children;
		private final CGnntItem item;

		private CVisibleHierarchyNode(final CGnntItem item, final List<CVisibleHierarchyNode> children) {
			this.item = item;
			this.children = children;
		}
	}

	private static final class CHierarchyContext {

		private final Map<String, List<CProjectItem<?>>> childrenByParentKey;
		private final Map<String, String> parentKeyByEntityKey;
		private final List<CProjectItem<?>> rootItems;

		private CHierarchyContext(final List<CProjectItem<?>> rootItems, final Map<String, List<CProjectItem<?>>> childrenByParentKey,
				final Map<String, String> parentKeyByEntityKey) {
			this.rootItems = rootItems;
			this.childrenByParentKey = childrenByParentKey;
			this.parentKeyByEntityKey = parentKeyByEntityKey;
		}
	}

	private static final Comparator<CProjectItem<?>> HIERARCHY_ITEM_COMPARATOR =
			Comparator.comparingInt(CGnntTimelineService::getHierarchyTypeOrder)
					.thenComparing(CProjectItem::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
					.thenComparing(CProjectItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
					.thenComparing(CProjectItem::getId, Comparator.nullsLast(Long::compareTo));

	private static String buildEntityKey(final CProjectItem<?> entity) {
		if (entity == null || entity.getId() == null) {
			return null;
		}
		return ProxyUtils.getUserClass(entity.getClass()).getSimpleName() + ":" + entity.getId();
	}

	private static String buildParentKey(final CProjectItem<?> entity) {
		if (!(entity instanceof IHasParentRelation)) {
			return null;
		}
		final CParentRelation parentRelation = ((IHasParentRelation) entity).getParentRelation();
		if (parentRelation == null || parentRelation.getParentItemId() == null || parentRelation.getParentItemType() == null
				|| parentRelation.getParentItemType().isBlank()) {
			return null;
		}
		return parentRelation.getParentItemType() + ":" + parentRelation.getParentItemId();
	}

	private CGnntHierarchyResult buildHierarchyResult(final Map<String, CProjectItem<?>> entitiesByKey,
			final CGnntBoardFilterCriteria filterCriteria) {
		final CHierarchyContext hierarchyContext = buildHierarchyContext(entitiesByKey);
		if (filterCriteria != null && filterCriteria.getEntityType() != null) {
			return buildAnchoredHierarchyResult(hierarchyContext, filterCriteria);
		}
		final List<CVisibleHierarchyNode> rootNodes = new ArrayList<>();
		final Set<String> visited = new HashSet<>();
		final AtomicLong uniqueIdSequence = new AtomicLong(1);
		for (final CProjectItem<?> rootItem : hierarchyContext.rootItems) {
			final CVisibleHierarchyNode visibleRootNode =
					buildVisibleNode(rootItem, 0, hierarchyContext.childrenByParentKey, visited, uniqueIdSequence, filterCriteria);
			if (visibleRootNode != null) {
				rootNodes.add(visibleRootNode);
			}
		}
		final List<CProjectItem<?>> remainingItems = new ArrayList<>(entitiesByKey.values());
		remainingItems.sort(HIERARCHY_ITEM_COMPARATOR);
		for (final CProjectItem<?> remainingItem : remainingItems) {
			if (!visited.contains(buildEntityKey(remainingItem))) {
				LOGGER.warn("Gnnt hierarchy item {} was not attached to a root. Rendering it as a top-level item.",
						buildEntityKey(remainingItem));
				final CVisibleHierarchyNode visibleRemainingNode =
						buildVisibleNode(remainingItem, 0, hierarchyContext.childrenByParentKey, visited, uniqueIdSequence, filterCriteria);
				if (visibleRemainingNode != null) {
					rootNodes.add(visibleRemainingNode);
				}
			}
		}
		return flattenHierarchy(rootNodes);
	}

	private CHierarchyContext buildHierarchyContext(final Map<String, CProjectItem<?>> entitiesByKey) {
		final Map<String, List<CProjectItem<?>>> childrenByParentKey = new HashMap<>();
		final Map<String, String> parentKeyByEntityKey = new HashMap<>();
		final List<CProjectItem<?>> rootItems = new ArrayList<>();
		for (final CProjectItem<?> entity : entitiesByKey.values()) {
			final String entityKey = buildEntityKey(entity);
			final String parentKey = buildParentKey(entity);
			if (entityKey != null) {
				parentKeyByEntityKey.put(entityKey, parentKey);
			}
			if (parentKey == null || !entitiesByKey.containsKey(parentKey)) {
				rootItems.add(entity);
			} else {
				childrenByParentKey.computeIfAbsent(parentKey, key -> new ArrayList<>()).add(entity);
			}
		}
		rootItems.sort(HIERARCHY_ITEM_COMPARATOR);
		for (final List<CProjectItem<?>> children : childrenByParentKey.values()) {
			children.sort(HIERARCHY_ITEM_COMPARATOR);
		}
		return new CHierarchyContext(rootItems, childrenByParentKey, parentKeyByEntityKey);
	}

	private CGnntHierarchyResult buildAnchoredHierarchyResult(final CHierarchyContext hierarchyContext,
			final CGnntBoardFilterCriteria filterCriteria) {
		// Type-filter mode is anchor-down: matched items stay as roots and carry their full subtree, without ancestor context above them.
		final List<CProjectItem<?>> directMatches = hierarchyContext.rootItems.stream().flatMap(root -> flattenEntities(root, hierarchyContext).stream())
				.filter(entity -> matchesFilters(entity, filterCriteria)).sorted(HIERARCHY_ITEM_COMPARATOR).toList();
		final Set<String> directMatchKeys = new HashSet<>();
		for (final CProjectItem<?> directMatch : directMatches) {
			final String entityKey = buildEntityKey(directMatch);
			if (entityKey != null) {
				directMatchKeys.add(entityKey);
			}
		}
		final List<CVisibleHierarchyNode> rootNodes = new ArrayList<>();
		final Set<String> visited = new HashSet<>();
		final AtomicLong uniqueIdSequence = new AtomicLong(1);
		for (final CProjectItem<?> directMatch : directMatches) {
			final String entityKey = buildEntityKey(directMatch);
			if (entityKey == null || hasMatchedAncestor(entityKey, hierarchyContext.parentKeyByEntityKey, directMatchKeys)) {
				continue;
			}
			rootNodes.add(buildSubtreeNode(directMatch, 0, hierarchyContext.childrenByParentKey, visited, uniqueIdSequence));
		}
		for (final CProjectItem<?> directMatch : directMatches) {
			final String entityKey = buildEntityKey(directMatch);
			if (entityKey != null && !visited.contains(entityKey)) {
				rootNodes.add(buildSubtreeNode(directMatch, 0, hierarchyContext.childrenByParentKey, visited, uniqueIdSequence));
			}
		}
		return flattenHierarchy(rootNodes);
	}

	private List<CProjectItem<?>> flattenEntities(final CProjectItem<?> rootItem, final CHierarchyContext hierarchyContext) {
		final List<CProjectItem<?>> entities = new ArrayList<>();
		appendEntityAndDescendants(rootItem, hierarchyContext.childrenByParentKey, entities);
		return entities;
	}

	private void appendEntityAndDescendants(final CProjectItem<?> entity, final Map<String, List<CProjectItem<?>>> childrenByParentKey,
			final List<CProjectItem<?>> entities) {
		entities.add(entity);
		final String entityKey = buildEntityKey(entity);
		if (entityKey == null) {
			return;
		}
		for (final CProjectItem<?> child : childrenByParentKey.getOrDefault(entityKey, List.of())) {
			appendEntityAndDescendants(child, childrenByParentKey, entities);
		}
	}

	private boolean hasMatchedAncestor(final String entityKey, final Map<String, String> parentKeyByEntityKey, final Set<String> directMatchKeys) {
		String parentKey = parentKeyByEntityKey.get(entityKey);
		while (parentKey != null) {
			if (directMatchKeys.contains(parentKey)) {
				return true;
			}
			parentKey = parentKeyByEntityKey.get(parentKey);
		}
		return false;
	}

	private CVisibleHierarchyNode buildVisibleNode(final CProjectItem<?> entity, final int hierarchyLevel,
			final Map<String, List<CProjectItem<?>>> childrenByParentKey, final Set<String> visited, final AtomicLong uniqueIdSequence,
			final CGnntBoardFilterCriteria filterCriteria) {
		final String entityKey = buildEntityKey(entity);
		if (entityKey == null) {
			return null;
		}
		if (!visited.add(entityKey)) {
			LOGGER.warn("Skipping already visited Gnnt hierarchy item {}", entityKey);
			return null;
		}
		final List<CProjectItem<?>> children = new ArrayList<>(childrenByParentKey.getOrDefault(entityKey, List.of()));
		children.sort(HIERARCHY_ITEM_COMPARATOR);
		final List<CVisibleHierarchyNode> visibleChildren = new ArrayList<>();
		for (final CProjectItem<?> child : children) {
			final CVisibleHierarchyNode visibleChild =
					buildVisibleNode(child, hierarchyLevel + 1, childrenByParentKey, visited, uniqueIdSequence, filterCriteria);
			if (visibleChild != null) {
				visibleChildren.add(visibleChild);
			}
		}
		if (!matchesFilters(entity, filterCriteria) && visibleChildren.isEmpty()) {
			return null;
		}
		return new CVisibleHierarchyNode(new CGnntItem(entity, uniqueIdSequence.getAndIncrement(), hierarchyLevel), visibleChildren);
	}

	private CVisibleHierarchyNode buildSubtreeNode(final CProjectItem<?> entity, final int hierarchyLevel,
			final Map<String, List<CProjectItem<?>>> childrenByParentKey, final Set<String> visited, final AtomicLong uniqueIdSequence) {
		final String entityKey = buildEntityKey(entity);
		if (entityKey == null || !visited.add(entityKey)) {
			return null;
		}
		final List<CVisibleHierarchyNode> visibleChildren = new ArrayList<>();
		for (final CProjectItem<?> child : childrenByParentKey.getOrDefault(entityKey, List.of())) {
			final CVisibleHierarchyNode visibleChild = buildSubtreeNode(child, hierarchyLevel + 1, childrenByParentKey, visited, uniqueIdSequence);
			if (visibleChild != null) {
				visibleChildren.add(visibleChild);
			}
		}
		return new CVisibleHierarchyNode(new CGnntItem(entity, uniqueIdSequence.getAndIncrement(), hierarchyLevel), visibleChildren);
	}

	private CGnntHierarchyResult flattenHierarchy(final List<CVisibleHierarchyNode> rootNodes) {
		final List<CGnntItem> flatItems = new ArrayList<>();
		final List<CGnntItem> rootItems = new ArrayList<>();
		final Map<String, List<CGnntItem>> childrenByParentKey = new LinkedHashMap<>();
		for (final CVisibleHierarchyNode rootNode : rootNodes) {
			rootItems.add(rootNode.item);
			flattenNode(rootNode, flatItems, childrenByParentKey);
		}
		return new CGnntHierarchyResult(rootItems, childrenByParentKey, flatItems);
	}

	private void flattenNode(final CVisibleHierarchyNode node, final List<CGnntItem> flatItems,
			final Map<String, List<CGnntItem>> childrenByParentKey) {
		node.item.setHasChildren(!node.children.isEmpty());
		flatItems.add(node.item);
		if (!node.children.isEmpty()) {
			final List<CGnntItem> childItems = node.children.stream().map(child -> child.item).toList();
			childrenByParentKey.put(node.item.getEntityKey(), childItems);
			for (final CVisibleHierarchyNode child : node.children) {
				flattenNode(child, flatItems, childrenByParentKey);
			}
		}
	}

	private static int getHierarchyTypeOrder(final CProjectItem<?> entity) {
		if (entity instanceof CEpic) {
			return 0;
		}
		if (entity instanceof CFeature) {
			return 1;
		}
		if (entity instanceof CUserStory) {
			return 2;
		}
		return 3;
	}

	private static boolean isSupportedGnntSourceEntity(final Class<?> entityClass) {
		return entityClass != null && !Modifier.isAbstract(entityClass.getModifiers()) && CProjectItem.class.isAssignableFrom(entityClass)
				&& IHasParentRelation.class.isAssignableFrom(entityClass);
	}

	public CGnntHierarchyResult buildHierarchy(final CGnntViewEntity gnntViewEntity, final CGnntBoardFilterCriteria filterCriteria) {
		Check.notNull(gnntViewEntity, "Gnnt view entity cannot be null");
		Check.notNull(gnntViewEntity.getProject(), "Gnnt view entity must belong to a project");
		final CProject<?> project = gnntViewEntity.getProject();
		final Map<String, CProjectItem<?>> entitiesByKey = new LinkedHashMap<>();
		for (final String entityKey : CEntityRegistry.getAllRegisteredEntityKeys()) {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entityKey);
			if (!isSupportedGnntSourceEntity(entityClass)) {
				continue;
			}
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			if (serviceClass == null) {
				continue;
			}
			try {
				final Object serviceBean = CSpringContext.getBean(serviceClass);
				if (!(serviceBean instanceof CEntityOfProjectService<?>)) {
					continue;
				}
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) serviceBean;
				for (final Object rawEntity : projectService.listByProject(project)) {
					if (!(rawEntity instanceof CProjectItem<?>)) {
						continue;
					}
					final CProjectItem<?> projectItem = (CProjectItem<?>) rawEntity;
					final String projectItemKey = buildEntityKey(projectItem);
					if (projectItemKey == null) {
						LOGGER.debug("Skipping Gnnt timeline source {} because entity ID is null", entityClass.getSimpleName());
						continue;
					}
					entitiesByKey.put(projectItemKey, projectItem);
				}
			} catch (final Exception e) {
				LOGGER.debug("Skipping Gnnt timeline source {} because it could not be queried: {}", entityClass.getSimpleName(), e.getMessage());
			}
		}
		return buildHierarchyResult(entitiesByKey, filterCriteria);
	}

	public List<CGnntItem> listTimelineItems(final CGnntViewEntity gnntViewEntity) {
		return buildHierarchy(gnntViewEntity, null).getFlatItems();
	}

	private boolean matchesFilters(final CProjectItem<?> entity, final CGnntBoardFilterCriteria filterCriteria) {
		if (entity == null || filterCriteria == null || !filterCriteria.hasAnyFilter()) {
			return true;
		}
		if (filterCriteria.getEntityType() != null && !filterCriteria.getEntityType().equals(ProxyUtils.getUserClass(entity.getClass()))) {
			return false;
		}
		if (filterCriteria.getEpic() != null && !matchesEntity(filterCriteria.getEpic(), CGnntAgileFilterSupport.resolveEpic(entity))) {
			return false;
		}
		if (filterCriteria.getFeature() != null && !matchesEntity(filterCriteria.getFeature(), CGnntAgileFilterSupport.resolveFeature(entity))) {
			return false;
		}
		if (filterCriteria.getUserStory() != null && !matchesEntity(filterCriteria.getUserStory(), CGnntAgileFilterSupport.resolveUserStory(entity))) {
			return false;
		}
		if (filterCriteria.getResponsible() != null
				&& !matchesEntity(filterCriteria.getResponsible(), CGnntAgileFilterSupport.resolveResponsible(entity))) {
			return false;
		}
		if (filterCriteria.getSprint() != null && !matchesEntity(filterCriteria.getSprint(), CGnntAgileFilterSupport.resolveSprint(entity))) {
			return false;
		}
		if (filterCriteria.getSearchText() != null && !filterCriteria.getSearchText().isBlank()) {
			final String searchText = filterCriteria.getSearchText().trim().toLowerCase();
			final String name = entity.getName() != null ? entity.getName().toLowerCase() : "";
			final String description = entity.getDescription() != null ? entity.getDescription().toLowerCase() : "";
			final String responsibleName = entity.getAssignedTo() != null && entity.getAssignedTo().getName() != null
					? entity.getAssignedTo().getName().toLowerCase() : "";
			if (!name.contains(searchText) && !description.contains(searchText) && !responsibleName.contains(searchText)
					&& !ProxyUtils.getUserClass(entity.getClass()).getSimpleName().toLowerCase().contains(searchText)) {
				return false;
			}
		}
		return true;
	}

	private static boolean matchesEntity(final Object expected, final Object actual) {
		if (expected == null) {
			return true;
		}
		if (actual == null) {
			return false;
		}
		if (expected instanceof CProjectItem<?> && actual instanceof CProjectItem<?>) {
			final CProjectItem<?> expectedEntity = (CProjectItem<?>) expected;
			final CProjectItem<?> actualEntity = (CProjectItem<?>) actual;
			return expectedEntity.getId() != null && expectedEntity.getId().equals(actualEntity.getId());
		}
		return expected.equals(actual);
	}

	public CGanttTimelineRange resolveRange(final List<CGnntItem> items) {
		if (items == null || items.isEmpty()) {
			final LocalDate today = LocalDate.now();
			return new CGanttTimelineRange(today.minusDays(7), today.plusDays(21));
		}
		LocalDate start = null;
		LocalDate end = null;
		for (final CGnntItem item : items) {
			if (!item.hasDates()) {
				continue;
			}
			if (start == null || item.getStartDate().isBefore(start)) {
				start = item.getStartDate();
			}
			if (end == null || item.getEndDate().isAfter(end)) {
				end = item.getEndDate();
			}
		}
		if (start == null || end == null) {
			final LocalDate today = LocalDate.now();
			return new CGanttTimelineRange(today.minusDays(7), today.plusDays(21));
		}
		return new CGanttTimelineRange(start.minusDays(3), end.plusDays(3));
	}
}
