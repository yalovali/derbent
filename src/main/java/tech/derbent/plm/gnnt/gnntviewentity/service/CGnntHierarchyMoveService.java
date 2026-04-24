package tech.derbent.plm.gnnt.gnntviewentity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;

/**
 * Shared Gnnt hierarchy move service.
 *
 * <p>The Gnnt board only supports drop-on-parent reparenting for now, so this service keeps the move
 * logic aligned with the same generic hierarchy validation used everywhere else.</p>
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CGnntHierarchyMoveService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntHierarchyMoveService.class);

	private final CHierarchyNavigationService hierarchyNavigationService;
	private final CParentRelationService parentRelationService;

	public CGnntHierarchyMoveService(final CHierarchyNavigationService hierarchyNavigationService,
			final CParentRelationService parentRelationService) {
		this.hierarchyNavigationService = hierarchyNavigationService;
		this.parentRelationService = parentRelationService;
	}

	@Transactional
	public CProjectItem<?> reparentItem(final CProjectItem<?> child, final CProjectItem<?> dropTarget) {
		Check.notNull(child, "Dragged Gnnt item cannot be null");
		Check.notNull(dropTarget, "Drop target Gnnt item cannot be null");
		Check.notNull(child.getId(), "Dragged Gnnt item must be persisted");
		Check.notNull(dropTarget.getId(), "Drop target Gnnt item must be persisted");
		Check.isTrue(!CHierarchyNavigationService.isSameEntity(child, dropTarget), "An item cannot be dropped onto itself");

		final CProjectItem<?> effectiveParent = resolveEffectiveParent(child, dropTarget);
		validateMove(child, dropTarget, effectiveParent);

		parentRelationService.setParent(child, effectiveParent);
		saveEntity(child);
		LOGGER.info("Reparented Gnnt item '{}' under '{}'", child.getName(), effectiveParent != null ? effectiveParent.getName() : "root");
		return effectiveParent;
	}

	@SuppressWarnings ("unchecked")
	private <T extends CProjectItem<T>> void saveEntity(final CProjectItem<?> entity) {
		// Registry lookup returns an untyped bean, so we narrow via cast after runtime validation.
		final CAbstractService<T> service = (CAbstractService<T>) resolveService(entity);
		service.save((T) entity);
	}

	private CProjectItem<?> resolveEffectiveParent(final CProjectItem<?> child, final CProjectItem<?> dropTarget) {
		final int childLevel = CHierarchyNavigationService.getEntityLevel(child);
		final int targetLevel = CHierarchyNavigationService.getEntityLevel(dropTarget);
		// UX rule: if the user drops on a same-level item, treat it as "move as sibling" (use the target's parent).
		if (childLevel == targetLevel && dropTarget instanceof IHasParentRelation hasParentRelation) {
			return hasParentRelation.getParentItem();
		}
		return dropTarget;
	}

	private void validateMove(final CProjectItem<?> child, final CProjectItem<?> dropTarget, final CProjectItem<?> effectiveParent) {
		if (effectiveParent == null) {
			final int childLevel = CHierarchyNavigationService.getEntityLevel(child);
			Check.isTrue(childLevel == 0 || childLevel == -1,
					"'%s' must have a parent for its hierarchy level (level=%d).".formatted(child.getName(), childLevel));
			return;
		}

		if (!hierarchyNavigationService.isValidParentCandidate(child, effectiveParent)) {
			final int childLevel = CHierarchyNavigationService.getEntityLevel(child);
			final int parentLevel = CHierarchyNavigationService.getEntityLevel(effectiveParent);
			final String dropHint = CHierarchyNavigationService.getEntityLevel(dropTarget) == childLevel
					? " (dropped on same level → using the target's parent)" : "";
			final String expectedRule = childLevel == -1
					? "Expected a non-leaf parent (level >= 0)."
					: childLevel == 0
							? "Expected no parent (root level)."
							: "Expected parent level %d.".formatted(childLevel - 1);
			throw new IllegalArgumentException(
					"Invalid drop%s: '%s' (level=%d) cannot be placed under '%s' (level=%d). %s"
							.formatted(dropHint, child.getName(), childLevel, effectiveParent.getName(), parentLevel, expectedRule));
		}
	}

	private CAbstractService<?> resolveService(final CProjectItem<?> entity) {
		final Class<?> entityClass = ProxyUtils.getUserClass(entity.getClass());
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		Check.notNull(serviceClass, "No service registered for hierarchy item type: " + entityClass.getSimpleName());
		final Object serviceBean = CSpringContext.getBean(serviceClass);
		Check.isTrue(serviceBean instanceof CAbstractService<?>,
				"Registered service does not extend CAbstractService: " + serviceClass.getSimpleName());
		return (CAbstractService<?>) serviceBean;
	}
}
