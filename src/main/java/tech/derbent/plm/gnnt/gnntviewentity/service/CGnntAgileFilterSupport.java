package tech.derbent.plm.gnnt.gnntviewentity.service;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasEpicParent;
import tech.derbent.api.interfaces.IHasFeatureParent;
import tech.derbent.api.interfaces.IHasUserStoryParent;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

public final class CGnntAgileFilterSupport {

	private CGnntAgileFilterSupport() {}

	public static CEpic resolveEpic(final CProjectItem<?> entity) {
		if (entity instanceof IHasEpicParent) {
			return ((IHasEpicParent) entity).getParentEpic();
		}
		final CFeature feature = resolveFeature(entity);
		return feature != null ? feature.getParentEpic() : null;
	}

	public static CFeature resolveFeature(final CProjectItem<?> entity) {
		if (entity instanceof IHasFeatureParent) {
			return ((IHasFeatureParent) entity).getParentFeature();
		}
		final CUserStory userStory = resolveUserStory(entity);
		return userStory != null ? userStory.getParentFeature() : null;
	}

	public static CUser resolveResponsible(final CProjectItem<?> entity) {
		if (entity instanceof ISprintableItem) {
			return ((ISprintableItem) entity).getAssignedTo();
		}
		return entity != null ? entity.getAssignedTo() : null;
	}

	public static CSprint resolveSprint(final CProjectItem<?> entity) {
		if (!(entity instanceof ISprintableItem)) {
			return null;
		}
		final CSprintItem sprintItem = ((ISprintableItem) entity).getSprintItem();
		return sprintItem != null ? sprintItem.getSprint() : null;
	}

	public static CUserStory resolveUserStory(final CProjectItem<?> entity) {
		if (entity instanceof IHasUserStoryParent) {
			return ((IHasUserStoryParent) entity).getParentUserStory();
		}
		return null;
	}
}
