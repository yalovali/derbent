package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.List;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;

/**
 * Legacy toolbar helper names kept for Gnnt filtering, but the implementation now resolves anchors by
 * hierarchy level instead of agile class chains.
 */
public final class CAgileToolbarSupport {

	private CAgileToolbarSupport() {}

	public static <T extends CProjectItem<?, ?>> List<T> filterByAncestorLevel(final List<T> items, final int level,
			final CProjectItem<?, ?> expectedAncestor) {
		if (items == null || items.isEmpty()) {
			return List.of();
		}
		if (expectedAncestor == null) {
			return items;
		}
		final List<T> filteredItems = new ArrayList<>();
		for (final T item : items) {
			if (item != null && CHierarchyNavigationService.isSameEntity(
					CHierarchyNavigationService.resolveAncestorAtLevel(item, level), expectedAncestor)) {
				filteredItems.add(item);
			}
		}
		return filteredItems;
	}

	public static boolean isSameEntity(final Object left, final Object right) {
		return CHierarchyNavigationService.isSameEntity(left, right);
	}

	public static List<CFeature> filterFeaturesByEpic(final List<CFeature> features, final CEpic epic) {
		return filterByAncestorLevel(features, 0, epic);
	}

	public static List<CUserStory> filterUserStories(final List<CUserStory> userStories, final CEpic epic, final CFeature feature) {
		if (feature != null) {
			return filterByAncestorLevel(userStories, 1, feature);
		}
		return filterByAncestorLevel(userStories, 0, epic);
	}

	public static CEpic resolveEpic(final Object entity) {
		final CProjectItem<?, ?> ancestor = CHierarchyNavigationService.resolveAncestorAtLevel(entity, 0);
		return ancestor instanceof CEpic epic ? epic : null;
	}

	public static CFeature resolveFeature(final Object entity) {
		final CProjectItem<?, ?> ancestor = CHierarchyNavigationService.resolveAncestorAtLevel(entity, 1);
		return ancestor instanceof CFeature feature ? feature : null;
	}

	public static CUserStory resolveUserStory(final Object entity) {
		final CProjectItem<?, ?> ancestor = CHierarchyNavigationService.resolveAncestorAtLevel(entity, 2);
		return ancestor instanceof CUserStory userStory ? userStory : null;
	}
}
