package tech.derbent.plm.gnnt.gnntviewentity.service;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasParentRelation;
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
		if (entity instanceof CEpic) {
			return (CEpic) entity;
		}
		if (entity instanceof IHasParentRelation) {
			final CProjectItem<?> parent = ((IHasParentRelation) entity).getParentItem();
			if (parent instanceof CEpic) {
				return (CEpic) parent;
			}
		}
		final CFeature feature = resolveFeature(entity);
		return feature != null ? resolveEpic(feature) : null;
	}

	public static CFeature resolveFeature(final CProjectItem<?> entity) {
		if (entity instanceof CFeature) {
			return (CFeature) entity;
		}
		if (entity instanceof IHasParentRelation) {
			final CProjectItem<?> parent = ((IHasParentRelation) entity).getParentItem();
			if (parent instanceof CFeature) {
				return (CFeature) parent;
			}
		}
		final CUserStory userStory = resolveUserStory(entity);
		return userStory != null ? resolveFeature(userStory) : null;
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
		if (entity instanceof CUserStory) {
			return (CUserStory) entity;
		}
		if (entity instanceof IHasParentRelation) {
			final CProjectItem<?> parent = ((IHasParentRelation) entity).getParentItem();
			if (parent instanceof CUserStory) {
				return (CUserStory) parent;
			}
		}
		return null;
	}
}
