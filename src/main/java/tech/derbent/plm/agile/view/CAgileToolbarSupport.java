package tech.derbent.plm.agile.view;

import java.util.ArrayList;
import java.util.List;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IHasEpicParent;
import tech.derbent.api.interfaces.IHasFeatureParent;
import tech.derbent.api.interfaces.IHasUserStoryParent;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;

public final class CAgileToolbarSupport {

	private CAgileToolbarSupport() {}

	public static List<CFeature> filterFeaturesByEpic(final List<CFeature> allFeatures, final CEpic epic) {
		if (allFeatures == null || allFeatures.isEmpty() || epic == null) {
			return allFeatures != null ? allFeatures : List.of();
		}
		final List<CFeature> filteredFeatures = new ArrayList<>();
		for (final CFeature feature : allFeatures) {
			if (feature != null && isSameEntity(feature.getParentEpic(), epic)) {
				filteredFeatures.add(feature);
			}
		}
		return filteredFeatures;
	}

	public static List<CUserStory> filterUserStories(final List<CUserStory> allUserStories, final CEpic epic, final CFeature feature) {
		if (allUserStories == null || allUserStories.isEmpty()) {
			return List.of();
		}
		if (feature != null) {
			final List<CUserStory> filteredUserStories = new ArrayList<>();
			for (final CUserStory userStory : allUserStories) {
				if (userStory != null && isSameEntity(userStory.getParentFeature(), feature)) {
					filteredUserStories.add(userStory);
				}
			}
			return filteredUserStories;
		}
		if (epic == null) {
			return allUserStories;
		}
		final List<CUserStory> filteredUserStories = new ArrayList<>();
		for (final CUserStory userStory : allUserStories) {
			final CEpic resolvedEpic = resolveEpic(userStory);
			if (resolvedEpic != null && isSameEntity(resolvedEpic, epic)) {
				filteredUserStories.add(userStory);
			}
		}
		return filteredUserStories;
	}

	public static boolean isSameEntity(final Object left, final Object right) {
		if (left == null || right == null) {
			return false;
		}
		if (left instanceof CEntityDB<?> && right instanceof CEntityDB<?>) {
			final CEntityDB<?> leftEntity = (CEntityDB<?>) left;
			final CEntityDB<?> rightEntity = (CEntityDB<?>) right;
			return leftEntity.getId() != null && leftEntity.getId().equals(rightEntity.getId());
		}
		return left.equals(right);
	}

	public static CEpic resolveEpic(final CUserStory userStory) {
		final CFeature feature = resolveFeature(userStory);
		return feature != null ? feature.getParentEpic() : null;
	}

	public static CFeature resolveFeature(final Object entity) {
		if (entity instanceof final IHasFeatureParent featureParent) {
			return featureParent.getParentFeature();
		}
		final CUserStory userStory = resolveUserStory(entity);
		return userStory != null ? userStory.getParentFeature() : null;
	}

	public static CUserStory resolveUserStory(final Object entity) {
		if (entity instanceof final IHasUserStoryParent userStoryParent) {
			return userStoryParent.getParentUserStory();
		}
		return null;
	}

	public static CEpic resolveEpic(final Object entity) {
		if (entity instanceof final IHasEpicParent epicParent) {
			return epicParent.getParentEpic();
		}
		final CFeature feature = resolveFeature(entity);
		return feature != null ? feature.getParentEpic() : null;
	}
}
