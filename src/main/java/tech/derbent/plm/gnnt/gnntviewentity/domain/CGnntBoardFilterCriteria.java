package tech.derbent.plm.gnnt.gnntviewentity.domain;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.sprints.domain.CSprint;

/**
 * Filter state for the Gnnt board.
 *
 * <p>The legacy field names epic/feature/userStory are preserved so existing query keys keep working,
 * but they now represent generic hierarchy anchors at levels 0, 1, and 2.</p>
 */
public class CGnntBoardFilterCriteria {

	public static final String DEFAULT_COLOR = "#607D8B";
	public static final String DEFAULT_ICON = "vaadin:filter";
	public static final String ENTITY_TITLE_PLURAL = "Gnnt Board Filters";
	public static final String ENTITY_TITLE_SINGULAR = "Gnnt Board Filter";
	public static final String VIEW_NAME = "Gnnt Board Filter Criteria";

	private CProjectItem<?, ?> epic;
	private Class<?> entityType;
	private CProjectItem<?, ?> feature;
	private CUser responsible;
	private String searchText;
	private CSprint sprint;
	private CProjectItem<?, ?> userStory;

	public CProjectItem<?, ?> getEpic() { return epic; }

	public Class<?> getEntityType() { return entityType; }

	public CProjectItem<?, ?> getFeature() { return feature; }

	public CUser getResponsible() { return responsible; }

	public String getSearchText() { return searchText; }

	public CSprint getSprint() { return sprint; }

	public CProjectItem<?, ?> getUserStory() { return userStory; }

	public boolean hasAnyFilter() {
		return epic != null || feature != null || userStory != null || responsible != null || sprint != null || entityType != null
				|| (searchText != null && !searchText.isBlank());
	}

	public void setEpic(final CProjectItem<?, ?> epic) { this.epic = epic; }

	public void setEntityType(final Class<?> entityType) { this.entityType = entityType; }

	public void setFeature(final CProjectItem<?, ?> feature) { this.feature = feature; }

	public void setResponsible(final CUser responsible) { this.responsible = responsible; }

	public void setSearchText(final String searchText) { this.searchText = searchText; }

	public void setSprint(final CSprint sprint) { this.sprint = sprint; }

	public void setUserStory(final CProjectItem<?, ?> userStory) { this.userStory = userStory; }
}
