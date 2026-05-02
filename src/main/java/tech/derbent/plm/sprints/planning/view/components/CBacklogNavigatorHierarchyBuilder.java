package tech.derbent.plm.sprints.planning.view.components;

import java.time.LocalDate;
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
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
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

	/** Computes backlog-level metrics (done/total items and story points) from a list of leaf items.
	 * @param leafItems Flat list of leaf CGnntItems already filtered for display
	 * @return Metrics record with done/total counts and story points */
	public static CSprintPlanningSprintMetrics computeBacklogMetrics(final List<CGnntItem> leafItems) {
		if (leafItems == null || leafItems.isEmpty()) {
			return new CSprintPlanningSprintMetrics(0, 0, 0, 0);
		}
		int total = 0;
		int done = 0;
		long spTotal = 0;
		long spDone = 0;
		for (final CGnntItem gnntItem : leafItems) {
			final CProjectItem<?, ?> entity = gnntItem != null ? gnntItem.getEntity() : null;
			if (entity == null) {
				continue;
			}
			total++;
			final boolean isDone = entity.getStatus() != null
					&& Boolean.TRUE.equals(entity.getStatus().getFinalStatus());
			if (isDone) {
				done++;
			}
			if (entity instanceof final ISprintableItem si && si.getSprintItem() != null
					&& si.getSprintItem().getStoryPoint() != null) {
				final long points = si.getSprintItem().getStoryPoint();
				spTotal += points;
				if (isDone) {
					spDone += points;
				}
			}
		}
		return new CSprintPlanningSprintMetrics(done, total, spDone, spTotal);
	}

	/** Computes per-parent rollup metrics by walking up the hierarchy from each leaf item.
	 * @param leafItems         Flat list of visible leaf CGnntItems
	 * @param hierarchyItemsByKey Full hierarchy map keyed by entity key
	 * @return Map of parent entity key → rollup metrics */
	public static Map<String, CSprintPlanningSprintMetrics> computeParentRollups(final List<CGnntItem> leafItems,
			final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey) {
		if (leafItems == null || leafItems.isEmpty() || hierarchyItemsByKey == null || hierarchyItemsByKey.isEmpty()) {
			return Map.of();
		}
		final Map<String, CSprintPlanningSprintMetrics> rollups = new HashMap<>();
		for (final CGnntItem gnntItem : leafItems) {
			final CProjectItem<?, ?> leaf = gnntItem != null ? gnntItem.getEntity() : null;
			if (leaf == null) {
				continue;
			}
			final boolean isDone = leaf.getStatus() != null && Boolean.TRUE.equals(leaf.getStatus().getFinalStatus());
			long points = 0;
			if (leaf instanceof final ISprintableItem si && si.getSprintItem() != null
					&& si.getSprintItem().getStoryPoint() != null) {
				points = si.getSprintItem().getStoryPoint();
			}
			String parentKey = CHierarchyNavigationService.buildParentKey(leaf);
			while (parentKey != null) {
				final CProjectItem<?, ?> parent = hierarchyItemsByKey.get(parentKey);
				if (parent == null) {
					break;
				}
				final CSprintPlanningSprintMetrics cur =
						rollups.getOrDefault(parentKey, new CSprintPlanningSprintMetrics(0, 0, 0, 0));
				rollups.put(parentKey, new CSprintPlanningSprintMetrics(cur.itemDoneCount() + (isDone ? 1 : 0),
						cur.itemTotalCount() + 1, cur.storyPointsDone() + (isDone ? points : 0),
						cur.storyPointsTotal() + points));
				parentKey = CHierarchyNavigationService.buildParentKey(parent);
			}
		}
		return rollups.isEmpty() ? Map.of() : Map.copyOf(rollups);
	}

	/** Computes per-parent rollup metrics from the FULL item hierarchy (backlog + sprint-assigned).
	 * <p>Use this for kanban/backlog views that need to show total/done/active counts across all leaf items,
	 * not just the items visible in the backlog panel.</p>
	 * @param allItemsByKey Full hierarchy map keyed by entity key (all project items)
	 * @return Map of parent entity key → rollup metrics with inSprintCount populated */
	public static Map<String, CSprintPlanningSprintMetrics> computeParentRollupsAll(
			final Map<String, CProjectItem<?, ?>> allItemsByKey) {
		if (allItemsByKey == null || allItemsByKey.isEmpty()) {
			return Map.of();
		}
		final Map<String, CSprintPlanningSprintMetrics> rollups = new HashMap<>();
		for (final CProjectItem<?, ?> leaf : allItemsByKey.values()) {
			if (CHierarchyNavigationService.getEntityLevel(leaf) != -1) {
				continue;
			}
			final boolean isDone = leaf.getStatus() != null && Boolean.TRUE.equals(leaf.getStatus().getFinalStatus());
			final boolean isInSprint = leaf instanceof final ISprintableItem si && si.getSprintItem() != null
					&& si.getSprintItem().getSprint() != null;
			long points = 0;
			if (leaf instanceof final ISprintableItem si && si.getSprintItem() != null
					&& si.getSprintItem().getStoryPoint() != null) {
				points = si.getSprintItem().getStoryPoint();
			}
			String parentKey = CHierarchyNavigationService.buildParentKey(leaf);
			while (parentKey != null) {
				final CProjectItem<?, ?> parent = allItemsByKey.get(parentKey);
				if (parent == null) {
					break;
				}
				final CSprintPlanningSprintMetrics cur =
						rollups.getOrDefault(parentKey, new CSprintPlanningSprintMetrics(0, 0, 0, 0));
				rollups.put(parentKey,
						new CSprintPlanningSprintMetrics(cur.itemDoneCount() + (isDone ? 1 : 0),
								cur.itemTotalCount() + 1, cur.storyPointsDone() + (isDone ? points : 0),
								cur.storyPointsTotal() + points, cur.inSprintCount() + (isInSprint ? 1 : 0)));
				parentKey = CHierarchyNavigationService.buildParentKey(parent);
			}
		}
		return rollups.isEmpty() ? Map.of() : Map.copyOf(rollups);
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

	/** Computes a timeline range that spans all provided item date ranges.
	 * Falls back to today ±30 days when no items have dates.
	 * @param itemGroups One or more item lists to consider (varargs for backlog + sprint items)
	 * @return A timeline range wide enough to show all items */
	@SafeVarargs
	public static CGanttTimelineRange resolveTimelineRange(final List<CGnntItem>... itemGroups) {
		LocalDate min = null;
		LocalDate max = null;
		for (final List<CGnntItem> items : itemGroups) {
			if (items == null) {
				continue;
			}
			for (final CGnntItem item : items) {
				final LocalDate start = item.getStartDate();
				final LocalDate end = item.getEndDate();
				if (start != null && (min == null || start.isBefore(min))) {
					min = start;
				}
				if (end != null && (max == null || end.isAfter(max))) {
					max = end;
				}
			}
		}
		if (min == null) {
			min = LocalDate.now().minusDays(30);
		}
		if (max == null || !max.isAfter(min)) {
			max = min.plusDays(60);
		}
		return new CGanttTimelineRange(min, max);
	}
}
