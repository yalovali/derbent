package tech.derbent.plm.gnnt.gnntviewentity.service;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

/**
 * Gnnt filter helper retained for compatibility with existing callers.
 *
 * <p>The returned anchors are now discovered from hierarchy levels 0/1/2, which lets agile and
 * requirement trees share the same filtering logic.</p>
 */
public final class CGnntAgileFilterSupport {

	private CGnntAgileFilterSupport() {}

	public static CProjectItem<?, ?> resolveEpic(final CProjectItem<?, ?> entity) {
		return CHierarchyNavigationService.resolveAncestorAtLevel(entity, 0);
	}

	public static CProjectItem<?, ?> resolveFeature(final CProjectItem<?, ?> entity) {
		return CHierarchyNavigationService.resolveAncestorAtLevel(entity, 1);
	}

	public static CUser resolveResponsible(final CProjectItem<?, ?> entity) {
		if (entity instanceof ISprintableItem sprintableItem) {
			return sprintableItem.getAssignedTo();
		}
		return entity != null ? entity.getAssignedTo() : null;
	}

	public static CSprint resolveSprint(final CProjectItem<?, ?> entity) {
		if (!(entity instanceof ISprintableItem sprintableItem)) {
			return null;
		}
		final CSprintItem sprintItem = sprintableItem.getSprintItem();
		return sprintItem != null ? sprintItem.getSprint() : null;
	}

	public static CProjectItem<?, ?> resolveUserStory(final CProjectItem<?, ?> entity) {
		return CHierarchyNavigationService.resolveAncestorAtLevel(entity, 2);
	}
}
