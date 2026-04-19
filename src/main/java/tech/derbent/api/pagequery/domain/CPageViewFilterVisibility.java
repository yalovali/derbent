package tech.derbent.api.pagequery.domain;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.interfaces.IHasEpicParent;
import tech.derbent.api.interfaces.IHasFeatureParent;
import tech.derbent.api.interfaces.IHasUserStoryParent;
import tech.derbent.api.interfaces.ISprintableItem;

/** CPageViewFilterVisibility - Design-time filter selection for page master toolbars.
 * <p>
 * Views can pass an explicit instance to control which filters are visible; defaults can be derived
 * from the entity class.
 * </p>
 */
public final class CPageViewFilterVisibility {

	public static CPageViewFilterVisibility autoFor(final Class<?> entityClass) {
		final boolean isProjectItem = entityClass != null && CProjectItem.class.isAssignableFrom(entityClass);
		final boolean isAgileHierarchical = entityClass != null && IHasAgileParentRelation.class.isAssignableFrom(entityClass);
		final boolean isSprintable = entityClass != null && ISprintableItem.class.isAssignableFrom(entityClass);

		final String simpleName = entityClass != null ? entityClass.getSimpleName() : "";

		// Hierarchy-aware visibility:
		// - A parent cannot filter its children (Epic grid must not show Feature/UserStory filters, etc.)
		// - A child can filter by its ancestors (Activity can filter by UserStory/Feature/Epic).
		final boolean hasEpicParent = entityClass != null && IHasEpicParent.class.isAssignableFrom(entityClass);
		final boolean hasFeatureParent = entityClass != null && IHasFeatureParent.class.isAssignableFrom(entityClass);
		final boolean hasUserStoryParent = entityClass != null && IHasUserStoryParent.class.isAssignableFrom(entityClass);

		final boolean showEpic = isAgileHierarchical && (hasEpicParent || hasFeatureParent || hasUserStoryParent);
		final boolean showFeature = isAgileHierarchical && (hasFeatureParent || hasUserStoryParent) && !"CFeature".equals(simpleName);
		final boolean showUserStory = isAgileHierarchical && hasUserStoryParent;

		// 'Responsible' is a ProjectItem concern (assignedTo). Sprint is driven by ISprintableItem.
		return new CPageViewFilterVisibility(showEpic, showFeature, showUserStory, isProjectItem, isSprintable);
	}

	private final boolean showEpic;
	private final boolean showFeature;
	private final boolean showUserStory;
	private final boolean showResponsible;
	private final boolean showSprint;

	public CPageViewFilterVisibility(final boolean showEpic, final boolean showFeature, final boolean showUserStory,
			final boolean showResponsible, final boolean showSprint) {
		this.showEpic = showEpic;
		this.showFeature = showFeature;
		this.showUserStory = showUserStory;
		this.showResponsible = showResponsible;
		this.showSprint = showSprint;
	}

	public boolean isShowEpic() { return showEpic; }

	public boolean isShowFeature() { return showFeature; }

	public boolean isShowResponsible() { return showResponsible; }

	public boolean isShowSprint() { return showSprint; }

	public boolean isShowUserStory() { return showUserStory; }

	public boolean isAnyAgileFilterVisible() {
		return showEpic || showFeature || showUserStory || showResponsible || showSprint;
	}
}
