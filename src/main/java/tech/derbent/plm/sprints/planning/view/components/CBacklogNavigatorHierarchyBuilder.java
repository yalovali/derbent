package tech.derbent.plm.sprints.planning.view.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;

/** Shared builder for the Sprint Planning backlog browser and other boards (e.g. Kanban).
 * <p>
 * Produces two synchronized views:
 * <ul>
 * <li>Parent hierarchy: only non-leaf nodes (levels 0..n).</li>
 * <li>Leaf hierarchy: a flat list of sprint-assignable leaf items (level -1).</li>
 * </ul>
 * </p>
 */
public final class CBacklogNavigatorHierarchyBuilder {

	private static final class CVisibleNode {
		private final List<CVisibleNode> children;
		private final CGnntItem item;

		private CVisibleNode(final CGnntItem item, final List<CVisibleNode> children) {
			this.item = item;
			this.children = children;
		}
	}

	private record CBacklogBuildResult(CVisibleNode node, boolean hasVisibleLeafDescendant) {}

	public record CBacklogData(CGnntHierarchyResult parentHierarchy, CGnntHierarchyResult leafHierarchy) {}

	private CBacklogNavigatorHierarchyBuilder() {
		// Utility.
	}

	public static CBacklogData buildBacklogData(final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey,
			final ESprintPlanningScope scope,
			final Predicate<CProjectItem<?, ?>> includeParent,
			final Predicate<CProjectItem<?, ?>> includeLeaf,
			final ToIntFunction<Object> orderResolver) {

		if (hierarchyItemsByKey == null || hierarchyItemsByKey.isEmpty() || scope == null) {
			final CGnntHierarchyResult empty = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
			return new CBacklogData(empty, empty);
		}

		final Map<String, List<CProjectItem<?, ?>>> childrenByParentKey = new HashMap<>();
		final List<CProjectItem<?, ?>> roots = new ArrayList<>();
		for (final CProjectItem<?, ?> item : hierarchyItemsByKey.values()) {
			final String key = CHierarchyNavigationService.buildEntityKey(item);
			if (key == null) {
				continue;
			}
			final String parentKey = CHierarchyNavigationService.buildParentKey(item);
			if (parentKey == null || !hierarchyItemsByKey.containsKey(parentKey)) {
				roots.add(item);
			} else {
				childrenByParentKey.computeIfAbsent(parentKey, ignored -> new ArrayList<>()).add(item);
			}
		}

		roots.sort(Comparator.comparingInt((final CProjectItem<?, ?> item) -> orderResolver.applyAsInt(item))
				.thenComparing(CProjectItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		for (final List<CProjectItem<?, ?>> children : childrenByParentKey.values()) {
			children.sort(Comparator.comparingInt((final CProjectItem<?, ?> item) -> orderResolver.applyAsInt(item))
					.thenComparing(CProjectItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		}

		final List<CVisibleNode> visibleParentRoots = new ArrayList<>();
		final List<CGnntItem> visibleLeafItems = new ArrayList<>();
		final long[] uniqueId = new long[] { 1 };
		for (final CProjectItem<?, ?> root : roots) {
			final CBacklogBuildResult result = buildVisibleParentNode(root, 0, scope, childrenByParentKey, includeParent,
					includeLeaf, visibleLeafItems, uniqueId);
			if (result.node() != null) {
				visibleParentRoots.add(result.node());
			}
		}

		visibleLeafItems.sort(Comparator.<CGnntItem>comparingInt(item -> orderResolver.applyAsInt(item.getEntity()))
				.thenComparing(CGnntItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

		final CGnntHierarchyResult parentHierarchy = flattenVisibleNodes(visibleParentRoots);
		final CGnntHierarchyResult leafHierarchy = new CGnntHierarchyResult(visibleLeafItems, Map.of(), visibleLeafItems);
		return new CBacklogData(parentHierarchy, leafHierarchy);
	}

	private static CBacklogBuildResult buildVisibleParentNode(final CProjectItem<?, ?> entity,
			final int hierarchyLevel,
			final ESprintPlanningScope scope,
			final Map<String, List<CProjectItem<?, ?>>> childrenByParentKey,
			final Predicate<CProjectItem<?, ?>> includeParent,
			final Predicate<CProjectItem<?, ?>> includeLeaf,
			final List<CGnntItem> visibleLeafItems,
			final long[] uniqueId) {

		final String entityKey = CHierarchyNavigationService.buildEntityKey(entity);
		if (entityKey == null) {
			return new CBacklogBuildResult(null, false);
		}

		if (isLeafItem(entity)) {
			final boolean leafVisible = entity instanceof ISprintableItem
					&& isBacklogCandidate(entity, scope)
					&& includeLeaf != null
					&& includeLeaf.test(entity);
			if (leafVisible) {
				visibleLeafItems.add(new CGnntItem(entity, uniqueId[0]++, 0));
			}
			return new CBacklogBuildResult(null, leafVisible);
		}

		final List<CVisibleNode> visibleChildren = new ArrayList<>();
		boolean hasVisibleLeaf = false;
		for (final CProjectItem<?, ?> child : childrenByParentKey.getOrDefault(entityKey, List.of())) {
			final CBacklogBuildResult visibleChild = buildVisibleParentNode(child, hierarchyLevel + 1, scope,
					childrenByParentKey, includeParent, includeLeaf, visibleLeafItems, uniqueId);
			if (visibleChild.node() != null) {
				visibleChildren.add(visibleChild.node());
			}
			hasVisibleLeaf = hasVisibleLeaf || visibleChild.hasVisibleLeafDescendant();
		}

		final boolean matchesFilters = isBacklogCandidate(entity, scope)
				&& includeParent != null
				&& includeParent.test(entity);
		if (!matchesFilters && visibleChildren.isEmpty() && !hasVisibleLeaf) {
			return new CBacklogBuildResult(null, false);
		}

		final CGnntItem item = new CGnntItem(entity, uniqueId[0]++, hierarchyLevel);
		item.setHasChildren(!visibleChildren.isEmpty());
		return new CBacklogBuildResult(new CVisibleNode(item, visibleChildren), hasVisibleLeaf);
	}

	private static void flattenVisibleNode(final CVisibleNode node,
			final List<CGnntItem> flatItems,
			final Map<String, List<CGnntItem>> childrenByParentKey) {

		flatItems.add(node.item);
		if (node.children == null || node.children.isEmpty()) {
			return;
		}
		final String key = node.item.getEntityKey();
		final List<CGnntItem> childItems = new ArrayList<>();
		node.children.forEach((final CVisibleNode child) -> {
			childItems.add(child.item);
			flattenVisibleNode(child, flatItems, childrenByParentKey);
		});
		childrenByParentKey.put(key, childItems);
	}

	private static CGnntHierarchyResult flattenVisibleNodes(final List<CVisibleNode> rootNodes) {
		final List<CGnntItem> flatItems = new ArrayList<>();
		final List<CGnntItem> rootItems = new ArrayList<>();
		final Map<String, List<CGnntItem>> childrenByParentKey = new HashMap<>();
		rootNodes.forEach((final CVisibleNode rootNode) -> {
			rootItems.add(rootNode.item);
			flattenVisibleNode(rootNode, flatItems, childrenByParentKey);
		});
		return new CGnntHierarchyResult(rootItems, childrenByParentKey, flatItems);
	}

	private static boolean isLeafItem(final CProjectItem<?, ?> entity) {
		return entity != null && CHierarchyNavigationService.getEntityLevel(entity) == -1;
	}

	private static boolean isBacklogCandidate(final CProjectItem<?, ?> entity, final ESprintPlanningScope scope) {
		if (scope == ESprintPlanningScope.ALL_ITEMS) {
			return true;
		}
		if (!(entity instanceof final ISprintableItem sprintableItem)) {
			return true;
		}
		final CSprint sprint = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		return sprint == null;
	}
}
