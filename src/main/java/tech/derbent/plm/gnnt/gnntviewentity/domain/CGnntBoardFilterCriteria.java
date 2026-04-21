package tech.derbent.plm.gnnt.gnntviewentity.domain;

import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.sprints.domain.CSprint;

public class CGnntBoardFilterCriteria {

	private CEpic epic;
	private CFeature feature;
	private CUserStory userStory;
	private CUser responsible;
	private CSprint sprint;
	private String searchText;
	private Class<?> entityType;

	public CEpic getEpic() {
		return epic;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public CFeature getFeature() {
		return feature;
	}

	public CUser getResponsible() {
		return responsible;
	}

	public String getSearchText() {
		return searchText;
	}

	public CSprint getSprint() {
		return sprint;
	}

	public CUserStory getUserStory() {
		return userStory;
	}

	public boolean hasAnyFilter() {
		return epic != null || feature != null || userStory != null || responsible != null || sprint != null || entityType != null
				|| (searchText != null && !searchText.isBlank());
	}

	public void setEpic(final CEpic epic) {
		this.epic = epic;
	}

	public void setEntityType(final Class<?> entityType) {
		this.entityType = entityType;
	}

	public void setFeature(final CFeature feature) {
		this.feature = feature;
	}

	public void setResponsible(final CUser responsible) {
		this.responsible = responsible;
	}

	public void setSearchText(final String searchText) {
		this.searchText = searchText;
	}

	public void setSprint(final CSprint sprint) {
		this.sprint = sprint;
	}

	public void setUserStory(final CUserStory userStory) {
		this.userStory = userStory;
	}
}
