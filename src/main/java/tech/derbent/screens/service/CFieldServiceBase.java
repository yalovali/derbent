package tech.derbent.screens.service;

import java.util.List;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.users.domain.CUser;

public class CFieldServiceBase {

	/**
	 * Get available entity types for screen configuration.
	 * @return list of entity types
	 */
	public List<String> getAvailableEntityTypes() {
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
	}

	protected Class<?> getEntityClass(final String entityType) {

		try {

			switch (entityType) {
			case "CActivity":
				return CActivity.class;
			case "CMeeting":
				return CMeeting.class;
			case "CRisk":
				return CRisk.class;
			case "CProject":
				return CProject.class;
			case "CUser":
				return CUser.class;
			default:
				return null;
			}
		} catch (final Exception e) {
			return null;
		}
	}
}
